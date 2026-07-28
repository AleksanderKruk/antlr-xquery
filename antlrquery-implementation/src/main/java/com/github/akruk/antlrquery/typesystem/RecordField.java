package com.github.akruk.antlrquery.typesystem;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;

public record RecordField(
    @NonNull String name, 
    @NonNull TypeOrReference typeOrReference, 
    boolean isRequired) 
{
    public sealed interface TypeOrReference 
        permits TypeOrReference.Type, 
                TypeOrReference.Reference 
    {
        record Type(@NonNull AntlrQuerySequenceType type)
            implements TypeOrReference { }
        record Reference(
            @NonNull QualifiedName reference, 
            @NonNull Cardinality cardinality
        ) implements TypeOrReference { }
    }




    public AntlrQuerySequenceType resolveFieldType(final AntlrQueryTypeFactory typeFactory) {
        final AntlrQuerySequenceType type = switch(this.typeOrReference) {
            case final TypeOrReference.Type t -> t.type;
            case final TypeOrReference.Reference r ->
                    typeFactory.sequence(typeFactory.guaranteedItemNamedType(r.reference, new IllegalStateException()), r.cardinality);
        };
        return isRequired? type : Types.optionalize(typeFactory, type);
    }

}
