package com.github.akruk.antlrxquery.languagefeatures.evaluation.varia;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class T extends EvaluationTestsBase {

    @Test
    void t() throws Exception
    {
        assertDynamicGrammarQuery(
            "Java20",
            Path.of("src/test/java/com/github/akruk/antlrxquery/languagefeatures/evaluation/varia/Java20.g4"),
            "start_",
            """

                public class test {
                    public static String localvar(String[] args) {
                        String x = args.length == 0? "a": "b";
                        return x;
                    }

                    public static String yield(String[] args) {
                        Integer x = 0;
                        return switch(x) {
                        case 0 -> {

                            yield args.length == 0? "a" : "b";
                        }
                        default -> null;
                        };
                    }

                    public static String return_(String[] args) {
                        return args.length == 0? "a": "b";
                    }
                    public static void main(String[] args) {
                        return;
                    }
                }
            """,
            """
                                    for $lvd in //localVariableDeclaration
                                let $declarators := $lvd//variableDeclarator
                                for $declarator at $declaratori in $declarators
                                let $conditionalExpression := $declarator/variableInitializer/expression/assignmentExpression/conditionalExpression[./expression]
                                return if ($conditionalExpression) {
                                    let $moved-type := $lvd/localVariableType
                                    let $moved-modifiers := $lvd/variableModifier
                                    let $moved-declarators := $declarators[1 to $declaratori - 1]
                                    let $remaining-declarators := $declarators[$declaratori + 1 to fn:count($declarators)]
                                    let $varname := $declarators/variableDeclaratorId
                                    let $previous-decl-line := if ($moved-declarators) {
                                        ``[`{($moved-modifiers=!>string(), $moved-type=!>string()) => string-join(" ")}` `{$moved-declarators=>string-join(", ")}`;]``
                                    }
                                    let $if-part :=
                ``[
                `{($moved-modifiers[. != 'final']=!>string(), $moved-type=!>string()) => string-join(" ")}` `{$varname}`;
                if (`{$conditionalExpression/conditionalOrExpression}`) {
                    `{$varname}` = `{$conditionalExpression/expression}`;
                } else {
                    `{$varname}` = `{$conditionalExpression/lambdaexpression otherwise $conditionalExpression/conditionalExpression}`;
                }
                ]``
                                    let $remaining-decl-line := if ($remaining-declarators) {
                                        ``[`{($moved-modifiers=!>string(), $moved-type=!>string()) => string-join(" ")}` `{$remaining-declarators=>string-join(", ")}`;]``
                                    }
                                        return
                                            ($previous-decl-line, $if-part, remaining-decl-line) => string-join("\n")
                                }


                                                                """,
            XQueryValue.sequence(
                List.of(XQueryValue.string(
                    """

                    String x;
                    if (args.length==0) {
                        x = "a";
                    } else {
                        x = "b";
                    }
                    """.stripIndent(),
                        typeFactory.string())),
                typeFactory.zeroOrMore(typeFactory.itemString())
            )
        );


    }





}
