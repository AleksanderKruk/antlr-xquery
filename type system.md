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

###


