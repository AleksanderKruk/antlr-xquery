package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQueryEnumItemTypeReference extends XQueryEnumItemType {
    private final Supplier<XQueryItemType> referenceSupplier;
    private XQueryEnumItemType referencedTypeCast;

    public XQueryEnumItemTypeReference(Supplier<XQueryItemType> referenceSupplier) {
        super(XQueryTypes.ANY_ITEM, List.of(), null, null, null, null, null, null, null);
        this.referenceSupplier = referenceSupplier;
    }

    private XQueryEnumItemType getReferencedType()  {
        if (referencedTypeCast == null) {
            var referencedType = referenceSupplier.get();
            referencedTypeCast = (XQueryEnumItemType) referencedType;
        }
        return referencedTypeCast;
    }

    @Override
    public XQueryTypes getType() {
        IXQueryEnumItemType type = getReferencedType();
        return type.getType();
    }

    @Override
    public List<XQuerySequenceType> getArgumentTypes() {
        return getReferencedType().getArgumentTypes();
    }
    @Override
    public XQuerySequenceType getArrayType() {
        return getReferencedType().getArrayType();
    }

    @Override
    public boolean equals(Object obj) {
        return getReferencedType().equals(obj);
    }

    @Override
    public Set<String> getElementNames() {
        return getReferencedType().getElementNames();
    }

    @Override
    public Collection<XQueryItemType> getItemTypes() {
        return getReferencedType().getItemTypes();
    }

    @Override
    public XQueryItemType getMapKeyType() {
        return getReferencedType().getMapKeyType();
    }

    @Override
    public XQuerySequenceType getMapValueType() {
        return getReferencedType().getMapValueType();
    }

    @Override
    public XQuerySequenceType getReturnedType() {
        return getReferencedType().getReturnedType();
    }

    @Override
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

}
