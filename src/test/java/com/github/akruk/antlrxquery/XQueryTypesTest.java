package com.github.akruk.antlrxquery;

import org.junit.Test;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeString;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.*;

public class XQueryTypesTest {

    XQueryTypeFactory typeFactory = new XQueryEnumTypeFactory();
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
    final XQueryItemType itemAnyFunction = typeFactory.itemAnyFunction();
    final XQueryItemType itemAnyItem = typeFactory.itemAnyItem();
    final XQueryItemType itemString = typeFactory.itemString();
    final XQueryItemType itemNumber = typeFactory.itemNumber();
    final XQueryItemType itemBoolean = typeFactory.itemBoolean();
    final XQueryItemType itemAnyNode = typeFactory.itemAnyNode();
    final XQueryItemType itemAnyElement = typeFactory.itemAnyElement();
    final XQueryItemType itemAnyMap = typeFactory.itemAnyMap();
    final XQueryItemType itemAnyArray = typeFactory.itemAnyArray();
    final XQueryItemType fooElementItem = typeFactory.itemElement("foo");
    final XQuerySequenceType fooElement = typeFactory.element("foo");



    @Test
    public void stringDirectEquality() throws XQueryUnsupportedOperation {
        assertEquals(string, new XQueryEnumItemTypeString());
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

        assertFalse(emptySequence.isSubtypeOf(typeFactory.anyElement()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemAnyElement)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemAnyElement)));
        assertFalse(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemAnyElement)));

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

        assertFalse(number.isSubtypeOf(typeFactory.anyElement()));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrOne(itemAnyElement)));
        assertFalse(number.isSubtypeOf(typeFactory.zeroOrMore(itemAnyElement)));
        assertFalse(number.isSubtypeOf(typeFactory.oneOrMore(itemAnyElement)));

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
        assertFalse(boolean.isSubtypeOf(typeFactory.emptySequence()));

        assertTrue(boolean.isSubtypeOf(typeFactory.anyItem()));
        assertTrue(boolean.isSubtypeOf(typeFactory.zeroOrOne(itemAnyItem)));
        assertTrue(boolean.isSubtypeOf(typeFactory.zeroOrMore(itemAnyItem)));
        assertTrue(boolean.isSubtypeOf(typeFactory.oneOrMore(itemAnyItem)));

        assertFalse(boolean.isSubtypeOf(typeFactory.string()));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrOne(itemString)));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrMore(itemString)));
        assertFalse(boolean.isSubtypeOf(typeFactory.oneOrMore(itemString)));

        assertTrue(boolean.isSubtypeOf(typeFactory.boolean()));
        assertTrue(boolean.isSubtypeOf(typeFactory.zeroOrOne(itemboolean)));
        assertTrue(boolean.isSubtypeOf(typeFactory.zeroOrMore(itemboolean)));
        assertTrue(boolean.isSubtypeOf(typeFactory.oneOrMore(itemboolean)));

        assertFalse(boolean.isSubtypeOf(typeFactory.boolean_()));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrOne(itemBoolean)));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrMore(itemBoolean)));
        assertFalse(boolean.isSubtypeOf(typeFactory.oneOrMore(itemBoolean)));

        assertFalse(boolean.isSubtypeOf(typeFactory.anyNode()));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrOne(itemAnyNode)));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrMore(itemAnyNode)));
        assertFalse(boolean.isSubtypeOf(typeFactory.oneOrMore(itemAnyNode)));

        assertFalse(boolean.isSubtypeOf(typeFactory.anyElement()));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrOne(itemAnyElement)));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrMore(itemAnyElement)));
        assertFalse(boolean.isSubtypeOf(typeFactory.oneOrMore(itemAnyElement)));

        assertFalse(boolean.isSubtypeOf(fooElement));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrOne(fooElementItem)));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrMore(fooElementItem)));
        assertFalse(boolean.isSubtypeOf(typeFactory.oneOrMore(fooElementItem)));

        assertFalse(boolean.isSubtypeOf(typeFactory.anyMap()));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrOne(itemAnyMap)));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrMore(itemAnyMap)));
        assertFalse(boolean.isSubtypeOf(typeFactory.oneOrMore(itemAnyMap)));


        assertFalse(boolean.isSubtypeOf(typeFactory.map(itemString, typeFactory.anyItem())));
        assertFalse(boolean.isSubtypeOf(typeFactory.map(itemboolean, typeFactory.anyItem())));
        assertFalse(boolean.isSubtypeOf(typeFactory.map(itemBoolean, typeFactory.anyItem())));


        assertFalse(boolean.isSubtypeOf(typeFactory.anyArray()));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrOne(itemAnyArray)));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrMore(itemAnyArray)));
        assertFalse(boolean.isSubtypeOf(typeFactory.oneOrMore(itemAnyArray)));

        assertFalse(boolean.isSubtypeOf(typeFactory.anyFunction()));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrOne(itemAnyFunction)));
        assertFalse(boolean.isSubtypeOf(typeFactory.zeroOrMore(itemAnyFunction)));
        assertFalse(boolean.isSubtypeOf(typeFactory.oneOrMore(itemAnyFunction)));
        // assertTrue(boolean.isSubtypeOf(typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(boolean.isSubtypeOf(typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(boolean.isSubtypeOf(function(T) as R))
        // assertTrue(boolean.isSubtypeOf(function(T1, T2) as R))

    }



}
