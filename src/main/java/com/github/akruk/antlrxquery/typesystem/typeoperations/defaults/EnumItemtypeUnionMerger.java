
package com.github.akruk.antlrxquery.typesystem.typeoperations.defaults;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeElement;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class EnumItemtypeUnionMerger
{
    private static final int ELEMENT = XQueryTypes.ELEMENT.ordinal();
    private static final int ANY_NODE = XQueryTypes.ANY_NODE.ordinal();
    private static final int ERROR = XQueryTypes.ERROR.ordinal();
    private static final int typesCount = XQueryTypes.values().length;

    private final BinaryOperator<XQueryItemType>[] unionItemMerger;
    private final XQueryTypeFactory typeFactory;

    @SuppressWarnings("unchecked")
    public EnumItemtypeUnionMerger(final int typeOrdinal, final XQueryTypeFactory typeFactory)
    {
        this.typeFactory = typeFactory;
        this.unionItemMerger = new BinaryOperator[typesCount];
        final BinaryOperator<XQueryItemType> error = (_, _) -> typeFactory.itemError();
        final BinaryOperator<XQueryItemType> anyNode = (_, _) -> typeFactory.itemAnyNode();

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

    public XQueryItemType unionMerge(final XQueryItemType type1, final XQueryItemType type2)
    {
        final int otherOrdinal = ((XQueryEnumItemType) type2).getType().ordinal();
        return this.unionItemMerger[otherOrdinal].apply(type1, type2);
    }



}
