package com.github.akruk.antlrxquery;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.AntlrXqueryParser.ParenthesizedExprContext;
import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

public class antlrtest {

    @Test
    public void myTest() {
        XQueryEnumTypeFactory typeFactory = new XQueryEnumTypeFactory();
        var sem = new XQuerySemanticAnalyzer(null, new XQueryBaseSemanticContextManager(), typeFactory, new XQueryMemoizedValueFactory(), new XQuerySemanticFunctionManager(typeFactory));
        var tree = new ParenthesizedExprContext(null, 0);
        var result = sem.visit(tree);
        assertEquals(typeFactory.emptySequence(), result);



    }
}
