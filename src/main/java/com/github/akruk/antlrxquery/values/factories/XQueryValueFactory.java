package com.github.akruk.antlrxquery.values.factories;

import java.math.BigDecimal;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.values.XQueryFunction;
import com.github.akruk.antlrxquery.values.XQueryValue;

public interface XQueryValueFactory {
    public XQueryValue bool(boolean v);
    public XQueryValue number(BigDecimal d);
    public XQueryValue string(String s);
    public XQueryValue sequence(List<XQueryValue> v);
    public XQueryValue functionReference(XQueryFunction f);
    public XQueryValue node(ParseTree v);
}
