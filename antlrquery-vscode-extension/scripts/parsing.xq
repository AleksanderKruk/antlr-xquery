declare function parse($code) {
    analyze-string($code,
    ``[
        /\*\*? [^*]* \*+ (?:[^/*][^*]*\*+) */
        | // .*? \n
        | " .*? "
        | ' .*? '
        | < .*? >
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
        | \|
        | [a-zA-Z_$][\w$]*
    ]``,
    'x'
    )/match[fn:normalize-space() => fn:starts-with("/*") => not()
            and fn:normalize-space() => fn:matches("^//") => not()
            and fn:normalize-space() => fn:starts-with("<") => not()]
    /string()
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
    let $types := ($primaryType?type, $rest?types)
    let $is-nullable := (($primaryType?is-nullable, $rest?is-nullable) = true())
    return map {
        "next-index": if ($is-extended)
            then $rest?next-index
            else $next-index,
        "type": (($types => string-join("|")) !
           (if ($types=>count() gt 1) then `({.})` else .)) 
           || (if ($is-nullable) {'?'}),
        "types": $types,
        "is-nullable": $is-nullable
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
    return if ($is-array)
        then 
        let $next-char := $contents[$next-index+2]
        return map {
            "next-index": $next-index + 2,
            "type": `array({$arrayBaseType?type})`,
            "is-nullable": $arrayBaseType?is-nullable
        }
        else 
        let $next-char := $contents[$next-index]
        return map {
            "next-index": $next-index,
            "type": $arrayBaseType?type,
            "is-nullable": $arrayBaseType?is-nullable
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
        case "uinteger" return "number"
        case "string" return "string"
        case "true" return "boolean"
        case "false" return "boolean"
        case "boolean" return "boolean"
        case "object" return "map(*)"
        case "array" return "array(*)"
        case "LSPAny" return "lsp:LSPAny?"
        case "T" return "item()*"
        default return `lsp:{$type}`
};



declare function arrayBaseType($contents, $i) {
    switch($contents[$i])
        case 'null' return map {
            "type": (),
            "next-index": $i+1,
            "is-nullable": true()
        }
        case '(' return
            let $type := type($contents, $i+1)
            return map {
                "next-index": $type?next-index+1,
                "type": $type?is-nullable
            }
        case '{' return
            let $type := typeBody($contents, $i+1)
            let $next := $contents[$type?next-index+1]
            return map {
                "next-index": $type?next-index+1,
                "type": $type?is-nullable
            }
        case '[' return
            let $type := tupleElementTypes($contents, $i+1)
            return map {
                "next-index": $type?next-index,
                "type": $type?is-nullable,
                "is-nullable": false()
            }
        default return
            if ($contents[$i] => fn:matches("^[+-]?\d+$")) then
                map {
                    "next-index": $i?next-index+1,
                    "type": "number",
                    "is-nullable": false()
                }
            else if ($contents[$i]=>fn:starts-with("'")) then
                map {
                    "next-index": $i+1,
                    "type": `enum({$contents[$i]})`,
                    "is-nullable": false()
                }

            else
                map {
                    "next-index": $i+1,
                    "type": $contents[$i]=>resolve-type(),
                    "is-nullable": false()
                }
};

(: 
tupleElementTypes
    : type_ (',' type_)* ','?
    ;                            
:)
declare function tupleElementTypes($parsed, $i) {
    let $t := type($parsed, $i)
    let $next-index := $t?next-index
    let $more-types-ahead := $parsed[$next-index] != ']'
    let $rest := if ($more-types-ahead) {
        type($parsed, $next-index+1)
    }
    let $types := distinct-values(($t?type, $rest?types))
    return map {
        "next-index": if ($more-types-ahead) then $rest?next-index else $next-index + 1,
        "type": `array({($types => string-join(" | ")) ! (if (count($types) > 1) then `({.})` else .)})`,
        "types": $types,
        "is-nullable": $types=>filter(fn:boolean#1)=>exists()
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
    let $next:= $contents[$typeMemberList?next-index]
    return map {
        "type": `record(
            {$typeMemberList?fields => string-join(",&#10;    ")
        })`,
        "next-index": $next-index+1,
        "is-nullable": $typeMemberList?is-nullable
    }
};

(:
typeMemberList
    : typeMember ((SemiColon | ',') typeMember)* (SemiColon | ',')?
    ;
:)
declare function typeMemberList($contents, $i) {
    let $typeMember := typeMember($contents, $i)
    return if ($typeMember?type=>empty())
        then map {
            "fields": (),
            "next-index": $typeMember?next-index
        }
        else
        let $next-index := $typeMember?next-index
        let $more-fields-ahead := $contents[$next-index] = (',', ';') and $contents[$next-index+1] != '}'
        let $rest := if ($more-fields-ahead) 
            { typeMemberList($contents, $next-index+1) }
        let $typeMemberList := ($typeMember?type, $rest?fields)
        let $is-nullable := ($typeMember?is-nullable, $rest?is-nullable) = true()
        return map {
            "fields": $typeMemberList,
            "next-index": if ($more-fields-ahead)
                then $rest?next-index
                else $next-index,
            "is-nullable": $is-nullable

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
            case "}" return map {
                "next-index": $i,
                "type": (),
                "is-nullable": fn:false()
            }
            case "[" return
                let $field-name := $contents[$i+1]
                let $key-type := $contents[$i+3]
                let $type := type($contents, $i+6)
                let $next := $contents[position() = ($type?next-index - 1, $type?next-index, $type?next-index+1)]
                return map {
                    "next-index": $type?next-index,
                    "type": `{$field-name} as map({$key-type=>resolve-type()}, {$type?type})`,
                    "is-nullable": fn:false()
                }
            case "readonly" return
                let $field-name := $contents[$i+1]
                let $optionality := $contents[$i+2] = '?'
                let $type := type($contents, if ($optionality) then $i+4 else $i+3)
                return map {
                    "next-index": $type?next-index,
                    "type": `{$field-name}{if ($optionality) {'?'}} as {$type?type}`,
                    "is-nullable": fn:false()
                }
            default return
                let $field-name := $contents[$i]
                let $optionality := $contents[$i+1] = '?'
                let $type := type($contents, if ($optionality) then $i+3 else $i+2)
                return map {
                    "next-index": $type?next-index,
                    "type": `{$field-name}{if ($optionality) {'?'}} as {$type?type}`,
                    "is-nullable": fn:false()
                }

    }
};


declare function local:getClass($parsed) {
    let $interfaceindex := index-of($parsed, 'interface')[1]
    let $classname := $parsed[$interfaceindex + 1]
    let $classbraceindex := index-of($parsed, '{')[1]
    let $extended-seps := (
        index-of($parsed, 'extends')[. le $classbraceindex][1], 
        index-of($parsed, ',')[. le $classbraceindex]
        )
    let $extended-names := for $i in $extended-seps return $parsed[$i+1]
    let $fields := typeMemberList($parsed, $classbraceindex+1)
    return if ($classname=>exists() and $classbraceindex gt $interfaceindex) {
        map {
            "class": $classname,
            "fields": $fields?fields,
            "extended": array{ $extended-names }
        }
    }
};

declare function local:getType($parsed) {
    let $typeindex := index-of($parsed, 'type')[1]
    let $typename := $parsed[$typeindex + 1]
    let $eq := $parsed[$typeindex + 2] = '='
    return if (count(($typeindex, $typename, $eq)) eq 3) {
        let $type := type($parsed, $typeindex+3)
        return if ($type?type) {
            map {
                "typename": $typename,
                "type": $type?type

            }
        }
    }
};

declare function generate-classes($codes) {
    let $classes := (
        for $code in $codes
        let $parsed := $code=>parse()
        let $c := local:getClass($parsed)
        return $c
    )
    let $extendable-classes := $classes?extended=>distinct-values()
    for $c in $classes
    let $extended-classes := $classes[?class = $c?extended]
    let $field-separator := '&#10;    ' 
    let $field-separator-with-comma := ',&#10;    ' 
    let $extension-fields := (
        for $e in $extended-classes 
        return if ($e?fields=>exists()) {
            let $e-name := $e?class=>resolve-type()
            let $preamble := '(: '|| $e-name ||' :)' || $field-separator
            return ($preamble||($e?fields=>head()), $e?fields=>tail())
        }
    )
    let $class-fields := 
        if ($extended-classes=>exists() and $c?fields=>exists())
            then ('(: '||$c?class=>resolve-type()||' :)'|| $field-separator ||fn:head($c?fields), fn:tail($c?fields))
            else $c?fields
    let $star := if ($extendable-classes = $c?class) { '*' }
    return ``[
declare record lsp:`{$c?class}`(
    `{string-join(($extension-fields, $class-fields, $star), $field-separator-with-comma)}`
);]``
};


declare function generate-types($codes) {
    let $types := (
        for $code in $codes
        let $parsed := $code=>parse()
        let $t := local:getType($parsed)
        return $t
    )
    for $type in $types
    return ``[
declare type lsp:`{$type?typename}` as `{$type?type}`;]``
};
(: generate-types(//code), :)
generate-classes(//code)