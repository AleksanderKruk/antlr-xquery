package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;
import java.util.Map;

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


    public XQueryValue nodeName( XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs)
    {

        XQueryValue node = args.get(0);

        if (node.isEmptySequence()) {
            return valueFactory.emptySequence();
        }

        if (!node.isNode()) {
            return XQueryError.InvalidArgumentType;
        }

        ParseTree nodeTree = node.node();
        String ruleName = targetParser.getRuleNames()[((ParserRuleContext)nodeTree).getRuleIndex()];
        return valueFactory.string(ruleName);
    }

    public XQueryValue string(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        var target = args.isEmpty() ? context.getItem() : args.get(0);
        return valueFactory.string(target.stringValue());
    }

    public XQueryValue data(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return args.get(0).data();
    }

}
