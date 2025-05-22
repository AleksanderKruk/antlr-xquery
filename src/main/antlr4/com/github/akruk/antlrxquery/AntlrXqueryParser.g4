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
                | whileClause
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
whileClause: WHILE exprSingle;
orderByClause: ((ORDER BY) | (STABLE ORDER BY)) orderSpecList;
orderSpecList: orderSpec (COMMA orderSpec)*;
orderSpec: exprSingle orderModifier;
orderModifier: (ASCENDING | DESCENDING)? (EMPTY (GREATEST | LEAST))?;
returnClause: RETURN exprSingle;
quantifiedExpr: (SOME | EVERY) DOLLAR varName typeDeclaration? IN exprSingle (COMMA DOLLAR varName typeDeclaration? IN exprSingle)* SATISFIES exprSingle;
ifExpr	:	IF LPAREN expr RPAREN (unbracedActions | bracedAction);
unbracedActions	:	THEN exprSingle ELSE exprSingle;
bracedAction	:	enclosedExpr;
enclosedExpr	:	LCURLY expr? RCURLY;

switchExpr: SWITCH  LPAREN switchedExpr=expr RPAREN switchCaseClause+ DEFAULT RETURN defaultExpr=exprSingle;
switchCaseClause: (CASE switchCaseOperand)+ RETURN exprSingle;
switchCaseOperand: exprSingle;

orExpr: andExpr ( OR andExpr)*;
andExpr: comparisonExpr ( AND comparisonExpr )*;
comparisonExpr: stringConcatExpr  ((valueComp | generalComp | nodeComp) stringConcatExpr)?;
stringConcatExpr: rangeExpr ( CONCATENATION rangeExpr )*;
rangeExpr: additiveExpr (TO additiveExpr)?;
additiveExpr: multiplicativeExpr (additiveOperator multiplicativeExpr)*;
multiplicativeExpr: unionExpr (multiplicativeOperator unionExpr )*;
unionExpr: intersectExpr (unionOperator intersectExpr)*;
intersectExpr: instanceofExpr (exceptOrIntersect instanceofExpr)*;
instanceofExpr: treatExpr (INSTANCE OF treatExpr)*;
treatExpr: castableExpr (TREAT AS castableExpr)*;
castableExpr: arrowExpr ((CAST AS) singleType)*;
arrowExpr: unaryExpr (ARROW arrowFunctionSpecifier argumentList)*;
unaryExpr: (MINUS | PLUS)? simpleMapExpr;
simpleMapExpr: pathExpr (EXCLAMATION_MARK pathExpr)*;
pathExpr: (SLASH relativePathExpr?) // TODO: verify optionality
        | (SLASHES relativePathExpr)
        | relativePathExpr;
relativePathExpr: stepExpr (pathOperator stepExpr)*;
stepExpr: postfixExpr | axisStep;
axisStep: (reverseStep | forwardStep) predicateList;
forwardStep: forwardAxis? nodeTest;

additiveOperator: PLUS | MINUS;
unionOperator: UNION | UNION_OP;
multiplicativeOperator: STAR | DIV | IDIV | MOD;
generalComp: EQ_OP | NE_OP | LT_OP | LE_OP | GT_OP | GE_OP;
valueComp: EQ | NE | LT | LE | GT | GE;
nodeComp: IS | PRECEDING_OP | FOLLOWING_OP;
pathOperator: SLASH | SLASHES;
singleType: anyName '?';


exceptOrIntersect: EXCEPT | INTERSECT;

forwardAxis: CHILD COLONS
        | DESCENDANT COLONS
        | SELF COLONS
        | DESCENDANT_OR_SELF COLONS
        | FOLLOWING_SIBLING COLONS
        | FOLLOWING COLONS
        | FOLLOWING_SIBLING_OR_SELF COLONS
        | FOLLOWING_OR_SELF COLONS;
reverseStep: (reverseAxis nodeTest) | abbrevReverseStep;
reverseAxis: PARENT COLONS
        | PRECEDING_SIBLING_OR_SELF COLONS
        | PRECEDING_OR_SELF COLONS
        | ANCESTOR COLONS
        | PRECEDING_SIBLING COLONS
        | PRECEDING COLONS
        | ANCESTOR_OR_SELF COLONS;
abbrevReverseStep: DOTS;
nodeTest: nameTest;
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
// sequenceType: qname occurrenceIndicator?;
occurrenceIndicator: QUESTION_MARK | STAR | PLUS;
sequenceTypeUnion: sequenceType ('|' sequenceType)*;

sequenceType	:	EMPTY_SEQUENCE LPAREN RPAREN
              | itemType occurrenceIndicator?;

itemType: anyItemTest
        | typeName
        | kindTest
        | functionType
        | mapType
        | arrayType
        | recordType
        | enumerationType
        | choiceItemType;

kindTest:	elementTest
        | anyKindTest;

elementTest	:	ELEMENT LPAREN nameTestUnion? RPAREN;
nameTestUnion	:	nameTest ('|' nameTest)*;
nameTest	:	qname | wildcard;

functionType:	annotation* (anyFunctionType | typedFunctionType);
annotation	:	PERCENTAGE qname (LPAREN annotationValue (',' annotationValue)* RPAREN)?;
annotationValue:	STRING | ('-'? (INTEGER | DECIMAL)) | (qname LPAREN RPAREN);
anyFunctionType	:	FUNCTION LPAREN '*' RPAREN;
typedFunctionType	:	FUNCTION LPAREN (typedFunctionParam (',' typedFunctionParam)*)? RPAREN 'as' sequenceType;
typedFunctionParam	:	('$' qname 'as')? sequenceType;

mapType	:	anyMapType | typedMapType;
anyMapType	:	MAP LPAREN '*' RPAREN;
typedMapType	:	MAP LPAREN itemType ',' sequenceType RPAREN;


recordType:	anyRecordType | typedRecordType;
anyRecordType: RECORD LPAREN '*' RPAREN;
typedRecordType: RECORD LPAREN (fieldDeclaration (',' fieldDeclaration)*)? extensibleFlag? RPAREN;
extensibleFlag:	',' '*';
fieldDeclaration	:	fieldName '?'? ('as' sequenceType)?;
fieldName	:	ID;


arrayType	:	anyArrayType | typedArrayType;
anyArrayType	:	ARRAY LPAREN '*' RPAREN;
typedArrayType	:	ARRAY LPAREN sequenceType RPAREN;

enumerationType	:	ENUM LPAREN STRING (',' STRING)* RPAREN;

choiceItemType	:	LPAREN itemType ('|' itemType)* RPAREN;

anyItemTest	:	ITEM LPAREN RPAREN;
anyKindTest	:	NODE LPAREN RPAREN;

functionName: qname;
typeName: qname;

qname: (namespace COLON)* anyName;
namespace: anyName;
anyName: ID | EMPTY | COUNT;
