package com.github.akruk.antlrxquery.semanticanalyzer;

import java.util.ArrayList;
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

    AndTrueImplication(TypeInContext andResult, List<ParseTree> andExprs, XQuerySemanticAnalyzer analyzer) {
        super(andResult, true);
        this.andResult = andResult;
        this.andEffectiveBooleanValues = andExprs;
        this.analyzer = analyzer;
    }

    @Override
    public void transform(XQuerySemanticContext context)
    {
        var errors = analyzer.getErrors();
        var preerrorcount = errors.size();
        for (var andExpr : andEffectiveBooleanValues) {
            var andEbv = andExpr.accept(analyzer);
            var ebv = context.resolveEffectiveBooleanValue(andEbv);
            context.currentScope().assume(ebv, new Assumption(ebv, true));
        }
        while (errors.size() != preerrorcount) {
            errors.removeLast();
        }
    }

    @Override
    public Implication remapTypes(Map<TypeInContext, TypeInContext> typeMapping)
    {
        TypeInContext remappedAndResult = typeMapping.get(andResult);
        return new AndTrueImplication(remappedAndResult, this.andEffectiveBooleanValues, this.analyzer);
    }

}
