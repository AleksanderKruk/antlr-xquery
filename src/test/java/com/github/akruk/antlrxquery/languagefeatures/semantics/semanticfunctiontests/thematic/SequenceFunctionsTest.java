package com.github.akruk.antlrxquery.languagefeatures.semantics.semanticfunctiontests.thematic;


import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;


public class SequenceFunctionsTest extends SemanticTestsBase {

    // fn:empty($input as item()*) as xs:boolean
    @Test
    public void empty_defaultAndVarious()
    {
        assertType("fn:empty(())", typeFactory.boolean_());
        assertType("fn:empty((1, 'x'))", typeFactory.boolean_());
    }

    // fn:exists($input as item()*) as xs:boolean
    @Test
    public void exists_defaultAndVarious()
    {
        assertType("fn:exists(())",
            typeFactory.boolean_());
        assertType("fn:exists(((),1, 2))",
            typeFactory.boolean_());
    }

    // fn:foot($input as item()*) as item()?
    @Test
    public void foot_defaultAndWithItems()
    {
        assertType("fn:foot(())",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        assertType("fn:foot((1,2,3))",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
    }

    // fn:head($input as item()*) as item()?
    @Test
    public void head_defaultAndWithItems()
    {
        assertType("fn:head(())",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        assertType("fn:head(('a','b'))",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
    }

    // fn:identity($input as item()*) as item()*
    @Test
    public void identity_various()
    {
        assertType("fn:identity(())",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        assertType("fn:identity((1, 'x'))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    // fn:insert-before($input as item()*, $position as xs:integer, $insert as
    // item()*) as item()*
    @Test
    public void insertBefore_valid()
    {
        assertType("fn:insert-before((1,2,3), 2, 'x')",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void insertBefore_missingOrExtra()
    {
        assertErrors("fn:insert-before(1,2)");
        assertErrors("fn:insert-before()");
        assertErrors("fn:insert-before(1,2,3,4)");
    }

    @Test
    public void insertBefore_wrongTypes()
    {
        assertErrors("fn:insert-before(1,'pos','x')");
    }

    // fn:items-at($input as item()*, $at as xs:integer*) as item()*
    @Test
    public void itemsAt_valid()
    {
        assertType("fn:items-at((10,20,30), (1, 3))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void itemsAt_missingOrExtra()
    {
        assertErrors("fn:items-at(1)");
        assertErrors("fn:items-at()");
    }

    @Test
    public void itemsAt_wrongTypes()
    {
        assertErrors("fn:items-at(1, 'x')");
    }

    // fn:remove($input as item()*, $positions as xs:integer*) as item()*
    @Test
    public void remove_valid()
    {
        assertType("fn:remove((1,2,3), 2)",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void remove_wrongArity()
    {
        assertErrors("fn:remove(1)");
        assertErrors("fn:remove()");
    }

    @Test
    public void remove_wrongTypes()
    {
        assertErrors("fn:remove((1,2), 'x')");
    }

    // fn:replicate($input as item()*, $count as xs:nonNegativeInteger) as item()*
    @Test
    public void replicate_valid()
    {
        assertType("fn:replicate('a', 3)",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void replicate_missing()
    {
        assertErrors("fn:replicate(1)");
        assertErrors("fn:replicate()");
    }

    @Test
    public void replicate_wrongCount()
    {
        // TODO: specialized numeric types?
        // assertErrors("fn:replicate('x', -1)");
        assertErrors("fn:replicate('x', 'n')");
    }

    // fn:reverse($input as item()*) as item()*
    @Test
    public void reverse_various()
    {
        assertType("fn:reverse(())",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        assertType("fn:reverse((1,2,3))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    // fn:sequence-join($input as item()*, $separator as item()*) as item()*
    @Test
    public void sequenceJoin_valid()
    {
        assertType("fn:sequence-join((1,'x'), ('-'))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void sequenceJoin_wrongArity()
    {
        assertErrors("fn:sequence-join(1)");
        assertErrors("fn:sequence-join()");
    }

    // fn:slice($input as item()*, $start? := (), $end? := (), $step? := ()) as
    // item()*
    @Test
    public void slice_valid()
    {
        assertType("fn:slice((1,2,3), 2)",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void slice_missingInput()
    {
        assertErrors("fn:slice()");
    }

    @Test
    public void slice_wrongTypes()
    {
        assertErrors("fn:slice(1, 'a')");
    }

    // fn:subsequence($input as item()*, $start as xs:double, $length? := ()) as
    // item()*
    @Test
    public void subsequence_valid()
    {
        assertType("fn:subsequence((1,2,3), 2.0)",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void subsequence_withLength()
    {
        assertType("fn:subsequence((1,2,3), 1.0, 2.0)",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void subsequence_wrongArityOrType()
    {
        assertErrors("fn:subsequence(1)");
        assertErrors("fn:subsequence((1), 'x')");
    }

    // fn:tail($input as item()*) as item()*
    @Test
    public void tail_various()
    {
        assertType("fn:tail(())",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        assertType("fn:tail((1,2))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    // fn:trunk($input as item()*) as item()*
    @Test
    public void trunk_various()
    {
        assertType("fn:trunk(())",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        assertType("fn:trunk(('a','b'))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    // fn:unordered($input as item()*) as item()*
    @Test
    public void unordered_various()
    {
        assertType("fn:unordered(())",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        assertType("fn:unordered((1,2,3))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    // fn:void($input as item()* := ()) as empty-sequence()
    @Test
    public void void_defaultAndVarious()
    {
        assertNoErrors("fn:void(())");
        assertNoErrors("fn:void((1,2,3))");
    }
}
