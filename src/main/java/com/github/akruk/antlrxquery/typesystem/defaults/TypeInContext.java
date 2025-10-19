package com.github.akruk.antlrxquery.typesystem.defaults;

public class TypeInContext {
    public XQuerySequenceType type;

    public TypeInContext(XQuerySequenceType type)
    {
        this.type = type;
    }

    public boolean isSubtypeOf(TypeInContext other)
    {
        return type.isSubtypeOf(other.type);
    }

    public boolean isSubtypeOf(XQuerySequenceType other)
    {
        return type.isSubtypeOf(other);
    }

    public boolean itemtypeIsSubtypeOf(TypeInContext obj)
    {
        return type.itemtypeIsSubtypeOf(obj.type);
    }

    public boolean itemtypeIsSubtypeOf(XQuerySequenceType obj)
    {
        return type.itemtypeIsSubtypeOf(obj);
    }

    public XQuerySequenceType iteratorType()
    {
        return type.iteratorType();
    }

    public TypeInContext()
    {
    }

    @Override
    public String toString() {
        return type.toString();
    }

}
