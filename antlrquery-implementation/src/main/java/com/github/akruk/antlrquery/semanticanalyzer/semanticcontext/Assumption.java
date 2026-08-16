package com.github.akruk.antlrquery.semanticanalyzer.semanticcontext;

import com.github.akruk.antlrquery.typesystem.types.TypeInContext;



public class Assumption
{
    public final TypeInContext type;

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        return true;
        if (obj == null)
        return false;
        if (getClass() != obj.getClass())
        return false;
        Assumption other = (Assumption) obj;
        return type == other.type && value.equals(other.value);
    }

    public final Object value;

    public Assumption(TypeInContext type, Object value) {
        this.type = type;
        this.value = value;
    }
}
