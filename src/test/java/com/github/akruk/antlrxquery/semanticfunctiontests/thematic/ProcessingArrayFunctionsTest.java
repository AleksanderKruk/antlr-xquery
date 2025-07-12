package com.github.akruk.antlrxquery.semanticfunctiontests.thematic;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.semanticfunctiontests.FunctionsSemanticTest;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;

public class ProcessingArrayFunctionsTest extends FunctionsSemanticTest {

    // array:append($array as array(*), $member as item()*) as array(*)
    @Test
    public void append_valid() {
        assertType(
                "array:append(array{1,2}, 3)",
                typeFactory.anyArray());
    }

    @Test
    public void append_errors() {
        assertErrors("array:append((),1)");
        assertErrors("array:append(array{1},)");
    }

    // array:build($input as item()*, $action as fn(item(),xs:integer) as item()* :=
    // identity#1) as array(*)
    @Test
    public void buildArray_minimal() {
        assertType(
                "array:build((1,2,3))",
                typeFactory.anyArray());
    }

    @Test
    public void buildArray_withAction() {
        assertType(
                "array:build((1,2), function($v,$i){ $v })",
                typeFactory.anyArray());
    }

    @Test
    public void buildArray_errors() {
        assertErrors("array:build((1),1)");
    }

    // array:empty($array as array(*)) as xs:boolean
    @Test
    public void arrayEmpty_valid() {
        assertType("array:empty(array{})", typeFactory.boolean_());
    }

    @Test
    public void arrayEmpty_errors() {
        assertErrors("array:empty()");
        assertErrors("array:empty(1)");
    }

    // array:filter($array as array(*), $predicate as fn(item()*,xs:integer) as
    // xs:boolean?) as array(*)
    @Test
    public void filterArray_valid() {
        assertType(
                "array:filter(array{1,2}, function($v as item(), $i as number) as boolean { true() })",
                typeFactory.anyArray());
    }

    @Test
    public void filterArray_errors() {
        assertErrors("array:filter()");
        assertErrors("array:filter(array{},1)");
    }

