// Generated from ./src/test/testgrammars/Test.g4 by ANTLR 4.13.2
package com.github.akruk.antlrxquery.testgrammars;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TestParser}.
 */
public interface TestListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TestParser#test}.
	 * @param ctx the parse tree
	 */
	void enterTest(TestParser.TestContext ctx);
	/**
	 * Exit a parse tree produced by {@link TestParser#test}.
	 * @param ctx the parse tree
	 */
	void exitTest(TestParser.TestContext ctx);
	/**
	 * Enter a parse tree produced by {@link TestParser#rule}.
	 * @param ctx the parse tree
	 */
	void enterRule(TestParser.RuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link TestParser#rule}.
	 * @param ctx the parse tree
	 */
	void exitRule(TestParser.RuleContext ctx);
}