package com.github.akruk.antlrxquery;

import org.junit.Test;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
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
    final XQuerySequenceType recordAny = typeFactory.record(Map.of("foo", typeFactory.anyItem(), "bar", typeFactory.anyItem()));

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
    final XQueryItemType itemRecordAny = typeFactory.itemRecord(Map.of("foo", typeFactory.anyItem(), "bar", typeFactory.anyItem()));
    final XQueryItemType itemRecordString = typeFactory.itemRecord(Map.of("foo", typeFactory.string(), "bar", typeFactory.string()));



    @Test
    public void stringDirectEquality() throws XQueryUnsupportedOperation {
        assertEquals(string, string);
        assertNotEquals(string, number);
        assertNotEquals(string, emptySequence);
        assertNotEquals(string, stringSequenceOneOrMore);
        assertNotEquals(string, stringSequenceZeroOrMore);
        assertNotEquals(string, stringSequenceZeroOrOne);
    }

    @Test
    public void stringIsSubtypeOf() throws XQueryUnsupportedOperation {
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


    // @Test
    // public void mapItemSubtyping() {
    //     final var tested = typeFactory.itemMap(itemString, typeFactory.anyItem());

    //     assertFalse(tested.itemtypeIsSubtypeOf(itemError));
    //     assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
    //     assertFalse(tested.itemtypeIsSubtypeOf(itemAnyNode));
    //     assertFalse(tested.itemtypeIsSubtypeOf(fooElementItem));
    //     assertFalse(tested.itemtypeIsSubtypeOf(fooElementItem));
    //     assertTrue(tested.itemtypeIsSubtypeOf(itemAnyMap));
    //     assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
    //     assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
    //     assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
    //     assertTrue(tested.itemtypeIsSubtypeOf(itemAnyFunction));
    //     assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

    //     assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
    //     assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
    //     assertFalse(tested.itemtypeIsSubtypeOf(itemString));

    //     assertFalse(tested.itemtypeIsSubtypeOf(itemABCenum));
    // }


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



}
