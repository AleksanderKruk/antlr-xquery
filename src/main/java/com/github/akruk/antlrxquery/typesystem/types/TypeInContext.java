package com.github.akruk.antlrxquery.typesystem.types;

import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticScope;

public class TypeInContext {
    public AntlrQuerySequenceType type;
    public final XQuerySemanticContext context;
    public final XQuerySemanticScope scope;

    public TypeInContext(AntlrQuerySequenceType type, XQuerySemanticContext context, XQuerySemanticScope scope)
    {
        this.type = type;
        this.scope = scope;
        this.context = context;
    }

    public boolean isSubtypeOf(TypeInContext other)
    {
        return type.isSubtypeOf(other.type);
    }

    public boolean isSubtypeOf(AntlrQuerySequenceType other)
    {
        return type.isSubtypeOf(other);
    }

    public boolean itemtypeIsSubtypeOf(TypeInContext obj)
    {
        return type.itemtypeIsSubtypeOf(obj.type);
    }

    public boolean itemtypeIsSubtypeOf(AntlrQuerySequenceType obj)
    {
        return type.itemtypeIsSubtypeOf(obj);
    }

    public AntlrQuerySequenceType iteratorType()
    {
        return type.iteratorType();
    }

    @Override
    public String toString() {
        return type.toString();
    }

}
