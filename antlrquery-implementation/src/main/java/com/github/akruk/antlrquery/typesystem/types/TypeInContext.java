package com.github.akruk.antlrquery.typesystem.types;

import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticScope;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class TypeInContext {
    private final AntlrQueryTypeFactory typeFactory;
    public AntlrQuerySequenceType type;
    public final AntlrQuerySemanticContext context;
    public final AntlrQuerySemanticScope scope;

    public TypeInContext(AntlrQueryTypeFactory typeFactory, AntlrQuerySequenceType type, AntlrQuerySemanticContext context, AntlrQuerySemanticScope scope)
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
