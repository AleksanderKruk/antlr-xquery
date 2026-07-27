package com.github.akruk.antlrxquery.typesystem.types;

import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticScope;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.Types;

public class TypeInContext {
    private final AntlrQueryTypeFactory typeFactory;
    public AntlrQuerySequenceType type;
    public final XQuerySemanticContext context;
    public final XQuerySemanticScope scope;

    public TypeInContext(AntlrQueryTypeFactory typeFactory, AntlrQuerySequenceType type, XQuerySemanticContext context, XQuerySemanticScope scope)
    {
        this.typeFactory = typeFactory;
        this.type = type;
        this.scope = scope;
        this.context = context;
    }

    public boolean isSubtypeOf(TypeInContext other)
    {
        return Types.isSubtype(typeFactory, type, other.type);
    }

    public boolean isSubtypeOf(AntlrQuerySequenceType other)
    {
        return Types.isSubtype(typeFactory, type, other);
    }

    public AntlrQuerySequenceType iteratorType()
    {
        return Types.iteratorType(typeFactory, type);
    }

    @Override
    public String toString() {
        return type.toString();
    }

}
