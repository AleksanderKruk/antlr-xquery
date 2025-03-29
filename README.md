# antlr-xquery

As the name suggests this repository attempts to provide the ability to query text files parsed with any given antlr grammar
using XQuery, an xml query language.

Antlr itself provides limited implementation of XPath expressions, which makes it only natural to use XQuery as the guideline.

XQuery specification [3.1](https://www.w3.org/TR/xquery-31/) is followed for the most part,
with plans of using [updating expressions extension](https://www.w3.org/TR/xquery-update-30/#id-updating-expressions)
for tree modyfing queries.

This is, however, an Antlr-centric adaptation of XQuery and as such it should change specification whenever it makes sense in context of Antlr.
For example there is no such thing as an attribute in antlr rule, so attribute axis does not make much sense.
On the other hand there needs to be an axis for `locals` and `arguments` of a parametrized rule.



## Goals

The first step is the implementation of


## Roadmap


## Planned usage
<!-- -l --language > -->
```
./antlr-xquery
    [ --grammars <grammar-paths>    		]
    [  --query <xquery-cli-expression>
     | --query-file <xquery-path>       	]
    [ --starting-rule <starting-rule-name> 	]
    [ --files <target-file-paths> 			]
    [ --compile 							]
    [ --stdin	 							]
    [ --stdout	 							]
    [ --stderr	 							]
    [ --stdlog	 							]
    [ --verbose	 							]
```

## Rationale


