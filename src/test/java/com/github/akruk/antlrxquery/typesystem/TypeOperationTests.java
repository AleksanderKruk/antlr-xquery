package com.github.akruk.antlrxquery.typesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;

public class TypeOperationTests {
    final XQueryTypeFactory typeFactory = new XQueryEnumTypeFactory();
    final XQuerySequenceType error = typeFactory.error();
    final XQuerySequenceType boolean_ = typeFactory.boolean_();
    final XQuerySequenceType string = typeFactory.string();
    final XQuerySequenceType number = typeFactory.number();
    final XQuerySequenceType anyNode = typeFactory.anyNode();
    final XQuerySequenceType emptySequence = typeFactory.emptySequence();
    final XQuerySequenceType stringSequenceOneOrMore = typeFactory.oneOrMore(typeFactory.itemString());
    final XQuerySequenceType stringSequenceZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemString());
    final XQuerySequenceType stringSequenceZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemString());
    final XQuerySequenceType numberSequenceOneOrMore = typeFactory.oneOrMore(typeFactory.itemNumber());
    final XQuerySequenceType numberSequenceZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemNumber());
    final XQuerySequenceType numberSequenceZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemNumber());
    final XQuerySequenceType fooElement = typeFactory.element(Set.of("foo"));
    final XQuerySequenceType barElement = typeFactory.element(Set.of("bar"));
    final XQuerySequenceType anyArray = typeFactory.anyArray();
    final XQuerySequenceType anyMap = typeFactory.anyMap();
    final XQuerySequenceType anyItem = typeFactory.anyItem();
    final XQuerySequenceType anyFunction = typeFactory.anyFunction();

    final XQueryRecordField requiredAnyItem = new XQueryRecordField(typeFactory.anyItem(), true);
    final XQueryRecordField requiredNumber = new XQueryRecordField(typeFactory.number(), true);
    final XQueryRecordField requiredString = new XQueryRecordField(typeFactory.string(), true);
    final XQuerySequenceType recordAny = typeFactory.record(Map.of("foo", requiredAnyItem, "bar", requiredAnyItem));

    final XQueryItemType itemError = typeFactory.itemError();
    final XQueryItemType itemAnyFunction = typeFactory.itemAnyFunction();
    final XQueryItemType itemAnyItem = typeFactory.itemAnyItem();
    final XQueryItemType itemString = typeFactory.itemString();
    final XQueryItemType itemNumber = typeFactory.itemNumber();
    final XQueryItemType itemBoolean = typeFactory.itemBoolean();
    final XQueryItemType itemAnyNode = typeFactory.itemAnyNode();
    final XQueryItemType itemAnyMap = typeFactory.itemAnyMap();
    final XQueryItemType itemAnyArray = typeFactory.itemAnyArray();
    final XQueryItemType itemElementFoo = typeFactory.itemElement(Set.of("foo"));
    final XQueryItemType itemElementBar = typeFactory.itemElement(Set.of("bar"));
    final XQueryItemType itemABenum = typeFactory.itemEnum(Set.of("A", "B"));
    final XQueryItemType itemABCenum = typeFactory.itemEnum(Set.of("A", "B", "C"));
    final XQueryItemType itemABCDenum = typeFactory.itemEnum(Set.of("A", "B", "C", "D"));
    final XQueryItemType itemRecordAny = typeFactory.itemRecord(Map.of("foo", requiredAnyItem, "bar", requiredAnyItem));
    final XQueryItemType itemRecordString = typeFactory.itemRecord(Map.of("foo", requiredString, "bar", requiredString));

    @Test
    public void typeAlternatives() {
        final var empty = typeFactory.emptySequence();
        final var numberZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemNumber());
        final var numberZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemNumber());
        final var numberOneOrMore = typeFactory.oneOrMore(typeFactory.itemNumber());
        final var $00 = empty.alternativeMerge(empty);
        final var $01 = empty.alternativeMerge(number);
        final var $0_zeroOrOne = empty.alternativeMerge(numberZeroOrOne);
        final var $0_zeroOrMore = empty.alternativeMerge(numberZeroOrMore);
        final var $0_oneOrMore = empty.alternativeMerge(numberOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, numberZeroOrOne);
        assertEquals($0_zeroOrOne, numberZeroOrOne);
        assertEquals($0_zeroOrMore, numberZeroOrMore);
        assertEquals($0_oneOrMore, numberZeroOrMore);

        final var $10 = number.alternativeMerge(empty);
        final var $11 = number.alternativeMerge(number);
        final var $1_zeroOrOne = number.alternativeMerge(numberZeroOrOne);
        final var $1_zeroOrMore = number.alternativeMerge(numberZeroOrMore);
        final var $1_oneOrMore = number.alternativeMerge(numberOneOrMore);

        assertEquals($10, numberZeroOrOne);
        assertEquals($11, number);
        assertEquals($1_zeroOrOne, numberZeroOrOne);
        assertEquals($1_zeroOrMore, numberZeroOrMore);
        assertEquals($1_oneOrMore, numberOneOrMore);

        final var $zeroOrOne_0 = numberZeroOrOne.alternativeMerge(empty);
        final var $zeroOrOne_1 = numberZeroOrOne.alternativeMerge(number);
        final var $zeroOrOne_zeroOrOne = numberZeroOrOne.alternativeMerge(numberZeroOrOne);
        final var $zeroOrOne_zeroOrMore = numberZeroOrOne.alternativeMerge(numberZeroOrMore);
        final var $zeroOrOne_oneOrMore = numberZeroOrOne.alternativeMerge(numberOneOrMore);

        assertEquals($zeroOrOne_0, numberZeroOrOne);
        assertEquals($zeroOrOne_1, numberZeroOrOne);
        assertEquals($zeroOrOne_zeroOrOne, numberZeroOrOne);
        assertEquals($zeroOrOne_zeroOrMore, numberZeroOrMore);
        assertEquals($zeroOrOne_oneOrMore, numberZeroOrMore);

        final var $zeroOrMore_0 = numberZeroOrMore.alternativeMerge(empty);
        final var $zeroOrMore_1 = numberZeroOrMore.alternativeMerge(number);
        final var $zeroOrMore_zeroOrOne = numberZeroOrMore.alternativeMerge(numberZeroOrOne);
        final var $zeroOrMore_zeroOrMore = numberZeroOrMore.alternativeMerge(numberZeroOrMore);
        final var $zeroOrMore_oneOrMore = numberZeroOrMore.alternativeMerge(numberOneOrMore);

        assertEquals($zeroOrMore_0, numberZeroOrMore);
        assertEquals($zeroOrMore_1, numberZeroOrMore);
        assertEquals($zeroOrMore_zeroOrOne, numberZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, numberZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, numberZeroOrMore);

        final var $oneOrMore_0 = numberOneOrMore.alternativeMerge(empty);
        final var $oneOrMore_1 = numberOneOrMore.alternativeMerge(number);
        final var $oneOrMore_zeroOrOne = numberOneOrMore.alternativeMerge(numberZeroOrOne);
        final var $oneOrMore_zeroOrMore = numberOneOrMore.alternativeMerge(numberZeroOrMore);
        final var $oneOrMore_oneOrMore = numberOneOrMore.alternativeMerge(numberOneOrMore);

        assertEquals($oneOrMore_0, numberZeroOrMore);
        assertEquals($oneOrMore_1, numberOneOrMore);
        assertEquals($oneOrMore_zeroOrOne, numberZeroOrMore);
        assertEquals($oneOrMore_zeroOrMore, numberZeroOrMore);
        assertEquals($oneOrMore_oneOrMore, numberOneOrMore);
    }


    @Test
    public void unionNodeMerging() {
        final var empty = typeFactory.emptySequence();
        final var node = typeFactory.anyNode();
        final var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        final var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());

        final var $00 = empty.unionMerge(empty);
        final var $01 = empty.unionMerge(node);
        final var $0_zeroOrOne = empty.unionMerge(nodeZeroOrOne);
        final var $0_zeroOrMore = empty.unionMerge(nodeZeroOrMore);
        final var $0_oneOrMore = empty.unionMerge(nodeOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, node);
        assertEquals($0_zeroOrOne, nodeZeroOrOne);
        assertEquals($0_zeroOrMore, nodeZeroOrMore);
        assertEquals($0_oneOrMore, nodeOneOrMore);

        final var $10 = node.unionMerge(empty);
        final var $11 = node.unionMerge(node);
        final var $1_zeroOrOne = node.unionMerge(nodeZeroOrOne);
        final var $1_zeroOrMore = node.unionMerge(nodeZeroOrMore);
        final var $1_oneOrMore = node.unionMerge(nodeOneOrMore);

        assertEquals($10, node);
        assertEquals($11, nodeOneOrMore);
        assertEquals($1_zeroOrOne, nodeOneOrMore);
        assertEquals($1_zeroOrMore, nodeOneOrMore);
        assertEquals($1_oneOrMore, nodeOneOrMore);

        final var $zeroOrOne_0 = nodeZeroOrOne.unionMerge(empty);
        final var $zeroOrOne_1 = nodeZeroOrOne.unionMerge(node);
        final var $zeroOrOne_zeroOrOne = nodeZeroOrOne.unionMerge(nodeZeroOrOne);
        final var $zeroOrOne_zeroOrMore = nodeZeroOrOne.unionMerge(nodeZeroOrMore);
        final var $zeroOrOne_oneOrMore = nodeZeroOrOne.unionMerge(nodeOneOrMore);

        assertEquals($zeroOrOne_0, nodeZeroOrOne);
        assertEquals($zeroOrOne_1, nodeOneOrMore);
        assertEquals($zeroOrOne_zeroOrOne, nodeZeroOrMore);
        assertEquals($zeroOrOne_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrOne_oneOrMore, nodeOneOrMore);

        final var $zeroOrMore_0 = nodeZeroOrMore.unionMerge(empty);
        final var $zeroOrMore_1 = nodeZeroOrMore.unionMerge(node);
        final var $zeroOrMore_zeroOrOne = nodeZeroOrMore.unionMerge(nodeZeroOrOne);
        final var $zeroOrMore_zeroOrMore = nodeZeroOrMore.unionMerge(nodeZeroOrMore);
        final var $zeroOrMore_oneOrMore = nodeZeroOrMore.unionMerge(nodeOneOrMore);

        assertEquals($zeroOrMore_0, nodeZeroOrMore);
        assertEquals($zeroOrMore_1, nodeOneOrMore);
        assertEquals($zeroOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, nodeOneOrMore);

        final var $oneOrMore_0 = nodeOneOrMore.unionMerge(empty);
        final var $oneOrMore_1 = nodeOneOrMore.unionMerge(node);
        final var $oneOrMore_zeroOrOne = nodeOneOrMore.unionMerge(nodeZeroOrOne);
        final var $oneOrMore_zeroOrMore = nodeOneOrMore.unionMerge(nodeZeroOrMore);
        final var $oneOrMore_oneOrMore = nodeOneOrMore.unionMerge(nodeOneOrMore);

        assertEquals($oneOrMore_0, nodeOneOrMore);
        assertEquals($oneOrMore_1, nodeOneOrMore);
        assertEquals($oneOrMore_zeroOrOne, nodeOneOrMore);
        assertEquals($oneOrMore_zeroOrMore, nodeOneOrMore);
        assertEquals($oneOrMore_oneOrMore, nodeOneOrMore);


        final var elementFoo = typeFactory.element(Set.of("foo"));
        final var elementBar = typeFactory.element(Set.of("bar"));
        final var merged$elements = elementFoo.unionMerge(elementBar);
        assertEquals( typeFactory.oneOrMore(typeFactory.itemElement(Set.of("foo", "bar"))),
                        merged$elements);

        final var merged$any = elementFoo.unionMerge(anyNode);
        assertEquals(merged$any, nodeOneOrMore);

        final var merged$any2 = anyNode.unionMerge(elementFoo);
        assertEquals(merged$any2, nodeOneOrMore);
    }


    @Test
    public void intersectNodeMerging() {
        final var empty = typeFactory.emptySequence();
        final var node = typeFactory.anyNode();
        final var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        final var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());

        final var $00 = empty.intersectionMerge(empty);
        final var $01 = empty.intersectionMerge(node);
        final var $0_zeroOrOne = empty.intersectionMerge(nodeZeroOrOne);
        final var $0_zeroOrMore = empty.intersectionMerge(nodeZeroOrMore);
        final var $0_oneOrMore = empty.intersectionMerge(nodeOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, empty);
        assertEquals($0_zeroOrOne, empty);
        assertEquals($0_zeroOrMore, empty);
        assertEquals($0_oneOrMore, empty);

        final var $10 = node.intersectionMerge(empty);
        final var $11 = node.intersectionMerge(node);
        final var $1_zeroOrOne = node.intersectionMerge(nodeZeroOrOne);
        final var $1_zeroOrMore = node.intersectionMerge(nodeZeroOrMore);
        final var $1_oneOrMore = node.intersectionMerge(nodeOneOrMore);

        assertEquals($10, empty);
        assertEquals($11, nodeZeroOrOne);
        assertEquals($1_zeroOrOne, nodeZeroOrOne);
        assertEquals($1_zeroOrMore, nodeZeroOrOne);
        assertEquals($1_oneOrMore, nodeZeroOrOne);

        final var $zeroOrOne_0 = nodeZeroOrOne.intersectionMerge(empty);
        final var $zeroOrOne_1 = nodeZeroOrOne.intersectionMerge(node);
        final var $zeroOrOne_zeroOrOne = nodeZeroOrOne.intersectionMerge(nodeZeroOrOne);
        final var $zeroOrOne_zeroOrMore = nodeZeroOrOne.intersectionMerge(nodeZeroOrMore);
        final var $zeroOrOne_oneOrMore = nodeZeroOrOne.intersectionMerge(nodeOneOrMore);

        assertEquals($zeroOrOne_0, empty);
        assertEquals($zeroOrOne_1, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrOne, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrMore, nodeZeroOrOne);
        assertEquals($zeroOrOne_oneOrMore, nodeZeroOrOne);

        final var $zeroOrMore_0 = nodeZeroOrMore.intersectionMerge(empty);
        final var $zeroOrMore_1 = nodeZeroOrMore.intersectionMerge(node);
        final var $zeroOrMore_zeroOrOne = nodeZeroOrMore.intersectionMerge(nodeZeroOrOne);
        final var $zeroOrMore_zeroOrMore = nodeZeroOrMore.intersectionMerge(nodeZeroOrMore);
        final var $zeroOrMore_oneOrMore = nodeZeroOrMore.intersectionMerge(nodeOneOrMore);

        assertEquals($zeroOrMore_0, empty);
        assertEquals($zeroOrMore_1, nodeZeroOrOne);
        assertEquals($zeroOrMore_zeroOrOne, nodeZeroOrOne);
        assertEquals($zeroOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, nodeZeroOrMore);

        final var $oneOrMore_0 = nodeOneOrMore.intersectionMerge(empty);
        final var $oneOrMore_1 = nodeOneOrMore.intersectionMerge(node);
        final var $oneOrMore_zeroOrOne = nodeOneOrMore.intersectionMerge(nodeZeroOrOne);
        final var $oneOrMore_zeroOrMore = nodeOneOrMore.intersectionMerge(nodeZeroOrMore);
        final var $oneOrMore_oneOrMore = nodeOneOrMore.intersectionMerge(nodeOneOrMore);

        assertEquals($oneOrMore_0, empty);
        assertEquals($oneOrMore_1, nodeZeroOrOne);
        assertEquals($oneOrMore_zeroOrOne, nodeZeroOrOne);
        assertEquals($oneOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($oneOrMore_oneOrMore, nodeZeroOrMore);


        final var elementFoo = typeFactory.element(Set.of("foo", "x"));
        final var elementBar = typeFactory.element(Set.of("bar", "x"));
        final var merged$elements = elementFoo.intersectionMerge(elementBar);
        assertEquals(merged$elements, typeFactory.zeroOrOne(typeFactory.itemElement(Set.of("x"))));

        final var merged$any = elementFoo.intersectionMerge(anyNode);
        assertEquals(merged$any, typeFactory.zeroOrOne(typeFactory.itemElement(Set.of("foo", "x"))));

        final var merged$any2 = anyNode.intersectionMerge(elementFoo);
        assertEquals(merged$any2, typeFactory.zeroOrOne(typeFactory.itemElement(Set.of("foo", "x"))));
    }

    @Test
    public void exceptNodeMerging() {
        final var empty = typeFactory.emptySequence();
        final var node = typeFactory.anyNode();
        final var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        final var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());

        final var $00 = empty.exceptionMerge(empty);
        final var $01 = empty.exceptionMerge(node);
        final var $0_zeroOrOne = empty.exceptionMerge(nodeZeroOrOne);
        final var $0_zeroOrMore = empty.exceptionMerge(nodeZeroOrMore);
        final var $0_oneOrMore = empty.exceptionMerge(nodeOneOrMore);
        assertEquals($00, empty);
        assertEquals($01, empty);
        assertEquals($0_zeroOrOne, empty);
        assertEquals($0_zeroOrMore, empty);
        assertEquals($0_oneOrMore, empty);

        final var $10 = node.exceptionMerge(empty);
        final var $11 = node.exceptionMerge(node);
        final var $1_zeroOrOne = node.exceptionMerge(nodeZeroOrOne);
        final var $1_zeroOrMore = node.exceptionMerge(nodeZeroOrMore);
        final var $1_oneOrMore = node.exceptionMerge(nodeOneOrMore);

        assertEquals($10, node);
        assertEquals($11, nodeZeroOrOne);
        assertEquals($1_zeroOrOne, nodeZeroOrOne);
        assertEquals($1_zeroOrMore, nodeZeroOrOne);
        assertEquals($1_oneOrMore, nodeZeroOrOne);

        final var $zeroOrOne_0 = nodeZeroOrOne.exceptionMerge(empty);
        final var $zeroOrOne_1 = nodeZeroOrOne.exceptionMerge(node);
        final var $zeroOrOne_zeroOrOne = nodeZeroOrOne.exceptionMerge(nodeZeroOrOne);
        final var $zeroOrOne_zeroOrMore = nodeZeroOrOne.exceptionMerge(nodeZeroOrMore);
        final var $zeroOrOne_oneOrMore = nodeZeroOrOne.exceptionMerge(nodeOneOrMore);

        assertEquals($zeroOrOne_0, nodeZeroOrOne);
        assertEquals($zeroOrOne_1, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrOne, nodeZeroOrOne);
        assertEquals($zeroOrOne_zeroOrMore, nodeZeroOrOne);
        assertEquals($zeroOrOne_oneOrMore, nodeZeroOrOne);

        final var $zeroOrMore_0 = nodeZeroOrMore.exceptionMerge(empty);
        final var $zeroOrMore_1 = nodeZeroOrMore.exceptionMerge(node);
        final var $zeroOrMore_zeroOrOne = nodeZeroOrMore.exceptionMerge(nodeZeroOrOne);
        final var $zeroOrMore_zeroOrMore = nodeZeroOrMore.exceptionMerge(nodeZeroOrMore);
        final var $zeroOrMore_oneOrMore = nodeZeroOrMore.exceptionMerge(nodeOneOrMore);

        assertEquals($zeroOrMore_0, nodeZeroOrMore);
        assertEquals($zeroOrMore_1, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($zeroOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($zeroOrMore_oneOrMore, nodeZeroOrMore);

        final var $oneOrMore_0 = nodeOneOrMore.exceptionMerge(empty);
        final var $oneOrMore_1 = nodeOneOrMore.exceptionMerge(node);
        final var $oneOrMore_zeroOrOne = nodeOneOrMore.exceptionMerge(nodeZeroOrOne);
        final var $oneOrMore_zeroOrMore = nodeOneOrMore.exceptionMerge(nodeZeroOrMore);
        final var $oneOrMore_oneOrMore = nodeOneOrMore.exceptionMerge(nodeOneOrMore);

        assertEquals($oneOrMore_0, nodeOneOrMore);
        assertEquals($oneOrMore_1, nodeZeroOrMore);
        assertEquals($oneOrMore_zeroOrOne, nodeZeroOrMore);
        assertEquals($oneOrMore_zeroOrMore, nodeZeroOrMore);
        assertEquals($oneOrMore_oneOrMore, nodeZeroOrMore);

        final var elementFoo = typeFactory.element(Set.of("foo"));
        final var elementBar = typeFactory.element(Set.of("bar"));
        final var merged$elements = elementFoo.exceptionMerge(elementBar);
        assertEquals(merged$elements, typeFactory.zeroOrOne(typeFactory.itemElement(Set.of("foo"))));

        final var merged$any = elementFoo.exceptionMerge(anyNode);
        assertEquals(merged$any, typeFactory.zeroOrOne(typeFactory.itemElement(Set.of("foo"))));

        final var merged$any2 = anyNode.exceptionMerge(elementFoo);
        assertEquals(merged$any2, typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
    }


}
