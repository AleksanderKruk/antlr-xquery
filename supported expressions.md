Expression                                    | Example                                                 | Is implemented | Comment
----------------------------------------------|---------------------------------------------------------|----------------|---------
Float literals                                | `123.4`                                                 | yes            |
Integer literals                              | `123`                                                   | yes            |
String literals                               | `"string"`<br>`'string'`                                | yes            |
Variable References                           | `$x`                                                    |                |
Parenthesized Expressions                     | `(...)`                                                 | yes            |
Context Item Expression                       | `.`                                                     |                |
Static Function Calls                         | `string-length("string")`                               | yes            | listed in <a href="supported functions.md">supported functions</a>
Named Function References                     | `abs#1` /* abs function with arity of 1 */              |                |
Inline Function Expressions                   | `function() as xs:integer+ { 2, 3, 5, 7, 11, 13 }`      |                |
Enclosed Expressions                          | `{ ... }`                                               |                |
Filter Expressions                            | `$x[filter]`                                            |                |
Dynamic Function Calls                        | `$x($arg)`                                              |                |
Path Expressions                              | `/`<br>`//element`<br>`/x//y`                           |                |
Child axis                                    | `//child::*`                                            |                |
Descendant axis                               | `//descendant::*`                                       |                |
Attribute axis                                | `//attribute::*`                                        |                |
Self axis                                     | `//self::*`                                             |                |
Descendant or self axis                       | `//descendant-or-self::*`                               |                |
Following sibling axis                        | `//following-sibling::*`                                |                |
Following axis                                | `//following::*`                                        |                |
Parent axis                                   | `parent::*`                                             |                |
Ancestor axis                                 | `//ancestor::*`                                         |                |
Preceding axis                                | `//preceding-sibling::*`                                |                |
Preceding axis                                | `//preceding::*`                                        |                |
Ancestor or self axis                         | `//ancestor-or-self::*`                                 |                |
Predicates within Steps                       | `//div[@class="header"]`                                |                |
Sequence Expressions                          | `(1, 2, 3)`                                             |                |
Sequence union                                | `(1,2,3) | (4, 5, 6)`<br>`(1,2,3) union (4, 5, 6)`      |                |
Sequence subtraction                          | `(1,2,3) except (2, 3)`                                 |                |
Sequence intersection                         | `(1,2,3) intersect (4, 5, 6)`                           |                |
Arithmetic Expressions                        | `4*5 + +10`<br>`5 div 5 - -1`<br>`10 idiv 2 + 10 mod 2` |                |
String Concatenation Expressions              | `"a" || "b"`                                            |                |
Value comparisons                             | `$sequence[. eq "a"]`                                   |                |
General comparisons                           | `$sequence = "a"`                                       |                |
Node comparisons                              | `$x[. is y]`<br>`$x[. << after]`<br>`$x[. >> before]`   |                |
Logical Expressions                           | `false() or true() and false()`                         | yes            |
Node Constructors                             |                                                         |                |
Direct Element Constructors                   |                                                         |                |
Attributes                                    |                                                         |                |
Namespace Declaration Attributes              |                                                         |                |
Content                                       |                                                         |                |
Boundary Whitespace                           |                                                         |                |
Other Direct Constructors                     |                                                         |                |
Computed Constructors                         |                                                         |                |
Computed Element Constructors                 |                                                         |                |
Computed Attribute Constructors               |                                                         |                |
Document Node Constructors                    |                                                         |                |
Text Node Constructors                        |                                                         |                |
Computed Processing Instruction Constructors  |                                                         |                |
Computed Comment Constructors                 |                                                         |                |
Computed Namespace Constructors               |                                                         |                |
In-scope Namespaces of a Constructed Element  |                                                         |                |
String Constructors                           |                                                         |                |
Maps and Arrays                               |                                                         |                |
Maps                                          |                                                         |                |
Map Constructors                              |                                                         |                |
Map Lookup using Function Call Syntax         |                                                         |                |
Arrays                                        |                                                         |                |
Array Constructors                            |                                                         |                |
Array Lookup using Function Call Syntax       |                                                         |                |
The Lookup Operator ("?") for Maps and Arrays |                                                         |                |
Unary Lookup                                  |                                                         |                |
Postfix Lookup                                |                                                         |                |
FLWOR Expressions                             |                                                         |                |
Variable Bindings                             |                                                         |                |
For Clause                                    |                                                         |                |
Let Clause                                    |                                                         |                |
Window Clause                                 |                                                         |                |
Tumbling Windows                              |                                                         |                |
Sliding Windows                               |                                                         |                |
Effects of Window Clauses on the Tuple Stream |                                                         |                |
Where Clause                                  |                                                         |                |
Count Clause                                  |                                                         |                |
Group By Clause                               |                                                         |                |
Order By Clause                               |                                                         |                |
Return Clause                                 |                                                         |                |
Ordered and Unordered Expressions             |                                                         |                |
Conditional Expressions                       |                                                         |                |
Switch Expression                             |                                                         |                |
Quantified Expressions                        |                                                         |                |
Try/Catch Expressions                         |                                                         |                |
Expressions on SequenceTypes                  |                                                         |                |
Instance Of                                   |                                                         |                |
Typeswitch                                    |                                                         |                |
Cast                                          |                                                         |                |
Castable                                      |                                                         |                |
Constructor Functions                         |                                                         |                |
Treat                                         |                                                         |                |
Simple map operator (!)                       |                                                         |                |
Arrow operator (=>)                           |                                                         |                |
Validate Expressions                          |                                                         |                |
Extension Expressions                         |                                                         |                |
