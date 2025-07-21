
package com.github.akruk.antlrxquery.typesystem.typeoperations;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class ItemtypeIntersectionMerger
{
    private static final int ELEMENT = XQueryTypes.ELEMENT.ordinal();
    private static final int ANY_NODE = XQueryTypes.ANY_NODE.ordinal();
    private static final int ERROR = XQueryTypes.ERROR.ordinal();
    private static final int typesCount = XQueryTypes.values().length;

    private final BinaryOperator<XQueryItemType>[] intersectionItemMerger;
    private final XQueryTypeFactory typeFactory;

    @SuppressWarnings("unchecked")
    public ItemtypeIntersectionMerger(final int typeOrdinal, final XQueryTypeFactory typeFactory)
    {
        this.typeFactory = typeFactory;
        this.intersectionItemMerger = new BinaryOperator[typesCount];
        intersectionItemMerger[ELEMENT] = this::mergeElements;
        intersectionItemMerger[ANY_NODE] = (i1, _) -> i1;
        intersectionItemMerger[ELEMENT] = (_, i2) -> i2;
        intersectionItemMerger[ANY_NODE] = (_, _) -> typeFactory.itemAnyNode();
        final BinaryOperator<XQueryItemType> error = (_, _) -> typeFactory.itemError();
        final BinaryOperator<XQueryItemType> anyNode = (_, _) -> typeFactory.itemAnyNode();

        switch (XQueryTypes.values()[typeOrdinal]) {
            case ANY_NODE:
                intersectionItemMerger[ERROR] = error;
                intersectionItemMerger[ANY_NODE] = anyNode;
                intersectionItemMerger[ELEMENT] = (_, i2) -> i2;
                break;

            case ELEMENT:
                intersectionItemMerger[ERROR] = error;
                intersectionItemMerger[ANY_NODE] = (i1, _) -> i1;
                intersectionItemMerger[ELEMENT] = this::mergeElements;
                break;
            default:
                ;
        }
    }

    private XQueryItemType mergeElements(XQueryItemType x, XQueryItemType y)
    {
            final var i1_ = (XQueryItemType) x;
            final var i2_ = (XQueryItemType) y;
            final var i1Elements = i1_.getElementNames();
            final var i2ELements = i2_.getElementNames();
            final Set<String> mergedElements = new HashSet<>(i1Elements.size());
            mergedElements.addAll(i1Elements);
            mergedElements.retainAll(i2ELements);
            return typeFactory.itemElement(mergedElements);
    }

    public XQueryItemType intersectionMerge(final XQueryItemType type1, final XQueryItemType type2)
    {
        final int otherOrdinal = ((XQueryItemType) type2).getType().ordinal();
        return this.intersectionItemMerger[otherOrdinal].apply(type1, type2);
    }



}
