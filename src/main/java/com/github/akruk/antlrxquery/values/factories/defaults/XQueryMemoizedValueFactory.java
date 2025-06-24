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


    private XQueryBoolean TRUE = new XQueryBoolean(true, this);
    private XQueryBoolean FALSE = new XQueryBoolean(false, this);


    @Override
    public XQueryValue bool(boolean v) {
        if (v) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    @Override
    public XQueryValue functionReference(XQueryFunction f) {
        return new XQueryFunctionReference(f, this);
    }

    @Override
    public XQueryValue node(ParseTree v) {
        XQueryValue returnedNode = createdNodes.computeIfAbsent(v, _ -> new XQueryTreeNode(v, this));
        return returnedNode;
    }

    @Override
    public XQueryValue number(BigDecimal d) {
        XQueryValue returnedNumber = createdNumbers.computeIfAbsent(d, _ -> new XQueryNumber(d, this));
        return returnedNumber;
    }
    @Override
    public XQueryValue number(int integer) {
        XQueryValue returnedNumber = createdIntegers.computeIfAbsent(integer, _ -> new XQueryNumber(integer, this));
        return returnedNumber;
    }

    @Override
    public XQueryValue sequence(List<XQueryValue> v) {
        XQueryValue returnedSequence = createdSequences.computeIfAbsent(v, _ -> new XQuerySequence(v, this));
        return returnedSequence;
    }

    @Override
    public XQueryValue string(String s) {
        XQueryValue returnedString = createdStrings.computeIfAbsent(s, _ -> new XQueryString(s, this));
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
