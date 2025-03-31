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
import typesystem.defaults.XQueryTypes;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import static org.junit.Assert.*;

public class XQueryTypesTest {
    @Test
    public void directEquality() throws XQueryUnsupportedOperation {
        var type1 = new XQueryEnumBasedType(XQueryTypes.STRING,
                null, null, null,
                        null, null);
        assertTrue();
    }
}
