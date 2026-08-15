package com.github.akruk.antlrquery.semanticanalyzer.visitors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Predicate;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.github.akruk.Utils;
import com.github.akruk.antlrquery.*;
import com.github.akruk.antlrquery.semanticanalyzer.*;
import com.github.akruk.antlrquery.typesystem.typeoperations.*;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.*;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.akruk.antlrquery.AntlrQueryParser.*;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.semanticanalyzer.GrammarManager.GrammarFile;
import com.github.akruk.antlrquery.semanticanalyzer.ModuleManager.ImportResult;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.Assumption;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticScope.EntypingResult;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticScope.VariableInfo;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager.AnalysisResult;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager.ArgumentSpecification;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager.ModuleInfo;
import com.github.akruk.antlrquery.charescaper.AntlrQuerySemanticCharEscaper;
import com.github.akruk.antlrquery.charescaper.AntlrQuerySemanticCharEscaper.XQuerySemanticCharEscaperResult;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.inputgrammaranalyzer.InputGrammarAnalyzer;
import com.github.akruk.antlrquery.inputgrammaranalyzer.InputGrammarAnalyzer.QualifiedGrammarAnalysisResult;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.RecordField.TypeOrReference;
import com.github.akruk.antlrquery.typesystem.factories.CardinalityFactory;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory.NamedItemAccessingResult;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory.RegistrationResult;
import com.github.akruk.antlrquery.typesystem.typeoperations.SequenceTypePathOperator.GrammarStatus;
import com.github.akruk.antlrquery.typesystem.typeoperations.SequenceTypePathOperator.PathOperatorResult;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types.RelativeCoercibility;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

@DefaultQualifier(NonNull.class)
public class AntlrQuerySemanticAnalyzer extends AntlrQueryParserBaseVisitor<@Nullable TypeInContext>
{
    private final List<DiagnosticError> errors;
    private final List<DiagnosticWarning> warnings;
    private final AntlrQueryTypeFactory typeFactory;
    private final CardinalityVisitor cardinalityVisitor;
    private final AxisVisitor axisVisitor;
    private final TypeVisitor typeVisitor;
    private final AntlrQueryValueFactory valueFactory;
    private final SemanticSymbolManager symbolManager;
    private final SequenceTypePathOperator pathOperator;
    private final ModuleManager moduleManager;
    private final GrammarManager grammarManager;
    private final AntlrQuerySequenceType nonNegativeNumber;
    private final AntlrQuerySequenceType anyNumber;
    private final ItemTypeVisitor itemTypeVisitor;

    private VisitingSemanticContext context;
    private @Nullable List<TypeInContext> visitedPositionalArguments;
    private @Nullable Map<String, TypeInContext> visitedKeywordArguments;
    private NamespaceResolver namespaceResolver;

    protected final AntlrQuerySequenceType zeroOrMoreNodes;
    protected final AntlrQuerySequenceType anyArray;
    protected final AntlrQuerySequenceType anyMap;
    protected final AntlrQuerySequenceType boolean_;
    protected final AntlrQuerySequenceType string;
    protected final AntlrQuerySequenceType optionalNumber;
    protected final AntlrQuerySequenceType anyNumbers;
    protected final AntlrQuerySequenceType optionalString;
    protected final AntlrQuerySequenceType anyItem;
    protected final AntlrQuerySequenceType anyArrayOrMap;
    protected final AntlrQuerySequenceType zeroOrMoreItems;
    protected final AntlrQuerySequenceType emptySequence;
    protected final AntlrQuerySequenceType zeroOrMoreNumbers;

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

