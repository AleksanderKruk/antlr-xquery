package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.semanticanalyzer.EffectiveBooleanValueTrue;
import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType.EffectiveBooleanValueType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;


public class XQuerySemanticScope {
    final Map<String, TypeInContext> variables;
    final List<TypeInContext> scopedTypes;
    final Map<TypeInContext, List<Assumption>> scopedAssumptions;
    final Map<TypeInContext, List<Implication>> scopedImplications;
    final XQuerySemanticContext context;
    final Map<TypeInContext, TypeInContext> typeMapping;
    final Map<TypeInContext, TypeInContext> ebvs;
    final private XQueryTypeFactory typeFactory;

    public XQuerySemanticScope(
        XQuerySemanticContext context,
        XQuerySemanticScope previousScope,
        XQueryTypeFactory typeFactory
    )
    {
        this.typeFactory = typeFactory;
        this.context = context;
        scopedTypes = new ArrayList<>(previousScope.scopedTypes.size() * 2);
        scopedAssumptions = new HashMap<>(previousScope.scopedAssumptions.size() * 2);
        scopedImplications = new HashMap<>(previousScope.scopedImplications.size() * 2);
        variables = new HashMap<>(previousScope.variables.size() * 2);
        ebvs = new HashMap<>(previousScope.ebvs.size()*2);

        typeMapping = new HashMap<>(previousScope.scopedTypes.size()*2);
        for (var type : previousScope.scopedTypes) {
            if (typeMapping.containsKey(type)) {
                continue;
            }
            var copiedType = typeInContext(type.type);
            typeMapping.put(type, copiedType);
            var ebv = previousScope.ebvs.get(type);
            if (ebv != null) {
                if (typeMapping.containsKey(ebv)) {
                    continue;
                }
                var copiedEbv = typeInContext(ebv.type);
                typeMapping.put(ebv, copiedEbv);
                ebvs.put(copiedType, copiedEbv);
            }
        }

        for (var variableEntry : previousScope.variables.entrySet()) {
            var variableName = variableEntry.getKey();
            var variableType = variableEntry.getValue();
            var copiedType = typeMapping.get(variableType);
            variables.put(variableName, copiedType);
        }

        for (var entry : previousScope.scopedAssumptions.entrySet()) {
            var originalType = entry.getKey();
            var assumptionsForType = entry.getValue();
            var copiedType = typeMapping.getOrDefault(originalType, originalType);
            for (var assumption : assumptionsForType) {
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

    }

    public XQuerySemanticScope(
        XQuerySemanticContext context,
        XQueryTypeFactory typeFactory
    )
    {
        this.scopedTypes = new ArrayList<>();
        this.variables = new HashMap<>();
        this.scopedAssumptions = new HashMap<>();
        this.scopedImplications = new HashMap<>();
        this.typeMapping = new HashMap<>();
        this.context = context;
        this.ebvs = new HashMap<>();
        this.typeFactory = typeFactory;
    }

    public Map<String, TypeInContext> getVariables() {
        return variables;
    }

    /**
     * Either creates variable with required type
     * or overrides existing variable
     * @param variableName
     * @param assignedType
     * @return true if variable was added
     */
    public boolean entypeVariable(String variableName, TypeInContext assignedType) {
        if (assignedType.context != context)
        {
            final TypeInContext copiedType = typeMapping.computeIfAbsent(assignedType, t->typeInContext(t.type));
            ebvs.put(copiedType, assignedType.scope.ebvs.get(assignedType));
            for (var implication : assignedType.scope.scopedImplications.getOrDefault(assignedType, List.of()))
            {
                var remappedImplication = implication.remapTypes(typeMapping);
                scopedImplications.computeIfAbsent(copiedType, _ -> new ArrayList<>()).add(remappedImplication);
            }
            for (var assumption : assignedType.scope.scopedAssumptions.getOrDefault(assignedType, List.of()))
            {
                var copiedAssumption = new Assumption(copiedType, assumption.value);
                scopedAssumptions.computeIfAbsent(copiedType, _ -> new ArrayList<>()).add(copiedAssumption);
            }
            boolean addedVariable = variables.containsKey(variableName);
            variables.put(variableName, copiedType);
            return addedVariable;
        } else {
            boolean addedVariable = variables.containsKey(variableName);
            variables.put(variableName, assignedType);
            return addedVariable;

        }
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
        return inscope != null? inscope : List.of();
    }

    public List<Assumption> resolveAssumptionsForType(TypeInContext type)
    {
        var resolvedType = resolveType(type);
        var inscope = this.scopedAssumptions.get(resolvedType);
        return inscope != null? inscope : List.of();
    }

    public boolean hasVariable(String variableName)
    {
        return variables.containsKey(variableName);
    }

    public TypeInContext typeInContext(XQuerySequenceType type)
    {
        var tic = new TypeInContext(type, context, this);
        scopedTypes.add(tic);
        var ebvType = type.effectiveBooleanValueType();
        if (ebvType != EffectiveBooleanValueType.NO_EBV) {
            TypeInContext effectiveBooleanValue = resolveEffectiveBooleanValue(tic, ebvType);
            if (tic != effectiveBooleanValue) {
                imply(effectiveBooleanValue, new EffectiveBooleanValueTrue(effectiveBooleanValue, tic, typeFactory));
            }
        }
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

    public TypeInContext resolveEffectiveBooleanValue(TypeInContext typeInContext, EffectiveBooleanValueType ebvType) {
        var resolvedType = resolveType(typeInContext);
        return switch (ebvType) {
            case ALWAYS_FALSE__EMPTY_SEQUENCE, ALWAYS_TRUE__NODE, NODE, NO_EBV ->
            {
                yield ebvs.computeIfAbsent(resolvedType, (_) -> typeInContext(typeFactory.boolean_()));
            }
            case ALWAYS_TRUE__NUMBER_STRING_BOOLEAN, NUMBER_STRING_BOOLEAN-> {
                if (typeInContext.type.itemType.type == XQueryTypes.BOOLEAN && typeInContext.type.isOne) {
                    yield resolvedType;
                }
                yield ebvs.computeIfAbsent(resolvedType, (_) -> typeInContext(typeFactory.boolean_()));
            }
        };
    }

    public TypeInContext resolveEffectiveBooleanValue(TypeInContext type) {
        return resolveEffectiveBooleanValue(type, type.type.effectiveBooleanValueType());
    }

    public boolean namespaceExists(String namespace) {
        return variables.containsKey(namespace);
    }

}
