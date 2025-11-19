package com.github.akruk.antlrxquery.semanticanalyzer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.antlr.v4.parse.ANTLRParser.finallyClause_return;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.akruk.antlrxquery.AntlrXqueryParser.*;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.semanticanalyzer.GrammarManager.GrammarFile;
import com.github.akruk.antlrxquery.semanticanalyzer.ModuleManager.ImportResult;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.Assumption;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager.AnalysisResult;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager.ArgumentSpecification;
import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.HelperTrees;
import com.github.akruk.antlrxquery.XQueryAxis;
import com.github.akruk.antlrxquery.charescaper.XQuerySemanticCharEscaper;
import com.github.akruk.antlrxquery.charescaper.XQuerySemanticCharEscaper.XQuerySemanticCharEscaperResult;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.QualifiedGrammarAnalysisResult;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField.TypeOrReference;
import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType.EffectiveBooleanValueType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType.RelativeCoercability;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory.NamedAccessingStatus;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory.NamedItemAccessingResult;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory.RegistrationResult;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypeAtomization;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypeCastable;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypePathOperator;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypeCastable.Castability;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypeCastable.IsCastableResult;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypePathOperator.GrammarStatus;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypePathOperator.PathOperatorResult;


public class XQuerySemanticAnalyzer extends AntlrXqueryParserBaseVisitor<TypeInContext>
{
    private final XQuerySemanticContextManager contextManager;
    private final List<DiagnosticError> errors;
    private final List<DiagnosticWarning> warnings;
    private final XQueryTypeFactory typeFactory;
    private final XQueryValueFactory valueFactory;
    private final XQuerySemanticSymbolManager symbolManager;
    private final SequencetypePathOperator pathOperator;
    // private final Parser antlrQueryParser;
    // private final List<Path> modulePaths;
    private final ModuleManager moduleManager;
    private final GrammarManager grammarManager;

    private XQueryVisitingSemanticContext context;
    private List<TypeInContext> visitedPositionalArguments;
    private Map<String, TypeInContext> visitedKeywordArguments;

    protected final XQuerySequenceType number;
    protected final XQuerySequenceType zeroOrMoreNodes;
    protected final XQuerySequenceType anyArray;
    protected final XQuerySequenceType anyMap;
    protected final XQuerySequenceType boolean_;
    protected final XQuerySequenceType string;
    protected final XQuerySequenceType optionalNumber;
    protected final XQuerySequenceType anyNumbers;
    protected final XQuerySequenceType optionalString;
    protected final XQuerySequenceType anyItem;
    protected final XQuerySequenceType anyArrayOrMap;
    protected final XQuerySequenceType zeroOrMoreItems;
    protected final XQuerySequenceType emptySequence;
    protected final XQuerySequenceType zeroOrMoreNumbers;

    public interface AnalysisListener {
        void onVariableDeclaration(VarNameContext varName, TypeInContext type);
        void onVariableReference(VarRefContext varRef, TypeInContext type);
    }

    private List<AnalysisListener> listeners = new ArrayList<>();

    public List<AnalysisListener> getListeners() {
        return listeners;
    }

    public void setListeners(final List<AnalysisListener> listeners) {
        this.listeners = listeners;
    }

    public void addListener(final AnalysisListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final AnalysisListener listener) {
        listeners.remove(listener);
    }

    public List<DiagnosticError> getErrors()
    {
        return errors;
    }

    public List<DiagnosticWarning> getWarnings()
    {
        return warnings;
    }

    public XQuerySemanticSymbolManager getSymbolManager() {
        return symbolManager;
    }


    Map<QualifiedName, UnresolvedRecordSpecification> recordsMapped = Map.of();
    Map<QualifiedName, ItemTypeDeclContext> itemsMapped = Map.of();
    Map<QualifiedName, List<UnresolvedFunctionSpecification>> functionsMapped = Map.of();

    @Override
    public TypeInContext visitXquery(final XqueryContext ctx)
    {
       if (ctx.libraryModule() != null) {
            return visitLibraryModule(ctx.libraryModule());
        } else {
            return visitMainModule(ctx.mainModule());
        }
    }

    @Override
    public TypeInContext visitQueryBody(final QueryBodyContext ctx)
    {
        if (ctx.expr()!=null) {
            return visitExpr(ctx.expr());
        }
        return contextManager.typeInContext(emptySequence);
    }


    @Override
    public TypeInContext visitMainModule(final MainModuleContext ctx)
    {
        final var p = ctx.prolog();
        symbolManager.provideNamespace("");
        handleDefaultNamespaceDeclarations(
            p.defaultNamespaceDecl(),
            "fn",
            "",
            "",
            "",
            ""
            );
        handleNamespaceDeclarations(p, "");
        handleContextValueDeclarations(p);
        final Map<Boolean, List<ImportDeclContext>> groupedByIsGrammarImport =
            p.importDecl()
            .stream()
            .collect(Collectors.groupingBy(c->c.grammarImport() != null));
        handleGrammarImports(groupedByIsGrammarImport.getOrDefault(true, List.of()));
        handleSymbolResolution(
            groupedByIsGrammarImport.getOrDefault(false, List.of()),
            p.functionDecl(),
            p.itemTypeDecl(),
            p.namedRecordTypeDecl(),
            "");

        return visitQueryBody(ctx.queryBody());
    }




    @Override
    public TypeInContext visitLibraryModule(final LibraryModuleContext ctx) {
        final var p = ctx.prolog();
        final String moduleNamespace = ctx.moduleDecl().qname().getText();
        registerUniqueNamespace(ctx.moduleDecl(), moduleNamespace, ErrorType.NAMESPACE_DECL__NAMESPACE_REDECLARATION);
        handleDefaultNamespaceDeclarations(p.defaultNamespaceDecl(), "fn", "", moduleNamespace, "", "");
        handleNamespaceDeclarations(p, moduleNamespace);
        final Map<Boolean, List<ImportDeclContext>> groupedByIsGrammarImport =
            p.importDecl()
            .stream()
            .collect(Collectors.groupingBy(c->c.grammarImport() != null));
        handleGrammarImports(groupedByIsGrammarImport.getOrDefault(true, List.of()));
        handleSymbolResolution(
            groupedByIsGrammarImport.getOrDefault(false, List.of()),
            p.functionDecl(),
            p.itemTypeDecl(),
            p.namedRecordTypeDecl(),
            moduleNamespace);



        handleLibraryContextDeclarations(p);

        //     : ((... | setter | ... | importDecl) SEPARATOR)*
        // ((... | varDecl | functionDecl | itemTypeDecl | namedRecordTypeDecl | optionDecl) SEPARATOR)*


        return null;
    }





    private void handleGrammarImports(final List<ImportDeclContext> imports)
    {
        for (final ImportDeclContext grammarImport : imports) {
            grammarImport.grammarImport().accept(this);
        }
    }

    @Override
    public TypeInContext visitNamespaceGrammarImport(final NamespaceGrammarImportContext ctx) {
        final List<String> paths = ctx.STRING().stream().map(TerminalNode::getText).toList();
        final var namespace = ctx.namespacePrefix().qname().getText();
        if (symbolManager.grammarExists(namespace)) {
            error(ctx, ErrorType.GRAMMAR_IMPORT__GRAMMAR_ALREADY_REGISTERED, List.of(namespace));
            return null;
        }
        final var importResult = grammarManager.namespaceGrammarImport(paths);
        switch(importResult.status()) {
            case OK -> {
                final var trees = importResult.validPaths().values().stream().map(GrammarFile::tree).toList();
                final var analyzer = new InputGrammarAnalyzer();
                final var qualifiedResult = analyzer.analyze(namespace, trees);
                symbolManager.registerGrammar(namespace, qualifiedResult);
            }
            case MANY_VALID_PATHS -> {
                error(ctx, ErrorType.GRAMMAR_IMPORT__MANY_VALID_PATHS, List.of(importResult));
            }
            case NO_PATH_FOUND__NEITHER_FOUND -> {
                error(ctx, ErrorType.GRAMMAR_IMPORT__NEITHER_FOUND, List.of(importResult));
            }
            case NO_PATH_FOUND__NO_LEXER -> {
                error(ctx, ErrorType.GRAMMAR_IMPORT__NO_LEXER, List.of(importResult));
            }
            case NO_PATH_FOUND__NO_PARSER -> {
                error(ctx, ErrorType.GRAMMAR_IMPORT__NO_PARSER, List.of(importResult));
            }
        }
        return null;
    }


    private void handleSymbolResolution(
        final List<ImportDeclContext> imports,
        final List<FunctionDeclContext> functions,
        final List<ItemTypeDeclContext> items,
        final List<NamedRecordTypeDeclContext> records,
        final String moduleNamespace)
    {
        final Map<QualifiedName, UnresolvedRecordSpecification> recordsMapped
            = new HashMap<>();
        final Map<QualifiedName, ItemTypeDeclContext> itemsMapped
            = new HashMap<>();
        final Map<QualifiedName, List<UnresolvedFunctionSpecification>> functionsMapped
            = new HashMap<>();

        for (final NamedRecordTypeDeclContext record : records) {
            final QualifiedName name = namespaceResolver.resolveType(record.qname().getText());
            validateRecordNamespace(moduleNamespace, record, name);
            final var extendedFieldDeclaration = record.extendedFieldDeclaration();
            final var fieldDeclarations = extendedFieldDeclaration.stream().map(ExtendedFieldDeclarationContext::fieldDeclaration).toList();
            validateRecordFieldNames(record, name, fieldDeclarations);
            final UnresolvedRecordSpecification recordSpecification = getUnresolvedRecord(name, record);
            recordsMapped.put(name, recordSpecification);
        }

        for (final ItemTypeDeclContext itemtype : items) {
            final QualifiedName name = namespaceResolver.resolveType(itemtype.qname().getText());
            validateItemTypeNamespace(moduleNamespace, itemtype, name);
            itemsMapped.put(name, itemtype);
        }

        for (final FunctionDeclContext function : functions) {
            final QualifiedName name = namespaceResolver.resolveType(function.qname().getText());
            validateFunctionNamespace(moduleNamespace, function, name);
            functionsMapped.computeIfAbsent(name, e->(new ArrayList<>()))
                .add(getUnresolvedFunction(name, function));
        }


        for (final var import_ : imports) {
            visitImportDecl(import_);
        }
        int importedRecordsCount = 0;
        int importedItemsCount = 0;
        int importedFunctionsCount = 0;
        for (final var module : currentFileImportedModules.keySet()) {
            final XqueryContext moduleTree = currentFileImportedModules.get(module);
            final PrologContext moduleProlog = moduleTree.libraryModule().prolog();
            importedRecordsCount += moduleProlog.namedRecordTypeDecl().size();
            importedItemsCount += moduleProlog.itemTypeDecl().size();
            importedFunctionsCount += moduleProlog.functionDecl().size();
        }
        final var importedRecords = new ArrayList<NamedRecordTypeDeclContext>(importedRecordsCount);
        final var importedItems = new ArrayList<ItemTypeDeclContext>(importedItemsCount);
        final var importedFunctions = new ArrayList<FunctionDeclContext>(importedFunctionsCount);
        for (final var module : currentFileImportedModules.keySet()) {
            final XqueryContext moduleTree = currentFileImportedModules.get(module);
            final PrologContext moduleProlog = moduleTree.libraryModule().prolog();
            importedRecords.addAll(moduleProlog.namedRecordTypeDecl());
            importedItems.addAll(moduleProlog.itemTypeDecl());
            importedFunctions.addAll(moduleProlog.functionDecl());
        }

        for (final NamedRecordTypeDeclContext r : importedRecords) {
            final QualifiedName qName = namespaceResolver.resolveType(r.qname().getText());
            UnresolvedRecordSpecification unresolvedRecord = getUnresolvedRecord(qName, r);
            if (isBuiltInType(qName)) {
                error(r.qname(), ErrorType.RECORD_DECLARATION__USED_RESERVED_NAME, List.of(qName, r));

            } else if (recordsMapped.containsKey(qName)) {
                error(r.qname(), ErrorType.RECORD_DECLARATION__ALREADY_REGISTERED_BY_NAME,
                    List.of(qName, r, recordsMapped.get(qName)));

            } else {
                recordsMapped.put(qName, unresolvedRecord);
            }
        }

        for (final ItemTypeDeclContext i : importedItems) {
            final QualifiedName qName = namespaceResolver.resolveType(i.qname().getText());
            if (isBuiltInType(qName)) {
                error(
                    i.qname(),
                    ErrorType.ITEM_DECLARATION__USED_RESERVED_NAME,
                    List.of(qName, i));
            } else if (itemsMapped.containsKey(qName)) {
                error(
                    i.qname(),
                ErrorType.ITEM_DECLARATION__ALREADY_REGISTERED_BY_NAME,
                    List.of(qName, i, itemsMapped.get(qName))
                );

            } else {
                itemsMapped.put(qName, i);
            }
        }

        final Set<QualifiedName> crossReferences = new HashSet<>(recordsMapped.keySet());
        crossReferences.retainAll(itemsMapped.keySet());
        for (final var cr : crossReferences) {
            final UnresolvedRecordSpecification whereRecord = recordsMapped.get(cr);
            final ItemTypeDeclContext whereItem = itemsMapped.get(cr);
            error(
                whereItem.qname(),
                ErrorType.NAMED_TYPES__RECORD_ITEM_TYPE_CROSS_REFERENCE,
                List.of(cr, whereRecord, whereItem));
        }



        this.recordsMapped = recordsMapped;
        this.itemsMapped = itemsMapped;


        for (var r : recordsMapped.values()) {
            RecordResolutionResult resolved = resolveRecordFromUnresolved(r.name, r);
            symbolManager.registerFunction(
                r.name.namespace(),
                r.name.name(),
                resolved.fieldsAsArgs,
                typeFactory.one(resolved.recordItemType));
        }


        for (final FunctionDeclContext f : importedFunctions) {
            final QualifiedName qName = namespaceResolver.resolveFunction(f.qname().getText());
            final UnresolvedFunctionSpecification spec = getUnresolvedFunction(qName, f);
            boolean isValid = validateUnresolvedFunction(spec);
            if (isValid) {
                var declarationResult = symbolManager.declareFunction(spec);
                switch(declarationResult.status()) {
                    case COLLISION -> {
                        error(f, ErrorType.FUNCTION__ARITY_COLLISION, List.of(qName, spec.minArity, spec.maxArity, declarationResult.collisions()) );
                    }
                    case OK -> {}
                }
            }
        }

    }

    private boolean isBuiltInType(final QualifiedName qName) {
        if (!qName.namespace().equals("")) {
            return false;
        }
        return switch(qName.name()) {
            case "string" -> true;
            case "number" -> true;
            case "boolean" -> true;
            default -> false;
        };
    }

    private void validateFunctionNamespace(final String moduleNamespace, final FunctionDeclContext function, final QualifiedName name) {
        if (!name.namespace().startsWith(moduleNamespace)) {
            error(
                function.qname(),
                ErrorType.FUNCTION__INVALID_NAMESPACE,
                List.of(name, moduleNamespace, function));
        }
    }

    private void validateItemTypeNamespace(final String moduleNamespace, final ItemTypeDeclContext itemtype, final QualifiedName name) {
        if (!name.namespace().startsWith(moduleNamespace)) {
            error(
                itemtype.qname(),
                ErrorType.ITEM_DECLARATION__INVALID_NAMESPACE,
                List.of(name, itemtype));
        }
    }

    private void validateRecordFieldNames(final NamedRecordTypeDeclContext record, final QualifiedName name,
            final List<FieldDeclarationContext> fieldDeclarations) {
        final Set<String> fieldNames = new HashSet<>();
        for (final var fD : fieldDeclarations) {
            if (!fieldNames.add(fD.fieldName().getText())) {
                error(
                    fD.fieldName(),
                    ErrorType.RECORD_DECLARATION__DUPLICATE_FIELD_NAME,
                    List.of(name, record));
            }
        }
    }

    private void validateRecordNamespace(final String moduleNamespace, final NamedRecordTypeDeclContext record, final QualifiedName name) {
        if (!name.namespace().startsWith(moduleNamespace)) {
            error(
                record.qname(),
                ErrorType.RECORD_DECLARATION__INVALID_NAMESPACE,
                List.of(name.namespace(), moduleNamespace, record));
        }
    }

    private void registerUniqueNamespace(final ModuleDeclContext ctx, final String moduleNamespace, final ErrorType errorType) {
        if (symbolManager.namespaceExists(moduleNamespace)) {
            error(ctx, errorType, List.of(moduleNamespace));
        } else {
            symbolManager.provideNamespace(moduleNamespace);
        }
    }

    private void handleLibraryContextDeclarations(final PrologContext p) {
        for (final var ctxValueDecl : p.contextValueDecl()) {
            error(ctxValueDecl, ErrorType.CONTEXT_VALUE_DECL__NOT_IN_MAIN_MODULE, null);
        }
    }

    private void handleNamespaceDeclarations(final PrologContext p, final String moduleNamespace) {
        for (final var namespaceDecl : p.namespaceDecl()) {
            final var namespace = namespaceDecl.qname().getText();
            if (symbolManager.namespaceExists(namespace)) {
                error(namespaceDecl, ErrorType.NAMESPACE_DECL__NAMESPACE_REDECLARATION, List.of(namespace, namespaceDecl));
            } else {
                if (!namespace.startsWith(moduleNamespace)) {
                    error(namespaceDecl, ErrorType.NAMESPACE_DECL__INVALID_PREFIX, List.of(namespace, moduleNamespace));
                }
                symbolManager.provideNamespace(namespace);
            }
        }
    }




