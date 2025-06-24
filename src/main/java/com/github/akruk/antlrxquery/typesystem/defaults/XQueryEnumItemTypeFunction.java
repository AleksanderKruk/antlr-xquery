package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.List;

import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeFunction extends XQueryEnumItemType {

  public XQueryEnumItemTypeFunction(XQuerySequenceType returnedType, List<XQuerySequenceType> argumentType, XQueryTypeFactory factory) {
    super(XQueryTypes.FUNCTION, argumentType, returnedType, null, null, null, null, factory);
  }

}
