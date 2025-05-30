lexer grammar AntlrXqueryLexer;
INTEGER: Digits;
DECIMAL: (DOT Digits) | (Digits DOT Digits?);
fragment Digits: [0-9]+;

STRING: ('"' ('""' | ~["&])* '"')
    | ('\'' ('\'\'' | ~['&])* '\'');

COMMENT: '(:' .*? ':)'-> channel(HIDDEN);
WS: [\p{White_Space}]+ -> channel(HIDDEN);
FOR: 'for';
COMMA: ',';
AT: 'at';
LET: 'let';
COUNT: 'count';
WHERE: 'where';
WHILE: 'while';
DOLLAR: '$';
CHILD: 'child';
DESCENDANT: 'descendant';
SELF: 'self';
OTHERWISE: 'otherwise';
DESCENDANT_OR_SELF: 'descendant-or-self';
ANCESTOR_OR_SELF: 'ancestor-or-self';
FOLLOWING_OR_SELF: 'following-or-self';
FOLLOWING_SIBLING: 'following-sibling';
FOLLOWING_SIBLING_OR_SELF: 'following-sibling-or-self';
PRECEDING_OR_SELF: 'preceding-or-self';
PRECEDING_SIBLING_OR_SELF: 'preceding-sibling-or-self';
FOLLOWING: 'following';
COLONS: '::';
COLON: ':';
DOTS: '..';
DOT: '.';
PRECEDING: 'preceding';
PARENT: 'parent';
ANCESTOR: 'ancestor';
PRECEDING_SIBLING: 'preceding-sibling';
STAR: '*';
PLUS: '+';
QUESTION_MARK: '?';
LBRACKET: '[';
RBRACKET: ']';
LPAREN: '(';
RPAREN: ')';
LCURLY: '{';
RCURLY: '}';
COLONSTAR: ':*';
STARCOLON: '*:';
ALLOWING: 'allowing';
EMPTY: 'empty';
ORDER: 'order';
BY: 'by';
STABLE: 'stable';
ASCENDING: 'ascending';
DESCENDING: 'descending';
RETURN: 'return';
SOME: 'some';
EVERY: 'every';
THEN: 'then';
ELSE: 'else';
OR:'or';
AND: 'and';
TO: 'to';
DIV: 'div';
IDIV: 'idiv';
MOD: 'mod';
UNION: 'union';
INTERSECT: 'intersect';
EXCEPT: 'except';
ELEMENT: 'element';
MAP: 'map';
RECORD: 'record';
ARRAY: 'array';
ENUM: 'enum';
NODE: 'node';
ITEM: 'item';
FUNCTION: 'function';
EMPTY_SEQUENCE: 'empty-sequence';
AS: 'as';
EQ: 'eq';
NE: 'ne';
LT: 'lt';
LE: 'le';
GT: 'gt';
GE: 'ge';
MINUS: '-';
CONCATENATION: '||';
UNION_OP: '|';
EXCLAMATION_MARK: '!';
ARROW: '=>';
EQ_OP: '=';
NE_OP: '!=';
LT_OP: '<';
GT_OP: '>';
GE_OP: '>=';
LE_OP: '<=';
PRECEDING_OP: '<<';
FOLLOWING_OP: '>>';
ASSIGNMENT_OP: ':=';
IS: 'is';
SLASHES: '//';
SLASH: '/';
GREATEST: 'greatest';
LEAST: 'least';
COLLATION: 'collation';
IF: 'if';
IN: 'in';
SATISFIES: 'satisfies';
SWITCH: 'switch';
DEFAULT: 'default';
CASE: 'case';
INSTANCE: 'instance';
OF: 'of';
TREAT: 'treat';
CAST: 'cast';
PERCENTAGE: '%';


ID: NAME_START (DASH NAME_MIDDLE)*
// [\p{Alpha}][\p{Alpha}\p{Alnum}-]*
    ; /* Replace with antlr compatible */

fragment NAME_START: [\p{Alpha}][\p{Alpha}\p{Alnum}]*;
fragment NAME_MIDDLE: [\p{Alpha}\p{Alnum}]+;
fragment DASH: '-';

