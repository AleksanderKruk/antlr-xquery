package com.github.akruk.antlrquery.typesystem.types;



import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;


@DefaultQualifier(NonNull.class)
public sealed interface AntlrQuerySequenceType 
    permits AntlrQuerySequenceType.EmptySequence, 
            AntlrQuerySequenceType.NonEmptySequence
{
    public static final record EmptySequence() 
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

        
    }
    public static record NonEmptySequence(AntlrQueryItemType itemType, Cardinality cardinality)
        implements AntlrQuerySequenceType {}


    public Cardinality cardinality();
    public AntlrQueryItemType itemType();


}

//     public final XQueryItemType itemType;
//     public final Cardinality cardinality;

//     private final AntlrQueryTypeFactory typeFactory;






// }
