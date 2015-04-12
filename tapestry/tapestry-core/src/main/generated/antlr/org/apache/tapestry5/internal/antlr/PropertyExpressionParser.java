// $ANTLR 3.5.2 C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g 2014-10-08 09:29:41

package org.apache.tapestry5.internal.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class PropertyExpressionParser extends org.apache.tapestry5.internal.antlr.BaseParser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "A", "BANG", "COLON", "COMMA", 
		"DECIMAL", "DEREF", "DIGIT", "E", "F", "FALSE", "H", "I", "IDENTIFIER", 
		"INTEGER", "JAVA_ID_PART", "JAVA_ID_START", "L", "LBRACE", "LBRACKET", 
		"LETTER", "LPAREN", "N", "NULL", "NUMBER_OR_RANGEOP", "QUOTE", "R", "RANGEOP", 
		"RBRACE", "RBRACKET", "RPAREN", "S", "SAFEDEREF", "SIGN", "STRING", "T", 
		"THIS", "TRUE", "U", "WS", "INVOKE", "LIST", "MAP", "NOT"
	};
	public static final int EOF=-1;
	public static final int A=4;
	public static final int BANG=5;
	public static final int COLON=6;
	public static final int COMMA=7;
	public static final int DECIMAL=8;
	public static final int DEREF=9;
	public static final int DIGIT=10;
	public static final int E=11;
	public static final int F=12;
	public static final int FALSE=13;
	public static final int H=14;
	public static final int I=15;
	public static final int IDENTIFIER=16;
	public static final int INTEGER=17;
	public static final int JAVA_ID_PART=18;
	public static final int JAVA_ID_START=19;
	public static final int L=20;
	public static final int LBRACE=21;
	public static final int LBRACKET=22;
	public static final int LETTER=23;
	public static final int LPAREN=24;
	public static final int N=25;
	public static final int NULL=26;
	public static final int NUMBER_OR_RANGEOP=27;
	public static final int QUOTE=28;
	public static final int R=29;
	public static final int RANGEOP=30;
	public static final int RBRACE=31;
	public static final int RBRACKET=32;
	public static final int RPAREN=33;
	public static final int S=34;
	public static final int SAFEDEREF=35;
	public static final int SIGN=36;
	public static final int STRING=37;
	public static final int T=38;
	public static final int THIS=39;
	public static final int TRUE=40;
	public static final int U=41;
	public static final int WS=42;
	public static final int INVOKE=43;
	public static final int LIST=44;
	public static final int MAP=45;
	public static final int NOT=46;

	// delegates
	public org.apache.tapestry5.internal.antlr.BaseParser[] getDelegates() {
		return new org.apache.tapestry5.internal.antlr.BaseParser[] {};
	}

	// delegators


	public PropertyExpressionParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public PropertyExpressionParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return PropertyExpressionParser.tokenNames; }
	@Override public String getGrammarFileName() { return "C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g"; }


	public static class start_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "start"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:45:1: start : expression ^ EOF !;
	public final PropertyExpressionParser.start_return start() throws RecognitionException {
		PropertyExpressionParser.start_return retval = new PropertyExpressionParser.start_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token EOF2=null;
		ParserRuleReturnScope expression1 =null;

		CommonTree EOF2_tree=null;

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:45:8: ( expression ^ EOF !)
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:45:10: expression ^ EOF !
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_expression_in_start130);
			expression1=expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) root_0 = (CommonTree)adaptor.becomeRoot(expression1.getTree(), root_0);
			EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_start133); if (state.failed) return retval;
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "start"


	public static class expression_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "expression"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:47:1: expression : ( keyword | rangeOp | constant | propertyChain | list | notOp | map );
	public final PropertyExpressionParser.expression_return expression() throws RecognitionException {
		PropertyExpressionParser.expression_return retval = new PropertyExpressionParser.expression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope keyword3 =null;
		ParserRuleReturnScope rangeOp4 =null;
		ParserRuleReturnScope constant5 =null;
		ParserRuleReturnScope propertyChain6 =null;
		ParserRuleReturnScope list7 =null;
		ParserRuleReturnScope notOp8 =null;
		ParserRuleReturnScope map9 =null;


		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:48:2: ( keyword | rangeOp | constant | propertyChain | list | notOp | map )
			int alt1=7;
			switch ( input.LA(1) ) {
			case FALSE:
			case NULL:
			case THIS:
			case TRUE:
				{
				alt1=1;
				}
				break;
			case INTEGER:
				{
				int LA1_2 = input.LA(2);
				if ( (synpred2_PropertyExpressionParser()) ) {
					alt1=2;
				}
				else if ( (synpred3_PropertyExpressionParser()) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case IDENTIFIER:
				{
				int LA1_3 = input.LA(2);
				if ( (synpred2_PropertyExpressionParser()) ) {
					alt1=2;
				}
				else if ( (synpred4_PropertyExpressionParser()) ) {
					alt1=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case DECIMAL:
			case STRING:
				{
				alt1=3;
				}
				break;
			case LBRACKET:
				{
				alt1=5;
				}
				break;
			case BANG:
				{
				alt1=6;
				}
				break;
			case LBRACE:
				{
				alt1=7;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 1, 0, input);
				throw nvae;
			}
			switch (alt1) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:48:4: keyword
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_keyword_in_expression145);
					keyword3=keyword();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, keyword3.getTree());

					}
					break;
				case 2 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:49:4: rangeOp
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_rangeOp_in_expression150);
					rangeOp4=rangeOp();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, rangeOp4.getTree());

					}
					break;
				case 3 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:50:4: constant
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_constant_in_expression155);
					constant5=constant();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, constant5.getTree());

					}
					break;
				case 4 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:51:4: propertyChain
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_propertyChain_in_expression160);
					propertyChain6=propertyChain();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, propertyChain6.getTree());

					}
					break;
				case 5 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:52:4: list
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_list_in_expression165);
					list7=list();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, list7.getTree());

					}
					break;
				case 6 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:53:4: notOp
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_notOp_in_expression170);
					notOp8=notOp();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, notOp8.getTree());

					}
					break;
				case 7 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:54:4: map
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_map_in_expression175);
					map9=map();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, map9.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "expression"


	public static class keyword_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "keyword"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:57:1: keyword : ( NULL | TRUE | FALSE | THIS );
	public final PropertyExpressionParser.keyword_return keyword() throws RecognitionException {
		PropertyExpressionParser.keyword_return retval = new PropertyExpressionParser.keyword_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token set10=null;

		CommonTree set10_tree=null;

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:57:9: ( NULL | TRUE | FALSE | THIS )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:
			{
			root_0 = (CommonTree)adaptor.nil();


			set10=input.LT(1);
			if ( input.LA(1)==FALSE||input.LA(1)==NULL||(input.LA(1) >= THIS && input.LA(1) <= TRUE) ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set10));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "keyword"


	public static class constant_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "constant"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:59:1: constant : ( INTEGER | DECIMAL | STRING );
	public final PropertyExpressionParser.constant_return constant() throws RecognitionException {
		PropertyExpressionParser.constant_return retval = new PropertyExpressionParser.constant_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token set11=null;

		CommonTree set11_tree=null;

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:59:9: ( INTEGER | DECIMAL | STRING )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:
			{
			root_0 = (CommonTree)adaptor.nil();


			set11=input.LT(1);
			if ( input.LA(1)==DECIMAL||input.LA(1)==INTEGER||input.LA(1)==STRING ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set11));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "constant"


	public static class propertyChain_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "propertyChain"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:61:1: propertyChain : ( term DEREF propertyChain -> ^( DEREF term propertyChain ) | term SAFEDEREF propertyChain -> ^( SAFEDEREF term propertyChain ) | term );
	public final PropertyExpressionParser.propertyChain_return propertyChain() throws RecognitionException {
		PropertyExpressionParser.propertyChain_return retval = new PropertyExpressionParser.propertyChain_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token DEREF13=null;
		Token SAFEDEREF16=null;
		ParserRuleReturnScope term12 =null;
		ParserRuleReturnScope propertyChain14 =null;
		ParserRuleReturnScope term15 =null;
		ParserRuleReturnScope propertyChain17 =null;
		ParserRuleReturnScope term18 =null;

		CommonTree DEREF13_tree=null;
		CommonTree SAFEDEREF16_tree=null;
		RewriteRuleTokenStream stream_DEREF=new RewriteRuleTokenStream(adaptor,"token DEREF");
		RewriteRuleTokenStream stream_SAFEDEREF=new RewriteRuleTokenStream(adaptor,"token SAFEDEREF");
		RewriteRuleSubtreeStream stream_propertyChain=new RewriteRuleSubtreeStream(adaptor,"rule propertyChain");
		RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:62:2: ( term DEREF propertyChain -> ^( DEREF term propertyChain ) | term SAFEDEREF propertyChain -> ^( SAFEDEREF term propertyChain ) | term )
			int alt2=3;
			int LA2_0 = input.LA(1);
			if ( (LA2_0==IDENTIFIER) ) {
				int LA2_1 = input.LA(2);
				if ( (synpred12_PropertyExpressionParser()) ) {
					alt2=1;
				}
				else if ( (synpred13_PropertyExpressionParser()) ) {
					alt2=2;
				}
				else if ( (true) ) {
					alt2=3;
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 2, 0, input);
				throw nvae;
			}

			switch (alt2) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:62:4: term DEREF propertyChain
					{
					pushFollow(FOLLOW_term_in_propertyChain222);
					term12=term();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_term.add(term12.getTree());
					DEREF13=(Token)match(input,DEREF,FOLLOW_DEREF_in_propertyChain224); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DEREF.add(DEREF13);

					pushFollow(FOLLOW_propertyChain_in_propertyChain226);
					propertyChain14=propertyChain();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_propertyChain.add(propertyChain14.getTree());
					// AST REWRITE
					// elements: propertyChain, DEREF, term
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 62:29: -> ^( DEREF term propertyChain )
					{
						// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:62:32: ^( DEREF term propertyChain )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot(stream_DEREF.nextNode(), root_1);
						adaptor.addChild(root_1, stream_term.nextTree());
						adaptor.addChild(root_1, stream_propertyChain.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:63:4: term SAFEDEREF propertyChain
					{
					pushFollow(FOLLOW_term_in_propertyChain241);
					term15=term();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_term.add(term15.getTree());
					SAFEDEREF16=(Token)match(input,SAFEDEREF,FOLLOW_SAFEDEREF_in_propertyChain243); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SAFEDEREF.add(SAFEDEREF16);

					pushFollow(FOLLOW_propertyChain_in_propertyChain245);
					propertyChain17=propertyChain();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_propertyChain.add(propertyChain17.getTree());
					// AST REWRITE
					// elements: term, SAFEDEREF, propertyChain
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 63:33: -> ^( SAFEDEREF term propertyChain )
					{
						// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:63:36: ^( SAFEDEREF term propertyChain )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot(stream_SAFEDEREF.nextNode(), root_1);
						adaptor.addChild(root_1, stream_term.nextTree());
						adaptor.addChild(root_1, stream_propertyChain.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:64:4: term
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_term_in_propertyChain260);
					term18=term();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, term18.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "propertyChain"


	public static class term_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "term"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:67:1: term : ( IDENTIFIER | methodInvocation );
	public final PropertyExpressionParser.term_return term() throws RecognitionException {
		PropertyExpressionParser.term_return retval = new PropertyExpressionParser.term_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER19=null;
		ParserRuleReturnScope methodInvocation20 =null;

		CommonTree IDENTIFIER19_tree=null;

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:67:6: ( IDENTIFIER | methodInvocation )
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==IDENTIFIER) ) {
				int LA3_1 = input.LA(2);
				if ( (LA3_1==LPAREN) ) {
					alt3=2;
				}
				else if ( (LA3_1==EOF||(LA3_1 >= COLON && LA3_1 <= COMMA)||LA3_1==DEREF||(LA3_1 >= RANGEOP && LA3_1 <= RPAREN)||LA3_1==SAFEDEREF) ) {
					alt3=1;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
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

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}

			switch (alt3) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:67:8: IDENTIFIER
					{
					root_0 = (CommonTree)adaptor.nil();


					IDENTIFIER19=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_term272); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					IDENTIFIER19_tree = (CommonTree)adaptor.create(IDENTIFIER19);
					adaptor.addChild(root_0, IDENTIFIER19_tree);
					}

					}
					break;
				case 2 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:68:4: methodInvocation
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_methodInvocation_in_term277);
					methodInvocation20=methodInvocation();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, methodInvocation20.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "term"


	public static class methodInvocation_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "methodInvocation"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:71:1: methodInvocation : (id= IDENTIFIER LPAREN RPAREN -> ^( INVOKE $id) |id= IDENTIFIER LPAREN expressionList RPAREN -> ^( INVOKE $id expressionList ) );
	public final PropertyExpressionParser.methodInvocation_return methodInvocation() throws RecognitionException {
		PropertyExpressionParser.methodInvocation_return retval = new PropertyExpressionParser.methodInvocation_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token id=null;
		Token LPAREN21=null;
		Token RPAREN22=null;
		Token LPAREN23=null;
		Token RPAREN25=null;
		ParserRuleReturnScope expressionList24 =null;

		CommonTree id_tree=null;
		CommonTree LPAREN21_tree=null;
		CommonTree RPAREN22_tree=null;
		CommonTree LPAREN23_tree=null;
		CommonTree RPAREN25_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_expressionList=new RewriteRuleSubtreeStream(adaptor,"rule expressionList");

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:72:2: (id= IDENTIFIER LPAREN RPAREN -> ^( INVOKE $id) |id= IDENTIFIER LPAREN expressionList RPAREN -> ^( INVOKE $id expressionList ) )
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==IDENTIFIER) ) {
				int LA4_1 = input.LA(2);
				if ( (LA4_1==LPAREN) ) {
					int LA4_2 = input.LA(3);
					if ( (LA4_2==RPAREN) ) {
						alt4=1;
					}
					else if ( (LA4_2==BANG||LA4_2==DECIMAL||LA4_2==FALSE||(LA4_2 >= IDENTIFIER && LA4_2 <= INTEGER)||(LA4_2 >= LBRACE && LA4_2 <= LBRACKET)||LA4_2==NULL||LA4_2==STRING||(LA4_2 >= THIS && LA4_2 <= TRUE)) ) {
						alt4=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 4, 2, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 4, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 4, 0, input);
				throw nvae;
			}

			switch (alt4) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:72:4: id= IDENTIFIER LPAREN RPAREN
					{
					id=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_methodInvocation291); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_IDENTIFIER.add(id);

					LPAREN21=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_methodInvocation293); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN21);

					RPAREN22=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_methodInvocation295); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN22);

					// AST REWRITE
					// elements: id
					// token labels: id
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleTokenStream stream_id=new RewriteRuleTokenStream(adaptor,"token id",id);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 72:32: -> ^( INVOKE $id)
					{
						// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:72:35: ^( INVOKE $id)
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INVOKE, "INVOKE"), root_1);
						adaptor.addChild(root_1, stream_id.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:73:4: id= IDENTIFIER LPAREN expressionList RPAREN
					{
					id=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_methodInvocation311); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_IDENTIFIER.add(id);

					LPAREN23=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_methodInvocation313); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN23);

					pushFollow(FOLLOW_expressionList_in_methodInvocation315);
					expressionList24=expressionList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_expressionList.add(expressionList24.getTree());
					RPAREN25=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_methodInvocation317); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN25);

					// AST REWRITE
					// elements: id, expressionList
					// token labels: id
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleTokenStream stream_id=new RewriteRuleTokenStream(adaptor,"token id",id);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 73:47: -> ^( INVOKE $id expressionList )
					{
						// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:73:50: ^( INVOKE $id expressionList )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INVOKE, "INVOKE"), root_1);
						adaptor.addChild(root_1, stream_id.nextNode());
						adaptor.addChild(root_1, stream_expressionList.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "methodInvocation"


	public static class expressionList_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "expressionList"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:76:1: expressionList : expression ( COMMA ! expression )* ;
	public final PropertyExpressionParser.expressionList_return expressionList() throws RecognitionException {
		PropertyExpressionParser.expressionList_return retval = new PropertyExpressionParser.expressionList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token COMMA27=null;
		ParserRuleReturnScope expression26 =null;
		ParserRuleReturnScope expression28 =null;

		CommonTree COMMA27_tree=null;

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:77:2: ( expression ( COMMA ! expression )* )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:77:4: expression ( COMMA ! expression )*
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_expression_in_expressionList341);
			expression26=expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, expression26.getTree());

			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:77:15: ( COMMA ! expression )*
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( (LA5_0==COMMA) ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:77:16: COMMA ! expression
					{
					COMMA27=(Token)match(input,COMMA,FOLLOW_COMMA_in_expressionList344); if (state.failed) return retval;
					pushFollow(FOLLOW_expression_in_expressionList347);
					expression28=expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, expression28.getTree());

					}
					break;

				default :
					break loop5;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "expressionList"


	public static class rangeOp_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "rangeOp"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:80:1: rangeOp : from= rangeopArg RANGEOP to= rangeopArg -> ^( RANGEOP $from $to) ;
	public final PropertyExpressionParser.rangeOp_return rangeOp() throws RecognitionException {
		PropertyExpressionParser.rangeOp_return retval = new PropertyExpressionParser.rangeOp_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token RANGEOP29=null;
		ParserRuleReturnScope from =null;
		ParserRuleReturnScope to =null;

		CommonTree RANGEOP29_tree=null;
		RewriteRuleTokenStream stream_RANGEOP=new RewriteRuleTokenStream(adaptor,"token RANGEOP");
		RewriteRuleSubtreeStream stream_rangeopArg=new RewriteRuleSubtreeStream(adaptor,"rule rangeopArg");

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:81:2: (from= rangeopArg RANGEOP to= rangeopArg -> ^( RANGEOP $from $to) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:81:4: from= rangeopArg RANGEOP to= rangeopArg
			{
			pushFollow(FOLLOW_rangeopArg_in_rangeOp363);
			from=rangeopArg();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_rangeopArg.add(from.getTree());
			RANGEOP29=(Token)match(input,RANGEOP,FOLLOW_RANGEOP_in_rangeOp366); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RANGEOP.add(RANGEOP29);

			pushFollow(FOLLOW_rangeopArg_in_rangeOp370);
			to=rangeopArg();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_rangeopArg.add(to.getTree());
			// AST REWRITE
			// elements: to, from, RANGEOP
			// token labels: 
			// rule labels: to, retval, from
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_to=new RewriteRuleSubtreeStream(adaptor,"rule to",to!=null?to.getTree():null);
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
			RewriteRuleSubtreeStream stream_from=new RewriteRuleSubtreeStream(adaptor,"rule from",from!=null?from.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 81:43: -> ^( RANGEOP $from $to)
			{
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:81:46: ^( RANGEOP $from $to)
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot(stream_RANGEOP.nextNode(), root_1);
				adaptor.addChild(root_1, stream_from.nextTree());
				adaptor.addChild(root_1, stream_to.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "rangeOp"


	public static class rangeopArg_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "rangeopArg"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:84:1: rangeopArg : ( INTEGER | propertyChain );
	public final PropertyExpressionParser.rangeopArg_return rangeopArg() throws RecognitionException {
		PropertyExpressionParser.rangeopArg_return retval = new PropertyExpressionParser.rangeopArg_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INTEGER30=null;
		ParserRuleReturnScope propertyChain31 =null;

		CommonTree INTEGER30_tree=null;

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:85:2: ( INTEGER | propertyChain )
			int alt6=2;
			int LA6_0 = input.LA(1);
			if ( (LA6_0==INTEGER) ) {
				alt6=1;
			}
			else if ( (LA6_0==IDENTIFIER) ) {
				alt6=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 6, 0, input);
				throw nvae;
			}

			switch (alt6) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:85:4: INTEGER
					{
					root_0 = (CommonTree)adaptor.nil();


					INTEGER30=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_rangeopArg396); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					INTEGER30_tree = (CommonTree)adaptor.create(INTEGER30);
					adaptor.addChild(root_0, INTEGER30_tree);
					}

					}
					break;
				case 2 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:86:4: propertyChain
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_propertyChain_in_rangeopArg401);
					propertyChain31=propertyChain();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, propertyChain31.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "rangeopArg"


	public static class list_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "list"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:89:1: list : ( LBRACKET RBRACKET -> ^( LIST ) | LBRACKET expressionList RBRACKET -> ^( LIST expressionList ) );
	public final PropertyExpressionParser.list_return list() throws RecognitionException {
		PropertyExpressionParser.list_return retval = new PropertyExpressionParser.list_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LBRACKET32=null;
		Token RBRACKET33=null;
		Token LBRACKET34=null;
		Token RBRACKET36=null;
		ParserRuleReturnScope expressionList35 =null;

		CommonTree LBRACKET32_tree=null;
		CommonTree RBRACKET33_tree=null;
		CommonTree LBRACKET34_tree=null;
		CommonTree RBRACKET36_tree=null;
		RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
		RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
		RewriteRuleSubtreeStream stream_expressionList=new RewriteRuleSubtreeStream(adaptor,"rule expressionList");

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:89:6: ( LBRACKET RBRACKET -> ^( LIST ) | LBRACKET expressionList RBRACKET -> ^( LIST expressionList ) )
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0==LBRACKET) ) {
				int LA7_1 = input.LA(2);
				if ( (LA7_1==RBRACKET) ) {
					alt7=1;
				}
				else if ( (LA7_1==BANG||LA7_1==DECIMAL||LA7_1==FALSE||(LA7_1 >= IDENTIFIER && LA7_1 <= INTEGER)||(LA7_1 >= LBRACE && LA7_1 <= LBRACKET)||LA7_1==NULL||LA7_1==STRING||(LA7_1 >= THIS && LA7_1 <= TRUE)) ) {
					alt7=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 7, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 7, 0, input);
				throw nvae;
			}

			switch (alt7) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:89:8: LBRACKET RBRACKET
					{
					LBRACKET32=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_list413); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET32);

					RBRACKET33=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_list415); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET33);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 89:26: -> ^( LIST )
					{
						// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:89:29: ^( LIST )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:90:4: LBRACKET expressionList RBRACKET
					{
					LBRACKET34=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_list426); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET34);

					pushFollow(FOLLOW_expressionList_in_list428);
					expressionList35=expressionList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_expressionList.add(expressionList35.getTree());
					RBRACKET36=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_list430); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET36);

					// AST REWRITE
					// elements: expressionList
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 90:37: -> ^( LIST expressionList )
					{
						// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:90:40: ^( LIST expressionList )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);
						adaptor.addChild(root_1, stream_expressionList.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "list"


	public static class notOp_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "notOp"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:93:1: notOp : BANG expression -> ^( NOT expression ) ;
	public final PropertyExpressionParser.notOp_return notOp() throws RecognitionException {
		PropertyExpressionParser.notOp_return retval = new PropertyExpressionParser.notOp_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token BANG37=null;
		ParserRuleReturnScope expression38 =null;

		CommonTree BANG37_tree=null;
		RewriteRuleTokenStream stream_BANG=new RewriteRuleTokenStream(adaptor,"token BANG");
		RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:93:8: ( BANG expression -> ^( NOT expression ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:93:10: BANG expression
			{
			BANG37=(Token)match(input,BANG,FOLLOW_BANG_in_notOp451); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_BANG.add(BANG37);

			pushFollow(FOLLOW_expression_in_notOp453);
			expression38=expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_expression.add(expression38.getTree());
			// AST REWRITE
			// elements: expression
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 93:26: -> ^( NOT expression )
			{
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:93:29: ^( NOT expression )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NOT, "NOT"), root_1);
				adaptor.addChild(root_1, stream_expression.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "notOp"


	public static class map_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "map"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:96:1: map : ( LBRACE RBRACE -> ^( MAP ) | LBRACE mapEntryList RBRACE -> ^( MAP mapEntryList ) );
	public final PropertyExpressionParser.map_return map() throws RecognitionException {
		PropertyExpressionParser.map_return retval = new PropertyExpressionParser.map_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LBRACE39=null;
		Token RBRACE40=null;
		Token LBRACE41=null;
		Token RBRACE43=null;
		ParserRuleReturnScope mapEntryList42 =null;

		CommonTree LBRACE39_tree=null;
		CommonTree RBRACE40_tree=null;
		CommonTree LBRACE41_tree=null;
		CommonTree RBRACE43_tree=null;
		RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
		RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
		RewriteRuleSubtreeStream stream_mapEntryList=new RewriteRuleSubtreeStream(adaptor,"rule mapEntryList");

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:96:6: ( LBRACE RBRACE -> ^( MAP ) | LBRACE mapEntryList RBRACE -> ^( MAP mapEntryList ) )
			int alt8=2;
			int LA8_0 = input.LA(1);
			if ( (LA8_0==LBRACE) ) {
				int LA8_1 = input.LA(2);
				if ( (LA8_1==RBRACE) ) {
					alt8=1;
				}
				else if ( (LA8_1==DECIMAL||LA8_1==FALSE||(LA8_1 >= IDENTIFIER && LA8_1 <= INTEGER)||LA8_1==NULL||LA8_1==STRING||(LA8_1 >= THIS && LA8_1 <= TRUE)) ) {
					alt8=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 8, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}

			switch (alt8) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:96:8: LBRACE RBRACE
					{
					LBRACE39=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_map472); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE39);

					RBRACE40=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_map474); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE40);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 96:22: -> ^( MAP )
					{
						// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:96:25: ^( MAP )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MAP, "MAP"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:97:4: LBRACE mapEntryList RBRACE
					{
					LBRACE41=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_map485); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE41);

					pushFollow(FOLLOW_mapEntryList_in_map487);
					mapEntryList42=mapEntryList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_mapEntryList.add(mapEntryList42.getTree());
					RBRACE43=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_map489); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE43);

					// AST REWRITE
					// elements: mapEntryList
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 97:31: -> ^( MAP mapEntryList )
					{
						// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:97:34: ^( MAP mapEntryList )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MAP, "MAP"), root_1);
						adaptor.addChild(root_1, stream_mapEntryList.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "map"


	public static class mapEntryList_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "mapEntryList"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:100:1: mapEntryList : mapEntry ( COMMA ! mapEntry )* ;
	public final PropertyExpressionParser.mapEntryList_return mapEntryList() throws RecognitionException {
		PropertyExpressionParser.mapEntryList_return retval = new PropertyExpressionParser.mapEntryList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token COMMA45=null;
		ParserRuleReturnScope mapEntry44 =null;
		ParserRuleReturnScope mapEntry46 =null;

		CommonTree COMMA45_tree=null;

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:100:14: ( mapEntry ( COMMA ! mapEntry )* )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:100:16: mapEntry ( COMMA ! mapEntry )*
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_mapEntry_in_mapEntryList511);
			mapEntry44=mapEntry();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, mapEntry44.getTree());

			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:100:25: ( COMMA ! mapEntry )*
			loop9:
			while (true) {
				int alt9=2;
				int LA9_0 = input.LA(1);
				if ( (LA9_0==COMMA) ) {
					alt9=1;
				}

				switch (alt9) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:100:26: COMMA ! mapEntry
					{
					COMMA45=(Token)match(input,COMMA,FOLLOW_COMMA_in_mapEntryList514); if (state.failed) return retval;
					pushFollow(FOLLOW_mapEntry_in_mapEntryList517);
					mapEntry46=mapEntry();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, mapEntry46.getTree());

					}
					break;

				default :
					break loop9;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "mapEntryList"


	public static class mapEntry_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "mapEntry"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:102:1: mapEntry : mapKey COLON ! expression ;
	public final PropertyExpressionParser.mapEntry_return mapEntry() throws RecognitionException {
		PropertyExpressionParser.mapEntry_return retval = new PropertyExpressionParser.mapEntry_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token COLON48=null;
		ParserRuleReturnScope mapKey47 =null;
		ParserRuleReturnScope expression49 =null;

		CommonTree COLON48_tree=null;

		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:102:10: ( mapKey COLON ! expression )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:102:13: mapKey COLON ! expression
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_mapKey_in_mapEntry528);
			mapKey47=mapKey();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, mapKey47.getTree());

			COLON48=(Token)match(input,COLON,FOLLOW_COLON_in_mapEntry530); if (state.failed) return retval;
			pushFollow(FOLLOW_expression_in_mapEntry533);
			expression49=expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, expression49.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "mapEntry"


	public static class mapKey_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "mapKey"
	// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:104:1: mapKey : ( keyword | constant | propertyChain );
	public final PropertyExpressionParser.mapKey_return mapKey() throws RecognitionException {
		PropertyExpressionParser.mapKey_return retval = new PropertyExpressionParser.mapKey_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope keyword50 =null;
		ParserRuleReturnScope constant51 =null;
		ParserRuleReturnScope propertyChain52 =null;


		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:104:8: ( keyword | constant | propertyChain )
			int alt10=3;
			switch ( input.LA(1) ) {
			case FALSE:
			case NULL:
			case THIS:
			case TRUE:
				{
				alt10=1;
				}
				break;
			case DECIMAL:
			case INTEGER:
			case STRING:
				{
				alt10=2;
				}
				break;
			case IDENTIFIER:
				{
				alt10=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 10, 0, input);
				throw nvae;
			}
			switch (alt10) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:104:10: keyword
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_keyword_in_mapKey542);
					keyword50=keyword();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, keyword50.getTree());

					}
					break;
				case 2 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:104:20: constant
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_constant_in_mapKey546);
					constant51=constant();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, constant51.getTree());

					}
					break;
				case 3 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:104:31: propertyChain
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_propertyChain_in_mapKey550);
					propertyChain52=propertyChain();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, propertyChain52.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "mapKey"

	// $ANTLR start synpred2_PropertyExpressionParser
	public final void synpred2_PropertyExpressionParser_fragment() throws RecognitionException {
		// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:49:4: ( rangeOp )
		// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:49:4: rangeOp
		{
		pushFollow(FOLLOW_rangeOp_in_synpred2_PropertyExpressionParser150);
		rangeOp();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred2_PropertyExpressionParser

	// $ANTLR start synpred3_PropertyExpressionParser
	public final void synpred3_PropertyExpressionParser_fragment() throws RecognitionException {
		// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:50:4: ( constant )
		// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:50:4: constant
		{
		pushFollow(FOLLOW_constant_in_synpred3_PropertyExpressionParser155);
		constant();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred3_PropertyExpressionParser

	// $ANTLR start synpred4_PropertyExpressionParser
	public final void synpred4_PropertyExpressionParser_fragment() throws RecognitionException {
		// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:51:4: ( propertyChain )
		// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:51:4: propertyChain
		{
		pushFollow(FOLLOW_propertyChain_in_synpred4_PropertyExpressionParser160);
		propertyChain();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred4_PropertyExpressionParser

	// $ANTLR start synpred12_PropertyExpressionParser
	public final void synpred12_PropertyExpressionParser_fragment() throws RecognitionException {
		// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:62:4: ( term DEREF propertyChain )
		// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:62:4: term DEREF propertyChain
		{
		pushFollow(FOLLOW_term_in_synpred12_PropertyExpressionParser222);
		term();
		state._fsp--;
		if (state.failed) return;

		match(input,DEREF,FOLLOW_DEREF_in_synpred12_PropertyExpressionParser224); if (state.failed) return;

		pushFollow(FOLLOW_propertyChain_in_synpred12_PropertyExpressionParser226);
		propertyChain();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred12_PropertyExpressionParser

	// $ANTLR start synpred13_PropertyExpressionParser
	public final void synpred13_PropertyExpressionParser_fragment() throws RecognitionException {
		// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:63:4: ( term SAFEDEREF propertyChain )
		// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionParser.g:63:4: term SAFEDEREF propertyChain
		{
		pushFollow(FOLLOW_term_in_synpred13_PropertyExpressionParser241);
		term();
		state._fsp--;
		if (state.failed) return;

		match(input,SAFEDEREF,FOLLOW_SAFEDEREF_in_synpred13_PropertyExpressionParser243); if (state.failed) return;

		pushFollow(FOLLOW_propertyChain_in_synpred13_PropertyExpressionParser245);
		propertyChain();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred13_PropertyExpressionParser

	// Delegated rules

	public final boolean synpred4_PropertyExpressionParser() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred4_PropertyExpressionParser_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred12_PropertyExpressionParser() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred12_PropertyExpressionParser_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred13_PropertyExpressionParser() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred13_PropertyExpressionParser_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred3_PropertyExpressionParser() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred3_PropertyExpressionParser_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred2_PropertyExpressionParser() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred2_PropertyExpressionParser_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}



	public static final BitSet FOLLOW_expression_in_start130 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_start133 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_keyword_in_expression145 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_rangeOp_in_expression150 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_constant_in_expression155 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_propertyChain_in_expression160 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_list_in_expression165 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_notOp_in_expression170 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_map_in_expression175 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_propertyChain222 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_DEREF_in_propertyChain224 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_propertyChain_in_propertyChain226 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_propertyChain241 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_SAFEDEREF_in_propertyChain243 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_propertyChain_in_propertyChain245 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_propertyChain260 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_term272 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_methodInvocation_in_term277 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_methodInvocation291 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_LPAREN_in_methodInvocation293 = new BitSet(new long[]{0x0000000200000000L});
	public static final BitSet FOLLOW_RPAREN_in_methodInvocation295 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_methodInvocation311 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_LPAREN_in_methodInvocation313 = new BitSet(new long[]{0x000001A004632120L});
	public static final BitSet FOLLOW_expressionList_in_methodInvocation315 = new BitSet(new long[]{0x0000000200000000L});
	public static final BitSet FOLLOW_RPAREN_in_methodInvocation317 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expression_in_expressionList341 = new BitSet(new long[]{0x0000000000000082L});
	public static final BitSet FOLLOW_COMMA_in_expressionList344 = new BitSet(new long[]{0x000001A004632120L});
	public static final BitSet FOLLOW_expression_in_expressionList347 = new BitSet(new long[]{0x0000000000000082L});
	public static final BitSet FOLLOW_rangeopArg_in_rangeOp363 = new BitSet(new long[]{0x0000000040000000L});
	public static final BitSet FOLLOW_RANGEOP_in_rangeOp366 = new BitSet(new long[]{0x0000000000030000L});
	public static final BitSet FOLLOW_rangeopArg_in_rangeOp370 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_rangeopArg396 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_propertyChain_in_rangeopArg401 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_list413 = new BitSet(new long[]{0x0000000100000000L});
	public static final BitSet FOLLOW_RBRACKET_in_list415 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_list426 = new BitSet(new long[]{0x000001A004632120L});
	public static final BitSet FOLLOW_expressionList_in_list428 = new BitSet(new long[]{0x0000000100000000L});
	public static final BitSet FOLLOW_RBRACKET_in_list430 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BANG_in_notOp451 = new BitSet(new long[]{0x000001A004632120L});
	public static final BitSet FOLLOW_expression_in_notOp453 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_map472 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_RBRACE_in_map474 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_map485 = new BitSet(new long[]{0x000001A004032100L});
	public static final BitSet FOLLOW_mapEntryList_in_map487 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_RBRACE_in_map489 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_mapEntry_in_mapEntryList511 = new BitSet(new long[]{0x0000000000000082L});
	public static final BitSet FOLLOW_COMMA_in_mapEntryList514 = new BitSet(new long[]{0x000001A004032100L});
	public static final BitSet FOLLOW_mapEntry_in_mapEntryList517 = new BitSet(new long[]{0x0000000000000082L});
	public static final BitSet FOLLOW_mapKey_in_mapEntry528 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_COLON_in_mapEntry530 = new BitSet(new long[]{0x000001A004632120L});
	public static final BitSet FOLLOW_expression_in_mapEntry533 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_keyword_in_mapKey542 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_constant_in_mapKey546 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_propertyChain_in_mapKey550 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_rangeOp_in_synpred2_PropertyExpressionParser150 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_constant_in_synpred3_PropertyExpressionParser155 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_propertyChain_in_synpred4_PropertyExpressionParser160 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_synpred12_PropertyExpressionParser222 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_DEREF_in_synpred12_PropertyExpressionParser224 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_propertyChain_in_synpred12_PropertyExpressionParser226 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_synpred13_PropertyExpressionParser241 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_SAFEDEREF_in_synpred13_PropertyExpressionParser243 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_propertyChain_in_synpred13_PropertyExpressionParser245 = new BitSet(new long[]{0x0000000000000002L});
}
