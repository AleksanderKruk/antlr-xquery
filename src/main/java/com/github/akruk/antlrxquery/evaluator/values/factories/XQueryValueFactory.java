package com.github.akruk.antlrxquery.evaluator.values.factories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryFunction;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

public interface XQueryValueFactory {
    public XQueryValue error(XQueryError error, String message);
    public XQueryValue bool(boolean v);
    public XQueryValue number(BigDecimal d);
    public XQueryValue number(int integer);
    public XQueryValue string(String s);
    public XQueryValue emptyString();
    public XQueryValue sequence(List<XQueryValue> v);
    public XQueryValue emptySequence();
    public XQueryValue functionReference(XQueryFunction f, XQuerySequenceType type);
    public XQueryValue node(ParseTree v);
    public XQueryValue array(List<XQueryValue> value);
    public XQueryValue map(Map<XQueryValue, XQueryValue> value);
    public XQueryValue record(Map<String, XQueryValue> value);
}
