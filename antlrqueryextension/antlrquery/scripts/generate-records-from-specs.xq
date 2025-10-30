

declare function local:get-fields(
    $type-mapping,
    $class-information
) as xs:string*
{
    let $fields := $class-information ? 'fields'
    let $field-types := $class-information ? 'field-types'
    let $field-optionalities := $class-information ? 'field-optionalities'
    for $field at $i in $fields
        let $type as xs:string? := $field-types[$i]
        let $field-optionality := $field-optionalities[$i]
        return `{$field}{if ($field-optionality) { '?' }} as {$type}`
};



declare function local:parse-type(
    $type-mapping,
    $type
) as xs:string*
{
    (: let $is-record := $colon/following-sibling::*[1][normalize-space() = '[];']
    let $s := switch($colon/following-sibling::*[1])
        case '({' return ()
        case '{' return()
        default return $colon/following-sibling::* :)


    let $is-array := $type/following-sibling::*[1][normalize-space() = '[];']
    let $accessed-type as xs:string? := $type-mapping?($type)
    let $used-type as xs:string := if ($accessed-type=>exists())
        then $accessed-type
        else 'lsp:'||$type
    let $arrayed-type := if ($is-array) 
        then `array({$used-type})` 
        else $used-type
    return $arrayed-type
};




let $type-mapping := map {
    'LSPAny': 'lsp:Any',
    'LSPObject': 'lsp:Object',
    'uinteger': 'number',
    'integer': 'number',
    'decimal': 'number',
    'boolean': 'boolean',
    'string': 'string'
}
let $structuredClassInformation := (
    for $class as node()? in //span[@class='kr' and text()='interface']
                                /following-sibling::span[@class='nx'][1]
    let $curly as node()? := $class/following-sibling::span[@class='p' and string()='{'][1]
    let $extentions as node()* := $class
        /following-sibling::span[@class='kd' and text()='extends']
        /following-sibling::span[@class='nx'][. << $curly]
    let $fields as node()* := $class/following-sibling::span[@class='nl']
                            | $class/following-sibling::span[normalize-space() = 'readonly']/following-sibling::*[1]
    
    return map {
        'class': $class,
        'extentions': $extentions,
        'fields': $fields,
        'field-types': (
            (
                for $field in $fields
                    let $type := $field/following-sibling::span[2][@class=('nx', 'kr')]
                    let $is-record := $type=>exists()
                    let $used-type := local:parse-type($type-mapping, $type)
                    return $used-type
            )
        ),
        'field-optionalities': (
            for $field in $fields
            return $field/following-sibling::span[1][@class='p'] = '?:'
        )
    }
)
let $extendable-classes := $structuredClassInformation?extentions
for $class-information in $structuredClassInformation[?fields=>count() = ?field-types=>count()]
    let $class as node() := $class-information?class
    let $class-name := $class/string()
    let $is-extendable := $extendable-classes = $class
    let $extentions := $class-information?extentions
    let $extended-fields := $structuredClassInformation[$extentions = ?class]
      ! local:get-fields($type-mapping, .)
    let $class-fields := local:get-fields($type-mapping, $class-information)
    return ``[
declare record lsp:`{$class-name}`(
    `{fn:string-join(($extended-fields, $class-fields, if ($is-extendable) { '*' }), ",
    ")}`
);
]``
