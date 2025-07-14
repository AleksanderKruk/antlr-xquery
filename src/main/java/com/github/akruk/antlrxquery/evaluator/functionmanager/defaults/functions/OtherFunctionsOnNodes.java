package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.nodegetter.INodeGetter;

public class OtherFunctionsOnNodes {
    private final XQueryValueFactory valueFactory;
    private final INodeGetter nodeGetter;
    private final Parser targetParser;
    public OtherFunctionsOnNodes(final XQueryValueFactory valueFactory, final INodeGetter nodeGetter, final Parser targetParser) {
        this.valueFactory = valueFactory;
        this.nodeGetter = nodeGetter;
        this.targetParser = targetParser;
    }


    private XQueryValue getNode(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs)
    {
        XQueryValue node;
        if (args.isEmpty()) {
            if (context.getItem() == null) {
                return XQueryError.MissingDynamicContextComponent;
            }
            node = context.getItem();
        } else {
            node = args.get(0);
            if (node.isEmptySequence()) {
                return valueFactory.emptyString();
            }
            if (!node.isNode()) {
                return XQueryError.InvalidArgumentType;
            }
        }
        return node;
    }




    public XQueryValue root(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs)
    {
        XQueryValue node = getNode(context, args, kwargs);
        if (!node.isNode()) {
            return node;
        }
        ParseTree nodeTree = node.node();
        var ancestors = nodeGetter.getAncestors(nodeTree);
        if (ancestors.size() == 0)
            return node;
        return valueFactory.node(ancestors.get(0));
    }

    // public XQueryValue path(
    //         XQueryVisitingContext context,
    //         List<XQueryValue> args,
    //         Map<String, XQueryValue> kwargs)
    // {

    // }

    public XQueryValue hasChildren(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs)
    {
        return null;

    }

    public XQueryValue siblings(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs)
    {
        return null;

    }



}
