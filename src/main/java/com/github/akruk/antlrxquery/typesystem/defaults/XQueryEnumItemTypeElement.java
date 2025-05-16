package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Set;

public class XQueryEnumItemTypeElement extends XQueryEnumItemType {

  public XQueryEnumItemTypeElement(Set<String> elementName) {
    super(XQueryTypes.ELEMENT, null, null, null, null, null, elementName);
  }

}
