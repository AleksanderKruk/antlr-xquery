package com.github.akruk.antlrxquery.semanticfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.semanticfunctiontests.FunctionsSemanticTest;

public class AggregateFunctionsTest extends FunctionsSemanticTest {

    // fn:count($input as item()*) as xs:integer
    @Test
    public void count_emptySequenceAndMultipleItems()
    {
        assertType("fn:count(())",
            typeFactory.number());
        assertType("fn:count((1,2,3))",
            typeFactory.number());
    }

    @Test
    public void count_missingArgument()
    {
        assertErrors("fn:count()");
    }

    // fn:avg($values as xs:anyAtomicType*) as xs:anyAtomicType?
    @Test
    public void avg_noValuesAndSeveralValues()
    {
        assertType("fn:avg(())",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        assertType("fn:avg((1, 2, 3))",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
    }

    // fn:max($values as xs:anyAtomicType*, $collation as xs:string? := default) as
    // xs:anyAtomicType?
    @Test
    public void max_defaultsAndWithValues()
    {
        assertType("fn:max(())",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        assertType("fn:max(('a','b','c'))",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
    }

    @Test
    public void max_namedCollationAndWrongType()
    {
        assertType("fn:max((1,2), collation := 'uci')",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        assertErrors("fn:max(1, collation := 5)");
    }

    // fn:min($values as xs:anyAtomicType*, $collation as xs:string? := default) as
    // xs:anyAtomicType?
    @Test
    public void min_defaultsAndWithValues()
    {
        assertType("fn:min(())",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        assertType("fn:min((3,2,1))",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
    }

    @Test
    public void min_namedCollationAndWrongType()
    {
        assertType("fn:min(('x','y'), collation := 'uci')",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        assertErrors("fn:min(('x'), collation := true())");
    }

    // fn:sum($values as xs:anyAtomicType*, $zero as xs:anyAtomicType? := 0) as
    // xs:anyAtomicType?
    @Test
    public void sum_defaultZeroAndWithValues()
    {
        assertType("fn:sum(())",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        assertType("fn:sum((1,2,3))",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        assertType("fn:sum((), zero := 'x')",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
    }

    // fn:all-equal($values as xs:anyAtomicType*, $collation as xs:string? :=
    // default) as xs:boolean
    @Test
    public void allEqual_defaultsAndWithValues()
    {
        assertType("fn:all-equal(())", typeFactory.boolean_());
        assertType("fn:all-equal((1,1,1))", typeFactory.boolean_());
        assertType("fn:all-equal(('a','A'), collation := 'uci')", typeFactory.boolean_());
    }

    // fn:all-different($values as xs:anyAtomicType*, $collation as xs:string? :=
    // default) as xs:boolean
    @Test
    public void allDifferent_defaultsAndWithValues()
    {
        assertType("fn:all-different(())",
            typeFactory.boolean_());
        assertType("fn:all-different((1,2,3))",
            typeFactory.boolean_());
        assertType("fn:all-different(('x','y'), collation := 'uci')",
            typeFactory.boolean_());
    }

    @Test
    public void allDifferent_wrong()
    {
        assertErrors("fn:all-different('a', collation := true())");
    }
}
