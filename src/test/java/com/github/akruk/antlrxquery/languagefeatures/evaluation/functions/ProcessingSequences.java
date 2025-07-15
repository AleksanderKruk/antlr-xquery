package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.util.List;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrxquery.values.XQueryString;

public class ProcessingSequences extends EvaluationTestsBase {

    @Test public void testEmptyOutOfBoundsFilter() {
        assertResult("empty((1, 2, 3)[10])", baseFactory.bool(true));
    }

    @Test public void testEmptyAfterRemove() {
        assertResult("empty(remove(('hello', 'world'), 1))", baseFactory.bool(false));
    }

    @Test public void testEmptyArray() {
        assertResult("empty([])", baseFactory.bool(false));
    }

    @Test public void testEmptyMap() {
        assertResult("empty({})", baseFactory.bool(false));
    }

    @Test public void testEmptyString() {
        assertResult("empty('')", baseFactory.bool(false));
    }

    @Test public void testExistsRemoveSingle() {
        assertResult("exists(remove(('hello'), 1))", baseFactory.bool(false));
    }

    @Test public void testExistsAfterRemovePartial() {
        assertResult("exists(remove(('hello', 'world'), 1))", baseFactory.bool(true));
    }

    @Test public void testExistsEmptyArray() {
        assertResult("exists([])", baseFactory.bool(true));
    }

    @Test public void testExistsEmptyMap() {
        assertResult("exists({})", baseFactory.bool(true));
    }

    @Test public void testExistsEmptyString() {
        assertResult("exists('')", baseFactory.bool(true));
    }


    @Test public void testFootLastItem() {
        assertResult("foot(1 to 5)", baseFactory.number(5));
    }

    @Test public void testFootEmpty() {
        assertResult("foot(())", baseFactory.emptySequence());
    }

    @Test public void testHeadFirst() {
        assertResult("head(('a','b','c'))", baseFactory.string("a"));
    }

    @Test public void testHeadArray() {
        assertResult("head([1,2,3])", baseFactory.array(List.of(
            baseFactory.number(1),
            baseFactory.number(2),
            baseFactory.number(3)
        )));
    }


    @Test public void testIdentityEmpty() {
        assertResult("identity(())", baseFactory.emptySequence());
    }

    @Test public void testVoidBigSequence() {
        assertResult("void(1 to 1000000)", baseFactory.emptySequence());
    }
    @Test public void testInsertAtVarious() {
        assertResult("insert-before(('a','b','c'), 2, 'z')",
            baseFactory.sequence(List.of(
                baseFactory.string("a"),
                baseFactory.string("z"),
                baseFactory.string("b"),
                baseFactory.string("c")
            ))
        );
    }

    @Test public void testRemoveOutOfBounds() {
        assertResult("remove(('a','b','c'), 6)",
            baseFactory.sequence(List.of(
                baseFactory.string("a"),
                baseFactory.string("b"),
                baseFactory.string("c")
            ))
        );
    }

    @Test public void testReplicateZero() {
        assertResult("replicate(('A','B','C'), 0)",
            baseFactory.emptySequence()
        );
    }

    @Test public void testReverseArraySequence() {
        assertResult("reverse(([1,2,3], [4,5,6]))",
            baseFactory.sequence(List.of(
                baseFactory.array(List.of(
                    baseFactory.number(4),
                    baseFactory.number(5),
                    baseFactory.number(6)
                )),
                baseFactory.array(List.of(
                    baseFactory.number(1),
                    baseFactory.number(2),
                    baseFactory.number(3)
                ))
            ))
        );
    }