    private void handleDefaultNamespaceDeclarations(
        final List<DefaultNamespaceDeclContext> defaultNamespaceDecls,
        final String moduleFunctionNamespace,
        final String moduleElementNamespace,
        final String moduleTypeNamespace,
        final String moduleAnnotationNamespace,
        final String moduleConstructionNamespace
        )
    {
        final Map<Integer, List<DefaultNamespaceDeclContext>> splitByType =
            defaultNamespaceDecls
            .stream()
            .collect(Collectors.groupingBy(DefaultNamespaceDeclContext::getAltNumber));
        final List<DefaultNamespaceDeclContext> elementDecls = splitByType.getOrDefault(2, List.of());
        final String defaultElementNamespace = validateDefaultElementNamespace(moduleElementNamespace, elementDecls);

        final List<DefaultNamespaceDeclContext> functionDecls = splitByType.getOrDefault(0, List.of());
        final String defaultFunctionNamespace = validateDefaultFunctionNamespace(moduleFunctionNamespace, functionDecls);
        symbolManager.provideNamespace(defaultFunctionNamespace);

        final List<DefaultNamespaceDeclContext> typeDecls = splitByType.getOrDefault(1, List.of());
        final String defaultTypeNamespace = validateDefaultTypeNamespace(moduleTypeNamespace, typeDecls);
        symbolManager.provideNamespace(defaultTypeNamespace);

        final List<DefaultNamespaceDeclContext> annotationDecls = splitByType.getOrDefault(3, List.of());
        final String defaultAnnotationNamespace = validateDefaultAnnotationNamespace(moduleAnnotationNamespace, annotationDecls);
        symbolManager.provideNamespace(defaultAnnotationNamespace);

        final List<DefaultNamespaceDeclContext> constructionDecls = splitByType.getOrDefault(4, List.of());
        final String defaultConstructionNamespace = validateDefaultConstructionNamespace(moduleConstructionNamespace, constructionDecls);


        namespaceResolver = new NamespaceResolver(
            defaultFunctionNamespace,
            defaultElementNamespace,
            defaultTypeNamespace,
            defaultConstructionNamespace,
            defaultAnnotationNamespace
            );
    }

    private String validateDefaultFunctionNamespace(
        final String moduleFunctionNamespace,
        final List<DefaultNamespaceDeclContext> functionDecls)
    {
        final String defaultFunctionNamespace = switch(functionDecls.size())
        {
            case 0 -> moduleFunctionNamespace;
            case 1 -> functionDecls.get(0).qname().getText();
            default -> {
                for (final var d : functionDecls) {
                    error(d, ErrorType.DEFAULT_NAMESPACE_DECL__MULTIPLE_FUNCTION_NAMESPACE_DECLARATIONS, null);
                }
                yield moduleFunctionNamespace;
            }
        };
        return defaultFunctionNamespace;
    }

    private String validateDefaultAnnotationNamespace(
        final String moduleAnnotationNamespace,
        final List<DefaultNamespaceDeclContext> AnnotationDecls)
    {
        final String defaultAnnotationNamespace = switch(AnnotationDecls.size())
        {
            case 0 -> moduleAnnotationNamespace;
            case 1 -> AnnotationDecls.get(0).qname().getText();
            default -> {
                for (final var d : AnnotationDecls) {
                    error(d, ErrorType.DEFAULT_NAMESPACE_DECL__MULTIPLE_ANNOTATION_NAMESPACE_DECLARATIONS, null);
                }
                yield moduleAnnotationNamespace;
            }
        };
        return defaultAnnotationNamespace;
    }

    private String validateDefaultConstructionNamespace(
        final String moduleConstructionNamespace,
        final List<DefaultNamespaceDeclContext> ConstructionDecls)
    {
        final String defaultConstructionNamespace = switch(ConstructionDecls.size())
        {
            case 0 -> moduleConstructionNamespace;
            case 1 -> {
                final var defaultNamespace = ConstructionDecls.get(0).qname().getText();
                if (symbolManager.grammarExists(defaultNamespace)) {

                }
                yield defaultNamespace;
            }
            default -> {
                for (final var d : ConstructionDecls) {
                    error(d, ErrorType.DEFAULT_NAMESPACE_DECL__MULTIPLE_CONSTRUCTION_NAMESPACE_DECLARATIONS, null);
                }
                yield moduleConstructionNamespace;
            }
        };
        return defaultConstructionNamespace;
    }

    private String validateDefaultElementNamespace(
            final String moduleElementNamespace,
            final List<DefaultNamespaceDeclContext> elementDecls)
    {
        final String defaultElementNamespace = switch(elementDecls.size())
        {
            case 0 -> moduleElementNamespace;
            case 1 -> elementDecls.get(0).qname().getText();
            default -> {
                for (final var d : elementDecls) {
                    error(d, ErrorType.DEFAULT_NAMESPACE_DECL__MULTIPLE_ELEMENT_NAMESPACE_DECLARATIONS, null);
                }
                yield moduleElementNamespace;
            }
        };
        return defaultElementNamespace;
    }

    private String validateDefaultTypeNamespace(
        final String moduleTypeNamespace,
        final List<DefaultNamespaceDeclContext> typeDecls
        )
    {
        final String defaultTypeNamespace = switch(typeDecls.size())
        {
            case 0 -> moduleTypeNamespace;
            case 1 -> typeDecls.get(0).qname().getText();
            default -> {
                for (final var d : typeDecls) {
                    error(d, ErrorType.DEFAULT_NAMESPACE_DECL__MULTIPLE_TYPE_NAMESPACE_DECLARATIONS, null);
                }
                yield moduleTypeNamespace;
            }
        };
        return defaultTypeNamespace;
    }

    private void handleContextValueDeclarations(final PrologContext p) {
        switch (p.contextValueDecl().size()) {
            case 0 -> {}// set in constructor
            case 1 -> visitContextValueDecl(p.contextValueDecl(0));
            default -> {
                for (final var ctxValueDecl : p.contextValueDecl()) {
                    error(ctxValueDecl, ErrorType.CONTEXT_VALUE_DECL__MULTIPLE_DECLARATIONS, null);
                }
            }
        }
    }


    public XQuerySemanticAnalyzer(
        final Parser antlrQueryParser,
        final XQuerySemanticContextManager contextManager,
        final XQueryTypeFactory typeFactory,
        final XQueryValueFactory valueFactory,
        final XQuerySemanticSymbolManager functionCaller,
        final Map<String, QualifiedGrammarAnalysisResult> importedGrammars,
        final ModuleManager moduleManager,
        final GrammarManager grammarManager,
        final XQuerySequenceType contextType)
    {
        // this.antlrQueryParser = antlrQueryParser;
        // this.modulePaths = modulePaths;
        this.typeFactory = typeFactory;
        this.valueFactory = valueFactory;
        this.symbolManager = functionCaller;
        this.symbolManager.setAnalyzer(this);
        this.contextManager = contextManager;
        this.contextManager.enterContext();
        this.context = new XQueryVisitingSemanticContext();
        this.context.setType(contextManager.typeInContext(contextType));
        this.context.setPositionType(null);
        this.context.setSizeType(null);
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.anyArrayOrMap = typeFactory.zeroOrMore(typeFactory.itemChoice(Set.of(typeFactory.itemAnyMap(), typeFactory.itemAnyArray())));
        this.zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        this.emptySequence = typeFactory.emptySequence();
        this.number = typeFactory.number();
        this.zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        this.anyArray = typeFactory.anyArray();
        this.anyMap = typeFactory.anyMap();
        this.boolean_ = typeFactory.boolean_();
        this.string = typeFactory.string();
        this.optionalNumber = typeFactory.zeroOrOne(typeFactory.itemNumber());
        this.anyNumbers = typeFactory.zeroOrMore(typeFactory.itemNumber());
        this.optionalString = typeFactory.zeroOrOne(typeFactory.itemString());
        this.anyItem = typeFactory.anyItem();
        this.zeroOrOneItem = typeFactory.zeroOrOne(typeFactory.itemAnyItem());

        if (importedGrammars !=  null) {
            for (final String grammarname : importedGrammars.keySet()) {
                symbolManager.registerGrammar(grammarname, importedGrammars.get(grammarname));
            }
        }
        this.atomizer = new SequencetypeAtomization(typeFactory);
        this.castability = new SequencetypeCastable(typeFactory, atomizer);
        this.anyNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        this.pathOperator = new SequencetypePathOperator(
            typeFactory,
            symbolManager);
        this.moduleManager = moduleManager;
        this.grammarManager = grammarManager;
        zeroOrMoreNumbers = typeFactory.zeroOrMore(typeFactory.itemNumber());


    }

    @Override
    public TypeInContext visitFLWORExpr(final FLWORExprContext ctx)
    {
        final var saveReturnedOccurence = saveReturnedOccurence();
        contextManager.enterScope();
        visitInitialClause(ctx.initialClause());
        for (final var clause : ctx.intermediateClause()) {
            clause.accept(this);
        }
        // at this point visitedTupleStream should contain all tuples
        final var expressionValue = visitReturnClause(ctx.returnClause());
        contextManager.leaveScope();
        returnedOccurrence = saveReturnedOccurence;
        return expressionValue;
    }

    private int returnedOccurrence = 1;

    private int saveReturnedOccurence()
    {
        final var saved = returnedOccurrence;
        returnedOccurrence = 1;
        return saved;
    }

    @Override
    public TypeInContext visitLetClause(final LetClauseContext ctx)
    {
        for (final var letBinding : ctx.letBinding()) {
            final VarNameAndTypeContext varNameAndType = letBinding.varNameAndType();
            declareVariable(letBinding, varNameAndType, letBinding.exprSingle());
        }
        return null;
    }

    private void declareVariable(final ParserRuleContext ctx,
                                final VarNameAndTypeContext varNameAndType,
                                final ExprSingleContext assignedValueCtx)
    {
        final String variableName = varNameAndType.varName().qname().getText();
        final TypeInContext assignedValue = visitExprSingle(assignedValueCtx);
        if (varNameAndType.typeDeclaration() == null) {
            declareVariable(assignedValue, variableName, varNameAndType.varName());
        } else {
            final TypeInContext type = varNameAndType.typeDeclaration().accept(this);
            if (!assignedValue.isSubtypeOf(type)) {
                error(ctx, ErrorType.LOOKUP__INVALID_TARGET, List.of(variableName, assignedValue, type));
            }
            declareVariable(type, variableName, varNameAndType.varName());
        }
    }

    private void declareVariable(final TypeInContext type, final VarNameContext varNameCtx) {
        final String varName = varNameCtx.qname().getText();
        declareVariable(type, varName, varNameCtx);
    }

    private void declareVariable(final TypeInContext type, final String varName, final VarNameContext varNameCtx) {
        contextManager.entypeVariable(varName, type);
        for (final var listener : listeners) {
            listener.onVariableDeclaration(varNameCtx, type);
        }
    }


    @Override
    public TypeInContext visitForClause(final ForClauseContext ctx) {
        // TODO: add coercion
        for (final ForBindingContext forBinding : ctx.forBinding()) {
            if (forBinding.forItemBinding() != null) {
                processForItemBinding(forBinding.forItemBinding());
            } else if (forBinding.forMemberBinding() != null) {
                processForMemberBinding(forBinding.forMemberBinding());
            } else if (forBinding.forEntryBinding() != null) {
                processForEntryBinding(forBinding.forEntryBinding());
            }
        }
        return null;
    }

    @Override
    public TypeInContext visitTumblingWindowClause(final TumblingWindowClauseContext ctx) {
        final var iteratedType = visitExprSingle(ctx.exprSingle());
        final var iterator = contextManager.typeInContext(iteratedType.iteratorType());
        final var optionalIterator = contextManager.typeInContext(iterator.type.addOptionality());
        final String windowVariableName = ctx.varNameAndType().varName().qname().getText();
        final TypeInContext windowSequenceType = contextManager.typeInContext(typeFactory.oneOrMore(iterator.type.itemType));

        returnedOccurrence = arrayMergeFLWOROccurence();
        handleWindowStartClause(ctx.windowStartCondition(), iterator, optionalIterator);
        handleWindowEndClause(ctx.windowEndCondition(), iterator, optionalIterator);
        handleWindowIterator(ctx.varNameAndType(), windowVariableName, windowSequenceType);
        return null;
    }

    private void entypeWindowVariables(
        final TypeInContext iterator,
        final TypeInContext optionalIterator,
        final WindowVarsContext windowVars)
    {
        final var currentVar = windowVars.currentVar();
        if (currentVar != null) {
            declareVariable(iterator, currentVar.varName());
        }
        final var currentVarPos = windowVars.positionalVar();
        if (currentVarPos != null) {
            declareVariable(contextManager.typeInContext(number), currentVarPos.varName());
        }
        final var previousVar = windowVars.previousVar();
        if (previousVar != null) {
            declareVariable(optionalIterator, previousVar.varName());
        }
        final var nextVar = windowVars.nextVar();
        if (nextVar != null) {
            declareVariable(optionalIterator, nextVar.varName());
        }
    }

    @Override
    public TypeInContext visitSlidingWindowClause(final SlidingWindowClauseContext ctx) {
        final var iteratedType = visitExprSingle(ctx.exprSingle());
        final var iterator = contextManager.typeInContext(iteratedType.iteratorType());
        final var optionalIterator = contextManager.typeInContext(iterator.type.addOptionality());
        final String windowVariableName = ctx.varNameAndType().varName().qname().getText();
        final TypeInContext windowSequenceType = contextManager.typeInContext(typeFactory.oneOrMore(iterator.type.itemType));

        returnedOccurrence = arrayMergeFLWOROccurence();
        handleWindowStartClause(ctx.windowStartCondition(), iterator, optionalIterator);
        handleWindowEndClause(ctx.windowEndCondition(), iterator, optionalIterator);
        handleWindowIterator(ctx.varNameAndType(), windowVariableName, windowSequenceType);
        return null;
    }

    private void handleWindowIterator(final VarNameAndTypeContext ctx, final String windowVariableName,
            final TypeInContext windowSequenceType) {
        if (ctx.typeDeclaration() != null) {
            final TypeInContext windowDeclaredVarType = visitTypeDeclaration(ctx.typeDeclaration());
            if (!windowDeclaredVarType.isSubtypeOf(windowSequenceType)) {
                error(ctx, ErrorType.WINDOW__DECLARATION_MISMATCH, List.of(windowDeclaredVarType, windowSequenceType));
            }
            declareVariable(windowDeclaredVarType, windowVariableName, ctx.varName());
        } else {
            declareVariable(windowSequenceType, windowVariableName, ctx.varName());
        }
    }

    private void handleWindowStartClause(
        final WindowStartConditionContext windowStartCondition,
        final TypeInContext iterator,
        final TypeInContext optionalIterator)
    {
        if (windowStartCondition != null) {
            final var windowVars = windowStartCondition.windowVars();
            entypeWindowVariables(iterator, optionalIterator, windowVars);
            if (windowStartCondition.WHEN() != null) {
                final var conditionType = visitExprSingle(windowStartCondition.exprSingle());
                if (!conditionType.type.hasEffectiveBooleanValue()) {
                    error(
                        windowStartCondition.exprSingle(),
                        ErrorType.WINDOW__START_CLAUSE_CONDITION_NOT_EBV,
                        List.of(conditionType)
                        );
                }
            }
        }
    }

    private void handleWindowEndClause(
        final WindowEndConditionContext windowEndConditionContext,
        final TypeInContext iterator,
        final TypeInContext optionalIterator)
    {
        if (windowEndConditionContext != null) {
            final var windowVars = windowEndConditionContext.windowVars();
            entypeWindowVariables(iterator, optionalIterator, windowVars);
            if (windowEndConditionContext.WHEN() != null) {
                final var conditionType = visitExprSingle(windowEndConditionContext.exprSingle());
                if (!conditionType.type.hasEffectiveBooleanValue()) {
                    error(
                        windowEndConditionContext.exprSingle(),
                        ErrorType.WINDOW__END_CLAUSE_CONDITION_NOT_EBV,
                        List.of(conditionType)
                        );
                }
            }
        }
    }

    @Override
    public TypeInContext visitGroupByClause(final GroupByClauseContext ctx) {
        final List<String> groupingVars = new ArrayList<>(ctx.groupingSpec().size());
        final List<VarNameContext> groupingVarsCtx = new ArrayList<>(ctx.groupingSpec().size());
        for (final var gs : ctx.groupingSpec()) {
            if (gs.exprSingle() != null) {
                declareVariable(gs, gs.varNameAndType(), gs.exprSingle());
            } else {
                final VarNameContext varName2 = gs.varNameAndType().varName();
                final String varname = varName2.qname().getText();
                TypeInContext variableType = contextManager.getVariable(varname);
                if (variableType == null) {
                    error(
                        varName2,
                        ErrorType.GROUP_BY__UNDEFINED_GROUPING_VARIABLE,
                        List.of(varname)
                        );
                    variableType = contextManager.typeInContext(zeroOrMoreItems);
                }
                final XQuerySequenceType atomizedType = atomizer.atomize(variableType.type);
                if (!atomizedType.isSubtypeOf(zeroOrOneItem)) {
                    error(
                        varName2,
                        ErrorType.GROUP_BY__WRONG_GROUPING_VAR_TYPE,
                        List.of(varname, zeroOrOneItem, atomizedType)
                        );

                }
                declareVariable(contextManager.typeInContext(atomizedType.iteratorType()), varname, varName2);
                if (groupingVars.contains(varname)) {
                    error(varName2, ErrorType.GROUP_BY__DUPLICATED_VAR, List.of(varname));
                } else {
                    groupingVars.add(varname);
                    groupingVarsCtx.add(varName2);
                }
            }
        }
        final Set<Entry<String, TypeInContext>> variablesInContext = contextManager.currentContext().getVariables().entrySet();
        int i = 0;
        for (final var variableNameAndType : variablesInContext) {
            final String varName = variableNameAndType.getKey();
            if (groupingVars.contains(varName)) {
                continue;
            }
            final var varType = variableNameAndType.getValue();
            declareVariable(contextManager.typeInContext(varType.type.addOptionality()), varName, groupingVarsCtx.get(i));
            i++;
        }
        return null;
    }

    public void processForItemBinding(final ForItemBindingContext ctx) {
        final String variableName = ctx.varNameAndType().varName().qname().getText();
        final TypeInContext sequenceType = ctx.exprSingle().accept(this);
        returnedOccurrence = mergeFLWOROccurrence(sequenceType.type);

        checkPositionalVariableDistinct(ctx.positionalVar(), variableName, ctx);

        final XQueryItemType itemType = sequenceType.type.itemType;
        final XQuerySequenceType iteratorType = (ctx.allowingEmpty() != null)
                ? typeFactory.zeroOrOne(itemType)
                : typeFactory.one(itemType);

        processVariableTypeDeclaration(ctx.varNameAndType(), contextManager.typeInContext(iteratorType), variableName, ctx);

        if (ctx.positionalVar() != null) {
            final String positionalVariableName = ctx.positionalVar().varName().qname().getText();
            declareVariable(contextManager.typeInContext(number), positionalVariableName, ctx.positionalVar().varName());
        }
    }

    public void processForMemberBinding(final ForMemberBindingContext ctx) {
        final String variableName = ctx.varNameAndType().varName().qname().getText();
        final TypeInContext arrayType = ctx.exprSingle().accept(this);
        returnedOccurrence = arrayMergeFLWOROccurence();

        if (!arrayType.type.isSubtypeOf(anyArray)) {
            error(ctx, ErrorType.FOR_MEMBER__WRONG_ITERABLE_TYPE, List.of(arrayType));
        }

        checkPositionalVariableDistinct(ctx.positionalVar(), variableName, ctx);

        final XQuerySequenceType memberType = arrayType.type.itemType.arrayMemberType;

        processVariableTypeDeclaration(ctx.varNameAndType(), contextManager.typeInContext(memberType), variableName, ctx);

        if (ctx.positionalVar() != null) {
            final String positionalVariableName = ctx.positionalVar().varName().qname().getText();
            declareVariable(contextManager.typeInContext(number), positionalVariableName, ctx.positionalVar().varName());
        }
    }

