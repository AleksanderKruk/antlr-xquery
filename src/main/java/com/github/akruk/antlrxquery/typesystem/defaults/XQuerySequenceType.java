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

    public final XQueryItemType itemType;
    public final XQueryOccurence occurence;
    public final int occurenceOrdinal;
    public final boolean hasEffectiveBooleanValue;
    public final boolean isZero;
    public final boolean isOne;
    public final boolean isZeroOrOne;
    public final boolean isZeroOrMore;
    public final boolean isOneOrMore;
    public final XQuerySequenceType iteratorType;

    private final XQueryTypeFactory typeFactory;
    private final String occurenceSuffix;

    public XQuerySequenceType(final XQueryTypeFactory typeFactory, final XQueryItemType itemType, final XQueryOccurence occurence) {
        this.typeFactory = typeFactory;
        this.itemType = itemType;
        this.occurence = occurence;
        this.occurenceOrdinal = occurence.ordinal();
        this.factoryByOccurence = new Function[XQueryOccurence.values().length];
        this.factoryByOccurence[ZERO]         = _ -> typeFactory.emptySequence();
        this.factoryByOccurence[ONE]          = i -> typeFactory.one(i);
        this.factoryByOccurence[ZERO_OR_ONE]  = i -> typeFactory.zeroOrOne(i);
        this.factoryByOccurence[ZERO_OR_MORE] = i -> typeFactory.zeroOrMore(i);
        this.factoryByOccurence[ONE_OR_MORE]  = i -> typeFactory.oneOrMore(i);
        this.occurenceSuffix = occurence.occurenceSuffix();
        this.requiresParentheses = requiresParentheses();
        this.isZero = XQueryOccurence.ZERO == occurence;
        this.isOne = XQueryOccurence.ONE == occurence;
        this.isZeroOrOne = XQueryOccurence.ZERO_OR_ONE == occurence;
        this.isZeroOrMore = XQueryOccurence.ZERO_OR_MORE == occurence;
        this.isOneOrMore = XQueryOccurence.ONE_OR_MORE == occurence;
        this.hasEffectiveBooleanValue = hasEffectiveBooleanValue();
        this.iteratorType = this.iteratedItem_();
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
        if (occurence != other.occurence)
            return false;
        if (!isNullableEquals(this.itemType, other.itemType))
            return false;
        return true;
    }

    private static final IsSuboccurence isSuboccurence = new IsSuboccurence();

    public boolean isSubtypeOf(final XQuerySequenceType other) {
        if (!isSuboccurence.test(occurenceOrdinal, other.occurenceOrdinal)) {
            return false;
        }
        if (itemType == null)
            return true;
        return itemType.itemtypeIsSubtypeOf(other.itemType);
    }


    private static final SequenceOccurenceMerger sequenceOccurenceMerger = new SequenceOccurenceMerger();


    public XQuerySequenceType sequenceMerge(final XQuerySequenceType other) {
        final var itemType1 = this.itemType;
        final var itemType2 = other.itemType;
        final byte mergedOccurence = sequenceOccurenceMerger.merge(occurenceOrdinal, other.occurenceOrdinal);
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


    private boolean hasEffectiveBooleanValue() {
        return switch(occurence) {
            case ZERO -> true;
            case ZERO_OR_ONE, ONE -> itemType.hasEffectiveBooleanValue;
            default -> false;
        };
    }

    private static final UnionOccurenceMerger unionOccurences = new UnionOccurenceMerger();



    public XQuerySequenceType unionMerge(final XQuerySequenceType other) {
        final XQueryItemType otherItemType = other.itemType;
        final XQueryOccurence mergedOccurence = unionOccurences.merge(occurence, other.occurence);
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
        final XQueryItemType otherItemType = other.itemType;
        final XQueryOccurence mergedOccurence = intersectionOccurences.merge(occurence, other.occurence);
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
        final XQueryOccurence mergedOccurence = exceptOccurences.merge(this.occurence, other_.occurence);
        final Function typeFactoryMethod = factoryByOccurence[mergedOccurence.ordinal()];
        final var usedItemType = occurence == XQueryOccurence.ZERO? typeFactory.itemAnyNode(): itemType;
        return (XQuerySequenceType) typeFactoryMethod.apply(usedItemType);
    }


    private static final AlternativeOccurenceMerger typeAlternativeOccurence = new AlternativeOccurenceMerger();
	final Function<XQueryItemType, XQuerySequenceType>[] factoryByOccurence;

    public XQuerySequenceType alternativeMerge(final XQuerySequenceType other) {
        final var occurence_ = typeAlternativeOccurence.merge(occurence, other.occurence);
		final Function sequenceTypeFactory = factoryByOccurence[occurence_.ordinal()];
        final XQueryItemType otherItemType = other.itemType;
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
        if (!this.isOne || !other.isOne) {
            return false;
        }
        return this.itemType.castableAs(otherEnum.itemType);
    }


    public XQuerySequenceType addOptionality() {
        return alternativeMerge(typeFactory.emptySequence());
    }



    private static final IsValueComparableWith occurenceIsValueComparable = new IsValueComparableWith();

    public boolean isValueComparableWith(final XQuerySequenceType other) {
        if (occurence == XQueryOccurence.ZERO
            || other.occurence == XQueryOccurence.ZERO)
        {
            return true;
        }
        return (occurenceIsValueComparable.isValueComparableWith(occurence, other.occurence)
                && itemType.isValueComparableWith(other.itemType));
    }



    private XQuerySequenceType iteratedItem_()
    {
        if (occurence != XQueryOccurence.ZERO)
            return typeFactory.one(itemType);
        else
            return typeFactory.emptySequence();
    }


    public XQuerySequenceType mapping(final XQuerySequenceType mappingExpressionType)
    {
        return (XQuerySequenceType) factoryByOccurence[occurenceOrdinal].apply(mappingExpressionType.itemType);
    }


    public static XQuerySequenceType emptySequence(XQueryTypeFactory typeFactory) {
        return new XQuerySequenceType(typeFactory, null, XQueryOccurence.ZERO);
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
        final boolean suffixIsPresent = occurenceSuffix != "";
        if (!suffixIsPresent)
            return false;
        if (itemType == null)
            return false;
        final boolean containsComplexItemtype = switch(itemType.type) {
            case FUNCTION, ANY_FUNCTION, CHOICE -> true;
            default -> false;
        };
        return containsComplexItemtype;
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



}