    @Test public void testSequenceJoinSimple() {
        assertResult("sequence-join(1 to 3, '|')",
            baseFactory.sequence(List.of(
                baseFactory.number(1),
                baseFactory.string("|"),
                baseFactory.number(2),
                baseFactory.string("|"),
                baseFactory.number(3)
            ))
        );
    }

@Test public void testSliceStart2End4() {
    assertResult("slice(('a','b','c','d','e'), 2, 4)",
        baseFactory.sequence(List.of(
            baseFactory.string("b"),
            baseFactory.string("c"),
            baseFactory.string("d")
        ))
    );
}

@Test public void testSliceStart2Only() {
    assertResult("slice(('a','b','c','d','e'), 2)",
        baseFactory.sequence(List.of(
            baseFactory.string("b"),
            baseFactory.string("c"),
            baseFactory.string("d"),
            baseFactory.string("e")
        ))
    );
}

@Test public void testSliceEnd2Only() {
    assertResult("slice(('a','b','c','d','e'), (), 2)",
        baseFactory.sequence(List.of(
            baseFactory.string("a"),
            baseFactory.string("b")
        ))
    );
}

@Test public void testSliceStart3End3() {
    assertResult("slice(('a','b','c','d','e'), 3, 3)",
        baseFactory.sequence(List.of(
            baseFactory.string("c")
        ))
    );
}

@Test public void testSliceStart4End3() {
    assertResult("slice(('a','b','c','d','e'), 4, 3)",
        baseFactory.sequence(List.of(
            baseFactory.string("d"),
            baseFactory.string("c")
        ))
    );
}

@Test public void testSliceStart2End5Step2() {
    assertResult("slice(('a','b','c','d','e'), 2, 5, 2)",
        baseFactory.sequence(List.of(
            baseFactory.string("b"),
            baseFactory.string("d")
        ))
    );
}

@Test public void testSliceStart5End2StepNegative2() {
    assertResult("slice(('a','b','c','d','e'), 5, 2, -2)",
        baseFactory.sequence(List.of(
            baseFactory.string("e"),
            baseFactory.string("c")
        ))
    );
}

@Test public void testSliceStart2End5StepNegative2() {
    assertResult("slice(('a','b','c','d','e'), 2, 5, -2)",
        baseFactory.sequence(List.of())
    );
}

@Test public void testSliceStart5End2Step2() {
    assertResult("slice(('a','b','c','d','e'), 5, 2, 2)",
        baseFactory.sequence(List.of())
    );
}

@Test public void testSliceDefault() {
    assertResult("slice(('a','b','c','d','e'))",
        baseFactory.sequence(List.of(
            baseFactory.string("a"),
            baseFactory.string("b"),
            baseFactory.string("c"),
            baseFactory.string("d"),
            baseFactory.string("e")
        ))
    );
}

@Test public void testSliceStartNegative1() {
    assertResult("slice(('a','b','c','d','e'), -1)",
        baseFactory.sequence(List.of(
            baseFactory.string("e")
        ))
    );
}

@Test public void testSliceStartNegative3() {
    assertResult("slice(('a','b','c','d','e'), -3)",
        baseFactory.sequence(List.of(
            baseFactory.string("c"),
            baseFactory.string("d"),
            baseFactory.string("e")
        ))
    );
}

@Test public void testSliceEndNegative2Only() {
    assertResult("slice(('a','b','c','d','e'), (), -2)",
        baseFactory.sequence(List.of(
            baseFactory.string("a"),
            baseFactory.string("b"),
            baseFactory.string("c"),
            baseFactory.string("d")
        ))
    );
}

@Test public void testSliceStart2EndNegative2() {
    assertResult("slice(('a','b','c','d','e'), 2, -2)",
        baseFactory.sequence(List.of(
            baseFactory.string("b"),
            baseFactory.string("c"),
            baseFactory.string("d")
        ))
    );
}

@Test public void testSliceStartNegative2End2() {
    assertResult("slice(('a','b','c','d','e'), -2, 2)",
        baseFactory.sequence(List.of(
            baseFactory.string("d"),
            baseFactory.string("c"),
            baseFactory.string("b")
        ))
    );
}

@Test public void testSliceStartNegative4EndNegative2() {
    assertResult("slice(('a','b','c','d','e'), -4, -2)",
        baseFactory.sequence(List.of(
            baseFactory.string("b"),
            baseFactory.string("c"),
            baseFactory.string("d")
        ))
    );
}

@Test public void testSliceStartNegative2EndNegative4() {
    assertResult("slice(('a','b','c','d','e'), -2, -4)",
        baseFactory.sequence(List.of(
            baseFactory.string("d"),
            baseFactory.string("c"),
            baseFactory.string("b")
        ))
    );
}

@Test public void testSliceStartNegative4EndNegative2Step2() {
    assertResult("slice(('a','b','c','d','e'), -4, -2, 2)",
        baseFactory.sequence(List.of(
            baseFactory.string("b"),
            baseFactory.string("d")
        ))
    );
}

@Test public void testSliceStartNegative2EndNegative4StepNegative2() {
    assertResult("slice(('a','b','c','d','e'), -2, -4, -2)",
        baseFactory.sequence(List.of(
            baseFactory.string("d"),
            baseFactory.string("b")
        ))
    );
}

@Test public void testSliceZeroStart() {
    assertResult("slice(('a','b','c','d'), 0)",
        baseFactory.sequence(List.of(
            baseFactory.string("a"),
            baseFactory.string("b"),
            baseFactory.string("c"),
            baseFactory.string("d")
        ))
    );
}

@Test public void testSliceStep2OverRange() {
    assertResult("slice((1 to 5), (), (), 2)",
        baseFactory.sequence(List.of(
            baseFactory.number(1),
            baseFactory.number(3),
            baseFactory.number(5)
        ))
    );
}