    public void processForEntryBinding(final ForEntryBindingContext ctx) {
        final TypeInContext mapType = ctx.exprSingle().accept(this);
        returnedOccurrence = arrayMergeFLWOROccurence();

        if (!mapType.type.isSubtypeOf(anyMap)) {
            error(
                ctx,
                ErrorType.FOR_ENTRY__WRONG_ITERABLE_TYPE,
                List.of());
            return;
        }

        final ForEntryKeyBindingContext keyBinding = ctx.forEntryKeyBinding();
        final ForEntryValueBindingContext valueBinding = ctx.forEntryValueBinding();

        // Check for duplicate key and value variable names
        if (keyBinding != null && valueBinding != null) {
            final String keyVarName = keyBinding.varNameAndType().varName().qname().getText();
            final String valueVarName = valueBinding.varNameAndType().varName().qname().getText();
            if (keyVarName.equals(valueVarName)) {
                error(ctx, ErrorType.FOR_ENTRY__KEY_VALUE_VARS_DUPLICATED_NAME, List.of());
            }
        }

        // Process key binding
        if (keyBinding != null) {
            final String keyVariableName = keyBinding.varNameAndType().varName().qname().getText();
            final XQueryItemType keyType = mapType.type.itemType.mapKeyType;
            final XQuerySequenceType keyIteratorType = typeFactory.one(keyType);

            checkPositionalVariableDistinct(ctx.positionalVar(), keyVariableName, ctx);
            processVariableTypeDeclaration(keyBinding.varNameAndType(), contextManager.typeInContext(keyIteratorType), keyVariableName, ctx);
        }

        // Process value binding
        if (valueBinding != null) {
            final String valueVariableName = valueBinding.varNameAndType().varName().qname().getText();
            final XQuerySequenceType valueType = mapType.type.itemType.mapValueType;

            checkPositionalVariableDistinct(ctx.positionalVar(), valueVariableName, ctx);
            processVariableTypeDeclaration(valueBinding.varNameAndType(), contextManager.typeInContext(valueType), valueVariableName, ctx);
        }

        if (ctx.positionalVar() != null) {
            final String positionalVariableName = ctx.positionalVar().varName().qname().getText();
            declareVariable(contextManager.typeInContext(number), positionalVariableName, ctx.positionalVar().varName());
        }
    }

    private void checkPositionalVariableDistinct(final PositionalVarContext positionalVar,
                                            final String mainVariableName,
                                            final ParserRuleContext context)
    {
        if (positionalVar != null) {
            final String positionalVariableName = positionalVar.varName().qname().getText();
            if (mainVariableName.equals(positionalVariableName)) {
                error(context, ErrorType.FOR_ENTRY__POSITIONAL_VARIABLE_SAME_AS_MAIN_VARIABLE_NAME, List.of());
            }
        }
    }

    protected void processVariableTypeDeclaration(final VarNameAndTypeContext varNameAndType,
                                            final TypeInContext inferredType,
                                            final String variableName,
                                            final ParseTree context)
    {
        if (varNameAndType.typeDeclaration() == null) {
            declareVariable(inferredType, variableName, varNameAndType.varName());
            return;
        }

        final TypeInContext declaredType = visitTypeDeclaration(varNameAndType.typeDeclaration());
        if (!inferredType.isSubtypeOf(declaredType)) {
            error(
                (ParserRuleContext)context,
                ErrorType.VARIABLE_DECLARATION__ASSIGNED_TYPE_INCOMPATIBLE,
                List.of(variableName, inferredType, declaredType)
                );
        }
        declareVariable(declaredType, variableName, varNameAndType.varName());
    }


    @Override
    public TypeInContext visitSequenceType(final SequenceTypeContext ctx)
    {
        if (ctx.emptySequence() != null) {
            return contextManager.typeInContext(emptySequence);
        }
        final var itemType = ctx.itemType().accept(this).type.itemType;
        if (ctx.occurrenceIndicator() == null) {
            return contextManager.typeInContext(typeFactory.one(itemType));
        }
        return switch (ctx.occurrenceIndicator().getText()) {
            case "?" -> contextManager.typeInContext(typeFactory.zeroOrOne(itemType));
            case "*" -> contextManager.typeInContext(typeFactory.zeroOrMore(itemType));
            case "+" -> contextManager.typeInContext(typeFactory.oneOrMore(itemType));
            default -> null;
        };
    }

    @Override
    public TypeInContext visitAnyItemTest(final AnyItemTestContext ctx)
    {
        return contextManager.typeInContext(typeFactory.anyItem());
    }

    @Override
    public TypeInContext visitChoiceItemType(final ChoiceItemTypeContext ctx)
    {
        final List<ItemTypeContext> itemTypes = ctx.itemType();
        if (itemTypes.size() == 1) {
            return ctx.itemType(0).accept(this);
        }
        final var choiceItemNames = itemTypes.stream().map(i -> i.getText()).collect(Collectors.toSet());
        if (choiceItemNames.size() != itemTypes.size()) {
            error(ctx, ErrorType.CHOICE_ITEM_TYPE__DUPLICATED, List.of());
        }
        final List<XQueryItemType> choiceItems = itemTypes.stream().map(i -> i.accept(this))
            .map(sequenceType -> sequenceType.type.itemType)
            .toList();
        return contextManager.typeInContext(typeFactory.choice(choiceItems));
    }

    @Override
    public TypeInContext visitTypeName(final TypeNameContext ctx)
    {
        final var name = ctx.getText();
        final XQuerySequenceType result = switch (name) {
            case "number" -> number;
            case "string" -> string;
            case "boolean" -> boolean_;
            default -> {
                final var visitedQualifiedName = namespaceResolver.resolveType(name);
                final var type = typeFactory.namedType(visitedQualifiedName);
                if (type.status() == NamedAccessingStatus.OK)
                    yield type.type();

                for (final QualifiedName resolvedName : recordsMapped.keySet()) {
                    if (resolvedName.equals(visitedQualifiedName)) {
                        final var namedRecordResult = resolveRecordFromUnresolved(resolvedName, recordsMapped.get(resolvedName));
                        yield typeFactory.one(namedRecordResult.recordItemType);
                    }
                }
                for (final var resolved : itemsMapped.keySet()) {
                    if (resolved.equals(visitedQualifiedName)) {
                        final var t = resolveItemTypeFromDecl(resolved, itemsMapped.get(resolved));
                        yield typeFactory.one(t.registered());
                    }
                }

                error(ctx, ErrorType.TYPE_NAME__UNKNOWN, List.of(name));
                yield zeroOrMoreItems;
            }
        };
        return contextManager.typeInContext(result);
    }

    @Override
    public TypeInContext visitAnyKindTest(final AnyKindTestContext ctx)
    {
        return contextManager.typeInContext(typeFactory.anyNode());
    }

    @Override
    public TypeInContext visitElementTest(final ElementTestContext ctx)
    {
        final Set<QualifiedName> elementNames = ctx.nameTestUnion().nameTest().stream().map(e -> namespaceResolver.resolveElement(e.getText()))
            .collect(Collectors.toSet());
        return contextManager.typeInContext(typeFactory.element(elementNames));
    }

    @Override
    public TypeInContext visitFunctionType(final FunctionTypeContext ctx)
    {
        if (ctx.anyFunctionType() != null) {
            return contextManager.typeInContext(typeFactory.anyFunction());
        }
        final var func = ctx.typedFunctionType();
        final List<XQuerySequenceType> parameterTypes = func.typedFunctionParam().stream()
            .map(p -> visitSequenceType(p.sequenceType()).type)
            .collect(Collectors.toList());
        final var function =  typeFactory.function(visitSequenceType(func.sequenceType()).type, parameterTypes);
        return contextManager.typeInContext(function);
    }

    @Override
    public TypeInContext visitMapType(final MapTypeContext ctx)
    {
        if (ctx.anyMapType() != null) {
            return contextManager.typeInContext(typeFactory.anyMap());
        }
        final var map = ctx.typedMapType();
        final XQueryItemType keyType = map.itemType().accept(this).type.itemType;
        final TypeInContext valueType = visitSequenceType(map.sequenceType());
        return contextManager.typeInContext(typeFactory.map(keyType, valueType.type));
    }

    @Override
    public TypeInContext visitArrayType(final ArrayTypeContext ctx)
    {
        if (ctx.anyArrayType() != null) {
            return contextManager.typeInContext(typeFactory.anyArray());
        }
        final var array = ctx.typedArrayType();
        final var sequenceType = visitSequenceType(array.sequenceType());
        return contextManager.typeInContext(typeFactory.array(sequenceType.type));
    }

    @Override
    public TypeInContext visitRecordType(final RecordTypeContext ctx)
    {
        if (ctx.anyRecordType() != null) {
            return contextManager.typeInContext(typeFactory.anyMap());
        }
        final var record = ctx.typedRecordType();
        final var fieldDeclarations = record.fieldDeclaration();
        final Map<String, XQueryRecordField> fields = new HashMap<>(fieldDeclarations.size());
        for (final var field : fieldDeclarations) {
            final String fieldName = field.fieldName().getText();
            final var fieldType = visitSequenceType(field.sequenceType());
            final boolean isRequired = field.QUESTION_MARK() != null;
            final XQueryRecordField recordField = new XQueryRecordField(TypeOrReference.type(fieldType.type), isRequired);
            fields.put(fieldName, recordField);
        }
        if (record.extensibleFlag() == null) {
            return contextManager.typeInContext(typeFactory.extensibleRecord(fields));
        }
        return contextManager.typeInContext(typeFactory.record(fields));
    }

    @Override
    public TypeInContext visitEnumerationType(final EnumerationTypeContext ctx)
    {
        final Set<String> enumMembers = ctx.STRING().stream()
            .map(TerminalNode::getText)
            .map(s->s.substring(1, s.length()-1))
            .collect(Collectors.toSet());
        return contextManager.typeInContext(typeFactory.enum_(enumMembers));
    }

    @Override
    public TypeInContext visitCountClause(final CountClauseContext ctx)
    {
        declareVariable(contextManager.typeInContext(number), ctx.varName());
        return contextManager.typeInContext(number);
    }

    @Override
    public TypeInContext visitWhereClause(final WhereClauseContext ctx)
    {
        final var filteringExpression = ctx.exprSingle();
        final var filteringExpressionType = filteringExpression.accept(this);
        if (!filteringExpressionType.type.hasEffectiveBooleanValue()) {
            error(filteringExpression, ErrorType.FILTERING__EXPR_NOT_EBV, List.of(filteringExpressionType));
        }
        returnedOccurrence = addOptionality(returnedOccurrence);
        return null;
    }

    @Override
    public TypeInContext visitVarRef(final VarRefContext ctx)
    {
        final String variableName = ctx.qname().getText();
        final TypeInContext variableType = contextManager.getVariable(variableName);
        if (variableType == null) {
            error(ctx, ErrorType.VAR_REF__UNDECLARED, List.of(variableName));
            return contextManager.typeInContext(zeroOrMoreItems);
        } else {
            for (final var l : listeners) {
                l.onVariableReference(ctx, variableType);
            }
            return variableType;
        }
    }

    private static final int[][] OCCURRENCE_MERGE_AUTOMATA = {
        // returnedOccurrence 0 (Zero)
        { 0, 0, 0, 0, 0 },
        // returnedOccurrence 1 (One)
        { 0, 1, 2, 3, 4 },
        // returnedOccurrence 2 (ZeroOrOne)
        { 0, 2, 2, 3, 3 },
        // returnedOccurrence 3 (ZeroOrMore)
        { 0, 3, 3, 3, 3 },
        // returnedOccurrence 4 (OneOrMore/Other)
        { 0, 4, 3, 3, 4 }
    };

    private int occurrence(final XQuerySequenceType type)
    {
        if (type.isZero)
            return 0;
        if (type.isOne)
            return 1;
        if (type.isZeroOrOne)
            return 2;
        if (type.isZeroOrMore)
            return 3;
        return 4;
    }

    private int mergeFLWOROccurrence(final XQuerySequenceType type)
    {
        final int typeOccurrence = occurrence(type);
        return OCCURRENCE_MERGE_AUTOMATA[returnedOccurrence][typeOccurrence];
    }

    private int arrayMergeFLWOROccurence() {
        if (returnedOccurrence == 0)
            return 0;
        return 3;
    }

    @Override
    public TypeInContext visitReturnClause(final ReturnClauseContext ctx)
    {
        final var type = ctx.exprSingle().accept(this);
        final var itemType = type.type.itemType;
        returnedOccurrence = mergeFLWOROccurrence(type.type);
        final var sequenceType = switch (returnedOccurrence) {
            case 0 -> emptySequence;
            case 1 -> typeFactory.one(itemType);
            case 2 -> typeFactory.zeroOrOne(itemType);
            case 3 -> typeFactory.zeroOrMore(itemType);
            default -> typeFactory.oneOrMore(itemType);
        };
        return contextManager.typeInContext(sequenceType);
    }

    @Override
    public TypeInContext visitWhileClause(final WhileClauseContext ctx)
    {
        final var filteringExpression = ctx.exprSingle();
        final var filteringExpressionType = filteringExpression.accept(this);
        if (!filteringExpressionType.type.hasEffectiveBooleanValue()) {
            error(filteringExpression, ErrorType.FILTERING__EXPR_NOT_EBV, List.of(filteringExpressionType));
        }
        returnedOccurrence = addOptionality(returnedOccurrence);
        return null;
    }

    private int addOptionality(final int occurence)
    {
        return switch (occurence) {
            case 0 -> 0;
            case 1 -> 2;
            case 2 -> 2;
            default -> 3;
        };
    }

    @Override
    public TypeInContext visitLiteral(final LiteralContext ctx)
    {
        if (ctx.STRING() != null) {
            return handleString(ctx);
        }

        final var numeric = ctx.numericLiteral();
        if (numeric.IntegerLiteral() != null) {
            return handleNumber(numeric);
        }

        if (numeric.HexIntegerLiteral() != null) {
            final String raw = numeric.HexIntegerLiteral().getText();
            final String hex = raw.replace("_", "").substring(2);
            valueFactory.number(new BigDecimal(new java.math.BigInteger(hex, 16)));
            return contextManager.typeInContext(number);
        }

        if (numeric.BinaryIntegerLiteral() != null) {
            final String raw = numeric.BinaryIntegerLiteral().getText();
            final String binary = raw.replace("_", "").substring(2);
            valueFactory.number(new BigDecimal(new java.math.BigInteger(binary, 2)));
            return contextManager.typeInContext(number);
        }

        if (numeric.DecimalLiteral() != null) {
            final String cleaned = numeric.DecimalLiteral().getText().replace("_", "");
            valueFactory.number(new BigDecimal(cleaned));
            return contextManager.typeInContext(number);
        }

        if (numeric.DoubleLiteral() != null) {
            final String cleaned = numeric.DoubleLiteral().getText().replace("_", "");
            valueFactory.number(new BigDecimal(cleaned));
            return contextManager.typeInContext(number);
        }
        return null;
    }

    private TypeInContext handleNumber(final TerminalNode numeric) {
        final String value = numeric.getText().replace("_", "");
        valueFactory.number(new BigDecimal(value));
        return contextManager.typeInContext(number);
    }

    private TypeInContext handleNumber(final NumericLiteralContext numeric) {
        final String value = numeric.IntegerLiteral().getText().replace("_", "");
        valueFactory.number(new BigDecimal(value));
        return contextManager.typeInContext(number);
    }

    private TypeInContext handleString(final ParserRuleContext ctx) {
        final String content = processStringLiteral(ctx);
        return contextManager.typeInContext(typeFactory.enum_(Set.of(content)));
    }

    private String processStringLiteral(final ParserRuleContext ctx) {
        final String rawText = ctx.getText();
        final String content = unescapeString(ctx, rawText.substring(1, rawText.length() - 1));
        valueFactory.string(content);
        return content;
    }

    @Override
    public TypeInContext visitParenthesizedExpr(final ParenthesizedExprContext ctx)
    {
        // Empty parentheses mean an empty sequence '()'
        if (ctx.expr() == null) {
            valueFactory.sequence(List.of());
            return contextManager.typeInContext(emptySequence);
        }
        return ctx.expr().accept(this);
    }

    @Override
    public TypeInContext visitExpr(final ExprContext ctx)
    {
        // Only one expression
        // e.g. 13
        if (ctx.exprSingle().size() == 1) {
            return ctx.exprSingle(0).accept(this);
        }
        // More than one expression
        final var previousExpr = ctx.exprSingle(0);
        var previousExprType = visitExprSingle(previousExpr).type;
        final int size = ctx.exprSingle().size();
        for (int i = 1; i < size; i++) {
            final var exprSingle = ctx.exprSingle(i);
            final TypeInContext expressionType = exprSingle.accept(this);
            previousExprType = previousExprType.sequenceMerge(expressionType.type);
        }
        return contextManager.typeInContext(previousExprType);
    }

    private String unescapeString(final ParserRuleContext where, final String str)
    {
        final var charEscaper = new XQuerySemanticCharEscaper();
        final XQuerySemanticCharEscaperResult result = charEscaper.escapeWithDiagnostics(where, str);
        errors.addAll(result.errors());
        return result.unescaped();
    }

    private NamespaceResolver namespaceResolver = new NamespaceResolver("fn", "", "", "", "");

    @Override
    public TypeInContext visitFunctionCall(final FunctionCallContext ctx)
    {
        final var savedArgs = saveVisitedArguments();
        final var savedKwargs = saveVisitedKeywordArguments();

        ctx.argumentList().accept(this);

        final TypeInContext callAnalysisResult = callFunction(
            ctx,
            ctx.functionName().getText(),
            visitedPositionalArguments,
            visitedKeywordArguments);

        visitedPositionalArguments = savedArgs;
        visitedKeywordArguments = savedKwargs;
        return callAnalysisResult;
    }

    private TypeInContext callFunction(
        final ParserRuleContext ctx,
        final String functionQname,
        final List<TypeInContext> args,
        final Map<String, TypeInContext> kwargs
    )
    {
        final String fullName = functionQname;
        final var resolution = namespaceResolver.resolveFunction(fullName);
        final String namespace = resolution.namespace();
        final String functionName = resolution.name();

        final AnalysisResult callAnalysisResult = symbolManager.call(
            ctx, namespace, functionName, args,
            kwargs, context, contextManager.currentContext());
        errors.addAll(callAnalysisResult.errors());
        return callAnalysisResult.result();
    }





