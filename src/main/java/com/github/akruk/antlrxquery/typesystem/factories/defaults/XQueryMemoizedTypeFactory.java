package com.github.akruk.antlrxquery.typesystem.factories.defaults;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.types.*;

public class XQueryMemoizedTypeFactory implements AntlrQueryTypeFactory
{
    private final XQueryItemType ERROR_ITEM_TYPE = XQueryItemType.error(this);
    private final XQueryItemType STRING_ITEM_TYPE = XQueryItemType.string(this);
    private final XQueryItemType NUMBER_ITEM_TYPE = XQueryItemType.number(this);
    private final XQueryItemType ANY_NODE_TYPE = XQueryItemType.anyNode(this);
    private final XQueryItemType ANY_ARRAY = XQueryItemType.anyArray(this);
    private final XQueryItemType BOOLEAN_ITEM_TYPE = XQueryItemType.boolean_(this);
    private final XQueryItemType ANY_ITEM_TYPE = XQueryItemType.anyItem(this);
    private final XQueryItemType ANY_FUNCTION = XQueryItemType.anyFunction(this);
    private final XQueryItemType ANY_MAP = XQueryItemType.anyMap(this);

    private final Map<AntlrQuerySequenceType, AntlrQuerySequenceType> arrays = new HashMap<>();
    private final Map<XQueryItemType, Map<AntlrQuerySequenceType, AntlrQuerySequenceType>> maps=new HashMap<>();
    private final Map<Set<String>, XQueryItemType> enums = new HashMap<>();
    private final Map<Set<QualifiedName>, XQueryItemType> elementTypes = new HashMap<>();
    private final Map<XQueryItemType, AntlrQuerySequenceType> oneTypes = new HashMap<>();
    private final Map<XQueryItemType, AntlrQuerySequenceType> zeroOrOneTypes = new HashMap<>();
    private final Map<XQueryItemType, AntlrQuerySequenceType> zeroOrMoreTypes = new HashMap<>();
    private final Map<XQueryItemType, AntlrQuerySequenceType> oneOrMoreTypes = new HashMap<>();

    private final AntlrQuerySequenceType STRING_TYPE = one(STRING_ITEM_TYPE);
    private final AntlrQuerySequenceType NUMBER_TYPE = one(NUMBER_ITEM_TYPE);
    private final AntlrQuerySequenceType ANY_NODE = one(ANY_NODE_TYPE);
    private final AntlrQuerySequenceType ANY_ARRAY_TYPE = one(ANY_ARRAY);
    private final AntlrQuerySequenceType ANY_MAP_TYPE = one(ANY_MAP);
    private final AntlrQuerySequenceType ERROR_ITEM = one(ERROR_ITEM_TYPE);
    private final AntlrQuerySequenceType ANY_FUNCTION_TYPE = one(ANY_FUNCTION);
    private final AntlrQuerySequenceType ANY_ITEM = one(ANY_ITEM_TYPE);
    private final AntlrQuerySequenceType BOOLEAN_TYPE = one(BOOLEAN_ITEM_TYPE);
    private final AntlrQuerySequenceType EMPTY_SEQUENCE = AntlrQuerySequenceType.emptySequence(this);



