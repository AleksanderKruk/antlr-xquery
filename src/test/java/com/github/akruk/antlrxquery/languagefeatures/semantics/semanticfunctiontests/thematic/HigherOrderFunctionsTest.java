package com.github.akruk.antlrxquery.languagefeatures.semantics.semanticfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;


public class HigherOrderFunctionsTest extends SemanticTestsBase {

    // fn:function-lookup($name as xs:QName, $arity as xs:integer) as fn(*)?
    @Test public void functionLookup_valid() {
        assertType(
            "fn:function-lookup('f', 2)",
            typeFactory.zeroOrOne(typeFactory.itemAnyFunction())
        );
    }
    @Test public void functionLookup_wrongTypesOrArity() {
        assertErrors("fn:function-lookup(1, 1)");
        assertErrors("fn:function-lookup(1, 1, 1)");
    }

    // fn:function-name($function as fn(*)) as xs:QName?
    @Test public void functionName_valid() {
        assertType(
            "fn:function-name(true#0)",
            typeFactory.zeroOrOne(typeFactory.itemString())
        );
    }
    @Test public void functionName_errors() {
        assertErrors("fn:function-name()");
        assertErrors("fn:function-name(1)");
    }

    // fn:function-arity($function as fn(*)) as xs:integer
    @Test public void functionArity_valid() {
        assertType(
            "fn:function-arity(string#1)",
            typeFactory.number()
        );
    }
    @Test public void functionArity_errors() {
        assertErrors("fn:function-arity()");
        assertErrors("fn:function-arity(1)");
    }

    // fn:function-identity($function as fn(*)) as xs:string
    @Test public void functionIdentity_valid() {
        assertType(
            "fn:function-identity(boolean#1)",
            typeFactory.string()
        );
    }
    @Test public void functionIdentity_wrong() {
        assertErrors("fn:function-identity()");
        assertErrors("fn:function-identity('x')");
    }

