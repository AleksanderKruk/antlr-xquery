package com.github.akruk.antlrquery.semanticanalyzer.visitors;

import com.github.akruk.antlrquery.AntlrQueryParser;
import com.github.akruk.antlrquery.AntlrQueryParserBaseVisitor;
import com.github.akruk.antlrquery.AntlrQueryParser.*;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


/**
 * CardinalityVisitor visits AntlrQuery parse tree to determine the cardinality of type
 */
@DefaultQualifier(NonNull.class)
public class ItemTypeVisitor 
    extends AntlrQueryParserBaseVisitor<AntlrQueryItemType>
{
    private TypeVisitor typeVisitor;
    private CardinalityVisitor cardinalityVisitor;
    private NumericRangeVisitor numericRangeVisitor;
    private AntlrQueryTypeFactory typeFactory;
    public ItemTypeVisitor(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }



    @Override
    public AntlrQueryItemType visitAnyItem(AnyItemContext ctx) {
        return typeFactory.itemAnyItem();
    }

    @Override
    public AntlrQueryItemType visitConstrainedString(ConstrainedStringContext ctx) {
        var cardinality = cardinalityVisitor.visitCardinality(ctx.cardinality());
        return typeFactory.itemString(cardinality);
    }

    @Override
    public AntlrQueryItemType visitAnyString(AnyStringContext ctx) {
        return typeFactory.itemString();
    }

    @Override
    public AntlrQueryItemType visitAnyNumber(AnyNumberContext ctx) {
        return typeFactory.itemNumber();
    }

    @Override
    public AntlrQueryItemType visitNumericSet(NumericSetContext ctx) {
        return typeFactory.itemNumber(ctx.numericRange().accept(numericRangeVisitor));
    }

    @Override
    public AntlrQueryItemType visitAnyBooleanType(AnyBooleanTypeContext ctx) {
        return typeFactory.itemBoolean();
    }

    @Override
    public AntlrQueryItemType visitTrueType(TrueTypeContext ctx) {
        return typeFactory.itemTrue();
    }

    @Override
    public AntlrQueryItemType visitFalseType(FalseTypeContext ctx) {
        return typeFactory.itemFalse();
    }

    @Override
    public AntlrQueryItemType visitAnyFunctionType(AnyFunctionTypeContext ctx) {
        return typeFactory.itemAnyFunction();
    }

    @Override
    public AntlrQueryItemType visitTypedFunctionType(TypedFunctionTypeContext ctx) {
        final List<AntlrQuerySequenceType> parameterTypes = ctx.typedFunctionParam().stream()
                .map(p -> p.type().accept(typeVisitor))
                .collect(Collectors.toList());
        return typeFactory.itemFunction(ctx.type().accept(typeVisitor), parameterTypes);
    }

    @Override
    public AntlrQueryItemType visitAnyMapType(AnyMapTypeContext ctx) {
        return typeFactory.itemAnyMap();
    }

    @Override
    public AntlrQueryItemType visitTypedMapType(TypedMapTypeContext ctx) {
        final AntlrQueryItemType keyType = visitItemType(ctx.itemType());
        final AntlrQuerySequenceType valueType = typeVisitor.visitType(ctx.type());
        return typeFactory.itemMap(keyType, valueType);
    }

    @Override
    public AntlrQueryItemType visitAnyKeyMapType(AnyKeyMapTypeContext ctx) {
        final AntlrQueryItemType keyType = typeFactory.itemAnyItem();
        final AntlrQuerySequenceType valueType = typeVisitor.visitType(ctx.type());
        return typeFactory.itemMap(keyType, valueType);
    }

    @Override
    public AntlrQueryItemType visitAnyValueMapType(AnyValueMapTypeContext ctx) {
        final AntlrQueryItemType keyType = visitItemType(ctx.itemType());
        final AntlrQuerySequenceType valueType = typeFactory.any();
        return typeFactory.itemMap(keyType, valueType);
    }


    @Override
    public AntlrQueryItemType visitExplicitArrayTypeNoCardinality(ExplicitArrayTypeNoCardinalityContext ctx) {
        final var sequenceType = typeVisitor.visitType(ctx.type());
        return typeFactory.itemArray(sequenceType, Cardinality.ZERO_OR_MORE);
    }

    @Override
    public AntlrQueryItemType visitExplicitArrayTypeWithCardinality(ExplicitArrayTypeWithCardinalityContext ctx) {
        final var sequenceType = typeVisitor.visitType(ctx.type());
        final var cardinality = cardinalityVisitor.visitCardinality(ctx.cardinality());
        return typeFactory.itemArray(sequenceType, cardinality);
    }


    @Override
    public AntlrQueryItemType  visitChoiceItemType(final AntlrQueryParser.ChoiceItemTypeContext ctx)
    {
        final List<AntlrQueryParser.ItemTypeContext> itemTypes = ctx.itemType();
        if (itemTypes.size() == 1) {
            return visitItemType(ctx.itemType().getFirst());
        }
        final var choiceItemNames = itemTypes.stream().map(RuleContext::getText).collect(Collectors.toSet());
//        if (choiceItemNames.size() != itemTypes.size()) {
//            error(ctx, ErrorType.CHOICE_ITEM_TYPE__DUPLICATED, List.of());
//        }
        final List<AntlrQueryItemType> choiceItems = itemTypes.stream().map(i -> i.accept(this)).toList();
        return typeFactory.itemChoice(choiceItems.toArray(AntlrQueryItemType[]::new));
    }

    @Override
    public AntlrQueryItemType visitTupleType(TupleTypeContext ctx) {
        var types = ctx.type().stream().map(typeVisitor::visitType).toList();
        return typeFactory.itemTuple(types);
    }

    @Override
    public AntlrQueryItemType visitEnumerationType(final AntlrQueryParser.EnumerationTypeContext ctx)
    {
        final Set<String> enumMembers = ctx.STRING().stream()
                .map(TerminalNode::getText)
                .map(s->s.substring(1, s.length()-1))
                .collect(Collectors.toSet());
        return typeFactory.itemEnum(enumMembers);
    }

     @Override
     public AntlrQueryItemType visitFunctionType(final FunctionTypeContext ctx)
     {
         if (ctx.anyFunctionType() != null) {
             return typeFactory.itemAnyFunction();
         }
         final var func = ctx.typedFunctionType();
         final List<AntlrQuerySequenceType> parameterTypes = func.typedFunctionParam()
                 .stream()
                 .map(TypedFunctionParamContext::type)
                 .map(typeVisitor::visitType)
                 .toList();

         final AntlrQuerySequenceType returnType = typeVisitor.visitType(func.type());
         return typeFactory.itemFunction(returnType, parameterTypes);
     }

    @Override
    public AntlrQueryItemType visitAnyRuleType(AnyRuleTypeContext ctx) {
        return typeFactory.itemAnyRule();
    }

    @Override
    public AntlrQueryItemType visitAnyTokenType(AnyTokenTypeContext ctx) {
        return typeFactory.itemAnyToken();
    }

    @Override
    public AntlrQueryItemType visitAnyNodeType(AnyNodeTypeContext ctx) {
        return typeFactory.itemAnyNode();
    }

    @Override
    public AntlrQueryItemType visitAnyTokenTypeFromGrammar(AnyTokenTypeFromGrammarContext ctx) {
        return typeFactory.itemAnyTokenFromGrammar(ctx.namespace().getText());
    }

    @Override
    public AntlrQueryItemType visitAnyRuleTypeFromGrammar(AnyRuleTypeFromGrammarContext ctx) {
        return typeFactory.itemAnyRuleFromGrammar(ctx.namespace().getText());
    }

    @Override
    public AntlrQueryItemType visitAnyNodeTypeFromGrammar(AnyNodeTypeFromGrammarContext ctx) {
        return typeFactory.itemAnyNodeFromGrammar(ctx.namespace().getText());
    }

    NamespaceResolver resolver;
    @Override
    public AntlrQueryItemType visitQnameEnumeratedNodeType(QnameEnumeratedNodeTypeContext ctx) {
        return getItemChoiceTypeFromTreeElements(ctx.qname(), typeFactory::itemNodesFromGrammar);
    }

    @Override
    public AntlrQueryItemType visitQnameEnumeratedTokenType(QnameEnumeratedTokenTypeContext ctx) {
        return getItemChoiceTypeFromTreeElements(ctx.qname(), typeFactory::itemTokensFromGrammar);
    }

    @Override
    public AntlrQueryItemType visitQnameEnumeratedRuleType(QnameEnumeratedRuleTypeContext ctx) {
        return getItemChoiceTypeFromTreeElements(ctx.qname(), typeFactory::itemRulesFromGrammar);
    }

    private @NonNull AntlrQueryItemType getItemChoiceTypeFromTreeElements(
            List<QnameContext> ctx,
            BiFunction<String, Set<NamespaceResolver.QualifiedName>, AntlrQueryItemType> treeElementFactory
    )
    {
        var elementsMappedToGrammar = ctx.stream()
                .map(Objects::toString)
                .map(resolver::resolveElement)
                .collect(Collectors.groupingBy(NamespaceResolver.QualifiedName::namespace, Collectors.toSet()))
                ;
        AntlrQueryItemType[] combinedTypes = new AntlrQueryItemType[elementsMappedToGrammar.size()];
        int i = 0;
        for (var grammar : elementsMappedToGrammar.keySet()) {
            combinedTypes[i++] = treeElementFactory.apply(grammar, elementsMappedToGrammar.get(grammar));
        }

        return typeFactory.itemChoice(combinedTypes);
    }


     @Override
     public AntlrQueryItemType visitRecordType(final RecordTypeContext ctx)
     {
         final var record = ctx.constrainedRecordType();
         final var fieldDeclarations = record.fieldDeclaration();
         final LinkedHashMap<String, RecordField> fields = new LinkedHashMap<>(fieldDeclarations.size());
         for (final var field : fieldDeclarations) {
             final String fieldName = field.fieldName().getText();
             final var fieldType = typeVisitor.visitType(field.type());
             final boolean isRequired = field.QUESTION_MARK() != null;
             final RecordField recordField = new RecordField(
                     fieldName,
                     new RecordField.TypeOrReference.Type(fieldType),
                     isRequired);
             fields.put(fieldName, recordField);
         }
         if (record.extensibleType() == null) {
             var additionalFieldType = typeVisitor.visitExtensibleType(record.extensibleType());
             return typeFactory.itemExtensibleRecord(fields, additionalFieldType);
         }
         return typeFactory.itemRecord(fields);
     }

    @Override
    public AntlrQueryItemType visitRegexType(RegexTypeContext ctx) {
        return typeFactory.itemRegex();
    }


    @Override
    public AntlrQueryItemType visitGrammarReference(GrammarReferenceContext ctx) {
        return typeFactory.itemGrammarReference(ctx.grammarName().getText());
    }

    @Override
    public AntlrQueryItemType visitSingleRuleReference(SingleRuleReferenceContext ctx) {
        var qname = resolver.resolveElement(ctx.qname().getText());
        return typeFactory.itemRuleReference(qname.namespace(), Set.of(qname));
    }

    @Override
    public AntlrQueryItemType visitEnumeratedRuleReference(EnumeratedRuleReferenceContext ctx) {
        var grammarsToElements = ctx.qname().stream()
                .map(RuleContext::getText)
                .map(resolver::resolveElement)
                .collect(Collectors.groupingBy(NamespaceResolver.QualifiedName::namespace, Collectors.toUnmodifiableSet()));

        AntlrQueryItemType[] combinedTypes = new AntlrQueryItemType[grammarsToElements.size()];
        int i = 0;
        for (var grammar : grammarsToElements.keySet()) {
            combinedTypes[i++] = typeFactory.itemRuleReference(grammar, grammarsToElements.get(grammar));
        }

        return typeFactory.itemChoice(combinedTypes);
    }

    @Override
    public AntlrQueryItemType visitAllRulesFromGrammarReference(AllRulesFromGrammarReferenceContext ctx) {
        return typeFactory.itemAllRuleReferencesFromGrammar(ctx.grammarName().getText());
    }

    @Override
    public AntlrQueryItemType visitEnumeratedRulesFromGrammarReference(EnumeratedRulesFromGrammarReferenceContext ctx) {
        var p = ctx.anyName().stream().map(Objects::toString).map(resolver::resolveElement).collect(Collectors.toUnmodifiableSet());
        return typeFactory.itemRuleReferencesFromGrammar(ctx.grammarName().getText(), p);
    }


     @Override
     public AntlrQueryItemType visitTypeName(final TypeNameContext ctx)
     {
         final var name = ctx.getText();
         final var visitedQualifiedName = resolver.resolveType(name);
         return typeFactory.guaranteedItemNamedType(visitedQualifiedName, new IllegalStateException());
     }

    @Override
    public AntlrQueryItemType visitFromOperatorArrayType(FromOperatorArrayTypeContext ctx) {
        var sq = typeVisitor.visitTypePrimitive(ctx.typePrimitive());
        ArrayList<Cardinality> arrayCardinalities = new ArrayList<>(ctx.arrayOperator().size());
        for (var arrayOperator : ctx.arrayOperator()) {
            if (arrayOperator.anyArrayOperator() != null) {
                arrayCardinalities.add(Cardinality.ZERO_OR_MORE);
            } else {
                arrayCardinalities.add(cardinalityVisitor.visitCardinality(arrayOperator.constrainedArrayOperator().cardinality()));
            }
        }

        @MonotonicNonNull AntlrQueryItemType itemType = typeFactory.itemArray(sq, arrayCardinalities.getFirst());
        if (arrayCardinalities.size() == 1) {
            return itemType;
        }
        for (var c : arrayCardinalities.subList(1, arrayCardinalities.size())) {
            itemType = typeFactory.itemArray(typeFactory.one(itemType), c);
        }
        return itemType;
    }
}
