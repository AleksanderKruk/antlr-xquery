parser grammar AntlrQueryQname;

options {
    tokenVocab = AntlrQueryLexer;
}


qname
    : anyName (COLON anyName)?
    ;

namespace: anyName;

anyName
    : ID
    | ALLOWING
    | ANCESTOR
    | AND
    | ANNOTATION
    | ARRAY
    | AS
    | ASCENDING
    | AT
    | BOOLEAN
    | BY
    | CASE
    | CAST
    | CASTABLE
    | CATCH
    | CHILD
    | COLLATION
    | CONSTRUCTION
    | CONTEXT
    | COUNT
    | DECLARE
    | DEFAULT
    | DESCENDANT
    | DESCENDING
    | DIGIT
    | DIV
    | ELEMENT
    | ELSE
    | EMPTY
    | ENCODING
    | END
    | ENUM
    | EQ
    | EVERY
    | EXCEPT
    | EXTERNAL
    | FALSE
    | FINALLY
    | FIXED
    | FN
    | FOLLOWING
    | FOLLOWS
    | FOR
    | FUNCTION
    | GE
    | GRAMMAR
    | GREATEST
    | GROUP
    | GT
    | IDIV
    | IF
    | IMPORT
    | IN
    | INFINITY
    | INSTANCE
    | INTERSECT
    | IS
    | ITEM
    | KEY
    | LE
    | LEAST
    | LET
    | LT
    | MAP
    | MEMBER
    | MOD
    | MODULE
    | MULTIPLICATION
    | NAMESPACE
    | NAN
    | NE
    | NEXT
    | NODE
    | NUMBER
    | OF
    | ONLY
    | OPTION
    | OR
    | ORDER
    | ORDERING
    | OTHERWISE
    | PARENT
    | PERCENT
    | PRECEDES
    | PRECEDING
    | PRESERVE
    | PREVIOUS
    | RECORD
    | REGEX
    | RETURN
    | RULE
    | SATISFIES
    | SELF
    | SLIDING
    | SOME
    | STABLE
    | START
    | STRING_W
    | STRIP
    | SWITCH
    | THEN
    | TO
    | TOKEN
    | TREAT
    | TRUE
    | TRY
    | TUMBLING
    | TYPE
    | TYPESWITCH
    | UNION
    | VALUE
    | VARIABLE
    | VERSION
    | WHEN
    | WHERE
    | WHILE
    | WINDOW
    ;

