package com.github.akruk.antlrquery.semanticanalyzer.visitors;

import com.github.akruk.antlrquery.AntlrQueryParser;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticScope;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;

public interface AnalysisListener {
    default void onModuleDeclaration(final SemanticSymbolManager.ModuleInfo moduleInfo) {
    }

    default void onModuleReference(final AntlrQueryParser.QnameContext reference, final SemanticSymbolManager.ModuleInfo moduleInfo) {
    }

    default void onVariableDeclaration(final AntlrQuerySemanticScope.VariableInfo variableInfo) {
    }

    default void onVariableReference(final AntlrQueryParser.VarRefContext varRef, final AntlrQuerySemanticScope.VariableInfo variableInfo) {
    }

    default void onNamespaceDeclaration(final SemanticSymbolManager.NamespaceInfo namespaceInfo) {
    }

    default void onNamespaceReference(final AntlrQueryParser.QnameContext reference, final SemanticSymbolManager.NamespaceInfo namespaceInfo) {
    }

    default void onFunctionDeclaration(final SemanticSymbolManager.FunctionInfo functionInfo) {
    }

    default void onFunctionNamedReference(final AntlrQueryParser.NamedFunctionRefContext reference, final SemanticSymbolManager.FunctionInfo functionInfo) {
    }

    default void onFunctionCall(final AntlrQueryParser.FunctionCallContext reference, final SemanticSymbolManager.FunctionInfo functionInfo) {
    }

    default void onConstructorCall(final AntlrQueryParser.FunctionCallContext reference, final SemanticSymbolManager.RecordInfo recordInfo, final SemanticSymbolManager.FunctionInfo functionInfo) {
    }

    default void onFunctionArrowCall(final AntlrQueryParser.ArrowExprContext reference, final SemanticSymbolManager.FunctionInfo functionInfo) {
    }

    default void onMethodCall(final AntlrQueryParser.ArrowExprContext reference, final SemanticSymbolManager.RecordInfo recordInfo, final SemanticSymbolManager.FunctionInfo functionInfo) {
    }
    // TODO: partial function application renaming

    default void onNamedItemTypeDeclaration() {
    }

    default void onNamedItemTypeReference() {
    }

    default void onNamedRecordTypeDeclaration() {
    }

    default void onNamedRecordTypeReference() {
    }

    default void onGrammarDeclaration() {
    }

    default void onGrammarReference() {
    }

    default void onPathRuleReference() {
    }
    // TODO: Direct element constructors

}
