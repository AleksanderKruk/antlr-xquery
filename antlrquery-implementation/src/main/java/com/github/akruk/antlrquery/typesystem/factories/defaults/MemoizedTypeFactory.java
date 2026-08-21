package com.github.akruk.antlrquery.typesystem.factories.defaults;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@DefaultQualifier(NonNull.class)
public class MemoizedTypeFactory implements AntlrQueryTypeFactory {

    private static final AntlrQueryItemType ITEM_NOTHING = new NothingType();
    private static final AntlrQueryItemType ITEM_ERROR = new NothingType();
    private static final AntlrQueryItemType ITEM_STRING = new StringType.StringNonEnum(Cardinality.ZERO_OR_MORE);
    private static final AntlrQueryItemType ITEM_NUMBER = new NumberType(NumericRange.FULL);
    private static final AntlrQueryItemType ITEM_BOOLEAN = new BooleanType.Boolean();
    private static final AntlrQueryItemType ITEM_TRUE = new BooleanType.True();
    private static final AntlrQueryItemType ITEM_FALSE = new BooleanType.False();
    private static final AntlrQueryItemType ITEM_ANY_ITEM = new AnyItemType();

    private static final AntlrQuerySequenceType EMPTY_SEQUENCE = new AntlrQuerySequenceType.EmptySequence();
    private static final AntlrQuerySequenceType NEVER_TYPE = new AntlrQuerySequenceType.NonEmptySequence(AntlrQueryItemType.NEVER, Cardinality.ONE);

    private static final AntlrQuerySequenceType SEQ_ERROR = new AntlrQuerySequenceType.NonEmptySequence(ITEM_ERROR, Cardinality.ONE);
    private static final AntlrQuerySequenceType SEQ_STRING = new AntlrQuerySequenceType.NonEmptySequence(ITEM_STRING, Cardinality.ONE);
    private static final AntlrQuerySequenceType SEQ_NUMBER = new AntlrQuerySequenceType.NonEmptySequence(ITEM_NUMBER, Cardinality.ONE);
    private static final AntlrQuerySequenceType SEQ_BOOLEAN = new AntlrQuerySequenceType.NonEmptySequence(ITEM_BOOLEAN, Cardinality.ONE);
    private static final AntlrQuerySequenceType SEQ_ANY_ITEM = new AntlrQuerySequenceType.NonEmptySequence(ITEM_ANY_ITEM, Cardinality.ONE);

    private record TreeNodeKey(String grammar, Set<NamespaceResolver.QualifiedName> names) {}

    private final Map<Set<String>, AntlrQueryItemType> enumCache = new ConcurrentHashMap<>();
    private final Map<NumericRange, AntlrQueryItemType> numberRangeCache = new ConcurrentHashMap<>();
    private final Map<Cardinality, AntlrQueryItemType> stringLengthCache = new ConcurrentHashMap<>();
    private final Map<TreeNodeKey, AntlrQueryItemType> elementCache = new ConcurrentHashMap<>();
    private final Map<TreeNodeKey, AntlrQueryItemType> tokenCache = new ConcurrentHashMap<>();
    private final Map<TreeNodeKey, AntlrQueryItemType> ruleCache = new ConcurrentHashMap<>();
    private final Map<Set<ConcreteItemType>, AntlrQueryItemType> choiceCache = new ConcurrentHashMap<>();

    private final Map<String, Map<String, AntlrQueryItemType>> namedTypes;
    private final Map<String, Set<NamespaceResolver.QualifiedName>> grammarElements;
    private final Map<String, Set<NamespaceResolver.QualifiedName>> grammarRules;
    private final Map<String, Set<NamespaceResolver.QualifiedName>> grammarTokens;
    private final AntlrQueryItemType ITEM_ANY_ARRAY;
    private final AntlrQueryItemType ITEM_ANY_MAP;
    private final AntlrQueryItemType ITEM_ANY_FUNCTION;
    private final AntlrQueryItemType ITEM_ANY_NODE;
    private final AntlrQueryItemType ITEM_ANY_RULE;
    private final AntlrQueryItemType ITEM_ANY_TOKEN;
    private final AntlrQuerySequenceType SEQ_ANY_NODE;
    private final AntlrQuerySequenceType SEQ_ANY_ARRAY;
    private final AntlrQuerySequenceType SEQ_ANY_MAP;
    private final AntlrQuerySequenceType SEQ_ANY_FUNCTION;


