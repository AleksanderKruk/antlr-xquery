package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Set;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeEnum extends XQueryItemType {

  final Set<String> enumMembers;
  public Set<String> getEnumMembers() {
    return enumMembers;
  }
  public XQueryItemTypeEnum (Set<String> enumMembers, XQueryTypeFactory factory) {
    super(XQueryTypes.ENUM, null, null, null, null, null, null, factory, null, null);
    this.enumMembers = enumMembers;
  }


}
