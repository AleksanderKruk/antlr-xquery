package com.github.akruk.antlrxquery.languagefeatures.semantics.semanticfunctiontests.thematic;

import org.junit.jupiter.api.Test;


import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class ComparisonFunctionsTest extends SemanticTestsBase {

    // fn:atomic-equal($v1 as xs:anyAtomicType, $v2 as xs:anyAtomicType) as
    // xs:boolean
    @Test
    public void atomicEqual_valid()
    {
        assertType("fn:atomic-equal(1, 'a')",
            typeFactory.boolean_());
    }

    @Test
    public void atomicEqual_arityErrors()
    {
        assertErrors("fn:atomic-equal(1)");
        assertErrors("fn:atomic-equal()");
        assertErrors("fn:atomic-equal(1,2,3)");
    }

    // fn:deep-equal($in1 as item()*, $in2 as item()*, $options as
    // (xs:string|map(*))? := {}) as xs:boolean
    @Test
    public void deepEqual_minimal()
    {
        assertType("fn:deep-equal((1,'x'),(1,'x'))",
            typeFactory.boolean_());
    }

    @Test
    public void deepEqual_withStringOptions()
    {
        assertType("fn:deep-equal((1),(1),'collation=uc')",
            typeFactory.boolean_());
    }

    @Test
    public void deepEqual_withMapOptions()
    {
        assertType("fn:deep-equal(1, 1, map{})",
            typeFactory.boolean_());
    }

    @Test
    public void deepEqual_missingOrExtra()
    {
        assertErrors("fn:deep-equal((1))");
        assertErrors("fn:deep-equal()");
        assertErrors("fn:deep-equal((1),(1), 'opt', 'x')");
    }

    @Test
    public void deepEqual_badOptionType()
    {
        assertErrors("fn:deep-equal((1),(1), 123)");
    }

    // fn:compare($v1 as xs:anyAtomicType?,$v2 as xs:anyAtomicType?, $collation as
    // xs:string? := default) as xs:integer?
    @Test
    public void compare_noArgs()
    {
        assertErrors("fn:compare()");
    }

    @Test
    public void compare_minimal()
    {
        assertType("fn:compare('a','b')",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }

    @Test
    public void compare_withCollation()
    {
        assertType("fn:compare('a','b','uci')",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }

    @Test
    public void compare_namedCollation()
    {
        assertType("fn:compare(value2 := 'b', value1 := 'a', collation := 'uc')",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }

    @Test
    public void compare_wrongTypes()
    {
        assertErrors("fn:compare('a','b', 1)");
    }

    // fn:distinct-values($values as xs:anyAtomicType*, $collation as xs:string? :=
    // default) as xs:anyAtomicType*
    @Test
    public void distinctValues_empty()
    {
        assertType("fn:distinct-values(())",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void distinctValues_withValues()
    {
        assertType("fn:distinct-values(('a','b','a'))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void distinctValues_namedCollation()
    {
        assertType("fn:distinct-values((1,2,1), collation := 'uc')",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void distinctValues_wrongType()
    {
        assertErrors("fn:distinct-values(<x/>)");
    }

    // fn:duplicate-values($values as xs:anyAtomicType*, $collation as xs:string? :=
    // default) as xs:anyAtomicType*
    @Test
    public void duplicateValues_empty()
    {
        assertType("fn:duplicate-values(())",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void duplicateValues_withValues()
    {
        assertType("fn:duplicate-values(('x','x','y'))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void duplicateValues_wrongType()
    {
        assertErrors("fn:duplicate-values(<x/>)");
    }

    // fn:index-of($in as xs:anyAtomicType*, $target as xs:anyAtomicType, $collation
    // as xs:string? := default) as xs:integer*
    @Test
    public void indexOf_minimal()
    {
        assertType("fn:index-of(1, 2, 'uc')",
            typeFactory.zeroOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void indexOf_defaultCollation()
    {
        assertType("fn:index-of('a','a')",
            typeFactory.zeroOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void indexOf_missingOrExtra()
    {
        assertErrors("fn:index-of(1)");
        assertErrors("fn:index-of()");
        assertErrors("fn:index-of(1,2,'x','y')");
    }

    @Test
    public void indexOf_wrongTypes()
    {
        assertErrors("fn:index-of(<a/>,1)");
        assertErrors("fn:index-of(1,<b/>)");
    }

    // fn:starts-with-subsequence($in as item()*, $sub as item()*, $cmp as
    // fn(item(),item())? := fn:deep-equal#2) as xs:boolean
    @Test
    public void startsWithSub_minimal()
    {
        assertType("fn:starts-with-subsequence((1,2,3),(1,2))",
            typeFactory.boolean_());
    }

    @Test
    public void startsWithSub_namedCompare()
    {
        assertType("fn:starts-with-subsequence(1,1, fn:deep-equal#2)",
            typeFactory.boolean_());
    }

    @Test
    public void startsWithSub_missingOrWrong()
    {
        assertErrors("fn:starts-with-subsequence((1))");
        assertErrors("fn:starts-with-subsequence()");
        assertErrors("fn:starts-with-subsequence((1),(1),'x')");
    }

    // fn:ends-with-subsequence(...) as xs:boolean
    @Test
    public void endsWithSub_minimal()
    {
        assertType("fn:ends-with-subsequence((1, 2),(3))",
            typeFactory.boolean_());
    }

    @Test
    public void endsWithSub_namedArgs()
    {
        assertType("fn:ends-with-subsequence(input := (1,2), subsequence := (2))",
            typeFactory.boolean_());
    }

    @Test
    public void endsWithSub_errors()
    {
        assertErrors("fn:ends-with-subsequence((1))");
        assertErrors("fn:ends-with-subsequence()");
    }

    // fn:contains-subsequence(...) as xs:boolean
    @Test
    public void containsSub_minimal()
    {
        assertType("fn:contains-subsequence((1,2,3),(2,3))",
            typeFactory.boolean_());
    }

    @Test
    public void containsSub_namedCompare()
    {
        assertType("fn:contains-subsequence((1,2),(1), fn:deep-equal#2)",
            typeFactory.boolean_());
    }

    @Test
    public void containsSub_errors()
    {
        assertErrors("fn:contains-subsequence((1))");
        assertErrors("fn:contains-subsequence()");
    }
}
