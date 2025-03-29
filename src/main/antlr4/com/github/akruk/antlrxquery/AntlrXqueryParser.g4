parser grammar AntlrXqueryParser;
options {
    tokenVocab = AntlrXqueryLexer;
}
xquery: expr;
expr: exprSingle (COMMA exprSingle)*;
exprSingle: fLWORExpr
        | quantifiedExpr
        | ifExpr
        | switchExpr
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
orderModifier: (ASCENDING | DESCENDING)? (EMPTY (GREATEST | LEAST))?;
returnClause: RETURN exprSingle;
quantifiedExpr: (SOME | EVERY) DOLLAR varName typeDeclaration? IN exprSingle (COMMA DOLLAR varName typeDeclaration? IN exprSingle)* SATISFIES exprSingle;
ifExpr: IF LPAREN condition=expr RPAREN THEN ifValue=exprSingle ELSE elseValue=exprSingle;

switchExpr: SWITCH  LPAREN switchedExpr=expr RPAREN switchCaseClause+ DEFAULT RETURN defaultExpr=exprSingle;
switchCaseClause: (CASE switchCaseOperand)+ RETURN exprSingle;
switchCaseOperand: exprSingle;

orExpr: orExpr ( OR orExpr )+
        | orExpr ( AND orExpr )+
        | orExpr  (valueComp | generalComp | nodeComp) orExpr
        | orExpr ( CONCATENATION orExpr )+
        | orExpr TO orExpr
        | orExpr (additiveOperator orExpr )+
        | orExpr (multiplicativeOperator orExpr)+
        | orExpr (unionOperator orExpr)+
        | orExpr ((INTERSECT | EXCEPT) orExpr)+
        | orExpr (ARROW arrowFunctionSpecifier argumentList)+
        | (MINUS | PLUS) orExpr
        | pathExpr (EXCLAMATION_MARK pathExpr)?
;
additiveOperator: PLUS | MINUS;
unionOperator: UNION | UNION_OP;
multiplicativeOperator: STAR | DIV | IDIV | MOD;
generalComp: EQ_OP | NE_OP | LT_OP | LE_OP | GT_OP | GE_OP;
valueComp: EQ | NE | LT | LE | GT | GE;
nodeComp: IS | PRECEDING_OP | FOLLOWING_OP;
pathExpr: (SLASH relativePathExpr?) // TODO: verify optionality
        | (SLASHES relativePathExpr)
        | relativePathExpr;
relativePathExpr: stepExpr (pathOperator stepExpr)*;
pathOperator: SLASH | SLASHES;
stepExpr: postfixExpr | axisStep;
axisStep: (reverseStep | forwardStep) predicateList;
forwardStep: forwardAxis? nodeTest;

forwardAxis: CHILD COLONS
        | DESCENDANT COLONS
        | SELF COLONS
        | DESCENDANT_OR_SELF COLONS
        | FOLLOWING_SIBLING COLONS
        | FOLLOWING COLONS;
reverseStep: (reverseAxis nodeTest) | abbrevReverseStep;
reverseAxis: PARENT COLONS
        | ANCESTOR COLONS
        | PRECEDING_SIBLING COLONS
        | PRECEDING COLONS
        | ANCESTOR_OR_SELF COLONS;
abbrevReverseStep: DOTS;
nodeTest: nameTest;
nameTest: ID | wildcard;
wildcard: STAR
        | (ID COLONSTAR)
        | (STARCOLON ID);
postfixExpr: primaryExpr (postfix)*;
postfix: predicate | argumentList;
argumentList: LPAREN (argument (COMMA argument)*)? RPAREN;
predicateList: predicate*;
predicate: LBRACKET expr RBRACKET;
arrowFunctionSpecifier: ID | varRef | parenthesizedExpr;
primaryExpr: literal
        | varRef
        | parenthesizedExpr
        | contextItemExpr
        | functionCall;
literal: INTEGER | DECIMAL | STRING;
varRef: DOLLAR varName;
varName: qname;
parenthesizedExpr: LPAREN expr? RPAREN;
contextItemExpr: DOT;
functionCall: functionName argumentList;
argument: exprSingle | argumentPlaceholder;
argumentPlaceholder: QUESTION_MARK;
typeDeclaration: AS sequenceType;
sequenceType: (ID occurrenceIndicator?);
occurrenceIndicator: QUESTION_MARK | STAR | PLUS;
functionName: qname;


qname: (namespace COLON)* anyName;
namespace: anyName;
anyName: ID | EMPTY | COUNT;
