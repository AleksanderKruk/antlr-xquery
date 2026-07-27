package com.github.akruk.antlrxquery.languageserver;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.lsp4j.Position;

import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;

public class PositionAnalyzer extends AntlrXqueryParserBaseVisitor<PositionAnalysis> {
    final Position position;

    public PositionAnalyzer(final Position position) {
        this.position = new Position(position.getLine()+1, position.getCharacter());
    }

    @Override
    public PositionAnalysis visitChildren(final RuleNode node) {
        if (!(node instanceof final ParserRuleContext rootCtx)) {
            return null;
        }

        final List<ParserRuleContext> contextStack = new ArrayList<>();
        ParserRuleContext currentCtx = rootCtx;

        while (true) {
            contextStack.add(currentCtx);

            final List<ParseTree> children = currentCtx.children;
            if (children == null || children.isEmpty()) {
                break;
            }

            boolean found = false;
            int left = 0;
            int right = children.size() - 1;

            while (left <= right) {
                final int mid = (left + right) / 2;
                final ParseTree child = children.get(mid);

                if (child instanceof final ParserRuleContext childCtx) {
                    final Token start = childCtx.getStart();
                    final Token stop = childCtx.getStop();

                    if (start == null || stop == null) {
                        if (start != null && isBefore(position, start)) {
                            right = mid - 1;
                        } else {
                            left = mid + 1;
                        }
                        continue;
                    }

                    if (isBefore(position, start)) {
                        right = mid - 1;
                    } else if (isAfter(position, stop)) {
                        left = mid + 1;
                    } else { // inside
                        currentCtx = childCtx;
                        found = true;
                        break;
                    }
                } else {
                    final Token token = getToken(child);
                    if (token != null && isInside(position, token)) {
                        // found terminal
                        return new PositionAnalysis(currentCtx, contextStack);
                    }

                    if (token != null && isBefore(position, token)) {
                        right = mid - 1;
                    } else if (token != null && isAfter(position, token)) {
                        left = mid + 1;
                    } else {
                        break;
                    }
                }
            }

            if (!found) {
                break;
            }
        }

        return new PositionAnalysis(currentCtx, contextStack);
    }

    private boolean isBefore(final Position pos, final Token token) {
        return pos.getLine() < token.getLine()
            || (pos.getLine() == token.getLine() && pos.getCharacter() < token.getCharPositionInLine());
    }

    private boolean isAfter(final Position pos, final Token token) {
        final int endChar = token.getCharPositionInLine() + (token.getText() != null ? token.getText().length() : 1);
        return pos.getLine() > token.getLine()
            || (pos.getLine() == token.getLine() && pos.getCharacter() >= endChar);
    }

    private boolean isInside(final Position pos, final Token token) {
        final int startLine = token.getLine();
        final int startChar = token.getCharPositionInLine();
        final int endChar = startChar + (token.getText() != null ? token.getText().length() : 1);
        return pos.getLine() == startLine && pos.getCharacter() >= startChar && pos.getCharacter() < endChar;
    }

    private Token getToken(final ParseTree tree)
    {
        if (tree instanceof final TerminalNode terminal) {
            return terminal.getSymbol();
        }
        return null;
    }
}
