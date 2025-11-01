declare function parse($code) {
    analyze-string($code,
    ``[
        /\*\*[^*]*\*+(?:[^/*][^*]*\*+)*/
        | // .*? \n
        | " .*? "
        | ' .*? '
        | \?
        | :
        | ,
        | ;
        | \{
        | \}
        | \(
        | \)
        | \[
        | \]
        | [a-zA-Z_$][\w$]*
    ]``,
    'x'
    )/match[fn:normalize-space() => fn:starts-with("/**") => not()]
};


declare function local:map-parentheses(
    $start
)
{
    map:merge(
        let $closing := $start[normalize-space() = ('}', ')', ']')]
        let $opening := $start[normalize-space() = ('{', '(', '[')]
        for $o at $oi in $opening
        for $c at $ci in $closing=>reverse()
        return if ($o=>exists() and $c => exists()) {
            if ($opening=>count() - $oi + 1 = $ci) {
                map {
                    $o=>generate-id(): map {
                        "bracket": $c=>generate-id(),
                        "contents": $o/following-sibling::match[. << $c and not(starts-with(., '/*'))] ! string(.)
                    }
                }
            }
        }
    )
};


(:
type:
    primaryType ('|' primaryType)*;
:)
declare function type($contents, $i) {
    let $primaryType := primaryType($contents, $i)
    let $next-index := $primaryType?next-index
    let $is-extended := $contents[$next-index] = '|'
    let $rest := if ($is-extended) {
        type($contents, $next-index+1)
    }
    return map {
        "next-index": if ($is-extended)
            then $rest?next-index
            else $next-index,
        "type": ((($primaryType?type, $rest?types) => string-join("|")) !
           (if ($is-extended) then '('||.||')' else .)),
        "types": ($primaryType?type, $rest?types)
    }
};

(:
primaryType:
    arrayBaseType ('[' ']')?
    ;
:)

declare function primaryType($contents, $i) {
    let $arrayBaseType := arrayBaseType($contents, $i)
    let $next-index := $arrayBaseType?next-index
    let $is-array := $contents[$next-index] = '['

    return if ($contents[$next-index] = '[')
        then map {
            "next-index": $next-index + 2,
            "type": `array({$arrayBaseType?type})`
        }
        else map {
            "next-index": $next-index,
            "type": $arrayBaseType?type
        }
};

(:
arrayBaseType:
    NullLiteral
    | Number
    | Boolean
    | String
    | Object
    | BooleanLiteral

    | DecimalLiteral
    | StringLiteral


    | typeName

    : '(' type ')'                              # parenthesized

    | '{' typeBody? '}'                          # objectType

:)


declare function resolve-type($type) {
    switch($type)
    case "integer" return "number"
    case "true" return "boolean"
    case "false" return "boolean"
    default return `lsp:{$type}`
};



declare function arrayBaseType($contents, $i) {
    switch($contents[$i])
        case 'null' return map {
            "next-index": $i+1,
            "is-nullable": true()
        }
        case 'true' return map {
            "next-index": $i+1,
            "type": "boolean",
            "is-nullable": false()
        }
        case 'false' return map {
            "next-index": $i+1,
            "type": "boolean",
            "is-nullable": false()
        }
        case 'object' return map {
            "next-index": $i+1,
            "type": "map(*)",
            "is-nullable": false()
        }
        case '(' return
            let $type := type($contents, $i+1)
            return map {
                "next-index": $type?next-index+1,
                "type": $type?type
            }
        case '{' return
            let $type := typeBody($contents, $i+1)
            return map {
                "next-index": $type?next-index+1,
                "type": $type?type
            }
        default return
            if ($contents[$i] => fn:matches("^[+-]?\d+$")) then
                map {
                    "next-index": $i?next-index+1,
                    "type": "number"
                }
            else if ($contents[$i]=>fn:starts-with("'")) then
                map {
                    "next-index": $i+1,
                    "type": `enum({$contents[$i]=>substring(2, $contents[$i]=>string-length())})`
                }

            else
                map {
                    "next-index": $i+1,
                    "type": $contents[$i]=>resolve-type()
                }
};

(:
typeBody
    : typeMemberList
    ;
:)

declare function typeBody($contents, $i) {
    let $typeMemberList := typeMemberList($contents, $i)
    let $next-index := $typeMemberList?next-index
    return map {
        "type": `record(
            {$typeMemberList?fields ! string-join(., ",&#10;    ")
        })`,
        "next-index": $next-index
    }
};

(:
typeMemberList
    : typeMember ((SemiColon | ',') typeMember)* (SemiColon | ',')?
    ;
:)
declare function typeMemberList($contents, $i) {
    let $typeMember := typeMember($contents, $i)
    let $next-index := $typeMember?next-index
    let $rest := if ($contents[$next-index] = (',', ';')) { typeMemberList($contents, $next-index+1) }
    let $typeMemberList := ($typeMember?type, $rest?fields)
    return map {
        "fields": $typeMemberList,
        "next-index": if ($contents[$next-index] = (',', ';'))
            then $rest?next-index
            else $next-index

    }
};


(:
typeMember
    :  ReadOnly? propertyName '?'? ':' type
    | '[' identifier ':' (Number | String) ']' ':' type
    ;
:)
declare function typeMember($contents, $i) {
    if ($contents[$i]=>exists()) {
        switch($contents[$i])
            case "[" return
                let $field-name := $contents[$i+1]
                let $key-type := $contents[$i+2]
                let $type := type($contents, $i+5)
                return map {
                    "next-index": $type?next-index,
                    "type": `{$field-name} as map({$key-type}, {$type?type})`
                }
            case "readonly" return
                let $field-name := $contents[$i+1]
                let $optionality := $contents[$i+2] = '?'
                let $type := type($contents, if ($optionality) then $i+4 else $i+3)
                return map {
                    "next-index": $type?next-index,
                    "type": `{$field-name}{if ($optionality) {'?'}} as {$type?type}`
                }
            default return
                let $field-name := $contents[$i]
                let $optionality := $contents[$i+1] = '?'
                let $type := type($contents, if ($optionality) then $i+3 else $i+2)
                return map {
                    "next-index": $type?next-index,
                    "type": `{$field-name}{if ($optionality) {'?'}} as {$type?type}`
                }

    }
};


declare function local:getClass($parsed, $mapped-parentheses) {
    let $classname := $parsed[normalize-space() = 'interface']
        /following-sibling::match[1]
    let $classbrace := $parsed[normalize-space() = '{'][1]
    let $extended-names := $parsed[normalize-space() = ('extends', ',') and . << $classbrace]
        /following-sibling::match[1]
    let $contents := $mapped-parentheses ? ($classbrace=>generate-id()) ? contents
    let $fields := typeMemberList($contents, 1)
    return if ($classname) {map {
        "class": $classname,
        "fields": $fields?fields,
        "extended": array{ $extended-names },
        "class-contents": $contents
    }}
};

let $classes := (
    for $code in //code
    let $parsed := $code=>parse()
    let $parens := local:map-parentheses($parsed)
    let $c := local:getClass($parsed, $parens)
    return $c
)
let $extendable-classes := $classes?extended=>distinct-values()
for $c in $classes
let $extended-classes := $classes[?class = $c?extended]
return ``[
declare record lsp:`{$c?class}`(
    `{string-join(
        ( for $e in $extended-classes return ('(: '||$e?class=>resolve-type()||' :)'|| '&#10;    ' ||`{$e?fields}`),
          if ($extended-classes=>exists()) {'(: '||$c?class=>resolve-type()||' :)'}, $c?fields,
          if ($extendable-classes = $c?class) { '*' }
        ), ",&#10;    ")
    }`
)]``


(: for $code in (
    ``[
/**
 * A versioned notebook document identifier.
 *
 * @since 3.17.0
 */
export interface VersionedNotebookDocumentIdentifier {

    /**
     * The version number of this notebook document.
     */
    version: integer;

    /**
     * The notebook document's URI.
     */
    uri: URI;
}
    ]``
)
let $parsed := $code=>parse()
let $parens := local:map-parentheses($parsed)
return local:getClass($parsed, $parens) :)