package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;



public class Assumption
{
    public TypeInContext type;

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

    public Object value;

    public Assumption(TypeInContext type, Object value) {
        this.type = type;
        this.value = value;
    }
}
