package com.github.akruk.antlrxquery.typesystem;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;
import com.github.akruk.antlrxquery.typesystem.typeoperations.Types;

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
