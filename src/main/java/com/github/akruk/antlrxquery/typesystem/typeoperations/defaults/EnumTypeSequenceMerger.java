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

    @SuppressWarnings("rawtypes")
    private static final Function[][] mergedOccurences = new Function[XQueryOccurence.values().length][XQueryOccurence.values().length];
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zero =
        typeFactory -> (_) -> typeFactory.emptySequence();
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> one =
        typeFactory -> (item) -> typeFactory.one(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zeroOrOne
        = typeFactory -> (item) -> typeFactory.zeroOrOne(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> zeroOrMore
        = typeFactory -> (item) -> typeFactory.zeroOrMore(item);
    private static final Function<XQueryTypeFactory, Function<XQueryItemType, XQuerySequenceType>> oneOrMore
        = typeFactory -> (item) -> typeFactory.oneOrMore(item);

    static {
        final int zero_ = XQueryOccurence.ZERO.ordinal();
        final int one_ = XQueryOccurence.ONE.ordinal();
        final int zeroOrOne_ = XQueryOccurence.ZERO_OR_ONE.ordinal();
        final int zeroOrMore_ = XQueryOccurence.ZERO_OR_MORE.ordinal();
        final int oneOrMore_ = XQueryOccurence.ONE_OR_MORE.ordinal();

        mergedOccurences[zero_][zero_] = zero;
        mergedOccurences[zero_][one_] = one;
        mergedOccurences[zero_][zeroOrOne_] = zeroOrOne;
        mergedOccurences[zero_][zeroOrMore_] = zeroOrMore;
        mergedOccurences[zero_][oneOrMore_] = oneOrMore;

        mergedOccurences[one_][zero_] = one;
        mergedOccurences[one_][one_] = oneOrMore;
        mergedOccurences[one_][zeroOrOne_] = oneOrMore;
        mergedOccurences[one_][zeroOrMore_] = oneOrMore;
        mergedOccurences[one_][oneOrMore_] = oneOrMore;

        mergedOccurences[zeroOrOne_][zero_] = zeroOrOne;
        mergedOccurences[zeroOrOne_][one_] = oneOrMore;
        mergedOccurences[zeroOrOne_][zeroOrOne_] = zeroOrMore;
        mergedOccurences[zeroOrOne_][zeroOrMore_] = zeroOrMore;
        mergedOccurences[zeroOrOne_][oneOrMore_] = oneOrMore;

        mergedOccurences[zeroOrMore_][zero_] = zeroOrMore;
        mergedOccurences[zeroOrMore_][one_] = oneOrMore;
        mergedOccurences[zeroOrMore_][zeroOrOne_] = zeroOrMore;
        mergedOccurences[zeroOrMore_][zeroOrMore_] = zeroOrMore;
        mergedOccurences[zeroOrMore_][oneOrMore_] = oneOrMore;

        mergedOccurences[oneOrMore_][zero_] = oneOrMore;
        mergedOccurences[oneOrMore_][one_] = oneOrMore;
        mergedOccurences[oneOrMore_][zeroOrOne_] = oneOrMore;
        mergedOccurences[oneOrMore_][zeroOrMore_] = oneOrMore;
        mergedOccurences[oneOrMore_][oneOrMore_] = oneOrMore;
    }



    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public XQuerySequenceType merge(XQuerySequenceType type1, XQuerySequenceType type2) {
        var enumType1 = (XQueryEnumSequenceType) type1;
        var enumType2 = (XQueryEnumSequenceType) type2;
        var enumItemType1 = type1.getItemType();
        var enumItemType2 = type2.getItemType();
        var sequenceGetterWithoutFactory = mergedOccurences[enumType1.getOccurence().ordinal()][enumType2.getOccurence().ordinal()];
        final Function<XQueryItemType, XQuerySequenceType> sequenceGetter = (Function) sequenceGetterWithoutFactory.apply(typeFactory);
        boolean equalItemTypes = enumItemType1.equals(enumItemType2);
        return sequenceGetter.apply(equalItemTypes ? enumItemType1 : enumItemType2);
    }
}
