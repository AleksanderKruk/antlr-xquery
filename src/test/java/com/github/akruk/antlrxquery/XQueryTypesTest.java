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

    final XQueryItemType itemAnyItem = typeFactory.itemAnyItem();

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
        assertTrue(emptySequence.isSubtypeOf(typeFactory.anyItem()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemAnyItem)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemAnyItem)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemAnyItem)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.string()));
        XQueryItemType itemString = typeFactory.itemString();
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemString)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemString)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemString)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.number()));
        XQueryItemType itemNumber = typeFactory.itemNumber();
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemNumber)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemNumber)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemNumber)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.boolean_()));
        XQueryItemType itemBoolean = typeFactory.itemBoolean();
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemBoolean)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemBoolean)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemBoolean)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.anyNode()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(typeFactory.itemAnyNode())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemAnyNode())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.oneOrMore(typeFactory.itemAnyNode())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.anyItem()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemAnyItem)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemAnyItem)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemAnyItem)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.anyElement()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(typeFactory.itemAnyElement())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemAnyElement())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.oneOrMore(typeFactory.itemAnyElement())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.anyMap()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(typeFactory.itemAnyMap())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemAnyMap())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.oneOrMore(typeFactory.itemAnyMap())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.anyMap()));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(typeFactory.itemAnyMap())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemAnyMap())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.oneOrMore(typeFactory.itemAnyMap())));

        //
        assertTrue(emptySequence.isSubtypeOf(typeFactory.map(itemString, typeFactory.anyItem())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.map(itemNumber, typeFactory.anyItem())));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.map(itemBoolean, typeFactory.anyItem())));
        //
        assertTrue(emptySequence.isSubtypeOf(typeFactory.anyArray()));
        XQueryItemType itemAnyArray = typeFactory.itemAnyArray();
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemAnyArray)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemAnyArray)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemAnyArray)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.array(typeFactory.anyItem())));
        //

        assertTrue(emptySequence.isSubtypeOf(typeFactory.anyFunction()));

        XQueryItemType itemAnyFunction = typeFactory.itemAnyFunction();
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrOne(itemAnyFunction)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.zeroOrMore(itemAnyFunction)));
        assertTrue(emptySequence.isSubtypeOf(typeFactory.oneOrMore(itemAnyFunction)));
        // assertTrue(emptySequence.isSubtypeOf(typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(emptySequence.isSubtypeOf(typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(emptySequence.isSubtypeOf(function(T) as R))
        // assertTrue(emptySequence.isSubtypeOf(function(T1, T2) as R))

    }
}
