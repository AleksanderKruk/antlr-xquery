# Function index

## Processing sequences

### General

Function                                           | Is implemented      | Comment
---------------------------------------------------|---------------------|---------
`fn:empty`                                         | &#10004;            |
`fn:exists`                                        | &#10004;            |
`fn:foot`                                          | &#10004;            |
`fn:head`                                          | &#10004;            |
`fn:identity`                                      | &#10004;            |
`fn:insert-before`                                 | &#10004;            |
`fn:items-at`                                      | &#10004;            |
`fn:remove`                                        | &#10004;            |
`fn:replicate`                                     | &#10004;            |
`fn:reverse`                                       | &#10004;            |
`fn:sequence-join`                                 | &#10004;            |
`fn:slice`                                         | &#10004;            |
`fn:subsequence`                                   | &#10004;            |
`fn:unordered`                                     | &#10006;            | Adding sets will probably be a better idea than a separate function for unordered sequences
`fn:void`                                          | &#10004;            |

### Sequence Comparisons

Function                                           | Is implemented      | Comment
---------------------------------------------------|---------------------|---------
`fn:atomic-equal`                                  |                     |
`fn:compare`                                       |                     |
`fn:contains-subsequence`                          |                     |
`fn:deep-equal`                                    |                     |
`fn:distinct-values`                               |                     |
`fn:duplicate-values`                              |                     |
`fn:ends-with-subsequence`                         |                     |
`fn:index-of`                                      |                     |
`fn:starts-with-subsequence`                       |                     |


### Asserting Cardinality
Function                                           | Is implemented      | Comment
---------------------------------------------------|---------------------|---------
`fn:zero-or-one`                                   | &#10004;            |
`fn:one-or-more`                                   | &#10004;            |
`fn:exactly-one`                                   | &#10004;            |

### Aggregate functions
Function                                           | Is implemented      | Comment
---------------------------------------------------|---------------------|---------
`fn:count`                                         | &#10004;            |
`fn:all-equal`                                     | &#10004;            |
`fn:all-different`                                 | &#10004;            |
`fn:avg`                                           | &#10004;            |
`fn:max`                                           | &#10004;            |
`fn:min`                                           | &#10004;            |
`fn:sum`                                           | &#10004;            |


### Basic higher-order functions
Function                                           | Is implemented      | Comment
---------------------------------------------------|---------------------|---------
`fn:apply`                                         |                     |
`fn:do-until`                                      |                     |
`fn:every`                                         |                     |
`fn:filter`                                        |                     |
`fn:fold-left`                                     |                     |
`fn:fold-right`                                    |                     |
`fn:for-each`                                      |                     |
`fn:for-each-pair`                                 |                     |
`fn:highest`                                       |                     |
`fn:index-where`                                   |                     |
`fn:lowest`                                        |                     |
`fn:partial-apply`                                 |                     |
`fn:partition`                                     |                     |
`fn:scan-left`                                     |                     |
`fn:scan-right`                                    |                     |
`fn:some`                                          |                     |
`fn:sort`                                          |                     |
`fn:sort-by`                                       |                     |
`fn:sort-with`                                     |                     |
`fn:subsequence-where`                             |                     |
`fn:take-while`                                    |                     |
`fn:transitive-closure`                            |                     |
`fn:while-do`                                      |                     |


## Processing booleans
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:true`                                          | &#10004;       |
`fn:false`                                         | &#10004;       |
`fn:boolean`                                       | &#10004;       |
`fn:not`                                           | &#10004;       |


## Processing numerics

### Functions on numeric values
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:abs`                                           | &#10004;            |
`fn:ceiling`                                       | &#10004;            |
`fn:divide-decimals`                               | &#10004;            |
`fn:floor`                                         | &#10004;            |
`fn:is-NaN`                                        | &#10006;            | NaN values are not modelled
`fn:round`                                         | &#10004;            |
`fn:round-half-to-even`                            | &#10004;            |

### Parsing numbers
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:number`                                        |                |
`fn:parse-integer`                                 |                |

### Formatting numbers
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:format-integer`                                |                |
`fn:format-number`                                 |                |

