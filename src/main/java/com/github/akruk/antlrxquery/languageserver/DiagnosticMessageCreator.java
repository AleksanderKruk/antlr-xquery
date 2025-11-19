package com.github.akruk.antlrxquery.languageserver;


import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticError;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticWarning;
import com.github.akruk.antlrxquery.semanticanalyzer.GrammarManager.GrammarImportResult;
import com.github.akruk.antlrxquery.semanticanalyzer.ModuleManager.ImportResult;
import com.github.akruk.antlrxquery.semanticanalyzer.ModuleManager.ResolvingStatus;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypePathOperator.GrammarStatus;

public class DiagnosticMessageCreator {
    public String create(final DiagnosticError error) {
        switch (error.type()) {
            case ADDITIVE__INVALID -> {
                final var operand = error.data().get(0);
                return String.format("Operands in additive expression must be numeric\n\treceived: %s", operand);
            }
            case AND__NON_EBV -> {
                return String.format(
                    "Operands of 'and expression' need to have effective boolean value\n\treceived: %s",
                    error.data().get(0));
            }
            case CAST__EMPTY_SEQUENCE -> {
                return "Tested expression is an empty sequence";
            }
            case CAST__EMPTY_WITHOUT_FLAG -> {
                return "Tested expression of type " + error.data().get(0) + " can be an empty sequence without flag '?'";
            }
            case CAST__IMPOSSIBLE -> {
                final var tested = error.data().get(0);
                final var type = error.data().get(1);
                return "Casting from type " + tested + " to type " + type + " will never succeed";

            }
            case CAST__WRONG_TARGET_TYPE -> {
                final var type = error.data().get(0);
                return "Type: " + type + " is invalid casting target";
            }
            case CAST__ZERO_OR_MORE -> {
                final var tested = error.data().get(0);
                return "Tested expression of type " + tested + " can be a sequence of cardinality greater than one (or '?')";
            }
            case CHOICE_ITEM_TYPE__DUPLICATED -> {
                return "Duplicated type signatures in choice item type declaration";
            }
            case CHOICE_ITEM_TYPE__UNRECOGNIZED -> {
                return String.format("Type %s is not recognized", error.data().get(0));
            }
            case CONCAT__INVALID -> {
                return "Operands of 'concatenation expression' need to be subtype of item()?";
            }
            case FILTERING__EXPR_NOT_EBV -> {
                return "Filtering expression must have effective boolean value; received: " + error.data().get(0);
            }
            case FOR_ENTRY__KEY_VALUE_VARS_DUPLICATED_NAME -> {
                return "Key and value variable names must be distinct";
            }
            case FOR_ENTRY__POSITIONAL_VARIABLE_SAME_AS_MAIN_VARIABLE_NAME -> {
                return "Positional variable name must be distinct from main variable name";
            }
            case FOR_ENTRY__WRONG_ITERABLE_TYPE -> {
                return "ForEntryBinding requires a single map value";
            }
            case FOR_MEMBER__WRONG_ITERABLE_TYPE -> {
                final var arrayType = error.data().get(0);
                return "ForMemberBinding requires a single array value; received: " + arrayType;
            }
            case FUNCTION__DUPLICATED_ARG_NAME -> {
                final var parameterName = error.data().get(0);
                return "Duplicate parameter name: " + parameterName;
            }
            case FUNCTION__INVALID_BODY_TYPE -> {
                final var inlineType = error.data().get(0);
                final var returnedType = error.data().get(1);
                return String.format(
                    "Function body type %s is not a subtype of the declared return type %s",
                    inlineType.toString(), returnedType.toString()
                    );
            }
            case FUNCTION__INVALID_DEFAULT -> {
                final var dvt = error.data().get(0);
                final var paramType = error.data().get(1);
                return "Invalid default value: " + dvt + " is not subtype of " + paramType;
            }
            case FUNCTION__INVALID_RETURNED_TYPE -> {
                final var bodyType = error.data().get(0);
                final var returned = error.data().get(1);
                return "Invalid returned type: " + bodyType + " is not subtype of " + returned;
            }
            case FUNCTION__POSITIONAL_ARG_BEFORE_DEFAULT -> {
                return "Positional arguments must be located before default arguments";
            }
            case FUNCTION__UNKNOWN_NAMESPACE -> {
                final var namespace = error.data().get(0);
                return "Unknown function namespace: " + namespace;
            }
            case FUNCTION__UNKNOWN_FUNCTION -> {
                final var namespace = error.data().get(0);
                final var functionName = error.data().get(1);
                return "Unknown function: " + namespace + ":" + functionName;

            }
            case FUNCTION__NO_MATCHING_FUNCTION -> {
                final var namespace = error.data().get(0);
                final var name = error.data().get(1);
                final var requiredArity = error.data().get(2);
                @SuppressWarnings("unchecked")
                final var mismatchReasons = (List<Object>) error.data().get(3);
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("No matching function ");
                stringBuilder.append(namespace);
                stringBuilder.append(":");
                stringBuilder.append(name);
                stringBuilder.append(" for arity ");
                stringBuilder.append(requiredArity);
                for (final Object reason : mismatchReasons) {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append("\t");
                    stringBuilder.append(reason);
                }
                return stringBuilder.toString();

            }
            case FUNCTION_REFERENCE__UNKNOWN -> {
                final var namespace = error.data().get(0);
                final var name = error.data().get(1);
                final var arity = error.data().get(2);
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown function reference: ");
                stringBuilder.append(namespace);
                stringBuilder.append(":");
                stringBuilder.append(name);
                stringBuilder.append("#");
                stringBuilder.append(arity);
                return stringBuilder.toString();
            }
            case GENERAL_COMP__INVALID -> {
                final var leftHandSide = error.data().get(0);
                final var rightHandSide = error.data().get(1);
                return String.format("The types: %s and %s in general comparison are not comparable",
                    leftHandSide.toString(), rightHandSide.toString());
            }
            case GROUP_BY__DUPLICATED_VAR -> {
                final var varname = error.data().get(0);
                return "Grouping variable: " + varname + " used multiple times";
            }
            case GROUP_BY__UNDEFINED_GROUPING_VARIABLE -> {
                final var varname = error.data().get(0);
                return "Variable: " + varname + " is not defined";
            }
            case GROUP_BY__WRONG_GROUPING_VAR_TYPE -> {
                final var varname = error.data().get(0);
                final var zeroOrOneItem = error.data().get(1);
                final var atomizedType = error.data().get(2);
                return "Grouping variable: "
                    + varname
                    + " must be of type "
                    + zeroOrOneItem
                    + " received: "
                    + atomizedType;
            }
            case IF__CONDITION_NON_EBV -> {
                return String.format(
                "If condition must have an effective boolean value and the type %s doesn't have one",
                    error.data().get(0));
            }
            case IMPORT_MODULE__NO_PATH_FOUND -> {
                return error.data().get(0).toString();

            }
            case INTERSECT_OR_EXCEPT__INVALID -> {
                final var expressionType = error.data().get(0);
                return """
                       Expression of operator node()* except/intersect node()* does match the type 'node()'
                       \treceived type: """.stripIndent() + expressionType;
            }
            case ITEM_DECLARATION__ALREADY_REGISTERED_DIFFERENT -> {
                final var typeName = error.data().get(0);
                final var expected = error.data().get(1);
                return typeName + " has already been registered as type: " + expected;
            }
            case ITEM_DECLARATION__ALREADY_REGISTERED_SAME -> {
            }
            case LOOKUP__ARRAY_INVALID_KEY -> {
                final var targetType = error.data().get(0);
                final var keySpecifierType = error.data().get(1);
                return "Key type for lookup expression on " + targetType + " must be of type number*"
                    + "\n\ttarget type: " + targetType
                    + "\n\t   key type: " + keySpecifierType
                    ;
            }
            case LOOKUP__ARRAY_OR_MAP_INVALID_KEY -> {
                final var targetType = error.data().get(0);
                final var expectedKeyItemtype = error.data().get(1);
                return "Key type for lookup expression on "
                    + targetType
                    + " must be subtype of type "
                    + expectedKeyItemtype;
            }
            case LOOKUP__INVALID_EXTENDED_RECORD_KEY_TYPE -> {
                final var targetType = error.data().get(0);
                final var expectedKeyItemtype = error.data().get(1);
                return "Key type for lookup expression on " + targetType + " must be subtype of type " + expectedKeyItemtype;
            }
            case LOOKUP__INVALID_RECORD_KEY_NAME -> {
                final var key = error.data().get(0);
                final var targetType = error.data().get(1);
                return "Key specifier: " + key + " does not match record of type " + targetType;
            }
            case LOOKUP__INVALID_RECORD_KEY_TYPE -> {
                final var targetType = error.data().get(0);
                final var expectedKeyItemtype = error.data().get(1);
                return "Key type for lookup expression on "
                    + targetType
                    + " must be subtype of type "
                    + expectedKeyItemtype;
            }
            case LOOKUP__INVALID_TARGET -> {
                return String.format("Left side of lookup expression '<left> ? ...' must be map(*)* or array(*)*\n\treceived: %s", error.data().get(0));
            }
            case LOOKUP__MAP_INVALID_KEY -> {
            }
            case MAPPING__EMPTY_SEQUENCE -> {
                return "Mapping empty sequence";
            }
            case MUL__INVALID -> {
                final var visitedType = error.data().get(0);
                return "Multiplicative expression requires a number, received: " + visitedType;
            }
            case NODE_COMP__BOTH_INVALID -> {
                return String.format("""
                                Operands of node comparison must be of type 'node()?'\

                                	invalid left: \
                                %s\

                                	invalid right: \
                                %s""", error.data().get(0), error.data().get(1));
            }
            case NODE_COMP__LHS_INVALID -> {
                return String.format("Operands of node comparison must be of type 'node()?'\n\tinvalid left: %s", error.data().get(0));
            }
            case NODE_COMP__RHS_INVALID -> {
                return String.format("Operands of node comparison must be of type 'node()?'\n\tinvalid right: %s", error.data().get(0));
            }
            case PATH_OPERATOR__NOT_SEQUENCE_OF_NODES -> {
                return String.format("Path expression requires left hand side argument to be of type node()*\n\tfound: %s", error.data().get(0));
            }
            case PATH_OPERATOR__UNRECOGNIZED_RULE_NAMES -> {
                final Object joinedNames = error.data().get(0);
                return "Path expression references unrecognized rule names: " + joinedNames;
            }
            case OR__NON_EBV -> {
                return "Operands of 'or expression' need to have effective boolean value; received: " + error.data().get(0);
            }
            case FUNCTION__PARAM_HAS_NAMESPACE -> {
                return "Parameter " + error.data().get(0) + " cannot have a namespace";
            }
            case PATH_EXPR__CONTEXT_NOT_NODES -> {
                final var contexttype = error.data().get(0);
                return "Path expression starting from root requires context to be of type node()*;\n\tfound: " + contexttype;
            }
            case PATH_EXPR__CONTEXT_TYPE_ABSENT -> {
                return "Path expression starting from root requires context to be present and of type node()*";
            }
            case PREDICATE__NON_EBV -> {
                final var predicateExpression = error.data().get(0);
                return String.format(
                    "Predicate requires either number* type (for item by index aquisition) or a value that has effective boolean value, provided type: %s",
                    predicateExpression);
            }
            case QUANTIFIED__CRITERION_NON_EBV -> {
                return "Criterion value needs to have effective boolean value; received: " + error.data().get(0);
            }
            case RANGE__INVALID_FROM -> {
                return "Wrong type in 'from' operand of 'range expression': '<number?> to <number?>'";
            }
            case RANGE__INVALID_TO -> {
                return "Wrong type in 'to' operand of range expression: '<number?> to <number?>'";
            }
            case RANGE__INVALID_BOTH -> {
                return String.format("""
                                Wrong type in 'to' and 'from' operands of range expression: '<number?> to <number?>'\

                                	from: \
                                %s\

                                	to  : \
                                %s""", error.data().get(0), error.data().get(1))
                        ;
            }
            case RECORD_DECLARATION__ALREADY_REGISTERED_DIFFERENT -> {
                final var typeName = error.data().get(0);
                final var expr = error.data().get(1);
                return typeName + " has already been registered as type: " + expr;
            }
            case RECORD_DECLARATION__ALREADY_REGISTERED_SAME -> {
                final var typeName = error.data().get(0);
                return typeName + " has already been registered";
            }
            case RESTRICTED_DYNAMIC_CALL__NON_FUNCTION -> {
                final var value = error.data().get(0);
                return "Expected function in dynamic function call expression, received: " + value;
            }
            case RESTRICTED_DYNAMIC_CALL__INVALID_FUNCTION -> {
                final var expectedFunction = error.data().get(0);
                final var value = error.data().get(1);
                return String.format("Dynamic function call expects: %s received: %s", expectedFunction, value);
            }
            case SWITCH__INVALID_CASE -> {
                final var operandType = error.data().get(0);
                final var comparand = error.data().get(1);
                return String.format("Invalid operand type; %s is not a subtype of %s", operandType, comparand);
            }
            case SWITCH__UNCOERSABLE -> {
                final var type = error.data().get(0);
                return "Unknown type: " + type;

            }
            case TRY_CATCH__DUPLICATED_ERROR -> {
                final var name = error.data().get(0);
                return "Error: " + name + "already used in catch clause";
            }
            case TRY_CATCH__FINALLY_NON_EMPTY -> {
                final var finallyType = error.data().get(0);
                return "Finally clause needs to evaluate to empty sequence, currently:" + finallyType;
            }
            case TRY_CATCH__NON_ERROR -> {
                final var typeRef = error.data().get(0);
                final var err = error.data().get(1);
                return String.format("Type %s is not an error in try/catch: %s", typeRef.toString(), err);

            }
            case TRY_CATCH__ERROR__UNKNOWN_NAME -> {
                final var err = error.data().get(0);
                return "Unknown error in try/catch: " + err;
            }
            case TRY_CATCH__ERROR__UNKNOWN_NAMESPACE -> {
                final var err = error.data().get(0);
                return "Unknown error in try/catch, unknown namespace:" + err;
            }
            case TRY_CATCH__UNNECESSARY_ERROR_BECAUSE_OF_WILDCARD -> {
                return "Unnecessary catch clause, wildcard already used";
            }
            case TYPE_NAME__UNKNOWN -> {
                final var type = error.data().get(0);
                return "Unknown type: " + type;
            }
            case UNARY__INVALID -> {
                return """
                       Arithmetic unary expression requires a number
                       \treceived: """.stripIndent() + error.data().get(0);
            }
            case UNION__INVALID -> {
                return """
                       Expression of union operator node()* | node()* does match the type 'node()';
                       \treceived: """.stripIndent() + error.data().get(0);
            }
            case VALUE_COMP__INCOMPARABLE -> {
                return """
                       Given operands of 'value comparison' are incomparable
                       \t left operand: """.stripIndent() + error.data().get(0)
                        +"\n\tright operand: " + error.data().get(1)
                        ;
            }
            case VALUE_COMP__LHS_INVALID -> {
                return """
                       Left hand side of 'value comparison' must be of type 'item()?'
                       \treceived: """.stripIndent() + error.data().get(0);
            }
            case VALUE_COMP__RHS_INVALID -> {
                return """
                       Right hand side of 'value comparison' must be of type 'item()?'
                       \treceived: """.stripIndent() + error.data().get(0);
            }
            case VALUE_COMP__BOTH_INVALID -> {
                return """
                       Operands of 'value comparison' must be of type 'item()?'
                       \t left operand: """.stripIndent() + error.data().get(0)
                        +"\n\tright operand: " + error.data().get(1);

            }
            case VARIABLE_DECLARATION__ASSIGNED_TYPE_INCOMPATIBLE -> {
                final Object variableName = error.data().get(0);
                final Object inferredType = error.data().get(1);
                final Object declaredType = error.data().get(2);
                return String.format(
                    "Type of variable %s is not compatible with the assigned value: %s is not subtype of %s",
                    variableName, inferredType, declaredType);
            }
            case VAR_DECL_WITH_COERSION__INVALID -> {
                final Object assignedType = error.data().get(0);
                final Object desiredType = error.data().get(1);
                return String.format("Type: %s is not coercable to %s", assignedType, desiredType);
            }
            case VAR_DECL__UNCOERSABLE -> {
                final var name = error.data().get(0);
                final var declaredType = error.data().get(1);
                final var assignedType = error.data().get(2);
                return
                    "Variable "
                    + name
                    + " of type "
                    + declaredType
                    + " cannot be assigned value of type "
                    + assignedType;

            }
            case VAR_REF__UNDECLARED -> {
                return "Undeclared variable referenced: " + error.data().get(0);
            }
            case WINDOW__DECLARATION_MISMATCH -> {
                final var windowDeclaredVarType = error.data().get(0);
                final var windowSequenceType = error.data().get(1);
                return "Mismatched types; declared: " + windowDeclaredVarType + " is not subtype of received: " + windowSequenceType;
            }
            case WINDOW__END_CLAUSE_CONDITION_NOT_EBV -> {
                final var conditionType = error.data().get(0);
                return "Condition must have effective boolean value, received: " + conditionType;
            }
            case WINDOW__START_CLAUSE_CONDITION_NOT_EBV -> {
                final var conditionType = error.data().get(0);
                return "Condition must have effective boolean value, received: " + conditionType;
            }
            case CHAR__INVALID_UNICODE_POINT -> {
                final var charCode = error.data().get(0);
                final var i = error.data().get(1);
                return "Invalid Unicode code point: " + charCode + " at index: " + i;

            }
            case CHAR__INVALID_CHARACTER_REFERENCE -> {
                final var code = error.data().get(0);
                final var i = error.data().get(1);
                return "Invalid character reference: " + code + " at index: " + i;
            }
            case CHAR__UNTERMINATED_CHARACTER_REFERENCE -> {
                final var i = error.data().get(0);
                return "Unterminated character reference at " + i;
            }


            case RECORD_DECLARATION__INVALID_NAMESPACE -> {
                var namespace = error.data().get(0);
                var expectedNamespace = error.data().get(1);
                return "Record declared in module must be in module's namespace"
                        + "\n    expected namespace: " + expectedNamespace
                        + "\n    found namespace   : " + namespace;
            }

            case RECORD_DECLARATION__ALREADY_REGISTERED_BY_NAME -> {
                // TODO: add location with file tracking
                var name = error.data().get(0);
                // var r1 = error.data().get(1);
                // var r2 = error.data().get(2);
                return "Record '"+name+"' already exists";
            }

            case RECORD_DECLARATION__DUPLICATE_FIELD_NAME -> {
                var name = error.data().get(0);
                // var r1 = error.data().get(1);
                // var r2 = error.data().get(2);
                return "Field name '"+name+"' used multiple times";
            }

            case RECORD_DECLARATION__USED_RESERVED_NAME -> {
                return "Record must not have the name of a built in type";
            }

            case FUNCTION__INVALID_NAMESPACE -> {
                var namespace = error.data().get(0);
                var expectedNamespace = error.data().get(1);
                return "Function declared in module must be in module's namespace"
                        + "\n    expected namespace: " + expectedNamespace
                        + "\n    found namespace   : " + namespace;

            }


            case FUNCTION__USED_RESERVED_NAME -> {
                return "Function must not have the name of a built in type";
            }

            case NAMESPACE_DECL__INVALID_PREFIX -> {
                var namespace = error.data().get(0);
                var expectedNamespace = error.data().get(1);
                return "Namespace declared in a module must be in module's namespace"
                        + "\n    expected namespace: " + expectedNamespace
                        + "\n    found namespace   : " + namespace;
            }

            case NAMESPACE_DECL__NAMESPACE_REDECLARATION -> {
                // TODO: add location
                var name = error.data().get(0);
                return "Namespace '"+ name + "' has already beend declared";
            }

            case ITEM_DECLARATION__ALREADY_REGISTERED_BY_NAME -> {
                // TODO: add location with file tracking
                var name = error.data().get(0);
                // var r1 = error.data().get(1);
                // var r2 = error.data().get(2);
                return "Item type '"+name+"' already exists";
            }

            case NAMED_TYPES__RECORD_ITEM_TYPE_CROSS_REFERENCE -> {
                // TODO: add location with file tracking
                var name = error.data().get(0);
                // var r1 = error.data().get(1);
                // var r2 = error.data().get(2);
                return "Type '"+name+"' is declared both as a record and as a type";
            }

            case MODULE_DECL__NAMESPACE_ALREADY_DECLARED -> {
                // TODO: add location with file tracking
                var name = error.data().get(0);
                return "Module namespace '" + name + "' has already been declared";
            }

            case CONTEXT_VALUE_DECL__MULTIPLE_DECLARATIONS -> {
                return "There can only be one context value declaration";
            }
            case CONTEXT_VALUE_DECL__NOT_IN_MAIN_MODULE -> {
                return "Context value declaration in module is forbidden";
            }
            case CONTEXT_VALUE_DECL__UNCOERSABLE -> {
                var defaultValueType = error.data().get(0);
                var declaredType = error.data().get(0);
                return "Value of type: " + defaultValueType
                        + "\n    cannot be coerced to type: " + declaredType;
            }
            case PATH_OPERATOR__FOUND_UNREGISTERED_GRAMMARS -> {
                @SuppressWarnings("unchecked")
                List<String> invalidInputGrammars = (List<String>) error.data().get(0);
                StringBuilder sb = new StringBuilder();
                sb.append("Found unregistered grammars in path expression:");
                for (var g : invalidInputGrammars) {
                    sb.append("\n    " );
                    sb.append(g);
                }
                return sb.toString();
            }

            case DEFAULT_NAMESPACE_DECL__MULTIPLE_ANNOTATION_NAMESPACE_DECLARATIONS -> {
                return "There can be only one default annotation namespace declaration";
            }
            case DEFAULT_NAMESPACE_DECL__MULTIPLE_CONSTRUCTION_NAMESPACE_DECLARATIONS -> {
                return "There can be only one default construction namespace declaration";
            }
            case DEFAULT_NAMESPACE_DECL__MULTIPLE_ELEMENT_NAMESPACE_DECLARATIONS -> {
                return "There can be only one default element namespace declaration";
            }
            case DEFAULT_NAMESPACE_DECL__MULTIPLE_FUNCTION_NAMESPACE_DECLARATIONS -> {
                return "There can be only one default function namespace declaration";
            }
            case DEFAULT_NAMESPACE_DECL__MULTIPLE_TYPE_NAMESPACE_DECLARATIONS -> {
                return "There can be only one default type namespace declaration";
            }
            case GRAMMAR_IMPORT__GRAMMAR_ALREADY_REGISTERED -> {
                // TODO: location
                var grammarName = error.data().get(0);
                return "Grammar '"+grammarName+"' has already been registered";
            }
            case GRAMMAR_IMPORT__MANY_VALID_PATHS -> {
                var importResult = (GrammarImportResult)error.data().get(0);
                var sb = new StringBuilder("There are many paths that match the import:");
                for (int i = 0; i < importResult.resolvedPaths().size(); i++) {
                    sb.append(importResult.resolvedPaths().get(i));
                    sb.append(": ");
                    sb.append(importResult.resolvingStatuses().get(i));
                }
                return sb.toString();
            }
            case GRAMMAR_IMPORT__NO_LEXER -> {
                var importResult = (GrammarImportResult)error.data().get(0);
                var sb = new StringBuilder("No lexer file resolved bn import:");
                for (int i = 0; i < importResult.resolvedPaths().size(); i++) {
                    sb.append(importResult.resolvedPaths().get(i));
                    sb.append(": ");
                    sb.append(importResult.resolvingStatuses().get(i));
                }
                return sb.toString();

            }
            case GRAMMAR_IMPORT__NO_PARSER -> {
                var importResult = (GrammarImportResult)error.data().get(0);
                var sb = new StringBuilder("No parser file resolved in import:");
                for (int i = 0; i < importResult.resolvedPaths().size(); i++) {
                    sb.append(importResult.resolvedPaths().get(i));
                    sb.append(": ");
                    sb.append(importResult.resolvingStatuses().get(i));
                }
                return sb.toString();

            }
            case GRAMMAR_IMPORT__NEITHER_FOUND -> {
                var importResult = (GrammarImportResult)error.data().get(0);
                var sb = new StringBuilder("No valid file resolved in import:");
                for (int i = 0; i < importResult.resolvedPaths().size(); i++) {
                    sb.append(importResult.resolvedPaths().get(i));
                    sb.append(": ");
                    sb.append(importResult.resolvingStatuses().get(i));
                }
                return sb.toString();

            }

            case IMPORT_MODULE__DUPLICATE_IMPORT_BY_NAMESPACE -> {
                // TODO: add separate error for duplicated import itself
                var namespace = error.data().get(0);
                return "Module '"+namespace+"'' has already been declared";
            }


            case IMPORT_MODULE__DUPLICATE_IMPORT_BY_PATH -> {
                ImportResult result = (ImportResult) error.data().get(0);
                StringBuilder sb = new StringBuilder("Module import has duplicated path imports");
                for (int i = 0; i < result.resolvingStatuses().size(); i++) {
                    var status = result.resolvingStatuses().get(i);
                    var path = result.resolvedPaths().get(i);
                    switch(status) {
                    case FILE_ALREADY_IMPORTED:
                        sb.append("\n    ");
                        sb.append(path);
                        break;
                    case FOUND_OTHER_THAN_FILE, OK, UNREADABLE:
                    }

                }

            }
            case IMPORT_MODULE__IMPORTED_MAIN_MODULE -> {
                ImportResult result = (ImportResult) error.data().get(0);
                Path main = IntStream.range(0, result.resolvingStatuses().size())
                    .filter(i->result.resolvingStatuses().get(i) == ResolvingStatus.OK)
                    .mapToObj(i->result.resolvedPaths().get(i))
                    .findFirst().get()
                    ;
                return "Imported file '"+main+"' is a main module";

            }
            case ITEM_DECLARATION__INVALID_NAMESPACE -> {
                var namespace = error.data().get(0);
                var expectedNamespace = error.data().get(1);
                return "Item type declared in module must be in module's namespace"
                        + "\n    expected namespace: " + expectedNamespace
                        + "\n    found namespace   : " + namespace;

            }
            case ITEM_DECLARATION__USED_RESERVED_NAME -> {
                return "Item type must not have the name of a built in type";
            }

            case PATH_OPERATOR__MULTIGRAMMAR -> {
                var inputType = error.data().get(0);
                StringBuilder sb = new StringBuilder();
                sb.append("Path expression contains many grammars");
                sb.append("    type: " + inputType);
                @SuppressWarnings("unchecked")
                Map<String, GrammarStatus> invalidInputGrammars = (Map<String, GrammarStatus>)error.data().get(1);
                for (var g : invalidInputGrammars.keySet()) {
                    sb.append("\n    " );
                    sb.append(g);
                }

            }

        }
        return error.toString();

    }

