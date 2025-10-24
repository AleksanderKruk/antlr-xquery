package com.github.akruk.antlrxquery.languageserver;


import java.util.List;

import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticError;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticWarning;

public class DiagnosticMessageCreator {
    public String create(final DiagnosticError error) {
        switch (error.type()) {
            case ADDITIVE__INVALID: {
                final var operand = error.data().get(0);
                return "Operands in additive expression must be numeric"
                        + "\n\treceived: " + operand;
            }
            case AND__NON_EBV: {
                return "Operands of 'and expression' need to have effective boolean value"
                    + "\n\treceived: " + error.data().get(0);
            }
            case CAST__EMPTY_SEQUENCE: {
                return "Tested expression is an empty sequence";
            }
            case CAST__EMPTY_WITHOUT_FLAG: {
                return "Tested expression of type " + error.data().get(0) + " can be an empty sequence without flag '?'";
            }
            case CAST__IMPOSSIBLE: {
                final var tested = error.data().get(0);
                final var type = error.data().get(1);
                return "Casting from type " + tested + " to type " + type + " will never succeed";

            }
            case CAST__WRONG_TARGET_TYPE: {
                final var type = error.data().get(0);
                return "Type: " + type + " is invalid casting target";
            }
            case CAST__ZERO_OR_MORE: {
                final var tested = error.data().get(0);
                return "Tested expression of type " + tested + " can be a sequence of cardinality greater than one (or '?')";
            }
            case CHOICE_ITEM_TYPE__DUPLICATED:{
                return "Duplicated type signatures in choice item type declaration";
            }
            case CHOICE_ITEM_TYPE__UNRECOGNIZED:{
                return String.format("Type %s is not recognized", error.data().get(0));
            }
            case CONCAT__INVALID: {
                return "Operands of 'concatenation expression' need to be subtype of item()?";
            }
            case FILTERING__EXPR_NOT_EBV: {
                return "Filtering expression must have effective boolean value; received: " + error.data().get(0);
            }
            case FOR_ENTRY__KEY_VALUE_VARS_DUPLICATED_NAME: {
                return "Key and value variable names must be distinct";
            }
            case FOR_ENTRY__POSITIONAL_VARIABLE_SAME_AS_MAIN_VARIABLE_NAME: {
                return "Positional variable name must be distinct from main variable name";
            }
            case FOR_ENTRY__WRONG_ITERABLE_TYPE: {
                return "ForEntryBinding requires a single map value";
            }
            case FOR_MEMBER__WRONG_ITERABLE_TYPE: {
                final var arrayType = error.data().get(0);
                return "ForMemberBinding requires a single array value; received: " + arrayType;
            }
            case FUNCTION__DUPLICATED_ARG_NAME: {
                final var parameterName = error.data().get(0);
                return "Duplicate parameter name: " + parameterName;
            }
            case FUNCTION__INVALID_BODY_TYPE: {
                final var inlineType = error.data().get(0);
                final var returnedType = error.data().get(1);
                return String.format(
                    "Function body type %s is not a subtype of the declared return type %s",
                    inlineType.toString(), returnedType.toString()
                    );
            }
            case FUNCTION__INVALID_DEFAULT: {
                final var dvt = error.data().get(0);
                final var paramType = error.data().get(1);
                return "Invalid default value: " + dvt + " is not subtype of " + paramType;
            }
            case FUNCTION__INVALID_RETURNED_TYPE: {
                final var bodyType = error.data().get(0);
                final var returned = error.data().get(1);
                return "Invalid returned type: " + bodyType + " is not subtype of " + returned;
            }
            case FUNCTION__POSITIONAL_ARG_BEFORE_DEFAULT: {
                return "Positional arguments must be located before default arguments";
            }
            case FUNCTION__UNKNOWN_NAMESPACE: {
                final var namespace = error.data().get(0);
                return "Unknown function namespace: " + namespace;
            }
            case FUNCTION__UNKNOWN_FUNCTION: {
                final var namespace = error.data().get(0);
                final var functionName = error.data().get(1);
                return "Unknown function: " + namespace + ":" + functionName;

            }
            case FUNCTION__NO_MATCHING_FUNCTION: {
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
            case FUNCTION_REFERENCE__UNKNOWN: {
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
            case GENERAL_COMP__INVALID: {
                final var leftHandSide = error.data().get(0);
                final var rightHandSide = error.data().get(1);
                return String.format("The types: %s and %s in general comparison are not comparable",
                    leftHandSide.toString(), rightHandSide.toString());
            }
            case GROUP_BY__DUPLICATED_VAR: {
                final var varname = error.data().get(0);
                return "Grouping variable: " + varname + " used multiple times";
            }
            case GROUP_BY__UNDEFINED_GROUPING_VARIABLE: {
                final var varname = error.data().get(0);
                return "Variable: " + varname + " is not defined";
            }
            case GROUP_BY__WRONG_GROUPING_VAR_TYPE:{
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
            case IF__CONDITION_NON_EBV: {
                return String.format(
                "If condition must have an effective boolean value and the type %s doesn't have one",
                    error.data().get(0));
            }
            case IMPORT_MODULE__NO_PATH_FOUND: {
                return error.data().get(0).toString();

            }
            case INTERSECT_OR_EXCEPT__INVALID: {
                final var expressionType = error.data().get(0);
                return "Expression of operator node()* except/intersect node()* does match the type 'node()'"
                        + "\n\treceived type: " + expressionType;
            }
            case ITEM_DECLARATION__ALREADY_REGISTERED_DIFFERENT: {
                final var typeName = error.data().get(0);
                final var expected = error.data().get(1);
                return typeName + " has already been registered as type: " + expected;
            }
            case ITEM_DECLARATION__ALREADY_REGISTERED_SAME:
                break;
            case LOOKUP__ARRAY_INVALID_KEY: {
                final var targetType = error.data().get(0);
                final var keySpecifierType = error.data().get(1);
                return "Key type for lookup expression on " + targetType + " must be of type number*"
                    + "\n\ttarget type: " + targetType
                    + "\n\t   key type: " + keySpecifierType
                    ;
            }
            case LOOKUP__ARRAY_OR_MAP_INVALID_KEY: {
                final var targetType = error.data().get(0);
                final var expectedKeyItemtype = error.data().get(1);
                return "Key type for lookup expression on "
                    + targetType
                    + " must be subtype of type "
                    + expectedKeyItemtype;
            }
            case LOOKUP__INVALID_EXTENDED_RECORD_KEY_TYPE: {
                final var targetType = error.data().get(0);
                final var expectedKeyItemtype = error.data().get(1);
                return "Key type for lookup expression on " + targetType + " must be subtype of type " + expectedKeyItemtype;
            }
            case LOOKUP__INVALID_RECORD_KEY_NAME: {
                final var key = error.data().get(0);
                final var targetType = error.data().get(1);
                return "Key specifier: " + key + " does not match record of type " + targetType;
            }
            case LOOKUP__INVALID_RECORD_KEY_TYPE: {
                final var targetType = error.data().get(0);
                final var expectedKeyItemtype = error.data().get(1);
                return "Key type for lookup expression on "
                    + targetType
                    + " must be subtype of type "
                    + expectedKeyItemtype;
            }
            case LOOKUP__INVALID_TARGET: {
                return "Left side of lookup expression '<left> ? ...' must be map(*)* or array(*)*"
                    + "\n\treceived: " + error.data().get(0);
            }
            case LOOKUP__MAP_INVALID_KEY:
                break;
            case MAPPING__EMPTY_SEQUENCE: {
                return "Mapping empty sequence";
            }
            case MUL__INVALID: {
                final var visitedType = error.data().get(0);
                return "Multiplicative expression requires a number, received: " + visitedType;
            }
            case NODE_COMP__BOTH_INVALID: {
                return "Operands of node comparison must be of type 'node()?'"
                            +"\n\tinvalid left: " + error.data().get(0)
                            +"\n\tinvalid right: " + error.data().get(1);
            }
            case NODE_COMP__LHS_INVALID: {
                return "Operands of node comparison must be of type 'node()?'"
                            +"\n\tinvalid left: " + error.data().get(0);
            }
            case NODE_COMP__RHS_INVALID: {
                return "Operands of node comparison must be of type 'node()?'"
                            +"\n\tinvalid right: " + error.data().get(0);
            }
            case NODE_TEST__LHS_INVALID: {
                return "Path expression requires left hand side argument to be of type node()*"
                        +"\n\tfound: " + error.data().get(0);
            }
            case NODE_TEST__RHS_INVALID: {
                return "Node test requires node()*\n\treceived: " + error.data().get(0);
            }
            case NODE_TEST__UNRECOGNIZED_RULE_NAMES: {
                final Object joinedNames = error.data().get(0);
                return "Path expression references unrecognized rule names: " + joinedNames;
            }
            case OR__NON_EBV: {
                return "Operands of 'or expression' need to have effective boolean value; received: " + error.data().get(0);
            }
            case PARAM__NAMESPACE: {
                return "Parameter " + error.data().get(0) + " cannot have a namespace";
            }
            case PATH_EXPR__CONTEXT_NOT_NODES: {
                final var contexttype = error.data().get(0);
                return "Path expression starting from root requires context to be of type node()*;\n\tfound: " + contexttype;
            }
            case PATH_EXPR__CONTEXT_TYPE_ABSENT: {
                return "Path expression starting from root requires context to be present and of type node()*";
            }
            case PREDICATE__NON_EBV: {
                final var predicateExpression = error.data().get(0);
                return String.format(
                    "Predicate requires either number* type (for item by index aquisition) or a value that has effective boolean value, provided type: %s",
                    predicateExpression);
            }
            case QUANTIFIED__CRITERION_NON_EBV: {
                return "Criterion value needs to have effective boolean value; received: " + error.data().get(0);
            }
            case RANGE__INVALID_FROM: {
                return "Wrong type in 'from' operand of 'range expression': '<number?> to <number?>'";
            }
            case RANGE__INVALID_TO: {
                return "Wrong type in 'to' operand of range expression: '<number?> to <number?>'";
            }
            case RANGE__INVALID_BOTH: {
                return "Wrong type in 'to' and 'from' operands of range expression: '<number?> to <number?>'"
                        + "\n\tfrom: " + error.data().get(0)
                        + "\n\tto  : " + error.data().get(1)
                        ;
            }
            case RECORD_DECLARATION__ALREADY_REGISTERED_DIFFERENT: {
                final var typeName = error.data().get(0);
                final var expr = error.data().get(1);
                return typeName + " has already been registered as type: " + expr;
            }
            case RECORD_DECLARATION__ALREADY_REGISTERED_SAME:{
                final var typeName = error.data().get(0);
                return typeName + " has already been registered";
            }
            case RESTRICTED_DYNAMIC_CALL__NON_FUNCTION: {
                final var value = error.data().get(0);
                return "Expected function in dynamic function call expression, received: " + value;
            }
            case RESTRICTED_DYNAMIC_CALL__INVALID_FUNCTION: {
                final var expectedFunction = error.data().get(0);
                final var value = error.data().get(1);
                return "Dynamic function call expects: " + expectedFunction + " received: " + value;
            }
            case SWITCH__INVALID_CASE: {
                final var operandType = error.data().get(0);
                final var comparand = error.data().get(1);
                return "Invalid operand type; " + operandType + " is not a subtype of " + comparand;
            }
            case SWITCH__UNCOERSABLE:
                break;
            case TRY_CATCH__DUPLICATED_ERROR: {
                final var name = error.data().get(0);
                return "Error: " + name + "already used in catch clause";
            }
            case TRY_CATCH__FINALLY_NON_EMPTY: {
                final var finallyType = error.data().get(0);
                return "Finally clause needs to evaluate to empty sequence, currently:" + finallyType;
            }
            case TRY_CATCH__NON_ERROR: {
                final var typeRef = error.data().get(0);
                final var err = error.data().get(1);
                return "Type "
                    + typeRef.toString()
                    + " is not an error in try/catch: "
                    + err;

            }
            case TRY_CATCH__UNKNOWN_ERROR: {
                return "Unknown error in try/catch: " + error;
            }
            case TRY_CATCH__UNNECESSARY_ERROR_BECAUSE_OF_WILDCARD: {
                return "Unnecessary catch clause, wildcard already used";
            }
            case TYPE_NAME__UNKNOWN: {

            }
            case UNARY__INVALID: {
                return "Arithmetic unary expression requires a number"
                        + "\n\treceived: " + error.data().get(0);
            }
            case UNION__INVALID: {
                return "Expression of union operator node()* | node()* does match the type 'node()';"
                        + "\n\treceived: " + error.data().get(0);
            }
            case VALUE_COMP__INCOMPARABLE: {
                return "Given operands of 'value comparison' are incomparable"
                        +"\n\t left operand: " + error.data().get(0)
                        +"\n\tright operand: " + error.data().get(1)
                        ;
            }
            case VALUE_COMP__LHS_INVALID: {
                return "Left hand side of 'value comparison' must be of type 'item()?'"
                        +"\n\treceived: " + error.data().get(0);
            }
            case VALUE_COMP__RHS_INVALID: {
                return "Right hand side of 'value comparison' must be of type 'item()?'"
                        +"\n\treceived: " + error.data().get(0);
            }
            case VALUE_COMP__BOTH_INVALID: {
                return "Operands of 'value comparison' must be of type 'item()?'"
                        +"\n\t left operand: " + error.data().get(0)
                        +"\n\tright operand: " + error.data().get(1);

            }
            case VARIABLE_DECLARATION__ASSIGNED_TYPE_INCOMPATIBLE: {
                final Object variableName = error.data().get(0);
                final Object inferredType = error.data().get(1);
                final Object declaredType = error.data().get(2);
                return String.format(
                    "Type of variable %s is not compatible with the assigned value: %s is not subtype of %s",
                    variableName, inferredType, declaredType);
            }
            case VAR_DECL_WITH_COERSION__INVALID: {
                final Object assignedType = error.data().get(0);
                final Object desiredType = error.data().get(1);
                return String.format("Type: %s is not coercable to %s", assignedType, desiredType);
            }
            case VAR_DECL__UNCOERSABLE: {
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
            case VAR_REF__UNDECLARED: {
                return "Undeclared variable referenced: " + error.data().get(0);
            }
            case WINDOW__DECLARATION_MISMATCH: {
                final var windowDeclaredVarType = error.data().get(0);
                final var windowSequenceType = error.data().get(1);
                return "Mismatched types; declared: " + windowDeclaredVarType + " is not subtype of received: " + windowSequenceType;
            }
            case WINDOW__END_CLAUSE_CONDITION_NOT_EBV: {
                final var conditionType = error.data().get(0);
                return "Condition must have effective boolean value, received: " + conditionType;
            }
            case WINDOW__START_CLAUSE_CONDITION_NOT_EBV: {
                final var conditionType = error.data().get(0);
                return "Condition must have effective boolean value, received: " + conditionType;
            }
            case CHAR__INVALID_UNICODE_POINT: {
                final var charCode = error.data().get(0);
                final var i = error.data().get(1);
                return "Invalid Unicode code point: " + charCode + " at index: " + i;

            }
            case CHAR__INVALID_CHARACTER_REFERENCE: {
                final var code = error.data().get(0);
                final var i = error.data().get(1);
                return "Invalid character reference: " + code + " at index: " + i;
            }
            case CHAR__UNTERMINATED_CHARACTER_REFERENCE: {
                final var i = error.data().get(0);
                return "Unterminated character reference at " + i;
            }
        }
        return null;

    }

    public String create(final DiagnosticWarning warning) {
        switch(warning.type()) {
        case CASTABLE__UNNECESSARY: {
            return "Unnecessary castability test"
                + "\n\tatomized: " + warning.data().get(0)
                + "\n\t  tested: " + warning.data().get(1)
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
        case NODE_TEST__DUPLICATED_NAME: {

        }
        case OTHERWISE__IMPOSSIBLE: {
            return "Unnecessary otherwise expression"
                    + "\n\ttype: " + warning.data().get(0) + " can never be an empty sequence";
        }
        case PATH_OPERATOR__EMPTY_SEQUENCE: {
            return "Empty sequence as target of path operator";
        }
        case TREAT__UNLIKELY:
            return "Unlikely treat expression"
                + "\n\ta: " + warning.data().get(0)
                + "\n\tb: " + warning.data().get(1)
                ;
        }
        return null;

    }

}
