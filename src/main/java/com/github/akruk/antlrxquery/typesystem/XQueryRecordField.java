package com.github.akruk.antlrxquery.typesystem;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;

public record XQueryRecordField(TypeOrReference typeOrReference, boolean isRequired) {
    public sealed interface TypeOrReference 
        permits TypeOrReference.Type, 
                TypeOrReference.Reference 
    {
        public static record Type(AntlrQuerySequenceType type) 
            implements TypeOrReference { }
        public static record Reference(QualifiedName reference, Cardinality cardinality) 
            implements TypeOrReference { }
    }




    public AntlrQuerySequenceType resolveFieldType(final AntlrQueryTypeFactory typeFactory) {
        final var type = switch(this.typeOrReference) {
            case final TypeOrReference.Type t -> t.type;
            case final TypeOrReference.Reference r -> typeFactory.namedType(r.reference).type();
        };
        return isRequired? type : type.addOptionality();
    }

    @Override
    public String toString()
    {
        return switch(this.typeOrReference) {
            case final TypeOrReference.Reference r -> {
                if (isRequired) {
                    final Cardinality cardinality = r.cardinality;
                    yield r.reference.toString() + "^" + Cardinalities.stringify(cardinality);
                } else {
                    final Cardinality cardinality = r.cardinality;
                    yield r.reference.toString() + "^" + Cardinalities.stringify(Cardinalities.optionalize(cardinality));
                }
            }
            case final TypeOrReference.Type t -> {
                if (isRequired) {
                    yield t.type.toString();
                } else {
                    yield t.type.addOptionality().toString();
                }
            }
        };
    }
}
