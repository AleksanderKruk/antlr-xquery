# Command line interface
## Usage
<!-- --compiled-grammar (<grammar.jar> | <grammar.class>) -->
```
java -jar ./antlr-xquery-tool.jar
    [ --grammars <files-necessary-to-compile-grammar> ]
    [ --starting-rule <starting-rule-name> ]
    [  --query <xquery-cli-expression>
     | --query-file <xquery-path> ]
    [ --target-files <target-file-paths> ]
    [ --stdin <path> ]
    [ --stdout <path> ]
    [ --stderr <path> ]
    [ --output-format
      [
        representation
        | node-text
        | file-text
        | xml
        | shortened-xml
          {
            # <x><y><z></z></y></x> === <x::y::z></x::y::z>
            nested-nodes-collapse
            # <node-name></>
            | short-markup-closures
            # <true-literal>true</true-literal> === <true-literal/>
            | short-node-literals
          }
      ]
    ]
    [ --verbose ]
```

### Passing grammars
#### Using command line
#### Using query declaration
### Passing query
### Passing target files
### Changing standard streams
### Specifing output format
