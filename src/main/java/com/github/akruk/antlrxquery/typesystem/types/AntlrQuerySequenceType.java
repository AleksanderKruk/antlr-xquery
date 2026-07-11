package com.github.akruk.antlrxquery.typesystem.types;

import java.util.Set;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Cardinalities;

public class AntlrQuerySequenceType {
    public enum RelativeCoercability {
        ALWAYS, POSSIBLE, NEVER
    }
    public final XQueryItemType itemType;
    public final Cardinality cardinality;

    private final AntlrQueryTypeFactory typeFactory;



    public AntlrQuerySequenceType(
        final AntlrQueryTypeFactory typeFactory,
        final XQueryItemType itemType,
        final Cardinality cardinality)
    {
        this.typeFactory = typeFactory;
        this.itemType = itemType;
        this.cardinality = cardinality;
        this.requiresParentheses = requiresParentheses();
    }

    private static boolean isNullableEquals(final Object one, final Object other)
    {
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
        if (!(obj instanceof final AntlrQuerySequenceType other))
            return false;
        if (!cardinality.equals(other.cardinality))
            return false;
        if (!isNullableEquals(this.itemType, other.itemType))
            return false;
        return true;
    }


    public boolean isSubtypeOf(final AntlrQuerySequenceType other) {
        if (!Cardinalities.isSubset(cardinality, other.cardinality)) {
            return false;
        }
        if (itemType == null)
            return true;
        return itemType.itemtypeIsSubtypeOf(other.itemType);
    }




    public AntlrQuerySequenceType sequenceMerge(final AntlrQuerySequenceType other) {
        final var itemType1 = this.itemType != null ? this.itemType : typeFactory.itemAnyItem();
        final var itemType2 = other.itemType != null ? other.itemType : typeFactory.itemAnyItem();
        final Cardinality mergedCardinality = Cardinalities.sequenceMerge(this.cardinality, other.cardinality);
        final XQueryItemType mergedItemType = itemType1.alternativeMerge(itemType2);
        return typeFactory.sequence(mergedItemType, mergedCardinality);

    }


    public boolean itemtypeIsSubtypeOf(final AntlrQuerySequenceType obj) {
        return itemType.itemtypeIsSubtypeOf(itemType);
    }


    public enum EffectiveBooleanValueType {
        ALWAYS_FALSE__EMPTY_SEQUENCE,
        ALWAYS_TRUE__NUMBER_STRING_BOOLEAN,
        NUMBER_STRING_BOOLEAN,
        ALWAYS_TRUE__NODE,
        NODE,
        NO_EBV
    }

    public EffectiveBooleanValueType effectiveBooleanValueType() {
        if (isSubtypeOf(typeFactory.emptySequence())) {
            return EffectiveBooleanValueType.ALWAYS_FALSE__EMPTY_SEQUENCE;
        }
        final XQueryItemType itemChoice = typeFactory.itemChoice(Set.of(
            typeFactory.itemString(),
            typeFactory.itemBoolean(),
            typeFactory.itemNumber()
        ));

        var variantSingleton = typeFactory.one(itemChoice);
        if (isSubtypeOf(variantSingleton)) {
            return EffectiveBooleanValueType.ALWAYS_TRUE__NUMBER_STRING_BOOLEAN;
        }

        variantSingleton = typeFactory.zeroOrOne(itemChoice);
        if (isSubtypeOf(variantSingleton)) {
            return EffectiveBooleanValueType.NUMBER_STRING_BOOLEAN;
        }
        var variantNodes = typeFactory.oneOrMore(typeFactory.itemAnyNode());
        if (isSubtypeOf(variantNodes)) {
            return EffectiveBooleanValueType.ALWAYS_TRUE__NODE;
        }
        variantNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        if (isSubtypeOf(variantNodes)) {
            return EffectiveBooleanValueType.NODE;
        }
        return EffectiveBooleanValueType.NO_EBV;
    }


    public boolean hasEffectiveBooleanValue() {
        return effectiveBooleanValueType() != EffectiveBooleanValueType.NO_EBV;
    }

    public AntlrQuerySequenceType unionMerge(final AntlrQuerySequenceType other) {
        final XQueryItemType thisItemType = this.itemType != null? this.itemType : typeFactory.itemAnyItem();
        final XQueryItemType otherItemType = other.itemType != null? other.itemType : typeFactory.itemAnyItem();
        final Cardinality mergedCardinality = Cardinalities.union(cardinality, other.cardinality);
        final var mergedType = thisItemType.unionMerge(otherItemType);
        return typeFactory.sequence(mergedType, mergedCardinality);
    }

