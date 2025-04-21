# Type system

Antlr XQuery is untyped by default.
It may however be desirable to have type checking for each query.


## Sequences
XQuery type system is based on sequences. A sequence consists of
`item type` - that is the type that the sequence consists of,
`occurence` - which is denoted with the use of following symbols:
Type designation | Interpretation
--- | ---
`empty-sequence()` | any empty sequence
`Type` | sequence consisting of only one item of type `Type`
`Type?`| sequence consisting of zero or one items of type `Type`
`Type*`| sequence consisting of zero or more items of type `Type`
`Type+`| sequence consisting of one or more items of type `Type`

## Items
Sequences consist of items.
The most generalized type an item can be is `item()`. This means that any atomic value can take place of `item()`.

### Nodes | Elements
Item of type node represents any parsed tree node.
Because ANTLR trees do not have comment or text or namespace or any
other nodes apart from element nodes XQuery type system can be reduced to
a simpler form, where `node()` and `element()` and `element(*)` all mean 'any parse tree node'.
If we want to entype only the parse tree nodes of a given name we use `element(<elements-name>)`.
To sum up:

Type designation | Interpretation
--- | ---
`item()` | sequence consisting of one element of any type.
`element()` | as above
`element(*)` | as above
`element(test)?`| sequence consisting of zero or one parse tree nodes named `test`

### Primary types
Primary types are the most basic of types. These are `string` for representing text, `number` for representing numbers and `boolean` for boolean values.
Type designation | Interpretation
--- | ---
`string` | one-element-sequence of type `string`
`number` | one-element-sequence of type `number`
`boolean` | one-element-sequence of type `boolean`

## Expression semantics

### Sequence range expression
Sequence range expression has the following semantics:
```xquery
number() to number() -> number()*
```

### Sequence constructor expression
As *sequence constructor operator* we understand `(v1, v2, v3, ...)` expression. The deduced type resulting from this expression is
achieved by 'merging' types of values `v1` and `v2` and `v3` and ... using the following rules:
1. If values have the same item type the resulting item type is this item type. Otherwise the deduced item type is `any()`
2. Occurence is deduced using the following table

(v1, v2)  | 0 | 1 | ? | \* | \+
---    |---|---|---|--- |---
**0**  | 0 | 1 | ? | *  | +
**1**  | 1 | + | + | +  | +
**?**  | ? | + | * | *  | +
**\*** | * | + | * | *  | +
**\+** | + | + | + | +  | +


### Union operator
Union operators have the same semantics as sequence constructor operator.
```xquery
v1 union v2 -> merged
```


**Merging occurence semantics:**
(v1, v2)  | 0 | 1 | ? | \* | \+
---    |---|---|---|--- |---
**0**  | 0 | 1 | ? | *  | +
**1**  | 1 | + | + | +  | +
**?**  | ? | + | * | *  | +
**\*** | * | + | * | *  | +
**\+** | + | + | + | +  | +


### Intersect operator
The deduced type resulting from `intersect` operator is
achieved by 'merging' types of values `v1` and `v2` and `v3` and ... using the following rules:
1. Item type is that of left hand side expression
2. Occurence is deduced using the following table

v1 intersect v2  | 0 | 1 | ? | \* | \+
---              |---|---|---|--- |---
**0**            | 0 | 0 | 0 | 0  | 0
**1**            | 0 | ? | ? | ?  | ?
**?**            | 0 | ? | ? | ?  | ?
**\***           | 0 | ? | ? | *  | *
**\+**           | 0 | ? | ? | *  | *

### Except operator
The deduced type resulting from `except` operator is
achieved by 'merging' types of values `v1` and `v2` and `v3` and ... using the following rules:
1. Item type is that of left hand side expression
2. Occurence is deduced using the following table

