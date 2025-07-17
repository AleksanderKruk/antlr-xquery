package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.text.Collator;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class ComparisonFunctions {

    private final XQueryValueFactory valueFactory;
    private final Parser targetParser;;

    public ComparisonFunctions(final XQueryValueFactory valueFactory, final Parser targetParser, final Map<String, Collator> collations) {
        this.valueFactory = valueFactory;
        this.targetParser = targetParser;
    }


    public XQueryValue atomicEqual(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        var arg1 = args.get(0);
        var arg2 = args.get(1);
        if (!arg1.isAtomic() || !arg2.isAtomic()) {
            return XQueryError.InvalidArgumentType;
        }
        var equality = arg1.valueEqual(arg2);
        if (!(equality instanceof XQueryError))
            return equality;
        var arg1Str = args.get(0).stringValue();
        var arg2Str = args.get(1).stringValue();
        return valueFactory.bool(arg1Str.equals(arg2Str));
    }


    public XQueryValue deepEqual(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        return null;
    }

    public XQueryValue compare(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        return null;
    }

    public XQueryValue distinctValues(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        return null;
    }

    public XQueryValue duplicateValues(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        return null;
    }

    public XQueryValue indexOf(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        return null;
    }

    public XQueryValue startsWithSubsequence(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        return null;
    }

    public XQueryValue endsWithSubsequence(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        return null;
    }

    public XQueryValue containsSubsequence(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        return null;
    }

}
