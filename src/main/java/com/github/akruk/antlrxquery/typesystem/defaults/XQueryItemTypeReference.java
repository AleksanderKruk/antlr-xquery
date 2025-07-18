package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;


public class XQueryItemTypeReference extends XQueryItemType {
    private final Supplier<XQueryItemType> referenceSupplier;
    private XQueryItemType referencedTypeCast;

    public XQueryItemTypeReference(Supplier<XQueryItemType> referenceSupplier) {
        super();
        this.referenceSupplier = referenceSupplier;
    }

    private XQueryItemType getReferencedType()  {
        if (referencedTypeCast == null) {
            var referencedType = referenceSupplier.get();
            referencedTypeCast = (XQueryItemType) referencedType;
        }
        return referencedTypeCast;
    }

    public XQueryTypes getType() {
        XQueryItemType type = getReferencedType();
        return type.getType();
    }

    public List<XQuerySequenceType> getArgumentTypes() {
        return getReferencedType().getArgumentTypes();
    }

    public boolean equals(Object obj) {
        return getReferencedType().equals(obj);
    }

    public Set<String> getElementNames() {
        return getReferencedType().getElementNames();
    }

    public Collection<XQueryItemType> getItemTypes() {
        return getReferencedType().getItemTypes();
    }

    public XQueryItemType getMapKeyType() {
        return getReferencedType().getMapKeyType();
    }

    public XQuerySequenceType getMapValueType() {
        return getReferencedType().getMapValueType();
    }

    public XQuerySequenceType getReturnedType() {
        return getReferencedType().getReturnedType();
    }

    public String toString() {
        return getReferencedType().toString();
    }

    public boolean isFunction(XQuerySequenceType returnedType, List<XQuerySequenceType> argumentTypes) {
        return getReferencedType().isFunction(returnedType, argumentTypes);
    }

    public boolean hasEffectiveBooleanValue() {
        return getReferencedType().hasEffectiveBooleanValue();
    }

    public boolean itemtypeIsSubtypeOf(XQueryItemType obj) {
        return getReferencedType().itemtypeIsSubtypeOf(obj);
    }

    public boolean castableAs(XQueryItemType other) {
        return getReferencedType().castableAs(other);
    }

    public XQueryItemType alternativeMerge(XQueryItemType other) {
        return getReferencedType().alternativeMerge(other);
    }

    public XQueryItemType unionMerge(XQueryItemType other) {
        return getReferencedType().unionMerge(other);
    }

    public XQueryItemType intersectionMerge(XQueryItemType other) {
        return getReferencedType().intersectionMerge(other);
    }

    public XQueryItemType exceptionMerge(XQueryItemType other) {
        return getReferencedType().exceptionMerge(other);
    }

    public boolean isValueComparableWith(XQueryItemType other) {
        return getReferencedType().isValueComparableWith(other);
    }

    public XQuerySequenceType lookup(XQuerySequenceType keySpecifierType) {
        return getReferencedType().lookup(keySpecifierType);
    }

    public XQuerySequenceType getArrayMemberType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getArrayMemberType'");
    }

}
