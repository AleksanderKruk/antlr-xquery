lexer grammar AntlrXqueryLexer;
IntegerLiteral: Digits;
DecimalLiteral: ('.' Digits) | (Digits '.' [0-9]*)	/* ws: explicit */;
DoubleLiteral: (('.' Digits) | (Digits ('.' [0-9]*)?)) [eE] [+-]? Digits	/* ws: explicit */;
StringLiteral: ('"' (EscapeQuot | ~["&])* '"') 
            | ('\'' (EscapeApos | ~['&])* '\'')	/* ws: explicit */;
EscapeQuot: '""';
EscapeApos: '\'\'';
Comment: '(:' .*? ':)'	/* ws: explicit */;
ID: [\P{alpha}][\P{alnum}]*; /* Replace with antlr compatible */
fragment Digits: [0-9]+;
