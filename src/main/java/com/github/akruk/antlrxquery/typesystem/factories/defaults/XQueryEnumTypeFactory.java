package com.github.akruk.antlrxquery.typesystem.factories.defaults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.*;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumTypeFactory implements XQueryTypeFactory {



    private static final XQueryEnumItemTypeString STRING_ITEM_TYPE = new XQueryEnumItemTypeString();
    @Override
    public XQueryItemType itemString() {
        return STRING_ITEM_TYPE;
    }

    private static final XQueryEnumItemTypeNumber NUMBER_ITEM_TYPE = new XQueryEnumItemTypeNumber();
    @Override
    public XQueryItemType itemNumber() {
        return NUMBER_ITEM_TYPE;
    }

    private static final XQueryEnumItemTypeAnyNode ANY_NODE_TYPE = new XQueryEnumItemTypeAnyNode();
    @Override
    public XQueryItemType itemAnyNode() {
        return ANY_NODE_TYPE;
    }

    private static final XQueryEnumItemTypeAnyArray ANY_ARRAY = new XQueryEnumItemTypeAnyArray();
    @Override
    public XQueryItemType itemAnyArray() {
        return ANY_ARRAY;
    }

    private static final XQueryEnumItemTypeAnyMap ANY_MAP = new XQueryEnumItemTypeAnyMap();
    @Override
    public XQueryItemType itemAnyMap() {
        return ANY_MAP;
    }


    Map<Set<String>, XQueryEnumItemType> elementTypes = new HashMap<>();
    @Override
    public XQueryItemType itemElement(Set<String> elementName) {
        return elementTypes.computeIfAbsent(elementName, k -> new XQueryEnumItemTypeElement(k));
    }

    private static final XQueryEnumItemTypeAnyFunction ANY_FUNCTION = new XQueryEnumItemTypeAnyFunction();
    @Override
    public XQueryItemType itemAnyFunction() {
        return ANY_FUNCTION;
    }

    private static final XQueryEnumItemTypeAnyItem ANY_ITEM_TYPE = new XQueryEnumItemTypeAnyItem();
    @Override
    public XQueryItemType itemAnyItem() {
        return ANY_ITEM_TYPE;
    }

    private static final XQueryEnumItemTypeBoolean BOOLEAN_ITEM_TYPE = new XQueryEnumItemTypeBoolean();
    @Override
    public XQueryItemType itemBoolean() {
        return BOOLEAN_ITEM_TYPE;
    }

    private final XQuerySequenceType STRING_TYPE = one(STRING_ITEM_TYPE);
    @Override
    public XQuerySequenceType string() {
        return STRING_TYPE;
    }

    private final XQuerySequenceType NUMBER_TYPE = one(NUMBER_ITEM_TYPE);
    @Override
    public XQuerySequenceType number() {
        return NUMBER_TYPE;
    }

    private final XQuerySequenceType ANY_NODE = one(ANY_NODE_TYPE);
    @Override
    public XQuerySequenceType anyNode() {
        return ANY_NODE;
    }

    private final XQuerySequenceType ANY_ARRAY_TYPE = one(ANY_ARRAY);
    @Override
    public XQuerySequenceType anyArray() {
        return ANY_ARRAY_TYPE;
    }

    private final XQuerySequenceType ANY_MAP_TYPE = one(ANY_MAP);
    @Override
    public XQuerySequenceType anyMap() {
        return ANY_MAP_TYPE;
    }

    @Override
    public XQuerySequenceType element(Set<String> elementName) {
        return one(itemElement(elementName));
    }

    @Override
    public XQueryItemType itemArray(XQuerySequenceType itemType) {
        return new XQueryEnumItemTypeArray((XQueryEnumSequenceType) itemType);
    }

    @Override
    public XQueryItemType itemFunction(XQuerySequenceType returnType, List<XQuerySequenceType> argumentTypes) {
        List<XQuerySequenceType> argumentTypesEnum = argumentTypes.stream()
                .map(t -> (XQueryEnumSequenceType) t)
                .collect(Collectors.toList());
        return new XQueryEnumItemTypeFunction(returnType, argumentTypesEnum);
    }

    @Override
    public XQueryItemType itemMap(XQueryItemType keyType, XQuerySequenceType valueType) {
        return new XQueryEnumItemTypeMap((XQueryEnumItemType) keyType, (XQueryEnumSequenceType) valueType);
    }

    private final Map<XQuerySequenceType, XQuerySequenceType> arrays = new HashMap<>();
    @Override
    public XQuerySequenceType array(XQuerySequenceType containedItemType) {
        return arrays.computeIfAbsent(containedItemType, it -> one(itemArray(containedItemType)));
    }

    private final Map<XQueryItemType, Map<XQuerySequenceType, XQuerySequenceType>> maps = new HashMap<>();
    @Override
    public XQuerySequenceType map(XQueryItemType mapKeyType, XQuerySequenceType mapValueType) {
        var keyMap = maps.computeIfAbsent(mapKeyType, k-> new HashMap<>());
        return keyMap.computeIfAbsent(mapValueType, k -> one(itemMap(mapKeyType, mapValueType)));
    }

    private final Map<XQuerySequenceType, XQuerySequenceType> functions = new HashMap<>();
    @Override
    public XQuerySequenceType function(XQuerySequenceType returnType, List<XQuerySequenceType> argumentTypes) {
        return functions.computeIfAbsent(returnType, it -> {
            return one(itemFunction(returnType, argumentTypes));
        });
    }

    private final XQuerySequenceType ANY_FUNCTION_TYPE = one(ANY_FUNCTION);
    @Override
    public XQuerySequenceType anyFunction() {
        return ANY_FUNCTION_TYPE;
    }

    private final XQuerySequenceType ANY_ITEM = one(ANY_ITEM_TYPE);
    @Override
    public XQuerySequenceType anyItem() {
        return ANY_ITEM;
    }

    private final XQuerySequenceType BOOLEAN_TYPE = one(BOOLEAN_ITEM_TYPE);
    @Override
    public XQuerySequenceType boolean_() {
        return BOOLEAN_TYPE;
    }

    private final XQuerySequenceType EMPTY_SEQUENCE = new XQueryEnumEmptySequenceType(this);
    @Override
    public XQuerySequenceType emptySequence() {
        return EMPTY_SEQUENCE;
    }

    @Override
    public XQuerySequenceType one(XQueryItemType itemType) {
        return new XQueryEnumSequenceType(this, (XQueryEnumItemType) itemType, XQueryOccurence.ONE);
    }

    @Override
    public XQuerySequenceType zeroOrOne(XQueryItemType itemType) {
        return new XQueryEnumSequenceType(this, (XQueryEnumItemType) itemType, XQueryOccurence.ZERO_OR_ONE);
    }

    @Override
    public XQuerySequenceType zeroOrMore(XQueryItemType itemType) {
        return new XQueryEnumSequenceType(this, (XQueryEnumItemType) itemType, XQueryOccurence.ZERO_OR_MORE);
    }

    @Override
    public XQuerySequenceType oneOrMore(XQueryItemType itemType) {
        return new XQueryEnumSequenceType(this, (XQueryEnumItemType) itemType, XQueryOccurence.ONE_OR_MORE);
    }

}
