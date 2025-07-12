package com.github.akruk.antlrxquery.semanticfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.semanticfunctiontests.FunctionsSemanticTest;

public class ProcessingMapFunctionsTest extends FunctionsSemanticTest {

    // map:build($input as item()*,
    // $key as fn(item(),xs:integer) as anyAtomic* := identity#1,
    // $value as fn(item(),xs:integer) as item()* := identity#1,
    // $options as map(*)? := {}) as map(*)
    @Test
    public void build_minimal() {
        assertType(
                "map:build((1,2,3))",
                typeFactory.anyMap());
    }

    @Test
    public void build_wrongKeyOrValue() {
        assertErrors("map:build((1), key := 1)");
        assertErrors("map:build((1), value := '')");
    }

    @Test
    public void build_tooManyArgs() {
        assertErrors("map:build((1), identity#1, identity#1, map{}, 1)");
    }

    // map:contains($map as map(*), $key as anyAtomicType) as xs:boolean
    @Test
    public void contains_valid() {
        assertType(
                "map:contains(map{}, 'k')",
                typeFactory.one(typeFactory.itemBoolean()));
    }

    @Test
    public void contains_wrong() {
        assertErrors("map:contains('notMap','k')");
        assertErrors("map:contains(map{}, <a/>)");
    }

    // map:empty($map as map(*)) as xs:boolean
    @Test
    public void emptyMap_valid() {
        assertType(
                "map:empty(map{})",
                typeFactory.one(typeFactory.itemBoolean()));
    }

    @Test
    public void emptyMap_errors() {
        assertErrors("map:empty()");
        assertErrors("map:empty(1)");
    }

    // map:entries($map as map(*)) as map(*)*
    @Test
    public void entries_valid() {
        assertType(
                "map:entries(map{'x':1})",
                typeFactory.zeroOrMore(typeFactory.itemAnyMap()));
    }

    @Test
    public void entries_errors() {
        assertErrors("map:entries()");
        assertErrors("map:entries('x')");
    }

    // map:entry($key as anyAtomicType, $value as item()*) as map(*)
    @Test
    public void entry_valid() {
        assertType(
                "map:entry('k', 1)",
                typeFactory.one(typeFactory.itemAnyMap()));
    }

    @Test
    public void entry_errors() {
        assertErrors("map:entry()");
        assertErrors("map:entry(1)");
        assertErrors("map:entry('k', <a/>)");
    }

    // map:filter($map as map(*), $predicate as fn(item(),item()*) as xs:boolean?)
    // as map(*)
    @Test
    public void filterMap_valid() {
        assertType(
                "map:filter(map{'a':1}, function($k,$v){ true() })",
                typeFactory.one(typeFactory.itemAnyMap()));
    }

    @Test
    public void filterMap_errors() {
        assertErrors("map:filter()");
        assertErrors("map:filter(map{}, 1)");
    }

    // map:find($input as item()*, $key as anyAtomicType) as array(*)
    @Test
    public void find_valid() {
        assertType(
                "map:find((1,2), 1)",
                typeFactory.anyArray());
    }

    @Test
    public void find_errors() {
        assertErrors("map:find()");
        assertErrors("map:find((1), <a/>)");
    }

