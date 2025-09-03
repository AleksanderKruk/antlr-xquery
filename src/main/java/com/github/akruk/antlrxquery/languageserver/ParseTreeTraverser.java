package com.github.akruk.antlrxquery.languageserver;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Stack;

public class ParseTreeTraverser {
    enum HoverKind {
        VARIABLE,
        FUNCTION_CALL,
        OTHER
    }

    public static HoverKind findWordAtPosition(ParseTree parseTree, int line, int charPosition)
    {
        Stack<ParseTree> stack = new Stack<>();
        stack.push(parseTree);

        while (!stack.isEmpty()) {
            ParseTree node = stack.pop();

            if (node instanceof TerminalNode) {
                Token token = ((TerminalNode) node).getSymbol();
                if (token != null) {
                    // Check if the desired position falls within the token's range
                    if (token.getLine() == line + 1 // ANTLR lines are 1-based
                        && token.getCharPositionInLine() <= charPosition
                        && token.getCharPositionInLine() + token.getText().length() > charPosition)
                    {
                        switch(token.getType()) {

                        }
                        token.getType();
                        return HoverKind.OTHER;
                    }
                }
            } else {
                // Push children onto the stack in reverse order to process them left-to-right
                for (int i = node.getChildCount() - 1; i >= 0; i--) {
                    stack.push(node.getChild(i));
                }
            }
        }
        return null;
    }
}
