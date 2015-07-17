// $ANTLR Sun-2.7.7(NoEx) (2006-01-29): "DisScanner.g" -> "DisScanner.java"$

package org.netbeans.modules.cnd.asm.base.generated;

import org.netbeans.modules.cnd.asm.base.dis.*;
import org.netbeans.modules.cnd.asm.base.syntax.*;


import java.io.InputStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.antlr.TokenStreamIOException;
import org.netbeans.modules.cnd.antlr.TokenStreamRecognitionException;
import org.netbeans.modules.cnd.antlr.CharStreamIOException;
import org.netbeans.modules.cnd.antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import org.netbeans.modules.cnd.antlr.CharScannerNoEx;
import org.netbeans.modules.cnd.antlr.InputBuffer;
import org.netbeans.modules.cnd.antlr.ByteBuffer;
import org.netbeans.modules.cnd.antlr.CharBuffer;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.CommonToken;
import org.netbeans.modules.cnd.antlr.RecognitionException;
import org.netbeans.modules.cnd.antlr.NoViableAltForCharException;
import org.netbeans.modules.cnd.antlr.MismatchedCharException;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.ANTLRHashString;
import org.netbeans.modules.cnd.antlr.LexerSharedInputState;
import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;
import org.netbeans.modules.cnd.antlr.SemanticException;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings({"unchecked", "cast", "fallthrough"})

