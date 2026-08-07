package com.github.akruk.antlrquery;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.swing.*;
import java.util.concurrent.Future;

public final class ParseDebug {

    public static void main(String[] args) throws Exception {
        CharStream input;

        input = CharStreams.fromString("fn:boolean#1");

        AntlrQueryLexer lexer = new AntlrQueryLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        System.out.println("=== TOKENS ===");
        for (Token token : tokens.getTokens()) {
            String symbolic = AntlrQueryLexer.VOCABULARY.getSymbolicName(token.getType());
            String literal = AntlrQueryLexer.VOCABULARY.getLiteralName(token.getType());

            System.out.printf(
                    "%4d:%-3d %-25s %-15s %s%n",
                    token.getLine(),
                    token.getCharPositionInLine(),
                    symbolic != null ? symbolic : literal,
                    "'" + token.getText().replace("\n", "\\n").replace("\r", "\\r") + "'",
                    token.getChannel() == Token.DEFAULT_CHANNEL ? "" : "(hidden)"
            );
        }

        tokens.seek(0);

        AntlrQueryParser parser = new AntlrQueryParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(new DiagnosticErrorListener());
        parser.addErrorListener(ConsoleErrorListener.INSTANCE);

        ParseTree tree = parser.expr();

        System.out.println(tree.toStringTree(parser));

        Future<JFrame> frame = Trees.inspect(tree, parser);
        frame.get().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
