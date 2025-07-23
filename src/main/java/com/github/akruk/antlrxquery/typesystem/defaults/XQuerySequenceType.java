package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.function.Function;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.AlternativeOccurenceMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.ExceptionOccurenceMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.IntersectionOccurenceMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.IsSuboccurence;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.IsValueComparableWith;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.SequenceOccurenceMerger;
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
        this.factoryByOccurence[ZERO]         = _ -> typeFactory.emptySequence();
        this.factoryByOccurence[ONE]          = i -> typeFactory.one(i);
        this.factoryByOccurence[ZERO_OR_ONE]  = i -> typeFactory.zeroOrOne(i);
        this.factoryByOccurence[ZERO_OR_MORE] = i -> typeFactory.zeroOrMore(i);
        this.factoryByOccurence[ONE_OR_MORE]  = i -> typeFactory.oneOrMore(i);
        this.occurenceSuffix = occurence.occurenceSuffix();
        this.requiresParentheses = requiresParentheses();
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
        if (!(obj instanceof final XQuerySequenceType other))
            return false;
        if (!isNullableEquals(this.itemType, other.getItemType()))
            return false;
        if (occurence != other.getOccurence())
            return false;
        return true;
    }

    private static final IsSuboccurence isSuboccurence = new IsSuboccurence();

    public boolean isSubtypeOf(final XQuerySequenceType other) {
        final XQueryOccurence otherOccurence = other.getOccurence();
        if (!isSuboccurence.test(occurence_, otherOccurence.ordinal())) {
            return false;
        }
        if (itemType == null)
            return true;
        return itemType.itemtypeIsSubtypeOf(other.getItemType());
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


    private static final SequenceOccurenceMerger sequenceOccurenceMerger = new SequenceOccurenceMerger();


    public XQuerySequenceType sequenceMerge(final XQuerySequenceType other) {
        final var itemType1 = this.getItemType();
        final var itemType2 = other.getItemType();
        final byte mergedOccurence = sequenceOccurenceMerger.merge(occurence_, (byte)other.getOccurence().ordinal());
        final Function<XQueryItemType, XQuerySequenceType> factory = factoryByOccurence[mergedOccurence];
        if (itemType1 == null && itemType2 == null) {
            return factory.apply(typeFactory.itemAnyItem());
        }
        if (itemType1 == null) {
            return factory.apply(itemType2);
        }
        if (itemType2 == null) {
            return factory.apply(itemType1);
        }
        final XQueryItemType mergedItemType = itemType1.alternativeMerge(itemType2);
        return factory.apply(mergedItemType);

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
            return factoryByOccurence[occurence_].apply(otherItemType);
        }
        if (otherItemType == null) {
            return factoryByOccurence[occurence_].apply(itemType);
        }
        final var mergedType = itemType.unionMerge(otherItemType);
        return factoryByOccurence[occurence_].apply(mergedType);
    }

    private static final IntersectionOccurenceMerger intersectionOccurences = new IntersectionOccurenceMerger();

    public XQuerySequenceType intersectionMerge(final XQuerySequenceType other) {
        final var other_ = (XQuerySequenceType) other;
        final XQueryItemType otherItemType = other_.getItemType();
        final XQueryOccurence mergedOccurence = intersectionOccurences.merge(occurence, other.getOccurence());
        final int occurence_ = mergedOccurence.ordinal();
        if (itemType == null) {
            return factoryByOccurence[occurence_].apply(otherItemType);
        }
        if (otherItemType == null) {
            return factoryByOccurence[occurence_].apply(itemType);
        }
        final var mergedType = itemType.intersectionMerge(otherItemType);
        return factoryByOccurence[occurence_].apply(mergedType);
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



    private static final IsValueComparableWith isValueComparable = new IsValueComparableWith();

    public boolean isValueComparableWith(final XQuerySequenceType other) {
        if (isZero() || other.isZero())
            return true;
        return (isValueComparable.isValueComparableWith(occurence, other.getOccurence())
                && itemType.isValueComparableWith(other.getItemType()));
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
        else
        {
            sb.append(itemType);
        }
        sb.append(occurenceSuffix);
        return sb.toString();
    }

    private final boolean requiresParentheses;
    private boolean requiresParentheses() {
        return occurenceSuffix != "" &&(itemType instanceof XQueryItemTypeFunction
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
