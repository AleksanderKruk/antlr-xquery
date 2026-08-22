package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import com.github.akruk.Utils;
import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@DefaultQualifier(NonNull.class)
public class ArrayMembersGranularAnalysis
        implements SemanticSymbolManager.GrainedFunctionCallAnalysis
{

    private final AntlrQueryTypeFactory typeFactory;

    public ArrayMembersGranularAnalysis(
            final AntlrQueryTypeFactory typeFactory) {

        this.typeFactory = typeFactory;
    }


    @Override
    public SemanticSymbolManager.FunctionCallAnalysis analyze(List<SemanticSymbolManager.UsedArg> args,
                                                              VisitingSemanticContext context,
                                                              ParseTree functionBody,
                                                              AntlrQuerySemanticContext typeContext)
    {
        final AntlrQuerySequenceType arrayType = args.getFirst().type().type;

        final AntlrQuerySequenceType result =
                arrayMembers(arrayType.itemType(), arrayType.cardinality());

        return SemanticSymbolManager.FunctionCallAnalysis.typeOnly(typeContext.typeInContext(result));
    }

    private AntlrQuerySequenceType arrayMembers(
            final AntlrQueryItemType itemType,
            final Cardinality cardinality) {

        return switch (itemType) {
            case ChoiceItemType choice -> arrayMembers(choice, cardinality);
            case ConcreteItemType concrete -> arrayMembers(concrete, cardinality);
            case NamedItemType named ->
                    arrayMembers(
                            typeFactory.guaranteedItemNamedType(
                                    named.reference(),
                                    new IllegalStateException()),
                            cardinality);

            case NeverType _, NothingType _, AnyItemType _ ->
                    throw new IllegalStateException(
                            "Analysis should have prevented type: " + itemType);
        };
    }

    private AntlrQuerySequenceType arrayMembers(
            final ChoiceItemType choice,
            final Cardinality cardinality) {

        return Types.union(
                typeFactory,
                Arrays.stream(choice.itemTypes())
                        .map(item -> arrayMembers(item, cardinality))
                        .toArray(AntlrQuerySequenceType[]::new));
    }

    private AntlrQuerySequenceType arrayMembers(
            final ConcreteItemType concrete,
            final Cardinality cardinality) {

        return switch (concrete) {
            case ArrayLikeType array ->
                    arrayMembers(array, cardinality);

            default ->
                    throw new IllegalStateException(
                            "Expected array-like type, got: " + concrete);
        };
    }

    private AntlrQuerySequenceType arrayMembers(
            final ArrayLikeType array,
            final Cardinality cardinality) {

        return switch (array) {
            case ArrayLikeType.ArrayType(
                    AntlrQuerySequenceType memberType,
                    Cardinality _) -> {

                final AntlrQuerySequenceType valueType =
                        arrayMembers(memberType);

                yield typeFactory.sequence(
                        valueType.itemType(),
                        Cardinalities.multiply(
                                cardinality,
                                valueType.cardinality()));
            }

            case ArrayLikeType.TupleType tuple ->
                    arrayMembers(tuple, cardinality);
        };
    }

    private AntlrQuerySequenceType arrayMembers(
            final ArrayLikeType.TupleType tuple,
            final Cardinality cardinality) {

        final AntlrQuerySequenceType[] members = tuple.members();

        if (members.length == 0) {
            return typeFactory.emptySequence();
        }

        final AntlrQuerySequenceType memberType =
                Types.union(typeFactory, members);

        return typeFactory.sequence(
                typeFactory.itemRecord(Utils.linkedHashMap(
                        Map.entry("value", new RecordField("value", new RecordField.TypeOrReference.Type(memberType), true)))
                ),
                cardinality);
    }

    private AntlrQuerySequenceType arrayMembers(
            final AntlrQuerySequenceType memberType) {

        return typeFactory.sequence(
                typeFactory.itemRecord(Utils.linkedHashMap(
                        Map.entry("value", new RecordField("value", new RecordField.TypeOrReference.Type(memberType), true)))
                ),
                Cardinality.ONE);
    }

}
