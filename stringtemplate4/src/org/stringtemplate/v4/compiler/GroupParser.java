// $ANTLR 3.5.2 Group.g 2015-02-20 23:48:10

/*
 * [The "BSD license"]
 *  Copyright (c) 2011 Terence Parr
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.stringtemplate.v4.compiler;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import org.stringtemplate.v4.misc.*;
import org.stringtemplate.v4.*;
import java.io.File;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class GroupParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ANONYMOUS_TEMPLATE", "BIGSTRING", 
		"BIGSTRING_NO_NL", "COMMENT", "FALSE", "ID", "LBRACK", "LINE_COMMENT", 
		"RBRACK", "STRING", "TRUE", "WS", "'('", "')'", "','", "'.'", "':'", "'::='", 
		"';'", "'='", "'@'", "'default'", "'delimiters'", "'group'", "'implements'", 
		"'import'"
	};
	public static final int EOF=-1;
	public static final int T__16=16;
	public static final int T__17=17;
	public static final int T__18=18;
	public static final int T__19=19;
	public static final int T__20=20;
	public static final int T__21=21;
	public static final int T__22=22;
	public static final int T__23=23;
	public static final int T__24=24;
	public static final int T__25=25;
	public static final int T__26=26;
	public static final int T__27=27;
	public static final int T__28=28;
	public static final int T__29=29;
	public static final int ANONYMOUS_TEMPLATE=4;
	public static final int BIGSTRING=5;
	public static final int BIGSTRING_NO_NL=6;
	public static final int COMMENT=7;
	public static final int FALSE=8;
	public static final int ID=9;
	public static final int LBRACK=10;
	public static final int LINE_COMMENT=11;
	public static final int RBRACK=12;
	public static final int STRING=13;
	public static final int TRUE=14;
	public static final int WS=15;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public GroupParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public GroupParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return GroupParser.tokenNames; }
	@Override public String getGrammarFileName() { return "Group.g"; }


	public STGroup group;

	@Override
	public void displayRecognitionError(String[] tokenNames,
	                                    RecognitionException e)
	{
	    String msg = getErrorMessage(e, tokenNames);
	    group.errMgr.groupSyntaxError(ErrorType.SYNTAX_ERROR, getSourceName(), e, msg);
	}
	@Override
	public String getSourceName() {
	    String fullFileName = super.getSourceName();
	    File f = new File(fullFileName); // strip to simple name
	    return f.getName();
	}
	public void error(String msg) {
	    NoViableAltException e = new NoViableAltException("", 0, 0, input);
	    group.errMgr.groupSyntaxError(ErrorType.SYNTAX_ERROR, getSourceName(), e, msg);
	    recover(input, null);
	}



	// $ANTLR start "group"
	// Group.g:160:1: group[STGroup group, String prefix] : ( oldStyleHeader )? ( delimiters )? ( 'import' STRING | 'import' ID ( '.' ID )* )* ( def[prefix] )* EOF ;
	public final void group(STGroup group, String prefix) throws RecognitionException {
		Token STRING1=null;


		GroupLexer lexer = (GroupLexer)input.getTokenSource();
		this.group = lexer.group = group;

		try {
			// Group.g:165:2: ( ( oldStyleHeader )? ( delimiters )? ( 'import' STRING | 'import' ID ( '.' ID )* )* ( def[prefix] )* EOF )
			// Group.g:165:4: ( oldStyleHeader )? ( delimiters )? ( 'import' STRING | 'import' ID ( '.' ID )* )* ( def[prefix] )* EOF
			{
			// Group.g:165:4: ( oldStyleHeader )?
			int alt1=2;
			int LA1_0 = input.LA(1);
			if ( (LA1_0==27) ) {
				alt1=1;
			}
			switch (alt1) {
				case 1 :
					// Group.g:165:4: oldStyleHeader
					{
					pushFollow(FOLLOW_oldStyleHeader_in_group86);
					oldStyleHeader();
					state._fsp--;

					}
					break;

			}

			// Group.g:166:3: ( delimiters )?
			int alt2=2;
			int LA2_0 = input.LA(1);
			if ( (LA2_0==26) ) {
				alt2=1;
			}
			switch (alt2) {
				case 1 :
					// Group.g:166:3: delimiters
					{
					pushFollow(FOLLOW_delimiters_in_group91);
					delimiters();
					state._fsp--;

					}
					break;

			}

			// Group.g:167:6: ( 'import' STRING | 'import' ID ( '.' ID )* )*
			loop4:
			while (true) {
				int alt4=3;
				int LA4_0 = input.LA(1);
				if ( (LA4_0==29) ) {
					int LA4_2 = input.LA(2);
					if ( (LA4_2==STRING) ) {
						alt4=1;
					}
					else if ( (LA4_2==ID) ) {
						alt4=2;
					}

				}

				switch (alt4) {
				case 1 :
					// Group.g:167:8: 'import' STRING
					{
					match(input,29,FOLLOW_29_in_group101); 
					STRING1=(Token)match(input,STRING,FOLLOW_STRING_in_group103); 
					group.importTemplates(STRING1);
					}
					break;
				case 2 :
					// Group.g:168:5: 'import' ID ( '.' ID )*
					{
					match(input,29,FOLLOW_29_in_group111); 

								MismatchedTokenException e = new MismatchedTokenException(STRING, input);
								reportError(e);
								
					match(input,ID,FOLLOW_ID_in_group122); 
					// Group.g:173:7: ( '.' ID )*
					loop3:
					while (true) {
						int alt3=2;
						int LA3_0 = input.LA(1);
						if ( (LA3_0==19) ) {
							alt3=1;
						}

						switch (alt3) {
						case 1 :
							// Group.g:173:8: '.' ID
							{
							match(input,19,FOLLOW_19_in_group125); 
							match(input,ID,FOLLOW_ID_in_group127); 
							}
							break;

						default :
							break loop3;
						}
					}

					}
					break;

				default :
					break loop4;
				}
			}

			// Group.g:175:3: ( def[prefix] )*
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( (LA5_0==ID||LA5_0==24) ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// Group.g:175:3: def[prefix]
					{
					pushFollow(FOLLOW_def_in_group139);
					def(prefix);
					state._fsp--;

					}
					break;

				default :
					break loop5;
				}
			}

			match(input,EOF,FOLLOW_EOF_in_group145); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "group"



	// $ANTLR start "oldStyleHeader"
	// Group.g:179:1: oldStyleHeader : 'group' ID ( ':' ID )? ( 'implements' ID ( ',' ID )* )? ';' ;
	public final void oldStyleHeader() throws RecognitionException {
		try {
			// Group.g:180:5: ( 'group' ID ( ':' ID )? ( 'implements' ID ( ',' ID )* )? ';' )
			// Group.g:180:9: 'group' ID ( ':' ID )? ( 'implements' ID ( ',' ID )* )? ';'
			{
			match(input,27,FOLLOW_27_in_oldStyleHeader162); 
			match(input,ID,FOLLOW_ID_in_oldStyleHeader164); 
			// Group.g:180:20: ( ':' ID )?
			int alt6=2;
			int LA6_0 = input.LA(1);
			if ( (LA6_0==20) ) {
				alt6=1;
			}
			switch (alt6) {
				case 1 :
					// Group.g:180:22: ':' ID
					{
					match(input,20,FOLLOW_20_in_oldStyleHeader168); 
					match(input,ID,FOLLOW_ID_in_oldStyleHeader170); 
					}
					break;

			}

			// Group.g:181:6: ( 'implements' ID ( ',' ID )* )?
			int alt8=2;
			int LA8_0 = input.LA(1);
			if ( (LA8_0==28) ) {
				alt8=1;
			}
			switch (alt8) {
				case 1 :
					// Group.g:181:8: 'implements' ID ( ',' ID )*
					{
					match(input,28,FOLLOW_28_in_oldStyleHeader182); 
					match(input,ID,FOLLOW_ID_in_oldStyleHeader184); 
					// Group.g:181:24: ( ',' ID )*
					loop7:
					while (true) {
						int alt7=2;
						int LA7_0 = input.LA(1);
						if ( (LA7_0==18) ) {
							alt7=1;
						}

						switch (alt7) {
						case 1 :
							// Group.g:181:25: ',' ID
							{
							match(input,18,FOLLOW_18_in_oldStyleHeader187); 
							match(input,ID,FOLLOW_ID_in_oldStyleHeader189); 
							}
							break;

						default :
							break loop7;
						}
					}

					}
					break;

			}

			match(input,22,FOLLOW_22_in_oldStyleHeader201); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "oldStyleHeader"



	// $ANTLR start "groupName"
	// Group.g:185:1: groupName returns [String name] : a= ID ( '.' a= ID )* ;
	public final String groupName() throws RecognitionException {
		String name = null;


		Token a=null;

		StringBuilder buf = new StringBuilder();
		try {
			// Group.g:187:2: (a= ID ( '.' a= ID )* )
			// Group.g:187:4: a= ID ( '.' a= ID )*
			{
			a=(Token)match(input,ID,FOLLOW_ID_in_groupName223); 
			buf.append((a!=null?a.getText():null));
			// Group.g:187:32: ( '.' a= ID )*
			loop9:
			while (true) {
				int alt9=2;
				int LA9_0 = input.LA(1);
				if ( (LA9_0==19) ) {
					alt9=1;
				}

				switch (alt9) {
				case 1 :
					// Group.g:187:33: '.' a= ID
					{
					match(input,19,FOLLOW_19_in_groupName228); 
					a=(Token)match(input,ID,FOLLOW_ID_in_groupName232); 
					buf.append((a!=null?a.getText():null));
					}
					break;

				default :
					break loop9;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return name;
	}
	// $ANTLR end "groupName"



	// $ANTLR start "delimiters"
	// Group.g:190:1: delimiters : 'delimiters' a= STRING ',' b= STRING ;
	public final void delimiters() throws RecognitionException {
		Token a=null;
		Token b=null;

		try {
			// Group.g:191:5: ( 'delimiters' a= STRING ',' b= STRING )
			// Group.g:191:7: 'delimiters' a= STRING ',' b= STRING
			{
			match(input,26,FOLLOW_26_in_delimiters250); 
			a=(Token)match(input,STRING,FOLLOW_STRING_in_delimiters254); 
			match(input,18,FOLLOW_18_in_delimiters256); 
			b=(Token)match(input,STRING,FOLLOW_STRING_in_delimiters260); 

			     	group.delimiterStartChar=a.getText().charAt(1);
			        group.delimiterStopChar=b.getText().charAt(1);
			        
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "delimiters"



	// $ANTLR start "def"
	// Group.g:202:1: def[String prefix] : ( templateDef[prefix] | dictDef );
	public final void def(String prefix) throws RecognitionException {
		try {
			// Group.g:202:20: ( templateDef[prefix] | dictDef )
			int alt10=2;
			int LA10_0 = input.LA(1);
			if ( (LA10_0==24) ) {
				alt10=1;
			}
			else if ( (LA10_0==ID) ) {
				int LA10_2 = input.LA(2);
				if ( (LA10_2==16) ) {
					alt10=1;
				}
				else if ( (LA10_2==21) ) {
					int LA10_3 = input.LA(3);
					if ( (LA10_3==ID) ) {
						alt10=1;
					}
					else if ( (LA10_3==LBRACK) ) {
						alt10=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 10, 3, input);
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
							new NoViableAltException("", 10, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 10, 0, input);
				throw nvae;
			}

			switch (alt10) {
				case 1 :
					// Group.g:202:22: templateDef[prefix]
					{
					pushFollow(FOLLOW_templateDef_in_def284);
					templateDef(prefix);
					state._fsp--;

					}
					break;
				case 2 :
					// Group.g:202:44: dictDef
					{
					pushFollow(FOLLOW_dictDef_in_def289);
					dictDef();
					state._fsp--;

					}
					break;

			}
		}
		catch (RecognitionException re) {

					// pretend we already saw an error here
					state.lastErrorIndex = input.index();
					error("garbled template definition starting at '"+input.LT(1).getText()+"'");
				
		}

		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "def"



	// $ANTLR start "templateDef"
	// Group.g:209:1: templateDef[String prefix] : ( ( '@' enclosing= ID '.' name= ID '(' ')' |name= ID '(' formalArgs ')' ) '::=' ( STRING | BIGSTRING | BIGSTRING_NO_NL |) |alias= ID '::=' target= ID );
	public final void templateDef(String prefix) throws RecognitionException {
		Token enclosing=null;
		Token name=null;
		Token alias=null;
		Token target=null;
		Token STRING2=null;
		Token BIGSTRING3=null;
		Token BIGSTRING_NO_NL4=null;
		List<FormalArgument> formalArgs5 =null;


		    String template=null;
		    int n=0; // num char to strip from left, right of template def

		try {
			// Group.g:214:2: ( ( '@' enclosing= ID '.' name= ID '(' ')' |name= ID '(' formalArgs ')' ) '::=' ( STRING | BIGSTRING | BIGSTRING_NO_NL |) |alias= ID '::=' target= ID )
			int alt13=2;
			int LA13_0 = input.LA(1);
			if ( (LA13_0==24) ) {
				alt13=1;
			}
			else if ( (LA13_0==ID) ) {
				int LA13_2 = input.LA(2);
				if ( (LA13_2==16) ) {
					alt13=1;
				}
				else if ( (LA13_2==21) ) {
					alt13=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 13, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 13, 0, input);
				throw nvae;
			}

			switch (alt13) {
				case 1 :
					// Group.g:214:4: ( '@' enclosing= ID '.' name= ID '(' ')' |name= ID '(' formalArgs ')' ) '::=' ( STRING | BIGSTRING | BIGSTRING_NO_NL |)
					{
					// Group.g:214:4: ( '@' enclosing= ID '.' name= ID '(' ')' |name= ID '(' formalArgs ')' )
					int alt11=2;
					int LA11_0 = input.LA(1);
					if ( (LA11_0==24) ) {
						alt11=1;
					}
					else if ( (LA11_0==ID) ) {
						alt11=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 11, 0, input);
						throw nvae;
					}

					switch (alt11) {
						case 1 :
							// Group.g:214:6: '@' enclosing= ID '.' name= ID '(' ')'
							{
							match(input,24,FOLLOW_24_in_templateDef313); 
							enclosing=(Token)match(input,ID,FOLLOW_ID_in_templateDef317); 
							match(input,19,FOLLOW_19_in_templateDef319); 
							name=(Token)match(input,ID,FOLLOW_ID_in_templateDef323); 
							match(input,16,FOLLOW_16_in_templateDef325); 
							match(input,17,FOLLOW_17_in_templateDef327); 
							}
							break;
						case 2 :
							// Group.g:215:5: name= ID '(' formalArgs ')'
							{
							name=(Token)match(input,ID,FOLLOW_ID_in_templateDef335); 
							match(input,16,FOLLOW_16_in_templateDef337); 
							pushFollow(FOLLOW_formalArgs_in_templateDef339);
							formalArgs5=formalArgs();
							state._fsp--;

							match(input,17,FOLLOW_17_in_templateDef341); 
							}
							break;

					}

					match(input,21,FOLLOW_21_in_templateDef352); 
					Token templateToken = input.LT(1);
					// Group.g:219:6: ( STRING | BIGSTRING | BIGSTRING_NO_NL |)
					int alt12=4;
					switch ( input.LA(1) ) {
					case STRING:
						{
						alt12=1;
						}
						break;
					case BIGSTRING:
						{
						alt12=2;
						}
						break;
					case BIGSTRING_NO_NL:
						{
						alt12=3;
						}
						break;
					case EOF:
					case ID:
					case 24:
						{
						alt12=4;
						}
						break;
					default:
						NoViableAltException nvae =
							new NoViableAltException("", 12, 0, input);
						throw nvae;
					}
					switch (alt12) {
						case 1 :
							// Group.g:219:8: STRING
							{
							STRING2=(Token)match(input,STRING,FOLLOW_STRING_in_templateDef368); 
							template=(STRING2!=null?STRING2.getText():null); n=1;
							}
							break;
						case 2 :
							// Group.g:220:8: BIGSTRING
							{
							BIGSTRING3=(Token)match(input,BIGSTRING,FOLLOW_BIGSTRING_in_templateDef383); 
							template=(BIGSTRING3!=null?BIGSTRING3.getText():null); n=2;
							}
							break;
						case 3 :
							// Group.g:221:8: BIGSTRING_NO_NL
							{
							BIGSTRING_NO_NL4=(Token)match(input,BIGSTRING_NO_NL,FOLLOW_BIGSTRING_NO_NL_in_templateDef395); 
							template=(BIGSTRING_NO_NL4!=null?BIGSTRING_NO_NL4.getText():null); n=2;
							}
							break;
						case 4 :
							// Group.g:222:8: 
							{

								    	template = "";
								    	String msg = "missing template at '"+input.LT(1).getText()+"'";
							            NoViableAltException e = new NoViableAltException("", 0, 0, input);
							    	    group.errMgr.groupSyntaxError(ErrorType.SYNTAX_ERROR, getSourceName(), e, msg);
							    	    
							}
							break;

					}


						    if ( (name!=null?name.getTokenIndex():0) >= 0 ) { // if ID missing
								template = Misc.strip(template, n);
								String templateName = (name!=null?name.getText():null);
								if ( prefix.length()>0 ) templateName = prefix+(name!=null?name.getText():null);
								String enclosingTemplateName = (enclosing!=null?enclosing.getText():null);
								if (enclosingTemplateName != null && enclosingTemplateName.length()>0 && prefix.length()>0) {
									enclosingTemplateName = prefix + enclosingTemplateName;
								}
								group.defineTemplateOrRegion(templateName, enclosingTemplateName, templateToken,
															 template, name, formalArgs5);
							}
						    
					}
					break;
				case 2 :
					// Group.g:242:6: alias= ID '::=' target= ID
					{
					alias=(Token)match(input,ID,FOLLOW_ID_in_templateDef430); 
					match(input,21,FOLLOW_21_in_templateDef432); 
					target=(Token)match(input,ID,FOLLOW_ID_in_templateDef436); 
					group.defineTemplateAlias(alias, target);
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "templateDef"


	protected static class formalArgs_scope {
		boolean hasOptionalParameter;
	}
	protected Stack<formalArgs_scope> formalArgs_stack = new Stack<formalArgs_scope>();


	// $ANTLR start "formalArgs"
	// Group.g:245:1: formalArgs returns [List<FormalArgument> args = new ArrayList<FormalArgument>()] : ( formalArg[$args] ( ',' formalArg[$args] )* |);
	public final List<FormalArgument> formalArgs() throws RecognitionException {
		formalArgs_stack.push(new formalArgs_scope());
		List<FormalArgument> args =  new ArrayList<FormalArgument>();


		 formalArgs_stack.peek().hasOptionalParameter = false; 
		try {
			// Group.g:250:2: ( formalArg[$args] ( ',' formalArg[$args] )* |)
			int alt15=2;
			int LA15_0 = input.LA(1);
			if ( (LA15_0==ID) ) {
				alt15=1;
			}
			else if ( (LA15_0==17) ) {
				alt15=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}

			switch (alt15) {
				case 1 :
					// Group.g:250:4: formalArg[$args] ( ',' formalArg[$args] )*
					{
					pushFollow(FOLLOW_formalArg_in_formalArgs462);
					formalArg(args);
					state._fsp--;

					// Group.g:250:21: ( ',' formalArg[$args] )*
					loop14:
					while (true) {
						int alt14=2;
						int LA14_0 = input.LA(1);
						if ( (LA14_0==18) ) {
							alt14=1;
						}

						switch (alt14) {
						case 1 :
							// Group.g:250:22: ',' formalArg[$args]
							{
							match(input,18,FOLLOW_18_in_formalArgs466); 
							pushFollow(FOLLOW_formalArg_in_formalArgs468);
							formalArg(args);
							state._fsp--;

							}
							break;

						default :
							break loop14;
						}
					}

					}
					break;
				case 2 :
					// Group.g:252:2: 
					{
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			formalArgs_stack.pop();
		}
		return args;
	}
	// $ANTLR end "formalArgs"



	// $ANTLR start "formalArg"
	// Group.g:254:1: formalArg[List<FormalArgument> args] : ID ( '=' a= ( STRING | ANONYMOUS_TEMPLATE | 'true' | 'false' ) | '=' a= '[' ']' |) ;
	public final void formalArg(List<FormalArgument> args) throws RecognitionException {
		Token a=null;
		Token ID6=null;

		try {
			// Group.g:255:2: ( ID ( '=' a= ( STRING | ANONYMOUS_TEMPLATE | 'true' | 'false' ) | '=' a= '[' ']' |) )
			// Group.g:255:4: ID ( '=' a= ( STRING | ANONYMOUS_TEMPLATE | 'true' | 'false' ) | '=' a= '[' ']' |)
			{
			ID6=(Token)match(input,ID,FOLLOW_ID_in_formalArg486); 
			// Group.g:256:3: ( '=' a= ( STRING | ANONYMOUS_TEMPLATE | 'true' | 'false' ) | '=' a= '[' ']' |)
			int alt16=3;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==23) ) {
				int LA16_1 = input.LA(2);
				if ( (LA16_1==ANONYMOUS_TEMPLATE||LA16_1==FALSE||(LA16_1 >= STRING && LA16_1 <= TRUE)) ) {
					alt16=1;
				}
				else if ( (LA16_1==LBRACK) ) {
					alt16=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 16, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( ((LA16_0 >= 17 && LA16_0 <= 18)) ) {
				alt16=3;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}

			switch (alt16) {
				case 1 :
					// Group.g:256:5: '=' a= ( STRING | ANONYMOUS_TEMPLATE | 'true' | 'false' )
					{
					match(input,23,FOLLOW_23_in_formalArg492); 
					a=input.LT(1);
					if ( input.LA(1)==ANONYMOUS_TEMPLATE||input.LA(1)==FALSE||(input.LA(1) >= STRING && input.LA(1) <= TRUE) ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					formalArgs_stack.peek().hasOptionalParameter = true;
					}
					break;
				case 2 :
					// Group.g:257:5: '=' a= '[' ']'
					{
					match(input,23,FOLLOW_23_in_formalArg512); 
					a=(Token)match(input,LBRACK,FOLLOW_LBRACK_in_formalArg516); 
					match(input,RBRACK,FOLLOW_RBRACK_in_formalArg518); 
					formalArgs_stack.peek().hasOptionalParameter = true;
					}
					break;
				case 3 :
					// Group.g:258:5: 
					{

								if (formalArgs_stack.peek().hasOptionalParameter) {
									group.errMgr.compileTimeError(ErrorType.REQUIRED_PARAMETER_AFTER_OPTIONAL,
									 							  null, ID6);
								}
								
					}
					break;

			}

			args.add(new FormalArgument((ID6!=null?ID6.getText():null), a));
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "formalArg"



	// $ANTLR start "dictDef"
	// Group.g:277:1: dictDef : ID '::=' dict ;
	public final void dictDef() throws RecognitionException {
		Token ID7=null;
		Map<String,Object> dict8 =null;

		try {
			// Group.g:278:2: ( ID '::=' dict )
			// Group.g:278:4: ID '::=' dict
			{
			ID7=(Token)match(input,ID,FOLLOW_ID_in_dictDef551); 
			match(input,21,FOLLOW_21_in_dictDef553); 
			pushFollow(FOLLOW_dict_in_dictDef555);
			dict8=dict();
			state._fsp--;


			        if ( group.rawGetDictionary((ID7!=null?ID7.getText():null))!=null ) {
						group.errMgr.compileTimeError(ErrorType.MAP_REDEFINITION, null, ID7);
			        }
			        else if ( group.rawGetTemplate((ID7!=null?ID7.getText():null))!=null ) {
						group.errMgr.compileTimeError(ErrorType.TEMPLATE_REDEFINITION_AS_MAP, null, ID7);
			        }
			        else {
			            group.defineDictionary((ID7!=null?ID7.getText():null), dict8);
			        }
			        
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "dictDef"



	// $ANTLR start "dict"
	// Group.g:292:1: dict returns [Map<String,Object> mapping] : '[' dictPairs[mapping] ']' ;
	public final Map<String,Object> dict() throws RecognitionException {
		Map<String,Object> mapping = null;


		mapping=new HashMap<String,Object>();
		try {
			// Group.g:294:2: ( '[' dictPairs[mapping] ']' )
			// Group.g:294:6: '[' dictPairs[mapping] ']'
			{
			match(input,LBRACK,FOLLOW_LBRACK_in_dict587); 
			pushFollow(FOLLOW_dictPairs_in_dict589);
			dictPairs(mapping);
			state._fsp--;

			match(input,RBRACK,FOLLOW_RBRACK_in_dict592); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return mapping;
	}
	// $ANTLR end "dict"



	// $ANTLR start "dictPairs"
	// Group.g:297:1: dictPairs[Map<String,Object> mapping] : ( keyValuePair[mapping] ( ',' keyValuePair[mapping] )* ( ',' defaultValuePair[mapping] )? | defaultValuePair[mapping] );
	public final void dictPairs(Map<String,Object> mapping) throws RecognitionException {
		try {
			// Group.g:298:5: ( keyValuePair[mapping] ( ',' keyValuePair[mapping] )* ( ',' defaultValuePair[mapping] )? | defaultValuePair[mapping] )
			int alt19=2;
			int LA19_0 = input.LA(1);
			if ( (LA19_0==STRING) ) {
				alt19=1;
			}
			else if ( (LA19_0==25) ) {
				alt19=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 19, 0, input);
				throw nvae;
			}

			switch (alt19) {
				case 1 :
					// Group.g:298:7: keyValuePair[mapping] ( ',' keyValuePair[mapping] )* ( ',' defaultValuePair[mapping] )?
					{
					pushFollow(FOLLOW_keyValuePair_in_dictPairs607);
					keyValuePair(mapping);
					state._fsp--;

					// Group.g:299:6: ( ',' keyValuePair[mapping] )*
					loop17:
					while (true) {
						int alt17=2;
						int LA17_0 = input.LA(1);
						if ( (LA17_0==18) ) {
							int LA17_1 = input.LA(2);
							if ( (LA17_1==STRING) ) {
								alt17=1;
							}

						}

						switch (alt17) {
						case 1 :
							// Group.g:299:7: ',' keyValuePair[mapping]
							{
							match(input,18,FOLLOW_18_in_dictPairs616); 
							pushFollow(FOLLOW_keyValuePair_in_dictPairs618);
							keyValuePair(mapping);
							state._fsp--;

							}
							break;

						default :
							break loop17;
						}
					}

					// Group.g:299:35: ( ',' defaultValuePair[mapping] )?
					int alt18=2;
					int LA18_0 = input.LA(1);
					if ( (LA18_0==18) ) {
						alt18=1;
					}
					switch (alt18) {
						case 1 :
							// Group.g:299:36: ',' defaultValuePair[mapping]
							{
							match(input,18,FOLLOW_18_in_dictPairs624); 
							pushFollow(FOLLOW_defaultValuePair_in_dictPairs626);
							defaultValuePair(mapping);
							state._fsp--;

							}
							break;

					}

					}
					break;
				case 2 :
					// Group.g:300:7: defaultValuePair[mapping]
					{
					pushFollow(FOLLOW_defaultValuePair_in_dictPairs637);
					defaultValuePair(mapping);
					state._fsp--;

					}
					break;

			}
		}
		catch (RecognitionException re) {

					error("missing dictionary entry at '"+input.LT(1).getText()+"'");
				
		}

		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "dictPairs"



	// $ANTLR start "defaultValuePair"
	// Group.g:306:1: defaultValuePair[Map<String,Object> mapping] : 'default' ':' keyValue ;
	public final void defaultValuePair(Map<String,Object> mapping) throws RecognitionException {
		Object keyValue9 =null;

		try {
			// Group.g:307:2: ( 'default' ':' keyValue )
			// Group.g:307:4: 'default' ':' keyValue
			{
			match(input,25,FOLLOW_25_in_defaultValuePair660); 
			match(input,20,FOLLOW_20_in_defaultValuePair662); 
			pushFollow(FOLLOW_keyValue_in_defaultValuePair664);
			keyValue9=keyValue();
			state._fsp--;

			mapping.put(STGroup.DEFAULT_KEY, keyValue9);
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "defaultValuePair"



	// $ANTLR start "keyValuePair"
	// Group.g:310:1: keyValuePair[Map<String,Object> mapping] : STRING ':' keyValue ;
	public final void keyValuePair(Map<String,Object> mapping) throws RecognitionException {
		Token STRING10=null;
		Object keyValue11 =null;

		try {
			// Group.g:311:2: ( STRING ':' keyValue )
			// Group.g:311:4: STRING ':' keyValue
			{
			STRING10=(Token)match(input,STRING,FOLLOW_STRING_in_keyValuePair678); 
			match(input,20,FOLLOW_20_in_keyValuePair680); 
			pushFollow(FOLLOW_keyValue_in_keyValuePair682);
			keyValue11=keyValue();
			state._fsp--;

			mapping.put(Misc.replaceEscapes(Misc.strip((STRING10!=null?STRING10.getText():null), 1)), keyValue11);
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "keyValuePair"



	// $ANTLR start "keyValue"
	// Group.g:314:1: keyValue returns [Object value] : ( BIGSTRING | BIGSTRING_NO_NL | ANONYMOUS_TEMPLATE | STRING | TRUE | FALSE | '[' ']' |{...}? => ID );
	public final Object keyValue() throws RecognitionException {
		Object value = null;


		Token BIGSTRING12=null;
		Token BIGSTRING_NO_NL13=null;
		Token ANONYMOUS_TEMPLATE14=null;
		Token STRING15=null;

		try {
			// Group.g:315:2: ( BIGSTRING | BIGSTRING_NO_NL | ANONYMOUS_TEMPLATE | STRING | TRUE | FALSE | '[' ']' |{...}? => ID )
			int alt20=8;
			int LA20_0 = input.LA(1);
			if ( (LA20_0==BIGSTRING) ) {
				alt20=1;
			}
			else if ( (LA20_0==BIGSTRING_NO_NL) ) {
				alt20=2;
			}
			else if ( (LA20_0==ANONYMOUS_TEMPLATE) ) {
				alt20=3;
			}
			else if ( (LA20_0==STRING) ) {
				alt20=4;
			}
			else if ( (LA20_0==TRUE) ) {
				alt20=5;
			}
			else if ( (LA20_0==FALSE) ) {
				alt20=6;
			}
			else if ( (LA20_0==LBRACK) ) {
				alt20=7;
			}
			else if ( (LA20_0==ID) && ((input.LT(1).getText().equals("key")))) {
				alt20=8;
			}

			switch (alt20) {
				case 1 :
					// Group.g:315:4: BIGSTRING
					{
					BIGSTRING12=(Token)match(input,BIGSTRING,FOLLOW_BIGSTRING_in_keyValue699); 
					value = group.createSingleton(BIGSTRING12);
					}
					break;
				case 2 :
					// Group.g:316:4: BIGSTRING_NO_NL
					{
					BIGSTRING_NO_NL13=(Token)match(input,BIGSTRING_NO_NL,FOLLOW_BIGSTRING_NO_NL_in_keyValue708); 
					value = group.createSingleton(BIGSTRING_NO_NL13);
					}
					break;
				case 3 :
					// Group.g:317:4: ANONYMOUS_TEMPLATE
					{
					ANONYMOUS_TEMPLATE14=(Token)match(input,ANONYMOUS_TEMPLATE,FOLLOW_ANONYMOUS_TEMPLATE_in_keyValue716); 
					value = group.createSingleton(ANONYMOUS_TEMPLATE14);
					}
					break;
				case 4 :
					// Group.g:318:4: STRING
					{
					STRING15=(Token)match(input,STRING,FOLLOW_STRING_in_keyValue723); 
					value = Misc.replaceEscapes(Misc.strip((STRING15!=null?STRING15.getText():null), 1));
					}
					break;
				case 5 :
					// Group.g:319:4: TRUE
					{
					match(input,TRUE,FOLLOW_TRUE_in_keyValue733); 
					value = true;
					}
					break;
				case 6 :
					// Group.g:320:4: FALSE
					{
					match(input,FALSE,FOLLOW_FALSE_in_keyValue743); 
					value = false;
					}
					break;
				case 7 :
					// Group.g:321:4: '[' ']'
					{
					match(input,LBRACK,FOLLOW_LBRACK_in_keyValue753); 
					match(input,RBRACK,FOLLOW_RBRACK_in_keyValue755); 
					value = Collections.emptyList();
					}
					break;
				case 8 :
					// Group.g:322:4: {...}? => ID
					{
					if ( !((input.LT(1).getText().equals("key"))) ) {
						throw new FailedPredicateException(input, "keyValue", "input.LT(1).getText().equals(\"key\")");
					}
					match(input,ID,FOLLOW_ID_in_keyValue768); 
					value = STGroup.DICT_KEY;
					}
					break;

			}
		}
		catch (RecognitionException re) {

					error("missing value for key at '"+input.LT(1).getText()+"'");
				
		}

		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "keyValue"

	// Delegated rules



	public static final BitSet FOLLOW_oldStyleHeader_in_group86 = new BitSet(new long[]{0x0000000025000200L});
	public static final BitSet FOLLOW_delimiters_in_group91 = new BitSet(new long[]{0x0000000021000200L});
	public static final BitSet FOLLOW_29_in_group101 = new BitSet(new long[]{0x0000000000002000L});
	public static final BitSet FOLLOW_STRING_in_group103 = new BitSet(new long[]{0x0000000021000200L});
	public static final BitSet FOLLOW_29_in_group111 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_group122 = new BitSet(new long[]{0x0000000021080200L});
	public static final BitSet FOLLOW_19_in_group125 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_group127 = new BitSet(new long[]{0x0000000021080200L});
	public static final BitSet FOLLOW_def_in_group139 = new BitSet(new long[]{0x0000000001000200L});
	public static final BitSet FOLLOW_EOF_in_group145 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_27_in_oldStyleHeader162 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_oldStyleHeader164 = new BitSet(new long[]{0x0000000010500000L});
	public static final BitSet FOLLOW_20_in_oldStyleHeader168 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_oldStyleHeader170 = new BitSet(new long[]{0x0000000010400000L});
	public static final BitSet FOLLOW_28_in_oldStyleHeader182 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_oldStyleHeader184 = new BitSet(new long[]{0x0000000000440000L});
	public static final BitSet FOLLOW_18_in_oldStyleHeader187 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_oldStyleHeader189 = new BitSet(new long[]{0x0000000000440000L});
	public static final BitSet FOLLOW_22_in_oldStyleHeader201 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_groupName223 = new BitSet(new long[]{0x0000000000080002L});
	public static final BitSet FOLLOW_19_in_groupName228 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_groupName232 = new BitSet(new long[]{0x0000000000080002L});
	public static final BitSet FOLLOW_26_in_delimiters250 = new BitSet(new long[]{0x0000000000002000L});
	public static final BitSet FOLLOW_STRING_in_delimiters254 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_18_in_delimiters256 = new BitSet(new long[]{0x0000000000002000L});
	public static final BitSet FOLLOW_STRING_in_delimiters260 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_templateDef_in_def284 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dictDef_in_def289 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_24_in_templateDef313 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_templateDef317 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_19_in_templateDef319 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_templateDef323 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_16_in_templateDef325 = new BitSet(new long[]{0x0000000000020000L});
	public static final BitSet FOLLOW_17_in_templateDef327 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_ID_in_templateDef335 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_16_in_templateDef337 = new BitSet(new long[]{0x0000000000020200L});
	public static final BitSet FOLLOW_formalArgs_in_templateDef339 = new BitSet(new long[]{0x0000000000020000L});
	public static final BitSet FOLLOW_17_in_templateDef341 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_templateDef352 = new BitSet(new long[]{0x0000000000002062L});
	public static final BitSet FOLLOW_STRING_in_templateDef368 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BIGSTRING_in_templateDef383 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BIGSTRING_NO_NL_in_templateDef395 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_templateDef430 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_templateDef432 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_templateDef436 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_formalArg_in_formalArgs462 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_18_in_formalArgs466 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_formalArg_in_formalArgs468 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_ID_in_formalArg486 = new BitSet(new long[]{0x0000000000800002L});
	public static final BitSet FOLLOW_23_in_formalArg492 = new BitSet(new long[]{0x0000000000006110L});
	public static final BitSet FOLLOW_set_in_formalArg496 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_23_in_formalArg512 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_LBRACK_in_formalArg516 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_RBRACK_in_formalArg518 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_dictDef551 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_dictDef553 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_dict_in_dictDef555 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACK_in_dict587 = new BitSet(new long[]{0x0000000002002000L});
	public static final BitSet FOLLOW_dictPairs_in_dict589 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_RBRACK_in_dict592 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_keyValuePair_in_dictPairs607 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_18_in_dictPairs616 = new BitSet(new long[]{0x0000000000002000L});
	public static final BitSet FOLLOW_keyValuePair_in_dictPairs618 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_18_in_dictPairs624 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_defaultValuePair_in_dictPairs626 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_defaultValuePair_in_dictPairs637 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_25_in_defaultValuePair660 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_20_in_defaultValuePair662 = new BitSet(new long[]{0x0000000000006770L});
	public static final BitSet FOLLOW_keyValue_in_defaultValuePair664 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_in_keyValuePair678 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_20_in_keyValuePair680 = new BitSet(new long[]{0x0000000000006770L});
	public static final BitSet FOLLOW_keyValue_in_keyValuePair682 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BIGSTRING_in_keyValue699 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BIGSTRING_NO_NL_in_keyValue708 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ANONYMOUS_TEMPLATE_in_keyValue716 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_in_keyValue723 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TRUE_in_keyValue733 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FALSE_in_keyValue743 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACK_in_keyValue753 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_RBRACK_in_keyValue755 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_keyValue768 = new BitSet(new long[]{0x0000000000000002L});
}
