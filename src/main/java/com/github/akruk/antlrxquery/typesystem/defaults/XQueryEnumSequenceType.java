package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.function.BiPredicate;
import java.util.function.Function;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class XQueryEnumSequenceType implements XQuerySequenceType {
    private static final int ONE_OR_MORE = XQueryOccurence.ONE_OR_MORE.ordinal();
    private static final int ZERO_OR_MORE = XQueryOccurence.ZERO_OR_MORE.ordinal();
    private static final int ZERO_OR_ONE = XQueryOccurence.ZERO_OR_ONE.ordinal();
    private static final int ONE = XQueryOccurence.ONE.ordinal();
    private static final int ZERO = XQueryOccurence.ZERO.ordinal();
    private final XQueryEnumItemType itemType;
    private final XQueryOccurence occurence;
    private final int occurence_;
    private final XQueryTypeFactory typeFactory;
    private final String occurenceSuffix;
    private final Function<XQuerySequenceType, XQuerySequenceType> lookup;

    public XQueryEnumItemType getItemType() {
        return itemType;
    }

    public XQueryEnumSequenceType(final XQueryTypeFactory typeFactory, final XQueryEnumItemType itemType, final XQueryOccurence occurence) {
        this.typeFactory = typeFactory;
        this.itemType = itemType;
        this.occurence = occurence;
        this.occurence_ = occurence.ordinal();
        this.factoryByOccurence = new Function[XQueryOccurence.values().length];
        this.factoryByOccurence[ZERO] = _ -> typeFactory.emptySequence();
        this.factoryByOccurence[ONE] = i -> typeFactory.one((XQueryItemType)i);
        this.factoryByOccurence[ZERO_OR_ONE] = i -> typeFactory.zeroOrOne((XQueryItemType)i);
        this.factoryByOccurence[ZERO_OR_MORE] = i -> typeFactory.zeroOrMore((XQueryItemType)i);
        this.factoryByOccurence[ONE_OR_MORE] = i -> typeFactory.oneOrMore((XQueryItemType)i);
        this.occurenceSuffix = occurence.occurenceSuffix();
        this.requiresParentheses = requiresParentheses();
        this.lookup = lookup_();
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
        if (!(obj instanceof XQueryEnumSequenceType))
            return false;
        final XQueryEnumSequenceType other = (XQueryEnumSequenceType) obj;
        if (!isNullableEquals(this.itemType, other.getItemType()))
            return false;
        if (occurence != other.getOccurence())
            return false;
        return true;
    }

    private static final BiPredicate<XQueryEnumSequenceType, XQueryEnumSequenceType> alwaysTrue = (_, _) -> true;
    private static final BiPredicate<XQueryEnumSequenceType, XQueryEnumSequenceType> alwaysFalse = (_, _) -> false;
    private static final int occurenceCount = XQueryOccurence.values().length;
	private static final BiPredicate[][] isSubtypeOf;
    static {
        isSubtypeOf = new BiPredicate[occurenceCount][occurenceCount];
        for (int i = 0; i < occurenceCount; i++) {
            for (int j = 0; j < occurenceCount; j++) {
                isSubtypeOf[i][j] = alwaysFalse;
            }
        }
        isSubtypeOf[ZERO][ZERO] = alwaysTrue;
        isSubtypeOf[ZERO][ZERO_OR_ONE] = alwaysTrue;
        isSubtypeOf[ZERO][ZERO_OR_MORE] = alwaysTrue;

        isSubtypeOf[ZERO_OR_ONE][ZERO_OR_ONE] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[ZERO_OR_ONE][ZERO_OR_MORE] = XQueryEnumSequenceType::isSubtypeItemtype;
        ;

        isSubtypeOf[ZERO_OR_MORE][ZERO_OR_MORE] = XQueryEnumSequenceType::isSubtypeItemtype;

        isSubtypeOf[ONE][ONE] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[ONE][ONE_OR_MORE] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[ONE][ZERO_OR_MORE] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[ONE][ZERO_OR_ONE] = XQueryEnumSequenceType::isSubtypeItemtype;

        isSubtypeOf[ONE_OR_MORE][ZERO_OR_MORE] = XQueryEnumSequenceType::isSubtypeItemtype;
        isSubtypeOf[ONE_OR_MORE][ONE_OR_MORE] = XQueryEnumSequenceType::isSubtypeItemtype;
    }

    private static boolean isSubtypeItemtype(final Object x, final Object y) {
        final XQueryEnumSequenceType this_ = (XQueryEnumSequenceType) x;
        final XQueryEnumSequenceType other = (XQueryEnumSequenceType) y;
        return this_.getItemType().itemtypeIsSubtypeOf(other.getItemType());
    }

    @Override
    public boolean isSubtypeOf(final XQuerySequenceType obj) {
        if (!(obj instanceof XQueryEnumSequenceType))
            return false;
        final XQueryEnumSequenceType other = (XQueryEnumSequenceType) obj;
        final XQueryOccurence otherOccurence = other.getOccurence();
        final BiPredicate<XQueryEnumSequenceType, XQueryEnumSequenceType> predicate =
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
        mergedOccurences[ZERO][ZERO] = zero;
        mergedOccurences[ZERO][ONE] = one;
        mergedOccurences[ZERO][ZERO_OR_ONE] = zeroOrOne;
        mergedOccurences[ZERO][ZERO_OR_MORE] = zeroOrMore;
        mergedOccurences[ZERO][ONE_OR_MORE] = oneOrMore;

        mergedOccurences[ONE][ZERO] = one;
        mergedOccurences[ONE][ONE] = oneOrMore;
        mergedOccurences[ONE][ZERO_OR_ONE] = oneOrMore;
        mergedOccurences[ONE][ZERO_OR_MORE] = oneOrMore;
        mergedOccurences[ONE][ONE_OR_MORE] = oneOrMore;

        mergedOccurences[ZERO_OR_ONE][ZERO] = zeroOrOne;
        mergedOccurences[ZERO_OR_ONE][ONE] = oneOrMore;
        mergedOccurences[ZERO_OR_ONE][ZERO_OR_ONE] = zeroOrMore;
        mergedOccurences[ZERO_OR_ONE][ZERO_OR_MORE] = zeroOrMore;
        mergedOccurences[ZERO_OR_ONE][ONE_OR_MORE] = oneOrMore;

        mergedOccurences[ZERO_OR_MORE][ZERO] = zeroOrMore;
        mergedOccurences[ZERO_OR_MORE][ONE] = oneOrMore;
        mergedOccurences[ZERO_OR_MORE][ZERO_OR_ONE] = zeroOrMore;
        mergedOccurences[ZERO_OR_MORE][ZERO_OR_MORE] = zeroOrMore;
        mergedOccurences[ZERO_OR_MORE][ONE_OR_MORE] = oneOrMore;

        mergedOccurences[ONE_OR_MORE][ZERO] = oneOrMore;
        mergedOccurences[ONE_OR_MORE][ONE] = oneOrMore;
        mergedOccurences[ONE_OR_MORE][ZERO_OR_ONE] = oneOrMore;
        mergedOccurences[ONE_OR_MORE][ZERO_OR_MORE] = oneOrMore;
        mergedOccurences[ONE_OR_MORE][ONE_OR_MORE] = oneOrMore;
    }


    @Override
    public XQuerySequenceType sequenceMerge(final XQuerySequenceType other) {
        final var enumType1 = this;
        final var enumType2 = (XQueryEnumSequenceType) other;
        final var enumItemType1 = this.getItemType();
        final var enumItemType2 = other.getItemType();
        final var sequenceGetterWithoutFactory = mergedOccurences[enumType1.getOccurence().ordinal()][enumType2.getOccurence().ordinal()];
		final Function<XQueryItemType, XQuerySequenceType> sequenceGetter = (Function) sequenceGetterWithoutFactory.apply(typeFactory);
        if (enumItemType1 == null && enumItemType2 == null) {
            return sequenceGetter.apply(typeFactory.itemAnyItem());
        }

        if (enumItemType1 == null) {
            return sequenceGetter.apply(enumItemType2);
        }
        if (enumItemType2 == null) {
            return sequenceGetter.apply(enumItemType1);
        }
        final XQueryItemType mergedItemType = enumItemType1.alternativeMerge(enumItemType2);
        return sequenceGetter.apply(mergedItemType);

    }

    @Override
    public boolean itemtypeIsSubtypeOf(final XQuerySequenceType obj) {
        return itemType.itemtypeIsSubtypeOf(itemType);
    }

    @Override
    public boolean hasEffectiveBooleanValue() {
        if (occurence == XQueryOccurence.ONE)
            return itemType.hasEffectiveBooleanValue();
        return true;
    }

    private static final XQueryOccurence[][] unionOccurences;
    static {
        final int occurenceCount = XQueryOccurence.values().length;
        unionOccurences = new XQueryOccurence[occurenceCount][occurenceCount];

        unionOccurences[ZERO][ZERO] = XQueryOccurence.ZERO;
        unionOccurences[ZERO][ONE] = XQueryOccurence.ONE;
        unionOccurences[ZERO][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
        unionOccurences[ZERO][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        unionOccurences[ZERO][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;

        unionOccurences[ONE][ZERO] = XQueryOccurence.ONE;
        unionOccurences[ONE][ONE] = XQueryOccurence.ONE_OR_MORE;
        unionOccurences[ONE][ZERO_OR_ONE] = XQueryOccurence.ONE_OR_MORE;
        unionOccurences[ONE][ZERO_OR_MORE] = XQueryOccurence.ONE_OR_MORE;
        unionOccurences[ONE][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;

        unionOccurences[ZERO_OR_ONE][ZERO] = XQueryOccurence.ZERO_OR_ONE;
        unionOccurences[ZERO_OR_ONE][ONE] = XQueryOccurence.ONE_OR_MORE;
        unionOccurences[ZERO_OR_ONE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_MORE;
        unionOccurences[ZERO_OR_ONE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        unionOccurences[ZERO_OR_ONE][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;

        unionOccurences[ZERO_OR_MORE][ZERO] = XQueryOccurence.ZERO_OR_MORE;
        unionOccurences[ZERO_OR_MORE][ONE] = XQueryOccurence.ONE_OR_MORE;
        unionOccurences[ZERO_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_MORE;
        unionOccurences[ZERO_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        unionOccurences[ZERO_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;

        unionOccurences[ONE_OR_MORE][ZERO] = XQueryOccurence.ONE_OR_MORE;
        unionOccurences[ONE_OR_MORE][ONE] = XQueryOccurence.ONE_OR_MORE;
        unionOccurences[ONE_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ONE_OR_MORE;
        unionOccurences[ONE_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ONE_OR_MORE;
        unionOccurences[ONE_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;
    }


    @Override
    public XQuerySequenceType unionMerge(final XQuerySequenceType other) {
        final var other_ = (XQueryEnumSequenceType) other;
        final XQueryEnumItemType otherItemType = other_.getItemType();
        final XQueryOccurence mergedOccurence = unionOccurences[this.occurence.ordinal()][other_.getOccurence().ordinal()];
        final int occurence_ = mergedOccurence.ordinal();
        if (itemType == null) {
            return (XQuerySequenceType) factoryByOccurence[occurence_].apply(otherItemType);
        }
        if (otherItemType == null) {
            return (XQuerySequenceType) factoryByOccurence[occurence_].apply(itemType);
        }
        final var mergedType = itemType.unionMerge(otherItemType);
        return (XQuerySequenceType) factoryByOccurence[occurence_].apply(mergedType);
    }

    private static final XQueryOccurence[][] intersectionOccurences;
    static {
        final int occurenceCount = XQueryOccurence.values().length;
        intersectionOccurences = new XQueryOccurence[occurenceCount][occurenceCount];

        final XQueryOccurence zero = XQueryOccurence.ZERO;
        // final XQueryOccurence one = XQueryOccurence.ONE;
        final XQueryOccurence zeroOrOne = XQueryOccurence.ZERO_OR_ONE;
        final XQueryOccurence zeroOrMore = XQueryOccurence.ZERO_OR_MORE;
        // final XQueryOccurence oneOrMore = XQueryOccurence.ONE_OR_MORE;

        final int zero_ = ZERO;
        final int one_ = ONE;
        final int zeroOrOne_ = ZERO_OR_ONE;
        final int zeroOrMore_ = ZERO_OR_MORE;
        final int oneOrMore_ = ONE_OR_MORE;

        intersectionOccurences[zero_][zero_] = zero;
        intersectionOccurences[zero_][one_] = zero;
        intersectionOccurences[zero_][zeroOrOne_] = zero;
        intersectionOccurences[zero_][zeroOrMore_] = zero;
        intersectionOccurences[zero_][oneOrMore_] = zero;

        intersectionOccurences[one_][zero_] = zero;
        intersectionOccurences[one_][one_] = zeroOrOne;
        intersectionOccurences[one_][zeroOrOne_] = zeroOrOne;
        intersectionOccurences[one_][zeroOrMore_] = zeroOrOne;
        intersectionOccurences[one_][oneOrMore_] = zeroOrOne;

        intersectionOccurences[zeroOrOne_][zero_] = zero;
        intersectionOccurences[zeroOrOne_][one_] = zeroOrOne;
        intersectionOccurences[zeroOrOne_][zeroOrOne_] = zeroOrOne;
        intersectionOccurences[zeroOrOne_][zeroOrMore_] = zeroOrOne;
        intersectionOccurences[zeroOrOne_][oneOrMore_] = zeroOrOne;

        intersectionOccurences[zeroOrMore_][zero_] = zero;
        intersectionOccurences[zeroOrMore_][one_] = zeroOrOne;
        intersectionOccurences[zeroOrMore_][zeroOrOne_] = zeroOrOne;
        intersectionOccurences[zeroOrMore_][zeroOrMore_] = zeroOrMore;
        intersectionOccurences[zeroOrMore_][oneOrMore_] = zeroOrMore;

        intersectionOccurences[oneOrMore_][zero_] = zero;
        intersectionOccurences[oneOrMore_][one_] = zeroOrOne;
        intersectionOccurences[oneOrMore_][zeroOrOne_] = zeroOrOne;
        intersectionOccurences[oneOrMore_][zeroOrMore_] = zeroOrMore;
        intersectionOccurences[oneOrMore_][oneOrMore_] = zeroOrMore;
    }

    @Override
    public XQuerySequenceType intersectionMerge(final XQuerySequenceType other) {
        final var other_ = (XQueryEnumSequenceType) other;
        final XQueryEnumItemType otherItemType = other_.getItemType();
        final XQueryOccurence mergedOccurence = intersectionOccurences[this.occurence.ordinal()][other_.getOccurence().ordinal()];
        final int occurence_ = mergedOccurence.ordinal();
        if (itemType == null) {
            return (XQuerySequenceType) factoryByOccurence[occurence_].apply(otherItemType);
        }
        if (otherItemType == null) {
            return (XQuerySequenceType) factoryByOccurence[occurence_].apply(itemType);
        }
        final var mergedType = itemType.intersectionMerge(otherItemType);
        return (XQuerySequenceType) factoryByOccurence[occurence_].apply(mergedType);
    }


    private static final XQueryOccurence[][] exceptOccurences;
    static {
        final int occurenceCount = XQueryOccurence.values().length;
        exceptOccurences = new XQueryOccurence[occurenceCount][occurenceCount];

        exceptOccurences[ZERO][ZERO] = XQueryOccurence.ZERO;
        exceptOccurences[ZERO][ONE] = XQueryOccurence.ZERO;
        exceptOccurences[ZERO][ZERO_OR_ONE] = XQueryOccurence.ZERO;
        exceptOccurences[ZERO][ZERO_OR_MORE] = XQueryOccurence.ZERO;
        exceptOccurences[ZERO][ONE_OR_MORE] = XQueryOccurence.ZERO;

        exceptOccurences[ONE][ZERO] = XQueryOccurence.ONE;
        exceptOccurences[ONE][ONE] = XQueryOccurence.ZERO_OR_ONE;
        exceptOccurences[ONE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
        exceptOccurences[ONE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;
        exceptOccurences[ONE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;

        exceptOccurences[ZERO_OR_ONE][ZERO] = XQueryOccurence.ZERO_OR_ONE;
        exceptOccurences[ZERO_OR_ONE][ONE] = XQueryOccurence.ZERO_OR_ONE;
        exceptOccurences[ZERO_OR_ONE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
        exceptOccurences[ZERO_OR_ONE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;
        exceptOccurences[ZERO_OR_ONE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;

        exceptOccurences[ZERO_OR_MORE][ZERO] = XQueryOccurence.ZERO_OR_MORE;
        exceptOccurences[ZERO_OR_MORE][ONE] = XQueryOccurence.ZERO_OR_MORE;
        exceptOccurences[ZERO_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_MORE;
        exceptOccurences[ZERO_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        exceptOccurences[ZERO_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;

        exceptOccurences[ONE_OR_MORE][ZERO] = XQueryOccurence.ONE_OR_MORE;
        exceptOccurences[ONE_OR_MORE][ONE] = XQueryOccurence.ZERO_OR_MORE;
        exceptOccurences[ONE_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_MORE;
        exceptOccurences[ONE_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        exceptOccurences[ONE_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
    }

    @Override
    public XQuerySequenceType exceptionMerge(final XQuerySequenceType other) {
        final var other_ = (XQueryEnumSequenceType) other;
        final XQueryOccurence mergedOccurence = exceptOccurences[this.occurence.ordinal()][other_.getOccurence().ordinal()];
        final Function typeFactoryMethod = factoryByOccurence[mergedOccurence.ordinal()];
        final var usedItemType = occurence == XQueryOccurence.ZERO? typeFactory.itemAnyNode(): itemType;
        return (XQuerySequenceType) typeFactoryMethod.apply(usedItemType);
    }


    private static XQueryOccurence[][] typeAlternativeOccurence = new XQueryOccurence[XQueryOccurence.values().length][XQueryOccurence.values().length];
    static {
        typeAlternativeOccurence[ZERO][ZERO] = XQueryOccurence.ZERO;
        typeAlternativeOccurence[ZERO][ONE] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[ZERO][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[ZERO][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[ZERO][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;

        typeAlternativeOccurence[ONE][ZERO] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[ONE][ONE] = XQueryOccurence.ONE;
        typeAlternativeOccurence[ONE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[ONE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[ONE][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;

        typeAlternativeOccurence[ZERO_OR_ONE][ZERO] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[ZERO_OR_ONE][ONE] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[ZERO_OR_ONE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
        typeAlternativeOccurence[ZERO_OR_ONE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[ZERO_OR_ONE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;

        typeAlternativeOccurence[ZERO_OR_MORE][ZERO] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[ZERO_OR_MORE][ONE] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[ZERO_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[ZERO_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[ZERO_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;

        typeAlternativeOccurence[ONE_OR_MORE][ZERO] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[ONE_OR_MORE][ONE] = XQueryOccurence.ONE_OR_MORE;
        typeAlternativeOccurence[ONE_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[ONE_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        typeAlternativeOccurence[ONE_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;
    }

	final Function<XQueryItemType, XQuerySequenceType>[] factoryByOccurence;

    @Override
    public XQuerySequenceType alternativeMerge(final XQuerySequenceType other) {
        final var other_ = (XQueryEnumSequenceType) other;
        final var occurence_ = typeAlternativeOccurence[occurence.ordinal()][other_.getOccurence().ordinal()];
		final Function sequenceTypeFactory = factoryByOccurence[occurence_.ordinal()];
        XQueryItemType otherItemType = other.getItemType();
        if (this.itemType == null)
            return (XQuerySequenceType)sequenceTypeFactory.apply(otherItemType);
        if (otherItemType == null)
            return (XQuerySequenceType)sequenceTypeFactory.apply(itemType);
        final XQueryItemType mergedItemType = itemType.alternativeMerge(otherItemType);
        return (XQuerySequenceType) sequenceTypeFactory.apply(mergedItemType);
    }

    @Override
    public boolean castableAs(final XQuerySequenceType other) {
        if (!(other instanceof XQueryEnumSequenceType))
            return false;
        final XQueryEnumSequenceType otherEnum = (XQueryEnumSequenceType) other;
        if (!this.isOne() || !other.isOne()) {
            return false;
        }
        return this.getItemType().castableAs(otherEnum.getItemType());
    }

	@Override
    public XQuerySequenceType addOptionality() {
        return alternativeMerge(typeFactory.emptySequence());
    }


    private static final boolean[][] isValueComparableWith;
    static {
        isValueComparableWith = new boolean[occurenceCount][occurenceCount];
        for (int i = 0; i < isValueComparableWith.length; i++) {
            for (int j = 0; j < isValueComparableWith.length; j++) {
                isValueComparableWith[i][j] = false;
            }
        }
        isValueComparableWith[ZERO][ZERO] = true;
        isValueComparableWith[ZERO][ONE] = true;
        isValueComparableWith[ZERO][ZERO_OR_ONE] = true;

        isValueComparableWith[ONE][ZERO] = true;
        isValueComparableWith[ONE][ONE] = true;
        isValueComparableWith[ONE][ZERO_OR_ONE] = true;

        isValueComparableWith[ZERO_OR_ONE][ONE] = true;
        isValueComparableWith[ZERO_OR_ONE][ZERO] = true;
        isValueComparableWith[ZERO_OR_ONE][ZERO_OR_ONE] = true;
    }

    @Override
    public boolean isValueComparableWith(final XQuerySequenceType other) {
        final var cast = (XQueryEnumSequenceType) other;
        if (isZero() || other.isZero())
            return true;
        return isValueComparableWith[occurence.ordinal()][cast.getOccurence().ordinal()] && itemType.isValueComparableWith(other.getItemType());
    }


    @Override
    public XQuerySequenceType iteratedItem() {
        if (occurence != XQueryOccurence.ZERO)
            return typeFactory.one(itemType);
        else
            return typeFactory.emptySequence();
    }

    @Override
    public XQuerySequenceType mapping(final XQuerySequenceType mappingExpressionType) {
        return (XQuerySequenceType) factoryByOccurence[occurence.ordinal()].apply(mappingExpressionType.getItemType());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (occurence == XQueryOccurence.ZERO) {
            sb.append("empty-sequence()");
            return sb.toString();
        }

        if (requiresParentheses)
        {
            sb.append("(");
            sb.append(itemType);
            sb.append(")");
        }
        else {
            sb.append(itemType);
        }
        sb.append(occurenceSuffix);
        return sb.toString();
    }

    private final boolean requiresParentheses;
    private boolean requiresParentheses() {
        return occurenceSuffix != "" &&( itemType instanceof XQueryEnumItemTypeFunction
                                        || itemType instanceof XQueryEnumChoiceItemType);
    }

    @Override
    public RelativeCoercability coerceableTo(XQuerySequenceType requiredType) {
        if (this == requiredType || isSubtypeOf(requiredType)) {
            return RelativeCoercability.ALWAYS;
        }
        boolean emptySequenceRequired = requiredType.isSubtypeOf(typeFactory.emptySequence());
        if (emptySequenceRequired) {
            return RelativeCoercability.NEVER;
        }
        return RelativeCoercability.POSSIBLE;
    }


    @Override
    public XQueryItemType getMapKeyType() {
        return itemType.getMapKeyType();
    }

    @Override
    public XQuerySequenceType getMapValueType() {
        return itemType.getMapValueType();
    }

    @Override
    public XQuerySequenceType getArrayMemberType() {
        return itemType.getArrayType();
    }

    @Override
    public XQuerySequenceType getReturnedType() {
        return itemType.getReturnedType();
    }

    @Override
    public XQuerySequenceType lookup(XQuerySequenceType keySpecifierType) {
        return this.lookup.apply(keySpecifierType);
    }

    public Function<XQuerySequenceType, XQuerySequenceType> lookup_() {
        if (itemType.itemtypeIsSubtypeOf(typeFactory.itemAnyArray())) {
            return (keySpecifierType) -> {
                XQueryItemType lookedUpItem = itemType.lookup(keySpecifierType).getItemType();
                XQuerySequenceType lookedUpSequence = factoryByOccurence[occurence_].apply(lookedUpItem);
                return lookedUpSequence;
            };
        }

        if (itemType.itemtypeIsSubtypeOf(typeFactory.itemAnyMap())) {
            return keySpecifierType -> {
                XQueryItemType lookedUpItem = itemType.lookup(keySpecifierType).getItemType();
                XQuerySequenceType lookedUpSequence = factoryByOccurence[occurence_].apply(lookedUpItem);
                return lookedUpSequence.addOptionality();
            };
        }
        return (_) -> typeFactory.error();
    }

}
