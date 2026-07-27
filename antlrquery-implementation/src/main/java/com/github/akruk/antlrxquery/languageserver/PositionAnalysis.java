package com.github.akruk.antlrxquery.languageserver;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

public record PositionAnalysis(
    ParserRuleContext innerMostContext,
    List<ParserRuleContext> contextStack
) {}
