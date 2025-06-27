package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults;

import java.util.*;

import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticFunctionCaller.CallAnalysisResult;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQueryBaseSemanticFunctionCaller.XQuerySemanticFunction;
import com.github.akruk.antlrxquery.semanticanalyzer.XQueryVisitingSemanticContext;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class FunctionTrie {

    private final Map<String, Map<String, ReturnTypeNode>> root = new HashMap<>();

    public void register(String namespace,
                         String functionName,
                         XQuerySequenceType returnType,
                         List<XQuerySequenceType> argumentTypes,
                         XQuerySemanticFunction function) {
        root.computeIfAbsent(namespace, _ -> new HashMap<>())
            .computeIfAbsent(functionName, _ -> new ReturnTypeNode())
            .register(returnType, argumentTypes, function);
    }

    public Optional<CallAnalysisResult> resolve(String namespace,
                                                String functionName,
                                                XQueryTypeFactory typeFactory,
                                                XQueryVisitingSemanticContext context,
                                                List<XQuerySequenceType> argTypes) {
        var nsMap = root.get(namespace);
        if (nsMap == null) return Optional.empty();
        var fnNode = nsMap.get(functionName);
        if (fnNode == null) return Optional.empty();
        return fnNode.resolve(typeFactory, context, argTypes);
    }

    public Optional<CallAnalysisResult> resolve(String qualifiedName) {
        var parts = qualifiedName.split(":", 2);
        if (parts.length != 2) return Optional.empty();
        var nsMap = root.get(parts[0]);
        if (nsMap == null) return Optional.empty();
        var fnNode = nsMap.get(parts[1]);
        if (fnNode == null) return Optional.empty();
        return fnNode.firstDefinition();
    }

    private static class ReturnTypeNode {
        private final Map<XQuerySequenceType, ArgumentNode> returnTypes = new HashMap<>();

        public void register(XQuerySequenceType returnType,
                             List<XQuerySequenceType> argTypes,
                             XQuerySemanticFunction fn) {
            returnTypes.computeIfAbsent(returnType, _ -> new ArgumentNode())
                       .insert(argTypes, fn);
        }

        public Optional<CallAnalysisResult> resolve(XQueryTypeFactory factory,
                                                    XQueryVisitingSemanticContext context,
                                                    List<XQuerySequenceType> argTypes) {
            for (var entry : returnTypes.entrySet()) {
                var maybe = entry.getValue().find(argTypes);
                if (maybe.isPresent()) {
                    return Optional.of(maybe.get().call(factory, context, argTypes));
                }
            }
            return Optional.empty();
        }

        public Optional<CallAnalysisResult> firstDefinition() {
            for (var entry : returnTypes.entrySet()) {
                var maybe = entry.getValue().first();
                if (maybe.isPresent()) {
                    return Optional.of(new CallAnalysisResult(entry.getKey(), List.of()));
                }
            }
            return Optional.empty();
        }
    }

    private static class ArgumentNode {
        private final Node root = new Node();

        public void insert(List<XQuerySequenceType> args, XQuerySemanticFunction fn) {
            root.insert(args, fn);
        }

        public Optional<XQuerySemanticFunction> find(List<XQuerySequenceType> args) {
            return root.find(args);
        }

        public Optional<XQuerySemanticFunction> first() {
            return root.first();
        }

        private static class Node {
            private final Map<XQuerySequenceType, Node> children = new HashMap<>();
            private XQuerySemanticFunction function = null;

            public void insert(List<XQuerySequenceType> path, XQuerySemanticFunction fn) {
                Node current = this;
                for (var type : path) {
                    current = current.children.computeIfAbsent(type, _ -> new Node());
                }
                current.function = fn;
            }

            public Optional<XQuerySemanticFunction> find(List<XQuerySequenceType> path) {
                Node current = this;
                for (var type : path) {
                    current = current.children.get(type);
                    if (current == null) return Optional.empty();
                }
                return Optional.ofNullable(current.function);
            }

            public Optional<XQuerySemanticFunction> first() {
                if (function != null) return Optional.of(function);
                for (var child : children.values()) {
                    var maybe = child.first();
                    if (maybe.isPresent()) return maybe;
                }
                return Optional.empty();
            }
        }
    }
}
