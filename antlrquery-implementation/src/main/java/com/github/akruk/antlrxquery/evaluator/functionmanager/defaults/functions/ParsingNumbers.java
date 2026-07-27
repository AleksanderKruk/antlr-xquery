package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class ParsingNumbers {

    private final XQueryValueFactory valueFactory;
    private final Parser targetParser;;

    public ParsingNumbers(final XQueryValueFactory valueFactory, final Parser targetParser) {
        this.valueFactory = valueFactory;
        this.targetParser = targetParser;
    }


    public XQueryValue nodeName(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        XQueryValue node;
        if (args.isEmpty()) {
            if (context.getValue() == null) {
                return valueFactory.error(XQueryError.MissingDynamicContextComponent, "");
            }
            node = context.getValue();
        } else {
            node = args.get(0);
            if (node.isEmptySequence) {
                return valueFactory.emptyString();
            }
            if (!node.isNode) {
                return valueFactory.error(XQueryError.InvalidArgumentType, "");
            }
        }

        final ParseTree nodeTree = node.node;
        if (nodeTree == null || !(nodeTree instanceof ParserRuleContext ctx)) {
            return valueFactory.emptyString();
        }

        final String ruleName = targetParser.getRuleNames()[ctx.getRuleIndex()];
        return valueFactory.string(ruleName != null ? ruleName : "");
    }


    public XQueryValue string(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        XQueryValue target;

        if (args.isEmpty()) {
            if (context.getValue() == null) {
                return valueFactory.error(XQueryError.MissingDynamicContextComponent, "");
            }
            target = context.getValue();
        } else {
            target = args.get(0);
            if (target.isEmptySequence) {
                return valueFactory.emptyString();
            }
        }
        if (target.isEmptySequence) {
            return valueFactory.emptyString();
        }

        return valueFactory.string(target.stringValue);
    }


    public XQueryValue data(XQueryVisitingContext ctx, List<XQueryValue> args) {
        return null;
        // return args.get(0).data();
    }

}