    public MemoizedTypeFactory(final Map<String, Map<String, AntlrQueryItemType>> predefinedNamedTypes, Map<String, Set<NamespaceResolver.QualifiedName>> predefinedGrammars) {
        this.namedTypes = predefinedNamedTypes;
        GrammarTypes grammarTypes = getGrammarTypes(predefinedGrammars);
        this.grammarElements = grammarTypes.grammarElements;
        this.grammarTokens = grammarTypes.grammarTokens;
        this.grammarRules = grammarTypes.grammarRules;
        AntlrQuerySequenceType ZERO_OR_MORE_ITEMS = zeroOrMore(ITEM_ANY_ITEM);
        this.ITEM_ANY_ARRAY = new ArrayLikeType.ArrayType(ZERO_OR_MORE_ITEMS, Cardinality.ZERO_OR_MORE);
        this.ITEM_ANY_MAP = new MapLikeType.MapType(ITEM_ANY_ITEM, ZERO_OR_MORE_ITEMS);
        this.ITEM_ANY_NODE = new TreeNodeType.AnyNode();
        this.ITEM_ANY_FUNCTION = new FunctionType.AnyFunction(this);
        this.SEQ_ANY_NODE = new AntlrQuerySequenceType.NonEmptySequence(ITEM_ANY_NODE, Cardinality.ONE);
        this.SEQ_ANY_ARRAY = new AntlrQuerySequenceType.NonEmptySequence(ITEM_ANY_ARRAY, Cardinality.ONE);
        this.SEQ_ANY_MAP = new AntlrQuerySequenceType.NonEmptySequence(ITEM_ANY_MAP, Cardinality.ONE);
        this.SEQ_ANY_FUNCTION = new AntlrQuerySequenceType.NonEmptySequence(ITEM_ANY_FUNCTION, Cardinality.ONE);
        ITEM_ANY_RULE = new TreeRuleType.AnyRule();
        ITEM_ANY_TOKEN = new TreeTokenType.AnyToken();
    }

    record GrammarTypes(
            Map<String, Set<NamespaceResolver.QualifiedName>> grammarElements,
            Map<String, Set<NamespaceResolver.QualifiedName>> grammarTokens,
            Map<String, Set<NamespaceResolver.QualifiedName>> grammarRules
    ) {}

    private GrammarTypes getGrammarTypes(Map<String, Set<NamespaceResolver.QualifiedName>> grammars) {
        final Map<String, Set<NamespaceResolver.QualifiedName>> grammarElements = new HashMap<>(grammars.size());
        final Map<String, Set<NamespaceResolver.QualifiedName>> grammarRules = new HashMap<>(grammars.size());
        final Map<String, Set<NamespaceResolver.QualifiedName>> grammarTokens = new HashMap<>(grammars.size());
        for (var g : grammars.keySet()) {
            Set<NamespaceResolver.QualifiedName> elements = grammars.get(g);
            Set<NamespaceResolver.QualifiedName> rules = new HashSet<>(elements.size());
            Set<NamespaceResolver.QualifiedName> tokens = new HashSet<>(elements.size());
            for (NamespaceResolver.QualifiedName e : elements) {
                if (Pattern.matches("^\\P{IsUpper}]", e.name())) {
                    rules.add(e);
                } else {
                    tokens.add(e);
                }
            }
            grammarElements.put(g, elements);
            grammarRules.put(g, rules);
            grammarTokens.put(g, tokens);
        }
        return new GrammarTypes(grammarElements, grammarTokens, grammarRules);
    }


