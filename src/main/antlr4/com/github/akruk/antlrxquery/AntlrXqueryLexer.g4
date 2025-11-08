lexer grammar AntlrXqueryLexer;

fragment Digits: [0-9]+;
fragment DEC_DIGIT      : [0-9];
fragment HEX_DIGIT      : [0-9a-fA-F];
fragment BINARY_DIGIT   : [01];

fragment E              : [eE];
fragment UNDER          : '_';

fragment HEX_PREFIX     : '0x';
fragment BIN_PREFIX     : '0b';

HASH: '#';

LOOKUP: '?[';

DecimalLiteral
    : DOT DigitSeq                          // np. .75
    | DigitSeq DOT DigitSeq?               // np. 1.2 lub 1.
    ;

DoubleLiteral
    : (DOT DigitSeq | DigitSeq (DOT DigitSeq?)?) ExponentPart
    ;


IntegerLiteral
    : DigitSeq
    ;

HexIntegerLiteral
    : HEX_PREFIX HexDigitSeq
    ;

BinaryIntegerLiteral
    : BIN_PREFIX BinaryDigitSeq
    ;

fragment DigitSeq
    : DEC_DIGIT ( (DEC_DIGIT | UNDER)* DEC_DIGIT )?
    ;

fragment HexDigitSeq
    : HEX_DIGIT ( (HEX_DIGIT | UNDER)* HEX_DIGIT )?
    ;

fragment BinaryDigitSeq
    : BINARY_DIGIT ( (BINARY_DIGIT | UNDER)* BINARY_DIGIT )?
    ;

fragment ExponentPart
    : [eE] (PLUS | MINUS)? DigitSeq
    ;


STRING: ('"' ('""' | CHARREF | PREDEFINED_ENTITY_REF | ~["&])* '"')
    | ('\'' ('\'\'' | CHARREF | PREDEFINED_ENTITY_REF | ~['&])* '\'');
fragment CHARREF: '&#' [0-9]+ ';'
                | '&#x' [0-9a-fA-F]+ ';';
fragment PREDEFINED_ENTITY_REF:
    '&lt;'
  | '&gt;'
  | '&amp;'
  | '&apos;'
  | '&quot;';


TUMBLING: 'tumbling';
SLIDING: 'sliding';
WINDOW: 'window';
START: 'start';
END: 'end';
ONLY: 'only';
WHEN: 'when';
PREVIOUS: 'previous';
NEXT: 'next';

TRY: 'try' ;
CATCH: 'catch' ;
FINALLY: 'finally' ;

IS: 'is';
IS_NOT: 'is-not';
FOLLOWS: 'follows' ;
PRECEDES: 'precedes' ;
FOLLOWS_OR_IS: 'follows-or-is' ;
PRECEDES_OR_IS: 'precedes-or-is' ;

PIPE_ARROW: '->';
CASTABLE: 'castable';
MAPPING_ARROW : '=!>' ;

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
DIV_OP: '÷';
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
CONCATENATION: '||';
UNION_OP: '|';
EXCLAMATION_MARK: '!';
MINUS: '-';
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
MULTIPLICATION: 'x';
FN: 'fn';
MEMBER : 'member' ;
KEY : 'key' ;
VALUE : 'value' ;




NAMESPACE: 'namespace';
FIXED: 'fixed';
DECIMAL_SEPARATOR: 'decimal-separator';
GROUPING_SEPARATOR: 'grouping-separator';
INFINITY: 'infinity';
MINUS_SIGN: 'minus-sign';
NAN: 'NaN';
PERCENT: 'percent';
PER_MILLE: 'per-mille';
ZERO_DIGIT: 'zero-digit';
DIGIT: 'digit';
PATTERN_SEPARATOR: 'pattern-separator';
EXPONENT_SEPARATOR: 'exponent-separator';
XQUERY: 'xquery';
ENCODING: 'encoding';
VERSION: 'version';
SEPARATOR: ';';
DECLARE: 'declare';
MODULE: 'module';
IMPORT: 'import';
GRAMMAR: 'grammar';
CONTEXT: 'context';
EXTERNAL: 'external';
VARIABLE: 'variable';
TYPE: 'type';
OPTION: 'option';
BOUNDARY_SPACE: 'boundary-space';
PRESERVE: 'preserve';
STRIP: 'strip';
BASE_URI: 'base-uri';
CONSTRUCTION: 'construction';
ORDERING: 'ordering';
COPY_NAMESPACES: 'copy-namespaces';
DECIMAL_FORMAT: 'decimal-format';
GROUP: 'group';


ORDERED: 'ordered';
UNORDERED: 'unordered';


TYPESWITCH     : 'typeswitch';





ID: NAME_START (DASH NAME_MIDDLE)*
    ; /* Replace with antlr compatible */

fragment NAME_START:  [\p{Alpha}_][\p{Alpha}\p{Alnum}_]*;
fragment NAME_MIDDLE: [\p{Alpha}_][\p{Alpha}\p{Alnum}_]*;
fragment DASH: '-';

STRING_CONSTRUCTOR_START : '``[' -> pushMode(INSIDE_STRING_CONSTRUCTOR);
STRING_INTERPOLATION_START  : '`' -> pushMode(INSIDE_INTERPOLATION);
// CONSTRUCTION_END: '}`' -> popMode;

LCURLY: '{' -> pushMode(DEFAULT_MODE);
RCURLY: '}' -> popMode;

CLOSE_SHORT_CONSTRUCTOR: '/>';
CLOSE_LONG_CONSTRUCTOR: '</';



mode INSIDE_INTERPOLATION;

STRING_INTERPOLATION_END  : '`' -> popMode;

INTERPOLATION_START : '{' -> pushMode(DEFAULT_MODE);

INTERPOLATION_CHARS        :
    ~[`{]+
    | '`{' ~[`{]+
    ;

mode INSIDE_STRING_CONSTRUCTOR;

STRING_CONSTRUCTOR_END  : ']``' -> popMode ;

CONSTRUCTION_START : '`{' -> pushMode(DEFAULT_MODE) ;

CONSTRUCTOR_CHARS:
  ~[`\]]+
    ;

BACKTICK: '`';
BRACKET: ']';

// mode XML;
