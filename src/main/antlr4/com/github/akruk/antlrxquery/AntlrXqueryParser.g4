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
        | tryCatchExpr
        | orExpr;
fLWORExpr: initialClause intermediateClause* returnClause;
initialClause: forClause
            | windowClause
            | letClause;
intermediateClause: initialClause
                | windowClause
                | whereClause
                | whileClause
                | orderByClause
                | countClause;

forClause
    : FOR forBinding (COMMA forBinding)*
    ;

forBinding
    : forItemBinding
    | forMemberBinding
    | forEntryBinding
    ;

forItemBinding
    : varNameAndType allowingEmpty? positionalVar? IN exprSingle
    ;

allowingEmpty
    : ALLOWING EMPTY
    ;

forMemberBinding
    : MEMBER varNameAndType positionalVar? IN exprSingle
    ;

forEntryBinding
    : ((forEntryKeyBinding forEntryValueBinding?) | forEntryValueBinding) positionalVar? IN exprSingle
    ;

forEntryKeyBinding
    : KEY varNameAndType
    ;

forEntryValueBinding
    : VALUE varNameAndType
    ;


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

quantifiedExpr: (SOME | EVERY) quantifierBinding (COMMA quantifierBinding)* SATISFIES exprSingle;
quantifierBinding	:	varNameAndType IN exprSingle;


ifExpr	:	IF LPAREN expr RPAREN (unbracedActions | bracedAction);
otherwiseExpr	:	stringConcatExpr (OTHERWISE stringConcatExpr)*;
unbracedActions	:	THEN exprSingle ELSE exprSingle;
bracedAction	:	enclosedExpr;
enclosedExpr	:	LCURLY expr? RCURLY;

switchExpr	:	SWITCH switchComparand (switchCases | bracedSwitchCases);
switchComparand	:	LPAREN switchedExpr=expr? RPAREN;
switchCases	:	switchCaseClause+ DEFAULT RETURN defaultExpr=exprSingle;
switchCaseClause: (CASE switchCaseOperand)+ RETURN exprSingle;
switchCaseOperand	:	expr;
bracedSwitchCases	:	LCURLY switchCases RCURLY;


tryCatchExpr : tryClause ( (catchClause+ finallyClause?) | finallyClause ) ;
tryClause : TRY enclosedExpr ;
catchClause : CATCH (pureNameTestUnion | wildcard) enclosedExpr ;
finallyClause : FINALLY enclosedExpr ;
pureNameTestUnion	:	nameTest ('|' nameTest)*;






orExpr: andExpr ( OR andExpr)*;
andExpr: comparisonExpr ( AND comparisonExpr )*;
comparisonExpr: otherwiseExpr ((valueComp | generalComp | nodeComp) otherwiseExpr)?;
stringConcatExpr: rangeExpr ( CONCATENATION rangeExpr )*;
rangeExpr: additiveExpr (TO additiveExpr)?;
additiveExpr: multiplicativeExpr (additiveOperator multiplicativeExpr)*;
multiplicativeExpr: unionExpr (multiplicativeOperator unionExpr )*;
unionExpr: intersectExpr (unionOperator intersectExpr)*;
intersectExpr: instanceofExpr (exceptOrIntersect instanceofExpr)*;
instanceofExpr: treatExpr (INSTANCE OF sequenceType)?;
treatExpr: castableExpr (TREAT AS sequenceType)?;
castableExpr: castExpr ((CASTABLE AS) castTarget)?;
  castTarget: (typeName | choiceItemType | enumerationType) QUESTION_MARK?;
castExpr: pipelineExpr ((CAST AS) castTarget)?;
pipelineExpr:	arrowExpr (PIPE_ARROW arrowExpr)*;
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
multiplicativeOperator: STAR | DIV | DIV_OP | IDIV | MOD | MULTIPLICATION;
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
nodeTest: pathNameTestUnion | wildcard;
wildcard: STAR
        | (ID COLONSTAR)
        | (STARCOLON ID);
// postfix: predicate | argumentList;
argumentList: LPAREN (positionalArguments (COMMA keywordArguments)? | keywordArguments)? RPAREN;
positionalArguments: argument (COMMA argument)*;
keywordArguments: keywordArgument (COMMA keywordArgument)* ;
keywordArgument: qname ASSIGNMENT_OP argument;
argument: exprSingle | argumentPlaceholder;
argumentPlaceholder: QUESTION_MARK;



