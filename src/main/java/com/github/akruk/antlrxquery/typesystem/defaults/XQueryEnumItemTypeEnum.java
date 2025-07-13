package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Set;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeEnum extends XQueryEnumItemType {

  final Set<String> enumMembers;
  public Set<String> getEnumMembers() {
    return enumMembers;
  }
  public XQueryEnumItemTypeEnum (Set<String> enumMembers, XQueryTypeFactory factory) {
    super(XQueryTypes.ENUM, null, null, null, null, null, null, factory, null);
    this.enumMembers = enumMembers;
  }

@Override
public String toString() {
        return "enum(" + enumMembers.stream()
                .map(s -> "'" + s + "'")
                .reduce((a, b) -> a + ", " + b)
                .orElse("") + ")";
}

}
