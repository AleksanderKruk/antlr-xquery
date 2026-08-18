package com.github.akruk.antlrquery.semanticanalyzer.semanticcontext;

import com.github.akruk.antlrquery.typesystem.types.TypeInContext;



public record Assumption(TypeInContext type, Object value)
{
}
