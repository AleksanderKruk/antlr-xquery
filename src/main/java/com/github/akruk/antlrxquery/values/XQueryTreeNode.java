package com.github.akruk.antlrxquery.values;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

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
    public String stringValue() {
        return value.toString();
    }

    @Override
    public XQueryValue valueEqual(XQueryValueFactory factoryValue, XQueryValue other) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XQueryValue valueLessThan(XQueryValueFactory factoryValue, XQueryValue other) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XQueryValue data(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return factoryValue.sequence(atomized);
    }
}
