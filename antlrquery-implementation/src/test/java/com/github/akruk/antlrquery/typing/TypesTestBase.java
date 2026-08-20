package com.github.akruk.antlrquery.typing;

import com.github.akruk.Utils;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.MemoizedTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;

import java.util.Map;
import java.util.Set;

public class TypesTestBase {
    final AntlrQueryTypeFactory typeFactory = new MemoizedTypeFactory(Map.of(), Map.of());
    final AntlrQuerySequenceType boolean_ = typeFactory.boolean_();
    final AntlrQuerySequenceType string = typeFactory.string();
    final AntlrQuerySequenceType number = typeFactory.number(NumericRange.FULL);
    final AntlrQuerySequenceType anyNode = typeFactory.anyNode();
    final AntlrQuerySequenceType emptySequence = typeFactory.emptySequence();
    final AntlrQuerySequenceType stringSequenceOneOrMore = typeFactory.oneOrMore(typeFactory.itemString());
    final AntlrQuerySequenceType stringSequenceZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemString());
    final AntlrQuerySequenceType stringSequenceZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemString());
    final AntlrQuerySequenceType fooElement = typeFactory.element("", Set.of(new NamespaceResolver.QualifiedName("", "foo")));
    final AntlrQuerySequenceType anyMap = typeFactory.anyMap();
    final AntlrQuerySequenceType anyItem = typeFactory.anyItem();
    final AntlrQuerySequenceType zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());


    final AntlrQueryItemType itemError = typeFactory.itemError();
    final AntlrQueryItemType itemAnyFunction = typeFactory.itemAnyFunction();
    final AntlrQueryItemType itemAnyItem = typeFactory.itemAnyItem();
    final AntlrQueryItemType itemString = typeFactory.itemString();
    final AntlrQueryItemType itemNumber = typeFactory.itemNumber();
    final AntlrQueryItemType itemNonNegativeNumber = typeFactory.itemNumber(NumericRange.NON_NEGATIVE);
    final AntlrQueryItemType itemBoolean = typeFactory.itemBoolean();
    final AntlrQueryItemType itemAnyNode = typeFactory.itemAnyNode();
    final AntlrQueryItemType itemAnyMap = typeFactory.itemAnyMap();
    final AntlrQueryItemType itemAnyArray = typeFactory.itemAnyArray();
    final AntlrQueryItemType itemElementFoo = typeFactory.itemNodesFromGrammar(
            "", Set.of(new NamespaceResolver.QualifiedName("", "foo"))
    );
    final AntlrQueryItemType itemElementBar = typeFactory.itemNodesFromGrammar(
            "", Set.of(new NamespaceResolver.QualifiedName("", "bar"))
    );
    final AntlrQueryItemType itemABenum = typeFactory.itemEnum(Set.of("A", "B"));
    final AntlrQueryItemType itemABCenum = typeFactory.itemEnum(Set.of("A", "B", "C"));
    final AntlrQueryItemType itemABCDenum = typeFactory.itemEnum(Set.of("A", "B", "C", "D"));
    final RecordField requiredFooAnyItem =
            new RecordField("foo", new RecordField.TypeOrReference.Type(typeFactory.anyItem()), true);
    final RecordField requiredBarAnyItem =
            new RecordField("bar", new RecordField.TypeOrReference.Type(typeFactory.anyItem()), true);
    final RecordField requiredFooString =
            new RecordField("foo", new RecordField.TypeOrReference.Type(typeFactory.string()), true);
    final RecordField requiredBarString =
            new RecordField("bar", new RecordField.TypeOrReference.Type(typeFactory.string()), true);
    final AntlrQueryItemType itemRecordAny = typeFactory.itemRecord(
            Utils.linkedHashMap(Map.entry("foo", requiredFooAnyItem), Map.entry("bar", requiredBarAnyItem))
    );
    final AntlrQueryItemType itemRecordString = typeFactory.itemRecord(
            Utils.linkedHashMap(Map.entry("foo", requiredFooString), Map.entry("bar", requiredBarString))
    );
}
