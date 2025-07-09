package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class XQueryEnumItemType implements XQueryItemType {
    private static final int STRING = XQueryTypes.STRING.ordinal();
    private static final int ENUM = XQueryTypes.ENUM.ordinal();
    private static final int BOOLEAN = XQueryTypes.BOOLEAN.ordinal();
    private static final int NUMBER = XQueryTypes.NUMBER.ordinal();
    private static final int ERROR = XQueryTypes.ERROR.ordinal();
    private static final int ANY_ITEM = XQueryTypes.ANY_ITEM.ordinal();
    private static final int ANY_MAP = XQueryTypes.ANY_MAP.ordinal();
    private static final int MAP = XQueryTypes.MAP.ordinal();
    private static final int CHOICE = XQueryTypes.CHOICE.ordinal();
    private static final int ANY_ARRAY = XQueryTypes.ANY_ARRAY.ordinal();
    private static final int ARRAY = XQueryTypes.ARRAY.ordinal();
    private static final int ANY_FUNCTION = XQueryTypes.ANY_FUNCTION.ordinal();
    private static final int FUNCTION = XQueryTypes.FUNCTION.ordinal();
    private static final int RECORD = XQueryTypes.RECORD.ordinal();
    private static final int EXTENSIBLE_RECORD = XQueryTypes.EXTENSIBLE_RECORD.ordinal();
    private final XQueryTypes type;
    private final int typeOrdinal;

    private final BinaryOperator[][] sequenceItemMerger;
    private final BinaryOperator[][] unionItemMerger;
    private final BinaryOperator[][] intersectionItemMerger;
    private final XQueryTypeFactory typeFactory;
    private final Collection<XQueryItemType> itemTypes;

    public Collection<XQueryItemType> getItemTypes() {
        return itemTypes;
    }
    public XQueryEnumItemType(final XQueryTypes type,
                                final List<XQuerySequenceType> argumentTypes,
                                final XQuerySequenceType returnedType,
                                final XQuerySequenceType arrayType,
                                final XQueryEnumItemType key,
                                final XQuerySequenceType mapValueType,
                                final Set<String> elementNames,
                                final XQueryTypeFactory factory,
                                final Collection<XQueryItemType> itemTypes)
    {
        this.type = type;
        this.typeOrdinal = type.ordinal();
        this.argumentTypes = argumentTypes;
        this.returnedType = returnedType;
        this.arrayType = arrayType;
        this.mapKeyType = key;
        this.mapValueType = mapValueType;
        this.elementNames = elementNames;
        this.typeFactory = factory;
        this.itemTypes = itemTypes;

        // Union merging preparation
        final int ELEMENT = XQueryTypes.ELEMENT.ordinal();
        final int ANY_NODE = XQueryTypes.ANY_NODE.ordinal();

        unionItemMerger = new BinaryOperator[typesCount][typesCount];
        unionItemMerger[ELEMENT][ELEMENT] = (i1, i2) -> {
            final var i1_ = (XQueryEnumItemType) i1;
            final var i2_ = (XQueryEnumItemType) i2;
            final var i1Elements = i1_.getElementNames();
            final var i2ELements = i2_.getElementNames();
            final Set<String> mergedElements = new HashSet<>(i1Elements.size() + i2ELements.size());
            mergedElements.addAll(i1Elements);
            mergedElements.addAll(i2ELements);
            return typeFactory.itemElement(mergedElements);
        };
        final BinaryOperator<XQueryItemType> anyNodeReturn = (_, _) -> {
            return typeFactory.itemAnyNode();
        };
        unionItemMerger[ELEMENT][ANY_NODE] = anyNodeReturn;
        unionItemMerger[ANY_NODE][ELEMENT] = anyNodeReturn;
        unionItemMerger[ANY_NODE][ANY_NODE] = anyNodeReturn;



        intersectionItemMerger = new BinaryOperator[typesCount][typesCount];
        intersectionItemMerger[ELEMENT][ELEMENT] = (i1, i2) -> {
            final var i1_ = (XQueryEnumItemType) i1;
            final var i2_ = (XQueryEnumItemType) i2;
            final var i1Elements = i1_.getElementNames();
            final var i2ELements = i2_.getElementNames();
            final Set<String> mergedElements = new HashSet<>(i1Elements.size());
            mergedElements.addAll(i1Elements);
            mergedElements.retainAll(i2ELements);
            return typeFactory.itemElement(mergedElements);
        };
        intersectionItemMerger[ELEMENT][ANY_NODE] = (i1, _) -> {
            return i1;
        };
        intersectionItemMerger[ANY_NODE][ELEMENT] = (_, i2) -> {
            return i2;
        };
        intersectionItemMerger[ANY_NODE][ANY_NODE] = anyNodeReturn;

        BinaryOperator<XQueryItemType> error = (_, _) -> typeFactory.itemError();
        BinaryOperator<XQueryItemType> anyItem = (_, _) -> typeFactory.itemAnyItem();
        BinaryOperator<XQueryItemType> anyNode = (_, _) -> typeFactory.itemAnyNode();
        BinaryOperator<XQueryItemType> simpleChoice = (x, y) -> typeFactory.itemChoice(Set.of(x, y));
        BinaryOperator<XQueryItemType> leftMergeChoice = (x, y) -> {
            var leftItems = ((XQueryEnumChoiceItemType) x).getItemTypes();
            Set<XQueryItemType> sum = new HashSet<>(leftItems.size()+1);
            sum.addAll(leftItems);
            sum.add(y);
            return typeFactory.itemChoice(sum);
        };
        BinaryOperator<XQueryItemType> rightMergeChoice = (x, y) -> {
            var rightItems = ((XQueryEnumChoiceItemType) y).getItemTypes();
            Set<XQueryItemType> sum = new HashSet<>(rightItems.size()+1);
            sum.addAll(rightItems);
            sum.add(x);
            return typeFactory.itemChoice(sum);
        };

        BinaryOperator<XQueryItemType> choiceMerging = (x, y) -> {
            var leftItems = ((XQueryEnumChoiceItemType) x).getItemTypes();
            var rightItems = ((XQueryEnumChoiceItemType) y).getItemTypes();
            Set<XQueryItemType> sum = new HashSet<>(rightItems.size()+leftItems.size());
            sum.addAll(leftItems);
            sum.addAll(rightItems);
            return typeFactory.itemChoice(sum);
        };

        sequenceItemMerger = new BinaryOperator[typesCount][typesCount];
        for (int i = 0; i < typesCount; i++) {
            sequenceItemMerger[ERROR][i] = error;
            sequenceItemMerger[i][ERROR] = error;
        }

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            sequenceItemMerger[ANY_ITEM][i] = anyItem;
            sequenceItemMerger[i][ANY_ITEM] = anyItem;
        }



        BinaryOperator<XQueryItemType> elementSequenceMerger = (x, y) -> {
            var els1 = ((XQueryEnumItemTypeElement) x).getElementNames();
            var els2 = ((XQueryEnumItemTypeElement) y).getElementNames();
            Set<String> merged = new HashSet<>(els1.size() + els2.size());
            merged.addAll(els1);
            merged.addAll(els2);
            return typeFactory.itemElement(merged);
        };

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            sequenceItemMerger[ANY_NODE][i] = simpleChoice;
            sequenceItemMerger[i][ANY_NODE] = simpleChoice;
        }
        sequenceItemMerger[ANY_NODE][ANY_NODE] = anyNode;
        sequenceItemMerger[ANY_NODE][ELEMENT] = anyNode;
        sequenceItemMerger[ELEMENT][ANY_NODE] = anyNode;
        sequenceItemMerger[ANY_NODE][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][ANY_NODE] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            sequenceItemMerger[ELEMENT][i] = simpleChoice;
            sequenceItemMerger[i][ELEMENT] = simpleChoice;
        }
        sequenceItemMerger[ELEMENT][ELEMENT] = elementSequenceMerger;
        sequenceItemMerger[ELEMENT][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][ELEMENT] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            sequenceItemMerger[ANY_MAP][i] = simpleChoice;
            sequenceItemMerger[i][ANY_MAP] = simpleChoice;
        }
        BinaryOperator<XQueryItemType> anyMap = (_, _) -> typeFactory.itemAnyMap();
        sequenceItemMerger[ANY_MAP][ANY_MAP] = anyMap;
        sequenceItemMerger[ANY_MAP][MAP] = anyMap;
        sequenceItemMerger[MAP][ANY_MAP] = anyMap;
        sequenceItemMerger[ANY_MAP][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][ANY_MAP] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            sequenceItemMerger[MAP][i] = simpleChoice;
            sequenceItemMerger[i][MAP] = simpleChoice;
        }

        final BinaryOperator<XQueryItemType> mapMerging = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeMap) x;
            final var y_ = (XQueryEnumItemTypeMap) y;
            final var xKey = x_.getMapKeyType();
            final var yKey = y_.getMapKeyType();
            final var xValue = x_.getMapValueType();
            final var yValue = y_.getMapValueType();
            final var mergedKeyType = xKey.sequenceMerge(yKey);
            final var mergedValueType = xValue.sequenceMerge(yValue);
            return typeFactory.itemMap(mergedKeyType, mergedValueType);
        };

        sequenceItemMerger[MAP][MAP] = mapMerging;
        sequenceItemMerger[ANY_MAP][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][ANY_MAP] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            sequenceItemMerger[ANY_ARRAY][i] = simpleChoice;
            sequenceItemMerger[i][ANY_ARRAY] = simpleChoice;
        }
        BinaryOperator<XQueryItemType> anyArray = (_, _) -> typeFactory.itemAnyArray();
        sequenceItemMerger[ANY_ARRAY][ANY_ARRAY] = anyArray;
        sequenceItemMerger[ARRAY][ANY_ARRAY] = anyArray;
        sequenceItemMerger[ANY_ARRAY][ARRAY] = anyArray;
        sequenceItemMerger[ANY_ARRAY][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][ANY_ARRAY] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            sequenceItemMerger[ANY_ARRAY][i] = simpleChoice;
            sequenceItemMerger[i][ANY_ARRAY] = simpleChoice;
        }
        final BinaryOperator<XQueryItemType> arrayMerging = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeArray) x;
            final var y_ = (XQueryEnumItemTypeArray) y;
            final var xArrayType = x_.getArrayType();
            final var yArrayType = y_.getArrayType();
            final var merged = xArrayType.sequenceMerge(yArrayType);
            return typeFactory.itemArray(merged);
        };
        sequenceItemMerger[ARRAY][ARRAY] = arrayMerging;
        sequenceItemMerger[ARRAY][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][ARRAY] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            sequenceItemMerger[ANY_FUNCTION][i] = simpleChoice;
            sequenceItemMerger[i][ANY_FUNCTION] = simpleChoice;
        }
        sequenceItemMerger[ANY_FUNCTION][ANY_FUNCTION] = arrayMerging;
        sequenceItemMerger[ANY_FUNCTION][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][ANY_FUNCTION] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            sequenceItemMerger[FUNCTION][i] = simpleChoice;
            sequenceItemMerger[i][FUNCTION] = simpleChoice;
        }
        final BinaryOperator<XQueryItemType> functionMerging = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeFunction) x;
            final var y_ = (XQueryEnumItemTypeFunction) y;
            final var xReturnedType = x_.getReturnedType();
            final var yReturnedType = y_.getReturnedType();
            final var xArgs = x_.getArgumentTypes();
            final var yArgs = y_.getArgumentTypes();
            // final var merged = xArrayType.sequenceMerge(yArrayType);
            // return typeFactory.itemArray(merged);
            return null;
        };
        sequenceItemMerger[FUNCTION][FUNCTION] = functionMerging;
        sequenceItemMerger[FUNCTION][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][FUNCTION] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            if (i == FUNCTION) continue;
            sequenceItemMerger[ENUM][i] = simpleChoice;
            sequenceItemMerger[i][ENUM] = simpleChoice;
        }
        final BinaryOperator<XQueryItemType> enumMerging = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeEnum) x;
            final var y_ = (XQueryEnumItemTypeEnum) y;
            final var xMembers = x_.getEnumMembers();
            final var yMembers = y_.getEnumMembers();
            final var merged = new HashSet<String>(xMembers.size() + yMembers.size());
            merged.addAll(xMembers);
            merged.addAll(yMembers);
            return typeFactory.itemEnum(merged);
        };
        final BinaryOperator<XQueryItemType> itemString = (_, _) -> typeFactory.itemString();
        sequenceItemMerger[ENUM][ENUM] = enumMerging;
        sequenceItemMerger[ENUM][STRING] = itemString;
        sequenceItemMerger[STRING][ENUM] = itemString;
        sequenceItemMerger[ENUM][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][ENUM] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            if (i == FUNCTION) continue;
            if (i == ENUM) continue;
            sequenceItemMerger[RECORD][i] = simpleChoice;
            sequenceItemMerger[i][RECORD] = simpleChoice;
        }
        final BinaryOperator<XQueryItemType> recordMerging = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeEnum) x;
            final var y_ = (XQueryEnumItemTypeEnum) y;
            final var xMembers = x_.getEnumMembers();
            final var yMembers = y_.getEnumMembers();
            final var merged = new HashSet<String>(xMembers.size() + yMembers.size());
            merged.addAll(xMembers);
            merged.addAll(yMembers);
            return typeFactory.itemEnum(merged);
        };
        final BinaryOperator<XQueryItemType> extensibleRecordMerging = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeEnum) x;
            final var y_ = (XQueryEnumItemTypeEnum) y;
            final var xMembers = x_.getEnumMembers();
            final var yMembers = y_.getEnumMembers();
            final var merged = new HashSet<String>(xMembers.size() + yMembers.size());
            merged.addAll(xMembers);
            merged.addAll(yMembers);
            return typeFactory.itemEnum(merged);
        };
        sequenceItemMerger[RECORD][RECORD] = recordMerging;
        sequenceItemMerger[EXTENSIBLE_RECORD][RECORD] = recordMerging;
        sequenceItemMerger[RECORD][EXTENSIBLE_RECORD] = recordMerging;
        sequenceItemMerger[RECORD][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][RECORD] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            if (i == FUNCTION) continue;
            if (i == ENUM) continue;
            if (i == RECORD) continue;
            sequenceItemMerger[EXTENSIBLE_RECORD][i] = simpleChoice;
            sequenceItemMerger[i][EXTENSIBLE_RECORD] = simpleChoice;
        }
        sequenceItemMerger[EXTENSIBLE_RECORD][EXTENSIBLE_RECORD] = recordMerging;
        sequenceItemMerger[EXTENSIBLE_RECORD][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][EXTENSIBLE_RECORD] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            if (i == FUNCTION) continue;
            if (i == ENUM) continue;
            if (i == RECORD) continue;
            if (i == EXTENSIBLE_RECORD) continue;
            sequenceItemMerger[BOOLEAN][i] = simpleChoice;
            sequenceItemMerger[i][BOOLEAN] = simpleChoice;
        }
        sequenceItemMerger[BOOLEAN][BOOLEAN] = (_, _) -> typeFactory.itemBoolean();
        sequenceItemMerger[EXTENSIBLE_RECORD][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][EXTENSIBLE_RECORD] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            if (i == FUNCTION) continue;
            if (i == ENUM) continue;
            if (i == RECORD) continue;
            if (i == EXTENSIBLE_RECORD) continue;
            if (i == BOOLEAN) continue;
            sequenceItemMerger[STRING][i] = simpleChoice;
            sequenceItemMerger[i][STRING] = simpleChoice;
        }
        sequenceItemMerger[STRING][STRING] = (_, _) -> typeFactory.itemString();
        sequenceItemMerger[EXTENSIBLE_RECORD][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][EXTENSIBLE_RECORD] = leftMergeChoice;


        sequenceItemMerger[NUMBER][NUMBER] = (_, _) -> typeFactory.itemNumber();
        sequenceItemMerger[NUMBER][CHOICE] = rightMergeChoice;
        sequenceItemMerger[CHOICE][NUMBER] = leftMergeChoice;

        sequenceItemMerger[CHOICE][CHOICE] = choiceMerging;

    }



    private final List<XQuerySequenceType> argumentTypes;
    private final XQuerySequenceType returnedType;
    private final XQuerySequenceType arrayType;
    public XQuerySequenceType getArrayType() {
        return arrayType;
    }

    private final XQueryItemType mapKeyType;

    private final XQuerySequenceType mapValueType;

    private final Set<String> elementNames;


    public XQueryItemType getMapKeyType() {
        return mapKeyType;
    }
    public XQuerySequenceType getMapValueType() {
        return mapValueType;
    }
    public List<XQuerySequenceType> getArgumentTypes() {
        return argumentTypes;
    }

    public XQuerySequenceType getReturnedType() {
        return returnedType;
    }

    public Set<String> getElementNames() {
        return elementNames;
    }

    public XQueryTypes getType() {
        return type;
    }

    private static boolean isNullableEquals(final Object one, final Object other) {
        if (one != null)
            return one.equals(other);
        return one == other;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof XQueryEnumItemType))
            return false;
        final XQueryEnumItemType other = (XQueryEnumItemType) obj;
        if (type != other.getType())
            return false;
        final var otherArgumentTypes = other.getArgumentTypes();
        if (this.argumentTypes == null && otherArgumentTypes != null)
            return false;
        if (this.argumentTypes != null && otherArgumentTypes == null)
            return false;
        if (this.argumentTypes != null) {
            if (this.argumentTypes.size() != otherArgumentTypes.size())
                return false;
            if (IntStream.range(0, this.argumentTypes.size())
                    .allMatch(i -> this.argumentTypes.get(i).equals(otherArgumentTypes.get(i))))
                return false;
        }
        final XQuerySequenceType otherReturnedType = other.getReturnedType();
        return isNullableEquals(this.returnedType, otherReturnedType);
    }

    private static final int typesCount = XQueryTypes.values().length;
    private static final BiPredicate<XQueryEnumItemType, XQueryEnumItemType> alwaysTrue = (_, _) -> true;
    private static final BiPredicate<XQueryEnumItemType, XQueryEnumItemType> alwaysFalse = (_, _) -> false;

    private static final BiPredicate[][] itemtypeIsSubtypeOf;
    static {
        itemtypeIsSubtypeOf = new BiPredicate[typesCount][typesCount];
        for (int i = 0; i < typesCount; i++) {
            for (int j = 0; j < typesCount; j++) {
                itemtypeIsSubtypeOf[i][j] = i == j ? alwaysTrue : alwaysFalse;
            }
        }
        final int anyItem = XQueryTypes.ANY_ITEM.ordinal();
        for (int i = 0; i < typesCount; i++) {
            itemtypeIsSubtypeOf[i][anyItem] = alwaysTrue;
        }

        final int element = XQueryTypes.ELEMENT.ordinal();
        final int anyNode = XQueryTypes.ANY_NODE.ordinal();
        final int anyFunction = XQueryTypes.ANY_FUNCTION.ordinal();
        final int function = XQueryTypes.FUNCTION.ordinal();
        final int array = XQueryTypes.ARRAY.ordinal();
        final int map = XQueryTypes.MAP.ordinal();
        final int record = XQueryTypes.RECORD.ordinal();
        final int extensibleRecord = XQueryTypes.EXTENSIBLE_RECORD.ordinal();
        final int anyMap = XQueryTypes.ANY_MAP.ordinal();
        final int anyArray = XQueryTypes.ANY_ARRAY.ordinal();
        final int enum_ = ENUM;
        final int string = STRING;
        final int choice = XQueryTypes.CHOICE.ordinal();

        final BiPredicate<XQueryItemType, XQueryItemType> choicesubtype = (x, y) -> {
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            final var items = y_.getItemTypes();
            final boolean anyIsSubtype = items.stream().anyMatch(i-> x.itemtypeIsSubtypeOf(i));
            return anyIsSubtype;
        };

        for (int i = 0; i < typesCount; i++) {
            itemtypeIsSubtypeOf[i][choice] = choicesubtype;
        }

        itemtypeIsSubtypeOf[choice][choice] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            final var xItems = x_.getItemTypes();
            final var yItems = y_.getItemTypes();
            final var allPresent = xItems.stream().allMatch(xItem->{
                final boolean anyIsSubtype = yItems.stream().anyMatch(i-> xItem.itemtypeIsSubtypeOf(i));
                return anyIsSubtype;
            });
            return allPresent;
        }
        ;


        itemtypeIsSubtypeOf[anyArray][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[anyArray][map] = (_, y) -> {
            final XQueryEnumItemTypeMap y_ = (XQueryEnumItemTypeMap) y;
            final var mapKeyType = (XQueryEnumItemType) y_.getMapKeyType();
            final boolean isNumber = mapKeyType.getType() == XQueryTypes.NUMBER;
            return isNumber;
        };

        itemtypeIsSubtypeOf[anyArray][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[anyArray][function] = (_, y) -> {
            final XQueryEnumItemTypeFunction y_ = (XQueryEnumItemTypeFunction) y;
            final var argumentTypes = y_.getArgumentTypes();
            if (argumentTypes.size() != 1)
                return false;
            final var onlyArg =  (XQueryEnumSequenceType) argumentTypes.get(0);
            final var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne() || onlyArg.isOneOrMore();
            return correctOccurence
                    && onlyArgItem.getType() == XQueryTypes.NUMBER;
        };

        itemtypeIsSubtypeOf[array][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[array][map] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            if (!itemtypeIsSubtypeOf[anyArray][map].test(x, y))
                return false;
            return x_.getArrayType().isSubtypeOf(y_.getMapValueType());
        };

        itemtypeIsSubtypeOf[element][anyNode] = alwaysTrue;
        itemtypeIsSubtypeOf[element][element] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            return y_.elementNames.containsAll(x_.elementNames);
        };

        itemtypeIsSubtypeOf[function][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[function][function] = (x, y) -> {
            final XQueryEnumItemType i1 = (XQueryEnumItemType) x;
            final XQueryEnumItemType i2 = (XQueryEnumItemType) y;
            return i1.isFunction(i2.getReturnedType(), i2.getArgumentTypes());
        };

        final var canBeKey = booleanEnumArray(XQueryTypes.NUMBER, XQueryTypes.BOOLEAN, XQueryTypes.STRING, XQueryTypes.ENUM);
        itemtypeIsSubtypeOf[function][anyMap] = (x, _) -> {
            final XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            // function must have one argument
            if (x_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQueryEnumSequenceType) x_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && canBeKey[onlyArgItem.getType().ordinal()];
        };

        itemtypeIsSubtypeOf[function][map] = (x, y) -> {
            if (!itemtypeIsSubtypeOf[function][anyMap].test(x, y))
                return false;
            final XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            final XQueryEnumItemTypeMap y_ = (XQueryEnumItemTypeMap) y;
            final var onlyArg =  (XQueryEnumSequenceType) x_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            final boolean argCanBeKey = onlyArgItem.itemtypeIsSubtypeOf(y_.getMapKeyType());
            final boolean returnedCanBeValue = x_.getReturnedType().isSubtypeOf(y_.getMapValueType());
            final boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && argCanBeKey
                    && returnedCanBeValue;
        };

        itemtypeIsSubtypeOf[function][anyArray] = (x, _) -> {
            final XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            // function must have one argument
            if (x_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQueryEnumSequenceType) x_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            // this one argument must be either number or number+
            final boolean correctOccurence = onlyArg.isOne() || onlyArg.isOneOrMore();
            return correctOccurence
                    && onlyArgItem.getType() == XQueryTypes.NUMBER;
        };

        itemtypeIsSubtypeOf[function][array] = (x, y) -> {
            if (!itemtypeIsSubtypeOf[function][anyArray].test(x, y))
                return false;
            final XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            final XQueryEnumItemTypeArray y_ = (XQueryEnumItemTypeArray) y;
            final var returnedType = x_.getReturnedType();

            return returnedType.isSubtypeOf(y_.getArrayType());
        };

        itemtypeIsSubtypeOf[function][function] = (x, y) -> {
            final XQueryEnumItemTypeFunction a = (XQueryEnumItemTypeFunction) x;
            final XQueryEnumItemTypeFunction b = (XQueryEnumItemTypeFunction) y;
            if (a.getArgumentTypes().size() != b.getArgumentTypes().size())
                return false;
            for (int i = 0; i < a.getArgumentTypes().size(); i++) {
                final var aArgType = a.getArgumentTypes().get(i);
                final var bArgType = b.getArgumentTypes().get(i);
                if (!bArgType.isSubtypeOf(aArgType))
                    return false;
            }
            return a.getReturnedType().isSubtypeOf(b.getReturnedType());
        };


        itemtypeIsSubtypeOf[array][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[array][function] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            if (y_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQueryEnumSequenceType) y_.getArgumentTypes().get(0);
            final var onlyArgItem = (XQueryEnumItemType) onlyArg.getItemType();
            if (onlyArgItem.getType() == XQueryTypes.NUMBER) {

            }

            return x_.getArrayType().isSubtypeOf(y_.getReturnedType());
        };

        itemtypeIsSubtypeOf[array][anyArray] = alwaysTrue;
        itemtypeIsSubtypeOf[array][array] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            final boolean isSubtypeOfAnyArray = itemtypeIsSubtypeOf[array][anyArray].test(x, y);
            if (!isSubtypeOfAnyArray)
                return false;
            final XQuerySequenceType xArrayItemType = x_.getArrayType();
            final XQuerySequenceType yArrayItemType = y_.getArrayType();
            return xArrayItemType.isSubtypeOf(yArrayItemType);
        };

        itemtypeIsSubtypeOf[extensibleRecord][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[record][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[anyMap][anyFunction] = alwaysTrue;

        itemtypeIsSubtypeOf[map][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[map][function] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            if (y_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQueryEnumSequenceType) y_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && x_.getMapKeyType().itemtypeIsSubtypeOf(onlyArgItem);
        };

        itemtypeIsSubtypeOf[map][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[map][map] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            return x_.getMapKeyType().itemtypeIsSubtypeOf(y_.getMapKeyType())
                    && x_.getMapValueType().isSubtypeOf(y_.getMapValueType());
        };


        itemtypeIsSubtypeOf[map][anyArray] = (x, _) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            // map must have a key that is a number
            final var key = (XQueryEnumItemType) x_.getMapKeyType();
            return key.getType() == XQueryTypes.NUMBER;
        };


        itemtypeIsSubtypeOf[enum_][string] = alwaysTrue;
        itemtypeIsSubtypeOf[enum_][enum_] = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeEnum) x;
            final var y_ = (XQueryEnumItemTypeEnum) y;
            return y_.getEnumMembers().containsAll(x_.getEnumMembers());
        };

        itemtypeIsSubtypeOf[record][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[extensibleRecord][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[record][map] = XQueryEnumItemType::recordIsSubtypeOfMap;
        // itemtypeIsSubtypeOf[extensibleRecord][map] = XQueryEnumItemType::recordIsSubtypeOfMap;


        itemtypeIsSubtypeOf[record][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[extensibleRecord][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[record][function] = XQueryEnumItemType::recordIsSubtypeOfFunction;
        itemtypeIsSubtypeOf[extensibleRecord][function] = XQueryEnumItemType::recordIsSubtypeOfFunction;

        itemtypeIsSubtypeOf[record][record] = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeRecord) x;
            final var y_ = (XQueryEnumItemTypeRecord) y;
            final boolean allFieldsPresent = y_.getRecordFields().keySet().containsAll(x_.getRecordFields().keySet());
            if (!allFieldsPresent)
                return false;
            for (final var key : x_.getRecordFields().keySet()) {
                final var xFieldType = x_.getRecordFields().get(key);
                final var yFieldType = y_.getRecordFields().get(key);
                if (!xFieldType.type().isSubtypeOf(yFieldType.type()))
                    return false;
            }
            return true;
        };
        itemtypeIsSubtypeOf[extensibleRecord][extensibleRecord] = (x, y) -> {
            // All of the following are true:
            // A is an extensible record type
            // B is an extensible record type
            final var x_ = (XQueryEnumItemTypeRecord) x;
            final var y_ = (XQueryEnumItemTypeRecord) y;
            // Every mandatory field in B is also declared as mandatory in A.
            if (!areAllMandatoryFieldsPresent(x_, y_)) {
                return false;
            }
            // For every field that is declared in both A and B, where the declared type in A is T
            // and the declared type in B is U, T ⊑ U .
            if  (!isEveryDeclaredFieldSubtype(x_, y_)) {
                return false;
            }
            // For every field that is declared in B but not in A, the declared type in B is item()*.
            return true;
        };

        itemtypeIsSubtypeOf[record][extensibleRecord] = (x, y) -> {
            // All of the following are true:
            // A is a non-extensible record type.
            // B is an extensible record type.
            final var x_ = (XQueryEnumItemTypeRecord) x;
            final var y_ = (XQueryEnumItemTypeRecord) y;
            // Every mandatory field in B is also declared as mandatory in A.
            if (!areAllMandatoryFieldsPresent(x_, y_)) {
                return false;
            }
            // For every field that is declared in both A and B, where the declared type in A is T
            // and the declared type in B is U, T ⊑ U .
            return isEveryDeclaredFieldSubtype(x_, y_);
        };


    }

    private static boolean isEveryDeclaredFieldSubtype(final XQueryEnumItemTypeRecord x_, final XQueryEnumItemTypeRecord y_) {
        final Map<String, XQueryRecordField> recordFieldsX = x_.getRecordFields();
        final var commonFields = new HashSet<String>(recordFieldsX.keySet());
        final Map<String, XQueryRecordField> recordFieldsY = y_.getRecordFields();
        commonFields.retainAll(recordFieldsY.keySet());
        for (final String commonField : commonFields) {
            final var xFieldType = recordFieldsX.get(commonField);
            final var yFieldType = recordFieldsY.get(commonField);
            if (!xFieldType.type().isSubtypeOf(yFieldType.type()))
                return false;
        }
        return true;
    }

    private static boolean areAllMandatoryFieldsPresent(final XQueryEnumItemTypeRecord x_, final XQueryEnumItemTypeRecord y_)
    {
        final var mandatoryFieldsX = new HashSet<String>();
        getMandatoryFields(x_, mandatoryFieldsX);
        final var mandatoryFieldsY = new HashSet<String>();
        getMandatoryFields(y_, mandatoryFieldsY);
        final boolean allMandatoryFieldsPresent = mandatoryFieldsX.containsAll(mandatoryFieldsY);
        return allMandatoryFieldsPresent;
    }

    private static void getMandatoryFields(final XQueryEnumItemTypeRecord x_, final HashSet<String> mandatoryFieldsX) {
        for (var field : x_.getRecordFields().keySet()) {
            var recordInfo = x_.getRecordFields().get(field);
            if (recordInfo.isRequired()) {
                mandatoryFieldsX.add(field);
            }
        }
    }

    private static boolean recordIsSubtypeOfFunction(Object x, Object y) {
        final var x_ = (XQueryEnumItemTypeRecord) x;
        final var y_ = (XQueryEnumItemTypeFunction) y;
        final var yArgumentTypes = y_.getArgumentTypes();
        if (yArgumentTypes.size() != 1)
            return false;
        final var yFieldType = (XQueryEnumSequenceType) yArgumentTypes.get(0);
        final var yFieldItemType = (XQueryEnumItemType) yFieldType.getItemType();
        if (yFieldItemType.getType() != XQueryTypes.STRING
            && yFieldItemType.getType() != XQueryTypes.ANY_ITEM)
            return false;
        final var yReturnedType = y_.getReturnedType();
        for (final var key : x_.getRecordFields().keySet()) {
            final var xFieldType = x_.getRecordFields().get(key);
            if (!xFieldType.type().isSubtypeOf(yReturnedType))
                return false;
        }
        return true;
    }

    private static boolean recordIsSubtypeOfMap(Object x, Object y) {
        final var x_ = (XQueryEnumItemTypeRecord) x;
        final var y_ = (XQueryEnumItemTypeMap) y;
        final var keyItemType = (XQueryEnumItemType) y_.getMapKeyType();
        if (keyItemType.getType() != XQueryTypes.STRING
            && keyItemType.getType() != XQueryTypes.ANY_ITEM)
            return false;
        final var yFieldType = y_.getMapValueType();
        for (final var key : x_.getRecordFields().keySet()) {
            final XQueryRecordField xFieldType = x_.getRecordFields().get(key);
            if (!xFieldType.type().isSubtypeOf(yFieldType))
                return false;
        }
        return true;
    }

    @Override
    public boolean itemtypeIsSubtypeOf(final XQueryItemType obj) {
        if (!(obj instanceof XQueryEnumItemType))
            return false;
        final var typed = (XQueryEnumItemType) obj;
        return itemtypeIsSubtypeOf[typeOrdinal][typed.getType().ordinal()].test(this, obj);
    }


    private static boolean[] booleanEnumArray(final XQueryTypes... values) {
        final var array = new boolean[XQueryTypes.values().length];
        for (final var v : values) {
            array[v.ordinal()] = true;
        }
        return array;
    }

    private static final boolean[] isFunction = booleanEnumArray(XQueryTypes.ANY_FUNCTION, XQueryTypes.FUNCTION);

    @Override
    public boolean isFunction(final XQuerySequenceType otherReturnedType, final List<XQuerySequenceType> otherArgumentTypes) {
        return isFunction[typeOrdinal]
                && this.returnedType.equals(otherReturnedType)
                && this.argumentTypes.size() == otherArgumentTypes.size()
                && IntStream.range(0, this.argumentTypes.size())
                        .allMatch(i -> this.argumentTypes.get(i).equals(otherArgumentTypes.get(i)));
    }

    private static final boolean[] noEffectiveBooleanValue = booleanEnumArray(XQueryTypes.FUNCTION,
                                                                                XQueryTypes.ANY_ARRAY,
                                                                                XQueryTypes.MAP,
                                                                                XQueryTypes.ANY_MAP,
                                                                                XQueryTypes.ARRAY,
                                                                                XQueryTypes.ANY_ARRAY);
    @Override
    public boolean hasEffectiveBooleanValue() {
        if (type == XQueryTypes.CHOICE) {
            return itemTypes.stream().allMatch(itemType->itemType.hasEffectiveBooleanValue());
        }
        return !noEffectiveBooleanValue[typeOrdinal];
    }

    private static final boolean[][] castableAs;
    static {
        castableAs = new boolean[typesCount][typesCount];
        for (int i = 0; i < typesCount; i++) {
            for (int j = 0; j < typesCount; j++) {
                castableAs[i][j] = i == j;
            }
        }
        // final int number = XQueryTypes.NUMBER.ordinal();
        final int string = STRING;
        final int anyItem = XQueryTypes.ANY_ITEM.ordinal();
        for (int i = 0; i < typesCount; i++) {
            castableAs[i][anyItem] = true;
            castableAs[i][string] = true;
        }
    }

    @Override
    public boolean castableAs(final XQueryItemType itemType) {
        if (!(itemType instanceof XQueryEnumItemType))
            return false;
        final var typed = (XQueryEnumItemType) itemType;
        return castableAs[typeOrdinal][typed.getType().ordinal()];
    }

    @Override
    public XQueryItemType unionMerge(final XQueryItemType other) {
        final var other_ = (XQueryEnumItemType) other;
        return (XQueryItemType)unionItemMerger[typeOrdinal][other_.getType().ordinal()].apply(this, other);
    }

    @Override
    public XQueryItemType intersectionMerge(final XQueryItemType other) {
        final var other_ = (XQueryEnumItemType) other;
        return (XQueryItemType)intersectionItemMerger[typeOrdinal][other_.getType().ordinal()].apply(this, other);
    }

    @Override
    public XQueryItemType exceptionMerge(final XQueryItemType other) {
        return this;
    }

    private static final boolean[][] isValueComparableWith;
    static {
        isValueComparableWith = new boolean[typesCount][typesCount];
        isValueComparableWith[STRING][STRING] = true;
        isValueComparableWith[NUMBER][NUMBER] = true;
        isValueComparableWith[BOOLEAN][BOOLEAN] = true;
        isValueComparableWith[ENUM][STRING] = true;
        isValueComparableWith[STRING][ENUM] = true;
        isValueComparableWith[ENUM][ENUM] = true;
    }


    @Override
    public boolean isValueComparableWith(final XQueryItemType other) {
        final var other_ = (XQueryEnumItemType) other;
        return isValueComparableWith[typeOrdinal][other_.getType().ordinal()];
    }

    @Override
    public XQueryItemType sequenceMerge(XQueryItemType other) {
        var otherType = ((XQueryEnumItemType)other).getType().ordinal();
        return (XQueryItemType) sequenceItemMerger[typeOrdinal][otherType].apply(this, other);
    }
}
