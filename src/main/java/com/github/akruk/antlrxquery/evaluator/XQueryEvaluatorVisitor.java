package com.github.akruk.antlrxquery.evaluator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LiteralContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ParenthesizedExprContext;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQuerySequence;
import com.github.akruk.antlrxquery.values.XQueryString;
import com.github.akruk.antlrxquery.values.XQueryValue;

class XQueryEvaluatorVisitor extends AntlrXqueryParserBaseVisitor<XQueryValue> {
    ParseTree tree;
    Parser parser;

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



}
