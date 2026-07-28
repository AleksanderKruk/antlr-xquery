package com.github.akruk.antlrquery.evaluator.values.operations;

import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.nodeindexer.NodeIndexer;

public class NodeComparisonOperator {

    private final AntlrQueryValueFactory valueFactory;
    private final Map<ParseTree, Integer> indices;

    public NodeComparisonOperator(
        final AntlrQueryValueFactory valueFactory,
        final ParseTree tree)
    {
        final NodeIndexer indexer = new NodeIndexer();
        this.indices = indexer.indexNodes(tree);
        this.valueFactory = valueFactory;
    }

    public AntlrQueryValue is(final AntlrQueryValue o1, final AntlrQueryValue o2)
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
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        return valueFactory.bool(o1.node == o2.node);
    }

    public AntlrQueryValue isNot(final AntlrQueryValue o1, final AntlrQueryValue o2)
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
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        return valueFactory.bool(o1.node != o2.node);
    }

    public AntlrQueryValue precedes(final AntlrQueryValue o1, final AntlrQueryValue o2)
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
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        final int i1 = indices.get(o1.node);
        final int i2 = indices.get(o2.node);
        return valueFactory.bool(i1 < i2);
    }

    public AntlrQueryValue precedesOrIs(final AntlrQueryValue o1, final AntlrQueryValue o2)
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
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        final int i1 = indices.get(o1.node);
        final int i2 = indices.get(o2.node);
        return valueFactory.bool(i1 <= i2);
    }

    public AntlrQueryValue follows(final AntlrQueryValue o1, final AntlrQueryValue o2)
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
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        final int i1 = indices.get(o1.node);
        final int i2 = indices.get(o2.node);
        return valueFactory.bool(i1 > i2);
    }

    public AntlrQueryValue followsOrIs(final AntlrQueryValue o1, final AntlrQueryValue o2)
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
            return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                "Node comparison between " + o1 + " and " + o2 + " is impossible");
        }
        final int i1 = indices.get(o1.node);
        final int i2 = indices.get(o2.node);
        return valueFactory.bool(i1 >= i2);
    }
}