    public String create(final DiagnosticWarning warning) {
        switch(warning.type()) {
        case CASTABLE__UNNECESSARY: {
            return String.format("Unnecessary castability test\n\tatomized: %s\n\t  tested: %s", warning.data().get(0), warning.data().get(1))
                ;
        }
        case CAST__POSSIBLE_MANY_ITEMTYPES: {
            final var tested = warning.data().get(0);
            final var type = warning.data().get(1);
            return "Casting from type " + tested + " to type " + type + " will always succeed";
        }
        case CAST__POSSIBLE_MANY_SEQUENCETYPES: {
            final var tested = warning.data().get(0);
            final var type = warning.data().get(1);
            return "Casting from type " + tested + " to type " + type + " will always succeed";
        }
        case CAST__SELFCAST: {
            final var tested = warning.data().get(0);
            final var type = warning.data().get(1);
            return "Casting from " + tested + " to type " + type + " is a selfcast";
        }
        case CAST__SUBTYPE_CAST: {
            final var tested = warning.data().get(0);
            final var type = warning.data().get(1);
            return "Casting from subtype " + tested + " supertype " + type + " will always succeed";
        }
        case CAST__TARGET_CAST: {
            final var tested = warning.data().get(0);
            final var type = warning.data().get(1);
            return "Casting from type " + tested + " to type " + type + " will always succeed";
        }
        case IMPORT_MODULE__MANY_VALID_PATHS: {
            return "There are multiple possible import candidates: " + warning.data();
        }
        case INSTANCE_OF__ALWAYS_TRUE: {
            return "Unnecessary instance of expression is always true";
        }
        case LOOKUP__EMPTY_RECORD: {
            return "Empty record will always return empty sequence...";
        }
        case LOOKUP__IMPOSSIBLE_RECORD_FIELD: {
            final var member = warning.data().get(0);
            return "The following enum member: " + member + "does not match any record field";
        }
        case LOOKUP__KEY_EMPTY: {
            return "Empty sequence as key specifier in lookup expression";
        }
        case LOOKUP__RETURNS_ALWAYS_EMPTY: {
            return "Empty record will always return empty sequence...";
        }
        case LOOKUP__TARGET_EMPTY: {
            return "Target type of lookup expression is an empty sequence";
        }
        case PATH_OPERATOR__DUPLICATED_NAME: {

        @SuppressWarnings("unchecked")
        var invalidNames = (Set<QualifiedName>)warning.data().get(0);
        final String joinedNames = invalidNames
            .stream()
            .map(QualifiedName::toString)
            .collect(Collectors.joining("\n    "));
            return "Path expression contains duplicated names:\n" + joinedNames;
        }
        case OTHERWISE__IMPOSSIBLE: {
            return String.format("Unnecessary otherwise expression\n\ttype: %s can never be an empty sequence", warning.data().get(0));
        }
        case PATH_OPERATOR__EMPTY_SEQUENCE: {
            return "Empty sequence as target of path operator";
        }
        case TREAT__UNLIKELY:
            return String.format("Unlikely treat expression\n\ta: %s\n\tb: %s", warning.data().get(0), warning.data().get(1))
                ;
        }
        return warning.toString();

    }

}