### Trigonometric and exponential functions
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`math:pi`                                          | &#10004;       |
`math:e`                                           | &#10004;       |
`math:acos`                                        | &#10004;       |
`math:asin`                                        | &#10004;       |
`math:atan`                                        | &#10004;       |
`math:atan2`                                       | &#10004;       |
`math:cos`                                         | &#10004;       |
`math:cosh`                                        | &#10004;       |
`math:exp`                                         | &#10004;       |
`math:exp10`                                       | &#10004;       |
`math:log`                                         | &#10004;       |
`math:log10`                                       | &#10004;       |
`math:pow`                                         | &#10004;       |
`math:sin`                                         | &#10004;       |
`math:sinh`                                        | &#10004;       |
`math:sqrt`                                        | &#10004;       |
`math:tan`                                         | &#10004;       |
`math:tanh`                                        | &#10004;       |

### Random numbers
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:random-number-generator`                       |                |

## Operators
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`op:numeric-add`                                   | &#10004;       |
`op:numeric-subtract`                              | &#10004;       |
`op:numeric-multiply`                              | &#10004;       |
`op:numeric-divide`                                | &#10004;       |
`op:numeric-integer-divide`                        | &#10004;       |
`op:numeric-mod`                                   | &#10004;       |
`op:numeric-unary-plus`                            | &#10004;       |
`op:numeric-unary-minus`                           | &#10004;       |
`op:numeric-equal`                                 | &#10004;       |
`op:numeric-less-than`                             | &#10004;       |
`op:numeric-greater-than`                          | &#10004;       |
`op:boolean-equal`                                 | &#10004;       |
`op:boolean-less-than`                             | &#10004;       |
`op:boolean-greater-than`                          | &#10004;       |
`op:hexBinary-equal`                               |                |
`op:hexBinary-less-than`                           |                |
`op:hexBinary-greater-than`                        |                |
`op:base64Binary-equal`                            |                |
`op:base64Binary-less-than`                        |                |
`op:base64Binary-greater-than`                     |                |
`op:NOTATION-equal`                                |                |
`op:same-key`                                      |                |


## Processing strings

### Functions to assemble and disassemble strigns
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:codepoints-to-string`                          |                |
`fn:string-to-codepoints`                          |                |

### Comparison of strings
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:codepoint-equal`                               |                |
`fn:collation`                                     |                |
`fn:collation-available`                           |                |
`fn:collation-key`                                 |                |
`fn:contains-token`                                |                |

### Functions on string values
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:char`                                          | &#10004;       |
`fn:characters`                                    | &#10004;       |
`fn:graphemes`                                     | &#10004;       |
`fn:concat`                                        | &#10004;       |
`fn:string-join`                                   | &#10004;       |
`fn:substring`                                     | &#10004;       |
`fn:string-length`                                 | &#10004;       |
`fn:string-empty`                                  | &#10004;       | non-standard
`fn:normalize-space`                               | &#10004;       |
`fn:normalize-unicode`                             | &#10004;       |
`fn:upper-case`                                    | &#10004;       |
`fn:lower-case`                                    | &#10004;       |
`fn:translate`                                     | &#10004;       |
`fn:hash`                                          |                |

### Functions based on substring matching
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:contains`                                      | &#10004;       |
`fn:starts-with`                                   | &#10004;       |
`fn:ends-with`                                     | &#10004;       |
`fn:substring`                                     | &#10004;       |
`fn:substring-before`                              | &#10004;       |
`fn:substring-after`                               | &#10004;       |

### Functions using regular expressions
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:matches`                                       |                |
`fn:replace`                                       |                |
`fn:tokenize`                                      |                |
`fn:analyze-string`                                |                |

## Processing URIs
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:decode-from-uri`                               |                |
`fn:encode-for-uri`                                |                |
`fn:escape-html-uri`                               |                |
`fn:iri-to-uri`                                    |                |
`fn:resolve-uri`                                   |                |

### Parsing and building URIs
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:parse-uri`                                     |                |
`fn:build-uri`                                     |                |


## Processing nodes

### Accessors
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:base-uri`                                      |                |
`fn:document-uri`                                  |                |
`fn:nilled`                                        |                |
`fn:node-name`                                     |                |
`fn:string`                                        |                |
`fn:data`                                          |                |
`fn:node-name`                                     |                |
`antlr:start`                                      |                | non-standard, equivalent to $start in grammar
`antlr:stop`                                       |                | non-standard, equivalent to $stop in grammar
`antlr:line`                                       |                | non-standard, equivalent to $ctx in grammar
`antlr:ctx`                                        |                | non-standard, equivalent to $ctx in grammar


### Other properties of nodes
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:has-children`                                  |                |
`fn:in-scope-namespaces`                           |                |
`fn:in-scope-prefixes`                             |                |
`fn:lang`                                          |                |
`fn:local-name`                                    |                |
`fn:name`                                          |                |
`fn:namespace-uri`                                 |                |
`fn:namespace-uri-for-prefix`                      |                |
`fn:path`                                          |                |
`fn:root`                                          |                |
`fn:siblings`                                      |                |


