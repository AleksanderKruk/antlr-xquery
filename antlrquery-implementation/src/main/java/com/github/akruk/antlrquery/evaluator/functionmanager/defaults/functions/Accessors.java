package com.github.akruk.antlrquery.evaluator.functionmanager.defaults.functions;

import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.evaluator.values.operations.Stringifier;
import com.github.akruk.antlrquery.evaluator.values.operations.ValueAtomizer;

public class Accessors {

    private final AntlrQueryValueFactory valueFactory;
    private final Parser targetParser;
    private final ValueAtomizer atomizer;
    private final Stringifier stringifier;

    public Accessors(
        final AntlrQueryValueFactory valueFactory,
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


    public AntlrQueryValue nodeName(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        AntlrQueryValue node;
        if (args.isEmpty()) {
            if (context.getValue() == null) {
                return valueFactory.error(AntlrQueryError.MissingDynamicContextComponent, "");
            }
            node = context.getValue();
        } else {
            node = args.getFirst();
            if (node.sequence.isEmpty()) {
                return valueFactory.emptyString();
            }
            if (!node.isNode) {
                return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
            }
        }

        ParseTree nodeTree = node.node;
        if (!(nodeTree instanceof ParserRuleContext ctx)) {
            return valueFactory.emptyString();
        }

        String ruleName = targetParser.getRuleNames()[ctx.getRuleIndex()];
        return valueFactory.string(ruleName != null ? ruleName : "");
    }


    public AntlrQueryValue string(
            AntlrQueryVisitingContext ignoredContext,
            List<AntlrQueryValue> args)
    {
        AntlrQueryValue target = args.getFirst();
        return stringifier.stringify(target);
    }


    public AntlrQueryValue data(AntlrQueryVisitingContext ignoredCtx, List<AntlrQueryValue> args) {
        return valueFactory.sequence(atomizer.atomize(args.getFirst()));
    }

}
