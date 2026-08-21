package com.github.akruk.antlrquery.evaluator.functionmanager.functions;

import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class FunctionsOnSequencesOfNodes {

    private final AntlrQueryValueFactory valueFactory;
    private final Parser targetParser;

    public FunctionsOnSequencesOfNodes(final AntlrQueryValueFactory valueFactory, final Parser targetParser) {
        this.valueFactory = valueFactory;
        this.targetParser = targetParser;
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
            if (node.isEmptySequence) {
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
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        AntlrQueryValue target;

        if (args.isEmpty()) {
            if (context.getValue() == null) {
                return valueFactory.error(AntlrQueryError.MissingDynamicContextComponent, "");
            }
            target = context.getValue();
        } else {
            target = args.getFirst();
            if (target.isEmptySequence) {
                return valueFactory.emptyString();
            }
        }
        if (target.isEmptySequence) {
            return valueFactory.emptyString();
        }

        return valueFactory.string(target.stringValue);
    }


}
