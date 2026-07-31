package com.github.akruk.antlrquery.semanticanalyzer.semanticcontext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrquery.AntlrQueryParser;
import org.eclipse.lsp4j.Location;

import com.github.akruk.antlrquery.semanticanalyzer.EffectiveBooleanValueTrue;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.TypeInContext;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types.EffectiveBooleanValueType;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;


public class AntlrQuerySemanticScope {
    public static record VariableInfo(String name, TypeInContext type, AntlrQueryParser.VarNameContext definition, Location location) {}
    final Map<String, VariableInfo> variables;
    final List<TypeInContext> scopedTypes;
    final Map<TypeInContext, List<Assumption>> scopedAssumptions;
    final Map<TypeInContext, List<Implication>> scopedImplications;
    final AntlrQuerySemanticContext context;
    final Map<TypeInContext, TypeInContext> typeMapping;
    final Map<TypeInContext, TypeInContext> ebvs;
    final private AntlrQueryTypeFactory typeFactory;
    private AntlrQuerySequenceType boolean_;

    public AntlrQuerySemanticScope(
        AntlrQuerySemanticContext context,
        AntlrQuerySemanticScope previousScope,
        AntlrQueryTypeFactory typeFactory
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
            VariableInfo variableInfo = variableEntry.getValue();
            var copiedType = typeMapping.get(variableInfo.type);
            variables.put(variableName, new VariableInfo(variableName, copiedType, variableInfo.definition, variableInfo.location));
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
        boolean_ = typeFactory.boolean_();

    }

    public AntlrQuerySemanticScope(
        AntlrQuerySemanticContext context,
        AntlrQueryTypeFactory typeFactory
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
        boolean_ = typeFactory.boolean_();
    }

    public Map<String, VariableInfo> getVariables() {
        return variables;
    }

    public final record EntypingResult(VariableInfo oldVariable, VariableInfo newVariable){}

    /**
     * Either creates variable with required type
     * or overrides existing variable
     * @param variableName
     * @param variableDefinition
     * @param assignedType
     * @return EntypingResult {
     *      VariableInfo? oldVariable;
     *      VariableInfo  newVariable;
     * }
     */
    public EntypingResult entypeVariable(
        String variableName,
        AntlrQueryParser.VarNameContext variableDefinition,
        Location variableLocation,
        TypeInContext assignedType)
    {
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
            if (variableDefinition == null) { // called by redeclaration or undeclared external variable
                final VariableInfo variableInfo = variables.get(variableName);
                if (variableInfo == null) { // new variable
                    VariableInfo newVariable = new VariableInfo(variableName, copiedType, null, null);
                    variables.put(variableName, newVariable);
                    return new EntypingResult(variableInfo, newVariable);
                } else { // redeclaration
                    VariableInfo newVariable = new VariableInfo(variableName, copiedType, variableInfo.definition, variableInfo.location);
                    variables.put(variableName, newVariable);
                    return new EntypingResult(variableInfo, newVariable);
                }
            } else { // use given location
                VariableInfo newVariable = new VariableInfo(variableName, copiedType, variableDefinition, variableLocation);
                variables.put(variableName, newVariable);
                return new EntypingResult(null, newVariable);
            }
        } else {
            if (variableDefinition == null) { // called by redeclaration or external variable
                final VariableInfo variableInfo = variables.get(variableName);
                if (variableInfo == null) { // new variable
                    VariableInfo newVariable = new VariableInfo(variableName, assignedType, null, null);
                    variables.put(variableName, newVariable);
                    return new EntypingResult(variableInfo, newVariable);
                } else { // redeclaration
                    VariableInfo newVariable = new VariableInfo(variableName, assignedType, variableInfo.definition, variableInfo.location);
                    variables.put(variableName, newVariable);
                    return new EntypingResult(variableInfo, newVariable);
                }
            } else { // use given location
                VariableInfo newVariable = new VariableInfo(variableName, assignedType, variableDefinition, variableLocation);
                variables.put(variableName, newVariable);
                return new EntypingResult(null, newVariable);
            }
        }
    }


    public VariableInfo getVariable(String variableName) {
        var variableInfo = variables.get(variableName);
        if (variableInfo == null) {
            return null;
        } else {
            return variableInfo;
        }
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

    public TypeInContext typeInContext(AntlrQuerySequenceType type)
    {
        assert type != null;
        var tic = new TypeInContext(typeFactory, type, context, this);
        scopedTypes.add(tic);
        var ebvType = Types.effectiveBooleanValueType(typeFactory, type);
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
                if (typeInContext.type.equals(typeFactory.boolean_())) {
                    yield resolvedType;
                }
                yield ebvs.computeIfAbsent(resolvedType, (_) -> typeInContext(boolean_));
            }
        };
    }

    public TypeInContext resolveEffectiveBooleanValue(TypeInContext type) {
        return resolveEffectiveBooleanValue(type, Types.effectiveBooleanValueType(typeFactory, type.type));
    }

    public boolean namespaceExists(String namespace) {
        return variables.containsKey(namespace);
    }

}
