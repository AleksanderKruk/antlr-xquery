package com.github.akruk.antlrxquery.typesystem.factories;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public interface XQueryTypeFactory {
    public XQueryItemType itemString();
    public XQueryItemType itemNumber();
    public XQueryItemType itemAnyNode();
    public XQueryItemType itemAnyArray();
    public XQueryItemType itemAnyMap();
    public XQueryItemType itemAnyElement();
    public XQueryItemType itemElement(String elementName);
    public XQueryItemType itemAnyFunction();
    public XQueryItemType itemAnyItem();
    public XQueryItemType itemBoolean();

    public XQuerySequenceType string();
    public XQuerySequenceType number();
    public XQuerySequenceType anyNode();
    public XQuerySequenceType anyArray();
    public XQuerySequenceType anyMap();
    public XQuerySequenceType anyElement();
    public XQuerySequenceType element(String elementName);
    public XQuerySequenceType anyFunction();
    public XQuerySequenceType anyItem();
    public XQuerySequenceType boolean_();
    public XQuerySequenceType emptySequence();
    public XQuerySequenceType one(XQueryItemType itemType);
    public XQuerySequenceType zeroOrOne(XQueryItemType itemType);
    public XQuerySequenceType zeroOrMore(XQueryItemType itemType);
    public XQuerySequenceType oneOrMore(XQueryItemType itemType);
}
