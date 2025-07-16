package com.github.akruk.antlrxquery.values;


import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryTreeNode extends XQueryValueBase<ParseTree> {
    public XQueryTreeNode(ParseTree node, XQueryValueFactory valueFactory) {
        super(node, valueFactory);
    }

    @Override
    public ParseTree node() {
        return value;
    }

    @Override
    public String stringValue() {
        return value.getText();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<");
        sb.append(super.toString());
        sb.append(":");
        sb.append(value.getClass().getSimpleName());
        sb.append(":");
        sb.append(value.getText());
        sb.append("/>");
        return sb.toString();
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        return valueFactory.bool(this == other);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue data() {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }

    @Override
    public XQueryValue empty() {
        return null;
    }


    @Override
    public List<XQueryValue> arrayMembers() {
        return null;
    }

    @Override
    public Map<XQueryValue, XQueryValue> mapEntries() {
        return null;
    }
}
