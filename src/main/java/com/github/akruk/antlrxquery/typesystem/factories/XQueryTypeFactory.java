package com.github.akruk.antlrxquery.typesystem.factories;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

public interface XQueryTypeFactory {
    public XQueryItemType itemError();
    public XQueryItemType itemString();
    public XQueryItemType itemEnum(Set<String> memberNames);
    public XQueryItemType itemNumber();
    public XQueryItemType itemAnyNode();
    public XQueryItemType itemAnyArray();
    public XQueryItemType itemAnyMap();
    public XQueryItemType itemElement(Set<QualifiedName> elementName);
    public XQueryItemType itemAnyFunction();
    public XQueryItemType itemAnyItem();
    public XQueryItemType itemBoolean();
    public XQueryItemType itemMap(XQueryItemType keyType, XQuerySequenceType valueType);
    public XQueryItemType itemArray(XQuerySequenceType itemType);
    public XQueryItemType itemFunction(XQuerySequenceType returnType, List<XQuerySequenceType> argumentTypes);
    public XQueryItemType itemRecord(Map<String, XQueryRecordField> fields);
    public XQueryItemType itemExtensibleRecord(Map<String, XQueryRecordField> fields);
    public XQueryItemType itemChoice(Collection<XQueryItemType> items);

    public enum NamedAccessingStatus {
        OK, UNKNOWN_NAMESPACE, UNKNOWN_NAME
    }
    public record NamedItemAccessingResult(XQueryItemType type, NamedAccessingStatus status) {}
    public NamedItemAccessingResult itemNamedType(QualifiedName name);

    public enum RegistrationStatus {
        OK, ALREADY_REGISTERED_SAME, ALREADY_REGISTERED_DIFFERENT
    }
    record RegistrationResult(XQueryItemType registered, RegistrationStatus status){}
    public RegistrationResult registerNamedType(QualifiedName name, XQueryItemType itemType);

    public XQuerySequenceType error();
    public XQuerySequenceType string();
    public XQuerySequenceType enum_(Set<String> memberNames);
    public XQuerySequenceType number();
    public XQuerySequenceType anyNode();
    public XQuerySequenceType anyArray();
    public XQuerySequenceType array(XQuerySequenceType itemType);
    public XQuerySequenceType anyMap();
    public XQuerySequenceType map(XQueryItemType mapKeyType, XQuerySequenceType mapValueType);
    public XQuerySequenceType record(Map<String, XQueryRecordField> fields);
    public XQuerySequenceType extensibleRecord(Map<String, XQueryRecordField> fields);
    public XQuerySequenceType element(Set<QualifiedName> elementName);
    public XQuerySequenceType anyFunction();
    public XQuerySequenceType function(XQuerySequenceType returnType, List<XQuerySequenceType> argumentTypes);
    public XQuerySequenceType choice(Collection<XQueryItemType> items);
    public XQuerySequenceType anyItem();
    public XQuerySequenceType boolean_();
    public XQuerySequenceType emptySequence();

    public record NamedAccessingResult(XQuerySequenceType type, NamedAccessingStatus status) {}
    public NamedAccessingResult namedType(QualifiedName name);

    public XQuerySequenceType one(XQueryItemType itemType);
    public XQuerySequenceType zeroOrOne(XQueryItemType itemType);
    public XQuerySequenceType zeroOrMore(XQueryItemType itemType);
    public XQuerySequenceType oneOrMore(XQueryItemType itemType);
}
