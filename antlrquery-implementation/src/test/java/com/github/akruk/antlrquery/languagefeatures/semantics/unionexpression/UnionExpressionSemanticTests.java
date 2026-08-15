package com.github.akruk.antlrquery.languagefeatures.semantics.unionexpression;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;

public class UnionExpressionSemanticTests extends SemanticTestsBase {

    @Test
    public void unionExpression() {
        assertType("""
                    let $x as node()* := (),
                        $y as node()* := (),
                        $z as node()* := ()
                    return $x | $y | $z
                """, typeFactory.zeroOrMore(typeFactory.itemAnyNode()));

        assertType("""
                    let $x as <a>* := (),
                        $y as <b>* := (),
                        $z as <c>* := ()
                    return $x | $y | $z
                """, typeFactory.zeroOrMore(typeFactory.itemRulesFromGrammar("", Set.of(
                    new QualifiedName("", "a"),
                    new QualifiedName("", "b"),
                    new QualifiedName("", "c")
                ))));

        assertErrors("""
                    let $x as number+ := (1, 2, 3)
                    return $x | $x
                """);
    }

    @Test
    public void ebv() {
        assertType("""
                    let $x as number? := 3
                    return if ($x)
                        then $x
                        else 1
                """, typeFactory.one(typeFactory.itemNumber()));
    }

}
