// $ANTLR 3.5.2 STParser.g 2015-02-20 23:47:58

package org.stringtemplate.v4.compiler;
import org.stringtemplate.v4.misc.ErrorManager;
import org.stringtemplate.v4.misc.ErrorType;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


/** Build an AST from a single StringTemplate template */
@SuppressWarnings("all")
public class STParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "IF", "ELSE", "ELSEIF", "ENDIF", 
		"SUPER", "SEMI", "BANG", "ELLIPSIS", "EQUALS", "COLON", "LPAREN", "RPAREN", 
		"LBRACK", "RBRACK", "COMMA", "DOT", "LCURLY", "RCURLY", "TEXT", "LDELIM", 
		"RDELIM", "ID", "STRING", "WS", "PIPE", "OR", "AND", "INDENT", "NEWLINE", 
		"AT", "END", "TRUE", "FALSE", "COMMENT", "ARGS", "ELEMENTS", "EXEC_FUNC", 
		"EXPR", "INCLUDE", "INCLUDE_IND", "INCLUDE_REGION", "INCLUDE_SUPER", "INCLUDE_SUPER_REGION", 
		"INDENTED_EXPR", "LIST", "MAP", "NULL", "OPTIONS", "PROP", "PROP_IND", 
		"REGION", "SUBTEMPLATE", "TO_STR", "ZIP"
	};
	public static final int EOF=-1;
	public static final int RBRACK=17;
	public static final int LBRACK=16;
	public static final int ELSE=5;
	public static final int ELLIPSIS=11;
	public static final int LCURLY=20;
	public static final int BANG=10;
	public static final int EQUALS=12;
	public static final int TEXT=22;
	public static final int ID=25;
	public static final int SEMI=9;
	public static final int LPAREN=14;
	public static final int IF=4;
	public static final int ELSEIF=6;
	public static final int COLON=13;
	public static final int RPAREN=15;
	public static final int WS=27;
	public static final int COMMA=18;
	public static final int RCURLY=21;
	public static final int ENDIF=7;
	public static final int RDELIM=24;
	public static final int SUPER=8;
	public static final int DOT=19;
	public static final int LDELIM=23;
	public static final int STRING=26;
	public static final int PIPE=28;
	public static final int OR=29;
	public static final int AND=30;
	public static final int INDENT=31;
	public static final int NEWLINE=32;
	public static final int AT=33;
	public static final int END=34;
	public static final int TRUE=35;
	public static final int FALSE=36;
	public static final int COMMENT=37;
	public static final int ARGS=38;
	public static final int ELEMENTS=39;
	public static final int EXEC_FUNC=40;
	public static final int EXPR=41;
	public static final int INCLUDE=42;
	public static final int INCLUDE_IND=43;
	public static final int INCLUDE_REGION=44;
	public static final int INCLUDE_SUPER=45;
	public static final int INCLUDE_SUPER_REGION=46;
	public static final int INDENTED_EXPR=47;
	public static final int LIST=48;
	public static final int MAP=49;
	public static final int NULL=50;
	public static final int OPTIONS=51;
	public static final int PROP=52;
	public static final int PROP_IND=53;
	public static final int REGION=54;
	public static final int SUBTEMPLATE=55;
	public static final int TO_STR=56;
	public static final int ZIP=57;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public STParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public STParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return STParser.tokenNames; }
	@Override public String getGrammarFileName() { return "STParser.g"; }


	ErrorManager errMgr;
	Token templateToken;
	public STParser(TokenStream input, ErrorManager errMgr, Token templateToken) {
		this(input);
		this.errMgr = errMgr;
		this.templateToken = templateToken;
	}
	@Override
	protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow)
		throws RecognitionException
	{
		throw new MismatchedTokenException(ttype, input);
	}


	public static class templateAndEOF_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "templateAndEOF"
	// STParser.g:72:1: templateAndEOF : template EOF -> ( template )? ;
	public final STParser.templateAndEOF_return templateAndEOF() throws RecognitionException {
		STParser.templateAndEOF_return retval = new STParser.templateAndEOF_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken EOF2=null;
		ParserRuleReturnScope template1 =null;

		CommonTree EOF2_tree=null;
		RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
		RewriteRuleSubtreeStream stream_template=new RewriteRuleSubtreeStream(adaptor,"rule template");

		try {
			// STParser.g:72:16: ( template EOF -> ( template )? )
			// STParser.g:72:18: template EOF
			{
			pushFollow(FOLLOW_template_in_templateAndEOF139);
			template1=template();
			state._fsp--;

			stream_template.add(template1.getTree());
			EOF2=(CommonToken)match(input,EOF,FOLLOW_EOF_in_templateAndEOF141);  
			stream_EOF.add(EOF2);

			// AST REWRITE
			// elements: template
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 72:31: -> ( template )?
			{
				// STParser.g:72:34: ( template )?
				if ( stream_template.hasNext() ) {
					adaptor.addChild(root_0, stream_template.nextTree());
				}
				stream_template.reset();

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "templateAndEOF"


	public static class template_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "template"
	// STParser.g:74:1: template : ( element )* ;
	public final STParser.template_return template() throws RecognitionException {
		STParser.template_return retval = new STParser.template_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope element3 =null;


		try {
			// STParser.g:74:10: ( ( element )* )
			// STParser.g:74:12: ( element )*
			{
			root_0 = (CommonTree)adaptor.nil();


			// STParser.g:74:12: ( element )*
			loop1:
			while (true) {
				int alt1=2;
				switch ( input.LA(1) ) {
				case INDENT:
					{
					int LA1_2 = input.LA(2);
					if ( (LA1_2==LDELIM) ) {
						int LA1_5 = input.LA(3);
						if ( (LA1_5==IF||LA1_5==SUPER||LA1_5==LPAREN||LA1_5==LBRACK||LA1_5==LCURLY||(LA1_5 >= ID && LA1_5 <= STRING)||LA1_5==AT||(LA1_5 >= TRUE && LA1_5 <= FALSE)) ) {
							alt1=1;
						}

					}
					else if ( (LA1_2==TEXT||LA1_2==NEWLINE||LA1_2==COMMENT) ) {
						alt1=1;
					}

					}
					break;
				case LDELIM:
					{
					int LA1_3 = input.LA(2);
					if ( (LA1_3==IF||LA1_3==SUPER||LA1_3==LPAREN||LA1_3==LBRACK||LA1_3==LCURLY||(LA1_3 >= ID && LA1_3 <= STRING)||LA1_3==AT||(LA1_3 >= TRUE && LA1_3 <= FALSE)) ) {
						alt1=1;
					}

					}
					break;
				case TEXT:
				case NEWLINE:
				case COMMENT:
					{
					alt1=1;
					}
					break;
				}
				switch (alt1) {
				case 1 :
					// STParser.g:74:12: element
					{
					pushFollow(FOLLOW_element_in_template155);
					element3=element();
					state._fsp--;

					adaptor.addChild(root_0, element3.getTree());

					}
					break;

				default :
					break loop1;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "template"


	public static class element_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "element"
	// STParser.g:76:1: element : ({...}? ( INDENT )? COMMENT NEWLINE ->| INDENT singleElement -> ^( INDENTED_EXPR INDENT ( singleElement )? ) | singleElement | compoundElement );
	public final STParser.element_return element() throws RecognitionException {
		STParser.element_return retval = new STParser.element_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken INDENT4=null;
		CommonToken COMMENT5=null;
		CommonToken NEWLINE6=null;
		CommonToken INDENT7=null;
		ParserRuleReturnScope singleElement8 =null;
		ParserRuleReturnScope singleElement9 =null;
		ParserRuleReturnScope compoundElement10 =null;

		CommonTree INDENT4_tree=null;
		CommonTree COMMENT5_tree=null;
		CommonTree NEWLINE6_tree=null;
		CommonTree INDENT7_tree=null;
		RewriteRuleTokenStream stream_NEWLINE=new RewriteRuleTokenStream(adaptor,"token NEWLINE");
		RewriteRuleTokenStream stream_COMMENT=new RewriteRuleTokenStream(adaptor,"token COMMENT");
		RewriteRuleTokenStream stream_INDENT=new RewriteRuleTokenStream(adaptor,"token INDENT");
		RewriteRuleSubtreeStream stream_singleElement=new RewriteRuleSubtreeStream(adaptor,"rule singleElement");

		try {
			// STParser.g:77:2: ({...}? ( INDENT )? COMMENT NEWLINE ->| INDENT singleElement -> ^( INDENTED_EXPR INDENT ( singleElement )? ) | singleElement | compoundElement )
			int alt3=4;
			switch ( input.LA(1) ) {
			case INDENT:
				{
				switch ( input.LA(2) ) {
				case COMMENT:
					{
					int LA3_5 = input.LA(3);
					if ( (LA3_5==NEWLINE) ) {
						int LA3_11 = input.LA(4);
						if ( ((input.LT(1).getCharPositionInLine()==0)) ) {
							alt3=1;
						}
						else if ( (true) ) {
							alt3=2;
						}

					}
					else if ( (LA3_5==EOF||(LA3_5 >= RCURLY && LA3_5 <= LDELIM)||LA3_5==INDENT||LA3_5==COMMENT) ) {
						alt3=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 3, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case LDELIM:
					{
					switch ( input.LA(3) ) {
					case IF:
						{
						alt3=4;
						}
						break;
					case AT:
						{
						int LA3_12 = input.LA(4);
						if ( (LA3_12==ID) ) {
							int LA3_15 = input.LA(5);
							if ( (LA3_15==RDELIM) ) {
								alt3=4;
							}
							else if ( (LA3_15==LPAREN) ) {
								alt3=2;
							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 3, 15, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}
						else if ( (LA3_12==SUPER) ) {
							alt3=2;
						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 3, 12, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

						}
						break;
					case SUPER:
					case LPAREN:
					case LBRACK:
					case LCURLY:
					case ID:
					case STRING:
					case TRUE:
					case FALSE:
						{
						alt3=2;
						}
						break;
					default:
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 3, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}
					}
					break;
				case TEXT:
				case NEWLINE:
					{
					alt3=2;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 3, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case COMMENT:
				{
				int LA3_2 = input.LA(2);
				if ( (LA3_2==NEWLINE) ) {
					int LA3_8 = input.LA(3);
					if ( ((input.LT(1).getCharPositionInLine()==0)) ) {
						alt3=1;
					}
					else if ( (true) ) {
						alt3=3;
					}

				}
				else if ( (LA3_2==EOF||(LA3_2 >= RCURLY && LA3_2 <= LDELIM)||LA3_2==INDENT||LA3_2==COMMENT) ) {
					alt3=3;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 3, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LDELIM:
				{
				switch ( input.LA(2) ) {
				case IF:
					{
					alt3=4;
					}
					break;
				case AT:
					{
					int LA3_10 = input.LA(3);
					if ( (LA3_10==ID) ) {
						int LA3_14 = input.LA(4);
						if ( (LA3_14==RDELIM) ) {
							alt3=4;
						}
						else if ( (LA3_14==LPAREN) ) {
							alt3=3;
						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 3, 14, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA3_10==SUPER) ) {
						alt3=3;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 3, 10, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case SUPER:
				case LPAREN:
				case LBRACK:
				case LCURLY:
				case ID:
				case STRING:
				case TRUE:
				case FALSE:
					{
					alt3=3;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 3, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case TEXT:
			case NEWLINE:
				{
				alt3=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}
			switch (alt3) {
				case 1 :
					// STParser.g:77:4: {...}? ( INDENT )? COMMENT NEWLINE
					{
					if ( !((input.LT(1).getCharPositionInLine()==0)) ) {
						throw new FailedPredicateException(input, "element", "input.LT(1).getCharPositionInLine()==0");
					}
					// STParser.g:77:46: ( INDENT )?
					int alt2=2;
					int LA2_0 = input.LA(1);
					if ( (LA2_0==INDENT) ) {
						alt2=1;
					}
					switch (alt2) {
						case 1 :
							// STParser.g:77:46: INDENT
							{
							INDENT4=(CommonToken)match(input,INDENT,FOLLOW_INDENT_in_element168);  
							stream_INDENT.add(INDENT4);

							}
							break;

					}

					COMMENT5=(CommonToken)match(input,COMMENT,FOLLOW_COMMENT_in_element171);  
					stream_COMMENT.add(COMMENT5);

					NEWLINE6=(CommonToken)match(input,NEWLINE,FOLLOW_NEWLINE_in_element173);  
					stream_NEWLINE.add(NEWLINE6);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 77:70: ->
					{
						root_0 = null;
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// STParser.g:78:4: INDENT singleElement
					{
					INDENT7=(CommonToken)match(input,INDENT,FOLLOW_INDENT_in_element181);  
					stream_INDENT.add(INDENT7);

					pushFollow(FOLLOW_singleElement_in_element183);
					singleElement8=singleElement();
					state._fsp--;

					stream_singleElement.add(singleElement8.getTree());
					// AST REWRITE
					// elements: singleElement, INDENT
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 78:25: -> ^( INDENTED_EXPR INDENT ( singleElement )? )
					{
						// STParser.g:78:28: ^( INDENTED_EXPR INDENT ( singleElement )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INDENTED_EXPR, "INDENTED_EXPR"), root_1);
						adaptor.addChild(root_1, stream_INDENT.nextNode());
						// STParser.g:78:51: ( singleElement )?
						if ( stream_singleElement.hasNext() ) {
							adaptor.addChild(root_1, stream_singleElement.nextTree());
						}
						stream_singleElement.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// STParser.g:79:4: singleElement
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_singleElement_in_element200);
					singleElement9=singleElement();
					state._fsp--;

					adaptor.addChild(root_0, singleElement9.getTree());

					}
					break;
				case 4 :
					// STParser.g:80:4: compoundElement
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_compoundElement_in_element205);
					compoundElement10=compoundElement();
					state._fsp--;

					adaptor.addChild(root_0, compoundElement10.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "element"


	public static class singleElement_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "singleElement"
	// STParser.g:83:1: singleElement : ( exprTag | TEXT | NEWLINE | COMMENT !);
	public final STParser.singleElement_return singleElement() throws RecognitionException {
		STParser.singleElement_return retval = new STParser.singleElement_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken TEXT12=null;
		CommonToken NEWLINE13=null;
		CommonToken COMMENT14=null;
		ParserRuleReturnScope exprTag11 =null;

		CommonTree TEXT12_tree=null;
		CommonTree NEWLINE13_tree=null;
		CommonTree COMMENT14_tree=null;

		try {
			// STParser.g:84:2: ( exprTag | TEXT | NEWLINE | COMMENT !)
			int alt4=4;
			switch ( input.LA(1) ) {
			case LDELIM:
				{
				alt4=1;
				}
				break;
			case TEXT:
				{
				alt4=2;
				}
				break;
			case NEWLINE:
				{
				alt4=3;
				}
				break;
			case COMMENT:
				{
				alt4=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 4, 0, input);
				throw nvae;
			}
			switch (alt4) {
				case 1 :
					// STParser.g:84:4: exprTag
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_exprTag_in_singleElement216);
					exprTag11=exprTag();
					state._fsp--;

					adaptor.addChild(root_0, exprTag11.getTree());

					}
					break;
				case 2 :
					// STParser.g:85:4: TEXT
					{
					root_0 = (CommonTree)adaptor.nil();


					TEXT12=(CommonToken)match(input,TEXT,FOLLOW_TEXT_in_singleElement221); 
					TEXT12_tree = (CommonTree)adaptor.create(TEXT12);
					adaptor.addChild(root_0, TEXT12_tree);

					}
					break;
				case 3 :
					// STParser.g:86:4: NEWLINE
					{
					root_0 = (CommonTree)adaptor.nil();


					NEWLINE13=(CommonToken)match(input,NEWLINE,FOLLOW_NEWLINE_in_singleElement226); 
					NEWLINE13_tree = (CommonTree)adaptor.create(NEWLINE13);
					adaptor.addChild(root_0, NEWLINE13_tree);

					}
					break;
				case 4 :
					// STParser.g:87:4: COMMENT !
					{
					root_0 = (CommonTree)adaptor.nil();


					COMMENT14=(CommonToken)match(input,COMMENT,FOLLOW_COMMENT_in_singleElement231); 
					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "singleElement"


	public static class compoundElement_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "compoundElement"
	// STParser.g:90:1: compoundElement : ( ifstat | region );
	public final STParser.compoundElement_return compoundElement() throws RecognitionException {
		STParser.compoundElement_return retval = new STParser.compoundElement_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope ifstat15 =null;
		ParserRuleReturnScope region16 =null;


		try {
			// STParser.g:91:2: ( ifstat | region )
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==INDENT) ) {
				int LA5_1 = input.LA(2);
				if ( (LA5_1==LDELIM) ) {
					int LA5_2 = input.LA(3);
					if ( (LA5_2==IF) ) {
						alt5=1;
					}
					else if ( (LA5_2==AT) ) {
						alt5=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 5, 2, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 5, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA5_0==LDELIM) ) {
				int LA5_2 = input.LA(2);
				if ( (LA5_2==IF) ) {
					alt5=1;
				}
				else if ( (LA5_2==AT) ) {
					alt5=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 5, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 5, 0, input);
				throw nvae;
			}

			switch (alt5) {
				case 1 :
					// STParser.g:91:4: ifstat
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_ifstat_in_compoundElement244);
					ifstat15=ifstat();
					state._fsp--;

					adaptor.addChild(root_0, ifstat15.getTree());

					}
					break;
				case 2 :
					// STParser.g:92:4: region
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_region_in_compoundElement249);
					region16=region();
					state._fsp--;

					adaptor.addChild(root_0, region16.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "compoundElement"


	public static class exprTag_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "exprTag"
	// STParser.g:95:1: exprTag : LDELIM expr ( ';' exprOptions )? RDELIM -> ^( EXPR[$LDELIM,\"EXPR\"] expr ( exprOptions )? ) ;
	public final STParser.exprTag_return exprTag() throws RecognitionException {
		STParser.exprTag_return retval = new STParser.exprTag_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken LDELIM17=null;
		CommonToken char_literal19=null;
		CommonToken RDELIM21=null;
		ParserRuleReturnScope expr18 =null;
		ParserRuleReturnScope exprOptions20 =null;

		CommonTree LDELIM17_tree=null;
		CommonTree char_literal19_tree=null;
		CommonTree RDELIM21_tree=null;
		RewriteRuleTokenStream stream_RDELIM=new RewriteRuleTokenStream(adaptor,"token RDELIM");
		RewriteRuleTokenStream stream_LDELIM=new RewriteRuleTokenStream(adaptor,"token LDELIM");
		RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
		RewriteRuleSubtreeStream stream_exprOptions=new RewriteRuleSubtreeStream(adaptor,"rule exprOptions");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");

		try {
			// STParser.g:96:2: ( LDELIM expr ( ';' exprOptions )? RDELIM -> ^( EXPR[$LDELIM,\"EXPR\"] expr ( exprOptions )? ) )
			// STParser.g:96:4: LDELIM expr ( ';' exprOptions )? RDELIM
			{
			LDELIM17=(CommonToken)match(input,LDELIM,FOLLOW_LDELIM_in_exprTag260);  
			stream_LDELIM.add(LDELIM17);

			pushFollow(FOLLOW_expr_in_exprTag262);
			expr18=expr();
			state._fsp--;

			stream_expr.add(expr18.getTree());
			// STParser.g:96:16: ( ';' exprOptions )?
			int alt6=2;
			int LA6_0 = input.LA(1);
			if ( (LA6_0==SEMI) ) {
				alt6=1;
			}
			switch (alt6) {
				case 1 :
					// STParser.g:96:18: ';' exprOptions
					{
					char_literal19=(CommonToken)match(input,SEMI,FOLLOW_SEMI_in_exprTag266);  
					stream_SEMI.add(char_literal19);

					pushFollow(FOLLOW_exprOptions_in_exprTag268);
					exprOptions20=exprOptions();
					state._fsp--;

					stream_exprOptions.add(exprOptions20.getTree());
					}
					break;

			}

			RDELIM21=(CommonToken)match(input,RDELIM,FOLLOW_RDELIM_in_exprTag273);  
			stream_RDELIM.add(RDELIM21);

			// AST REWRITE
			// elements: expr, exprOptions
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 97:3: -> ^( EXPR[$LDELIM,\"EXPR\"] expr ( exprOptions )? )
			{
				// STParser.g:97:6: ^( EXPR[$LDELIM,\"EXPR\"] expr ( exprOptions )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, LDELIM17, "EXPR"), root_1);
				adaptor.addChild(root_1, stream_expr.nextTree());
				// STParser.g:97:34: ( exprOptions )?
				if ( stream_exprOptions.hasNext() ) {
					adaptor.addChild(root_1, stream_exprOptions.nextTree());
				}
				stream_exprOptions.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "exprTag"


	public static class region_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "region"
	// STParser.g:100:1: region : (i= INDENT )? x= LDELIM '@' ID RDELIM template ( INDENT )? LDELIM '@end' RDELIM ({...}? => NEWLINE )? -> {indent!=null}? ^( INDENTED_EXPR $i ^( REGION[$x] ID ( template )? ) ) -> ^( REGION[$x] ID ( template )? ) ;
	public final STParser.region_return region() throws RecognitionException {
		STParser.region_return retval = new STParser.region_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken i=null;
		CommonToken x=null;
		CommonToken char_literal22=null;
		CommonToken ID23=null;
		CommonToken RDELIM24=null;
		CommonToken INDENT26=null;
		CommonToken LDELIM27=null;
		CommonToken string_literal28=null;
		CommonToken RDELIM29=null;
		CommonToken NEWLINE30=null;
		ParserRuleReturnScope template25 =null;

		CommonTree i_tree=null;
		CommonTree x_tree=null;
		CommonTree char_literal22_tree=null;
		CommonTree ID23_tree=null;
		CommonTree RDELIM24_tree=null;
		CommonTree INDENT26_tree=null;
		CommonTree LDELIM27_tree=null;
		CommonTree string_literal28_tree=null;
		CommonTree RDELIM29_tree=null;
		CommonTree NEWLINE30_tree=null;
		RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
		RewriteRuleTokenStream stream_RDELIM=new RewriteRuleTokenStream(adaptor,"token RDELIM");
		RewriteRuleTokenStream stream_NEWLINE=new RewriteRuleTokenStream(adaptor,"token NEWLINE");
		RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
		RewriteRuleTokenStream stream_END=new RewriteRuleTokenStream(adaptor,"token END");
		RewriteRuleTokenStream stream_LDELIM=new RewriteRuleTokenStream(adaptor,"token LDELIM");
		RewriteRuleTokenStream stream_INDENT=new RewriteRuleTokenStream(adaptor,"token INDENT");
		RewriteRuleSubtreeStream stream_template=new RewriteRuleSubtreeStream(adaptor,"rule template");

		Token indent=null;
		try {
			// STParser.g:102:2: ( (i= INDENT )? x= LDELIM '@' ID RDELIM template ( INDENT )? LDELIM '@end' RDELIM ({...}? => NEWLINE )? -> {indent!=null}? ^( INDENTED_EXPR $i ^( REGION[$x] ID ( template )? ) ) -> ^( REGION[$x] ID ( template )? ) )
			// STParser.g:102:4: (i= INDENT )? x= LDELIM '@' ID RDELIM template ( INDENT )? LDELIM '@end' RDELIM ({...}? => NEWLINE )?
			{
			// STParser.g:102:5: (i= INDENT )?
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0==INDENT) ) {
				alt7=1;
			}
			switch (alt7) {
				case 1 :
					// STParser.g:102:5: i= INDENT
					{
					i=(CommonToken)match(input,INDENT,FOLLOW_INDENT_in_region305);  
					stream_INDENT.add(i);

					}
					break;

			}

			x=(CommonToken)match(input,LDELIM,FOLLOW_LDELIM_in_region310);  
			stream_LDELIM.add(x);

			char_literal22=(CommonToken)match(input,AT,FOLLOW_AT_in_region312);  
			stream_AT.add(char_literal22);

			ID23=(CommonToken)match(input,ID,FOLLOW_ID_in_region314);  
			stream_ID.add(ID23);

			RDELIM24=(CommonToken)match(input,RDELIM,FOLLOW_RDELIM_in_region316);  
			stream_RDELIM.add(RDELIM24);

			if (input.LA(1)!=NEWLINE) indent=i;
			pushFollow(FOLLOW_template_in_region322);
			template25=template();
			state._fsp--;

			stream_template.add(template25.getTree());
			// STParser.g:104:3: ( INDENT )?
			int alt8=2;
			int LA8_0 = input.LA(1);
			if ( (LA8_0==INDENT) ) {
				alt8=1;
			}
			switch (alt8) {
				case 1 :
					// STParser.g:104:3: INDENT
					{
					INDENT26=(CommonToken)match(input,INDENT,FOLLOW_INDENT_in_region326);  
					stream_INDENT.add(INDENT26);

					}
					break;

			}

			LDELIM27=(CommonToken)match(input,LDELIM,FOLLOW_LDELIM_in_region329);  
			stream_LDELIM.add(LDELIM27);

			string_literal28=(CommonToken)match(input,END,FOLLOW_END_in_region331);  
			stream_END.add(string_literal28);

			RDELIM29=(CommonToken)match(input,RDELIM,FOLLOW_RDELIM_in_region333);  
			stream_RDELIM.add(RDELIM29);

			// STParser.g:106:3: ({...}? => NEWLINE )?
			int alt9=2;
			int LA9_0 = input.LA(1);
			if ( (LA9_0==NEWLINE) ) {
				int LA9_1 = input.LA(2);
				if ( ((((CommonToken)retval.start).getLine()!=input.LT(1).getLine())) ) {
					alt9=1;
				}
			}
			switch (alt9) {
				case 1 :
					// STParser.g:106:4: {...}? => NEWLINE
					{
					if ( !((((CommonToken)retval.start).getLine()!=input.LT(1).getLine())) ) {
						throw new FailedPredicateException(input, "region", "$region.start.getLine()!=input.LT(1).getLine()");
					}
					NEWLINE30=(CommonToken)match(input,NEWLINE,FOLLOW_NEWLINE_in_region344);  
					stream_NEWLINE.add(NEWLINE30);

					}
					break;

			}

			// AST REWRITE
			// elements: template, ID, ID, template, i
			// token labels: i
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleTokenStream stream_i=new RewriteRuleTokenStream(adaptor,"token i",i);
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 107:3: -> {indent!=null}? ^( INDENTED_EXPR $i ^( REGION[$x] ID ( template )? ) )
			if (indent!=null) {
				// STParser.g:108:6: ^( INDENTED_EXPR $i ^( REGION[$x] ID ( template )? ) )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INDENTED_EXPR, "INDENTED_EXPR"), root_1);
				adaptor.addChild(root_1, stream_i.nextNode());
				// STParser.g:108:25: ^( REGION[$x] ID ( template )? )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(REGION, x), root_2);
				adaptor.addChild(root_2, stream_ID.nextNode());
				// STParser.g:108:41: ( template )?
				if ( stream_template.hasNext() ) {
					adaptor.addChild(root_2, stream_template.nextTree());
				}
				stream_template.reset();

				adaptor.addChild(root_1, root_2);
				}

				adaptor.addChild(root_0, root_1);
				}

			}

			else // 109:3: -> ^( REGION[$x] ID ( template )? )
			{
				// STParser.g:109:25: ^( REGION[$x] ID ( template )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(REGION, x), root_1);
				adaptor.addChild(root_1, stream_ID.nextNode());
				// STParser.g:109:41: ( template )?
				if ( stream_template.hasNext() ) {
					adaptor.addChild(root_1, stream_template.nextTree());
				}
				stream_template.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "region"


	public static class subtemplate_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "subtemplate"
	// STParser.g:112:1: subtemplate : lc= '{' (ids+= ID ( ',' ids+= ID )* '|' )? template ( INDENT )? '}' -> ^( SUBTEMPLATE[$lc,\"SUBTEMPLATE\"] ( ^( ARGS $ids) )* ( template )? ) ;
	public final STParser.subtemplate_return subtemplate() throws RecognitionException {
		STParser.subtemplate_return retval = new STParser.subtemplate_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken lc=null;
		CommonToken char_literal31=null;
		CommonToken char_literal32=null;
		CommonToken INDENT34=null;
		CommonToken char_literal35=null;
		CommonToken ids=null;
		List<Object> list_ids=null;
		ParserRuleReturnScope template33 =null;

		CommonTree lc_tree=null;
		CommonTree char_literal31_tree=null;
		CommonTree char_literal32_tree=null;
		CommonTree INDENT34_tree=null;
		CommonTree char_literal35_tree=null;
		CommonTree ids_tree=null;
		RewriteRuleTokenStream stream_LCURLY=new RewriteRuleTokenStream(adaptor,"token LCURLY");
		RewriteRuleTokenStream stream_PIPE=new RewriteRuleTokenStream(adaptor,"token PIPE");
		RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_INDENT=new RewriteRuleTokenStream(adaptor,"token INDENT");
		RewriteRuleTokenStream stream_RCURLY=new RewriteRuleTokenStream(adaptor,"token RCURLY");
		RewriteRuleSubtreeStream stream_template=new RewriteRuleSubtreeStream(adaptor,"rule template");

		try {
			// STParser.g:113:2: (lc= '{' (ids+= ID ( ',' ids+= ID )* '|' )? template ( INDENT )? '}' -> ^( SUBTEMPLATE[$lc,\"SUBTEMPLATE\"] ( ^( ARGS $ids) )* ( template )? ) )
			// STParser.g:113:4: lc= '{' (ids+= ID ( ',' ids+= ID )* '|' )? template ( INDENT )? '}'
			{
			lc=(CommonToken)match(input,LCURLY,FOLLOW_LCURLY_in_subtemplate420);  
			stream_LCURLY.add(lc);

			// STParser.g:113:11: (ids+= ID ( ',' ids+= ID )* '|' )?
			int alt11=2;
			int LA11_0 = input.LA(1);
			if ( (LA11_0==ID) ) {
				alt11=1;
			}
			switch (alt11) {
				case 1 :
					// STParser.g:113:12: ids+= ID ( ',' ids+= ID )* '|'
					{
					ids=(CommonToken)match(input,ID,FOLLOW_ID_in_subtemplate426);  
					stream_ID.add(ids);

					if (list_ids==null) list_ids=new ArrayList<Object>();
					list_ids.add(ids);
					// STParser.g:113:21: ( ',' ids+= ID )*
					loop10:
					while (true) {
						int alt10=2;
						int LA10_0 = input.LA(1);
						if ( (LA10_0==COMMA) ) {
							alt10=1;
						}

						switch (alt10) {
						case 1 :
							// STParser.g:113:23: ',' ids+= ID
							{
							char_literal31=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_subtemplate430);  
							stream_COMMA.add(char_literal31);

							ids=(CommonToken)match(input,ID,FOLLOW_ID_in_subtemplate435);  
							stream_ID.add(ids);

							if (list_ids==null) list_ids=new ArrayList<Object>();
							list_ids.add(ids);
							}
							break;

						default :
							break loop10;
						}
					}

					char_literal32=(CommonToken)match(input,PIPE,FOLLOW_PIPE_in_subtemplate440);  
					stream_PIPE.add(char_literal32);

					}
					break;

			}

			pushFollow(FOLLOW_template_in_subtemplate445);
			template33=template();
			state._fsp--;

			stream_template.add(template33.getTree());
			// STParser.g:113:55: ( INDENT )?
			int alt12=2;
			int LA12_0 = input.LA(1);
			if ( (LA12_0==INDENT) ) {
				alt12=1;
			}
			switch (alt12) {
				case 1 :
					// STParser.g:113:55: INDENT
					{
					INDENT34=(CommonToken)match(input,INDENT,FOLLOW_INDENT_in_subtemplate447);  
					stream_INDENT.add(INDENT34);

					}
					break;

			}

			char_literal35=(CommonToken)match(input,RCURLY,FOLLOW_RCURLY_in_subtemplate450);  
			stream_RCURLY.add(char_literal35);

			// AST REWRITE
			// elements: ids, template
			// token labels: 
			// rule labels: retval
			// token list labels: ids
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleTokenStream stream_ids=new RewriteRuleTokenStream(adaptor,"token ids", list_ids);
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 115:3: -> ^( SUBTEMPLATE[$lc,\"SUBTEMPLATE\"] ( ^( ARGS $ids) )* ( template )? )
			{
				// STParser.g:115:6: ^( SUBTEMPLATE[$lc,\"SUBTEMPLATE\"] ( ^( ARGS $ids) )* ( template )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SUBTEMPLATE, lc, "SUBTEMPLATE"), root_1);
				// STParser.g:115:39: ( ^( ARGS $ids) )*
				while ( stream_ids.hasNext() ) {
					// STParser.g:115:39: ^( ARGS $ids)
					{
					CommonTree root_2 = (CommonTree)adaptor.nil();
					root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGS, "ARGS"), root_2);
					adaptor.addChild(root_2, stream_ids.nextNode());
					adaptor.addChild(root_1, root_2);
					}

				}
				stream_ids.reset();

				// STParser.g:115:53: ( template )?
				if ( stream_template.hasNext() ) {
					adaptor.addChild(root_1, stream_template.nextTree());
				}
				stream_template.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "subtemplate"


	public static class ifstat_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "ifstat"
	// STParser.g:118:1: ifstat : (i= INDENT )? LDELIM 'if' '(' c1= conditional ')' RDELIM t1= template ( ( INDENT )? LDELIM 'elseif' '(' c2+= conditional ')' RDELIM t2+= template )* ( ( INDENT )? LDELIM 'else' RDELIM t3= template )? ( INDENT )? endif= LDELIM 'endif' RDELIM ({...}? => NEWLINE )? -> {indent!=null}? ^( INDENTED_EXPR $i ^( 'if' $c1 ( $t1)? ( ^( 'elseif' $c2 $t2) )* ( ^( 'else' ( $t3)? ) )? ) ) -> ^( 'if' $c1 ( $t1)? ( ^( 'elseif' $c2 $t2) )* ( ^( 'else' ( $t3)? ) )? ) ;
	public final STParser.ifstat_return ifstat() throws RecognitionException {
		STParser.ifstat_return retval = new STParser.ifstat_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken i=null;
		CommonToken endif=null;
		CommonToken LDELIM36=null;
		CommonToken string_literal37=null;
		CommonToken char_literal38=null;
		CommonToken char_literal39=null;
		CommonToken RDELIM40=null;
		CommonToken INDENT41=null;
		CommonToken LDELIM42=null;
		CommonToken string_literal43=null;
		CommonToken char_literal44=null;
		CommonToken char_literal45=null;
		CommonToken RDELIM46=null;
		CommonToken INDENT47=null;
		CommonToken LDELIM48=null;
		CommonToken string_literal49=null;
		CommonToken RDELIM50=null;
		CommonToken INDENT51=null;
		CommonToken string_literal52=null;
		CommonToken RDELIM53=null;
		CommonToken NEWLINE54=null;
		List<Object> list_c2=null;
		List<Object> list_t2=null;
		ParserRuleReturnScope c1 =null;
		ParserRuleReturnScope t1 =null;
		ParserRuleReturnScope t3 =null;
		RuleReturnScope c2 = null;
		RuleReturnScope t2 = null;
		CommonTree i_tree=null;
		CommonTree endif_tree=null;
		CommonTree LDELIM36_tree=null;
		CommonTree string_literal37_tree=null;
		CommonTree char_literal38_tree=null;
		CommonTree char_literal39_tree=null;
		CommonTree RDELIM40_tree=null;
		CommonTree INDENT41_tree=null;
		CommonTree LDELIM42_tree=null;
		CommonTree string_literal43_tree=null;
		CommonTree char_literal44_tree=null;
		CommonTree char_literal45_tree=null;
		CommonTree RDELIM46_tree=null;
		CommonTree INDENT47_tree=null;
		CommonTree LDELIM48_tree=null;
		CommonTree string_literal49_tree=null;
		CommonTree RDELIM50_tree=null;
		CommonTree INDENT51_tree=null;
		CommonTree string_literal52_tree=null;
		CommonTree RDELIM53_tree=null;
		CommonTree NEWLINE54_tree=null;
		RewriteRuleTokenStream stream_ENDIF=new RewriteRuleTokenStream(adaptor,"token ENDIF");
		RewriteRuleTokenStream stream_RDELIM=new RewriteRuleTokenStream(adaptor,"token RDELIM");
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_NEWLINE=new RewriteRuleTokenStream(adaptor,"token NEWLINE");
		RewriteRuleTokenStream stream_LDELIM=new RewriteRuleTokenStream(adaptor,"token LDELIM");
		RewriteRuleTokenStream stream_INDENT=new RewriteRuleTokenStream(adaptor,"token INDENT");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
		RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
		RewriteRuleTokenStream stream_ELSEIF=new RewriteRuleTokenStream(adaptor,"token ELSEIF");
		RewriteRuleSubtreeStream stream_template=new RewriteRuleSubtreeStream(adaptor,"rule template");
		RewriteRuleSubtreeStream stream_conditional=new RewriteRuleSubtreeStream(adaptor,"rule conditional");

		Token indent=null;
		try {
			// STParser.g:120:2: ( (i= INDENT )? LDELIM 'if' '(' c1= conditional ')' RDELIM t1= template ( ( INDENT )? LDELIM 'elseif' '(' c2+= conditional ')' RDELIM t2+= template )* ( ( INDENT )? LDELIM 'else' RDELIM t3= template )? ( INDENT )? endif= LDELIM 'endif' RDELIM ({...}? => NEWLINE )? -> {indent!=null}? ^( INDENTED_EXPR $i ^( 'if' $c1 ( $t1)? ( ^( 'elseif' $c2 $t2) )* ( ^( 'else' ( $t3)? ) )? ) ) -> ^( 'if' $c1 ( $t1)? ( ^( 'elseif' $c2 $t2) )* ( ^( 'else' ( $t3)? ) )? ) )
			// STParser.g:120:4: (i= INDENT )? LDELIM 'if' '(' c1= conditional ')' RDELIM t1= template ( ( INDENT )? LDELIM 'elseif' '(' c2+= conditional ')' RDELIM t2+= template )* ( ( INDENT )? LDELIM 'else' RDELIM t3= template )? ( INDENT )? endif= LDELIM 'endif' RDELIM ({...}? => NEWLINE )?
			{
			// STParser.g:120:5: (i= INDENT )?
			int alt13=2;
			int LA13_0 = input.LA(1);
			if ( (LA13_0==INDENT) ) {
				alt13=1;
			}
			switch (alt13) {
				case 1 :
					// STParser.g:120:5: i= INDENT
					{
					i=(CommonToken)match(input,INDENT,FOLLOW_INDENT_in_ifstat491);  
					stream_INDENT.add(i);

					}
					break;

			}

			LDELIM36=(CommonToken)match(input,LDELIM,FOLLOW_LDELIM_in_ifstat494);  
			stream_LDELIM.add(LDELIM36);

			string_literal37=(CommonToken)match(input,IF,FOLLOW_IF_in_ifstat496);  
			stream_IF.add(string_literal37);

			char_literal38=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_ifstat498);  
			stream_LPAREN.add(char_literal38);

			pushFollow(FOLLOW_conditional_in_ifstat502);
			c1=conditional();
			state._fsp--;

			stream_conditional.add(c1.getTree());
			char_literal39=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_ifstat504);  
			stream_RPAREN.add(char_literal39);

			RDELIM40=(CommonToken)match(input,RDELIM,FOLLOW_RDELIM_in_ifstat506);  
			stream_RDELIM.add(RDELIM40);

			if (input.LA(1)!=NEWLINE) indent=i;
			pushFollow(FOLLOW_template_in_ifstat515);
			t1=template();
			state._fsp--;

			stream_template.add(t1.getTree());
			// STParser.g:122:4: ( ( INDENT )? LDELIM 'elseif' '(' c2+= conditional ')' RDELIM t2+= template )*
			loop15:
			while (true) {
				int alt15=2;
				int LA15_0 = input.LA(1);
				if ( (LA15_0==INDENT) ) {
					int LA15_1 = input.LA(2);
					if ( (LA15_1==LDELIM) ) {
						int LA15_2 = input.LA(3);
						if ( (LA15_2==ELSEIF) ) {
							alt15=1;
						}

					}

				}
				else if ( (LA15_0==LDELIM) ) {
					int LA15_2 = input.LA(2);
					if ( (LA15_2==ELSEIF) ) {
						alt15=1;
					}

				}

				switch (alt15) {
				case 1 :
					// STParser.g:122:6: ( INDENT )? LDELIM 'elseif' '(' c2+= conditional ')' RDELIM t2+= template
					{
					// STParser.g:122:6: ( INDENT )?
					int alt14=2;
					int LA14_0 = input.LA(1);
					if ( (LA14_0==INDENT) ) {
						alt14=1;
					}
					switch (alt14) {
						case 1 :
							// STParser.g:122:6: INDENT
							{
							INDENT41=(CommonToken)match(input,INDENT,FOLLOW_INDENT_in_ifstat522);  
							stream_INDENT.add(INDENT41);

							}
							break;

					}

					LDELIM42=(CommonToken)match(input,LDELIM,FOLLOW_LDELIM_in_ifstat525);  
					stream_LDELIM.add(LDELIM42);

					string_literal43=(CommonToken)match(input,ELSEIF,FOLLOW_ELSEIF_in_ifstat527);  
					stream_ELSEIF.add(string_literal43);

					char_literal44=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_ifstat529);  
					stream_LPAREN.add(char_literal44);

					pushFollow(FOLLOW_conditional_in_ifstat533);
					c2=conditional();
					state._fsp--;

					stream_conditional.add(c2.getTree());
					if (list_c2==null) list_c2=new ArrayList<Object>();
					list_c2.add(c2.getTree());
					char_literal45=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_ifstat535);  
					stream_RPAREN.add(char_literal45);

					RDELIM46=(CommonToken)match(input,RDELIM,FOLLOW_RDELIM_in_ifstat537);  
					stream_RDELIM.add(RDELIM46);

					pushFollow(FOLLOW_template_in_ifstat541);
					t2=template();
					state._fsp--;

					stream_template.add(t2.getTree());
					if (list_t2==null) list_t2=new ArrayList<Object>();
					list_t2.add(t2.getTree());
					}
					break;

				default :
					break loop15;
				}
			}

			// STParser.g:123:4: ( ( INDENT )? LDELIM 'else' RDELIM t3= template )?
			int alt17=2;
			int LA17_0 = input.LA(1);
			if ( (LA17_0==INDENT) ) {
				int LA17_1 = input.LA(2);
				if ( (LA17_1==LDELIM) ) {
					int LA17_2 = input.LA(3);
					if ( (LA17_2==ELSE) ) {
						alt17=1;
					}
				}
			}
			else if ( (LA17_0==LDELIM) ) {
				int LA17_2 = input.LA(2);
				if ( (LA17_2==ELSE) ) {
					alt17=1;
				}
			}
			switch (alt17) {
				case 1 :
					// STParser.g:123:6: ( INDENT )? LDELIM 'else' RDELIM t3= template
					{
					// STParser.g:123:6: ( INDENT )?
					int alt16=2;
					int LA16_0 = input.LA(1);
					if ( (LA16_0==INDENT) ) {
						alt16=1;
					}
					switch (alt16) {
						case 1 :
							// STParser.g:123:6: INDENT
							{
							INDENT47=(CommonToken)match(input,INDENT,FOLLOW_INDENT_in_ifstat551);  
							stream_INDENT.add(INDENT47);

							}
							break;

					}

					LDELIM48=(CommonToken)match(input,LDELIM,FOLLOW_LDELIM_in_ifstat554);  
					stream_LDELIM.add(LDELIM48);

					string_literal49=(CommonToken)match(input,ELSE,FOLLOW_ELSE_in_ifstat556);  
					stream_ELSE.add(string_literal49);

					RDELIM50=(CommonToken)match(input,RDELIM,FOLLOW_RDELIM_in_ifstat558);  
					stream_RDELIM.add(RDELIM50);

					pushFollow(FOLLOW_template_in_ifstat562);
					t3=template();
					state._fsp--;

					stream_template.add(t3.getTree());
					}
					break;

			}

			// STParser.g:124:4: ( INDENT )?
			int alt18=2;
			int LA18_0 = input.LA(1);
			if ( (LA18_0==INDENT) ) {
				alt18=1;
			}
			switch (alt18) {
				case 1 :
					// STParser.g:124:4: INDENT
					{
					INDENT51=(CommonToken)match(input,INDENT,FOLLOW_INDENT_in_ifstat570);  
					stream_INDENT.add(INDENT51);

					}
					break;

			}

			endif=(CommonToken)match(input,LDELIM,FOLLOW_LDELIM_in_ifstat576);  
			stream_LDELIM.add(endif);

			string_literal52=(CommonToken)match(input,ENDIF,FOLLOW_ENDIF_in_ifstat578);  
			stream_ENDIF.add(string_literal52);

			RDELIM53=(CommonToken)match(input,RDELIM,FOLLOW_RDELIM_in_ifstat582);  
			stream_RDELIM.add(RDELIM53);

			// STParser.g:127:3: ({...}? => NEWLINE )?
			int alt19=2;
			int LA19_0 = input.LA(1);
			if ( (LA19_0==NEWLINE) ) {
				int LA19_1 = input.LA(2);
				if ( ((((CommonToken)retval.start).getLine()!=input.LT(1).getLine())) ) {
					alt19=1;
				}
			}
			switch (alt19) {
				case 1 :
					// STParser.g:127:4: {...}? => NEWLINE
					{
					if ( !((((CommonToken)retval.start).getLine()!=input.LT(1).getLine())) ) {
						throw new FailedPredicateException(input, "ifstat", "$ifstat.start.getLine()!=input.LT(1).getLine()");
					}
					NEWLINE54=(CommonToken)match(input,NEWLINE,FOLLOW_NEWLINE_in_ifstat593);  
					stream_NEWLINE.add(NEWLINE54);

					}
					break;

			}

			// AST REWRITE
			// elements: IF, ELSEIF, i, t2, t1, c1, t2, ELSEIF, c2, t3, c1, ELSE, c2, ELSE, IF, t1, t3
			// token labels: i
			// rule labels: t3, retval, t1, c1
			// token list labels: 
			// rule list labels: t2, c2
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleTokenStream stream_i=new RewriteRuleTokenStream(adaptor,"token i",i);
			RewriteRuleSubtreeStream stream_t3=new RewriteRuleSubtreeStream(adaptor,"rule t3",t3!=null?t3.getTree():null);
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
			RewriteRuleSubtreeStream stream_t1=new RewriteRuleSubtreeStream(adaptor,"rule t1",t1!=null?t1.getTree():null);
			RewriteRuleSubtreeStream stream_c1=new RewriteRuleSubtreeStream(adaptor,"rule c1",c1!=null?c1.getTree():null);
			RewriteRuleSubtreeStream stream_t2=new RewriteRuleSubtreeStream(adaptor,"token t2",list_t2);
			RewriteRuleSubtreeStream stream_c2=new RewriteRuleSubtreeStream(adaptor,"token c2",list_c2);
			root_0 = (CommonTree)adaptor.nil();
			// 128:3: -> {indent!=null}? ^( INDENTED_EXPR $i ^( 'if' $c1 ( $t1)? ( ^( 'elseif' $c2 $t2) )* ( ^( 'else' ( $t3)? ) )? ) )
			if (indent!=null) {
				// STParser.g:129:6: ^( INDENTED_EXPR $i ^( 'if' $c1 ( $t1)? ( ^( 'elseif' $c2 $t2) )* ( ^( 'else' ( $t3)? ) )? ) )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INDENTED_EXPR, "INDENTED_EXPR"), root_1);
				adaptor.addChild(root_1, stream_i.nextNode());
				// STParser.g:129:25: ^( 'if' $c1 ( $t1)? ( ^( 'elseif' $c2 $t2) )* ( ^( 'else' ( $t3)? ) )? )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot(stream_IF.nextNode(), root_2);
				adaptor.addChild(root_2, stream_c1.nextTree());
				// STParser.g:129:37: ( $t1)?
				if ( stream_t1.hasNext() ) {
					adaptor.addChild(root_2, stream_t1.nextTree());
				}
				stream_t1.reset();

				// STParser.g:129:41: ( ^( 'elseif' $c2 $t2) )*
				while ( stream_t2.hasNext()||stream_c2.hasNext()||stream_ELSEIF.hasNext() ) {
					// STParser.g:129:41: ^( 'elseif' $c2 $t2)
					{
					CommonTree root_3 = (CommonTree)adaptor.nil();
					root_3 = (CommonTree)adaptor.becomeRoot(stream_ELSEIF.nextNode(), root_3);
					adaptor.addChild(root_3, stream_c2.nextTree());
					adaptor.addChild(root_3, stream_t2.nextTree());
					adaptor.addChild(root_2, root_3);
					}

				}
				stream_t2.reset();
				stream_c2.reset();
				stream_ELSEIF.reset();

				// STParser.g:129:62: ( ^( 'else' ( $t3)? ) )?
				if ( stream_ELSE.hasNext()||stream_t3.hasNext() ) {
					// STParser.g:129:62: ^( 'else' ( $t3)? )
					{
					CommonTree root_3 = (CommonTree)adaptor.nil();
					root_3 = (CommonTree)adaptor.becomeRoot(stream_ELSE.nextNode(), root_3);
					// STParser.g:129:72: ( $t3)?
					if ( stream_t3.hasNext() ) {
						adaptor.addChild(root_3, stream_t3.nextTree());
					}
					stream_t3.reset();

					adaptor.addChild(root_2, root_3);
					}

				}
				stream_ELSE.reset();
				stream_t3.reset();

				adaptor.addChild(root_1, root_2);
				}

				adaptor.addChild(root_0, root_1);
				}

			}

			else // 130:3: -> ^( 'if' $c1 ( $t1)? ( ^( 'elseif' $c2 $t2) )* ( ^( 'else' ( $t3)? ) )? )
			{
				// STParser.g:130:25: ^( 'if' $c1 ( $t1)? ( ^( 'elseif' $c2 $t2) )* ( ^( 'else' ( $t3)? ) )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot(stream_IF.nextNode(), root_1);
				adaptor.addChild(root_1, stream_c1.nextTree());
				// STParser.g:130:37: ( $t1)?
				if ( stream_t1.hasNext() ) {
					adaptor.addChild(root_1, stream_t1.nextTree());
				}
				stream_t1.reset();

				// STParser.g:130:41: ( ^( 'elseif' $c2 $t2) )*
				while ( stream_c2.hasNext()||stream_ELSEIF.hasNext()||stream_t2.hasNext() ) {
					// STParser.g:130:41: ^( 'elseif' $c2 $t2)
					{
					CommonTree root_2 = (CommonTree)adaptor.nil();
					root_2 = (CommonTree)adaptor.becomeRoot(stream_ELSEIF.nextNode(), root_2);
					adaptor.addChild(root_2, stream_c2.nextTree());
					adaptor.addChild(root_2, stream_t2.nextTree());
					adaptor.addChild(root_1, root_2);
					}

				}
				stream_c2.reset();
				stream_ELSEIF.reset();
				stream_t2.reset();

				// STParser.g:130:62: ( ^( 'else' ( $t3)? ) )?
				if ( stream_ELSE.hasNext()||stream_t3.hasNext() ) {
					// STParser.g:130:62: ^( 'else' ( $t3)? )
					{
					CommonTree root_2 = (CommonTree)adaptor.nil();
					root_2 = (CommonTree)adaptor.becomeRoot(stream_ELSE.nextNode(), root_2);
					// STParser.g:130:72: ( $t3)?
					if ( stream_t3.hasNext() ) {
						adaptor.addChild(root_2, stream_t3.nextTree());
					}
					stream_t3.reset();

					adaptor.addChild(root_1, root_2);
					}

				}
				stream_ELSE.reset();
				stream_t3.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ifstat"


	protected static class conditional_scope {
		boolean inside;
	}
	protected Stack<conditional_scope> conditional_stack = new Stack<conditional_scope>();

	public static class conditional_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "conditional"
	// STParser.g:133:1: conditional : andConditional ( '||' ^ andConditional )* ;
	public final STParser.conditional_return conditional() throws RecognitionException {
		conditional_stack.push(new conditional_scope());
		STParser.conditional_return retval = new STParser.conditional_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken string_literal56=null;
		ParserRuleReturnScope andConditional55 =null;
		ParserRuleReturnScope andConditional57 =null;

		CommonTree string_literal56_tree=null;

		try {
			// STParser.g:137:2: ( andConditional ( '||' ^ andConditional )* )
			// STParser.g:137:4: andConditional ( '||' ^ andConditional )*
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_andConditional_in_conditional713);
			andConditional55=andConditional();
			state._fsp--;

			adaptor.addChild(root_0, andConditional55.getTree());

			// STParser.g:137:19: ( '||' ^ andConditional )*
			loop20:
			while (true) {
				int alt20=2;
				int LA20_0 = input.LA(1);
				if ( (LA20_0==OR) ) {
					alt20=1;
				}

				switch (alt20) {
				case 1 :
					// STParser.g:137:21: '||' ^ andConditional
					{
					string_literal56=(CommonToken)match(input,OR,FOLLOW_OR_in_conditional717); 
					string_literal56_tree = (CommonTree)adaptor.create(string_literal56);
					root_0 = (CommonTree)adaptor.becomeRoot(string_literal56_tree, root_0);

					pushFollow(FOLLOW_andConditional_in_conditional720);
					andConditional57=andConditional();
					state._fsp--;

					adaptor.addChild(root_0, andConditional57.getTree());

					}
					break;

				default :
					break loop20;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
			conditional_stack.pop();
		}
		return retval;
	}
	// $ANTLR end "conditional"


	public static class andConditional_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "andConditional"
	// STParser.g:140:1: andConditional : notConditional ( '&&' ^ notConditional )* ;
	public final STParser.andConditional_return andConditional() throws RecognitionException {
		STParser.andConditional_return retval = new STParser.andConditional_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken string_literal59=null;
		ParserRuleReturnScope notConditional58 =null;
		ParserRuleReturnScope notConditional60 =null;

		CommonTree string_literal59_tree=null;

		try {
			// STParser.g:140:16: ( notConditional ( '&&' ^ notConditional )* )
			// STParser.g:140:18: notConditional ( '&&' ^ notConditional )*
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_notConditional_in_andConditional733);
			notConditional58=notConditional();
			state._fsp--;

			adaptor.addChild(root_0, notConditional58.getTree());

			// STParser.g:140:33: ( '&&' ^ notConditional )*
			loop21:
			while (true) {
				int alt21=2;
				int LA21_0 = input.LA(1);
				if ( (LA21_0==AND) ) {
					alt21=1;
				}

				switch (alt21) {
				case 1 :
					// STParser.g:140:35: '&&' ^ notConditional
					{
					string_literal59=(CommonToken)match(input,AND,FOLLOW_AND_in_andConditional737); 
					string_literal59_tree = (CommonTree)adaptor.create(string_literal59);
					root_0 = (CommonTree)adaptor.becomeRoot(string_literal59_tree, root_0);

					pushFollow(FOLLOW_notConditional_in_andConditional740);
					notConditional60=notConditional();
					state._fsp--;

					adaptor.addChild(root_0, notConditional60.getTree());

					}
					break;

				default :
					break loop21;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "andConditional"


	public static class notConditional_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "notConditional"
	// STParser.g:142:1: notConditional : ( '!' ^ notConditional | memberExpr );
	public final STParser.notConditional_return notConditional() throws RecognitionException {
		STParser.notConditional_return retval = new STParser.notConditional_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken char_literal61=null;
		ParserRuleReturnScope notConditional62 =null;
		ParserRuleReturnScope memberExpr63 =null;

		CommonTree char_literal61_tree=null;

		try {
			// STParser.g:143:2: ( '!' ^ notConditional | memberExpr )
			int alt22=2;
			int LA22_0 = input.LA(1);
			if ( (LA22_0==BANG) ) {
				alt22=1;
			}
			else if ( (LA22_0==SUPER||LA22_0==LBRACK||LA22_0==LCURLY||(LA22_0 >= ID && LA22_0 <= STRING)||LA22_0==AT||(LA22_0 >= TRUE && LA22_0 <= FALSE)) ) {
				alt22=2;
			}
			else if ( (LA22_0==LPAREN) && (((conditional_stack.size()==0)||(conditional_stack.size()>0)))) {
				alt22=2;
			}

			switch (alt22) {
				case 1 :
					// STParser.g:143:4: '!' ^ notConditional
					{
					root_0 = (CommonTree)adaptor.nil();


					char_literal61=(CommonToken)match(input,BANG,FOLLOW_BANG_in_notConditional753); 
					char_literal61_tree = (CommonTree)adaptor.create(char_literal61);
					root_0 = (CommonTree)adaptor.becomeRoot(char_literal61_tree, root_0);

					pushFollow(FOLLOW_notConditional_in_notConditional756);
					notConditional62=notConditional();
					state._fsp--;

					adaptor.addChild(root_0, notConditional62.getTree());

					}
					break;
				case 2 :
					// STParser.g:144:4: memberExpr
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_memberExpr_in_notConditional761);
					memberExpr63=memberExpr();
					state._fsp--;

					adaptor.addChild(root_0, memberExpr63.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "notConditional"


	public static class notConditionalExpr_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "notConditionalExpr"
	// STParser.g:147:1: notConditionalExpr : ( ID -> ID ) (p= '.' prop= ID -> ^( PROP[$p,\"PROP\"] $notConditionalExpr $prop) |p= '.' '(' mapExpr ')' -> ^( PROP_IND[$p,\"PROP_IND\"] $notConditionalExpr mapExpr ) )* ;
	public final STParser.notConditionalExpr_return notConditionalExpr() throws RecognitionException {
		STParser.notConditionalExpr_return retval = new STParser.notConditionalExpr_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken p=null;
		CommonToken prop=null;
		CommonToken ID64=null;
		CommonToken char_literal65=null;
		CommonToken char_literal67=null;
		ParserRuleReturnScope mapExpr66 =null;

		CommonTree p_tree=null;
		CommonTree prop_tree=null;
		CommonTree ID64_tree=null;
		CommonTree char_literal65_tree=null;
		CommonTree char_literal67_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_mapExpr=new RewriteRuleSubtreeStream(adaptor,"rule mapExpr");

		try {
			// STParser.g:148:2: ( ( ID -> ID ) (p= '.' prop= ID -> ^( PROP[$p,\"PROP\"] $notConditionalExpr $prop) |p= '.' '(' mapExpr ')' -> ^( PROP_IND[$p,\"PROP_IND\"] $notConditionalExpr mapExpr ) )* )
			// STParser.g:148:4: ( ID -> ID ) (p= '.' prop= ID -> ^( PROP[$p,\"PROP\"] $notConditionalExpr $prop) |p= '.' '(' mapExpr ')' -> ^( PROP_IND[$p,\"PROP_IND\"] $notConditionalExpr mapExpr ) )*
			{
			// STParser.g:148:4: ( ID -> ID )
			// STParser.g:148:5: ID
			{
			ID64=(CommonToken)match(input,ID,FOLLOW_ID_in_notConditionalExpr773);  
			stream_ID.add(ID64);

			// AST REWRITE
			// elements: ID
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 148:7: -> ID
			{
				adaptor.addChild(root_0, stream_ID.nextNode());
			}


			retval.tree = root_0;

			}

			// STParser.g:149:3: (p= '.' prop= ID -> ^( PROP[$p,\"PROP\"] $notConditionalExpr $prop) |p= '.' '(' mapExpr ')' -> ^( PROP_IND[$p,\"PROP_IND\"] $notConditionalExpr mapExpr ) )*
			loop23:
			while (true) {
				int alt23=3;
				int LA23_0 = input.LA(1);
				if ( (LA23_0==DOT) ) {
					int LA23_2 = input.LA(2);
					if ( (LA23_2==ID) ) {
						alt23=1;
					}
					else if ( (LA23_2==LPAREN) ) {
						alt23=2;
					}

				}

				switch (alt23) {
				case 1 :
					// STParser.g:149:5: p= '.' prop= ID
					{
					p=(CommonToken)match(input,DOT,FOLLOW_DOT_in_notConditionalExpr784);  
					stream_DOT.add(p);

					prop=(CommonToken)match(input,ID,FOLLOW_ID_in_notConditionalExpr788);  
					stream_ID.add(prop);

					// AST REWRITE
					// elements: prop, notConditionalExpr
					// token labels: prop
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleTokenStream stream_prop=new RewriteRuleTokenStream(adaptor,"token prop",prop);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 149:24: -> ^( PROP[$p,\"PROP\"] $notConditionalExpr $prop)
					{
						// STParser.g:149:27: ^( PROP[$p,\"PROP\"] $notConditionalExpr $prop)
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROP, p, "PROP"), root_1);
						adaptor.addChild(root_1, stream_retval.nextTree());
						adaptor.addChild(root_1, stream_prop.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// STParser.g:150:5: p= '.' '(' mapExpr ')'
					{
					p=(CommonToken)match(input,DOT,FOLLOW_DOT_in_notConditionalExpr814);  
					stream_DOT.add(p);

					char_literal65=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_notConditionalExpr816);  
					stream_LPAREN.add(char_literal65);

					pushFollow(FOLLOW_mapExpr_in_notConditionalExpr818);
					mapExpr66=mapExpr();
					state._fsp--;

					stream_mapExpr.add(mapExpr66.getTree());
					char_literal67=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_notConditionalExpr820);  
					stream_RPAREN.add(char_literal67);

					// AST REWRITE
					// elements: mapExpr, notConditionalExpr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 150:30: -> ^( PROP_IND[$p,\"PROP_IND\"] $notConditionalExpr mapExpr )
					{
						// STParser.g:150:33: ^( PROP_IND[$p,\"PROP_IND\"] $notConditionalExpr mapExpr )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROP_IND, p, "PROP_IND"), root_1);
						adaptor.addChild(root_1, stream_retval.nextTree());
						adaptor.addChild(root_1, stream_mapExpr.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

				default :
					break loop23;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "notConditionalExpr"


	public static class exprOptions_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "exprOptions"
	// STParser.g:154:1: exprOptions : option ( ',' option )* -> ^( OPTIONS ( option )* ) ;
	public final STParser.exprOptions_return exprOptions() throws RecognitionException {
		STParser.exprOptions_return retval = new STParser.exprOptions_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken char_literal69=null;
		ParserRuleReturnScope option68 =null;
		ParserRuleReturnScope option70 =null;

		CommonTree char_literal69_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleSubtreeStream stream_option=new RewriteRuleSubtreeStream(adaptor,"rule option");

		try {
			// STParser.g:154:13: ( option ( ',' option )* -> ^( OPTIONS ( option )* ) )
			// STParser.g:154:15: option ( ',' option )*
			{
			pushFollow(FOLLOW_option_in_exprOptions850);
			option68=option();
			state._fsp--;

			stream_option.add(option68.getTree());
			// STParser.g:154:22: ( ',' option )*
			loop24:
			while (true) {
				int alt24=2;
				int LA24_0 = input.LA(1);
				if ( (LA24_0==COMMA) ) {
					alt24=1;
				}

				switch (alt24) {
				case 1 :
					// STParser.g:154:24: ',' option
					{
					char_literal69=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_exprOptions854);  
					stream_COMMA.add(char_literal69);

					pushFollow(FOLLOW_option_in_exprOptions856);
					option70=option();
					state._fsp--;

					stream_option.add(option70.getTree());
					}
					break;

				default :
					break loop24;
				}
			}

			// AST REWRITE
			// elements: option
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 154:38: -> ^( OPTIONS ( option )* )
			{
				// STParser.g:154:41: ^( OPTIONS ( option )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(OPTIONS, "OPTIONS"), root_1);
				// STParser.g:154:51: ( option )*
				while ( stream_option.hasNext() ) {
					adaptor.addChild(root_1, stream_option.nextTree());
				}
				stream_option.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "exprOptions"


	public static class option_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "option"
	// STParser.g:156:1: option : ID ( '=' exprNoComma -> {validOption}? ^( '=' ID exprNoComma ) ->| -> {validOption&&defVal!=null}? ^( EQUALS[\"=\"] ID STRING[$ID,'\"'+defVal+'\"'] ) ->) ;
	public final STParser.option_return option() throws RecognitionException {
		STParser.option_return retval = new STParser.option_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken ID71=null;
		CommonToken char_literal72=null;
		ParserRuleReturnScope exprNoComma73 =null;

		CommonTree ID71_tree=null;
		CommonTree char_literal72_tree=null;
		RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
		RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
		RewriteRuleSubtreeStream stream_exprNoComma=new RewriteRuleSubtreeStream(adaptor,"rule exprNoComma");


			String id = input.LT(1).getText();
			String defVal = Compiler.defaultOptionValues.get(id);
			boolean validOption = Compiler.supportedOptions.get(id)!=null;

		try {
			// STParser.g:162:2: ( ID ( '=' exprNoComma -> {validOption}? ^( '=' ID exprNoComma ) ->| -> {validOption&&defVal!=null}? ^( EQUALS[\"=\"] ID STRING[$ID,'\"'+defVal+'\"'] ) ->) )
			// STParser.g:162:4: ID ( '=' exprNoComma -> {validOption}? ^( '=' ID exprNoComma ) ->| -> {validOption&&defVal!=null}? ^( EQUALS[\"=\"] ID STRING[$ID,'\"'+defVal+'\"'] ) ->)
			{
			ID71=(CommonToken)match(input,ID,FOLLOW_ID_in_option883);  
			stream_ID.add(ID71);


					if ( !validOption ) {
			            errMgr.compileTimeError(ErrorType.NO_SUCH_OPTION, templateToken, ID71, (ID71!=null?ID71.getText():null));
					}
					
			// STParser.g:168:3: ( '=' exprNoComma -> {validOption}? ^( '=' ID exprNoComma ) ->| -> {validOption&&defVal!=null}? ^( EQUALS[\"=\"] ID STRING[$ID,'\"'+defVal+'\"'] ) ->)
			int alt25=2;
			int LA25_0 = input.LA(1);
			if ( (LA25_0==EQUALS) ) {
				alt25=1;
			}
			else if ( (LA25_0==COMMA||LA25_0==RDELIM) ) {
				alt25=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 25, 0, input);
				throw nvae;
			}

			switch (alt25) {
				case 1 :
					// STParser.g:168:5: '=' exprNoComma
					{
					char_literal72=(CommonToken)match(input,EQUALS,FOLLOW_EQUALS_in_option893);  
					stream_EQUALS.add(char_literal72);

					pushFollow(FOLLOW_exprNoComma_in_option895);
					exprNoComma73=exprNoComma();
					state._fsp--;

					stream_exprNoComma.add(exprNoComma73.getTree());
					// AST REWRITE
					// elements: exprNoComma, EQUALS, ID
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 168:26: -> {validOption}? ^( '=' ID exprNoComma )
					if (validOption) {
						// STParser.g:168:44: ^( '=' ID exprNoComma )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot(stream_EQUALS.nextNode(), root_1);
						adaptor.addChild(root_1, stream_ID.nextNode());
						adaptor.addChild(root_1, stream_exprNoComma.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}

					else // 169:13: ->
					{
						root_0 = null;
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// STParser.g:170:5: 
					{

								if ( defVal==null ) {
									errMgr.compileTimeError(ErrorType.NO_DEFAULT_VALUE, templateToken, ID71);
								}
								
					// AST REWRITE
					// elements: ID
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 175:13: -> {validOption&&defVal!=null}? ^( EQUALS[\"=\"] ID STRING[$ID,'\"'+defVal+'\"'] )
					if (validOption&&defVal!=null) {
						// STParser.g:176:16: ^( EQUALS[\"=\"] ID STRING[$ID,'\"'+defVal+'\"'] )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EQUALS, "="), root_1);
						adaptor.addChild(root_1, stream_ID.nextNode());
						adaptor.addChild(root_1, (CommonTree)adaptor.create(STRING, ID71, '"'+defVal+'"'));
						adaptor.addChild(root_0, root_1);
						}

					}

					else // 177:13: ->
					{
						root_0 = null;
					}


					retval.tree = root_0;

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "option"


	public static class exprNoComma_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "exprNoComma"
	// STParser.g:181:1: exprNoComma : memberExpr ( ':' mapTemplateRef -> ^( MAP memberExpr mapTemplateRef ) | -> memberExpr ) ;
	public final STParser.exprNoComma_return exprNoComma() throws RecognitionException {
		STParser.exprNoComma_return retval = new STParser.exprNoComma_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken char_literal75=null;
		ParserRuleReturnScope memberExpr74 =null;
		ParserRuleReturnScope mapTemplateRef76 =null;

		CommonTree char_literal75_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_memberExpr=new RewriteRuleSubtreeStream(adaptor,"rule memberExpr");
		RewriteRuleSubtreeStream stream_mapTemplateRef=new RewriteRuleSubtreeStream(adaptor,"rule mapTemplateRef");

		try {
			// STParser.g:182:2: ( memberExpr ( ':' mapTemplateRef -> ^( MAP memberExpr mapTemplateRef ) | -> memberExpr ) )
			// STParser.g:182:4: memberExpr ( ':' mapTemplateRef -> ^( MAP memberExpr mapTemplateRef ) | -> memberExpr )
			{
			pushFollow(FOLLOW_memberExpr_in_exprNoComma1002);
			memberExpr74=memberExpr();
			state._fsp--;

			stream_memberExpr.add(memberExpr74.getTree());
			// STParser.g:183:3: ( ':' mapTemplateRef -> ^( MAP memberExpr mapTemplateRef ) | -> memberExpr )
			int alt26=2;
			int LA26_0 = input.LA(1);
			if ( (LA26_0==COLON) ) {
				alt26=1;
			}
			else if ( (LA26_0==RPAREN||(LA26_0 >= RBRACK && LA26_0 <= COMMA)||LA26_0==RDELIM) ) {
				alt26=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 26, 0, input);
				throw nvae;
			}

			switch (alt26) {
				case 1 :
					// STParser.g:183:5: ':' mapTemplateRef
					{
					char_literal75=(CommonToken)match(input,COLON,FOLLOW_COLON_in_exprNoComma1008);  
					stream_COLON.add(char_literal75);

					pushFollow(FOLLOW_mapTemplateRef_in_exprNoComma1010);
					mapTemplateRef76=mapTemplateRef();
					state._fsp--;

					stream_mapTemplateRef.add(mapTemplateRef76.getTree());
					// AST REWRITE
					// elements: mapTemplateRef, memberExpr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 183:28: -> ^( MAP memberExpr mapTemplateRef )
					{
						// STParser.g:183:31: ^( MAP memberExpr mapTemplateRef )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MAP, "MAP"), root_1);
						adaptor.addChild(root_1, stream_memberExpr.nextTree());
						adaptor.addChild(root_1, stream_mapTemplateRef.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// STParser.g:184:14: 
					{
					// AST REWRITE
					// elements: memberExpr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 184:14: -> memberExpr
					{
						adaptor.addChild(root_0, stream_memberExpr.nextTree());
					}


					retval.tree = root_0;

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "exprNoComma"


	public static class expr_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "expr"
	// STParser.g:188:1: expr : mapExpr ;
	public final STParser.expr_return expr() throws RecognitionException {
		STParser.expr_return retval = new STParser.expr_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope mapExpr77 =null;


		try {
			// STParser.g:188:6: ( mapExpr )
			// STParser.g:188:8: mapExpr
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_mapExpr_in_expr1055);
			mapExpr77=mapExpr();
			state._fsp--;

			adaptor.addChild(root_0, mapExpr77.getTree());

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "expr"


	public static class mapExpr_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "mapExpr"
	// STParser.g:192:1: mapExpr : memberExpr ( (c= ',' memberExpr )+ col= ':' mapTemplateRef -> ^( ZIP[$col] ^( ELEMENTS ( memberExpr )+ ) mapTemplateRef ) | -> memberExpr ) (col= ':' x+= mapTemplateRef ({...}? => ',' x+= mapTemplateRef )* -> ^( MAP[$col] $mapExpr ( $x)+ ) )* ;
	public final STParser.mapExpr_return mapExpr() throws RecognitionException {
		STParser.mapExpr_return retval = new STParser.mapExpr_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken c=null;
		CommonToken col=null;
		CommonToken char_literal81=null;
		List<Object> list_x=null;
		ParserRuleReturnScope memberExpr78 =null;
		ParserRuleReturnScope memberExpr79 =null;
		ParserRuleReturnScope mapTemplateRef80 =null;
		RuleReturnScope x = null;
		CommonTree c_tree=null;
		CommonTree col_tree=null;
		CommonTree char_literal81_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleSubtreeStream stream_memberExpr=new RewriteRuleSubtreeStream(adaptor,"rule memberExpr");
		RewriteRuleSubtreeStream stream_mapTemplateRef=new RewriteRuleSubtreeStream(adaptor,"rule mapTemplateRef");

		try {
			// STParser.g:193:2: ( memberExpr ( (c= ',' memberExpr )+ col= ':' mapTemplateRef -> ^( ZIP[$col] ^( ELEMENTS ( memberExpr )+ ) mapTemplateRef ) | -> memberExpr ) (col= ':' x+= mapTemplateRef ({...}? => ',' x+= mapTemplateRef )* -> ^( MAP[$col] $mapExpr ( $x)+ ) )* )
			// STParser.g:193:4: memberExpr ( (c= ',' memberExpr )+ col= ':' mapTemplateRef -> ^( ZIP[$col] ^( ELEMENTS ( memberExpr )+ ) mapTemplateRef ) | -> memberExpr ) (col= ':' x+= mapTemplateRef ({...}? => ',' x+= mapTemplateRef )* -> ^( MAP[$col] $mapExpr ( $x)+ ) )*
			{
			pushFollow(FOLLOW_memberExpr_in_mapExpr1067);
			memberExpr78=memberExpr();
			state._fsp--;

			stream_memberExpr.add(memberExpr78.getTree());
			// STParser.g:194:3: ( (c= ',' memberExpr )+ col= ':' mapTemplateRef -> ^( ZIP[$col] ^( ELEMENTS ( memberExpr )+ ) mapTemplateRef ) | -> memberExpr )
			int alt28=2;
			int LA28_0 = input.LA(1);
			if ( (LA28_0==COMMA) ) {
				alt28=1;
			}
			else if ( (LA28_0==SEMI||LA28_0==COLON||LA28_0==RPAREN||LA28_0==RDELIM) ) {
				alt28=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 28, 0, input);
				throw nvae;
			}

			switch (alt28) {
				case 1 :
					// STParser.g:194:5: (c= ',' memberExpr )+ col= ':' mapTemplateRef
					{
					// STParser.g:194:5: (c= ',' memberExpr )+
					int cnt27=0;
					loop27:
					while (true) {
						int alt27=2;
						int LA27_0 = input.LA(1);
						if ( (LA27_0==COMMA) ) {
							alt27=1;
						}

						switch (alt27) {
						case 1 :
							// STParser.g:194:6: c= ',' memberExpr
							{
							c=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_mapExpr1076);  
							stream_COMMA.add(c);

							pushFollow(FOLLOW_memberExpr_in_mapExpr1078);
							memberExpr79=memberExpr();
							state._fsp--;

							stream_memberExpr.add(memberExpr79.getTree());
							}
							break;

						default :
							if ( cnt27 >= 1 ) break loop27;
							EarlyExitException eee = new EarlyExitException(27, input);
							throw eee;
						}
						cnt27++;
					}

					col=(CommonToken)match(input,COLON,FOLLOW_COLON_in_mapExpr1084);  
					stream_COLON.add(col);

					pushFollow(FOLLOW_mapTemplateRef_in_mapExpr1086);
					mapTemplateRef80=mapTemplateRef();
					state._fsp--;

					stream_mapTemplateRef.add(mapTemplateRef80.getTree());
					// AST REWRITE
					// elements: mapTemplateRef, memberExpr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 195:13: -> ^( ZIP[$col] ^( ELEMENTS ( memberExpr )+ ) mapTemplateRef )
					{
						// STParser.g:195:16: ^( ZIP[$col] ^( ELEMENTS ( memberExpr )+ ) mapTemplateRef )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ZIP, col), root_1);
						// STParser.g:195:28: ^( ELEMENTS ( memberExpr )+ )
						{
						CommonTree root_2 = (CommonTree)adaptor.nil();
						root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ELEMENTS, "ELEMENTS"), root_2);
						if ( !(stream_memberExpr.hasNext()) ) {
							throw new RewriteEarlyExitException();
						}
						while ( stream_memberExpr.hasNext() ) {
							adaptor.addChild(root_2, stream_memberExpr.nextTree());
						}
						stream_memberExpr.reset();

						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_1, stream_mapTemplateRef.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// STParser.g:196:14: 
					{
					// AST REWRITE
					// elements: memberExpr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 196:14: -> memberExpr
					{
						adaptor.addChild(root_0, stream_memberExpr.nextTree());
					}


					retval.tree = root_0;

					}
					break;

			}

			// STParser.g:198:3: (col= ':' x+= mapTemplateRef ({...}? => ',' x+= mapTemplateRef )* -> ^( MAP[$col] $mapExpr ( $x)+ ) )*
			loop30:
			while (true) {
				int alt30=2;
				int LA30_0 = input.LA(1);
				if ( (LA30_0==COLON) ) {
					alt30=1;
				}

				switch (alt30) {
				case 1 :
					// STParser.g:198:5: col= ':' x+= mapTemplateRef ({...}? => ',' x+= mapTemplateRef )*
					{
					if (list_x!=null) list_x.clear();
					col=(CommonToken)match(input,COLON,FOLLOW_COLON_in_mapExpr1149);  
					stream_COLON.add(col);

					pushFollow(FOLLOW_mapTemplateRef_in_mapExpr1153);
					x=mapTemplateRef();
					state._fsp--;

					stream_mapTemplateRef.add(x.getTree());
					if (list_x==null) list_x=new ArrayList<Object>();
					list_x.add(x.getTree());
					// STParser.g:199:30: ({...}? => ',' x+= mapTemplateRef )*
					loop29:
					while (true) {
						int alt29=2;
						int LA29_0 = input.LA(1);
						if ( (LA29_0==COMMA) && ((c==null))) {
							alt29=1;
						}

						switch (alt29) {
						case 1 :
							// STParser.g:199:31: {...}? => ',' x+= mapTemplateRef
							{
							if ( !((c==null)) ) {
								throw new FailedPredicateException(input, "mapExpr", "$c==null");
							}
							char_literal81=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_mapExpr1159);  
							stream_COMMA.add(char_literal81);

							pushFollow(FOLLOW_mapTemplateRef_in_mapExpr1163);
							x=mapTemplateRef();
							state._fsp--;

							stream_mapTemplateRef.add(x.getTree());
							if (list_x==null) list_x=new ArrayList<Object>();
							list_x.add(x.getTree());
							}
							break;

						default :
							break loop29;
						}
					}

					// AST REWRITE
					// elements: mapExpr, x
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: x
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
					RewriteRuleSubtreeStream stream_x=new RewriteRuleSubtreeStream(adaptor,"token x",list_x);
					root_0 = (CommonTree)adaptor.nil();
					// 200:13: -> ^( MAP[$col] $mapExpr ( $x)+ )
					{
						// STParser.g:200:16: ^( MAP[$col] $mapExpr ( $x)+ )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MAP, col), root_1);
						adaptor.addChild(root_1, stream_retval.nextTree());
						if ( !(stream_x.hasNext()) ) {
							throw new RewriteEarlyExitException();
						}
						while ( stream_x.hasNext() ) {
							adaptor.addChild(root_1, stream_x.nextTree());
						}
						stream_x.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

				default :
					break loop30;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "mapExpr"


	public static class mapTemplateRef_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "mapTemplateRef"
	// STParser.g:209:1: mapTemplateRef : ( ID '(' args ')' -> ^( INCLUDE ID ( args )? ) | subtemplate |lp= '(' mapExpr rp= ')' '(' ( argExprList )? ')' -> ^( INCLUDE_IND mapExpr ( argExprList )? ) );
	public final STParser.mapTemplateRef_return mapTemplateRef() throws RecognitionException {
		STParser.mapTemplateRef_return retval = new STParser.mapTemplateRef_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken lp=null;
		CommonToken rp=null;
		CommonToken ID82=null;
		CommonToken char_literal83=null;
		CommonToken char_literal85=null;
		CommonToken char_literal88=null;
		CommonToken char_literal90=null;
		ParserRuleReturnScope args84 =null;
		ParserRuleReturnScope subtemplate86 =null;
		ParserRuleReturnScope mapExpr87 =null;
		ParserRuleReturnScope argExprList89 =null;

		CommonTree lp_tree=null;
		CommonTree rp_tree=null;
		CommonTree ID82_tree=null;
		CommonTree char_literal83_tree=null;
		CommonTree char_literal85_tree=null;
		CommonTree char_literal88_tree=null;
		CommonTree char_literal90_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_argExprList=new RewriteRuleSubtreeStream(adaptor,"rule argExprList");
		RewriteRuleSubtreeStream stream_args=new RewriteRuleSubtreeStream(adaptor,"rule args");
		RewriteRuleSubtreeStream stream_mapExpr=new RewriteRuleSubtreeStream(adaptor,"rule mapExpr");

		try {
			// STParser.g:210:2: ( ID '(' args ')' -> ^( INCLUDE ID ( args )? ) | subtemplate |lp= '(' mapExpr rp= ')' '(' ( argExprList )? ')' -> ^( INCLUDE_IND mapExpr ( argExprList )? ) )
			int alt32=3;
			switch ( input.LA(1) ) {
			case ID:
				{
				alt32=1;
				}
				break;
			case LCURLY:
				{
				alt32=2;
				}
				break;
			case LPAREN:
				{
				alt32=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 32, 0, input);
				throw nvae;
			}
			switch (alt32) {
				case 1 :
					// STParser.g:210:4: ID '(' args ')'
					{
					ID82=(CommonToken)match(input,ID,FOLLOW_ID_in_mapTemplateRef1210);  
					stream_ID.add(ID82);

					char_literal83=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_mapTemplateRef1212);  
					stream_LPAREN.add(char_literal83);

					pushFollow(FOLLOW_args_in_mapTemplateRef1214);
					args84=args();
					state._fsp--;

					stream_args.add(args84.getTree());
					char_literal85=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_mapTemplateRef1216);  
					stream_RPAREN.add(char_literal85);

					// AST REWRITE
					// elements: ID, args
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 210:26: -> ^( INCLUDE ID ( args )? )
					{
						// STParser.g:210:29: ^( INCLUDE ID ( args )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INCLUDE, "INCLUDE"), root_1);
						adaptor.addChild(root_1, stream_ID.nextNode());
						// STParser.g:210:42: ( args )?
						if ( stream_args.hasNext() ) {
							adaptor.addChild(root_1, stream_args.nextTree());
						}
						stream_args.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// STParser.g:211:4: subtemplate
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_subtemplate_in_mapTemplateRef1238);
					subtemplate86=subtemplate();
					state._fsp--;

					adaptor.addChild(root_0, subtemplate86.getTree());

					}
					break;
				case 3 :
					// STParser.g:212:4: lp= '(' mapExpr rp= ')' '(' ( argExprList )? ')'
					{
					lp=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_mapTemplateRef1245);  
					stream_LPAREN.add(lp);

					pushFollow(FOLLOW_mapExpr_in_mapTemplateRef1247);
					mapExpr87=mapExpr();
					state._fsp--;

					stream_mapExpr.add(mapExpr87.getTree());
					rp=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_mapTemplateRef1251);  
					stream_RPAREN.add(rp);

					char_literal88=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_mapTemplateRef1253);  
					stream_LPAREN.add(char_literal88);

					// STParser.g:212:30: ( argExprList )?
					int alt31=2;
					int LA31_0 = input.LA(1);
					if ( (LA31_0==SUPER||LA31_0==LBRACK||LA31_0==LCURLY||(LA31_0 >= ID && LA31_0 <= STRING)||LA31_0==AT||(LA31_0 >= TRUE && LA31_0 <= FALSE)) ) {
						alt31=1;
					}
					else if ( (LA31_0==LPAREN) && (((conditional_stack.size()==0)||(conditional_stack.size()>0)))) {
						alt31=1;
					}
					switch (alt31) {
						case 1 :
							// STParser.g:212:30: argExprList
							{
							pushFollow(FOLLOW_argExprList_in_mapTemplateRef1255);
							argExprList89=argExprList();
							state._fsp--;

							stream_argExprList.add(argExprList89.getTree());
							}
							break;

					}

					char_literal90=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_mapTemplateRef1258);  
					stream_RPAREN.add(char_literal90);

					// AST REWRITE
					// elements: argExprList, mapExpr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 212:47: -> ^( INCLUDE_IND mapExpr ( argExprList )? )
					{
						// STParser.g:212:50: ^( INCLUDE_IND mapExpr ( argExprList )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INCLUDE_IND, "INCLUDE_IND"), root_1);
						adaptor.addChild(root_1, stream_mapExpr.nextTree());
						// STParser.g:212:72: ( argExprList )?
						if ( stream_argExprList.hasNext() ) {
							adaptor.addChild(root_1, stream_argExprList.nextTree());
						}
						stream_argExprList.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "mapTemplateRef"


	public static class memberExpr_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "memberExpr"
	// STParser.g:215:1: memberExpr : ( includeExpr -> includeExpr ) (p= '.' ID -> ^( PROP[$p,\"PROP\"] $memberExpr ID ) |p= '.' '(' mapExpr ')' -> ^( PROP_IND[$p,\"PROP_IND\"] $memberExpr mapExpr ) )* ;
	public final STParser.memberExpr_return memberExpr() throws RecognitionException {
		STParser.memberExpr_return retval = new STParser.memberExpr_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken p=null;
		CommonToken ID92=null;
		CommonToken char_literal93=null;
		CommonToken char_literal95=null;
		ParserRuleReturnScope includeExpr91 =null;
		ParserRuleReturnScope mapExpr94 =null;

		CommonTree p_tree=null;
		CommonTree ID92_tree=null;
		CommonTree char_literal93_tree=null;
		CommonTree char_literal95_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_includeExpr=new RewriteRuleSubtreeStream(adaptor,"rule includeExpr");
		RewriteRuleSubtreeStream stream_mapExpr=new RewriteRuleSubtreeStream(adaptor,"rule mapExpr");

		try {
			// STParser.g:216:2: ( ( includeExpr -> includeExpr ) (p= '.' ID -> ^( PROP[$p,\"PROP\"] $memberExpr ID ) |p= '.' '(' mapExpr ')' -> ^( PROP_IND[$p,\"PROP_IND\"] $memberExpr mapExpr ) )* )
			// STParser.g:216:4: ( includeExpr -> includeExpr ) (p= '.' ID -> ^( PROP[$p,\"PROP\"] $memberExpr ID ) |p= '.' '(' mapExpr ')' -> ^( PROP_IND[$p,\"PROP_IND\"] $memberExpr mapExpr ) )*
			{
			// STParser.g:216:4: ( includeExpr -> includeExpr )
			// STParser.g:216:5: includeExpr
			{
			pushFollow(FOLLOW_includeExpr_in_memberExpr1281);
			includeExpr91=includeExpr();
			state._fsp--;

			stream_includeExpr.add(includeExpr91.getTree());
			// AST REWRITE
			// elements: includeExpr
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 216:16: -> includeExpr
			{
				adaptor.addChild(root_0, stream_includeExpr.nextTree());
			}


			retval.tree = root_0;

			}

			// STParser.g:217:3: (p= '.' ID -> ^( PROP[$p,\"PROP\"] $memberExpr ID ) |p= '.' '(' mapExpr ')' -> ^( PROP_IND[$p,\"PROP_IND\"] $memberExpr mapExpr ) )*
			loop33:
			while (true) {
				int alt33=3;
				int LA33_0 = input.LA(1);
				if ( (LA33_0==DOT) ) {
					int LA33_2 = input.LA(2);
					if ( (LA33_2==ID) ) {
						alt33=1;
					}
					else if ( (LA33_2==LPAREN) ) {
						alt33=2;
					}

				}

				switch (alt33) {
				case 1 :
					// STParser.g:217:5: p= '.' ID
					{
					p=(CommonToken)match(input,DOT,FOLLOW_DOT_in_memberExpr1292);  
					stream_DOT.add(p);

					ID92=(CommonToken)match(input,ID,FOLLOW_ID_in_memberExpr1294);  
					stream_ID.add(ID92);

					// AST REWRITE
					// elements: memberExpr, ID
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 217:20: -> ^( PROP[$p,\"PROP\"] $memberExpr ID )
					{
						// STParser.g:217:23: ^( PROP[$p,\"PROP\"] $memberExpr ID )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROP, p, "PROP"), root_1);
						adaptor.addChild(root_1, stream_retval.nextTree());
						adaptor.addChild(root_1, stream_ID.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// STParser.g:218:5: p= '.' '(' mapExpr ')'
					{
					p=(CommonToken)match(input,DOT,FOLLOW_DOT_in_memberExpr1320);  
					stream_DOT.add(p);

					char_literal93=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_memberExpr1322);  
					stream_LPAREN.add(char_literal93);

					pushFollow(FOLLOW_mapExpr_in_memberExpr1324);
					mapExpr94=mapExpr();
					state._fsp--;

					stream_mapExpr.add(mapExpr94.getTree());
					char_literal95=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_memberExpr1326);  
					stream_RPAREN.add(char_literal95);

					// AST REWRITE
					// elements: mapExpr, memberExpr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 218:30: -> ^( PROP_IND[$p,\"PROP_IND\"] $memberExpr mapExpr )
					{
						// STParser.g:218:33: ^( PROP_IND[$p,\"PROP_IND\"] $memberExpr mapExpr )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROP_IND, p, "PROP_IND"), root_1);
						adaptor.addChild(root_1, stream_retval.nextTree());
						adaptor.addChild(root_1, stream_mapExpr.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

				default :
					break loop33;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "memberExpr"


	public static class includeExpr_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "includeExpr"
	// STParser.g:222:1: includeExpr options {k=2; } : ({...}? ID '(' ( expr )? ')' -> ^( EXEC_FUNC ID ( expr )? ) | 'super' '.' ID '(' args ')' -> ^( INCLUDE_SUPER ID ( args )? ) | ID '(' args ')' -> ^( INCLUDE ID ( args )? ) | '@' 'super' '.' ID '(' rp= ')' -> ^( INCLUDE_SUPER_REGION ID ) | '@' ID '(' rp= ')' -> ^( INCLUDE_REGION ID ) | primary );
	public final STParser.includeExpr_return includeExpr() throws RecognitionException {
		STParser.includeExpr_return retval = new STParser.includeExpr_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken rp=null;
		CommonToken ID96=null;
		CommonToken char_literal97=null;
		CommonToken char_literal99=null;
		CommonToken string_literal100=null;
		CommonToken char_literal101=null;
		CommonToken ID102=null;
		CommonToken char_literal103=null;
		CommonToken char_literal105=null;
		CommonToken ID106=null;
		CommonToken char_literal107=null;
		CommonToken char_literal109=null;
		CommonToken char_literal110=null;
		CommonToken string_literal111=null;
		CommonToken char_literal112=null;
		CommonToken ID113=null;
		CommonToken char_literal114=null;
		CommonToken char_literal115=null;
		CommonToken ID116=null;
		CommonToken char_literal117=null;
		ParserRuleReturnScope expr98 =null;
		ParserRuleReturnScope args104 =null;
		ParserRuleReturnScope args108 =null;
		ParserRuleReturnScope primary118 =null;

		CommonTree rp_tree=null;
		CommonTree ID96_tree=null;
		CommonTree char_literal97_tree=null;
		CommonTree char_literal99_tree=null;
		CommonTree string_literal100_tree=null;
		CommonTree char_literal101_tree=null;
		CommonTree ID102_tree=null;
		CommonTree char_literal103_tree=null;
		CommonTree char_literal105_tree=null;
		CommonTree ID106_tree=null;
		CommonTree char_literal107_tree=null;
		CommonTree char_literal109_tree=null;
		CommonTree char_literal110_tree=null;
		CommonTree string_literal111_tree=null;
		CommonTree char_literal112_tree=null;
		CommonTree ID113_tree=null;
		CommonTree char_literal114_tree=null;
		CommonTree char_literal115_tree=null;
		CommonTree ID116_tree=null;
		CommonTree char_literal117_tree=null;
		RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_SUPER=new RewriteRuleTokenStream(adaptor,"token SUPER");
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_args=new RewriteRuleSubtreeStream(adaptor,"rule args");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");

		try {
			// STParser.g:224:2: ({...}? ID '(' ( expr )? ')' -> ^( EXEC_FUNC ID ( expr )? ) | 'super' '.' ID '(' args ')' -> ^( INCLUDE_SUPER ID ( args )? ) | ID '(' args ')' -> ^( INCLUDE ID ( args )? ) | '@' 'super' '.' ID '(' rp= ')' -> ^( INCLUDE_SUPER_REGION ID ) | '@' ID '(' rp= ')' -> ^( INCLUDE_REGION ID ) | primary )
			int alt35=6;
			int LA35_0 = input.LA(1);
			if ( (LA35_0==ID) ) {
				int LA35_1 = input.LA(2);
				if ( (LA35_1==LPAREN) ) {
					int LA35_10 = input.LA(3);
					if ( ((Compiler.funcs.containsKey(input.LT(1).getText()))) ) {
						alt35=1;
					}
					else if ( (true) ) {
						alt35=3;
					}

				}
				else if ( (LA35_1==SEMI||LA35_1==COLON||LA35_1==RPAREN||(LA35_1 >= RBRACK && LA35_1 <= DOT)||LA35_1==RDELIM||(LA35_1 >= OR && LA35_1 <= AND)) ) {
					alt35=6;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 35, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA35_0==SUPER) ) {
				alt35=2;
			}
			else if ( (LA35_0==AT) ) {
				int LA35_3 = input.LA(2);
				if ( (LA35_3==SUPER) ) {
					alt35=4;
				}
				else if ( (LA35_3==ID) ) {
					alt35=5;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 35, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA35_0==LBRACK||LA35_0==LCURLY||LA35_0==STRING||(LA35_0 >= TRUE && LA35_0 <= FALSE)) ) {
				alt35=6;
			}
			else if ( (LA35_0==LPAREN) && (((conditional_stack.size()==0)||(conditional_stack.size()>0)))) {
				alt35=6;
			}

			switch (alt35) {
				case 1 :
					// STParser.g:224:4: {...}? ID '(' ( expr )? ')'
					{
					if ( !((Compiler.funcs.containsKey(input.LT(1).getText()))) ) {
						throw new FailedPredicateException(input, "includeExpr", "Compiler.funcs.containsKey(input.LT(1).getText())");
					}
					ID96=(CommonToken)match(input,ID,FOLLOW_ID_in_includeExpr1370);  
					stream_ID.add(ID96);

					char_literal97=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_includeExpr1372);  
					stream_LPAREN.add(char_literal97);

					// STParser.g:225:10: ( expr )?
					int alt34=2;
					int LA34_0 = input.LA(1);
					if ( (LA34_0==SUPER||LA34_0==LBRACK||LA34_0==LCURLY||(LA34_0 >= ID && LA34_0 <= STRING)||LA34_0==AT||(LA34_0 >= TRUE && LA34_0 <= FALSE)) ) {
						alt34=1;
					}
					else if ( (LA34_0==LPAREN) && (((conditional_stack.size()==0)||(conditional_stack.size()>0)))) {
						alt34=1;
					}
					switch (alt34) {
						case 1 :
							// STParser.g:225:10: expr
							{
							pushFollow(FOLLOW_expr_in_includeExpr1374);
							expr98=expr();
							state._fsp--;

							stream_expr.add(expr98.getTree());
							}
							break;

					}

					char_literal99=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_includeExpr1377);  
					stream_RPAREN.add(char_literal99);

					// AST REWRITE
					// elements: expr, ID
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 225:25: -> ^( EXEC_FUNC ID ( expr )? )
					{
						// STParser.g:225:28: ^( EXEC_FUNC ID ( expr )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXEC_FUNC, "EXEC_FUNC"), root_1);
						adaptor.addChild(root_1, stream_ID.nextNode());
						// STParser.g:225:43: ( expr )?
						if ( stream_expr.hasNext() ) {
							adaptor.addChild(root_1, stream_expr.nextTree());
						}
						stream_expr.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// STParser.g:226:4: 'super' '.' ID '(' args ')'
					{
					string_literal100=(CommonToken)match(input,SUPER,FOLLOW_SUPER_in_includeExpr1398);  
					stream_SUPER.add(string_literal100);

					char_literal101=(CommonToken)match(input,DOT,FOLLOW_DOT_in_includeExpr1400);  
					stream_DOT.add(char_literal101);

					ID102=(CommonToken)match(input,ID,FOLLOW_ID_in_includeExpr1402);  
					stream_ID.add(ID102);

					char_literal103=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_includeExpr1404);  
					stream_LPAREN.add(char_literal103);

					pushFollow(FOLLOW_args_in_includeExpr1406);
					args104=args();
					state._fsp--;

					stream_args.add(args104.getTree());
					char_literal105=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_includeExpr1408);  
					stream_RPAREN.add(char_literal105);

					// AST REWRITE
					// elements: ID, args
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 226:35: -> ^( INCLUDE_SUPER ID ( args )? )
					{
						// STParser.g:226:38: ^( INCLUDE_SUPER ID ( args )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INCLUDE_SUPER, "INCLUDE_SUPER"), root_1);
						adaptor.addChild(root_1, stream_ID.nextNode());
						// STParser.g:226:57: ( args )?
						if ( stream_args.hasNext() ) {
							adaptor.addChild(root_1, stream_args.nextTree());
						}
						stream_args.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// STParser.g:227:4: ID '(' args ')'
					{
					ID106=(CommonToken)match(input,ID,FOLLOW_ID_in_includeExpr1427);  
					stream_ID.add(ID106);

					char_literal107=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_includeExpr1429);  
					stream_LPAREN.add(char_literal107);

					pushFollow(FOLLOW_args_in_includeExpr1431);
					args108=args();
					state._fsp--;

					stream_args.add(args108.getTree());
					char_literal109=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_includeExpr1433);  
					stream_RPAREN.add(char_literal109);

					// AST REWRITE
					// elements: args, ID
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 227:26: -> ^( INCLUDE ID ( args )? )
					{
						// STParser.g:227:29: ^( INCLUDE ID ( args )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INCLUDE, "INCLUDE"), root_1);
						adaptor.addChild(root_1, stream_ID.nextNode());
						// STParser.g:227:42: ( args )?
						if ( stream_args.hasNext() ) {
							adaptor.addChild(root_1, stream_args.nextTree());
						}
						stream_args.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 4 :
					// STParser.g:228:4: '@' 'super' '.' ID '(' rp= ')'
					{
					char_literal110=(CommonToken)match(input,AT,FOLLOW_AT_in_includeExpr1455);  
					stream_AT.add(char_literal110);

					string_literal111=(CommonToken)match(input,SUPER,FOLLOW_SUPER_in_includeExpr1457);  
					stream_SUPER.add(string_literal111);

					char_literal112=(CommonToken)match(input,DOT,FOLLOW_DOT_in_includeExpr1459);  
					stream_DOT.add(char_literal112);

					ID113=(CommonToken)match(input,ID,FOLLOW_ID_in_includeExpr1461);  
					stream_ID.add(ID113);

					char_literal114=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_includeExpr1463);  
					stream_LPAREN.add(char_literal114);

					rp=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_includeExpr1467);  
					stream_RPAREN.add(rp);

					// AST REWRITE
					// elements: ID
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 228:36: -> ^( INCLUDE_SUPER_REGION ID )
					{
						// STParser.g:228:39: ^( INCLUDE_SUPER_REGION ID )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INCLUDE_SUPER_REGION, "INCLUDE_SUPER_REGION"), root_1);
						adaptor.addChild(root_1, stream_ID.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 5 :
					// STParser.g:229:4: '@' ID '(' rp= ')'
					{
					char_literal115=(CommonToken)match(input,AT,FOLLOW_AT_in_includeExpr1482);  
					stream_AT.add(char_literal115);

					ID116=(CommonToken)match(input,ID,FOLLOW_ID_in_includeExpr1484);  
					stream_ID.add(ID116);

					char_literal117=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_includeExpr1486);  
					stream_LPAREN.add(char_literal117);

					rp=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_includeExpr1490);  
					stream_RPAREN.add(rp);

					// AST REWRITE
					// elements: ID
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 229:27: -> ^( INCLUDE_REGION ID )
					{
						// STParser.g:229:30: ^( INCLUDE_REGION ID )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INCLUDE_REGION, "INCLUDE_REGION"), root_1);
						adaptor.addChild(root_1, stream_ID.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 6 :
					// STParser.g:230:4: primary
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_primary_in_includeExpr1508);
					primary118=primary();
					state._fsp--;

					adaptor.addChild(root_0, primary118.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "includeExpr"


	public static class primary_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "primary"
	// STParser.g:233:1: primary : ( ID | STRING | TRUE | FALSE | subtemplate | list |{...}? => '(' ! conditional ')' !|{...}? =>lp= '(' expr ')' ( '(' ( argExprList )? ')' -> ^( INCLUDE_IND[$lp] expr ( argExprList )? ) | -> ^( TO_STR[$lp] expr ) ) );
	public final STParser.primary_return primary() throws RecognitionException {
		STParser.primary_return retval = new STParser.primary_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken lp=null;
		CommonToken ID119=null;
		CommonToken STRING120=null;
		CommonToken TRUE121=null;
		CommonToken FALSE122=null;
		CommonToken char_literal125=null;
		CommonToken char_literal127=null;
		CommonToken char_literal129=null;
		CommonToken char_literal130=null;
		CommonToken char_literal132=null;
		ParserRuleReturnScope subtemplate123 =null;
		ParserRuleReturnScope list124 =null;
		ParserRuleReturnScope conditional126 =null;
		ParserRuleReturnScope expr128 =null;
		ParserRuleReturnScope argExprList131 =null;

		CommonTree lp_tree=null;
		CommonTree ID119_tree=null;
		CommonTree STRING120_tree=null;
		CommonTree TRUE121_tree=null;
		CommonTree FALSE122_tree=null;
		CommonTree char_literal125_tree=null;
		CommonTree char_literal127_tree=null;
		CommonTree char_literal129_tree=null;
		CommonTree char_literal130_tree=null;
		CommonTree char_literal132_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_argExprList=new RewriteRuleSubtreeStream(adaptor,"rule argExprList");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");

		try {
			// STParser.g:234:2: ( ID | STRING | TRUE | FALSE | subtemplate | list |{...}? => '(' ! conditional ')' !|{...}? =>lp= '(' expr ')' ( '(' ( argExprList )? ')' -> ^( INCLUDE_IND[$lp] expr ( argExprList )? ) | -> ^( TO_STR[$lp] expr ) ) )
			int alt38=8;
			int LA38_0 = input.LA(1);
			if ( (LA38_0==ID) ) {
				alt38=1;
			}
			else if ( (LA38_0==STRING) ) {
				alt38=2;
			}
			else if ( (LA38_0==TRUE) ) {
				alt38=3;
			}
			else if ( (LA38_0==FALSE) ) {
				alt38=4;
			}
			else if ( (LA38_0==LCURLY) ) {
				alt38=5;
			}
			else if ( (LA38_0==LBRACK) ) {
				alt38=6;
			}
			else if ( (LA38_0==LPAREN) && (((conditional_stack.size()==0)||(conditional_stack.size()>0)))) {
				int LA38_7 = input.LA(2);
				if ( ((conditional_stack.size()>0)) ) {
					alt38=7;
				}
				else if ( ((conditional_stack.size()==0)) ) {
					alt38=8;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 38, 7, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			switch (alt38) {
				case 1 :
					// STParser.g:234:4: ID
					{
					root_0 = (CommonTree)adaptor.nil();


					ID119=(CommonToken)match(input,ID,FOLLOW_ID_in_primary1519); 
					ID119_tree = (CommonTree)adaptor.create(ID119);
					adaptor.addChild(root_0, ID119_tree);

					}
					break;
				case 2 :
					// STParser.g:235:4: STRING
					{
					root_0 = (CommonTree)adaptor.nil();


					STRING120=(CommonToken)match(input,STRING,FOLLOW_STRING_in_primary1524); 
					STRING120_tree = (CommonTree)adaptor.create(STRING120);
					adaptor.addChild(root_0, STRING120_tree);

					}
					break;
				case 3 :
					// STParser.g:236:4: TRUE
					{
					root_0 = (CommonTree)adaptor.nil();


					TRUE121=(CommonToken)match(input,TRUE,FOLLOW_TRUE_in_primary1529); 
					TRUE121_tree = (CommonTree)adaptor.create(TRUE121);
					adaptor.addChild(root_0, TRUE121_tree);

					}
					break;
				case 4 :
					// STParser.g:237:4: FALSE
					{
					root_0 = (CommonTree)adaptor.nil();


					FALSE122=(CommonToken)match(input,FALSE,FOLLOW_FALSE_in_primary1534); 
					FALSE122_tree = (CommonTree)adaptor.create(FALSE122);
					adaptor.addChild(root_0, FALSE122_tree);

					}
					break;
				case 5 :
					// STParser.g:238:4: subtemplate
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_subtemplate_in_primary1539);
					subtemplate123=subtemplate();
					state._fsp--;

					adaptor.addChild(root_0, subtemplate123.getTree());

					}
					break;
				case 6 :
					// STParser.g:239:4: list
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_list_in_primary1544);
					list124=list();
					state._fsp--;

					adaptor.addChild(root_0, list124.getTree());

					}
					break;
				case 7 :
					// STParser.g:240:4: {...}? => '(' ! conditional ')' !
					{
					root_0 = (CommonTree)adaptor.nil();


					if ( !((conditional_stack.size()>0)) ) {
						throw new FailedPredicateException(input, "primary", "$conditional.size()>0");
					}
					char_literal125=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_primary1553); 
					pushFollow(FOLLOW_conditional_in_primary1556);
					conditional126=conditional();
					state._fsp--;

					adaptor.addChild(root_0, conditional126.getTree());

					char_literal127=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_primary1558); 
					}
					break;
				case 8 :
					// STParser.g:241:4: {...}? =>lp= '(' expr ')' ( '(' ( argExprList )? ')' -> ^( INCLUDE_IND[$lp] expr ( argExprList )? ) | -> ^( TO_STR[$lp] expr ) )
					{
					if ( !((conditional_stack.size()==0)) ) {
						throw new FailedPredicateException(input, "primary", "$conditional.size()==0");
					}
					lp=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_primary1569);  
					stream_LPAREN.add(lp);

					pushFollow(FOLLOW_expr_in_primary1571);
					expr128=expr();
					state._fsp--;

					stream_expr.add(expr128.getTree());
					char_literal129=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_primary1573);  
					stream_RPAREN.add(char_literal129);

					// STParser.g:242:3: ( '(' ( argExprList )? ')' -> ^( INCLUDE_IND[$lp] expr ( argExprList )? ) | -> ^( TO_STR[$lp] expr ) )
					int alt37=2;
					int LA37_0 = input.LA(1);
					if ( (LA37_0==LPAREN) ) {
						alt37=1;
					}
					else if ( (LA37_0==SEMI||LA37_0==COLON||LA37_0==RPAREN||(LA37_0 >= RBRACK && LA37_0 <= DOT)||LA37_0==RDELIM||(LA37_0 >= OR && LA37_0 <= AND)) ) {
						alt37=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 37, 0, input);
						throw nvae;
					}

					switch (alt37) {
						case 1 :
							// STParser.g:242:5: '(' ( argExprList )? ')'
							{
							char_literal130=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_primary1579);  
							stream_LPAREN.add(char_literal130);

							// STParser.g:242:9: ( argExprList )?
							int alt36=2;
							int LA36_0 = input.LA(1);
							if ( (LA36_0==SUPER||LA36_0==LBRACK||LA36_0==LCURLY||(LA36_0 >= ID && LA36_0 <= STRING)||LA36_0==AT||(LA36_0 >= TRUE && LA36_0 <= FALSE)) ) {
								alt36=1;
							}
							else if ( (LA36_0==LPAREN) && (((conditional_stack.size()==0)||(conditional_stack.size()>0)))) {
								alt36=1;
							}
							switch (alt36) {
								case 1 :
									// STParser.g:242:9: argExprList
									{
									pushFollow(FOLLOW_argExprList_in_primary1581);
									argExprList131=argExprList();
									state._fsp--;

									stream_argExprList.add(argExprList131.getTree());
									}
									break;

							}

							char_literal132=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_primary1584);  
							stream_RPAREN.add(char_literal132);

							// AST REWRITE
							// elements: argExprList, expr
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (CommonTree)adaptor.nil();
							// 242:35: -> ^( INCLUDE_IND[$lp] expr ( argExprList )? )
							{
								// STParser.g:242:38: ^( INCLUDE_IND[$lp] expr ( argExprList )? )
								{
								CommonTree root_1 = (CommonTree)adaptor.nil();
								root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INCLUDE_IND, lp), root_1);
								adaptor.addChild(root_1, stream_expr.nextTree());
								// STParser.g:242:62: ( argExprList )?
								if ( stream_argExprList.hasNext() ) {
									adaptor.addChild(root_1, stream_argExprList.nextTree());
								}
								stream_argExprList.reset();

								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;

							}
							break;
						case 2 :
							// STParser.g:243:14: 
							{
							// AST REWRITE
							// elements: expr
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (CommonTree)adaptor.nil();
							// 243:14: -> ^( TO_STR[$lp] expr )
							{
								// STParser.g:243:17: ^( TO_STR[$lp] expr )
								{
								CommonTree root_1 = (CommonTree)adaptor.nil();
								root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TO_STR, lp), root_1);
								adaptor.addChild(root_1, stream_expr.nextTree());
								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;

							}
							break;

					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "primary"


	public static class args_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "args"
	// STParser.g:247:1: args : ( argExprList | namedArg ( ',' namedArg )* ( ',' '...' )? -> ( namedArg )+ ( '...' )? | '...' |);
	public final STParser.args_return args() throws RecognitionException {
		STParser.args_return retval = new STParser.args_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken char_literal135=null;
		CommonToken char_literal137=null;
		CommonToken string_literal138=null;
		CommonToken string_literal139=null;
		ParserRuleReturnScope argExprList133 =null;
		ParserRuleReturnScope namedArg134 =null;
		ParserRuleReturnScope namedArg136 =null;

		CommonTree char_literal135_tree=null;
		CommonTree char_literal137_tree=null;
		CommonTree string_literal138_tree=null;
		CommonTree string_literal139_tree=null;
		RewriteRuleTokenStream stream_ELLIPSIS=new RewriteRuleTokenStream(adaptor,"token ELLIPSIS");
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleSubtreeStream stream_namedArg=new RewriteRuleSubtreeStream(adaptor,"rule namedArg");

		try {
			// STParser.g:247:5: ( argExprList | namedArg ( ',' namedArg )* ( ',' '...' )? -> ( namedArg )+ ( '...' )? | '...' |)
			int alt41=4;
			int LA41_0 = input.LA(1);
			if ( (LA41_0==ID) ) {
				int LA41_1 = input.LA(2);
				if ( ((LA41_1 >= COLON && LA41_1 <= RPAREN)||(LA41_1 >= COMMA && LA41_1 <= DOT)) ) {
					alt41=1;
				}
				else if ( (LA41_1==EQUALS) ) {
					alt41=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA41_0==SUPER||LA41_0==LBRACK||LA41_0==LCURLY||LA41_0==STRING||LA41_0==AT||(LA41_0 >= TRUE && LA41_0 <= FALSE)) ) {
				alt41=1;
			}
			else if ( (LA41_0==LPAREN) && (((conditional_stack.size()==0)||(conditional_stack.size()>0)))) {
				alt41=1;
			}
			else if ( (LA41_0==ELLIPSIS) ) {
				alt41=3;
			}
			else if ( (LA41_0==RPAREN) ) {
				alt41=4;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 41, 0, input);
				throw nvae;
			}

			switch (alt41) {
				case 1 :
					// STParser.g:247:7: argExprList
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_argExprList_in_args1640);
					argExprList133=argExprList();
					state._fsp--;

					adaptor.addChild(root_0, argExprList133.getTree());

					}
					break;
				case 2 :
					// STParser.g:248:4: namedArg ( ',' namedArg )* ( ',' '...' )?
					{
					pushFollow(FOLLOW_namedArg_in_args1645);
					namedArg134=namedArg();
					state._fsp--;

					stream_namedArg.add(namedArg134.getTree());
					// STParser.g:248:13: ( ',' namedArg )*
					loop39:
					while (true) {
						int alt39=2;
						int LA39_0 = input.LA(1);
						if ( (LA39_0==COMMA) ) {
							int LA39_1 = input.LA(2);
							if ( (LA39_1==ID) ) {
								alt39=1;
							}

						}

						switch (alt39) {
						case 1 :
							// STParser.g:248:15: ',' namedArg
							{
							char_literal135=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_args1649);  
							stream_COMMA.add(char_literal135);

							pushFollow(FOLLOW_namedArg_in_args1651);
							namedArg136=namedArg();
							state._fsp--;

							stream_namedArg.add(namedArg136.getTree());
							}
							break;

						default :
							break loop39;
						}
					}

					// STParser.g:248:31: ( ',' '...' )?
					int alt40=2;
					int LA40_0 = input.LA(1);
					if ( (LA40_0==COMMA) ) {
						alt40=1;
					}
					switch (alt40) {
						case 1 :
							// STParser.g:248:32: ',' '...'
							{
							char_literal137=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_args1657);  
							stream_COMMA.add(char_literal137);

							string_literal138=(CommonToken)match(input,ELLIPSIS,FOLLOW_ELLIPSIS_in_args1659);  
							stream_ELLIPSIS.add(string_literal138);

							}
							break;

					}

					// AST REWRITE
					// elements: ELLIPSIS, namedArg
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 248:44: -> ( namedArg )+ ( '...' )?
					{
						if ( !(stream_namedArg.hasNext()) ) {
							throw new RewriteEarlyExitException();
						}
						while ( stream_namedArg.hasNext() ) {
							adaptor.addChild(root_0, stream_namedArg.nextTree());
						}
						stream_namedArg.reset();

						// STParser.g:248:57: ( '...' )?
						if ( stream_ELLIPSIS.hasNext() ) {
							adaptor.addChild(root_0, stream_ELLIPSIS.nextNode());
						}
						stream_ELLIPSIS.reset();

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// STParser.g:249:9: '...'
					{
					root_0 = (CommonTree)adaptor.nil();


					string_literal139=(CommonToken)match(input,ELLIPSIS,FOLLOW_ELLIPSIS_in_args1679); 
					string_literal139_tree = (CommonTree)adaptor.create(string_literal139);
					adaptor.addChild(root_0, string_literal139_tree);

					}
					break;
				case 4 :
					// STParser.g:251:2: 
					{
					root_0 = (CommonTree)adaptor.nil();


					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "args"


	public static class argExprList_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "argExprList"
	// STParser.g:253:1: argExprList : arg ( ',' arg )* -> ( arg )+ ;
	public final STParser.argExprList_return argExprList() throws RecognitionException {
		STParser.argExprList_return retval = new STParser.argExprList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken char_literal141=null;
		ParserRuleReturnScope arg140 =null;
		ParserRuleReturnScope arg142 =null;

		CommonTree char_literal141_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");

		try {
			// STParser.g:253:13: ( arg ( ',' arg )* -> ( arg )+ )
			// STParser.g:253:15: arg ( ',' arg )*
			{
			pushFollow(FOLLOW_arg_in_argExprList1692);
			arg140=arg();
			state._fsp--;

			stream_arg.add(arg140.getTree());
			// STParser.g:253:19: ( ',' arg )*
			loop42:
			while (true) {
				int alt42=2;
				int LA42_0 = input.LA(1);
				if ( (LA42_0==COMMA) ) {
					alt42=1;
				}

				switch (alt42) {
				case 1 :
					// STParser.g:253:21: ',' arg
					{
					char_literal141=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_argExprList1696);  
					stream_COMMA.add(char_literal141);

					pushFollow(FOLLOW_arg_in_argExprList1698);
					arg142=arg();
					state._fsp--;

					stream_arg.add(arg142.getTree());
					}
					break;

				default :
					break loop42;
				}
			}

			// AST REWRITE
			// elements: arg
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 253:32: -> ( arg )+
			{
				if ( !(stream_arg.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_arg.hasNext() ) {
					adaptor.addChild(root_0, stream_arg.nextTree());
				}
				stream_arg.reset();

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "argExprList"


	public static class arg_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "arg"
	// STParser.g:255:1: arg : exprNoComma ;
	public final STParser.arg_return arg() throws RecognitionException {
		STParser.arg_return retval = new STParser.arg_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope exprNoComma143 =null;


		try {
			// STParser.g:255:5: ( exprNoComma )
			// STParser.g:255:7: exprNoComma
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_exprNoComma_in_arg1715);
			exprNoComma143=exprNoComma();
			state._fsp--;

			adaptor.addChild(root_0, exprNoComma143.getTree());

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "arg"


	public static class namedArg_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "namedArg"
	// STParser.g:257:1: namedArg : ID '=' arg -> ^( '=' ID arg ) ;
	public final STParser.namedArg_return namedArg() throws RecognitionException {
		STParser.namedArg_return retval = new STParser.namedArg_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken ID144=null;
		CommonToken char_literal145=null;
		ParserRuleReturnScope arg146 =null;

		CommonTree ID144_tree=null;
		CommonTree char_literal145_tree=null;
		RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
		RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
		RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");

		try {
			// STParser.g:257:10: ( ID '=' arg -> ^( '=' ID arg ) )
			// STParser.g:257:12: ID '=' arg
			{
			ID144=(CommonToken)match(input,ID,FOLLOW_ID_in_namedArg1724);  
			stream_ID.add(ID144);

			char_literal145=(CommonToken)match(input,EQUALS,FOLLOW_EQUALS_in_namedArg1726);  
			stream_EQUALS.add(char_literal145);

			pushFollow(FOLLOW_arg_in_namedArg1728);
			arg146=arg();
			state._fsp--;

			stream_arg.add(arg146.getTree());
			// AST REWRITE
			// elements: arg, EQUALS, ID
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 257:23: -> ^( '=' ID arg )
			{
				// STParser.g:257:26: ^( '=' ID arg )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot(stream_EQUALS.nextNode(), root_1);
				adaptor.addChild(root_1, stream_ID.nextNode());
				adaptor.addChild(root_1, stream_arg.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "namedArg"


	public static class list_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "list"
	// STParser.g:259:1: list : ({...}?lb= '[' ']' -> LIST[$lb] |lb= '[' listElement ( ',' listElement )* ']' -> ^( LIST[$lb] ( listElement )* ) );
	public final STParser.list_return list() throws RecognitionException {
		STParser.list_return retval = new STParser.list_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		CommonToken lb=null;
		CommonToken char_literal147=null;
		CommonToken char_literal149=null;
		CommonToken char_literal151=null;
		ParserRuleReturnScope listElement148 =null;
		ParserRuleReturnScope listElement150 =null;

		CommonTree lb_tree=null;
		CommonTree char_literal147_tree=null;
		CommonTree char_literal149_tree=null;
		CommonTree char_literal151_tree=null;
		RewriteRuleTokenStream stream_RBRACK=new RewriteRuleTokenStream(adaptor,"token RBRACK");
		RewriteRuleTokenStream stream_LBRACK=new RewriteRuleTokenStream(adaptor,"token LBRACK");
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleSubtreeStream stream_listElement=new RewriteRuleSubtreeStream(adaptor,"rule listElement");

		try {
			// STParser.g:259:5: ({...}?lb= '[' ']' -> LIST[$lb] |lb= '[' listElement ( ',' listElement )* ']' -> ^( LIST[$lb] ( listElement )* ) )
			int alt44=2;
			int LA44_0 = input.LA(1);
			if ( (LA44_0==LBRACK) ) {
				int LA44_1 = input.LA(2);
				if ( (LA44_1==RBRACK) ) {
					int LA44_2 = input.LA(3);
					if ( ((input.LA(2)==RBRACK)) ) {
						alt44=1;
					}
					else if ( (true) ) {
						alt44=2;
					}

				}
				else if ( (LA44_1==SUPER||LA44_1==LPAREN||LA44_1==LBRACK||LA44_1==COMMA||LA44_1==LCURLY||(LA44_1 >= ID && LA44_1 <= STRING)||LA44_1==AT||(LA44_1 >= TRUE && LA44_1 <= FALSE)) ) {
					alt44=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 44, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 44, 0, input);
				throw nvae;
			}

			switch (alt44) {
				case 1 :
					// STParser.g:259:7: {...}?lb= '[' ']'
					{
					if ( !((input.LA(2)==RBRACK)) ) {
						throw new FailedPredicateException(input, "list", "input.LA(2)==RBRACK");
					}
					lb=(CommonToken)match(input,LBRACK,FOLLOW_LBRACK_in_list1753);  
					stream_LBRACK.add(lb);

					char_literal147=(CommonToken)match(input,RBRACK,FOLLOW_RBRACK_in_list1755);  
					stream_RBRACK.add(char_literal147);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 260:14: -> LIST[$lb]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(LIST, lb));
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// STParser.g:261:4: lb= '[' listElement ( ',' listElement )* ']'
					{
					lb=(CommonToken)match(input,LBRACK,FOLLOW_LBRACK_in_list1767);  
					stream_LBRACK.add(lb);

					pushFollow(FOLLOW_listElement_in_list1769);
					listElement148=listElement();
					state._fsp--;

					stream_listElement.add(listElement148.getTree());
					// STParser.g:261:23: ( ',' listElement )*
					loop43:
					while (true) {
						int alt43=2;
						int LA43_0 = input.LA(1);
						if ( (LA43_0==COMMA) ) {
							alt43=1;
						}

						switch (alt43) {
						case 1 :
							// STParser.g:261:25: ',' listElement
							{
							char_literal149=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_list1773);  
							stream_COMMA.add(char_literal149);

							pushFollow(FOLLOW_listElement_in_list1775);
							listElement150=listElement();
							state._fsp--;

							stream_listElement.add(listElement150.getTree());
							}
							break;

						default :
							break loop43;
						}
					}

					char_literal151=(CommonToken)match(input,RBRACK,FOLLOW_RBRACK_in_list1780);  
					stream_RBRACK.add(char_literal151);

					// AST REWRITE
					// elements: listElement
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 261:48: -> ^( LIST[$lb] ( listElement )* )
					{
						// STParser.g:261:51: ^( LIST[$lb] ( listElement )* )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, lb), root_1);
						// STParser.g:261:63: ( listElement )*
						while ( stream_listElement.hasNext() ) {
							adaptor.addChild(root_1, stream_listElement.nextTree());
						}
						stream_listElement.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "list"


	public static class listElement_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "listElement"
	// STParser.g:264:1: listElement : ( exprNoComma | -> NULL );
	public final STParser.listElement_return listElement() throws RecognitionException {
		STParser.listElement_return retval = new STParser.listElement_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope exprNoComma152 =null;


		try {
			// STParser.g:264:13: ( exprNoComma | -> NULL )
			int alt45=2;
			int LA45_0 = input.LA(1);
			if ( (LA45_0==SUPER||LA45_0==LBRACK||LA45_0==LCURLY||(LA45_0 >= ID && LA45_0 <= STRING)||LA45_0==AT||(LA45_0 >= TRUE && LA45_0 <= FALSE)) ) {
				alt45=1;
			}
			else if ( (LA45_0==LPAREN) && (((conditional_stack.size()==0)||(conditional_stack.size()>0)))) {
				alt45=1;
			}
			else if ( ((LA45_0 >= RBRACK && LA45_0 <= COMMA)) ) {
				alt45=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 45, 0, input);
				throw nvae;
			}

			switch (alt45) {
				case 1 :
					// STParser.g:264:15: exprNoComma
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_exprNoComma_in_listElement1800);
					exprNoComma152=exprNoComma();
					state._fsp--;

					adaptor.addChild(root_0, exprNoComma152.getTree());

					}
					break;
				case 2 :
					// STParser.g:264:29: 
					{
					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 264:29: -> NULL
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(NULL, "NULL"));
					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		   catch (RecognitionException re) { throw re; }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "listElement"

	// Delegated rules



	public static final BitSet FOLLOW_template_in_templateAndEOF139 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_templateAndEOF141 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_element_in_template155 = new BitSet(new long[]{0x0000002180C00002L});
	public static final BitSet FOLLOW_INDENT_in_element168 = new BitSet(new long[]{0x0000002000000000L});
	public static final BitSet FOLLOW_COMMENT_in_element171 = new BitSet(new long[]{0x0000000100000000L});
	public static final BitSet FOLLOW_NEWLINE_in_element173 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INDENT_in_element181 = new BitSet(new long[]{0x0000002100C00000L});
	public static final BitSet FOLLOW_singleElement_in_element183 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_singleElement_in_element200 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_compoundElement_in_element205 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_exprTag_in_singleElement216 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_singleElement221 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEWLINE_in_singleElement226 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COMMENT_in_singleElement231 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ifstat_in_compoundElement244 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_region_in_compoundElement249 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LDELIM_in_exprTag260 = new BitSet(new long[]{0x0000001A06114100L});
	public static final BitSet FOLLOW_expr_in_exprTag262 = new BitSet(new long[]{0x0000000001000200L});
	public static final BitSet FOLLOW_SEMI_in_exprTag266 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_exprOptions_in_exprTag268 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_RDELIM_in_exprTag273 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INDENT_in_region305 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_LDELIM_in_region310 = new BitSet(new long[]{0x0000000200000000L});
	public static final BitSet FOLLOW_AT_in_region312 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_ID_in_region314 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_RDELIM_in_region316 = new BitSet(new long[]{0x0000002180C00000L});
	public static final BitSet FOLLOW_template_in_region322 = new BitSet(new long[]{0x0000000080800000L});
	public static final BitSet FOLLOW_INDENT_in_region326 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_LDELIM_in_region329 = new BitSet(new long[]{0x0000000400000000L});
	public static final BitSet FOLLOW_END_in_region331 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_RDELIM_in_region333 = new BitSet(new long[]{0x0000000100000002L});
	public static final BitSet FOLLOW_NEWLINE_in_region344 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LCURLY_in_subtemplate420 = new BitSet(new long[]{0x0000002182E00000L});
	public static final BitSet FOLLOW_ID_in_subtemplate426 = new BitSet(new long[]{0x0000000010040000L});
	public static final BitSet FOLLOW_COMMA_in_subtemplate430 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_ID_in_subtemplate435 = new BitSet(new long[]{0x0000000010040000L});
	public static final BitSet FOLLOW_PIPE_in_subtemplate440 = new BitSet(new long[]{0x0000002180E00000L});
	public static final BitSet FOLLOW_template_in_subtemplate445 = new BitSet(new long[]{0x0000000080200000L});
	public static final BitSet FOLLOW_INDENT_in_subtemplate447 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_RCURLY_in_subtemplate450 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INDENT_in_ifstat491 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_LDELIM_in_ifstat494 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_IF_in_ifstat496 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_LPAREN_in_ifstat498 = new BitSet(new long[]{0x0000001A06114500L});
	public static final BitSet FOLLOW_conditional_in_ifstat502 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_ifstat504 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_RDELIM_in_ifstat506 = new BitSet(new long[]{0x0000002180C00000L});
	public static final BitSet FOLLOW_template_in_ifstat515 = new BitSet(new long[]{0x0000000080800000L});
	public static final BitSet FOLLOW_INDENT_in_ifstat522 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_LDELIM_in_ifstat525 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_ELSEIF_in_ifstat527 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_LPAREN_in_ifstat529 = new BitSet(new long[]{0x0000001A06114500L});
	public static final BitSet FOLLOW_conditional_in_ifstat533 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_ifstat535 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_RDELIM_in_ifstat537 = new BitSet(new long[]{0x0000002180C00000L});
	public static final BitSet FOLLOW_template_in_ifstat541 = new BitSet(new long[]{0x0000000080800000L});
	public static final BitSet FOLLOW_INDENT_in_ifstat551 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_LDELIM_in_ifstat554 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_ELSE_in_ifstat556 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_RDELIM_in_ifstat558 = new BitSet(new long[]{0x0000002180C00000L});
	public static final BitSet FOLLOW_template_in_ifstat562 = new BitSet(new long[]{0x0000000080800000L});
	public static final BitSet FOLLOW_INDENT_in_ifstat570 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_LDELIM_in_ifstat576 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_ENDIF_in_ifstat578 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_RDELIM_in_ifstat582 = new BitSet(new long[]{0x0000000100000002L});
	public static final BitSet FOLLOW_NEWLINE_in_ifstat593 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_andConditional_in_conditional713 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_OR_in_conditional717 = new BitSet(new long[]{0x0000001A06114500L});
	public static final BitSet FOLLOW_andConditional_in_conditional720 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_notConditional_in_andConditional733 = new BitSet(new long[]{0x0000000040000002L});
	public static final BitSet FOLLOW_AND_in_andConditional737 = new BitSet(new long[]{0x0000001A06114500L});
	public static final BitSet FOLLOW_notConditional_in_andConditional740 = new BitSet(new long[]{0x0000000040000002L});
	public static final BitSet FOLLOW_BANG_in_notConditional753 = new BitSet(new long[]{0x0000001A06114500L});
	public static final BitSet FOLLOW_notConditional_in_notConditional756 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_memberExpr_in_notConditional761 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_notConditionalExpr773 = new BitSet(new long[]{0x0000000000080002L});
	public static final BitSet FOLLOW_DOT_in_notConditionalExpr784 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_ID_in_notConditionalExpr788 = new BitSet(new long[]{0x0000000000080002L});
	public static final BitSet FOLLOW_DOT_in_notConditionalExpr814 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_LPAREN_in_notConditionalExpr816 = new BitSet(new long[]{0x0000001A06114100L});
	public static final BitSet FOLLOW_mapExpr_in_notConditionalExpr818 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_notConditionalExpr820 = new BitSet(new long[]{0x0000000000080002L});
	public static final BitSet FOLLOW_option_in_exprOptions850 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_COMMA_in_exprOptions854 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_option_in_exprOptions856 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_ID_in_option883 = new BitSet(new long[]{0x0000000000001002L});
	public static final BitSet FOLLOW_EQUALS_in_option893 = new BitSet(new long[]{0x0000001A06114100L});
	public static final BitSet FOLLOW_exprNoComma_in_option895 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_memberExpr_in_exprNoComma1002 = new BitSet(new long[]{0x0000000000002002L});
	public static final BitSet FOLLOW_COLON_in_exprNoComma1008 = new BitSet(new long[]{0x0000000002104000L});
	public static final BitSet FOLLOW_mapTemplateRef_in_exprNoComma1010 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_mapExpr_in_expr1055 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_memberExpr_in_mapExpr1067 = new BitSet(new long[]{0x0000000000042002L});
	public static final BitSet FOLLOW_COMMA_in_mapExpr1076 = new BitSet(new long[]{0x0000001A06114100L});
	public static final BitSet FOLLOW_memberExpr_in_mapExpr1078 = new BitSet(new long[]{0x0000000000042000L});
	public static final BitSet FOLLOW_COLON_in_mapExpr1084 = new BitSet(new long[]{0x0000000002104000L});
	public static final BitSet FOLLOW_mapTemplateRef_in_mapExpr1086 = new BitSet(new long[]{0x0000000000002002L});
	public static final BitSet FOLLOW_COLON_in_mapExpr1149 = new BitSet(new long[]{0x0000000002104000L});
	public static final BitSet FOLLOW_mapTemplateRef_in_mapExpr1153 = new BitSet(new long[]{0x0000000000042002L});
	public static final BitSet FOLLOW_COMMA_in_mapExpr1159 = new BitSet(new long[]{0x0000000002104000L});
	public static final BitSet FOLLOW_mapTemplateRef_in_mapExpr1163 = new BitSet(new long[]{0x0000000000042002L});
	public static final BitSet FOLLOW_ID_in_mapTemplateRef1210 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_LPAREN_in_mapTemplateRef1212 = new BitSet(new long[]{0x0000001A0611C900L});
	public static final BitSet FOLLOW_args_in_mapTemplateRef1214 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_mapTemplateRef1216 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subtemplate_in_mapTemplateRef1238 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_mapTemplateRef1245 = new BitSet(new long[]{0x0000001A06114100L});
	public static final BitSet FOLLOW_mapExpr_in_mapTemplateRef1247 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_mapTemplateRef1251 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_LPAREN_in_mapTemplateRef1253 = new BitSet(new long[]{0x0000001A0611C100L});
	public static final BitSet FOLLOW_argExprList_in_mapTemplateRef1255 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_mapTemplateRef1258 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_includeExpr_in_memberExpr1281 = new BitSet(new long[]{0x0000000000080002L});
	public static final BitSet FOLLOW_DOT_in_memberExpr1292 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_ID_in_memberExpr1294 = new BitSet(new long[]{0x0000000000080002L});
	public static final BitSet FOLLOW_DOT_in_memberExpr1320 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_LPAREN_in_memberExpr1322 = new BitSet(new long[]{0x0000001A06114100L});
	public static final BitSet FOLLOW_mapExpr_in_memberExpr1324 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_memberExpr1326 = new BitSet(new long[]{0x0000000000080002L});
	public static final BitSet FOLLOW_ID_in_includeExpr1370 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_LPAREN_in_includeExpr1372 = new BitSet(new long[]{0x0000001A0611C100L});
	public static final BitSet FOLLOW_expr_in_includeExpr1374 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_includeExpr1377 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUPER_in_includeExpr1398 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_DOT_in_includeExpr1400 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_ID_in_includeExpr1402 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_LPAREN_in_includeExpr1404 = new BitSet(new long[]{0x0000001A0611C900L});
	public static final BitSet FOLLOW_args_in_includeExpr1406 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_includeExpr1408 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_includeExpr1427 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_LPAREN_in_includeExpr1429 = new BitSet(new long[]{0x0000001A0611C900L});
	public static final BitSet FOLLOW_args_in_includeExpr1431 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_includeExpr1433 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AT_in_includeExpr1455 = new BitSet(new long[]{0x0000000000000100L});
	public static final BitSet FOLLOW_SUPER_in_includeExpr1457 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_DOT_in_includeExpr1459 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_ID_in_includeExpr1461 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_LPAREN_in_includeExpr1463 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_includeExpr1467 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AT_in_includeExpr1482 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_ID_in_includeExpr1484 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_LPAREN_in_includeExpr1486 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_includeExpr1490 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primary_in_includeExpr1508 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_primary1519 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_in_primary1524 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TRUE_in_primary1529 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FALSE_in_primary1534 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subtemplate_in_primary1539 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_list_in_primary1544 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_primary1553 = new BitSet(new long[]{0x0000001A06114500L});
	public static final BitSet FOLLOW_conditional_in_primary1556 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_primary1558 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_primary1569 = new BitSet(new long[]{0x0000001A06114100L});
	public static final BitSet FOLLOW_expr_in_primary1571 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_primary1573 = new BitSet(new long[]{0x0000000000004002L});
	public static final BitSet FOLLOW_LPAREN_in_primary1579 = new BitSet(new long[]{0x0000001A0611C100L});
	public static final BitSet FOLLOW_argExprList_in_primary1581 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_RPAREN_in_primary1584 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_argExprList_in_args1640 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_namedArg_in_args1645 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_COMMA_in_args1649 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_namedArg_in_args1651 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_COMMA_in_args1657 = new BitSet(new long[]{0x0000000000000800L});
	public static final BitSet FOLLOW_ELLIPSIS_in_args1659 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ELLIPSIS_in_args1679 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arg_in_argExprList1692 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_COMMA_in_argExprList1696 = new BitSet(new long[]{0x0000001A06114100L});
	public static final BitSet FOLLOW_arg_in_argExprList1698 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_exprNoComma_in_arg1715 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_namedArg1724 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_EQUALS_in_namedArg1726 = new BitSet(new long[]{0x0000001A06114100L});
	public static final BitSet FOLLOW_arg_in_namedArg1728 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACK_in_list1753 = new BitSet(new long[]{0x0000000000020000L});
	public static final BitSet FOLLOW_RBRACK_in_list1755 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACK_in_list1767 = new BitSet(new long[]{0x0000001A06174100L});
	public static final BitSet FOLLOW_listElement_in_list1769 = new BitSet(new long[]{0x0000000000060000L});
	public static final BitSet FOLLOW_COMMA_in_list1773 = new BitSet(new long[]{0x0000001A06174100L});
	public static final BitSet FOLLOW_listElement_in_list1775 = new BitSet(new long[]{0x0000000000060000L});
	public static final BitSet FOLLOW_RBRACK_in_list1780 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_exprNoComma_in_listElement1800 = new BitSet(new long[]{0x0000000000000002L});
}
