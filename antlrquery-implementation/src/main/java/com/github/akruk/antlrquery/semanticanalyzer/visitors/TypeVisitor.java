package com.github.akruk.antlrquery.semanticanalyzer.visitors;

import com.github.akruk.antlrquery.AntlrQueryParserBaseVisitor;
import com.github.akruk.antlrquery.AntlrQueryParser.EmptySequenceTypeContext;
import com.github.akruk.antlrquery.AntlrQueryParser.NonEmptySequenceTypeContext;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class TypeVisitor
    extends AntlrQueryParserBaseVisitor<AntlrQuerySequenceType>
{


    private final AntlrQueryTypeFactory typeFactory;
    private final CardinalityVisitor cardinalityVisitor;
    private final ItemTypeVisitor itemTypeVisitor;

    public TypeVisitor(
        AntlrQueryTypeFactory factory, 
        CardinalityVisitor cardinalityVisitor,
        ItemTypeVisitor itemTypeVisitor
    ) 
    {
        this.typeFactory = factory;
        this.cardinalityVisitor = cardinalityVisitor;
        this.itemTypeVisitor = itemTypeVisitor;
    }


    @Override
    public AntlrQuerySequenceType visitEmptySequenceType(EmptySequenceTypeContext ctx) {
        return typeFactory.emptySequence();
    }

    @Override
    public AntlrQuerySequenceType visitNonEmptySequenceType(NonEmptySequenceTypeContext ctx) {
        final AntlrQueryItemType it = ctx.itemType().accept(itemTypeVisitor);
        final Cardinality c = ctx.cardinality().accept(cardinalityVisitor);
        return typeFactory.sequence(it, c);
    }

    // @Override
    // public AntlrQuerySequenceType visitAnyItem(AnyItemContext ctx) {
    //     return typeFactory.anyItem();
        
    // }


    // @Override
    // public TypeInContext visitChoiceItemType(final ChoiceItemTypeContext ctx)
    // {
    //     final List<ItemTypeContext> itemTypes = ctx.itemType();
    //     if (itemTypes.size() == 1) {
    //         return ctx.itemType(0).accept(this);
    //     }
    //     final var choiceItemNames = itemTypes.stream().map(i -> i.getText()).collect(Collectors.toSet());
    //     if (choiceItemNames.size() != itemTypes.size()) {
    //         error(ctx, ErrorType.CHOICE_ITEM_TYPE__DUPLICATED, List.of());
    //     }
    //     final List<XQueryItemType> choiceItems = itemTypes.stream().map(i -> i.accept(this))
    //         .map(sequenceType -> sequenceType.type.itemType)
    //         .toList();
    //     return symbolManager.typeInContext(typeFactory.choice(choiceItems));
    // }

    // @Override
    // public TypeInContext visitTypeName(final TypeNameContext ctx)
    // {
    //     final var name = ctx.getText();
    //     final AntlrQuerySequenceType result = switch (name) {
    //         case "number" -> number;
    //         case "string" -> string;
    //         case "boolean" -> boolean_;
    //         default -> {
    //             final var visitedQualifiedName = namespaceResolver.resolveType(name);
    //             final var type = typeFactory.namedType(visitedQualifiedName);
    //             if (type.status() == NamedAccessingStatus.OK)
    //                 yield type.type();

    //             for (final QualifiedName resolvedName : recordsMapped.keySet()) {
    //                 if (resolvedName.equals(visitedQualifiedName)) {
    //                     final var namedRecordResult = resolveRecord(resolvedName, recordsMapped.get(resolvedName));
    //                     yield typeFactory.one(namedRecordResult.recordItemType);
    //                 }
    //             }
    //             for (final var resolved : itemsMapped.keySet()) {
    //                 if (resolved.equals(visitedQualifiedName)) {
    //                     final var t = resolveItemTypeFromDecl(resolved, itemsMapped.get(resolved));
    //                     yield typeFactory.one(t.registered());
    //                 }
    //             }

    //             error(ctx, ErrorType.TYPE_NAME__UNKNOWN, List.of(name));
    //             yield zeroOrMoreItems;
    //         }
    //     };
    //     return symbolManager.typeInContext(result);
    // }

    // @Override
    // public TypeInContext visitAnyKindType(final AnyKindTypeContext ctx)
    // {
    //     return symbolManager.typeInContext(typeFactory.anyNode());
    // }

    // @Override
    // public TypeInContext visitElementType(final ElementTypeContext ctx)
    // {
    //     final Set<QualifiedName> elementNames = ctx.nameTypeUnion().nameTest().stream().map(e -> namespaceResolver.resolveElement(e.getText()))
    //         .collect(Collectors.toSet());
    //     return symbolManager.typeInContext(typeFactory.element(elementNames));
    // }

    // @Override
    // public TypeInContext visitFunctionType(final FunctionTypeContext ctx)
    // {
    //     if (ctx.anyFunctionType() != null) {
    //         return symbolManager.typeInContext(typeFactory.anyFunction());
    //     }
    //     final var func = ctx.typedFunctionType();
    //     final List<AntlrQuerySequenceType> parameterTypes = func.typedFunctionParam().stream()
    //         .map(p -> visitSequenceType(p.sequenceType()).type)
    //         .collect(Collectors.toList());
    //     final var function =  typeFactory.function(visitSequenceType(func.sequenceType()).type, parameterTypes);
    //     return symbolManager.typeInContext(function);
    // }

    // @Override
    // public TypeInContext visitMapType(final MapTypeContext ctx)
    // {
    //     if (ctx.anyMapType() != null) {
    //         return symbolManager.typeInContext(typeFactory.anyMap());
    //     }
    //     final var map = ctx.typedMapType();
    //     final XQueryItemType keyType = map.itemType().accept(this).type.itemType;
    //     final TypeInContext valueType = visitSequenceType(map.sequenceType());
    //     return symbolManager.typeInContext(typeFactory.map(keyType, valueType.type));
    // }

    // @Override
    // public TypeInContext visitArrayType(final ArrayTypeContext ctx)
    // {
    //     if (ctx.anyArrayType() != null) {
    //         return symbolManager.typeInContext(typeFactory.anyArray());
    //     }
    //     final var array = ctx.typedArrayType();
    //     final var sequenceType = visitSequenceType(array.sequenceType());
    //     return symbolManager.typeInContext(typeFactory.array(sequenceType.type));
    // }

    // @Override
    // public TypeInContext visitRecordType(final RecordTypeContext ctx)
    // {
    //     if (ctx.anyRecordType() != null) {
    //         return symbolManager.typeInContext(typeFactory.anyMap());
    //     }
    //     final var record = ctx.typedRecordType();
    //     final var fieldDeclarations = record.fieldDeclaration();
    //     final Map<String, XQueryRecordField> fields = new HashMap<>(fieldDeclarations.size());
    //     for (final var field : fieldDeclarations) {
    //         final String fieldName = field.fieldName().getText();
    //         final var fieldType = visitSequenceType(field.sequenceType());
    //         final boolean isRequired = field.QUESTION_MARK() != null;
    //         final XQueryRecordField recordField = new XQueryRecordField(TypeOrReference.type(fieldType.type), isRequired);
    //         fields.put(fieldName, recordField);
    //     }
    //     if (record.extensibleFlag() == null) {
    //         return symbolManager.typeInContext(typeFactory.extensibleRecord(fields));
    //     }
    //     return symbolManager.typeInContext(typeFactory.record(fields));
    // }

    // @Override
    // public TypeInContext visitEnumerationType(final EnumerationTypeContext ctx)
    // {
    //     final Set<String> members = ctx.STRING().stream()
    //         .map(TerminalNode::getText)
    //         .map(s->s.substring(1, s.cardinality()-1))
    //         .collect(Collectors.toSet());
    //     return symbolManager.typeInContext(typeFactory.enum_(members));
    // }




    // AntlrQuerySequenceType getAnyArrayOrMapLookupType(
    //     final LookupContext ctx,
    //     final boolean isWildcard,
    //     final TypeInContext targetType,
    //     final TypeInContext keySpecifierType)
    // {
    //     if (isWildcard) {
    //         return null;
    //     }
    //     final XQueryItemType targetItemType = targetType.type.itemType;
    //     final Collection<XQueryItemType> choiceItemTypes = targetItemType.itemTypes;
    //     XQueryItemType targetKeyItemType = null;
    //     AntlrQuerySequenceType resultingType = null;
    //     for (final var itemType : choiceItemTypes) {
    //         if (resultingType == null) {
    //             if (!isWildcard)
    //                 resultingType = switch(keySpecifierType.type.cardinality) {
    //                     case ONE -> typeFactory.zeroOrOne(itemType);
    //                     default -> typeFactory.zeroOrMore(itemType);
    //                 };
    //             else {
    //                 resultingType = typeFactory.zeroOrMore(itemType);
    //             }
    //             continue;
    //         }

    //         switch (itemType.type) {
    //             case ARRAY -> {
    //                 resultingType = resultingType.alternativeMerge(itemType.arrayMemberType);
    //                 targetKeyItemType = targetItemType.alternativeMerge(typeFactory.itemNumber());
    //             }
    //             case MAP -> {
    //                 resultingType = resultingType.alternativeMerge(itemType.mapValueType);
    //                 targetKeyItemType = targetItemType.alternativeMerge(itemType.mapKeyType);
    //             }
    //             default -> {
    //                 resultingType = zeroOrMoreItems;
    //                 targetKeyItemType = typeFactory.itemAnyItem();
    //             }
    //         }
    //     }
    //     resultingType = resultingType.addOptionality();
    //     if (isWildcard) {
    //         return resultingType;
    //     }
    //     final XQueryItemType numberOrKey = targetKeyItemType.alternativeMerge(typeFactory.itemNumber());

    //     final AntlrQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(numberOrKey);
    //     if (!keySpecifierType.itemtypeIsSubtypeOf(expectedKeyItemtype)) {
    //         error(ctx, ErrorType.LOOKUP__ARRAY_OR_MAP_INVALID_KEY, List.of(targetType, expectedKeyItemtype));
    //     }
    //     return resultingType;
    // }

    
    // private  AntlrQuerySequenceType getMapLookuptype(
    //         final ParserRuleContext target,
    //         final LookupContext lookup,
    //         final KeySpecifierContext keySpecifier,
    //         final TypeInContext targetType,
    //         final TypeInContext keySpecifierType,
    //         final boolean isWildcard)
    // {
    //     final XQueryItemType targetKeyItemType = targetType.type.itemType.mapKeyType;
    //     final AntlrQuerySequenceType targetValueType = targetType.type.itemType.mapValueType;
    //     final XQueryItemType targetValueItemtype = targetValueType.itemType;
    //     if (isWildcard) {
    //         return typeFactory.zeroOrMore(targetValueItemtype);
    //     }
    //     final AntlrQuerySequenceType result = switch(keySpecifierType.type.cardinality) {
    //             case ONE -> typeFactory.zeroOrOne(targetValueItemtype);
    //             default -> typeFactory.zeroOrMore(targetValueItemtype);
    //         };
    //     final AntlrQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
    //     if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
    //         error(lookup, ErrorType.LOOKUP__MAP_INVALID_KEY, List.of(targetType, expectedKeyItemtype));
    //     }
    //     if (targetValueItemtype.type == XQueryTypes.RECORD) {
    //         return result;
    //     }
    //     return result.addOptionality();
    // }

    // private AntlrQuerySequenceType getRecordLookupType(
    //     final ParserRuleContext target,
    //     final LookupContext lookup,
    //     final KeySpecifierContext keySpecifier,
    //     final TypeInContext targetType,
    //     final TypeInContext keySpecifierType,
    //     final boolean isWildcard)
    // {
    //     final XQueryItemType targetKeyItemType = typeFactory.itemString();
    //     final Map<String, XQueryRecordField> recordFields = targetType.type.itemType.recordFields;
    //     if (recordFields.isEmpty()) {
    //         warn(target, WarningType.LOOKUP__EMPTY_RECORD, List.of());
    //         return emptySequence;
    //     }
    //     final AntlrQuerySequenceType mergedRecordFieldTypes = recordFields
    //         .values()
    //         .stream()
    //         .map(this::resolveRecordFieldType)
    //         .reduce((x, y)->x.alternativeMerge(y))
    //         .get();
    //     if (isWildcard) {
    //         return mergedRecordFieldTypes;
    //     }
    //     if (!keySpecifierType.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemString()))) {
    //         error(keySpecifier, ErrorType.LOOKUP__INVALID_RECORD_KEY_TYPE, List.of(targetType, keySpecifierType));
    //         return zeroOrMoreItems;
    //     }
    //     final var string = keySpecifier.STRING();
    //     if (string != null) {
    //         final String key = processStringLiteral(keySpecifier);
    //         final var valueType = recordFields.get(key);
    //         if (valueType == null) {
    //             error(keySpecifier, ErrorType.LOOKUP__INVALID_RECORD_KEY_NAME, List.of(key, targetType));
    //             return zeroOrMoreItems;
    //         }
    //         return resolveRecordFieldType(valueType);
    //     }
    //     final AntlrQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
    //     if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
    //         error(lookup, ErrorType.LOOKUP__INVALID_RECORD_KEY_TYPE, List.of(targetType, expectedKeyItemtype));
    //     }
    //     if (keySpecifierType.type.itemType.type == XQueryTypes.ENUM) {
    //         final var members = keySpecifierType.type.itemType.members;
    //         final var firstField = members.stream().findFirst().get();
    //         final var firstRecordField = recordFields.get(firstField);
    //         AntlrQuerySequenceType merged = resolveRecordFieldType(firstRecordField);
    //         for (final var member : members) {
    //             if (member.equals(firstField))
    //                 continue;
    //             final var recordField = recordFields.get(member);
    //             if (recordField == null) {
    //                 warn(lookup, WarningType.LOOKUP__IMPOSSIBLE_RECORD_FIELD, List.of(member));
    //                 return zeroOrMoreItems;
    //             }
    //             merged = merged.sequenceMerge(resolveRecordFieldType(recordField));
    //         }
    //         return merged;
    //     }
    //     return mergedRecordFieldTypes.addOptionality();
    // }

    // private AntlrQuerySequenceType getExtensibleRecordLookupType(
    //     final ParserRuleContext ctx,
    //     final LookupContext lookup,
    //     final KeySpecifierContext keySpecifier,
    //     final TypeInContext targetType,
    //     final TypeInContext keySpecifierType,
    //     final boolean isWildcard)
    // {
    //     final XQueryItemType targetKeyItemType = typeFactory.itemString();
    //     final Map<String, XQueryRecordField> recordFields = targetType.type.itemType.recordFields;
    //     if (recordFields.isEmpty()) {
    //         warn(ctx, WarningType.LOOKUP__RETURNS_ALWAYS_EMPTY, List.of());
    //         return emptySequence;
    //     }
    //     final AntlrQuerySequenceType mergedRecordFieldTypes = recordFields
    //         .values()
    //         .stream()
    //         .map(this::resolveRecordFieldType)
    //         .reduce((x, y)->x.alternativeMerge(y))
    //         .get();
    //     if (isWildcard) {
    //         return mergedRecordFieldTypes;
    //     }
    //     if (!keySpecifierType.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemString()))) {
    //         error(ctx, ErrorType.LOOKUP__INVALID_EXTENDED_RECORD_KEY_TYPE, List.of());
    //         return zeroOrMoreItems;
    //     }
    //     final var stringToken = keySpecifier.STRING();
    //     if (stringToken != null) {
    //         final String key = processStringLiteral(keySpecifier);
    //         final var recordField = recordFields.get(key);
    //         if (recordField == null) {
    //             return zeroOrMoreItems;
    //         }
    //         return resolveRecordFieldType(recordField);
    //     }
    //     final AntlrQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
    //     if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
    //         error(lookup, ErrorType.LOOKUP__INVALID_EXTENDED_RECORD_KEY_TYPE, List.of(targetType, expectedKeyItemtype));
    //     }
    //     if (keySpecifierType.type.itemType.type == XQueryTypes.ENUM) {
    //         final var members = keySpecifierType.type.itemType.members;
    //         final var firstField = members.stream().findFirst().get();
    //         final var firstRecordField = recordFields.get(firstField);
    //         AntlrQuerySequenceType merged = resolveRecordFieldType(firstRecordField);
    //         for (final var member : members) {
    //             if (member.equals(firstField))
    //                 continue;
    //             final var recordField = recordFields.get(member);
    //             if (recordField == null)  {
    //                 return zeroOrMoreItems;
    //             }
    //             merged = merged.alternativeMerge(resolveRecordFieldType(recordField));
    //         }
    //         return merged;
    //     }
    //     return mergedRecordFieldTypes.addOptionality();
    // }

	// private AntlrQuerySequenceType resolveRecordFieldType(final XQueryRecordField t) {
	// 	final var type = switch(t.typeOrReference().fieldType()) {
	// 	    case REFERENCE -> {
	// 	        yield typeFactory.namedType(t.typeOrReference().reference()).type();
	// 	    }
	// 	    case TYPE -> {
	// 	        yield t.typeOrReference().type();
	// 	    }
	// 	};
	// 	return t.isRequired()? type : type.addOptionality();
	// }

}
