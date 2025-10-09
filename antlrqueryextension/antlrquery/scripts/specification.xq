
declare function local:get-fields(
    $type-mapping,
    $class-information
) as string*
{
    let $fields := $class-information?fields
    let $field-types := $class-information?field-types
    let $field-optionalities := $class-information?field-optionalities
    for $field at $i in $fields
        let $type := $field-types[$i]
        let $field-optionality := $field-optionalities[$i]
        let $accessed-type := $type-mapping?($type)
        let $used-type := if ($accessed-type=>exists()) then $accessed-type else $type
    return ``[`{$field}``{if ($field-optionality) then '?' else ''}`: `{$used-type}`]``
};
let $type-mapping := map {
    'LSPAny': 'lsp:Any',
    'LSPObject': 'lsp:Object'
}

let $structuredClassInformation := map:merge(
    for $class in //span[@class='kr' and text()='interface']
                    /following-sibling::span[@class='nx']
    let $extentions := $class/following-sibling::span[@class='kd' and text()='extends']
                            /following-sibling::span[@class='nx']
    let $fields := $class/following-sibling::span[@class='nl']
    return map {
        'class': $class,
        'extentions': $extentions,
        'fields': $fields,
        'field-types': (
            for $field in $fields
            return $field/following-sibling::span[2][@class='nx']
        ),
        'field-optionalities': (
            for $field in $fields
            return $field/following-sibling::span[1][@class='p'] = '?:'
        )
    }
)
let $extendable-classes := $structuredClassInformation?extentions
for $class-information in $structuredClassInformation
    let $class := $class-information?class
    let $class-name := $class-information/text()
    let $is-extendable := some $x in $extendable-classes satisfies $x is $class
    let $extentions := $class-information?extentions
    let $extended-fields := ($structuredClassInformation[$extentions = ?class] ! local:get-fields($type-mapping, .))
    let $class-fields := local:get-fields($type-mapping, $class-information)
    return ``[
declare record lsp: `{$class-name}`(
    `{fn:string-join(($extended-fields, $class-fields), ",\n    ")}`
    `{if ($is-extendable) then ', *' else ''}`
);
]``
