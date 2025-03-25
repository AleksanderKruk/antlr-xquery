package com.github.akruk.antlrxquery.values;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQuerySequence extends XQueryValueBase<List<XQueryValue>> {

    public static final XQuerySequence EMPTY = new XQuerySequence();


    @Override
    public List<XQueryValue> sequence() {
        return value;
    }

    public XQuerySequence(List<XQueryValue> list) {
        value = list;
    }

    public XQuerySequence(XQueryValue... values) {
        value = Arrays.asList(values);
    }


    public XQuerySequence() {
        value = List.of();
    }

    @Override
    public Boolean effectiveBooleanValue() {
        return value.isEmpty();
    }

    @Override
    public List<XQueryValue> atomize() {
        List<XQueryValue> result = new ArrayList<>();
        for (XQueryValue element : value) {
            if (element.isAtomic()) {
                result.add(element);
                continue;
            }
            // If the result is not atomic we atomize it
            // and extend the result list
            var atomizedValues = element.atomize();
            result.addAll(atomizedValues);
        }
        return result;
    }


    @Override
    public XQueryValue valueEqual(XQueryValueFactory valueFactory, XQueryValue other) {
        return XQueryBoolean.FALSE;
    }

    @Override
    public XQueryValue valueLessThan(XQueryValueFactory valueFactory, XQueryValue other) {
        return XQueryBoolean.FALSE;
    }

    @Override
    public XQueryValue union(XQueryValueFactory valueFactory, XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        var newSequence = new ArrayList<XQueryValue>();
        newSequence.addAll(value);
        newSequence.addAll(otherSequence.sequence());
        return valueFactory.sequence(newSequence);
    }

    @Override
    public XQueryValue intersect(XQueryValueFactory valueFactory, XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        var newSequence = new ArrayList<XQueryValue>();
        var otherSequenceValue = otherSequence.sequence();
        for (var element : value) {
            for (var otherElement : otherSequenceValue) {
                if (element.valueEqual(valueFactory, otherElement).booleanValue()) {
                    newSequence.add(element);
                }
            }
        }
        return valueFactory.sequence(newSequence);
    }


    @Override
    public XQueryValue except(XQueryValueFactory valueFactory, XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        var newSequence = new ArrayList<XQueryValue>();
        var otherSequenceValue = otherSequence.sequence();
        NEXT_ELEMENT:
        for (var element : value) {
            for (var otherElement : otherSequenceValue) {
                if (element.valueEqual(valueFactory, otherElement).booleanValue()) {
                    continue NEXT_ELEMENT;
                }
            }
            newSequence.add(element);
        }
        return valueFactory.sequence(newSequence);
    }



    @Override
    public XQueryValue copy(XQueryValueFactory valueFactory) {
        return valueFactory.sequence(List.copyOf(value));
    }


    @Override
    public XQueryValue empty(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        return XQueryBoolean.of(value.isEmpty());
    }


    @Override
    public XQueryValue head(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQuerySequence.EMPTY;
        return value.get(0);
    }


    @Override
    public XQueryValue tail(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQuerySequence.EMPTY;
        return valueFactory.sequence(value.subList(1, value.size()));
    }

    @Override
    public XQueryValue insertBefore(
            XQueryValueFactory valueFactory,
            XQueryValue position,
            XQueryValue inserted) throws XQueryUnsupportedOperation
    {
        var newSequence = new ArrayList<XQueryValue>(value.size());
        newSequence.addAll(value);
        if (!position.isNumericValue())
            throw new XQueryUnsupportedOperation();
        int positionIndex = position.numericValue().intValue();
        if (positionIndex > value.size()) {
            newSequence.addAll(inserted.atomize());
            return valueFactory.sequence(newSequence);
        }
        if (positionIndex <= 0) {
            newSequence.addAll(0, inserted.atomize());
            return valueFactory.sequence(newSequence);
        }
        newSequence.addAll(positionIndex - 1, inserted.atomize());
        return valueFactory.sequence(newSequence);
    }

    @Override
    public XQueryValue remove(XQueryValueFactory valueFactory, XQueryValue position) throws XQueryUnsupportedOperation
    {
        var newSequence = new ArrayList<XQueryValue>(value.size());
        newSequence.addAll(value);
        if (!position.isNumericValue())
            throw new XQueryUnsupportedOperation();
        int positionIndex = position.numericValue().intValue();
        if (positionIndex > value.size()) {
            return valueFactory.sequence(newSequence);
        }
        if (positionIndex <= 0) {
            return valueFactory.sequence(newSequence);
        }
        newSequence.remove(positionIndex-1);
        return valueFactory.sequence(newSequence);
    }


    @Override
    public XQueryValue reverse(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation
    {
        var newSequence = List.copyOf(value);
        return valueFactory.sequence(newSequence.reversed());
    }



    @Override
    public XQueryValue subsequence(XQueryValueFactory valueFactory, int startingLoc) throws XQueryUnsupportedOperation {
        return subsequence(valueFactory, startingLoc, value.size()-startingLoc+1);
    }

    @Override
    public XQueryValue subsequence(XQueryValueFactory valueFactory, int startingLoc, int length) throws XQueryUnsupportedOperation {
        int currentLength = value.size();
        if (startingLoc > currentLength) {
            return XQuerySequence.EMPTY;
        }
        int startIndexIncluded = Math.max(startingLoc - 1, 0);
        int endIndexExcluded = Math.min(startingLoc + length - 1, currentLength);
        var newSequence = value.subList(startIndexIncluded, endIndexExcluded);
        return valueFactory.sequence(newSequence);
    }

    @Override
    public XQueryValue distinctValues(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        int currentLength = value.size();
        if (currentLength == 0) {
            return EMPTY;
        }
        var newSequence = new ArrayList<XQueryValue>(value.size());
        for (var element : value) {
            var exists = newSequence.stream().filter(
                    v -> v == element || v.valueEqual(valueFactory, element).booleanValue()).findFirst().isPresent();
            if (!exists) {
                newSequence.add(element);
            }
        }
        return valueFactory.sequence(newSequence);
    }

    @Override
    public XQueryValue zeroOrOne(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        return switch (value.size()) {
            case 0, 1 -> this;
            default -> null;
        };
    }

    @Override
    public XQueryValue oneOrMore(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        return switch (value.size()) {
            case 0 -> null;
            default -> this;
        };
    }

    @Override
    public XQueryValue exactlyOne(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        return switch (value.size()) {
            case 1 -> this;
            default -> null;
        };
    }

    @Override
    public XQueryValue data(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }
}