public class DisScanner extends org.netbeans.modules.cnd.antlr.CharScannerNoEx implements DisScannerTokenTypes, TokenStream
, AntlrScanner {

    private enum InternalState {
        INITAL, OP_CODES, INSTRUCTIONS;
    };
   
    private InternalState intState = InternalState.INITAL;

    private String commentLiterals = ";/!";

    private PartState partState =  AntlrScanner.PartState.DEFAULT;

    public void setCommentLiterals(String commentLiterals) {
        this.commentLiterals = commentLiterals;
    }

    public void setIntState(Object state) {
        if (state instanceof InternalState) {
            intState = (InternalState) state;
        }
    }

    public Object getIntState() {
        return intState;
    }

    public PartState getPartState() {
        return partState;
    }

    public int getOffset() {
        return offset;
    }

    public Token createToken(int type) {
        Token t = new AntlrToken();
        t.setType(type);
        return t;
    }

    public void resetText() {
        super.resetText();
        tokenStartOffset = offset;
    }
   
    public void consume() {
        super.consume();
        if (guessing == 0) {
            offset++;
        }
    }

    public int mark() {
        mkOffset = offset;
        return super.mark(); 
    }

    public void rewind(int mark) {
        super.rewind(mark);
        offset = mkOffset;
    }
   

    int offset = 0;
    int tokenStartOffset = 0;
    int mkOffset = 0;

    protected Token makeToken(int t) {
        Token tok = super.makeToken(t);
        AntlrToken k = (AntlrToken)tok;
        k.setStartOffset(tokenStartOffset);
        k.setEndOffset(offset);      
        return k;
    }
    
    private void deferredNewline() 
    {
        super.newline();
    }
   
public DisScanner(InputStream in) {
	this(new ByteBuffer(in));
}
public DisScanner(Reader in) {
	this(new CharBuffer(in));
}
public DisScanner(InputBuffer ib) {
	this(new LexerSharedInputState(), ib);
}
public DisScanner(char buf[]) {
	this(new LexerSharedInputState(), new CharBuffer(buf));
}
public DisScanner(LexerSharedInputState state, InputBuffer ib) {
	super(ib);
	caseSensitiveLiterals = true;
	setCaseSensitive(true);
	literals = LITERALS_TABLE;
}
private static final Map<ANTLRHashString, Integer> LITERALS_TABLE;
static {
	LITERALS_TABLE = new HashMap<ANTLRHashString, Integer>(256);
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		setCommitToPath(false);
		resetText();
		loop0:  while (true) {
			char LA1_1 = LA(1);
			char LA2_1 = LA(2);
			
			switch ( LA1_1) {
			case '\t':  case '\n':  case '\r':  case ' ':
			{
				mWhitespace(true);
				if (matchError) {break loop0;}
				theRetToken=_returnToken;
				break;
			}
			case '\'':
			{
				mCharLiteral(true);
				if (matchError) {break loop0;}
				theRetToken=_returnToken;
				break;
			}
			case '"':
			{
				mStingLiteral(true);
				if (matchError) {break loop0;}
				theRetToken=_returnToken;
				break;
			}
			case '!':  case '#':  case '/':  case ';':
			{
				mComment(true);
				if (matchError) {break loop0;}
				theRetToken=_returnToken;
				break;
			}
			case '.':  case '0':  case '1':  case '2':
			case '3':  case '4':  case '5':  case '6':
			case '7':  case '8':  case '9':  case 'A':
			case 'B':  case 'C':  case 'D':  case 'E':
			case 'F':  case 'G':  case 'H':  case 'I':
			case 'J':  case 'K':  case 'L':  case 'M':
			case 'N':  case 'O':  case 'P':  case 'Q':
			case 'R':  case 'S':  case 'T':  case 'U':
			case 'V':  case 'W':  case 'X':  case 'Y':
			case 'Z':  case '_':  case 'a':  case 'b':
			case 'c':  case 'd':  case 'e':  case 'f':
			case 'g':  case 'h':  case 'i':  case 'j':
			case 'k':  case 'l':  case 'm':  case 'n':
			case 'o':  case 'p':  case 'q':  case 'r':
			case 's':  case 't':  case 'u':  case 'v':
			case 'w':  case 'x':  case 'y':  case 'z':
			case '~':
			{
				mIdent(true);
				if (matchError) {break loop0;}
				theRetToken=_returnToken;
				break;
			}
			default:
				if ((LA1_1=='$') && (_tokenSet_0.member(LA2_1))) {
					mNumberOperand(true);
					if (matchError) {break loop0;}
					theRetToken=_returnToken;
				}
				else if ((LA1_1=='%') && (_tokenSet_1.member(LA2_1))) {
					mRegister(true);
					if (matchError) {break loop0;}
					theRetToken=_returnToken;
				}
				else if ((_tokenSet_2.member(LA1_1)) && (true)) {
					mMark(true);
					if (matchError) {break loop0;}
					theRetToken=_returnToken;
				}
			else {
				if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {consume(); continue tryAgain;}
			}
			}
			if ( _returnToken==null ) continue tryAgain; // found SKIP token
			return _returnToken;
		}
		if (matchError) {
			throw new TokenStreamRecognitionException(matchException);
		}
	}
}

	//Call mode always false
	protected final void mCOMMA(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMMA;
		int _saveIndex;
		
		match(',');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mSTAR(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR;
		int _saveIndex;
		
		match('*');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mAT(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = AT;
		int _saveIndex;
		
		match('@');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mEQ(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EQ;
		int _saveIndex;
		
		match('=');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mPLUS(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS;
		int _saveIndex;
		
		match('+');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode normal
	protected final void mTILDE(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = TILDE;
		int _saveIndex;
		
		match('~');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mFLSQUARE(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = FLSQUARE;
		int _saveIndex;
		
		match('{');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mFRSQUARE(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = FRSQUARE;
		int _saveIndex;
		
		match('}');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mDOLLAR(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOLLAR;
		int _saveIndex;
		
		match('$');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mPERCENT(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PERCENT;
		int _saveIndex;
		
		match('%');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mCOLON(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COLON;
		int _saveIndex;
		
		match(':');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mBITWISEOR(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BITWISEOR;
		int _saveIndex;
		
		match('^');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mQUESTIONMARK(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUESTIONMARK;
		int _saveIndex;
		
		match('?');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mAMPERSAND(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = AMPERSAND;
		int _saveIndex;
		
		match('&');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mLESS(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LESS;
		int _saveIndex;
		
		match('<');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mOR(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = OR;
		int _saveIndex;
		
		match('|');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mUPPER(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = UPPER;
		int _saveIndex;
		
		match('>');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mMINUS(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS;
		int _saveIndex;
		
		match('-');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mLPAREN(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LPAREN;
		int _saveIndex;
		
		match('(');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mRPAREN(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RPAREN;
		int _saveIndex;
		
		match(')');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mLSPAREN(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LSPAREN;
		int _saveIndex;
		
		match('[');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always false
	protected final void mRSPAREN(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RSPAREN;
		int _saveIndex;
		
		match(']');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_returnToken = makeToken(_ttype);
			// Const text, no need to set text
		}
	}
	
	//Call mode always true
	public final void mMark(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Mark;
		int _saveIndex;
		
		switch ( LA(1)) {
		case ']':
		{
			mRSPAREN(false);
			if (matchError) {return ;}
			break;
		}
		case '[':
		{
			mLSPAREN(false);
			if (matchError) {return ;}
			break;
		}
		case ')':
		{
			mRPAREN(false);
			if (matchError) {return ;}
			break;
		}
		case '(':
		{
			mLPAREN(false);
			if (matchError) {return ;}
			break;
		}
		case '-':
		{
			mMINUS(false);
			if (matchError) {return ;}
			break;
		}
		case '+':
		{
			mPLUS(false);
			if (matchError) {return ;}
			break;
		}
		case '@':
		{
			mAT(false);
			if (matchError) {return ;}
			break;
		}
		case '*':
		{
			mSTAR(false);
			if (matchError) {return ;}
			break;
		}
		case ',':
		{
			mCOMMA(false);
			if (matchError) {return ;}
			break;
		}
		case '=':
		{
			mEQ(false);
			if (matchError) {return ;}
			break;
		}
		case '<':
		{
			mLESS(false);
			if (matchError) {return ;}
			break;
		}
		case '>':
		{
			mUPPER(false);
			if (matchError) {return ;}
			break;
		}
		case '$':
		{
			mDOLLAR(false);
			if (matchError) {return ;}
			break;
		}
		case '^':
		{
			mBITWISEOR(false);
			if (matchError) {return ;}
			break;
		}
		case '&':
		{
			mAMPERSAND(false);
			if (matchError) {return ;}
			break;
		}
		case '?':
		{
			mQUESTIONMARK(false);
			if (matchError) {return ;}
			break;
		}
		case '%':
		{
			mPERCENT(false);
			if (matchError) {return ;}
			break;
		}
		case '}':
		{
			mFRSQUARE(false);
			if (matchError) {return ;}
			break;
		}
		case '{':
		{
			mFLSQUARE(false);
			if (matchError) {return ;}
			break;
		}
		case '|':
		{
			mOR(false);
			if (matchError) {return ;}
			break;
		}
		case ':':
		{
			mCOLON(false);
			if (matchError) {return ;}
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mDigit(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Digit;
		int _saveIndex;
		
		matchRange('0','9');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mHexDigit(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HexDigit;
		int _saveIndex;
		
		switch ( LA(1)) {
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			mDigit(false);
			if (matchError) {return ;}
			break;
		}
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':
		{
			consume();
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':
		{
			consume();
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode normal
	protected final void mOctDigit(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = OctDigit;
		int _saveIndex;
		
		matchRange('0','7');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mBinDigit(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BinDigit;
		int _saveIndex;
		
		switch ( LA(1)) {
		case '0':
		{
			consume();
			break;
		}
		case '1':
		{
			consume();
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mDecIntegerLiteral(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DecIntegerLiteral;
		int _saveIndex;
		
		switch ( LA(1)) {
		case '0':
		{
			consume();
			break;
		}
		case '1':  case '2':  case '3':  case '4':
		case '5':  case '6':  case '7':  case '8':
		case '9':
		{
			{
			matchRange('1','9');
			if (matchError) {return ;}
			}
			{
			_loop31:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					consume();
				}
				else {
					break _loop31;
				}
				
			} while (true);
			}
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mHexIntegerLiteral(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HexIntegerLiteral;
		int _saveIndex;
		
		match('0');
		if (matchError) {return ;}
		{
		switch ( LA(1)) {
		case 'x':
		{
			consume();
			break;
		}
		case 'X':
		{
			consume();
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		}
		{
		int _cnt35=0;
		_loop35:
		do {
			if ((_tokenSet_3.member(LA(1)))) {
				mHexDigit(false);
				if (matchError) {return ;}
			}
			else {
				if ( _cnt35>=1 ) { break _loop35; } else {matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}}
			}
			
			_cnt35++;
		} while (true);
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mBinIntegerLiteral(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BinIntegerLiteral;
		int _saveIndex;
		
		match('0');
		if (matchError) {return ;}
		{
		switch ( LA(1)) {
		case 'b':
		{
			consume();
			break;
		}
		case 'B':
		{
			consume();
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		}
		{
		int _cnt39=0;
		_loop39:
		do {
			if ((LA(1)=='0'||LA(1)=='1')) {
				mBinDigit(false);
				if (matchError) {return ;}
			}
			else {
				if ( _cnt39>=1 ) { break _loop39; } else {matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}}
			}
			
			_cnt39++;
		} while (true);
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode normal
	protected final void mIntegerLiteral(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = IntegerLiteral;
		int _saveIndex;
		
		char LA1_2 = LA(1);
		char LA2_2 = LA(2);
		char LA3_2 = LA(3);
		
		if ((LA1_2=='0') && (LA2_2=='B'||LA2_2=='b') && (LA3_2=='0'||LA3_2=='1')) {
			mBinIntegerLiteral(false);
			if (matchError) {return ;}
		}
		else if (((LA1_2 >= '0' && LA1_2 <= '9')) && (LA2_2=='b'||LA2_2=='f') && (true)) {
			{
			if (!(LA(2)=='b' || LA(2)=='f'))
			{matchError=true;
			matchException = new SemanticException("LA(2)=='b' || LA(2)=='f'");
			if (matchError) {return ;}}
			mDigit(false);
			if (matchError) {return ;}
			{
			switch ( LA(1)) {
			case 'b':
			{
				consume();
				break;
			}
			case 'f':
			{
				consume();
				break;
			}
			default:
			{
				matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
			}
			}
			}
			_ttype = DigitLabel;
			}
		}
		else if ((LA1_2=='0') && (LA2_2=='X'||LA2_2=='x')) {
			mHexIntegerLiteral(false);
			if (matchError) {return ;}
		}
		else if (((LA1_2 >= '0' && LA1_2 <= '9')) && (true)) {
			mDecIntegerLiteral(false);
			if (matchError) {return ;}
		}
		else {
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always true
	public final void mNumberOperand(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NumberOperand;
		int _saveIndex;
		
		match('$');
		if (matchError) {return ;}
		{
		switch ( LA(1)) {
		case '+':
		{
			consume();
			break;
		}
		case '-':
		{
			consume();
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':  case 'A':  case 'B':
		case 'C':  case 'D':  case 'E':  case 'F':
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':
		{
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		}
		{
		char LA1_3 = LA(1);
		char LA2_3 = LA(2);
		
		if ((LA1_3=='0') && (LA2_3=='x')) {
			consume();
			consume();
		}
		else if ((_tokenSet_3.member(LA1_3)) && (true)) {
		}
		else {
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		
		}
		{
		int _cnt47=0;
		_loop47:
		do {
			if ((_tokenSet_3.member(LA(1)))) {
				mHexDigit(false);
				if (matchError) {return ;}
			}
			else {
				if ( _cnt47>=1 ) { break _loop47; } else {matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}}
			}
			
			_cnt47++;
		} while (true);
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode normal
	protected final void mExponent(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Exponent;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'E':
		{
			consume();
			break;
		}
		case 'e':
		{
			consume();
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		}
		{
		switch ( LA(1)) {
		case '-':
		{
			consume();
			break;
		}
		case '+':
		{
			consume();
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		}
		{
		int _cnt52=0;
		_loop52:
		do {
			if (((LA(1) >= '0' && LA(1) <= '9'))) {
				consume();
			}
			else {
				if ( _cnt52>=1 ) { break _loop52; } else {matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}}
			}
			
			_cnt52++;
		} while (true);
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mEscape(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Escape;
		int _saveIndex;
		
		match('\\');
		if (matchError) {return ;}
		{
		switch ( LA(1)) {
		case 'a':
		{
			consume();
			break;
		}
		case 'b':
		{
			consume();
			break;
		}
		case 'f':
		{
			consume();
			break;
		}
		case 'n':
		{
			consume();
			break;
		}
		case 'r':
		{
			consume();
			break;
		}
		case 't':
		{
			consume();
			break;
		}
		case 'v':
		{
			consume();
			break;
		}
		case '"':
		{
			consume();
			break;
		}
		case '\'':
		{
			consume();
			break;
		}
		case '\\':
		{
			consume();
			break;
		}
		case '?':
		{
			consume();
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		{
			{
			matchRange('0','3');
			if (matchError) {return ;}
			}
			{
			char LA1_4 = LA(1);
			char LA2_4 = LA(2);
			char LA3_4 = LA(3);
			
			if (((LA1_4 >= '0' && LA1_4 <= '9')) && ((LA2_4 >= '\u0000' && LA2_4 <= '\ufffe')) && (true)) {
				mDigit(false);
				if (matchError) {return ;}
			}
			else if (((LA1_4 >= '\u0000' && LA1_4 <= '\ufffe')) && (true) && (true)) {
			}
			else {
				matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
			}
			
			}
			{
			char LA1_5 = LA(1);
			char LA2_5 = LA(2);
			char LA3_5 = LA(3);
			
			if (((LA1_5 >= '0' && LA1_5 <= '9')) && ((LA2_5 >= '\u0000' && LA2_5 <= '\ufffe')) && (true)) {
				mDigit(false);
				if (matchError) {return ;}
			}
			else if (((LA1_5 >= '\u0000' && LA1_5 <= '\ufffe')) && (true) && (true)) {
			}
			else {
				matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
			}
			
			}
			break;
		}
		case '4':  case '5':  case '6':  case '7':
		{
			{
			matchRange('4','7');
			if (matchError) {return ;}
			}
			{
			char LA1_6 = LA(1);
			char LA2_6 = LA(2);
			char LA3_6 = LA(3);
			
			if (((LA1_6 >= '0' && LA1_6 <= '9')) && ((LA2_6 >= '\u0000' && LA2_6 <= '\ufffe')) && (true)) {
				mDigit(false);
				if (matchError) {return ;}
			}
			else if (((LA1_6 >= '\u0000' && LA1_6 <= '\ufffe')) && (true) && (true)) {
			}
			else {
				matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
			}
			
			}
			break;
		}
		case 'x':
		{
			consume();
			{
			int _cnt61=0;
			_loop61:
			do {
				char LA1_7 = LA(1);
				char LA2_7 = LA(2);
				char LA3_7 = LA(3);
				
				if (((LA1_7 >= '0' && LA1_7 <= '9')) && ((LA2_7 >= '\u0000' && LA2_7 <= '\ufffe')) && (true)) {
					mDigit(false);
					if (matchError) {return ;}
				}
				else if (((LA1_7 >= 'a' && LA1_7 <= 'f')) && ((LA2_7 >= '\u0000' && LA2_7 <= '\ufffe')) && (true)) {
					consume();
				}
				else if (((LA1_7 >= 'A' && LA1_7 <= 'F')) && ((LA2_7 >= '\u0000' && LA2_7 <= '\ufffe')) && (true)) {
					consume();
				}
				else {
					if ( _cnt61>=1 ) { break _loop61; } else {matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}}
				}
				
				_cnt61++;
			} while (true);
			}
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mEndOfLine(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EndOfLine;
		int _saveIndex;
		
		{
		char LA1_8 = LA(1);
		char LA2_8 = LA(2);
		char LA3_8 = LA(3);
		
		if ((LA1_8=='\r') && (LA2_8=='\n') && (true)) {
			match("\r\n");
			if (matchError) {return ;}
			offset--;
		}
		else if ((LA1_8=='\r') && (true) && (true)) {
			consume();
		}
		else if ((LA1_8=='\n')) {
			consume();
		}
		else {
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		
		}
		intState = InternalState.INITAL;
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always true
	public final void mWhitespace(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Whitespace;
		int _saveIndex;
		
		{
		int _cnt66=0;
		_loop66:
		do {
			switch ( LA(1)) {
			case ' ':
			{
				consume();
				break;
			}
			case '\t':
			{
				consume();
				break;
			}
			case '\n':  case '\r':
			{
				mEndOfLine(false);
				if (matchError) {return ;}
				break;
			}
			default:
			{
				if ( _cnt66>=1 ) { break _loop66; } else {matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}}
			}
			}
			_cnt66++;
		} while (true);
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always true
	public final void mCharLiteral(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CharLiteral;
		int _saveIndex;
		
		match('\'');
		if (matchError) {return ;}
		{
		_loop70:
		do {
			if ((LA(1)=='\\')) {
				mEscape(false);
				if (matchError) {return ;}
			}
			else if ((_tokenSet_4.member(LA(1)))) {
				{
				match(_tokenSet_4);
				if (matchError) {return ;}
				}
			}
			else {
				break _loop70;
			}
			
		} while (true);
		}
		match('\'');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always true
	public final void mStingLiteral(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = StingLiteral;
		int _saveIndex;
		
		match('"');
		if (matchError) {return ;}
		mStringLiteralBody(false);
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mStringLiteralBody(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = StringLiteralBody;
		int _saveIndex;
		
		{
		_loop77:
		do {
			if ((LA(1)=='\\')) {
				consume();
				{
				char LA1_9 = LA(1);
				char LA2_9 = LA(2);
				char LA3_9 = LA(3);
				
				if ((LA1_9=='"') && (_tokenSet_5.member(LA2_9)) && (true)) {
					consume();
				}
				else if ((LA1_9=='\\') && (_tokenSet_5.member(LA2_9)) && (true)) {
					consume();
				}
				else if ((LA1_9=='\n'||LA1_9=='\r')) {
					{
					char LA1_10 = LA(1);
					char LA2_10 = LA(2);
					
					if ((LA1_10=='\r') && (LA2_10=='\n')) {
						match("\r\n");
						if (matchError) {return ;}
					}
					else if ((LA1_10=='\r') && (_tokenSet_5.member(LA2_10))) {
						match("\r");
						if (matchError) {return ;}
					}
					else if ((LA1_10=='\n')) {
						match("\n");
						if (matchError) {return ;}
					}
					else {
						matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
					}
					
					}
					deferredNewline();
				}
				else if ((_tokenSet_5.member(LA1_9)) && (true) && (true)) {
				}
				else {
					matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
				}
				
				}
			}
			else if ((_tokenSet_6.member(LA(1)))) {
				{
				match(_tokenSet_6);
				if (matchError) {return ;}
				}
			}
			else {
				break _loop77;
			}
			
		} while (true);
		}
		match('"');
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mIdent_(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Ident_;
		int _saveIndex;
		
		{
		{
		switch ( LA(1)) {
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			consume();
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':
		{
			consume();
			break;
		}
		case '_':
		{
			consume();
			break;
		}
		case '.':
		{
			consume();
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		}
		{
		_loop82:
		do {
			switch ( LA(1)) {
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':  case 'g':  case 'h':
			case 'i':  case 'j':  case 'k':  case 'l':
			case 'm':  case 'n':  case 'o':  case 'p':
			case 'q':  case 'r':  case 's':  case 't':
			case 'u':  case 'v':  case 'w':  case 'x':
			case 'y':  case 'z':
			{
				consume();
				break;
			}
			case 'A':  case 'B':  case 'C':  case 'D':
			case 'E':  case 'F':  case 'G':  case 'H':
			case 'I':  case 'J':  case 'K':  case 'L':
			case 'M':  case 'N':  case 'O':  case 'P':
			case 'Q':  case 'R':  case 'S':  case 'T':
			case 'U':  case 'V':  case 'W':  case 'X':
			case 'Y':  case 'Z':
			{
				consume();
				break;
			}
			case '_':
			{
				consume();
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				consume();
				break;
			}
			case '.':
			{
				consume();
				break;
			}
			default:
			{
				break _loop82;
			}
			}
		} while (true);
		}
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mIncompleteCComment(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = IncompleteCComment;
		int _saveIndex;
		
		match("/*");
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always false
	protected final void mCComment(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CComment;
		int _saveIndex;
		
		mIncompleteCComment(false);
		if (matchError) {return ;}
		partState = AntlrScanner.PartState.IN_COMMENT;
		{
		_loop86:
		do {
			// nongreedy exit test
			if ((LA(1)=='*') && (LA(2)=='/') && (true)) break _loop86;
			char LA1_11 = LA(1);
			char LA2_11 = LA(2);
			char LA3_11 = LA(3);
			
			if ((LA1_11=='\n'||LA1_11=='\r') && ((LA2_11 >= '\u0000' && LA2_11 <= '\ufffe')) && ((LA3_11 >= '\u0000' && LA3_11 <= '\ufffe'))) {
				mEndOfLine(false);
				if (matchError) {return ;}
				deferredNewline();
			}
			else if (((LA1_11 >= '\u0000' && LA1_11 <= '\ufffe')) && ((LA2_11 >= '\u0000' && LA2_11 <= '\ufffe')) && ((LA3_11 >= '\u0000' && LA3_11 <= '\ufffe'))) {
				matchNot(EOF_CHAR);
				if (matchError) {return ;}
			}
			else {
				break _loop86;
			}
			
		} while (true);
		}
		match("*/");
		if (matchError) {return ;}
		partState = AntlrScanner.PartState.DEFAULT;
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always true
	public final void mComment(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Comment;
		int _saveIndex;
		
		{
		char LA1_12 = LA(1);
		char LA2_12 = LA(2);
		char LA3_12 = LA(3);
		
		if ((LA1_12=='/') && (LA2_12=='*') && ((LA3_12 >= '\u0000' && LA3_12 <= '\ufffe'))) {
			{
			if (!(LA(1)=='/' && LA(2)=='*'))
			{matchError=true;
			matchException = new SemanticException("LA(1)=='/' && LA(2)=='*'");
			if (matchError) {return ;}}
			mCComment(false);
			if (matchError) {return ;}
			}
		}
		else if ((_tokenSet_7.member(LA1_12)) && (true) && (true)) {
			{
			switch ( LA(1)) {
			case '/':
			{
				consume();
				break;
			}
			case ';':
			{
				consume();
				break;
			}
			case '!':
			{
				consume();
				break;
			}
			case '#':
			{
				consume();
				break;
			}
			default:
			{
				matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
			}
			}
			}
			{
			if(commentLiterals.indexOf(LA(0)) == -1) { _ttype = Mark; } 
			else  
			
			{
			_loop94:
			do {
				if ((_tokenSet_5.member(LA(1)))) {
					{
					match(_tokenSet_5);
					if (matchError) {return ;}
					}
				}
				else {
					break _loop94;
				}
				
			} while (true);
			}
			}
		}
		else {
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always true
	public final void mIdent(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Ident;
		int _saveIndex;
		boolean wasLetters = false;
		
		{
		switch ( LA(1)) {
		case '.':  case 'G':  case 'H':  case 'I':
		case 'J':  case 'K':  case 'L':  case 'M':
		case 'N':  case 'O':  case 'P':  case 'Q':
		case 'R':  case 'S':  case 'T':  case 'U':
		case 'V':  case 'W':  case 'X':  case 'Y':
		case 'Z':  case '_':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':  case '~':
		{
			{
			{
			switch ( LA(1)) {
			case 'g':  case 'h':  case 'i':  case 'j':
			case 'k':  case 'l':  case 'm':  case 'n':
			case 'o':  case 'p':  case 'q':  case 'r':
			case 's':  case 't':  case 'u':  case 'v':
			case 'w':  case 'x':  case 'y':  case 'z':
			{
				consume();
				break;
			}
			case 'G':  case 'H':  case 'I':  case 'J':
			case 'K':  case 'L':  case 'M':  case 'N':
			case 'O':  case 'P':  case 'Q':  case 'R':
			case 'S':  case 'T':  case 'U':  case 'V':
			case 'W':  case 'X':  case 'Y':  case 'Z':
			{
				consume();
				break;
			}
			case '.':
			{
				consume();
				break;
			}
			case '_':
			{
				consume();
				break;
			}
			case '~':
			{
				consume();
				break;
			}
			default:
			{
				matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
			}
			}
			}
			wasLetters = true;
			}
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':  case 'A':  case 'B':
		case 'C':  case 'D':  case 'E':  case 'F':
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':
		{
			mHexDigit(false);
			if (matchError) {return ;}
			break;
		}
		default:
		{
			matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
		}
		}
		}
		{
		_loop102:
		do {
			switch ( LA(1)) {
			case '+':  case '.':  case 'G':  case 'H':
			case 'I':  case 'J':  case 'K':  case 'L':
			case 'M':  case 'N':  case 'O':  case 'P':
			case 'Q':  case 'R':  case 'S':  case 'T':
			case 'U':  case 'V':  case 'W':  case 'X':
			case 'Y':  case 'Z':  case '_':  case 'g':
			case 'h':  case 'i':  case 'j':  case 'k':
			case 'l':  case 'm':  case 'n':  case 'o':
			case 'p':  case 'q':  case 'r':  case 's':
			case 't':  case 'u':  case 'v':  case 'w':
			case 'x':  case 'y':  case 'z':
			{
				{
				{
				switch ( LA(1)) {
				case 'g':  case 'h':  case 'i':  case 'j':
				case 'k':  case 'l':  case 'm':  case 'n':
				case 'o':  case 'p':  case 'q':  case 'r':
				case 's':  case 't':  case 'u':  case 'v':
				case 'w':  case 'x':  case 'y':  case 'z':
				{
					consume();
					break;
				}
				case 'G':  case 'H':  case 'I':  case 'J':
				case 'K':  case 'L':  case 'M':  case 'N':
				case 'O':  case 'P':  case 'Q':  case 'R':
				case 'S':  case 'T':  case 'U':  case 'V':
				case 'W':  case 'X':  case 'Y':  case 'Z':
				{
					consume();
					break;
				}
				case '.':
				{
					consume();
					break;
				}
				case '_':
				{
					consume();
					break;
				}
				case '+':
				{
					consume();
					break;
				}
				default:
				{
					matchError=true;matchException=new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());if (matchError) {return ;}
				}
				}
				}
				wasLetters = true;
				}
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case 'A':  case 'B':
			case 'C':  case 'D':  case 'E':  case 'F':
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':
			{
				mHexDigit(false);
				if (matchError) {return ;}
				break;
			}
			default:
			{
				break _loop102;
			}
			}
		} while (true);
		}
		{
		if ((LA(1)==':')) {
			{
			match(':');
			if (matchError) {return ;}
			_ttype = LabelInst;  intState = InternalState.OP_CODES; 
			
			}
		}
		else {
			String tempText = new String(text.getBuffer(),_begin,text.length()-_begin);
			if (LA(1) == '(' && LA(2) == ')') {
			consume(); consume(); 
			_ttype = LabelInst;  intState = InternalState.OP_CODES;
			}
			else if ((tempText.startsWith("0x") || Character.isDigit(tempText.charAt(0))) &&
			intState == InternalState.INSTRUCTIONS) {
			_ttype = IntegerLiteral;
			
			}
			else if(!wasLetters && tempText.length() == 2 && LA(1) == ' ' &&
			intState == InternalState.OP_CODES) {                                    
			_ttype = Directive;                                                                      
			}
			else {
			intState = InternalState.INSTRUCTIONS;
			}
			
		}
		
		}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	//Call mode always true
	public final void mRegister(boolean _createToken) {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = Register;
		int _saveIndex;
		
		match('%');
		if (matchError) {return ;}
		mIdent_(false);
		if (matchError) {return ;}
		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			_token = makeToken(_ttype);
			if (_token != null) setTokenText(_token, text.getBuffer(), _begin, text.length()-_begin);
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[1025];
		data[0]=287992881640112128L;
		data[1]=541165879422L;
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = new long[1025];
		data[0]=70368744177664L;
		data[1]=576460745995190270L;
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[1025];
		data[0]=-864621378186248192L;
		data[1]=4035225267868794881L;
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=541165879422L;
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[2048];
		data[0]=-549755813889L;
		data[1]=-268435457L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = new long[2048];
		data[0]=-9217L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = new long[2048];
		data[0]=-17179878401L;
		data[1]=-268435457L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = new long[1025];
		data[0]=576601532741451776L;
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	
	}
