package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.List;

import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQueryEnumItemTypeFunction extends XQueryEnumItemType {

  public XQueryEnumItemTypeFunction(XQuerySequenceType returnedType, List<XQuerySequenceType> argumentType) {
    super(XQueryTypes.FUNCTION, argumentType, returnedType, null, null, null, null);
  }

}
