// $ANTLR 3.5.2 C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g 2014-10-08 09:29:40

package org.apache.tapestry5.internal.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PropertyExpressionLexer extends org.apache.tapestry5.internal.antlr.BaseLexer {
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

	// delegates
	// delegators
	public org.apache.tapestry5.internal.antlr.BaseLexer[] getDelegates() {
		return new org.apache.tapestry5.internal.antlr.BaseLexer[] {};
	}

	public PropertyExpressionLexer() {} 
	public PropertyExpressionLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public PropertyExpressionLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g"; }

	// $ANTLR start "INTEGER"
	public final void mINTEGER() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:31:2: ()
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:31:4: 
			{
			this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INTEGER"

	// $ANTLR start "DEREF"
	public final void mDEREF() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:35:2: ()
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:35:4: 
			{
			this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DEREF"

	// $ANTLR start "RANGEOP"
	public final void mRANGEOP() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:39:2: ()
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:39:4: 
			{
			this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RANGEOP"

	// $ANTLR start "DECIMAL"
	public final void mDECIMAL() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:43:2: ()
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:43:4: 
			{
			this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DECIMAL"

	// $ANTLR start "LETTER"
	public final void mLETTER() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:46:2: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LETTER"

	// $ANTLR start "DIGIT"
	public final void mDIGIT() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:48:2: ( '0' .. '9' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DIGIT"

	// $ANTLR start "SIGN"
	public final void mSIGN() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:50:2: ( ( '+' | '-' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SIGN"

	// $ANTLR start "LPAREN"
	public final void mLPAREN() throws RecognitionException {
		try {
			int _type = LPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:51:9: ( '(' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:51:11: '('
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
	// $ANTLR end "LPAREN"

	// $ANTLR start "RPAREN"
	public final void mRPAREN() throws RecognitionException {
		try {
			int _type = RPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:52:9: ( ')' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:52:11: ')'
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
	// $ANTLR end "RPAREN"

	// $ANTLR start "LBRACKET"
	public final void mLBRACKET() throws RecognitionException {
		try {
			int _type = LBRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:53:9: ( '[' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:53:11: '['
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
	// $ANTLR end "LBRACKET"

	// $ANTLR start "RBRACKET"
	public final void mRBRACKET() throws RecognitionException {
		try {
			int _type = RBRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:54:9: ( ']' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:54:11: ']'
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
	// $ANTLR end "RBRACKET"

	// $ANTLR start "COMMA"
	public final void mCOMMA() throws RecognitionException {
		try {
			int _type = COMMA;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:55:7: ( ',' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:55:9: ','
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
	// $ANTLR end "COMMA"

	// $ANTLR start "BANG"
	public final void mBANG() throws RecognitionException {
		try {
			int _type = BANG;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:56:6: ( '!' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:56:8: '!'
			{
			match('!'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BANG"

	// $ANTLR start "LBRACE"
	public final void mLBRACE() throws RecognitionException {
		try {
			int _type = LBRACE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:57:8: ( '{' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:57:10: '{'
			{
			match('{'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LBRACE"

	// $ANTLR start "RBRACE"
	public final void mRBRACE() throws RecognitionException {
		try {
			int _type = RBRACE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:58:8: ( '}' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:58:10: '}'
			{
			match('}'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RBRACE"

	// $ANTLR start "COLON"
	public final void mCOLON() throws RecognitionException {
		try {
			int _type = COLON;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:59:7: ( ':' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:59:9: ':'
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
	// $ANTLR end "COLON"

	// $ANTLR start "QUOTE"
	public final void mQUOTE() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:62:2: ( '\\'' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:62:4: '\\''
			{
			match('\''); 
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "QUOTE"

	// $ANTLR start "A"
	public final void mA() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:67:2: ( ( 'a' | 'A' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "A"

	// $ANTLR start "E"
	public final void mE() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:69:2: ( ( 'e' | 'E' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "E"

	// $ANTLR start "F"
	public final void mF() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:71:2: ( ( 'f' | 'F' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "F"

	// $ANTLR start "H"
	public final void mH() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:73:2: ( ( 'h' | 'H' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "H"

	// $ANTLR start "I"
	public final void mI() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:75:2: ( ( 'i' | 'I' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "I"

	// $ANTLR start "L"
	public final void mL() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:77:2: ( ( 'l' | 'L' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "L"

	// $ANTLR start "N"
	public final void mN() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:79:2: ( ( 'n' | 'N' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "N"

	// $ANTLR start "R"
	public final void mR() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:81:2: ( ( 'r' | 'R' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "R"

	// $ANTLR start "S"
	public final void mS() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:83:2: ( ( 's' | 'S' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "S"

	// $ANTLR start "T"
	public final void mT() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:85:2: ( ( 't' | 'T' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T"

	// $ANTLR start "U"
	public final void mU() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:87:2: ( ( 'u' | 'U' ) )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "U"

	// $ANTLR start "NULL"
	public final void mNULL() throws RecognitionException {
		try {
			int _type = NULL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:91:7: ( N U L L )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:91:9: N U L L
			{
			mN(); 

			mU(); 

			mL(); 

			mL(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NULL"

	// $ANTLR start "TRUE"
	public final void mTRUE() throws RecognitionException {
		try {
			int _type = TRUE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:92:6: ( T R U E )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:92:8: T R U E
			{
			mT(); 

			mR(); 

			mU(); 

			mE(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TRUE"

	// $ANTLR start "FALSE"
	public final void mFALSE() throws RecognitionException {
		try {
			int _type = FALSE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:93:7: ( F A L S E )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:93:9: F A L S E
			{
			mF(); 

			mA(); 

			mL(); 

			mS(); 

			mE(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FALSE"

	// $ANTLR start "THIS"
	public final void mTHIS() throws RecognitionException {
		try {
			int _type = THIS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:94:6: ( T H I S )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:94:8: T H I S
			{
			mT(); 

			mH(); 

			mI(); 

			mS(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "THIS"

	// $ANTLR start "IDENTIFIER"
	public final void mIDENTIFIER() throws RecognitionException {
		try {
			int _type = IDENTIFIER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:97:5: ( JAVA_ID_START ( JAVA_ID_PART )* )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:97:9: JAVA_ID_START ( JAVA_ID_PART )*
			{
			mJAVA_ID_START(); 

			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:97:23: ( JAVA_ID_PART )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0=='$'||(LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'Z')||LA1_0=='_'||(LA1_0 >= 'a' && LA1_0 <= 'z')||(LA1_0 >= '\u00C0' && LA1_0 <= '\u00D6')||(LA1_0 >= '\u00D8' && LA1_0 <= '\u00F6')||(LA1_0 >= '\u00F8' && LA1_0 <= '\u1FFF')||(LA1_0 >= '\u3040' && LA1_0 <= '\u318F')||(LA1_0 >= '\u3300' && LA1_0 <= '\u337F')||(LA1_0 >= '\u3400' && LA1_0 <= '\u3D2D')||(LA1_0 >= '\u4E00' && LA1_0 <= '\u9FFF')||(LA1_0 >= '\uF900' && LA1_0 <= '\uFAFF')) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
					{
					if ( input.LA(1)=='$'||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u3040' && input.LA(1) <= '\u318F')||(input.LA(1) >= '\u3300' && input.LA(1) <= '\u337F')||(input.LA(1) >= '\u3400' && input.LA(1) <= '\u3D2D')||(input.LA(1) >= '\u4E00' && input.LA(1) <= '\u9FFF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFAFF') ) {
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
	// $ANTLR end "IDENTIFIER"

	// $ANTLR start "JAVA_ID_START"
	public final void mJAVA_ID_START() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:102:5: ( '\\u0024' | '\\u0041' .. '\\u005a' | '\\u005f' | '\\u0061' .. '\\u007a' | '\\u00c0' .. '\\u00d6' | '\\u00d8' .. '\\u00f6' | '\\u00f8' .. '\\u00ff' | '\\u0100' .. '\\u1fff' | '\\u3040' .. '\\u318f' | '\\u3300' .. '\\u337f' | '\\u3400' .. '\\u3d2d' | '\\u4e00' .. '\\u9fff' | '\\uf900' .. '\\ufaff' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='$'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u3040' && input.LA(1) <= '\u318F')||(input.LA(1) >= '\u3300' && input.LA(1) <= '\u337F')||(input.LA(1) >= '\u3400' && input.LA(1) <= '\u3D2D')||(input.LA(1) >= '\u4E00' && input.LA(1) <= '\u9FFF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFAFF') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "JAVA_ID_START"

	// $ANTLR start "JAVA_ID_PART"
	public final void mJAVA_ID_PART() throws RecognitionException {
		try {
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:119:5: ( JAVA_ID_START | '\\u0030' .. '\\u0039' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
			{
			if ( input.LA(1)=='$'||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u3040' && input.LA(1) <= '\u318F')||(input.LA(1) >= '\u3300' && input.LA(1) <= '\u337F')||(input.LA(1) >= '\u3400' && input.LA(1) <= '\u3D2D')||(input.LA(1) >= '\u4E00' && input.LA(1) <= '\u9FFF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFAFF') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "JAVA_ID_PART"

	// $ANTLR start "SAFEDEREF"
	public final void mSAFEDEREF() throws RecognitionException {
		try {
			int _type = SAFEDEREF;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:128:2: ( '?.' )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:128:5: '?.'
			{
			match("?."); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SAFEDEREF"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:130:5: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:130:7: ( ' ' | '\\t' | '\\n' | '\\r' )+
			{
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:130:7: ( ' ' | '\\t' | '\\n' | '\\r' )+
			int cnt2=0;
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( ((LA2_0 >= '\t' && LA2_0 <= '\n')||LA2_0=='\r'||LA2_0==' ') ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
					{
					if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
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
					if ( cnt2 >= 1 ) break loop2;
					EarlyExitException eee = new EarlyExitException(2, input);
					throw eee;
				}
				cnt2++;
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

	// $ANTLR start "STRING"
	public final void mSTRING() throws RecognitionException {
		try {
			int _type = STRING;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:135:2: ( QUOTE ( options {greedy=false; } : . )* QUOTE )
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:135:4: QUOTE ( options {greedy=false; } : . )* QUOTE
			{
			mQUOTE(); 

			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:135:10: ( options {greedy=false; } : . )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( (LA3_0=='\'') ) {
					alt3=2;
				}
				else if ( ((LA3_0 >= '\u0000' && LA3_0 <= '&')||(LA3_0 >= '(' && LA3_0 <= '\uFFFF')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:135:37: .
					{
					matchAny(); 
					}
					break;

				default :
					break loop3;
				}
			}

			mQUOTE(); 

			 setText(getText().substring(1, getText().length()-1)); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING"

	// $ANTLR start "NUMBER_OR_RANGEOP"
	public final void mNUMBER_OR_RANGEOP() throws RecognitionException {
		try {
			int _type = NUMBER_OR_RANGEOP;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:148:2: ( ( SIGN )? ( DIGIT )+ ({...}? => '.' ( DIGIT )* |) | SIGN '.' ( DIGIT )+ | '.' ( ( DIGIT )+ | '.' |) )
			int alt11=3;
			switch ( input.LA(1) ) {
			case '+':
			case '-':
				{
				int LA11_1 = input.LA(2);
				if ( ((LA11_1 >= '0' && LA11_1 <= '9')) ) {
					alt11=1;
				}
				else if ( (LA11_1=='.') ) {
					alt11=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 11, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				{
				alt11=1;
				}
				break;
			case '.':
				{
				alt11=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}
			switch (alt11) {
				case 1 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:148:4: ( SIGN )? ( DIGIT )+ ({...}? => '.' ( DIGIT )* |)
					{
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:148:4: ( SIGN )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( (LA4_0=='+'||LA4_0=='-') ) {
						alt4=1;
					}
					switch (alt4) {
						case 1 :
							// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
							{
							if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
								input.consume();
							}
							else {
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:148:10: ( DIGIT )+
					int cnt5=0;
					loop5:
					while (true) {
						int alt5=2;
						int LA5_0 = input.LA(1);
						if ( ((LA5_0 >= '0' && LA5_0 <= '9')) ) {
							alt5=1;
						}

						switch (alt5) {
						case 1 :
							// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
							{
							if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
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
							if ( cnt5 >= 1 ) break loop5;
							EarlyExitException eee = new EarlyExitException(5, input);
							throw eee;
						}
						cnt5++;
					}

					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:149:3: ({...}? => '.' ( DIGIT )* |)
					int alt7=2;
					int LA7_0 = input.LA(1);
					if ( (LA7_0=='.') && (( input.LA(2) != '.' ))) {
						alt7=1;
					}

					switch (alt7) {
						case 1 :
							// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:150:4: {...}? => '.' ( DIGIT )*
							{
							if ( !(( input.LA(2) != '.' )) ) {
								throw new FailedPredicateException(input, "NUMBER_OR_RANGEOP", " input.LA(2) != '.' ");
							}
							match('.'); 
							// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:150:35: ( DIGIT )*
							loop6:
							while (true) {
								int alt6=2;
								int LA6_0 = input.LA(1);
								if ( ((LA6_0 >= '0' && LA6_0 <= '9')) ) {
									alt6=1;
								}

								switch (alt6) {
								case 1 :
									// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
									{
									if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
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

							   _type = DECIMAL; stripLeadingPlus(); 
							}
							break;
						case 2 :
							// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:151:6: 
							{
							  _type = INTEGER;  stripLeadingPlus(); 
							}
							break;

					}

					}
					break;
				case 2 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:154:4: SIGN '.' ( DIGIT )+
					{
					mSIGN(); 

					match('.'); 
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:154:13: ( DIGIT )+
					int cnt8=0;
					loop8:
					while (true) {
						int alt8=2;
						int LA8_0 = input.LA(1);
						if ( ((LA8_0 >= '0' && LA8_0 <= '9')) ) {
							alt8=1;
						}

						switch (alt8) {
						case 1 :
							// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
							{
							if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
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
							if ( cnt8 >= 1 ) break loop8;
							EarlyExitException eee = new EarlyExitException(8, input);
							throw eee;
						}
						cnt8++;
					}

					  _type = DECIMAL;  stripLeadingPlus(); 
					}
					break;
				case 3 :
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:156:4: '.' ( ( DIGIT )+ | '.' |)
					{
					match('.'); 
					// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:157:3: ( ( DIGIT )+ | '.' |)
					int alt10=3;
					switch ( input.LA(1) ) {
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						{
						alt10=1;
						}
						break;
					case '.':
						{
						alt10=2;
						}
						break;
					default:
						alt10=3;
					}
					switch (alt10) {
						case 1 :
							// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:158:4: ( DIGIT )+
							{
							// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:158:4: ( DIGIT )+
							int cnt9=0;
							loop9:
							while (true) {
								int alt9=2;
								int LA9_0 = input.LA(1);
								if ( ((LA9_0 >= '0' && LA9_0 <= '9')) ) {
									alt9=1;
								}

								switch (alt9) {
								case 1 :
									// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:
									{
									if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
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
									if ( cnt9 >= 1 ) break loop9;
									EarlyExitException eee = new EarlyExitException(9, input);
									throw eee;
								}
								cnt9++;
							}

							 _type = DECIMAL; stripLeadingPlus();
							}
							break;
						case 2 :
							// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:159:6: '.'
							{
							match('.'); 
							_type = RANGEOP; 
							}
							break;
						case 3 :
							// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:160:6: 
							{
							_type = DEREF; 
							}
							break;

					}

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NUMBER_OR_RANGEOP"

	@Override
	public void mTokens() throws RecognitionException {
		// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:8: ( LPAREN | RPAREN | LBRACKET | RBRACKET | COMMA | BANG | LBRACE | RBRACE | COLON | NULL | TRUE | FALSE | THIS | IDENTIFIER | SAFEDEREF | WS | STRING | NUMBER_OR_RANGEOP )
		int alt12=18;
		int LA12_0 = input.LA(1);
		if ( (LA12_0=='(') ) {
			alt12=1;
		}
		else if ( (LA12_0==')') ) {
			alt12=2;
		}
		else if ( (LA12_0=='[') ) {
			alt12=3;
		}
		else if ( (LA12_0==']') ) {
			alt12=4;
		}
		else if ( (LA12_0==',') ) {
			alt12=5;
		}
		else if ( (LA12_0=='!') ) {
			alt12=6;
		}
		else if ( (LA12_0=='{') ) {
			alt12=7;
		}
		else if ( (LA12_0=='}') ) {
			alt12=8;
		}
		else if ( (LA12_0==':') ) {
			alt12=9;
		}
		else if ( (LA12_0=='N'||LA12_0=='n') ) {
			int LA12_10 = input.LA(2);
			if ( (LA12_10=='U'||LA12_10=='u') ) {
				int LA12_18 = input.LA(3);
				if ( (LA12_18=='L'||LA12_18=='l') ) {
					int LA12_22 = input.LA(4);
					if ( (LA12_22=='L'||LA12_22=='l') ) {
						int LA12_26 = input.LA(5);
						if ( (LA12_26=='$'||(LA12_26 >= '0' && LA12_26 <= '9')||(LA12_26 >= 'A' && LA12_26 <= 'Z')||LA12_26=='_'||(LA12_26 >= 'a' && LA12_26 <= 'z')||(LA12_26 >= '\u00C0' && LA12_26 <= '\u00D6')||(LA12_26 >= '\u00D8' && LA12_26 <= '\u00F6')||(LA12_26 >= '\u00F8' && LA12_26 <= '\u1FFF')||(LA12_26 >= '\u3040' && LA12_26 <= '\u318F')||(LA12_26 >= '\u3300' && LA12_26 <= '\u337F')||(LA12_26 >= '\u3400' && LA12_26 <= '\u3D2D')||(LA12_26 >= '\u4E00' && LA12_26 <= '\u9FFF')||(LA12_26 >= '\uF900' && LA12_26 <= '\uFAFF')) ) {
							alt12=14;
						}

						else {
							alt12=10;
						}

					}

					else {
						alt12=14;
					}

				}

				else {
					alt12=14;
				}

			}

			else {
				alt12=14;
			}

		}
		else if ( (LA12_0=='T'||LA12_0=='t') ) {
			switch ( input.LA(2) ) {
			case 'R':
			case 'r':
				{
				int LA12_19 = input.LA(3);
				if ( (LA12_19=='U'||LA12_19=='u') ) {
					int LA12_23 = input.LA(4);
					if ( (LA12_23=='E'||LA12_23=='e') ) {
						int LA12_27 = input.LA(5);
						if ( (LA12_27=='$'||(LA12_27 >= '0' && LA12_27 <= '9')||(LA12_27 >= 'A' && LA12_27 <= 'Z')||LA12_27=='_'||(LA12_27 >= 'a' && LA12_27 <= 'z')||(LA12_27 >= '\u00C0' && LA12_27 <= '\u00D6')||(LA12_27 >= '\u00D8' && LA12_27 <= '\u00F6')||(LA12_27 >= '\u00F8' && LA12_27 <= '\u1FFF')||(LA12_27 >= '\u3040' && LA12_27 <= '\u318F')||(LA12_27 >= '\u3300' && LA12_27 <= '\u337F')||(LA12_27 >= '\u3400' && LA12_27 <= '\u3D2D')||(LA12_27 >= '\u4E00' && LA12_27 <= '\u9FFF')||(LA12_27 >= '\uF900' && LA12_27 <= '\uFAFF')) ) {
							alt12=14;
						}

						else {
							alt12=11;
						}

					}

					else {
						alt12=14;
					}

				}

				else {
					alt12=14;
				}

				}
				break;
			case 'H':
			case 'h':
				{
				int LA12_20 = input.LA(3);
				if ( (LA12_20=='I'||LA12_20=='i') ) {
					int LA12_24 = input.LA(4);
					if ( (LA12_24=='S'||LA12_24=='s') ) {
						int LA12_28 = input.LA(5);
						if ( (LA12_28=='$'||(LA12_28 >= '0' && LA12_28 <= '9')||(LA12_28 >= 'A' && LA12_28 <= 'Z')||LA12_28=='_'||(LA12_28 >= 'a' && LA12_28 <= 'z')||(LA12_28 >= '\u00C0' && LA12_28 <= '\u00D6')||(LA12_28 >= '\u00D8' && LA12_28 <= '\u00F6')||(LA12_28 >= '\u00F8' && LA12_28 <= '\u1FFF')||(LA12_28 >= '\u3040' && LA12_28 <= '\u318F')||(LA12_28 >= '\u3300' && LA12_28 <= '\u337F')||(LA12_28 >= '\u3400' && LA12_28 <= '\u3D2D')||(LA12_28 >= '\u4E00' && LA12_28 <= '\u9FFF')||(LA12_28 >= '\uF900' && LA12_28 <= '\uFAFF')) ) {
							alt12=14;
						}

						else {
							alt12=13;
						}

					}

					else {
						alt12=14;
					}

				}

				else {
					alt12=14;
				}

				}
				break;
			default:
				alt12=14;
			}
		}
		else if ( (LA12_0=='F'||LA12_0=='f') ) {
			int LA12_12 = input.LA(2);
			if ( (LA12_12=='A'||LA12_12=='a') ) {
				int LA12_21 = input.LA(3);
				if ( (LA12_21=='L'||LA12_21=='l') ) {
					int LA12_25 = input.LA(4);
					if ( (LA12_25=='S'||LA12_25=='s') ) {
						int LA12_29 = input.LA(5);
						if ( (LA12_29=='E'||LA12_29=='e') ) {
							int LA12_33 = input.LA(6);
							if ( (LA12_33=='$'||(LA12_33 >= '0' && LA12_33 <= '9')||(LA12_33 >= 'A' && LA12_33 <= 'Z')||LA12_33=='_'||(LA12_33 >= 'a' && LA12_33 <= 'z')||(LA12_33 >= '\u00C0' && LA12_33 <= '\u00D6')||(LA12_33 >= '\u00D8' && LA12_33 <= '\u00F6')||(LA12_33 >= '\u00F8' && LA12_33 <= '\u1FFF')||(LA12_33 >= '\u3040' && LA12_33 <= '\u318F')||(LA12_33 >= '\u3300' && LA12_33 <= '\u337F')||(LA12_33 >= '\u3400' && LA12_33 <= '\u3D2D')||(LA12_33 >= '\u4E00' && LA12_33 <= '\u9FFF')||(LA12_33 >= '\uF900' && LA12_33 <= '\uFAFF')) ) {
								alt12=14;
							}

							else {
								alt12=12;
							}

						}

						else {
							alt12=14;
						}

					}

					else {
						alt12=14;
					}

				}

				else {
					alt12=14;
				}

			}

			else {
				alt12=14;
			}

		}
		else if ( (LA12_0=='$'||(LA12_0 >= 'A' && LA12_0 <= 'E')||(LA12_0 >= 'G' && LA12_0 <= 'M')||(LA12_0 >= 'O' && LA12_0 <= 'S')||(LA12_0 >= 'U' && LA12_0 <= 'Z')||LA12_0=='_'||(LA12_0 >= 'a' && LA12_0 <= 'e')||(LA12_0 >= 'g' && LA12_0 <= 'm')||(LA12_0 >= 'o' && LA12_0 <= 's')||(LA12_0 >= 'u' && LA12_0 <= 'z')||(LA12_0 >= '\u00C0' && LA12_0 <= '\u00D6')||(LA12_0 >= '\u00D8' && LA12_0 <= '\u00F6')||(LA12_0 >= '\u00F8' && LA12_0 <= '\u1FFF')||(LA12_0 >= '\u3040' && LA12_0 <= '\u318F')||(LA12_0 >= '\u3300' && LA12_0 <= '\u337F')||(LA12_0 >= '\u3400' && LA12_0 <= '\u3D2D')||(LA12_0 >= '\u4E00' && LA12_0 <= '\u9FFF')||(LA12_0 >= '\uF900' && LA12_0 <= '\uFAFF')) ) {
			alt12=14;
		}
		else if ( (LA12_0=='?') ) {
			alt12=15;
		}
		else if ( ((LA12_0 >= '\t' && LA12_0 <= '\n')||LA12_0=='\r'||LA12_0==' ') ) {
			alt12=16;
		}
		else if ( (LA12_0=='\'') ) {
			alt12=17;
		}
		else if ( (LA12_0=='+'||(LA12_0 >= '-' && LA12_0 <= '.')||(LA12_0 >= '0' && LA12_0 <= '9')) ) {
			alt12=18;
		}

		else {
			NoViableAltException nvae =
				new NoViableAltException("", 12, 0, input);
			throw nvae;
		}

		switch (alt12) {
			case 1 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:10: LPAREN
				{
				mLPAREN(); 

				}
				break;
			case 2 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:17: RPAREN
				{
				mRPAREN(); 

				}
				break;
			case 3 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:24: LBRACKET
				{
				mLBRACKET(); 

				}
				break;
			case 4 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:33: RBRACKET
				{
				mRBRACKET(); 

				}
				break;
			case 5 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:42: COMMA
				{
				mCOMMA(); 

				}
				break;
			case 6 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:48: BANG
				{
				mBANG(); 

				}
				break;
			case 7 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:53: LBRACE
				{
				mLBRACE(); 

				}
				break;
			case 8 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:60: RBRACE
				{
				mRBRACE(); 

				}
				break;
			case 9 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:67: COLON
				{
				mCOLON(); 

				}
				break;
			case 10 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:73: NULL
				{
				mNULL(); 

				}
				break;
			case 11 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:78: TRUE
				{
				mTRUE(); 

				}
				break;
			case 12 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:83: FALSE
				{
				mFALSE(); 

				}
				break;
			case 13 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:89: THIS
				{
				mTHIS(); 

				}
				break;
			case 14 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:94: IDENTIFIER
				{
				mIDENTIFIER(); 

				}
				break;
			case 15 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:105: SAFEDEREF
				{
				mSAFEDEREF(); 

				}
				break;
			case 16 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:115: WS
				{
				mWS(); 

				}
				break;
			case 17 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:118: STRING
				{
				mSTRING(); 

				}
				break;
			case 18 :
				// C:\\Users\\kaosko\\Documents\\personal\\git-tapestry5\\tapestry-core\\src\\main\\antlr\\org\\apache\\tapestry5\\internal\\antlr\\PropertyExpressionLexer.g:1:125: NUMBER_OR_RANGEOP
				{
				mNUMBER_OR_RANGEOP(); 

				}
				break;

		}
	}



}
