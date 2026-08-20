package com.github.akruk.antlrquery.typing;

import com.github.akruk.Utils;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.Test;

import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.RecordField.TypeOrReference;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;

import java.util.*;

import static org.junit.Assert.*;

public class AntlrQueryTypesTest extends TypesTestBase {
    @Test
    public void stringDirectEquality() {
        assertEquals(typeFactory.string(), typeFactory.string());
        assertNotEquals(string, number);
        assertNotEquals(string, emptySequence);
        assertNotEquals(string, stringSequenceOneOrMore);
        assertNotEquals(string, stringSequenceZeroOrMore);
        assertNotEquals(string, stringSequenceZeroOrOne);
    }

    @Test
    public void stringIsSubtypeOf() {
        assertTrue (Types.isSubtype(typeFactory, string, string));
        assertFalse(Types.isSubtype(typeFactory, string, number));
        assertFalse(Types.isSubtype(typeFactory, string, emptySequence));
        assertTrue (Types.isSubtype(typeFactory, string, stringSequenceOneOrMore));
        assertTrue (Types.isSubtype(typeFactory, string, stringSequenceZeroOrMore));
        assertTrue (Types.isSubtype(typeFactory, string, stringSequenceZeroOrOne));
    }


    @Test
    public void emptySequenceSubtyping() {
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.emptySequence()));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.anyItem()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemAnyItem)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemAnyItem)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemAnyItem)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.string()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemString)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemString)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemString)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.number(NumericRange.FULL)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemNumber)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemNumber)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemNumber)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.boolean_()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemBoolean)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemBoolean)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemBoolean)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.anyNode()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemAnyNode)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemAnyNode)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemAnyNode)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, fooElement));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemElementFoo)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemElementFoo)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemElementFoo)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.anyMap()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemAnyMap)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemAnyMap)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemAnyMap)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.map(itemString, typeFactory.anyItem())));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.map(itemNumber, typeFactory.anyItem())));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.map(itemBoolean, typeFactory.anyItem())));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.anyArray()));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemAnyArray)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemAnyArray)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemAnyArray)));

        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.anyFunction()));

        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrOne(itemAnyFunction)));
        assertTrue(Types.isSubtype(typeFactory, emptySequence, typeFactory.zeroOrMore(itemAnyFunction)));
        assertFalse(Types.isSubtype(typeFactory, emptySequence, typeFactory.oneOrMore(itemAnyFunction)));
        // assertTrue(Types.isSubtypeOf(typeFactory, emptySequence, typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(Types.isSubtypeOf(typeFactory, emptySequence, typeFactory.function(typeFactory.boolean_(), List.of())))
        // assertTrue(Types.isSubtypeOf(typeFactory, emptySequence, function(T) as R))
        // assertTrue(Types.isSubtypeOf(typeFactory, emptySequence, function(T1, T2) as R))
    }

    @Test
    public void numberItemSubtyping() {
        final var tested = typeFactory.itemNumber();

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementBar));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void stringItemSubtyping() {
        final var tested = typeFactory.itemString();

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype (typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementBar));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertTrue(ItemTypes.isSubtype (typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void booleanItemSubtyping() {
        final var tested = typeFactory.itemBoolean();

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementBar));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }


    @Test
    public void namedElementItemSubtyping() {
        final var tested = itemElementFoo;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementBar));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void anyMapItemSubtyping() {
        final var tested = itemAnyMap;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        // var itemRecord = typeFactory.itemRec(itemString, typeFactory.anyItem());
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void errorItemSubtyping() {
        final var tested = itemError;

        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void anyItemSubtyping() {
        final var tested = itemAnyItem;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));
        
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }


    @Test
    public void anyNodeItemSubtyping() {
        final var tested = itemAnyNode;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertTrue (ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
    }

    @Test
    public void enumItemSubtyping() {
        final var tested = itemABCenum;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, itemABCDenum, itemABCenum));
    }


    @Test
    public void anyFunctionItemSubtyping() {
        final var tested = itemAnyFunction;

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
    }



    @Test
    public void anyArrayItemSubtyping() {
        final var tested = typeFactory.itemAnyArray();

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, zeroOrMoreItems)));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, zeroOrMoreItems)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.string())));


        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(zeroOrMoreItems, List.of(typeFactory.number(NumericRange.NON_NEGATIVE)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.zeroOrMore(typeFactory.itemString()), List.of(typeFactory.number(NumericRange.NON_NEGATIVE)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(zeroOrMoreItems, List.of(typeFactory.oneOrMore(itemNonNegativeNumber)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number(NumericRange.FULL), typeFactory.number(NumericRange.FULL)))));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
    }

    @Test
    public void typedArrayItemSubtyping() {
        final var tested = typeFactory.itemArray(typeFactory.string(), Cardinality.ZERO_OR_MORE);

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.string())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.number(NumericRange.FULL))));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.string(), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(
                ItemTypes.isSubtype(typeFactory,
                        tested,
                        typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number(NumericRange.NON_NEGATIVE))))
        );
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.NON_NEGATIVE)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.oneOrMore(itemNonNegativeNumber)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.number(NumericRange.NON_NEGATIVE), List.of(typeFactory.number(NumericRange.FULL)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.NON_NEGATIVE), typeFactory.number(NumericRange.FULL)))));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
    }


    @Test
    public void typedMapItemSubtyping() {
        final var tested = typeFactory.itemMap(itemString, typeFactory.string());
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemAnyItem, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemAnyItem, typeFactory.number(NumericRange.FULL))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.number(NumericRange.FULL))));


        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.string(), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.anyItem()))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.oneOrMore(itemString), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.number(NumericRange.FULL), List.of(typeFactory.number(NumericRange.FULL)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()))));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
    }


    @Test
    public void typedFunctionSubtyping() {
        final var string1_$string1 = typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()));
        final var numberToItem = typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.number(NumericRange.FULL)));

        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemElementFoo));

        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.oneOrMore(itemString), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.number(NumericRange.FULL), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, string1_$string1, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()))));

        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemAnyMap));
        assertFalse(ItemTypes.isSubtype(typeFactory, numberToItem, itemAnyArray));

        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemRecordAny));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemRecordString));

        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, string1_$string1, itemABCDenum));
    }


    @Test
    public void recordItemSubtyping() {
        final var tested = itemRecordAny;
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemError));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyItem));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyNode));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemElementFoo));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyMap));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemAnyItem, typeFactory.anyItem())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemString, typeFactory.string())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.anyItem())));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemAnyItem, typeFactory.number(NumericRange.FULL))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemMap(itemNumber, typeFactory.number(NumericRange.FULL))));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemAnyArray));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.string(), Cardinality.ZERO_OR_MORE)));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemArray(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE)));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemAnyFunction));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of())));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.anyItem()))));
        assertTrue(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.anyItem(), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertTrue(ItemTypes.isSubtype(typeFactory, itemRecordString, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.number(NumericRange.FULL)))));
        assertTrue(ItemTypes.isSubtype(typeFactory, itemRecordString,
                typeFactory.itemFunction(typeFactory.oneOrMore(itemString), List.of(typeFactory.string()))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, typeFactory.itemFunction(typeFactory.number(NumericRange.FULL), List.of(typeFactory.number(NumericRange.FULL)))));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested,
                typeFactory.itemFunction(typeFactory.string(), List.of(typeFactory.string(), typeFactory.string()))));

        assertTrue(ItemTypes.isSubtype(typeFactory, tested, itemRecordAny));
        assertTrue(ItemTypes.isSubtype(typeFactory, itemRecordString, itemRecordAny));
        assertTrue(ItemTypes.isSubtype(typeFactory, itemRecordString, itemRecordString));
        assertFalse(ItemTypes.isSubtype(typeFactory, itemRecordAny, itemRecordString));

        final RecordField foostr = new RecordField("foo", new TypeOrReference.Type(typeFactory.string()), true);
        final RecordField bar = new RecordField("bar", new TypeOrReference.Type(typeFactory.string()), true);
        final RecordField hoo = new RecordField("hoo", new TypeOrReference.Type(typeFactory.string()), true);
        final RecordField hooOpt = new RecordField("hoo", new TypeOrReference.Type(typeFactory.string()), false);
        final var itemFooBarHoo = typeFactory.itemRecord(
                Utils.linkedHashMap(Map.entry("foo", foostr), Map.entry("bar", bar), Map.entry("hoo", hoo)));
        final var itemFooBarHooOpt = typeFactory.itemRecord(
                Utils.linkedHashMap(Map.entry("foo", foostr), Map.entry("bar", bar), Map.entry("hoo", hooOpt)));
        final var itemFooBarNum = typeFactory
                .itemRecord(
                        Utils.linkedHashMap(
                                Map.entry("foo", foostr),
                                Map.entry("bar", bar)
                        )
                );
        assertFalse(ItemTypes.isSubtype(typeFactory, itemRecordString, itemFooBarHoo));
        assertTrue(ItemTypes.isSubtype(typeFactory, itemRecordString, itemFooBarHooOpt));
        assertTrue(ItemTypes.isSubtype(typeFactory, itemFooBarHoo, itemRecordString));
        assertTrue(ItemTypes.isSubtype(typeFactory, itemRecordString, itemFooBarNum));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemBoolean));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemNumber));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemString));

        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCenum));
        assertFalse(ItemTypes.isSubtype(typeFactory, tested, itemABCDenum));
    }

    @Test
    public void typeAlternatives() {
        final var empty = typeFactory.emptySequence();
        final var numberZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemNumber());
        final var numberZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemNumber());
        final var numberOneOrMore = typeFactory.oneOrMore(typeFactory.itemNumber());
        final var $00 = Types.union(typeFactory, empty, empty);
        final var $01 = Types.union(typeFactory, empty, number);
        final var $0_zeroOrOne = Types.union(typeFactory, empty, numberZeroOrOne);
        final var $0_zeroOrMore = Types.union(typeFactory, empty, numberZeroOrMore);
        final var $0_oneOrMore = Types.union(typeFactory, empty, numberOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, numberZeroOrOne);
        assertEquals($0_zeroOrOne, numberZeroOrOne);
        assertEquals($0_zeroOrMore, numberZeroOrMore);
        assertEquals($0_oneOrMore, numberZeroOrMore);

        final var $10 = Types.union(typeFactory, number, empty);
        final var $11 = Types.union(typeFactory, number, number);
        final var $1_zeroOrOne = Types.union(typeFactory, number, numberZeroOrOne);
        final var $1_zeroOrMore = Types.union(typeFactory, number, numberZeroOrMore);
        final var $1_oneOrMore = Types.union(typeFactory, number, numberOneOrMore);

        assertEquals($10, numberZeroOrOne);
        assertEquals($11, number);
        assertEquals($1_zeroOrOne, numberZeroOrOne);
        assertEquals($1_zeroOrMore, numberZeroOrMore);
        assertEquals($1_oneOrMore, numberOneOrMore);

        final var $zeroOrOne_0 = Types.union(typeFactory, numberZeroOrOne, empty);
        final var $zeroOrOne_1 = Types.union(typeFactory, numberZeroOrOne, number);
        final var $zeroOrOne_zeroOrOne = Types.union(typeFactory, numberZeroOrOne, numberZeroOrOne);
        final var $zeroOrOne_zeroOrMore = Types.union(typeFactory, numberZeroOrOne, numberZeroOrMore);
        final var $zeroOrOne_oneOrMore = Types.union(typeFactory, numberZeroOrOne, numberOneOrMore);

        assertEquals($zeroOrOne_0, numberZeroOrOne);
        assertEquals($zeroOrOne_1, numberZeroOrOne);
        assertEquals($zeroOrOne_zeroOrOne, numberZeroOrOne);
        assertEquals($zeroOrOne_zeroOrMore, numberZeroOrMore);
        assertEquals($zeroOrOne_oneOrMore, numberZeroOrMore);

        final var $zeroOrMore_0 = Types.union(typeFactory, numberZeroOrMore, empty);
        final var $zeroOrMore_1 = Types.union(typeFactory, numberZeroOrMore, number);
        final var $zeroOrMore_zeroOrOne = Types.union(typeFactory, numberZeroOrMore, numberZeroOrOne);
        final var $zeroOrMore_zeroOrMore = Types.union(typeFactory, numberZeroOrMore, numberZeroOrMore);
        final var $zeroOrMore_oneOrMore = Types.union(typeFactory, numberZeroOrMore, numberOneOrMore);

        assertEquals($zeroOrMore_0, numberZeroOrMore);
        assertEquals($zeroOrMore_1, numberZeroOrMore);
        assertEquals($zeroOrMore_zeroOrOne, numberZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, numberZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, numberZeroOrMore);

        final var $oneOrMore_0 = Types.union(typeFactory, numberOneOrMore, empty);
        final var $oneOrMore_1 = Types.union(typeFactory, numberOneOrMore, number);
        final var $oneOrMore_zeroOrOne = Types.union(typeFactory, numberOneOrMore, numberZeroOrOne);
        final var $oneOrMore_zeroOrMore = Types.union(typeFactory, numberOneOrMore, numberZeroOrMore);
        final var $oneOrMore_oneOrMore = Types.union(typeFactory, numberOneOrMore, numberOneOrMore);

        assertEquals($oneOrMore_0, numberZeroOrMore);
        assertEquals($oneOrMore_1, numberOneOrMore);
        assertEquals($oneOrMore_zeroOrOne, numberZeroOrMore);
        assertEquals($oneOrMore_zeroOrMore, numberZeroOrMore);
        assertEquals($oneOrMore_oneOrMore, numberOneOrMore);
    }


    @Test
    public void typeAdditionNodeMerging() {
        final var empty = typeFactory.emptySequence();
        final var node = typeFactory.anyNode();
        final var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        final var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());
        final var node$2 = typeFactory.sequence(typeFactory.itemAnyNode(), Cardinality.of(2));

        final var $00 = Types.addition(typeFactory, empty, empty);
        final var $01 = Types.addition(typeFactory, empty, node);
        final var $0_zeroOrOne = Types.addition(typeFactory, empty, nodeZeroOrOne);
        final var $0_zeroOrMore = Types.addition(typeFactory, empty, nodeZeroOrMore);
        final var $0_oneOrMore = Types.addition(typeFactory, empty, nodeOneOrMore);
        assertEquals($00, empty);
        assertEquals(node, $01);
        assertEquals($0_zeroOrOne, nodeZeroOrOne);
        assertEquals($0_zeroOrMore, nodeZeroOrMore);
        assertEquals($0_oneOrMore, nodeOneOrMore);

        final var $10 = Types.addition(typeFactory, node, empty);
        final var $11 = Types.addition(typeFactory, node, node);
        final var $1_zeroOrOne = Types.addition(typeFactory, node, nodeZeroOrOne);
        final var $1_zeroOrMore = Types.addition(typeFactory, node, nodeZeroOrMore);
        final var $1_oneOrMore = Types.addition(typeFactory, node, nodeOneOrMore);

        assertEquals($10, node);
        assertEquals($11, node$2);
        assertEquals($1_zeroOrOne, typeFactory.sequence(typeFactory.itemAnyNode(), Cardinality.inclusiveRange(1, 2)));
        assertEquals($1_zeroOrMore, nodeOneOrMore);
        assertEquals($1_oneOrMore, typeFactory.sequence(typeFactory.itemAnyNode(), Cardinality.greaterThan(2)));

        final var $zeroOrOne_0 = Types.addition(typeFactory, nodeZeroOrOne, empty);
        final var $zeroOrOne_1 = Types.addition(typeFactory, nodeZeroOrOne, node);
        final var $zeroOrOne_zeroOrOne = Types.addition(typeFactory, nodeZeroOrOne, nodeZeroOrOne);
        final var $zeroOrOne_zeroOrMore = Types.addition(typeFactory, nodeZeroOrOne, nodeZeroOrMore);
        final var $zeroOrOne_oneOrMore = Types.addition(typeFactory, nodeZeroOrOne, nodeOneOrMore);

        assertEquals($zeroOrOne_0, nodeZeroOrOne);
        assertEquals($zeroOrOne_1, typeFactory.sequence(typeFactory.itemAnyNode(), Cardinality.inclusiveRange(1, 2)));
        assertEquals($zeroOrOne_zeroOrOne, typeFactory.sequence(typeFactory.itemAnyNode(), Cardinality.inclusiveRange(0, 2)));
        assertEquals($zeroOrOne_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrOne_oneOrMore, nodeOneOrMore);

        final var $zeroOrMore_0 = Types.addition(typeFactory, nodeZeroOrMore, empty);
        final var $zeroOrMore_1 = Types.addition(typeFactory, nodeZeroOrMore, node);
        final var $zeroOrMore_zeroOrOne = Types.addition(typeFactory, nodeZeroOrMore, nodeZeroOrOne);
        final var $zeroOrMore_zeroOrMore = Types.addition(typeFactory, nodeZeroOrMore, nodeZeroOrMore);
        final var $zeroOrMore_oneOrMore = Types.addition(typeFactory, nodeZeroOrMore, nodeOneOrMore);

        assertEquals($zeroOrMore_0, nodeZeroOrMore);
        assertEquals($zeroOrMore_1, nodeOneOrMore);
        assertEquals($zeroOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, nodeOneOrMore);

        final var $oneOrMore_0 = Types.addition(typeFactory, nodeOneOrMore, empty);
        final var $oneOrMore_1 = Types.addition(typeFactory, nodeOneOrMore, node);
        final var $oneOrMore_zeroOrOne = Types.addition(typeFactory, nodeOneOrMore, nodeZeroOrOne);
        final var $oneOrMore_zeroOrMore = Types.addition(typeFactory, nodeOneOrMore, nodeZeroOrMore);
        final var $oneOrMore_oneOrMore = Types.addition(typeFactory, nodeOneOrMore, nodeOneOrMore);

        assertEquals($oneOrMore_0, nodeOneOrMore);
        assertEquals($oneOrMore_1, typeFactory.sequence(typeFactory.itemAnyNode(), Cardinality.greaterThan(2)));
        assertEquals($oneOrMore_zeroOrOne, nodeOneOrMore);
        assertEquals($oneOrMore_zeroOrMore, nodeOneOrMore);
        assertEquals($oneOrMore_oneOrMore, typeFactory.sequence(typeFactory.itemAnyNode(), Cardinality.greaterThan(2)));


        final var elementFoo = typeFactory.element("", Set.of(new QualifiedName("", "foo")));
        final var elementBar = typeFactory.element("", Set.of(new QualifiedName("", "bar")));
        final var merged$elements = Types.addition(typeFactory, elementFoo, elementBar);
        assertEquals(
            typeFactory.sequence(
                typeFactory.itemNodesFromGrammar("", Set.of(
                    new QualifiedName("", "foo"),
                    new QualifiedName("", "bar")
                )), Cardinality.of(2)
            ),
            merged$elements
        );

        final var merged$any = Types.addition(typeFactory, elementFoo, anyNode);
        assertEquals(merged$any, typeFactory.sequence(typeFactory.itemAnyNode(), Cardinality.of(2)));

        final var merged$any2 = Types.addition(typeFactory, anyNode, elementFoo);
        assertEquals(merged$any2, typeFactory.sequence(typeFactory.itemAnyNode(), Cardinality.of(2)));
    }



    @Test
    public void nodeRemoveMerging() {
        final var empty = typeFactory.emptySequence();
        final var node = typeFactory.anyNode();
        final var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        final var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());

        final var $00           = Types.remove(typeFactory, empty, empty);
        final var $01           = Types.remove(typeFactory, empty, node);
        final var $0_zeroOrOne  = Types.remove(typeFactory, empty, nodeZeroOrOne);
        final var $0_zeroOrMore = Types.remove(typeFactory, empty, nodeZeroOrMore);
        final var $0_oneOrMore  = Types.remove(typeFactory, empty, nodeOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, empty);
        assertEquals($0_zeroOrOne, empty);
        assertEquals($0_zeroOrMore, empty);
        assertEquals($0_oneOrMore, empty);

        final var $10 = Types.remove(typeFactory, node, empty);
        assertEquals($10, node);
        final var $11 = Types.remove(typeFactory, node, node);
        assertEquals(nodeZeroOrOne, $11);
        final var $1_zeroOrOne = Types.remove(typeFactory, node, nodeZeroOrOne);
        assertEquals(nodeZeroOrOne, $1_zeroOrOne);
        final var $1_zeroOrMore = Types.remove(typeFactory, node, nodeZeroOrMore);
        assertEquals(nodeZeroOrOne, $1_zeroOrMore);
        final var $1_oneOrMore = Types.remove(typeFactory, node, nodeOneOrMore);
        assertEquals(nodeZeroOrOne, $1_oneOrMore);


        final var $zeroOrOne_0 = Types.remove(typeFactory, nodeZeroOrOne, empty);
        final var $zeroOrOne_1 = Types.remove(typeFactory, nodeZeroOrOne, node);
        final var $zeroOrOne_zeroOrOne = Types.remove(typeFactory, nodeZeroOrOne, nodeZeroOrOne);
        final var $zeroOrOne_zeroOrMore = Types.remove(typeFactory, nodeZeroOrOne, nodeZeroOrMore);
        final var $zeroOrOne_oneOrMore = Types.remove(typeFactory, nodeZeroOrOne, nodeOneOrMore);

        assertEquals($zeroOrOne_0, nodeZeroOrOne);
        assertEquals($zeroOrOne_1, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrOne, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrMore, nodeZeroOrOne);
        assertEquals($zeroOrOne_oneOrMore, nodeZeroOrOne);

        final var $zeroOrMore_0 = Types.remove(typeFactory, nodeZeroOrMore, empty);
        final var $zeroOrMore_1 = Types.remove(typeFactory, nodeZeroOrMore, node);
        final var $zeroOrMore_zeroOrOne = Types.remove(typeFactory, nodeZeroOrMore, nodeZeroOrOne);
        final var $zeroOrMore_zeroOrMore = Types.remove(typeFactory, nodeZeroOrMore, nodeZeroOrMore);
        final var $zeroOrMore_oneOrMore = Types.remove(typeFactory, nodeZeroOrMore, nodeOneOrMore);

        assertEquals($zeroOrMore_0, nodeZeroOrMore);
        assertEquals($zeroOrMore_1, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, nodeZeroOrMore);

        final var $oneOrMore_0 = Types.remove(typeFactory, nodeOneOrMore, empty);
        final var $oneOrMore_1 = Types.remove(typeFactory, nodeOneOrMore, node);
        final var $oneOrMore_zeroOrOne = Types.remove(typeFactory, nodeOneOrMore, nodeZeroOrOne);
        final var $oneOrMore_zeroOrMore = Types.remove(typeFactory, nodeOneOrMore, nodeZeroOrMore);
        final var $oneOrMore_oneOrMore = Types.remove(typeFactory, nodeOneOrMore, nodeOneOrMore);

        assertEquals($oneOrMore_0, nodeOneOrMore);
        assertEquals($oneOrMore_1, nodeZeroOrMore);
        assertEquals($oneOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($oneOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($oneOrMore_oneOrMore, nodeZeroOrMore);

        final QualifiedName foo = new QualifiedName("", "foo");
        final var elementFoo = typeFactory.element("",
                Set.of( foo)
        );

        final QualifiedName bar = new QualifiedName("", "bar");
        final var elementBar = typeFactory.element("", Set.of(bar));

        final var merged$elements = Types.remove(typeFactory, elementFoo, elementBar);
        assertEquals(
            merged$elements,
            typeFactory.zeroOrOne(
                typeFactory.itemNodesFromGrammar("", Set.of(foo))
            )
        );

        final var merged$any = Types.remove(typeFactory, elementFoo, anyNode);
        assertEquals(
            merged$any,
            typeFactory.zeroOrOne(
                typeFactory.itemNodesFromGrammar("", Set.of(foo))
            )
        );

        final var merged$any2 = Types.remove(typeFactory, anyNode, elementFoo);
        assertEquals(
            merged$any2,
            typeFactory.zeroOrOne(
                typeFactory.itemAnyNode()
            )
        );

    }


    @Test
    public void choiceItemTypeSubtyping() {
        final var numberOrBool = typeFactory.choice(
                typeFactory.itemNumber(), typeFactory.itemBoolean()
        );
        final var boolOrNumber = typeFactory.choice(
                typeFactory.itemBoolean(), typeFactory.itemNumber()
        );
        final var stringOrBool = typeFactory.choice(
                typeFactory.itemString(), typeFactory.itemBoolean()
        );
        final var stringOrBoolOrNumber = typeFactory.choice(
                typeFactory.itemString(), typeFactory.itemBoolean(), typeFactory.itemNumber()
        );

        assertTrue(Types.isSubtype(typeFactory, number, numberOrBool));
        assertTrue(Types.isSubtype(typeFactory, boolean_, numberOrBool));
        assertFalse(Types.isSubtype(typeFactory, numberOrBool, number));
        assertFalse(Types.isSubtype(typeFactory, numberOrBool, boolean_));
        assertFalse(Types.isSubtype(typeFactory, numberOrBool, boolean_));
                    
        assertTrue(Types.isSubtype(typeFactory, numberOrBool, anyItem));
        assertFalse(Types.isSubtype(typeFactory, anyItem, numberOrBool));
                    
        assertFalse(Types.isSubtype(typeFactory, numberOrBool, stringOrBool));
        assertTrue(Types.isSubtype(typeFactory, numberOrBool, boolOrNumber));
        assertTrue(Types.isSubtype(typeFactory, boolOrNumber, numberOrBool));
                    
        assertTrue(Types.isSubtype(typeFactory, numberOrBool, stringOrBoolOrNumber));
        assertTrue(Types.isSubtype(typeFactory, stringOrBool, stringOrBoolOrNumber));
        assertFalse(Types.isSubtype(typeFactory, stringOrBoolOrNumber, numberOrBool));
    }

    @Test
    public void extensibleRecordsSubtyping() {
        final var numberRequired = new RecordField("a", new TypeOrReference.Type(typeFactory.number(NumericRange.FULL)), true);
        final var a_number = typeFactory.extensibleRecord(
                Utils.linkedHashMap(Map.entry("a", numberRequired))
        );
        assertTrue(Types.isSubtype(typeFactory, a_number, anyMap));
        //         3.3.2.8 Subtyping Records
        // Given item types A and B, A ⊆ B is true if any of the following apply:

        // A is map(*) and B is record(*).
        assertTrue(Types.isSubtype(typeFactory, anyMap, anyMap));

        // All of the following are true:
        // A is a record type.
        // B is map(*) or record(*).
        assertTrue(Types.isSubtype(typeFactory, a_number, anyMap));

        final var longitude = new RecordField("longitude", new TypeOrReference.Type(anyItem), true);
        final var latitude = new RecordField("longitude", new TypeOrReference.Type(anyItem), true);
        // Examples:
        // record(longitude, latitude) ⊆ map(*)
        final var longitudeLatitudeRecord = typeFactory.record(
                Utils.linkedHashMap(
                        Map.entry("longitude", longitude),
                        Map.entry("latitude", latitude)
                )
        );
        assertTrue(Types.isSubtype(typeFactory, longitudeLatitudeRecord, anyMap));

        // record(longitude, latitude, *) ⊆ record(*)
        final var longitudeLatitudeRecordExtensible = typeFactory.record(
                Utils.linkedHashMap(
                        Map.entry("longitude", longitude),
                        Map.entry("latitude", latitude)
                )
        );
        assertTrue(Types.isSubtype(typeFactory, longitudeLatitudeRecordExtensible, anyMap));

        // All of the following are true:
        // A is a non-extensible record type
        // B is map(K, V)
        // K is either xs:string or xs:anyAtomicType
        // For every field F in A, where T is the declared type of F (or its default, item()*), T ⊑ V .
        // Examples:
        // record(x, y) ⊆ map(xs:string, item()*)
        final var xy = typeFactory.record(
            Utils.linkedHashMap(
                    Map.entry("x", new RecordField("x", new TypeOrReference.Type(zeroOrMoreItems), true)),
                    Map.entry("y", new RecordField("y", new TypeOrReference.Type(zeroOrMoreItems), true))
            )
        );

        final AntlrQuerySequenceType anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        final var mapStringItem = typeFactory.map(typeFactory.itemString(),
                                                  anyItems);
        assertTrue(Types.isSubtype(typeFactory, xy, mapStringItem));

        // record(x as xs:double, y as xs:double) ⊆ map(xs:string, xs:double)
        final var xy_number = typeFactory.record(
                Utils.linkedHashMap(Map.entry("x", numberRequired), Map.entry("y", numberRequired))
        );
        final var mapStringNumber = typeFactory.map(typeFactory.itemString(), typeFactory.number(NumericRange.FULL));
        assertTrue(Types.isSubtype(typeFactory, xy_number, mapStringNumber));

        // All of the following are true:
        // A is a non-extensible record type.
        // B is a non-extensible record type.
        // Every field in A is also declared in B.
        // Every mandatory field in B is also declared as mandatory in A.
        // For every field that is declared in both A and B, where the declared type in A is T and the declared type in B is U, T ⊑ U .
        // Examples:
        // record(x, y) ⊆ record(x, y, z?)
        {
        final var xField = new RecordField("x", new TypeOrReference.Type(anyItems), true);
        final var yField = new RecordField("y", new TypeOrReference.Type(anyItems), true);
        final var zField = new RecordField("z", new TypeOrReference.Type(anyItems), false);
        final var xyz = typeFactory.record(
                Utils.linkedHashMap(Map.entry("x", xField), Map.entry("y", yField), Map.entry("z", zField))
        );
        assertTrue(Types.isSubtype(typeFactory, xy, xyz));
        }

        // All of the following are true:
        // A is an extensible record type
        // B is an extensible record type
        // Every mandatory field in B is also declared as mandatory in A.
        // For every field that is declared in both A and B, where the declared type in A is T and the declared type in B is U, T ⊑ U .
        // For every field that is declared in B but not in A, the declared type in B is item()*.
        // Examples:
        // record(x, y, z, *) ⊆ record(x, y, *)
        {
        final var xFieldExtensible = new RecordField("x", new TypeOrReference.Type(anyItems), true);
        final var yFieldExtensible = new RecordField("y", new TypeOrReference.Type(anyItems), true);
        final var zFieldExtensible = new RecordField("z", new TypeOrReference.Type(anyItems), true);
        final var xyzExtensible = typeFactory.extensibleRecord(
                Utils.linkedHashMap(
                        Map.entry("x", xFieldExtensible),
                        Map.entry("y", yFieldExtensible),
                        Map.entry("z", zFieldExtensible)
                )
        );
        final var xyExtensible = typeFactory.extensibleRecord(
            Utils.linkedHashMap(Map.entry("x", xFieldExtensible), Map.entry("y", yFieldExtensible))
        );
        assertTrue(Types.isSubtype(typeFactory, xyzExtensible, xyExtensible));
        }

        // Error in documentation?
        // ??? record(x?, y?, z?, *) ⊆ record(x, y, *) ???
        // more likely: record(x, y, z?, *) ⊆ record(x, y, *)
        {
        final var xFieldExtensible = new RecordField("x", new TypeOrReference.Type(anyItems), true);
        final var yFieldExtensible = new RecordField("y", new TypeOrReference.Type(anyItems), true);
        final var zFieldOptionalExtensible = new RecordField("z", new TypeOrReference.Type(anyItems), false);
        final var xyzExtensibleOptional = typeFactory.extensibleRecord(
                Utils.linkedHashMap(
                        Map.entry("x", xFieldExtensible),
                        Map.entry("y", yFieldExtensible),
                        Map.entry("z", zFieldOptionalExtensible)
                )
        );
        final var xyExtensible = typeFactory.extensibleRecord(
                Utils.linkedHashMap(
                        Map.entry("x", xFieldExtensible),
                        Map.entry("y", yFieldExtensible)
                )
        );
        assertTrue(Types.isSubtype(typeFactory, xyzExtensibleOptional, xyExtensible));
        }

        // All of the following are true:
        // A is a non-extensible record type.
        // B is an extensible record type.
        // Every mandatory field in B is also declared as mandatory in A.
        // For every field that is declared in both A and B, where the declared type in A is T and the declared type in B is U, T ⊑ U .
        // Examples:
        // record(x, y as xs:integer) ⊆ record(x, y as xs:decimal, *)
        {
            final var xFieldAny = new RecordField("x", new TypeOrReference.Type(zeroOrMoreItems), true);
            final var yFieldNumber = new RecordField("x", new TypeOrReference.Type(number), true);
            final var record_x_any_y_number = typeFactory.record(
                Utils.linkedHashMap(Map.entry("x", xFieldAny),Map.entry("y", yFieldNumber))
            );
            final var extensible_record_x_any_y_number = typeFactory.extensibleRecord(
                Utils.linkedHashMap(Map.entry("x", xFieldAny),Map.entry("y", yFieldNumber))
            );
            assertTrue(Types.isSubtype(typeFactory, record_x_any_y_number, extensible_record_x_any_y_number));
        }

        // record(y as xs:integer) ⊆ record(x?, y as xs:decimal, *)
        final var record_y_number = typeFactory.record(
            Utils.linkedHashMap(Map.entry("y", numberRequired))
        );
        final var xFieldAny = new RecordField("x", new TypeOrReference.Type(zeroOrMoreItems), true);
        final var extensible_record_x_any_y_number_2 = typeFactory.extensibleRecord(
            Utils.linkedHashMap(Map.entry("x", xFieldAny),Map.entry("y", numberRequired))
        );
        assertTrue(Types.isSubtype(typeFactory, record_y_number, extensible_record_x_any_y_number_2));
    }



}
