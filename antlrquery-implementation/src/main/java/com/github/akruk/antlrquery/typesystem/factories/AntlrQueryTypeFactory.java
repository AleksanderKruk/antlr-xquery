package com.github.akruk.antlrquery.typesystem.factories;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.factories.defaults.MemoizedTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface AntlrQueryTypeFactory {
    AntlrQueryItemType itemError();
    AntlrQueryItemType itemString();
    AntlrQueryItemType itemEnum(Set<String> memberNames);
    AntlrQueryItemType itemNumber();
    AntlrQueryItemType itemAnyNode();
    AntlrQueryItemType itemAnyArray();
    AntlrQueryItemType itemAnyMap();
    AntlrQueryItemType itemElement(String grammar, Set<QualifiedName> elementName);
    AntlrQueryItemType itemAnyFunction();
    AntlrQueryItemType itemAnyItem();
    AntlrQueryItemType itemBoolean();
    AntlrQueryItemType itemMap(AntlrQueryItemType keyType, AntlrQuerySequenceType valueType);
    AntlrQueryItemType itemFunction(AntlrQuerySequenceType returnType, List<AntlrQuerySequenceType> argumentTypes);
    AntlrQueryItemType itemRecord(LinkedHashMap<String, RecordField> fields);
    AntlrQueryItemType itemExtensibleRecord(LinkedHashMap<String, RecordField> fields, AntlrQuerySequenceType additionalFieldType);
    AntlrQueryItemType itemChoice(AntlrQueryItemType... items);
    AntlrQueryItemType itemNothing();

    NamedItemAccessingResult itemNamedType(QualifiedName name);
    AntlrQueryItemType guaranteedItemNamedType(QualifiedName name, Exception ifNoMatch);

    AntlrQueryItemType itemNumber(NumericRange numericRange);

    AntlrQueryItemType itemString(Cardinality union);

    AntlrQueryItemType itemToken(String grammar, Set<QualifiedName> mergedNames);

    AntlrQueryItemType itemRule(String grammar, Set<QualifiedName> mergedNames);

    AntlrQueryItemType itemFalse();

    AntlrQueryItemType itemTrue();

    AntlrQuerySequenceType neverType();

    AntlrQueryItemType itemArray(AntlrQuerySequenceType itemType, Cardinality c);

    AntlrQueryItemType itemTuple(List<AntlrQuerySequenceType> mergedElements);

    Set<QualifiedName> grammarTokens(String grammar);
    Set<QualifiedName> grammarNodes(String grammar);
    Set<QualifiedName> grammarRules(String grammar);

    enum RegistrationStatus {
        OK, ALREADY_REGISTERED_SAME, ALREADY_REGISTERED_DIFFERENT
    }
    record RegistrationResult(AntlrQueryItemType registered, RegistrationStatus status){}
    RegistrationResult registerNamedType(QualifiedName name, AntlrQueryItemType itemType);

    AntlrQuerySequenceType error();
    AntlrQuerySequenceType string();
    AntlrQuerySequenceType enum_(Set<String> memberNames);

    AntlrQuerySequenceType number();

    AntlrQuerySequenceType number(NumericRange union);
    AntlrQuerySequenceType anyNode();
    AntlrQuerySequenceType anyArray();
    AntlrQuerySequenceType anyMap();
    AntlrQuerySequenceType array(AntlrQuerySequenceType itemType, Cardinality c);
    AntlrQuerySequenceType map(AntlrQueryItemType mapKeyType, AntlrQuerySequenceType mapValueType);
    AntlrQuerySequenceType record(LinkedHashMap<String, RecordField> fields);
    AntlrQuerySequenceType extensibleRecord(LinkedHashMap<String, RecordField> fields);
    AntlrQuerySequenceType anyFunction();
    AntlrQuerySequenceType element(String grammar, Set<QualifiedName> elementName);
    AntlrQuerySequenceType function(AntlrQuerySequenceType returnType, List<AntlrQuerySequenceType> argumentTypes);
    AntlrQuerySequenceType choice(AntlrQueryItemType... items);
    AntlrQuerySequenceType anyItem();
    AntlrQuerySequenceType boolean_();

    MemoizedTypeFactory.GrammarRegistrationResult registerGrammars(String name, Set<QualifiedName> elements);

    AntlrQuerySequenceType emptySequence();


    sealed interface NamedItemAccessingResult {
        record Success(AntlrQueryItemType type) implements NamedItemAccessingResult {}
        record UnknownNamespace() implements NamedItemAccessingResult {}
        record UnknownName() implements NamedItemAccessingResult {}
    }

    AntlrQuerySequenceType one(AntlrQueryItemType itemType);
    AntlrQuerySequenceType zeroOrOne(AntlrQueryItemType itemType);
    AntlrQuerySequenceType zeroOrMore(AntlrQueryItemType itemType);
    AntlrQuerySequenceType oneOrMore(AntlrQueryItemType itemType);
    AntlrQuerySequenceType sequence(AntlrQueryItemType itemType, Cardinality cardinality);
}
