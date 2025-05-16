package com.github.akruk.antlrxquery;

import org.junit.Test;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import static org.junit.Assert.assertEquals;

import java.util.List;
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
    final XQueryItemType itemError = typeFactory.itemError();
    final XQueryItemType itemAnyFunction = typeFactory.itemAnyFunction();
    final XQueryItemType itemAnyItem = typeFactory.itemAnyItem();
    final XQueryItemType itemString = typeFactory.itemString();
    final XQueryItemType itemNumber = typeFactory.itemNumber();
    final XQueryItemType itemBoolean = typeFactory.itemBoolean();
    final XQueryItemType itemAnyNode = typeFactory.itemAnyNode();
    final XQueryItemType itemAnyMap = typeFactory.itemAnyMap();
    final XQueryItemType itemAnyArray = typeFactory.itemAnyArray();
    final XQueryItemType fooElementItem = typeFactory.itemElement(Set.of("foo"));
    final XQueryItemType barElementItem = typeFactory.itemElement(Set.of("bar"));
    final XQueryItemType itemABenum = typeFactory.itemEnum(Set.of("A", "B"));
    final XQueryItemType itemABCenum = typeFactory.itemEnum(Set.of("A", "B", "C"));
    final XQueryItemType itemABCDenum = typeFactory.itemEnum(Set.of("A", "B", "C", "D"));



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
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(fooElementItem)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(fooElementItem)));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.oneOrMore(fooElementItem)));

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
    public void numberSubtyping() {
        assertFalse(number.isSubtypeOf(typeFactory.emptySequence()));

        assertTrue(number.isSubtypeOf(typeFactory.anyItem()));
        assertTrue(number.isSubtypeOf(typeFactory.zeroOrOne(itemAnyItem)));
        assertTrue(number.isSubtypeOf(typeFactory.zeroOrMore(itemAnyItem)));
        assertTrue(number.isSubtypeOf(typeFactory.oneOrMore(itemAnyItem)));

        assertFalse(number.isSubtypeOf(typeFactory.string()));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrOne(itemString)));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrMore(itemString)));
        assertFalse(number.isSubtypeOf(typeFactory.oneOrMore(itemString)));

        assertTrue(number.isSubtypeOf(typeFactory.number()));
        assertTrue(number.isSubtypeOf(typeFactory.zeroOrOne(itemNumber)));
        assertTrue(number.isSubtypeOf(typeFactory.zeroOrMore(itemNumber)));
        assertTrue(number.isSubtypeOf(typeFactory.oneOrMore(itemNumber)));

        assertFalse(number.isSubtypeOf(typeFactory.boolean_()));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrOne(itemBoolean)));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrMore(itemBoolean)));
        assertFalse(number.isSubtypeOf(typeFactory.oneOrMore(itemBoolean)));

        assertFalse(number.isSubtypeOf(typeFactory.anyNode()));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrOne(itemAnyNode)));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrMore(itemAnyNode)));
        assertFalse(number.isSubtypeOf(typeFactory.oneOrMore(itemAnyNode)));

        assertFalse(number.isSubtypeOf(fooElement));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrOne(fooElementItem)));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrMore(fooElementItem)));
        assertFalse(number.isSubtypeOf(typeFactory.oneOrMore(fooElementItem)));

        assertFalse(number.isSubtypeOf(typeFactory.anyMap()));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrOne(itemAnyMap)));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrMore(itemAnyMap)));
        assertFalse(number.isSubtypeOf(typeFactory.oneOrMore(itemAnyMap)));


        assertFalse(number.isSubtypeOf(typeFactory.map(itemString, typeFactory.anyItem())));
        assertFalse(number.isSubtypeOf(typeFactory.map(itemNumber, typeFactory.anyItem())));
        assertFalse(number.isSubtypeOf(typeFactory.map(itemBoolean, typeFactory.anyItem())));


        assertFalse(number.isSubtypeOf(typeFactory.anyArray()));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrOne(itemAnyArray)));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrMore(itemAnyArray)));
        assertFalse(number.isSubtypeOf(typeFactory.oneOrMore(itemAnyArray)));

        assertFalse(number.isSubtypeOf(typeFactory.anyFunction()));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrOne(itemAnyFunction)));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrMore(itemAnyFunction)));
        assertFalse(number.isSubtypeOf(typeFactory.oneOrMore(itemAnyFunction)));
        // assertTrue(number.isSubtypeOf(typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(number.isSubtypeOf(typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(number.isSubtypeOf(function(T) as R))
        // assertTrue(number.isSubtypeOf(function(T1, T2) as R))

    }

    @Test
    public void booleanSubtyping() {
        assertFalse(boolean_.isSubtypeOf(typeFactory.emptySequence()));

        assertTrue(boolean_.isSubtypeOf(typeFactory.anyItem()));
        assertTrue(boolean_.isSubtypeOf(typeFactory.zeroOrOne(itemAnyItem)));
        assertTrue(boolean_.isSubtypeOf(typeFactory.zeroOrMore(itemAnyItem)));
        assertTrue(boolean_.isSubtypeOf(typeFactory.oneOrMore(itemAnyItem)));

        assertFalse(boolean_.isSubtypeOf(typeFactory.string()));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrOne(itemString)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrMore(itemString)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.oneOrMore(itemString)));

        assertFalse(boolean_.isSubtypeOf(typeFactory.number()));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrOne(itemNumber)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrMore(itemNumber)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.oneOrMore(itemNumber)));

        assertTrue(boolean_.isSubtypeOf(typeFactory.boolean_()));
        assertTrue(boolean_.isSubtypeOf(typeFactory.zeroOrOne(itemBoolean)));
        assertTrue(boolean_.isSubtypeOf(typeFactory.zeroOrMore(itemBoolean)));
        assertTrue(boolean_.isSubtypeOf(typeFactory.oneOrMore(itemBoolean)));

        assertFalse(boolean_.isSubtypeOf(typeFactory.anyNode()));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrOne(itemAnyNode)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrMore(itemAnyNode)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.oneOrMore(itemAnyNode)));

        assertFalse(boolean_.isSubtypeOf(fooElement));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrOne(fooElementItem)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrMore(fooElementItem)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.oneOrMore(fooElementItem)));

        assertFalse(boolean_.isSubtypeOf(typeFactory.anyMap()));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrOne(itemAnyMap)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrMore(itemAnyMap)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.oneOrMore(itemAnyMap)));


        assertFalse(boolean_.isSubtypeOf(typeFactory.map(itemString, typeFactory.anyItem())));
        assertFalse(boolean_.isSubtypeOf(typeFactory.map(itemBoolean, typeFactory.anyItem())));
        assertFalse(boolean_.isSubtypeOf(typeFactory.map(itemBoolean, typeFactory.anyItem())));


        assertFalse(boolean_.isSubtypeOf(typeFactory.anyArray()));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrOne(itemAnyArray)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrMore(itemAnyArray)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.oneOrMore(itemAnyArray)));

        assertFalse(boolean_.isSubtypeOf(typeFactory.anyFunction()));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrOne(itemAnyFunction)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.zeroOrMore(itemAnyFunction)));
        assertFalse(boolean_.isSubtypeOf(typeFactory.oneOrMore(itemAnyFunction)));
        // assertTrue(boolean_.isSubtypeOf(typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(boolean_.isSubtypeOf(typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(boolean_.isSubtypeOf(function(T) as R))
        // assertTrue(boolean_.isSubtypeOf(function(T1, T2) as R))

    }


    @Test
    public void namedElementItemSubtyping() {
        final var tested = fooElementItem;

        assertFalse(tested.itemtypeIsSubtypeOf(itemError));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyItem));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyNode));
        assertTrue(tested.itemtypeIsSubtypeOf(fooElementItem));
        assertFalse(tested.itemtypeIsSubtypeOf(barElementItem));
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
        assertFalse(tested.itemtypeIsSubtypeOf(fooElementItem));
        assertFalse(tested.itemtypeIsSubtypeOf(fooElementItem));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertTrue(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

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
        assertFalse(tested.itemtypeIsSubtypeOf(fooElementItem));
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
        assertFalse(tested.itemtypeIsSubtypeOf(fooElementItem));
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
        assertFalse(tested.itemtypeIsSubtypeOf(fooElementItem));
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
        assertFalse(tested.itemtypeIsSubtypeOf(fooElementItem));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyMap));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyArray));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemArray(typeFactory.number())));
        assertFalse(tested.itemtypeIsSubtypeOf(itemAnyFunction));
        assertFalse(tested.itemtypeIsSubtypeOf(typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(tested.itemtypeIsSubtypeOf(itemBoolean));
        assertFalse(tested.itemtypeIsSubtypeOf(itemNumber));
        assertTrue(tested.itemtypeIsSubtypeOf(itemString));

        assertTrue(tested.itemtypeIsSubtypeOf(itemABCenum));
        assertTrue(tested.itemtypeIsSubtypeOf(itemABCDenum));
        assertFalse(itemABCDenum.itemtypeIsSubtypeOf(itemABCenum));
    }



    @Test
    public void anyArraySubtyping() {
        final var tested = typeFactory.anyArray();
        assertFalse(tested.isSubtypeOf(typeFactory.emptySequence()));

        assertTrue(tested.isSubtypeOf(typeFactory.anyItem()));
        assertTrue(tested.isSubtypeOf(typeFactory.zeroOrOne(itemAnyItem)));
        assertTrue(tested.isSubtypeOf(typeFactory.zeroOrMore(itemAnyItem)));
        assertTrue(tested.isSubtypeOf(typeFactory.oneOrMore(itemAnyItem)));

        assertFalse(tested.isSubtypeOf(typeFactory.string()));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrOne(itemString)));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrMore(itemString)));
        assertFalse(tested.isSubtypeOf(typeFactory.oneOrMore(itemString)));

        assertFalse(tested.isSubtypeOf(typeFactory.number()));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrOne(itemNumber)));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrMore(itemNumber)));
        assertFalse(tested.isSubtypeOf(typeFactory.oneOrMore(itemNumber)));

        assertFalse(tested.isSubtypeOf(typeFactory.boolean_()));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrOne(itemBoolean)));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrMore(itemBoolean)));
        assertFalse(tested.isSubtypeOf(typeFactory.oneOrMore(itemBoolean)));

        assertFalse(tested.isSubtypeOf(typeFactory.anyNode()));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrOne(itemAnyNode)));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrMore(itemAnyNode)));
        assertFalse(tested.isSubtypeOf(typeFactory.oneOrMore(itemAnyNode)));

        assertFalse(tested.isSubtypeOf(fooElement));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrOne(fooElementItem)));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrMore(fooElementItem)));
        assertFalse(tested.isSubtypeOf(typeFactory.oneOrMore(fooElementItem)));

        assertFalse(tested.isSubtypeOf(typeFactory.anyMap()));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrOne(itemAnyMap)));
        assertFalse(tested.isSubtypeOf(typeFactory.zeroOrMore(itemAnyMap)));
        assertFalse(tested.isSubtypeOf(typeFactory.oneOrMore(itemAnyMap)));

        assertFalse(tested.isSubtypeOf(typeFactory.map(itemString, typeFactory.anyItem())));
        assertFalse(tested.isSubtypeOf(typeFactory.map(itemNumber, typeFactory.anyItem())));
        assertFalse(tested.isSubtypeOf(typeFactory.map(itemBoolean, typeFactory.anyItem())));

        assertTrue(tested.isSubtypeOf(typeFactory.anyArray()));
        assertTrue(tested.isSubtypeOf(typeFactory.zeroOrOne(itemAnyArray)));
        assertTrue(tested.isSubtypeOf(typeFactory.zeroOrMore(itemAnyArray)));
        assertTrue(tested.isSubtypeOf(typeFactory.oneOrMore(itemAnyArray)));

        final XQuerySequenceType stringArray = typeFactory.array(typeFactory.string());
        assertFalse(tested.isSubtypeOf(stringArray));

        assertTrue(tested.isSubtypeOf(typeFactory.anyFunction()));
        assertTrue(tested.isSubtypeOf(typeFactory.function(typeFactory.anyItem(), List.of(typeFactory.number()))));
        assertTrue(tested.isSubtypeOf(
                typeFactory.function(typeFactory.zeroOrOne(typeFactory.itemAnyItem()), List.of(typeFactory.number()))));
        assertTrue(tested.isSubtypeOf(typeFactory.function(typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                List.of(typeFactory.number()))));
        assertTrue(tested.isSubtypeOf(
                typeFactory.function(typeFactory.oneOrMore(typeFactory.itemAnyItem()), List.of(typeFactory.number()))));

        // assertTrue(tested.isSubtypeOf(typeFactory.function(typeFactory.tested(), List.of())))
        // assertTrue(tested.isSubtypeOf(typeFactory.function(typeFactory.tested(), List.of())))
        // assertTrue(tested.isSubtypeOf(function(T) as R))
        // assertTrue(tested.isSubtypeOf(function(T1, T2) as R))
    }



}
