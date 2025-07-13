
package com.github.akruk.antlrxquery.typesystem.typeoperations.defaults;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeElement;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class EnumItemtypeUnionMerger implements IItemtypeUnionMerger
{
    private static final int ELEMENT = XQueryTypes.ELEMENT.ordinal();
    private static final int ANY_NODE = XQueryTypes.ANY_NODE.ordinal();

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
    private static final int typesCount = XQueryTypes.values().length;

    private final BinaryOperator<XQueryItemType>[] unionItemMerger;
    private final XQueryTypeFactory typeFactory;

    @SuppressWarnings("unchecked")
    public EnumItemtypeUnionMerger(final int typeOrdinal, final XQueryTypeFactory typeFactory)
    {
        this.typeFactory = typeFactory;
        this.unionItemMerger = new BinaryOperator[typesCount];
        final BinaryOperator<XQueryItemType> anyNodeReturn = (_, _) -> typeFactory.itemAnyNode();
        final BinaryOperator<XQueryItemType> error = (_, _) -> typeFactory.itemError();
        final BinaryOperator<XQueryItemType> anyItem = (_, _) -> typeFactory.itemAnyItem();
        final BinaryOperator<XQueryItemType> anyNode = (_, _) -> typeFactory.itemAnyNode();
        final BinaryOperator<XQueryItemType> anyMap = (_, _) -> typeFactory.itemAnyMap();
        final BinaryOperator<XQueryItemType> anyArray = (_, _) -> typeFactory.itemAnyArray();
        final BinaryOperator<XQueryItemType> itemString = (_, _) -> typeFactory.itemString();
        final BinaryOperator<XQueryItemType> simpleChoice = (x, y) -> typeFactory.itemChoice(Set.of(x, y));

        unionItemMerger[ELEMENT] = this::mergeElements;
        unionItemMerger[ANY_NODE] = anyNodeReturn;
        unionItemMerger[ELEMENT] = anyNodeReturn;
        unionItemMerger[ANY_NODE] = anyNodeReturn;

        switch (XQueryTypes.values()[typeOrdinal]) {
            case ANY_NODE:
                unionItemMerger[ERROR] = error;
                unionItemMerger[ANY_NODE] = anyNode;
                unionItemMerger[ELEMENT] = anyNode;
                break;

            case ELEMENT:
                unionItemMerger[ERROR] = error;
                unionItemMerger[ANY_NODE] = anyNode;
                unionItemMerger[ELEMENT] = this::mergeElements;
                break;
            default:
                ;
        }


    }

    private XQueryItemType mergeElements(XQueryItemType x, XQueryItemType y)
    {
        final var els1 = ((XQueryEnumItemTypeElement) x).getElementNames();
        final var els2 = ((XQueryEnumItemTypeElement) y).getElementNames();
        final Set<String> merged = new HashSet<>(els1.size() + els2.size());
        merged.addAll(els1);
        merged.addAll(els2);
        return typeFactory.itemElement(merged);
    }

    @Override
    public XQueryItemType unionMerge(final XQueryItemType type1, final XQueryItemType type2)
    {
        final int otherOrdinal = ((XQueryEnumItemType) type2).getType().ordinal();
        return this.unionItemMerger[otherOrdinal].apply(type1, type2);
    }



}