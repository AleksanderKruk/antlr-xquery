package com.github.akruk.antlrquery.typing;

import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.Test;

import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.RecordField.TypeOrReference;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.MemoizedTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;

import java.util.*;

import static org.junit.Assert.*;

public class AntlrQueryTypesTest {

    final AntlrQueryTypeFactory typeFactory = new MemoizedTypeFactory(Map.of(), Map.of());
    final AntlrQuerySequenceType error = typeFactory.error();
    final AntlrQuerySequenceType boolean_ = typeFactory.boolean_();
    final AntlrQuerySequenceType string = typeFactory.string();
    final AntlrQuerySequenceType number = typeFactory.number(NumericRange.FULL);
    final AntlrQuerySequenceType anyNode = typeFactory.anyNode();
    final AntlrQuerySequenceType emptySequence = typeFactory.emptySequence();
    final AntlrQuerySequenceType stringSequenceOneOrMore = typeFactory.oneOrMore(typeFactory.itemString());
    final AntlrQuerySequenceType stringSequenceZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemString());
    final AntlrQuerySequenceType stringSequenceZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemString());
    final AntlrQuerySequenceType numberSequenceOneOrMore = typeFactory.oneOrMore(typeFactory.itemNumber());
    final AntlrQuerySequenceType numberSequenceZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemNumber());
    final AntlrQuerySequenceType numberSequenceZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemNumber());
    final AntlrQuerySequenceType fooElement = typeFactory.element("", Set.of(new QualifiedName("", "foo")));
    final AntlrQuerySequenceType barElement = typeFactory.element("", Set.of(new QualifiedName("", "bar")));
    final AntlrQuerySequenceType anyArray = typeFactory.anyArray();
    final AntlrQuerySequenceType anyMap = typeFactory.anyMap();
    final AntlrQuerySequenceType anyItem = typeFactory.anyItem();
    final AntlrQuerySequenceType zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
    final AntlrQuerySequenceType anyFunction = typeFactory.anyFunction();


    // final RecordField requiredNumber =
    //     new RecordField(new TypeOrReference.Type(typeFactory.number()), true);


    final AntlrQueryItemType itemError = typeFactory.itemError();
    final AntlrQueryItemType itemAnyFunction = typeFactory.itemAnyFunction();
    final AntlrQueryItemType itemAnyItem = typeFactory.itemAnyItem();
    final AntlrQueryItemType itemString = typeFactory.itemString();
    final AntlrQueryItemType itemNumber = typeFactory.itemNumber();
    final AntlrQueryItemType itemBoolean = typeFactory.itemBoolean();
    final AntlrQueryItemType itemAnyNode = typeFactory.itemAnyNode();
    final AntlrQueryItemType itemAnyMap = typeFactory.itemAnyMap();
    final AntlrQueryItemType itemAnyArray = typeFactory.itemAnyArray();
    final AntlrQueryItemType itemElementFoo = typeFactory.itemElement(
            "", Set.of(new QualifiedName("", "foo"))
    );
    final AntlrQueryItemType itemElementBar = typeFactory.itemElement(
            "", Set.of(new QualifiedName("", "bar"))
    );
    final AntlrQueryItemType itemABenum = typeFactory.itemEnum(Set.of("A", "B"));
    final AntlrQueryItemType itemABCenum = typeFactory.itemEnum(Set.of("A", "B", "C"));
    final AntlrQueryItemType itemABCDenum = typeFactory.itemEnum(Set.of("A", "B", "C", "D"));
    final RecordField requiredFooAnyItem =
        new RecordField("foo", new TypeOrReference.Type(typeFactory.anyItem()), true);
    final RecordField requiredBarAnyItem =
        new RecordField("bar", new TypeOrReference.Type(typeFactory.anyItem()), true);
    final RecordField requiredFooString =
        new RecordField("foo", new TypeOrReference.Type(typeFactory.string()), true);
    final RecordField requiredBarString =
        new RecordField("bar", new TypeOrReference.Type(typeFactory.string()), true);
    final AntlrQueryItemType itemRecordAny = typeFactory.itemRecord(
            new LinkedHashMap<>(Map.of("foo", requiredFooAnyItem, "bar", requiredBarAnyItem))
    );
    final AntlrQueryItemType itemRecordString = typeFactory.itemRecord(
            new LinkedHashMap<>(Map.of("foo", requiredFooString, "bar", requiredBarString))
    );

    @Test
    public void stringDirectEquality() {
        assertEquals(string, string);
        assertNotEquals(string, number);
        assertNotEquals(string, emptySequence);
        assertNotEquals(string, stringSequenceOneOrMore);
        assertNotEquals(string, stringSequenceZeroOrMore);
        assertNotEquals(string, stringSequenceZeroOrOne);
    }

    @Test
    public void stringIsSubtypeOf() {
        assertTrue (Types.isSubtype(typeFactory, string, string));
        assertFalse(Types.isSubtype(typeFactory, string, number));
        assertFalse(Types.isSubtype(typeFactory, string, emptySequence));
        assertTrue (Types.isSubtype(typeFactory, string, stringSequenceOneOrMore));
        assertTrue (Types.isSubtype(typeFactory, string, stringSequenceZeroOrMore));
        assertTrue (Types.isSubtype(typeFactory, string, stringSequenceZeroOrOne));
    }


    @Test
    public void emptySequenceSubtyping() {
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.emptySequence()));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.anyItem()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemAnyItem)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemAnyItem)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemAnyItem)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.string()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemString)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemString)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemString)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.number(NumericRange.FULL)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemNumber)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemNumber)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemNumber)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.boolean_()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemBoolean)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemBoolean)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemBoolean)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.anyNode()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemAnyNode)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemAnyNode)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemAnyNode)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, fooElement));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemElementFoo)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemElementFoo)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemElementFoo)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.anyMap()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemAnyMap)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemAnyMap)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemAnyMap)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.map(itemString, typeFactory.anyItem())));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.map(itemNumber, typeFactory.anyItem())));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.map(itemBoolean, typeFactory.anyItem())));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.anyArray()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemAnyArray)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemAnyArray)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemAnyArray)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.anyFunction()));

        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemAnyFunction)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemAnyFunction)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemAnyFunction)));
        // assertTrue(Types.isSubtypeOf(typeFactory, emptySequence, typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(Types.isSubtypeOf(typeFactory, emptySequence, typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(Types.isSubtypeOf(typeFactory, emptySequence, function(T) as R))
        // assertTrue(Types.isSubtypeOf(typeFactory, emptySequence, function(T1, T2) as R))
    }

    @Test
    public void numberItemSubtyping() {
        final var tested = typeFactory.itemNumber();

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementBar));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void stringItemSubtyping() {
        final var tested = typeFactory.itemString();

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype (typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementBar));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertTrue(ItemTypes.isSubtype (typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void booleanItemSubtyping() {
        final var tested = typeFactory.itemBoolean();

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementBar));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }


    @Test
    public void namedElementItemSubtyping() {
        final var tested = itemElementFoo;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementBar));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void anyMapItemSubtyping() {
        final var tested = itemAnyMap;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        // var itemRecord = typeFactory.itemRec(itemString, typeFactory.anyItem());
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void errorItemSubtyping() {
        final var tested = itemError;

        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void anyItemSubtyping() {
        final var tested = itemAnyItem;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));
        
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }


    @Test
    public void anyNodeItemSubtyping() {
        final var tested = itemAnyNode;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void enumItemSubtyping() {
        final var tested = itemABCenum;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, itemABCDenum, itemABCenum));
    }


    @Test
    public void anyFunctionItemSubtyping() {
        final var tested = itemAnyFunction;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
    }



    @Test
    public void anyArrayItemSubtyping() {
        final var tested = typeFactory.itemAnyArray();

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.string())));


        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.oneOrMore(itemNumber)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL), typeFactory.number(NumericRange.FULL)))));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
    }

    @Test
    public void typedArrayItemSubtyping() {
        final var tested = typeFactory.itemArray(typeFactory.string(), Cardinality.ZERO_OR_MORE);

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.string())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.number(NumericRange.FULL))));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.string(), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.oneOrMore(itemNumber)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.number(NumericRange.FULL), List.of(typeFactory.number(NumericRange.FULL)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL), typeFactory.number(NumericRange.FULL)))));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
    }


    @Test
    public void typedMapItemSubtyping() {
        final var tested = typeFactory.itemMap(itemString, typeFactory.string());
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemAnyItem, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemAnyItem, typeFactory.number(NumericRange.FULL))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.number(NumericRange.FULL))));


        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.string(), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.anyItem()))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.oneOrMore(itemString), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.number(NumericRange.FULL), List.of(typeFactory.number(NumericRange.FULL)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()))));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
    }


    @Test
    public void typedFunctionSubtyping() {
        final var string1_$string1 = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()));
        final var noargFunction = typeFactory.itemFunction(typeFactory.string(), List.of());
        final var $2argfunction = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()));
        final var numberToItem = typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number(NumericRange.FULL)));
        final var numberToString = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL)));

        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemElementFoo));

        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, noargFunction, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, $2argfunction, itemAnyMap));

        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemMap(itemAnyItem, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemMap(itemString, typeFactory.string())));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemMap(itemAnyItem, typeFactory.number(NumericRange.FULL))));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemMap(itemNumber, typeFactory.number(NumericRange.FULL))));


        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemAnyArray));
        assertTrue(ItemTypes.isSubtype(typeFactory, numberToItem, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemArray(typeFactory.string(), Cardinality.ZERO_OR_MORE)));
        assertTrue(ItemTypes.isSubtype(typeFactory, numberToString, typeFactory.itemArray(typeFactory.string(), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));

        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.oneOrMore(itemString), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.number(NumericRange.FULL), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()))));

        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemABCDenum));
    }


    @Test
    public void recordItemSubtyping() {
        final var tested = itemRecordAny;
        final var numberToItem = typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number(NumericRange.FULL)));
        final var numberToString = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemAnyItem, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.string())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemAnyItem, typeFactory.number(NumericRange.FULL))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.number(NumericRange.FULL))));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertTrue(ItemTypes.isSubtype(typeFactory, numberToItem, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.string(), Cardinality.ZERO_OR_MORE)));
        assertTrue(ItemTypes.isSubtype(typeFactory, numberToString, typeFactory.itemArray(typeFactory.string(), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.anyItem()))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertTrue(ItemTypes.isSubtype(typeFactory, itemRecordString, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, itemRecordString,
                typeFactory.itemFunction(typeFactory.oneOrMore(itemString), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.number(NumericRange.FULL), List.of(typeFactory.number(NumericRange.FULL)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested,
                typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()))));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertTrue(ItemTypes.isSubtype(typeFactory, itemRecordString, itemRecordAny));
        assertTrue(ItemTypes.isSubtype(typeFactory, itemRecordString, itemRecordString));
        assertFalse(ItemTypes.isSubtype(typeFactory, itemRecordAny, itemRecordString));

        final RecordField foostr = new RecordField("foo", new TypeOrReference.Type(typeFactory.string()), true);
        final RecordField foonum = new RecordField("num", new TypeOrReference.Type(typeFactory.number(NumericRange.FULL)), true);
        final RecordField bar = new RecordField("bar", new TypeOrReference.Type(typeFactory.string()), true);
        final RecordField hoo = new RecordField("hoo", new TypeOrReference.Type(typeFactory.string()), true);
        final var itemFooBarHoo = typeFactory.itemRecord(
                new LinkedHashMap<>(Map.of("foo", foostr, "bar", bar, "hoo", hoo)));
        final var itemFooBarNum = typeFactory
                .itemRecord(
                        new LinkedHashMap<>(Map.of("foo", foonum, "bar", bar))
                );
        assertTrue(ItemTypes.isSubtype(typeFactory, itemRecordString, itemFooBarHoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, itemFooBarHoo, itemRecordString));
        assertFalse(ItemTypes.isSubtype(typeFactory, itemRecordString, itemFooBarNum));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
    }

    @Test
    public void typeAlternatives() {
        final var empty = typeFactory.emptySequence();
        final var numberZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemNumber());
        final var numberZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemNumber());
        final var numberOneOrMore = typeFactory.oneOrMore(typeFactory.itemNumber());
        final var $00 = Types.union(typeFactory, empty, empty);
        final var $01 = Types.union(typeFactory, empty, number);
        final var $0_zeroOrOne = Types.union(typeFactory, empty, numberZeroOrOne);
        final var $0_zeroOrMore = Types.union(typeFactory, empty, numberZeroOrMore);
        final var $0_oneOrMore = Types.union(typeFactory, empty, numberOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, numberZeroOrOne);
        assertEquals($0_zeroOrOne, numberZeroOrOne);
        assertEquals($0_zeroOrMore, numberZeroOrMore);
        assertEquals($0_oneOrMore, numberZeroOrMore);

        final var $10 = Types.union(typeFactory, number, empty);
        final var $11 = Types.union(typeFactory, number, number);
        final var $1_zeroOrOne = Types.union(typeFactory, number, numberZeroOrOne);
        final var $1_zeroOrMore = Types.union(typeFactory, number, numberZeroOrMore);
        final var $1_oneOrMore = Types.union(typeFactory, number, numberOneOrMore);

        assertEquals($10, numberZeroOrOne);
        assertEquals($11, number);
        assertEquals($1_zeroOrOne, numberZeroOrOne);
        assertEquals($1_zeroOrMore, numberZeroOrMore);
        assertEquals($1_oneOrMore, numberOneOrMore);

        final var $zeroOrOne_0 = Types.union(typeFactory, numberZeroOrOne, empty);
        final var $zeroOrOne_1 = Types.union(typeFactory, numberZeroOrOne, number);
        final var $zeroOrOne_zeroOrOne = Types.union(typeFactory, numberZeroOrOne, numberZeroOrOne);
        final var $zeroOrOne_zeroOrMore = Types.union(typeFactory, numberZeroOrOne, numberZeroOrMore);
        final var $zeroOrOne_oneOrMore = Types.union(typeFactory, numberZeroOrOne, numberOneOrMore);

        assertEquals($zeroOrOne_0, numberZeroOrOne);
        assertEquals($zeroOrOne_1, numberZeroOrOne);
        assertEquals($zeroOrOne_zeroOrOne, numberZeroOrOne);
        assertEquals($zeroOrOne_zeroOrMore, numberZeroOrMore);
        assertEquals($zeroOrOne_oneOrMore, numberZeroOrMore);

        final var $zeroOrMore_0 = Types.union(typeFactory, numberZeroOrMore, empty);
        final var $zeroOrMore_1 = Types.union(typeFactory, numberZeroOrMore, number);
        final var $zeroOrMore_zeroOrOne = Types.union(typeFactory, numberZeroOrMore, numberZeroOrOne);
        final var $zeroOrMore_zeroOrMore = Types.union(typeFactory, numberZeroOrMore, numberZeroOrMore);
        final var $zeroOrMore_oneOrMore = Types.union(typeFactory, numberZeroOrMore, numberOneOrMore);

        assertEquals($zeroOrMore_0, numberZeroOrMore);
        assertEquals($zeroOrMore_1, numberZeroOrMore);
        assertEquals($zeroOrMore_zeroOrOne, numberZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, numberZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, numberZeroOrMore);

        final var $oneOrMore_0 = Types.union(typeFactory, numberOneOrMore, empty);
        final var $oneOrMore_1 = Types.union(typeFactory, numberOneOrMore, number);
        final var $oneOrMore_zeroOrOne = Types.union(typeFactory, numberOneOrMore, numberZeroOrOne);
        final var $oneOrMore_zeroOrMore = Types.union(typeFactory, numberOneOrMore, numberZeroOrMore);
        final var $oneOrMore_oneOrMore = Types.union(typeFactory, numberOneOrMore, numberOneOrMore);

        assertEquals($oneOrMore_0, numberZeroOrMore);
        assertEquals($oneOrMore_1, numberOneOrMore);
        assertEquals($oneOrMore_zeroOrOne, numberZeroOrMore);
        assertEquals($oneOrMore_zeroOrMore, numberZeroOrMore);
        assertEquals($oneOrMore_oneOrMore, numberOneOrMore);
    }


    @Test
    public void unionNodeMerging() {
        final var empty = typeFactory.emptySequence();
        final var node = typeFactory.anyNode();
        final var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        final var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());

        final var $00 = Types.union(typeFactory, empty, empty);
        final var $01 = Types.union(typeFactory, empty, node);
        final var $0_zeroOrOne = Types.union(typeFactory, empty, nodeZeroOrOne);
        final var $0_zeroOrMore = Types.union(typeFactory, empty, nodeZeroOrMore);
        final var $0_oneOrMore = Types.union(typeFactory, empty, nodeOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, node);
        assertEquals($0_zeroOrOne, nodeZeroOrOne);
        assertEquals($0_zeroOrMore, nodeZeroOrMore);
        assertEquals($0_oneOrMore, nodeOneOrMore);

        final var $10 = Types.union(typeFactory, node, empty);
        final var $11 = Types.union(typeFactory, node, node);
        final var $1_zeroOrOne = Types.union(typeFactory, node, nodeZeroOrOne);
        final var $1_zeroOrMore = Types.union(typeFactory, node, nodeZeroOrMore);
        final var $1_oneOrMore = Types.union(typeFactory, node, nodeOneOrMore);

        assertEquals($10, node);
        assertEquals($11, nodeOneOrMore);
        assertEquals($1_zeroOrOne, nodeOneOrMore);
        assertEquals($1_zeroOrMore, nodeOneOrMore);
        assertEquals($1_oneOrMore, nodeOneOrMore);

        final var $zeroOrOne_0 = Types.union(typeFactory, nodeZeroOrOne, empty);
        final var $zeroOrOne_1 = Types.union(typeFactory, nodeZeroOrOne, node);
        final var $zeroOrOne_zeroOrOne = Types.union(typeFactory, nodeZeroOrOne, nodeZeroOrOne);
        final var $zeroOrOne_zeroOrMore = Types.union(typeFactory, nodeZeroOrOne, nodeZeroOrMore);
        final var $zeroOrOne_oneOrMore = Types.union(typeFactory, nodeZeroOrOne, nodeOneOrMore);

        assertEquals($zeroOrOne_0, nodeZeroOrOne);
        assertEquals($zeroOrOne_1, nodeOneOrMore);
        assertEquals($zeroOrOne_zeroOrOne, nodeZeroOrMore);
        assertEquals($zeroOrOne_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrOne_oneOrMore, nodeOneOrMore);

        final var $zeroOrMore_0 = Types.union(typeFactory, nodeZeroOrMore, empty);
        final var $zeroOrMore_1 = Types.union(typeFactory, nodeZeroOrMore, node);
        final var $zeroOrMore_zeroOrOne = Types.union(typeFactory, nodeZeroOrMore, nodeZeroOrOne);
        final var $zeroOrMore_zeroOrMore = Types.union(typeFactory, nodeZeroOrMore, nodeZeroOrMore);
        final var $zeroOrMore_oneOrMore = Types.union(typeFactory, nodeZeroOrMore, nodeOneOrMore);

        assertEquals($zeroOrMore_0, nodeZeroOrMore);
        assertEquals($zeroOrMore_1, nodeOneOrMore);
        assertEquals($zeroOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, nodeOneOrMore);

        final var $oneOrMore_0 = Types.union(typeFactory, nodeOneOrMore, empty);
        final var $oneOrMore_1 = Types.union(typeFactory, nodeOneOrMore, node);
        final var $oneOrMore_zeroOrOne = Types.union(typeFactory, nodeOneOrMore, nodeZeroOrOne);
        final var $oneOrMore_zeroOrMore = Types.union(typeFactory, nodeOneOrMore, nodeZeroOrMore);
        final var $oneOrMore_oneOrMore = Types.union(typeFactory, nodeOneOrMore, nodeOneOrMore);

        assertEquals($oneOrMore_0, nodeOneOrMore);
        assertEquals($oneOrMore_1, nodeOneOrMore);
        assertEquals($oneOrMore_zeroOrOne, nodeOneOrMore);
        assertEquals($oneOrMore_zeroOrMore, nodeOneOrMore);
        assertEquals($oneOrMore_oneOrMore, nodeOneOrMore);


        final var elementFoo = typeFactory.element("", Set.of(new QualifiedName("", "foo")));
        final var elementBar = typeFactory.element("", Set.of(new QualifiedName("", "bar")));
        final var merged$elements = Types.union(typeFactory, elementFoo, elementBar);
        assertEquals(
            typeFactory.oneOrMore(
                typeFactory.itemElement("", Set.of(
                    new QualifiedName("", "foo"),
                    new QualifiedName("", "bar")
                ))
            ),
            merged$elements
        );

        final var merged$any = Types.union(typeFactory, elementFoo, anyNode);
        assertEquals(merged$any, nodeOneOrMore);

        final var merged$any2 = Types.union(typeFactory, anyNode, elementFoo);
        assertEquals(merged$any2, nodeOneOrMore);
    }


    @Test
    public void intersectNodeMerging() {
        final var empty = typeFactory.emptySequence();
        final var node = typeFactory.anyNode();
        final var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        final var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());

        final var $00           = Types.intersection(typeFactory, empty, empty);
        final var $01           = Types.intersection(typeFactory, empty, node);
        final var $0_zeroOrOne  = Types.intersection(typeFactory, empty, nodeZeroOrOne);
        final var $0_zeroOrMore = Types.intersection(typeFactory, empty, nodeZeroOrMore);
        final var $0_oneOrMore  = Types.intersection(typeFactory, empty, nodeOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, empty);
        assertEquals($0_zeroOrOne, empty);
        assertEquals($0_zeroOrMore, empty);
        assertEquals($0_oneOrMore, empty);

        final var $10           = Types.intersection(typeFactory, node, empty);
        final var $11           = Types.intersection(typeFactory, node, node);
        final var $1_zeroOrOne  = Types.intersection(typeFactory, node, nodeZeroOrOne);
        final var $1_zeroOrMore = Types.intersection(typeFactory, node, nodeZeroOrMore);
        final var $1_oneOrMore  = Types.intersection(typeFactory, node, nodeOneOrMore);

        assertEquals($10, empty);
        assertEquals($11, nodeZeroOrOne);
        assertEquals($1_zeroOrOne, nodeZeroOrOne);
        assertEquals($1_zeroOrMore, nodeZeroOrOne);
        assertEquals($1_oneOrMore, nodeZeroOrOne);

        final var $zeroOrOne_0 = Types.intersection(typeFactory, nodeZeroOrOne, empty);
        final var $zeroOrOne_1 = Types.intersection(typeFactory, nodeZeroOrOne, node);
        final var $zeroOrOne_zeroOrOne = Types.intersection(typeFactory, nodeZeroOrOne, nodeZeroOrOne);
        final var $zeroOrOne_zeroOrMore = Types.intersection(typeFactory, nodeZeroOrOne, nodeZeroOrMore);
        final var $zeroOrOne_oneOrMore = Types.intersection(typeFactory, nodeZeroOrOne, nodeOneOrMore);

        assertEquals($zeroOrOne_0, empty);
        assertEquals($zeroOrOne_1, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrOne, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrMore, nodeZeroOrOne);
        assertEquals($zeroOrOne_oneOrMore, nodeZeroOrOne);

        final var $zeroOrMore_0 = Types.intersection(typeFactory, nodeZeroOrMore, empty);
        final var $zeroOrMore_1 = Types.intersection(typeFactory, nodeZeroOrMore, node);
        final var $zeroOrMore_zeroOrOne = Types.intersection(typeFactory, nodeZeroOrMore, nodeZeroOrOne);
        final var $zeroOrMore_zeroOrMore = Types.intersection(typeFactory, nodeZeroOrMore, nodeZeroOrMore);
        final var $zeroOrMore_oneOrMore = Types.intersection(typeFactory, nodeZeroOrMore, nodeOneOrMore);

        assertEquals($zeroOrMore_0, empty);
        assertEquals($zeroOrMore_1, nodeZeroOrOne);
        assertEquals($zeroOrMore_zeroOrOne, nodeZeroOrOne);
        assertEquals($zeroOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, nodeZeroOrMore);

        final var $oneOrMore_0 = Types.intersection(typeFactory, nodeOneOrMore, empty);
        final var $oneOrMore_1 = Types.intersection(typeFactory, nodeOneOrMore, node);
        final var $oneOrMore_zeroOrOne = Types.intersection(typeFactory, nodeOneOrMore, nodeZeroOrOne);
        final var $oneOrMore_zeroOrMore = Types.intersection(typeFactory, nodeOneOrMore, nodeZeroOrMore);
        final var $oneOrMore_oneOrMore = Types.intersection(typeFactory, nodeOneOrMore, nodeOneOrMore);

        assertEquals($oneOrMore_0, empty);
        assertEquals($oneOrMore_1, nodeZeroOrOne);
        assertEquals($oneOrMore_zeroOrOne, nodeZeroOrOne);
        assertEquals($oneOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($oneOrMore_oneOrMore, nodeZeroOrMore);


        final var elementFoo = typeFactory.element("", Set.of(
            new QualifiedName("", "foo"),
            new QualifiedName("", "x")
        ));

        final var elementBar = typeFactory.element("",
                Set.of(
            new QualifiedName("", "bar"),
            new QualifiedName("", "x")
        ));

        final var merged$elements = Types.intersection(typeFactory, elementFoo, elementBar);
        assertEquals(
            merged$elements,
            typeFactory.zeroOrOne(
                typeFactory.itemElement("", Set.of(new QualifiedName("", "x")))
            )
        );

        final var merged$any = Types.intersection(typeFactory, elementFoo, anyNode);
        assertEquals(
            merged$any,
            typeFactory.zeroOrOne(
                typeFactory.itemElement("", Set.of(
                    new QualifiedName("", "foo"),
                    new QualifiedName("", "x")
                ))
            )
        );

        final var merged$any2 = Types.intersection(typeFactory, anyNode, elementFoo);
        assertEquals(
            merged$any2,
            typeFactory.zeroOrOne(
                typeFactory.itemElement("", Set.of(
                    new QualifiedName("", "foo"),
                    new QualifiedName("", "x")
                ))
            )
        );

    }

    @Test
    public void exceptNodeMerging() {
        final var empty = typeFactory.emptySequence();
        final var node = typeFactory.anyNode();
        final var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        final var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());

        final var $00           = Types.subtract(typeFactory, empty, empty);
        final var $01           = Types.subtract(typeFactory, empty, node);
        final var $0_zeroOrOne  = Types.subtract(typeFactory, empty, nodeZeroOrOne);
        final var $0_zeroOrMore = Types.subtract(typeFactory, empty, nodeZeroOrMore);
        final var $0_oneOrMore  = Types.subtract(typeFactory, empty, nodeOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, empty);
        assertEquals($0_zeroOrOne, empty);
        assertEquals($0_zeroOrMore, empty);
        assertEquals($0_oneOrMore, empty);

        final var $10 = Types.subtract(typeFactory, node, empty);
        final var $11 = Types.subtract(typeFactory, node, node);
        final var $1_zeroOrOne = Types.subtract(typeFactory, node, nodeZeroOrOne);
        final var $1_zeroOrMore = Types.subtract(typeFactory, node, nodeZeroOrMore);
        final var $1_oneOrMore = Types.subtract(typeFactory, node, nodeOneOrMore);

        assertEquals($10, node);
        assertEquals($11, nodeZeroOrOne);
        assertEquals($1_zeroOrOne, nodeZeroOrOne);
        assertEquals($1_zeroOrMore, nodeZeroOrOne);
        assertEquals($1_oneOrMore, nodeZeroOrOne);

        final var $zeroOrOne_0 = Types.subtract(typeFactory, nodeZeroOrOne, empty);
        final var $zeroOrOne_1 = Types.subtract(typeFactory, nodeZeroOrOne, node);
        final var $zeroOrOne_zeroOrOne = Types.subtract(typeFactory, nodeZeroOrOne, nodeZeroOrOne);
        final var $zeroOrOne_zeroOrMore = Types.subtract(typeFactory, nodeZeroOrOne, nodeZeroOrMore);
        final var $zeroOrOne_oneOrMore = Types.subtract(typeFactory, nodeZeroOrOne, nodeOneOrMore);

        assertEquals($zeroOrOne_0, nodeZeroOrOne);
        assertEquals($zeroOrOne_1, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrOne, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrMore, nodeZeroOrOne);
        assertEquals($zeroOrOne_oneOrMore, nodeZeroOrOne);

        final var $zeroOrMore_0 = Types.subtract(typeFactory, nodeZeroOrMore, empty);
        final var $zeroOrMore_1 = Types.subtract(typeFactory, nodeZeroOrMore, node);
        final var $zeroOrMore_zeroOrOne = Types.subtract(typeFactory, nodeZeroOrMore, nodeZeroOrOne);
        final var $zeroOrMore_zeroOrMore = Types.subtract(typeFactory, nodeZeroOrMore, nodeZeroOrMore);
        final var $zeroOrMore_oneOrMore = Types.subtract(typeFactory, nodeZeroOrMore, nodeOneOrMore);

        assertEquals($zeroOrMore_0, nodeZeroOrMore);
        assertEquals($zeroOrMore_1, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, nodeZeroOrMore);

        final var $oneOrMore_0 = Types.subtract(typeFactory, nodeOneOrMore, empty);
        final var $oneOrMore_1 = Types.subtract(typeFactory, nodeOneOrMore, node);
        final var $oneOrMore_zeroOrOne = Types.subtract(typeFactory, nodeOneOrMore, nodeZeroOrOne);
        final var $oneOrMore_zeroOrMore = Types.subtract(typeFactory, nodeOneOrMore, nodeZeroOrMore);
        final var $oneOrMore_oneOrMore = Types.subtract(typeFactory, nodeOneOrMore, nodeOneOrMore);

        assertEquals($oneOrMore_0, nodeOneOrMore);
        assertEquals($oneOrMore_1, nodeZeroOrMore);
        assertEquals($oneOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($oneOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($oneOrMore_oneOrMore, nodeZeroOrMore);

        final QualifiedName foo = new QualifiedName("", "foo");
        final var elementFoo = typeFactory.element("",
                Set.of( foo)
        );

        final QualifiedName bar = new QualifiedName("", "bar");
        final var elementBar = typeFactory.element("", Set.of(bar));

        final var merged$elements = Types.subtract(typeFactory, elementFoo, elementBar);
        assertEquals(
            merged$elements,
            typeFactory.zeroOrOne(
                typeFactory.itemElement("", Set.of(foo))
            )
        );

        final var merged$any = Types.subtract(typeFactory, elementFoo, anyNode);
        assertEquals(
            merged$any,
            typeFactory.zeroOrOne(
                typeFactory.itemElement("", Set.of(foo))
            )
        );

        final var merged$any2 = Types.subtract(typeFactory, anyNode, elementFoo);
        assertEquals(
            merged$any2,
            typeFactory.zeroOrOne(
                typeFactory.itemAnyNode()
            )
        );

    }


    @Test
    public void choiceItemTypeSubtyping() {
        final var numberOrBool = typeFactory.choice(
                typeFactory.itemNumber(), typeFactory.itemBoolean()
        );
        final var boolOrNumber = typeFactory.choice(
                typeFactory.itemBoolean(), typeFactory.itemNumber()
        );
        final var stringOrBool = typeFactory.choice(
                typeFactory.itemString(), typeFactory.itemBoolean()
        );
        final var stringOrBoolOrNumber = typeFactory.choice(
                typeFactory.itemString(), typeFactory.itemBoolean(), typeFactory.itemNumber()
        );

        assertTrue(Types.isSubtype(typeFactory, number, numberOrBool));
        assertTrue(Types.isSubtype(typeFactory, boolean_, numberOrBool));
        assertFalse(Types.isSubtype(typeFactory, numberOrBool, number));
        assertFalse(Types.isSubtype(typeFactory, numberOrBool, boolean_));
        assertFalse(Types.isSubtype(typeFactory, numberOrBool, boolean_));
                    
        assertTrue(Types.isSubtype(typeFactory, numberOrBool, anyItem));
        assertFalse(Types.isSubtype(typeFactory, anyItem, numberOrBool));
                    
        assertFalse(Types.isSubtype(typeFactory, numberOrBool, stringOrBool));
        assertTrue(Types.isSubtype(typeFactory, numberOrBool, boolOrNumber));
        assertTrue(Types.isSubtype(typeFactory, boolOrNumber, numberOrBool));
                    
        assertTrue(Types.isSubtype(typeFactory, numberOrBool, stringOrBoolOrNumber));
        assertTrue(Types.isSubtype(typeFactory, stringOrBool, stringOrBoolOrNumber));
        assertFalse(Types.isSubtype(typeFactory, stringOrBoolOrNumber, numberOrBool));
    }

    @Test
    public void extensibleRecordsSubtyping() {
        final var numberRequired = new RecordField("a", new TypeOrReference.Type(typeFactory.number(NumericRange.FULL)), true);
        final var a_number = typeFactory.extensibleRecord(
                new LinkedHashMap<>(Map.of("a", numberRequired))
        );
        assertTrue(Types.isSubtype(typeFactory, a_number, anyMap));
        //         3.3.2.8 Subtyping Records
        // Given item types A and B, A ⊆ B is true if any of the following apply:

        // A is map(*) and B is record(*).
        assertTrue(Types.isSubtype(typeFactory, anyMap, anyMap));

        // All of the following are true:
        // A is a record type.
        // B is map(*) or record(*).
        assertTrue(Types.isSubtype(typeFactory, a_number, anyMap));

        final var longitude = new RecordField("longitude", new TypeOrReference.Type(anyItem), true);
        final var latitude = new RecordField("longitude", new TypeOrReference.Type(anyItem), true);
        // Examples:
        // record(longitude, latitude) ⊆ map(*)
        final var longitudeLatitudeRecord = typeFactory.record(
                new LinkedHashMap<>(Map.of("longitude", longitude, "latitude", latitude))
        );
        assertTrue(Types.isSubtype(typeFactory, longitudeLatitudeRecord, anyMap));

        // record(longitude, latitude, *) ⊆ record(*)
        final var longitudeLatitudeRecordExtensible = typeFactory.record(
                new LinkedHashMap<>(Map.of("longitude", longitude, "latitude", latitude))
        );
        assertTrue(Types.isSubtype(typeFactory, longitudeLatitudeRecordExtensible, anyMap));

        // All of the following are true:
        // A is a non-extensible record type
        // B is map(K, V)
        // K is either xs:string or xs:anyAtomicType
        // For every field F in A, where T is the declared type of F (or its default, item()*), T ⊑ V .
        // Examples:
        // record(x, y) ⊆ map(xs:string, item()*)
        final var xy = typeFactory.record(
            new LinkedHashMap<>(Map.of(
                "x", new RecordField("x", new TypeOrReference.Type(zeroOrMoreItems), true), 
                "y", new RecordField( "y", new TypeOrReference.Type(zeroOrMoreItems), true)
                )
            ));

        final AntlrQuerySequenceType anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        final var mapStringItem = typeFactory.map(typeFactory.itemString(),
                                                  anyItems);
        assertTrue(Types.isSubtype(typeFactory, xy, mapStringItem));

        // record(x as xs:double, y as xs:double) ⊆ map(xs:string, xs:double)
        final var xy_number = typeFactory.record(
                new LinkedHashMap<>(Map.of("x", numberRequired, "y", numberRequired))
        );
        final var mapStringNumber = typeFactory.map(typeFactory.itemString(), typeFactory.number(NumericRange.FULL));
        assertTrue(Types.isSubtype(typeFactory, xy_number, mapStringNumber));

        // All of the following are true:
        // A is a non-extensible record type.
        // B is a non-extensible record type.
        // Every field in A is also declared in B.
        // Every mandatory field in B is also declared as mandatory in A.
        // For every field that is declared in both A and B, where the declared type in A is T and the declared type in B is U, T ⊑ U .
        // Examples:
        // record(x, y) ⊆ record(x, y, z?)
        {
        final var xField = new RecordField("x", new TypeOrReference.Type(anyItems), true);
        final var yField = new RecordField("y", new TypeOrReference.Type(anyItems), true);
        final var zField = new RecordField("z", new TypeOrReference.Type(anyItems), true);
        final var xyz = typeFactory.record(
                new LinkedHashMap<>(Map.of("x", xField, "y", yField, "z", zField))
        );
        assertTrue(Types.isSubtype(typeFactory, xy, xyz));
        }

        // All of the following are true:
        // A is an extensible record type
        // B is an extensible record type
        // Every mandatory field in B is also declared as mandatory in A.
        // For every field that is declared in both A and B, where the declared type in A is T and the declared type in B is U, T ⊑ U .
        // For every field that is declared in B but not in A, the declared type in B is item()*.
        // Examples:
        // record(x, y, z, *) ⊆ record(x, y, *)
        {
        final var xFieldExtensible = new RecordField("x", new TypeOrReference.Type(anyItems), true);
        final var yFieldExtensible = new RecordField("y", new TypeOrReference.Type(anyItems), true);
        final var zFieldExtensible = new RecordField("z", new TypeOrReference.Type(anyItems), true);
        final var xyzExtensible = typeFactory.extensibleRecord(
            new LinkedHashMap<>(Map.of("x", xFieldExtensible, "y", yFieldExtensible, "z", zFieldExtensible))
        );
        final var xyExtensible = typeFactory.extensibleRecord(
            new LinkedHashMap<>(Map.of("x", xFieldExtensible, "y", yFieldExtensible))
        );
        assertTrue(Types.isSubtype(typeFactory, xyzExtensible, xyExtensible));
        }

        // Error in documentation?
        // ??? record(x?, y?, z?, *) ⊆ record(x, y, *) ???
        // more likely: record(x, y, z?, *) ⊆ record(x, y, *)
        {
        final var xFieldExtensible = new RecordField("x", new TypeOrReference.Type(anyItems), true);
        final var yFieldExtensible = new RecordField("y", new TypeOrReference.Type(anyItems), true);
        final var zFieldOptionalExtensible = new RecordField("z", new TypeOrReference.Type(anyItems), false);
        final var xyzExtensibleOptional = typeFactory.extensibleRecord(
            new LinkedHashMap<>(Map.of("x", xFieldExtensible, "y", yFieldExtensible, "z", zFieldOptionalExtensible))
        );
        final var xyExtensible = typeFactory.extensibleRecord(
            new LinkedHashMap<>(Map.of("x", xFieldExtensible, "y", yFieldExtensible))
        );
        assertTrue(Types.isSubtype(typeFactory, xyzExtensibleOptional, xyExtensible));
        }

        // All of the following are true:
        // A is a non-extensible record type.
        // B is an extensible record type.
        // Every mandatory field in B is also declared as mandatory in A.
        // For every field that is declared in both A and B, where the declared type in A is T and the declared type in B is U, T ⊑ U .
        // Examples:
        // record(x, y as xs:integer) ⊆ record(x, y as xs:decimal, *)
        {
            final var xFieldAny = new RecordField("x", new TypeOrReference.Type(zeroOrMoreItems), true);
            final var yFieldNumber = new RecordField("x", new TypeOrReference.Type(number), true);
            final var record_x_any_y_number = typeFactory.record(
                new LinkedHashMap<>(Map.of("x", xFieldAny,"y", yFieldNumber))
            );
            final var extensible_record_x_any_y_number = typeFactory.extensibleRecord(
                new LinkedHashMap<>(Map.of("x", xFieldAny,"y", yFieldNumber))
            );
            assertTrue(Types.isSubtype(typeFactory, record_x_any_y_number, extensible_record_x_any_y_number));
        }

        // record(y as xs:integer) ⊆ record(x?, y as xs:decimal, *)
        final var yFieldNumberOnly = numberRequired;
        final var record_y_number = typeFactory.record(
            new LinkedHashMap<>(Map.of("y", yFieldNumberOnly))
        );
        final var xFieldAny = new RecordField("x", new TypeOrReference.Type(zeroOrMoreItems), true);
        final var extensible_record_x_any_y_number_2 = typeFactory.extensibleRecord(
            new LinkedHashMap<>(Map.of("x", xFieldAny,"y", yFieldNumberOnly))
        );
        assertTrue(Types.isSubtype(typeFactory, record_y_number, extensible_record_x_any_y_number_2));
    }



}
