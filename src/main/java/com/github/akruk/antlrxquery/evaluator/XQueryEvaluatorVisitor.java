package com.github.akruk.antlrxquery.evaluator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionCallContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LiteralContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.OrExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ParenthesizedExprContext;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQuerySequence;
import com.github.akruk.antlrxquery.values.XQueryString;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.XQueryBoolean;

class XQueryEvaluatorVisitor extends AntlrXqueryParserBaseVisitor<XQueryValue> {
    ParseTree tree;
    Parser parser;

    static final Map<String, Supplier<XQueryValue>> noArgumentCalls = Map.of(
        "true", (()->{return XQueryBoolean.TRUE;}),
        "false", (()->{return XQueryBoolean.FALSE;})
    );

    public XQueryEvaluatorVisitor(ParseTree tree, Parser parser) {
        this.tree = tree;
        this.parser = parser;
    }

    @Override
    public XQueryValue visitLiteral(LiteralContext ctx) {

        if (ctx.STRING() != null) {
            String text = ctx.getText();
            String removepars = ctx.getText().substring(1, text.length() - 1);
            String string = unescapeString(removepars);
            return new XQueryString(string);
        }

        if (ctx.INTEGER() != null) {
            return new XQueryNumber(new BigDecimal(ctx.INTEGER().getText()));
        }

        return new XQueryNumber(new BigDecimal(ctx.DECIMAL().getText()));
    }

    @Override
    public XQueryValue visitParenthesizedExpr(ParenthesizedExprContext ctx) {
        // Empty parentheses mean an empty sequence '()'
        if (ctx.expr() == null) {
            return new XQuerySequence();
        }
        return ctx.expr().accept(this);
    }

    @Override
    public XQueryValue visitExpr(ExprContext ctx) {
        // Only one expression
        // e.g. 13
        if (ctx.exprSingle().size() == 1) {
            return ctx.exprSingle(0).accept(this);
        }
        // More than one expression
        // are turned into a flattened list
        List<XQueryValue> result = new ArrayList<>();
        for (var exprSingle : ctx.exprSingle()) {
            var expressionValue = exprSingle.accept(this);
            if (expressionValue.isAtomic()) {
                result.add(expressionValue);
                continue;
            }
            // If the result is not atomic we atomize it
            // and extend the result list
            var atomizedValues = expressionValue.atomize();
            result.addAll(atomizedValues);
        }
        return new XQuerySequence(result);
    }


    // TODO: ESCAPE characters
    // &lt ...
    private String unescapeString(String str) {
        return str.replace("\"\"", "\"").replace("''", "'");
    }

    @Override
    public XQueryValue visitFunctionCall(FunctionCallContext ctx) {
        var functionName = ctx.ID().getText();
        if (!noArgumentCalls.containsKey(functionName)) {
            // TODO: error handling missing function
            return null;
        }
        Supplier<XQueryValue> function = noArgumentCalls.get(functionName);
        return function.get();
    }

    @Override
    public XQueryValue visitOrExpr(OrExprContext ctx) {
        try {
            XQueryValue value = null;
            if (ctx.orExpr().size() == 0) {
                value = ctx.pathExpr(0).accept(this);
            } else {
            }
            var hasOr = !ctx.OR().isEmpty();
            if (hasOr)
                return handleOrExpr(ctx);
            var hasAnd = !ctx.AND().isEmpty();
            if (hasAnd)
                return handleAndExpr(ctx);

            return value;
        } catch (XQueryUnsupportedOperation e) {
            // TODO: error handling
            return null;
        }
    }

    private XQueryValue handleOrExpr(OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isBooleanValue()) {
            // TODO: type error
        }
        // Short circuit
        if (value.booleanValue()) {
            return XQueryBoolean.TRUE;
        }
        var orCount = ctx.OR().size();
        for (int i = 1; i <= orCount; i++) {
            var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.or(visitedExpression);
            // Short circuit
            if (value.booleanValue()) {
                return XQueryBoolean.TRUE;
            }
            i++;
        }

        return value;
    }


    private XQueryValue handleAndExpr(OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isBooleanValue()) {
            // TODO: type error
        }
        // Short circuit
        if (!value.booleanValue()) {
            return XQueryBoolean.FALSE;
        }
        var orCount = ctx.AND().size();
        for (int i = 1; i <= orCount; i++) {
            var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.and(visitedExpression);
            // Short circuit
            if (!value.booleanValue()) {
                return XQueryBoolean.FALSE;
            }
            i++;
        }

        return value;
    }




}
