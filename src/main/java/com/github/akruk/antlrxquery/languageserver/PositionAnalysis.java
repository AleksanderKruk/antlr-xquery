package com.github.akruk.antlrxquery.languageserver;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.Position;

import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;

public record PositionAnalysis(
    ParserRuleContext innerMostContext,
    List<ParserRuleContext> contextStack
) {}

class PositionAnalyzer extends AntlrXqueryParserBaseVisitor<PositionAnalysis> {
    final Position position;

    public PositionAnalyzer(Position position) {
        this.position = position;
    }

    @Override
    public PositionAnalysis visitChildren(RuleNode node) {
        if (!(node instanceof ParserRuleContext ctx)) {
            return null;
        }

        List<ParseTree> children = ctx.children;
        if (children == null || children.isEmpty()) {
            return null;
        }

        int left = 0;
        int right = children.size() - 1;

        while (left <= right) {
            int mid = (left + right) / 2;
            ParseTree child = children.get(mid);

            if (child instanceof ParserRuleContext childCtx) {
                Token start = childCtx.getStart();
                Token stop = childCtx.getStop();

                if (start == null || stop == null) {
                    break;
                }

                if (isBefore(position, start)) {
                    right = mid - 1;
                } else if (isAfter(position, stop)) {
                    left = mid + 1;
                } else {
                    // The position is inside this child node
                    PositionAnalysis deeper = childCtx.accept(this);
                    if (deeper != null) {
                        List<ParserRuleContext> stack = new ArrayList<>(deeper.contextStack());
                        stack.add(0, ctx); // Add current context to the top of the stack
                        return new PositionAnalysis(deeper.innerMostContext(), stack);
                    } else {
                        List<ParserRuleContext> stack = new ArrayList<>();
                        stack.add(ctx);
                        stack.add(childCtx);
                        return new PositionAnalysis(childCtx, stack);
                    }
                }
            } else {
                // If it's not a ParserRuleContext, it may be a terminal (e.g. WS, COMMENT)
                Token token = getToken(child);
                if (token != null && isInside(position, token)) {
                    // Return the current context as the deepest match
                    List<ParserRuleContext> stack = new ArrayList<>();
                    stack.add(ctx);
                    return new PositionAnalysis(ctx, stack);
                }

                // Continue binary search
                if (token != null && isBefore(position, token)) {
                    right = mid - 1;
                } else if (token != null && isAfter(position, token)) {
                    left = mid + 1;
                } else {
                    break;
                }
            }
        }

        // If no deeper match was found, return the current context
        List<ParserRuleContext> stack = new ArrayList<>();
        stack.add(ctx);
        return new PositionAnalysis(ctx, stack);
    }

    private boolean isBefore(Position pos, Token token) {
        return pos.getLine() < token.getLine()
            || (pos.getLine() == token.getLine() && pos.getCharacter() < token.getCharPositionInLine());
    }

    private boolean isAfter(Position pos, Token token) {
        int endChar = token.getCharPositionInLine() + (token.getText() != null ? token.getText().length() : 1);
        return pos.getLine() > token.getLine()
            || (pos.getLine() == token.getLine() && pos.getCharacter() >= endChar);
    }

    private boolean isInside(Position pos, Token token) {
        int startLine = token.getLine();
        int startChar = token.getCharPositionInLine();
        int endChar = startChar + (token.getText() != null ? token.getText().length() : 1);
        return pos.getLine() == startLine && pos.getCharacter() >= startChar && pos.getCharacter() < endChar;
    }

    private Token getToken(ParseTree tree)
    {
        if (tree instanceof TerminalNode terminal) {
            return terminal.getSymbol();
        }
        return null;
    }
}