    public XQueryMemoizedTypeFactory(final Map<String, Map<String, XQueryItemType>> predefinedNamedTypes) {
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
    public XQueryItemType itemElement(final Set<QualifiedName> elementName) {
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
    public AntlrQuerySequenceType error() {
        return ERROR_ITEM;
    }

    @Override
    public AntlrQuerySequenceType string() {
        return STRING_TYPE;
    }

    @Override
    public XQueryItemType itemEnum(final Set<String> memberNames) {
        return enums.computeIfAbsent(memberNames, k -> XQueryItemType.enum_(k, this));
    }

    @Override
    public AntlrQuerySequenceType enum_(final Set<String> memberNames) {
        return one(itemEnum(memberNames));
    }

    @Override
    public AntlrQuerySequenceType number() {
        return NUMBER_TYPE;
    }

    @Override
    public AntlrQuerySequenceType anyNode() {
        return ANY_NODE;
    }

    @Override
    public AntlrQuerySequenceType anyArray() {
        return ANY_ARRAY_TYPE;
    }

    @Override
    public AntlrQuerySequenceType anyMap() {
        return ANY_MAP_TYPE;
    }

    @Override
    public AntlrQuerySequenceType element(final Set<QualifiedName> elementName) {
        return one(itemElement(elementName));
    }

    @Override
    public XQueryItemType itemArray(final AntlrQuerySequenceType itemType) {
        return XQueryItemType.array(itemType, this);
    }

    @Override
    public XQueryItemType itemFunction(final AntlrQuerySequenceType returnType, final List<AntlrQuerySequenceType> argumentTypes) {
        final List<AntlrQuerySequenceType> argumentTypesEnum = argumentTypes.stream()
                .map(t -> (AntlrQuerySequenceType) t)
                .collect(Collectors.toList());
        return XQueryItemType.function(returnType, argumentTypesEnum, this);
    }
    @Override
    public XQueryItemType itemMap(final XQueryItemType keyType, final AntlrQuerySequenceType valueType) {
        return XQueryItemType.map((XQueryItemType) keyType, (AntlrQuerySequenceType) valueType, this);
    }

    @Override
    public AntlrQuerySequenceType record(final Map<String, XQueryRecordField> fields) {
        return one(itemRecord(fields));
    }

    @Override
    public AntlrQuerySequenceType extensibleRecord(final Map<String, XQueryRecordField> fields) {
        return one(itemExtensibleRecord(fields));
    }

    @Override
    public AntlrQuerySequenceType array(final AntlrQuerySequenceType containedItemType) {
        return arrays.computeIfAbsent(containedItemType, _ -> one(itemArray(containedItemType)));
    }

    @Override
    public AntlrQuerySequenceType map(final XQueryItemType mapKeyType, final AntlrQuerySequenceType mapValueType) {
        final var keyMap = maps.computeIfAbsent(mapKeyType, _-> new HashMap<>());
        return keyMap.computeIfAbsent(mapValueType, _ -> one(itemMap(mapKeyType, mapValueType)));
    }

    @Override
    public AntlrQuerySequenceType function(final AntlrQuerySequenceType returnType, final List<AntlrQuerySequenceType> argumentTypes) {
        return one(itemFunction(returnType, argumentTypes));
    }

    @Override
    public AntlrQuerySequenceType anyFunction() {
        return ANY_FUNCTION_TYPE;
    }

    @Override
    public AntlrQuerySequenceType anyItem() {
        return ANY_ITEM;
    }

    @Override
    public AntlrQuerySequenceType boolean_() {
        return BOOLEAN_TYPE;
    }

    @Override
    public AntlrQuerySequenceType emptySequence() {
        return EMPTY_SEQUENCE;
    }

    @Override
    public AntlrQuerySequenceType one(final XQueryItemType itemType) {
        return oneTypes.computeIfAbsent(itemType,
                _ -> new AntlrQuerySequenceType(this, itemType, Cardinality.ONE));
    }

    @Override
    public AntlrQuerySequenceType zeroOrOne(final XQueryItemType itemType) {
        return zeroOrOneTypes.computeIfAbsent(itemType,
                _ -> new AntlrQuerySequenceType(this, (XQueryItemType) itemType, Cardinality.ZERO_OR_ONE));
    }

    @Override
    public AntlrQuerySequenceType zeroOrMore(final XQueryItemType itemType) {
        return zeroOrMoreTypes.computeIfAbsent(itemType,
                _ -> new AntlrQuerySequenceType(this, (XQueryItemType) itemType, Cardinality.ZERO_OR_MORE));
    }

    @Override
    public AntlrQuerySequenceType oneOrMore(final XQueryItemType itemType) {
        return oneOrMoreTypes.computeIfAbsent(itemType,
                _ -> new AntlrQuerySequenceType(this, (XQueryItemType) itemType, Cardinality.ONE_OR_MORE));
    }

    @Override
    public XQueryItemType itemChoice(final Collection<XQueryItemType> items) {
        return XQueryItemType.choice(this, items);
    }

    @Override
    public AntlrQuerySequenceType choice(final Collection<XQueryItemType> items) {
        if (items.size() == 1) {
            return one(items.stream().findFirst().get());
        }
        return one(itemChoice(items));
    }


    private final Map<String, Map<String, XQueryItemType>> namedTypes;

    @Override
    public NamedItemAccessingResult itemNamedType(final QualifiedName name)
    {
        final var namespace = namedTypes.get(name.namespace());
        if (namespace!=null) {
            final var type = namespace.get(name.name());
            if (type != null) {
                return new NamedItemAccessingResult(type, NamedAccessingStatus.OK);
            }
            return new NamedItemAccessingResult(null, NamedAccessingStatus.UNKNOWN_NAME);
        }
        return new NamedItemAccessingResult(null, NamedAccessingStatus.UNKNOWN_NAMESPACE)  ;
    }



    @Override
    public RegistrationResult registerNamedType(final QualifiedName name, final XQueryItemType itemType)
    {
        final var namespace = namedTypes.computeIfAbsent(name.namespace(), _ -> new HashMap<>());
        final var existing = namespace.put(name.name(), itemType);
        if (existing == null) {
            return new RegistrationResult(itemType, RegistrationStatus.OK);
        } else if (existing.equals(itemType)) {
            return new RegistrationResult(existing, RegistrationStatus.ALREADY_REGISTERED_SAME);
        }
        return new RegistrationResult(existing, RegistrationStatus.ALREADY_REGISTERED_DIFFERENT);
    }

    @Override
    public NamedAccessingResult namedType(final QualifiedName name) {
        final var item = itemNamedType(name);
        switch(item.status()) {
        case OK:
            return new NamedAccessingResult(one(item.type()), NamedAccessingStatus.OK);
        case UNKNOWN_NAME:
            return new NamedAccessingResult(null, NamedAccessingStatus.UNKNOWN_NAME);
        case UNKNOWN_NAMESPACE:
            return new NamedAccessingResult(null, NamedAccessingStatus.UNKNOWN_NAMESPACE);
        }
        return null;
    }

    @Override
    public AntlrQuerySequenceType sequence(XQueryItemType itemType, Cardinality cardinality) {
        return new AntlrQuerySequenceType(this, itemType, cardinality);
    }

}
