package com.github.akruk.antlrxquery.typesystem.factories.defaults;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.defaults.*;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumTypeFactory implements XQueryTypeFactory {
    private final XQueryItemType ERROR_ITEM_TYPE = XQueryItemType.error(this);
    private final XQueryItemType STRING_ITEM_TYPE = XQueryItemType.string(this);
    private final XQueryItemType NUMBER_ITEM_TYPE = XQueryItemType.number(this);
    private final XQueryItemType ANY_NODE_TYPE = XQueryItemType.anyNode(this);
    private final XQueryItemType ANY_ARRAY = XQueryItemType.anyArray(this);
    private final XQueryItemType BOOLEAN_ITEM_TYPE = XQueryItemType.boolean_(this);
    private final XQueryItemType ANY_ITEM_TYPE = XQueryItemType.anyItem(this);
    private final XQueryItemType ANY_FUNCTION = XQueryItemType.anyFunction(this);
    private final XQueryItemType ANY_MAP = XQueryItemType.anyMap(this);

    private final Map<XQuerySequenceType, XQuerySequenceType> arrays = new HashMap<>();
    private final Map<XQueryItemType, Map<XQuerySequenceType, XQuerySequenceType>> maps=new HashMap<>();
    private final Map<Set<String>, XQueryItemType> enums = new HashMap<>();
    private final Map<Set<String>, XQueryItemType> elementTypes = new HashMap<>();
    private final Map<XQueryItemType, XQuerySequenceType> oneTypes = new HashMap<>();
    private final Map<XQueryItemType, XQuerySequenceType> zeroOrOneTypes = new HashMap<>();
    private final Map<XQueryItemType, XQuerySequenceType> zeroOrMoreTypes = new HashMap<>();
    private final Map<XQueryItemType, XQuerySequenceType> oneOrMoreTypes = new HashMap<>();

    private final XQuerySequenceType STRING_TYPE = one(STRING_ITEM_TYPE);
    private final XQuerySequenceType NUMBER_TYPE = one(NUMBER_ITEM_TYPE);
    private final XQuerySequenceType ANY_NODE = one(ANY_NODE_TYPE);
    private final XQuerySequenceType ANY_ARRAY_TYPE = one(ANY_ARRAY);
    private final XQuerySequenceType ANY_MAP_TYPE = one(ANY_MAP);
    private final XQuerySequenceType ERROR_ITEM = one(ERROR_ITEM_TYPE);
    private final XQuerySequenceType ANY_FUNCTION_TYPE = one(ANY_FUNCTION);
    private final XQuerySequenceType ANY_ITEM = one(ANY_ITEM_TYPE);
    private final XQuerySequenceType BOOLEAN_TYPE = one(BOOLEAN_ITEM_TYPE);
    private final XQuerySequenceType EMPTY_SEQUENCE = XQuerySequenceType.emptySequence(this);



    public XQueryEnumTypeFactory(final Map<String, XQueryItemType> predefinedNamedTypes) {
        namedTypes = predefinedNamedTypes;
    }

    @Override
    public XQueryItemType itemRecord(final Map<String, XQueryRecordField> fields) {
        return XQueryItemType.contrainedRecord(fields, this);
    }

    @Override
    public XQueryItemType itemExtensibleRecord(final Map<String, XQueryRecordField> fields) {
        return XQueryItemType.extensibleRecord(fields, this);
    }

    @Override
    public XQueryItemType itemError() {
        return ERROR_ITEM_TYPE;
    }

    @Override
    public XQueryItemType itemString() {
        return STRING_ITEM_TYPE;
    }

    @Override
    public XQueryItemType itemNumber() {
        return NUMBER_ITEM_TYPE;
    }

    @Override
    public XQueryItemType itemAnyNode() {
        return ANY_NODE_TYPE;
    }

    @Override
    public XQueryItemType itemAnyArray() {
        return ANY_ARRAY;
    }

    @Override
    public XQueryItemType itemAnyMap() {
        return ANY_MAP;
    }


    @Override
    public XQueryItemType itemElement(final Set<String> elementName) {
        return elementTypes.computeIfAbsent(elementName, k -> XQueryItemType.element(k, this));
    }

    @Override
    public XQueryItemType itemAnyFunction() {
        return ANY_FUNCTION;
    }

    @Override
    public XQueryItemType itemAnyItem() {
        return ANY_ITEM_TYPE;
    }

    @Override
    public XQueryItemType itemBoolean() {
        return BOOLEAN_ITEM_TYPE;
    }

    @Override
    public XQuerySequenceType error() {
        return ERROR_ITEM;
    }

    @Override
    public XQuerySequenceType string() {
        return STRING_TYPE;
    }

    @Override
    public XQueryItemType itemEnum(final Set<String> memberNames) {
        return enums.computeIfAbsent(memberNames, k -> XQueryItemType.enum_(k, this));
    }

    @Override
    public XQuerySequenceType enum_(final Set<String> memberNames) {
        return one(itemEnum(memberNames));
    }

    @Override
    public XQuerySequenceType number() {
        return NUMBER_TYPE;
    }

    @Override
    public XQuerySequenceType anyNode() {
        return ANY_NODE;
    }

    @Override
    public XQuerySequenceType anyArray() {
        return ANY_ARRAY_TYPE;
    }

    @Override
    public XQuerySequenceType anyMap() {
        return ANY_MAP_TYPE;
    }

    @Override
    public XQuerySequenceType element(final Set<String> elementName) {
        return one(itemElement(elementName));
    }

    @Override
    public XQueryItemType itemArray(final XQuerySequenceType itemType) {
        return XQueryItemType.array(itemType, this);
    }

    @Override
    public XQueryItemType itemFunction(final XQuerySequenceType returnType, final List<XQuerySequenceType> argumentTypes) {
        final List<XQuerySequenceType> argumentTypesEnum = argumentTypes.stream()
                .map(t -> (XQuerySequenceType) t)
                .collect(Collectors.toList());
        return XQueryItemType.function(returnType, argumentTypesEnum, this);
    }
    @Override
    public XQueryItemType itemMap(final XQueryItemType keyType, final XQuerySequenceType valueType) {
        return XQueryItemType.map((XQueryItemType) keyType, (XQuerySequenceType) valueType, this);
    }

    @Override
    public XQuerySequenceType record(final Map<String, XQueryRecordField> fields) {
        return one(itemRecord(fields));
    }

    @Override
    public XQuerySequenceType extensibleRecord(final Map<String, XQueryRecordField> fields) {
        return one(itemExtensibleRecord(fields));
    }

    @Override
    public XQuerySequenceType array(final XQuerySequenceType containedItemType) {
        return arrays.computeIfAbsent(containedItemType, _ -> one(itemArray(containedItemType)));
    }

    @Override
    public XQuerySequenceType map(final XQueryItemType mapKeyType, final XQuerySequenceType mapValueType) {
        final var keyMap = maps.computeIfAbsent(mapKeyType, _-> new HashMap<>());
        return keyMap.computeIfAbsent(mapValueType, _ -> one(itemMap(mapKeyType, mapValueType)));
    }

    @Override
    public XQuerySequenceType function(final XQuerySequenceType returnType, final List<XQuerySequenceType> argumentTypes) {
        return one(itemFunction(returnType, argumentTypes));
    }

    @Override
    public XQuerySequenceType anyFunction() {
        return ANY_FUNCTION_TYPE;
    }

    @Override
    public XQuerySequenceType anyItem() {
        return ANY_ITEM;
    }

    @Override
    public XQuerySequenceType boolean_() {
        return BOOLEAN_TYPE;
    }

    @Override
    public XQuerySequenceType emptySequence() {
        return EMPTY_SEQUENCE;
    }

    @Override
    public XQuerySequenceType one(final XQueryItemType itemType) {
        return oneTypes.computeIfAbsent(itemType,
                _ -> new XQuerySequenceType(this, itemType, XQueryOccurence.ONE));
    }

    @Override
    public XQuerySequenceType zeroOrOne(final XQueryItemType itemType) {
        return zeroOrOneTypes.computeIfAbsent(itemType,
                _ -> new XQuerySequenceType(this, (XQueryItemType) itemType, XQueryOccurence.ZERO_OR_ONE));
    }

    @Override
    public XQuerySequenceType zeroOrMore(final XQueryItemType itemType) {
        return zeroOrMoreTypes.computeIfAbsent(itemType,
                _ -> new XQuerySequenceType(this, (XQueryItemType) itemType, XQueryOccurence.ZERO_OR_MORE));
    }

    @Override
    public XQuerySequenceType oneOrMore(final XQueryItemType itemType) {
        return oneOrMoreTypes.computeIfAbsent(itemType,
                _ -> new XQuerySequenceType(this, (XQueryItemType) itemType, XQueryOccurence.ONE_OR_MORE));
    }

    @Override
    public XQueryItemType itemChoice(final Collection<XQueryItemType> items) {
        return XQueryItemType.choice(this, items);
    }

    @Override
    public XQuerySequenceType choice(final Collection<XQueryItemType> items) {
        if (items.size() == 1) {
            return one(items.stream().findFirst().get());
        }
        return one(itemChoice(items));
    }


    private final Map<String, XQueryItemType> namedTypes;

    public XQueryItemType registerNamedType(final String name, final XQueryItemType aliasedType) {
        return namedTypes.put(name, aliasedType);
    }


    @Override
    public XQueryItemType itemNamedType(final String name) {
        return namedTypes.get(name);
    }

    @Override
    public XQuerySequenceType namedType(final String name) {
        var item = itemNamedType(name);
        return (item != null)? one(itemNamedType(name)) : null;
    }
}
