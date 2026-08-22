package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import java.util.Arrays;
import java.util.List;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.ArrayType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.TupleType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ArrayFlattenGranularAnalysis
        implements SemanticSymbolManager.GrainedFunctionCallAnalysis {

    private final AntlrQueryTypeFactory typeFactory;

    public ArrayFlattenGranularAnalysis(
            final AntlrQueryTypeFactory typeFactory) {

        this.typeFactory = typeFactory;
    }

    @Override
    public SemanticSymbolManager.FunctionCallAnalysis analyze(
            final List<SemanticSymbolManager.UsedArg> args,
            final @Nullable VisitingSemanticContext context,
            final @Nullable ParseTree functionBody,
            final AntlrQuerySemanticContext typeContext) {

        final AntlrQuerySequenceType input =
                args.getFirst().type().type;

        var type = typeContext.typeInContext(flatten(input));
        return SemanticSymbolManager.FunctionCallAnalysis.typeOnly(type);
    }

    /*
     * Flatten the input sequence.
     *
     * The cardinality of the input describes how many arrays
     * are present in the input sequence. Each array contributes
     * the cardinality obtained by flattening one array item.
     */
    private AntlrQuerySequenceType flatten(
            final AntlrQuerySequenceType input) {

        final AntlrQuerySequenceType flattened =
                flatten(input.itemType());

        return typeFactory.sequence(
                flattened.itemType(),
                Cardinalities.multiply(
                        flattened.cardinality(),
                        input.cardinality()));
    }

    /*
     * Flatten one item.
     */
    private AntlrQuerySequenceType flatten(
            final AntlrQueryItemType item) {

        return switch (item) {
            case ChoiceItemType choice ->
                    flatten(choice);

            case ConcreteItemType concrete ->
                    flatten(concrete);

            case NamedItemType(
                    NamespaceResolver.QualifiedName reference) ->
                    flatten(
                            typeFactory.guaranteedItemNamedType(
                                    reference,
                                    new IllegalStateException()));

            case NothingType _ ->
                    typeFactory.emptySequence();

            case NeverType _, AnyItemType _ ->
                    throw new IllegalStateException(
                            "Analysis should have prevented type: " +
                                    item +
                                    " from reaching analysis");
        };
    }

    /*
     * A choice is flattened branch by branch and then unionized.
     */
    private AntlrQuerySequenceType flatten(
            final ChoiceItemType choice) {


        AntlrQuerySequenceType[] flattenedTypes = Arrays.stream(choice.itemTypes())
                .map(this::flatten)
                .toArray(AntlrQuerySequenceType[]::new);

        return Types.union(typeFactory, flattenedTypes);
    }

    /*
     * Flattening a concrete item only has special semantics
     * for array-like types. Ordinary atomic/item types contribute
     * exactly one item.
     */
    private AntlrQuerySequenceType flatten(final ConcreteItemType item) {
        return switch (item) {
            case ArrayLikeType arrayLike -> flatten(arrayLike);
            default -> typeFactory.one(item);
        };
    }

    private AntlrQuerySequenceType flatten(final ArrayLikeType array) {
        return switch (array) {
            case ArrayType arrayType -> flatten(arrayType);
            case TupleType tupleType -> flatten(tupleType);
        };
    }

    /*
     * One array member may itself be a sequence.
     *
     * memberType() describes the sequence contributed by one
     * member of the array. Its cardinality therefore has to be
     * multiplied by the cardinality of the array itself.
     */
    private AntlrQuerySequenceType flatten(final ArrayType array) {
        final AntlrQuerySequenceType member = flatten(array.memberType());
        return typeFactory.sequence(
                member.itemType(),
                Cardinalities.multiply(member.cardinality(), array.cardinality()));
    }

    /*
     * A tuple is a fixed concatenation of its members.
     *
     * Every member must itself be flattened. In particular,
     * tuple members may contain nested arrays or tuples.
     */
    private AntlrQuerySequenceType flatten(final TupleType tuple) {
        if (tuple.members().length == 0) {
            return typeFactory.emptySequence();
        }
        AntlrQuerySequenceType[] flattenedTypes = Arrays.stream(tuple.members())
                .map(this::flatten)
                .toArray(AntlrQuerySequenceType[]::new);
        return Types.addition(typeFactory, flattenedTypes);
    }
}
