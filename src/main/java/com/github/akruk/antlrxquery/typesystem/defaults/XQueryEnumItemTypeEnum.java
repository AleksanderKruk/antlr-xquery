package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Set;

public class XQueryEnumItemTypeEnum extends XQueryEnumItemType {

  final Set<String> enumMembers;
  public Set<String> getEnumMembers() {
    return enumMembers;
  }
  public XQueryEnumItemTypeEnum (Set<String> enumMembers) {
    super(XQueryTypes.ENUM, null, null, null, null, null, null);
    this.enumMembers = enumMembers;
  }

}