    public AntlrQuerySequenceType intersectionMerge(final AntlrQuerySequenceType other) {
        final XQueryItemType thisItemType = this.itemType != null ? this.itemType : typeFactory.itemAnyItem();
        final XQueryItemType otherItemType = other.itemType != null ? other.itemType : typeFactory.itemAnyItem();
        final Cardinality mergedOccurence = Cardinalities.intersection(cardinality, other.cardinality);
        final var mergedType = thisItemType.intersectionMerge(otherItemType);
        return typeFactory.sequence(mergedType, mergedOccurence);
    }


    public AntlrQuerySequenceType exceptionMerge(final AntlrQuerySequenceType other) {
        final XQueryItemType thisItemType = this.itemType != null ? this.itemType : typeFactory.itemAnyItem();
        final XQueryItemType otherItemType = other.itemType != null ? other.itemType : typeFactory.itemAnyItem();
        final Cardinality mergedCardinality = Cardinalities.subtract(cardinality, other.cardinality);
        final var mergedType = thisItemType.exceptionMerge(otherItemType);
        return typeFactory.sequence(mergedType, mergedCardinality);
    }


    public AntlrQuerySequenceType alternativeMerge(final AntlrQuerySequenceType other) {
        final Cardinality mergedCardinality = Cardinalities.union(cardinality, other.cardinality);
        final XQueryItemType mergedType;
        if (this.itemType == null && other.itemType != null) {
            mergedType = other.itemType;
        } else if (other.itemType == null && this.itemType != null) {
            mergedType = this.itemType;
        } else {
            final XQueryItemType thisItemType = this.itemType != null ? this.itemType : typeFactory.itemAnyItem();
            final XQueryItemType otherItemType = other.itemType != null ? other.itemType : typeFactory.itemAnyItem();
            mergedType = thisItemType.alternativeMerge(otherItemType);
        }
        return typeFactory.sequence(mergedType, mergedCardinality);
    }


    public boolean castableAs(final AntlrQuerySequenceType other) {
        if (!(other instanceof AntlrQuerySequenceType))
            return false;
        final AntlrQuerySequenceType otherEnum = (AntlrQuerySequenceType) other;
        if (!this.cardinality.isOne() || !other.cardinality.isOne()) {
            return false;
        }
        return this.itemType.castableAs(otherEnum.itemType);
    }


    public AntlrQuerySequenceType addOptionality() {
        return alternativeMerge(typeFactory.emptySequence());
    }



    public boolean isValueComparableWith(final AntlrQuerySequenceType other) {
        if (cardinality.isZero() || other.cardinality.isZero())
        {
            return true;
        }
        return (Cardinalities.isValueComparableWith(cardinality, other.cardinality)
                && itemType.isValueComparableWith(other.itemType));
    }



    public AntlrQuerySequenceType iteratorType()
    {
        if (!cardinality.isZero())
            return typeFactory.one(itemType);
        else
            return typeFactory.emptySequence();
    }



    public static AntlrQuerySequenceType emptySequence(final AntlrQueryTypeFactory typeFactory) {
        return new AntlrQuerySequenceType(typeFactory, null, Cardinality.ZERO);
    }



    public String toString() {
        if (cardinality.isZero()) {
            return "empty-sequence()";
        }
        final StringBuilder sb = new StringBuilder();

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
        sb.append(Cardinalities.stringifyWithPrefix(cardinality));
        return sb.toString();
    }

    private final boolean requiresParentheses;
    private boolean requiresParentheses() {
        final boolean suffixIsPresent = this.cardinality.isOne() || this.cardinality.isZero();
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


    public RelativeCoercability coerceableTo(final AntlrQuerySequenceType requiredType) {
        if (this == requiredType || isSubtypeOf(requiredType)) {
            return RelativeCoercability.ALWAYS;
        }
        final boolean emptySequenceRequired = requiredType.isSubtypeOf(typeFactory.emptySequence());
        if (emptySequenceRequired) {
            return RelativeCoercability.NEVER;
        }
        return RelativeCoercability.POSSIBLE;
    }


}