    @Override
    public AntlrQueryItemType itemNothing() {
        return ITEM_NOTHING;
    }

    @Override
    public AntlrQueryItemType itemError() {
        return ITEM_ERROR;
    }

    @Override
    public AntlrQueryItemType itemString() {
        return ITEM_STRING;
    }

    @Override
    public AntlrQueryItemType itemNumber() {
        return ITEM_NUMBER;
    }

    @Override
    public AntlrQueryItemType itemBoolean() {
        return ITEM_BOOLEAN;
    }

    @Override
    public AntlrQueryItemType itemTrue() {
        return ITEM_TRUE;
    }

    @Override
    public AntlrQueryItemType itemFalse() {
        return ITEM_FALSE;
    }

    @Override
    public AntlrQueryItemType itemAnyNode() {
        return ITEM_ANY_NODE;
    }

    @Override
    public AntlrQueryItemType itemAnyArray() {
        return ITEM_ANY_ARRAY;
    }

    @Override
    public AntlrQueryItemType itemAnyMap() {
        return ITEM_ANY_MAP;
    }

    @Override
    public AntlrQueryItemType itemAnyFunction() {
        return ITEM_ANY_FUNCTION;
    }

    @Override
    public AntlrQueryItemType itemAnyItem() {
        return ITEM_ANY_ITEM;
    }

    @Override
    public AntlrQueryItemType itemEnum(Set<String> memberNames) {
        if (memberNames.isEmpty()) return ITEM_NOTHING;
        return enumCache.computeIfAbsent(memberNames, StringType.StringEnum::new);
    }

    @Override
    public AntlrQueryItemType itemNumber(NumericRange numericRange) {
        return numberRangeCache.computeIfAbsent(numericRange, NumberType::new);
    }

    @Override
    public AntlrQueryItemType itemString(Cardinality length) {
        return stringLengthCache.computeIfAbsent(length, StringType.StringNonEnum::new);
    }

    @Override
    public AntlrQueryItemType itemNodesFromGrammar(String grammar, Set<NamespaceResolver.QualifiedName> elementName) {
        boolean containsToken = elementName.stream().anyMatch(qualifiedName -> qualifiedName.name().matches("^\\p{IsUpper}.*$"));
        boolean containsRule = elementName.stream().allMatch(qualifiedName -> qualifiedName.name().matches("^\\p{IsLower}.*$"));
        if (containsToken && containsRule) {
            return elementCache.computeIfAbsent(
                    new TreeNodeKey(grammar, Set.copyOf(elementName)),
                    k -> new TreeNodeType.NodeType(k.grammar(), k.names())
            );
        } else if (containsToken) {
            return elementCache.computeIfAbsent(
                    new TreeNodeKey(grammar, Set.copyOf(elementName)),
                    k -> new TreeTokenType.TokenType(k.grammar(), k.names())
            );
        } else {
            return elementCache.computeIfAbsent(
                    new TreeNodeKey(grammar, Set.copyOf(elementName)),
                    k -> new TreeRuleType.RuleType(k.grammar(), k.names())
            );
        }
    }

    private final Map<String, AntlrQueryItemType> anyNodesFromGrammar = new ConcurrentHashMap<>();

    @Override
    public AntlrQueryItemType itemAnyNodeFromGrammar(String grammar) {
        return anyNodesFromGrammar.computeIfAbsent(grammar, TreeNodeType.AnyNodeFromGrammar::new);
    }

    @Override
    public AntlrQueryItemType itemAnyToken() {
        return ITEM_ANY_TOKEN;
    }

    @Override
    public AntlrQueryItemType itemAnyTokenFromGrammar(String grammar) {
        return anyNodesFromGrammar.computeIfAbsent(grammar, TreeTokenType.AnyTokenFromGrammar::new);
    }

    @Override
    public AntlrQueryItemType itemAnyRule() {
        return ITEM_ANY_RULE;
    }


