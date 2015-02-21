// $ANTLR 3.5.2 CodeGenerator.g 2015-02-20 23:48:03

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
import org.stringtemplate.v4.misc.*;
import org.stringtemplate.v4.*;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class CodeGenerator extends TreeParser {
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
	public TreeParser[] getDelegates() {
		return new TreeParser[] {};
	}

	// delegators


	public CodeGenerator(TreeNodeStream input) {
		this(input, new RecognizerSharedState());
	}
	public CodeGenerator(TreeNodeStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return CodeGenerator.tokenNames; }
	@Override public String getGrammarFileName() { return "CodeGenerator.g"; }


		String outermostTemplateName;	// name of overall template
		CompiledST outermostImpl;
		Token templateToken;			// overall template token
		String template;  				// overall template text
		ErrorManager errMgr;
		public CodeGenerator(TreeNodeStream input, ErrorManager errMgr, String name, String template, Token templateToken) {
			this(input, new RecognizerSharedState());
			this.errMgr = errMgr;
			this.outermostTemplateName = name;
			this.template = template;
			this.templateToken = templateToken;
		}

		// convience funcs to hide offensive sending of emit messages to
		// CompilationState temp data object.

		public void emit1(CommonTree opAST, short opcode, int arg) {
			template_stack.peek().state.emit1(opAST, opcode, arg);
		}
		public void emit1(CommonTree opAST, short opcode, String arg) {
			template_stack.peek().state.emit1(opAST, opcode, arg);
		}
		public void emit2(CommonTree opAST, short opcode, int arg, int arg2) {
			template_stack.peek().state.emit2(opAST, opcode, arg, arg2);
		}
		public void emit2(CommonTree opAST, short opcode, String s, int arg2) {
			template_stack.peek().state.emit2(opAST, opcode, s, arg2);
		}
	    public void emit(CommonTree opAST, short opcode) {
			template_stack.peek().state.emit(opAST, opcode);
		}
		public void insert(int addr, short opcode, String s) {
			template_stack.peek().state.insert(addr, opcode, s);
		}
		public void setOption(CommonTree id) {
			template_stack.peek().state.setOption(id);
		}
		public void write(int addr, short value) {
			template_stack.peek().state.write(addr,value);
		}
		public int address() { return template_stack.peek().state.ip; }
		public void func(CommonTree id) { template_stack.peek().state.func(templateToken, id); }
		public void refAttr(CommonTree id) { template_stack.peek().state.refAttr(templateToken, id); }
		public int defineString(String s) { return template_stack.peek().state.defineString(s); }



	// $ANTLR start "templateAndEOF"
	// CodeGenerator.g:117:1: templateAndEOF : template[null,null] EOF ;
	public final void templateAndEOF() throws RecognitionException {
		try {
			// CodeGenerator.g:117:16: ( template[null,null] EOF )
			// CodeGenerator.g:117:18: template[null,null] EOF
			{
			pushFollow(FOLLOW_template_in_templateAndEOF50);
			template(null, null);
			state._fsp--;

			match(input,EOF,FOLLOW_EOF_in_templateAndEOF53); 
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
	// $ANTLR end "templateAndEOF"


	protected static class template_scope {
		CompilationState state;
	}
	protected Stack<template_scope> template_stack = new Stack<template_scope>();


	// $ANTLR start "template"
	// CodeGenerator.g:119:1: template[String name, List<FormalArgument> args] returns [CompiledST impl] : chunk ;
	public final CompiledST template(String name, List<FormalArgument> args) throws RecognitionException {
		template_stack.push(new template_scope());
		CompiledST impl = null;



		 	template_stack.peek().state = new CompilationState(errMgr, name, input.getTokenStream());
			impl = template_stack.peek().state.impl;
		 	if ( template_stack.size() == 1 ) outermostImpl = impl;
			impl.defineFormalArgs(args); // make sure args are defined prior to compilation
			if ( name!=null && name.startsWith(Compiler.SUBTEMPLATE_PREFIX) ) {
			    impl.addArg(new FormalArgument("i"));
			    impl.addArg(new FormalArgument("i0"));
		    }
			impl.template = template; // always forget the entire template; char indexes are relative to it

		try {
			// CodeGenerator.g:134:2: ( chunk )
			// CodeGenerator.g:134:4: chunk
			{
			pushFollow(FOLLOW_chunk_in_template77);
			chunk();
			state._fsp--;

			 // finish off the CompiledST result
			        if ( template_stack.peek().state.stringtable!=null ) impl.strings = template_stack.peek().state.stringtable.toArray();
			        impl.codeSize = template_stack.peek().state.ip;
					
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			template_stack.pop();
		}
		return impl;
	}
	// $ANTLR end "template"



	// $ANTLR start "chunk"
	// CodeGenerator.g:141:1: chunk : ( element )* ;
	public final void chunk() throws RecognitionException {
		try {
			// CodeGenerator.g:142:2: ( ( element )* )
			// CodeGenerator.g:142:4: ( element )*
			{
			// CodeGenerator.g:142:4: ( element )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==IF||LA1_0==TEXT||LA1_0==NEWLINE||LA1_0==EXPR||LA1_0==INDENTED_EXPR||LA1_0==REGION) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// CodeGenerator.g:142:4: element
					{
					pushFollow(FOLLOW_element_in_chunk92);
					element();
					state._fsp--;

					}
					break;

				default :
					break loop1;
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
	}
	// $ANTLR end "chunk"



	// $ANTLR start "element"
	// CodeGenerator.g:145:1: element : ( ^( INDENTED_EXPR INDENT compoundElement[$INDENT] ) | compoundElement[null] | ^( INDENTED_EXPR INDENT singleElement ) | singleElement );
	public final void element() throws RecognitionException {
		CommonTree INDENT1=null;
		CommonTree INDENT2=null;

		try {
			// CodeGenerator.g:146:2: ( ^( INDENTED_EXPR INDENT compoundElement[$INDENT] ) | compoundElement[null] | ^( INDENTED_EXPR INDENT singleElement ) | singleElement )
			int alt2=4;
			switch ( input.LA(1) ) {
			case INDENTED_EXPR:
				{
				int LA2_1 = input.LA(2);
				if ( (LA2_1==DOWN) ) {
					int LA2_4 = input.LA(3);
					if ( (LA2_4==INDENT) ) {
						int LA2_5 = input.LA(4);
						if ( (LA2_5==IF||LA2_5==REGION) ) {
							alt2=1;
						}
						else if ( (LA2_5==TEXT||LA2_5==NEWLINE||LA2_5==EXPR) ) {
							alt2=3;
						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 2, 5, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 2, 4, input);
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
							new NoViableAltException("", 2, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case IF:
			case REGION:
				{
				alt2=2;
				}
				break;
			case TEXT:
			case NEWLINE:
			case EXPR:
				{
				alt2=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 2, 0, input);
				throw nvae;
			}
			switch (alt2) {
				case 1 :
					// CodeGenerator.g:146:4: ^( INDENTED_EXPR INDENT compoundElement[$INDENT] )
					{
					match(input,INDENTED_EXPR,FOLLOW_INDENTED_EXPR_in_element105); 
					match(input, Token.DOWN, null); 
					INDENT1=(CommonTree)match(input,INDENT,FOLLOW_INDENT_in_element107); 
					pushFollow(FOLLOW_compoundElement_in_element109);
					compoundElement(INDENT1);
					state._fsp--;

					match(input, Token.UP, null); 

					}
					break;
				case 2 :
					// CodeGenerator.g:147:4: compoundElement[null]
					{
					pushFollow(FOLLOW_compoundElement_in_element117);
					compoundElement(null);
					state._fsp--;

					}
					break;
				case 3 :
					// CodeGenerator.g:148:4: ^( INDENTED_EXPR INDENT singleElement )
					{
					match(input,INDENTED_EXPR,FOLLOW_INDENTED_EXPR_in_element124); 
					match(input, Token.DOWN, null); 
					INDENT2=(CommonTree)match(input,INDENT,FOLLOW_INDENT_in_element126); 
					template_stack.peek().state.indent(INDENT2);
					pushFollow(FOLLOW_singleElement_in_element130);
					singleElement();
					state._fsp--;

					template_stack.peek().state.emit(Bytecode.INSTR_DEDENT);
					match(input, Token.UP, null); 

					}
					break;
				case 4 :
					// CodeGenerator.g:149:4: singleElement
					{
					pushFollow(FOLLOW_singleElement_in_element138);
					singleElement();
					state._fsp--;

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
	// $ANTLR end "element"



	// $ANTLR start "singleElement"
	// CodeGenerator.g:152:1: singleElement : ( exprElement | TEXT | NEWLINE );
	public final void singleElement() throws RecognitionException {
		CommonTree TEXT3=null;
		CommonTree NEWLINE4=null;

		try {
			// CodeGenerator.g:153:2: ( exprElement | TEXT | NEWLINE )
			int alt3=3;
			switch ( input.LA(1) ) {
			case EXPR:
				{
				alt3=1;
				}
				break;
			case TEXT:
				{
				alt3=2;
				}
				break;
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
					// CodeGenerator.g:153:4: exprElement
					{
					pushFollow(FOLLOW_exprElement_in_singleElement149);
					exprElement();
					state._fsp--;

					}
					break;
				case 2 :
					// CodeGenerator.g:154:4: TEXT
					{
					TEXT3=(CommonTree)match(input,TEXT,FOLLOW_TEXT_in_singleElement154); 

							if ( (TEXT3!=null?TEXT3.getText():null).length()>0 ) {
								emit1(TEXT3,Bytecode.INSTR_WRITE_STR, (TEXT3!=null?TEXT3.getText():null));
							}
							
					}
					break;
				case 3 :
					// CodeGenerator.g:161:4: NEWLINE
					{
					NEWLINE4=(CommonTree)match(input,NEWLINE,FOLLOW_NEWLINE_in_singleElement164); 
					emit(NEWLINE4, Bytecode.INSTR_NEWLINE);
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
	// $ANTLR end "singleElement"



	// $ANTLR start "compoundElement"
	// CodeGenerator.g:164:1: compoundElement[CommonTree indent] : ( ifstat[indent] | region[indent] );
	public final void compoundElement(CommonTree indent) throws RecognitionException {
		try {
			// CodeGenerator.g:165:2: ( ifstat[indent] | region[indent] )
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==IF) ) {
				alt4=1;
			}
			else if ( (LA4_0==REGION) ) {
				alt4=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 4, 0, input);
				throw nvae;
			}

			switch (alt4) {
				case 1 :
					// CodeGenerator.g:165:4: ifstat[indent]
					{
					pushFollow(FOLLOW_ifstat_in_compoundElement178);
					ifstat(indent);
					state._fsp--;

					}
					break;
				case 2 :
					// CodeGenerator.g:166:4: region[indent]
					{
					pushFollow(FOLLOW_region_in_compoundElement184);
					region(indent);
					state._fsp--;

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
	// $ANTLR end "compoundElement"



	// $ANTLR start "exprElement"
	// CodeGenerator.g:169:1: exprElement : ^( EXPR expr ( exprOptions )? ) ;
	public final void exprElement() throws RecognitionException {
		CommonTree EXPR5=null;

		 short op = Bytecode.INSTR_WRITE; 
		try {
			// CodeGenerator.g:171:2: ( ^( EXPR expr ( exprOptions )? ) )
			// CodeGenerator.g:171:4: ^( EXPR expr ( exprOptions )? )
			{
			EXPR5=(CommonTree)match(input,EXPR,FOLLOW_EXPR_in_exprElement203); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_expr_in_exprElement205);
			expr();
			state._fsp--;

			// CodeGenerator.g:171:17: ( exprOptions )?
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==OPTIONS) ) {
				alt5=1;
			}
			switch (alt5) {
				case 1 :
					// CodeGenerator.g:171:18: exprOptions
					{
					pushFollow(FOLLOW_exprOptions_in_exprElement208);
					exprOptions();
					state._fsp--;

					op=Bytecode.INSTR_WRITE_OPT;
					}
					break;

			}

			match(input, Token.UP, null); 


					/*
					CompilationState state = template_stack.peek().state;
					CompiledST impl = state.impl;
					if ( impl.instrs[state.ip-1] == Bytecode.INSTR_LOAD_LOCAL ) {
						impl.instrs[state.ip-1] = Bytecode.INSTR_WRITE_LOCAL;
					}
					else {
						emit(EXPR5, op);
					}
					*/
					emit(EXPR5, op);
					
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
	// $ANTLR end "exprElement"


	public static class region_return extends TreeRuleReturnScope {
		public String name;
	};


	// $ANTLR start "region"
	// CodeGenerator.g:187:1: region[CommonTree indent] returns [String name] : ^( REGION ID template[$name,null] ) ;
	public final CodeGenerator.region_return region(CommonTree indent) throws RecognitionException {
		CodeGenerator.region_return retval = new CodeGenerator.region_return();
		retval.start = input.LT(1);

		CommonTree ID6=null;
		CompiledST template7 =null;


			if ( indent!=null ) template_stack.peek().state.indent(indent);

		try {
			// CodeGenerator.g:194:2: ( ^( REGION ID template[$name,null] ) )
			// CodeGenerator.g:194:4: ^( REGION ID template[$name,null] )
			{
			match(input,REGION,FOLLOW_REGION_in_region246); 
			match(input, Token.DOWN, null); 
			ID6=(CommonTree)match(input,ID,FOLLOW_ID_in_region248); 
			retval.name = STGroup.getMangledRegionName(outermostTemplateName, (ID6!=null?ID6.getText():null));
			pushFollow(FOLLOW_template_in_region258);
			template7=template(retval.name, null);
			state._fsp--;


						CompiledST sub = template7;
				        sub.isRegion = true;
				        sub.regionDefType = ST.RegionType.EMBEDDED;
				        sub.templateDefStartToken = ID6.token;
						//sub.dump();
						outermostImpl.addImplicitlyDefinedTemplate(sub);
						emit2(((CommonTree)retval.start), Bytecode.INSTR_NEW, retval.name, 0);
						emit(((CommonTree)retval.start), Bytecode.INSTR_WRITE);
						
			match(input, Token.UP, null); 

			}


				if ( indent!=null ) template_stack.peek().state.emit(Bytecode.INSTR_DEDENT);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "region"


	public static class subtemplate_return extends TreeRuleReturnScope {
		public String name;
		public int nargs;
	};


	// $ANTLR start "subtemplate"
	// CodeGenerator.g:210:1: subtemplate returns [String name, int nargs] : ( ^( SUBTEMPLATE ( ^( ARGS ( ID )+ ) )* template[$name,args] ) | SUBTEMPLATE );
	public final CodeGenerator.subtemplate_return subtemplate() throws RecognitionException {
		CodeGenerator.subtemplate_return retval = new CodeGenerator.subtemplate_return();
		retval.start = input.LT(1);

		CommonTree ID8=null;
		CommonTree SUBTEMPLATE10=null;
		CommonTree SUBTEMPLATE11=null;
		CompiledST template9 =null;


		    retval.name = Compiler.getNewSubtemplateName();
			List<FormalArgument> args = new ArrayList<FormalArgument>();

		try {
			// CodeGenerator.g:215:2: ( ^( SUBTEMPLATE ( ^( ARGS ( ID )+ ) )* template[$name,args] ) | SUBTEMPLATE )
			int alt8=2;
			int LA8_0 = input.LA(1);
			if ( (LA8_0==SUBTEMPLATE) ) {
				int LA8_1 = input.LA(2);
				if ( (LA8_1==DOWN) ) {
					alt8=1;
				}
				else if ( ((LA8_1 >= UP && LA8_1 <= ELSEIF)||(LA8_1 >= BANG && LA8_1 <= EQUALS)||LA8_1==TEXT||(LA8_1 >= ID && LA8_1 <= STRING)||(LA8_1 >= OR && LA8_1 <= AND)||LA8_1==NEWLINE||(LA8_1 >= TRUE && LA8_1 <= FALSE)||(LA8_1 >= EXEC_FUNC && LA8_1 <= ZIP)) ) {
					alt8=2;
				}

				else {
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
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}

			switch (alt8) {
				case 1 :
					// CodeGenerator.g:215:4: ^( SUBTEMPLATE ( ^( ARGS ( ID )+ ) )* template[$name,args] )
					{
					SUBTEMPLATE10=(CommonTree)match(input,SUBTEMPLATE,FOLLOW_SUBTEMPLATE_in_subtemplate291); 
					if ( input.LA(1)==Token.DOWN ) {
						match(input, Token.DOWN, null); 
						// CodeGenerator.g:216:4: ( ^( ARGS ( ID )+ ) )*
						loop7:
						while (true) {
							int alt7=2;
							int LA7_0 = input.LA(1);
							if ( (LA7_0==ARGS) ) {
								alt7=1;
							}

							switch (alt7) {
							case 1 :
								// CodeGenerator.g:216:5: ^( ARGS ( ID )+ )
								{
								match(input,ARGS,FOLLOW_ARGS_in_subtemplate298); 
								match(input, Token.DOWN, null); 
								// CodeGenerator.g:216:12: ( ID )+
								int cnt6=0;
								loop6:
								while (true) {
									int alt6=2;
									int LA6_0 = input.LA(1);
									if ( (LA6_0==ID) ) {
										alt6=1;
									}

									switch (alt6) {
									case 1 :
										// CodeGenerator.g:216:13: ID
										{
										ID8=(CommonTree)match(input,ID,FOLLOW_ID_in_subtemplate301); 
										args.add(new FormalArgument((ID8!=null?ID8.getText():null)));
										}
										break;

									default :
										if ( cnt6 >= 1 ) break loop6;
										EarlyExitException eee = new EarlyExitException(6, input);
										throw eee;
									}
									cnt6++;
								}

								match(input, Token.UP, null); 

								}
								break;

							default :
								break loop7;
							}
						}

						retval.nargs = args.size();
						pushFollow(FOLLOW_template_in_subtemplate318);
						template9=template(retval.name, args);
						state._fsp--;


									CompiledST sub = template9;
									sub.isAnonSubtemplate = true;
							        sub.templateDefStartToken = SUBTEMPLATE10.token;
						            sub.ast = SUBTEMPLATE10;
						            sub.ast.setUnknownTokenBoundaries();
						            sub.tokens = input.getTokenStream();
									//sub.dump();
									outermostImpl.addImplicitlyDefinedTemplate(sub);
									
						match(input, Token.UP, null); 
					}

					}
					break;
				case 2 :
					// CodeGenerator.g:230:4: SUBTEMPLATE
					{
					SUBTEMPLATE11=(CommonTree)match(input,SUBTEMPLATE,FOLLOW_SUBTEMPLATE_in_subtemplate334); 

								CompiledST sub = new CompiledST();
								sub.name = retval.name;
								sub.template = "";
								sub.addArg(new FormalArgument("i"));
								sub.addArg(new FormalArgument("i0"));
								sub.isAnonSubtemplate = true;
						        sub.templateDefStartToken = SUBTEMPLATE11.token;
					            sub.ast = SUBTEMPLATE11;
					            sub.ast.setUnknownTokenBoundaries();
					            sub.tokens = input.getTokenStream();
								//sub.dump();
								outermostImpl.addImplicitlyDefinedTemplate(sub);
								
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
		return retval;
	}
	// $ANTLR end "subtemplate"



	// $ANTLR start "ifstat"
	// CodeGenerator.g:247:1: ifstat[CommonTree indent] : ^(i= 'if' conditional chunk ( ^(eif= 'elseif' ec= conditional chunk ) )* ( ^(el= 'else' chunk ) )? ) ;
	public final void ifstat(CommonTree indent) throws RecognitionException {
		CommonTree i=null;
		CommonTree eif=null;
		CommonTree el=null;
		TreeRuleReturnScope ec =null;


		    /** Tracks address of branch operand (in code block).  It's how
		     *  we backpatch forward references when generating code for IFs.
		     */
		    int prevBranchOperand = -1;
		    /** Branch instruction operands that are forward refs to end of IF.
		     *  We need to update them once we see the endif.
		     */
		    List<Integer> endRefs = new ArrayList<Integer>();
		    if ( indent!=null ) template_stack.peek().state.indent(indent);

		try {
			// CodeGenerator.g:262:2: ( ^(i= 'if' conditional chunk ( ^(eif= 'elseif' ec= conditional chunk ) )* ( ^(el= 'else' chunk ) )? ) )
			// CodeGenerator.g:262:4: ^(i= 'if' conditional chunk ( ^(eif= 'elseif' ec= conditional chunk ) )* ( ^(el= 'else' chunk ) )? )
			{
			i=(CommonTree)match(input,IF,FOLLOW_IF_in_ifstat366); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_conditional_in_ifstat368);
			conditional();
			state._fsp--;


				        prevBranchOperand = address()+1;
				        emit1(i,Bytecode.INSTR_BRF, -1); // write placeholder as branch target
						
			pushFollow(FOLLOW_chunk_in_ifstat378);
			chunk();
			state._fsp--;

			// CodeGenerator.g:268:4: ( ^(eif= 'elseif' ec= conditional chunk ) )*
			loop9:
			while (true) {
				int alt9=2;
				int LA9_0 = input.LA(1);
				if ( (LA9_0==ELSEIF) ) {
					alt9=1;
				}

				switch (alt9) {
				case 1 :
					// CodeGenerator.g:268:6: ^(eif= 'elseif' ec= conditional chunk )
					{
					eif=(CommonTree)match(input,ELSEIF,FOLLOW_ELSEIF_in_ifstat388); 

									endRefs.add(address()+1);
									emit1(eif,Bytecode.INSTR_BR, -1); // br end
									// update previous branch instruction
									write(prevBranchOperand, (short)address());
									prevBranchOperand = -1;
									
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_conditional_in_ifstat402);
					ec=conditional();
					state._fsp--;


							       	prevBranchOperand = address()+1;
							       	// write placeholder as branch target
							       	emit1((ec!=null?((CommonTree)ec.start):null), Bytecode.INSTR_BRF, -1);
									
					pushFollow(FOLLOW_chunk_in_ifstat414);
					chunk();
					state._fsp--;

					match(input, Token.UP, null); 

					}
					break;

				default :
					break loop9;
				}
			}

			// CodeGenerator.g:285:4: ( ^(el= 'else' chunk ) )?
			int alt10=2;
			int LA10_0 = input.LA(1);
			if ( (LA10_0==ELSE) ) {
				alt10=1;
			}
			switch (alt10) {
				case 1 :
					// CodeGenerator.g:285:6: ^(el= 'else' chunk )
					{
					el=(CommonTree)match(input,ELSE,FOLLOW_ELSE_in_ifstat437); 

										endRefs.add(address()+1);
										emit1(el, Bytecode.INSTR_BR, -1); // br end
										// update previous branch instruction
										write(prevBranchOperand, (short)address());
										prevBranchOperand = -1;
										
					if ( input.LA(1)==Token.DOWN ) {
						match(input, Token.DOWN, null); 
						pushFollow(FOLLOW_chunk_in_ifstat451);
						chunk();
						state._fsp--;

						match(input, Token.UP, null); 
					}

					}
					break;

			}

			match(input, Token.UP, null); 


					if ( prevBranchOperand>=0 ) {
						write(prevBranchOperand, (short)address());
					}
			        for (int opnd : endRefs) write(opnd, (short)address());
					
			}


				if ( indent!=null ) template_stack.peek().state.emit(Bytecode.INSTR_DEDENT);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ifstat"


	public static class conditional_return extends TreeRuleReturnScope {
	};


	// $ANTLR start "conditional"
	// CodeGenerator.g:305:1: conditional : ( ^( OR conditional conditional ) | ^( AND conditional conditional ) | ^( BANG conditional ) | expr );
	public final CodeGenerator.conditional_return conditional() throws RecognitionException {
		CodeGenerator.conditional_return retval = new CodeGenerator.conditional_return();
		retval.start = input.LT(1);

		CommonTree OR12=null;
		CommonTree AND13=null;
		CommonTree BANG14=null;

		try {
			// CodeGenerator.g:306:2: ( ^( OR conditional conditional ) | ^( AND conditional conditional ) | ^( BANG conditional ) | expr )
			int alt11=4;
			switch ( input.LA(1) ) {
			case OR:
				{
				alt11=1;
				}
				break;
			case AND:
				{
				alt11=2;
				}
				break;
			case BANG:
				{
				alt11=3;
				}
				break;
			case ID:
			case STRING:
			case TRUE:
			case FALSE:
			case EXEC_FUNC:
			case INCLUDE:
			case INCLUDE_IND:
			case INCLUDE_REGION:
			case INCLUDE_SUPER:
			case INCLUDE_SUPER_REGION:
			case LIST:
			case MAP:
			case PROP:
			case PROP_IND:
			case SUBTEMPLATE:
			case TO_STR:
			case ZIP:
				{
				alt11=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}
			switch (alt11) {
				case 1 :
					// CodeGenerator.g:306:4: ^( OR conditional conditional )
					{
					OR12=(CommonTree)match(input,OR,FOLLOW_OR_in_conditional485); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_conditional_in_conditional487);
					conditional();
					state._fsp--;

					pushFollow(FOLLOW_conditional_in_conditional489);
					conditional();
					state._fsp--;

					match(input, Token.UP, null); 

					emit(OR12, Bytecode.INSTR_OR);
					}
					break;
				case 2 :
					// CodeGenerator.g:307:4: ^( AND conditional conditional )
					{
					AND13=(CommonTree)match(input,AND,FOLLOW_AND_in_conditional499); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_conditional_in_conditional501);
					conditional();
					state._fsp--;

					pushFollow(FOLLOW_conditional_in_conditional503);
					conditional();
					state._fsp--;

					match(input, Token.UP, null); 

					emit(AND13, Bytecode.INSTR_AND);
					}
					break;
				case 3 :
					// CodeGenerator.g:308:4: ^( BANG conditional )
					{
					BANG14=(CommonTree)match(input,BANG,FOLLOW_BANG_in_conditional513); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_conditional_in_conditional515);
					conditional();
					state._fsp--;

					match(input, Token.UP, null); 

					emit(BANG14, Bytecode.INSTR_NOT);
					}
					break;
				case 4 :
					// CodeGenerator.g:309:4: expr
					{
					pushFollow(FOLLOW_expr_in_conditional527);
					expr();
					state._fsp--;

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
		return retval;
	}
	// $ANTLR end "conditional"


	public static class exprOptions_return extends TreeRuleReturnScope {
	};


	// $ANTLR start "exprOptions"
	// CodeGenerator.g:312:1: exprOptions : ^( OPTIONS ( option )* ) ;
	public final CodeGenerator.exprOptions_return exprOptions() throws RecognitionException {
		CodeGenerator.exprOptions_return retval = new CodeGenerator.exprOptions_return();
		retval.start = input.LT(1);

		try {
			// CodeGenerator.g:312:13: ( ^( OPTIONS ( option )* ) )
			// CodeGenerator.g:312:15: ^( OPTIONS ( option )* )
			{
			emit(((CommonTree)retval.start), Bytecode.INSTR_OPTIONS);
			match(input,OPTIONS,FOLLOW_OPTIONS_in_exprOptions541); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// CodeGenerator.g:312:65: ( option )*
				loop12:
				while (true) {
					int alt12=2;
					int LA12_0 = input.LA(1);
					if ( (LA12_0==EQUALS) ) {
						alt12=1;
					}

					switch (alt12) {
					case 1 :
						// CodeGenerator.g:312:65: option
						{
						pushFollow(FOLLOW_option_in_exprOptions543);
						option();
						state._fsp--;

						}
						break;

					default :
						break loop12;
					}
				}

				match(input, Token.UP, null); 
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
		return retval;
	}
	// $ANTLR end "exprOptions"



	// $ANTLR start "option"
	// CodeGenerator.g:314:1: option : ^( '=' ID expr ) ;
	public final void option() throws RecognitionException {
		CommonTree ID15=null;

		try {
			// CodeGenerator.g:314:8: ( ^( '=' ID expr ) )
			// CodeGenerator.g:314:10: ^( '=' ID expr )
			{
			match(input,EQUALS,FOLLOW_EQUALS_in_option555); 
			match(input, Token.DOWN, null); 
			ID15=(CommonTree)match(input,ID,FOLLOW_ID_in_option557); 
			pushFollow(FOLLOW_expr_in_option559);
			expr();
			state._fsp--;

			match(input, Token.UP, null); 

			setOption(ID15);
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
	// $ANTLR end "option"



	// $ANTLR start "expr"
	// CodeGenerator.g:316:1: expr : ( ^( ZIP ^( ELEMENTS ( expr )+ ) mapTemplateRef[ne] ) | ^( MAP expr ( mapTemplateRef[1] )+ ) | prop | includeExpr );
	public final void expr() throws RecognitionException {
		CommonTree ZIP16=null;
		CommonTree MAP17=null;

		int nt = 0, ne = 0;
		try {
			// CodeGenerator.g:318:2: ( ^( ZIP ^( ELEMENTS ( expr )+ ) mapTemplateRef[ne] ) | ^( MAP expr ( mapTemplateRef[1] )+ ) | prop | includeExpr )
			int alt15=4;
			switch ( input.LA(1) ) {
			case ZIP:
				{
				alt15=1;
				}
				break;
			case MAP:
				{
				alt15=2;
				}
				break;
			case PROP:
			case PROP_IND:
				{
				alt15=3;
				}
				break;
			case ID:
			case STRING:
			case TRUE:
			case FALSE:
			case EXEC_FUNC:
			case INCLUDE:
			case INCLUDE_IND:
			case INCLUDE_REGION:
			case INCLUDE_SUPER:
			case INCLUDE_SUPER_REGION:
			case LIST:
			case SUBTEMPLATE:
			case TO_STR:
				{
				alt15=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}
			switch (alt15) {
				case 1 :
					// CodeGenerator.g:318:4: ^( ZIP ^( ELEMENTS ( expr )+ ) mapTemplateRef[ne] )
					{
					ZIP16=(CommonTree)match(input,ZIP,FOLLOW_ZIP_in_expr578); 
					match(input, Token.DOWN, null); 
					match(input,ELEMENTS,FOLLOW_ELEMENTS_in_expr581); 
					match(input, Token.DOWN, null); 
					// CodeGenerator.g:318:21: ( expr )+
					int cnt13=0;
					loop13:
					while (true) {
						int alt13=2;
						int LA13_0 = input.LA(1);
						if ( ((LA13_0 >= ID && LA13_0 <= STRING)||(LA13_0 >= TRUE && LA13_0 <= FALSE)||LA13_0==EXEC_FUNC||(LA13_0 >= INCLUDE && LA13_0 <= INCLUDE_SUPER_REGION)||(LA13_0 >= LIST && LA13_0 <= MAP)||(LA13_0 >= PROP && LA13_0 <= PROP_IND)||(LA13_0 >= SUBTEMPLATE && LA13_0 <= ZIP)) ) {
							alt13=1;
						}

						switch (alt13) {
						case 1 :
							// CodeGenerator.g:318:22: expr
							{
							pushFollow(FOLLOW_expr_in_expr584);
							expr();
							state._fsp--;

							ne++;
							}
							break;

						default :
							if ( cnt13 >= 1 ) break loop13;
							EarlyExitException eee = new EarlyExitException(13, input);
							throw eee;
						}
						cnt13++;
					}

					match(input, Token.UP, null); 

					pushFollow(FOLLOW_mapTemplateRef_in_expr591);
					mapTemplateRef(ne);
					state._fsp--;

					match(input, Token.UP, null); 

					emit1(ZIP16, Bytecode.INSTR_ZIP_MAP, ne);
					}
					break;
				case 2 :
					// CodeGenerator.g:320:4: ^( MAP expr ( mapTemplateRef[1] )+ )
					{
					MAP17=(CommonTree)match(input,MAP,FOLLOW_MAP_in_expr603); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_expr_in_expr605);
					expr();
					state._fsp--;

					// CodeGenerator.g:320:15: ( mapTemplateRef[1] )+
					int cnt14=0;
					loop14:
					while (true) {
						int alt14=2;
						int LA14_0 = input.LA(1);
						if ( ((LA14_0 >= INCLUDE && LA14_0 <= INCLUDE_IND)||LA14_0==SUBTEMPLATE) ) {
							alt14=1;
						}

						switch (alt14) {
						case 1 :
							// CodeGenerator.g:320:16: mapTemplateRef[1]
							{
							pushFollow(FOLLOW_mapTemplateRef_in_expr608);
							mapTemplateRef(1);
							state._fsp--;

							nt++;
							}
							break;

						default :
							if ( cnt14 >= 1 ) break loop14;
							EarlyExitException eee = new EarlyExitException(14, input);
							throw eee;
						}
						cnt14++;
					}

					match(input, Token.UP, null); 


							if ( nt>1 ) emit1(MAP17, nt>1?Bytecode.INSTR_ROT_MAP:Bytecode.INSTR_MAP, nt);
							else emit(MAP17, Bytecode.INSTR_MAP);
							
					}
					break;
				case 3 :
					// CodeGenerator.g:325:4: prop
					{
					pushFollow(FOLLOW_prop_in_expr623);
					prop();
					state._fsp--;

					}
					break;
				case 4 :
					// CodeGenerator.g:326:4: includeExpr
					{
					pushFollow(FOLLOW_includeExpr_in_expr628);
					includeExpr();
					state._fsp--;

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
	// $ANTLR end "expr"



	// $ANTLR start "prop"
	// CodeGenerator.g:329:1: prop : ( ^( PROP expr ID ) | ^( PROP_IND expr expr ) );
	public final void prop() throws RecognitionException {
		CommonTree PROP18=null;
		CommonTree ID19=null;
		CommonTree PROP_IND20=null;

		try {
			// CodeGenerator.g:329:5: ( ^( PROP expr ID ) | ^( PROP_IND expr expr ) )
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==PROP) ) {
				alt16=1;
			}
			else if ( (LA16_0==PROP_IND) ) {
				alt16=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}

			switch (alt16) {
				case 1 :
					// CodeGenerator.g:329:7: ^( PROP expr ID )
					{
					PROP18=(CommonTree)match(input,PROP,FOLLOW_PROP_in_prop638); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_expr_in_prop640);
					expr();
					state._fsp--;

					ID19=(CommonTree)match(input,ID,FOLLOW_ID_in_prop642); 
					match(input, Token.UP, null); 

					emit1(PROP18, Bytecode.INSTR_LOAD_PROP, (ID19!=null?ID19.getText():null));
					}
					break;
				case 2 :
					// CodeGenerator.g:330:4: ^( PROP_IND expr expr )
					{
					PROP_IND20=(CommonTree)match(input,PROP_IND,FOLLOW_PROP_IND_in_prop656); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_expr_in_prop658);
					expr();
					state._fsp--;

					pushFollow(FOLLOW_expr_in_prop660);
					expr();
					state._fsp--;

					match(input, Token.UP, null); 

					emit(PROP_IND20, Bytecode.INSTR_LOAD_PROP_IND);
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
	// $ANTLR end "prop"


	public static class mapTemplateRef_return extends TreeRuleReturnScope {
	};


	// $ANTLR start "mapTemplateRef"
	// CodeGenerator.g:333:1: mapTemplateRef[int num_exprs] : ( ^( INCLUDE ID args ) | subtemplate | ^( INCLUDE_IND expr args ) );
	public final CodeGenerator.mapTemplateRef_return mapTemplateRef(int num_exprs) throws RecognitionException {
		CodeGenerator.mapTemplateRef_return retval = new CodeGenerator.mapTemplateRef_return();
		retval.start = input.LT(1);

		CommonTree INCLUDE21=null;
		CommonTree ID23=null;
		CommonTree INCLUDE_IND25=null;
		TreeRuleReturnScope args22 =null;
		TreeRuleReturnScope subtemplate24 =null;
		TreeRuleReturnScope args26 =null;

		try {
			// CodeGenerator.g:334:2: ( ^( INCLUDE ID args ) | subtemplate | ^( INCLUDE_IND expr args ) )
			int alt17=3;
			switch ( input.LA(1) ) {
			case INCLUDE:
				{
				alt17=1;
				}
				break;
			case SUBTEMPLATE:
				{
				alt17=2;
				}
				break;
			case INCLUDE_IND:
				{
				alt17=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 17, 0, input);
				throw nvae;
			}
			switch (alt17) {
				case 1 :
					// CodeGenerator.g:334:4: ^( INCLUDE ID args )
					{
					INCLUDE21=(CommonTree)match(input,INCLUDE,FOLLOW_INCLUDE_in_mapTemplateRef680); 
					match(input, Token.DOWN, null); 
					ID23=(CommonTree)match(input,ID,FOLLOW_ID_in_mapTemplateRef682); 
					for (int i=1; i<=num_exprs; i++) emit(INCLUDE21,Bytecode.INSTR_NULL);
					pushFollow(FOLLOW_args_in_mapTemplateRef692);
					args22=args();
					state._fsp--;

					match(input, Token.UP, null); 


							if ( (args22!=null?((CodeGenerator.args_return)args22).passThru:false) ) emit1(((CommonTree)retval.start), Bytecode.INSTR_PASSTHRU, (ID23!=null?ID23.getText():null));
							if ( (args22!=null?((CodeGenerator.args_return)args22).namedArgs:false) ) emit1(INCLUDE21, Bytecode.INSTR_NEW_BOX_ARGS, (ID23!=null?ID23.getText():null));
							else emit2(INCLUDE21, Bytecode.INSTR_NEW, (ID23!=null?ID23.getText():null), (args22!=null?((CodeGenerator.args_return)args22).n:0)+num_exprs);
							
					}
					break;
				case 2 :
					// CodeGenerator.g:343:4: subtemplate
					{
					pushFollow(FOLLOW_subtemplate_in_mapTemplateRef705);
					subtemplate24=subtemplate();
					state._fsp--;


							if ( (subtemplate24!=null?((CodeGenerator.subtemplate_return)subtemplate24).nargs:0) != num_exprs ) {
					            errMgr.compileTimeError(ErrorType.ANON_ARGUMENT_MISMATCH,
					            						templateToken, (subtemplate24!=null?((CommonTree)subtemplate24.start):null).token, (subtemplate24!=null?((CodeGenerator.subtemplate_return)subtemplate24).nargs:0), num_exprs);
							}
							for (int i=1; i<=num_exprs; i++) emit((subtemplate24!=null?((CommonTree)subtemplate24.start):null),Bytecode.INSTR_NULL);
					        emit2((subtemplate24!=null?((CommonTree)subtemplate24.start):null), Bytecode.INSTR_NEW,
						              (subtemplate24!=null?((CodeGenerator.subtemplate_return)subtemplate24).name:null),
						              num_exprs);
							
					}
					break;
				case 3 :
					// CodeGenerator.g:355:4: ^( INCLUDE_IND expr args )
					{
					INCLUDE_IND25=(CommonTree)match(input,INCLUDE_IND,FOLLOW_INCLUDE_IND_in_mapTemplateRef717); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_expr_in_mapTemplateRef719);
					expr();
					state._fsp--;


								emit(INCLUDE_IND25,Bytecode.INSTR_TOSTR);
								for (int i=1; i<=num_exprs; i++) emit(INCLUDE_IND25,Bytecode.INSTR_NULL);
								
					pushFollow(FOLLOW_args_in_mapTemplateRef729);
					args26=args();
					state._fsp--;


								emit1(INCLUDE_IND25, Bytecode.INSTR_NEW_IND, (args26!=null?((CodeGenerator.args_return)args26).n:0)+num_exprs);
								
					match(input, Token.UP, null); 

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
		return retval;
	}
	// $ANTLR end "mapTemplateRef"


	public static class includeExpr_return extends TreeRuleReturnScope {
	};


	// $ANTLR start "includeExpr"
	// CodeGenerator.g:367:1: includeExpr : ( ^( EXEC_FUNC ID ( expr )? ) | ^( INCLUDE ID args ) | ^( INCLUDE_SUPER ID args ) | ^( INCLUDE_REGION ID ) | ^( INCLUDE_SUPER_REGION ID ) | primary );
	public final CodeGenerator.includeExpr_return includeExpr() throws RecognitionException {
		CodeGenerator.includeExpr_return retval = new CodeGenerator.includeExpr_return();
		retval.start = input.LT(1);

		CommonTree ID27=null;
		CommonTree ID29=null;
		CommonTree INCLUDE30=null;
		CommonTree ID32=null;
		CommonTree INCLUDE_SUPER33=null;
		CommonTree ID34=null;
		CommonTree INCLUDE_REGION35=null;
		CommonTree ID36=null;
		CommonTree INCLUDE_SUPER_REGION37=null;
		TreeRuleReturnScope args28 =null;
		TreeRuleReturnScope args31 =null;

		try {
			// CodeGenerator.g:368:2: ( ^( EXEC_FUNC ID ( expr )? ) | ^( INCLUDE ID args ) | ^( INCLUDE_SUPER ID args ) | ^( INCLUDE_REGION ID ) | ^( INCLUDE_SUPER_REGION ID ) | primary )
			int alt19=6;
			switch ( input.LA(1) ) {
			case EXEC_FUNC:
				{
				alt19=1;
				}
				break;
			case INCLUDE:
				{
				alt19=2;
				}
				break;
			case INCLUDE_SUPER:
				{
				alt19=3;
				}
				break;
			case INCLUDE_REGION:
				{
				alt19=4;
				}
				break;
			case INCLUDE_SUPER_REGION:
				{
				alt19=5;
				}
				break;
			case ID:
			case STRING:
			case TRUE:
			case FALSE:
			case INCLUDE_IND:
			case LIST:
			case SUBTEMPLATE:
			case TO_STR:
				{
				alt19=6;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 19, 0, input);
				throw nvae;
			}
			switch (alt19) {
				case 1 :
					// CodeGenerator.g:368:4: ^( EXEC_FUNC ID ( expr )? )
					{
					match(input,EXEC_FUNC,FOLLOW_EXEC_FUNC_in_includeExpr751); 
					match(input, Token.DOWN, null); 
					ID27=(CommonTree)match(input,ID,FOLLOW_ID_in_includeExpr753); 
					// CodeGenerator.g:368:19: ( expr )?
					int alt18=2;
					int LA18_0 = input.LA(1);
					if ( ((LA18_0 >= ID && LA18_0 <= STRING)||(LA18_0 >= TRUE && LA18_0 <= FALSE)||LA18_0==EXEC_FUNC||(LA18_0 >= INCLUDE && LA18_0 <= INCLUDE_SUPER_REGION)||(LA18_0 >= LIST && LA18_0 <= MAP)||(LA18_0 >= PROP && LA18_0 <= PROP_IND)||(LA18_0 >= SUBTEMPLATE && LA18_0 <= ZIP)) ) {
						alt18=1;
					}
					switch (alt18) {
						case 1 :
							// CodeGenerator.g:368:19: expr
							{
							pushFollow(FOLLOW_expr_in_includeExpr755);
							expr();
							state._fsp--;

							}
							break;

					}

					match(input, Token.UP, null); 

					func(ID27);
					}
					break;
				case 2 :
					// CodeGenerator.g:369:4: ^( INCLUDE ID args )
					{
					INCLUDE30=(CommonTree)match(input,INCLUDE,FOLLOW_INCLUDE_in_includeExpr766); 
					match(input, Token.DOWN, null); 
					ID29=(CommonTree)match(input,ID,FOLLOW_ID_in_includeExpr768); 
					pushFollow(FOLLOW_args_in_includeExpr770);
					args28=args();
					state._fsp--;

					match(input, Token.UP, null); 


							if ( (args28!=null?((CodeGenerator.args_return)args28).passThru:false) ) emit1(((CommonTree)retval.start), Bytecode.INSTR_PASSTHRU, (ID29!=null?ID29.getText():null));
							if ( (args28!=null?((CodeGenerator.args_return)args28).namedArgs:false) ) emit1(INCLUDE30, Bytecode.INSTR_NEW_BOX_ARGS, (ID29!=null?ID29.getText():null));
							else emit2(INCLUDE30, Bytecode.INSTR_NEW, (ID29!=null?ID29.getText():null), (args28!=null?((CodeGenerator.args_return)args28).n:0));
							
					}
					break;
				case 3 :
					// CodeGenerator.g:375:4: ^( INCLUDE_SUPER ID args )
					{
					INCLUDE_SUPER33=(CommonTree)match(input,INCLUDE_SUPER,FOLLOW_INCLUDE_SUPER_in_includeExpr781); 
					match(input, Token.DOWN, null); 
					ID32=(CommonTree)match(input,ID,FOLLOW_ID_in_includeExpr783); 
					pushFollow(FOLLOW_args_in_includeExpr785);
					args31=args();
					state._fsp--;

					match(input, Token.UP, null); 


							if ( (args31!=null?((CodeGenerator.args_return)args31).passThru:false) ) emit1(((CommonTree)retval.start), Bytecode.INSTR_PASSTHRU, (ID32!=null?ID32.getText():null));
							if ( (args31!=null?((CodeGenerator.args_return)args31).namedArgs:false) ) emit1(INCLUDE_SUPER33, Bytecode.INSTR_SUPER_NEW_BOX_ARGS, (ID32!=null?ID32.getText():null));
							else emit2(INCLUDE_SUPER33, Bytecode.INSTR_SUPER_NEW, (ID32!=null?ID32.getText():null), (args31!=null?((CodeGenerator.args_return)args31).n:0));
							
					}
					break;
				case 4 :
					// CodeGenerator.g:381:4: ^( INCLUDE_REGION ID )
					{
					INCLUDE_REGION35=(CommonTree)match(input,INCLUDE_REGION,FOLLOW_INCLUDE_REGION_in_includeExpr796); 
					match(input, Token.DOWN, null); 
					ID34=(CommonTree)match(input,ID,FOLLOW_ID_in_includeExpr798); 
					match(input, Token.UP, null); 


														CompiledST impl =
															Compiler.defineBlankRegion(outermostImpl, ID34.token);
														//impl.dump();
														emit2(INCLUDE_REGION35,Bytecode.INSTR_NEW,impl.name,0);
														
					}
					break;
				case 5 :
					// CodeGenerator.g:387:4: ^( INCLUDE_SUPER_REGION ID )
					{
					INCLUDE_SUPER_REGION37=(CommonTree)match(input,INCLUDE_SUPER_REGION,FOLLOW_INCLUDE_SUPER_REGION_in_includeExpr808); 
					match(input, Token.DOWN, null); 
					ID36=(CommonTree)match(input,ID,FOLLOW_ID_in_includeExpr810); 
					match(input, Token.UP, null); 


							                            String mangled =
							                                STGroup.getMangledRegionName(outermostImpl.name, (ID36!=null?ID36.getText():null));
														emit2(INCLUDE_SUPER_REGION37,Bytecode.INSTR_SUPER_NEW,mangled,0);
														
					}
					break;
				case 6 :
					// CodeGenerator.g:392:4: primary
					{
					pushFollow(FOLLOW_primary_in_includeExpr818);
					primary();
					state._fsp--;

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
		return retval;
	}
	// $ANTLR end "includeExpr"


	public static class primary_return extends TreeRuleReturnScope {
	};


	// $ANTLR start "primary"
	// CodeGenerator.g:395:1: primary : ( ID | STRING | TRUE | FALSE | subtemplate | list | ^( INCLUDE_IND expr args ) | ^( TO_STR expr ) );
	public final CodeGenerator.primary_return primary() throws RecognitionException {
		CodeGenerator.primary_return retval = new CodeGenerator.primary_return();
		retval.start = input.LT(1);

		CommonTree ID38=null;
		CommonTree STRING39=null;
		CommonTree TRUE40=null;
		CommonTree FALSE41=null;
		CommonTree INCLUDE_IND43=null;
		CommonTree TO_STR45=null;
		TreeRuleReturnScope subtemplate42 =null;
		TreeRuleReturnScope args44 =null;

		try {
			// CodeGenerator.g:396:2: ( ID | STRING | TRUE | FALSE | subtemplate | list | ^( INCLUDE_IND expr args ) | ^( TO_STR expr ) )
			int alt20=8;
			switch ( input.LA(1) ) {
			case ID:
				{
				alt20=1;
				}
				break;
			case STRING:
				{
				alt20=2;
				}
				break;
			case TRUE:
				{
				alt20=3;
				}
				break;
			case FALSE:
				{
				alt20=4;
				}
				break;
			case SUBTEMPLATE:
				{
				alt20=5;
				}
				break;
			case LIST:
				{
				alt20=6;
				}
				break;
			case INCLUDE_IND:
				{
				alt20=7;
				}
				break;
			case TO_STR:
				{
				alt20=8;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 20, 0, input);
				throw nvae;
			}
			switch (alt20) {
				case 1 :
					// CodeGenerator.g:396:4: ID
					{
					ID38=(CommonTree)match(input,ID,FOLLOW_ID_in_primary829); 
					refAttr(ID38);
					}
					break;
				case 2 :
					// CodeGenerator.g:397:4: STRING
					{
					STRING39=(CommonTree)match(input,STRING,FOLLOW_STRING_in_primary839); 
					emit1(STRING39,Bytecode.INSTR_LOAD_STR, Misc.strip((STRING39!=null?STRING39.getText():null),1));
					}
					break;
				case 3 :
					// CodeGenerator.g:398:4: TRUE
					{
					TRUE40=(CommonTree)match(input,TRUE,FOLLOW_TRUE_in_primary848); 
					emit(TRUE40, Bytecode.INSTR_TRUE);
					}
					break;
				case 4 :
					// CodeGenerator.g:399:4: FALSE
					{
					FALSE41=(CommonTree)match(input,FALSE,FOLLOW_FALSE_in_primary857); 
					emit(FALSE41, Bytecode.INSTR_FALSE);
					}
					break;
				case 5 :
					// CodeGenerator.g:400:4: subtemplate
					{
					pushFollow(FOLLOW_subtemplate_in_primary866);
					subtemplate42=subtemplate();
					state._fsp--;

					emit2(((CommonTree)retval.start),Bytecode.INSTR_NEW, (subtemplate42!=null?((CodeGenerator.subtemplate_return)subtemplate42).name:null), 0);
					}
					break;
				case 6 :
					// CodeGenerator.g:402:4: list
					{
					pushFollow(FOLLOW_list_in_primary893);
					list();
					state._fsp--;

					}
					break;
				case 7 :
					// CodeGenerator.g:403:4: ^( INCLUDE_IND expr args )
					{
					INCLUDE_IND43=(CommonTree)match(input,INCLUDE_IND,FOLLOW_INCLUDE_IND_in_primary900); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_expr_in_primary905);
					expr();
					state._fsp--;

					emit(INCLUDE_IND43, Bytecode.INSTR_TOSTR);
					pushFollow(FOLLOW_args_in_primary914);
					args44=args();
					state._fsp--;

					emit1(INCLUDE_IND43, Bytecode.INSTR_NEW_IND, (args44!=null?((CodeGenerator.args_return)args44).n:0));
					match(input, Token.UP, null); 

					}
					break;
				case 8 :
					// CodeGenerator.g:407:4: ^( TO_STR expr )
					{
					TO_STR45=(CommonTree)match(input,TO_STR,FOLLOW_TO_STR_in_primary934); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_expr_in_primary936);
					expr();
					state._fsp--;

					match(input, Token.UP, null); 

					emit(TO_STR45, Bytecode.INSTR_TOSTR);
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
		return retval;
	}
	// $ANTLR end "primary"



	// $ANTLR start "arg"
	// CodeGenerator.g:410:1: arg : expr ;
	public final void arg() throws RecognitionException {
		try {
			// CodeGenerator.g:410:5: ( expr )
			// CodeGenerator.g:410:7: expr
			{
			pushFollow(FOLLOW_expr_in_arg949);
			expr();
			state._fsp--;

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
	// $ANTLR end "arg"


	public static class args_return extends TreeRuleReturnScope {
		public int n=0;
		public boolean namedArgs=false;
		public boolean passThru;
	};


	// $ANTLR start "args"
	// CodeGenerator.g:412:1: args returns [int n=0, boolean namedArgs=false, boolean passThru] : ( ( arg )+ | ( ^(eq= '=' ID expr ) )+ ( '...' )? | '...' |);
	public final CodeGenerator.args_return args() throws RecognitionException {
		CodeGenerator.args_return retval = new CodeGenerator.args_return();
		retval.start = input.LT(1);

		CommonTree eq=null;
		CommonTree ID46=null;

		try {
			// CodeGenerator.g:413:2: ( ( arg )+ | ( ^(eq= '=' ID expr ) )+ ( '...' )? | '...' |)
			int alt24=4;
			switch ( input.LA(1) ) {
			case ID:
			case STRING:
			case TRUE:
			case FALSE:
			case EXEC_FUNC:
			case INCLUDE:
			case INCLUDE_IND:
			case INCLUDE_REGION:
			case INCLUDE_SUPER:
			case INCLUDE_SUPER_REGION:
			case LIST:
			case MAP:
			case PROP:
			case PROP_IND:
			case SUBTEMPLATE:
			case TO_STR:
			case ZIP:
				{
				alt24=1;
				}
				break;
			case EQUALS:
				{
				alt24=2;
				}
				break;
			case ELLIPSIS:
				{
				alt24=3;
				}
				break;
			case UP:
				{
				alt24=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 24, 0, input);
				throw nvae;
			}
			switch (alt24) {
				case 1 :
					// CodeGenerator.g:413:4: ( arg )+
					{
					// CodeGenerator.g:413:4: ( arg )+
					int cnt21=0;
					loop21:
					while (true) {
						int alt21=2;
						int LA21_0 = input.LA(1);
						if ( ((LA21_0 >= ID && LA21_0 <= STRING)||(LA21_0 >= TRUE && LA21_0 <= FALSE)||LA21_0==EXEC_FUNC||(LA21_0 >= INCLUDE && LA21_0 <= INCLUDE_SUPER_REGION)||(LA21_0 >= LIST && LA21_0 <= MAP)||(LA21_0 >= PROP && LA21_0 <= PROP_IND)||(LA21_0 >= SUBTEMPLATE && LA21_0 <= ZIP)) ) {
							alt21=1;
						}

						switch (alt21) {
						case 1 :
							// CodeGenerator.g:413:6: arg
							{
							pushFollow(FOLLOW_arg_in_args965);
							arg();
							state._fsp--;

							retval.n++;
							}
							break;

						default :
							if ( cnt21 >= 1 ) break loop21;
							EarlyExitException eee = new EarlyExitException(21, input);
							throw eee;
						}
						cnt21++;
					}

					}
					break;
				case 2 :
					// CodeGenerator.g:414:4: ( ^(eq= '=' ID expr ) )+ ( '...' )?
					{
					emit(((CommonTree)retval.start), Bytecode.INSTR_ARGS); retval.namedArgs =true;
					// CodeGenerator.g:415:3: ( ^(eq= '=' ID expr ) )+
					int cnt22=0;
					loop22:
					while (true) {
						int alt22=2;
						int LA22_0 = input.LA(1);
						if ( (LA22_0==EQUALS) ) {
							alt22=1;
						}

						switch (alt22) {
						case 1 :
							// CodeGenerator.g:415:5: ^(eq= '=' ID expr )
							{
							eq=(CommonTree)match(input,EQUALS,FOLLOW_EQUALS_in_args984); 
							match(input, Token.DOWN, null); 
							ID46=(CommonTree)match(input,ID,FOLLOW_ID_in_args986); 
							pushFollow(FOLLOW_expr_in_args988);
							expr();
							state._fsp--;

							match(input, Token.UP, null); 

							retval.n++; emit1(eq, Bytecode.INSTR_STORE_ARG, defineString((ID46!=null?ID46.getText():null)));
							}
							break;

						default :
							if ( cnt22 >= 1 ) break loop22;
							EarlyExitException eee = new EarlyExitException(22, input);
							throw eee;
						}
						cnt22++;
					}

					// CodeGenerator.g:418:3: ( '...' )?
					int alt23=2;
					int LA23_0 = input.LA(1);
					if ( (LA23_0==ELLIPSIS) ) {
						alt23=1;
					}
					switch (alt23) {
						case 1 :
							// CodeGenerator.g:418:5: '...'
							{
							match(input,ELLIPSIS,FOLLOW_ELLIPSIS_in_args1005); 
							retval.passThru =true;
							}
							break;

					}

					}
					break;
				case 3 :
					// CodeGenerator.g:419:9: '...'
					{
					match(input,ELLIPSIS,FOLLOW_ELLIPSIS_in_args1020); 
					retval.passThru =true; emit(((CommonTree)retval.start), Bytecode.INSTR_ARGS); retval.namedArgs =true;
					}
					break;
				case 4 :
					// CodeGenerator.g:421:3: 
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
		}
		return retval;
	}
	// $ANTLR end "args"


	public static class list_return extends TreeRuleReturnScope {
	};


	// $ANTLR start "list"
	// CodeGenerator.g:423:1: list : ^( LIST ( listElement )* ) ;
	public final CodeGenerator.list_return list() throws RecognitionException {
		CodeGenerator.list_return retval = new CodeGenerator.list_return();
		retval.start = input.LT(1);

		TreeRuleReturnScope listElement47 =null;

		try {
			// CodeGenerator.g:423:5: ( ^( LIST ( listElement )* ) )
			// CodeGenerator.g:423:7: ^( LIST ( listElement )* )
			{
			emit(((CommonTree)retval.start), Bytecode.INSTR_LIST);
			match(input,LIST,FOLLOW_LIST_in_list1040); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// CodeGenerator.g:424:10: ( listElement )*
				loop25:
				while (true) {
					int alt25=2;
					int LA25_0 = input.LA(1);
					if ( ((LA25_0 >= ID && LA25_0 <= STRING)||(LA25_0 >= TRUE && LA25_0 <= FALSE)||LA25_0==EXEC_FUNC||(LA25_0 >= INCLUDE && LA25_0 <= INCLUDE_SUPER_REGION)||(LA25_0 >= LIST && LA25_0 <= NULL)||(LA25_0 >= PROP && LA25_0 <= PROP_IND)||(LA25_0 >= SUBTEMPLATE && LA25_0 <= ZIP)) ) {
						alt25=1;
					}

					switch (alt25) {
					case 1 :
						// CodeGenerator.g:424:11: listElement
						{
						pushFollow(FOLLOW_listElement_in_list1043);
						listElement47=listElement();
						state._fsp--;

						emit((listElement47!=null?((CommonTree)listElement47.start):null), Bytecode.INSTR_ADD);
						}
						break;

					default :
						break loop25;
					}
				}

				match(input, Token.UP, null); 
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
		return retval;
	}
	// $ANTLR end "list"


	public static class listElement_return extends TreeRuleReturnScope {
	};


	// $ANTLR start "listElement"
	// CodeGenerator.g:427:1: listElement : ( expr | NULL );
	public final CodeGenerator.listElement_return listElement() throws RecognitionException {
		CodeGenerator.listElement_return retval = new CodeGenerator.listElement_return();
		retval.start = input.LT(1);

		CommonTree NULL48=null;

		try {
			// CodeGenerator.g:427:13: ( expr | NULL )
			int alt26=2;
			int LA26_0 = input.LA(1);
			if ( ((LA26_0 >= ID && LA26_0 <= STRING)||(LA26_0 >= TRUE && LA26_0 <= FALSE)||LA26_0==EXEC_FUNC||(LA26_0 >= INCLUDE && LA26_0 <= INCLUDE_SUPER_REGION)||(LA26_0 >= LIST && LA26_0 <= MAP)||(LA26_0 >= PROP && LA26_0 <= PROP_IND)||(LA26_0 >= SUBTEMPLATE && LA26_0 <= ZIP)) ) {
				alt26=1;
			}
			else if ( (LA26_0==NULL) ) {
				alt26=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 26, 0, input);
				throw nvae;
			}

			switch (alt26) {
				case 1 :
					// CodeGenerator.g:427:15: expr
					{
					pushFollow(FOLLOW_expr_in_listElement1059);
					expr();
					state._fsp--;

					}
					break;
				case 2 :
					// CodeGenerator.g:427:22: NULL
					{
					NULL48=(CommonTree)match(input,NULL,FOLLOW_NULL_in_listElement1063); 
					emit(NULL48,Bytecode.INSTR_NULL);
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
		return retval;
	}
	// $ANTLR end "listElement"

	// Delegated rules



	public static final BitSet FOLLOW_template_in_templateAndEOF50 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_templateAndEOF53 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_chunk_in_template77 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_element_in_chunk92 = new BitSet(new long[]{0x0040820100400012L});
	public static final BitSet FOLLOW_INDENTED_EXPR_in_element105 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INDENT_in_element107 = new BitSet(new long[]{0x0040000000000010L});
	public static final BitSet FOLLOW_compoundElement_in_element109 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_compoundElement_in_element117 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INDENTED_EXPR_in_element124 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INDENT_in_element126 = new BitSet(new long[]{0x0000020100400000L});
	public static final BitSet FOLLOW_singleElement_in_element130 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_singleElement_in_element138 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_exprElement_in_singleElement149 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_singleElement154 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEWLINE_in_singleElement164 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ifstat_in_compoundElement178 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_region_in_compoundElement184 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_EXPR_in_exprElement203 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_expr_in_exprElement205 = new BitSet(new long[]{0x0008000000000008L});
	public static final BitSet FOLLOW_exprOptions_in_exprElement208 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_REGION_in_region246 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ID_in_region248 = new BitSet(new long[]{0x0040820100400010L});
	public static final BitSet FOLLOW_template_in_region258 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_SUBTEMPLATE_in_subtemplate291 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ARGS_in_subtemplate298 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ID_in_subtemplate301 = new BitSet(new long[]{0x0000000002000008L});
	public static final BitSet FOLLOW_template_in_subtemplate318 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_SUBTEMPLATE_in_subtemplate334 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IF_in_ifstat366 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_conditional_in_ifstat368 = new BitSet(new long[]{0x0040820100400078L});
	public static final BitSet FOLLOW_chunk_in_ifstat378 = new BitSet(new long[]{0x0000000000000068L});
	public static final BitSet FOLLOW_ELSEIF_in_ifstat388 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_conditional_in_ifstat402 = new BitSet(new long[]{0x0040820100400018L});
	public static final BitSet FOLLOW_chunk_in_ifstat414 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_ELSE_in_ifstat437 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_chunk_in_ifstat451 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_OR_in_conditional485 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_conditional_in_conditional487 = new BitSet(new long[]{0x03B37D1866000400L});
	public static final BitSet FOLLOW_conditional_in_conditional489 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_AND_in_conditional499 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_conditional_in_conditional501 = new BitSet(new long[]{0x03B37D1866000400L});
	public static final BitSet FOLLOW_conditional_in_conditional503 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_BANG_in_conditional513 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_conditional_in_conditional515 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_expr_in_conditional527 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_OPTIONS_in_exprOptions541 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_option_in_exprOptions543 = new BitSet(new long[]{0x0000000000001008L});
	public static final BitSet FOLLOW_EQUALS_in_option555 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ID_in_option557 = new BitSet(new long[]{0x03B37D1806000000L});
	public static final BitSet FOLLOW_expr_in_option559 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_ZIP_in_expr578 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ELEMENTS_in_expr581 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_expr_in_expr584 = new BitSet(new long[]{0x03B37D1806000008L});
	public static final BitSet FOLLOW_mapTemplateRef_in_expr591 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_MAP_in_expr603 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_expr_in_expr605 = new BitSet(new long[]{0x00800C0000000000L});
	public static final BitSet FOLLOW_mapTemplateRef_in_expr608 = new BitSet(new long[]{0x00800C0000000008L});
	public static final BitSet FOLLOW_prop_in_expr623 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_includeExpr_in_expr628 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PROP_in_prop638 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_expr_in_prop640 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_ID_in_prop642 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_PROP_IND_in_prop656 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_expr_in_prop658 = new BitSet(new long[]{0x03B37D1806000000L});
	public static final BitSet FOLLOW_expr_in_prop660 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_INCLUDE_in_mapTemplateRef680 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ID_in_mapTemplateRef682 = new BitSet(new long[]{0x03B37D1806001808L});
	public static final BitSet FOLLOW_args_in_mapTemplateRef692 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_subtemplate_in_mapTemplateRef705 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INCLUDE_IND_in_mapTemplateRef717 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_expr_in_mapTemplateRef719 = new BitSet(new long[]{0x03B37D1806001808L});
	public static final BitSet FOLLOW_args_in_mapTemplateRef729 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_EXEC_FUNC_in_includeExpr751 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ID_in_includeExpr753 = new BitSet(new long[]{0x03B37D1806000008L});
	public static final BitSet FOLLOW_expr_in_includeExpr755 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_INCLUDE_in_includeExpr766 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ID_in_includeExpr768 = new BitSet(new long[]{0x03B37D1806001808L});
	public static final BitSet FOLLOW_args_in_includeExpr770 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_INCLUDE_SUPER_in_includeExpr781 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ID_in_includeExpr783 = new BitSet(new long[]{0x03B37D1806001808L});
	public static final BitSet FOLLOW_args_in_includeExpr785 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_INCLUDE_REGION_in_includeExpr796 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ID_in_includeExpr798 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_INCLUDE_SUPER_REGION_in_includeExpr808 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ID_in_includeExpr810 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_primary_in_includeExpr818 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_primary829 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_in_primary839 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TRUE_in_primary848 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FALSE_in_primary857 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subtemplate_in_primary866 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_list_in_primary893 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INCLUDE_IND_in_primary900 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_expr_in_primary905 = new BitSet(new long[]{0x03B37D1806001808L});
	public static final BitSet FOLLOW_args_in_primary914 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_TO_STR_in_primary934 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_expr_in_primary936 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_expr_in_arg949 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arg_in_args965 = new BitSet(new long[]{0x03B37D1806000002L});
	public static final BitSet FOLLOW_EQUALS_in_args984 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ID_in_args986 = new BitSet(new long[]{0x03B37D1806000000L});
	public static final BitSet FOLLOW_expr_in_args988 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_ELLIPSIS_in_args1005 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ELLIPSIS_in_args1020 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LIST_in_list1040 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_listElement_in_list1043 = new BitSet(new long[]{0x03B77D1806000008L});
	public static final BitSet FOLLOW_expr_in_listElement1059 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NULL_in_listElement1063 = new BitSet(new long[]{0x0000000000000002L});
}