    @Test public void testSubsequenceSimple() {
        assertResult("subsequence(('item1','item2','item3','item4','item5'), 3, 2)",
            baseFactory.sequence(List.of(
                baseFactory.string("item3"),
                baseFactory.string("item4")
            ))
        );
    }

    @Test public void testItemsAtSingleIndex() {
        assertResult("items-at(11 to 20, 4)",
            baseFactory.number(14)
        );
    }

    @Test public void testItemsAtRange() {
        assertResult("items-at(11 to 20, 4 to 6)",
            baseFactory.sequence(List.of(
                baseFactory.number(14),
                baseFactory.number(15),
                baseFactory.number(16)
            ))
        );
    }

    @Test public void testItemsAtUnorderedIndexes() {
        assertResult("items-at(11 to 20, (7, 3))",
            baseFactory.sequence(List.of(
                baseFactory.number(17),
                baseFactory.number(13)
            ))
        );
    }

    @Test public void testItemsAtCharactersAtMultiplePositions() {
        assertResult("items-at(characters('quintessential'), (4, 8, 3))",
            baseFactory.sequence(List.of(
                baseFactory.string("n"),
                baseFactory.string("s"),
                baseFactory.string("i")
            ))
        );
    }

    @Test public void testItemsAtCharactersDuplicateIndexes() {
        assertResult("items-at(characters('quintessential'), (4, 8, 1, 1))",
            baseFactory.sequence(List.of(
                baseFactory.string("n"),
                baseFactory.string("s"),
                baseFactory.string("q"),
                baseFactory.string("q")
            ))
        );
    }

    @Test public void testItemsAtEmptySequenceWithIndex() {
        assertResult("items-at((), 832)",
            baseFactory.emptySequence()
        );
    }

    @Test public void testItemsAtEmptySequenceWithEmptyIndexes() {
        assertResult("items-at((), ())",
            baseFactory.emptySequence()
        );
    }


