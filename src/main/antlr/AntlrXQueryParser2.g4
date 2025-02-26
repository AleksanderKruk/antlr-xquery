parser grammar AntlrXQueryParser2;
options {
    tokenVocab = AntlrXQueryLexer;
}
query: expr;
expr: exprSingle (',' exprSingle)*;
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
forClause: 'for' forBinding (',' forBinding)*;
forBinding: '$' varName typeDeclaration? allowingEmpty? positionalVar? 'in' exprSingle;
allowingEmpty: 'allowing' 'empty';
positionalVar: 'at' '$' varName;
letClause: 'let' letBinding (',' letBinding)*;
letBinding: '$' varName typeDeclaration? ':=' exprSingle;
countClause: 'count' '$' varName;
whereClause: 'where' exprSingle;
orderByClause: (('order' 'by') | ('stable' 'order' 'by')) orderSpecList;
orderSpecList: orderSpec (',' orderSpec)*;
orderSpec: exprSingle orderModifier;
orderModifier: ('ascending' | 'descending')? ('empty' ('greatest' | 'least'))? ('collation' uRILiteral)?;
returnClause: 'return' exprSingle;
quantifiedExpr: ('some' | 'every') '$' varName typeDeclaration? 'in' exprSingle (',' '$' varName typeDeclaration? 'in' exprSingle)* 'satisfies' exprSingle;
ifExpr: 'if' '(' expr ')' 'then' exprSingle 'else' exprSingle;
orExpr: orExpr ( 'or' orExpr )*
        | orExpr ( 'and' orExpr )*
        | orExpr ( (valueComp | generalComp | nodeComp) orExpr )?
        | orExpr ( '||' orExpr )*
        | orExpr ( 'to' orExpr )?
        | orExpr ( ('+' | '-') orExpr )*
        | orExpr ( ('*' | 'div' | 'idiv' | 'mod') orExpr )*
        | orExpr ( ('union' | '|') orExpr )*
        | orExpr ( ('intersect' | 'except') orExpr)*
        | orExpr ( '=>' arrowFunctionSpecifier argumentList )*
        | ('-' | '+')* orExpr
        | pathExpr ('!' pathExpr)*
;
generalComp: '=' | '!=' | '<' | '<=' | '>' | '>=';
valueComp: 'eq' | 'ne' | 'lt' | 'le' | 'gt' | 'ge';
nodeComp: 'is' | '<<' | '>>';
pathExpr: ('/' relativePathExpr?)
        | ('//' relativePathExpr)
        | relativePathExpr; /* xgc: leading-lone-slash */
relativePathExpr: stepExpr (('/' | '//') stepExpr)*;
stepExpr: postfixExpr | axisStep;
axisStep: (reverseStep | forwardStep) predicateList;
forwardStep: (forwardAxis nodeTest) | abbrevForwardStep;
forwardAxis: ('child' '::')
        | ('descendant' '::')
        | ('attribute' '::')
        | ('self' '::')
        | ('descendant-or-self' '::')
        | ('following-sibling' '::')
        | ('following' '::');
abbrevForwardStep: '@'? nodeTest;
reverseStep: (reverseAxis nodeTest) | abbrevReverseStep;
reverseAxis: ('parent' '::')
        | ('ancestor' '::')
        | ('preceding-sibling' '::')
        | ('preceding' '::')
        | ('ancestor-or-self' '::');
abbrevReverseStep: '..';
nodeTest: nameTest;
nameTest: ID | wildcard;
wildcard: '*'
        | (ID ':*')
        | ('*:' ID);
postfixExpr: primaryExpr (predicate | argumentList)*;
argumentList: '(' (argument (',' argument)*)? ')';
predicateList: predicate*;
predicate: '[' expr ']';
arrowFunctionSpecifier: ID | varRef | parenthesizedExpr;
primaryExpr: literal
| varRef
| parenthesizedExpr
| contextItemExpr
| functionCall;
literal: numericLiteral | StringLiteral;
numericLiteral: IntegerLiteral | DecimalLiteral | DoubleLiteral;
varRef: '$' varName;
varName: ID;
parenthesizedExpr: '(' expr? ')';
contextItemExpr: '.';
functionCall: ID argumentList; /* xgc: reserved-function-names */
/* gn: parens */
argument: exprSingle | argumentPlaceholder;
argumentPlaceholder: '?';
elementDeclaration: elementName;
attributeName: ID;
elementName: ID;
simpleTypeName: typeName;
typeName: ID;
uRILiteral: StringLiteral;

typeDeclaration: 'as' sequenceType;
sequenceType: (ID occurrenceIndicator?);
occurrenceIndicator: '?' | '*' | '+';