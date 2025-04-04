package com.github.akruk.antlrxquery.typesystem.factories;

import com.github.akruk.antlrxquery.typesystem.XQueryType;

public interface XQueryTypeFactory {
    public XQueryType string();
    public XQueryType number();
    public XQueryType anyNode();
    public XQueryType anyArray();
    public XQueryType anyMap();
    public XQueryType anyElement();
    public XQueryType anyFunction();
    public XQueryType anyItem();
    public XQueryType boolean_();
}
