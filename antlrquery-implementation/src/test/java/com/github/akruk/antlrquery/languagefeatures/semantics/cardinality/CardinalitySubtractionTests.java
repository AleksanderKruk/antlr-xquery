package com.github.akruk.antlrquery.languagefeatures.semantics.cardinality;

import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CardinalitySubtractionTests extends CardinalityTestUtils {

    @Test
    public void subtraction_multiInterval_singlePoint() {
        assertEquals(
                Cardinalities.union(point(1), range(3, 10)),
                Cardinalities.subtract(range(1, 10), point(2))
        );
    }

    @Test
    public void subtraction_multiInterval_then_subtract_leftSegment() {
        Cardinality first = Cardinalities.subtract(
                Cardinality.inclusiveRange(1, 10),
                Cardinality.of(5)
        );

        Assertions.assertNotNull(first);
        assertEquals(
                Cardinalities.union(
                        Cardinality.inclusiveRange(1, 3),
                        Cardinality.inclusiveRange(6, 10)
                ),
                Cardinalities.subtract(
                        first,
                        Cardinality.of(4)
                )
        );
    }

    @Test
    public void subtraction_multiInterval_then_subtract_rightSegment() {
        Cardinality first = Cardinalities.subtract(
                Cardinality.inclusiveRange(1, 10),
                Cardinality.of(5)
        );

        Assertions.assertNotNull(first);
        assertEquals(
                Cardinalities.union(
                        Cardinality.inclusiveRange(1, 4),
                        Cardinality.inclusiveRange(7, 10)
                ),
                Cardinalities.subtract(
                        first,
                        Cardinality.of(6)
                )
        );
    }

    @Test
    public void subtraction_multiInterval_then_split_both_segments() {
        Cardinality first = Cardinalities.subtract(
                Cardinality.inclusiveRange(1, 10),
                Cardinality.of(5)
        );

        Assertions.assertNotNull(first);
        assertEquals(
                Cardinalities.union(
                        Cardinality.inclusiveRange(1, 2),
                        Cardinality.inclusiveRange(4, 4),
                        Cardinality.inclusiveRange(6, 8),
                        Cardinality.of(10)
                ),
                Cardinalities.subtract(
                        first,
                        Cardinalities.union(
                                Cardinality.of(3),
                                Cardinality.of(9)
                        )
                )
        );
    }

    @Test
    public void subtraction_multiInterval_y_spans_gap() {
        Cardinality x = Cardinalities.union(
                Cardinality.inclusiveRange(1, 4),
                Cardinality.inclusiveRange(7, 10)
        );

        assertEquals(
                Cardinalities.union(
                        Cardinality.of(1),
                        Cardinality.inclusiveRange(9, 10)
                ),
                Cardinalities.subtract(
                        x,
                        Cardinality.inclusiveRange(2, 8)
                )
        );
    }

    @Test
    public void subtraction_multiInterval_y_covers_multiple_segments() {
        Cardinality x = Cardinalities.union(
                Cardinality.inclusiveRange(1, 3),
                Cardinality.inclusiveRange(5, 7),
                Cardinality.inclusiveRange(9, 11)
        );

        assertEquals(
                Cardinalities.union(Cardinality.of(1), Cardinality.of(11)),
                Cardinalities.subtract(
                        x,
                        Cardinality.inclusiveRange(2, 10)
                )
        );
    }

    @Test
    public void subtraction_repeated_splitting() {
        Cardinality result = Cardinality.inclusiveRange(1, 20);

        result = Cardinalities.subtract(result, Cardinality.of(5));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.of(10));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.of(15));

        assertEquals(
                Cardinalities.union(
                        Cardinality.inclusiveRange(1, 4),
                        Cardinality.inclusiveRange(6, 9),
                        Cardinality.inclusiveRange(11, 14),
                        Cardinality.inclusiveRange(16, 20)
                ),
                result
        );
    }

    @Test
    public void subtraction_repeated_splitting_nested() {
        Cardinality result = Cardinality.inclusiveRange(1, 16);

        result = Cardinalities.subtract(result, Cardinality.of(8));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.of(4));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.of(12));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.of(2));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.of(6));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.of(10));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.of(14));

        assertEquals(
                Cardinalities.union(
                        Cardinality.of(1),
                        Cardinality.of(3),
                        Cardinality.of(5),
                        Cardinality.of(7),
                        Cardinality.of(9),
                        Cardinality.of(11),
                        Cardinality.of(13),
                        Cardinality.of(15),
                        Cardinality.of(16)
                ),
                result
        );
    }

    @Test
    public void subtraction_multiple_right_intervals() {
        assertEquals(
                Cardinalities.union(
                        Cardinality.inclusiveRange(1, 2),
                        Cardinality.inclusiveRange(4, 5),
                        Cardinality.inclusiveRange(7, 8),
                        Cardinality.inclusiveRange(10, 12)
                ),
                Cardinalities.subtract(
                        Cardinality.inclusiveRange(1, 12),
                        Cardinality.of(3),
                        Cardinality.of(6),
                        Cardinality.of(9)
                )
        );
    }

    @Test
    public void subtraction_multiple_right_ranges() {
        assertEquals(
                Cardinalities.union(
                        Cardinality.of(1),
                        Cardinality.inclusiveRange(4, 6),
                        Cardinality.inclusiveRange(9, 10),
                        Cardinality.of(13)
                ),
                Cardinalities.subtract(
                        Cardinality.inclusiveRange(1, 13),
                        Cardinalities.union(
                                Cardinality.inclusiveRange(2, 3),
                                Cardinality.inclusiveRange(7, 8),
                                Cardinality.inclusiveRange(11, 12)
                        )
                )
        );
    }

    @Test
    public void subtraction_right_range_splits_multiple_left_segments() {
        final Cardinality left = Cardinalities.union(
                Cardinality.inclusiveRange(1, 5),
                Cardinality.inclusiveRange(10, 15),
                Cardinality.inclusiveRange(20, 25)
        );

        final Cardinality right = Cardinalities.union(
                Cardinality.inclusiveRange(3, 12),
                Cardinality.inclusiveRange(22, 23)
        );

        assertEquals(
                Cardinalities.union(
                        Cardinality.inclusiveRange(1, 2),
                        Cardinality.inclusiveRange(13, 15),
                        Cardinality.inclusiveRange(20, 21),
                        Cardinality.inclusiveRange(24, 25)
                ),
                Cardinalities.subtract(left, right)
        );
    }

    @Test
    public void subtraction_multiInterval_result_then_large_subtraction() {
        Cardinality result = Cardinalities.subtract(
                Cardinality.inclusiveRange(1, 20),
                Cardinality.inclusiveRange(8, 12)
        );

        Assertions.assertNotNull(result);
        assertEquals(
                Cardinalities.union(
                        Cardinality.inclusiveRange(1, 4),
                        Cardinality.inclusiveRange(16, 20)
                ),
                Cardinalities.subtract(
                        result,
                        Cardinality.inclusiveRange(5, 15)
                )
        );
    }


    @Test
    public void subtraction_multiInterval_boundary_after_split() {
        Cardinality result = Cardinalities.subtract(
                Cardinality.inclusiveRange(1, 10),
                Cardinality.of(5)
        );

        Assertions.assertNotNull(result);
        assertEquals(
                Cardinalities.union(
                        Cardinality.inclusiveRange(1, 3),
                        Cardinality.inclusiveRange(7, 10)
                ),
                Cardinalities.subtract(
                        result,
                        Cardinality.of(4),
                        Cardinality.of(6)
                )
        );
    }


    @Test
    public void subtraction_multiInterval_remove_one_segment() {
        final Cardinality left = Cardinalities.union(
                Cardinality.inclusiveRange(1, 5),
                Cardinality.inclusiveRange(10, 15)
        );

        assertEquals(
                Cardinality.inclusiveRange(10, 15),
                Cardinalities.subtract(
                        left,
                        Cardinality.inclusiveRange(1, 5)
                )
        );
    }
    @Test
    public void subtraction_multiInterval_remove_part_of_one_segment() {
        final Cardinality left = Cardinalities.union(
                Cardinality.inclusiveRange(1, 5),
                Cardinality.inclusiveRange(10, 15)
        );

        assertEquals(
                Cardinalities.union(
                        Cardinality.inclusiveRange(1, 5),
                        Cardinality.inclusiveRange(13, 15)
                ),
                Cardinalities.subtract(
                        left,
                        Cardinality.inclusiveRange(10, 12)
                )
        );
    }

    @Test
    public void subtraction_multiInterval_eventually_empty() {
        Cardinality result = Cardinality.inclusiveRange(1, 10);

        result = Cardinalities.subtract(result, Cardinality.of(3));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.of(7));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.inclusiveRange(1, 2));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.inclusiveRange(4, 6));
        Assertions.assertNotNull(result);
        result = Cardinalities.subtract(result, Cardinality.inclusiveRange(8, 10));

        assertNull(result);
    }

    @Test
    public void subtraction_disjoint_left() {
        // X = [1,3], Y = [5,7]
        assertEquals(range(1, 3), Cardinalities.subtract(range(1, 3), range(5, 7)));
    }

    @Test
    public void subtraction_disjoint_right() {
        // X = [5,7], Y = [1,3]
        assertEquals(range(5, 7), Cardinalities.subtract(range(5, 7), range(1, 3)));
    }

    @Test
    public void subtraction_y_inside_x() {
        // X = [1,10], Y = [4,7]
        assertEquals(
                Cardinalities.union(range(1, 3), range(8, 10)),
                Cardinalities.subtract(range(1, 10), range(4, 7))
        );
    }

    @Test
    public void subtraction_y_inside_x_touching_left() {
        // X = [1,10], Y = [1,4]
        assertEquals(range(5, 10), Cardinalities.subtract(range(1, 10), range(1, 4)));
    }

    @Test
    public void subtraction_y_inside_x_touching_right() {
        // X = [1,10], Y = [7,10]
        assertEquals(range(1, 6), Cardinalities.subtract(range(1, 10), range(7, 10)));
    }

    @Test
    public void subtraction_x_inside_y() {
        // X = [4,7], Y = [1,10]
        assertNull(Cardinalities.subtract(range(4, 7), range(1, 10)));
    }

    @Test
    public void subtraction_overlap_left() {
        // X = [1,7], Y = [5,10]
        assertEquals(range(1, 4), Cardinalities.subtract(range(1, 7), range(5, 10)));
    }

    @Test
    public void subtraction_overlap_right() {
        // X = [5,10], Y = [1,7]
        assertEquals(range(8, 10), Cardinalities.subtract(range(5, 10), range(1, 7)));
    }

    @Test
    public void subtraction_same_range() {
        assertNull(Cardinalities.subtract(range(1, 10), range(1, 10)));
    }

}
