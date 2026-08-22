package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import java.math.BigDecimal;
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
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.TypeInContext;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ArrayGetGranularAnalysis
        implements SemanticSymbolManager.GrainedAnalysis {

    private final AntlrQueryTypeFactory typeFactory;

    public ArrayGetGranularAnalysis(
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

        final AntlrQuerySequenceType positionArg = args.get(1).type().type;

        final List<AntlrQueryItemType> arrays = new ArrayList<>();
        if (input.itemType() instanceof ChoiceItemType(ConcreteItemType[] itemTypes)) {
            Collections.addAll(arrays, itemTypes);
        } else {
            arrays.add(input.itemType());
        }

        final Cardinality arrayCardinality =
                Cardinalities.union(
                        arrays.stream()
                                .map(ArrayLikeType.class::cast)
                                .map(ArrayLikeType::cardinality)
                                .toArray(Cardinality[]::new));

        if (!isPositionRangeCompatibleWithArrayLength(positionArg, arrayCardinality)) {
            return typeContext.typeInContext(typeFactory.neverType());
        }

        final AntlrQuerySequenceType memberType =
                Objects.requireNonNull(
                        Types.getMemberType(
                                typeFactory,
                                input.itemType()));

        return typeContext.typeInContext(memberType);
    }

    private static boolean isPositionRangeCompatibleWithArrayLength(
            final AntlrQuerySequenceType positionArg,
            final Cardinality arrayCardinality) {

        if (!(positionArg.itemType() instanceof NumberType(NumericRange positionRange))) {
            return true;
        }

        final NumericRange lengthRange = Cardinalities.toNumericRange(arrayCardinality);
        final NumericRange lengthIntegers = Ranges.integers(lengthRange);

        @MonotonicNonNull BigDecimal maxLength = null;
        for (NumericRange.Event e : lengthIntegers.events()) {
            if (e.value() instanceof NumericRange.FiniteBound fb
                    && e.type() == NumericRange.Type.END) {
                maxLength = fb.value();
            }
        }

        @MonotonicNonNull BigDecimal minPos = null;
        @MonotonicNonNull BigDecimal maxPos = null;
        for (NumericRange.Event e : positionRange.events()) {
            if (e.value() instanceof NumericRange.FiniteBound fb) {
                if (e.type() == NumericRange.Type.START) {
                    if (minPos == null || fb.value().compareTo(minPos) < 0) {
                        minPos = fb.value();
                    }
                } else {
                    if (maxPos == null || fb.value().compareTo(maxPos) > 0) {
                        maxPos = fb.value();
                    }
                }
            }
        }

        if (maxPos != null && maxPos.compareTo(BigDecimal.ONE) < 0) {
            return false;
        }

        return maxLength == null || minPos == null || minPos.compareTo(maxLength) <= 0;
    }


}
