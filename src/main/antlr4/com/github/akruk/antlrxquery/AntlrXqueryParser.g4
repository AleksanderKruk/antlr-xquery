parser grammar AntlrXqueryParser;
options {
    tokenVocab = AntlrXQueryLexer;
}
query: expr;
expr: exprSingle (COMMA exprSingle)*;
exprSingle: fLWORExpr
        | quantifiedExpr
        | ifExpr
        | orExpr;
fLWORExpr: initialClause intermediateClause* returnClause;
initialClause: forClause 
            | letClause;
intermediateClause: initialClause 
                | whereClause 
                | orderByClause 
                | countClause;
forClause: FOR forBinding (COMMA forBinding)*;
forBinding: DOLLAR varName typeDeclaration? allowingEmpty? positionalVar? IN exprSingle;
allowingEmpty: ALLOWING EMPTY;
positionalVar: AT DOLLAR varName;
letClause: LET letBinding (COMMA letBinding)*;
letBinding: DOLLAR varName typeDeclaration? ASSIGNMENT_OP exprSingle;
countClause: COUNT DOLLAR varName;
whereClause: WHERE exprSingle;
orderByClause: ((ORDER BY) | (STABLE ORDER BY)) orderSpecList;
orderSpecList: orderSpec (COMMA orderSpec)*;
orderSpec: exprSingle orderModifier;
orderModifier: (ASCENDING | DESCENDING)? (EMPTY (GREATEST | LEAST))? (COLLATION uRILiteral)?;
returnClause: RETURN exprSingle;
quantifiedExpr: (SOME | EVERY) DOLLAR varName typeDeclaration? IN exprSingle (COMMA DOLLAR varName typeDeclaration? IN exprSingle)* SATISFIES exprSingle;
ifExpr: IF LPAREN expr RPAREN THEN exprSingle ELSE exprSingle;
orExpr: orExpr ( OR orExpr )+
        | orExpr ( AND orExpr )+
        | orExpr  (valueComp | generalComp | nodeComp) orExpr 
        | orExpr ( OR_LOGICAL_OP orExpr )+
        | orExpr TO orExpr
        | orExpr ( (PLUS | MINUS) orExpr )
        | orExpr (STAR | DIV | IDIV | MOD) orExpr
        | orExpr (UNION | UNION_OP) orExpr
        | orExpr (INTERSECT | EXCEPT) orExpr
        | orExpr ARROW arrowFunctionSpecifier argumentList
        | (MINUS | PLUS) orExpr
        | pathExpr (EXCLAMATION_MARK pathExpr)
;
generalComp: EQ_OP | NE_OP | LT_OP | LE_OP | GT_OP | GE_OP;
valueComp: EQ | NE | LT | LE | GT | GE;
nodeComp: IS | PRECEDING_OP | FOLLOWING_OP;
pathExpr: (SLASH relativePathExpr?)
        | (SLASHES relativePathExpr)
        | relativePathExpr; /* xgc: leading-lone-slash */
relativePathExpr: stepExpr ((SLASH | SLASHES) stepExpr)*;
stepExpr: postfixExpr | axisStep;
axisStep: (reverseStep | forwardStep) predicateList;
forwardStep: (forwardAxis nodeTest) | abbrevForwardStep;

forwardAxis: (CHILD COLONS)
        | (DESCENDANT COLONS)
        | (ATTRIBUTE COLONS)
        | (SELF COLONS)
        | (DESCENDANT_OR_SELF COLONS)
        | (FOLLOWING_SIBLING COLONS)
        | (FOLLOWING COLONS);
abbrevForwardStep: AT_OP? nodeTest;
reverseStep: (reverseAxis nodeTest) | abbrevReverseStep;
reverseAxis: (PARENT COLONS)
        | (ANCESTOR COLONS)
        | (PRECEDING_SIBLING COLONS)
        | (PRECEDING COLONS)
        | (ANCESTOR_OR_SELF COLONS);
abbrevReverseStep: DOTS;
nodeTest: nameTest;
nameTest: ID | wildcard;
wildcard: STAR
        | (ID COLONSTAR)
        | (STARCOLON ID);
postfixExpr: primaryExpr (predicate | argumentList)*;
argumentList: LPAREN (argument (COMMA argument)*)? RPAREN;
predicateList: predicate*;
predicate: LBRACKET expr RBRACKET;
arrowFunctionSpecifier: ID | varRef | parenthesizedExpr;
primaryExpr: literal
        | varRef
        | parenthesizedExpr
        | contextItemExpr
        | functionCall;
literal: numericLiteral | StringLiteral;
numericLiteral: IntegerLiteral | DecimalLiteral | DoubleLiteral;
varRef: DOLLAR varName;
varName: ID;
parenthesizedExpr: LPAREN expr? RPAREN;
contextItemExpr: DOT;
functionCall: ID argumentList;  /* xgc: reserved-function-names */
                                /* gn: parens */
argument: exprSingle | argumentPlaceholder;
argumentPlaceholder: QUESTION_MARK;
elementDeclaration: elementName;
attributeName: ID;
elementName: ID;
simpleTypeName: typeName;
typeName: ID;
uRILiteral: StringLiteral;

typeDeclaration: AS sequenceType;
sequenceType: (ID occurrenceIndicator?);
occurrenceIndicator: QUESTION_MARK | STAR | PLUS;
