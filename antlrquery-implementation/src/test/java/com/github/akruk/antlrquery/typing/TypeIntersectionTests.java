package com.github.akruk.antlrquery.typing;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeIntersectionTests extends TypesTestBase {

    @Test
    void intersectionOfSuperAndSubTypeInRegardsToItemType() {
        assertEquals(itemNumber, ItemTypes.intersect(typeFactory, itemAnyItem, itemNumber));
    }

    @Test
    void intersectionOfSuperAndSubTypeInRegardsToCardinality() {
        assertEquals(number, Types.intersect(typeFactory, typeFactory.zeroOrOne(itemNumber), number));
    }

    @Test
    void intersectionOfSuperAndSubTypeCombined() {
        assertEquals(number, Types.intersect(typeFactory, typeFactory.zeroOrOne(itemNumber), anyItem));
    }

    @Test
    void intersectionOfConstrainedFunctions() {
        assertEquals(
                typeFactory.function(
                        number,
                        List.of( typeFactory.zeroOrOne(typeFactory.itemNumber()) )
                ),
                Types.intersect(
                        typeFactory,
                        typeFactory.function(
                                typeFactory.zeroOrOne(typeFactory.itemNumber()),
                                List.of( typeFactory.zeroOrOne(typeFactory.itemNumber()) )
                        ),
                        typeFactory.zeroOrOne(typeFactory.itemFunction(
                                anyItem,
                                List.of( zeroOrMoreItems )
                        ))
                )
        );
    }


    private void assertIntersectionBothOrders(AntlrQuerySequenceType expected, AntlrQuerySequenceType a, AntlrQuerySequenceType b) {
        var actualAB = Types.intersect(typeFactory, a, b);
        var actualBA = Types.intersect(typeFactory, b, a);
        assertEquals(expected, actualAB);
        assertEquals(expected, actualBA);
    }

    @Test
    public void emptyIntersectionsProduceEmpty() {
        var empty = typeFactory.emptySequence();
        var node = typeFactory.anyNode();
        var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());

        assertIntersectionBothOrders(empty, empty, empty);
        assertIntersectionBothOrders(null, empty, node);
        assertIntersectionBothOrders(empty, empty, nodeZeroOrOne);
        assertIntersectionBothOrders(empty, empty, nodeZeroOrMore);
        assertIntersectionBothOrders(null, empty, nodeOneOrMore);
    }

    @Test
    public void multiplicityMergingForNodeTypes() {
        var empty = typeFactory.emptySequence();
        var node = typeFactory.anyNode();
        var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        var nodeZeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        var nodeOneOrMore = typeFactory.oneOrMore(typeFactory.itemAnyNode());

        assertIntersectionBothOrders(null, node, empty);

        assertIntersectionBothOrders(node, node, node);

        assertIntersectionBothOrders(node, node, nodeZeroOrOne);
        assertIntersectionBothOrders(node, node, nodeZeroOrMore);
        assertIntersectionBothOrders(node, node, nodeOneOrMore);

        assertIntersectionBothOrders(nodeZeroOrOne, nodeZeroOrOne, nodeZeroOrOne);
        assertIntersectionBothOrders(nodeZeroOrMore, nodeZeroOrMore, nodeZeroOrMore);
        assertIntersectionBothOrders(nodeOneOrMore, nodeOneOrMore, nodeOneOrMore);

        assertIntersectionBothOrders(node, nodeZeroOrOne, nodeOneOrMore);
        assertIntersectionBothOrders(nodeZeroOrOne, nodeZeroOrMore, nodeZeroOrOne);
        assertIntersectionBothOrders(nodeOneOrMore, nodeZeroOrMore, nodeOneOrMore);
    }

    @Test
    public void elementNameIntersectionProducesCommonNamesOnly() {
        var elementFoo = typeFactory.element("", Set.of(
                new NamespaceResolver.QualifiedName("", "foo"),
                new NamespaceResolver.QualifiedName("", "x")
        ));

        var elementBar = typeFactory.element("", Set.of(
                new NamespaceResolver.QualifiedName("", "bar"),
                new NamespaceResolver.QualifiedName("", "x")
        ));

        var expectedCommon = typeFactory.one(
                typeFactory.itemNodesFromGrammar("", Set.of(new NamespaceResolver.QualifiedName("", "x")))
        );

        assertIntersectionBothOrders(expectedCommon, elementFoo, elementBar);
    }

    @Test
    public void anyNodeIntersectPreservesOtherNames() {
        var anyNode = typeFactory.anyNode();

        var elementFoo = typeFactory.element("", Set.of(
                new NamespaceResolver.QualifiedName("", "foo"),
                new NamespaceResolver.QualifiedName("", "x")
        ));

        var expected = typeFactory.one(
                typeFactory.itemNodesFromGrammar("", Set.of(
                        new NamespaceResolver.QualifiedName("", "foo"),
                        new NamespaceResolver.QualifiedName("", "x")
                ))
        );

        assertIntersectionBothOrders(expected, anyNode, elementFoo);
    }

    @Test
    public void groupedBidirectionalResultsAreConsistent() {
        var empty = typeFactory.emptySequence();
        var node = typeFactory.anyNode();
        var nodeZeroOrOne = typeFactory.zeroOrOne(typeFactory.itemAnyNode());

        var pairs = Map.of(
                "empty-node", new Object[]{empty, node},
                "node-zeroOrOne", new Object[]{node, nodeZeroOrOne},
                "empty-zeroOrOne", new Object[]{empty, nodeZeroOrOne}
        );

        for (var e : pairs.entrySet()) {
            var a = (AntlrQuerySequenceType) e.getValue()[0];
            var b = (AntlrQuerySequenceType) e.getValue()[1];
            var resultAB = Types.intersect(typeFactory, a, b);
            var resultBA = Types.intersect(typeFactory, b, a);
            assertEquals(resultAB, resultBA);
        }
    }

}