v1 except v2  | 0 | 1 | ? | \* | \+
---           |---|---|---|--- |---
**0**         | 0 | 0 | 0 | 0  | 0
**1**         | 1 | ? | ? | ?  | ?
**?**         | ? | ? | ? | ?  | ?
**\***        | * | * | * | *  | *
**\+**        | + | * | * | *  | *

### Arithmetic operators
Arithmetic operators take `number`s as arguments and return
`number` as the resulting type.

### String concatenation
Arithmetic operators take item types that can be converted to string as arguments and return `string` as the resulting type.





### Logical expressions
Logical expressions take values that have effective boolean value as arguments and return `boolean` as the resulting type.
```xquery
Effective Boolean Value Type or Effective Boolean Value Type
Effective Boolean Value Type and Effective Boolean Value Type
```

#### Effective boolean value
Type     |Has effective boolean value?
---      |---
`any()`    | no
`any()?`   | yes, whether or not item is present
`any()*`   | yes, whether or not sequence has items
`any()+`   | yes, always true because non empty sequence
`boolean()`| yes
`number()` | yes, whether or not number equals zero
`string()` | yes, whether or not string is not empty
`node()`  | no

### Value comparisons
Value comparisons are used to compare directly item types.
```xquery
T eq T -> boolean
T ne T -> boolean
T lt T -> boolean
T gt T -> boolean
T le T -> boolean
T ge T -> boolean
```
T needs to be a single item type, same for both operands.


### General comparisons
General comparisons are expanded value comparisons. They work both for single item values as well as multi item values.
```xquery
T* =  T* -> boolean
T* != T* -> boolean
T* >  T* -> boolean
T* <  T* -> boolean
T* >= T* -> boolean
T* <= T* -> boolean
```
T can be any sequence type, as long as the item type can be compared by value.

### Node comparisons
Node comparisons take one item sequences of `node()` and return `boolean`.
```xquery
node() is node() -> boolean
node() << node() -> boolean
node() >> node() -> boolean
```

### Arrow expression
Arrow expressions semantics are equivalent to embedded function calls with the previous expression type having to match the first argument for the following function.
```xquery
Arg1Type => function(Arg1Type , *) as ReturnedType -> ReturnedType
```

### Simple map expression
Simple map expressions are interpreted as a sequence type with the item type of the mapped expression, and the occurence same as that of the mapped type.
```xquery
MappedType() ! ExpressionType -> ExpressionType
MappedType()? ! ExpressionType -> ExpressionType?
MappedType()* ! ExpressionType -> ExpressionType*
MappedType()+ ! ExpressionType -> ExpressionType+
```
If the expression type is a sequence type then sequence construction operator semantics are applied before interpreting simple map expression type.

### Instance of expression
```xquery
TestedType instance of ExpectedType -> boolean
```
### Typeswitch expression
```
typeswitch(TestedType)
   case $a as TypeVariantA return ExprAType
   case $b as TypeVariantB return ExprBType
   case $c as TypeVariantC return ExprCType
   default return DefaultExprType
```
If `ExprAType` and `ExprBType` and `ExprCType` and `DefaultExprType` are all the same item type then the interpreted item type is the same as these item types. Otherwise the interpreted item type is `item()`.

Occurence is interpreted using the following merging table:
v1 \| v2 | 0 | 1 | ? | \* | \+
---      |---|---|---|--- |---
**0**    | 0 | ? | ? | *  | *
**1**    | ? | 1 | ? | *  | +
**?**    | ? | ? | ? | *  | *
**\***   | * | * | * | *  | *
**\+**   | * | + | * | *  | +



### Cast expression
```xquery
InputType cast as TargetType -> TargetType
InputType cast as TargetType? -> TargetType?
```
`InputType` must have occurence `1`. If `TargetType?` is specified then occurence of `InputType` can be `?`

### Castable expression


### Treat expression

### Let clause
```xquery
let $x := AssignedExpressionType -> AssignedExpressionType
let $x as DeclaredType := AssignedExpressionType ->
```


