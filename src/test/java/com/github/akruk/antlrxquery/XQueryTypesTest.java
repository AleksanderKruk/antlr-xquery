package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.testgrammars.TestLexer;
import com.github.akruk.antlrxquery.testgrammars.TestParser;
import com.github.akruk.antlrxquery.values.XQueryBoolean;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQueryString;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryBaseValueFactory;

import typesystem.defaults.XQueryEnumBasedType;
import typesystem.defaults.XQueryOccurence;
import typesystem.defaults.XQueryTypes;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import static org.junit.Assert.*;

public class XQueryTypesTest {

    final XQueryEnumBasedType string = XQueryEnumBasedType.string();
    final XQueryEnumBasedType integer = XQueryEnumBasedType.integer();
    final XQueryEnumBasedType number = XQueryEnumBasedType.number();
    final XQueryEnumBasedType anyNode = XQueryEnumBasedType.anyNode();
    final XQueryEnumBasedType emptySequence = XQueryEnumBasedType.emptySequence();
    final XQueryEnumBasedType stringSequenceOneOrMore = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string(), XQueryOccurence.ONE_OR_MORE);
    final XQueryEnumBasedType stringSequenceZeroOrMore = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string(), XQueryOccurence.ZERO_OR_MORE);
    final XQueryEnumBasedType stringSequenceZeroOrOne = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string(), XQueryOccurence.ZERO_OR_ONE);
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
}
