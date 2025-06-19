package com.github.akruk.antlrxquery.values;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryTreeNode extends XQueryValueBase<ParseTree> {
    public XQueryTreeNode(ParseTree node) {
        value = node;
    }

    @Override
    public XQueryValue copy(XQueryValueFactory valueFactory) {
        return valueFactory.node(value);
    }

    @Override
    public ParseTree node() {
        return value;
    }

    @Override
    public String stringValue() {
        return value.getText();
    }

    // private record LineEndCharPosEnd(int lineEnd, int charPosEnd) {}

    // static private LineEndCharPosEnd getLineEndCharPosEnd(Token end) {
    //     final var string = end.getText();
    //     final int length = string.length();

    //     int newlineCount = 0;
    //     int lastNewlineIndex = 0;
    //     for (int i = 0; i < length; i++) {
    //         if (string.codePointAt(i) == '\n') {
    //             newlineCount++;
    //             lastNewlineIndex = i;
    //         }
    //     }

    //     final int lineEnd = end.getLine() + newlineCount;
    //     final int charPositionInLineEnd = newlineCount == 0 ?
    //             end.getCharPositionInLine() + length : length - lastNewlineIndex;
    //     return new LineEndCharPosEnd(lineEnd, charPositionInLineEnd);
    // }

    // @Override
    // public String toString() {
    //     final Token start = value.getStart();
    //     final Token stop = value.getStop();
    //     final int line = start.getLine();
    //     final int charPositionInLine = start.getCharPositionInLine();
    //     final LineEndCharPosEnd lineEndCharPosEnd = getLineEndCharPosEnd(stop);
    //     final int lineEnd = lineEndCharPosEnd.lineEnd();
    //     final int charPositionInLineEnd = lineEndCharPosEnd.charPosEnd();
    //     return String.format("[line:%s, column:%s] %s [/line:%s, column:%s]",
    //                 line, charPositionInLine,
    //                 message,
    //                 lineEnd, charPositionInLineEnd));
    // }

    @Override
    public XQueryValue valueEqual(XQueryValueFactory factoryValue, XQueryValue other) {
        return factoryValue.bool(this == other);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValueFactory factoryValue, XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue data(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return factoryValue.sequence(atomized);
    }

    @Override
    public XQueryValue empty(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

}
