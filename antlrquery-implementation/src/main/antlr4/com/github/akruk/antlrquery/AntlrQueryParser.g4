parser grammar AntlrQueryParser;

import AntlrQueryQname, AntlrQueryTypeSystem, AntlrQueryCardinality, AntlrQueryNumericRange;

options {
    tokenVocab = AntlrQueryLexer;
}


xquery
    : versionDecl? (libraryModule | mainModule) EOF
    ;

expr
    : exprSingle (COMMA exprSingle)*
    ;

exprSingle
    : fLWORExpr
    | quantifiedExpr
    | ifExpr
    | switchExpr
    | typeswitchExpr
    | tryCatchExpr
    | orExpr
    ;

fLWORExpr
    : initialClause intermediateClause* returnClause;

initialClause
    : forClause
    | windowClause
    | letClause
    ;

intermediateClause
    : forClause
    | windowClause
    | letClause
    | windowClause
    | whereClause
    | whileClause
    | orderByClause
    | groupByClause
    | countClause
    ;

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


positionalVar: AT varName;

// TODO: let destructuring
// letSequenceBinding	:	'$" '(" (varNameAndType ++ ',") ')" typeDeclaration? ':=' exprSingle
// letArrayBinding	:	'$' '[' (varNameAndType ++ ',') ']' typeDeclaration? ':=' exprSingle
// letMapBinding	:	'$" '{" (varNameAndType ++ ',") '}" typeDeclaration? ':=" exprSingle
letClause:
    LET letBinding (COMMA letBinding)*;

letBinding:
    varNameAndType ASSIGNMENT_OP exprSingle;

countClause:
    COUNT varName;

whereClause:
    WHERE exprSingle;

whileClause:
    WHILE exprSingle;

orderByClause:
    ((ORDER BY) | (STABLE ORDER BY)) orderSpecList;

orderSpecList:
    orderSpec (COMMA orderSpec)*;

orderSpec:
    exprSingle orderModifier;

orderModifier:
    (ASCENDING | DESCENDING)? (EMPTY (GREATEST | LEAST))?;

returnClause:
    RETURN exprSingle;

groupByClause
    : GROUP BY groupingSpec (COMMA groupingSpec)*
    ;

groupingSpec
    : varNameAndType (ASSIGNMENT_OP exprSingle)? (COLLATION STRING)?
    ;

quantifiedExpr:
    (SOME | EVERY) quantifierBinding (COMMA quantifierBinding)* SATISFIES exprSingle;

quantifierBinding:
    varNameAndType IN exprSingle;

ifExpr:
    IF LPAREN expr RPAREN (unbracedActions | bracedAction);

otherwiseExpr:
    stringConcatExpr (OTHERWISE stringConcatExpr)*;

unbracedActions:
    THEN exprSingle ELSE exprSingle;

bracedAction:
    enclosedExpr;

enclosedExpr:
    LCURLY expr? RCURLY;

switchExpr:
    SWITCH switchComparand (switchCases | bracedSwitchCases);

switchComparand:
    LPAREN switchedExpr=expr? RPAREN;

switchCases:
    switchCaseClause+ DEFAULT RETURN defaultExpr=exprSingle;

switchCaseClause:
    (CASE switchCaseOperand)+ RETURN exprSingle;

switchCaseOperand:
    expr;

bracedSwitchCases:
    LCURLY switchCases RCURLY;


tryCatchExpr : tryClause ( (catchClause+ finallyClause?) | finallyClause ) ;

tryClause : TRY enclosedExpr ;

catchClause : CATCH (pureNameTestUnion | wildcard) enclosedExpr ;

finallyClause : FINALLY enclosedExpr ;

pureNameTestUnion: nameTest (UNION_OP nameTest)*;

nameTest:
    qname | wildcard;


typeswitchExpr
    : TYPESWITCH LPAREN expr RPAREN (typeswitchCases | bracedTypeswitchCases)
    ;

typeswitchCases
    : caseClause+ DEFAULT varName? RETURN exprSingle
    ;

