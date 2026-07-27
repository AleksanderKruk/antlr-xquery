package com.github.akruk.antlrxquery.evaluator.values.factories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryFunction;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;

public interface XQueryValueFactory {
    XQueryValue error(XQueryError error, String message);
    XQueryValue bool(boolean v);

    XQueryValue node(String grammar, ParseTree v);

    XQueryValue number(BigDecimal d);
    XQueryValue number(int integer);
    XQueryValue string(String s);
    XQueryValue emptyString();
    XQueryValue sequence(List<XQueryValue> v);
    XQueryValue emptySequence();
    XQueryValue functionReference(XQueryFunction f, AntlrQuerySequenceType type);
    XQueryValue array(List<XQueryValue> value);
    XQueryValue map(Map<XQueryValue, XQueryValue> value);
    XQueryValue record(Map<String, XQueryValue> value);
}
