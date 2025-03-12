package com.github.akruk.antlrxquery.values;

import org.antlr.v4.runtime.tree.ParseTree;

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
    
}