caseClause
    : CASE (varName AS)? type RETURN exprSingle
    ;

bracedTypeswitchCases
    : LCURLY typeswitchCases RCURLY
    ;



orExpr: andExpr ( OR andExpr)*;
andExpr: comparisonExpr ( AND comparisonExpr )*;
comparisonExpr: otherwiseExpr ((valueComp | generalComp | nodeComp) otherwiseExpr)?;
stringConcatExpr: rangeExpr ( CONCATENATION rangeExpr )*;
rangeExpr: additiveExpr (TO additiveExpr)?;
additiveExpr: multiplicativeExpr (additiveOperator multiplicativeExpr)*;
multiplicativeExpr: unionExpr (multiplicativeOperator unionExpr )*;
unionExpr: intersectExpr (unionOperator intersectExpr)*;
intersectExpr: instanceofExpr (exceptOrIntersect instanceofExpr)*;
instanceofExpr: treatExpr (INSTANCE OF type)?;
treatExpr: castableExpr (TREAT AS type)?;
castableExpr: castExpr (CASTABLE AS castTarget)?;
castTarget: (typeName | choiceItemType | enumerationType) QUESTION_MARK?;
castExpr: pipelineExpr (CAST AS castTarget)?;
pipelineExpr:arrowExpr (PIPE_ARROW arrowExpr)*;

arrowExpr
    : unaryExpr (sequenceArrowTarget | mappingArrowTarget)*
    ;

sequenceArrowTarget
    : ARROW arrowTarget
    ;

arrowTarget
    : functionCall
    | restrictedDynamicCall
    ;

restrictedDynamicCall
    : (varRef | parenthesizedExpr | functionItemExpr | mapConstructor | arrayConstructor) positionalArgumentList
    ;

functionItemExpr
    : namedFunctionRef
    | inlineFunctionExpr
    ;

mappingArrowTarget
    : MAPPING_ARROW arrowTarget
    ;

unaryExpr: (MINUS | PLUS)? simpleMapExpr;
simpleMapExpr: pathExpr (EXCLAMATION_MARK pathExpr)*;
pathExpr
    : SLASH relativePathExpr?
    | SLASHES relativePathExpr
    | relativePathExpr
    ;

relativePathExpr: stepExpr (pathOperator stepExpr)*;
stepExpr: postfixExpr | axisStep;
axisStep: (reverseStep | forwardStep) predicateList;
forwardStep: forwardAxis? nodeTest;

additiveOperator: PLUS | MINUS;
unionOperator: UNION | UNION_OP;
multiplicativeOperator: STAR | DIV | DIV_OP | IDIV | MOD | MULTIPLICATION;
generalComp: EQ_OP | NE_OP | LT_OP | LE_OP | GT_OP | GE_OP;
valueComp: EQ | NE | LT | LE | GT | GE;
nodeComp: IS | IS_NOT | PRECEDING_OP | FOLLOWING_OP | FOLLOWS | PRECEDES | PRECEDES_OR_IS | FOLLOWS_OR_IS;
pathOperator: SLASH | SLASHES;


exceptOrIntersect: EXCEPT | INTERSECT;


// TODO: add remaining combinations of axes
forwardAxis
    : CHILD COLONS                  # childAxis
    | DESCENDANT COLONS                # descendantAxis
    | SELF COLONS                      # selfAxis
    | DESCENDANT_OR_SELF COLONS        # descendantOrSelfAxis
    | FOLLOWING_SIBLING COLONS         # followingSiblingAxis
    | FOLLOWING COLONS                 # followingAxis
    | FOLLOWING_SIBLING_OR_SELF COLONS # followingSiblingOrSelfAxis
    | FOLLOWING_OR_SELF COLONS         # followingOrSelfAxis;

reverseStep
    : (reverseAxis nodeTest)
    | abbrevReverseStep
    ;