mapConstructor	: MAP? LCURLY (mapConstructorEntry (COMMA mapConstructorEntry)*)? RCURLY;
mapConstructorEntry	:	mapKeyExpr COLON mapValueExpr;
mapKeyExpr	:	exprSingle;
mapValueExpr	:	exprSingle;

arrayConstructor
    : squareArrayConstructor
    | curlyArrayConstructor
    ;

squareArrayConstructor
    : LBRACKET (exprSingle (COMMA exprSingle)*)? RBRACKET
    ;

curlyArrayConstructor
    : ARRAY enclosedExpr
    ;




predicateList: predicate*;
predicate: LBRACKET expr RBRACKET;
arrowFunctionSpecifier: ID | varRef | parenthesizedExpr;
primaryExpr: literal
        | varRef
        | parenthesizedExpr
        | contextItemExpr // contextValueRef
        | functionCall
        // | orderedExpr
        // | unorderedExpr
        // | nodeConstructor
        // | nodeConstructor
        // | stringInterpolation
        | functionItemExpr
        | mapConstructor
        | arrayConstructor
        // | stringTemplate
        | stringConstructor
        | unaryLookup
        ;

functionItemExpr    :	namedFunctionRef
    | inlineFunctionExpr
    ;
namedFunctionRef	:	qname HASH IntegerLiteral;

literal:
  numericLiteral
  | STRING;

numericLiteral
    : IntegerLiteral
    | HexIntegerLiteral
    | BinaryIntegerLiteral
    | DecimalLiteral
    |  DoubleLiteral
    ;

varRef: DOLLAR varName;
varName: qname;
parenthesizedExpr: LPAREN expr? RPAREN;
contextItemExpr: DOT;
functionCall: functionName argumentList;
typeDeclaration: AS sequenceType;
occurrenceIndicator: QUESTION_MARK | STAR | PLUS;
sequenceTypeUnion: sequenceType (UNION_OP sequenceType)*;

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


pathNameTestUnion	:	qname ('|' qname)*;

nameTestUnion	:	nameTest ('|' nameTest)*;
nameTest	:	qname | wildcard;

functionType:	annotation* (anyFunctionType | typedFunctionType);
annotation	:	PERCENTAGE qname (LPAREN annotationValue (COMMA annotationValue)* RPAREN)?;
annotationValue:	STRING | ('-'? numericLiteral) | (qname LPAREN RPAREN);
anyFunctionType	:	FUNCTION LPAREN STAR RPAREN;
typedFunctionType	:	FUNCTION LPAREN (typedFunctionParam (COMMA typedFunctionParam)*)? RPAREN 'as' sequenceType;
typedFunctionParam	:	(DOLLAR qname AS)? sequenceType;

mapType	:	anyMapType | typedMapType;
anyMapType	:	MAP LPAREN STAR RPAREN;
typedMapType	:	MAP LPAREN itemType COMMA sequenceType RPAREN;


recordType:	anyRecordType | typedRecordType;
anyRecordType: RECORD LPAREN STAR RPAREN;
typedRecordType: RECORD LPAREN (fieldDeclaration (COMMA fieldDeclaration)*)? extensibleFlag? RPAREN;
extensibleFlag:	COMMA STAR;
fieldDeclaration	:	fieldName QUESTION_MARK? (AS sequenceType)?;
fieldName	:	ID;


arrayType	:	anyArrayType | typedArrayType;
anyArrayType	:	ARRAY LPAREN STAR RPAREN;
typedArrayType	:	ARRAY LPAREN sequenceType RPAREN;

enumerationType	:	ENUM LPAREN STRING (COMMA STRING)* RPAREN;

choiceItemType	:	LPAREN itemType (UNION_OP itemType)* RPAREN;

anyItemTest	:	ITEM LPAREN RPAREN;
anyKindTest	:	NODE LPAREN RPAREN;

functionName: qname;
typeName: qname;



postfixExpr
    : primaryExpr                         # postfixPrimary
    | postfixExpr predicate               # filterExpr
    | postfixExpr positionalArgumentList  # dynamicFunctionCall
    | postfixExpr lookup                  # lookupExpr
    | postfixExpr LOOKUP expr RBRACKET    # filterExprAMLookup
    ;