    @Override
    public AntlrQueryItemType itemAnyRuleFromGrammar(String grammar) {
        return anyNodesFromGrammar.computeIfAbsent(grammar, TreeRuleType.AnyRuleFromGrammar::new);
    }

    @Override
    public AntlrQuerySequenceType any() {
        return zeroOrMore(ITEM_ANY_ITEM);
    }

    @Override
    public AntlrQueryItemType itemTokensFromGrammar(String grammar, Set<NamespaceResolver.QualifiedName> mergedNames) {
        assert mergedNames.stream().allMatch(qualifiedName -> qualifiedName.name().matches("^\\p{IsUpper}.*$"));
        return tokenCache.computeIfAbsent(
                new TreeNodeKey(grammar, Set.copyOf(mergedNames)),
                k -> new TreeTokenType.TokenType(k.grammar(), k.names())
        );
    }

    @Override
    public AntlrQueryItemType itemRulesFromGrammar(String grammar, Set<NamespaceResolver.QualifiedName> mergedNames) {
        assert mergedNames.stream().allMatch(qualifiedName -> qualifiedName.name().matches("^\\p{IsLower}.*$"));
        return ruleCache.computeIfAbsent(
                new TreeNodeKey(grammar, Set.copyOf(mergedNames)),
                k -> new TreeRuleType.RuleType(k.grammar(), k.names())
        );
    }

    @Override
    public AntlrQueryItemType itemMap(AntlrQueryItemType keyType, AntlrQuerySequenceType valueType) {
        return new MapLikeType.MapType(keyType, valueType);
    }

    @Override
    public AntlrQueryItemType itemArray(AntlrQuerySequenceType itemType, Cardinality c) {
        return new ArrayLikeType.ArrayType(itemType, c);
    }

    @Override
    public AntlrQueryItemType itemTuple(List<AntlrQuerySequenceType> mergedElements) {
        return new ArrayLikeType.TupleType(mergedElements.toArray(AntlrQuerySequenceType[]::new));
    }

    @Override
    public AntlrQueryItemType itemTuple(AntlrQuerySequenceType... mergedElements) {
        return new ArrayLikeType.TupleType(mergedElements);
    }

    @Override
    public Set<NamespaceResolver.QualifiedName> grammarTokens(String grammar) {
        return grammarTokens.get(grammar);
    }

    @Override
    public Set<NamespaceResolver.QualifiedName> grammarNodes(String grammar) {
        return Set.of();
    }

    @Override
    public Set<NamespaceResolver.QualifiedName> grammarRules(String grammar) {
        return Set.of();
    }

    @Override
    public AntlrQueryItemType itemRegex() {
        return new RegexType(Pattern.compile("\\w+"));
    }

    @Override
    public AntlrQueryItemType itemGrammarReference(String text) {
        return new GrammarEntityType.GrammarType();
    }

    @Override
    public AntlrQueryItemType itemRuleReference(NamespaceResolver.QualifiedName qualifiedName) {
        return new GrammarEntityType.GrammarRuleType();
    }

    @Override
    public AntlrQueryItemType itemRuleReference(String namespace, Set<NamespaceResolver.QualifiedName> qname) {
        return new GrammarEntityType.GrammarRuleType();
    }

    @Override
    public AntlrQueryItemType itemAllRuleReferencesFromGrammar(String text) {
        return new GrammarEntityType.GrammarRuleType();
    }

    @Override
    public AntlrQueryItemType itemRuleReferencesFromGrammar(String text, Set<NamespaceResolver.QualifiedName> p) {
        return new GrammarEntityType.GrammarRuleType();
    }

    @Override
    public AntlrQueryItemType itemFunction(AntlrQuerySequenceType returnType, List<AntlrQuerySequenceType> argumentTypes) {
        return new FunctionType.ConstrainedFunction(argumentTypes, returnType);
    }

