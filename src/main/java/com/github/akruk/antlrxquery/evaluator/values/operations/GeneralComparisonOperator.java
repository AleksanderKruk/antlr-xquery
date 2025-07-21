package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.util.List;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class GeneralComparisonOperator {

    private final XQueryValueFactory valueFactory;
    private final ValueAtomizer atomizer;
    private final ValueComparisonOperator valueComparisonOperator;

    public GeneralComparisonOperator(final XQueryValueFactory valueFactory, ValueAtomizer atomizer, ValueComparisonOperator valueComparisonOperator)
    {
        this.valueFactory = valueFactory;
        this.atomizer = atomizer;
        this.valueComparisonOperator = valueComparisonOperator;
    }

    public XQueryValue generalEquals(final XQueryValue o1, final XQueryValue o2)
    {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueEquals(element1, element2);
                if (comparison.isError)
                    return comparison;
                if (comparison.booleanValue) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }

    public XQueryValue generalUnequals(final XQueryValue o1, final XQueryValue o2) {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueUnequal(element1, element2);
                if (comparison.isError)
                    return comparison;
                if (comparison.booleanValue) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }



    public XQueryValue generalLessThan(final XQueryValue o1, final XQueryValue o2) {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueLessThan(element1, element2);
                if (comparison.isError)
                    return comparison;
                if (comparison.booleanValue) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }


    public XQueryValue generalGreaterThan(final XQueryValue o1, final XQueryValue o2) {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueGreaterThan(element1, element2);
                if (comparison.isError)
                    return comparison;
                if (comparison.booleanValue) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }


    public XQueryValue generalLessEqual(final XQueryValue o1, final XQueryValue o2) {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueLessEqual(element1, element2);
                if (comparison.isError)
                    return comparison;
                if (comparison.booleanValue) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }


    public XQueryValue generalGreaterEqual(final XQueryValue o1, final XQueryValue o2) {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueGreaterEqual(element1, element2);
                if (comparison.isError)
                    return comparison;
                if (comparison.booleanValue) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }



}




    // public XQueryValue generalEqual(final XQueryValue other) {
	// }


    // public XQueryValue generalUnequal(final XQueryValue other) {
    //     final var thisAtomized = atomize();
    //     final var otherAtomized = other.atomize();
    //     for (final var thisElement : thisAtomized) {
    //         for (final var otherElement : otherAtomized) {
    //             if (thisElement.valueUnequal(otherElement).effectiveBooleanValue()) {
    //                 return valueFactory.bool(true);
    //             }
    //         }
    //     }
    //     return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	// }


    // public XQueryValue generalLessThan(final XQueryValue other) {
    //     final var thisAtomized = atomize();
    //     final var otherAtomized = other.atomize();
    //     for (final var thisElement : thisAtomized) {
    //         for (final var otherElement : otherAtomized) {
    //             if (thisElement.valueLessThan(otherElement).effectiveBooleanValue()) {
    //                 return valueFactory.bool(true);
    //             }
    //         }
    //     }
    //     return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);

	// }


    // public XQueryValue generalLessEqual(final XQueryValue other) {
    //     final var thisAtomized = atomize();
    //     final var otherAtomized = other.atomize();
    //     for (final var thisElement : thisAtomized) {
    //         for (final var otherElement : otherAtomized) {
    //             if (thisElement.valueLessEqual(otherElement).effectiveBooleanValue()) {
    //                 return valueFactory.bool(true);
    //             }
    //         }
    //     }
    //     return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);

	// }


    // public XQueryValue generalGreaterThan(final XQueryValue other) {
    //     final var thisAtomized = atomize();
    //     final var otherAtomized = other.atomize();
    //     for (final var thisElement : thisAtomized) {
    //         for (final var otherElement : otherAtomized) {
    //             if (thisElement.valueGreaterThan(otherElement).effectiveBooleanValue()) {
    //                 return valueFactory.bool(true);
    //             }
    //         }
    //     }
    //     return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	// }


    // public XQueryValue generalGreaterEqual(final XQueryValue other) {
    //     final var thisAtomized = atomize();
    //     final var otherAtomized = other.atomize();
    //     for (final var thisElement : thisAtomized) {
    //         for (final var otherElement : otherAtomized) {
    //             if (thisElement.valueGreaterEqual(otherElement).effectiveBooleanValue()) {
    //                 return valueFactory.bool(true);
    //             }
    //         }
    //     }
    //     return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	// }