reverseAxis
    : PARENT COLONS                    # parentAxis
    | PRECEDING_SIBLING_OR_SELF COLONS # precedingSiblingOrSelfAxis
    | PRECEDING_OR_SELF COLONS         # precedingOrSelfAxis
    | ANCESTOR COLONS                  # ancestorAxis
    | PRECEDING_SIBLING COLONS         # precedingSiblingAxis
    | PRECEDING COLONS                 # precedingAxis
    | ANCESTOR_OR_SELF COLONS          # ancestorOrSelfAxis;

abbrevReverseStep
    : DOTS
    ;

nodeTest: pathNameTestUnion | wildcard;

pathNameTestUnion
    : qname
    | LPAREN qname (UNION_OP qname)* RPAREN;

wildcard: STAR
        | (ID COLONSTAR)
        | (STARCOLON ID);
// postfix: predicate | argumentList;

argumentList
    : LPAREN (positionalArguments (COMMA keywordArguments)? | keywordArguments)? RPAREN
    ;

positionalArguments
    : argument (COMMA argument)*
    ;

keywordArguments
    : keywordArgument (COMMA keywordArgument)*
    ;

keywordArgument
    : qname ASSIGNMENT_OP argument
    ;
argument
    : exprSingle | argumentPlaceholder
    ;

argumentPlaceholder: QUESTION_MARK;



mapConstructor: (MAP|RECORD)? LCURLY (mapConstructorEntry (COMMA mapConstructorEntry)*)? RCURLY;
mapConstructorEntry:mapKeyExpr COLON mapValueExpr;
mapKeyExpr:exprSingle;
mapValueExpr:exprSingle;

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
primaryExpr
    : literal
    | varRef
    | parenthesizedExpr
    | contextValueRef
    | functionCall
    | functionItemExpr
    | mapConstructor
    | arrayConstructor
    | stringConstructor
    | stringInterpolation
    | unaryLookup
    // | nodeConstructor
    ;


namedFunctionRef:
    qname HASH IntegerLiteral;

literal:
  numericLiteral
  | STRING;

numericLiteral
    : IntegerLiteral        # integerLiteral
    | HexIntegerLiteral     # hexIntegerLiteral
    | BinaryIntegerLiteral  # binaryIntegerLiteral
    | DecimalLiteral        # decimalLiteral
    | DoubleLiteral         # doubleLiteral
    ;

varRef: DOLLAR qname;
varName: DOLLAR qname;
parenthesizedExpr: LPAREN expr? RPAREN;
contextValueRef: DOT;
functionCall: functionName argumentList;
typeDeclaration: AS type;


postfixExpr
    : primaryExpr                         # postfixPrimary
    | postfixExpr positionalArgumentList  # dynamicFunctionCall
    | postfixExpr predicate               # filterExpr
    | postfixExpr lookup                  # lookupExpr
    | postfixExpr LOOKUP expr RBRACKET    # filterExprAMLookup
    ;

positionalArgumentList:LPAREN positionalArguments? RPAREN;

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


functionDecl
    : DECLARE annotation* FUNCTION qname LPAREN paramListWithDefaults? RPAREN typeDeclaration? (functionBody | EXTERNAL)
    ;

paramListWithDefaults
    : paramWithDefault (COMMA paramWithDefault)*
    ;

paramWithDefault
    : varNameAndType (EQ_OP exprSingle)?
    ;

functionBody
    : enclosedExpr
    ;


versionDecl
    : VERSION STRING (ENCODING STRING)? SEPARATOR
    ;

libraryModule
    : moduleDecl prolog
    ;

moduleDecl
    : MODULE qname SEPARATOR
    ;

prolog
    : ((defaultNamespaceDecl | setter | namespaceDecl | importDecl) SEPARATOR)*
      ((contextValueDecl | varDecl | functionDecl | itemTypeDecl | namedRecordTypeDecl | optionDecl) SEPARATOR)*
    ;

defaultNamespaceDecl
    : DECLARE DEFAULT FUNCTION NAMESPACE qname
    | DECLARE DEFAULT TYPE NAMESPACE qname
    | DECLARE DEFAULT ELEMENT NAMESPACE qname
    | DECLARE DEFAULT ANNOTATION NAMESPACE qname
    | DECLARE DEFAULT CONSTRUCTION NAMESPACE qname
    ;