    @Override
    public TypeInContext visitQuantifiedExpr(final QuantifiedExprContext ctx) {
        final List<QuantifierBindingContext> quantifierBindings = ctx.quantifierBinding();

        final List<String> variableNames = quantifierBindings.stream()
                .map(binding -> binding.varNameAndType().varName().qname().getText())
                .toList();

        final List<VarNameContext> variableNameCtxs = quantifierBindings.stream()
                .map(binding -> binding.varNameAndType().varName())
                .toList();

        final List<XQuerySequenceType> coercedTypes = quantifierBindings.stream()
                .map(binding -> {
                    final TypeDeclarationContext typeDeclaration = binding.varNameAndType().typeDeclaration();
                    return typeDeclaration != null? typeDeclaration.accept(this).type : null;
                })
                .toList();

        final List<XQuerySequenceType> variableTypes = quantifierBindings.stream()
                .map(binding -> binding.exprSingle().accept(this).type)
                .toList();

        final ExprSingleContext criterionNode = ctx.exprSingle();

        for (int i = 0; i < variableNames.size(); i++) {
            final var assignedType = variableTypes.get(i);
            final var desiredType = coercedTypes.get(i);
            if (desiredType !=null) {
                if (assignedType.coerceableTo(desiredType) == RelativeCoercability.NEVER){
                    error(
                        ctx.quantifierBinding(i).varNameAndType(),
                        ErrorType.VAR_DECL_WITH_COERSION__INVALID,
                        List.of(assignedType, desiredType));
                }
                declareVariable(contextManager.typeInContext(desiredType), variableNames.get(i), variableNameCtxs.get(i));
                continue;
            }
            declareVariable(contextManager.typeInContext(assignedType), variableNames.get(i), variableNameCtxs.get(i));
        }

        final XQuerySequenceType queriedType = criterionNode.accept(this).type;
        if (!queriedType.hasEffectiveBooleanValue()) {
            error(criterionNode, ErrorType.QUANTIFIED__CRITERION_NON_EBV, List.of(queriedType));
        }

        return contextManager.typeInContext(boolean_);
    }

    @Override
    public TypeInContext visitOrExpr(final OrExprContext ctx)
    {
        if (ctx.OR().isEmpty()) {
            return ctx.andExpr(0).accept(this);
        }
        final var orCount = ctx.OR().size();
        for (int i = 0; i <= orCount; i++) {
            final var visitedType = ctx.andExpr(i).accept(this);
            if (!visitedType.type.hasEffectiveBooleanValue()) {
                error(ctx.andExpr(i), ErrorType.OR__NON_EBV, List.of(visitedType));
            }
        }
        final var andBool = contextManager.typeInContext(boolean_);
        // contextManager.currentScope().imply(andBool);
        return andBool;
    }

    @Override
    public TypeInContext visitRangeExpr(final RangeExprContext ctx)
    {
        if (ctx.TO() == null) {
            return ctx.additiveExpr(0).accept(this);
        }
        final var fromValue = ctx.additiveExpr(0).accept(this);
        final var toValue = ctx.additiveExpr(1).accept(this);
        final boolean validFrom = fromValue.type.isSubtypeOf(optionalNumber);
        final boolean validTo = toValue.type.isSubtypeOf(optionalNumber);
        if (!validFrom && !validTo) {
            error(ctx.additiveExpr(0), ErrorType.RANGE__INVALID_BOTH, List.of(fromValue, toValue));
        } else if (!validFrom) {
            error(ctx.additiveExpr(0), ErrorType.RANGE__INVALID_FROM, List.of(fromValue));
        } else if (!validTo) {
            error(ctx.additiveExpr(1), ErrorType.RANGE__INVALID_TO, List.of(toValue));
        }
        return contextManager.typeInContext(anyNumbers);
    }

    @Override
    public TypeInContext visitPathExpr(final PathExprContext ctx)
    {
        final boolean pathExpressionFromRoot = ctx.SLASH() != null;
        if (pathExpressionFromRoot) {
            final var savedAxis = saveAxis();
            contextTypeMustBeAnyNodes(ctx);
            currentAxis = XQueryAxis.CHILD;
            final var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        final boolean useDescendantOrSelfAxis = ctx.SLASHES() != null;
        if (useDescendantOrSelfAxis) {
            final var savedAxis = saveAxis();
            contextTypeMustBeAnyNodes(ctx);
            currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
            final var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        return ctx.relativePathExpr().accept(this);
    }

    @Override
    public TypeInContext visitNodeTest(final NodeTestContext ctx)
    {
        final XQuerySequenceType nodeType = context.getType().type;
        final PathOperatorResult result = getOperatorPathResultFromTree(ctx, nodeType);

        // reporting input status
        switch(result.inputStatus()) {
            case EMPTY_SEQUENCE -> warn(ctx, WarningType.PATH_OPERATOR__EMPTY_SEQUENCE, List.of());
            case NON_NODES -> error(ctx, ErrorType.PATH_OPERATOR__NOT_SEQUENCE_OF_NODES, List.of(nodeType));
            case MULTIGRAMMAR -> error(ctx, ErrorType.PATH_OPERATOR__MULTIGRAMMAR, List.of(nodeType));
            case OK -> {}
        }

        { // reporting errors in input grammars
            final var inputGrammars = result.inputGrammars();
            final Map<GrammarStatus, List<String>> inputGrammarsGroupedByStatus = inputGrammars
                .keySet()
                .stream()
                .collect(Collectors.groupingBy(inputGrammars::get));
            final var invalidInputGrammars = inputGrammarsGroupedByStatus.get(GrammarStatus.UNREGISTERED);
            if (invalidInputGrammars != null) {
                error(
                    ctx,
                    ErrorType.PATH_OPERATOR__FOUND_UNREGISTERED_GRAMMARS,
                    List.of(invalidInputGrammars));
            }
        }

        { // validating element grammars
            final var elementGrammars = result.elementGrammars();
            final Map<GrammarStatus, List<String>> elementGrammarsGroupedByStatus = elementGrammars
                .keySet()
                .stream()
                .collect(Collectors.groupingBy(elementGrammars::get));
            final var invalidElementGrammars = elementGrammarsGroupedByStatus.get(GrammarStatus.UNREGISTERED);
            if (invalidElementGrammars != null) {
                error(
                    ctx,
                    ErrorType.PATH_OPERATOR__FOUND_UNREGISTERED_GRAMMARS,
                    List.of(invalidElementGrammars));
            }

        }

        // reporting element invalid names
        if (!result.invalidElementNames().isEmpty()) {
            // TODO: move to message
            final String joinedNames = result
                .invalidElementNames()
                .stream()
                .map(QualifiedName::toString)
                .collect(Collectors.joining(", "));
            error(
                ctx,
                ErrorType.PATH_OPERATOR__UNRECOGNIZED_RULE_NAMES,
                List.of(joinedNames));
        }

        { // reporting duplicated names
            if (!result.duplicatedNames().isEmpty()) {
                warn(
                    ctx,
                    WarningType.PATH_OPERATOR__DUPLICATED_NAME,
                    List.of(result.invalidElementNames()));
            }
        }

        return contextManager.typeInContext(result.result());
    }

    private PathOperatorResult getOperatorPathResultFromTree(final NodeTestContext ctx, final XQuerySequenceType nodeType) {
        PathOperatorResult result;
        if (ctx.wildcard() != null) {
            result = pathOperator.pathOperator(nodeType, currentAxis, null, namespaceResolver);
        } else {
            final List<String> names = ctx.pathNameTestUnion().qname()
                .stream()
                .map(QnameContext::getText)
                .toList();
            result = pathOperator.pathOperator(nodeType, currentAxis, names, namespaceResolver);
        }
        return result;
    }

    /**
     * Makes sure that context type is subtype of node()*
     * If it is not, error is recorded and the value is corrected to node()*
     * @param ctx rule where the error potentially has occured
     */
    private void contextTypeMustBeAnyNodes(final PathExprContext ctx)
    {
        final XQuerySequenceType contexttype = context.getType().type;
        if (contexttype == null) {
            error(ctx, ErrorType.PATH_EXPR__CONTEXT_TYPE_ABSENT, List.of());
            context.setType(contextManager.typeInContext(anyNodes));
        } else if (!contexttype.isSubtypeOf(anyNodes)) {
            error(ctx, ErrorType.PATH_EXPR__CONTEXT_NOT_NODES, List.of(contexttype));
            context.setType(contextManager.typeInContext(anyNodes));
        }
    }

    @Override
    public TypeInContext visitRelativePathExpr(final RelativePathExprContext ctx)
    {
        if (ctx.pathOperator().isEmpty()) {
            return ctx.stepExpr(0).accept(this);
        }
        final var savedContext = saveContext();
        context.setType(savedContext.getType());
        context.setPositionType(savedContext.getPositionType());
        context.setSizeType(savedContext.getSizeType());
        TypeInContext result = visitStepExpr(ctx.stepExpr(0));
        context.setType(result);
        final var operationCount = ctx.pathOperator().size();
        for (int i = 1; i <= operationCount; i++) {
            currentAxis = (ctx.pathOperator(i-1).SLASH() != null)
                ? XQueryAxis.DESCENDANT_OR_SELF
                : XQueryAxis.CHILD;
            result = visitStepExpr(ctx.stepExpr(i));
            context.setType(result);
        }
        context = savedContext;
        return result;
    }




    @Override
    public TypeInContext visitStepExpr(final StepExprContext ctx)
    {
        if (ctx.postfixExpr() != null)
            return ctx.postfixExpr().accept(this);
        return visitAxisStep(ctx.axisStep());
    }

    @Override
    public TypeInContext visitAxisStep(final AxisStepContext ctx)
    {
        XQuerySequenceType stepResult = zeroOrMoreItems;
        if (ctx.reverseStep() != null)
            stepResult = visitReverseStep(ctx.reverseStep()).type;
        else if (ctx.forwardStep() != null)
            stepResult = visitForwardStep(ctx.forwardStep()).type;
        if (ctx.predicateList().predicate().isEmpty()) {
            return contextManager.typeInContext(stepResult);
        }
        final var savedArgs = saveVisitedArguments();
        final var savedContext = saveContext();
        context.setType(contextManager.typeInContext(stepResult.iteratorType()));
        context.setPositionType(contextManager.typeInContext(number));
        context.setSizeType(contextManager.typeInContext(number));
        for (final var predicate : ctx.predicateList().predicate()) {
            predicate.accept(this);
        }
        visitedPositionalArguments = savedArgs;
        context = savedContext;
        return contextManager.typeInContext(stepResult);
    }

    private XQueryVisitingSemanticContext saveContext() {
        final var saved = context;
        context = new XQueryVisitingSemanticContext();
        return saved;
    }


    @Override
    public TypeInContext visitFilterExpr(final FilterExprContext ctx)
    {
        final XQuerySequenceType expr = ctx.postfixExpr().accept(this).type;
        final var savedContext = saveContext();
        context.setType(contextManager.typeInContext(expr.iteratorType()));
        final var filtered = visitPredicate(ctx.predicate());
        context = savedContext;
        return filtered;
    }

    @Override
    public TypeInContext visitPredicate(final PredicateContext ctx)
    {
        final var contextType = context.getType();
        final var predicateExpression = ctx.expr().accept(this);
        final var savedContext = saveContext();
        context.setType(contextManager.typeInContext(savedContext.getType().iteratorType()));
        context.setPositionType(contextManager.typeInContext(number));
        context.setSizeType(contextManager.typeInContext(number));
        if (predicateExpression.type.isSubtypeOf(emptySequence))
            return contextManager.typeInContext(emptySequence);
        if (predicateExpression.type.isSubtypeOf(typeFactory.zeroOrOne(typeFactory.itemNumber()))) {
            final var item = contextType.type.itemType;
            final var deducedType = typeFactory.zeroOrOne(item);
            return contextManager.typeInContext(deducedType);
        }
        if (predicateExpression.type.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemNumber()))) {
            final var item = contextType.type.itemType;
            final var deducedType = typeFactory.zeroOrMore(item);
            final TypeInContext deducedInContext = contextManager.typeInContext(deducedType);
            context.setType(deducedInContext);
            return deducedInContext;
        }
        if (!predicateExpression.type.hasEffectiveBooleanValue()) {
            error(ctx.expr(), ErrorType.PREDICATE__NON_EBV, List.of(predicateExpression));
        }
        context = savedContext;
        return contextManager.typeInContext(contextType.type.addOptionality());
    }