    @Test
    public void remove() {
        var a = baseFactory.string("a");
        var b = baseFactory.string("b");
        var c = baseFactory.string("c");
        // The expression fn:remove($abc, 0) returns ("a", "b", "c").
        assertResult("""
                remove(("a", "b", "c"), 0)
                """, List.of(a, b, c));
        // The expression fn:remove($abc, 1) returns ("b", "c").
        assertResult("""
                remove(("a", "b", "c"), 1)
                """, List.of(b, c));
        // The expression fn:remove($abc, 6) returns ("a", "b", "c").
        assertResult("""
                remove(("a", "b", "c"), 6)
                """, List.of(a, b, c));
        // The expression fn:remove((), 3) returns ().
        assertResult("remove((), 3)", List.of());
    }

    @Test
    public void reverse() {
        var a = new XQueryString("a", baseFactory);
        var b = new XQueryString("b", baseFactory);
        var c = new XQueryString("c", baseFactory);
        // The expression fn:reverse($abc) returns ("c", "b", "a").
        assertResult("""
                reverse(("a", "b", "c"))
                """, List.of(c, b, a));
        // The expression fn:reverse(("hello")) returns ("hello").
        assertResult("reverse((\"Hello\"))", List.of(new XQueryString("Hello", baseFactory)));
        // The expression fn:reverse(()) returns ().
        assertResult("reverse(())", List.of());
        // The expression fn:reverse([1,2,3]) returns [1,2,3]. (The input is a sequence
        // containing a single item (the array)).
        // The expression fn:reverse(([1,2,3],[4,5,6])) returns ([4,5,6],[1,2,3]).
    }

    @Test
    public void subsequence() {
        // var i1 = new XQueryString("item1");
        // var i2 = new XQueryString("item2");
        var i3 = new XQueryString("item3", baseFactory);
        var i4 = new XQueryString("item4", baseFactory);
        var i5 = new XQueryString("item5", baseFactory);
        // The expression fn:subsequence($seq, 4) returns ("item4", "item5").
        // The expression fn:subsequence($seq, 3, 2) returns ("item3", "item4").
        assertResult("""
                    subsequence(("item1", "item2", "item3", "item4", "item5"), 4)
                """, List.of(i4, i5));
        assertResult("""
                    subsequence(("item1", "item2", "item3", "item4", "item5"), 3, 2)
                """, List.of(i3, i4));
    }

    @Test public void testRemoveIndexZero() {
        assertResult("remove(('a','b','c'), 0)",
            baseFactory.sequence(List.of(
                baseFactory.string("a"),
                baseFactory.string("b"),
                baseFactory.string("c")
            ))
        );
    }

    @Test public void testRemoveIndexOne() {
        assertResult("remove(('a','b','c'), 1)",
            baseFactory.sequence(List.of(
                baseFactory.string("b"),
                baseFactory.string("c")
            ))
        );
    }

    @Test public void testRemoveIndexOutOfBounds() {
        assertResult("remove(('a','b','c'), 6)",
            baseFactory.sequence(List.of(
                baseFactory.string("a"),
                baseFactory.string("b"),
                baseFactory.string("c")
            ))
        );
    }

    @Test public void testRemoveFromEmpty() {
        assertResult("remove((), 3)",
            baseFactory.emptySequence()
        );
    }

    @Test public void testRemoveRangeTwoToThree() {
        assertResult("remove(('a','b','c'), 2 to 3)",
            baseFactory.sequence(List.of(
                baseFactory.string("a")
            ))
        );
    }

    @Test public void testRemoveEmptyIndex() {
        assertResult("remove(('a','b','c'), ())",
            baseFactory.sequence(List.of(
                baseFactory.string("a"),
                baseFactory.string("b"),
                baseFactory.string("c")
            ))
        );
    }


    @Test public void testReplicateAtomicValueMultipleTimes() {
        assertResult("replicate(0, 6)",
            baseFactory.sequence(List.of(
                baseFactory.number(0),
                baseFactory.number(0),
                baseFactory.number(0),
                baseFactory.number(0),
                baseFactory.number(0),
                baseFactory.number(0)
            ))
        );
    }

