package com.github.akruk.antlrxquery.typesystem.factories;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.types.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;

public interface AntlrQueryTypeFactory {
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
    public XQueryItemType itemMap(XQueryItemType keyType, AntlrQuerySequenceType valueType);
    public XQueryItemType itemArray(AntlrQuerySequenceType itemType);
    public XQueryItemType itemFunction(AntlrQuerySequenceType returnType, List<AntlrQuerySequenceType> argumentTypes);
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

    public AntlrQuerySequenceType error();
    public AntlrQuerySequenceType string();
    public AntlrQuerySequenceType enum_(Set<String> memberNames);
    public AntlrQuerySequenceType number();
    public AntlrQuerySequenceType anyNode();
    public AntlrQuerySequenceType anyArray();
    public AntlrQuerySequenceType array(AntlrQuerySequenceType itemType);
    public AntlrQuerySequenceType anyMap();
    public AntlrQuerySequenceType map(XQueryItemType mapKeyType, AntlrQuerySequenceType mapValueType);
    public AntlrQuerySequenceType record(Map<String, XQueryRecordField> fields);
    public AntlrQuerySequenceType extensibleRecord(Map<String, XQueryRecordField> fields);
    public AntlrQuerySequenceType element(Set<QualifiedName> elementName);
    public AntlrQuerySequenceType anyFunction();
    public AntlrQuerySequenceType function(AntlrQuerySequenceType returnType, List<AntlrQuerySequenceType> argumentTypes);
    public AntlrQuerySequenceType choice(Collection<XQueryItemType> items);
    public AntlrQuerySequenceType anyItem();
    public AntlrQuerySequenceType boolean_();
    public AntlrQuerySequenceType emptySequence();

    public record NamedAccessingResult(AntlrQuerySequenceType type, NamedAccessingStatus status) {}
    public NamedAccessingResult namedType(QualifiedName name);

    public AntlrQuerySequenceType one(XQueryItemType itemType);
    public AntlrQuerySequenceType zeroOrOne(XQueryItemType itemType);
    public AntlrQuerySequenceType zeroOrMore(XQueryItemType itemType);
    public AntlrQuerySequenceType oneOrMore(XQueryItemType itemType);
    public AntlrQuerySequenceType sequence(XQueryItemType itemType, Cardinality cardinality);
}
