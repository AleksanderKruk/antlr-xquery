package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.List;

import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeFunction extends XQueryEnumItemType {

  public XQueryEnumItemTypeFunction(XQuerySequenceType returnedType, List<XQuerySequenceType> argumentType, XQueryTypeFactory factory) {
    super(XQueryTypes.FUNCTION, argumentType, returnedType, null, null, null, null, factory, null);
  }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        var returnedType = getReturnedType();
        var argumentType = getArgumentTypes();
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

}
