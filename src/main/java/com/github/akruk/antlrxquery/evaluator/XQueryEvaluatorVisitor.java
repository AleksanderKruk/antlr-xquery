package com.github.akruk.antlrxquery.evaluator;

import java.math.BigDecimal;
import java.util.Formatter;

import javax.inject.Inject;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LiteralContext;
import com.github.akruk.antlrxquery.values.XQueryNumber;
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
            String string = unescapeString(ctx.STRING().getText());
            return new XQueryString(string);
        }
        if (ctx.INTEGER() != null) {
            // BigDecimal number = ctx.INTEGER().getText();
            return new XQueryNumber(new BigDecimal(ctx.INTEGER().getText()));
        }
        return new XQueryNumber(new BigDecimal(ctx.DECIMAL().getText()));
    }

    // TODO: ESCAPE
    private String unescapeString(String str) {
        return str;
    }

}
