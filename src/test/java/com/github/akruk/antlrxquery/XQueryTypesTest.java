package com.github.akruk.antlrxquery;

import org.junit.Test;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class XQueryTypesTest {

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
        final var tested = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()));
        final var noargFunction = typeFactory.itemFunction(typeFactory.string(), List.of());
        final var $2argfunction = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()));
        final var numberToItem = typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number()));
        final var numberToString = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number()));

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertFalse(tested.itemtypeIsSubtypeOf(itemElementFoo));

        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(noargFunction.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse($2argfunction.itemtypeIsSubtypeOf(itemAnyMap));

        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemAnyItem, typeFactory.anyItem())));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertTrue(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.string())));
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
    public void typeAlternatives() {
        final var empty = typeFactory.emptySequence();
        final var numberZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemNumber());
        final var numberZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemNumber());
        final var numberOneOrMore = typeFactory.oneOrMore(typeFactory.itemNumber());
        final var $00 = empty.typeAlternative(empty);
        final var $01 = empty.typeAlternative(number);
        final var $0_zeroOrOne = empty.typeAlternative(numberZeroOrOne);
        final var $0_zeroOrMore = empty.typeAlternative(numberZeroOrMore);
        final var $0_oneOrMore = empty.typeAlternative(numberOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, numberZeroOrOne);
        assertEquals($0_zeroOrOne, numberZeroOrOne);
        assertEquals($0_zeroOrMore, numberZeroOrMore);
        assertEquals($0_oneOrMore, numberZeroOrMore);

        final var $10 = number.typeAlternative(empty);
        final var $11 = number.typeAlternative(number);
        final var $1_zeroOrOne = number.typeAlternative(numberZeroOrOne);
        final var $1_zeroOrMore = number.typeAlternative(numberZeroOrMore);
        final var $1_oneOrMore = number.typeAlternative(numberOneOrMore);

        assertEquals($10, numberZeroOrOne);
        assertEquals($11, number);
        assertEquals($1_zeroOrOne, numberZeroOrOne);
        assertEquals($1_zeroOrMore, numberZeroOrMore);
        assertEquals($1_oneOrMore, numberOneOrMore);

        final var $zeroOrOne_0 = numberZeroOrOne.typeAlternative(empty);
        final var $zeroOrOne_1 = numberZeroOrOne.typeAlternative(number);
        final var $zeroOrOne_zeroOrOne = numberZeroOrOne.typeAlternative(numberZeroOrOne);
        final var $zeroOrOne_zeroOrMore = numberZeroOrOne.typeAlternative(numberZeroOrMore);
        final var $zeroOrOne_oneOrMore = numberZeroOrOne.typeAlternative(numberOneOrMore);

        assertEquals($zeroOrOne_0, numberZeroOrOne);
        assertEquals($zeroOrOne_1, numberZeroOrOne);
        assertEquals($zeroOrOne_zeroOrOne, numberZeroOrOne);
        assertEquals($zeroOrOne_zeroOrMore, numberZeroOrMore);
        assertEquals($zeroOrOne_oneOrMore, numberZeroOrMore);

        final var $zeroOrMore_0 = numberZeroOrMore.typeAlternative(empty);
        final var $zeroOrMore_1 = numberZeroOrMore.typeAlternative(number);
        final var $zeroOrMore_zeroOrOne = numberZeroOrMore.typeAlternative(numberZeroOrOne);
        final var $zeroOrMore_zeroOrMore = numberZeroOrMore.typeAlternative(numberZeroOrMore);
        final var $zeroOrMore_oneOrMore = numberZeroOrMore.typeAlternative(numberOneOrMore);

        assertEquals($zeroOrMore_0, numberZeroOrMore);
        assertEquals($zeroOrMore_1, numberZeroOrMore);
        assertEquals($zeroOrMore_zeroOrOne, numberZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, numberZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, numberZeroOrMore);

        final var $oneOrMore_0 = numberOneOrMore.typeAlternative(empty);
        final var $oneOrMore_1 = numberOneOrMore.typeAlternative(number);
        final var $oneOrMore_zeroOrOne = numberOneOrMore.typeAlternative(numberZeroOrOne);
        final var $oneOrMore_zeroOrMore = numberOneOrMore.typeAlternative(numberZeroOrMore);
        final var $oneOrMore_oneOrMore = numberOneOrMore.typeAlternative(numberOneOrMore);

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

        final var $00 = empty.unionMerge(empty);
        final var $01 = empty.unionMerge(node);
        final var $0_zeroOrOne = empty.unionMerge(nodeZeroOrOne);
        final var $0_zeroOrMore = empty.unionMerge(nodeZeroOrMore);
        final var $0_oneOrMore = empty.unionMerge(nodeOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, node);
        assertEquals($0_zeroOrOne, nodeZeroOrOne);
        assertEquals($0_zeroOrMore, nodeZeroOrMore);
        assertEquals($0_oneOrMore, nodeOneOrMore);

        final var $10 = node.unionMerge(empty);
        final var $11 = node.unionMerge(node);
        final var $1_zeroOrOne = node.unionMerge(nodeZeroOrOne);
        final var $1_zeroOrMore = node.unionMerge(nodeZeroOrMore);
        final var $1_oneOrMore = node.unionMerge(nodeOneOrMore);

        assertEquals($10, node);
        assertEquals($11, nodeOneOrMore);
        assertEquals($1_zeroOrOne, nodeOneOrMore);
        assertEquals($1_zeroOrMore, nodeOneOrMore);
        assertEquals($1_oneOrMore, nodeOneOrMore);

        final var $zeroOrOne_0 = nodeZeroOrOne.unionMerge(empty);
        final var $zeroOrOne_1 = nodeZeroOrOne.unionMerge(node);
        final var $zeroOrOne_zeroOrOne = nodeZeroOrOne.unionMerge(nodeZeroOrOne);
        final var $zeroOrOne_zeroOrMore = nodeZeroOrOne.unionMerge(nodeZeroOrMore);
        final var $zeroOrOne_oneOrMore = nodeZeroOrOne.unionMerge(nodeOneOrMore);

        assertEquals($zeroOrOne_0, nodeZeroOrOne);
        assertEquals($zeroOrOne_1, nodeOneOrMore);
        assertEquals($zeroOrOne_zeroOrOne, nodeZeroOrMore);
        assertEquals($zeroOrOne_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrOne_oneOrMore, nodeOneOrMore);

        final var $zeroOrMore_0 = nodeZeroOrMore.unionMerge(empty);
        final var $zeroOrMore_1 = nodeZeroOrMore.unionMerge(node);
        final var $zeroOrMore_zeroOrOne = nodeZeroOrMore.unionMerge(nodeZeroOrOne);
        final var $zeroOrMore_zeroOrMore = nodeZeroOrMore.unionMerge(nodeZeroOrMore);
        final var $zeroOrMore_oneOrMore = nodeZeroOrMore.unionMerge(nodeOneOrMore);

        assertEquals($zeroOrMore_0, nodeZeroOrMore);
        assertEquals($zeroOrMore_1, nodeOneOrMore);
        assertEquals($zeroOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, nodeOneOrMore);

        final var $oneOrMore_0 = nodeOneOrMore.unionMerge(empty);
        final var $oneOrMore_1 = nodeOneOrMore.unionMerge(node);
        final var $oneOrMore_zeroOrOne = nodeOneOrMore.unionMerge(nodeZeroOrOne);
        final var $oneOrMore_zeroOrMore = nodeOneOrMore.unionMerge(nodeZeroOrMore);
        final var $oneOrMore_oneOrMore = nodeOneOrMore.unionMerge(nodeOneOrMore);

        assertEquals($oneOrMore_0, nodeOneOrMore);
        assertEquals($oneOrMore_1, nodeOneOrMore);
        assertEquals($oneOrMore_zeroOrOne, nodeOneOrMore);
        assertEquals($oneOrMore_zeroOrMore, nodeOneOrMore);
        assertEquals($oneOrMore_oneOrMore, nodeOneOrMore);


        final var elementFoo = typeFactory.element(Set.of("foo"));
        final var elementBar = typeFactory.element(Set.of("bar"));
        final var merged$elements = elementFoo.unionMerge(elementBar);
        assertEquals(merged$elements, typeFactory.oneOrMore(typeFactory.itemElement(Set.of("foo", "bar"))));

        final var merged$any = elementFoo.unionMerge(anyNode);
        assertEquals(merged$any, nodeOneOrMore);

        final var merged$any2 = anyNode.unionMerge(elementFoo);
        assertEquals(merged$any2, nodeOneOrMore);
    }


    @Test
    public void intersectNodeMerging() {
        final var empty = typeFactory.emptySequence();
        final var node = typeFactory.anyNode();
        final var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        final var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());

        final var $00 = empty.intersectionMerge(empty);
        final var $01 = empty.intersectionMerge(node);
        final var $0_zeroOrOne = empty.intersectionMerge(nodeZeroOrOne);
        final var $0_zeroOrMore = empty.intersectionMerge(nodeZeroOrMore);
        final var $0_oneOrMore = empty.intersectionMerge(nodeOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, empty);
        assertEquals($0_zeroOrOne, empty);
        assertEquals($0_zeroOrMore, empty);
        assertEquals($0_oneOrMore, empty);

        final var $10 = node.intersectionMerge(empty);
        final var $11 = node.intersectionMerge(node);
        final var $1_zeroOrOne = node.intersectionMerge(nodeZeroOrOne);
        final var $1_zeroOrMore = node.intersectionMerge(nodeZeroOrMore);
        final var $1_oneOrMore = node.intersectionMerge(nodeOneOrMore);

        assertEquals($10, empty);
        assertEquals($11, nodeZeroOrOne);
        assertEquals($1_zeroOrOne, nodeZeroOrOne);
        assertEquals($1_zeroOrMore, nodeZeroOrOne);
        assertEquals($1_oneOrMore, nodeZeroOrOne);

        final var $zeroOrOne_0 = nodeZeroOrOne.intersectionMerge(empty);
        final var $zeroOrOne_1 = nodeZeroOrOne.intersectionMerge(node);
        final var $zeroOrOne_zeroOrOne = nodeZeroOrOne.intersectionMerge(nodeZeroOrOne);
        final var $zeroOrOne_zeroOrMore = nodeZeroOrOne.intersectionMerge(nodeZeroOrMore);
        final var $zeroOrOne_oneOrMore = nodeZeroOrOne.intersectionMerge(nodeOneOrMore);

        assertEquals($zeroOrOne_0, empty);
        assertEquals($zeroOrOne_1, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrOne, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrMore, nodeZeroOrOne);
        assertEquals($zeroOrOne_oneOrMore, nodeZeroOrOne);

        final var $zeroOrMore_0 = nodeZeroOrMore.intersectionMerge(empty);
        final var $zeroOrMore_1 = nodeZeroOrMore.intersectionMerge(node);
        final var $zeroOrMore_zeroOrOne = nodeZeroOrMore.intersectionMerge(nodeZeroOrOne);
        final var $zeroOrMore_zeroOrMore = nodeZeroOrMore.intersectionMerge(nodeZeroOrMore);
        final var $zeroOrMore_oneOrMore = nodeZeroOrMore.intersectionMerge(nodeOneOrMore);

        assertEquals($zeroOrMore_0, empty);
        assertEquals($zeroOrMore_1, nodeZeroOrOne);
        assertEquals($zeroOrMore_zeroOrOne, nodeZeroOrOne);
        assertEquals($zeroOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, nodeZeroOrMore);

        final var $oneOrMore_0 = nodeOneOrMore.intersectionMerge(empty);
        final var $oneOrMore_1 = nodeOneOrMore.intersectionMerge(node);
        final var $oneOrMore_zeroOrOne = nodeOneOrMore.intersectionMerge(nodeZeroOrOne);
        final var $oneOrMore_zeroOrMore = nodeOneOrMore.intersectionMerge(nodeZeroOrMore);
        final var $oneOrMore_oneOrMore = nodeOneOrMore.intersectionMerge(nodeOneOrMore);

        assertEquals($oneOrMore_0, empty);
        assertEquals($oneOrMore_1, nodeZeroOrOne);
        assertEquals($oneOrMore_zeroOrOne, nodeZeroOrOne);
        assertEquals($oneOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($oneOrMore_oneOrMore, nodeZeroOrMore);


        final var elementFoo = typeFactory.element(Set.of("foo", "x"));
        final var elementBar = typeFactory.element(Set.of("bar", "x"));
        final var merged$elements = elementFoo.intersectionMerge(elementBar);
        assertEquals(merged$elements, typeFactory.zeroOrOne(typeFactory.itemElement(Set.of("x"))));

        final var merged$any = elementFoo.intersectionMerge(anyNode);
        assertEquals(merged$any, typeFactory.zeroOrOne(typeFactory.itemElement(Set.of("foo", "x"))));

        final var merged$any2 = anyNode.intersectionMerge(elementFoo);
        assertEquals(merged$any2, typeFactory.zeroOrOne(typeFactory.itemElement(Set.of("foo", "x"))));
    }

    @Test
    public void exceptNodeMerging() {
        final var empty = typeFactory.emptySequence();
        final var node = typeFactory.anyNode();
        final var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        final var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());

        final var $00 = empty.exceptionMerge(empty);
        final var $01 = empty.exceptionMerge(node);
        final var $0_zeroOrOne = empty.exceptionMerge(nodeZeroOrOne);
        final var $0_zeroOrMore = empty.exceptionMerge(nodeZeroOrMore);
        final var $0_oneOrMore = empty.exceptionMerge(nodeOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, empty);
        assertEquals($0_zeroOrOne, empty);
        assertEquals($0_zeroOrMore, empty);
        assertEquals($0_oneOrMore, empty);

        final var $10 = node.exceptionMerge(empty);
        final var $11 = node.exceptionMerge(node);
        final var $1_zeroOrOne = node.exceptionMerge(nodeZeroOrOne);
        final var $1_zeroOrMore = node.exceptionMerge(nodeZeroOrMore);
        final var $1_oneOrMore = node.exceptionMerge(nodeOneOrMore);

        assertEquals($10, node);
        assertEquals($11, nodeZeroOrOne);
        assertEquals($1_zeroOrOne, nodeZeroOrOne);
        assertEquals($1_zeroOrMore, nodeZeroOrOne);
        assertEquals($1_oneOrMore, nodeZeroOrOne);

        final var $zeroOrOne_0 = nodeZeroOrOne.exceptionMerge(empty);
        final var $zeroOrOne_1 = nodeZeroOrOne.exceptionMerge(node);
        final var $zeroOrOne_zeroOrOne = nodeZeroOrOne.exceptionMerge(nodeZeroOrOne);
        final var $zeroOrOne_zeroOrMore = nodeZeroOrOne.exceptionMerge(nodeZeroOrMore);
        final var $zeroOrOne_oneOrMore = nodeZeroOrOne.exceptionMerge(nodeOneOrMore);

        assertEquals($zeroOrOne_0, nodeZeroOrOne);
        assertEquals($zeroOrOne_1, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrOne, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrMore, nodeZeroOrOne);
        assertEquals($zeroOrOne_oneOrMore, nodeZeroOrOne);

        final var $zeroOrMore_0 = nodeZeroOrMore.exceptionMerge(empty);
        final var $zeroOrMore_1 = nodeZeroOrMore.exceptionMerge(node);
        final var $zeroOrMore_zeroOrOne = nodeZeroOrMore.exceptionMerge(nodeZeroOrOne);
        final var $zeroOrMore_zeroOrMore = nodeZeroOrMore.exceptionMerge(nodeZeroOrMore);
        final var $zeroOrMore_oneOrMore = nodeZeroOrMore.exceptionMerge(nodeOneOrMore);

        assertEquals($zeroOrMore_0, nodeZeroOrMore);
        assertEquals($zeroOrMore_1, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, nodeZeroOrMore);

        final var $oneOrMore_0 = nodeOneOrMore.exceptionMerge(empty);
        final var $oneOrMore_1 = nodeOneOrMore.exceptionMerge(node);
        final var $oneOrMore_zeroOrOne = nodeOneOrMore.exceptionMerge(nodeZeroOrOne);
        final var $oneOrMore_zeroOrMore = nodeOneOrMore.exceptionMerge(nodeZeroOrMore);
        final var $oneOrMore_oneOrMore = nodeOneOrMore.exceptionMerge(nodeOneOrMore);

        assertEquals($oneOrMore_0, nodeOneOrMore);
        assertEquals($oneOrMore_1, nodeZeroOrMore);
        assertEquals($oneOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($oneOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($oneOrMore_oneOrMore, nodeZeroOrMore);

        final var elementFoo = typeFactory.element(Set.of("foo"));
        final var elementBar = typeFactory.element(Set.of("bar"));
        final var merged$elements = elementFoo.exceptionMerge(elementBar);
        assertEquals(merged$elements, typeFactory.zeroOrOne(typeFactory.itemElement(Set.of("foo"))));

        final var merged$any = elementFoo.exceptionMerge(anyNode);
        assertEquals(merged$any, typeFactory.zeroOrOne(typeFactory.itemElement(Set.of("foo"))));

        final var merged$any2 = anyNode.exceptionMerge(elementFoo);
        assertEquals(merged$any2, typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
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
        final var a_number_b_string = typeFactory.extensibleRecord(
            Map.of("a", numberRequired)
        );
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

        // record(x?, y?, z?, *) ⊆ record(x, y, *)

        // record(x as xs:integer, y as xs:integer, *) ⊆ record(x as xs:decimal, y as xs:integer*, *)

        // record(x as xs:integer, *) ⊆ record(x as xs:decimal, y as item(), *)

        // All of the following are true:

        // A is a non-extensible record type.

        // B is an extensible record type.

        // Every mandatory field in B is also declared as mandatory in A.

        // For every field that is declared in both A and B, where the declared type in A is T and the declared type in B is U, T ⊑ U .

        // Examples:
        // record(x, y as xs:integer) ⊆ record(x, y as xs:decimal, *)

        // record(y as xs:integer) ⊆ record(x?, y as xs:decimal, *)
    }



}