### Functions on sequences of nodes
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:distinct-ordered-nodes`                        |                |
`fn:innermost`                                     |                |
`fn:outermost`                                     |                |


### Identifying nodes
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:id`                                            |                |
`fn:element-with-id`                               |                |
`fn:idref`                                         |                |
`fn:generate-id`                                   |                |


### Processing function items
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:function-lookup`                               |                |
`fn:function-name`                                 |                |
`fn:function-arity`                                |                |
`fn:function-identity`                             |                |
`fn:function-annotations`                          |                |


## Map functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`map:build`                                        |                |
`map:contains`                                     |                |
`map:empty`                                        |                |
`map:entries`                                      |                |
`map:entry`                                        |                |
`map:filter`                                       |                |
`map:find`                                         |                |
`map:for-each`                                     |                |
`map:get`                                          |                |
`map:items`                                        |                |
`map:keys`                                         |                |
`map:keys-where`                                   |                |
`map:merge`                                        |                |
`map:put`                                          |                |
`map:remove`                                       |                |
`map:size`                                         |                |



## Array functions

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`array:append`                                     |                |
`array:build`                                      |                |
`array:empty`                                      |                |
`array:filter`                                     |                |
`array:flatten`                                    |                |
`array:fold-left`                                  |                |
`array:fold-right`                                 |                |
`array:foot`                                       |                |
`array:for-each`                                   |                |
`array:for-each-pair`                              |                |
`array:get`                                        |                |
`array:head`                                       |                |
`array:index-of`                                   |                |
`array:index-where`                                |                |
`array:insert-before`                              |                |
`array:items`                                      |                |
`array:join`                                       |                |
`array:members`                                    |                |
`array:of-members`                                 |                |
`array:put`                                        |                |
`array:remove`                                     |                |
`array:reverse`                                    |                |
`array:size`                                       |                |
`array:slice`                                      |                |
`array:sort`                                       |                |
`array:sort-by`                                    |                |
`array:split`                                      |                |
`array:subarray`                                   |                |
`array:tail`                                       |                |
`array:trunk`                                      |                |

## External resources and data formats
### Accessing external information
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:doc`                                           |                |
`fn:doc-available`                                 |                |
`fn:collection`                                    |                |
`fn:uri-collection`                                |                |
`fn:unparsed-text`                                 |                |
`fn:unparsed-text-lines`                           |                |
`fn:unparsed-text-available`                       |                |
`fn:unparsed-binary`                               |                |
`fn:environment-variable`                          |                |
`fn:available-environment-variables`               |                |

<!--
### Functions on ANTLR Data
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
``         |                |
``|                |
-->

### Functions on XML Data
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:parse-xml`                                     |                |
`fn:parse-xml-fragment`                            |                |
`fn:serialize`                                     |                |
`fn:xsd-validator`                                 |                |
`fn:invisible-xml`                                 |                |


### Functions on HTML Data
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:parse-html`                                    |                |
`fn:doc-available`                                 |                |

### Functions on JSON Data
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:parse-json`                                    |                |
`fn:json-doc`                                      |                |
`fn:json-to-xml`                                   |                |
`fn:xml-to-json`                                   |                |

### Functions on CSV Data
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:parse-csv`                                     |                |
`fn:csv-doc`                                       |                |
`fn:csv-to-xml`                                    |                |


## Dynamic evaluation
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:load-module`                                   |                |
`fn:transform`                                     |                |
`fn:op`                                            |                |

## Processing types
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:schema-type`                                   |                |
`fn:type-of`                                       |                |

## Accessing context
Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:current-date`                                  |                |
`fn:current-dateTime`                              |                |
`fn:current-time`                                  |                |
`fn:current-collation`                             |                |
`fn:current-language`                              |                |
`fn:implicit-timezone`                             |                |
`fn:last`                                          |                |
`fn:position`                                      |                |
`fn:static-base-uri`                               |                |

## Errors and diagnostics

Function                                           | Is implemented | Comment
---------------------------------------------------|----------------|---------
`fn:error`                                         |                |
`fn:trace`                                         |                |
`fn:message`                                       |                |
