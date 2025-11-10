package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class AntlrFunctions {

    private final XQueryValueFactory valueFactory;

    public AntlrFunctions(
        final XQueryValueFactory valueFactory)
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

    public XQueryValue start(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        final var input = args.get(0);
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(XQueryError.InvalidArgumentType,
                "fn:start($node as node()? := .) as node()? argument must be a 'node()?', found: " + input);
        var node = input.node;
        if (node instanceof ParserRuleContext ctx) {
            Token startToken = ctx.getStart();
            TerminalNode startNode = findTerminalMatchingToken(ctx, startToken);
            return valueFactory.node(startNode);
        } else { // node instanceof TerminalNode
            return input;
        }
    }



    public XQueryValue stop(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        final var input = args.get(0);
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(XQueryError.InvalidArgumentType,
                "fn:stop($node as node()? := .) as node()? argument must be a 'node()?', found: " + input);
        var node = input.node;
        if (node instanceof ParserRuleContext ctx) {
            Token stopToken = ctx.getStop();
            TerminalNode stopNode = findTerminalMatchingToken(ctx, stopToken);
            return valueFactory.node(stopNode);
        } else { // node instanceof TerminalNode
            return input;
        }
    }

    public XQueryValue pos(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        final var input = args.get(0);
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(XQueryError.InvalidArgumentType,
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

    public XQueryValue index(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        final var input = args.get(0);
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(XQueryError.InvalidArgumentType,
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

    public XQueryValue line(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        final var input = args.get(0);
        if (input.isEmptySequence)
            return input;
        if (!input.isNode)
            return valueFactory.error(XQueryError.InvalidArgumentType,
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


}
