
declare option output:indent "yes";




let $structuredClassInformation := (  for $class as node()? in //span[@class='kr' and text()='interface']
                    /following-sibling::span[@class='nx'][1]
    let $curly as node()? := $class/following-sibling::span[@class='p' and string()='{'][1]
    let $extentions as node()* := $class/following-sibling::span[@class='kd' and text()='extends']
                            /following-sibling::span[@class='nx'][. << $curly]
    let $fields as node()* := $class/following-sibling::span[@class='nl']
    return map {
        'class': $class,
        'extentions': $extentions,
        'fields': $fields,
        'field-types': (
            (
              for $field in $fields
              return $field/following-sibling::span[2][@class=('nx', 'kr')]
            )
        ),
        'field-optionalities': (
            for $field in $fields
            return $field/following-sibling::span[1][@class='p'] = '?:'
        )
    }
 )
 
return $structuredClassInformation[?fields=>count() != ?field-types=>count()]