positionalArgumentList:	LPAREN positionalArguments? RPAREN;

lookup
    : QUESTION_MARK keySpecifier
    ;

keySpecifier
    : qname
    | IntegerLiteral
    | STRING
    | varRef
    | parenthesizedExpr
    | lookupWildcard
    ;


lookupWildcard: STAR;

unaryLookup
    : lookup
    ;










qname: (namespace COLON)* anyName;
namespace: anyName;
anyName: ID
        | EMPTY
        | COUNT
        | MULTIPLICATION
        | ALLOWING
        | ANCESTOR
        | ANCESTOR_OR_SELF
        | AND
        | ARRAY
        | AS
        | ASCENDING
        | AT
        | BY
        | CASE
        | CAST
        | CHILD
        | COLLATION
        | COUNT
        | DEFAULT
        | DESCENDANT
        | DESCENDANT_OR_SELF
        | DESCENDING
        | ELEMENT
        | ELSE
        | EMPTY
        | EMPTY_SEQUENCE
        | END
        | ENUM
        | EQ
        | EVERY
        | EXCEPT
        | FOLLOWING
        | FOLLOWING_OR_SELF
        | FOLLOWING_SIBLING
        | FOLLOWING_SIBLING_OR_SELF
        | FOR
        | FN
        | FUNCTION
        | GE
        | GREATEST
        | GT
        | IDIV
        | IF
        | IN
        | INSTANCE
        | INTERSECT
        | IS
        | ITEM
        | KEY
        | VALUE
        | LE
        | LEAST
        | LET
        | LT
        | MAP
        | MEMBER
        | MOD
        | MULTIPLICATION
        | NE
        | NEXT
        | NODE
        | OF
        | ONLY
        | PARENT
        | PRECEDING
        | PRECEDING_OR_SELF
        | PRECEDING_SIBLING
        | PRECEDING_SIBLING_OR_SELF
        | PREVIOUS
        | RECORD
        | RETURN
        | SATISFIES
        | SELF
        | SLIDING
        | SOME
        | THEN
        | TO
        | THEN
        | TREAT
        | TUMBLING
        | UNION
        | WHEN
        | WHERE
        | WHILE
        | WINDOW
        ;

stringConstructor:
    STRING_CONSTRUCTOR_START
    stringConstructorContent
    STRING_CONSTRUCTOR_END
    ;

stringConstructorContent:
    (constructorChars | constructorInterpolation)*
    ;

constructorChars:
    (CONSTRUCTOR_CHARS | BACKTICK | BRACKET)+
    ;

constructorInterpolation:
    CONSTRUCTION_START
    expr?
    CONSTRUCTION_END
    ;


// stringInterpolation:
//     STRING_INTERPOLATION_START
//     stringInterpolationContent
//     STRING_INTERPOLATION_END?
//     ;

// stringInterpolationContent:
//     (interpolationChars | interpolationInterpolation)*
//     ;

// interpolationChars:
//     INTERPOLATION_CHARS
//     ;

// interpolationInterpolation:
//     INTERPOLATION_START
//     expr?
//     RCURLY
//     ;

windowClause
    : FOR (tumblingWindowClause | slidingWindowClause)
    ;

tumblingWindowClause
    : TUMBLING WINDOW varNameAndType IN exprSingle windowStartCondition? windowEndCondition?
    ;

slidingWindowClause
    : SLIDING WINDOW varNameAndType IN exprSingle windowStartCondition? windowEndCondition
    ;

varNameAndType
    : DOLLAR qname typeDeclaration?
    ;

windowStartCondition
    : START windowVars (WHEN exprSingle)?
    ;

windowEndCondition
    : ONLY? END windowVars (WHEN exprSingle)?
    ;

windowVars
    : currentVar? positionalVar? previousVar? nextVar?
    ;

currentVar
    : varRef
    ;

previousVar
    : PREVIOUS varRef
    ;

nextVar
    : NEXT varRef
    ;



inlineFunctionExpr
    : annotation* (FUNCTION | FN) functionSignature? functionBody
    ;

functionSignature
    : '(' paramList ')' typeDeclaration?
    ;

paramList
    : (varNameAndType (',' varNameAndType)*)?
    ;

functionBody
    : enclosedExpr
    ;
