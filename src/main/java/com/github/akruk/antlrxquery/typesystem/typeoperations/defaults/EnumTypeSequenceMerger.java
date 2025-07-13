// package com.github.akruk.antlrxquery.typesystem.typeoperations.defaults;

// import java.util.function.Function;

// import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
// import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
// import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumSequenceType;
// import com.github.akruk.antlrxquery.typesystem.defaults.XQueryOccurence;
// import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
// import com.github.akruk.antlrxquery.typesystem.typeoperations.TypeSequenceMerger;

// public class EnumTypeSequenceMerger implements TypeSequenceMerger {
//     private XQueryTypeFactory typeFactory;

//     public EnumTypeSequenceMerger(XQueryTypeFactory typeFactory) {
//         this.typeFactory = typeFactory;
//     }






//     @Override
//     public XQuerySequenceType sequenceMerge(XQuerySequenceType type1, XQuerySequenceType type2) {
//         var enumType1 = (XQueryEnumSequenceType) type1;
//         var enumType2 = (XQueryEnumSequenceType) type2;
//         return sequenceGetter.apply(equalItemTypes ? enumItemType1 : enumItemType2);
//     }
// }