    @Override
    public AntlrQueryItemType itemChoice(AntlrQueryItemType... items) {
        assert items.length != 0;
        if (items.length == 1) return items[0];

        Set<ConcreteItemType> flattened = new HashSet<>();
        for (AntlrQueryItemType item : items) {
            if (item instanceof ChoiceItemType(ConcreteItemType[] itemTypes)) {
                Collections.addAll(flattened, itemTypes);
            } else if (!(item instanceof NothingType)) {
                flattened.add((ConcreteItemType) item);
            }
        }

        if (flattened.isEmpty()) return ITEM_NOTHING;
        if (flattened.size() == 1) return flattened.iterator().next();
        return choiceCache.computeIfAbsent(flattened, set->new ChoiceItemType(set.toArray(ConcreteItemType[]::new)));
    }

    @Override
    public AntlrQueryItemType itemRecord(final LinkedHashMap<String, RecordField> fields) {
        return new MapLikeType.RecordType(fields);
    }

    @Override
    public AntlrQueryItemType itemExtensibleRecord(final LinkedHashMap<String, RecordField> fields, AntlrQuerySequenceType additionalFieldType) {
        return new MapLikeType.ExtensibleRecordType(fields, additionalFieldType);
    }

    @Override
    public NamedItemAccessingResult itemNamedType(final NamespaceResolver.QualifiedName name) {
        final var namespace = namedTypes.get(name.namespace());
        if (namespace != null) {
            final var type = namespace.get(name.name());
            if (type != null) {
                return new NamedItemAccessingResult.Success(type);
            }
            return new NamedItemAccessingResult.UnknownName();
        }
        return new NamedItemAccessingResult.UnknownNamespace();
    }

