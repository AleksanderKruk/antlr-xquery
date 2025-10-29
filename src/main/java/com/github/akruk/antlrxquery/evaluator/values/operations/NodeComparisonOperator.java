package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.nodeindexer.NodeIndexer;

public class NodeComparisonOperator {

    private final XQueryValueFactory valueFactory;
    private final Map<ParseTree, Integer> indices;

    public NodeComparisonOperator(
        final XQueryValueFactory valueFactory,
        final ParseTree tree)
    {
        final NodeIndexer indexer = new NodeIndexer();
        this.indices = indexer.indexNodes(tree);
        this.valueFactory = valueFactory;
    }

    public XQueryValue is(final XQueryValue o1, final XQueryValue o2)
    {
        if (o1.isEmptySequence)
            return o1;
        if (o2.isEmptySequence)
            return o2;
        if (!o1.isNode || !o2.isNode) {
            if (o1.isError)
                return o1;
            if (o2.isError)
                return o2;
            return valueFactory.error(XQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        return valueFactory.bool(o1.node == o2.node);
    }

    public XQueryValue isNot(final XQueryValue o1, final XQueryValue o2)
    {
        if (o1.isEmptySequence)
            return o1;
        if (o2.isEmptySequence)
            return o2;
        if (!o1.isNode || !o2.isNode) {
            if (o1.isError)
                return o1;
            if (o2.isError)
                return o2;
            return valueFactory.error(XQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        return valueFactory.bool(o1.node != o2.node);
    }

    public XQueryValue precedes(final XQueryValue o1, final XQueryValue o2)
    {
        if (o1.isEmptySequence)
            return o1;
        if (o2.isEmptySequence)
            return o2;
        if (!o1.isNode || !o2.isNode) {
            if (o1.isError)
                return o1;
            if (o2.isError)
                return o2;
            return valueFactory.error(XQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        final int i1 = indices.get(o1.node);
        final int i2 = indices.get(o2.node);
        return valueFactory.bool(i1 < i2);
    }

    public XQueryValue precedesOrIs(final XQueryValue o1, final XQueryValue o2)
    {
        if (o1.isEmptySequence)
            return o1;
        if (o2.isEmptySequence)
            return o2;
        if (!o1.isNode || !o2.isNode) {
            if (o1.isError)
                return o1;
            if (o2.isError)
                return o2;
            return valueFactory.error(XQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        final int i1 = indices.get(o1.node);
        final int i2 = indices.get(o2.node);
        return valueFactory.bool(i1 <= i2);
    }

    public XQueryValue follows(final XQueryValue o1, final XQueryValue o2)
    {
        if (o1.isEmptySequence)
            return o1;
        if (o2.isEmptySequence)
            return o2;
        if (!o1.isNode || !o2.isNode) {
            if (o1.isError)
                return o1;
            if (o2.isError)
                return o2;
            return valueFactory.error(XQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        final int i1 = indices.get(o1.node);
        final int i2 = indices.get(o2.node);
        return valueFactory.bool(i1 > i2);
    }

    public XQueryValue followsOrIs(final XQueryValue o1, final XQueryValue o2)
    {
        if (o1.isEmptySequence)
            return o1;
        if (o2.isEmptySequence)
            return o2;
        if (!o1.isNode || !o2.isNode) {
            if (o1.isError)
                return o1;
            if (o2.isError)
                return o2;
            return valueFactory.error(XQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        final int i1 = indices.get(o1.node);
        final int i2 = indices.get(o2.node);
        return valueFactory.bool(i1 >= i2);
    }
}