    @Override
    public TypeInContext visitDynamicFunctionCall(final DynamicFunctionCallContext ctx) {
        final var savedArgs = saveVisitedArguments();
        final var savedContext = saveContext();
        context.setType(savedContext.getType());
        context.setPositionType(contextManager.typeInContext(number));
        context.setSizeType(contextManager.typeInContext(number));
        final XQuerySequenceType value = ctx.postfixExpr().accept(this).type;
        final boolean isCallable = value.isSubtypeOf(typeFactory.anyFunction());
        if (!isCallable) {
            error(ctx.postfixExpr(), ErrorType.PREDICATE__NON_EBV, List.of(value));
        }
        ctx.positionalArgumentList().accept(this);
        visitedPositionalArguments = savedArgs;


        context = savedContext;

        if (isCallable)
            return contextManager.typeInContext(value.itemType.returnedType);
        else
            return contextManager.typeInContext(typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }



    @Override
    public TypeInContext visitLookupExpr(final LookupExprContext ctx) {
        final var targetType = ctx.postfixExpr().accept(this);
        final TypeInContext keySpecifierType = getKeySpecifier(ctx);
        final LookupContext lookup = ctx.lookup();
        final var lookupType = typecheckLookup(ctx, lookup, lookup.keySpecifier(), targetType, keySpecifierType);
        return contextManager.typeInContext(lookupType);
    }


    private XQuerySequenceType typecheckLookup(
        final ParserRuleContext ctx,
        final LookupContext lookup,
        final KeySpecifierContext keySpecifier,
        final TypeInContext targetType,
        final TypeInContext keySpecifierType)
    {
        if (targetType.type.isZero) {
            warn(ctx, WarningType.LOOKUP__TARGET_EMPTY, List.of());
            return emptySequence;
        }
        final boolean isWildcard = keySpecifierType == null;
        if (!isWildcard && keySpecifierType.type.isZero) {
            warn(ctx, WarningType.LOOKUP__KEY_EMPTY, List.of());
            return emptySequence;
        }

        if (!targetType.isSubtypeOf(anyArrayOrMap)) {
            error(ctx, ErrorType.LOOKUP__INVALID_TARGET, List.of(targetType));
            return zeroOrMoreItems;
        }

        switch (targetType.type.itemType.type) {
            case ARRAY -> {
                final XQuerySequenceType targetItemType = targetType.type.itemType.arrayMemberType;
                if (targetItemType == null)
                    return zeroOrMoreItems;
                final XQuerySequenceType result = targetItemType.sequenceMerge(targetItemType).addOptionality();
                if (isWildcard) {
                    return result;
                }
                if (!keySpecifierType.type.itemtypeIsSubtypeOf(zeroOrMoreNumbers))
                {
                    error(lookup, ErrorType.LOOKUP__ARRAY_INVALID_KEY, List.of(targetType));
                }
                return result;
            }
            case ANY_ARRAY -> {
                if (isWildcard) {
                    return zeroOrMoreItems;
                }
                if (!keySpecifierType.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemNumber())))
                {
                    error(lookup, ErrorType.LOOKUP__ARRAY_INVALID_KEY, List.of(targetType, keySpecifierType));
                }
                return zeroOrMoreItems;
            }
            case MAP -> {
                return getMapLookuptype(ctx, lookup, keySpecifier, targetType, keySpecifierType, isWildcard);
            }
            case EXTENSIBLE_RECORD -> {
                return getExtensibleRecordLookupType(ctx, lookup, keySpecifier,targetType, keySpecifierType, isWildcard);
            }
            case RECORD -> {
                return getRecordLookupType(ctx, lookup, keySpecifier,targetType, keySpecifierType, isWildcard);
            }
            case ANY_MAP -> {
                return zeroOrMoreItems;
            }
            default -> {
                return getAnyArrayOrMapLookupType(lookup, isWildcard, targetType, keySpecifierType);
            }
        }
    }


    XQuerySequenceType getAnyArrayOrMapLookupType(
        final LookupContext ctx,
        final boolean isWildcard,
        final TypeInContext targetType,
        final TypeInContext keySpecifierType)
    {
        if (isWildcard) {
            return null;
        }
        final XQueryItemType targetItemType = targetType.type.itemType;
        final Collection<XQueryItemType> choiceItemTypes = targetItemType.itemTypes;
        XQueryItemType targetKeyItemType = null;
        XQuerySequenceType resultingType = null;
        for (final var itemType : choiceItemTypes) {
            if (resultingType == null) {
                if (!isWildcard)
                    resultingType = switch(keySpecifierType.type.occurence) {
                        case ONE -> typeFactory.zeroOrOne(itemType);
                        default -> typeFactory.zeroOrMore(itemType);
                    };
                else {
                    resultingType = typeFactory.zeroOrMore(itemType);
                }
                continue;
            }

            switch (itemType.type) {
                case ARRAY -> {
                    resultingType = resultingType.alternativeMerge(itemType.arrayMemberType);
                    targetKeyItemType = targetItemType.alternativeMerge(typeFactory.itemNumber());
                }
                case MAP -> {
                    resultingType = resultingType.alternativeMerge(itemType.mapValueType);
                    targetKeyItemType = targetItemType.alternativeMerge(itemType.mapKeyType);
                }
                default -> {
                    resultingType = zeroOrMoreItems;
                    targetKeyItemType = typeFactory.itemAnyItem();
                }
            }
        }
        resultingType = resultingType.addOptionality();
        if (isWildcard) {
            return resultingType;
        }
        final XQueryItemType numberOrKey = targetKeyItemType.alternativeMerge(typeFactory.itemNumber());

        final XQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(numberOrKey);
        if (!keySpecifierType.itemtypeIsSubtypeOf(expectedKeyItemtype)) {
            error(ctx, ErrorType.LOOKUP__ARRAY_OR_MAP_INVALID_KEY, List.of(targetType, expectedKeyItemtype));
        }
        return resultingType;
    }

    @Override
    public TypeInContext visitUnaryLookup(final UnaryLookupContext ctx) {
        final var contextType = context.getType();
        final var keySpecifierType = visitKeySpecifier(ctx.lookup().keySpecifier());
        final var lookupType =  typecheckLookup(ctx, ctx.lookup(), ctx.lookup().keySpecifier(), contextType, keySpecifierType);
        return contextManager.typeInContext(lookupType);
    }



    private  XQuerySequenceType getMapLookuptype(
            final ParserRuleContext target,
            final LookupContext lookup,
            final KeySpecifierContext keySpecifier,
            final TypeInContext targetType,
            final TypeInContext keySpecifierType,
            final boolean isWildcard)
    {
        final XQueryItemType targetKeyItemType = targetType.type.itemType.mapKeyType;
        final XQuerySequenceType targetValueType = targetType.type.itemType.mapValueType;
        final XQueryItemType targetValueItemtype = targetValueType.itemType;
        if (isWildcard) {
            return typeFactory.zeroOrMore(targetValueItemtype);
        }
        final XQuerySequenceType result = switch(keySpecifierType.type.occurence) {
                case ONE -> typeFactory.zeroOrOne(targetValueItemtype);
                default -> typeFactory.zeroOrMore(targetValueItemtype);
            };
        final XQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
        if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
            error(lookup, ErrorType.LOOKUP__MAP_INVALID_KEY, List.of(targetType, expectedKeyItemtype));
        }
        if (targetValueItemtype.type == XQueryTypes.RECORD) {
            return result;
        }
        return result.addOptionality();
    }

    private XQuerySequenceType getRecordLookupType(
        final ParserRuleContext target,
        final LookupContext lookup,
        final KeySpecifierContext keySpecifier,
        final TypeInContext targetType,
        final TypeInContext keySpecifierType,
        final boolean isWildcard)
    {
        final XQueryItemType targetKeyItemType = typeFactory.itemString();
        final Map<String, XQueryRecordField> recordFields = targetType.type.itemType.recordFields;
        if (recordFields.isEmpty()) {
            warn(target, WarningType.LOOKUP__EMPTY_RECORD, List.of());
            return emptySequence;
        }
        final XQuerySequenceType mergedRecordFieldTypes = recordFields
            .values()
            .stream()
            .map(this::resolveRecordFieldType)
            .reduce((x, y)->x.alternativeMerge(y))
            .get();
        if (isWildcard) {
            return mergedRecordFieldTypes;
        }
        if (!keySpecifierType.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemString()))) {
            error(keySpecifier, ErrorType.LOOKUP__INVALID_RECORD_KEY_TYPE, List.of(targetType, keySpecifierType));
            return zeroOrMoreItems;
        }
        final var string = keySpecifier.STRING();
        if (string != null) {
            final String key = processStringLiteral(keySpecifier);
            final var valueType = recordFields.get(key);
            if (valueType == null) {
                error(keySpecifier, ErrorType.LOOKUP__INVALID_RECORD_KEY_NAME, List.of(key, targetType));
                return zeroOrMoreItems;
            }
            return resolveRecordFieldType(valueType);
        }
        final XQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
        if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
            error(lookup, ErrorType.LOOKUP__INVALID_RECORD_KEY_TYPE, List.of(targetType, expectedKeyItemtype));
        }
        if (keySpecifierType.type.itemType.type == XQueryTypes.ENUM) {
            final var members = keySpecifierType.type.itemType.enumMembers;
            final var firstField = members.stream().findFirst().get();
            final var firstRecordField = recordFields.get(firstField);
            XQuerySequenceType merged = resolveRecordFieldType(firstRecordField);
            for (final var member : members) {
                if (member.equals(firstField))
                    continue;
                final var recordField = recordFields.get(member);
                if (recordField == null) {
                    warn(lookup, WarningType.LOOKUP__IMPOSSIBLE_RECORD_FIELD, List.of(member));
                    return zeroOrMoreItems;
                }
                merged = merged.sequenceMerge(resolveRecordFieldType(recordField));
            }
            return merged;
        }
        return mergedRecordFieldTypes.addOptionality();
    }

    private XQuerySequenceType getExtensibleRecordLookupType(
        final ParserRuleContext ctx,
        final LookupContext lookup,
        final KeySpecifierContext keySpecifier,
        final TypeInContext targetType,
        final TypeInContext keySpecifierType,
        final boolean isWildcard)
    {
        final XQueryItemType targetKeyItemType = typeFactory.itemString();
        final Map<String, XQueryRecordField> recordFields = targetType.type.itemType.recordFields;
        if (recordFields.isEmpty()) {
            warn(ctx, WarningType.LOOKUP__RETURNS_ALWAYS_EMPTY, List.of());
            return emptySequence;
        }
        final XQuerySequenceType mergedRecordFieldTypes = recordFields
            .values()
            .stream()
            .map(this::resolveRecordFieldType)
            .reduce((x, y)->x.alternativeMerge(y))
            .get();
        if (isWildcard) {
            return mergedRecordFieldTypes;
        }
        if (!keySpecifierType.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemString()))) {
            error(ctx, ErrorType.LOOKUP__INVALID_EXTENDED_RECORD_KEY_TYPE, List.of());
            return zeroOrMoreItems;
        }
        final var stringToken = keySpecifier.STRING();
        if (stringToken != null) {
            final String key = processStringLiteral(keySpecifier);
            final var recordField = recordFields.get(key);
            if (recordField == null) {
                return zeroOrMoreItems;
            }
            return resolveRecordFieldType(recordField);
        }
        final XQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
        if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
            error(lookup, ErrorType.LOOKUP__INVALID_EXTENDED_RECORD_KEY_TYPE, List.of(targetType, expectedKeyItemtype));
        }
        if (keySpecifierType.type.itemType.type == XQueryTypes.ENUM) {
            final var members = keySpecifierType.type.itemType.enumMembers;
            final var firstField = members.stream().findFirst().get();
            final var firstRecordField = recordFields.get(firstField);
            XQuerySequenceType merged = resolveRecordFieldType(firstRecordField);
            for (final var member : members) {
                if (member.equals(firstField))
                    continue;
                final var recordField = recordFields.get(member);
                if (recordField == null)  {
                    return zeroOrMoreItems;
                }
                merged = merged.alternativeMerge(resolveRecordFieldType(recordField));
            }
            return merged;
        }
        return mergedRecordFieldTypes.addOptionality();
    }

	private XQuerySequenceType resolveRecordFieldType(XQueryRecordField t) {
		var type = switch(t.typeOrReference().fieldType()) {
		    case REFERENCE -> {
		        yield typeFactory.namedType(t.typeOrReference().reference()).type();
		    }
		    case TYPE -> {
		        yield t.typeOrReference().type();
		    }
		};
		return t.isRequired()? type : type.addOptionality();
	}

    TypeInContext getKeySpecifier(final LookupExprContext ctx) {
        final KeySpecifierContext keySpecifier = ctx.lookup().keySpecifier();
        if (keySpecifier.qname() != null) {
            final XQuerySequenceType enum_ = typeFactory.enum_(Set.of(keySpecifier.qname().getText()));
            return contextManager.typeInContext(enum_);
        }
        if (keySpecifier.STRING() != null ) {
            return handleString(keySpecifier);
        }
        if (keySpecifier.IntegerLiteral() != null) {
            return handleNumber(keySpecifier.IntegerLiteral());
        }
        return keySpecifier.accept(this);
    }


    @Override
    public TypeInContext visitContextValueRef(final ContextValueRefContext ctx)
    {
        return context.getType();
    }


    @Override
    public TypeInContext visitForwardAxis(final ForwardAxisContext ctx) {
        if (ctx.CHILD() != null)
            currentAxis = XQueryAxis.CHILD;
        if (ctx.DESCENDANT() != null)
            currentAxis = XQueryAxis.DESCENDANT;
        if (ctx.SELF() != null)
            currentAxis = XQueryAxis.SELF;
        if (ctx.DESCENDANT_OR_SELF() != null)
            currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
        if (ctx.FOLLOWING_SIBLING() != null)
            currentAxis = XQueryAxis.FOLLOWING_SIBLING;
        if (ctx.FOLLOWING() != null)
            currentAxis = XQueryAxis.FOLLOWING;
        if (ctx.FOLLOWING_SIBLING_OR_SELF() != null)
            currentAxis = XQueryAxis.FOLLOWING_SIBLING_OR_SELF;
        if (ctx.FOLLOWING_OR_SELF() != null)
            currentAxis = XQueryAxis.FOLLOWING_OR_SELF;
        return null;
    }

    @Override
    public TypeInContext visitReverseAxis(final ReverseAxisContext ctx) {
        if (ctx.PARENT() != null)
            currentAxis = XQueryAxis.PARENT;
        if (ctx.ANCESTOR() != null)
            currentAxis = XQueryAxis.ANCESTOR;
        if (ctx.PRECEDING_SIBLING_OR_SELF() != null)
            currentAxis = XQueryAxis.PRECEDING_SIBLING_OR_SELF;
        if (ctx.PRECEDING_OR_SELF() != null)
            currentAxis = XQueryAxis.PRECEDING_OR_SELF;
        if (ctx.PRECEDING_SIBLING() != null)
            currentAxis = XQueryAxis.PRECEDING_SIBLING;
        if (ctx.PRECEDING() != null)
            currentAxis = XQueryAxis.PRECEDING;
        if (ctx.ANCESTOR_OR_SELF() != null)
            currentAxis = XQueryAxis.ANCESTOR_OR_SELF;
        return null;
    }



    @Override
    public TypeInContext visitForwardStep(final ForwardStepContext ctx)
    {
        if (ctx.forwardAxis() != null) {
            ctx.forwardAxis().accept(this);
        } else {
            if (currentAxis == null) {
                currentAxis = XQueryAxis.CHILD;
            }
        }
        return visitNodeTest(ctx.nodeTest());
    }

    @Override
    public TypeInContext visitReverseStep(final ReverseStepContext ctx)
    {
        if (ctx.abbrevReverseStep() != null) {
            return ctx.abbrevReverseStep().accept(this);
        }
        ctx.reverseAxis().accept(this);
        return visitNodeTest(ctx.nodeTest());
    }




    @Override
    public TypeInContext visitStringConcatExpr(final StringConcatExprContext ctx)
    {
        if (ctx.CONCATENATION().isEmpty()) {
            return visitRangeExpr(ctx.rangeExpr(0));
        }
        for (int i = 0; i < ctx.rangeExpr().size(); i++) {
            final var visitedType = visitRangeExpr(ctx.rangeExpr(i)).type;
            if (!visitedType.isSubtypeOf(zeroOrMoreItems)) {
                error(ctx.rangeExpr(i), ErrorType.CONCAT__INVALID, List.of());
            }
        }
        return contextManager.typeInContext(string);
    }

    @Override
    public TypeInContext visitSimpleMapExpr(final SimpleMapExprContext ctx)
    {
        if (ctx.EXCLAMATION_MARK().isEmpty())
            return visitPathExpr(ctx.pathExpr(0));
        final TypeInContext firstExpressionType = visitPathExpr(ctx.pathExpr(0));
        final XQuerySequenceType iterator = firstExpressionType.iteratorType();
        final var savedContext = saveContext();
        context.setType(contextManager.typeInContext(iterator));
        context.setPositionType(contextManager.typeInContext(number));
        context.setSizeType(contextManager.typeInContext(number));
        TypeInContext result = firstExpressionType;
        final var theRest = ctx.pathExpr().subList(1, ctx.pathExpr().size());
        for (final var mappedExpression : theRest) {
            final TypeInContext type = visitPathExpr(mappedExpression);
            result = contextManager.typeInContext(result.type.mapping(type.type));
            context.setType(contextManager.typeInContext(result.iteratorType()));
        }
        context = savedContext;
        return result;
    }

    @Override
    public TypeInContext visitInstanceofExpr(final InstanceofExprContext ctx)
    {
        final TypeInContext expression = visitTreatExpr(ctx.treatExpr());
        if (ctx.INSTANCE() == null) {
            return expression;
        }
        final var testedType = ctx.sequenceType().accept(this);
        if (expression.isSubtypeOf(testedType)) {
            // UNNECESSARY_INSTANCE_OF__ALWAYS_TRUE
            warn(ctx, WarningType.INSTANCE_OF__ALWAYS_TRUE, List.of());
        }
        // TODO: add warning on impossible instance of tests
        final var bool = contextManager.typeInContext(this.boolean_);
        contextManager.currentScope().imply(
            bool,
            new InstanceOfSuccessImplication(
                bool,
                true,
                expression,
                testedType
                )
            );
        return bool;
    }

    @Override
    public TypeInContext visitTreatExpr(final TreatExprContext ctx)
    {
        final TypeInContext expression = visitCastableExpr(ctx.castableExpr());
        if (ctx.TREAT() == null) {
            return expression;
        }
        final var relevantType = visitSequenceType(ctx.sequenceType());
        if (!relevantType.isSubtypeOf(expression)
            && !expression.isSubtypeOf(relevantType))
        {
            warn(ctx, WarningType.TREAT__UNLIKELY, List.of(expression, relevantType));
        }
        return relevantType;
    }

    private final SequencetypeAtomization atomizer;



    private final SequencetypeCastable castability;
    private final XQuerySequenceType anyNodes;

    @Override
    public TypeInContext visitCastableExpr(final CastableExprContext ctx) {
        if (ctx.CASTABLE() == null)
            return this.visitCastExpr(ctx.castExpr());
        final var type = this.visitCastTarget(ctx.castTarget());
        final var tested = this.visitCastExpr(ctx.castExpr());
        final boolean emptyAllowed = ctx.castTarget().QUESTION_MARK() != null;
        final IsCastableResult result = castability.isCastable(type.type, tested.type, emptyAllowed);
        verifyCastability(ctx, type, tested.type, result.castability(), result);
        return contextManager.typeInContext(result.resultingType());
    }

    private <T> void verifyCastability(
            final ParserRuleContext ctx,
            final T type,
            final XQuerySequenceType tested,
            final Castability castability,
            final IsCastableResult result)
    {
        // TODO: add atomized info
        switch(castability) {
            case POSSIBLE: { break; }
            case ALWAYS_POSSIBLE_CASTING_TO_SAME: {
                warn(ctx, WarningType.CAST__SELFCAST, List.of(tested, type));
            }
            case ALWAYS_POSSIBLE_CASTING_TO_SUBTYPE: {
                warn(ctx, WarningType.CAST__SUBTYPE_CAST, List.of(tested, type));
            }
            case ALWAYS_POSSIBLE_CASTING_TO_TARGET:{
                warn(ctx, WarningType.CAST__TARGET_CAST, List.of(tested, type));
            }
            case ALWAYS_POSSIBLE_MANY_ITEMTYPES: {
                warn(ctx, WarningType.CAST__POSSIBLE_MANY_ITEMTYPES, List.of(tested, type));
                final XQueryItemType[] wrongItemtypes = result.wrongItemtypes();
                final int itemtypeCount = wrongItemtypes.length;
                for (int i = 0; i < itemtypeCount; i++) {
                    verifyCastability(ctx, wrongItemtypes[i], tested, result.problems()[i], null);
                }
                break;
            }
            case ALWAYS_POSSIBLE_MANY_SEQUENCETYPES: {
                warn(ctx, WarningType.CAST__POSSIBLE_MANY_SEQUENCETYPES, List.of(tested, type));
                break;
            }
            case IMPOSSIBLE: {
                error(ctx, ErrorType.CAST__IMPOSSIBLE, List.of(tested, type));
                break;
            }
            case TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG: {
                error(ctx, ErrorType.CAST__EMPTY_WITHOUT_FLAG, List.of(tested));
                break;
            }
            case TESTED_EXPRESSION_IS_EMPTY_SEQUENCE: {
                error(ctx, ErrorType.CAST__EMPTY_SEQUENCE, List.of());
                break;
            }
            case TESTED_EXPRESSION_IS_ZERO_OR_MORE: {
                error(ctx, ErrorType.CAST__ZERO_OR_MORE, List.of(tested));
                break;
            }
            case WRONG_TARGET_TYPE: {
                error(ctx, ErrorType.CAST__WRONG_TARGET_TYPE, List.of(type));
                break;
            }
        }
    }


    @Override
    public TypeInContext visitCastExpr(final CastExprContext ctx) {
        if (ctx.CAST() == null)
            return this.visitPipelineExpr(ctx.pipelineExpr());
        final var type = this.visitCastTarget(ctx.castTarget());
        final var tested = this.visitPipelineExpr(ctx.pipelineExpr());
        final boolean emptyAllowed = ctx.castTarget().QUESTION_MARK() != null;
        final IsCastableResult result = castability.isCastable(type.type, tested.type, emptyAllowed);
        verifyCastability(ctx, type.type, tested.type, result.castability(), result);
        return contextManager.typeInContext(result.resultingType());
    }


    @Override
    public TypeInContext visitCastTarget(final CastTargetContext ctx) {
        var type = super.visitCastTarget(ctx);
        if (ctx.QUESTION_MARK() != null)
            type = contextManager.typeInContext(type.type.addOptionality());
        return type;
    }

    @Override
    public TypeInContext visitNamedFunctionRef(final NamedFunctionRefContext ctx)
    {
        final int arity = Integer.parseInt(ctx.IntegerLiteral().getText());
        final QualifiedName resolvedName = namespaceResolver.resolveFunction(ctx.qname().getText());
        final var analysis = symbolManager.getFunctionReference(
            ctx, resolvedName.namespace(), resolvedName.name(), arity, contextManager.currentContext());
        errors.addAll(analysis.errors());
        return analysis.result();
    }

    @Override
    public TypeInContext visitSquareArrayConstructor(final SquareArrayConstructorContext ctx)
    {
        if (ctx.exprSingle().isEmpty()) {
            return contextManager.typeInContext(anyArray);
        }
        final XQuerySequenceType arrayType = ctx.exprSingle().stream()
            .map(expr -> expr.accept(this).type)
            .reduce((t1, t2) -> t1.alternativeMerge(t2))
            .get();
        return contextManager.typeInContext(typeFactory.array(arrayType));
    }

    @Override
    public TypeInContext visitCurlyArrayConstructor(final CurlyArrayConstructorContext ctx)
    {
        final var expressions = ctx.enclosedExpr().expr();
        if (expressions == null) {
            return contextManager.typeInContext(anyArray);
        }

        final XQuerySequenceType arrayType = expressions.exprSingle().stream()
            .map(expr -> expr.accept(this).type)
            .reduce((t1, t2) -> t1.alternativeMerge(t2))
            .get();
        return contextManager.typeInContext(typeFactory.array(arrayType));

    }

    @Override
    public TypeInContext visitPipelineExpr(final PipelineExprContext ctx)
    {
        if (ctx.PIPE_ARROW().isEmpty())
            return ctx.arrowExpr(0).accept(this);
        final var saved = saveContext();
        final int size = ctx.arrowExpr().size();
        TypeInContext contextType = visitArrowExpr(ctx.arrowExpr(0));
        for (var i = 1; i < size; i++) {
            final var contextualizedExpr = ctx.arrowExpr(i);
            context.setType(contextType);
            context.setPositionType(null);
            context.setSizeType(null);
            contextType = contextualizedExpr.accept(this);
        }
        context = saved;
        return contextType;
    }

    @Override
    public TypeInContext visitTryCatchExpr(final TryCatchExprContext ctx)
    {
        final var savedContext = saveContext();
        final XQueryItemType errorType = typeFactory.itemError();
        final var testedExprType = ctx.tryClause().enclosedExpr().accept(this);
        final var alternativeCatches = ctx.catchClause().stream()
            .map(c -> {
                XQuerySequenceType choicedErrors;
                if (c.pureNameTestUnion() != null) {
                    final var foundErrors = new ArrayList<XQueryItemType>();
                    for (final var error : c.pureNameTestUnion().nameTest()) {
                        final String errorText = error.getText();
                        final QualifiedName errorQName = namespaceResolver.resolveType(errorText);
                        final NamedItemAccessingResult namedItemResult = typeFactory.itemNamedType(errorQName);
                        var caughtErrorType = namedItemResult.type();
                        switch (namedItemResult.status()) {
                            case OK-> {
                                if (!caughtErrorType.itemtypeIsSubtypeOf(errorType)) {
                                    error(c, ErrorType.TRY_CATCH__NON_ERROR, List.of(caughtErrorType, errorText));
                                    caughtErrorType = errorType;
                                }
                                foundErrors.add(caughtErrorType);
                            }
                            case UNKNOWN_NAMESPACE -> {
                                error(c, ErrorType.TRY_CATCH__ERROR__UNKNOWN_NAMESPACE, List.of(errorText));
                                caughtErrorType = errorType;
                                foundErrors.add(caughtErrorType);
                            }
                            case UNKNOWN_NAME -> {
                                error(c, ErrorType.TRY_CATCH__ERROR__UNKNOWN_NAME, List.of(errorText));
                                caughtErrorType = errorType;
                                foundErrors.add(caughtErrorType);
                            }
                        }
                    }
                    choicedErrors = typeFactory.choice(foundErrors);
                } else {
                    choicedErrors = typeFactory.error();
                }
                context.setType(contextManager.typeInContext(choicedErrors));
                context.setPositionType(null);
                context.setSizeType(null);
                contextManager.enterScope();
                declareVariable(contextManager.typeInContext(string), "err:code", null);
                declareVariable(contextManager.typeInContext(optionalString), "err:description", null);
                declareVariable(contextManager.typeInContext(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), "err:value", null);
                declareVariable(contextManager.typeInContext(typeFactory.zeroOrOne(typeFactory.itemString())), "err:module", null);
                declareVariable(contextManager.typeInContext(typeFactory.zeroOrOne(typeFactory.itemNumber())), "err:line-number", null);
                declareVariable(contextManager.typeInContext(typeFactory.zeroOrOne(typeFactory.itemNumber())), "err:column-number", null);
                declareVariable(contextManager.typeInContext(typeFactory.zeroOrOne(typeFactory.itemString())), "err:stack-trace", null);
                declareVariable(contextManager.typeInContext(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), "err:additional", null);
                declareVariable(contextManager.typeInContext(typeFactory.anyMap()), "err:map", null);

                final var visited = c.enclosedExpr().accept(this);
                contextManager.leaveScope();
                return visited;
            });

        final Set<String> localErrors = new HashSet<>();
        // Marking duplicate error type names as errors
        for (final var catchClause : ctx.catchClause()) {
            if (catchClause.pureNameTestUnion() != null) {
                for (final var qname : catchClause.pureNameTestUnion().nameTest()) {
                    final String name = qname.getText();
                    if (localErrors.contains(name)) {
                        error(qname, ErrorType.TRY_CATCH__DUPLICATED_ERROR, List.of(name));
                    } else {
                        localErrors.add(name);
                    }

                }
            }
        }

        // Marking multiple catch * {} as errors
        int wildcardCount = 0;
        for (final var catchClause : ctx.catchClause()) {
            if (catchClause.wildcard() != null && wildcardCount++ > 1) {
                error(
                    catchClause,
                    ErrorType.TRY_CATCH__UNNECESSARY_ERROR_BECAUSE_OF_WILDCARD,
                    List.of());
            }
        }

        final FinallyClauseContext finallyClause = ctx.finallyClause();
        if (finallyClause != null) {
            context = new XQueryVisitingSemanticContext();
            context.setType(contextManager.typeInContext(typeFactory.anyNode()));
            final XQuerySequenceType finallyType = visitEnclosedExpr(finallyClause.enclosedExpr()).type;
            if (!finallyType.isSubtypeOf(emptySequence)) {
                error(finallyClause, ErrorType.TRY_CATCH__FINALLY_NON_EMPTY, List.of(finallyType));
            }
        }
        context = savedContext;
        final XQuerySequenceType mergedAlternativeCatches = alternativeCatches
            .map(x->x.type)
            .reduce(XQuerySequenceType::alternativeMerge)
            .get();
        final var merged = testedExprType.type.alternativeMerge(mergedAlternativeCatches);
        return contextManager.typeInContext(merged);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeInContext visitMapConstructor(final MapConstructorContext ctx)
    {
        final var entries = ctx.mapConstructorEntry();
        if (entries.isEmpty()) {
            // return contextManager.typeInContext(typeFactory.record(Map.of()));
            return contextManager.typeInContext(typeFactory.anyMap());
        }
        final XQueryItemType keyType = entries.stream()
            .map(e -> e.mapKeyExpr().accept(this).type.itemType)
            .reduce((t1, t2) -> t1.alternativeMerge(t2))
            .get();
        if (keyType.type == XQueryTypes.ENUM) {
            final var enum_ = keyType;
            final var enumMembers = enum_.enumMembers;
            final List<Entry<String, XQueryRecordField>> recordEntries = new ArrayList<>(enumMembers.size());
            int i = 0;
            for (final var enumMember : enumMembers) {
                final var valueType = entries.get(i).mapValueExpr().accept(this);
                recordEntries.add(Map.entry(enumMember, new XQueryRecordField(
                    TypeOrReference.type(valueType.type),
                    true)));
                i++;
            }
            return contextManager.typeInContext(typeFactory.record(Map.ofEntries(recordEntries.toArray(Entry[]::new))));
        }
        // TODO: refine
        final XQuerySequenceType valueType = entries.stream()
            .map(e -> visitMapValueExpr(e.mapValueExpr()).type)
            .reduce((t1, t2) -> t1.alternativeMerge(t2))
            .get();
        return contextManager.typeInContext(typeFactory.map(keyType, valueType));
    }


    @Override
    public TypeInContext visitArrowExpr(final ArrowExprContext ctx) {
        final boolean notSequenceArrow = ctx.sequenceArrowTarget().isEmpty();
        final boolean notMappingArrow = ctx.mappingArrowTarget().isEmpty();
        if (notSequenceArrow && notMappingArrow) {
            return ctx.unaryExpr().accept(this);
        }
        final var savedArgs = saveVisitedArguments();
        final var savedKwargs = saveVisitedKeywordArguments();

        var contextArgument = ctx.unaryExpr().accept(this);
        visitedPositionalArguments.add(contextArgument);
        for (final var arrowexpr : ctx.children.subList(1, ctx.children.size())) {
            contextArgument = arrowexpr.accept(this);
            visitedPositionalArguments = new ArrayList<>();
            visitedPositionalArguments.add(contextArgument);
            visitedKeywordArguments = new HashMap<>();
        }

        visitedPositionalArguments = savedArgs;
        visitedKeywordArguments = savedKwargs;
        return contextArgument;
    }

    @Override
    public TypeInContext visitArrowTarget(final ArrowTargetContext ctx) {
        if (ctx.functionCall() != null) {
            ctx.functionCall().argumentList().accept(this);
            final String functionQname = ctx.functionCall().functionName().getText();
            return callFunction(
                ctx.functionCall(),
                functionQname,
                visitedPositionalArguments,
                visitedKeywordArguments);
        }
        return ctx.restrictedDynamicCall().accept(this);
    }


    @Override
    public TypeInContext visitMappingArrowTarget(final MappingArrowTargetContext ctx) {
        final TypeInContext mappedSequence = visitedPositionalArguments
            .get(visitedPositionalArguments.size() - 1) ;


        if (mappedSequence.type.isZero) {
            return mappedSequence;
        }
        final XQuerySequenceType iterator = mappedSequence.iteratorType();
        visitedPositionalArguments = new ArrayList<>();
        visitedPositionalArguments.add(contextManager.typeInContext(iterator));
        final var call = ctx.arrowTarget().accept(this);
        return switch(mappedSequence.type.occurence) {
            case ONE -> call;
            case ONE_OR_MORE -> contextManager.typeInContext(call.type.sequenceMerge(call.type));
            case ZERO_OR_MORE -> contextManager.typeInContext(call.type.sequenceMerge(call.type).addOptionality());
            case ZERO_OR_ONE -> contextManager.typeInContext(call.type.addOptionality());
            case ZERO -> {
                error(ctx, ErrorType.MAPPING__EMPTY_SEQUENCE, List.of());
                yield contextManager.typeInContext(emptySequence);
            }
        };
    }

    @Override
    public TypeInContext visitRestrictedDynamicCall(final RestrictedDynamicCallContext ctx) {
        final var value = ctx.children.get(0).accept(this);
        final boolean isCallable = value.isSubtypeOf(typeFactory.anyFunction());
        if (!isCallable) {
            error(ctx, ErrorType.RESTRICTED_DYNAMIC_CALL__NON_FUNCTION, List.of(value));
        }
        ctx.positionalArgumentList().accept(this);

        final List<XQuerySequenceType> args = visitedPositionalArguments.stream().map(a->a.type).toList();
        final var expectedFunction = typeFactory.itemFunction(zeroOrMoreItems, args);
        if (!value.type.itemType.itemtypeIsSubtypeOf(expectedFunction))
        {
            error(ctx, ErrorType.RESTRICTED_DYNAMIC_CALL__INVALID_FUNCTION, List.of(expectedFunction, value));
        }

        if (isCallable)
            return contextManager.typeInContext(value.type.itemType.returnedType);
        else
            return contextManager.typeInContext(zeroOrMoreItems);
    }






    @Override
    public TypeInContext visitAndExpr(final AndExprContext ctx)
    {
        if (ctx.AND().isEmpty()) {
            return ctx.comparisonExpr(0).accept(this);
        }
        final var operatorCount = ctx.AND().size();
        final List<ParseTree> exprs = new ArrayList<>(operatorCount+1);
        contextManager.enterScope();
        boolean valid = true;
        for (int i = 0; i <= operatorCount; i++) {
            final ComparisonExprContext expr = ctx.comparisonExpr(i);
            final var visitedType = visitComparisonExpr(expr);
            if (!visitedType.type.hasEffectiveBooleanValue()) {
                error(expr, ErrorType.AND__NON_EBV, List.of(visitedType));
                valid = false;
            } else {
                final var ebv = contextManager.resolveEffectiveBooleanValue(visitedType);
                contextManager.currentScope().assume(ebv, new Assumption(ebv, true));
                exprs.add(expr);
            }
        }
        contextManager.leaveScope();
        final var andExpr = contextManager.typeInContext(boolean_);
        if (valid) {
            contextManager.currentScope().imply(andExpr, new AndTrueImplication(andExpr, exprs, this));
        }
        return andExpr;
    }

    @Override
    public TypeInContext visitAdditiveExpr(final AdditiveExprContext ctx)
    {
        if (ctx.additiveOperator().isEmpty()) {
            return ctx.multiplicativeExpr(0).accept(this);
        }
        for (final var operandExpr : ctx.multiplicativeExpr()) {
            final var operand = operandExpr.accept(this);
            if (!operand.isSubtypeOf(number)) {
                error(operandExpr, ErrorType.ADDITIVE__INVALID, List.of(operand));
            }
        }
        return contextManager.typeInContext(number);
    }

    @Override
    public TypeInContext visitComparisonExpr(final ComparisonExprContext ctx)
    {
        if (ctx.generalComp() != null) {
            return handleGeneralComparison(ctx);
        }
        if (ctx.valueComp() != null) {
            return handleValueComparison(ctx);
        }
        if (ctx.nodeComp() != null) {
            return handleNodeComp(ctx);
        }
        return ctx.otherwiseExpr(0).accept(this);
    }

    private TypeInContext handleGeneralComparison(final ComparisonExprContext ctx)
    {
        final var firstOtherwise = visitOtherwiseExpr(ctx.otherwiseExpr(0));
        final var secondOtherwise = visitOtherwiseExpr(ctx.otherwiseExpr(1));
        final var leftHandSide = atomizer.atomize(firstOtherwise.type);
        final var rightHandSide = atomizer.atomize(secondOtherwise.type);
        if (!leftHandSide.isSubtypeOf(rightHandSide) && !rightHandSide.isSubtypeOf(leftHandSide)) {
            error(ctx, ErrorType.GENERAL_COMP__INVALID, List.of(leftHandSide, rightHandSide));
        }
        return contextManager.typeInContext(typeFactory.boolean_());
    }

    private TypeInContext handleValueComparison(final ComparisonExprContext ctx)
    {
        final var leftHandSide = ctx.otherwiseExpr(0).accept(this);
        final var rightHandSide = ctx.otherwiseExpr(1).accept(this);
        final var optionalItem = typeFactory.zeroOrOne(typeFactory.itemAnyItem());
        final var optionalBoolean = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        final boolean invalidLeft = !leftHandSide.isSubtypeOf(optionalItem);
        final boolean invalidRight = !rightHandSide.isSubtypeOf(optionalItem);
        if (invalidLeft && invalidRight) {
            error(ctx.otherwiseExpr(0), ErrorType.VALUE_COMP__BOTH_INVALID, List.of(leftHandSide, rightHandSide));
        }
        else if (invalidLeft) {
            error(ctx.otherwiseExpr(0), ErrorType.VALUE_COMP__LHS_INVALID, List.of(leftHandSide));
        }
        else if (invalidRight) {
            error(ctx.otherwiseExpr(1), ErrorType.VALUE_COMP__RHS_INVALID, List.of(rightHandSide));
        }
        else if (!leftHandSide.type.isValueComparableWith(rightHandSide.type)) {
            error(ctx, ErrorType.VALUE_COMP__INCOMPARABLE, List.of(leftHandSide, rightHandSide));
        }
        if (leftHandSide.isSubtypeOf(typeFactory.anyItem())
            && rightHandSide.isSubtypeOf(typeFactory.anyItem()))
        {
            return contextManager.typeInContext(typeFactory.boolean_());
        }
        return contextManager.typeInContext(optionalBoolean);
    }

    private TypeInContext handleNodeComp(final ComparisonExprContext ctx)
    {
        final var anyNode = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var optionalBoolean = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        final var visitedLeft = visitOtherwiseExpr(ctx.otherwiseExpr(0));
        final var visitedRight = visitOtherwiseExpr(ctx.otherwiseExpr(1));
        final boolean validLhs = visitedLeft.isSubtypeOf(anyNode);
        final boolean validRhs = visitedRight.isSubtypeOf(anyNode);
        if (!validLhs && !validRhs) {
            error(ctx.otherwiseExpr(0), ErrorType.NODE_COMP__BOTH_INVALID, List.of(visitedLeft, visitedRight));
        }
        else if (!validLhs) {
            error(ctx.otherwiseExpr(0), ErrorType.NODE_COMP__LHS_INVALID, List.of(visitedLeft));
        }
        else if (!validRhs) {
            error(ctx.otherwiseExpr(1), ErrorType.NODE_COMP__RHS_INVALID, List.of(visitedRight));
        }
        return contextManager.typeInContext(optionalBoolean);

    }

    @Override
    public TypeInContext visitMultiplicativeExpr(final MultiplicativeExprContext ctx)
    {
        if (ctx.multiplicativeOperator().isEmpty()) {
            return ctx.unionExpr(0).accept(this);
        }
        for (final var expr : ctx.unionExpr()) {
            final var visitedType = expr.accept(this);
            if (!visitedType.isSubtypeOf(number)) {
                error(ctx, ErrorType.MUL__INVALID, List.of(visitedType));
            }
        }
        return contextManager.typeInContext(number);
    }

    @Override
    public TypeInContext visitOtherwiseExpr(final OtherwiseExprContext ctx)
    {
        if (ctx.OTHERWISE().isEmpty())
            return ctx.stringConcatExpr(0).accept(this);
        final int length = ctx.stringConcatExpr().size();
        XQuerySequenceType merged = visitStringConcatExpr(ctx.stringConcatExpr(0)).type;
        if (merged.isOne || merged.isOneOrMore) {
            warn(ctx.stringConcatExpr(0), WarningType.OTHERWISE__IMPOSSIBLE, List.of(merged));
        }
        for (int i = 1; i < length; i++) {
            final var expr = ctx.stringConcatExpr(i);
            final XQuerySequenceType exprType = visitStringConcatExpr(expr).type;
            if (exprType.isOne || exprType.isOneOrMore) {
                warn(expr, WarningType.OTHERWISE__IMPOSSIBLE, List.of(exprType));
            }
            merged = exprType.alternativeMerge(merged);
        }
        return contextManager.typeInContext(merged);
    }

    @Override
    public TypeInContext visitUnionExpr(final UnionExprContext ctx)
    {
        if (ctx.unionOperator().isEmpty()) {
            return ctx.intersectExpr(0).accept(this);
        }
        var expressionNode = ctx.intersectExpr(0);
        var expressionType = expressionNode.accept(this);
        if (!expressionType.isSubtypeOf(zeroOrMoreNodes)) {
            error(expressionNode, ErrorType.UNION__INVALID, List.of(expressionType));
            expressionType = contextManager.typeInContext(zeroOrMoreNodes);
        }
        final var unionCount = ctx.unionOperator().size();
        for (int i = 1; i <= unionCount; i++) {
            expressionNode = ctx.intersectExpr(i);
            final var visitedType = expressionNode.accept(this);
            if (!visitedType.isSubtypeOf(zeroOrMoreNodes)) {
                error(expressionNode, ErrorType.UNION__INVALID, List.of(expressionType));
                expressionType = contextManager.typeInContext(zeroOrMoreNodes);
            } else {
                expressionType = contextManager.typeInContext(expressionType.type.unionMerge(visitedType.type));
            }
        }
        return expressionType;
    }

    @Override
    public TypeInContext visitIntersectExpr(final IntersectExprContext ctx)
    {
        if (ctx.exceptOrIntersect().isEmpty()) {
            return ctx.instanceofExpr(0).accept(this);
        }
        var expressionType = ctx.instanceofExpr(0).accept(this);
        if (!expressionType.isSubtypeOf(zeroOrMoreNodes)) {
            error(ctx.instanceofExpr(0), ErrorType.INTERSECT_OR_EXCEPT__INVALID, List.of(expressionType));
            expressionType = contextManager.typeInContext(zeroOrMoreNodes);
        }
        final var operatorCount = ctx.exceptOrIntersect().size();
        for (int i = 1; i < operatorCount; i++) {
            final var instanceofExpr = ctx.instanceofExpr(i);
            final var visitedType = instanceofExpr.accept(this);
            if (!visitedType.isSubtypeOf(zeroOrMoreNodes)) {
                error(ctx.instanceofExpr(i), ErrorType.INTERSECT_OR_EXCEPT__INVALID, List.of(expressionType));
                expressionType = contextManager.typeInContext(zeroOrMoreNodes);
            } else {
                if (ctx.exceptOrIntersect(i).EXCEPT() != null)
                    expressionType = contextManager.typeInContext(expressionType.type.exceptionMerge(visitedType.type));
                else
                    expressionType = contextManager.typeInContext(expressionType.type.intersectionMerge(visitedType.type));
            }
        }
        return expressionType;
    }

    @Override
    public TypeInContext visitUnaryExpr(final UnaryExprContext ctx)
    {
        if (ctx.MINUS() == null && ctx.PLUS() == null) {
            return ctx.simpleMapExpr().accept(this);
        }
        final var type = ctx.simpleMapExpr().accept(this);
        if (!type.isSubtypeOf(number)) {
            error(ctx, ErrorType.UNARY__INVALID, List.of(type));
        }
        return contextManager.typeInContext(number);
    }

    @Override
    public TypeInContext visitSwitchExpr(final SwitchExprContext ctx) {
        final SwitchComparandContext switchComparand = ctx.switchComparand();

        final TypeInContext comparand = visitExpr(switchComparand.switchedExpr);
        final SwitchCasesContext switchCases = ctx.switchCases();
        final boolean notBraced = switchCases != null;
        final var defaultExpr = notBraced
            ? switchCases.defaultExpr
            : ctx.bracedSwitchCases().switchCases().defaultExpr;
        final var clauses = notBraced
            ? switchCases.switchCaseClause()
            : ctx.bracedSwitchCases().switchCases().switchCaseClause();

        XQuerySequenceType merged = null;
        for (final var clause : clauses) {
            final var operandType = clause.switchCaseOperand().stream()
                .map(this::visit)
                .map(x->x.type)
                .reduce(XQuerySequenceType::alternativeMerge)
                .get();
            if (!operandType.isSubtypeOf(comparand.type)) {
                error(clause, ErrorType.SWITCH__INVALID_CASE, List.of(operandType, comparand));
            }
            final var returned = clause.exprSingle().accept(this);
            if (merged == null) {
                merged = returned.type;
                continue;
            }
            merged = merged.alternativeMerge(returned.type);
        }
        final var merg =  merged.alternativeMerge(visitExprSingle(defaultExpr).type);
        return contextManager.typeInContext(merg);
    }

    @Override
    public TypeInContext visitArgument(final ArgumentContext ctx)
    {
        final var value = super.visitArgument(ctx);
        visitedPositionalArguments.add(value);
        return value;
    }

    @Override
    public TypeInContext visitKeywordArgument(final KeywordArgumentContext ctx)
    {
        final ExprSingleContext keywordAssignedTypeExpr = ctx.argument().exprSingle();
        if (keywordAssignedTypeExpr != null) {
            final var keywordType = keywordAssignedTypeExpr.accept(this);
            final String keyword = ctx.qname().getText();
            visitedKeywordArguments.put(keyword, keywordType);
        }
        // TODO: add placeholder
        return null;

    }

    private List<TypeInContext> saveVisitedArguments()
    {
        final var saved = visitedPositionalArguments;
        visitedPositionalArguments = new ArrayList<>();
        return saved;
    }

    private Map<String, TypeInContext> saveVisitedKeywordArguments()
    {
        final var saved = visitedKeywordArguments;
        visitedKeywordArguments = new HashMap<>();
        return saved;
    }


    void error(final ParserRuleContext where, final ErrorType errorType, final List<Object> data)
    {
        final Token start = where.getStart();
        final Token stop = where.getStop();
        final DiagnosticError error = DiagnosticError.of(start, stop, errorType, data);
        errors.add(error);
    }

    void warn(final ParserRuleContext where, final WarningType type, final List<Object> data)
    {
        final Token start = where.getStart();
        final Token stop = where.getStop();
        warnings.add(DiagnosticWarning.of(start, stop, type, data));
    }



    record LineEndCharPosEnd(int lineEnd, int charPosEnd) {
    }



    @Override
    public TypeInContext visitIfExpr(final IfExprContext ctx)
    {
        final var conditionType = visitExpr(ctx.expr());
        TypeInContext ebv;
        final EffectiveBooleanValueType ebvtype = conditionType.type.effectiveBooleanValueType();
        if (ebvtype == EffectiveBooleanValueType.NO_EBV) { // no effective boolean value
            ebv = contextManager.currentScope().typeInContext(typeFactory.boolean_());
            error(ctx, ErrorType.IF__CONDITION_NON_EBV, List.of(conditionType));
        } else {
            ebv = contextManager.resolveEffectiveBooleanValue(conditionType, ebvtype);
        }
        TypeInContext trueType = null;
        TypeInContext falseType = null;
        // contextManager.currentScope().imply(ebv, new EffectiveBooleanValueTrue(ebv, conditionType, typeFactory));
        if (ctx.bracedAction() != null) {
            contextManager.enterScope();
            contextManager.currentScope().assume(ebv, new Assumption(ebv, true));
            trueType = visitEnclosedExpr(ctx.bracedAction().enclosedExpr());
            contextManager.leaveScope();

            contextManager.enterScope();
            contextManager.currentScope().assume(ebv, new Assumption(ebv, false));
            falseType = contextManager.typeInContext(emptySequence);
            contextManager.leaveScope();
        } else {
            contextManager.enterScope();
            contextManager.currentScope().assume(ebv, new Assumption(ebv, true));
            trueType = ctx.unbracedActions().exprSingle(0).accept(this);
            contextManager.leaveScope();
            contextManager.enterScope();
            contextManager.currentScope().assume(ebv, new Assumption(ebv, false));
            falseType = ctx.unbracedActions().exprSingle(1).accept(this);
            contextManager.leaveScope();
        }
        return contextManager.typeInContext(trueType.type.alternativeMerge(falseType.type));
    }


    @Override
    public TypeInContext visitStringConstructor(final StringConstructorContext ctx)
    {
        return contextManager.typeInContext(typeFactory.string());
    }

    @Override
    public TypeInContext visitStringInterpolation(final StringInterpolationContext ctx)
    {
        return contextManager.typeInContext(typeFactory.string());
    }


    @Override
    public TypeInContext visitInlineFunctionExpr(final InlineFunctionExprContext ctx)
    {
        // Is a focus function?
        if (ctx.functionSignature() == null) {
            // TODO: implement focus function
            return contextManager.typeInContext(typeFactory.anyFunction());
        }
        final Set<String> argumentNames = new HashSet<>();
        final List<XQuerySequenceType> args = new ArrayList<>();
        final var functionSignature = ctx.functionSignature();
        final var returnTypeDeclaration = functionSignature.typeDeclaration();
        contextManager.enterScope();
        for (final var parameter : functionSignature.paramList().varNameAndType()) {
            final String parameterName = parameter.varName().qname().getText();
            final TypeDeclarationContext typeDeclaration = parameter.typeDeclaration();
            final XQuerySequenceType parameterType = typeDeclaration != null
                ? typeDeclaration.accept(this).type
                : zeroOrMoreItems;
            if (argumentNames.contains(parameterName)) {
                error(parameter, ErrorType.FUNCTION__DUPLICATED_ARG_NAME, List.of(parameterName));
            }
            argumentNames.add(parameterName);
            args.add(parameterType);
            declareVariable(contextManager.typeInContext(parameterType), parameterName, parameter.varName());
        }
        final var inlineType = ctx.functionBody().enclosedExpr().accept(this);
        if (returnTypeDeclaration != null) {
            final var returnedType = returnTypeDeclaration.accept(this);
            if (!inlineType.isSubtypeOf(returnedType)) {
                error(ctx.functionBody(), ErrorType.FUNCTION__INVALID_BODY_TYPE, List.of(inlineType, returnedType));
            }
            contextManager.leaveScope();
            return contextManager.typeInContext(typeFactory.function(returnedType.type, args));
        } else {
            contextManager.leaveScope();
            return contextManager.typeInContext(typeFactory.function(inlineType.type, args));
        }
    }

    @Override
    public TypeInContext visitEnclosedExpr(final EnclosedExprContext ctx)
    {
        if (ctx.expr() != null) {
            return visitExpr(ctx.expr());
        }
        return contextManager.typeInContext(emptySequence);
    }



    public record UnresolvedFunctionSpecification(
        ParseTree location,
        QualifiedName name,
        List<UnresolvedArgumentSpecification> args,
        FunctionBodyContext body,
        ParseTree returnedType,
        int minArity,
        int maxArity

    ) {}

    public UnresolvedFunctionSpecification getUnresolvedFunction(
        final QualifiedName qualifiedName,
        final FunctionDeclContext ctx
        )
    {
        final var args = new ArrayList<UnresolvedArgumentSpecification>();
        final var argNameCtx = new ArrayList<VarNameContext>();
        int minArity = 0;
        int maxArity = 0;
        if (ctx.paramListWithDefaults() != null) {
            final var params = ctx.paramListWithDefaults().paramWithDefault();
            for (int i = 0; i < params.size(); i++)
            {
                final var param = params.get(i);
                final var argName = getArgName(param);
                final SequenceTypeContext typeDeclaration = param.varNameAndType().typeDeclaration().sequenceType();
                final ExprSingleContext defaultValue = param.exprSingle();
                if (defaultValue == null)
                    minArity += 1;
                maxArity += 1;

                final var argDecl = new UnresolvedArgumentSpecification(
                    param,
                    argName,
                    typeDeclaration,
                    defaultValue);
                args.add(argDecl);
                argNameCtx.add(param.varNameAndType().varName());
            }
        }

        final FunctionBodyContext functionBody = ctx.functionBody();
        return new UnresolvedFunctionSpecification(
            ctx,
            qualifiedName,
            args,
            functionBody,
            ctx.typeDeclaration(),
            minArity,
            maxArity
            );
    }


    /**
     * @param function
     * @return true if function has valid construction apart from type semantics
     */
    boolean validateUnresolvedFunction(final UnresolvedFunctionSpecification function) {
        int i = 0;
        final Set<String> uniqueNames = new HashSet<>();
        boolean valid = true;
        for (UnresolvedArgumentSpecification fArg : function.args) {
            if (fArg.defaultValue != null)
                break;
            if (!uniqueNames.add(fArg.name)) {
                error(fArg.location, ErrorType.FUNCTION__DUPLICATED_ARG_NAME, List.of(fArg.name));
                valid = false;
            }
            i++;
        }
        List<UnresolvedArgumentSpecification> defaultArgs = function.args.subList(i, function.args.size());
        for (final UnresolvedArgumentSpecification fArg : defaultArgs)
        {
            if (fArg.defaultValue != null) {
                error(fArg.location, ErrorType.FUNCTION__POSITIONAL_ARG_BEFORE_DEFAULT, List.of());
                valid = false;
            }
            if (!uniqueNames.add(fArg.name)) {
                error(fArg.location, ErrorType.FUNCTION__DUPLICATED_ARG_NAME, List.of(fArg.name));
                valid = false;
            }
        }
        return valid;
    }

    public void resolveFunction(final UnresolvedFunctionSpecification spec)
    {
        final var args = new ArrayList<ArgumentSpecification>();
        final var argNameCtx = new ArrayList<VarNameContext>();
        contextManager.enterContext();
        for (final var param : spec.args.subList(0, spec.minArity))
        {
            final XQuerySequenceType paramType = param.type == null
                ? zeroOrMoreItems
                : param.type.accept(this).type;
            final var argDecl = new ArgumentSpecification(param.name, paramType, param.defaultValue);
        }

        for (final var defaultParam : spec.args.subList(spec.minArity, spec.maxArity+1))
        {
            final var paramType = defaultParam.type.accept(this);
            final var dvt = defaultParam.defaultValue.accept(this);
            if (!dvt.isSubtypeOf(paramType)) {
                error((ParserRuleContext)defaultParam.defaultValue, ErrorType.FUNCTION__INVALID_DEFAULT, List.of(dvt, paramType));
            }
            final var argDecl = new ArgumentSpecification(defaultParam.name, paramType.type, defaultParam.defaultValue);
            args.add(argDecl);
            argNameCtx.add(defaultParam.location);
        //


            for (final ParamWithDefaultContext param : params.subList(i, params.size())) {
                final var argName = getArgName(param);
                final var paramType = param.varNameAndType().typeDeclaration().accept(this);
                final var defaultValue = param.exprSingle();
                if (defaultValue == null) {
                    error(param, ErrorType.FUNCTION__POSITIONAL_ARG_BEFORE_DEFAULT, List.of());
                    continue;
                } else {
                    final var dvt = defaultValue.accept(this);
                    if (!dvt.isSubtypeOf(paramType)) {
                        error(defaultValue, ErrorType.FUNCTION__INVALID_DEFAULT, List.of(dvt, paramType));
                    }
                }
                final var argDecl = new ArgumentSpecification(argName, paramType.type, defaultValue);
                if (!argNames.add(argName)) {
                    error(param.getParent(), ErrorType.FUNCTION__DUPLICATED_ARG_NAME, List.of(argName));
                }
                args.add(argDecl);
                argNameCtx.add(param.varNameAndType().varName());
            }
            for (final var arg : args) {
                declareVariable(contextManager.typeInContext(arg.type()), arg.name(), argNameCtx.get(i));
            }
        }

        final TypeInContext returned = ctx.typeDeclaration() != null
            ? visitTypeDeclaration(ctx.typeDeclaration())
            : contextManager.typeInContext(zeroOrMoreItems);

        final FunctionBodyContext functionBody = ctx.functionBody();
        if (functionBody != null) {
            final var bodyType = visitEnclosedExpr(functionBody.enclosedExpr());
            if (!bodyType.isSubtypeOf(returned)) {
                error(functionBody, ErrorType.FUNCTION__INVALID_RETURNED_TYPE, List.of(bodyType, returned));
            }
            symbolManager.registerFunction(
                resolved.namespace(),
                resolved.name(),
                args,
                returned.type,
                ctx.functionBody().enclosedExpr());
        } else {
            symbolManager.registerFunction(
                resolved.namespace(),
                resolved.name(),
                args,
                returned.type);

        }
        contextManager.leaveContext();
        return null;
    }

    @Override
    public TypeInContext visitFunctionDecl(final FunctionDeclContext ctx)
    {
        final String qname = ctx.qname().getText();
        final QualifiedName resolved = namespaceResolver.resolveFunction(qname);
        int i = 0;
        final var args = new ArrayList<ArgumentSpecification>();
        final var argNameCtx = new ArrayList<VarNameContext>();
        contextManager.enterContext();
        if (ctx.paramListWithDefaults() != null) {
            final Set<String> argNames = new HashSet<>();
            final var params = ctx.paramListWithDefaults().paramWithDefault();
            for (final var param : params) {
                final var defaultValue = param.exprSingle();
                if (defaultValue != null)
                    break;
                final var argName = getArgName(param);
                final TypeDeclarationContext typeDeclaration = param.varNameAndType().typeDeclaration();
                final XQuerySequenceType paramType = typeDeclaration == null
                    ? zeroOrMoreItems
                    : visitTypeDeclaration(typeDeclaration).type;
                final var argDecl = new ArgumentSpecification(argName, paramType, null);
                final boolean added = argNames.add(argName);
                if (!added) {
                    error(param, ErrorType.FUNCTION__DUPLICATED_ARG_NAME, List.of(argName));
                }
                args.add(argDecl);
                argNameCtx.add(param.varNameAndType().varName());
                i++;
            }
            for (final ParamWithDefaultContext param : params.subList(i, params.size())) {
                final var argName = getArgName(param);
                final var paramType = param.varNameAndType().typeDeclaration().accept(this);
                final var defaultValue = param.exprSingle();
                if (defaultValue == null) {
                    error(param, ErrorType.FUNCTION__POSITIONAL_ARG_BEFORE_DEFAULT, List.of());
                    continue;
                } else {
                    final var dvt = defaultValue.accept(this);
                    if (!dvt.isSubtypeOf(paramType)) {
                        error(defaultValue, ErrorType.FUNCTION__INVALID_DEFAULT, List.of(dvt, paramType));
                    }
                }
                final var argDecl = new ArgumentSpecification(argName, paramType.type, defaultValue);
                if (!argNames.add(argName)) {
                    error(param.getParent(), ErrorType.FUNCTION__DUPLICATED_ARG_NAME, List.of(argName));
                }
                args.add(argDecl);
                argNameCtx.add(param.varNameAndType().varName());
            }
            i = 0;
            for (final var arg : args) {
                declareVariable(contextManager.typeInContext(arg.type()), arg.name(), argNameCtx.get(i));
                i++;
            }
        }

        final TypeInContext returned = ctx.typeDeclaration() != null
            ? visitTypeDeclaration(ctx.typeDeclaration())
            : contextManager.typeInContext(zeroOrMoreItems);

        final FunctionBodyContext functionBody = ctx.functionBody();
        if (functionBody != null) {
            final var bodyType = visitEnclosedExpr(functionBody.enclosedExpr());
            if (!bodyType.isSubtypeOf(returned)) {
                error(functionBody, ErrorType.FUNCTION__INVALID_RETURNED_TYPE, List.of(bodyType, returned));
            }
            symbolManager.registerFunction(
                resolved.namespace(),
                resolved.name(),
                args,
                returned.type,
                ctx.functionBody().enclosedExpr());
        } else {
            symbolManager.registerFunction(
                resolved.namespace(),
                resolved.name(),
                args,
                returned.type);

        }
        contextManager.leaveContext();
        return null;
    }

    private String getArgName(final ParamWithDefaultContext param)
    {
        final var paramName = param.varNameAndType().varName().qname();
        if (!paramName.namespace().isEmpty())
        {
            error(param, ErrorType.FUNCTION__PARAM_HAS_NAMESPACE, List.of(paramName.anyName().getText()));
        }
        return paramName.anyName().getText();
    }


    @Override
    public TypeInContext visitVarDecl(final VarDeclContext ctx)
    {
        final VarNameContext varNameCtx = ctx.varNameAndType().varName();
        final var name = varNameCtx.qname().getText();
        final var declaredType = visitTypeDeclaration(ctx.varNameAndType().typeDeclaration());
        if (ctx.EXTERNAL() == null) {
            final var assignedType = visitVarValue(ctx.varValue()).type;
            if (assignedType.coerceableTo(declaredType.type) == RelativeCoercability.NEVER) {
                error(ctx, ErrorType.VAR_DECL__UNCOERSABLE, List.of(name, declaredType, assignedType));
            }
        }
        declareVariable(declaredType, name, varNameCtx);
        return null;
    }

    private RegistrationResult resolveItemTypeFromDecl(final QualifiedName qName, final ItemTypeDeclContext ctx) {
        final var itemType = visitItemType(ctx.itemType()).type.itemType;
        return typeFactory.registerNamedType(qName, itemType);
    }

    @Override
    public TypeInContext visitItemTypeDecl(final ItemTypeDeclContext ctx)
    {
        final var typeName = ctx.qname().getText();
        final var qName = namespaceResolver.resolveType(typeName);
        final var result = resolveItemTypeFromDecl(qName, ctx);
        switch (result.status()) {
            case ALREADY_REGISTERED_DIFFERENT ->  {
                final var expected = result.registered();
                error(
                    ctx,
                    ErrorType.ITEM_DECLARATION__ALREADY_REGISTERED_DIFFERENT,
                    List.of(qName, expected));
            }
            case ALREADY_REGISTERED_SAME ->  {
                error(ctx, ErrorType.ITEM_DECLARATION__ALREADY_REGISTERED_SAME, List.of(qName));
            }
            case OK -> { }
        }
        return null;

    }

    private record RecordResolutionResult(
        // RegistrationResult registrationResult,
        XQueryItemType recordItemType,
        List<ArgumentSpecification> fieldsAsArgs){}

    private RecordResolutionResult resolveRecordFromTypeDecl(
        final QualifiedName qName,
        final NamedRecordTypeDeclContext ctx
        )
    {
        final List<ExtendedFieldDeclarationContext> extendedFieldDeclaration = ctx.extendedFieldDeclaration();
        final int size = extendedFieldDeclaration.size();
        final Map<String, XQueryRecordField> fields = new HashMap<>(size);
        final List<ArgumentSpecification> mandatoryArgs = new ArrayList<>(size);
        final List<ArgumentSpecification> optionalArgs = new ArrayList<>(size);
        for (final ExtendedFieldDeclarationContext field : extendedFieldDeclaration) {
            final var fieldName = field.fieldDeclaration().fieldName().getText();
            final var fieldTypeCtx = field.fieldDeclaration().sequenceType();
            XQuerySequenceType fieldType = zeroOrMoreItems;
            if (fieldTypeCtx != null) {
                fieldType = visitSequenceType(fieldTypeCtx).type;
            }
            final boolean isRequired = field.fieldDeclaration().QUESTION_MARK() == null;
            final ExprSingleContext defaultExpr = field.exprSingle();
            fields.put(fieldName, new XQueryRecordField(TypeOrReference.type(fieldType), isRequired));
            if (isRequired) {
                if (defaultExpr == null) {
                    mandatoryArgs.add(new ArgumentSpecification(fieldName, fieldType, null));
                }
                else {
                    optionalArgs.add(new ArgumentSpecification(fieldName, fieldType, defaultExpr));
                }
            } else {
                optionalArgs
                    .add(new ArgumentSpecification(fieldName, fieldType, new HelperTrees().EMPTY_SEQUENCE));
            }
        }
        mandatoryArgs.addAll(optionalArgs);
        final var itemRecordType = ctx.extensibleFlag() == null
            ? typeFactory.itemRecord(fields)
            : typeFactory.itemExtensibleRecord(fields);
        return new RecordResolutionResult(itemRecordType, mandatoryArgs);
    }

    private RecordResolutionResult resolveRecordFromUnresolved(
        final QualifiedName qName,
        final UnresolvedRecordSpecification recordSpecification
        )
    {

        final int size = recordSpecification.fields.size();
        final Map<String, XQueryRecordField> fields = new LinkedHashMap<>(size);
        final List<ArgumentSpecification> mandatoryArgs = new ArrayList<>(size);
        final List<ArgumentSpecification> optionalArgs = new ArrayList<>(size);
        for (final UnresolvedRecordFieldSpecification field : recordSpecification.fields) {
            final var fieldName = field.name;
            final SequenceTypeContext fieldTypeCtx = field.typeOrReferenceCtx;
            if (fieldTypeCtx == null) {
                fields.put(fieldName, new XQueryRecordField(TypeOrReference.type(zeroOrMoreItems), field.isRequired));
                continue;
            }
            if (fieldTypeCtx.itemType() == null || fieldTypeCtx.itemType().typeName() == null) {
                fields.put(fieldName, new XQueryRecordField(TypeOrReference.type(visitSequenceType(fieldTypeCtx).type), field.isRequired));
                continue;
            }
            QualifiedName reference = namespaceResolver.resolveType(fieldTypeCtx.itemType().typeName().getText());
            if (fieldTypeCtx.occurrenceIndicator() == null) {
                TypeOrReference typeOrReference = TypeOrReference.reference(reference, XQueryCardinality.ONE);
                fields.put(fieldName, new XQueryRecordField(typeOrReference, field.isRequired));
            } else {
                XQueryCardinality cardinality = switch(fieldTypeCtx.occurrenceIndicator().getText()) {
                    case "?" -> XQueryCardinality.ZERO_OR_ONE;
                    case "*" -> XQueryCardinality.ZERO_OR_MORE;
                    default -> XQueryCardinality.ONE_OR_MORE;
                };
                TypeOrReference typeOrReference = TypeOrReference.reference(qName, cardinality);
                fields.put(fieldName, new XQueryRecordField(typeOrReference, field.isRequired));
            }

        }
        for (var mandatoryArgSpec : recordSpecification.mandatoryFieldsAsArgs) {
            mandatoryArgs.add(new ArgumentSpecification(
                mandatoryArgSpec.name,
                visitSequenceType(mandatoryArgSpec.type).type,
                mandatoryArgSpec.defaultValue));
        }
        for (var optionalArgSpec : recordSpecification.optionalfieldsAsArgs) {
            mandatoryArgs.add(new ArgumentSpecification(
                optionalArgSpec.name,
                visitSequenceType(optionalArgSpec.type).type,
                optionalArgSpec.defaultValue));
        }

        mandatoryArgs.addAll(optionalArgs);
        final var itemRecordType = recordSpecification.isExtensible
            ? typeFactory.itemExtensibleRecord(fields)
            : typeFactory.itemRecord(fields);
        return new RecordResolutionResult(itemRecordType, mandatoryArgs);
    }



    private record UnresolvedRecordFieldSpecification(
        String name,
        SequenceTypeContext typeOrReferenceCtx,
        boolean isRequired
    ) {}

    private record UnresolvedArgumentSpecification(
        ParserRuleContext location,
        String name,
        SequenceTypeContext type,
        ParseTree defaultValue
    ) {}

    // private record UnresolvedFunctionSpecification(
    //     QualifiedName name,
    //     List<UnresolvedArgumentSpecification> mandatoryArgs,
    //     List<UnresolvedArgumentSpecification> optionalArgs,
    //     SequenceTypeContext returnedType
    // ){}


    private record UnresolvedRecordSpecification(
        NamedRecordTypeDeclContext location,
        QualifiedName name,
        List<UnresolvedRecordFieldSpecification> fields,
        List<UnresolvedArgumentSpecification> mandatoryFieldsAsArgs,
        List<UnresolvedArgumentSpecification> optionalfieldsAsArgs,
        boolean isExtensible
    ){}

    private UnresolvedRecordSpecification getUnresolvedRecord(
        final QualifiedName qName,
        final NamedRecordTypeDeclContext ctx
        )
    {
        final List<ExtendedFieldDeclarationContext> extendedFieldDeclaration = ctx.extendedFieldDeclaration();
        final int size = extendedFieldDeclaration.size();
        final List<UnresolvedRecordFieldSpecification> fields = new ArrayList<>(size);
        final List<UnresolvedArgumentSpecification> mandatoryArgs = new ArrayList<>(size);
        final List<UnresolvedArgumentSpecification> optionalArgs = new ArrayList<>(size);
        for (final ExtendedFieldDeclarationContext field : extendedFieldDeclaration) {
            final var fieldName = field.fieldDeclaration().fieldName().getText();
            final var fieldTypeCtx = field.fieldDeclaration().sequenceType();
            final boolean isRequired = field.fieldDeclaration().QUESTION_MARK() == null;
            final ExprSingleContext defaultExpr = field.exprSingle();
            fields.add(new UnresolvedRecordFieldSpecification(fieldName, fieldTypeCtx, isRequired));
            if (isRequired) {
                if (defaultExpr == null) {
                    mandatoryArgs.add(new UnresolvedArgumentSpecification(field, fieldName, fieldTypeCtx, null));
                }
                else {
                    optionalArgs.add(new UnresolvedArgumentSpecification(field, fieldName, fieldTypeCtx, defaultExpr));
                }
            } else {
                optionalArgs.add(new UnresolvedArgumentSpecification(field, fieldName, fieldTypeCtx, new HelperTrees().EMPTY_SEQUENCE));
            }
        }
        mandatoryArgs.addAll(optionalArgs);
        boolean isExtensible = ctx.extensibleFlag() != null;
        return new UnresolvedRecordSpecification(ctx, qName, fields, mandatoryArgs, optionalArgs, isExtensible);
    }


    @Override
    public TypeInContext visitNamedRecordTypeDecl(final NamedRecordTypeDeclContext ctx)
    {
        final var typeName = ctx.qname().getText();
        final var qName = namespaceResolver.resolveType(typeName);
        final RecordResolutionResult resolved = resolveRecordFromTypeDecl(qName, ctx);
        final var registrationResult = typeFactory.registerNamedType(qName, resolved.recordItemType);
        switch (registrationResult.status()) {
            case ALREADY_REGISTERED_DIFFERENT ->  {
                final var expr = registrationResult.registered();
                error(
                    ctx,
                    ErrorType.RECORD_DECLARATION__ALREADY_REGISTERED_DIFFERENT,
                    List.of(typeName, expr)
                    );
                return contextManager.typeInContext(typeFactory.one(registrationResult.registered()));
            }
            case ALREADY_REGISTERED_SAME ->  {
                error(
                    ctx,
                    ErrorType.RECORD_DECLARATION__ALREADY_REGISTERED_SAME,
                    List.of(typeName)
                    );
                return contextManager.typeInContext(typeFactory.one(registrationResult.registered()));
            }
            case OK -> {
                symbolManager.registerFunction(
                    qName.namespace(),
                    qName.name(),
                    resolved.fieldsAsArgs,
                    typeFactory.one(resolved.recordItemType));
                return contextManager.typeInContext(typeFactory.one(registrationResult.registered()));
            }
        }
        // unreachable
        return null;
    }


    XQueryAxis currentAxis;
    private final XQuerySequenceType zeroOrOneItem;

    private XQueryAxis saveAxis() {
        final var saved = currentAxis;
        currentAxis = null;
        return saved;
    }

    @Override
    public TypeInContext visitPathModuleImport(final PathModuleImportContext ctx) {
        final String pathQuery = stringContents(ctx.STRING());
        final var result = moduleManager.pathModuleImport(pathQuery);
        return handleModuleImport(ctx, result, "");
    }


    @Override
    public TypeInContext visitDefaultPathModuleImport(final DefaultPathModuleImportContext ctx)
    {
        final String namespace = ctx.qname().getText();
        if (symbolManager.namespaceExists(namespace)) {
            error(
                ctx,
                ErrorType.IMPORT_MODULE__DUPLICATE_IMPORT_BY_NAMESPACE,
                List.of(namespace)
                );
            return null;
        }
        final var result = moduleManager.defaultPathModuleImport(namespace);
        return handleModuleImport(ctx, result, namespace);
    }

    @Override
    public TypeInContext visitNamespaceModuleImport(final NamespaceModuleImportContext ctx) {
        final String namespace = ctx.namespacePrefix().qname().getText();
        if (symbolManager.namespaceExists(namespace)) {
            error(
                ctx,
                ErrorType.IMPORT_MODULE__DUPLICATE_IMPORT_BY_NAMESPACE,
                List.of(namespace)
                );
            return null;
        }
        final String pathQuery = stringContents(ctx.STRING());
        final var result = moduleManager.namespaceModuleImport(pathQuery);
        return handleModuleImport(ctx, result, namespace);
    }


    Map<String, XqueryContext> currentFileImportedModules = new HashMap<>();
    Map<String, XqueryContext> globalImportedModules = new HashMap<>();

    private TypeInContext handleModuleImport(final ParserRuleContext ctx, final ImportResult result, final String namespace) {
        switch (result.status()) {
            case NO_PATH_FOUND -> {
                final StringBuilder message = getNoPathMessageFromImport(result);
                error(ctx, ErrorType.IMPORT_MODULE__NO_PATH_FOUND, List.of(message));
            }
            case DUPLICATE_IMPORT -> {
                error(
                    ctx,
                    ErrorType.IMPORT_MODULE__DUPLICATE_IMPORT_BY_PATH,
                    List.of(result)
                    );
            }
            case MANY_VALID_PATHS -> {
                warn(ctx, WarningType.IMPORT_MODULE__MANY_VALID_PATHS, List.of(result.validPaths()));
                currentFileImportedModules.put(namespace, result.tree());
            }
            case OK -> {
                final var library = result.tree().libraryModule();
                if (library == null) {
                    error(
                        ctx,
                        ErrorType.IMPORT_MODULE__IMPORTED_MAIN_MODULE,
                        List.of(result));
                } else {
                    currentFileImportedModules.put(namespace, result.tree());
                }
            }
        }
        return null;
    }

    private StringBuilder getNoPathMessageFromImport(final ImportResult result) {
        final StringBuilder message = new StringBuilder("No path was found");
        int i = 0;
        for (final var p : result.resolvedPaths()) {
            switch(result.resolvingStatuses().get(i)) {
                case FOUND_OTHER_THAN_FILE -> {
                    message.append("\n\t");
                    message.append(p);
                    message.append(" is not a file");
                }
                case UNREADABLE -> {
                    message.append("\n\t");
                    message.append(p);
                    message.append(" cannot be read");
                }
                case OK, FILE_ALREADY_IMPORTED -> {
                }
            }
            i++;
        }
        return message;
    }

    private String stringContents(final TerminalNode ctx)
    {
        final var text = ctx.getText();
        return text.substring(1, text.length() - 1);
    }




    @Override
    public TypeInContext visitTypeswitchExpr(final TypeswitchExprContext ctx)
    {
        final var switched = visitExpr(ctx.expr());
        final var cases = ctx.bracedTypeswitchCases() != null
            ? ctx.bracedTypeswitchCases().typeswitchCases()
            : ctx.typeswitchCases()
            ;
        final var clauses = cases.caseClause();
        final List<XQuerySequenceType> types = new ArrayList<>();
        for (final var typeswitchCase : clauses) {
            for (final var typeCtx : typeswitchCase.sequenceTypeUnion().sequenceType()) {
                final var type = visitSequenceType(typeCtx);
                contextManager.enterScope();
                if (switched.type.isSubtypeOf(type.type)) {
                    if (typeswitchCase.varName() != null) {
                        final var caseVarName = cases.varName().qname().getText();
                        declareVariable(type, caseVarName, cases.varName());
                    }
                    final var evaluatedCase = visitExprSingle(typeswitchCase.exprSingle());
                    types.add(evaluatedCase.type);
                }
                contextManager.leaveScope();
            }
        }
        contextManager.enterScope();
        if (cases.varName() != null) {
            declareVariable(switched, cases.varName());
        }
        final var defaultType = visitExprSingle(cases.exprSingle());
        contextManager.leaveScope();
        types.add(defaultType.type);
        final XQuerySequenceType orElse = types.stream()
            .reduce(XQuerySequenceType::alternativeMerge)
            .orElse(zeroOrMoreItems);
        return contextManager.typeInContext(orElse);
    }

    @Override
    public TypeInContext visitContextValueDecl(final ContextValueDeclContext ctx) {
        if (ctx.EXTERNAL() != null) {
            // DECLARE CONTEXT VALUE (AS sequenceType)? EXTERNAL (EQ_OP varDefaultValue)?
            if (ctx.sequenceType() != null) {
                // DECLARE CONTEXT VALUE AS sequenceType EXTERNAL (EQ_OP varDefaultValue)?
                if (ctx.varDefaultValue() != null) {
                    // DECLARE CONTEXT VALUE AS sequenceType EXTERNAL EQ_OP varDefaultValue
                    final var declaredType = visitSequenceType(ctx.sequenceType());
                    final var defaultValueType = visitVarDefaultValue(ctx.varDefaultValue());
                    if (defaultValueType.type.coerceableTo(declaredType.type) == RelativeCoercability.NEVER) {
                        error(ctx, ErrorType.CONTEXT_VALUE_DECL__UNCOERSABLE, List.of(defaultValueType, declaredType));
                    }
                    context.setType(declaredType);
                } else {
                    // DECLARE CONTEXT VALUE AS sequenceType EXTERNAL
                    final var declaredType = visitSequenceType(ctx.sequenceType());
                    context.setType(declaredType);
                }
            } else {
                // DECLARE CONTEXT VALUE EXTERNAL (EQ_OP varDefaultValue)?
                if (ctx.varDefaultValue() != null) {
                    // DECLARE CONTEXT VALUE EXTERNAL EQ_OP varDefaultValue
                    final var defaultValueType = visitVarDefaultValue(ctx.varDefaultValue());
                    context.setType(defaultValueType);
                } else {
                    // DECLARE CONTEXT VALUE EXTERNAL
                }
            }
        } else {
            // DECLARE CONTEXT VALUE (AS sequenceType)? EQ_OP varValue
            if (ctx.sequenceType() != null) {
                // DECLARE CONTEXT VALUE AS sequenceType EQ_OP varValue
                final var declaredType = visitSequenceType(ctx.sequenceType());
                final var valueType = visitVarValue(ctx.varValue());
                if (valueType.type.coerceableTo(declaredType.type) == RelativeCoercability.NEVER) {
                    error(ctx, ErrorType.CONTEXT_VALUE_DECL__UNCOERSABLE, List.of(valueType, declaredType));
                }
                context.setType(declaredType);
            } else {
                // DECLARE CONTEXT VALUE EQ_OP varValue
                final var valueType = visitVarValue(ctx.varValue());
                context.setType(valueType);
            }

        }
        return null;
    }

}
