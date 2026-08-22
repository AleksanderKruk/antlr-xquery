package com.github.akruk.antlrquery.typesystem.types;



import com.github.akruk.antlrquery.typesystem.typeoperations.stringify.Stringify;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;


@DefaultQualifier(NonNull.class)
public sealed interface AntlrQuerySequenceType 
    permits AntlrQuerySequenceType.EmptySequence, 
            AntlrQuerySequenceType.NonEmptySequence
{
    record EmptySequence()
        implements AntlrQuerySequenceType 
    {
        @Override
        public Cardinality cardinality() {
            return Cardinality.ZERO;
        }

        @Override
        public AntlrQueryItemType itemType() {
            return AntlrQueryItemType.NOTHING;
        }

        @Override
        public String toString() {
            return Stringify.stringify(this);
        }
    }
    record NonEmptySequence(AntlrQueryItemType itemType, Cardinality cardinality)
        implements AntlrQuerySequenceType
    {
        @Override
        public String toString() {
            return Stringify.stringify(this);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            NonEmptySequence that = (NonEmptySequence) o;
            return Objects.equals(cardinality, that.cardinality)
                    && Objects.equals(itemType, that.itemType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemType, cardinality);
        }
    }


    @SuppressWarnings("SameReturnValue")
    Cardinality cardinality();
    @SuppressWarnings("SameReturnValue")
    AntlrQueryItemType itemType();


}