    @Override
    public AntlrQueryItemType guaranteedItemNamedType(NamespaceResolver.QualifiedName name, Exception ifNoMatch) {
        var result = itemNamedType(name);
        if (result instanceof NamedItemAccessingResult.Success(AntlrQueryItemType type)) {
            return type;
        }
        if (ifNoMatch instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new RuntimeException(ifNoMatch);
    }

    @Override
    public RegistrationResult registerNamedType(final NamespaceResolver.QualifiedName name, final AntlrQueryItemType itemType) {
        final var namespace = namedTypes.computeIfAbsent(name.namespace(), _ -> new HashMap<>());
        final @Nullable AntlrQueryItemType existing = namespace.put(name.name(), itemType);
        if (existing == null) {
            return new RegistrationResult(itemType, RegistrationStatus.OK);
        } else if (existing.equals(itemType)) {
            return new RegistrationResult(existing, RegistrationStatus.ALREADY_REGISTERED_SAME);
        }
        return new RegistrationResult(existing, RegistrationStatus.ALREADY_REGISTERED_DIFFERENT);
    }

    public sealed interface GrammarRegistrationResult
            permits
                GrammarRegistrationResult.AlreadyRegistered,
                GrammarRegistrationResult.Success
    {
        record Success() implements GrammarRegistrationResult {}
        record AlreadyRegistered() implements GrammarRegistrationResult {}
    }

    @Override
    public GrammarRegistrationResult registerGrammars(final String grammar, final Set<NamespaceResolver.QualifiedName> elements) {
        if (grammarElements.get(grammar) == null) {
            Set<NamespaceResolver.QualifiedName> rules = new HashSet<>(elements.size());
            Set<NamespaceResolver.QualifiedName> tokens = new HashSet<>(elements.size());
            for (NamespaceResolver.QualifiedName e : elements) {
                if (Pattern.matches("^\\P{IsUpper}]", e.name())) {
                    rules.add(e);
                } else {
                    tokens.add(e);
                }
            }
            grammarElements.put(grammar, elements);
            grammarRules.put(grammar, rules);
            grammarTokens.put(grammar, tokens);
        }
        return new GrammarRegistrationResult.Success();

    }

    @Override
    public AntlrQuerySequenceType emptySequence() {
        return EMPTY_SEQUENCE;
    }

    @Override
    public AntlrQuerySequenceType neverType() {
        return NEVER_TYPE;
    }

    @Override
    public AntlrQuerySequenceType error() {
        return SEQ_ERROR;
    }

    @Override
    public AntlrQuerySequenceType string() {
        return SEQ_STRING;
    }

    @Override
    public AntlrQuerySequenceType anyNode() {
        return SEQ_ANY_NODE;
    }

    @Override
    public AntlrQuerySequenceType anyArray() {
        return SEQ_ANY_ARRAY;
    }

    @Override
    public AntlrQuerySequenceType anyMap() {
        return SEQ_ANY_MAP;
    }

    @Override
    public AntlrQuerySequenceType anyFunction() {
        return SEQ_ANY_FUNCTION;
    }

    @Override
    public AntlrQuerySequenceType anyItem() {
        return SEQ_ANY_ITEM;
    }

    @Override
    public AntlrQuerySequenceType boolean_() {
        return SEQ_BOOLEAN;
    }

    @Override
    public AntlrQuerySequenceType enum_(Set<String> memberNames) {
        return one(itemEnum(memberNames));
    }

    @Override
    public AntlrQuerySequenceType number() {
        return SEQ_NUMBER;
    }

    @Override
    public AntlrQuerySequenceType number(NumericRange union) {
        return one(itemNumber(union));
    }

    @Override
    public AntlrQuerySequenceType array(AntlrQuerySequenceType itemType, Cardinality c) {
        return one(itemArray(itemType, c));
    }

    @Override
    public AntlrQuerySequenceType tuple(List<AntlrQuerySequenceType> mergedElements) {
        return one(itemTuple(mergedElements));
    }

    @Override
    public AntlrQuerySequenceType tuple(AntlrQuerySequenceType... mergedElements) {
        return one(itemTuple(mergedElements));
    }

    @Override
    public AntlrQuerySequenceType map(AntlrQueryItemType mapKeyType, AntlrQuerySequenceType mapValueType) {
        return one(itemMap(mapKeyType, mapValueType));
    }

    @Override
    public AntlrQuerySequenceType record(LinkedHashMap<String, RecordField> fields) {
        return one(itemRecord(new LinkedHashMap<>(fields)));
    }

    @Override
    public AntlrQuerySequenceType extensibleRecord(LinkedHashMap<String, RecordField> fields) {
        return one(itemExtensibleRecord(fields, zeroOrMore(itemAnyItem())));
    }

    @Override
    public AntlrQuerySequenceType element(String grammar, Set<NamespaceResolver.QualifiedName> elementName) {
        return one(itemNodesFromGrammar(grammar, elementName));
    }

    @Override
    public AntlrQuerySequenceType function(AntlrQuerySequenceType returnType, List<AntlrQuerySequenceType> argumentTypes) {
        return one(itemFunction(returnType, argumentTypes));
    }

    @Override
    public AntlrQuerySequenceType choice(AntlrQueryItemType... items) {
        return one(itemChoice(items));
    }

    @Override
    public AntlrQuerySequenceType one(AntlrQueryItemType itemType) {
        return sequence(itemType, Cardinality.ONE);
    }

    @Override
    public AntlrQuerySequenceType zeroOrOne(AntlrQueryItemType itemType) {
        return sequence(itemType, Cardinality.ZERO_OR_ONE);
    }

    @Override
    public AntlrQuerySequenceType zeroOrMore(AntlrQueryItemType itemType) {
        return sequence(itemType, Cardinality.ZERO_OR_MORE);
    }

    @Override
    public AntlrQuerySequenceType oneOrMore(AntlrQueryItemType itemType) {
        return sequence(itemType, Cardinality.ONE_OR_MORE);
    }

    @Override
    public AntlrQuerySequenceType sequence(AntlrQueryItemType itemType, Cardinality cardinality) {
        if (cardinality.equals(Cardinality.ZERO) || itemType instanceof NothingType) {
            return EMPTY_SEQUENCE;
        }
        return new AntlrQuerySequenceType.NonEmptySequence(itemType, cardinality);
    }
}