    // map:for-each($map as map(*), $action as fn(item(),item()*) as item()*) as
    // item()*
    @Test
    public void forEachMap_valid() {
        assertType(
                "map:for-each(map{'x':1}, function($k,$v){ $v })",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void forEachMap_errors() {
        assertErrors("map:for-each()");
        assertErrors("map:for-each(map{},1)");
    }

    // map:get($map as map(*), $key as anyAtomicType, $default as item()* := ()) as
    // item()*
    @Test
    public void getMap_valid() {
        assertType(
                "map:get(map{'x':'v'}, 'x')",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        assertType(
                "map:get(map{'x':1}, 'y', 0)",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void getMap_errors() {
        assertErrors("map:get()");
        assertErrors("map:get(map{}, <a/>)");
    }

    // map:items($map as map(*)) as item()*
    @Test
    public void itemsMap_valid() {
        assertType(
                "map:items(map{'a':1})",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void itemsMap_errors() {
        assertErrors("map:items()");
        assertErrors("map:items(1)");
    }

    // map:keys($map as map(*)) as anyAtomicType*
    @Test
    public void keysMap_valid() {
        assertType(
                "map:keys(map{'a':1})",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void keysMap_errors() {
        assertErrors("map:keys()");
    }

    // map:keys-where($map as map(*), $predicate as fn(item(),item()*) as
    // xs:boolean?) as anyAtomicType*
    @Test
    public void keysWhere_valid() {
        assertType(
                "map:keys-where(map{'a':1}, function($k,$v){ true() })",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void keysWhere_errors() {
        assertErrors("map:keys-where()");
        assertErrors("map:keys-where(map{}, 1)");
    }

    // map:merge($maps as map(*)*, $options as map(*)? := {}) as map(*)
    @Test
    public void mergeMaps_valid() {
        assertType(
                "map:merge(map{'a':1}, map{'b':2})",
                typeFactory.one(typeFactory.itemAnyMap()));
        assertType(
                "map:merge(map{}, options := map{})",
                typeFactory.one(typeFactory.itemAnyMap()));
    }

    @Test
    public void mergeMaps_errors() {
        assertErrors("map:merge()");
        assertErrors("map:merge('x')");
    }


    // map:of-pairs(
    // $input	as key-value-pair*,
    // $options	as map(*)?	:= {}
    // ) as map(*)
    @Test
    public void ofPairs_valid() {
        assertType(
                "map:of-pairs((map:pair('k',1), map:pair('x',2)))",
                typeFactory.anyMap());
    }

    @Test
    public void ofPairs_errors() {
        assertErrors("map:of-pairs()");
        assertErrors("map:of-pairs(map:pair('k',1),1)");
    }

    // map:pair($key as anyAtomicType, $value as item()*) as key-value-pair
    @Test
    public void pair_valid() {
        assertType(
                "map:pair('k', 1)",
                typeFactory.namedType("fn:key-value-pair"));
    }

    @Test
    public void pair_errors() {
        assertErrors("map:pair()");
        assertErrors("map:pair(1)");
    }

    // map:pairs($map as map(*)) as key-value-pair*
    @Test
    public void pairs_valid() {
        assertType(
                "map:pairs(map{'a':1})",
                typeFactory.zeroOrMore(typeFactory.itemNamedType("fn:key-value-pair")));
    }

    @Test
    public void pairs_errors() {
        assertErrors("map:pairs()");
    }

    // map:put($map as map(*), $key as anyAtomicType, $value as item()*) as map(*)
    @Test
    public void putMap_valid() {
        assertType(
                "map:put(map{}, 'k', 'v')",
                typeFactory.one(typeFactory.itemAnyMap()));
    }

    @Test
    public void putMap_errors() {
        assertErrors("map:put()");
        assertErrors("map:put(map{}, 1)");
    }

    // map:remove($map as map(*), $keys as anyAtomicType*) as map(*)
    @Test
    public void removeMap_valid() {
        assertType(
                "map:remove(map{'a':1}, ('a','b'))",
                typeFactory.one(typeFactory.itemAnyMap()));
    }

    @Test
    public void removeMap_errors() {
        assertErrors("map:remove()");
        assertErrors("map:remove(map{}, map{}, map{})");
    }

    // map:size($map as map(*)) as xs:integer
    @Test
    public void sizeMap_valid() {
        assertType(
                "map:size(map{'a':1})",
                typeFactory.number());
    }

    @Test
    public void sizeMap_errors() {
        assertErrors("map:size()");
        assertErrors("map:size(1)");
    }
}
