package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.operations.ValueAtomizer;

public class Accessors {

    private final XQueryValueFactory valueFactory;
    private final Parser targetParser;
    private final ValueAtomizer atomizer;

    public Accessors(final XQueryValueFactory valueFactory, final Parser targetParser, final ValueAtomizer atomizer) {
        this.valueFactory = valueFactory;
        this.targetParser = targetParser;
        this.atomizer = atomizer;
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
            if (node.sequence.size() == 0) {
                return valueFactory.emptyString();
            }
            if (!node.isNode) {
                return valueFactory.error(XQueryError.InvalidArgumentType, "");
            }
        }

        ParseTree nodeTree = node.node;
        if (nodeTree == null || !(nodeTree instanceof ParserRuleContext ctx)) {
            return valueFactory.emptyString();
        }

        String ruleName = targetParser.getRuleNames()[ctx.getRuleIndex()];
        return valueFactory.string(ruleName != null ? ruleName : "");
    }


    public XQueryValue string(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {

        XQueryValue target = args.get(0);
        if (target.isError)
            return target;
        if (target.isEmptySequence)
            return valueFactory.emptyString();
        if (target.isString)
            return target;
        if (target.isBoolean)
            return valueFactory.string(target.booleanValue? "true" : "false");
        if (target.isNumeric)
            return valueFactory.string(target.numericValue.toString());
        return valueFactory.string(target.toString());
    }


    public XQueryValue data(XQueryVisitingContext ctx, List<XQueryValue> args) {
        return valueFactory.sequence(atomizer.atomize(args.get(0)));
    }

}
