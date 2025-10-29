package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.operations.Stringifier;
import com.github.akruk.antlrxquery.evaluator.values.operations.ValueAtomizer;

public class Accessors {

    private final XQueryValueFactory valueFactory;
    private final Parser targetParser;
    private final ValueAtomizer atomizer;
    private final Stringifier stringifier;

    public Accessors(
        final XQueryValueFactory valueFactory,
        final Parser targetParser,
        final ValueAtomizer atomizer,
        final Stringifier stringifier
        )
    {
        this.valueFactory = valueFactory;
        this.targetParser = targetParser;
        this.atomizer = atomizer;
        this.stringifier = stringifier;
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
        return stringifier.stringify(target);
    }


    public XQueryValue data(XQueryVisitingContext ctx, List<XQueryValue> args) {
        return valueFactory.sequence(atomizer.atomize(args.get(0)));
    }

}
