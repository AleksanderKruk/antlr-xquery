parser grammar AntlrQueryTypeSystem;
import AntlrQueryQname, AntlrQueryCardinality, AntlrQueryNumericRange;

options {
    tokenVocab = AntlrQueryLexer;
}


type: sequenceTypeUnion;

sequenceTypeUnion
    : sequenceTypeIntersection (UNION_OP sequenceTypeIntersection)*
    ;

sequenceTypeIntersection
    : sequenceTypeSubtraction (HAT sequenceTypeSubtraction)*
    ;

sequenceTypeSubtraction
    : arrayTypeOperator (MINUS arrayTypeOperator)*
    ;

arrayTypeOperator
    : typePrimitive arrayOperator*
    ;

arrayOperator: anyArrayOperator | constrainedArrayOperator;
anyArrayOperator: LBRACKET RBRACKET;
constrainedArrayOperator: LBRACKET cardinality RBRACKET;

typePrimitive
    : typeGrouping
    | sequenceType
    ;

typeGrouping: LPAREN type RPAREN ;

sequenceType
    : emptySequenceType
    | anyType
    | neverType
    | constrainedSequenceType
    ;


neverType
    : EXCLAMATION_MARK
    ;

grammarEntityType
    : grammarReference
    | singleRuleReference
    | enumeratedRuleReference
    | allRulesFromGrammarReference
    | enumeratedRulesFromGrammarReference
    ;

grammarReference
    : PERCENTAGE grammarName
    ;

singleRuleReference
    : PERCENTAGE qname
    ;

enumeratedRuleReference
    : PERCENTAGE LCURLY (qname (COMMA qname)*)? RCURLY
    ;

allRulesFromGrammarReference
    : PERCENTAGE grammarName COLON STAR
    ;

enumeratedRulesFromGrammarReference
    : PERCENTAGE grammarName COLON LCURLY anyName+ RCURLY
    ;

grammarName: anyName;

regexType
    : REGEX
    ;

emptySequenceType: EMPTY_SEQUENCE? LPAREN RPAREN;

anyType: STAR;

constrainedSequenceType: itemType cardinality?;

itemType
    : anyItem
    | stringType
    | numberType
    | booleanType
    | treeNodeType
    | grammarEntityType
    | regexType
    | functionType
    | mapLikeType
    | arrayLikeType
    | enumerationType
    | choiceItemType
    | typeName
    ;

mapLikeType
    : mapType
    | recordType
    ;

booleanType
    : anyBooleanType
    | trueType
    | falseType
    ;

anyBooleanType: BOOLEAN;
trueType: TRUE;
falseType: FALSE;

numberType
    : anyNumber
    | simpleNumberType
    | numericSet
    ;

simpleNumberType: numericRangeTerm;

anyNumber: NUMBER;
numericSet
    : NUMBER? LPAREN numericRange RPAREN
    ;

treeNodeType
    : nodeType
    | ruleType
    | tokenType
    ;

nodeType
    : constrainedNodeType
    | anyNodeType
    ;

constrainedNodeType
    : qnameEnumeratedNodeType
    | anyNodeTypeFromGrammar
    | enumeratedNodeTypeFromGrammar
    ;

qnameEnumeratedNodeType
    : NODE LPAREN qname (UNION_OP qname)* RPAREN
    | LT_OP qname (UNION_OP qname)* GT_OP
    ;

anyNodeTypeFromGrammar
    : NODE LPAREN namespace COLON STAR RPAREN
    | LT_OP namespace COLON STAR GT_OP
    ;

enumeratedNodeTypeFromGrammar
    : NODE LPAREN namespace COLON LCURLY anyName+ RCURLY RPAREN
    | LT_OP namespace COLON LCURLY anyName+ RCURLY GT_OP
    ;

anyNodeType: NODE (LPAREN STAR? RPAREN)?;


ruleType
    : constrainedRuleType
    | anyRuleType
    ;

constrainedRuleType
    : qnameEnumeratedRuleType
    | anyRuleTypeFromGrammar
    | enumeratedRuleTypeFromGrammar
    ;

qnameEnumeratedRuleType
    : RULE LPAREN qname (UNION_OP qname)* RPAREN
    ;

