
parser grammar AntlrQueryNumericRange;

options {
    tokenVocab = AntlrQueryLexer;
}

numericRange
    : numericRangeTerm              # singleTermNumericRange
    | LPAREN numericRangeSet RPAREN # parenthesizedNumericRange
    ;

numericRangeSet
    : numericRangeTerm (UNION_OP numericRangeTerm)*
    ;

numericRangeTerm
    : singleNumberNumericRange

    | inclusiveRangeNumericRange
    | minimumNumericRange
    | maximumNumericRange

    | greaterThanNumericRange
    | greaterOrEqualNumericRange
    | lessThanNumericRange
    | lessOrEqualNumericRange

    | openRangeNumericRange
    | closedRangeNumericRange
    | leftOpenRangeNumericRange
    | rightOpenRangeNumericRange
    ;

singleNumberNumericRange
    : IntegerLiteral
    ;

inclusiveRangeNumericRange
    :  IntegerLiteral DOTS IntegerLiteral
    ;

minimumNumericRange
    : IntegerLiteral DOTS
    ;

maximumNumericRange
    :  DOTS IntegerLiteral
    ;

greaterThanNumericRange
    : DOT GT_OP IntegerLiteral
    | IntegerLiteral LT_OP DOT
    ;
greaterOrEqualNumericRange
    : DOT GE_OP IntegerLiteral
    | IntegerLiteral LE_OP DOT
    ;

lessThanNumericRange
    : DOT LT_OP IntegerLiteral
    | IntegerLiteral GT_OP DOT
    ;

lessOrEqualNumericRange
    : DOT LE_OP IntegerLiteral
    | IntegerLiteral GE_OP DOT
    ;

openRangeNumericRange
    : openRangeLessThanNumericRange
    | openRangeGreaterThanNumericRange
    ;

openRangeLessThanNumericRange
    : IntegerLiteral LT DOT LT IntegerLiteral
    ;

openRangeGreaterThanNumericRange
    : IntegerLiteral GT DOT GT IntegerLiteral
    ;

closedRangeNumericRange
    : closedRangeLessEqualNumericRange
    | closedRangeGreaterEqualNumericRange
    ;

closedRangeLessEqualNumericRange
    : IntegerLiteral LE DOT LE IntegerLiteral
    ;

closedRangeGreaterEqualNumericRange
    : IntegerLiteral GE DOT GE IntegerLiteral
    ;

leftOpenRangeNumericRange
    :  IntegerLiteral LT DOT LE IntegerLiteral
    ;

rightOpenRangeNumericRange
    :  IntegerLiteral LE DOT LT IntegerLiteral
    ;
