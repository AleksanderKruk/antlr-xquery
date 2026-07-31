package com.github.akruk.antlrquery;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.lsp4j.Position;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrquery.languageserver.PositionAnalysis;
import com.github.akruk.antlrquery.languageserver.PositionAnalyzer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class PositionalAnalysisTest extends EvaluationTestsBase {

    @Test
    public void testPositionAnalysis() {
        String xquery = "(1, 2, 3) ! (. + 1)";
        AntlrQueryParser parser = new AntlrQueryParser(new CommonTokenStream(new AntlrQueryLexer(CharStreams.fromString(xquery))));
        var tree = parser.xquery();
        var analyzer = new PositionAnalyzer(new Position(1, 4));
        PositionAnalysis analysis = analyzer.visit(tree);
        assertNotNull(analysis);
        assertNotNull(analysis.innerMostContext());
        assertNotNull(analysis.contextStack());
        assertFalse(analysis.contextStack().isEmpty());

    }
}
