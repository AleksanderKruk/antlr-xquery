
parser grammar AntlrQueryCardinality;

options {
    tokenVocab = AntlrQueryLexer;
}

cardinality
    : singleTermCardinality
    | parenthesizedCardinality
    | zeroOrMoreCardinality
    | oneOrMoreCardinality
    | zeroOrOneCardinality
    | exactlyOneCardinality
    ;

singleTermCardinality
    : HAT cardinalityTerm
    ;

parenthesizedCardinality
    : HAT LPAREN cardinalitySet RPAREN
    ;

zeroOrMoreCardinality
    : STAR
    ;

oneOrMoreCardinality
    : PLUS
    ;

zeroOrOneCardinality
    : QUESTION_MARK
    ;

exactlyOneCardinality
    : QUESTION_MARK
    ;

cardinalitySet
    : cardinalityTerm (UNION_OP cardinalityTerm)*
    ;

cardinalityTerm
    : IntegerLiteral                     # singleNumberCardinality
    | IntegerLiteral DOTS IntegerLiteral # inclusiveRangeCardinality
    | IntegerLiteral DOTS                # minimumCardinality
    | DOTS IntegerLiteral                # maximumCardinality
    ;
