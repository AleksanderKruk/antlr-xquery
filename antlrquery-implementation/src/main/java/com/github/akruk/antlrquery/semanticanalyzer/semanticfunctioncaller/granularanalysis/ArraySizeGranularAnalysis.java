package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import java.util.List;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.ArrayType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.TupleType;

@DefaultQualifier(NonNull.class)
public class ArraySizeGranularAnalysis
        implements SemanticSymbolManager.GrainedFunctionCallAnalysis
{

    private final AntlrQueryTypeFactory typeFactory;

    public ArraySizeGranularAnalysis(
            AntlrQueryTypeFactory typeFactory) {

        this.typeFactory = typeFactory;
    }

    @Override
    public SemanticSymbolManager.FunctionCallAnalysis analyze(
            final List<SemanticSymbolManager.UsedArg> args,
            final @Nullable VisitingSemanticContext context,
            final @Nullable ParseTree functionBody,
            final AntlrQuerySemanticContext typeContext) {

        final AntlrQuerySequenceType array =
                args.getFirst().type().type;

        return SemanticSymbolManager.FunctionCallAnalysis.typeOnly(typeContext.typeInContext(
                analyze(array)));
    }

    private AntlrQuerySequenceType analyze(
            final AntlrQuerySequenceType array) {

        return typeFactory.one(
                arraySize(
                        array.itemType()));
    }

    private AntlrQueryItemType arraySize(
            final AntlrQueryItemType array) {

        return switch (array) {
            case ChoiceItemType choice ->
                    arraySize(choice);

            case ConcreteItemType concrete ->
                    arraySize(concrete);

            case NamedItemType(NamespaceResolver.QualifiedName reference) ->
                    arraySize(
                            typeFactory.guaranteedItemNamedType(
                                    reference,
                                    new IllegalStateException()));

            case NeverType _, NothingType _, AnyItemType _ ->
                    throw new IllegalStateException(
                            "Analysis should have prevented type: " + array +
                                    " from reaching analysis");
        };
    }

    private AntlrQueryItemType arraySize(
            final ChoiceItemType choice) {

        final ConcreteItemType[] itemTypes =
                choice.itemTypes();

        final NumericRange[] sizes =
                new NumericRange[itemTypes.length];

        for (int i = 0; i < itemTypes.length; i++) {
            final AntlrQueryItemType result =
                    arraySize(itemTypes[i]);

            if (!(result instanceof NumberType(
                    NumericRange range))) {

                throw new IllegalStateException(
                        "Expected integer result, got: " + result);
            }

            sizes[i] = range;
        }

        return typeFactory.itemNumber(Ranges.union(sizes));
    }

    private AntlrQueryItemType arraySize(
            final ConcreteItemType array) {

        return switch (array) {
            case ArrayLikeType arrayLike ->
                    arraySize(arrayLike);

            default ->
                    throw new IllegalStateException(
                            "Expected array type, got: " + array);
        };
    }

    private AntlrQueryItemType arraySize(
            final ArrayLikeType array) {

        return switch (array) {
            case ArrayType arrayType ->
                    arraySize(arrayType);

            case TupleType tupleType ->
                    arraySize(tupleType);
        };
    }

    private AntlrQueryItemType arraySize(
            final ArrayType array) {

        return typeFactory.itemNumber(
                Ranges.integers(Cardinalities.toNumericRange(array.cardinality())));
    }

    private AntlrQueryItemType arraySize(
            final TupleType tuple) {

        return typeFactory.itemNumber(
                NumericRange.of(
                        tuple.members().length));
    }
}
