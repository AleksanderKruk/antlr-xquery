package com.github.akruk.antlrquery.evaluator.functionmanager.defaults.functions;

import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class AntlrFunctions {

    private final AntlrQueryValueFactory valueFactory;

    public AntlrFunctions(
        final AntlrQueryValueFactory valueFactory)
    {
        this.valueFactory = valueFactory;
    }



    public static TerminalNode findTerminalMatchingToken(ParseTree node, Token token) {
        if (node instanceof TerminalNode) {
            TerminalNode terminal = (TerminalNode) node;
            if (terminal.getSymbol() == token) {
                return terminal;
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            TerminalNode result = findTerminalMatchingToken(node.getChild(i), token);
            if (result != null) return result;
        }
        return null;
    }

    public AntlrQueryValue start(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var input = args.getFirst();
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                "fn:start($node as node()? := .) as node()? argument must be a 'node()?', found: " + input);
        var node = input.node;
        if (node instanceof ParserRuleContext ctx) {
            Token startToken = ctx.getStart();
            TerminalNode startNode = findTerminalMatchingToken(ctx, startToken);
            return valueFactory.node("", startNode);
        } else { // node instanceof TerminalNode
            return input;
        }
    }



    public AntlrQueryValue stop(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var input = args.getFirst();
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                "fn:stop($node as node()? := .) as node()? argument must be a 'node()?', found: " + input);
        var node = input.node;
        if (node instanceof ParserRuleContext ctx) {
            Token stopToken = ctx.getStop();
            TerminalNode stopNode = findTerminalMatchingToken(ctx, stopToken);
            return valueFactory.node("", stopNode);
        } else { // node instanceof TerminalNode
            return input;
        }
    }

    public AntlrQueryValue pos(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var input = args.getFirst();
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                "fn:pos($node as node()? := .) as number? argument must be a 'node()?', found: " + input);
        var node = input.node;
        Token token = null;
        if (node instanceof TerminalNode terminal) {
            token = terminal.getSymbol();
        } else if (node instanceof ParserRuleContext ctx) {
            token = ctx.getStart();
        }
        if (token != null) {
            return valueFactory.number(token.getStartIndex());
        }
        return valueFactory.emptySequence();
    }

    public AntlrQueryValue index(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var input = args.get(0);
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                "fn:index($node as node()? := .) as number? argument must be a 'node()?', found: " + input);
        var node = input.node;
        Token token = null;
        if (node instanceof TerminalNode terminal) {
            token = terminal.getSymbol();
        } else if (node instanceof ParserRuleContext ctx) {
            token = ctx.getStart();
        }
        if (token != null) {
            return valueFactory.number(token.getTokenIndex());
        }
        return valueFactory.emptySequence();
    }

    public AntlrQueryValue line(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var input = args.get(0);
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                "fn:line($node as node()? := .) as number? argument must be a 'node()?', found: " + input);
        var node = input.node;
        Token token = null;
        if (node instanceof TerminalNode terminal) {
            token = terminal.getSymbol();
        } else if (node instanceof ParserRuleContext ctx) {
            token = ctx.getStart();
        }
        if (token != null) {
            return valueFactory.number(token.getLine());
        }
        return valueFactory.emptySequence();
    }

    public AntlrQueryValue isToken(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var input = args.get(0);
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                """
                antlr:is-token($node as node()? := .) as number?
                    expected: argument must be a 'node()?'
                    received: """ + input);
        return valueFactory.bool(input.node instanceof TerminalNode);
    }


    public AntlrQueryValue isRule(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var input = args.getFirst();
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                """
                antlr:is-rule($node as node()? := .) as number?
                    expected: argument must be a 'node()?'
                    received: """ + input);
        return valueFactory.bool(input.node instanceof ParserRuleContext);
    }

}
