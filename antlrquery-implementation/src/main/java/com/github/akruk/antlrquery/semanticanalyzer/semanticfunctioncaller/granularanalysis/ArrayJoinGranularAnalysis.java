package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.TypeInContext;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ArrayJoinGranularAnalysis
        implements SemanticSymbolManager.GrainedAnalysis {

    private final AntlrQueryTypeFactory typeFactory;

    public ArrayJoinGranularAnalysis(
            final AntlrQueryTypeFactory typeFactory) {

        this.typeFactory = typeFactory;
    }

    @Override
    public TypeInContext analyze(
            final List<SemanticSymbolManager.UsedArg> args,
            final @Nullable VisitingSemanticContext context,
            final @Nullable ParseTree functionBody,
            final AntlrQuerySemanticContext typeContext) {

        final AntlrQuerySequenceType input = args.getFirst().type().type;

        if (input.itemType().equals(AntlrQueryItemType.NOTHING)) {
            return typeContext.typeInContext(
                    typeFactory.one(
                            typeFactory.itemTuple(List.of())));
        }

        final List<AntlrQueryItemType> arrays = new ArrayList<>();

        if (input.itemType() instanceof ChoiceItemType(
                ConcreteItemType[] itemTypes)) {
            Collections.addAll(arrays, itemTypes);
        } else {
            arrays.add(input.itemType());
        }

        final AntlrQuerySequenceType memberType =
                Objects.requireNonNull(
                        Types.getMemberType(
                                typeFactory,
                                input.itemType()));

        final Cardinality arrayCardinality =
                Cardinalities.union(
                        arrays.stream()
                                .map(ArrayLikeType.class::cast)
                                .map(ArrayLikeType::cardinality)
                                .toArray(Cardinality[]::new));

        final Cardinality resultCardinality =
                Cardinalities.multiply(
                        arrayCardinality,
                        input.cardinality());

        if (resultCardinality.isZero()) {
            return typeContext.typeInContext(typeFactory.tuple());
        }

        return typeContext.typeInContext(
                typeFactory.one(
                        typeFactory.itemArray(
                                memberType,
                                resultCardinality)));
    }
}
