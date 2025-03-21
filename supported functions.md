# Function index


## Operators
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`op:numeric-add`                                   | yes            |
`op:numeric-subtract`                              | yes            |
`op:numeric-multiply`                              | yes            |
`op:numeric-divide`                                | yes            |
`op:numeric-integer-divide`                        | yes            |
`op:numeric-mod`                                   | yes            |
`op:numeric-unary-plus`                            | yes            |
`op:numeric-unary-minus`                           | yes            |
`op:numeric-equal`                                 | yes            |
`op:numeric-less-than`                             | yes            |
`op:numeric-greater-than`                          | yes            |
`op:boolean-equal`                                 |                |
`op:boolean-less-than`                             |                |
`op:boolean-greater-than`                          |                |
`op:hexBinary-equal`                               |                |
`op:hexBinary-less-than`                           |                |
`op:hexBinary-greater-than`                        |                |
`op:base64Binary-equal`                            |                |
`op:base64Binary-less-than`                        |                |
`op:base64Binary-greater-than`                     |                |
`op:NOTATION-equal`                                |                |
`op:same-key`                                      |                |


## String functions
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:string`                                        | yes            |
`fn:codepoints-to-string`                          |                |
`fn:string-to-codepoints`                          |                |
`fn:compare`                                       |                |
`fn:codepoint-equal`                               |                |
`fn:collation-key`                                 |                |
`fn:contains-token`                                |                |
`fn:concat`                                        | yes            |
`fn:string-join`                                   | yes            |
`fn:string-length`                                 | yes            |
`fn:normalize-space`                               | yes            |
`fn:normalize-unicode`                             |                |
`fn:upper-case`                                    | yes            |
`fn:lower-case`                                    | yes            |
`fn:translate`                                     |                |
`fn:contains`                                      | yes            |
`fn:starts-with`                                   | yes            |
`fn:ends-with`                                     | yes            |
`fn:substring`                                     | yes            |
`fn:substring-before`                              | yes            |
`fn:substring-after`                               | yes            |
`fn:matches`                                       |                |
`fn:replace`                                       |                |
`fn:tokenize`                                      |                |
`fn:analyze-string`                                |                |
`fn:format-integer`                                |                |
`fn:format-number`                                 |                |
`fn:format-dateTime`                               |                |
`fn:format-date`                                   |                |
`fn:format-time`                                   |                |
`fn:serialize`                                     |                |
`fn:default-collation`                             |                |
`fn:default-language`                              |                |


## Node functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:node-name`                                     |                |
`fn:has-children`                                  |                |
`fn:name`                                          |                |
`fn:local-name`                                    |                |
`fn:lang`                                          |                |
`fn:id`                                            |                |
`fn:element-with-id`                               |                |
`fn:idref`                                         |                |

## Sequence functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:empty`                                         | yes            |
`fn:exists`                                        | yes            |
`fn:head`                                          | yes            |
`fn:tail`                                          | yes            |
`fn:insert-before`                                 | yes            |
`fn:remove`                                        | yes            |
`fn:reverse`                                       | yes            |
`fn:subsequence`                                   | yes            |
`fn:unordered`                                     | yes            |
`fn:distinct-values`                               | yes            |
`fn:innermost`                                     |                |
`fn:outermost`                                     |                |
`fn:index-of`                                      |                |
`fn:deep-equal`                                    |                |
`fn:zero-or-one`                                   |                |
`fn:one-or-more`                                   |                |
`fn:exactly-one`                                   |                |
`fn:position`                                      |                |
`fn:last`                                          |                |
`fn:data`                                          |                |


## Aggregate functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:count`                                         |                |
`fn:avg`                                           |                |
`fn:max`                                           |                |
`fn:min`                                           |                |
`fn:sum`                                           |                |

## Math functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:number`                                        |                |
`fn:abs`                                           | yes            |
`fn:ceiling`                                       | yes            |
`fn:floor`                                         | yes            |
`fn:round`                                         | yes            |
`fn:round-half-to-even`                            |                |
`math:pi`                                          | yes            |
`math:exp`                                         |                |
`math:exp10`                                       |                |
`math:log`                                         |                |
`math:log10`                                       |                |
`math:pow`                                         |                |
`math:sqrt`                                        |                |
`math:sin`                                         |                |
`math:cos`                                         |                |
`math:tan`                                         |                |
`math:asin`                                        |                |
`math:acos`                                        |                |
`math:atan`                                        |                |
`math:atan2`                                       |                |


## Array functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`array:size`                                       |                |
`array:get`                                        |                |
`array:put`                                        |                |
`array:append`                                     |                |
`array:subarray`                                   |                |
`array:remove`                                     |                |
`array:insert-before`                              |                |
`array:head`                                       |                |
`array:tail`                                       |                |
`array:reverse`                                    |                |
`array:join`                                       |                |
`array:for-each`                                   |                |
`array:filter`                                     |                |
`array:fold-left`                                  |                |
`array:fold-right`                                 |                |
`array:for-each-pair`                              |                |
`array:sort`                                       |                |
`array:flatten`                                    |                |

## Map functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`map:merge`                                        |                |
`map:size`                                         |                |
`map:keys`                                         |                |
`map:contains`                                     |                |
`map:get`                                          |                |
`map:find`                                         |                |
`map:put`                                          |                |
`map:entry`                                        |                |
`map:remove`                                       |                |
`map:for-each`                                     |                |

## Boolean functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:true`                                          | yes            |
`fn:false`                                         | yes            |
`fn:boolean`                                       |                |
`fn:not`                                           | yes            |


## Input functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:unparsed-text`                                 |                |
`fn:unparsed-text-lines`                           |                |
`fn:unparsed-text-available`                       |                |