setter
    : boundarySpaceDecl
    | defaultCollationDecl
    | baseURIDecl
    | constructionDecl
    | emptyOrderDecl
    | decimalFormatDecl
    ;

boundarySpaceDecl
    : DECLARE BOUNDARY_SPACE (PRESERVE | STRIP)
    ;

defaultCollationDecl
    : DECLARE DEFAULT COLLATION STRING
    ;

baseURIDecl
    : DECLARE BASE_URI STRING
    ;

constructionDecl
    : DECLARE CONSTRUCTION (STRIP | PRESERVE)
    ;

emptyOrderDecl
    : DECLARE DEFAULT ORDER EMPTY (GREATEST | LEAST)
    ;


decimalFormatDecl
    : DECLARE ((DECIMAL_FORMAT qname) | (DEFAULT DECIMAL_FORMAT)) (dfPropertyName EQ_OP STRING)*
    ;

dfPropertyName
    : DECIMAL_SEPARATOR
    | GROUPING_SEPARATOR
    | INFINITY
    | MINUS_SIGN
    | NAN
    | PERCENT
    | PER_MILLE
    | ZERO_DIGIT
    | DIGIT
    | PATTERN_SEPARATOR
    | EXPONENT_SEPARATOR
    ;



namespaceDecl
    : DECLARE NAMESPACE qname
    ;

importDecl
    : grammarImport
    | moduleImport
    ;

grammarImport
    : IMPORT GRAMMAR namespacePrefix STRING (COMMA STRING)* # namespaceGrammarImport
    | IMPORT GRAMMAR STRING (COMMA STRING)* # pathOnlyGrammarImport
    | IMPORT GRAMMAR qname # defaultPathGrammarImport
    ;

moduleImport
    : IMPORT MODULE STRING # pathModuleImport
    | IMPORT MODULE namespacePrefix STRING # namespaceModuleImport
    | IMPORT MODULE qname # defaultPathModuleImport
    ;

namespacePrefix:
    qname EQ_OP
    ;

contextValueDecl
    : DECLARE CONTEXT VALUE (AS type)?
      ((EQ_OP varValue) | (EXTERNAL (EQ_OP varDefaultValue)?))
    ;

varValue: exprSingle;

varDefaultValue: exprSingle;

varDecl
    : DECLARE annotation* VARIABLE varNameAndType ((EQ_OP varValue) | (EXTERNAL (EQ_OP varDefaultValue)?))
    ;

itemTypeDecl
    : DECLARE annotation* TYPE qname AS itemType
    ;

namedRecordTypeDecl
    : DECLARE annotation* RECORD qname LPAREN (extendedFieldDeclaration (COMMA extendedFieldDeclaration)*)? extensibleType? RPAREN
    ;

extendedFieldDeclaration
    : fieldDeclaration (EQ_OP exprSingle)?
    ;



optionDecl
    : DECLARE OPTION qname STRING
    ;

mainModule
    : prolog queryBody
    ;

queryBody
    : expr?
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

constructorInterpolation
    : CONSTRUCTION_START expr? RCURLY BACKTICK
    ;


stringInterpolation:
    STRING_INTERPOLATION_START stringInterpolationContent STRING_INTERPOLATION_END
    ;

stringInterpolationContent:
    (interpolationChars | interpolationInterpolation)*
    ;

interpolationChars:
    INTERPOLATION_CHARS
    ;

interpolationInterpolation:
    INTERPOLATION_START expr? RCURLY
    ;

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
    : varName typeDeclaration?
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
    : varName
    ;

previousVar
    : PREVIOUS varName
    ;

nextVar
    : NEXT varName
    ;



inlineFunctionExpr
    : annotation* (FUNCTION | FN) functionSignature? functionBody
    ;

functionSignature
    : LPAREN paramList RPAREN typeDeclaration?
    ;

paramList
    : (varNameAndType (COMMA varNameAndType)*)?
    ;

