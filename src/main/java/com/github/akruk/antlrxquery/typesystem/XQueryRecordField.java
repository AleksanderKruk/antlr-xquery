package com.github.akruk.antlrxquery.typesystem;

import java.util.List;
import java.util.StringJoiner;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.CardinalityInterval;



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
            case final Type t -> t.type;
            case final Reference r -> typeFactory.namedType(r.reference).type();
        };
        return isRequired? type : type.addOptionality();
    }

    @Override
    public String toString()
    {
        return switch(this.typeOrReference) {
            case final TypeOrReference.Reference r -> {
                if (isRequired) {
                    yield r.reference.toString();
                } else {
                    final Cardinality cardinality = r.cardinality;
                    final List<Cardinality.CardinalityInterval> intervals = cardinality.toIntervals();
                    if (intervals.size() == 1) {
                        yield r.reference.toString() + "^" + intervals.get(0).toCardinalityInterval();
                    }
                    final StringJoiner sj = new StringJoiner(" | ", "(", ")");
                    for (final CardinalityInterval interval : intervals) {
                        sj.add(interval.toCardinalityInterval());
                    }
                    final String cardinalityString = sj.toString();
                    yield r.reference.toString() + cardinalityString;
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
