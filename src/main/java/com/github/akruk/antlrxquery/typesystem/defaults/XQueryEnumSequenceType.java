package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.function.BiPredicate;
import java.util.function.Function;

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
        this.factoryByOccurence = new Function[XQueryOccurence.values().length];
        this.factoryByOccurence[XQueryOccurence.ZERO.ordinal()] = _ -> typeFactory.emptySequence();
        this.factoryByOccurence[XQueryOccurence.ONE.ordinal()] = i -> typeFactory.one((XQueryItemType)i);
        this.factoryByOccurence[XQueryOccurence.ZERO_OR_ONE.ordinal()] = i -> typeFactory.zeroOrOne((XQueryItemType)i);
        this.factoryByOccurence[XQueryOccurence.ZERO_OR_MORE.ordinal()] = i -> typeFactory.zeroOrMore((XQueryItemType)i);
        this.factoryByOccurence[XQueryOccurence.ONE_OR_MORE.ordinal()] = i -> typeFactory.oneOrMore((XQueryItemType)i);
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

    private static final BiPredicate<XQueryEnumSequenceType, XQueryEnumSequenceType> alwaysTrue = (_, _) -> true;
    private static final BiPredicate<XQueryEnumSequenceType, XQueryEnumSequenceType> alwaysFalse = (_, _) -> false;
    private static final int occurenceCount = XQueryOccurence.values().length;
    @SuppressWarnings("rawtypes")
	private static final BiPredicate[][] isSubtypeOf;
    static {
        isSubtypeOf = new BiPredicate[occurenceCount][occurenceCount];
        for (int i = 0; i < occurenceCount; i++) {
            for (int j = 0; j < occurenceCount; j++) {
                isSubtypeOf[i][j] = alwaysFalse;
            }
        }
        final int zero = XQueryOccurence.ZERO.ordinal();
        final int one = XQueryOccurence.ONE.ordinal();
        final int zeroOrOne = XQueryOccurence.ZERO_OR_ONE.ordinal();
        final int zeroOrMore = XQueryOccurence.ZERO_OR_MORE.ordinal();
        final int oneOrMore = XQueryOccurence.ONE_OR_MORE.ordinal();
        isSubtypeOf[zero][zero] = alwaysTrue;
        isSubtypeOf[zero][zeroOrOne] = alwaysTrue;
        isSubtypeOf[zero][zeroOrMore] = alwaysTrue;

        isSubtypeOf[zeroOrOne][zeroOrOne] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[zeroOrOne][zeroOrMore] = XQueryEnumSequenceType::isSubtypeItemtype;
        ;

        isSubtypeOf[zeroOrMore][zeroOrMore] = XQueryEnumSequenceType::isSubtypeItemtype;

        isSubtypeOf[one][one] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[one][oneOrMore] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[one][zeroOrMore] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[one][zeroOrOne] = XQueryEnumSequenceType::isSubtypeItemtype;

        isSubtypeOf[oneOrMore][zeroOrMore] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[oneOrMore][oneOrMore] = XQueryEnumSequenceType::isSubtypeItemtype;
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


    @SuppressWarnings("rawtypes")
	private static final Function[][] mergedOccurences = new Function[XQueryOccurence.values().length][XQueryOccurence.values().length];
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zero =
        typeFactory -> (_) -> typeFactory.emptySequence();
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> one =
        typeFactory -> (item) -> typeFactory.one(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zeroOrOne
        = typeFactory -> (item) -> typeFactory.zeroOrOne(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zeroOrMore
        = typeFactory -> (item) -> typeFactory.zeroOrMore(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> oneOrMore
        = typeFactory -> (item) -> typeFactory.oneOrMore(item);

    static {
        final int zeroOrdinal = XQueryOccurence.ZERO.ordinal();
        final int oneOrdinal = XQueryOccurence.ONE.ordinal();
        final int zeroOrOneOrdinal = XQueryOccurence.ZERO_OR_ONE.ordinal();
        final int zeroOrMoreOrdinal = XQueryOccurence.ZERO_OR_MORE.ordinal();
        final int oneOrMoreOrdinal = XQueryOccurence.ONE_OR_MORE.ordinal();
        mergedOccurences[zeroOrdinal][zeroOrdinal] = zero;
        mergedOccurences[zeroOrdinal][oneOrdinal] = one;
        mergedOccurences[zeroOrdinal][zeroOrOneOrdinal] = zeroOrOne;
        mergedOccurences[zeroOrdinal][zeroOrMoreOrdinal] = zeroOrMore;
        mergedOccurences[zeroOrdinal][oneOrMoreOrdinal] = oneOrMore;

        mergedOccurences[oneOrdinal][zeroOrdinal] = one;
        mergedOccurences[oneOrdinal][oneOrdinal] = oneOrMore;
        mergedOccurences[oneOrdinal][zeroOrOneOrdinal] = oneOrMore;
        mergedOccurences[oneOrdinal][zeroOrMoreOrdinal] = oneOrMore;
        mergedOccurences[oneOrdinal][oneOrMoreOrdinal] = oneOrMore;

        mergedOccurences[zeroOrOneOrdinal][zeroOrdinal] = zeroOrOne;
        mergedOccurences[zeroOrOneOrdinal][oneOrdinal] = oneOrMore;
        mergedOccurences[zeroOrOneOrdinal][zeroOrOneOrdinal] = zeroOrMore;
        mergedOccurences[zeroOrOneOrdinal][zeroOrMoreOrdinal] = zeroOrMore;
        mergedOccurences[zeroOrOneOrdinal][oneOrMoreOrdinal] = oneOrMore;

        mergedOccurences[zeroOrMoreOrdinal][zeroOrdinal] = zeroOrMore;
        mergedOccurences[zeroOrMoreOrdinal][oneOrdinal] = oneOrMore;
        mergedOccurences[zeroOrMoreOrdinal][zeroOrOneOrdinal] = zeroOrMore;
        mergedOccurences[zeroOrMoreOrdinal][zeroOrMoreOrdinal] = zeroOrMore;
        mergedOccurences[zeroOrMoreOrdinal][oneOrMoreOrdinal] = oneOrMore;

        mergedOccurences[oneOrMoreOrdinal][zeroOrdinal] = oneOrMore;
        mergedOccurences[oneOrMoreOrdinal][oneOrdinal] = oneOrMore;
        mergedOccurences[oneOrMoreOrdinal][zeroOrOneOrdinal] = oneOrMore;
        mergedOccurences[oneOrMoreOrdinal][zeroOrMoreOrdinal] = oneOrMore;
        mergedOccurences[oneOrMoreOrdinal][oneOrMoreOrdinal] = oneOrMore;
    }


    @Override
    public XQuerySequenceType sequenceMerge(XQuerySequenceType other) {
        final var enumType1 = this;
        final var enumType2 = (XQueryEnumSequenceType) other;
        final var enumItemType1 = this.getItemType();
        final var enumItemType2 = other.getItemType();
        final var sequenceGetterWithoutFactory = mergedOccurences[enumType1.getOccurence().ordinal()][enumType2.getOccurence().ordinal()];
        @SuppressWarnings({ "rawtypes", "unchecked" })
		Function<XQueryItemType, XQuerySequenceType> sequenceGetter = (Function) sequenceGetterWithoutFactory.apply(typeFactory);
        if (enumItemType1 == null && enumItemType2 == null) {
            return sequenceGetter.apply(typeFactory.itemAnyItem());
        }
        if (enumItemType1 == null) {
            return sequenceGetter.apply(enumItemType2);
        }
        if (enumItemType2 == null) {
            return sequenceGetter.apply(enumItemType1);
        }
        boolean itemType1MoreGeneral = enumItemType2.itemtypeIsSubtypeOf(enumItemType1);
        if (itemType1MoreGeneral) {
            return sequenceGetter.apply(enumItemType1);
        }
        boolean itemType2MoreGeneral = enumItemType1.itemtypeIsSubtypeOf(enumItemType2);
        if (itemType2MoreGeneral) {
            return sequenceGetter.apply(enumItemType2);
        }
        return sequenceGetter.apply(typeFactory.itemAnyItem());

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

    @Override
    public XQuerySequenceType unionMerge(XQuerySequenceType other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unionMerge'");
    }

    @Override
    public XQuerySequenceType intersectionMerge(XQuerySequenceType other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'intersectionMerge'");
    }

    @Override
    public XQuerySequenceType exceptionMerge(XQuerySequenceType other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exceptionMerge'");
    }


    private static XQueryOccurence[][] typeAlternativeOccurence = new XQueryOccurence[XQueryOccurence.values().length][XQueryOccurence.values().length];
    static {
        final int zeroOrdinal = XQueryOccurence.ZERO.ordinal();
        final int oneOrdinal = XQueryOccurence.ONE.ordinal();
        final int zeroOrOneOrdinal = XQueryOccurence.ZERO_OR_ONE.ordinal();
        final int zeroOrMoreOrdinal = XQueryOccurence.ZERO_OR_MORE.ordinal();
        final int oneOrMoreOrdinal = XQueryOccurence.ONE_OR_MORE.ordinal();
        typeAlternativeOccurence[zeroOrdinal][zeroOrdinal] = XQueryOccurence.ZERO;
        typeAlternativeOccurence[zeroOrdinal][oneOrdinal] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[zeroOrdinal][zeroOrOneOrdinal] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[zeroOrdinal][zeroOrMoreOrdinal] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[zeroOrdinal][oneOrMoreOrdinal] = XQueryOccurence.ZERO_OR_MORE;

        typeAlternativeOccurence[oneOrdinal][zeroOrdinal] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[oneOrdinal][oneOrdinal] = XQueryOccurence.ONE;
        typeAlternativeOccurence[oneOrdinal][zeroOrOneOrdinal] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[oneOrdinal][zeroOrMoreOrdinal] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[oneOrdinal][oneOrMoreOrdinal] = XQueryOccurence.ONE_OR_MORE;

        typeAlternativeOccurence[zeroOrOneOrdinal][zeroOrdinal] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[zeroOrOneOrdinal][oneOrdinal] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[zeroOrOneOrdinal][zeroOrOneOrdinal] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[zeroOrOneOrdinal][zeroOrMoreOrdinal] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[zeroOrOneOrdinal][oneOrMoreOrdinal] = XQueryOccurence.ZERO_OR_MORE;

        typeAlternativeOccurence[zeroOrMoreOrdinal][zeroOrdinal] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[zeroOrMoreOrdinal][oneOrdinal] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[zeroOrMoreOrdinal][zeroOrOneOrdinal] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[zeroOrMoreOrdinal][zeroOrMoreOrdinal] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[zeroOrMoreOrdinal][oneOrMoreOrdinal] = XQueryOccurence.ZERO_OR_MORE;

        typeAlternativeOccurence[oneOrMoreOrdinal][zeroOrdinal] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[oneOrMoreOrdinal][oneOrdinal] = XQueryOccurence.ONE_OR_MORE;
        typeAlternativeOccurence[oneOrMoreOrdinal][zeroOrOneOrdinal] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[oneOrMoreOrdinal][zeroOrMoreOrdinal] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[oneOrMoreOrdinal][oneOrMoreOrdinal] = XQueryOccurence.ONE_OR_MORE;
    }

    @SuppressWarnings("rawtypes")
	final Function[] factoryByOccurence;
    @SuppressWarnings("unchecked")
    @Override
    public XQuerySequenceType typeAlternative(XQuerySequenceType other) {
        final var other_ = (XQueryEnumSequenceType) other;
        final var occurence_ = typeAlternativeOccurence[occurence.ordinal()][other_.getOccurence().ordinal()];
        @SuppressWarnings("rawtypes")
		final Function sequenceTypeFactory = factoryByOccurence[occurence_.ordinal()];
        if (this.itemType == null)
            if (other.getItemType() == null)
                return (XQuerySequenceType)sequenceTypeFactory.apply(typeFactory.itemAnyItem());
            else
                return (XQuerySequenceType)sequenceTypeFactory.apply(other.getItemType());
        else
            if (other.getItemType() == null)
                return (XQuerySequenceType)sequenceTypeFactory.apply(this.itemType);
            else
                if (this.itemType.equals(other.getItemType()))
                    return (XQuerySequenceType)sequenceTypeFactory.apply(this.itemType);
                else
                    return (XQuerySequenceType)sequenceTypeFactory.apply(typeFactory.itemAnyItem());

    }

    @Override
    public boolean castableAs(XQuerySequenceType other) {
        if (!(other instanceof XQueryEnumSequenceType))
            return false;
        XQueryEnumSequenceType otherEnum = (XQueryEnumSequenceType) other;
        if (!this.isOne() || !other.isOne()) {
            return false;
        }
        return this.getItemType().castableAs(otherEnum.getItemType());
    }

	@Override
	public XQuerySequenceType addOptionality() {
        return typeAlternative(typeFactory.emptySequence());
	}

}
