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
        var arg1Str = arg1.stringValue();
        var arg2Str = arg2.stringValue();
        return valueFactory.bool(arg1Str.equals(arg2Str));
    }


    public XQueryValue deepEqual(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        var arg1 = args.get(0);
        var arg2 = args.get(1);
        // TODO: implement options
        // var options = args.get(2);
        var elements1 = arg1.sequence();
        var elements2 = arg2.sequence();
        final int size1 = elements1.size();
        if (size1 != elements2.size())
            return valueFactory.bool(false);
        if (size1 == 1) {
            if (arg1.isAtomic() && arg2.isAtomic()) {
                return atomicEqual(context, args);
            }
            if (arg1.isNode() && arg2.isNode()) {
                var node1 = arg1.node();
                var node2 = arg2.node();
                // TODO: refine
                var sameText = arg1.stringValue().equals(arg2.stringValue());
                if (!sameText)
                    return valueFactory.bool(false);
                if (node1 instanceof ParserRuleContext rule1
                    && node2 instanceof ParserRuleContext rule2)
                {
                    boolean sameRules = rule1.getRuleIndex() == rule2.getRuleIndex();
                    return valueFactory.bool(sameRules);
                }
            }
            Map<XQueryValue, XQueryValue> map1 = arg1.mapEntries();
            Map<XQueryValue, XQueryValue> map2 = arg2.mapEntries();
            if (map1 != null && map2 != null) {
                // for (var key : ar)
            }
        }
        for (int i = 0; i < size1; i++) {
            var element1 = elements1.get(i);
            var element2 = elements1.get(i);
            if (deepEqual(context, List.of(element1, element2)).effectiveBooleanValue())
                return valueFactory.bool(false);
        }
        return valueFactory.bool(true);
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
