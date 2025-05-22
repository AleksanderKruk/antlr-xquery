package com.github.akruk.antlrxquery.values.factories.defaults;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.values.XQueryBoolean;
import com.github.akruk.antlrxquery.values.XQueryFunction;
import com.github.akruk.antlrxquery.values.XQueryFunctionReference;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQuerySequence;
import com.github.akruk.antlrxquery.values.XQueryString;
import com.github.akruk.antlrxquery.values.XQueryTreeNode;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryMemoizedValueFactory implements XQueryValueFactory{
    private Map<ParseTree, XQueryValue> createdNodes = new HashMap<>();
    private Map<BigDecimal, XQueryValue> createdNumbers = new HashMap<>();
    private Map<Integer, XQueryValue> createdIntegers = new HashMap<>();
    private Map<String, XQueryValue> createdStrings = new HashMap<>();
    private Map<List<XQueryValue>, XQueryValue> createdSequences = new HashMap<>();
    @Override
    public XQueryValue bool(boolean v) {
        return XQueryBoolean.of(v);
    }

    @Override
    public XQueryValue functionReference(XQueryFunction f) {
        return new XQueryFunctionReference(f);
    }

    @Override
    public XQueryValue node(ParseTree v) {
        XQueryValue returnedNode = createdNodes.computeIfAbsent(v, _ -> new XQueryTreeNode(v));
        return returnedNode;
    }

    @Override
    public XQueryValue number(BigDecimal d) {
        XQueryValue returnedNumber = createdNumbers.computeIfAbsent(d, _ -> new XQueryNumber(d));
        return returnedNumber;
    }
    @Override
    public XQueryValue number(int integer) {
        XQueryValue returnedNumber = createdIntegers.computeIfAbsent(integer, _ -> new XQueryNumber(integer));
        return returnedNumber;
    }

    @Override
    public XQueryValue sequence(List<XQueryValue> v) {
        XQueryValue returnedSequence = createdSequences.computeIfAbsent(v, _ -> new XQuerySequence(v));
        return returnedSequence;
    }

    @Override
    public XQueryValue string(String s) {
        XQueryValue returnedString = createdStrings.computeIfAbsent(s, _ -> new XQueryString(s));
        return returnedString;
    }

    @Override
    public XQueryValue emptyString() {
        return string("");
    }

    @Override
    public XQueryValue emptySequence() {
        return sequence(List.of());
    }


}
