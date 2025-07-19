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
    private final XQueryItemTypeError ERROR_ITEM_TYPE = new XQueryItemTypeError(this);
    private final XQueryItemTypeString STRING_ITEM_TYPE = new XQueryItemTypeString(this);
    private final XQueryItemTypeNumber NUMBER_ITEM_TYPE = new XQueryItemTypeNumber(this);
    private final XQueryItemTypeAnyNode ANY_NODE_TYPE = new XQueryItemTypeAnyNode(this);
    private final XQueryItemTypeAnyArray ANY_ARRAY = new XQueryItemTypeAnyArray(this);
    private final XQueryItemTypeBoolean BOOLEAN_ITEM_TYPE = new XQueryItemTypeBoolean(this);
    private final XQueryItemTypeAnyItem ANY_ITEM_TYPE = new XQueryItemTypeAnyItem(this);
    private final XQueryItemTypeAnyFunction ANY_FUNCTION = new XQueryItemTypeAnyFunction(this);
    private final XQueryItemTypeAnyMap ANY_MAP = new XQueryItemTypeAnyMap(this);

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
    private final XQuerySequenceType EMPTY_SEQUENCE = new XQueryEmptySequenceType(this);



    public XQueryEnumTypeFactory(final Map<String, XQueryItemType> predefinedNamedTypes) {
        namedTypes = predefinedNamedTypes;
    }

    @Override
    public XQueryItemType itemRecord(final Map<String, XQueryRecordField> fields) {
        return new XQueryItemTypeRecord(fields, this);
    }

    @Override
    public XQueryItemType itemExtensibleRecord(final Map<String, XQueryRecordField> fields) {
        return new XQueryItemTypeExtensibleRecord(fields, this);
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
        return elementTypes.computeIfAbsent(elementName, k -> new XQueryItemTypeElement(k, this));
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
        return enums.computeIfAbsent(memberNames, k -> new XQueryItemTypeEnum(k, this));
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
        return new XQueryItemTypeArray((XQuerySequenceType) itemType, this);
    }

    @Override
    public XQueryItemType itemFunction(final XQuerySequenceType returnType, final List<XQuerySequenceType> argumentTypes) {
        final List<XQuerySequenceType> argumentTypesEnum = argumentTypes.stream()
                .map(t -> (XQuerySequenceType) t)
                .collect(Collectors.toList());
        return new XQueryItemTypeFunction(returnType, argumentTypesEnum, this);
    }
    @Override
    public XQueryItemType itemMap(final XQueryItemType keyType, final XQuerySequenceType valueType) {
        return new XQueryItemTypeMap((XQueryItemType) keyType, (XQuerySequenceType) valueType, this);
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
<<<<<<< HEAD
                _ -> new XQuerySequenceType(this, (XQueryItemType) itemType, XQueryOccurence.ONE));
=======
                _ -> new XQuerySequenceType(this, itemType, XQueryOccurence.ONE));
>>>>>>> language-features/lookup-expression
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
        return new XQueryChoiceItemType(items, this);
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
