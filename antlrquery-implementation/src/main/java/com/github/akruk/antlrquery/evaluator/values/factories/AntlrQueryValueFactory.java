package com.github.akruk.antlrquery.evaluator.values.factories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryFunction;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;

public interface AntlrQueryValueFactory {
    AntlrQueryValue error(AntlrQueryError error, String message);
    AntlrQueryValue bool(boolean v);

    AntlrQueryValue node(String grammar, ParseTree v);

    AntlrQueryValue number(BigDecimal d);
    AntlrQueryValue number(int integer);
    AntlrQueryValue string(String s);
    AntlrQueryValue emptyString();
    AntlrQueryValue sequence(List<AntlrQueryValue> v);
    AntlrQueryValue emptySequence();
    AntlrQueryValue functionReference(AntlrQueryFunction f, AntlrQuerySequenceType type);
    AntlrQueryValue array(List<AntlrQueryValue> value);
    AntlrQueryValue map(Map<AntlrQueryValue, AntlrQueryValue> value);
    AntlrQueryValue record(Map<String, AntlrQueryValue> value);
}
