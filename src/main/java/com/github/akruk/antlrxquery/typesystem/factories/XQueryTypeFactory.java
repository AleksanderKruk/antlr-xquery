package com.github.akruk.antlrxquery.typesystem.factories;

import com.github.akruk.antlrxquery.typesystem.XQueryType;

public interface XQueryTypeFactory {
    public XQueryType string();
    public XQueryType number();
    public XQueryType anyNode();
    public XQueryType anyArray();
    public XQueryType anyMap();
    public XQueryType anyElement();
    public XQueryType element(String elementName);
    public XQueryType anyFunction();
    public XQueryType anyItem();
    public XQueryType boolean_();
    public XQueryType emptySequence();
    public XQueryType one(XQueryType itemType);
    public XQueryType zeroOrOne(XQueryType itemType);
    public XQueryType zeroOrMore(XQueryType itemType);
    public XQueryType oneOrMore(XQueryType itemType);
}
