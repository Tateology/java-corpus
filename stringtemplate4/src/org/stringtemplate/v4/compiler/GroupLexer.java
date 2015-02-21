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
import org.stringtemplate.v4.*;
import org.stringtemplate.v4.misc.*;
import java.io.File;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class GroupLexer extends Lexer {
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

	public STGroup group;

	@Override
	public void reportError(RecognitionException e) {
	    String msg = null;
	    if ( e instanceof NoViableAltException ) {
	        msg = "invalid character '"+(char)input.LA(1)+"'";
	    }
	    else if ( e instanceof MismatchedTokenException && ((MismatchedTokenException)e).expecting=='"' ) {
	        msg = "unterminated string";
	    }
	    else {
	        msg = getErrorMessage(e, getTokenNames());
	    }
	    group.errMgr.groupSyntaxError(ErrorType.SYNTAX_ERROR, getSourceName(), e, msg);
	}
	@Override
	public String getSourceName() {
	    String fullFileName = super.getSourceName();
	    File f = new File(fullFileName); // strip to simple name
	    return f.getName();
	}


	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public GroupLexer() {} 
	public GroupLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public GroupLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "Group.g"; }

	// $ANTLR start "FALSE"
	public final void mFALSE() throws RecognitionException {
		try {
			int _type = FALSE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:65:7: ( 'false' )
			// Group.g:65:9: 'false'
			{
			match("false"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FALSE"

	// $ANTLR start "LBRACK"
	public final void mLBRACK() throws RecognitionException {
		try {
			int _type = LBRACK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:66:8: ( '[' )
			// Group.g:66:10: '['
			{
			match('['); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LBRACK"

	// $ANTLR start "RBRACK"
	public final void mRBRACK() throws RecognitionException {
		try {
			int _type = RBRACK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:67:8: ( ']' )
			// Group.g:67:10: ']'
			{
			match(']'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RBRACK"

	// $ANTLR start "TRUE"
	public final void mTRUE() throws RecognitionException {
		try {
			int _type = TRUE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:68:6: ( 'true' )
			// Group.g:68:8: 'true'
			{
			match("true"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TRUE"

	// $ANTLR start "T__16"
	public final void mT__16() throws RecognitionException {
		try {
			int _type = T__16;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:69:7: ( '(' )
			// Group.g:69:9: '('
			{
			match('('); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__16"

	// $ANTLR start "T__17"
	public final void mT__17() throws RecognitionException {
		try {
			int _type = T__17;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:70:7: ( ')' )
			// Group.g:70:9: ')'
			{
			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__17"

	// $ANTLR start "T__18"
	public final void mT__18() throws RecognitionException {
		try {
			int _type = T__18;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:71:7: ( ',' )
			// Group.g:71:9: ','
			{
			match(','); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__18"

	// $ANTLR start "T__19"
	public final void mT__19() throws RecognitionException {
		try {
			int _type = T__19;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:72:7: ( '.' )
			// Group.g:72:9: '.'
			{
			match('.'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__19"

	// $ANTLR start "T__20"
	public final void mT__20() throws RecognitionException {
		try {
			int _type = T__20;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:73:7: ( ':' )
			// Group.g:73:9: ':'
			{
			match(':'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__20"

	// $ANTLR start "T__21"
	public final void mT__21() throws RecognitionException {
		try {
			int _type = T__21;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:74:7: ( '::=' )
			// Group.g:74:9: '::='
			{
			match("::="); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__21"

	// $ANTLR start "T__22"
	public final void mT__22() throws RecognitionException {
		try {
			int _type = T__22;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:75:7: ( ';' )
			// Group.g:75:9: ';'
			{
			match(';'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__22"

	// $ANTLR start "T__23"
	public final void mT__23() throws RecognitionException {
		try {
			int _type = T__23;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:76:7: ( '=' )
			// Group.g:76:9: '='
			{
			match('='); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__23"

	// $ANTLR start "T__24"
	public final void mT__24() throws RecognitionException {
		try {
			int _type = T__24;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:77:7: ( '@' )
			// Group.g:77:9: '@'
			{
			match('@'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__24"

	// $ANTLR start "T__25"
	public final void mT__25() throws RecognitionException {
		try {
			int _type = T__25;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:78:7: ( 'default' )
			// Group.g:78:9: 'default'
			{
			match("default"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__25"

	// $ANTLR start "T__26"
	public final void mT__26() throws RecognitionException {
		try {
			int _type = T__26;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:79:7: ( 'delimiters' )
			// Group.g:79:9: 'delimiters'
			{
			match("delimiters"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__26"

	// $ANTLR start "T__27"
	public final void mT__27() throws RecognitionException {
		try {
			int _type = T__27;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:80:7: ( 'group' )
			// Group.g:80:9: 'group'
			{
			match("group"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__27"

	// $ANTLR start "T__28"
	public final void mT__28() throws RecognitionException {
		try {
			int _type = T__28;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:81:7: ( 'implements' )
			// Group.g:81:9: 'implements'
			{
			match("implements"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__28"

	// $ANTLR start "T__29"
	public final void mT__29() throws RecognitionException {
		try {
			int _type = T__29;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:82:7: ( 'import' )
			// Group.g:82:9: 'import'
			{
			match("import"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__29"

	// $ANTLR start "ID"
	public final void mID() throws RecognitionException {
		try {
			int _type = ID;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:329:4: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '-' | '_' )* )
			// Group.g:329:6: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '-' | '_' )*
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			// Group.g:329:30: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '-' | '_' )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0=='-'||(LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'Z')||LA1_0=='_'||(LA1_0 >= 'a' && LA1_0 <= 'z')) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// Group.g:
					{
					if ( input.LA(1)=='-'||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop1;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ID"

	// $ANTLR start "STRING"
	public final void mSTRING() throws RecognitionException {
		try {
			int _type = STRING;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:333:2: ( '\"' ( '\\\\' '\"' | '\\\\' ~ '\"' | '\\n' |~ ( '\\\\' | '\"' | '\\n' ) )* '\"' )
			// Group.g:333:4: '\"' ( '\\\\' '\"' | '\\\\' ~ '\"' | '\\n' |~ ( '\\\\' | '\"' | '\\n' ) )* '\"'
			{
			match('\"'); 
			// Group.g:334:3: ( '\\\\' '\"' | '\\\\' ~ '\"' | '\\n' |~ ( '\\\\' | '\"' | '\\n' ) )*
			loop2:
			while (true) {
				int alt2=5;
				int LA2_0 = input.LA(1);
				if ( (LA2_0=='\\') ) {
					int LA2_2 = input.LA(2);
					if ( (LA2_2=='\"') ) {
						alt2=1;
					}
					else if ( ((LA2_2 >= '\u0000' && LA2_2 <= '!')||(LA2_2 >= '#' && LA2_2 <= '\uFFFF')) ) {
						alt2=2;
					}

				}
				else if ( (LA2_0=='\n') ) {
					alt2=3;
				}
				else if ( ((LA2_0 >= '\u0000' && LA2_0 <= '\t')||(LA2_0 >= '\u000B' && LA2_0 <= '!')||(LA2_0 >= '#' && LA2_0 <= '[')||(LA2_0 >= ']' && LA2_0 <= '\uFFFF')) ) {
					alt2=4;
				}

				switch (alt2) {
				case 1 :
					// Group.g:334:5: '\\\\' '\"'
					{
					match('\\'); 
					match('\"'); 
					}
					break;
				case 2 :
					// Group.g:335:5: '\\\\' ~ '\"'
					{
					match('\\'); 
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 3 :
					// Group.g:336:5: '\\n'
					{

								String msg = "\\n in string";
					    		NoViableAltException e = new NoViableAltException("", 0, 0, input);
								group.errMgr.groupLexerError(ErrorType.SYNTAX_ERROR, getSourceName(), e, msg);
								
					match('\n'); 
					}
					break;
				case 4 :
					// Group.g:342:5: ~ ( '\\\\' | '\"' | '\\n' )
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop2;
				}
			}

			match('\"'); 

			        String txt = getText().replaceAll("\\\\\"","\"");
					setText(txt);
					
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING"

	// $ANTLR start "BIGSTRING_NO_NL"
	public final void mBIGSTRING_NO_NL() throws RecognitionException {
		try {
			int _type = BIGSTRING_NO_NL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:352:2: ( '<%' ( . )* '%>' )
			// Group.g:352:4: '<%' ( . )* '%>'
			{
			match("<%"); 

			// Group.g:352:9: ( . )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( (LA3_0=='%') ) {
					int LA3_1 = input.LA(2);
					if ( (LA3_1=='>') ) {
						alt3=2;
					}
					else if ( ((LA3_1 >= '\u0000' && LA3_1 <= '=')||(LA3_1 >= '?' && LA3_1 <= '\uFFFF')) ) {
						alt3=1;
					}

				}
				else if ( ((LA3_0 >= '\u0000' && LA3_0 <= '$')||(LA3_0 >= '&' && LA3_0 <= '\uFFFF')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// Group.g:352:11: .
					{
					matchAny(); 
					}
					break;

				default :
					break loop3;
				}
			}

			match("%>"); 


			        String txt = getText().replaceAll("%\\\\>","%>");
					setText(txt);
					
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BIGSTRING_NO_NL"

	// $ANTLR start "BIGSTRING"
	public final void mBIGSTRING() throws RecognitionException {
		try {
			int _type = BIGSTRING;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:371:2: ( '<<' ( options {greedy=false; } : '\\\\' '>' | '\\\\' ~ '>' |~ '\\\\' )* '>>' )
			// Group.g:371:4: '<<' ( options {greedy=false; } : '\\\\' '>' | '\\\\' ~ '>' |~ '\\\\' )* '>>'
			{
			match("<<"); 

			// Group.g:372:3: ( options {greedy=false; } : '\\\\' '>' | '\\\\' ~ '>' |~ '\\\\' )*
			loop4:
			while (true) {
				int alt4=4;
				int LA4_0 = input.LA(1);
				if ( (LA4_0=='>') ) {
					int LA4_1 = input.LA(2);
					if ( (LA4_1=='>') ) {
						alt4=4;
					}
					else if ( ((LA4_1 >= '\u0000' && LA4_1 <= '=')||(LA4_1 >= '?' && LA4_1 <= '\uFFFF')) ) {
						alt4=3;
					}

				}
				else if ( (LA4_0=='\\') ) {
					int LA4_2 = input.LA(2);
					if ( (LA4_2=='>') ) {
						alt4=1;
					}
					else if ( ((LA4_2 >= '\u0000' && LA4_2 <= '=')||(LA4_2 >= '?' && LA4_2 <= '\uFFFF')) ) {
						alt4=2;
					}

				}
				else if ( ((LA4_0 >= '\u0000' && LA4_0 <= '=')||(LA4_0 >= '?' && LA4_0 <= '[')||(LA4_0 >= ']' && LA4_0 <= '\uFFFF')) ) {
					alt4=3;
				}

				switch (alt4) {
				case 1 :
					// Group.g:373:5: '\\\\' '>'
					{
					match('\\'); 
					match('>'); 
					}
					break;
				case 2 :
					// Group.g:374:5: '\\\\' ~ '>'
					{
					match('\\'); 
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '=')||(input.LA(1) >= '?' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 3 :
					// Group.g:375:5: ~ '\\\\'
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop4;
				}
			}

			match(">>"); 


			        String txt = getText();
			        txt = Misc.replaceEscapedRightAngle(txt); // replace \> with > unless <\\>
					setText(txt);
					
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BIGSTRING"

	// $ANTLR start "ANONYMOUS_TEMPLATE"
	public final void mANONYMOUS_TEMPLATE() throws RecognitionException {
		try {
			int _type = ANONYMOUS_TEMPLATE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:381:5: ( '{' )
			// Group.g:381:7: '{'
			{
			match('{'); 

					Token templateToken = new CommonToken(input, ANONYMOUS_TEMPLATE, 0, getCharIndex(), getCharIndex());
					STLexer lexer =
						new STLexer(group.errMgr, input, templateToken, group.delimiterStartChar, group.delimiterStopChar);
					lexer.subtemplateDepth = 1;
					Token t = lexer.nextToken();
					while ( lexer.subtemplateDepth>=1 || t.getType()!=STLexer.RCURLY ) {
						if ( t.getType()==STLexer.EOF_TYPE ) {
			            	MismatchedTokenException e = new MismatchedTokenException('}', input);
							String msg = "missing final '}' in {...} anonymous template";
			    			group.errMgr.groupLexerError(ErrorType.SYNTAX_ERROR, getSourceName(), e, msg);
							break;
						}
						t = lexer.nextToken();
					}
					
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ANONYMOUS_TEMPLATE"

	// $ANTLR start "COMMENT"
	public final void mCOMMENT() throws RecognitionException {
		try {
			int _type = COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:403:5: ( '/*' ( options {greedy=false; } : . )* '*/' )
			// Group.g:403:9: '/*' ( options {greedy=false; } : . )* '*/'
			{
			match("/*"); 

			// Group.g:403:14: ( options {greedy=false; } : . )*
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( (LA5_0=='*') ) {
					int LA5_1 = input.LA(2);
					if ( (LA5_1=='/') ) {
						alt5=2;
					}
					else if ( ((LA5_1 >= '\u0000' && LA5_1 <= '.')||(LA5_1 >= '0' && LA5_1 <= '\uFFFF')) ) {
						alt5=1;
					}

				}
				else if ( ((LA5_0 >= '\u0000' && LA5_0 <= ')')||(LA5_0 >= '+' && LA5_0 <= '\uFFFF')) ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// Group.g:403:42: .
					{
					matchAny(); 
					}
					break;

				default :
					break loop5;
				}
			}

			match("*/"); 

			skip();
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMENT"

	// $ANTLR start "LINE_COMMENT"
	public final void mLINE_COMMENT() throws RecognitionException {
		try {
			int _type = LINE_COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:407:5: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' )
			// Group.g:407:7: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
			{
			match("//"); 

			// Group.g:407:12: (~ ( '\\n' | '\\r' ) )*
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( ((LA6_0 >= '\u0000' && LA6_0 <= '\t')||(LA6_0 >= '\u000B' && LA6_0 <= '\f')||(LA6_0 >= '\u000E' && LA6_0 <= '\uFFFF')) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// Group.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop6;
				}
			}

			// Group.g:407:26: ( '\\r' )?
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0=='\r') ) {
				alt7=1;
			}
			switch (alt7) {
				case 1 :
					// Group.g:407:26: '\\r'
					{
					match('\r'); 
					}
					break;

			}

			match('\n'); 
			skip();
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LINE_COMMENT"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Group.g:410:5: ( ( ' ' | '\\r' | '\\t' | '\\n' ) )
			// Group.g:410:7: ( ' ' | '\\r' | '\\t' | '\\n' )
			{
			if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			skip();
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	@Override
	public void mTokens() throws RecognitionException {
		// Group.g:1:8: ( FALSE | LBRACK | RBRACK | TRUE | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | ID | STRING | BIGSTRING_NO_NL | BIGSTRING | ANONYMOUS_TEMPLATE | COMMENT | LINE_COMMENT | WS )
		int alt8=26;
		alt8 = dfa8.predict(input);
		switch (alt8) {
			case 1 :
				// Group.g:1:10: FALSE
				{
				mFALSE(); 

				}
				break;
			case 2 :
				// Group.g:1:16: LBRACK
				{
				mLBRACK(); 

				}
				break;
			case 3 :
				// Group.g:1:23: RBRACK
				{
				mRBRACK(); 

				}
				break;
			case 4 :
				// Group.g:1:30: TRUE
				{
				mTRUE(); 

				}
				break;
			case 5 :
				// Group.g:1:35: T__16
				{
				mT__16(); 

				}
				break;
			case 6 :
				// Group.g:1:41: T__17
				{
				mT__17(); 

				}
				break;
			case 7 :
				// Group.g:1:47: T__18
				{
				mT__18(); 

				}
				break;
			case 8 :
				// Group.g:1:53: T__19
				{
				mT__19(); 

				}
				break;
			case 9 :
				// Group.g:1:59: T__20
				{
				mT__20(); 

				}
				break;
			case 10 :
				// Group.g:1:65: T__21
				{
				mT__21(); 

				}
				break;
			case 11 :
				// Group.g:1:71: T__22
				{
				mT__22(); 

				}
				break;
			case 12 :
				// Group.g:1:77: T__23
				{
				mT__23(); 

				}
				break;
			case 13 :
				// Group.g:1:83: T__24
				{
				mT__24(); 

				}
				break;
			case 14 :
				// Group.g:1:89: T__25
				{
				mT__25(); 

				}
				break;
			case 15 :
				// Group.g:1:95: T__26
				{
				mT__26(); 

				}
				break;
			case 16 :
				// Group.g:1:101: T__27
				{
				mT__27(); 

				}
				break;
			case 17 :
				// Group.g:1:107: T__28
				{
				mT__28(); 

				}
				break;
			case 18 :
				// Group.g:1:113: T__29
				{
				mT__29(); 

				}
				break;
			case 19 :
				// Group.g:1:119: ID
				{
				mID(); 

				}
				break;
			case 20 :
				// Group.g:1:122: STRING
				{
				mSTRING(); 

				}
				break;
			case 21 :
				// Group.g:1:129: BIGSTRING_NO_NL
				{
				mBIGSTRING_NO_NL(); 

				}
				break;
			case 22 :
				// Group.g:1:145: BIGSTRING
				{
				mBIGSTRING(); 

				}
				break;
			case 23 :
				// Group.g:1:155: ANONYMOUS_TEMPLATE
				{
				mANONYMOUS_TEMPLATE(); 

				}
				break;
			case 24 :
				// Group.g:1:174: COMMENT
				{
				mCOMMENT(); 

				}
				break;
			case 25 :
				// Group.g:1:182: LINE_COMMENT
				{
				mLINE_COMMENT(); 

				}
				break;
			case 26 :
				// Group.g:1:195: WS
				{
				mWS(); 

				}
				break;

		}
	}


	protected DFA8 dfa8 = new DFA8(this);
	static final String DFA8_eotS =
		"\1\uffff\1\20\2\uffff\1\20\4\uffff\1\31\3\uffff\3\20\6\uffff\2\20\2\uffff"+
		"\3\20\4\uffff\7\20\1\57\5\20\1\65\1\uffff\2\20\1\70\2\20\1\uffff\2\20"+
		"\1\uffff\1\20\1\76\1\77\2\20\2\uffff\4\20\1\106\1\107\2\uffff";
	static final String DFA8_eofS =
		"\110\uffff";
	static final String DFA8_minS =
		"\1\11\1\141\2\uffff\1\162\4\uffff\1\72\3\uffff\1\145\1\162\1\155\2\uffff"+
		"\1\45\1\uffff\1\52\1\uffff\1\154\1\165\2\uffff\1\146\1\157\1\160\4\uffff"+
		"\1\163\1\145\1\141\1\151\1\165\1\154\1\145\1\55\1\165\1\155\1\160\1\145"+
		"\1\162\1\55\1\uffff\1\154\1\151\1\55\1\155\1\164\1\uffff\2\164\1\uffff"+
		"\1\145\2\55\1\145\1\156\2\uffff\1\162\1\164\2\163\2\55\2\uffff";
	static final String DFA8_maxS =
		"\1\173\1\141\2\uffff\1\162\4\uffff\1\72\3\uffff\1\145\1\162\1\155\2\uffff"+
		"\1\74\1\uffff\1\57\1\uffff\1\154\1\165\2\uffff\1\154\1\157\1\160\4\uffff"+
		"\1\163\1\145\1\141\1\151\1\165\1\157\1\145\1\172\1\165\1\155\1\160\1\145"+
		"\1\162\1\172\1\uffff\1\154\1\151\1\172\1\155\1\164\1\uffff\2\164\1\uffff"+
		"\1\145\2\172\1\145\1\156\2\uffff\1\162\1\164\2\163\2\172\2\uffff";
	static final String DFA8_acceptS =
		"\2\uffff\1\2\1\3\1\uffff\1\5\1\6\1\7\1\10\1\uffff\1\13\1\14\1\15\3\uffff"+
		"\1\23\1\24\1\uffff\1\27\1\uffff\1\32\2\uffff\1\12\1\11\3\uffff\1\25\1"+
		"\26\1\30\1\31\16\uffff\1\4\5\uffff\1\1\2\uffff\1\20\5\uffff\1\22\1\16"+
		"\6\uffff\1\17\1\21";
	static final String DFA8_specialS =
		"\110\uffff}>";
	static final String[] DFA8_transitionS = {
			"\2\25\2\uffff\1\25\22\uffff\1\25\1\uffff\1\21\5\uffff\1\5\1\6\2\uffff"+
			"\1\7\1\uffff\1\10\1\24\12\uffff\1\11\1\12\1\22\1\13\2\uffff\1\14\32\20"+
			"\1\2\1\uffff\1\3\1\uffff\1\20\1\uffff\3\20\1\15\1\20\1\1\1\16\1\20\1"+
			"\17\12\20\1\4\6\20\1\23",
			"\1\26",
			"",
			"",
			"\1\27",
			"",
			"",
			"",
			"",
			"\1\30",
			"",
			"",
			"",
			"\1\32",
			"\1\33",
			"\1\34",
			"",
			"",
			"\1\35\26\uffff\1\36",
			"",
			"\1\37\4\uffff\1\40",
			"",
			"\1\41",
			"\1\42",
			"",
			"",
			"\1\43\5\uffff\1\44",
			"\1\45",
			"\1\46",
			"",
			"",
			"",
			"",
			"\1\47",
			"\1\50",
			"\1\51",
			"\1\52",
			"\1\53",
			"\1\54\2\uffff\1\55",
			"\1\56",
			"\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
			"\1\60",
			"\1\61",
			"\1\62",
			"\1\63",
			"\1\64",
			"\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
			"",
			"\1\66",
			"\1\67",
			"\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
			"\1\71",
			"\1\72",
			"",
			"\1\73",
			"\1\74",
			"",
			"\1\75",
			"\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
			"\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
			"\1\100",
			"\1\101",
			"",
			"",
			"\1\102",
			"\1\103",
			"\1\104",
			"\1\105",
			"\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
			"\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
			"",
			""
	};

	static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
	static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
	static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
	static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
	static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
	static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
	static final short[][] DFA8_transition;

	static {
		int numStates = DFA8_transitionS.length;
		DFA8_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
		}
	}

	protected class DFA8 extends DFA {

		public DFA8(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 8;
			this.eot = DFA8_eot;
			this.eof = DFA8_eof;
			this.min = DFA8_min;
			this.max = DFA8_max;
			this.accept = DFA8_accept;
			this.special = DFA8_special;
			this.transition = DFA8_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( FALSE | LBRACK | RBRACK | TRUE | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | ID | STRING | BIGSTRING_NO_NL | BIGSTRING | ANONYMOUS_TEMPLATE | COMMENT | LINE_COMMENT | WS );";
		}
	}

}
