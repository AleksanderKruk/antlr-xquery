package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class Accessors {

    private final XQueryValueFactory valueFactory;
    private final Parser targetParser;;

    public Accessors(final XQueryValueFactory valueFactory, final Parser targetParser) {
        this.valueFactory = valueFactory;
        this.targetParser = targetParser;
    }


    public XQueryValue nodeName(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        XQueryValue node;
        if (args.isEmpty()) {
            if (context.getValue() == null) {
                return XQueryError.MissingDynamicContextComponent;
            }
            node = context.getValue();
        } else {
            node = args.get(0);
            if (node.isEmptySequence()) {
                return valueFactory.emptyString();
            }
            if (!node.isNode()) {
                return XQueryError.InvalidArgumentType;
            }
        }

        ParseTree nodeTree = node.node();
        if (nodeTree == null || !(nodeTree instanceof ParserRuleContext ctx)) {
            return valueFactory.emptyString();
        }

        String ruleName = targetParser.getRuleNames()[ctx.getRuleIndex()];
        return valueFactory.string(ruleName != null ? ruleName : "");
    }


    public XQueryValue string(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        XQueryValue target;

        if (args.isEmpty()) {
            if (context.getValue() == null) {
                return XQueryError.MissingDynamicContextComponent;
            }
            target = context.getValue();
        } else {
            target = args.get(0);
            if (target.isEmptySequence()) {
                return valueFactory.emptyString();
            }
        }
        if (target.isEmptySequence()) {
            return valueFactory.emptyString();
        }

        return valueFactory.string(target.stringValue());
    }


    public XQueryValue data(XQueryVisitingContext ctx, List<XQueryValue> args) {
        return args.get(0).data();
    }

}
