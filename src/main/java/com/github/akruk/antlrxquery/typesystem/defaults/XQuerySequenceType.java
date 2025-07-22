package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.function.BiPredicate;
import java.util.function.Function;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.AlternativeOccurenceMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.ExceptionOccurenceMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.UnionOccurenceMerger;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class XQuerySequenceType {
    public enum RelativeCoercability {
        ALWAYS, POSSIBLE, NEVER
    }
    private static final int ONE_OR_MORE = XQueryOccurence.ONE_OR_MORE.ordinal();
    private static final int ZERO_OR_MORE = XQueryOccurence.ZERO_OR_MORE.ordinal();
    private static final int ZERO_OR_ONE = XQueryOccurence.ZERO_OR_ONE.ordinal();
    private static final int ONE = XQueryOccurence.ONE.ordinal();
    private static final int ZERO = XQueryOccurence.ZERO.ordinal();
    private final XQueryItemType itemType;
    private final XQueryOccurence occurence;
    private final int occurence_;
    private final XQueryTypeFactory typeFactory;
    private final String occurenceSuffix;
    private final BiPredicate<XQuerySequenceType, XQuerySequenceType>[] isSubtypeOf_;

    private Function<XQuerySequenceType, XQuerySequenceType> lookup;

    public XQueryItemType getItemType() {
        return itemType;
    }

    public XQuerySequenceType(final XQueryTypeFactory typeFactory, final XQueryItemType itemType, final XQueryOccurence occurence) {
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
        this.isSubtypeOf_ = XQuerySequenceType.isSubtypeOf[occurence_];
        // this.lookup = lookup_();
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
        if (!(obj instanceof XQuerySequenceType))
            return false;
        final XQuerySequenceType other = (XQuerySequenceType) obj;
        if (!isNullableEquals(this.itemType, other.getItemType()))
            return false;
        if (occurence != other.getOccurence())
            return false;
        return true;
    }

    private static final BiPredicate<XQuerySequenceType, XQuerySequenceType> alwaysTrue = (_, _) -> true;
    private static final BiPredicate<XQuerySequenceType, XQuerySequenceType> alwaysFalse = (_, _) -> false;
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

        isSubtypeOf[ZERO_OR_ONE][ZERO_OR_ONE] = XQuerySequenceType::isSubtypeItemtype;
        isSubtypeOf[ZERO_OR_ONE][ZERO_OR_MORE] = XQuerySequenceType::isSubtypeItemtype;
        ;

        isSubtypeOf[ZERO_OR_MORE][ZERO_OR_MORE] = XQuerySequenceType::isSubtypeItemtype;

        isSubtypeOf[ONE][ONE] = XQuerySequenceType::isSubtypeItemtype;
        isSubtypeOf[ONE][ONE_OR_MORE] = XQuerySequenceType::isSubtypeItemtype;
        isSubtypeOf[ONE][ZERO_OR_MORE] = XQuerySequenceType::isSubtypeItemtype;
        isSubtypeOf[ONE][ZERO_OR_ONE] = XQuerySequenceType::isSubtypeItemtype;

        isSubtypeOf[ONE_OR_MORE][ZERO_OR_MORE] = XQuerySequenceType::isSubtypeItemtype;
        isSubtypeOf[ONE_OR_MORE][ONE_OR_MORE] = XQuerySequenceType::isSubtypeItemtype;
    }

    private static boolean isSubtypeItemtype(final Object x, final Object y) {
        final XQuerySequenceType this_ = (XQuerySequenceType) x;
        final XQuerySequenceType other = (XQuerySequenceType) y;
        return this_.getItemType().itemtypeIsSubtypeOf(other.getItemType());
    }

    public boolean isSubtypeOf(final XQuerySequenceType obj) {
        if (!(obj instanceof XQuerySequenceType other))
            return false;
        final XQueryOccurence otherOccurence = other.getOccurence();
        final BiPredicate<XQuerySequenceType, XQuerySequenceType> predicate =
            isSubtypeOf_[otherOccurence.ordinal()];
        return predicate.test(this, other);
    }
    public XQueryOccurence getOccurence() {
        return occurence;
    }

    public boolean isOne() {
        return occurence == XQueryOccurence.ONE;
    }


    public boolean isOneOrMore() {
        return occurence == XQueryOccurence.ONE_OR_MORE;
    }


    public boolean isZeroOrMore() {
        return occurence == XQueryOccurence.ZERO_OR_MORE;
    }


    public boolean isZeroOrOne() {
        return occurence == XQueryOccurence.ZERO_OR_ONE;
    }


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



    public XQuerySequenceType sequenceMerge(final XQuerySequenceType other) {
        final var enumType1 = this;
        final var enumType2 = (XQuerySequenceType) other;
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


    public boolean itemtypeIsSubtypeOf(final XQuerySequenceType obj) {
        return itemType.itemtypeIsSubtypeOf(itemType);
    }


    public boolean hasEffectiveBooleanValue() {
        if (occurence == XQueryOccurence.ONE)
            return itemType.hasEffectiveBooleanValue();
        return true;
    }

    private static final UnionOccurenceMerger unionOccurences = new UnionOccurenceMerger();



    public XQuerySequenceType unionMerge(final XQuerySequenceType other) {
        final XQueryItemType otherItemType = other.getItemType();
        final XQueryOccurence mergedOccurence = unionOccurences.merge(occurence, other.getOccurence());
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

        intersectionOccurences[ZERO][ZERO] = XQueryOccurence.ZERO;
        intersectionOccurences[ZERO][ONE] = XQueryOccurence.ZERO;
        intersectionOccurences[ZERO][ZERO_OR_ONE] = XQueryOccurence.ZERO;
        intersectionOccurences[ZERO][ZERO_OR_MORE] = XQueryOccurence.ZERO;
        intersectionOccurences[ZERO][ONE_OR_MORE] = XQueryOccurence.ZERO;

        intersectionOccurences[ONE][ZERO] = XQueryOccurence.ZERO;
        intersectionOccurences[ONE][ONE] = XQueryOccurence.ZERO_OR_ONE;
        intersectionOccurences[ONE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
        intersectionOccurences[ONE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;
        intersectionOccurences[ONE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;

        intersectionOccurences[ZERO_OR_ONE][ZERO] = XQueryOccurence.ZERO;
        intersectionOccurences[ZERO_OR_ONE][ONE] = XQueryOccurence.ZERO_OR_ONE;
        intersectionOccurences[ZERO_OR_ONE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
        intersectionOccurences[ZERO_OR_ONE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;
        intersectionOccurences[ZERO_OR_ONE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;

        intersectionOccurences[ZERO_OR_MORE][ZERO] = XQueryOccurence.ZERO;
        intersectionOccurences[ZERO_OR_MORE][ONE] = XQueryOccurence.ZERO_OR_ONE;
        intersectionOccurences[ZERO_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
        intersectionOccurences[ZERO_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        intersectionOccurences[ZERO_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;

        intersectionOccurences[ONE_OR_MORE][ZERO] = XQueryOccurence.ZERO;
        intersectionOccurences[ONE_OR_MORE][ONE] = XQueryOccurence.ZERO_OR_ONE;
        intersectionOccurences[ONE_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
        intersectionOccurences[ONE_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        intersectionOccurences[ONE_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
    }


    public XQuerySequenceType intersectionMerge(final XQuerySequenceType other) {
        final var other_ = (XQuerySequenceType) other;
        final XQueryItemType otherItemType = other_.getItemType();
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


    private static final ExceptionOccurenceMerger exceptOccurences = new ExceptionOccurenceMerger();

    public XQuerySequenceType exceptionMerge(final XQuerySequenceType other) {
        final var other_ = (XQuerySequenceType) other;
        final XQueryOccurence mergedOccurence = exceptOccurences.merge(this.occurence, other_.getOccurence());
        final Function typeFactoryMethod = factoryByOccurence[mergedOccurence.ordinal()];
        final var usedItemType = occurence == XQueryOccurence.ZERO? typeFactory.itemAnyNode(): itemType;
        return (XQuerySequenceType) typeFactoryMethod.apply(usedItemType);
    }


    private static final AlternativeOccurenceMerger typeAlternativeOccurence = new AlternativeOccurenceMerger();
	final Function<XQueryItemType, XQuerySequenceType>[] factoryByOccurence;

    public XQuerySequenceType alternativeMerge(final XQuerySequenceType other) {
        final var occurence_ = typeAlternativeOccurence.merge(occurence, other.getOccurence());
		final Function sequenceTypeFactory = factoryByOccurence[occurence_.ordinal()];
        final XQueryItemType otherItemType = other.getItemType();
        if (this.itemType == null)
            return (XQuerySequenceType)sequenceTypeFactory.apply(otherItemType);
        if (otherItemType == null)
            return (XQuerySequenceType)sequenceTypeFactory.apply(itemType);
        final XQueryItemType mergedItemType = itemType.alternativeMerge(otherItemType);
        return (XQuerySequenceType) sequenceTypeFactory.apply(mergedItemType);
    }


    public boolean castableAs(final XQuerySequenceType other) {
        if (!(other instanceof XQuerySequenceType))
            return false;
        final XQuerySequenceType otherEnum = (XQuerySequenceType) other;
        if (!this.isOne() || !other.isOne()) {
            return false;
        }
        return this.getItemType().castableAs(otherEnum.getItemType());
    }


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


    public boolean isValueComparableWith(final XQuerySequenceType other) {
        final var cast = (XQuerySequenceType) other;
        if (isZero() || other.isZero())
            return true;
        return isValueComparableWith[occurence_][cast.getOccurence().ordinal()] && itemType.isValueComparableWith(other.getItemType());
    }



    public XQuerySequenceType iteratedItem() {
        if (occurence != XQueryOccurence.ZERO)
            return typeFactory.one(itemType);
        else
            return typeFactory.emptySequence();
    }


    public XQuerySequenceType mapping(final XQuerySequenceType mappingExpressionType) {
        return (XQuerySequenceType) factoryByOccurence[occurence_].apply(mappingExpressionType.getItemType());
    }


    public String toString() {
        if (occurence == XQueryOccurence.ZERO) {
            return "empty-sequence()";
        }
        StringBuilder sb = new StringBuilder();

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
        return occurenceSuffix != "" &&( itemType instanceof XQueryItemTypeFunction
                                        || itemType instanceof XQueryChoiceItemType);
    }


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



    public XQueryItemType getMapKeyType() {
        return itemType.getMapKeyType();
    }


    public XQuerySequenceType getMapValueType() {
        return itemType.getMapValueType();
    }


    public XQuerySequenceType getArrayMemberType() {
        return itemType.getArrayMemberType();
    }


    public XQuerySequenceType getReturnedType() {
        return itemType.getReturnedType();
    }


    public XQuerySequenceType lookup(XQuerySequenceType keySpecifierType) {
        if (lookup == null)
            lookup = lookup_();
        return this.lookup.apply(keySpecifierType);
    }

    public Function<XQuerySequenceType, XQuerySequenceType> lookup_() {
        if (occurence_ == ZERO)
            return (_)->typeFactory.emptySequence();
        if (itemType == null)
            return (_)->typeFactory.error();

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

    public XQuerySequenceType lookupWildcard() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lookupWildcard'");
    }

}
