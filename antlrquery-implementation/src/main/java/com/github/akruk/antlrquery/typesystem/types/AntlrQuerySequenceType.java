package com.github.akruk.antlrquery.typesystem.types;



import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;


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
            return Types.stringify(this);
        }
    }
    record NonEmptySequence(AntlrQueryItemType itemType, Cardinality cardinality)
        implements AntlrQuerySequenceType
    {
        @Override
        public String toString() {
            return Types.stringify(this);
        }
    }


    Cardinality cardinality();
    AntlrQueryItemType itemType();


}
