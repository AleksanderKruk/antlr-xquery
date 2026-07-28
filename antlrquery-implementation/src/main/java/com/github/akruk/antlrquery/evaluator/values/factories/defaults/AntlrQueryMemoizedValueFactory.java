package com.github.akruk.antlrquery.evaluator.values.factories.defaults;

import static com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue.boolean_;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryFunction;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;

public class AntlrQueryMemoizedValueFactory implements AntlrQueryValueFactory {
    private final Map<ParseTree, AntlrQueryValue> createdNodes;
    private final Map<BigDecimal, AntlrQueryValue> createdNumbers;
    private final Map<Integer, AntlrQueryValue> createdIntegers;
    private final Map<String, AntlrQueryValue> createdStrings;
    private final Map<List<AntlrQueryValue>, AntlrQueryValue> createdSequences;
    private final AntlrQueryTypeFactory typeFactory;
    private final AntlrQueryValue TRUE;
    private final AntlrQueryValue FALSE;


    public AntlrQueryMemoizedValueFactory(AntlrQueryTypeFactory typeFactory)
    {
        this( new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), typeFactory);
    }


    public AntlrQueryMemoizedValueFactory(Map<ParseTree, AntlrQueryValue> createdNodes,
                                          Map<BigDecimal, AntlrQueryValue> createdNumbers, Map<Integer, AntlrQueryValue> createdIntegers,
                                          Map<String, AntlrQueryValue> createdStrings, Map<List<AntlrQueryValue>, AntlrQueryValue> createdSequences,
                                          AntlrQueryTypeFactory typeFactory)
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
        this.EMPTY_SEQUENCE = AntlrQueryValue.emptySequence(typeFactory.emptySequence());
    }


    @Override
    public AntlrQueryValue bool(boolean v) {
        if (v) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    @Override
    public AntlrQueryValue functionReference(AntlrQueryFunction f, AntlrQuerySequenceType type) {
        return AntlrQueryValue.functionReference(f, type);
    }

    @Override
    public AntlrQueryValue node(String grammar, ParseTree v) {
        // TODO:
        String name = ParseTree.class.getName();
        return createdNodes.computeIfAbsent(
                v, _ -> AntlrQueryValue.node(v, typeFactory.element(grammar, Set.of(new NamespaceResolver.QualifiedName(grammar, name)))));
    }

    @Override
    public AntlrQueryValue number(BigDecimal d) {
        // TODO:
        return createdNumbers.computeIfAbsent(d, _ -> AntlrQueryValue.number(d, typeFactory.number(NumericRange.FULL)));
    }
    @Override
    public AntlrQueryValue number(int integer) {
        return createdIntegers.computeIfAbsent(integer, _ -> AntlrQueryValue.number(integer, typeFactory.number(NumericRange.FULL)));
    }

    final AntlrQueryValue EMPTY_SEQUENCE;

    @Override
    public AntlrQueryValue sequence(List<AntlrQueryValue> v) {
        if (v.isEmpty())
            return EMPTY_SEQUENCE;
        if (v.size() == 1)
            return v.getFirst();
        return createdSequences.computeIfAbsent(
            v, _ -> AntlrQueryValue.sequence(v, typeFactory.zeroOrMore(typeFactory.itemAnyItem())));
    }

    @Override
    public AntlrQueryValue string(String s) {
        return createdStrings.computeIfAbsent(s, _ -> AntlrQueryValue.string(s, typeFactory.string()));
    }

    @Override
    public AntlrQueryValue emptyString() {
        return string("");
    }

    @Override
    public AntlrQueryValue emptySequence() {
        return EMPTY_SEQUENCE;
    }

    @Override
    public AntlrQueryValue map(Map<AntlrQueryValue, AntlrQueryValue> value) {
        return AntlrQueryValue.map(value, typeFactory.anyMap());
    }

    @Override
    public AntlrQueryValue record(Map<String, AntlrQueryValue> value) {
        Map<AntlrQueryValue, AntlrQueryValue> converted = new HashMap<>(value.size(), 1.0f);
        for (Map.Entry<String, AntlrQueryValue> entry : value.entrySet()) {
            converted.put(string(entry.getKey()), entry.getValue());
        }
        return map(Map.copyOf(converted));
    }

    @Override
    public AntlrQueryValue array(List<AntlrQueryValue> value) {
        return AntlrQueryValue.array(value, typeFactory.anyArray());
    }


    @Override
    public AntlrQueryValue error(AntlrQueryError error, String message) {
        return AntlrQueryValue.error(error, message, typeFactory.error());
    }

}
