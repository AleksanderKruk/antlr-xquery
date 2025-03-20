package com.github.akruk.antlrxquery.values;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;

public class XQueryTreeNode extends XQueryValueBase<ParseTree> {
    public XQueryTreeNode(ParseTree node) {
        value = node;
    }
    @Override
    public XQueryValue copy() {
        return new XQueryTreeNode(value);
    }
    @Override
    public ParseTree node() {
        return value;
    }
    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XQueryValue data() throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return new XQuerySequence(atomized);
    }
}
