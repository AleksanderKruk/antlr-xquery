lexer grammar AntlrXqueryLexer;
INTEGER: Digits;
DECIMAL: (DOT Digits) | (Digits DOT Digits?);
fragment Digits: [0-9]+;

STRING: ('"' ('""' | ~["&])* '"')
    | ('\'' ('\'\'' | ~['&])* '\'');

COMMENT: '(:' .*? ':)';
WS: [\p{White_Space}]+ -> channel(HIDDEN);
FOR: 'for';
COMMA: ',';
AT: 'at';
LET: 'let';
COUNT: 'count';
WHERE: 'where';
DOLLAR: '$';
CHILD: 'child';
DESCENDANT: 'descendant';
SELF: 'self';
DESCENDANT_OR_SELF: 'descendant-or-self';
ANCESTOR_OR_SELF: 'ancestor-or-self';
FOLLOWING_SIBLING: 'following-sibling';
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


ID: NAME_START (DASH NAME_MIDDLE)*
// [\p{Alpha}][\p{Alpha}\p{Alnum}-]*
    ; /* Replace with antlr compatible */

fragment NAME_START: [\p{Alpha}][\p{Alpha}\p{Alnum}]*;
fragment NAME_MIDDLE: [\p{Alpha}\p{Alnum}]+;
fragment DASH: '-';

