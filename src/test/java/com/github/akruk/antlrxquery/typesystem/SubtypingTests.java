package com.github.akruk.antlrxquery.typesystem;

import org.junit.Test;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class SubtypingTests {

    final XQueryTypeFactory typeFactory = new XQueryEnumTypeFactory();
    final XQuerySequenceType error = typeFactory.error();
    final XQuerySequenceType boolean_ = typeFactory.boolean_();
    final XQuerySequenceType string = typeFactory.string();
    final XQuerySequenceType number = typeFactory.number();
    final XQuerySequenceType anyNode = typeFactory.anyNode();
    final XQuerySequenceType emptySequence = typeFactory.emptySequence();
    final XQuerySequenceType stringSequenceOneOrMore = typeFactory.oneOrMore(typeFactory.itemString());
    final XQuerySequenceType stringSequenceZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemString());
    final XQuerySequenceType stringSequenceZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemString());
    final XQuerySequenceType numberSequenceOneOrMore = typeFactory.oneOrMore(typeFactory.itemNumber());
    final XQuerySequenceType numberSequenceZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemNumber());
    final XQuerySequenceType numberSequenceZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemNumber());
    final XQuerySequenceType fooElement = typeFactory.element(Set.of("foo"));
    final XQuerySequenceType barElement = typeFactory.element(Set.of("bar"));
    final XQuerySequenceType anyArray = typeFactory.anyArray();
    final XQuerySequenceType anyMap = typeFactory.anyMap();
    final XQuerySequenceType anyItem = typeFactory.anyItem();
    final XQuerySequenceType anyFunction = typeFactory.anyFunction();

    final XQueryRecordField requiredAnyItem = new XQueryRecordField(typeFactory.anyItem(), true);
    final XQueryRecordField requiredNumber = new XQueryRecordField(typeFactory.number(), true);
    final XQueryRecordField requiredString = new XQueryRecordField(typeFactory.string(), true);
    final XQuerySequenceType recordAny = typeFactory.record(Map.of("foo", requiredAnyItem, "bar", requiredAnyItem));

    final XQueryItemType itemError = typeFactory.itemError();
    final XQueryItemType itemAnyFunction = typeFactory.itemAnyFunction();
    final XQueryItemType itemAnyItem = typeFactory.itemAnyItem();
    final XQueryItemType itemString = typeFactory.itemString();
    final XQueryItemType itemNumber = typeFactory.itemNumber();
    final XQueryItemType itemBoolean = typeFactory.itemBoolean();
    final XQueryItemType itemAnyNode = typeFactory.itemAnyNode();
    final XQueryItemType itemAnyMap = typeFactory.itemAnyMap();
    final XQueryItemType itemAnyArray = typeFactory.itemAnyArray();
    final XQueryItemType itemElementFoo = typeFactory.itemElement(Set.of("foo"));
    final XQueryItemType itemElementBar = typeFactory.itemElement(Set.of("bar"));
    final XQueryItemType itemABenum = typeFactory.itemEnum(Set.of("A", "B"));
    final XQueryItemType itemABCenum = typeFactory.itemEnum(Set.of("A", "B", "C"));
    final XQueryItemType itemABCDenum = typeFactory.itemEnum(Set.of("A", "B", "C", "D"));
    final XQueryItemType itemRecordAny = typeFactory.itemRecord(Map.of("foo", requiredAnyItem, "bar", requiredAnyItem));
    final XQueryItemType itemRecordString = typeFactory.itemRecord(Map.of("foo", requiredString, "bar", requiredString));



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
        assertTrue(string.isSubtypeOf(string));
        assertFalse(string.isSubtypeOf(number));
        assertFalse(string.isSubtypeOf(emptySequence));
        assertTrue(string.isSubtypeOf(stringSequenceOneOrMore));
        assertTrue(string.isSubtypeOf(stringSequenceZeroOrMore));
        assertTrue(string.isSubtypeOf(stringSequenceZeroOrOne));
    }


    @Test
    public void emptySequenceSubtyping() {
        assertTrue(emptySequence.isSubtypeOf(typeFactory.emptySequence()));

        assertFalse(emptySequence.isSubtypeOf(typeFactory.anyItem()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemAnyItem)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemAnyItem)));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemAnyItem)));

        assertFalse(emptySequence.isSubtypeOf(typeFactory.string()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemString)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemString)));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemString)));

        assertFalse(emptySequence.isSubtypeOf(typeFactory.number()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemNumber)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemNumber)));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemNumber)));

        assertFalse(emptySequence.isSubtypeOf(typeFactory.boolean_()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemBoolean)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemBoolean)));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemBoolean)));

        assertFalse(emptySequence.isSubtypeOf(typeFactory.anyNode()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemAnyNode)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemAnyNode)));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemAnyNode)));

        assertFalse(emptySequence.isSubtypeOf(fooElement));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemElementFoo)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemElementFoo)));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemElementFoo)));

        assertFalse(emptySequence.isSubtypeOf(typeFactory.anyMap()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemAnyMap)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemAnyMap)));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemAnyMap)));

        assertFalse(emptySequence.isSubtypeOf(typeFactory.map(itemString, typeFactory.anyItem())));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.map(itemNumber, typeFactory.anyItem())));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.map(itemBoolean, typeFactory.anyItem())));

        assertFalse(emptySequence.isSubtypeOf(typeFactory.anyArray()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemAnyArray)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemAnyArray)));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemAnyArray)));

        assertFalse(emptySequence.isSubtypeOf(typeFactory.anyFunction()));

        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemAnyFunction)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemAnyFunction)));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemAnyFunction)));
        // assertTrue(emptySequence.isSubtypeOf(typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(emptySequence.isSubtypeOf(typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(emptySequence.isSubtypeOf(function(T) as R))
        // assertTrue(emptySequence.isSubtypeOf(function(T1, T2) as R))

    }

    @Test
    public void numberItemSubtyping() {
        final var tested = typeFactory.itemNumber();

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementBar));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordAny));
        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordString));
        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertTrue(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));
        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
    }

    @Test
    public void stringItemSubtyping() {
        final var tested = typeFactory.itemString();

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementBar));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertTrue(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
    }

    @Test
    public void booleanItemSubtyping() {
        final var tested = typeFactory.itemBoolean();

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementBar));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertTrue(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
    }


    @Test
    public void namedElementItemSubtyping() {
        final var tested = itemElementFoo;

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertTrue(tested.itemtypeIsSubtypeOf(itemElementFoo));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementBar));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
    }

    @Test
    public void anyMapItemSubtyping() {
        final var tested = itemAnyMap;

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        // var itemRecord = typeFactory.itemRec(itemString, typeFactory.anyItem());
        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
    }

    @Test
    public void errorItemSubtyping() {
        final var tested = itemError;

        assertTrue(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
    }

    @Test
    public void anyItemSubtyping() {
        final var tested = itemAnyItem;

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
    }


    @Test
    public void anyNodeItemSubtyping() {
        final var tested = itemAnyNode;

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
    }

    @Test
    public void enumItemSubtyping() {
        final var tested = itemABCenum;

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordAny));
        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertTrue(tested.itemtypeIsSubtypeOf(itemString));

        assertTrue(tested.itemtypeIsSubtypeOf(itemABCenum));
        assertTrue(tested.itemtypeIsSubtypeOf(itemABCDenum));
        assertFalse(itemABCDenum.itemtypeIsSubtypeOf(itemABCenum));
    }


    @Test
    public void anyFunctionItemSubtyping() {
        final var tested = itemAnyFunction;

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordAny));
        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
        assertFalse(tested.itemtypeIsSubtypeOf(itemABCDenum));
    }



    @Test
    public void anyArrayItemSubtyping() {
        final var tested = typeFactory.itemAnyArray();

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyMap));

        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemNumber, typeFactory.string())));


        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));

        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number()))));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number()))));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.oneOrMore(itemNumber)))));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(), typeFactory.number()))));

        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordAny));
        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
        assertFalse(tested.itemtypeIsSubtypeOf(itemABCDenum));
    }

    @Test
    public void typedArrayItemSubtyping() {
        final var tested = typeFactory.itemArray(typeFactory.string());

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));

        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemNumber, typeFactory.string())));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemNumber, typeFactory.number())));

        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.string())));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));

        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number()))));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number()))));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.oneOrMore(itemNumber)))));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.number(), List.of(typeFactory.number()))));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(), typeFactory.number()))));

        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordAny));
        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
        assertFalse(tested.itemtypeIsSubtypeOf(itemABCDenum));
    }


    @Test
    public void typedMapItemSubtyping() {
        final var tested = typeFactory.itemMap(itemString, typeFactory.string());
        final var itemMapNumberToString = typeFactory.itemMap(itemNumber, typeFactory.string());

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));

        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemAnyItem, typeFactory.anyItem())));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemAnyItem, typeFactory.number())));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemNumber, typeFactory.number())));


        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertTrue(itemMapNumberToString.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.string())));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));

        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.anyItem()))));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number()))));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.oneOrMore(itemString), List.of(typeFactory.string()))));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.number(), List.of(typeFactory.number()))));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()))));

        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordAny));
        assertFalse(tested.itemtypeIsSubtypeOf(itemRecordString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
        assertFalse(tested.itemtypeIsSubtypeOf(itemABCDenum));
    }


    @Test
    public void typedFunctionSubtyping() {
        final var string1_$string1 = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()));
        final var noargFunction = typeFactory.itemFunction(typeFactory.string(), List.of());
        final var $2argfunction = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()));
        final var numberToItem = typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number()));
        final var numberToString = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number()));

        assertFalse(string1_$string1.itemtypeIsSubtypeOf(itemError));
        assertTrue(string1_$string1.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(itemElementFoo));

        assertTrue(string1_$string1.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(noargFunction.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse($2argfunction.itemtypeIsSubtypeOf(itemAnyMap));

        assertTrue(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemMap(itemAnyItem, typeFactory.anyItem())));
        assertTrue(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertTrue(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.string())));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemMap(itemAnyItem, typeFactory.number())));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemMap(itemNumber, typeFactory.number())));


        assertFalse(string1_$string1.itemtypeIsSubtypeOf(itemAnyArray));
        assertTrue(numberToItem.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.string())));
        assertTrue(numberToString.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.string())));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));

        assertTrue(string1_$string1.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number()))));
        assertTrue(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.oneOrMore(itemString), List.of(typeFactory.string()))));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.number(), List.of(typeFactory.number()))));
        assertTrue(string1_$string1.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()))));

        assertFalse(string1_$string1.itemtypeIsSubtypeOf(itemRecordAny));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(itemRecordString));

        assertFalse(string1_$string1.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(itemString));

        assertFalse(string1_$string1.itemtypeIsSubtypeOf(itemABCenum));
        assertFalse(string1_$string1.itemtypeIsSubtypeOf(itemABCDenum));
    }


    @Test
    public void recordItemSubtyping() {
        final var tested = itemRecordAny;
        final var numberToItem = typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number()));
        final var numberToString = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number()));
        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));

        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyMap));

        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemAnyItem, typeFactory.anyItem())));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.string())));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemAnyItem, typeFactory.number())));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemNumber, typeFactory.number())));

        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertTrue(numberToItem.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.string())));
        assertTrue(numberToString.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.string())));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));

        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(tested
                .itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.anyItem()))));
        assertTrue(tested
                .itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.string()))));
        assertFalse(tested
                .itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertTrue(itemRecordString
                .itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertFalse(tested
                .itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number()))));
        assertTrue(itemRecordString.itemtypeIsSubtypeOf(
                typeFactory.itemFunction(typeFactory.oneOrMore(itemString), List.of(typeFactory.string()))));
        assertFalse(tested
                .itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.number(), List.of(typeFactory.number()))));
        assertFalse(tested.itemtypeIsSubtypeOf(
                typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()))));

        assertTrue(tested.itemtypeIsSubtypeOf(itemRecordAny));
        assertTrue(itemRecordString.itemtypeIsSubtypeOf(itemRecordAny));
        assertTrue(itemRecordString.itemtypeIsSubtypeOf(itemRecordString));
        assertFalse(itemRecordAny.itemtypeIsSubtypeOf(itemRecordString));


        final var itemFooBarHoo = typeFactory.itemRecord(
                Map.of("foo", requiredString, "bar", requiredString, "hoo", requiredString));
        final var itemFooBarNum = typeFactory
                .itemRecord(Map.of("foo", requiredNumber, "bar", requiredString));
        assertTrue(itemRecordString.itemtypeIsSubtypeOf(itemFooBarHoo));
        assertFalse(itemFooBarHoo.itemtypeIsSubtypeOf(itemRecordString));
        assertFalse(itemRecordString.itemtypeIsSubtypeOf(itemFooBarNum));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertFalse(tested.itemtypeIsSubtypeOf(itemString));

        assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
        assertFalse(tested.itemtypeIsSubtypeOf(itemABCDenum));
    }


    @Test
    public void choiceItemTypeSubtyping() {
        final var numberOrBool = typeFactory.choice(Set.of(typeFactory.itemNumber(), typeFactory.itemBoolean()));
        final var boolOrNumber = typeFactory.choice(Set.of(typeFactory.itemBoolean(), typeFactory.itemNumber()));
        final var stringOrBool = typeFactory.choice(Set.of(typeFactory.itemString(), typeFactory.itemBoolean()));
        final var stringOrBoolOrNumber = typeFactory.choice(Set.of(typeFactory.itemString(), typeFactory.itemBoolean(), typeFactory.itemNumber()));

        assertTrue(number.isSubtypeOf(numberOrBool));
        assertTrue(boolean_.isSubtypeOf(numberOrBool));
        assertFalse(numberOrBool.isSubtypeOf(number));
        assertFalse(numberOrBool.isSubtypeOf(boolean_));
        assertFalse(numberOrBool.isSubtypeOf(boolean_));

        assertTrue(numberOrBool.isSubtypeOf(anyItem));
        assertFalse(anyItem.isSubtypeOf(numberOrBool));

        assertFalse(numberOrBool.isSubtypeOf(stringOrBool));

        assertTrue(numberOrBool.isSubtypeOf(boolOrNumber));
        assertTrue(boolOrNumber.isSubtypeOf(numberOrBool));

        assertTrue(numberOrBool.isSubtypeOf(stringOrBoolOrNumber));
        assertTrue(stringOrBool.isSubtypeOf(stringOrBoolOrNumber));
        assertFalse(stringOrBoolOrNumber.isSubtypeOf(numberOrBool));
    }

    @Test
    public void extensibleRecordsSubtyping() {
        final var numberRequired = new XQueryRecordField(typeFactory.number(), true);
        final var a_number = typeFactory.extensibleRecord( Map.of("a", numberRequired));
        assertTrue(a_number.isSubtypeOf(anyMap));
        //         3.3.2.8 Subtyping Records
        // Given item types A and B, A ⊆ B is true if any of the following apply:

        // A is map(*) and B is record(*).
        assertTrue(anyMap.isSubtypeOf(anyMap));

        // All of the following are true:
        // A is a record type.
        // B is map(*) or record(*).
        assertTrue(a_number.isSubtypeOf(anyMap));

        final var anyItemRequired = new XQueryRecordField(anyItem, true);
        // Examples:
        // record(longitude, latitude) ⊆ map(*)
        final var longitudeLatitudeRecord = typeFactory.record(Map.of("longitude", anyItemRequired,
                                                                      "latitude", anyItemRequired));
        assertTrue(longitudeLatitudeRecord.isSubtypeOf(anyMap));

        // record(longitude, latitude, *) ⊆ record(*)
        final var longitudeLatitudeRecordExtensible = typeFactory.record(Map.of("longitude", anyItemRequired,
                                                                                "latitude", anyItemRequired));
        longitudeLatitudeRecordExtensible.isSubtypeOf(anyMap);

        // All of the following are true:
        // A is a non-extensible record type
        // B is map(K, V)
        // K is either xs:string or xs:anyAtomicType
        // For every field F in A, where T is the declared type of F (or its default, item()*), T ⊑ V .
        // Examples:
        // record(x, y) ⊆ map(xs:string, item()*)
        final var xy = typeFactory.record(Map.of("x", anyItemRequired, "y", anyItemRequired));
        final XQuerySequenceType anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        final var mapStringItem = typeFactory.map(typeFactory.itemString(),
                                                  anyItems);
        xy.isSubtypeOf(mapStringItem);

        // record(x as xs:double, y as xs:double) ⊆ map(xs:string, xs:double)
        final var xy_number = typeFactory.record(Map.of("x", numberRequired, "y", numberRequired));
        final var mapStringNumber = typeFactory.map(typeFactory.itemString(), typeFactory.number());
        xy_number.isSubtypeOf(mapStringNumber);

        // All of the following are true:
        // A is a non-extensible record type.
        // B is a non-extensible record type.
        // Every field in A is also declared in B.
        // Every mandatory field in B is also declared as mandatory in A.
        // For every field that is declared in both A and B, where the declared type in A is T and the declared type in B is U, T ⊑ U .
        // Examples:
        // record(x, y) ⊆ record(x, y, z?)
        final var anyItemsRequired = new XQueryRecordField(anyItems, true);
        final var xyz = typeFactory.record(Map.of("x", anyItemsRequired, "y", anyItemsRequired, "z", anyItemsRequired));
        xy.isSubtypeOf(xyz);

        // All of the following are true:
        // A is an extensible record type
        // B is an extensible record type
        // Every mandatory field in B is also declared as mandatory in A.
        // For every field that is declared in both A and B, where the declared type in A is T and the declared type in B is U, T ⊑ U .
        // For every field that is declared in B but not in A, the declared type in B is item()*.
        // Examples:
        // record(x, y, z, *) ⊆ record(x, y, *)
        final var xyzExtensible = typeFactory.extensibleRecord(
            Map.of("x", anyItemsRequired, "y", anyItemsRequired, "z", anyItemsRequired)
        );
        final var xyExtensible = typeFactory.extensibleRecord(
            Map.of("x", anyItemsRequired, "y", anyItemsRequired)
        );
        assertTrue(xyzExtensible.isSubtypeOf(xyExtensible));

        // Error in documentation?
        // ??? record(x?, y?, z?, *) ⊆ record(x, y, *) ???
        // more likely: record(x, y, z?, *) ⊆ record(x, y, *)
        final var anyItemsOptional = new XQueryRecordField(anyItems, false);
        final var xyzExtensibleOptional = typeFactory.extensibleRecord(
            Map.of("x", anyItemsRequired, "y", anyItemsRequired, "z", anyItemsOptional)
        );
        assertTrue(xyzExtensibleOptional.isSubtypeOf(xyExtensible));

        // record(x as xs:integer, y as xs:integer, *) ⊆ record(x as xs:decimal, y as xs:integer*, *)
        final var xyExtensibleIntegers = typeFactory.extensibleRecord(
            Map.of("x", numberRequired, "y", numberRequired)
        );
        final var xyExtensibleIntegers2 = typeFactory.extensibleRecord(
            Map.of("x", numberRequired, "y", new XQueryRecordField(typeFactory.zeroOrMore(typeFactory.itemNumber()), true))
        );
        xyExtensibleIntegers.isSubtypeOf(xyExtensibleIntegers2);

        // record(x as xs:integer, *) ⊆ record(x as xs:decimal, y as item(), *)
        final var xExtensibleInteger = typeFactory.extensibleRecord(
            Map.of("x", numberRequired)
        );
        final var xyExtensibleIntegerItem = typeFactory.extensibleRecord(
            Map.of("x", numberRequired, "y", new XQueryRecordField(typeFactory.anyItem(), true))
        );
        xExtensibleInteger.isSubtypeOf(xyExtensibleIntegerItem);


        // All of the following are true:
        // A is a non-extensible record type.
        // B is an extensible record type.
        // Every mandatory field in B is also declared as mandatory in A.
        // For every field that is declared in both A and B, where the declared type in A is T and the declared type in B is U, T ⊑ U .
        // Examples:
        // record(x, y as xs:integer) ⊆ record(x, y as xs:decimal, *)
        final var record_x_any_y_number = typeFactory.record(
            Map.of("x", anyItemsRequired,"y", numberRequired)
        );
        final var extensible_record_x_any_y_number = typeFactory.extensibleRecord(
            Map.of("x", anyItemsRequired,"y", numberRequired)
        );
        assertTrue(record_x_any_y_number.isSubtypeOf(extensible_record_x_any_y_number));

        // record(y as xs:integer) ⊆ record(x?, y as xs:decimal, *)
        final var record_y_number = typeFactory.record(
            Map.of("y", numberRequired)
        );
        final var extensible_record_x_any_y_number_2 = typeFactory.extensibleRecord(
            Map.of("x", anyItemsOptional,"y", numberRequired)
        );
        assertTrue(record_y_number.isSubtypeOf(extensible_record_x_any_y_number_2));
    }



}
