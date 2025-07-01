package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;
import java.util.List;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQueryChoiceItemType implements XQueryItemType {
    Collection<XQueryItemType> itemTypes;

    @Override
    public boolean isFunction(XQuerySequenceType returnedType, List<XQuerySequenceType> argumentTypes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isFunction'");
    }

    @Override
    public boolean hasEffectiveBooleanValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasEffectiveBooleanValue'");
    }

    @Override
    public boolean itemtypeIsSubtypeOf(XQueryItemType obj) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'itemtypeIsSubtypeOf'");
    }

    @Override
    public boolean castableAs(XQueryItemType other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'castableAs'");
    }

    @Override
    public XQueryItemType unionMerge(XQueryItemType other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unionMerge'");
    }

    @Override
    public XQueryItemType intersectionMerge(XQueryItemType other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'intersectionMerge'");
    }

    @Override
    public XQueryItemType exceptionMerge(XQueryItemType other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exceptionMerge'");
    }

    @Override
    public boolean isValueComparableWith(XQueryItemType other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isValueComparableWith'");
    }



}
