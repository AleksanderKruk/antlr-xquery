package com.github.akruk.antlrxquery.typesystem;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;



public record XQueryRecordField(TypeOrReference typeOrReference, boolean isRequired) {
    public static enum FieldType {
        REFERENCE, TYPE
    }
    public static record TypeOrReference(
        FieldType fieldType,
        XQuerySequenceType type,
        QualifiedName reference
    ) {
        public static TypeOrReference type(XQuerySequenceType type) {
            return new TypeOrReference(FieldType.TYPE, type, null);
        }

        public static TypeOrReference reference(QualifiedName reference) {
            return new TypeOrReference(FieldType.REFERENCE, null, reference);
        }


    }
    public XQuerySequenceType resolveFieldType(XQueryTypeFactory typeFactory) {
        var type = switch(this.typeOrReference.fieldType) {
            case REFERENCE -> {
                yield typeFactory.namedType(typeOrReference.reference).type();
            }
            case TYPE -> {
                yield this.typeOrReference.type;
            }
        };
        return isRequired? type : type.addOptionality();
    }

    @Override
    public String toString()
    {
        return switch(this.typeOrReference.fieldType) {
            case REFERENCE -> {
                if (isRequired) {
                    yield typeOrReference.reference.toString();
                } else {
                    yield typeOrReference.reference + "?";
                }
            }
            case TYPE -> {
                if (isRequired) {
                    yield this.typeOrReference.type.toString();
                } else {
                    yield this.typeOrReference.type.addOptionality().toString();
                }
            }
        };
    }
}