## Output functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:error`                                         |                |
`fn:trace`                                         |                |

## Output format functions
`fn:parse-xml`                                     |                |
`fn:parse-xml-fragment`                            |                |
`fn:parse-json`                                    |                |
`fn:xml-to-json`                                   |                |
`fn:json-to-xml`                                   |                |
`fn:xml-to-json`                                   |                |
`fn:json-doc`                                      |                |


## Id functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:id`                                            |                |
`fn:element-with-id`                               |                |
`fn:idref`                                         |                |
`fn:generate-id`                                   |                |

## Path functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:root`                                          |                |
`fn:path`                                          |                |


## Higher order functions
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:function-lookup`                               |                |
`fn:function-name`                                 |                |
`fn:function-arity`                                |                |
`fn:for-each`                                      |                |
`fn:filter`                                        |                |
`fn:fold-left`                                     |                |
`fn:fold-right`                                    |                |
`fn:for-each-pair`                                 |                |
`fn:sort`                                          |                |
`fn:apply`                                         |                |


## Varia

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:nilled`                                        |                |
`fn:random-number-generator`                       |                |

## Environment Variables

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:environment-variable`                          |                |
`fn:available-environment-variables`               |                |


## Document functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:doc`                                           |                |
`fn:doc-available`                                 |                |

## URI

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:base-uri`                                      |                |
`fn:document-uri`                                  |                |
`fn:resolve-uri`                                   |                |
`fn:encode-for-uri`                                |                |
`fn:iri-to-uri`                                    |                |
`fn:escape-html-uri`                               |                |
`fn:namespace-uri`                                 |                |
`fn:namespace-uri-for-prefix`                      |                |
`fn:in-scope-prefixes`                             |                |
`fn:static-base-uri`                               |                |
`fn:collection`                                    |                |
`fn:uri-collection`                                |                |


## Time, Dates, Datetimes
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:current-dateTime`                              |                |
`fn:current-date`                                  |                |
`fn:current-time`                                  |                |
`fn:years-from-duration`                           |                |
`fn:months-from-duration`                          |                |
`fn:days-from-duration`                            |                |
`fn:hours-from-duration`                           |                |
`fn:minutes-from-duration`                         |                |
`fn:seconds-from-duration`                         |                |
`fn:dateTime`                                      |                |
`fn:year-from-dateTime`                            |                |
`fn:month-from-dateTime`                           |                |
`fn:day-from-dateTime`                             |                |
`fn:hours-from-dateTime`                           |                |
`fn:minutes-from-dateTime`                         |                |
`fn:seconds-from-dateTime`                         |                |
`fn:timezone-from-dateTime`                        |                |
`fn:year-from-date`                                |                |
`fn:month-from-date`                               |                |
`fn:day-from-date`                                 |                |
`fn:timezone-from-date`                            |                |
`fn:hours-from-time`                               |                |
`fn:minutes-from-time`                             |                |
`fn:seconds-from-time`                             |                |
`fn:timezone-from-time`                            |                |
`fn:adjust-dateTime-to-timezone`                   |                |
`fn:adjust-date-to-timezone`                       |                |
`fn:adjust-time-to-timezone`                       |                |
`fn:parse-ietf-date`                               |                |
`fn:implicit-timezone`                             |                |


## Other
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:lang`                                          |                |
`fn:serialize`                                     |                |
`fn:default-language`                              |                |
`fn:load-xquery-module`                            |                |
`fn:transform`                                     |                |
`op:yearMonthDuration-less-than`                   |                |
`op:yearMonthDuration-greater-than`                |                |
`op:dayTimeDuration-less-than`                     |                |
`op:dayTimeDuration-greater-than`                  |                |
`op:duration-equal`                                |                |
`op:add-yearMonthDurations`                        |                |
`op:subtract-yearMonthDurations`                   |                |
`op:multiply-yearMonthDuration`                    |                |
`op:divide-yearMonthDuration`                      |                |
`op:divide-yearMonthDuration-by-yearMonthDuration` |                |
`op:add-dayTimeDurations`                          |                |
`op:subtract-dayTimeDurations`                     |                |
`op:multiply-dayTimeDuration`                      |                |
`op:divide-dayTimeDuration`                        |                |
`op:divide-dayTimeDuration-by-dayTimeDuration`     |                |
`op:dateTime-equal`                                |                |
`op:dateTime-less-than`                            |                |
`op:dateTime-greater-than`                         |                |
`op:date-equal`                                    |                |
`op:date-less-than`                                |                |
`op:date-greater-than`                             |                |
`op:time-equal`                                    |                |
`op:time-less-than`                                |                |
`op:time-greater-than`                             |                |
`op:gYearMonth-equal`                              |                |
`op:gYear-equal`                                   |                |
`op:gMonthDay-equal`                               |                |
`op:gMonth-equal`                                  |                |
`op:gDay-equal`                                    |                |
`op:subtract-dateTimes`                            |                |
`op:subtract-dates`                                |                |
`op:subtract-times`                                |                |
`op:add-yearMonthDuration-to-dateTime`             |                |
`op:add-dayTimeDuration-to-dateTime`               |                |
`op:subtract-yearMonthDuration-from-dateTime`      |                |
`op:subtract-dayTimeDuration-from-dateTime`        |                |
`op:add-yearMonthDuration-to-date`                 |                |
`op:add-dayTimeDuration-to-date`                   |                |
`op:subtract-yearMonthDuration-from-date`          |                |
`op:subtract-dayTimeDuration-from-date `           |                |
`op:add-dayTimeDuration-to-time`                   |                |
`op:subtract-dayTimeDuration-from-time`            |                |
