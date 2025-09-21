package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.lsp4j.Position;
import org.junit.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrxquery.languageserver.PositionAnalysis;
import com.github.akruk.antlrxquery.languageserver.PositionAnalyzer;

import static org.junit.Assert.*;

public class PositionalAnalysisTest extends EvaluationTestsBase {

    @Test
    public void testPositionAnalysis() {
        String xquery = "(1, 2, 3) ! (. + 1)";
        AntlrXqueryParser parser = new AntlrXqueryParser(new CommonTokenStream(new AntlrXqueryLexer(CharStreams.fromString(xquery))));
        var tree = parser.xquery();
        var analyzer = new PositionAnalyzer(new Position(1, 4));
        PositionAnalysis analysis = analyzer.visit(tree);
        assertNotNull(analysis);
        assertNotNull(analysis.innerMostContext());
        assertNotNull(analysis.contextStack());
        assertFalse(analysis.contextStack().isEmpty());

    }
}