    // fn:function-annotations($function as fn(*)) as map(xs:QName,xs:anyAtomicType*)*
    @Test public void functionAnnotations_valid() {
        assertType(
            "fn:function-annotations(upper-case#1)",
            typeFactory.zeroOrMore(
                typeFactory.itemMap(
                    typeFactory.itemString(),
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem())
                )
            )
        );
    }
    @Test public void functionAnnotations_errors() {
        assertErrors("fn:function-annotations()");
        assertErrors("fn:function-annotations(1)");
    }

    // fn:apply($function as fn(*), $arguments as array(*)) as item()*
    @Test public void apply_valid() {
        assertType(
            "fn:apply(data#1, array{'a', 'b'})",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void apply_errors() {
        assertErrors("fn:apply()");
        assertErrors("fn:apply(1, array{})");
        assertErrors("fn:apply(upper-case#1, 1)");
    }

    // fn:do-until($input as item()*, $action as fn(item()*,xs:integer) as item()*,
    //             $predicate as fn(item()*,xs:integer) as xs:boolean?) as item()*
    @Test public void doUntil_valid() {
        assertType(
            "fn:do-until((1,2), function($x, $i){ $x }, function($x, $i){ true() })",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void doUntil_errors() {
        assertErrors("fn:do-until()");
        assertErrors("fn:do-until((),function($x,$i){$x})");
        assertErrors("fn:do-until((), function($x,$i){$x}, 1)");
    }

    // fn:every($input as item()*, $predicate as fn(item(),xs:integer) as xs:boolean? := fn:boolean#1) as xs:boolean
    @Test public void every_defaultsAndCustom() {
        assertType("fn:every((1,2))",
            typeFactory.boolean_());
        assertType(
            "fn:every((1,2), function($v as item(), $i as number) as boolean? { })",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void every_errors() {
        assertErrors("fn:every()");
        assertErrors("fn:every((1), 1)");
    }

    // fn:filter($input as item()*, $predicate as fn(item(),xs:integer) as xs:boolean?) as item()*
    @Test public void filter_valid() {
        assertType(
            "fn:filter((1,2,3), function($v as item(),$i as number){})",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void filter_errors() {
        assertErrors("fn:filter()");
        assertErrors("fn:filter((1,2), 1)");
    }

    // fn:fold-left($input as item()*, $init as item()*, $action as fn(item()*,item()) as item()*) as item()*
    @Test public void foldLeft_valid() {
        assertType(
            "fn:fold-left((1,2), (), function($acc,$v){ ($acc, $v) })",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void foldLeft_errors() {
        assertErrors("fn:fold-left((1),())");
        assertErrors("fn:fold-left((1),(),1)");
    }

    // fn:fold-right($input as item()*, $init as item()*, $action as fn(item(),item()*) as item()*) as item()*
    @Test public void foldRight_valid() {
        assertType(
            "fn:fold-right((1,2), (), function($v,$acc){ ($v, $acc) })",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void foldRight_errors() {
        assertErrors("fn:fold-right((1),())");
        assertErrors("fn:fold-right((1),(),1)");
    }

    // fn:for-each($input as item()*, $action as fn(item(),xs:integer) as item()*) as item()*
    @Test public void forEach_valid() {
        assertType(
            "fn:for-each((1,2), function($v,$i){})",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void forEach_errors() {
        assertErrors("fn:for-each()");
        assertErrors("fn:for-each((1),1)");
    }

    // fn:for-each-pair($input1 as item()*, $input2 as item()*,
    //                 $action as fn(item(),item(),xs:integer) as item()*) as item()*
    @Test public void forEachPair_valid() {
        assertType(
            "fn:for-each-pair((1,2),(3,4), function($a,$b,$i){})",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void forEachPair_errors() {
        assertErrors("fn:for-each-pair((1),(2))");
        assertErrors("fn:for-each-pair((1),(2),1)");
    }

    // fn:highest($input as item()*, $collation as xs:string? := default,
    //            $key as fn(item()) as xs:anyAtomicType* := fn:data#1) as item()*
    @Test public void highest_defaultsAndCustom() {
        assertType("fn:highest((1,2,3))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        assertType(
            "fn:highest(('a','b'), 'uci', function($v){ $v })",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void highest_errors() {
        assertErrors("fn:highest()");
        assertErrors("fn:highest((1),1)");
    }

    // fn:index-where($input as item()*, $predicate as fn(item(),xs:integer) as xs:boolean?) as xs:integer*
    @Test public void indexWhere_valid() {
        assertType(
            "fn:index-where((1,2,3), function($v,$i){})",
            typeFactory.zeroOrMore(typeFactory.itemNumber())
        );
    }
    @Test public void indexWhere_errors() {
        assertErrors("fn:index-where()");
        assertErrors("fn:index-where((1),1)");
    }

    // fn:lowest($input as item()*, $collation as xs:string? := default,
    //           $key as fn(item()) as xs:anyAtomicType* := fn:data#1) as item()*
    @Test public void lowest_defaultsAndCustom() {
        assertType("fn:lowest((3,1,2))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        assertType(
            "fn:lowest(('b','a'), 'uci', function($v){ $v })",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void lowest_errors() {
        assertErrors("fn:lowest()");
        assertErrors("fn:lowest((1),1)");
    }

    // fn:partial-apply($function as fn(*), $arguments as map(xs:positiveInteger,item()*)) as fn(*)
    @Test public void partialApply_valid() {
        assertType(
            "fn:partial-apply(substring#2, map{2:('x')})",
            typeFactory.one(typeFactory.itemAnyFunction())
        );
    }
    @Test public void partialApply_errors() {
        assertErrors("fn:partial-apply()");
        assertErrors("fn:partial-apply(1, map{})");
    }

    // fn:partition($input as item()*, $split-when as fn(item()*,item(),xs:integer) as xs:boolean?) as array(item()*)*
    @Test public void partition_valid() {
        assertType(
            "fn:partition((1,2,3), function($acc,$v,$i){true()})",
            typeFactory.zeroOrMore(typeFactory.itemAnyArray())
        );
    }
    @Test public void partition_errors() {
        assertErrors("fn:partition()");
        assertErrors("fn:partition((1),1)");
    }

    // fn:scan-left($input as item()*, $init as item()*, $action as fn(item()*,item()) as item()*) as array(*)*
    @Test public void scanLeft_valid() {
        assertType(
            "fn:scan-left((1,2), (), function($acc,$v){ ($acc,$v) })",
            typeFactory.zeroOrMore(typeFactory.itemAnyArray())
        );
    }
    @Test public void scanLeft_errors() {
        assertErrors("fn:scan-left((1),())");
        assertErrors("fn:scan-left((1),(),1)");
    }

    // fn:scan-right($input as item()*, $init as item()*, $action as fn(item(),item()*) as item()*) as array(*)*
    @Test public void scanRight_valid() {
        assertType(
            "fn:scan-right((1,2), (), function($v,$acc){ ($v,$acc) })",
            typeFactory.zeroOrMore(typeFactory.itemAnyArray())
        );
    }
    @Test public void scanRight_errors() {
        assertErrors("fn:scan-right((1),())");
        assertErrors("fn:scan-right((1),(),1)");
    }

    // fn:some($input as item()*, $predicate as fn(item(),xs:integer) as xs:boolean? := fn:boolean#1) as xs:boolean
    @Test public void some_defaultsAndCustom() {
        assertType("fn:some((0,1))", typeFactory.boolean_());
        assertType(
            "fn:some((1,2), function($v,$i){})",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void some_errors() {
        assertErrors("fn:some()");
        assertErrors("fn:some((1),1)");
    }

    // fn:sort($input as item()*, $collation as xs:string? := default, $key as fn(item()) as xs:anyAtomicType* := fn:data#1) as item()*
    @Test public void sort_defaultsAndCustom() {
        assertType("fn:sort((3,1,2))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        assertType(
            "fn:sort(('b','a'), 'uci', function($v){ $v })",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void sort_errors() {
        assertErrors("fn:sort()");
        assertErrors("fn:sort((1), 1)");
    }

    // // fn:sort-by($input as item()*, $keys as record(...)*) as item()*
    // @Test public void sortBy_valid() {
    //     assertType(
    //         "fn:sort-by((1,2), record{key: function($v){ $v }, order: 'descending'})",
    //         typeFactory.zeroOrMore(typeFactory.itemAnyItem())
    //     );
    // }
    // @Test public void sortBy_errors() {
    //     assertErrors("fn:sort-by()");
    //     assertErrors("fn:sort-by((1), 'x')");
    // }

    // fn:sort-with($input as item()*, $comparators as fn(item(),item()) as xs:integer *) as item()*
    @Test public void sortWith_valid() {
        assertType(
            "fn:sort-with((1,2), function($a,$b){ 1 })",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void sortWith_errors() {
        assertErrors("fn:sort-with()");
        assertErrors("fn:sort-with((1),1)");
    }

    // fn:subsequence-where($input as item()*,
    //                      $from as fn(item(),xs:integer) as xs:boolean? := true#0,
    //                      $to as fn(item(),xs:integer) as xs:boolean? := false#0
    // ) as item()*
    @Test public void subsequenceWhere_defaults() {
        assertType("fn:subsequence-where((1,2,3))",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }
    @Test public void subsequenceWhere_customPredicates() {
        assertType(
            "fn:subsequence-where((1,2), function($v,$i){}, function($v,$i){})",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void subsequenceWhere_errors() {
        assertErrors("fn:subsequence-where()");
        assertErrors("fn:subsequence-where((1),1)");
    }

    // fn:take-while($input as item()*, $predicate as fn(item(),xs:integer) as xs:boolean?) as item()*
    @Test public void takeWhile_valid() {
        assertType(
            "fn:take-while((1,2,3), function($v,$i){})",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }

    @Test public void takeWhile_errors() {
        assertErrors("fn:take-while()");
        assertErrors("fn:take-while((1),1)");
    }

    // fn:transitive-closure($node as node()?, $step as fn(node()) as node()*) as node()*
    @Test public void transitiveClosure_valid() {
        assertType(
            "fn:transitive-closure(., function($n){})",
            typeFactory.zeroOrMore(typeFactory.itemAnyNode())
        );
    }
    @Test public void transitiveClosure_errors() {
        assertErrors("fn:transitive-closure()");
        assertErrors("fn:transitive-closure(<a/>, 1)");
    }

    // fn:while-do($input as item()*, $predicate as fn(item()*,xs:integer) as xs:boolean?,
    //            $action as fn(item()*,xs:integer) as item()*) as item()*
    @Test public void whileDo_valid() {
        assertType(
            "fn:while-do((1), function($v,$i){}, function($v,$i){})",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );
    }
    @Test public void whileDo_errors() {
        assertErrors("fn:while-do()");
        assertErrors("fn:while-do((1),1, function($v,$i){$v})");
    }
}