anyRuleTypeFromGrammar
    : RULE LPAREN namespace COLON STAR RPAREN
    ;

enumeratedRuleTypeFromGrammar
    : RULE LPAREN namespace COLON LCURLY anyName+ RCURLY RPAREN
    ;


anyRuleType: RULE (LPAREN RPAREN)?;

tokenType
    : constrainedTokenType
    | anyTokenType
    ;

anyTokenType: TOKEN (LPAREN RPAREN)?;

constrainedTokenType
    : qnameEnumeratedTokenType
    | anyTokenTypeFromGrammar
    | enumeratedTokenTypeFromGrammar
    ;

qnameEnumeratedTokenType
    : RULE LPAREN qname (UNION_OP qname)* RPAREN
    ;

anyTokenTypeFromGrammar
    : RULE LPAREN namespace COLON STAR RPAREN
    ;

enumeratedTokenTypeFromGrammar
    : RULE LPAREN namespace COLON LCURLY anyName+ RCURLY RPAREN
    ;


functionType
    : annotation* (anyFunctionType | typedFunctionType)
    ;

annotation:
    PERCENTAGE qname (LPAREN annotationValue (COMMA annotationValue)* RPAREN)?;

annotationValue:
    STRING | (MINUS? numericLiteral) | (qname LPAREN RPAREN);


numericLiteral
    : IntegerLiteral        # integerLiteral
    | HexIntegerLiteral     # hexIntegerLiteral
    | BinaryIntegerLiteral  # binaryIntegerLiteral
    | DecimalLiteral        # decimalLiteral
    | DoubleLiteral         # doubleLiteral
    ;



anyFunctionType:
    FUNCTION LPAREN STAR RPAREN;

typedFunctionType:
    FUNCTION LPAREN (typedFunctionParam (COMMA typedFunctionParam)*)? RPAREN AS type;

typedFunctionParam:
    (paramName AS)? type;

paramName: DOLLAR qname;

mapType
    : anyMapType
    | typedMapType
    | anyKeyMapType
    | anyValueMapType
    ;

anyMapType
    : MAP LPAREN STAR RPAREN
    | LCURLY STAR RCURLY
    | LCURLY STAR COLON STAR RCURLY
    ;

typedMapType
    : MAP LPAREN itemType COMMA type RPAREN
    | LCURLY itemType COLON type RCURLY
    ;

anyKeyMapType
    : MAP LPAREN STAR COMMA type RPAREN
    | LCURLY STAR COLON type RCURLY
    ;

anyValueMapType
    : MAP LPAREN itemType COMMA STAR RPAREN
    | LCURLY type COLON STAR RCURLY
    ;

constrainedMapType
    : MAP LPAREN itemType COMMA type RPAREN
    | LCURLY itemType COLON type RCURLY
    ;


recordType
    : constrainedRecordType
    ;

constrainedRecordType
    : RECORD LPAREN (fieldDeclaration (COMMA fieldDeclaration)*)? extensibleType? RPAREN
    | LCURLY (fieldDeclaration (COMMA fieldDeclaration)*)? extensibleType? RCURLY
    ;

extensibleType
    : itemType? STAR
    ;

fieldDeclaration
    : fieldName QUESTION_MARK? (AS type)?
    ;

fieldName: anyName;

arrayLikeType
    : explicitArrayType
    | tupleType
    ;

explicitArrayType
    : explicitArrayTypeNoCardinality
    | explicitArrayTypeWithCardinality
    ;

explicitArrayTypeNoCardinality
    : ARRAY LPAREN type RPAREN
    ;

explicitArrayTypeWithCardinality
    : ARRAY LPAREN type COMMA cardinality RPAREN
    ;

tupleType
    : LBRACKET (type (COMMA type)*)? RBRACKET
    ;

stringType
    : constrainedString
    | anyString
    ;

anyString
    : STRING_W
    ;

constrainedString
    : STRING_W LPAREN cardinality RPAREN
    ;

enumerationType
    : ( ENUM | STRING_W ) LPAREN STRING (COMMA STRING)* RPAREN
    ;

choiceItemType
    : LPAREN itemType (UNION_OP itemType)* RPAREN
    ;

anyItem
    : ITEM (LPAREN RPAREN)?
    ;

functionName: qname;

typeName: qname;

