
declare function local:get-data($el as node()?, $data-name as xs:string) as node()?
{
  $el//dt[text() = $data-name]/following-sibling::*[1]
};
declare option output:method "xml";
declare option output:indent "yes";
declare option output:omit-xml-declaration "yes";

for $function-documentation in //div[h4[not(starts-with(text(), '1'))] and .//dt = 'Signature']
let $function-name := $function-documentation/h4/a[@href]/text()
let $function-summary := local:get-data($function-documentation, 'Summary') => normalize-space()
let $function-properties := local:get-data($function-documentation, 'Properties') => normalize-space()
let $function-signature := local:get-data($function-documentation, 'Signature')//text() => string-join(" ") => normalize-space() 
(: return ``[`{string-join(($function-name, $function-summary, $function-properties, $function-signature), ",")}`]`` :)
return 
<function>
  <function-name>
    { $function-name }
  </function-name>
  <function-summary>
    { $function-summary }
  </function-summary>
  <function-properties>
    { $function-properties }
  </function-properties>
  <function-signature>
    { $function-signature }
  </function-signature>
</function>