grammar markdown;

CODE_INLINE: '`' ( '\\`'| ~'`'*) '`';
CODE_BLOCK: '```' .*? '```';
NL: [\n\r]+;
DOT: '.';
COLUMN_SEP: '|';
TEXT: ('\\' ('.' | '|' | '`' ) | ~[|`\n\r])+;
markdown: (normalLine| tableHeader)*;
normalLine: ~COLUMN_SEP+ NL;
tableHeader: text? (COLUMN_SEP text?)+ NL tableRow+;
tableRow: ruleRow | cellRow;
ruleRow: '-'+ text? (COLUMN_SEP '-'+ text?)+;
cellRow: text? (COLUMN_SEP text?)+ NL;
text: (TEXT | CODE_BLOCK | CODE_INLINE)+;

