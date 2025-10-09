grammar regex;

regexExpr:
    block*
    ;

block: characterClass quantifier? ('@' ID)*
    ;

characterClass:
    '[' char+ ']' # bracketedCharacterClass
    | '\\' char   # escapedCharacterClass
    | char        # simpleCharacterClass
    | grouping    # groupingCharacterClass
    ;

grouping: '(' block* ')';

quantifier: unmodifiedQuantifier modifier?;

unmodifiedQuantifier:
    '+' # oneOrMore
    | '*' # zeroOrMore
    | '?' # zeroOrOne
    | '{' countQuantifier '}' # bracedCountQuantifier;

modifier:
    '+' # greedyModifier
    | '?' # nongreedyModifier
    ;

countQuantifier:
    INTEGER # exactCountQuantifier
    | INTEGER ',' # minCountQuantifier
    | ',' INTEGER # maxCountQuantifier
    | INTEGER ',' INTEGER # minMaxCountQuantifier
    ;

char: '%';

INTEGER: [0-9]+;

ID: [A-Za-z];

WS: [ \n]+ -> skip;
