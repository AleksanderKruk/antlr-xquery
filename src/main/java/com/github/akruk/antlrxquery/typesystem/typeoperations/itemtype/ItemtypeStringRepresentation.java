package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;

import java.util.Map;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;

public class ItemtypeStringRepresentation extends ItemtypeUnaryOperation<String> {

    public String string(XQueryItemType itemtype) {
        return automaton[itemtype.typeOrdinal].apply(itemtype);
    }


    @Override
    public String errorOperation(XQueryItemType x) {
        return "error()";
    }

    @Override
    public String anyItemOperation(XQueryItemType x) {
        return "item()";
    }

    @Override
    public String anyNodeOperation(XQueryItemType x) {
        return "node()";
    }

    @Override
    public String elementOperation(XQueryItemType x) {
        return "element(" + String.join(" | ", x.elementNames.stream().map(QualifiedName::toString).toList()) + ")";
    }

    @Override
    public String anyMapOperation(XQueryItemType x) {
        return "map(*)";
    }

    @Override
    public String mapOperation(XQueryItemType x) {
        return "map(" + x.mapKeyType + ", " + x.mapValueType + ")";
    }

    @Override
    public String anyArrayOperation(XQueryItemType x) {
        return "array(*)";
    }

    @Override
    public String arrayOperation(XQueryItemType x) {
        return "array(" + x.arrayMemberType + ")";
    }

    @Override
    public String anyFunctionOperation(XQueryItemType x) {
        return "function(*)";
    }

    @Override
    public String functionOperation(XQueryItemType x) {
        StringBuilder sb = new StringBuilder();
        var returnedType = x.returnedType;
        var argumentType = x.argumentTypes;
        sb.append("fn(");
        for (int i = 0; i < argumentType.size(); i++) {
                sb.append(argumentType.get(i));
                if (i < argumentType.size() - 1) {
                        sb.append(", ");
                }
        }
        sb.append(") as ");
        sb.append(returnedType);
        return sb.toString();
    }

    @Override
    public String enumOperation(XQueryItemType x) {
        final XQueryItemType enums = (XQueryItemType) x;
        return "enum(" + enums.enumMembers.stream()
                .map(s -> "'" + s + "'")
                .reduce((a, b) -> a + ", " + b)
                .orElse("") + ")";
    }

    @Override
    public String stringOperation(XQueryItemType x) {
        return "string";
    }

    @Override
    public String numberOperation(XQueryItemType x) {
        return "number";
    }

    @Override
    public String choiceOperation(XQueryItemType x) {
        return String.join(" | ", x.itemTypes.stream().map(Object::toString).toArray(String[]::new));
    }

    @Override
    public String recordOperation(XQueryItemType x) {
        StringBuilder sb = new StringBuilder();
        sb.append("record(");
        boolean first = true;
        for (Map.Entry<String, XQueryRecordField> entry : x.recordFields.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey());
            if (!entry.getValue().isRequired()) {
                sb.append("?");
            }
            sb.append(" as ").append(entry.getValue());
            first = false;
            }
            sb.append(")");
            return sb.toString();
    }

    @Override
    public String extensibleRecordOperation(XQueryItemType x) {
        StringBuilder sb = new StringBuilder("record(");
        boolean first = true;
        for (Map.Entry<String, XQueryRecordField> entry : x.recordFields.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey())
              .append(" as ")
              .append(entry.getValue());
            first = false;
        }
        sb.append(", *)");
        return sb.toString();
    }

    @Override
    public String booleanOperation(XQueryItemType x) {
        return "boolean";
    }
}
