package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Set;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeElement extends XQueryEnumItemType {

  public XQueryEnumItemTypeElement(Set<String> elementName, XQueryTypeFactory factory) {
    super(XQueryTypes.ELEMENT, null, null, null, null, null, elementName, factory, null);
  }

  @Override
  public String toString() {
    return "element(" + String.join(" | ", getElementNames()) + ")";
  }

}
