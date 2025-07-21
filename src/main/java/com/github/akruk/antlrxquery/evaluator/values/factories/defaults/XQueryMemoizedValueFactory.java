package com.github.akruk.antlrxquery.evaluator.values.factories.defaults;

import static com.github.akruk.antlrxquery.evaluator.values.XQueryValue.boolean_;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryFunction;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryMemoizedValueFactory implements XQueryValueFactory {
    private final Map<ParseTree, XQueryValue> createdNodes;
    private final Map<BigDecimal, XQueryValue> createdNumbers;
    private final Map<Integer, XQueryValue> createdIntegers;
    private final Map<String, XQueryValue> createdStrings;
    private final Map<List<XQueryValue>, XQueryValue> createdSequences;
    private final XQueryTypeFactory typeFactory;
    private final XQueryValue TRUE;
    private final XQueryValue FALSE;


    public XQueryMemoizedValueFactory(XQueryTypeFactory typeFactory)
    {
        this( new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), typeFactory);
    }


    public XQueryMemoizedValueFactory(Map<ParseTree, XQueryValue> createdNodes,
            Map<BigDecimal, XQueryValue> createdNumbers, Map<Integer, XQueryValue> createdIntegers,
            Map<String, XQueryValue> createdStrings, Map<List<XQueryValue>, XQueryValue> createdSequences,
            XQueryTypeFactory typeFactory)
    {
        this.createdNodes = createdNodes;
        this.createdNumbers = createdNumbers;
        this.createdIntegers = createdIntegers;
        this.createdStrings = createdStrings;
        this.createdSequences = createdSequences;
        this.typeFactory = typeFactory;
        final var booleanType = typeFactory.boolean_();
        this.TRUE = boolean_(true, booleanType);
        this.FALSE = boolean_(false, booleanType);
    }


    @Override
    public XQueryValue bool(boolean v) {
        if (v) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    @Override
    public XQueryValue functionReference(XQueryFunction f, XQuerySequenceType type) {
        return XQueryValue.functionReference(f, type);
    }

    @Override
    public XQueryValue node(ParseTree v) {
        // TODO:
        XQueryValue returnedNode = createdNodes.computeIfAbsent(v, _ -> XQueryValue.node(v, typeFactory.element(Set.of())));
        return returnedNode;
    }

    @Override
    public XQueryValue number(BigDecimal d) {
        // TODO:
        XQueryValue returnedNumber = createdNumbers.computeIfAbsent(d, _ -> XQueryValue.number(d, typeFactory.number()));
        return returnedNumber;
    }
    @Override
    public XQueryValue number(int integer) {
        XQueryValue returnedNumber = createdIntegers.computeIfAbsent(integer, _ -> XQueryValue.number(integer, typeFactory.number()));
        return returnedNumber;
    }

    @Override
    public XQueryValue sequence(List<XQueryValue> v) {
        if (v.size() == 1)
            return v.get(0);
        XQueryValue returnedSequence = createdSequences.computeIfAbsent(v, _ -> XQueryValue.sequence(v, typeFactory.zeroOrMore(typeFactory.itemAnyItem())));
        return returnedSequence;
    }

    @Override
    public XQueryValue string(String s) {
        XQueryValue returnedString = createdStrings.computeIfAbsent(s, _ -> XQueryValue.string(s, typeFactory.string()));
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

    @Override
    public XQueryValue map(Map<XQueryValue, XQueryValue> value) {
        return XQueryValue.map(value, typeFactory.anyMap());
    }

    @Override
    public XQueryValue record(Map<String, XQueryValue> value) {
        Map<XQueryValue, XQueryValue> converted = new HashMap<>(value.size(), 1.0f);
        for (Map.Entry<String, XQueryValue> entry : value.entrySet()) {
            converted.put(string(entry.getKey()), entry.getValue());
        }
        return map(Map.copyOf(converted));
    }

    @Override
    public XQueryValue array(List<XQueryValue> value) {
        return XQueryValue.array(value, typeFactory.anyArray());
    }


    @Override
    public XQueryValue error(XQueryError error, String message) {
        return XQueryValue.error(error, message, typeFactory.error());
    }

}