    public SemanticSymbolManager getSymbolManager() {
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
        return symbolManager.typeInContext(emptySequence);
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
        final ModuleInfo moduleInfo = new ModuleInfo(moduleNamespace, ctx.moduleDecl());
        for (final var listener : listeners) {
            listener.onModuleDeclaration(moduleInfo);
        }
        handleDefaultNamespaceDeclarations(
            p.defaultNamespaceDecl(),
            moduleNamespace,
            "",
            moduleNamespace,
            "",
            "");
        for (final DefaultNamespaceDeclContext defaultDeclaration : p.defaultNamespaceDecl()) {
            for (final var listener : listeners) {
                if (defaultDeclaration.qname().getText().startsWith(moduleNamespace)) {
                    listener.onModuleReference(
                        defaultDeclaration.qname(),
                        moduleInfo
                    );
                } else {
                    listener.onNamespaceReference(
                        defaultDeclaration.qname(),
                        symbolManager.getNamespace(defaultDeclaration.qname().getText())
                    );
                }
            }
        }

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
    public @Nullable TypeInContext visitNamespaceGrammarImport(final NamespaceGrammarImportContext ctx) {
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
            case NO_PATH_FOUND__NO_LEXER -> error(ctx, ErrorType.GRAMMAR_IMPORT__NO_LEXER, List.of(importResult));
            case NO_PATH_FOUND__NO_PARSER -> error(ctx, ErrorType.GRAMMAR_IMPORT__NO_PARSER, List.of(importResult));
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
            final UnresolvedRecordSpecification unresolvedRecord = getUnresolvedRecord(qName, r);
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


        for (final var r : recordsMapped.values()) {
            final RecordResolutionResult resolved = resolveRecord(r.name, r);
            symbolManager.registerFunction(
                r.name.namespace(),
                r.name.name(),
                resolved.fieldsAsArgs,
                typeFactory.one(resolved.recordItemType));
        }


        final List<UnresolvedFunctionSpecification> unresolvedFunctions = new ArrayList<>(importedFunctions.size());

        for (final FunctionDeclContext function : functions) {
            final QualifiedName qName = namespaceResolver.resolveFunction(function.qname().getText());
            validateFunctionNamespace(moduleNamespace, function, qName);
            final UnresolvedFunctionSpecification spec = getUnresolvedFunction(qName, function);
            final boolean isValid = validateUnresolvedFunction(spec);
            if (isValid) {
                final var declarationResult = symbolManager.declareFunction(spec);
                switch(declarationResult.status()) {
                    case COLLISION -> error(
                        function,
                        ErrorType.FUNCTION__ARITY_COLLISION,
                        List.of(qName, spec.minArity, spec.maxArity, declarationResult.collisions())
                    );
                    case OK -> {
                    }
                }
            }
            unresolvedFunctions.add(spec);
        }


        for (final FunctionDeclContext f : importedFunctions) {
            final QualifiedName qName = namespaceResolver.resolveFunction(f.qname().getText());
            final UnresolvedFunctionSpecification spec = getUnresolvedFunction(qName, f);
            final boolean isValid = validateUnresolvedFunction(spec);
            if (isValid) {
                final var declarationResult = symbolManager.declareFunction(spec);
                switch(declarationResult.status()) {
                    case COLLISION -> error(f, ErrorType.FUNCTION__ARITY_COLLISION, List.of(qName, spec.minArity, spec.maxArity, declarationResult.collisions()) );
                    case OK -> {
                    }
                }
            }
            unresolvedFunctions.add(spec);
        }

        for (final UnresolvedFunctionSpecification spec : unresolvedFunctions) {
            resolveFunction(spec);
        }



    }

    private boolean isBuiltInType(final QualifiedName qName) {
        if (!qName.namespace().isEmpty()) {
            return false;
        }
        return switch(qName.name()) {
            case "string", "number", "boolean", "true", "false" -> true;
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
            error(ctxValueDecl, ErrorType.CONTEXT_VALUE_DECL__NOT_IN_MAIN_MODULE, List.of());
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



    private enum DefaultNamespaceDeclType {
        FUNCTION,
        TYPE,
        ELEMENT,
        CONSTRUCTION,
        ANNOTATION
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
        final Map<DefaultNamespaceDeclType, List<DefaultNamespaceDeclContext>> splitByType =
            defaultNamespaceDecls
            .stream()
            .collect(Collectors.groupingBy(
                (final DefaultNamespaceDeclContext t) -> {
                    if (t.FUNCTION() != null) {
                        return DefaultNamespaceDeclType.FUNCTION;
                    } else if (t.ELEMENT() != null) {
                        return DefaultNamespaceDeclType.ELEMENT;
                    } else if (t.TYPE() != null) {
                        return DefaultNamespaceDeclType.TYPE;
                    } else if (t.CONSTRUCTION() != null) {
                        return DefaultNamespaceDeclType.CONSTRUCTION;
                    } else {
                        return DefaultNamespaceDeclType.ANNOTATION;
                    }
                }
            ));

        final List<DefaultNamespaceDeclContext> functionDecls = splitByType.getOrDefault(DefaultNamespaceDeclType.FUNCTION, List.of());
        final String defaultFunctionNamespace = validateDefaultFunctionNamespace(moduleFunctionNamespace, functionDecls);
        symbolManager.provideNamespace(defaultFunctionNamespace);

        final List<DefaultNamespaceDeclContext> typeDecls = splitByType.getOrDefault(DefaultNamespaceDeclType.TYPE, List.of());
        final String defaultTypeNamespace = validateDefaultTypeNamespace(moduleTypeNamespace, typeDecls);
        symbolManager.provideNamespace(defaultTypeNamespace);

        final List<DefaultNamespaceDeclContext> elementDecls = splitByType.getOrDefault(DefaultNamespaceDeclType.ELEMENT, List.of());
        final String defaultElementNamespace = validateDefaultElementNamespace(moduleElementNamespace, elementDecls);

        final List<DefaultNamespaceDeclContext> annotationDecls = splitByType.getOrDefault(DefaultNamespaceDeclType.ANNOTATION, List.of());
        final String defaultAnnotationNamespace = validateDefaultAnnotationNamespace(moduleAnnotationNamespace, annotationDecls);
        symbolManager.provideNamespace(defaultAnnotationNamespace);

        final List<DefaultNamespaceDeclContext> constructionDecls = splitByType.getOrDefault(DefaultNamespaceDeclType.CONSTRUCTION, List.of());
        final String defaultConstructionNamespace = validateDefaultConstructionNamespace(moduleConstructionNamespace, constructionDecls);


        namespaceResolver = new NamespaceResolver(
            defaultFunctionNamespace,
            defaultTypeNamespace,
            defaultElementNamespace,
            defaultConstructionNamespace,
            defaultAnnotationNamespace
            );
    }

    private String validateDefaultFunctionNamespace(
        final String moduleFunctionNamespace,
        final List<DefaultNamespaceDeclContext> functionDecls)
    {
        return switch(functionDecls.size())
        {
            case 0 -> moduleFunctionNamespace;
            case 1 -> functionDecls.getFirst().qname().getText();
            default -> {
                for (final var d : functionDecls) {
                    error(d, ErrorType.DEFAULT_NAMESPACE_DECL__MULTIPLE_FUNCTION_NAMESPACE_DECLARATIONS, List.of());
                }
                yield moduleFunctionNamespace;
            }
        };
    }

    private String validateDefaultAnnotationNamespace(
        final String moduleAnnotationNamespace,
        final List<DefaultNamespaceDeclContext> AnnotationDecls)
    {
        return switch(AnnotationDecls.size())
        {
            case 0 -> moduleAnnotationNamespace;
            case 1 -> AnnotationDecls.getFirst().qname().getText();
            default -> {
                for (final var d : AnnotationDecls) {
                    error(d, ErrorType.DEFAULT_NAMESPACE_DECL__MULTIPLE_ANNOTATION_NAMESPACE_DECLARATIONS, List.of());
                }
                yield moduleAnnotationNamespace;
            }
        };
    }

    private String validateDefaultConstructionNamespace(
        final String moduleConstructionNamespace,
        final List<DefaultNamespaceDeclContext> ConstructionDecls)
    {
        final String defaultConstructionNamespace = switch(ConstructionDecls.size())
        {
            case 0 -> moduleConstructionNamespace;
            case 1 -> {
                final var defaultNamespace = ConstructionDecls.getFirst().qname().getText();
                if (symbolManager.grammarExists(defaultNamespace)) {

                }
                yield defaultNamespace;
            }
            default -> {
                for (final var d : ConstructionDecls) {
                    error(d, ErrorType.DEFAULT_NAMESPACE_DECL__MULTIPLE_CONSTRUCTION_NAMESPACE_DECLARATIONS, List.of());
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
        return switch(elementDecls.size())
        {
            case 0 -> moduleElementNamespace;
            case 1 -> elementDecls.getFirst().qname().getText();
            default -> {
                for (final var d : elementDecls) {
                    error( d, ErrorType.DEFAULT_NAMESPACE_DECL__MULTIPLE_ELEMENT_NAMESPACE_DECLARATIONS, List.of());
                }
                yield moduleElementNamespace;
            }
        };
    }

    private String validateDefaultTypeNamespace(
        final String moduleTypeNamespace,
        final List<DefaultNamespaceDeclContext> typeDecls
        )
    {
        return switch(typeDecls.size())
        {
            case 0 -> moduleTypeNamespace;
            case 1 -> typeDecls.getFirst().qname().getText();
            default -> {
                for (final var d : typeDecls) {
                    error(d, ErrorType.DEFAULT_NAMESPACE_DECL__MULTIPLE_TYPE_NAMESPACE_DECLARATIONS, List.of());
                }
                yield moduleTypeNamespace;
            }
        };
    }

    private void handleContextValueDeclarations(final PrologContext p) {
        switch (p.contextValueDecl().size()) {
            case 0 -> {}// set in constructor
            case 1 -> visitContextValueDecl(p.contextValueDecl(0));
            default -> {
                for (final var ctxValueDecl : p.contextValueDecl()) {
                    error(ctxValueDecl, ErrorType.CONTEXT_VALUE_DECL__MULTIPLE_DECLARATIONS, List.of());
                }
            }
        }
    }


    String currentUri;

    public AntlrQuerySemanticAnalyzer(
        final Parser antlrQueryParser,
        final AntlrQueryTypeFactory typeFactory,
        final AntlrQueryValueFactory valueFactory,
        final SemanticSymbolManager symbolManager,
        final @Nullable Map<String, QualifiedGrammarAnalysisResult> importedGrammars,
        final ModuleManager moduleManager,
        final GrammarManager grammarManager,
        final AntlrQuerySequenceType contextType,
        final String startingUri,
        final Map<String, AntlrQuerySequenceType> variables, 
        final AxisVisitor axisVisitor, 
        final CardinalityFactory cardinalityFactory,
        final CardinalityVisitor cardinalityVisitor,
        final TypeVisitor typeVisitor,
        final ItemTypeVisitor itemTypeVisitor,
        final NamespaceResolver resolver
        )
    {
        this.currentUri = startingUri;
        this.typeFactory = typeFactory;
        this.cardinalityVisitor = cardinalityVisitor;
        this.axisVisitor = axisVisitor;
        this.typeVisitor = typeVisitor;
        this.itemTypeVisitor = itemTypeVisitor;
        this.valueFactory = valueFactory;
        this.symbolManager = symbolManager;
        this.symbolManager.setAnalyzer(this);
        this.symbolManager.enterContext();
        this.context = new VisitingSemanticContext();
        this.context.setType(symbolManager.typeInContext(contextType));
        this.context.setPositionType(null);
        this.context.setSizeType(null);
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.namespaceResolver = resolver;
        itemTypeVisitor.setNamespaceResolver(resolver);

        this.anyArrayOrMap = typeFactory.zeroOrMore(typeFactory.itemChoice(typeFactory.itemAnyMap(), typeFactory.itemAnyArray()));
        this.zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        this.emptySequence = typeFactory.emptySequence();
        this.anyNumber = typeFactory.number(NumericRange.FULL);
        this.nonNegativeNumber = typeFactory.number(NumericRange.NON_NEGATIVE);
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
            for (final String grammarName : importedGrammars.keySet()) {
                symbolManager.registerGrammar(grammarName, importedGrammars.get(grammarName));
            }
        }
        this.atomizer = new SequencetypeAtomization(typeFactory);
        this.castability = new SequenceTypeCastable(typeFactory);
        this.anyNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        this.pathOperator = new SequenceTypePathOperator(
            typeFactory,
            symbolManager);
        this.moduleManager = moduleManager;
        this.grammarManager = grammarManager;
        zeroOrMoreNumbers = typeFactory.zeroOrMore(typeFactory.itemNumber());
        for (final String variableName : variables.keySet()) {
            final AntlrQuerySequenceType variableType = variables.get(variableName);
            symbolManager.entypeVariable(
                variableName,
                null,
                null,
                symbolManager.typeInContext(variableType)
                );
        }


        lookupOperation = new LookupOperation(typeFactory);
    }

    @Override
    public TypeInContext visitFLWORExpr(final FLWORExprContext ctx)
    {
        final var saveReturnedOccurence = saveReturnedCardinality();
        symbolManager.enterScope();
        visitInitialClause(ctx.initialClause());
        for (final var clause : ctx.intermediateClause()) {
            clause.accept(this);
        }
        // at this point visitedTupleStream should contain all tuples
        final var expressionValue = visitReturnClause(ctx.returnClause());
        symbolManager.leaveScope();
        returnedCardinality = saveReturnedOccurence;
        return expressionValue;
    }

    private Cardinality returnedCardinality = Cardinality.ONE;

    private Cardinality saveReturnedCardinality()
    {
        final var saved = returnedCardinality;
        returnedCardinality = Cardinality.ONE;
        return saved;
    }

    @Override
    public @Nullable TypeInContext visitLetClause(final LetClauseContext ctx)
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
        final TypeInContext assignedValue = Objects.requireNonNull(visitExprSingle(assignedValueCtx));
        if (varNameAndType.typeDeclaration() == null) {
            declareVariable(assignedValue, variableName, varNameAndType.varName());
        } else {
            final AntlrQuerySequenceType type = varNameAndType.typeDeclaration().accept(typeVisitor);
            if (!assignedValue.isSubtypeOf(type)) {
                error(ctx, ErrorType.LOOKUP__INVALID_TARGET, List.of(variableName, assignedValue, type));
            }
            declareVariable(symbolManager.typeInContext(type), variableName, varNameAndType.varName());
        }
    }

    private void declareVariable(final TypeInContext type, final VarNameContext varNameCtx) {
        final String varName = varNameCtx.qname().getText();
        declareVariable(type, varName, varNameCtx);
    }

    private void declareVariable(final TypeInContext type, final String varName, final @Nullable VarNameContext varNameCtx) {
        final EntypingResult entypingresult = symbolManager.entypeVariable(
            varName,
            varNameCtx,
            new Location(currentUri, getContextRange(varNameCtx)),
            type
            );
        for (final var listener : listeners) {
            listener.onVariableDeclaration(entypingresult.newVariable());
        }
    }


    Range getContextRange(final ParserRuleContext ctx) {
        final Token startToken = ctx.getStart();
        final Token stopToken = ctx.getStop();

        final Position start = new Position(
            startToken.getLine() - 1,
            startToken.getCharPositionInLine()
            );
        final Position end = new Position(
            stopToken.getLine() - 1,
            stopToken.getCharPositionInLine() + stopToken.getText().length()
            );

        return new Range(start, end);
    }



    @Override
    public @Nullable TypeInContext visitForClause(final ForClauseContext ctx) {
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
    public @Nullable TypeInContext visitTumblingWindowClause(final TumblingWindowClauseContext ctx) {
        final var iteratedType = Objects.requireNonNull(visitExprSingle(ctx.exprSingle()));
        final var iterator = symbolManager.typeInContext(iteratedType.iteratorType());
        final var optionalIterator = symbolManager.typeInContext(Types.optionalize(typeFactory, iterator.type));
        final String windowVariableName = ctx.varNameAndType().varName().qname().getText();
        final TypeInContext windowSequenceType = symbolManager.typeInContext(typeFactory.oneOrMore(iterator.type.itemType()));

        returnedCardinality = Cardinalities.multiply(returnedCardinality, Cardinality.ZERO_OR_MORE); // TODO: use array cardinality instead of zero_or_more
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
            // TODO: Refine number value range
            declareVariable(symbolManager.typeInContext(nonNegativeNumber), currentVarPos.varName());
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
    public @Nullable TypeInContext visitSlidingWindowClause(final SlidingWindowClauseContext ctx) {
        final TypeInContext iteratedType = Objects.requireNonNull(visitExprSingle(ctx.exprSingle()));
        final var iterator = symbolManager.typeInContext(iteratedType.iteratorType());
        final var optionalIterator = symbolManager.typeInContext(Types.optionalize(typeFactory, iterator.type));
        final String windowVariableName = ctx.varNameAndType().varName().qname().getText();
        final TypeInContext windowSequenceType = symbolManager.typeInContext(typeFactory.oneOrMore(iterator.type.itemType()));

        returnedCardinality = Cardinalities.multiply(returnedCardinality, Cardinality.ZERO_OR_MORE); // TODO: use array cardinality instead of zero_or_more
        handleWindowStartClause(ctx.windowStartCondition(), iterator, optionalIterator);
        handleWindowEndClause(ctx.windowEndCondition(), iterator, optionalIterator);
        handleWindowIterator(ctx.varNameAndType(), windowVariableName, windowSequenceType);
        return null;
    }

    private void handleWindowIterator(final VarNameAndTypeContext ctx, final String windowVariableName,
            final TypeInContext windowSequenceType) {
        if (ctx.typeDeclaration() != null) {
            final TypeInContext windowDeclaredVarType = symbolManager.typeInContext(
                    typeVisitor.visitTypeDeclaration(ctx.typeDeclaration()));
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
                if (Types.hasNoEffectiveBooleanValue(typeFactory, conditionType.type)) {
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
                if (Types.hasNoEffectiveBooleanValue(typeFactory, conditionType.type)) {
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
                groupingVarsCtx.add(gs.varNameAndType().varName());
                groupingVars.add(gs.varNameAndType().varName().qname().getText());
            } else {
                final VarNameContext varName2 = gs.varNameAndType().varName();
                final String varname = varName2.qname().getText();
                final VariableInfo variable = symbolManager.getVariable(varname);
                TypeInContext variableType = variable.type();
                if (variableType == null) {
                    error(
                        varName2,
                        ErrorType.GROUP_BY__UNDEFINED_GROUPING_VARIABLE,
                        List.of(varname)
                        );
                    variableType = symbolManager.typeInContext(zeroOrMoreItems);
                }
                final AntlrQuerySequenceType atomizedType = atomizer.atomize(variableType.type);
                if (!Types.isSubtype(typeFactory, atomizedType, zeroOrOneItem)) {
                    error(
                        varName2,
                        ErrorType.GROUP_BY__WRONG_GROUPING_VAR_TYPE,
                        List.of(varname, zeroOrOneItem, atomizedType)
                        );

                }
                declareVariable(symbolManager.typeInContext(Types.iteratorType(typeFactory, atomizedType)), varname, varName2);
                if (groupingVars.contains(varname)) {
                    error(varName2, ErrorType.GROUP_BY__DUPLICATED_VAR, List.of(varname));
                } else {
                    groupingVars.add(varname);
                    groupingVarsCtx.add(varName2);
                }
            }
        }
        assert !groupingVarsCtx.isEmpty();
        final Set<Entry<String, VariableInfo>> variablesInContext = symbolManager.currentContext().getVariables().entrySet();
        int i = 0;
        for (final var variableNameAndType : variablesInContext) {
            final String varName = variableNameAndType.getKey();
            if (groupingVars.contains(varName)) {
                continue;
            }
            final var varType = variableNameAndType.getValue();
            declareVariable(
                    symbolManager.typeInContext(Types.optionalize(typeFactory, varType.type().type)),
                    varName,
                    groupingVarsCtx.get(i));
            i++;
        }
        return null;
    }

    public void processForItemBinding(final ForItemBindingContext ctx) {
        final String variableName = ctx.varNameAndType().varName().qname().getText();
        final TypeInContext sequenceType = ctx.exprSingle().accept(this);
        returnedCardinality = Cardinalities.multiply(sequenceType.type.cardinality(), returnedCardinality);

        checkPositionalVariableDistinct(ctx.positionalVar(), variableName, ctx);

        final AntlrQueryItemType itemType = sequenceType.type.itemType();
        final AntlrQuerySequenceType iteratorType = (ctx.allowingEmpty() != null)
                ? typeFactory.zeroOrOne(itemType)
                : typeFactory.one(itemType);

        processVariableTypeDeclaration(ctx.varNameAndType(), symbolManager.typeInContext(iteratorType), variableName, ctx);

        if (ctx.positionalVar() != null) {
            // TODO: Refine number value range
            final String positionalVariableName = ctx.positionalVar().varName().qname().getText();
            declareVariable(symbolManager.typeInContext(nonNegativeNumber), positionalVariableName, ctx.positionalVar().varName());
        }
    }

    public void processForMemberBinding(final ForMemberBindingContext ctx) {
        final String variableName = ctx.varNameAndType().varName().qname().getText();
        final TypeInContext arrayType = ctx.exprSingle().accept(this);
        returnedCardinality = Cardinalities.multiply(returnedCardinality, Cardinality.ZERO_OR_MORE); // TODO: use array cardinality instead of zero_or_more

        @Nullable AntlrQuerySequenceType memberType = Types.getMemberType(typeFactory, arrayType.type.itemType());
        if (memberType == null)
            error(ctx, ErrorType.FOR_MEMBER__WRONG_ITERABLE_TYPE, List.of(arrayType));


        checkPositionalVariableDistinct(ctx.positionalVar(), variableName, ctx);

        processVariableTypeDeclaration(ctx.varNameAndType(), symbolManager.typeInContext(memberType), variableName, ctx);

        if (ctx.positionalVar() != null) {
            // TODO: refine number value range
            final String positionalVariableName = ctx.positionalVar().varName().qname().getText();
            declareVariable(symbolManager.typeInContext(nonNegativeNumber), positionalVariableName, ctx.positionalVar().varName());
        }
    }

    public void processForEntryBinding(final ForEntryBindingContext ctx) {
        final TypeInContext mapType = ctx.exprSingle().accept(this);
        returnedCardinality = Cardinalities.multiply(returnedCardinality, Cardinality.ZERO_OR_MORE); // TODO: use array cardinality instead of zero_or_more

        final @Nullable AntlrQueryItemType keyType = Types.getMapKey(typeFactory, mapType.type.itemType());
        final @Nullable AntlrQuerySequenceType valueType = Types.getMapValue(typeFactory, mapType.type.itemType());
        if (keyType == null || valueType == null) {
            error(ctx, ErrorType.FOR_ENTRY__WRONG_ITERABLE_TYPE, List.of());
            return;
        }

        final @Nullable ForEntryKeyBindingContext keyBinding = ctx.forEntryKeyBinding();
        final @Nullable ForEntryValueBindingContext valueBinding = ctx.forEntryValueBinding();

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
            final AntlrQuerySequenceType keyIteratorType = typeFactory.one(keyType);

            checkPositionalVariableDistinct(ctx.positionalVar(), keyVariableName, ctx);
            processVariableTypeDeclaration(keyBinding.varNameAndType(), symbolManager.typeInContext(keyIteratorType), keyVariableName, ctx);
        }

        // Process value binding
        if (valueBinding != null) {
            final String valueVariableName = valueBinding.varNameAndType().varName().qname().getText();

            checkPositionalVariableDistinct(ctx.positionalVar(), valueVariableName, ctx);
            processVariableTypeDeclaration(valueBinding.varNameAndType(), symbolManager.typeInContext(valueType), valueVariableName, ctx);
        }

        if (ctx.positionalVar() != null) {
            // TODO: refine number value range
            final String positionalVariableName = ctx.positionalVar().varName().qname().getText();
            declareVariable(symbolManager.typeInContext(nonNegativeNumber), positionalVariableName, ctx.positionalVar().varName());
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

        final TypeInContext declaredType = symbolManager.typeInContext(
                typeVisitor.visitTypeDeclaration(varNameAndType.typeDeclaration()));
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
    public TypeInContext visitCountClause(final CountClauseContext ctx)
    {
        // TODO: refine value range
        declareVariable(symbolManager.typeInContext(nonNegativeNumber), ctx.varName());
        return symbolManager.typeInContext(nonNegativeNumber);
    }

    @Override
    public TypeInContext visitWhereClause(final WhereClauseContext ctx)
    {
        final var filteringExpression = ctx.exprSingle();
        final var filteringExpressionType = filteringExpression.accept(this);
        if (Types.hasNoEffectiveBooleanValue(typeFactory, filteringExpressionType.type)) {
            error(filteringExpression, ErrorType.FILTERING__EXPR_NOT_EBV, List.of(filteringExpressionType));
        }
        returnedCardinality = Cardinalities.optionalize(returnedCardinality);
        return null;
    }

    @Override
    public TypeInContext visitVarRef(final VarRefContext ctx)
    {
        final String variableName = ctx.qname().getText();
        final VariableInfo variable = symbolManager.getVariable(variableName);
        final TypeInContext variableType = variable.type();
        if (variableType == null) {
            error(ctx.qname(), ErrorType.VAR_REF__UNDECLARED, List.of(variableName));
            return symbolManager.typeInContext(zeroOrMoreItems);
        } else {
            for (final var l : listeners) {
                l.onVariableReference(ctx, variable);
            }
            return variableType;
        }
    }
    
    @Override
    public TypeInContext visitReturnClause(final ReturnClauseContext ctx)
    {
        final var type = ctx.exprSingle().accept(this);
        final var itemType = type.type.itemType();
        returnedCardinality = Cardinalities.multiply(returnedCardinality, type.type.cardinality());
        // returnedOccurrence = mergeFLWOROccurrence(type.type);
        final var sequenceType = typeFactory.sequence(itemType, returnedCardinality);
        return symbolManager.typeInContext(sequenceType);
    }

    @Override
    public TypeInContext visitWhileClause(final WhileClauseContext ctx)
    {
        final var filteringExpression = ctx.exprSingle();
        final var filteringExpressionType = filteringExpression.accept(this);
        if (Types.hasNoEffectiveBooleanValue(typeFactory, filteringExpressionType.type)) {
            error(filteringExpression, ErrorType.FILTERING__EXPR_NOT_EBV, List.of(filteringExpressionType));
        }
        returnedCardinality = Cardinalities.optionalize(returnedCardinality);
        return null;
    }

    @Override
    public @Nullable TypeInContext visitIntegerLiteral(IntegerLiteralContext ctx) {
        final String value = ctx.getText().replace("_", "");
        var aqValue = valueFactory.number(new BigDecimal(value));
        return symbolManager.typeInContext(aqValue.type);
    }

    @Override
    public @Nullable TypeInContext visitHexIntegerLiteral(HexIntegerLiteralContext ctx) {
        final String raw = ctx.getText();
        final String hex = raw.replace("_", "").substring(2);
        var aqValue = valueFactory.number(new BigDecimal(new java.math.BigInteger(hex, 16)));
        return symbolManager.typeInContext(aqValue.type);
    }

    @Override
    public @Nullable TypeInContext visitBinaryIntegerLiteral(BinaryIntegerLiteralContext ctx) {
        final String raw = ctx.getText();
        final String binary = raw.replace("_", "").substring(2);
        var aqValue = valueFactory.number(new BigDecimal(new java.math.BigInteger(binary, 2)));
        return symbolManager.typeInContext(aqValue.type);
    }

    @Override
    public @Nullable TypeInContext visitDecimalLiteral(DecimalLiteralContext ctx) {
        final String cleaned = ctx.getText().replace("_", "");
        var aqValue = valueFactory.number(new BigDecimal(cleaned));
        return symbolManager.typeInContext(aqValue.type);
    }

    @Override
    public @Nullable TypeInContext visitDoubleLiteral(DoubleLiteralContext ctx) {
        final String cleaned = ctx.getText().replace("_", "");
        var aqValue = valueFactory.number(new BigDecimal(cleaned));
        return symbolManager.typeInContext(aqValue.type);
    }

    @Override
    public TypeInContext visitLiteral(final LiteralContext ctx)
    {
        if (ctx.STRING() != null) {
            return handleString(ctx);
        }
        return Objects.requireNonNull(ctx.numericLiteral().accept(this));
    }

    private TypeInContext handleString(final ParserRuleContext ctx) {
        final String content = processStringLiteral(ctx);
        return symbolManager.typeInContext(typeFactory.enum_(Set.of(content)));
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
            return symbolManager.typeInContext(emptySequence);
        }
        return ctx.expr().accept(this);
    }

    @Override
    public TypeInContext visitExpr(final ExprContext ctx)
    {
        // Only one expression
        // e.g. 13
        if (ctx.exprSingle().size() == 1) {
            return visitExprSingle(ctx.exprSingle(0));
        }
        // More than one expression
        final var previousExpr = ctx.exprSingle(0);
        var previousExprType = visitExprSingle(previousExpr).type;
        final int size = ctx.exprSingle().size();
        for (int i = 1; i < size; i++) {
            final var exprSingle = ctx.exprSingle(i);
            final TypeInContext expressionType = exprSingle.accept(this);
            previousExprType = Types.addition(typeFactory, previousExprType, expressionType.type);
        }
        return symbolManager.typeInContext(previousExprType);
    }

    private String unescapeString(final ParserRuleContext where, final String str)
    {
        final var charEscaper = new AntlrQuerySemanticCharEscaper();
        final XQuerySemanticCharEscaperResult result = charEscaper.escapeWithDiagnostics(where, str);
        errors.addAll(result.errors());
        return result.unescaped();
    }


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
        final var resolution = namespaceResolver.resolveFunction(functionQname);

        symbolManager.enterContext();
        final AnalysisResult callAnalysisResult = symbolManager.call(
            ctx, resolution, args,
            kwargs, context, symbolManager.currentContext());
        symbolManager.leaveContext();
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

        final List<@Nullable AntlrQuerySequenceType> coercedTypes = quantifierBindings.stream()
                .map(binding -> {
                    final TypeDeclarationContext typeDeclaration = binding.varNameAndType().typeDeclaration();
                    return typeDeclaration != null? typeDeclaration.accept(typeVisitor) : null;
                })
                .toList();

        final List<AntlrQuerySequenceType> variableTypes = quantifierBindings.stream()
                .map(binding -> Objects.requireNonNull(binding.exprSingle().accept(this)).type)
                .toList();

        final ExprSingleContext criterionNode = ctx.exprSingle();

        for (int i = 0; i < variableNames.size(); i++) {
            final AntlrQuerySequenceType assignedType = variableTypes.get(i);
            final @Nullable AntlrQuerySequenceType desiredType = coercedTypes.get(i);
            if (desiredType !=null) {
                if (Types.coercibility(typeFactory, assignedType, desiredType) == RelativeCoercibility.NEVER){
                    error(
                        ctx.quantifierBinding(i).varNameAndType(),
                        ErrorType.VAR_DECL_WITH_COERSION__INVALID,
                        List.of(assignedType, desiredType));
                }
                declareVariable(symbolManager.typeInContext(desiredType), variableNames.get(i), variableNameCtxs.get(i));
                continue;
            }
            declareVariable(symbolManager.typeInContext(Types.iteratorType(typeFactory, assignedType)), variableNames.get(i), variableNameCtxs.get(i));
        }

        final AntlrQuerySequenceType queriedType = Objects.requireNonNull(criterionNode.accept(this)).type;
        if (Types.hasNoEffectiveBooleanValue(typeFactory, queriedType)) {
            error(criterionNode, ErrorType.QUANTIFIED__CRITERION_NON_EBV, List.of(queriedType));
        }

        return symbolManager.typeInContext(boolean_);
    }

    @Override
    public TypeInContext visitOrExpr(final OrExprContext ctx)
    {
        if (ctx.OR().isEmpty()) {
            return visitAndExpr(ctx.andExpr(0));
        }
        final var orCount = ctx.OR().size();
        for (int i = 0; i <= orCount; i++) {
            final var visitedType = ctx.andExpr(i).accept(this);
            if (Types.hasNoEffectiveBooleanValue(typeFactory, visitedType.type)) {
                error(ctx.andExpr(i), ErrorType.OR__NON_EBV, List.of(visitedType));
            }
        }
        // symbolManager.currentScope().imply(andBool);
        return symbolManager.typeInContext(boolean_);
    }

    @Override
    public TypeInContext visitRangeExpr(final RangeExprContext ctx)
    {
        AdditiveExprContext fromExprCtx = ctx.additiveExpr(0);
        if (ctx.TO() == null) {
            return visitAdditiveExpr(fromExprCtx);
        }
        final AdditiveExprContext toExprCtx = ctx.additiveExpr(1);
        final TypeInContext fromValue = visitAdditiveExpr(fromExprCtx);
        final TypeInContext toValue = visitAdditiveExpr(toExprCtx);
        final boolean validFrom = Types.isSubtype(typeFactory, fromValue.type, optionalNumber);
        final boolean validTo = Types.isSubtype(typeFactory, toValue.type, optionalNumber);
        if (!validFrom && !validTo) {
            error(ctx, ErrorType.RANGE__INVALID_BOTH, List.of(fromValue, toValue));
            return symbolManager.typeInContext(anyNumbers);
        } else if (!validFrom) {
            error(fromExprCtx, ErrorType.RANGE__INVALID_FROM, List.of(fromValue));
            return symbolManager.typeInContext(anyNumbers);
        } else if (!validTo) {
            error(toExprCtx, ErrorType.RANGE__INVALID_TO, List.of(toValue));
            return symbolManager.typeInContext(anyNumbers);
        }
        final boolean fromIsEmpty = fromValue.type.equals(emptySequence);
        final boolean toIsEmpty = toValue.type.equals(emptySequence);
        if (fromIsEmpty && toIsEmpty) {
            warn(fromExprCtx, WarningType.RANGE__EMPTY_BOTH, List.of());
            return symbolManager.typeInContext(emptySequence);
        }
        if (fromIsEmpty) {
            warn(fromExprCtx, WarningType.RANGE__EMPTY_FROM, List.of());
            return symbolManager.typeInContext(emptySequence);
        }
        if (toIsEmpty) {
            warn(toExprCtx, WarningType.RANGE__EMPTY_TO, List.of());
            return symbolManager.typeInContext(emptySequence);
        }
        if (!(fromValue.type.itemType() instanceof AtomicType.NumberType(NumericRange r1)
                && toValue.type.itemType() instanceof AtomicType.NumberType(NumericRange r2))
            )
        {
            throw new IllegalStateException("All other variants should have already been excluded");
        }

//        Nulls shouldn't be possible because NumericRanges shouldn't contain empty NumericRanges
        NumericRange.Event min1 = Objects.requireNonNull(Ranges.min(r1));
        NumericRange.Event max2 = Objects.requireNonNull(Ranges.max(r2));
//      e.g. [9, 10] to  [5, 8] will always produce empty sequences
        if (min1.value().compareTo(max2.value()) > 0) {
            warn(ctx, WarningType.RANGE__ALWAYS_EMPTY, List.of());
        }
        var numbers = Ranges.integers(NumericRange.of(min1, max2));
        boolean emptyPossible = Cardinalities.contains(fromValue.type.cardinality(), BigInteger.ZERO)
            || Cardinalities.contains(toValue.type.cardinality(), BigInteger.ZERO);

        var numberItemType = typeFactory.itemNumber(numbers);
        var type = emptyPossible
                ? typeFactory.zeroOrOne(numberItemType)
                : typeFactory.one(numberItemType);

        return symbolManager.typeInContext(type);
    }

    @Override
    public @Nullable TypeInContext visitPathExpr(final PathExprContext ctx)
    {
        final boolean pathExpressionFromRoot = ctx.SLASH() != null;
        if (pathExpressionFromRoot) {
            final var savedAxis = saveAxis();
            contextTypeMustBeAnyNodes(ctx);
            currentAxis = AntlrQueryAxis.CHILD;
            final @Nullable TypeInContext resultingNodeSequence = ctx.relativePathExpr().accept(this);
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        final boolean useDescendantOrSelfAxis = ctx.SLASHES() != null;
        if (useDescendantOrSelfAxis) {
            final var savedAxis = saveAxis();
            contextTypeMustBeAnyNodes(ctx);
            currentAxis = AntlrQueryAxis.DESCENDANT_OR_SELF;
            final @Nullable TypeInContext resultingNodeSequence = ctx.relativePathExpr().accept(this);
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        return visitRelativePathExpr(ctx.relativePathExpr());
    }

    @Override
    public TypeInContext visitNodeTest(final NodeTestContext ctx)
    {
        final AntlrQuerySequenceType nodeType = context.getType().type;
        final PathOperatorResult result = getOperatorPathResultFromTree(ctx, nodeType);

        // reporting input status
        switch(result.inputStatus()) {
            case EMPTY_SEQUENCE -> warn(
                ctx,
                WarningType.PATH_OPERATOR__EMPTY_SEQUENCE,
                List.of());
            case NON_NODES -> error(
                ctx,
                ErrorType.PATH_OPERATOR__NOT_SEQUENCE_OF_NODES,
                List.of(nodeType));
            case MULTIGRAMMAR -> error(
                ctx,
                ErrorType.PATH_OPERATOR__MULTIGRAMMAR,
                List.of(nodeType, result.inputGrammars()));
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

        // reporting invalid element names
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

        return symbolManager.typeInContext(result.result());
    }

    private PathOperatorResult getOperatorPathResultFromTree(final NodeTestContext ctx, final AntlrQuerySequenceType nodeType) {
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
        final AntlrQuerySequenceType contexttype = context.getType().type;
        if (contexttype == null) {
            error(ctx, ErrorType.PATH_EXPR__CONTEXT_TYPE_ABSENT, List.of());
            context.setType(symbolManager.typeInContext(anyNodes));
        } else if (!Types.isSubtype(typeFactory, contexttype, anyNodes)) {
            error(ctx, ErrorType.PATH_EXPR__CONTEXT_NOT_NODES, List.of(contexttype));
            context.setType(symbolManager.typeInContext(anyNodes));
        }
    }

    @Override
    public TypeInContext visitRelativePathExpr(final RelativePathExprContext ctx)
    {
        if (ctx.pathOperator().isEmpty()) {
            return visitStepExpr(ctx.stepExpr(0));
        }
        final var savedContext = saveContext();
        context.setType(savedContext.getType());
        context.setPositionType(savedContext.getPositionType());
        context.setSizeType(savedContext.getSizeType());
        TypeInContext result = visitStepExpr(ctx.stepExpr(0));
        context.setType(symbolManager.typeInContext(result.iteratorType()));
        final var operationCount = ctx.pathOperator().size();
        for (int i = 1; i <= operationCount; i++) {
            currentAxis = (ctx.pathOperator(i-1).SLASH() != null)
                ? AntlrQueryAxis.DESCENDANT_OR_SELF
                : AntlrQueryAxis.CHILD;
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
        AntlrQuerySequenceType stepResult = zeroOrMoreItems;
        if (ctx.reverseStep() != null)
            stepResult = visitReverseStep(ctx.reverseStep()).type;
        else if (ctx.forwardStep() != null)
            stepResult = visitForwardStep(ctx.forwardStep()).type;
        if (ctx.predicateList().predicate().isEmpty()) {
            return symbolManager.typeInContext(stepResult);
        }
        final var savedArgs = saveVisitedArguments();
        final var savedContext = saveContext();

        context.setType(symbolManager.typeInContext(stepResult));
        for (final var predicate : ctx.predicateList().predicate()) {
            stepResult = visitPredicate(predicate).type;
            context.setType(symbolManager.typeInContext(stepResult));
        }
        visitedPositionalArguments = savedArgs;
        context = savedContext;
        return symbolManager.typeInContext(stepResult);
    }

    private VisitingSemanticContext saveContext() {
        final var saved = context;
        context = new VisitingSemanticContext();
        return saved;
    }


    @Override
    public TypeInContext visitFilterExpr(final FilterExprContext ctx)
    {
        final AntlrQuerySequenceType expr = ctx.postfixExpr().accept(this).type;
        final var savedContext = saveContext();
        context.setType(symbolManager.typeInContext(expr));
        final var filtered = visitPredicate(ctx.predicate());
        context = savedContext;
        return filtered;
    }

    @Override
    public TypeInContext visitPredicate(final PredicateContext ctx)
    {
        final var contextType = context.getType();
        final var savedContext = saveContext();
        var iteratedSequence = savedContext.getType();
        context.setType(symbolManager.typeInContext(savedContext.getType().iteratorType()));
        context.setPositionType(symbolManager.typeInContext(
                typeFactory.number(
                        Ranges.union(NumericRange.ZERO, Ranges.integers(Cardinalities.toNumericRange(iteratedSequence.type.cardinality())))
                )
        ));// TODO: refine to integers // TODO: verify index range from 1
        context.setSizeType(symbolManager.typeInContext(nonNegativeNumber));
//        context.setSizeType(
//                typeFactory.number(NumericRange.of(Ranges.max(Ranges.integers(Cardinalities.toNumericRange(iteratedSequence.type.cardinality()))))
//                )
//        );    // TODO: refine value range
        final var predicateExpression = visitExpr(ctx.expr());
        if (Types.isSubtype(typeFactory, predicateExpression.type, emptySequence))
            return symbolManager.typeInContext(emptySequence);
        if (Types.isSubtype(typeFactory, predicateExpression.type, typeFactory.zeroOrMore(typeFactory.itemNumber()))) {
            final var item = contextType.type.itemType();
            Cardinality optionalizedCardinality = Objects.requireNonNull(
                    Cardinalities.optionalize(predicateExpression.type.cardinality())
            );
            final var deducedType = typeFactory.sequence(
                    item, optionalizedCardinality);
            return symbolManager.typeInContext(deducedType);
        }
        if (Types.hasNoEffectiveBooleanValue(typeFactory, predicateExpression.type)) {
            error(ctx.expr(), ErrorType.PREDICATE__NON_EBV, List.of(predicateExpression));
        }// TODO: add always true and always false checks
        context = savedContext;
        return symbolManager.typeInContext(Types.optionalize(typeFactory, contextType.type));
    }

    @Override
    public TypeInContext visitDynamicFunctionCall(final DynamicFunctionCallContext ctx) {
        final var savedArgs = saveVisitedArguments();
        final var savedContext = saveContext();
        context.setType(savedContext.getType());
        context.setPositionType(symbolManager.typeInContext(nonNegativeNumber));// TODO: refine value range
        context.setSizeType(symbolManager.typeInContext(nonNegativeNumber)); // TODO: refine value range
        final AntlrQuerySequenceType value = ctx.postfixExpr().accept(typeVisitor);
        // TODO: switch to getCallable due to array and map and record and tuple and ...
        final boolean isCallable = Types.isSubtype(typeFactory, value, typeFactory.anyFunction());
        if (!isCallable) {
            error(ctx.postfixExpr(), ErrorType.PREDICATE__NON_EBV, List.of(value));
        }
        ctx.positionalArgumentList().accept(this);
        visitedPositionalArguments = savedArgs;


        context = savedContext;

        if (value.itemType() instanceof final ConcreteItemType c
            && c instanceof final FunctionType ft) 
        {
            return symbolManager.typeInContext(ft.returnType());
        }
        return symbolManager.typeInContext(zeroOrMoreItems);
    }


    private final LookupOperation lookupOperation;

    @Override
    public TypeInContext visitLookupExpr(final LookupExprContext ctx) {
        final var targetType = Objects.requireNonNull(ctx.postfixExpr().accept(this));
        final TypeInContext keySpecifierType = getKeySpecifier(ctx);
        final LookupContext lookup = ctx.lookup();
        return typecheckLookup(ctx, lookup, targetType, keySpecifierType);
    }

    private <T extends ParserRuleContext> TypeInContext typecheckLookup(
            T ctx, LookupContext lookup, TypeInContext targetType, TypeInContext keySpecifierType)
    {
        final LookupOperation.LookupSemanticResult lookupType;
        if (lookup.keySpecifier().lookupWildcard() != null) {
            lookupType = lookupOperation.lookupWildcard(targetType.type);
        } else {
            lookupType = lookupOperation.lookupNonWildcard(targetType.type, keySpecifierType.type);
        }
        switch(lookupType) {
            case LookupOperation.LookupSemanticResult.Success(AntlrQuerySequenceType resultingType) -> {
                return symbolManager.typeInContext(resultingType);
            }
            case LookupOperation.LookupSemanticResult.LookupError.EmptyTarget(AntlrQuerySequenceType resultingType,
                                                                              AntlrQuerySequenceType target ) ->
            {
                error(ctx, ErrorType.LOOKUP__INVALID_TARGET, List.of(target));
                return symbolManager.typeInContext(resultingType);

            }
            case LookupOperation.LookupSemanticResult.LookupError.InvalidChoiceItem(AntlrQuerySequenceType resultingType,
                                                                                    LookupOperation.LookupSemanticResult.LookupError innerError,
                                                                                    AntlrQuerySequenceType target) ->
            {
                error(ctx, ErrorType.LOOKUP__INVALID_TARGET, List.of(target));
                return symbolManager.typeInContext(resultingType);
            }
            case LookupOperation.LookupSemanticResult.LookupError.InvalidTarget(AntlrQuerySequenceType resultingType,
                                                                                AntlrQuerySequenceType target) ->
            {
                error(ctx, ErrorType.LOOKUP__INVALID_TARGET, List.of(target));
                return symbolManager.typeInContext(resultingType);
            }
            case LookupOperation.LookupSemanticResult.LookupError.KeyEmpty(AntlrQuerySequenceType resultingType) ->
            {
                warn(ctx, WarningType.LOOKUP__KEY_EMPTY, List.of(keySpecifierType));
                return symbolManager.typeInContext(resultingType);
            }
            case LookupOperation.LookupSemanticResult.LookupError.InvalidRecordKey(
                    AntlrQuerySequenceType resultingType, AntlrQueryItemType key) ->
            {
                error(ctx, ErrorType.LOOKUP__INVALID_RECORD_KEY_TYPE, List.of(key));
                return symbolManager.typeInContext(resultingType);
            }
            case LookupOperation.LookupSemanticResult.LookupError.InvalidArrayIndex(
                    AntlrQuerySequenceType resultingType, AntlrQueryItemType givenKey, AntlrQueryItemType expectedKey)
                    ->
            {
                error(ctx, ErrorType.LOOKUP__INVALID_ARRAY_KEY__INDEX_OUTSIDE_OF_RANGE, List.of(givenKey, expectedKey));
                return symbolManager.typeInContext(resultingType);
            }
            case LookupOperation.LookupSemanticResult.LookupError.InvalidArrayKey(
                    AntlrQuerySequenceType resultingType, AntlrQueryItemType givenKey, AntlrQueryItemType expectedKey)
                    ->
            {
                error(ctx, ErrorType.LOOKUP__INVALID_ARRAY_KEY__WRONG_TYPE, List.of(givenKey, expectedKey));
                return symbolManager.typeInContext(resultingType);
            }
            case LookupOperation.LookupSemanticResult.LookupError.InvalidMapKey(
                    AntlrQuerySequenceType resultingType, AntlrQueryItemType givenKey, AntlrQueryItemType expectedKey) -> {
                error(ctx, ErrorType.LOOKUP__MAP_INVALID_KEY__WRONG_TYPE, List.of(givenKey, expectedKey));
                return symbolManager.typeInContext(resultingType);
            }
        }
    }


    @Override
    public TypeInContext visitUnaryLookup(final UnaryLookupContext ctx) {
        final TypeInContext contextType = context.getType();
        final TypeInContext keySpecifierType = visitKeySpecifier(ctx.lookup().keySpecifier());
        return typecheckLookup(ctx, ctx.lookup(), contextType, keySpecifierType);
    }




    TypeInContext getKeySpecifier(final LookupExprContext ctx) {
        final KeySpecifierContext keySpecifier = ctx.lookup().keySpecifier();
        if (keySpecifier.qname() != null) {
            final AntlrQuerySequenceType enum_ = typeFactory.enum_(Set.of(keySpecifier.qname().getText()));
            return symbolManager.typeInContext(enum_);
        }
        if (keySpecifier.STRING() != null ) {
            return handleString(keySpecifier);
        }
        if (keySpecifier.IntegerLiteral() != null) {
            final String value = keySpecifier.IntegerLiteral().getText().replace("_", "");
            var aqValue = valueFactory.number(new BigDecimal(value));
            return symbolManager.typeInContext(aqValue.type);
        }
        return keySpecifier.accept(this);
    }


    @Override
    public TypeInContext visitContextValueRef(final ContextValueRefContext ctx)
    {
        return context.getType();
    }




    @Override
    public TypeInContext visitForwardStep(final ForwardStepContext ctx)
    {
        if (ctx.forwardAxis() != null) {
            currentAxis = axisVisitor.visit(ctx.forwardAxis());
        } else {
            if (currentAxis == null) {
                currentAxis = AntlrQueryAxis.CHILD;
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
        currentAxis = axisVisitor.visit(ctx.reverseAxis());
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
            if (!Types.isSubtype(typeFactory, visitedType, zeroOrMoreItems)) {
                error(ctx.rangeExpr(i), ErrorType.CONCAT__INVALID, List.of());
            }
        }
        return symbolManager.typeInContext(string);
    }

    @Override
    public TypeInContext visitSimpleMapExpr(final SimpleMapExprContext ctx)
    {
        if (ctx.EXCLAMATION_MARK().isEmpty())
            return visitPathExpr(ctx.pathExpr(0));
        final TypeInContext firstExpressionType = Objects.requireNonNull(visitPathExpr(ctx.pathExpr(0)));
        if (firstExpressionType.type.equals(emptySequence)){
            return firstExpressionType;
        }
        final AntlrQuerySequenceType iterator = firstExpressionType.iteratorType();
        final var savedContext = saveContext();
        context.setType(symbolManager.typeInContext(iterator));
        context.setPositionType(symbolManager.typeInContext(nonNegativeNumber));// TODO: refine value range
        context.setSizeType(symbolManager.typeInContext(nonNegativeNumber));// TODO: refine value range
        TypeInContext result = firstExpressionType;
        final var theRest = ctx.pathExpr().subList(1, ctx.pathExpr().size());
        for (final var mappedExpression : theRest) {
            final TypeInContext type = visitPathExpr(mappedExpression);
            result = symbolManager.typeInContext(typeFactory.sequence(type.type.itemType(), result.type.cardinality()));
            context.setType(symbolManager.typeInContext(result.iteratorType()));
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
        final var testedType = Objects.requireNonNull(typeVisitor.visitType(ctx.type()));
        if (Types.isSubtype(typeFactory, expression.type, testedType)) {
            // UNNECESSARY_INSTANCE_OF__ALWAYS_TRUE
            warn(ctx, WarningType.INSTANCE_OF__ALWAYS_TRUE, List.of());
        }
        // TODO: add warning on impossible instance of tests
        final var bool = symbolManager.typeInContext(this.boolean_);
        symbolManager.currentScope().imply(
            bool,
            new InstanceOfSuccessImplication(
                bool,
                true,
                expression,
                symbolManager.typeInContext(testedType)
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
        final var relevantType = typeVisitor.visitType(ctx.type());
        if (!Types.isSubtype(typeFactory, relevantType, expression.type)
            && !expression.isSubtypeOf(relevantType))
        {
            warn(ctx, WarningType.TREAT__UNLIKELY, List.of(expression, relevantType));
        }
        return symbolManager.typeInContext(relevantType);
    }

    private final SequencetypeAtomization atomizer;



    private final SequenceTypeCastable castability;
    private final AntlrQuerySequenceType anyNodes;

    @Override
    public TypeInContext visitCastableExpr(final CastableExprContext ctx) {
        if (ctx.CASTABLE() == null)
            return this.visitCastExpr(ctx.castExpr());
        final TypeInContext type = this.visitCastTarget(ctx.castTarget());
        final TypeInContext tested = this.visitCastExpr(ctx.castExpr());
        final boolean emptyAllowed = ctx.castTarget().QUESTION_MARK() != null;
        final IsCastable result = castability.isCastable(type.type, tested.type, emptyAllowed);
        verifyCastability(ctx, type, tested.type, result);
        return type;
    }

    private <T> void verifyCastability(
            final ParserRuleContext ctx,
            final T type,
            final AntlrQuerySequenceType tested,
            final IsCastable result)
    {
        switch (result) {
            case IsCastable.AlwaysPossible.CastingToSame ignore -> {
                warn(ctx, WarningType.CAST__SELFCAST, List.of(tested, type));
            }
            case IsCastable.AlwaysPossible.TestedTypeIsSubtypeOfTargetType ignore -> {
                warn(ctx, WarningType.CAST__SUBTYPE_CAST, List.of(tested, type));
            }
            case IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget ignore -> {
                warn(ctx, WarningType.CAST__TARGET_CAST, List.of(tested, type));
            }
            case IsCastable.AlwaysPossible.ManyItemTypes many -> {
                warn(ctx, WarningType.CAST__POSSIBLE_MANY_ITEMTYPES, List.of(tested, type));
                final AntlrQueryItemType[] wrongItemTypes = many.wrongItemTypes();
                for (int i = 0; i < wrongItemTypes.length; i++) {
                    verifyCastability(ctx, wrongItemTypes[i], tested, many.problems()[i]);
                }
            }
            case IsCastable.AlwaysPossible.ManySequenceTypes ignore -> {
                warn(ctx, WarningType.CAST__POSSIBLE_MANY_SEQUENCETYPES, List.of(tested, type));
            }
            case IsCastable.Impossible impossible -> {
                error(ctx, ErrorType.CAST__IMPOSSIBLE, List.of(tested, type));
            }
            case IsCastable.Possible possible -> {}
            case IsCastable.TestedExpressionCanBeEmptySequenceWithoutFlag ignore -> {
                error(ctx, ErrorType.CAST__EMPTY_WITHOUT_FLAG, List.of(tested));
            }
            case IsCastable.TestedExpressionIsEmptySequence ignore -> {
                error(ctx, ErrorType.CAST__EMPTY_SEQUENCE, List.of());
            }
            case IsCastable.TestedExpressionIsZeroOrMore ignore -> {
                error(ctx, ErrorType.CAST__ZERO_OR_MORE, List.of(tested));
            }
            case IsCastable.WrongTargetType ignore -> {
                error(ctx, ErrorType.CAST__WRONG_TARGET_TYPE, List.of(type));
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
        final IsCastable result = castability.isCastable(type.type, tested.type, emptyAllowed);
        verifyCastability(ctx, type.type, tested.type, result);
        return symbolManager.typeInContext(tested.type);
    }


    @Override
    public TypeInContext visitCastTarget(final AntlrQueryParser.CastTargetContext ctx) {
        var type = super.visitCastTarget(ctx);
        if (ctx.QUESTION_MARK() != null)
            type = symbolManager.typeInContext(Types.optionalize(typeFactory, type.type));
        return type;
    }

    @Override
    public TypeInContext visitNamedFunctionRef(final NamedFunctionRefContext ctx)
    {
        final int arity = Integer.parseInt(ctx.IntegerLiteral().getText());
        final QualifiedName resolvedName = namespaceResolver.resolveFunction(ctx.qname().getText());
        final var analysis = symbolManager.getFunctionReference(
            ctx, resolvedName, arity, symbolManager.currentContext());
        errors.addAll(analysis.errors());
        return analysis.result();
    }

    @Override
    public TypeInContext visitSquareArrayConstructor(final SquareArrayConstructorContext ctx)
    {
        if (ctx.exprSingle().isEmpty()) {
            return symbolManager.typeInContext(typeFactory.one(typeFactory.itemTuple(List.of())));
        }

        final List<AntlrQuerySequenceType> types = ctx.exprSingle().stream()
            .map(expr -> expr.accept(this).type)
            .toList();
        final AntlrQuerySequenceType tuple = typeFactory.one(typeFactory.itemTuple(types));
        return symbolManager.typeInContext(tuple);
    }

    @Override
    public TypeInContext visitCurlyArrayConstructor(final CurlyArrayConstructorContext ctx)
    {
        final var expressions = ctx.enclosedExpr().expr();
        if (expressions == null) {
            return symbolManager.typeInContext(typeFactory.one(typeFactory.itemTuple(List.of())));
        }

        final AntlrQuerySequenceType arrayType = expressions.exprSingle().stream()
                .map(expr -> expr.accept(this).type)
                .collect(Collectors.teeing(
                        Collectors.mapping(AntlrQuerySequenceType::itemType, Collectors.toList()),
                        Collectors.mapping(AntlrQuerySequenceType::cardinality, Collectors.toList()),
                        (antlrQueryItemTypes, cardinalities) -> {
                            var itemType = ItemTypes.union(typeFactory, antlrQueryItemTypes.toArray(AntlrQueryItemType[]::new));
                            var cardinality = Cardinalities.add(cardinalities.toArray(Cardinality[]::new));
                            return typeFactory.array(typeFactory.one(itemType), cardinality);
                        }
                ));
        return symbolManager.typeInContext(arrayType);

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
        final AntlrQueryItemType errorType = typeFactory.itemError();
        final var testedExprType = ctx.tryClause().enclosedExpr().accept(this);
        final var alternativeCatches = ctx.catchClause().stream()
            .map(c -> {
                AntlrQuerySequenceType choicedErrors;
                if (c.pureNameTestUnion() != null) {
                    final ArrayList<AntlrQueryItemType> foundErrors = new ArrayList<>();
                    for (final var error : c.pureNameTestUnion().nameTest()) {
                        final String errorText = error.getText();
                        final QualifiedName errorQName = namespaceResolver.resolveType(errorText);
                        switch (typeFactory.itemNamedType(errorQName)) {
                            case NamedItemAccessingResult.Success(AntlrQueryItemType caughtErrorType)  -> {
                                if (!ItemTypes.isSubtype(typeFactory, caughtErrorType, errorType)) {
                                    error(c, ErrorType.TRY_CATCH__NON_ERROR, List.of(caughtErrorType, errorText));
                                    caughtErrorType = errorType;
                                }
                                foundErrors.add(caughtErrorType);
                            }
                            case NamedItemAccessingResult.UnknownName() -> {
                                error(c, ErrorType.TRY_CATCH__ERROR__UNKNOWN_NAMESPACE, List.of(errorText));
                            }
                            case NamedItemAccessingResult.UnknownNamespace unknownNamespace -> {
                                error(c, ErrorType.TRY_CATCH__ERROR__UNKNOWN_NAME, List.of(errorText));
                            }
                        }
                    }
                    choicedErrors = typeFactory.choice(foundErrors.toArray(AntlrQueryItemType[]::new));
                } else {
                    choicedErrors = typeFactory.error();
                }
                context.setType(symbolManager.typeInContext(choicedErrors));
                context.setPositionType(null);
                context.setSizeType(null);
                symbolManager.enterScope();
                declareVariable(symbolManager.typeInContext(string), "err:code", null);
                declareVariable(symbolManager.typeInContext(optionalString), "err:description", null);
                declareVariable(symbolManager.typeInContext(zeroOrMoreItems), "err:value", null);
                declareVariable(symbolManager.typeInContext(optionalString), "err:module", null);
                declareVariable(symbolManager.typeInContext(optionalNumber), "err:line-number", null);
                declareVariable(symbolManager.typeInContext(optionalNumber), "err:column-number", null);
                declareVariable(symbolManager.typeInContext(optionalString), "err:stack-trace", null);
                declareVariable(symbolManager.typeInContext(zeroOrMoreItems), "err:additional", null);
                declareVariable(symbolManager.typeInContext(typeFactory.anyMap()), "err:map", null);

                final var visited = c.enclosedExpr().accept(this);
                symbolManager.leaveScope();
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
            context = new VisitingSemanticContext();
            context.setType(symbolManager.typeInContext(typeFactory.anyNode()));
            final AntlrQuerySequenceType finallyType = visitEnclosedExpr(finallyClause.enclosedExpr()).type;
            if (!Types.isSubtype(typeFactory, finallyType, emptySequence)) {
                error(finallyClause, ErrorType.TRY_CATCH__FINALLY_NON_EMPTY, List.of(finallyType));
            }
        }
        context = savedContext;
        final AntlrQuerySequenceType mergedAlternativeCatches = alternativeCatches
            .map(typeInContext -> typeInContext.type)
            .reduce((t1, t2) ->Types.union(typeFactory, t1, t2))
            .get();
        final var merged = Types.union(typeFactory, testedExprType.type, mergedAlternativeCatches);
        return symbolManager.typeInContext(merged);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeInContext visitMapConstructor(final MapConstructorContext ctx)
    {
        final var entries = ctx.mapConstructorEntry();
        if (entries.isEmpty()) {
            if (ctx.MAP() == null) {
                return symbolManager.typeInContext(typeFactory.record(new LinkedHashMap<>()));
            } else {
                return symbolManager.typeInContext(typeFactory.map(typeFactory.itemAnyItem(), typeFactory.emptySequence()));
            }
        }
        final AntlrQueryItemType keyType = entries.stream()
            .map(e -> e.mapKeyExpr().accept(this).type.itemType())
            .reduce((t1, t2) -> ItemTypes.union(typeFactory, t1, t2))
            .get();
        if (keyType instanceof final StringType.StringEnum enum_) {
            final List<String> fieldNames = entries.stream()
                    .map(e -> e.mapKeyExpr().accept(this).type.itemType())
                    .map(e -> ((StringType.StringEnum) e).members().stream().findFirst().get())
                    .toList();
            final List<Entry<String, RecordField>> recordEntries = new ArrayList<Entry<String, RecordField>>(fieldNames.size());
            int i = 0;
            for (final var enumMember : fieldNames) {
                final var valueType = entries.get(i).mapValueExpr().accept(this);
                recordEntries.add(Map.entry(enumMember, new RecordField(enumMember,
                    new TypeOrReference.Type(valueType.type),
                    true)));
                i++;
            }
            return symbolManager.typeInContext(
                    typeFactory.record(
                            Utils.linkedHashMap(
                                recordEntries.toArray(Entry[]::new)
                            )
                    )
            );
        }
        // TODO: refine
        final AntlrQuerySequenceType valueType = entries.stream()
            .map(e -> visitMapValueExpr(e.mapValueExpr()).type)
            .reduce((t1, t2) -> Types.union(typeFactory, t1, t2))
            .get();
        return symbolManager.typeInContext(typeFactory.map(keyType, valueType));
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
            assert visitedPositionalArguments != null;
            assert visitedKeywordArguments != null;
            return callFunction(
                ctx.functionCall(),
                functionQname,
                visitedPositionalArguments,
                visitedKeywordArguments);
        }
        return Objects.requireNonNull(ctx.restrictedDynamicCall().accept(this));
    }


    @Override
    public TypeInContext visitMappingArrowTarget(final MappingArrowTargetContext ctx) {
        assert visitedPositionalArguments != null;
        final TypeInContext mappedSequence = visitedPositionalArguments.getLast() ;
        if (mappedSequence.type.cardinality().isZero()) {
            return mappedSequence;
        }
        final AntlrQuerySequenceType iterator = mappedSequence.iteratorType();
        visitedPositionalArguments = new ArrayList<>();
        visitedPositionalArguments.add(symbolManager.typeInContext(iterator));
        final var call = Objects.requireNonNull(ctx.arrowTarget().accept(this));
        final Cardinality mergedCardinality = Cardinalities.multiply(returnedCardinality, returnedCardinality);
        final AntlrQuerySequenceType x = typeFactory.sequence(call.type.itemType(), mergedCardinality);
        return symbolManager.typeInContext(x);
    }

    @Override
    public TypeInContext visitRestrictedDynamicCall(final RestrictedDynamicCallContext ctx) {
        final var value = Objects.requireNonNull(ctx.children.getFirst().accept(this));
        final boolean isCallable = value.isSubtypeOf(typeFactory.anyFunction());
        if (!isCallable) {
            error(ctx, ErrorType.RESTRICTED_DYNAMIC_CALL__NON_FUNCTION, List.of(value));
        }
        ctx.positionalArgumentList().accept(this);
        assert visitedPositionalArguments != null;
        final List<AntlrQuerySequenceType> args = visitedPositionalArguments.stream().map(a->a.type).toList();
        final var expectedFunction = typeFactory.itemFunction(zeroOrMoreItems, args);
        if (!Types.itemTypeIsSubtypeOf(typeFactory, value.type, expectedFunction))
        {
            error(ctx, ErrorType.RESTRICTED_DYNAMIC_CALL__INVALID_FUNCTION, List.of(expectedFunction, value));
        }

        if (isCallable)
            return symbolManager.typeInContext(Types.callResult(typeFactory, value.type, args));
        else
            return symbolManager.typeInContext(zeroOrMoreItems);
    }






    @Override
    public TypeInContext visitAndExpr(final AndExprContext ctx)
    {
        if (ctx.AND().isEmpty()) {
            return visitComparisonExpr(ctx.comparisonExpr(0));
        }
        final var operatorCount = ctx.AND().size();
        final List<ParseTree> exprs = new ArrayList<>(operatorCount+1);
        symbolManager.enterScope();
        boolean valid = true;
        for (int i = 0; i <= operatorCount; i++) {
            final ComparisonExprContext expr = ctx.comparisonExpr(i);
            final var visitedType = visitComparisonExpr(expr);
            if (Types.hasNoEffectiveBooleanValue(typeFactory, visitedType.type)) {
                error(expr, ErrorType.AND__NON_EBV, List.of(visitedType));
                valid = false;
            } else {
                final var ebv = symbolManager.resolveEffectiveBooleanValue(visitedType);
                symbolManager.currentScope().assume(ebv, new Assumption(ebv, true));
                exprs.add(expr);
            }
        }
        symbolManager.leaveScope();
        final var andExpr = symbolManager.typeInContext(boolean_);
        if (valid) {
            symbolManager.currentScope().imply(andExpr, new AndTrueImplication(andExpr, exprs, this));
        }
        return andExpr;
    }

    @Override
    public TypeInContext visitAdditiveExpr(final AdditiveExprContext ctx)
    {
        // TODO: refine semantics
        if (ctx.additiveOperator().isEmpty()) {
            return ctx.multiplicativeExpr(0).accept(this);
        }
        for (final var operandExpr : ctx.multiplicativeExpr()) {
            final var operand = operandExpr.accept(this);
            if (!operand.isSubtypeOf(anyNumber)) {
                error(operandExpr, ErrorType.ADDITIVE__INVALID, List.of(operand));
            }
        }
        return symbolManager.typeInContext(anyNumber);
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
        final var intersection = Types.intersect(typeFactory, leftHandSide, rightHandSide);
        if (intersection.itemType() instanceof NeverType)
        {
            error(ctx, ErrorType.GENERAL_COMP__INVALID, List.of(leftHandSide, rightHandSide));
        }
        return symbolManager.typeInContext(typeFactory.boolean_());
    }

    private TypeInContext handleValueComparison(final ComparisonExprContext ctx)
    {
        final var leftHandSide = Objects.requireNonNull(ctx.otherwiseExpr(0).accept(this));
        final var rightHandSide = Objects.requireNonNull(ctx.otherwiseExpr(1).accept(this));

        final var optionalItem = typeFactory.zeroOrOne(typeFactory.itemAnyItem());
        final boolean invalidLeft = !leftHandSide.isSubtypeOf(optionalItem);
        final boolean invalidRight = !rightHandSide.isSubtypeOf(optionalItem);
        if (invalidLeft && invalidRight) {
            error(ctx, ErrorType.VALUE_COMP__BOTH_INVALID, List.of(leftHandSide, rightHandSide));
        }
        else if (invalidLeft) {
            error(ctx.otherwiseExpr(0), ErrorType.VALUE_COMP__LHS_INVALID, List.of(leftHandSide));
        }
        else if (invalidRight) {
            error(ctx.otherwiseExpr(1), ErrorType.VALUE_COMP__RHS_INVALID, List.of(rightHandSide));
        }
        else if (!Types.isValueComparableWith(leftHandSide.type, rightHandSide.type)) {
            error(ctx, ErrorType.VALUE_COMP__INCOMPARABLE, List.of(leftHandSide, rightHandSide));
        }
        var resultingCardinality = Cardinalities.multiply(
            leftHandSide.type.cardinality(), rightHandSide.type.cardinality()
        );

        final var resultingType = typeFactory.sequence(typeFactory.itemBoolean(), resultingCardinality);
        return symbolManager.typeInContext(resultingType);
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
        return symbolManager.typeInContext(optionalBoolean);

    }

    @Override
    public TypeInContext visitMultiplicativeExpr(final MultiplicativeExprContext ctx)
    {
        if (ctx.multiplicativeOperator().isEmpty()) {
            return ctx.unionExpr(0).accept(this);
        }
        for (final var expr : ctx.unionExpr()) {
            final var visitedType = expr.accept(this);
            if (!visitedType.isSubtypeOf(anyNumber)) {
                error(ctx, ErrorType.MUL__INVALID, List.of(visitedType));
            }
        }
        return symbolManager.typeInContext(anyNumber);
    }

    @Override
    public TypeInContext visitOtherwiseExpr(final OtherwiseExprContext ctx)
    {
        if (ctx.OTHERWISE().isEmpty())
            return ctx.stringConcatExpr(0).accept(this);
        final int length = ctx.stringConcatExpr().size();
        AntlrQuerySequenceType merged = visitStringConcatExpr(ctx.stringConcatExpr(0)).type;
        if (!Cardinalities.contains(merged.cardinality(), BigInteger.ZERO)) {
            warn(ctx.stringConcatExpr(0), WarningType.OTHERWISE__IMPOSSIBLE, List.of(merged));
        }
        for (int i = 1; i < length; i++) {
            final var expr = ctx.stringConcatExpr(i);
            final AntlrQuerySequenceType exprType = visitStringConcatExpr(expr).type;
            if (!Cardinalities.contains(exprType.cardinality(), BigInteger.ZERO)) {
                warn(expr, WarningType.OTHERWISE__IMPOSSIBLE, List.of(exprType));
            }
            merged = Types.union(typeFactory, exprType, merged);
        }
        return symbolManager.typeInContext(merged);
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
            expressionType = symbolManager.typeInContext(zeroOrMoreNodes);
        }
        final var unionCount = ctx.unionOperator().size();
        for (int i = 1; i <= unionCount; i++) {
            expressionNode = ctx.intersectExpr(i);
            final var visitedType = expressionNode.accept(this);
            if (!visitedType.isSubtypeOf(zeroOrMoreNodes)) {
                error(expressionNode, ErrorType.UNION__INVALID, List.of(expressionType));
                expressionType = symbolManager.typeInContext(zeroOrMoreNodes);
            } else {
                expressionType = symbolManager.typeInContext(Types.addition(typeFactory, expressionType.type, visitedType.type));
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
            expressionType = symbolManager.typeInContext(zeroOrMoreNodes);
        }
        final var operatorCount = ctx.exceptOrIntersect().size();
        for (int i = 1; i < operatorCount; i++) {
            final var instanceofExpr = ctx.instanceofExpr(i);
            final var visitedType = instanceofExpr.accept(this);
            if (!visitedType.isSubtypeOf(zeroOrMoreNodes)) {
                error(ctx.instanceofExpr(i), ErrorType.INTERSECT_OR_EXCEPT__INVALID, List.of(expressionType));
                expressionType = symbolManager.typeInContext(zeroOrMoreNodes);
            } else {
                if (ctx.exceptOrIntersect(i).EXCEPT() != null) {
                    expressionType = symbolManager.typeInContext(
                            Types.subtract(typeFactory, expressionType.type, visitedType.type));
                }
                else {
                    expressionType = symbolManager.typeInContext(
                            Types.intersect(typeFactory, expressionType.type, visitedType.type));
                }
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
        if (!type.isSubtypeOf(anyNumber)) {
            error(ctx, ErrorType.UNARY__INVALID, List.of(type));
        }
        return symbolManager.typeInContext(anyNumber);
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

        @MonotonicNonNull  AntlrQuerySequenceType merged = null;
        assert !clauses.isEmpty() : "There needs to be at least one switch clause";
        for (final var clause : clauses) {
            final AntlrQuerySequenceType[] operands = clause.switchCaseOperand().stream()
                .map(this::visit)
                .map(x->x.type)
                .toArray(AntlrQuerySequenceType[]::new);
            final AntlrQuerySequenceType mergedOperands = Types.union(typeFactory, operands);
            
            if (!Types.isSubtype(typeFactory, mergedOperands, comparand.type)) {
                error(clause, ErrorType.SWITCH__INVALID_CASE, List.of(mergedOperands, comparand));
            }
            final var returned = clause.exprSingle().accept(this);
            if (merged == null) {
                merged = returned.type;
                continue;
            }
            merged = Types.union(typeFactory, merged, returned.type);
        }
        assert merged != null;
        final var merg = Types.union(typeFactory, merged, visitExprSingle(defaultExpr).type);
        return symbolManager.typeInContext(merg);
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
        final Types.EffectiveBooleanValueType ebvType = Types.effectiveBooleanValueType(typeFactory, conditionType.type);
        if (ebvType == Types.EffectiveBooleanValueType.NO_EBV) { // no effective boolean value
            ebv = symbolManager.currentScope().typeInContext(typeFactory.boolean_());
            error(ctx, ErrorType.IF__CONDITION_NON_EBV, List.of(conditionType));
        } else {
            ebv = symbolManager.resolveEffectiveBooleanValue(conditionType, ebvType);
        }
        TypeInContext trueType;
        TypeInContext falseType;
//         symbolManager.currentScope().imply(ebv, new EffectiveBooleanValueTrue(ebv, conditionType, typeFactory));
        if (ctx.bracedAction() != null) {
            symbolManager.enterScope();
            symbolManager.currentScope().assume(ebv, new Assumption(ebv, true));
            trueType = visitEnclosedExpr(ctx.bracedAction().enclosedExpr());
            symbolManager.leaveScope();

            symbolManager.enterScope();
            symbolManager.currentScope().assume(ebv, new Assumption(ebv, false));
            falseType = symbolManager.typeInContext(emptySequence);
            symbolManager.leaveScope();
        } else {
            symbolManager.enterScope();
            symbolManager.currentScope().assume(ebv, new Assumption(ebv, true));
            trueType = ctx.unbracedActions().exprSingle(0).accept(this);
            symbolManager.leaveScope();
            symbolManager.enterScope();
            symbolManager.currentScope().assume(ebv, new Assumption(ebv, false));
            falseType = ctx.unbracedActions().exprSingle(1).accept(this);
            symbolManager.leaveScope();
        }
        return symbolManager.typeInContext(Types.union(typeFactory, trueType.type, falseType.type));
    }


    @Override
    public TypeInContext visitStringConstructor(final StringConstructorContext ctx)
    {
        return symbolManager.typeInContext(typeFactory.string());
    }

    @Override
    public TypeInContext visitStringInterpolation(final StringInterpolationContext ctx)
    {
        return symbolManager.typeInContext(typeFactory.string());
    }


    @Override
    public TypeInContext visitInlineFunctionExpr(final InlineFunctionExprContext ctx)
    {
        // Is a focus function?
        if (ctx.functionSignature() == null) {
            // TODO: implement focus function
            return symbolManager.typeInContext(typeFactory.anyFunction());
        }
        final Set<String> argumentNames = new HashSet<>();
        final List<AntlrQuerySequenceType> args = new ArrayList<>();
        final var functionSignature = ctx.functionSignature();
        final var returnTypeDeclaration = functionSignature.typeDeclaration();
        symbolManager.enterScope();
        for (final var parameter : functionSignature.paramList().varNameAndType()) {
            final String parameterName = parameter.varName().qname().getText();
            final TypeDeclarationContext typeDeclaration = parameter.typeDeclaration();
            final AntlrQuerySequenceType parameterType = typeDeclaration != null
                ? typeVisitor.visitTypeDeclaration(typeDeclaration)
                : zeroOrMoreItems;
            if (argumentNames.contains(parameterName)) {
                error(parameter, ErrorType.FUNCTION__DUPLICATED_ARG_NAME, List.of(parameterName));
            }
            argumentNames.add(parameterName);
            args.add(parameterType);
            declareVariable(symbolManager.typeInContext(parameterType), parameterName, parameter.varName());
        }
        final var inlineType = Objects.requireNonNull(ctx.functionBody().enclosedExpr().accept(this));
        if (returnTypeDeclaration != null) {
            final var returnedType = returnTypeDeclaration.accept(typeVisitor);
            if (!Types.isSubtype(typeFactory, inlineType.type, returnedType)) {
                error(ctx.functionBody(), ErrorType.FUNCTION__INVALID_BODY_TYPE, List.of(inlineType, returnedType));
            }
            symbolManager.leaveScope();
            return symbolManager.typeInContext(typeFactory.function(returnedType, args));
        } else {
            symbolManager.leaveScope();
            return symbolManager.typeInContext(typeFactory.function(inlineType.type, args));
        }
    }

    @Override
    public TypeInContext visitEnclosedExpr(final EnclosedExprContext ctx)
    {
        if (ctx.expr() != null) {
            return visitExpr(ctx.expr());
        }
        return symbolManager.typeInContext(emptySequence);
    }

    public record UnresolvedFunctionSpecification(
        ParserRuleContext location,
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
        int minArity = 0;
        int maxArity = 0;
        if (ctx.paramListWithDefaults() != null) {
            final var params = ctx.paramListWithDefaults().paramWithDefault();
            for (final ParamWithDefaultContext param : params) {
                final var argName = getArgName(param);
                final TypeContext typeDeclaration = param.varNameAndType().typeDeclaration().type();
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
     * @return true if function has valid construction apart from type semantics
     */
    boolean validateUnresolvedFunction(final UnresolvedFunctionSpecification function) {
        final Set<String> uniqueNames = new HashSet<>();
        boolean valid = true;
        int i = 0;
        for (i = 0; i < function.args.size(); i++) {
            final UnresolvedArgumentSpecification fArg = function.args.get(i);
            if (fArg.defaultValue != null)
                break;
            if (!uniqueNames.add(fArg.name)) {
                error(fArg.location, ErrorType.FUNCTION__DUPLICATED_ARG_NAME, List.of(fArg.name));
                valid = false;
            }
        }
        final List<UnresolvedArgumentSpecification> defaultArgs = function.args.subList(i, function.args.size());
        for (final UnresolvedArgumentSpecification fArg : defaultArgs)
        {
            if (fArg.defaultValue == null) {
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
        symbolManager.enterContext();
        for (final UnresolvedArgumentSpecification param : spec.args.subList(0, spec.minArity))
        {
            final AntlrQuerySequenceType paramType = param.type == null
                ? zeroOrMoreItems
                : param.type.accept(typeVisitor);
            final var argDecl = new ArgumentSpecification(param.name, paramType, param.defaultValue);
            args.add(argDecl);
            argNameCtx.add(((ParamWithDefaultContext)param.location).varNameAndType().varName());
        }

        for (final var defaultParam : spec.args.subList(spec.minArity, spec.maxArity))
        {
            final AntlrQuerySequenceType paramType = defaultParam.type == null
                ? zeroOrMoreItems
                : defaultParam.type.accept(typeVisitor);
            assert defaultParam.defaultValue != null;
            final var dvt = defaultParam.defaultValue.accept(typeVisitor);
            if (!Types.isSubtype(typeFactory, dvt, paramType))
            {
                error((ParserRuleContext)defaultParam.defaultValue,
                    ErrorType.FUNCTION__INVALID_DEFAULT,
                    List.of(dvt, paramType));
            }
            final var argDecl = new ArgumentSpecification(defaultParam.name, paramType, defaultParam.defaultValue);
            args.add(argDecl);
            argNameCtx.add(((ParamWithDefaultContext)defaultParam.location).varNameAndType().varName());
        }
        for (int i = 0; i < args.size(); i++) {
            final var arg = args.get(i);
            declareVariable(
                symbolManager.typeInContext(arg.type()),
                arg.name(),
                argNameCtx.get(i));
        }

        final AntlrQuerySequenceType returned = spec.returnedType.accept(typeVisitor);

        final FunctionBodyContext functionBody = spec.body();
        if (functionBody != null) { // function body is present (non-external)
            final var calledFunctions = XPath.findAll(functionBody, "//functionName", new AntlrQueryParser(null));
            // TODO: refine to graphs or to cover arity range
            final Predicate<? super ParseTree> isRecursive
                = nameCtx->namespaceResolver.resolveFunction(nameCtx.getText()).equals(spec.name);
            // Registration should occur before function body validation due to recursion
            if (calledFunctions.stream().anyMatch(isRecursive))
            { // if called function is recursive we have to skip grained body analysis
                symbolManager.registerFunction(
                    spec.name.namespace(),
                    spec.name.name(),
                    args,
                    returned);

            } else { // if called function is NOT recursive we have to skip grained body analysis
                symbolManager.registerFunction(
                    spec.name.namespace(),
                    spec.name.name(),
                    args,
                    returned,
                    functionBody.enclosedExpr());
            }
            final var bodyType = visitEnclosedExpr(functionBody.enclosedExpr());
            if (!bodyType.isSubtypeOf(returned)) {
                error(functionBody, ErrorType.FUNCTION__INVALID_RETURNED_TYPE, List.of(bodyType, returned));
            }


        } else { // external function
            symbolManager.registerFunction(
                spec.name.namespace(),
                spec.name.name(),
                args,
                returned);

        }
        symbolManager.leaveContext();
    }

    private String getArgName(final ParamWithDefaultContext param)
    {
        final var paramName = param.varNameAndType().varName().qname();
        if (paramName.anyName().size() == 2)
        {
            error(param, ErrorType.FUNCTION__PARAM_HAS_NAMESPACE, List.of(paramName.anyName(0).getText()));
            return paramName.anyName(1).getText();
        }
        return paramName.anyName(0).getText();
    }


    @Override
    public TypeInContext visitVarDecl(final VarDeclContext ctx)
    {
        final VarNameContext varNameCtx = ctx.varNameAndType().varName();
        final var name = varNameCtx.qname().getText();
        final var declaredType = typeVisitor.visitTypeDeclaration(ctx.varNameAndType().typeDeclaration());
        if (ctx.EXTERNAL() == null) {
            final var assignedType = visitVarValue(ctx.varValue()).type;
            if (Types.coercibility(typeFactory, assignedType, declaredType) == RelativeCoercibility.NEVER) {
                error(ctx, ErrorType.VAR_DECL__UNCOERSABLE, List.of(name, declaredType, assignedType));
            }
        }
        declareVariable(symbolManager.typeInContext(declaredType), name, varNameCtx);
        return null;
    }

    private RegistrationResult resolveItemTypeFromDecl(final QualifiedName qName, final ItemTypeDeclContext ctx) {
        final var itemType = visitItemType(ctx.itemType()).type.itemType();
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
        AntlrQueryItemType recordItemType,
        List<ArgumentSpecification> fieldsAsArgs){}

    private RecordResolutionResult resolveRecord(
        final QualifiedName qName,
        final UnresolvedRecordSpecification recordSpecification
        )
    {
        final int size = recordSpecification.fields.size();
        final LinkedHashMap<String, RecordField> fields = new LinkedHashMap<>(size);
        final List<ArgumentSpecification> args = new ArrayList<>(size);
        for (final UnresolvedRecordFieldSpecification field : recordSpecification.fields) {
            final var fieldName = field.name;
            final TypeContext fieldTypeCtx = field.typeOrReferenceCtx;
            final AntlrQuerySequenceType type = typeVisitor.visitType(fieldTypeCtx);
            fields.put(fieldName, new RecordField(fieldName, new TypeOrReference.Type(type), field.isRequired));
        }
        for (final var mandatoryArgSpec : recordSpecification.mandatoryFieldsAsArgs) {
            args.add(
                new ArgumentSpecification(
                    mandatoryArgSpec.name,
                    Objects.requireNonNull(mandatoryArgSpec.type).accept(typeVisitor),
                    mandatoryArgSpec.defaultValue));
        }
        for (final var optionalArgSpec : recordSpecification.optionalFieldsAsArgs) {
            args.add(
                new ArgumentSpecification(
                    optionalArgSpec.name,
                    Objects.requireNonNull(optionalArgSpec.type).accept(typeVisitor),
                    optionalArgSpec.defaultValue)
                );
        }

        final var itemRecordType = recordSpecification.isExtensible
            ? typeFactory.itemExtensibleRecord(fields, zeroOrMoreItems)
            : typeFactory.itemRecord(fields);
        return new RecordResolutionResult(itemRecordType, args);
    }



    private record UnresolvedRecordFieldSpecification(
        String name,
        TypeContext typeOrReferenceCtx,
        boolean isRequired
    ) {}

    private record UnresolvedArgumentSpecification(
        ParserRuleContext location,
        String name,
        @Nullable TypeContext type,
        @Nullable ParseTree defaultValue
    ) {}


    private record UnresolvedRecordSpecification(
        NamedRecordTypeDeclContext location,
        QualifiedName name,
        List<UnresolvedRecordFieldSpecification> fields,
        List<UnresolvedArgumentSpecification> mandatoryFieldsAsArgs,
        List<UnresolvedArgumentSpecification> optionalFieldsAsArgs,
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
            final String fieldName = field.fieldDeclaration().fieldName().getText();
            final TypeContext fieldTypeCtx = field.fieldDeclaration().type();
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
        final boolean isExtensible = ctx.extensibleType() != null;
        return new UnresolvedRecordSpecification(ctx, qName, fields, mandatoryArgs, optionalArgs, isExtensible);
    }


    AntlrQueryAxis currentAxis;
    private final AntlrQuerySequenceType zeroOrOneItem;

    private AntlrQueryAxis saveAxis() {
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
            case DUPLICATE_IMPORT -> error(
                ctx,
                ErrorType.IMPORT_MODULE__DUPLICATE_IMPORT_BY_PATH,
                List.of(result)
                );
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
        throw new IllegalStateException("Unreachable");
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
        final List<AntlrQuerySequenceType> types = new ArrayList<>();
        for (final var typeSwitchCase : clauses) {
            final AntlrQuerySequenceType type = Objects.requireNonNull(typeVisitor.visitType(typeSwitchCase.type()));
            symbolManager.enterScope();
            if (Types.isSubtype(typeFactory, switched.type, type)) {
                if (typeSwitchCase.varName() != null) {
                    final var caseVarName = cases.varName().qname().getText();
                    declareVariable(symbolManager.typeInContext(type), caseVarName, cases.varName());
                }
                final TypeInContext evaluatedCase = visitExprSingle(typeSwitchCase.exprSingle());
                types.add(evaluatedCase.type);
            }
            symbolManager.leaveScope();
        }
        symbolManager.enterScope();
        if (cases.varName() != null) {
            declareVariable(switched, cases.varName());
        }
        final var defaultType = visitExprSingle(cases.exprSingle());
        symbolManager.leaveScope();
        types.add(defaultType.type);
        final AntlrQuerySequenceType orElse = types.stream()
            .reduce((t1, t2)-> Types.union(typeFactory, t1, t2))
            .orElse(zeroOrMoreItems);
        return symbolManager.typeInContext(orElse);
    }

    @Override
    public TypeInContext visitContextValueDecl(final ContextValueDeclContext ctx) {
        if (ctx.EXTERNAL() != null) {
            // DECLARE CONTEXT VALUE (AS sequenceType)? EXTERNAL (EQ_OP varDefaultValue)?
            if (ctx.type() != null) {
                // DECLARE CONTEXT VALUE AS sequenceType EXTERNAL (EQ_OP varDefaultValue)?
                if (ctx.varDefaultValue() != null) {
                    // DECLARE CONTEXT VALUE AS sequenceType EXTERNAL EQ_OP varDefaultValue
                    final var declaredType = ctx.type().accept(typeVisitor);
                    final var defaultValueType = visitVarDefaultValue(ctx.varDefaultValue());
                    if (Types.coercibility(typeFactory, defaultValueType.type, declaredType) == RelativeCoercibility.NEVER) {
                        error(ctx, ErrorType.CONTEXT_VALUE_DECL__UNCOERSABLE, List.of(defaultValueType, declaredType));
                    }
                    context.setType(symbolManager.typeInContext(declaredType));
                } else {
                    // DECLARE CONTEXT VALUE AS sequenceType EXTERNAL
                    final var declaredType = ctx.type().accept(typeVisitor);
                    context.setType(symbolManager.typeInContext(declaredType));
                }
            } else {
                // DECLARE CONTEXT VALUE EXTERNAL (EQ_OP varDefaultValue)?
                if (ctx.varDefaultValue() != null) {
                    // DECLARE CONTEXT VALUE EXTERNAL EQ_OP varDefaultValue
                    final var defaultValueType = visitVarDefaultValue(ctx.varDefaultValue());
                    context.setType(defaultValueType);
                } else { // TODO: DECLARE CONTEXT VALUE EXTERNAL
                }
            }
        } else {
            // DECLARE CONTEXT VALUE (AS sequenceType)? EQ_OP varValue
            if (ctx.type() != null) {
                // DECLARE CONTEXT VALUE AS sequenceType EQ_OP varValue
                final var declaredType = ctx.type().accept(typeVisitor);
                final var valueType = visitVarValue(ctx.varValue());
                if (Types.coercibility(typeFactory, valueType.type, declaredType) == RelativeCoercibility.NEVER) {
                    error(ctx, ErrorType.CONTEXT_VALUE_DECL__UNCOERSABLE, List.of(valueType, declaredType));
                }
                context.setType(symbolManager.typeInContext(declaredType));
            } else {
                // DECLARE CONTEXT VALUE EQ_OP varValue
                final var valueType = visitVarValue(ctx.varValue());
                context.setType(valueType);
            }

        }
        return null;
    }




}
