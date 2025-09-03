package com.github.akruk.antlrxquery.languageserver;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Stack;

public class HoverLogic {
    enum HoverKind {
        VARIABLE,
        TYPE,
        FUNCTION_CALL,
        FUNCTION_REF,
        OTHER
    }
    public record HoverInfo(HoverKind hoverKind, ParserRuleContext rule){}
    public static HoverInfo getHoverKind(ParseTree parseTree, int line, int charPosition)
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
                        return null;
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
