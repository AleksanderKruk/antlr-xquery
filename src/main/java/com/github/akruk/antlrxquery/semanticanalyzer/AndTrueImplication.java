package com.github.akruk.antlrxquery.semanticanalyzer;

import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.Assumption;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.Implication;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.ValueImplication;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;

final class AndTrueImplication extends ValueImplication<Boolean> {
    private final TypeInContext andResult;
    private final List<ParseTree> andEffectiveBooleanValues;
    private final XQuerySemanticAnalyzer analyzer;

    AndTrueImplication(final TypeInContext andResult, final List<ParseTree> andExprs, final XQuerySemanticAnalyzer analyzer) {
        super(andResult, true);
        this.andResult = andResult;
        this.andEffectiveBooleanValues = andExprs;
        this.analyzer = analyzer;
    }

    @Override
    public void transform(final XQuerySemanticContext context)
    {
        final var errors = analyzer.getErrors();
        final var preerrorcount = errors.size();
        for (final var andExpr : andEffectiveBooleanValues) {
            final var andEbv = andExpr.accept(analyzer);
            final var ebv = context.resolveEffectiveBooleanValue(andEbv);
            context.currentScope().assume(ebv, new Assumption(ebv, true));
        }
        while (errors.size() != preerrorcount) {
            errors.removeLast();
        }
    }

    @Override
    public Implication remapTypes(final Map<TypeInContext, TypeInContext> typeMapping)
    {
        final TypeInContext remappedAndResult = typeMapping.get(andResult);
        return new AndTrueImplication(remappedAndResult, this.andEffectiveBooleanValues, this.analyzer);
    }

}
