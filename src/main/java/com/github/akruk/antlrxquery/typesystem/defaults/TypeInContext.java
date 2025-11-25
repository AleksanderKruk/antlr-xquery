package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticScope;

public class TypeInContext {
    public XQuerySequenceType type;
    public final XQuerySemanticContext context;
    public final XQuerySemanticScope scope;

    public TypeInContext(XQuerySequenceType type, XQuerySemanticContext context, XQuerySemanticScope scope)
    {
        this.type = type;
        this.scope = scope;
        this.context = context;
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

    @Override
    public String toString() {
        return type.toString();
    }

}
