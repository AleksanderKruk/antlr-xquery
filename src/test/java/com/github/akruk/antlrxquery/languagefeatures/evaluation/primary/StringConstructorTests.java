package com.github.akruk.antlrxquery.languagefeatures.evaluation.primary;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class StringConstructorTests extends EvaluationTestsBase {
    @Test
    public void stringConstructorEmpty() {
        assertResult("``[]``", "");
    }

    @Test
    public void stringConstructorStaticText() {
        assertResult("``[Hello World]``", "Hello World");
    }

    @Test
    public void stringConstructorWithSpaces() {
        assertResult("``[  Hello  World  ]``", "  Hello  World  ");
    }

    @Test
    public void stringConstructorSimpleInterpolation() {
        assertResult("let $x := 'test' return ``[`{$x}`]``", "test");
    }

    @Test
    public void stringConstructorInterpolationWithSpaces() {
        assertResult("let $x := 'value' return ``[  `{$x}`  ]``", "  value  ");
    }

    @Test
    public void stringConstructorMultipleInterpolations() {
        assertResult("let $x := 'Hello', $y := 'World' return ``[`{$x}` `{$y}`]``", "Hello World");
    }

    @Test
    public void stringConstructorMixedContent() {
        assertResult("let $name := 'John' return ``[Hello `{$name}`, welcome!]``", "Hello John, welcome!");
    }

    @Test
    public void stringConstructorNumberInterpolation() {
        assertResult("let $x := 42 return ``[The answer is `{$x}`]``", "The answer is 42");
    }

    @Test
    public void stringConstructorDecimalInterpolation() {
        assertResult("let $x := 3.14 return ``[Pi is `{$x}`]``", "Pi is 3.14");
    }

    @Test
    public void stringConstructorBooleanInterpolation() {
        assertResult("let $x := true() return ``[Result: `{$x}`]``", "Result: true");
    }

    @Test
    public void stringConstructorEmptyInterpolation() {
        assertResult("``[Before`{}`After]``", "BeforeAfter");
    }

    @Test
    public void stringConstructorSequenceInterpolation() {
        assertResult("let $seq := (1, 2, 3) return ``[Numbers: `{$seq}`]``", "Numbers: 1 2 3");
    }

    @Test
    public void stringConstructorEmptySequenceInterpolation() {
        assertResult("let $seq := () return ``[Empty: `{$seq}`]``", "Empty: ");
    }

    @Test
    public void stringConstructorStringSequenceInterpolation() {
        assertResult("let $seq := ('a', 'b', 'c') return ``[Letters: `{$seq}`]``", "Letters: a b c");
    }

    @Test
    public void stringConstructorExpressionInterpolation() {
        assertResult("let $x := 5, $y := 3 return ``[Sum: `{$x + $y}`]``", "Sum: 8");
    }

    @Test
    public void stringConstructorFunctionCallInterpolation() {
        assertResult("``[Length: `{string-length('hello')}`]``", "Length: 5");
    }

    @Test
    public void stringConstructorConditionalInterpolation() {
        assertResult("let $x := 10 return ``[`{if ($x > 5) then 'big' else 'small'}`]``", "big");
    }

    // @Test
    // public void stringConstructorNestedExpressionInterpolation() {
    // assertResult("let $items := ('apple', 'banana') return ``[Count:
    // `{count($items)}`]``", "Count: 2");
    // }

    @Test
    public void stringConstructorComplexExample() {
        assertResult("let $name := 'Alice', $age := 25 return ``[User `{$name}` is `{$age}` years old]``",
                "User Alice is 25 years old");
    }

    @Test
    public void stringConstructorMultiLine() {
        assertResult("let $x := 'test' return ``[Line 1\n`{$x}`\nLine 3]``", "Line 1\ntest\nLine 3");
    }

    @Test
    public void stringConstructorWithTabs() {
        assertResult("let $x := 'value' return ``[\t`{$x}`\t]``", "\tvalue\t");
    }

    @Test
    public void stringConstructorBacktick() {
        assertResult("""
                ``[This is a `backtick]``
                """, "This is a `backtick");
    }

    @Test
    public void stringConstructorEscapedBraces() {
        assertResult("``[This is {not interpolation}]``", "This is {not interpolation}");
    }

    @Test
    public void stringConstructorWithNewlines() {
        assertResult("``[First line\nSecond line]``", "First line\nSecond line");
    }

    @Test
    public void stringConstructorWithNewlinesInline() {
        assertResult("``[First line\\nSecond line]``", "First line\nSecond line");
    }

    @Test
    public void stringConstructorWithCarriageReturn() {
        assertResult("``[First\\rSecond]``", "First\rSecond");
    }

    @Test
    public void stringConstructorWithBackslash() {
        assertResult("``[Path: C:\\\\Users\\\\test]``", "Path: C:\\Users\\test");
    }

    @Test
    public void stringConstructorFLWORExpression() {
        assertResult("``[Result: `{for $i in (1, 2, 3) return $i * 2}`]``", "Result: 2 4 6");
    }

    @Test
    public void stringConstructorQuantifiedExpression() {
        assertResult("let $nums := (2, 4, 6) return ``[All even: `{every $n in $nums satisfies $n mod 2 = 0}`]``",
                "All even: true");
    }

    @Test
    public void stringConstructorWithParentheses() {
        assertResult("let $x := 5 return ``[(`{$x}`)]``", "(5)");
    }

    @Test
    public void stringConstructorWithBrackets() {
        assertResult("let $x := 'content' return ``[ [`{$x}`] ]``", " [content] ");
        assertResult("let $x := 'content' return ``[[`{$x}`] ]``", "[content] ");
        assertResult("let $x := 'content' return ``[ [`{$x}`]]``", " [content]");
        assertResult("let $x := 'content' return ``[[`{$x}`]]``", "[content]");
    }

    @Test
    public void stringConstructorChainedInterpolations() {
        assertResult("let $a := 'A', $b := 'B', $c := 'C' return ``[`{$a}``{$b}``{$c}`]``", "ABC");
    }

    @Test
    public void stringConstructorArithmeticInInterpolation() {
        assertResult("let $x := 10, $y := 5 return ``[`{$x}` + `{$y}` = `{$x + $y}`]``", "10 + 5 = 15");
    }

    @Test
    public void stringConstructorVariableReference() {
        assertResult("let $greeting := 'Hello', $target := 'World' return ``[`{$greeting}`, `{$target}`!]``",
                "Hello, World!");
    }

    @Test
    public void stringConstructorWithComparison() {
        assertResult("let $x := 5 return ``[5 > 3 is `{$x > 3}`]``", "5 > 3 is true");
    }

    @Test
    public void stringConstructorStringFunctions() {
        assertResult("let $text := 'hello' return ``[Upper: `{upper-case($text)}`]``", "Upper: HELLO");
    }


    // TODO: fix concat
    @Test
    public void stringConstructorConcatenation() {
        assertResult("let $first := 'Hello', $second := 'World' return ``[`{concat(($first, ' ', $second))}`]``",
                "Hello World");
    }

    @Test
    public void stringConstructorWithWhitespace() {
        assertResult("``[   `{'test'}`   ]``", "   test   ");
    }

}
