package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;


public class XQuerySemanticScope {
    final Map<String, TypeInContext> variables;
    final List<TypeInContext> scopedTypes;
    final Map<TypeInContext, List<Assumption>> scopedAssumptions;
    final Map<TypeInContext, List<Implication>> scopedImplications;
    final XQuerySemanticContext context;
    final Map<TypeInContext, TypeInContext> typeMapping;

    public XQuerySemanticScope(
        XQuerySemanticContext context,
        XQuerySemanticScope previousScope
    )
    {
        scopedTypes = new ArrayList<>(previousScope.scopedTypes.size() * 2);
        scopedAssumptions = new HashMap<>(previousScope.scopedAssumptions.size() * 2);
        scopedImplications = new HashMap<>(previousScope.scopedImplications.size() * 2);
        variables = new HashMap<>(previousScope.variables.size() * 2);

        typeMapping = new HashMap<>();
        for (var type : previousScope.scopedTypes) {
            var copiedType = typeInContext(type.type);

            typeMapping.put(type, copiedType);
        }

        for (var variableEntry : previousScope.variables.entrySet()) {
            var variableName = variableEntry.getKey();
            var variableType = variableEntry.getValue();
            var copiedType = typeMapping.getOrDefault(variableType, variableType);
            variables.put(variableName, copiedType);
        }

        for (var entry : previousScope.scopedAssumptions.entrySet()) {
            var originalType = entry.getKey();
            var copiedType = typeMapping.getOrDefault(originalType, originalType);
            for (var assumption : entry.getValue()) {
            var copiedAssumption = new Assumption(copiedType, assumption.value);
            scopedAssumptions.computeIfAbsent(copiedType, _ -> new ArrayList<>()).add(copiedAssumption);
            }
        }

        for (var entry : previousScope.scopedImplications.entrySet()) {
            var originalType = entry.getKey();
            var copiedType = typeMapping.getOrDefault(originalType, originalType);
            for (var implication : entry.getValue()) {
                var remappedImplication = implication.remapTypes(typeMapping);
                scopedImplications.computeIfAbsent(copiedType, _ -> new ArrayList<>()).add(remappedImplication);
            }
        }

        this.context = context;
    }

    public XQuerySemanticScope(
        XQuerySemanticContext context
    )
    {
        this.scopedTypes = new ArrayList<>();
        this.variables = new HashMap<>();
        this.scopedAssumptions = new HashMap<>();
        this.scopedImplications = new HashMap<>();
        this.typeMapping = new HashMap<>();
        this.context = context;
    }

    public Map<String, TypeInContext> getVariables() {
        return variables;
    }

    public boolean entypeVariable(String variableName, TypeInContext assignedType) {
        boolean addedVariable = variables.containsKey(variableName);
        variables.put(variableName, assignedType);
        return addedVariable;
    }


    public TypeInContext getVariable(String variableName) {
        return variables.getOrDefault(variableName, null);
    }

    public void assume(TypeInContext type, Assumption assumption) {
        var resolvedType = resolveType(type);
        scopedAssumptions.computeIfAbsent(resolvedType, _-> new ArrayList<>()).add(assumption);
        context.applyImplications(resolvedType);
    }

    public void imply(TypeInContext type, Implication implication)
    {
        var resolvedType = resolveType(type);
        scopedImplications.computeIfAbsent(resolvedType, _ -> new ArrayList<>()).add(implication);
        if (implication.isApplicable(context)) {
            implication.transform(context);
        }
    }


    private TypeInContext resolveType(TypeInContext type)
    {
        if (scopedTypes.contains(type)) {
            return type;
        }
        return typeMapping.get(type);
    }


    public List<Implication> resolveImplicationsForType(TypeInContext type)
    {
        var resolvedType = resolveType(type);
        var inscope = scopedImplications.get(resolvedType);
        return inscope;
    }

    public List<Assumption> resolveAssumptionsForType(TypeInContext type)
    {
        var resolvedType = resolveType(type);
        var inscope = this.scopedAssumptions.get(resolvedType);
        return inscope;
    }

    public boolean hasVariable(String variableName)
    {
        return variables.containsKey(variableName);
    }

    public TypeInContext typeInContext(XQuerySequenceType type)
    {
        var tic = new TypeInContext(type);
        scopedTypes.add(tic);
        return tic;
    }

    public boolean existsAssumption(Assumption matchingAssumption)
    {
        var assumptions = resolveAssumptionsForType(matchingAssumption.type);
        if (assumptions == null) {
            return false;
        }
        for (var assumption : assumptions) {
            if (assumption.value.equals(matchingAssumption.value)) {
                return true;
            }
        }
        return false;
    }
}
