package com.github.akruk.antlrxquery.typesystem.typeoperations.defaults;

import java.util.function.Function;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumSequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryOccurence;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.TypeSequenceMerger;

public class EnumTypeSequenceMerger implements TypeSequenceMerger {
    private XQueryTypeFactory typeFactory;

    public EnumTypeSequenceMerger(XQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    private static final Function[][] mergedOccurences = new Function[XQueryOccurence.values().length][XQueryOccurence.values().length];
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zero =
        typeFactory -> (item) -> typeFactory.emptySequence();
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> one =
        typeFactory -> (item) -> typeFactory.one(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zeroOrOne
        = typeFactory -> (item) -> typeFactory.zeroOrOne(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zeroOrMore
        = typeFactory -> (item) -> typeFactory.zeroOrMore(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> oneOrMore
        = typeFactory -> (item) -> typeFactory.oneOrMore(item);

    static {
        mergedOccurences[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ZERO.ordinal()] = zero;
        mergedOccurences[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ONE.ordinal()] = one;
        mergedOccurences[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = zeroOrOne;
        mergedOccurences[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = oneOrMore;

        mergedOccurences[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ZERO.ordinal()] = one;
        mergedOccurences[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ONE.ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = oneOrMore;

        mergedOccurences[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ZERO
                .ordinal()] = zeroOrOne;
        mergedOccurences[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ONE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_ONE.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = oneOrMore;

        mergedOccurences[XQueryOccurence.ZERO_OR_MORE.ordinal()][XQueryOccurence.ZERO
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_MORE.ordinal()][XQueryOccurence.ONE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_MORE.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_MORE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = zeroOrMore;
        mergedOccurences[XQueryOccurence.ZERO_OR_MORE.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = oneOrMore;

        mergedOccurences[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ZERO
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ONE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ZERO_OR_ONE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ZERO_OR_MORE
                .ordinal()] = oneOrMore;
        mergedOccurences[XQueryOccurence.ONE_OR_MORE.ordinal()][XQueryOccurence.ONE_OR_MORE
                .ordinal()] = oneOrMore;
    }



    @Override
    public XQuerySequenceType merge(XQuerySequenceType type1, XQuerySequenceType type2) {
        var enumType1 = (XQueryEnumSequenceType) type1;
        var enumType2 = (XQueryEnumSequenceType) type2;
        var enumItemType1 = type1.getItemType();
        var enumItemType2 = type2.getItemType();
        var sequenceGetterWithoutFactory = mergedOccurences[enumType1.getOccurence().ordinal()][enumType2.getOccurence().ordinal()];
        Function<XQueryItemType, XQuerySequenceType> sequenceGetter = (Function) sequenceGetterWithoutFactory.apply(typeFactory);
        boolean equalItemTypes = enumItemType1.equals(enumItemType2);
        return sequenceGetter.apply(equalItemTypes ? enumItemType1 : enumItemType2);
    }
}
