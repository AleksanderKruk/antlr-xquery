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
                        "contents": $o/following-sibling::match[. << $c and not(starts-with(., '/*'))]
                    }
                }
            }
        }
    )
};

declare function local:get-fields(
    $contents,
    $mapped-parentheses,
    $i
) 
{
    local:parse-field($contents, $mapped-parentheses, $i)
};



declare function local:parse-field(
    $contents,
    $mapped-parentheses,
    $i
) 
{
    let $field-name := $contents[.!='readonly'][$i]
    let $optionality := ($contents[$i+1], $contents[$i+2]) = '?'
    let $colonindex := index-of($contents[$i to count($contents)], ':')[1]
    let $type-parsed := local:parse-type($contents, $colonindex+1)
    let $current-field := map {
            "name": $field-name,
            "optionality": $optionality,
            "type": $type-parsed?type
        }
    let $next-index := $type-parsed?index
    let $next-field := if ($contents[$next-index] = ',') { local:parse-field($contents, $mapped-parentheses, $next-index + 1) }
    return ($current-field, $next-field)
};


declare function local:parse-type(
    $contents,
    $mapped-parentheses,
    $i
) 
{
    switch($contents[$i])
        case "(" return 
        case "{" return
        case "[" return local:parse-tuple($contents, $i+1)
        default return local:parse-alternative($i)
            if ($contents[$i] => starts-with("'"))
                then 
                else 

};






declare function local:getClass($parsed, $mapped-parentheses) {
    let $classname := $parsed[normalize-space() = 'interface']
        /following-sibling::match[1]
    let $classbrace := $parsed[normalize-space() = '{'][1]
    let $extended-names := $parsed[normalize-space() = ('extends', ',') and . << $classbrace]
        /following-sibling::match[1]
    let $contents := $mapped-parentheses ? ($classbrace=>generate-id()) ? contents 
    let $fields := local:get-fields($contents, 1)
    return if ($classname) {map {
        "class": $classname,
        "fields": $classname,
        "extended": array{ $extended-names },
        "class-contents": $contents
    }}
};

for $code in //code
let $parsed := $code=>parse()
let $parens := local:map-parentheses($parsed)
return local:getClass($parsed, $parens)
(: return getClass($code) :)