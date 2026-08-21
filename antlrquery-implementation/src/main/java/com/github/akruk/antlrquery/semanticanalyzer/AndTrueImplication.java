package com.github.akruk.antlrquery.semanticanalyzer;

import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.Assumption;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.Implication;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.ValueImplication;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.visitors.AntlrQuerySemanticAnalyzer;
import com.github.akruk.antlrquery.typesystem.types.TypeInContext;

public final class AndTrueImplication extends ValueImplication<Boolean> {
    private final TypeInContext andResult;
    private final List<ParseTree> andEffectiveBooleanValues;
    private final AntlrQuerySemanticAnalyzer analyzer;

    public AndTrueImplication(final TypeInContext andResult, final List<ParseTree> andExprs, final AntlrQuerySemanticAnalyzer analyzer) {
        super(andResult, true);
        this.andResult = andResult;
        this.andEffectiveBooleanValues = andExprs;
        this.analyzer = analyzer;
    }

    @Override
    public void transform(final AntlrQuerySemanticContext context)
    {
        final var errors = analyzer.getErrors();
        final var preerrorcount = errors.size();
        for (final var andExpr : andEffectiveBooleanValues) {
            final TypeInContext andEbv = andExpr.accept(analyzer);
            assert andEbv != null;
            final TypeInContext ebv = context.resolveEffectiveBooleanValue(andEbv);
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
