package com.github.akruk.antlrxquery.values.factories.defaults;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class XQueryBaseValueFactory implements XQueryValueFactory{
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
        XQueryValue returnedNode = createdNodes.computeIfAbsent(v, t -> new XQueryTreeNode(v));
        return returnedNode;
    }

    @Override
    public XQueryValue number(BigDecimal d) {
        XQueryValue returnedNumber = createdNumbers.computeIfAbsent(d, t -> new XQueryNumber(d));
        return returnedNumber;
    }
    @Override
    public XQueryValue number(int integer) {
        XQueryValue returnedNumber = createdIntegers.computeIfAbsent(integer, t -> new XQueryNumber(integer));
        return returnedNumber;
    }

    @Override
    public XQueryValue sequence(List<XQueryValue> v) {
        XQueryValue returnedSequence = createdSequences.computeIfAbsent(v, t -> new XQuerySequence(v));
        return returnedSequence;
    }

    @Override
    public XQueryValue string(String s) {
        XQueryValue returnedString = createdStrings.computeIfAbsent(s, t -> new XQueryString(s));
        return returnedString;
    }


}
