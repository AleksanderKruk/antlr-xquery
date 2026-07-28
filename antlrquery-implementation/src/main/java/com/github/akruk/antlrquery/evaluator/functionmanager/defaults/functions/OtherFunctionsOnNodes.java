package com.github.akruk.antlrquery.evaluator.functionmanager.defaults.functions;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.nodegetter.NodeGetter;

public class OtherFunctionsOnNodes {
    private final AntlrQueryValueFactory valueFactory;
    private final NodeGetter nodeGetter;
    public OtherFunctionsOnNodes(final AntlrQueryValueFactory valueFactory, final NodeGetter nodeGetter, final Parser targetParser) {
        this.valueFactory = valueFactory;
        this.nodeGetter = nodeGetter;
    }


    private AntlrQueryValue getNode(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args)
    {
        AntlrQueryValue node;
        if (args.isEmpty()) {
            if (context.getValue() == null) {
                return valueFactory.error(AntlrQueryError.MissingDynamicContextComponent, "");
            }
            node = context.getValue();
        } else {
            node = args.get(0);
            if (node.isEmptySequence) {
                return valueFactory.emptyString();
            }
            if (!node.isNode) {
                return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
            }
        }
        return node;
    }




    public AntlrQueryValue root(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args)
    {
        AntlrQueryValue node = getNode(context, args);
        if (!node.isNode) {
            return node;
        }
        ParseTree nodeTree = node.node;
        var ancestors = nodeGetter.getAncestors(nodeTree);
        if (ancestors.isEmpty())
            return node;
        return valueFactory.node("", ancestors.getFirst());
    }

    // public XQueryValue path(
    //         XQueryVisitingContext context,
    //         List<XQueryValue> args,
    //         Map<String, XQueryValue> kwargs)
    // {

    // }

    public AntlrQueryValue hasChildren(AntlrQueryVisitingContext context, List<AntlrQueryValue> args)
    {
        AntlrQueryValue node = getNode(context, args);
        if (!node.isNode) {
            return node;
        }
        ParseTree nodeTree = node.node;
        return valueFactory.bool(nodeTree.getChildCount() != 0);
    }

    public AntlrQueryValue siblings(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args)
    {
        AntlrQueryValue node = getNode(context, args);
        if (!node.isNode) {
            return node;
        }
        ParseTree nodeTree = node.node;
        final var followingSiblings = nodeGetter.getFollowingSiblings(nodeTree);
        final var precedingSiblings = nodeGetter.getPrecedingSiblings(nodeTree);
        final var combined = new ArrayList<AntlrQueryValue>(followingSiblings.size() + precedingSiblings.size());

        for (ParseTree sibling : precedingSiblings) {
            combined.add(valueFactory.node("", sibling));
        }

        for (ParseTree sibling : followingSiblings) {
            combined.add(valueFactory.node("", sibling));
        }


        return valueFactory.sequence(combined);
    }



}
