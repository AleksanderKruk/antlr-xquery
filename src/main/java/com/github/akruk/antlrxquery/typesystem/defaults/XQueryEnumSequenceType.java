package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumSequenceType implements XQuerySequenceType {
    private final XQueryEnumItemType itemType;
    private final XQueryOccurence occurence;
    private final XQueryTypeFactory typeFactory;

    public XQueryEnumItemType getItemType() {
        return itemType;
    }

    public XQueryEnumSequenceType(XQueryTypeFactory typeFactory, XQueryEnumItemType itemType, XQueryOccurence occurence) {
        this.typeFactory = typeFactory;
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


    private static final Function[][] mergedOccurences = new Function[XQueryOccurence.values().length][XQueryOccurence.values().length];
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zero =
        typeFactory -> (item) -> typeFactory.emptySequence();
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> one =
        typeFactory -> (item) -> typeFactory.one(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zeroOrOne
        = typeFactory -> (item) -> typeFactory.zeroOrOne(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zeroOrMore
        = typeFactory -> (item) -> typeFactory.zeroOrMore(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> oneOrMore
        = typeFactory -> (item) -> typeFactory.oneOrMore(item);

    static {
        mergedOccurences[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ZERO.ordinal()] = zero;
        mergedOccurences[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ONE.ordinal()] = one;
        mergedOccurences[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = zeroOrOne;
        mergedOccurences[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = oneOrMore;

        mergedOccurences[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ZERO.ordinal()] = one;
        mergedOccurences[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ONE.ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = oneOrMore;

        mergedOccurences[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ZERO
                .ordinal()] = zeroOrOne;
        mergedOccurences[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ONE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = oneOrMore;

        mergedOccurences[XQueryOccurence.ZERO_OR_MORE.ordinal()][XQueryOccurence.ZERO
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_MORE.ordinal()][XQueryOccurence.ONE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_MORE.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_MORE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_MORE.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = oneOrMore;

        mergedOccurences[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ZERO
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ONE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = oneOrMore;
    }


    @Override
    public XQuerySequenceType sequenceMerge(XQuerySequenceType other) {
        var enumType1 = this;
        var enumType2 = (XQueryEnumSequenceType) other;
        var enumItemType1 = this.getItemType();
        var enumItemType2 = other.getItemType();
        var sequenceGetterWithoutFactory = mergedOccurences[enumType1.getOccurence().ordinal()][enumType2.getOccurence().ordinal()];
        Function<XQueryItemType, XQuerySequenceType> sequenceGetter = (Function) sequenceGetterWithoutFactory.apply(typeFactory);
        boolean equalItemTypes = enumItemType1.equals(enumItemType2);
        return sequenceGetter.apply(equalItemTypes ? enumItemType1 : enumItemType2);

    }

    @Override
    public boolean itemtypeIsSubtypeOf(XQuerySequenceType obj) {
        return itemType.itemtypeIsSubtypeOf(itemType);
    }

    @Override
    public boolean hasEffectiveBooleanValue() {
        if (occurence == XQueryOccurence.ONE)
            return itemType.hasEffectiveBooleanValue();
        return true;
    }

}
