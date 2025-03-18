// Generated from D:/Programowanie/antlr-xquery/src/test/testgrammars/Test.g4 by ANTLR 4.13.2
package com.github.akruk.antlrxquery.testgrammars;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class TestParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		A=1, B=2, C=3, D=4, WS=5;
	public static final int
		RULE_test = 0, RULE_rule = 1;
	private static String[] makeRuleNames() {
		return new String[] {
			"test", "rule"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'a'", "'b'", "'c'", "'d'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "A", "B", "C", "D", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Test.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TestParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TestContext extends ParserRuleContext {
		public List<TerminalNode> A() { return getTokens(TestParser.A); }
		public TerminalNode A(int i) {
			return getToken(TestParser.A, i);
		}
		public List<RuleContext> rule_() {
			return getRuleContexts(RuleContext.class);
		}
		public RuleContext rule_(int i) {
			return getRuleContext(RuleContext.class,i);
		}
		public TestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_test; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterTest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitTest(this);
		}
	}

	public final TestContext test() throws RecognitionException {
		TestContext _localctx = new TestContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_test);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(6);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(6);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case A:
					{
					setState(4);
					match(A);
					}
					break;
				case B:
				case D:
					{
					setState(5);
					rule_();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(8);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 22L) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RuleContext extends ParserRuleContext {
		public TerminalNode B() { return getToken(TestParser.B, 0); }
		public TerminalNode C() { return getToken(TestParser.C, 0); }
		public TerminalNode D() { return getToken(TestParser.D, 0); }
		public RuleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).enterRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TestListener ) ((TestListener)listener).exitRule(this);
		}
	}

	public final RuleContext rule_() throws RecognitionException {
		RuleContext _localctx = new RuleContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_rule);
		try {
			setState(13);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case B:
				enterOuterAlt(_localctx, 1);
				{
				setState(10);
				match(B);
				setState(11);
				match(C);
				}
				break;
			case D:
				enterOuterAlt(_localctx, 2);
				{
				setState(12);
				match(D);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0005\u0010\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0001\u0000\u0001\u0000\u0004\u0000\u0007\b\u0000\u000b\u0000\f\u0000"+
		"\b\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u000e\b\u0001\u0001"+
		"\u0001\u0000\u0000\u0002\u0000\u0002\u0000\u0000\u0010\u0000\u0006\u0001"+
		"\u0000\u0000\u0000\u0002\r\u0001\u0000\u0000\u0000\u0004\u0007\u0005\u0001"+
		"\u0000\u0000\u0005\u0007\u0003\u0002\u0001\u0000\u0006\u0004\u0001\u0000"+
		"\u0000\u0000\u0006\u0005\u0001\u0000\u0000\u0000\u0007\b\u0001\u0000\u0000"+
		"\u0000\b\u0006\u0001\u0000\u0000\u0000\b\t\u0001\u0000\u0000\u0000\t\u0001"+
		"\u0001\u0000\u0000\u0000\n\u000b\u0005\u0002\u0000\u0000\u000b\u000e\u0005"+
		"\u0003\u0000\u0000\f\u000e\u0005\u0004\u0000\u0000\r\n\u0001\u0000\u0000"+
		"\u0000\r\f\u0001\u0000\u0000\u0000\u000e\u0003\u0001\u0000\u0000\u0000"+
		"\u0003\u0006\b\r";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}