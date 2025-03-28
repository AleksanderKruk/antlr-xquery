Expression | Example | Is implemented | Comment
----------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|----------------|--------------------------------------------------------------------
Float literals | `123.4` | yes |
Integer literals | `123` | yes |
String literals | `"string"`<br>`'string'` | yes |
Variable References | `$x` | |
Grouping parentheses | `(...)` | yes |
Empty sequence | `()` | yes |
Context Item Expression | `.` | yes |
Static Function Calls | `string-length("string")` | yes | listed in <a href="supported functions.md">supported functions</a>
Named Function References | `abs#1` - abs function with arity of 1 | |
Inline Function Expressions | `function() as xs:integer+ { 2, 3, 5, 7, 11, 13 }` | |
Enclosed Expressions | `{ ... }` | |
Filter Expressions | `$x[filter]` | yes |
Dynamic Function Calls | `$x($arg)` | yes |
Path Expressions | `/`<br>`//element`<br>`/x//y` | yes |
Path wildcards | `/`<br>`//element`<br>`/x//y` | yes |
Child axis | `//child::*` | yes |
Descendant axis | `//descendant::*` | yes |
Self axis | `//self::*` | yes |
Descendant or self axis | `//descendant-or-self::*` | yes |
Following sibling axis | `//following-sibling::*` | yes |
Following axis | `//following::*` | yes |
Parent axis | `parent::*` | yes |
Ancestor axis | `//ancestor::*` | yes |
Preceding axis | `//preceding-sibling::*` | yes |
Preceding axis | `//preceding::*` | yes |
Ancestor or self axis | `//ancestor-or-self::*` | yes |
Predicates within Steps | `//div[@class="header"]` | yes |
Sequence Expressions | `(1, 2, 3)` | yes |
Sequence Range Expressions | `1 to 5` | yes |
Sequence union | `(1,2,3) \| (4, 5, 6)`<br>`(1,2,3) union (4, 5, 6)` | yes |
Sequence subtraction | `(1,2,3) except (2, 3)` | yes |
Sequence intersection | `(1,2,3) intersect (4, 5, 6)` | yes |
Arithmetic Expressions | `4*5 + +10`<br>`5 div 5 - -1`<br>`10 idiv 2 + 10 mod 2` | yes |
String Concatenation Expressions | `"a" \|\| "b"` | yes |
Value comparisons | `$sequence[. eq "a"]` | yes |
General comparisons | `$sequence = "a"` | yes |
Node comparisons | `$x[. is y]`<br>`$x[. << after]`<br>`$x[. >> before]` | yes |
Logical Expressions | `false() or true() and false()` | yes |
String Constructors (string interpolation) | ``` ``[`{$s}` fish]`` ``` | |
Map Constructors | ``` map { "a": "x", "b": "y" } ``` | |
Map Lookup using Function Call Syntax | ``` $map("key") ``` | |
Array Constructors | ``` [ 1, 2, (3, 4, 5) ] ``` | |
Array Lookup using Function Call Syntax | ``` $array(1) ``` | |
The Lookup Operator ("?") for Maps and Arrays | ``` $map ?key ```<br>```$array ? 1``` | |
Unary Lookup | ``` ?key ``` | |
Variable Bindings | ``` let $x := $expr1, $y := $expr2 ``` | yes |
For Clause | ```for $d in ./depts/deptno``` | yes|
Let Clause | ```let $e := ./emps/emp[deptno eq $d] ``` | yes|
Tumbling Windows | ``` for tumbling window $w in (2, 4, 6, 8, 10, 12, 14) start at $s when fn:true() only end at $e when $e - $s eq 2 return $w ``` | |
Sliding Windows | ``` for sliding window $w in (2, 4, 6, 8, 10, 12, 14) start at $s when fn:true() only end at $e when $e - $s eq 2 return $w``` | |
Where Clause | ``` for $x at $i in $inputvalues where $i mod 100 = 0 return $x ``` | yes |
Count Clause | ```... count $counter ``` | yes |
Group By Clause | ``` let $g2 := $expr1, $g3 := $expr2 group by $g1, $g2, $g3 ``` | yes |
Order By Clause | ``` for $b in $books/book[price < 100] order by $b/title return $b ``` | yes |
                | ``` for $b in $books/book[price < 100] order by $b/title ascending return $b ``` | yes |
                | ``` for $b in $books/book[price < 100] order by $b/title descending return $b ``` | yes |
                | ``` for $b in $books/book[price < 100] order by $b/title, $b/year return $b ``` | yes |
Conditional Expressions | ``` if ($part/discounted) then $part/wholesale else $part/retail ``` | |
Switch Expression | ``` switch ($animal) case "Cow" return "Moo" case "Cat" return "Meow" case "Duck" return "Quack" default return "What's that odd noise?" ``` | |
Quantified Expressions - some element in | ``` some $emp in /emps/employee satisfies ($emp/bonus > 0.25 * $emp/salary) ``` | yes |
Quantified Expressions - every element in | ``` every $x in (1, 2, 3), $y in (2, 3, 4) satisfies $x + $y = 4 ``` | yes |
Try/Catch Expressions | ``` try { ... } catch * { ... } ``` | |
Simple map operator (!) | ``` child::div1 / child::para / string() ! concat("id-", .) ``` | |
Arrow operator (=>) | ``` $string => upper-case() => normalize-unicode() => tokenize("\s+") ``` | yes |
