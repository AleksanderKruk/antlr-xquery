package com.github.akruk.antlrxquery.typesystem.factories;

import java.util.List;

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
    public XQueryItemType itemMap(XQueryItemType keyType, XQuerySequenceType valueType);
    public XQueryItemType itemArray(XQuerySequenceType itemType);
    public XQueryItemType itemFunction(XQuerySequenceType returnType, List<XQuerySequenceType> argumentTypes);

    public XQuerySequenceType string();
    public XQuerySequenceType number();
    public XQuerySequenceType anyNode();
    public XQuerySequenceType anyArray();
    public XQuerySequenceType array(XQuerySequenceType itemType);
    public XQuerySequenceType anyMap();
    public XQuerySequenceType map(XQueryItemType mapKeyType, XQuerySequenceType mapValueType);
    public XQuerySequenceType anyElement();
    public XQuerySequenceType element(String elementName);
    public XQuerySequenceType anyFunction();
    public XQuerySequenceType function(XQuerySequenceType returnType, List<XQuerySequenceType> argumentTypes);
    public XQuerySequenceType anyItem();
    public XQuerySequenceType boolean_();
    public XQuerySequenceType emptySequence();
    public XQuerySequenceType one(XQueryItemType itemType);
    public XQuerySequenceType zeroOrOne(XQueryItemType itemType);
    public XQuerySequenceType zeroOrMore(XQueryItemType itemType);
    public XQuerySequenceType oneOrMore(XQueryItemType itemType);
}
