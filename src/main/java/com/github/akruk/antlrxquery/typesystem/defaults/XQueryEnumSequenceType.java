package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.function.BiPredicate;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQueryEnumSequenceType implements XQuerySequenceType {
    private final XQueryEnumItemType itemType;
    private final XQueryOccurence occurence;

    public XQueryEnumItemType getItemType() {
        return itemType;
    }

    public XQueryEnumSequenceType(XQueryEnumItemType itemType, XQueryOccurence occurence) {
        this.itemType = itemType;
        this.occurence = occurence;
    }

    private static boolean isNullableEquals(Object one, Object other) {
        if (one != null)
            return one.equals(other);
        return one == other;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof XQueryEnumSequenceType))
            return false;
        XQueryEnumSequenceType other = (XQueryEnumSequenceType) obj;
        if (!isNullableEquals(this.itemType, other.getItemType()))
            return false;
        if (occurence != other.getOccurence())
            return false;
        return true;
    }

    private static final BiPredicate<XQueryEnumSequenceType, XQueryEnumSequenceType> alwaysTrue = (t1, t2) -> true;
    private static final BiPredicate<XQueryEnumSequenceType, XQueryEnumSequenceType> alwaysFalse = (t1, t2) -> false;
    private static final int occurenceCount = XQueryOccurence.values().length;
    private static final BiPredicate[][] isSubtypeOf;
    static {
        isSubtypeOf = new BiPredicate[occurenceCount][occurenceCount];
        for (int i = 0; i < occurenceCount; i++) {
            for (int j = 0; j < occurenceCount; j++) {
                isSubtypeOf[i][j] = alwaysFalse;
            }
        }
        isSubtypeOf[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ZERO.ordinal()] = alwaysTrue;
        isSubtypeOf[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ZERO_OR_ONE.ordinal()] = alwaysTrue;
        isSubtypeOf[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ZERO_OR_MORE.ordinal()] = alwaysTrue;

        isSubtypeOf[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = XQueryEnumSequenceType::isSubtypeItemtype;
        ;

        isSubtypeOf[XQueryOccurence.ZERO_OR_MORE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = XQueryEnumSequenceType::isSubtypeItemtype;

        isSubtypeOf[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ONE
                .ordinal()] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = XQueryEnumSequenceType::isSubtypeItemtype;

        isSubtypeOf[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = XQueryEnumSequenceType::isSubtypeItemtype;
    }

    private static boolean isSubtypeItemtype(Object x, Object y) {
        XQueryEnumSequenceType this_ = (XQueryEnumSequenceType) x;
        XQueryEnumSequenceType other = (XQueryEnumSequenceType) y;
        return this_.getItemType().itemtypeIsSubtypeOf(other.getItemType());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isSubtypeOf(XQuerySequenceType obj) {
        if (!(obj instanceof XQueryEnumSequenceType))
            return false;
        XQueryEnumSequenceType other = (XQueryEnumSequenceType) obj;
        XQueryOccurence otherOccurence = other.getOccurence();
        BiPredicate<XQueryEnumSequenceType, XQueryEnumSequenceType> predicate =
            isSubtypeOf[this.occurence.ordinal()][otherOccurence.ordinal()];
        return predicate.test(this, other);
    }
    public XQueryOccurence getOccurence() {
        return occurence;
    }

    @Override
    public boolean isOne() {
        return occurence == XQueryOccurence.ONE;
    }

    @Override
    public boolean isOneOrMore() {
        return occurence == XQueryOccurence.ONE_OR_MORE;
    }

    @Override
    public boolean isZeroOrMore() {
        return occurence == XQueryOccurence.ZERO_OR_MORE;
    }

    @Override
    public boolean isZeroOrOne() {
        return occurence == XQueryOccurence.ZERO_OR_ONE;
    }

    @Override
    public boolean isZero() {
        return occurence == XQueryOccurence.ZERO;
    }

}