    @Test public void testReplicateSequenceThreeTimes() {
        assertResult("replicate(('A','B','C'), 3)",
            baseFactory.sequence(List.of(
                baseFactory.string("A"),
                baseFactory.string("B"),
                baseFactory.string("C"),
                baseFactory.string("A"),
                baseFactory.string("B"),
                baseFactory.string("C"),
                baseFactory.string("A"),
                baseFactory.string("B"),
                baseFactory.string("C")
            ))
        );
    }

    @Test public void testReplicateEmptySequence() {
        assertResult("replicate((), 5)",
            baseFactory.emptySequence()
        );
    }

    @Test public void testReplicateSequenceOnce() {
        assertResult("replicate(('A','B','C'), 1)",
            baseFactory.sequence(List.of(
                baseFactory.string("A"),
                baseFactory.string("B"),
                baseFactory.string("C")
            ))
        );
    }

    @Test public void testReplicateZeroTimes() {
        assertResult("replicate(('A','B','C'), 0)",
            baseFactory.emptySequence()
        );
    }


    @Test public void testSequenceJoinNumberWithStringSeparator() {
        assertResult("sequence-join(1 to 5, '|')",
            baseFactory.sequence(List.of(
                baseFactory.number(1),
                baseFactory.string("|"),
                baseFactory.number(2),
                baseFactory.string("|"),
                baseFactory.number(3),
                baseFactory.string("|"),
                baseFactory.number(4),
                baseFactory.string("|"),
                baseFactory.number(5)
            ))
        );
    }

    @Test public void testSequenceJoinEmptySequence() {
        assertResult("sequence-join((), '|')",
            baseFactory.emptySequence()
        );
    }

    @Test public void testSequenceJoinSingleItemSequence() {
        assertResult("sequence-join('A', '|')",
            baseFactory.string("A")
        );
    }

    @Test public void testSequenceJoinCompositeSeparator() {
        assertResult("sequence-join(1 to 3, ('⅓','⅔'))",
            baseFactory.sequence(List.of(
                baseFactory.number(1),
                baseFactory.string("⅓"),
                baseFactory.string("⅔"),
                baseFactory.number(2),
                baseFactory.string("⅓"),
                baseFactory.string("⅔"),
                baseFactory.number(3)
            ))
        );
    }


    @Test public void testTrunkRange() {
        assertResult("trunk(1 to 5)",
            baseFactory.sequence(List.of(
                baseFactory.number(1),
                baseFactory.number(2),
                baseFactory.number(3),
                baseFactory.number(4)
            ))
        );
    }

    @Test public void testTrunkStringSequence() {
        assertResult("trunk(('a','b','c'))",
            baseFactory.sequence(List.of(
                baseFactory.string("a"),
                baseFactory.string("b")
            ))
        );
    }

    @Test public void testTrunkSingleItem() {
        assertResult("trunk('a')",
            baseFactory.emptySequence()
        );
    }

    @Test public void testTrunkEmptySequence() {
        assertResult("trunk(())",
            baseFactory.emptySequence()
        );
    }

    @Test public void testTrunkArray() {
        assertResult("trunk([1,2,3])",
            baseFactory.emptySequence()
        );
    }


@Test public void testTailRange() {
    assertResult("tail(1 to 5)",
        baseFactory.sequence(List.of(
            baseFactory.number(2),
            baseFactory.number(3),
            baseFactory.number(4),
            baseFactory.number(5)
        ))
    );
}

@Test public void testTailStringSequence() {
    assertResult("tail(('a','b','c'))",
        baseFactory.sequence(List.of(
            baseFactory.string("b"),
            baseFactory.string("c")
        ))
    );
}

@Test public void testTailSingleItem() {
    assertResult("tail('a')",
        baseFactory.emptySequence()
    );
}

@Test public void testTailEmptySequence() {
    assertResult("tail(())",
        baseFactory.emptySequence()
    );
}

@Test public void testTailArraySingleton() {
    assertResult("tail([1,2,3])",
        baseFactory.emptySequence()
    );
}


}