    // array:flatten($input as item()*) as item()*
    @Test
    public void flattenArray_valid() {
        assertType(
                "array:flatten((array{1},2,array{3}))",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void flattenArray_errors() {
        assertErrors("array:flatten(1, 2)");
    }

    // array:fold-left($array as array(*), $init as item()*, $action as
    // fn(item()*,item()*) as item()*) as item()*
    @Test
    public void foldLeftArray_valid() {
        assertType(
                "array:fold-left(array{1,2}, (), function($a,$v){ ($a,$v) })",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void foldLeftArray_errors() {
        assertErrors("array:fold-left(array{1},())");
    }

    // array:fold-right($array as array(*), $init as item()*, $action as
    // fn(item()*,item()*) as item()*) as item()*
    @Test
    public void foldRightArray_valid() {
        assertType(
                "array:fold-right(array{1,2}, (), function($a,$v){ ($v,$a) })",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void foldRightArray_errors() {
        assertErrors("array:fold-right(array{1},())");
    }

    // array:foot($array as array(*)) as item()?
    @Test
    public void footArray_valid() {
        assertType(
                "array:foot(array{1,2,3})",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void footArray_errors() {
        assertErrors("array:foot()");
    }

    // array:for-each($array as array(*), $action as fn(item()*,xs:integer) as item()*
    // ) as array(*)
    @Test
    public void forEachArray_valid() {
        assertType(
                "array:for-each(array{1,2}, function($v as item()*,$i as number) as item()* { $v })",
                typeFactory.anyArray());
    }

    @Test
    public void forEachArray_errors() {
        assertErrors("array:for-each()");
    }

    // array:for-each-pair($array1 as array(*), $array2 as array(*), $action as
    // fn(item()*,item()*,xs:integer) as item()*) as array(*)
    @Test
    public void forEachPairArray_valid() {
        assertType(
                "array:for-each-pair(array{1}, array{2}, function($a as item()*,$b as item()*,$i as number) as item()*{})",
                typeFactory.anyArray());
    }

    @Test
    public void forEachPairArray_errors() {
        assertErrors("array:for-each-pair(array{1}, array{2})");
    }

    // array:get($array as array(*), $position as xs:integer) as item()*
    @Test
    public void getArray_valid() {
        assertType(
                "array:get(array{1,2}, 2)",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void getArray_withDefault() {
        assertType(
                "array:get(array{1}, 2, 'x')",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void getArray_errors() {
        assertErrors("array:get()");
        assertErrors("array:get(array{},'x')");
    }

    // array:head($array as array(*)) as item()*
    @Test
    public void headArray_valid() {
        assertType(
                "array:head(array{1,2})",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void headArray_errors() {
        assertErrors("array:head()");
    }

    // array:index-of($array as array(*), $target as item()*, $collation as
    // xs:string? := default) as xs:integer*
    @Test
    public void indexOfArray_valid() {
        assertType(
                "array:index-of(array{1,2,1}, 1)",
                typeFactory.zeroOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void indexOfArray_errors() {
        assertErrors("array:index-of()");
    }

    // array:index-where($array as array(*), $predicate as fn(item()*,xs:integer) as
    // xs:boolean?) as xs:integer*
    @Test
    public void indexWhereArray_valid() {
        assertType(
                "array:index-where(array{1,2}, function($v,$i as number){})",
                typeFactory.zeroOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void indexWhereArray_errors() {
        assertErrors("array:index-where()");
    }

    // array:insert-before($array as array(*), $position as xs:integer, $member as
    // item()*) as array(*)
    @Test
    public void insertBeforeArray_valid() {
        assertType(
                "array:insert-before(array{1,2}, 2, 99)",
                typeFactory.anyArray());
    }

    @Test
    public void insertBeforeArray_errors() {
        assertErrors("array:insert-before(array{1},1)");
    }

    // array:items($array as array(*)) as item()*
    @Test
    public void itemsArray_valid() {
        assertType(
                "array:items(array{'x','y'})",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void itemsArray_errors() {
        assertErrors("array:items()");
    }

    // array:join($arrays as array(*)*, $separator as array(*)? := ()) as array(*)
    @Test
    public void joinArray_valid() {
        assertType(
                "array:join((array{1},array{2}), array{})",
                typeFactory.anyArray());
    }

    @Test
    public void joinArray_errors() {
        assertErrors("array:join()");
    }

    // array:members($array as array(*)) as record(value as item())*
    @Test
    public void membersArray_valid() {
        assertType(
                "array:members(array{1,2})",
                typeFactory.zeroOrMore(typeFactory.itemRecord(Map.of("value", new XQueryRecordField(typeFactory.anyItem(), true)))));
    }

    @Test
    public void membersArray_errors() {
        assertErrors("array:members()");
    }

    // array:of-members($input as record(value as item())*) as array(*)
    @Test
    public void ofMembersArray_valid() {
        assertType(
                "array:of-members(map {'value': 1 })",
                typeFactory.anyArray());
    }

    @Test
    public void ofMembersArray_errors() {
        assertErrors("array:of-members()");
    }

    // array:put($array as array(*), $position as xs:integer, $member as item()*) as
    // array(*)
    @Test
    public void putArray_valid() {
        assertType(
                "array:put(array{1,2}, 1, 0)",
                typeFactory.anyArray());
    }

    @Test
    public void putArray_errors() {
        assertErrors("array:put(array{},1)");
    }

    // array:remove($array as array(*), $positions as xs:integer*) as array(*)
    @Test
    public void removeArray_valid() {
        assertType(
                "array:remove(array{1,2,3}, (2,3))",
                typeFactory.anyArray());
    }

    @Test
    public void removeArray_errors() {
        assertErrors("array:remove()");
    }

    // array:reverse($array as array(*)) as array(*)
    @Test
    public void reverseArray_valid() {
        assertType(
                "array:reverse(array{1,2})",
                typeFactory.anyArray());
    }

    @Test
    public void reverseArray_errors() {
        assertErrors("array:reverse()");
    }

    // array:size($array as array(*)) as xs:integer
    @Test
    public void sizeArray_valid() {
        assertType(
                "array:size(array{1,2,3})",
                typeFactory.number());
    }

    @Test
    public void sizeArray_errors() {
        assertErrors("array:size()");
    }

    // array:slice($array as array(*), $start as xs:integer? := (), $end as
    // xs:integer? := (), $step as xs:integer? := ()) as array(*)
    @Test
    public void sliceArray_valid() {
        assertType(
                "array:slice(array{1,2,3}, 2)",
                typeFactory.anyArray());
    }

    @Test
    public void sliceArray_errors() {
        assertErrors("array:slice()");
    }

    // array:sort($array as array(*), $collation as xs:string? := default, $key as
    // fn(item()*) as xs:anyAtomicType* := fn:data#1) as array(*)
    @Test
    public void sortArray_valid() {
        assertType(
                "array:sort(array{'b','a'})",
                typeFactory.anyArray());
    }

    @Test
    public void sortArray_errors() {
        assertErrors("array:sort()");
    }

    // array:sort-by($array as array(*), $keys as record(...)*) as item()*
    @Test
    public void sortByArray_valid() {
        assertType(
                "array:sort-by(array{1,2}, map{ 'key':function($v){}})",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void sortByArray_errors() {
        assertErrors("array:sort-by()");
    }

    // array:split($array as array(*)) as array(*)*
    @Test
    public void splitArray_valid() {
        assertType(
                "array:split(array{1,2,3})",
                typeFactory.zeroOrMore(typeFactory.itemAnyArray()));
    }

    @Test
    public void splitArray_errors() {
        assertErrors("array:split()");
    }

    // array:subarray($array as array(*), $start as xs:integer, $length as
    // xs:integer? := ()) as array(*)
    @Test
    public void subarray_valid() {
        assertType(
                "array:subarray(array{1,2,3}, 2)",
                typeFactory.anyArray());
    }

    @Test
    public void subarray_errors() {
        assertErrors("array:subarray()");
    }

    // array:tail($array as array(*)) as array(*)
    @Test
    public void tailArray_valid() {
        assertType(
                "array:tail(array{1,2,3})",
                typeFactory.anyArray());
    }

    @Test
    public void tailArray_errors() {
        assertErrors("array:tail()");
    }

    // array:trunk($array as array(*)) as array(*)
    @Test
    public void trunkArray_valid() {
        assertType(
                "array:trunk(array{1,2,3})",
                typeFactory.anyArray());
    }

    @Test
    public void trunkArray_errors() {
        assertErrors("array:trunk()");
    }
}
