package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@DefaultQualifier(NonNull.class)
public class ArrayAppendGranularAnalysis
        implements SemanticSymbolManager.GrainedFunctionCallAnalysis
{

    private final AntlrQueryTypeFactory typeFactory;

    public ArrayAppendGranularAnalysis(final AntlrQueryTypeFactory typeFactory)
    {
        this.typeFactory = typeFactory;
    }



    @Override
    public SemanticSymbolManager.FunctionCallAnalysis analyze(
            List<SemanticSymbolManager.UsedArg> args,
            VisitingSemanticContext context,
            ParseTree functionBody,
            AntlrQuerySemanticContext typeContext)
    {
        final SemanticSymbolManager.UsedArg arrayArg = args.get(0);
        final SemanticSymbolManager.UsedArg memberArg = args.get(1);

        final AntlrQuerySequenceType arrayType = arrayArg.type().type;
        final AntlrQuerySequenceType appendedType = memberArg.type().type;

        final AntlrQueryItemType itemType =
                arrayAppend(arrayType.itemType(), appendedType);

        final AntlrQuerySequenceType type =
                typeFactory.sequence(itemType, arrayType.cardinality());

        return SemanticSymbolManager.FunctionCallAnalysis.typeOnly(typeContext.typeInContext(type));
    }

    private AntlrQueryItemType arrayAppend(
            final AntlrQueryItemType array,
            final AntlrQuerySequenceType appended) {

        return switch (array) {
            case ChoiceItemType choice ->
                    arrayAppend(choice, appended);

            case ConcreteItemType concrete ->
                    arrayAppend(concrete, appended);

            case NamedItemType named ->
                    arrayAppend(
                            typeFactory.guaranteedItemNamedType(
                                    named.reference(),
                                    new IllegalStateException()),
                            appended);

            case NeverType _, NothingType _, AnyItemType _ ->
                    throw new IllegalStateException(
                            "Analysis should have prevented type: " + array);
        };
    }

    private AntlrQueryItemType arrayAppend(
            final ChoiceItemType choice,
            final AntlrQuerySequenceType appended) {

        final ConcreteItemType[] itemTypes = choice.itemTypes();
        final ConcreteItemType[] newItemTypes =
                new ConcreteItemType[itemTypes.length];

        for (int i = 0; i < itemTypes.length; i++) {
            newItemTypes[i] = arrayAppend(itemTypes[i], appended);
        }

        return typeFactory.itemChoice(newItemTypes);
    }

    private ConcreteItemType arrayAppend(
            final ConcreteItemType array,
            final AntlrQuerySequenceType appended) {

        return switch (array) {
            case ArrayLikeType arrayLike ->
                    arrayAppend(arrayLike, appended);
            default ->
                    throw new IllegalStateException(
                            "Expected array-like type, got: " + array);
        };
    }

    private ConcreteItemType arrayAppend(
            final ArrayLikeType array,
            final AntlrQuerySequenceType appended) {

        return switch (array) {
            case ArrayLikeType.ArrayType(
                    AntlrQuerySequenceType memberType,
                    Cardinality cardinality) ->
                    arrayAppend(memberType, cardinality, appended);

            case ArrayLikeType.TupleType(
                    AntlrQuerySequenceType[] members) ->
                    arrayAppend(members, appended);
        };
    }

    private ConcreteItemType arrayAppend(
            final AntlrQuerySequenceType memberType,
            final Cardinality cardinality,
            final AntlrQuerySequenceType appended) {

        return (ConcreteItemType) typeFactory.itemArray(
                Types.addition(typeFactory, memberType, appended),
                Cardinalities.add(cardinality, Cardinality.ONE)
        );
    }

    private ConcreteItemType arrayAppend(
            final AntlrQuerySequenceType[] members,
            final AntlrQuerySequenceType appended) {

        final List<AntlrQuerySequenceType> newMembers =
                new ArrayList<>(members.length + 1);

        Collections.addAll(newMembers, members);
        newMembers.add(appended);

        return (ConcreteItemType) typeFactory.itemTuple(newMembers);
    }

}
