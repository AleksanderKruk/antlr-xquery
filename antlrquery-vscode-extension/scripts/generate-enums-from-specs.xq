

for $enum in //span[string()=>normalize-space()='export']
              /following-sibling::span[1][string()=>normalize-space()='namespace']
              /following-sibling::span[1][@class='nx']
let $enclosing-curly := $enum/following-sibling::*[string()=>normalize-space() = '}']
let $members := $enum/following-sibling::span[@class='kd' and string()='const']/following-sibling::span[@class='nx'][1]
let $values := ( for $m in $enum/following-sibling::span[@class='o' and string()='='][. << $enclosing-curly]
                 let $semicolon := $m/following-sibling::*[string()=';'][1]
                 let $value-parts := $m/following-sibling::*[. << $semicolon]
                 let $merged-parts := $value-parts=>string-join()
                 return $merged-parts )
let $is-numeric := $enum/following-sibling::span[@class='o' and string()='='][1]/(following-sibling::*[1][@class='mi']|following-sibling::*[2][@class='mi']) => exists()
let $is-extensible := $enum/((following-sibling::* | preceding-sibling::*)[string()=>normalize-space()='export'])
                        /following-sibling::span[1][string()=>normalize-space()='type']
                        /following-sibling::span[1][string()=>normalize-space() = $enum=>string()=>normalize-space()]
                        /following-sibling::span[@class='o' and string()=>normalize-space()='='][1]
                        /following-sibling::*[string()=>normalize-space() = '|'] => fn:empty()
return if ($is-numeric)
  then `declare type {$enum} as number;`
  else if ($is-extensible)
    then `declare type {$enum} as string;`
    else `declare type {$enum} as enum(
    {$values => string-join(",
    ")

});`
