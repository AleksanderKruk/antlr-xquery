package com.github.akruk.antlrquery.typesystem.typeoperations.cardinality;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.NumericRange.Event;
import com.github.akruk.antlrquery.typesystem.types.NumericRange.Type;
import com.github.akruk.antlrquery.typesystem.types.NumericRange.BoundValue;
import com.github.akruk.antlrquery.typesystem.types.NumericRange.FiniteBound;
import com.github.akruk.antlrquery.typesystem.types.NumericRange.NegativeInfinity;
import com.github.akruk.antlrquery.typesystem.types.NumericRange.PositiveInfinity;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class Ranges {

    private Ranges() {}

    private static final Comparator<Event> CMP =
            Comparator.comparing(Event::value)
                    .thenComparing(e -> e.type() == Type.START ? 0 : 1)
                    .thenComparing(e -> e.value().inclusive() ? 0 : 1);

    private static List<Event> merge(NumericRange... ranges) {
        List<Event> out = new ArrayList<>();
        for (NumericRange r : ranges) Collections.addAll(out, r.events());
        out.sort(CMP);
        return out;
    }

    public static NumericRange union(NumericRange... ranges) {
        List<Event> ev = merge(ranges);
        if (ev.isEmpty()) return NumericRange.ZERO;

        List<Event> out = new ArrayList<>();
        int active = 0;
        BoundValue start = null;

        for (Event e : ev) {
            if (e.type() == Type.START) {
                if (active == 0) start = e.value();
                active++;
            } else {
                active--;
                if (active == 0) {
                    out.add(new Event(start, Type.START));
                    out.add(new Event(e.value(), Type.END));
                }
            }
        }
        return NumericRange.of(out.toArray(Event[]::new));
    }

    public static @Nullable NumericRange intersection(NumericRange... ranges) {
        if (ranges.length == 1) return ranges[0];

        List<Event> ev = merge(ranges);
        if (ev.isEmpty()) return null;

        long need = ranges.length;
        int active = 0;
        BoundValue start = null;
        List<Event> out = new ArrayList<>();

        for (Event e : ev) {
            if (e.type() == Type.START) {
                active++;
                if (active == need) start = e.value();
            } else {
                if (active == need) {
                    out.add(new Event(start, Type.START));
                    out.add(new Event(e.value(), Type.END));
                }
                active--;
            }
        }
        return out.isEmpty() ? null : NumericRange.of(out.toArray(Event[]::new));
    }

    public static @Nullable NumericRange subtract(NumericRange left, NumericRange... right) {
        NumericRange rhs = union(right);

        Event[] a = left.events();
        Event[] b = rhs.events();

        if (a.length == 0) return null;
        if (b.length == 0) return left;

        List<Event> out = new ArrayList<>();

        int ia = 0;
        int ib = 0;

        int leftActive = 0;
        int rightActive = 0;

        @Nullable BoundValue segmentStart = null;

        while (ia < a.length || ib < b.length) {

            BoundValue value;
            if (ia == a.length) {
                value = b[ib].value();
            } else if (ib == b.length) {
                value = a[ia].value();
            } else {
                int cmp = a[ia].value().compareTo(b[ib].value());
                value = cmp <= 0 ? a[ia].value() : b[ib].value();
            }

            int leftStarts = 0;
            int leftEnds = 0;
            int leftInclusiveStarts = 0;
            int leftInclusiveEnds = 0;

            int rightStarts = 0;
            int rightEnds = 0;
            int rightInclusiveStarts = 0;
            int rightInclusiveEnds = 0;

            while (ia < a.length && a[ia].value().compareTo(value) == 0) {
                Event e = a[ia++];
                if (e.type() == Type.START) {
                    leftStarts++;
                    if (e.value().inclusive()) leftInclusiveStarts++;
                } else {
                    leftEnds++;
                    if (e.value().inclusive()) leftInclusiveEnds++;
                }
            }

            while (ib < b.length && b[ib].value().compareTo(value) == 0) {
                Event e = b[ib++];
                if (e.type() == Type.START) {
                    rightStarts++;
                    if (e.value().inclusive()) rightInclusiveStarts++;
                } else {
                    rightEnds++;
                    if (e.value().inclusive()) rightInclusiveEnds++;
                }
            }

            boolean leftBefore = leftActive > 0;
            boolean rightBefore = rightActive > 0;

            int leftAtCount =
                    leftActive
                            - leftEnds
                            + leftInclusiveEnds
                            + leftInclusiveStarts;

            int rightAtCount =
                    rightActive
                            - rightEnds
                            + rightInclusiveEnds
                            + rightInclusiveStarts;

            boolean leftAt = leftAtCount > 0;
            boolean rightAt = rightAtCount > 0;

            leftActive = leftActive - leftEnds + leftStarts;
            rightActive = rightActive - rightEnds + rightStarts;

            boolean leftAfter = leftActive > 0;
            boolean rightAfter = rightActive > 0;

            boolean resultBefore = leftBefore && !rightBefore;
            boolean resultAt = leftAt && !rightAt;
            boolean resultAfter = leftAfter && !rightAfter;

            if (!resultBefore && resultAfter) {
                segmentStart = adjustInclusive(value, resultAt);
                continue;
            }

            if (resultBefore && !resultAfter) {
                assert segmentStart != null;
                out.add(new Event(segmentStart, Type.START));
                out.add(new Event(adjustInclusive(value, resultAt), Type.END));
                segmentStart = null;
                continue;
            }

            if (resultBefore && resultAfter && !resultAt) {
                assert segmentStart != null;
                out.add(new Event(segmentStart, Type.START));
                out.add(new Event(adjustInclusive(value, false), Type.END));
                segmentStart = adjustInclusive(value, false);
                continue;
            }

            if (!resultBefore && resultAt && !resultAfter) {
                BoundValue p = adjustInclusive(value, true);
                out.add(new Event(p, Type.START));
                out.add(new Event(p, Type.END));
            }
        }

        if (segmentStart != null) {
            out.add(new Event(segmentStart, Type.START));
            out.add(new Event(BoundValue.POSITIVE_INFINITY, Type.END));
        }

        return out.isEmpty() ? null : NumericRange.skipNormalization(out.toArray(Event[]::new));
    }

    private static BoundValue adjustInclusive(BoundValue v, boolean inc) {
        if (v instanceof FiniteBound fb) {
            return new FiniteBound(fb.value(), inc);
        }
        return v;
    }

    public static boolean isSubSet(NumericRange a, NumericRange b) {
        return subtract(a, b) == null;
    }

    public static boolean overlaps(NumericRange a, NumericRange b) {
        NumericRange i = intersection(a, b);
        return i != null && i.events().length > 0;
    }

    public static NumericRange sequenceMerge(NumericRange a, NumericRange b) {
        Event[] A = a.events();
        Event[] B = b.events();
        if (A.length == 0 || B.length == 0) return NumericRange.ZERO;

        List<Event> out = new ArrayList<>();

        for (int i = 0; i < A.length; i += 2) {
            BoundValue aL = A[i].value();
            BoundValue aR = A[i + 1].value();

            for (int j = 0; j < B.length; j += 2) {
                BoundValue bL = B[j].value();
                BoundValue bR = B[j + 1].value();

                BoundValue L = add(aL, bL);
                BoundValue R = add(aR, bR);

                boolean incL = aL.inclusive() && bL.inclusive();
                boolean incR = aR.inclusive() && bR.inclusive();

                out.add(new Event(L, Type.START));
                out.add(new Event(R, Type.END));
            }
        }
        return NumericRange.of(out.toArray(Event[]::new));
    }

    private static BoundValue add(BoundValue a, BoundValue b) {
        if (a instanceof FiniteBound(BigDecimal value1, boolean inclusive1)) {
            if (b instanceof FiniteBound(BigDecimal value, boolean inclusive))
                return new FiniteBound(value1.add(value), inclusive1 && inclusive);
            return b instanceof PositiveInfinity ? BoundValue.POSITIVE_INFINITY : BoundValue.NEGATIVE_INFINITY;
        }
        return a instanceof PositiveInfinity ? BoundValue.POSITIVE_INFINITY : BoundValue.NEGATIVE_INFINITY;
    }

    public static boolean contains(NumericRange r, BigDecimal x) {
        Event[] ev = r.events();
        int active = 0;

        for (Event e : ev) {
            int cmp = compare(x, e.value());
            if (e.type() == Type.START) {
                if (cmp < 0) return false;
                if (cmp == 0 && e.value().inclusive()) return true;
                active++;
            } else {
                if (cmp < 0) return active > 0;
                if (cmp == 0 && e.value().inclusive() && active > 0) return true;
                active--;
            }
        }
        return false;
    }

    private static int compare(BigDecimal v, BoundValue b) {
        if (b instanceof FiniteBound fb) return v.compareTo(fb.value());
        return b instanceof NegativeInfinity ? 1 : -1;
    }

    public static String stringify(NumericRange r) {
        List<NumericRange.Interval> iv = r.toIntervals();
        if (iv.size() == 1) return interval(iv.getFirst());

        StringJoiner sj = new StringJoiner(" | ", "(", ")");
        for (var i : iv) sj.add(interval(i));
        return sj.toString();
    }

    private static String interval(NumericRange.Interval i) {
        String L = switch (i.lowerBound()) {
            case FiniteBound f -> f.value().toPlainString();
            case NegativeInfinity _ -> "-∞";
            case PositiveInfinity _ -> throw new IllegalStateException();
        };
        String R = switch (i.upperBound()) {
            case FiniteBound f -> f.value().toPlainString();
            case PositiveInfinity _ -> "∞";
            case NegativeInfinity _ -> throw new IllegalStateException();
        };
        return L.equals(R) ? L : L + ".." + R;
    }

    public static NumericRange integers(NumericRange r) {
        if (r.isZero()) return NumericRange.ZERO;

        List<Event> out = new ArrayList<>();
        Event[] ev = r.events();

        for (int i = 0; i < ev.length; i += 2) {
            BoundValue L = ev[i].value();
            BoundValue R = ev[i + 1].value();

            BigDecimal lo = L instanceof FiniteBound fb ? fb.value() : BigDecimal.ZERO;
            @Nullable BigDecimal hi = R instanceof FiniteBound fb ? fb.value() : null;

            BigDecimal start = lo.setScale(0, RoundingMode.CEILING);
            if (!L.inclusive() && start.compareTo(lo) == 0) start = start.add(BigDecimal.ONE);
            if (start.signum() < 0) start = BigDecimal.ZERO;

            if (hi == null) {
                out.add(new Event(new FiniteBound(start, true), Type.START));
                out.add(new Event(BoundValue.POSITIVE_INFINITY, Type.END));
                continue;
            }

            BigDecimal end = hi.setScale(0, RoundingMode.FLOOR);
            if (!R.inclusive() && end.compareTo(hi) == 0) end = end.subtract(BigDecimal.ONE);

            if (start.compareTo(end) > 0) continue;

            for (BigDecimal x = start; x.compareTo(end) <= 0; x = x.add(BigDecimal.ONE)) {
                FiniteBound fb = new FiniteBound(x, true);
                out.add(new Event(fb, Type.START));
                out.add(new Event(fb, Type.END));
            }
        }

        return NumericRange.of(out.toArray(Event[]::new));
    }


    public static NumericRange integers(int start, int stop) {
        if (start >= stop) {
            return NumericRange.ZERO;
        }

        NumericRange.Interval[] intervals = new NumericRange.Interval[stop - start];
        for (int i = start; i < stop; i++) {
            BigDecimal val = BigDecimal.valueOf(i);
            FiniteBound b = new FiniteBound(val, true);
            intervals[i - start] = new NumericRange.Interval(b, b);
        }

        return NumericRange.of(intervals);
    }

    public static @Nullable Event min(NumericRange r) {
        Event[] ev = r.events();
        return ev.length == 0 ? null : ev[0];
    }

    public static @Nullable Event max(NumericRange r) {
        Event[] ev = r.events();
        return ev.length == 0 ? null : ev[ev.length - 1];
    }
}
