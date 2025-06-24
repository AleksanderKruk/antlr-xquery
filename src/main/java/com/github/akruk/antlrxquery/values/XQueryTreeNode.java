package com.github.akruk.antlrxquery.values;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryTreeNode extends XQueryValueBase<ParseTree> {
    public XQueryTreeNode(ParseTree node, XQueryValueFactory valueFactory) {
        super(node, valueFactory);
    }

    @Override
    public XQueryValue copy() {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<");
        sb.append(super.toString());
        sb.append(":");
        sb.append(value.getClass().getSimpleName());
        sb.append(":");
        sb.append(value.getText());
        sb.append("/>");
        return sb.toString();
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
    public XQueryValue valueEqual(XQueryValue other) {
        return valueFactory.bool(this == other);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue data() throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }

    @Override
    public XQueryValue empty() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

}
