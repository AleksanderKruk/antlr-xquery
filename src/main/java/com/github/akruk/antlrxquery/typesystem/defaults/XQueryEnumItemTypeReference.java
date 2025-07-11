package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQueryEnumItemTypeReference implements XQueryItemType {
    private final Supplier<XQueryItemType> referenceSupplier;
    private XQueryItemType referencedType;

    public XQueryEnumItemTypeReference(Supplier<XQueryItemType> referenceSupplier) {
        this.referenceSupplier = referenceSupplier;
    }

    public boolean isFunction(XQuerySequenceType returnedType, List<XQuerySequenceType> argumentTypes) {
        if (referencedType == null)
            referencedType = referenceSupplier.get();
        return referencedType.isFunction(returnedType, argumentTypes);
    }

    public boolean hasEffectiveBooleanValue() {
        if (referencedType == null)
            referencedType = referenceSupplier.get();
        return referencedType.hasEffectiveBooleanValue();
    }

    public boolean itemtypeIsSubtypeOf(XQueryItemType obj) {
        if (referencedType == null)
            referencedType = referenceSupplier.get();
        return referencedType.itemtypeIsSubtypeOf(obj);
    }

    public boolean castableAs(XQueryItemType other) {
        if (referencedType == null)
            referencedType = referenceSupplier.get();
        return referencedType.castableAs(other);
    }

    public XQueryItemType alternativeMerge(XQueryItemType other) {
        if (referencedType == null)
            referencedType = referenceSupplier.get();
        return referencedType.alternativeMerge(other);
    }

    public XQueryItemType unionMerge(XQueryItemType other) {
        if (referencedType == null)
            referencedType = referenceSupplier.get();
        return referencedType.unionMerge(other);
    }

    public XQueryItemType intersectionMerge(XQueryItemType other) {
        if (referencedType == null)
            referencedType = referenceSupplier.get();
        return referencedType.intersectionMerge(other);
    }

    public XQueryItemType exceptionMerge(XQueryItemType other) {
        if (referencedType == null)
            referencedType = referenceSupplier.get();
        return referencedType.exceptionMerge(other);
    }

    public boolean isValueComparableWith(XQueryItemType other) {
        if (referencedType == null)
            referencedType = referenceSupplier.get();
        return referencedType.isValueComparableWith(other);
    }

}
