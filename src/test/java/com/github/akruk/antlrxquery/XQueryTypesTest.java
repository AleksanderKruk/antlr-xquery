package com.github.akruk.antlrxquery;

import org.junit.Test;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import typesystem.defaults.XQueryEnumBasedType;
import typesystem.defaults.XQueryOccurence;
import static org.junit.Assert.assertEquals;

import javax.print.DocFlavor.STRING;

import static org.junit.Assert.*;

public class XQueryTypesTest {

    final XQueryEnumBasedType string = XQueryEnumBasedType.string();
    final XQueryEnumBasedType integer = XQueryEnumBasedType.integer();
    final XQueryEnumBasedType number = XQueryEnumBasedType.number();
    final XQueryEnumBasedType anyNode = XQueryEnumBasedType.anyNode();
    final XQueryEnumBasedType emptySequence = XQueryEnumBasedType.emptySequence();
    final XQueryEnumBasedType stringSequenceOneOrMore = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string, XQueryOccurence.ONE_OR_MORE);
    final XQueryEnumBasedType stringSequenceZeroOrMore = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string, XQueryOccurence.ZERO_OR_MORE);
    final XQueryEnumBasedType stringSequenceZeroOrOne = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string, XQueryOccurence.ZERO_OR_ONE);

    @Test
    public void stringDirectEquality() throws XQueryUnsupportedOperation {
        assertEquals(string, XQueryEnumBasedType.string());
        assertNotEquals(string, integer);
        assertNotEquals(string, number);
        assertNotEquals(string, emptySequence);
        assertNotEquals(string, stringSequenceOneOrMore);
        assertNotEquals(string, stringSequenceZeroOrMore);
        assertNotEquals(string, stringSequenceZeroOrOne);
    }

    @Test
    public void stringIsSubtypeOf() throws XQueryUnsupportedOperation {
        assertTrue(string.isSubtypeOf(XQueryEnumBasedType.string()));
        assertTrue(string.isSubtypeOf(string));
        assertFalse(string.isSubtypeOf(number));
        assertFalse(string.isSubtypeOf(emptySequence));
        assertTrue(string.isSubtypeOf(stringSequenceOneOrMore));
        assertTrue(string.isSubtypeOf(stringSequenceZeroOrMore));
        assertTrue(string.isSubtypeOf(stringSequenceZeroOrOne));
    }
}
