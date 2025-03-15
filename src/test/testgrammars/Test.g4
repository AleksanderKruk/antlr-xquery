grammar Test;
test: (A | rule)+;
rule: B C | D;
A: 'a';
B: 'b';
C: 'c';
D: 'd';
WS: [\p{White_Space}]+ -> skip;
