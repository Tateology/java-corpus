/*
 * NOTES
 *
 * R303, R406, R417, R427, R428 underscore - added _ to rule (what happened
 * to it?) * R410 sign - had '?' rather than '-'
 * R1209 import-stmt: MISSING a ]
 *
 * check comments regarding deleted for correctness
 *
 * Replace all occurrences of T_EOS with end_of_stmt rule call so there is
 * a way to look ahead at the next token to see if it belongs to the same
 * input stream as the current one.  This serves as a way to detect that an
 * include statement had occured during the lexical phase.
 *
 * TODO add (label)? to all statements...
 *    finished: continue-stmt, end-do-stmt
 *
 */


// added (label)? to any rule for a statement (*_stmt, for the most
// part) because the draft says a label can exist with any statement.
// questions are:
// - what about constructs such as if/else; where can labels all occur?
// - or the masked_elsewhere_stmt rule...


parser grammar FortranParser;


options {
    output=AST;
    language=Java;
    tokenVocab=APTTokenTypes;
}

import FortranParser2;

@header {
/**
 * Copyright (c) 2005, 2006 Los Alamos National Security, LLC.  This
 * material was produced under U.S. Government contract DE-
 * AC52-06NA25396 for Los Alamos National Laboratory (LANL), which is
 * operated by the Los Alamos National Security, LLC (LANS) for the
 * U.S. Department of Energy. The U.S. Government has rights to use,
 * reproduce, and distribute this software. NEITHER THE GOVERNMENT NOR
 * LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
 * LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified to
 * produce derivative works, such modified software should be clearly
 * marked, so as not to confuse it with the version available from
 * LANL.
 *
 * Additionally, this program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

 /**
 *
 * @author Craig E Rasmussen, Christopher D. Rickett, Bryan Rasmussen
 */

 package org.netbeans.modules.cnd.modelimpl.parser.generated;

 import org.netbeans.modules.cnd.modelimpl.parser.*;
 import org.netbeans.modules.cnd.apt.support.APTTokenTypes;

 

}

@members {
    public Stack<String> inputStreams = null;
    public String fileName;

    public FortranParser(String[] args, TokenStream input, String kind,
        String filename) {
        super(input);
        state.ruleMemo = new HashMap[489+1];
        this.action = FortranParserActionFactory.newAction(args, this, kind,
                        filename);
        this.inputStreams = new Stack<String>();

        this.fileName = filename;
    }


    public void initStreamInfo() {
        String nextFileName = null;

        this.inputStreams.push(this.fileName);
        action.start_of_file(this.fileName);
        nextFileName = checkForStartOfFile();
        if(nextFileName != null) {
            action.start_of_file(nextFileName);
        }
    }


    public boolean hasErrorOccurred = false;

    public void reportError(RecognitionException re) {
        super.reportError(re);
        hasErrorOccurred = true;
    }

    public IFortranParserAction getAction() {
        return action;
    }

    /** Provide an action object to implement the AST */
    public IFortranParserAction action = null;

    /* TODO - implement, needed by FortranParserAction */
    public Token getRightIToken() {
        return null;
    }

    /* TODO - implement, may be needed by FortranParserAction */
    public Token getRhsIToken(int i) {
        return null;
    }

    private String checkForStartOfFile() {
        // The current stream is on the top of the stack if it's not empty.
        if(this.inputStreams.empty() == false) {
            if(input.LA(1) != APTTokenTypes.EOF) {
                FortranToken next = (FortranToken)(input.LT(1));
                String tosName = this.inputStreams.peek();
                String nextName = null;
                if(next.getInput() == null) {
                    // This can happen for the generated tokens, such as
                    // __T_ASSIGNMENT_STMT__
                    next = (FortranToken)(input.LT(2));
                    // Sanity check.
                    if(next.getInput() == null) {
                        return null;
                    }
                }
                if(next.getInput() != null) {
                    nextName = next.getInput().getFileName();
                    if(tosName.compareTo(nextName) != 0) {
                        this.inputStreams.push(nextName);
                        return nextName;
                    }
                }
            }
        }

        return null;
    }

    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        // do nothing
    }

}// end members


/*
 * Section 1:
 */

/*
 * Section 2:
 */


/**
 * Got rid of the following rules:
 * program
 * program_unit
 * external_subprogram
 *
 * this was done because Main() should now handle the top level rules
 * to try and reduce the amount of backtracking that must be done!
 * --Rickett, 12.07.06
 *
 * for some reason, leaving these three rules in, even though main()
 * does NOT call them, prevents the parser from failing on the tests:
 * main_program.f03
 * note_6.24.f03
 * it appears to be something with the (program_unit)* part of the
 * program rule.  --12.07.06
 *  --resolved: there's a difference in the code that is generated for
 *              the end_of_stmt rule if these three rules are in there.
 *              to get around this, i modified the end_of_stmt rule.
 *              see it for more details.  --12.11.06
 *
 */

 // R201
 program
     :    program_unit
         ( program_unit )*
     ;

 // R202
 // backtracking needed to resolve prefix (e.g., REAL) ambiguity with main_program (REAL)
 program_unit
 options {backtrack=true; memoize=true; greedy=true;}
     :    main_program
     |    external_subprogram
     |    module
     |    block_data
     ;

 // R203
 // modified to factor optional prefix
 external_subprogram
     :    (prefix)? function_subprogram
     |    subroutine_subprogram
     ;

// R1101
// specification_part made non-optional to remove END ambiguity (as can be empty)
main_program
@init {
    boolean hasProgramStmt = false;
    boolean hasExecutionPart = false;
    boolean hasInternalSubprogramPart = false;
}
    :        {action.main_program__begin();}
        ( program_stmt {hasProgramStmt = true;})?
        specification_part
        ( execution_part {hasExecutionPart = true;})?
        ( internal_subprogram_part {hasInternalSubprogramPart = true;})?
        end_program_stmt
            { action.main_program(hasProgramStmt, hasExecutionPart,
                                  hasInternalSubprogramPart); }
    ;

// added rule so could have one rule for main() to call for attempting
// to match a function subprogram.  the original rule,
// external_subprogram, has (prefix)? for a function_subprogram.
ext_function_subprogram
@init{boolean hasPrefix=false;}
    :   (prefix {hasPrefix=true;})? function_subprogram
            {action.ext_function_subprogram(hasPrefix);}
    ;

// R204
// ERR_CHK 204 see ERR_CHK 207, implicit_part? removed (was after import_stmt*)
specification_part
@init{int numUseStmts=0; int numImportStmts=0; int numDeclConstructs=0;}
    :    ( use_stmt {numUseStmts++;})*
        ( import_stmt {numImportStmts++;})*
        ( declaration_construct {numDeclConstructs++;})*
            {action.specification_part(numUseStmts, numImportStmts,
                                       numDeclConstructs);}
    ;

// R205 implicit_part removed from grammar (see ERR_CHK 207)

// R206 implicit_part_stmt removed from grammar (see ERR_CHK 207)

// R207
// ERR_CHK 207 implicit_stmt must precede all occurences of rules following
// it in text below
// has been removed from grammar so must be done when reducing
declaration_construct
@after {
    action.declaration_construct();
}
    :    entry_stmt
    |    parameter_stmt
    |    format_stmt
    |    implicit_stmt
        // implicit_stmt must precede all occurences of the below
    |    derived_type_def
    |    enum_def
    |    interface_block
    |    procedure_declaration_stmt
    |    specification_stmt
    |    type_declaration_stmt
    |    stmt_function_stmt
    ;

// R208
execution_part
@after {
    action.execution_part();
}
    :    executable_construct
        ( execution_part_construct )*
    ;

// R209
execution_part_construct
@after {
    action.execution_part_construct();
}
    :    executable_construct
    |    format_stmt
    |    entry_stmt
    |    data_stmt
    ;

// R210
internal_subprogram_part
@init{int count = 1;}
    :    contains_stmt
        internal_subprogram
        (internal_subprogram {count += 1;})*
            { action.internal_subprogram_part(count); }
    ;

// R211
// modified to factor optional prefix
internal_subprogram
@after {
    action.internal_subprogram();
}
    :    ( prefix )? function_subprogram
    |    subroutine_subprogram
    ;

// R212
specification_stmt
@after {
    action.specification_stmt();
}
    :    access_stmt
    |    allocatable_stmt
    |    asynchronous_stmt
    |    bind_stmt
    |    common_stmt
    |    data_stmt
    |    dimension_stmt
    |    equivalence_stmt
    |    external_stmt
    |    intent_stmt
    |    intrinsic_stmt
    |    namelist_stmt
    |    optional_stmt
    |    pointer_stmt
    |    protected_stmt
    |    save_stmt
    |    target_stmt
    |    volatile_stmt
    |    value_stmt
    ;

// R213
executable_construct
@after {
    action.executable_construct();
}
    :    action_stmt
    |    associate_construct
    |    case_construct
    |    do_construct
    |    forall_construct
    |    if_construct
    |    select_type_construct
    |    where_construct
    ;


// R214
// C201 (R208) An execution-part shall not contain an end-function-stmt, end-program-stmt, or
//             end-subroutine-stmt.  (But they can be in a branch target statement, which
//             is not in the grammar, so the end-xxx-stmts deleted.)
// TODO continue-stmt is ambiguous with same in end-do, check for label and if
// label matches do-stmt label, then match end-do there
// the original generated rules do not allow the label, so add (label)?
action_stmt
@after {
    action.action_stmt();
}
// Removed backtracking by inserting extra tokens in the stream by the
// prepass that signals whether we have an assignment-stmt, a
// pointer-assignment-stmt, or an arithmetic if.  this approach may work for
// other parts of backtracking also.  however, need to see if there is a way
// to define tokens w/o defining them in the lexer so that the lexer doesn't
// have to add them to it's parsing..  02.05.07
    :    allocate_stmt
    |    assignment_stmt
    |    backspace_stmt
    |    call_stmt
    |    close_stmt
    |    continue_stmt
    |    cycle_stmt
    |    deallocate_stmt
    |    endfile_stmt
    |    exit_stmt
    |    flush_stmt
    |    forall_stmt
    |    goto_stmt
    |    if_stmt
    |   inquire_stmt
    |    nullify_stmt
    |    open_stmt
    |    pointer_assignment_stmt
    |    print_stmt
    |    read_stmt
    |    return_stmt
    |    rewind_stmt
    |    stop_stmt
    |    wait_stmt
    |    where_stmt
    |    write_stmt
    |    arithmetic_if_stmt
    |    computed_goto_stmt
    |   assign_stmt
    |   assigned_goto_stmt
    |   pause_stmt
    ;


// R215
keyword returns [Token tk]
@after {
    action.keyword();
}
    :    name {retval.tk = $name.tk;}
    ;

/*
Section 3:
*/

// R301 character not used

// R302 alphanumeric_character converted to fragment

// R303 underscore inlined

// R304
name returns [Token tk]
    :    T_IDENT        { retval.tk = $T_IDENT; action.name(retval.tk); }
    ;

// R305
// ERR_CHK 305 named_constant replaced by T_IDENT
constant
    :    literal_constant    { action.constant(null); }
    |    T_IDENT                { action.constant($T_IDENT); }
    ;

scalar_constant
@after {
    action.scalar_constant();
}
    :    constant
    ;

// R306
literal_constant
@after {
    action.literal_constant();
}
    :    int_literal_constant
    |    real_literal_constant
    |    complex_literal_constant
    |    logical_literal_constant
    |    char_literal_constant
    |    boz_literal_constant
    ;

// R307 named_constant was name inlined as T_IDENT

// R308
// C302 R308 int_constant shall be of type integer
// inlined integer portion of constant
int_constant
    :    int_literal_constant    { action.int_constant(null); }
    |    T_IDENT                    { action.int_constant($T_IDENT); }
    ;

// R309
// C303 R309 char_constant shall be of type character
// inlined character portion of constant
char_constant
    :    char_literal_constant    { action.int_constant(null); }
    |    T_IDENT                    { action.int_constant($T_IDENT); }
    ;

// R310
intrinsic_operator returns [Token tk]
@after {
    action.intrinsic_operator();
}
    :    power_op    { retval.tk = $power_op.tk; }
    |    mult_op        { retval.tk = $mult_op.tk; }
    |    add_op        { retval.tk = $add_op.tk; }
    |    concat_op    { retval.tk = $concat_op.tk; }
    |    rel_op        { retval.tk = $rel_op.tk; }
    |    not_op        { retval.tk = $not_op.tk; }
    |    and_op        { retval.tk = $and_op.tk; }
    |    or_op        { retval.tk = $or_op.tk; }
    |    equiv_op    { retval.tk = $equiv_op.tk; }
    ;

// R311
// removed defined_unary_op or defined_binary_op ambiguity with T_DEFINED_OP
defined_operator
    :    T_DEFINED_OP
            { action.defined_operator($T_DEFINED_OP, false); }
    |    extended_intrinsic_op
            { action.defined_operator($extended_intrinsic_op.tk, true); }
    ;

// R312
extended_intrinsic_op returns [Token tk]
@after {
    action.extended_intrinsic_op();
}
    :    intrinsic_operator    { retval.tk = $intrinsic_operator.tk; }
    ;

// R313
// ERR_CHK 313 five characters or less
label returns [Token tk]
    : T_DIGIT_STRING { retval.tk = $T_DIGIT_STRING; action.label($T_DIGIT_STRING); }
    ;

// action.label called here to store label in action class
label_list
@init{ int count=0;}
    :          {action.label_list__begin();}
        lbl=label {count++;}
            ( T_COMMA lbl=label {count++;} )*
              {action.label_list(count);}
    ;

/*
Section 4:
 */

// R401
type_spec
@after {
    action.type_spec();
}
    :    intrinsic_type_spec
    |    derived_type_spec
    ;

// R402
// ERR_CHK 402 scalar_int_expr replaced by expr
type_param_value
    :    expr        { action.type_param_value(true, false, false); }
    |    T_ASTERISK    { action.type_param_value(false, true, false); }
    |    T_COLON     { action.type_param_value(false, false, true); }
    ;

// inlined scalar_int_expr C101 shall be a scalar

// inlined scalar_expr

// R403
// Nonstandard Extension: source BLAS
//    |    T_DOUBLE T_COMPLEX
//    |    T_DOUBLECOMPLEX
intrinsic_type_spec
@init{boolean hasKindSelector = false;}
    :    T_INTEGER (kind_selector {hasKindSelector = true;})?
            {action.intrinsic_type_spec($T_INTEGER, null,
                                        IActionEnums.IntrinsicTypeSpec_INTEGER,
                                        hasKindSelector);}
    |    T_REAL (kind_selector {hasKindSelector = true;})?
            {action.intrinsic_type_spec($T_REAL, null,
                                        IActionEnums.IntrinsicTypeSpec_REAL,
                                        hasKindSelector);}
    |    T_DOUBLE T_PRECISION
            {action.intrinsic_type_spec($T_DOUBLE, $T_PRECISION,
                                        IActionEnums.
                                        IntrinsicTypeSpec_DOUBLEPRECISION,
                                        false);}
    |    T_DOUBLEPRECISION
            {action.intrinsic_type_spec($T_DOUBLEPRECISION, null,
                                        IActionEnums.
                                        IntrinsicTypeSpec_DOUBLEPRECISION,
                                        false);}
    |    T_COMPLEX (kind_selector {hasKindSelector = true;})?
            {action.intrinsic_type_spec($T_COMPLEX, null,
                                        IActionEnums.IntrinsicTypeSpec_COMPLEX,
                                        hasKindSelector);}
    |    T_DOUBLE T_COMPLEX
            {action.intrinsic_type_spec($T_DOUBLE, $T_COMPLEX,
                                        IActionEnums.
                                        IntrinsicTypeSpec_DOUBLECOMPLEX,
                                        false);}
    |    T_DOUBLECOMPLEX
            {action.intrinsic_type_spec($T_DOUBLECOMPLEX, null,
                                        IActionEnums.
                                        IntrinsicTypeSpec_DOUBLECOMPLEX,
                                        false);}
    |    T_CHARACTER (char_selector {hasKindSelector = true;})?
            {action.intrinsic_type_spec($T_CHARACTER, null,
                                        IActionEnums.
                                        IntrinsicTypeSpec_CHARACTER,
                                        hasKindSelector);}
    |    T_LOGICAL (kind_selector {hasKindSelector = true;})?
            {action.intrinsic_type_spec($T_LOGICAL, null,
                                        IActionEnums.IntrinsicTypeSpec_LOGICAL,
                                        hasKindSelector);}
    ;

// R404
// ERR_CHK 404 scalar_int_initialization_expr replaced by expr
// Nonstandard extension: source common practice
//    | T_ASTERISK T_DIGIT_STRING  // e.g., COMPLEX*16
// TODO - check to see if second alternative is where it should go
kind_selector
@init{Token tk1=null; Token tk2=null;}
    : T_LPAREN (T_KIND T_EQUALS {tk1=$T_KIND; tk2=$T_EQUALS;})? expr T_RPAREN
        { action.kind_selector(tk1, tk2, true); }
    | T_ASTERISK T_DIGIT_STRING
        { action.kind_selector($T_ASTERISK, $T_DIGIT_STRING, false); }
    ;

// R405
signed_int_literal_constant
@init{Token sign = null;}
    :    (T_PLUS {sign=$T_PLUS;} | T_MINUS {sign=$T_MINUS;})?
        int_literal_constant
            { action.signed_int_literal_constant(sign); }
    ;

// R406
int_literal_constant
@init{Token kind = null;}
    :    T_DIGIT_STRING (T_UNDERSCORE kind_param {kind = $kind_param.tk;})?
            {action.int_literal_constant($T_DIGIT_STRING, kind);}
    ;

// R407
// T_IDENT inlined for scalar_int_constant_name
kind_param returns [Token tk]
    :    T_DIGIT_STRING
            { retval.tk = $T_DIGIT_STRING; action.kind_param($T_DIGIT_STRING); }
    |    T_IDENT
            { retval.tk = $T_IDENT; action.kind_param($T_IDENT); }
    ;

// R408 signed_digit_string inlined

// R409 digit_string converted to fragment

// R410 sign inlined

// R411
boz_literal_constant
    :    BINARY_CONSTANT { action.boz_literal_constant($BINARY_CONSTANT); }
    |    OCTAL_CONSTANT { action.boz_literal_constant($OCTAL_CONSTANT); }
    |    HEX_CONSTANT { action.boz_literal_constant($HEX_CONSTANT); }
    ;

// R412 binary-constant converted to terminal

// R413 octal_constant converted to terminal

// R414 hex_constant converted to terminal

// R415 hex_digit inlined

// R416
signed_real_literal_constant
@init{Token sign = null;}
    :    (T_PLUS {sign=$T_PLUS;} | T_MINUS {sign=$T_MINUS;})?
        real_literal_constant
            {action.signed_real_literal_constant(sign);}
    ;

// R417 modified to use terminal
// Grammar Modified slightly to prevent problems with input such as:
// if(1.and.1) then ...
real_literal_constant
@init{Token kind = null;}
//        WARNING must parse T_REAL_CONSTANT in action (look for D)
    :   T_REAL_CONSTANT (T_UNDERSCORE kind_param {kind = $kind_param.tk;})?
            { action.real_literal_constant($T_REAL_CONSTANT, kind); }

    ;

// R418 significand converted to fragment

// R419 exponent_letter inlined in new Exponent

// R420 exponent inlined in new Exponent

// R421
complex_literal_constant
@after {
    action.complex_literal_constant();
}
    :    T_LPAREN real_part T_COMMA imag_part T_RPAREN
    ;

// R422
// ERR_CHK 422 named_constant replaced by T_IDENT
real_part
    :    signed_int_literal_constant
            { action.real_part(true, false, null); }
    |    signed_real_literal_constant
            { action.real_part(false, true, null); }
    |    T_IDENT
            { action.real_part(false, false, $T_IDENT); }
    ;

// R423
// ERR_CHK 423 named_constant replaced by T_IDENT
imag_part
    :    signed_int_literal_constant
            { action.imag_part(true, false, null); }
    |    signed_real_literal_constant
            { action.imag_part(false, true, null); }
    |    T_IDENT
            { action.imag_part(false, false, $T_IDENT); }
    ;

// R424
// ERR_CHK 424a scalar_int_initialization_expr replaced by expr
// ERR_CHK 424b T_KIND, if type_param_value, must be a
// scalar_int_initialization_expr
// ERR_CHK 424c T_KIND and T_LEN cannot both be specified
char_selector
@init {
    int kindOrLen1; kindOrLen1 = IActionEnums.KindLenParam_none;
    int kindOrLen2; kindOrLen2 = IActionEnums.KindLenParam_none;
    Token tk1 = null;
    Token tk2 = null;
    boolean hasAsterisk = false;
}
    :    T_ASTERISK char_length (T_COMMA)?
            { hasAsterisk=true;
              action.char_selector(tk1, tk2, kindOrLen1, kindOrLen2,
                                   hasAsterisk); }
    |    T_LPAREN (tmp1=T_KIND { kindOrLen1=IActionEnums.KindLenParam_kind;
                                tk1=tmp1; }
                  | tmp1=T_LEN { kindOrLen1=IActionEnums.KindLenParam_len;
                                 tk1=tmp1; })
          T_EQUALS type_param_value
            { action.char_selector(tk1, tk2, kindOrLen1, kindOrLen2,
                                   hasAsterisk); }
          ( T_COMMA (tmp2=T_KIND { kindOrLen2=IActionEnums.KindLenParam_kind;
                                   tk2=tmp2; }
                     | tmp2=T_LEN { kindOrLen2=IActionEnums.KindLenParam_len;
                                    tk2=tmp2; })
            T_EQUALS type_param_value )?
        T_RPAREN
            { action.char_selector(tk1, tk2, kindOrLen1, kindOrLen2,
                                   hasAsterisk); }
    |    T_LPAREN type_param_value
            (T_COMMA (tmp3=T_KIND T_EQUALS { tk2=tmp3; })? expr
                { kindOrLen2=IActionEnums.KindLenParam_kind;
                action.type_param_value(true, false, false);} )?
        T_RPAREN
            { action.char_selector(tk1, tk2, IActionEnums.KindLenParam_len,
                                   kindOrLen2, hasAsterisk); }
    ;

// R425
length_selector
@init {
    Token len = null;
}
    :    T_LPAREN ( T_LEN { len=$T_LEN; } T_EQUALS )? type_param_value T_RPAREN
            { action.length_selector(len, IActionEnums.KindLenParam_len,
                                     false); }
    |    T_ASTERISK char_length (T_COMMA)?
            { action.length_selector(len, IActionEnums.KindLenParam_none,
                                     true); }
    ;

// R426
char_length
    :    T_LPAREN type_param_value T_RPAREN
            { action.char_length(true); }
    |    scalar_int_literal_constant
            { action.char_length(false); }
    ;

scalar_int_literal_constant
@after {
    action.scalar_int_literal_constant();
}
    :    int_literal_constant
    ;

// R427
// char_literal_constant
// // options {k=2;}
//     :    T_DIGIT_STRING T_UNDERSCORE T_CHAR_CONSTANT
//         // removed the T_UNDERSCORE because underscores are valid characters
//         // for identifiers, which means the lexer would match the T_IDENT and
//         // T_UNDERSCORE as one token (T_IDENT).
//     |    T_IDENT T_CHAR_CONSTANT
//     |    T_CHAR_CONSTANT
//     ;
char_literal_constant
    :    T_DIGIT_STRING T_UNDERSCORE T_CHAR_CONSTANT
            { action.char_literal_constant($T_DIGIT_STRING, null,
                                           $T_CHAR_CONSTANT); }
        // removed the T_UNDERSCORE because underscores are valid characters
        // for identifiers, which means the lexer would match the T_IDENT and
        // T_UNDERSCORE as one token (T_IDENT).
    |    T_IDENT T_CHAR_CONSTANT
            { action.char_literal_constant(null, $T_IDENT, $T_CHAR_CONSTANT); }
    |    T_CHAR_CONSTANT
            { action.char_literal_constant(null, null, $T_CHAR_CONSTANT); }
    ;

// R428
logical_literal_constant
@init{Token kind = null;}
    :    T_TRUE ( T_UNDERSCORE kind_param {kind = $kind_param.tk;})?
            {action.logical_literal_constant($T_TRUE, true, kind);}
    |    T_FALSE ( T_UNDERSCORE kind_param {kind = $kind_param.tk;})?
            {action.logical_literal_constant($T_FALSE, false, kind);}
    ;

// R429
//    ( component_part )? inlined as ( component_def_stmt )*
derived_type_def
@after {
    action.derived_type_def();
}
    :    derived_type_stmt
        // matches T_INTEGER possibilities in component_def_stmt
        type_param_or_comp_def_stmt_list
        ( private_or_sequence )*
      { /* ERR_CHK 429
         * if private_or_sequence present, component_def_stmt in
         * type_param_or_comp_def_stmt_list
         * is an error
         */
      }
        ( component_def_stmt )*
        ( type_bound_procedure_part )?
        end_type_stmt
    ;

// Includes:
//    ( type_param_def_stmt)*
//    ( component_def_stmt )* if starts with T_INTEGER (could be a parse error)
// REMOVED T_INTEGER junk (see statement above) with k=1
// TODO this must be tested can we get rid of this????
type_param_or_comp_def_stmt_list
@after {
    action.type_param_or_comp_def_stmt_list();
}
///options {k=1;}
//    :    (T_INTEGER) => (kind_selector)? T_COMMA type_param_or_comp_def_stmt
//            type_param_or_comp_def_stmt_list
    :    (kind_selector)? T_COMMA type_param_or_comp_def_stmt
            type_param_or_comp_def_stmt_list
    |
        { /* ERR_CHK R435
           * type_param_def_stmt(s) must precede component_def_stmt(s)
           */
        }
    ;

type_param_or_comp_def_stmt
    :    type_param_attr_spec T_COLON_COLON type_param_decl_list end_of_stmt
            // TODO: See if this is reachable now that type_param_attr_spec is
            // tokenized T_KIND or T_LEN. See R435
            {action.type_param_or_comp_def_stmt($end_of_stmt.tk,
                IActionEnums.TypeParamOrCompDef_typeParam);}
    |    component_attr_spec_list T_COLON_COLON component_decl_list end_of_stmt
            // See R440
            {action.type_param_or_comp_def_stmt($end_of_stmt.tk,
                IActionEnums.TypeParamOrCompDef_compDef);}
    ;

// R430
// generic_name_list substituted for type_param_name_list
derived_type_stmt
@init {
    Token lbl=null;
    boolean hasTypeAttrSpecList=false;
    boolean hasGenericNameList=false;
}
    :    (label {lbl=$label.tk;})? T_TYPE
        ( ( T_COMMA type_attr_spec_list {hasTypeAttrSpecList=true;} )?
            T_COLON_COLON )? T_IDENT
            ( T_LPAREN generic_name_list T_RPAREN {hasGenericNameList=true;} )?
            end_of_stmt
            {action.derived_type_stmt(lbl, $T_TYPE, $T_IDENT, $end_of_stmt.tk,
                hasTypeAttrSpecList, hasGenericNameList);}
    ;

type_attr_spec_list
@init{int count = 0;}
    :        {action.type_attr_spec_list__begin();}
        type_attr_spec {count++;} ( T_COMMA type_attr_spec {count++;} )*
            {action.type_attr_spec_list(count);}
    ;

generic_name_list
@init{int count = 0;}
    :        {action.generic_name_list__begin();}
        ident=T_IDENT
            {
                count++;
                action.generic_name_list_part(ident);
            } ( T_COMMA ident=T_IDENT
            {
                count++;
                action.generic_name_list_part(ident);
            } )*
            {action.generic_name_list(count);}
    ;

// R431
// T_IDENT inlined for parent_type_name
type_attr_spec
    :    access_spec
            {action.type_attr_spec(null, null,
                                   IActionEnums.TypeAttrSpec_access_spec);}
    |    T_EXTENDS T_LPAREN T_IDENT T_RPAREN
            {action.type_attr_spec($T_EXTENDS, $T_IDENT,
                                   IActionEnums.TypeAttrSpec_extends);}
    |    T_ABSTRACT
            {action.type_attr_spec($T_ABSTRACT, null,
                                   IActionEnums.TypeAttrSpec_abstract);}
    |    T_BIND T_LPAREN T_IDENT /* 'C' */ T_RPAREN
            {action.type_attr_spec($T_BIND, $T_IDENT,
                                   IActionEnums.TypeAttrSpec_bind);}
    ;

// R432
private_or_sequence
@after {
    action.private_or_sequence();
}
    :   private_components_stmt
    |   sequence_stmt
    ;

// R433
end_type_stmt
@init{Token lbl = null;Token id=null;}
    : (label {lbl=$label.tk;})? T_END T_TYPE ( T_IDENT {id=$T_IDENT;})?
        end_of_stmt
        {action.end_type_stmt(lbl, $T_END, $T_TYPE, id, $end_of_stmt.tk);}
    | (label {lbl=$label.tk;})? T_ENDTYPE ( T_IDENT {id=$T_IDENT;})?
        end_of_stmt
        {action.end_type_stmt(lbl, $T_ENDTYPE, null, id, $end_of_stmt.tk);}
    ;

// R434
sequence_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_SEQUENCE end_of_stmt
            {action.sequence_stmt(lbl, $T_SEQUENCE, $end_of_stmt.tk);}
    ;

// R435 type_param_def_stmt inlined in type_param_or_comp_def_stmt_list

// R436
// ERR_CHK 436 scalar_int_initialization_expr replaced by expr
// T_IDENT inlined for type_param_name
type_param_decl
@init{ boolean hasInit=false; }
    :    T_IDENT ( T_EQUALS expr {hasInit=true;})?
            {action.type_param_decl($T_IDENT, hasInit);}
    ;

type_param_decl_list
@init{int count=0;}
    :        {action.type_param_decl_list__begin();}
        type_param_decl {count++;} ( T_COMMA type_param_decl {count++;} )*
            {action.type_param_decl_list(count);}
    ;

// R437
// ADD isKind boolean.
type_param_attr_spec
    :     T_IDENT /* { KIND | LEN } */
            { action.type_param_attr_spec($T_IDENT); }
    ;

// R438 component_part inlined as ( component_def_stmt )* in R429

// R439
component_def_stmt
    :    data_component_def_stmt
            {action.component_def_stmt(IActionEnums.ComponentDefType_data);}
    |    proc_component_def_stmt
            {action.component_def_stmt(IActionEnums.
                                       ComponentDefType_procedure);}
    ;


// R440
data_component_def_stmt
@init{Token lbl = null; boolean hasSpec=false; }
    :    (label {lbl=$label.tk;})? declaration_type_spec
            ( ( T_COMMA component_attr_spec_list {hasSpec=true;})?
            T_COLON_COLON )? component_decl_list end_of_stmt
            {action.data_component_def_stmt(lbl, $end_of_stmt.tk, hasSpec);}
    ;

// R441, R442-F2008
// TODO putback F2008
// TODO it appears there is a bug in the standard for a parameterized type,
//      it needs to accept KIND, LEN keywords, see NOTE 4.24 and 4.25
component_attr_spec
    :    T_POINTER
            {action.component_attr_spec($T_POINTER,
                IActionEnums.ComponentAttrSpec_pointer);}
    |    T_DIMENSION T_LPAREN component_array_spec T_RPAREN
            {action.component_attr_spec($T_DIMENSION,
                IActionEnums.ComponentAttrSpec_dimension_paren);}
    |    T_DIMENSION /* (T_LPAREN component_array_spec T_RPAREN)? */
            T_LBRACKET co_array_spec T_RBRACKET
            {action.component_attr_spec($T_DIMENSION,
                IActionEnums.ComponentAttrSpec_dimension_bracket);}
    |    T_ALLOCATABLE
            {action.component_attr_spec($T_ALLOCATABLE,
                IActionEnums.ComponentAttrSpec_allocatable);}
    |    access_spec
            {action.component_attr_spec(null,
                IActionEnums.ComponentAttrSpec_access_spec);}
        // are T_KIND and T_LEN correct?
    |   T_KIND
            {action.component_attr_spec($T_KIND,
                IActionEnums.ComponentAttrSpec_kind);}
    |   T_LEN
            {action.component_attr_spec($T_LEN,
                IActionEnums.ComponentAttrSpec_len);}
    ;

component_attr_spec_list
@init{int count=1;}
    :        {action.component_attr_spec_list__begin();}
        component_attr_spec ( T_COMMA component_attr_spec {count++;} )*
            {action.component_attr_spec_list(count);}
    ;

// R442, R443-F2008
// T_IDENT inlined as component_name
component_decl
@init {
    boolean hasComponentArraySpec = false;
    boolean hasCoArraySpec = false;
    boolean hasCharLength = false;
    boolean hasComponentInitialization = false;
}
    :   T_IDENT ( T_LPAREN component_array_spec T_RPAREN
            {hasComponentArraySpec=true;})?
            ( T_LBRACKET co_array_spec T_RBRACKET {hasCoArraySpec=true;})?
            ( T_ASTERISK char_length {hasCharLength=true;})?
            ( component_initialization {hasComponentInitialization =true;})?
            { action.component_decl($T_IDENT, hasComponentArraySpec,
                                    hasCoArraySpec, hasCharLength,
                                    hasComponentInitialization);}
    ;

component_decl_list
@init{int count=0;}
    :        {action.component_decl_list__begin();}
       component_decl {count++;} ( T_COMMA component_decl {count++;} )*
            {action.component_decl_list(count);}
    ;

// R443
component_array_spec
    :    explicit_shape_spec_list
            {action.component_array_spec(true);}
    |    deferred_shape_spec_list
            {action.component_array_spec(false);}
    ;

// deferred_shape_spec replaced by T_COLON
deferred_shape_spec_list
@init{int count=0;}
    :        {action.deferred_shape_spec_list__begin();}
        T_COLON {count++;} ( T_COMMA T_COLON {count++;} )*
            {action.deferred_shape_spec_list(count);}
    ;

// R444
// R447-F2008 can also be => initial_data_target, see NOTE 4.40 in J3/07-007
// ERR_CHK 444 initialization_expr replaced by expr
component_initialization
@after {
    action.component_initialization();
}
    :    T_EQUALS expr
    |    T_EQ_GT null_init
    ;

// R445
proc_component_def_stmt
@init{Token lbl = null; boolean hasInterface=false;}
    :    (label {lbl=$label.tk;})? T_PROCEDURE T_LPAREN
            ( proc_interface {hasInterface=true;})? T_RPAREN T_COMMA
            proc_component_attr_spec_list T_COLON_COLON proc_decl_list
            end_of_stmt
                {action.proc_component_def_stmt(lbl, $T_PROCEDURE,
                    $end_of_stmt.tk, hasInterface);}
    ;

// R446
// T_IDENT inlined for arg_name
proc_component_attr_spec
@init{ Token id=null; }
    :    T_POINTER
            {action.proc_component_attr_spec($T_POINTER, id,
                                             IActionEnums.
                                             ProcComponentAttrSpec_pointer);}
    |    T_PASS ( T_LPAREN T_IDENT T_RPAREN {id=$T_IDENT;} )?
            {action.proc_component_attr_spec($T_PASS, id,
                                             IActionEnums.
                                             ProcComponentAttrSpec_pass);}
    |    T_NOPASS
            {action.proc_component_attr_spec($T_NOPASS, id,
                                             IActionEnums.
                                             ProcComponentAttrSpec_nopass);}
    |    access_spec
            {action.
                proc_component_attr_spec(null, id,
                                         IActionEnums.
                                         ProcComponentAttrSpec_access_spec);}
    ;

proc_component_attr_spec_list
@init{int count=0;}
    :        {action.proc_component_attr_spec_list__begin();}
        proc_component_attr_spec {count++;}
            ( T_COMMA proc_component_attr_spec {count++;})*
            {action.proc_component_attr_spec_list(count);}
    ;

// R447
private_components_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_PRIVATE end_of_stmt
            {action.private_components_stmt(lbl, $T_PRIVATE, $end_of_stmt.tk);}
    ;

// R448
type_bound_procedure_part
@init{int count=0; boolean hasBindingPrivateStmt = false;}
    :    contains_stmt
        ( binding_private_stmt {hasBindingPrivateStmt=true;})?
        proc_binding_stmt ( proc_binding_stmt {count++;})*
            {action.type_bound_procedure_part(count,
                                              hasBindingPrivateStmt);}
    ;

// R449
binding_private_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_PRIVATE end_of_stmt
            {action.binding_private_stmt(lbl, $T_PRIVATE, $end_of_stmt.tk);}
    ;

// R450
proc_binding_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? specific_binding end_of_stmt
            {action.proc_binding_stmt(lbl,
                IActionEnums.BindingStatementType_specific, $end_of_stmt.tk);}
    |    (label {lbl=$label.tk;})? generic_binding end_of_stmt
            {action.proc_binding_stmt(lbl,
                IActionEnums.BindingStatementType_generic, $end_of_stmt.tk);}
    |    (label {lbl=$label.tk;})? final_binding end_of_stmt
            {action.proc_binding_stmt(lbl,
                IActionEnums.BindingStatementType_final, $end_of_stmt.tk);}
    ;

// R451
// T_IDENT inlined for interface_name, binding_name and procedure_name
specific_binding
@init {
    Token interfaceName=null;
    Token bindingName=null;
    Token procedureName=null;
    boolean hasBindingAttrList=false;
}
    :   T_PROCEDURE (T_LPAREN tmpId1=T_IDENT T_RPAREN {interfaceName=tmpId1;})?
            ( ( T_COMMA binding_attr_list {hasBindingAttrList=true;})?
                T_COLON_COLON )?
            tmpId2=T_IDENT {bindingName=tmpId2;}
            ( T_EQ_GT tmpId3=T_IDENT {procedureName=tmpId3;})?
            { action.specific_binding($T_PROCEDURE, interfaceName, bindingName,
                                      procedureName, hasBindingAttrList);}
    ;

// R452
// generic_name_list substituted for binding_name_list
generic_binding
@init{boolean hasAccessSpec=false;}
    :    T_GENERIC ( T_COMMA access_spec {hasAccessSpec=true;})? T_COLON_COLON
            generic_spec T_EQ_GT generic_name_list
            {action.generic_binding($T_GENERIC, hasAccessSpec);}
    ;

// R453
// T_IDENT inlined for arg_name
binding_attr
@init{Token id = null;}
    : T_PASS ( T_LPAREN T_IDENT T_RPAREN {id=$T_IDENT;})?
        { action.binding_attr($T_PASS, IActionEnums.AttrSpec_PASS, id); }
    | T_NOPASS
        { action.binding_attr($T_NOPASS, IActionEnums.AttrSpec_NOPASS, id); }
    | T_NON_OVERRIDABLE
        { action.binding_attr($T_NON_OVERRIDABLE,
                              IActionEnums.AttrSpec_NON_OVERRIDABLE, id); }
    | T_DEFERRED
        { action.binding_attr($T_DEFERRED, IActionEnums.AttrSpec_DEFERRED,
                              id); }
    | access_spec
        { action.binding_attr(null, IActionEnums.AttrSpec_none, id); }
    ;

binding_attr_list
@init{int count=0;}
    :        {action.binding_attr_list__begin();}
        binding_attr {count++;} ( T_COMMA binding_attr {count++;} )*
            {action.binding_attr_list(count);}
    ;

// R454
// generic_name_list substituted for final_subroutine_name_list
final_binding
    :    T_FINAL ( T_COLON_COLON )? generic_name_list
            { action.final_binding($T_FINAL); }
    ;

// R455
derived_type_spec
@init{boolean hasList = false;}
    : T_IDENT ( T_LPAREN type_param_spec_list {hasList=true;} T_RPAREN )?
        { action.derived_type_spec($T_IDENT, hasList); }
    ;

// R456
type_param_spec
@init{ Token keyWord=null; }
    : ( keyword T_EQUALS {keyWord=$keyword.tk;})? type_param_value
            {action.type_param_spec(keyWord);}
    ;

type_param_spec_list
@init{int count=0;}
    :        {action.type_param_spec_list__begin();}
        type_param_spec {count++;}( T_COMMA type_param_spec {count++;})*
            {action.type_param_spec_list(count);}
    ;

// R457
// inlined derived_type_spec (R662) to remove ambiguity using backtracking
// ERR_CHK R457
// If any of the type-param-specs in the list are an '*' or ':', the
// component-spec-list is required.
// the second alternative to the original rule for structure_constructor is
// a subset of the first alternative because component_spec_list is a
// subset of type_param_spec_list.  by combining these two alternatives we can
// remove the backtracking on this rule.
structure_constructor
// options {backtrack=true;}
//     : T_IDENT T_LPAREN type_param_spec_list T_RPAREN
//         T_LPAREN
//         ( component_spec_list )?
//         T_RPAREN
//     | T_IDENT
//         T_LPAREN
//         ( component_spec_list )?
//         T_RPAREN
    : T_IDENT T_LPAREN type_param_spec_list T_RPAREN
        (T_LPAREN
        ( component_spec_list )?
        T_RPAREN)?
        { action.structure_constructor($T_IDENT); }
    ;

// R458
component_spec
@init { Token keyWord = null; }
    :   ( keyword T_EQUALS { keyWord=$keyword.tk; })? component_data_source
            { action.component_spec(keyWord); }
    ;

component_spec_list
@init{int count=0;}
    :        {action.component_spec_list__begin();}
        component_spec {count++;}( T_COMMA component_spec {count++;})*
            {action.component_spec_list(count);}
    ;

// R459
// is (expr | data-target | proc-target)
// data_target isa expr so data_target deleted
// proc_target isa expr so proc_target deleted
component_data_source
    :    expr
            { action.component_data_source(); }
    ;

// R460
enum_def
@init{ int numEls=1; }
    :    enum_def_stmt
        enumerator_def_stmt
        ( enumerator_def_stmt {numEls++;})*
        end_enum_stmt
            {action.enum_def(numEls);}
    ;

// R461
enum_def_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_ENUM T_COMMA T_BIND T_LPAREN
            T_IDENT /* 'C' */ T_RPAREN end_of_stmt
            {action.enum_def_stmt(lbl, $T_ENUM, $T_BIND, $T_IDENT,
                $end_of_stmt.tk);}
    ;

// R462
enumerator_def_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_ENUMERATOR ( T_COLON_COLON )?
            enumerator_list end_of_stmt
            {action.enumerator_def_stmt(lbl, $T_ENUMERATOR, $end_of_stmt.tk);}
    ;

// R463
// ERR_CHK 463 scalar_int_initialization_expr replaced by expr
// ERR_CHK 463 named_constant replaced by T_IDENT
enumerator
@init{boolean hasExpr = false;}
    :   T_IDENT ( T_EQUALS expr { hasExpr = true; })?
            { action.enumerator($T_IDENT, hasExpr); }
    ;

enumerator_list
@init{int count=0;}
    :        {action.enumerator_list__begin();}
        enumerator {count++;}( T_COMMA enumerator {count++;})*
            {action.enumerator_list(count);}
    ;

// R464
end_enum_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_END T_ENUM end_of_stmt
            { action.end_enum_stmt(lbl, $T_END, $T_ENUM, $end_of_stmt.tk); }
    |    (label {lbl=$label.tk;})? T_ENDENUM end_of_stmt
            { action.end_enum_stmt(lbl, $T_ENDENUM, null, $end_of_stmt.tk); }
    ;

// R465
array_constructor
    :    T_LPAREN T_SLASH ac_spec T_SLASH T_RPAREN
            { action.array_constructor(); }
    |    T_LBRACKET ac_spec T_RBRACKET
            { action.array_constructor(); }
    ;

// R466
// refactored to remove optional from lhs
ac_spec
options {backtrack=true;}
@after {
    action.ac_spec();
}
    : type_spec T_COLON_COLON (ac_value_list)?
    | ac_value_list
    ;

// R467 left_square_bracket inlined as T_LBRACKET

// R468 right_square_bracket inlined as T_RBRACKET

// R469
ac_value
options {backtrack=true;}
@after {
    action.ac_value();
}
    :    expr
    |    ac_implied_do
    ;

ac_value_list
@init{int count=0;}
    :        {action.ac_value_list__begin();}
        ac_value {count++;}( T_COMMA ac_value {count++;})*
            {action.ac_value_list(count);}
    ;

// R470
ac_implied_do
    :    T_LPAREN ac_value_list T_COMMA ac_implied_do_control T_RPAREN
            {action.ac_implied_do();}
    ;

// R471
// ERR_CHK 471a scalar_int_expr replaced by expr
// ERR_CHK 471b ac_do_variable replaced by scalar_int_variable replaced
// by variable replaced by T_IDENT
ac_implied_do_control
@init{boolean hasStride=false;}
    :    T_IDENT T_EQUALS expr T_COMMA expr ( T_COMMA expr {hasStride=true;})?
            {action.ac_implied_do_control(hasStride);}
    ;

// R472 inlined ac_do_variable as scalar_int_variable (and finally T_IDENT)
// in R471
// C493 (R472) ac-do-variable shall be a named variable
scalar_int_variable
    :   variable
            { action.scalar_int_variable(); }
    ;


/*
Section 5:
 */

// R501
type_declaration_stmt
@init{Token lbl = null; int numAttrSpecs = 0;}
    :    (label {lbl=$label.tk;})? declaration_type_spec
        ( (T_COMMA attr_spec {numAttrSpecs += 1;})* T_COLON_COLON )?
        entity_decl_list end_of_stmt
            { action.type_declaration_stmt(lbl, numAttrSpecs,
                    $end_of_stmt.tk); }
    ;

// R502
declaration_type_spec
    :    intrinsic_type_spec
            { action.declaration_type_spec(null,
                IActionEnums.DeclarationTypeSpec_INTRINSIC); }
    |    T_TYPE T_LPAREN    derived_type_spec T_RPAREN
            { action.declaration_type_spec($T_TYPE,
                IActionEnums.DeclarationTypeSpec_TYPE); }
    |    T_CLASS    T_LPAREN derived_type_spec T_RPAREN
            { action.declaration_type_spec($T_CLASS,
                IActionEnums.DeclarationTypeSpec_CLASS); }
    |    T_CLASS T_LPAREN T_ASTERISK T_RPAREN
            { action.declaration_type_spec($T_CLASS,
                IActionEnums.DeclarationTypeSpec_unlimited); }
    ;

// R503
attr_spec
    :    access_spec        { action.attr_spec(null,
                IActionEnums.AttrSpec_access); }
    |    T_ALLOCATABLE    { action.attr_spec($T_ALLOCATABLE,
                IActionEnums.AttrSpec_ALLOCATABLE); }
    |    T_ASYNCHRONOUS    { action.attr_spec($T_ASYNCHRONOUS,
                IActionEnums.AttrSpec_ASYNCHRONOUS); }
    |    T_DIMENSION T_LPAREN array_spec T_RPAREN
                        { action.attr_spec($T_DIMENSION,
                IActionEnums.AttrSpec_DIMENSION ); }
    |    T_EXTERNAL        { action.attr_spec($T_EXTERNAL,
                IActionEnums.AttrSpec_EXTERNAL); }
    |    T_INTENT T_LPAREN intent_spec T_RPAREN
                        { action.attr_spec($T_INTENT,
                IActionEnums.AttrSpec_INTENT); }
    |    T_INTRINSIC        { action.attr_spec($T_INTRINSIC,
                IActionEnums.AttrSpec_INTRINSIC); }
    |    language_binding_spec
                        { action.attr_spec(null,
                IActionEnums.AttrSpec_language_binding); }
    |    T_OPTIONAL        { action.attr_spec($T_OPTIONAL,
                IActionEnums.AttrSpec_OPTIONAL); }
    |    T_PARAMETER        { action.attr_spec($T_PARAMETER,
                IActionEnums.AttrSpec_PARAMETER); }
    |    T_POINTER        { action.attr_spec($T_POINTER,
                IActionEnums.AttrSpec_POINTER); }
    |    T_PROTECTED        { action.attr_spec($T_PROTECTED,
                IActionEnums.AttrSpec_PROTECTED); }
    |    T_SAVE            { action.attr_spec($T_SAVE,
                IActionEnums.AttrSpec_SAVE); }
    |    T_TARGET        { action.attr_spec($T_TARGET,
                IActionEnums.AttrSpec_TARGET); }
    |    T_VALUE            { action.attr_spec($T_VALUE,
                IActionEnums.AttrSpec_VALUE); }
    |    T_VOLATILE        { action.attr_spec($T_VOLATILE,
                IActionEnums.AttrSpec_VOLATILE); }
// TODO are T_KIND and T_LEN correct?
    |   T_KIND
            { action.attr_spec($T_KIND, IActionEnums.AttrSpec_KIND); }
    |   T_LEN
            { action.attr_spec($T_LEN, IActionEnums.AttrSpec_LEN); }
    ;


// R504, R503-F2008
// T_IDENT inlined for object_name and function_name
// T_IDENT ( T_ASTERISK char_length )? (second alt) subsumed in first alt
// TODO Pass more info to action....
entity_decl
    : T_IDENT ( T_LPAREN array_spec T_RPAREN )?
              ( T_LBRACKET co_array_spec T_RBRACKET )?
              ( T_ASTERISK char_length )? ( initialization )?
        {action.entity_decl($T_IDENT);}
    ;

entity_decl_list
@init{int count = 0;}
    :        {action.entity_decl_list__begin();}
        entity_decl {count += 1;} ( T_COMMA entity_decl {count += 1;} )*
            {action.entity_decl_list(count);}
    ;

// R505 object_name inlined as T_IDENT

// R506
// ERR_CHK 506 initialization_expr replaced by expr
initialization
    :    T_EQUALS expr        { action.initialization(true, false); }
    |    T_EQ_GT null_init    { action.initialization(false, true); }
    ;

// R507
// C506 The function-reference shall be a reference to the NULL intrinsic
// function with no arguments.
null_init
    :    T_IDENT /* 'NULL' */ T_LPAREN T_RPAREN
            { action.null_init($T_IDENT); }
    ;

// R508
access_spec
    :    T_PUBLIC
            {action.access_spec($T_PUBLIC,  IActionEnums.AttrSpec_PUBLIC);}
    |    T_PRIVATE
            {action.access_spec($T_PRIVATE, IActionEnums.AttrSpec_PRIVATE);}
    ;

// R509
// ERR_CHK 509 scalar_char_initialization_expr replaced by expr
language_binding_spec
@init{boolean hasName = false;}
    :    T_BIND T_LPAREN T_IDENT /* 'C' */
            (T_COMMA name T_EQUALS expr {hasName=true;})? T_RPAREN
            { action.language_binding_spec($T_BIND, $T_IDENT, hasName); }
    ;

// R510
array_spec
@init{int count=0;}
    :    array_spec_element {count++;}
        (T_COMMA array_spec_element {count++;})*
            {action.array_spec(count);}
    ;

// Array specifications can consist of these beasts. Note that we can't
// mix/match arbitrarily, so we have to check validity in actions.
// Types:     0 expr (e.g. 3 or m+1)
//             1 expr: (e.g. 3:)
//             2 expr:expr (e.g. 3:5 or 7:(m+1))
//             3 expr:* (e.g. 3:* end of assumed size)
//             4 *  (end of assumed size)
//             5 :     (could be part of assumed or deferred shape)
array_spec_element
@init{int type=IActionEnums.ArraySpecElement_expr;}
    :   expr ( T_COLON {type=IActionEnums.ArraySpecElement_expr_colon;}
            (  expr {type=IActionEnums.ArraySpecElement_expr_colon_expr;}
             | T_ASTERISK
                {type=IActionEnums.ArraySpecElement_expr_colon_asterisk;} )?
          )?
            { action.array_spec_element(type); }
    |   T_ASTERISK
            { action.array_spec_element(IActionEnums.
                ArraySpecElement_asterisk); }
    |    T_COLON
            { action.array_spec_element(IActionEnums.ArraySpecElement_colon); }
    ;

// R511
// refactored to remove conditional from lhs and inlined lower_bound and
// upper_bound
explicit_shape_spec
@init{boolean hasUpperBound=false;}
    :     expr (T_COLON expr {hasUpperBound=true;})?
            {action.explicit_shape_spec(hasUpperBound);}
    ;

explicit_shape_spec_list
@init{ int count=0;}
    :        {action.explicit_shape_spec_list__begin();}
         explicit_shape_spec {count++;}
            ( T_COMMA explicit_shape_spec {count++;})*
            {action.explicit_shape_spec_list(count);}
    ;

/*
 * F2008 co-array grammar addition
 */

// R511-F2008
co_array_spec
@after {
    action.co_array_spec();
}
    :    deferred_co_shape_spec_list
    |    explicit_co_shape_spec
    ;

// R519-F2008
deferred_co_shape_spec
    :    T_COLON
            { action.deferred_co_shape_spec(); }
    ;

deferred_co_shape_spec_list
@init{int count=0;}
    :        {action.deferred_co_shape_spec_list__begin();}
        T_COLON {count++;}( T_COMMA T_COLON {count++;})?
            {action.deferred_co_shape_spec_list(count);}
    ;

// R520-F2008
// TODO putback F2008
// TODO add T_ASTERISK token to action?
explicit_co_shape_spec
@after {
    action.explicit_co_shape_spec();
}
    :    T_XYZ expr explicit_co_shape_spec_suffix
    |    T_ASTERISK
    ;

// TODO add more info to action
explicit_co_shape_spec_suffix
@after {
    action.explicit_co_shape_spec_suffix();
}
    :    T_COLON T_ASTERISK
    |    T_COMMA explicit_co_shape_spec
    |    T_COLON expr explicit_co_shape_spec
    ;

// R512 lower_bound was specification_expr inlined as expr

// R513 upper_bound was specification_expr inlined as expr

// R514 assumed_shape_spec was ( lower_bound )? T_COLON not used in R510
// array_spec

// R515 deferred_shape_spec inlined as T_COLON in deferred_shape_spec_list

// R516 assumed_size_spec absorbed into array_spec.

// R517
intent_spec
    :    T_IN        { action.intent_spec($T_IN, null,
                IActionEnums.IntentSpec_IN); }
    |    T_OUT        { action.intent_spec($T_OUT, null,
                IActionEnums.IntentSpec_OUT); }
    |    T_IN T_OUT    { action.intent_spec($T_IN, $T_OUT,
                IActionEnums.IntentSpec_INOUT); }
    |    T_INOUT        { action.intent_spec($T_INOUT, null,
                IActionEnums.IntentSpec_INOUT); }
    ;

// R518
access_stmt
@init{Token lbl = null;boolean hasList=false;}
    :    (label {lbl=$label.tk;})? access_spec ( ( T_COLON_COLON )?
            access_id_list {hasList=true;})? end_of_stmt
            { action.access_stmt(lbl,$end_of_stmt.tk,hasList); }
    ;

// R519
// T_IDENT inlined for use_name
// generic_spec can be T_IDENT so T_IDENT deleted
// TODO - can this only be T_IDENTS?  generic_spec is more than that..
access_id
    :    generic_spec
            { action.access_id(); }
    ;

access_id_list
@init{ int count=0;}
    :          {action.access_id_list__begin();}
        access_id {count++;} ( T_COMMA access_id {count++;} )*
              {action.access_id_list(count);}
    ;

// R520, R526-F2008
// T_IDENT inlined for object_name
allocatable_stmt
@init{Token lbl = null; int count=1;}
    : (label {lbl=$label.tk;})? T_ALLOCATABLE ( T_COLON_COLON )?
            allocatable_decl ( T_COMMA allocatable_decl {count++;})*
                end_of_stmt
            { action.allocatable_stmt(lbl, $T_ALLOCATABLE, $end_of_stmt.tk,
                    count); }
    ;

// R527-F2008
// T_IDENT inlined for object_name
allocatable_decl
@init{boolean hasArraySpec=false; boolean hasCoArraySpec=false;}
    : T_IDENT ( T_LPAREN array_spec T_RPAREN {hasArraySpec=true;} )?
              ( T_LBRACKET co_array_spec T_RBRACKET {hasCoArraySpec=true;} )?
        {action.allocatable_decl($T_IDENT, hasArraySpec, hasCoArraySpec);}
    ;

// R521
// generic_name_list substituted for object_name_list
asynchronous_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_ASYNCHRONOUS ( T_COLON_COLON )?
        generic_name_list end_of_stmt
            {action.asynchronous_stmt(lbl,$T_ASYNCHRONOUS,$end_of_stmt.tk);}
    ;

// R522
bind_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? language_binding_spec
        ( T_COLON_COLON )? bind_entity_list end_of_stmt
            { action.bind_stmt(lbl, $end_of_stmt.tk); }
    ;

// R523
// T_IDENT inlined for entity_name and common_block_name
bind_entity
    :    T_IDENT
        { action.bind_entity($T_IDENT, false); }// isCommonBlockName=false
    |    T_SLASH T_IDENT T_SLASH
        { action.bind_entity($T_IDENT, true); }// isCommonBlockname=true
    ;

bind_entity_list
@init{ int count=0;}
    :          {action.bind_entity_list__begin();}
        bind_entity {count++;} ( T_COMMA bind_entity {count++;} )*
              {action.bind_entity_list(count);}
    ;

// R524
data_stmt
@init{Token lbl = null; int count=1;}
    :    (label {lbl=$label.tk;})? T_DATA data_stmt_set ( ( T_COMMA )?
            data_stmt_set {count++;})* end_of_stmt
            { action.data_stmt(lbl, $T_DATA, $end_of_stmt.tk, count); }
    ;

// R525
data_stmt_set
    :    data_stmt_object_list
        T_SLASH
        data_stmt_value_list
        T_SLASH
            { action.data_stmt_set(); }
    ;

// R526
data_stmt_object
@after {
    action.data_stmt_object();
}
    :    variable
    |    data_implied_do
    ;

data_stmt_object_list
@init{ int count=0;}
    :          {action.data_stmt_object_list__begin();}
        data_stmt_object {count++;} ( T_COMMA data_stmt_object {count++;} )*
              {action.data_stmt_object_list(count);}
    ;


// R527
// ERR_CHK 527 scalar_int_expr replaced by expr
// data_i_do_variable replaced by T_IDENT
data_implied_do
@init {
    boolean hasThirdExpr = false;
}
    : T_LPAREN data_i_do_object_list T_COMMA T_IDENT T_EQUALS
        expr T_COMMA expr ( T_COMMA expr { hasThirdExpr = true; })? T_RPAREN
        { action.data_implied_do($T_IDENT, hasThirdExpr); }
    ;

// R528
// data_ref inlined for scalar_structure_component and array_element
data_i_do_object
@after {
    action.data_i_do_object();
}
    :    data_ref
    |    data_implied_do
    ;

data_i_do_object_list
@init{ int count=0;}
    :          {action.data_i_do_object_list__begin();}
        data_i_do_object {count++;} ( T_COMMA data_i_do_object {count++;} )*
              {action.data_i_do_object_list(count);}
    ;

// R529 data_i_do_variable was scalar_int_variable inlined as T_IDENT
// C556 (R529) The data-i-do-variable shall be a named variable.

// R530
// ERR_CHK R530 designator is scalar-constant or integer constant when
// followed by '*'
// data_stmt_repeat inlined from R531
// structure_constructure covers null_init if 'NULL()' so null_init deleted
// TODO - check for other cases of signed_real_literal_constant and
// real_literal_constant problems
data_stmt_value
options {backtrack=true; k=3;}
@init{Token ast = null;}
@after {
    action.data_stmt_value(ast);
}
    :    designator (T_ASTERISK data_stmt_constant {ast=$T_ASTERISK;})?
    |    int_literal_constant (T_ASTERISK data_stmt_constant {ast=$T_ASTERISK;})?
    |   signed_real_literal_constant
    |    signed_int_literal_constant
    |    complex_literal_constant
    |    logical_literal_constant
    |    char_literal_constant
    |    boz_literal_constant
    |    structure_constructor // is null_init if 'NULL()'
    |   hollerith_constant // extension??
    ;

data_stmt_value_list
@init{ int count=0;}
    :          {action.data_stmt_value_list__begin();}
        data_stmt_value {count++;} ( T_COMMA data_stmt_value {count++;} )*
              {action.data_stmt_value_list(count);}
    ;

// R531 data_stmt_repeat inlined as (int_literal_constant | designator) in R530
// ERRCHK 531 int_constant shall be a scalar_int_constant
// scalar_int_constant replaced by int_constant replaced by
// int_literal_constant as T_IDENT covered by designator
// scalar_int_constant_subobject replaced by designator

scalar_int_constant
    :   int_constant
            { action.scalar_int_constant(); }
    ;

hollerith_constant
//     :   T_DIGIT_STRING T_IDENT
    :   T_HOLLERITH
            { action.hollerith_constant($T_HOLLERITH); }
    ;

// R532
// scalar_constant_subobject replaced by designator
// scalar_constant replaced by literal_constant as designator can be T_IDENT
// then literal_constant inlined (except for signed portion)
// structure_constructure covers null_init if 'NULL()' so null_init deleted
// The lookahead in the alternative for signed_real_literal_constant is
// necessary because ANTLR won't look far enough ahead by itself and when it
// sees a T_DIGIT_STRING, it tries the signed_int_literal_constant.  this isn't
// correct since the new version of the real_literal_constants can start with
// a T_DIGIT_STRING.
data_stmt_constant
options {backtrack=true; k=3;}
@after {
    action.data_stmt_constant();
}
    :    designator
    |    signed_int_literal_constant
    |   signed_real_literal_constant
    |    complex_literal_constant
    |    logical_literal_constant
    |    char_literal_constant
    |    boz_literal_constant
    |    structure_constructor // is null_init if 'NULL()'
    ;

// R533 int_constant_subobject was constant_subobject inlined as designator
// in R531

// R534 constant_subobject inlined as designator in R533
// C566 (R534) constant-subobject shall be a subobject of a constant.

// R535, R543-F2008
// array_name replaced by T_IDENT
dimension_stmt
@init{Token lbl=null; int count=1;}
    :    (label {lbl=$label.tk;})? T_DIMENSION ( T_COLON_COLON )?
        dimension_decl ( T_COMMA dimension_decl {count++;})* end_of_stmt
            { action.dimension_stmt(lbl, $T_DIMENSION, $end_of_stmt.tk,
                count); }
    ;

// R544-F2008
// ERR_CHK 509-F2008 at least one of the array specs must exist
dimension_decl
@init{boolean hasArraySpec=false; boolean hasCoArraySpec=false;}
    :   T_IDENT ( T_LPAREN array_spec T_RPAREN {hasArraySpec=true;})?
            ( T_LBRACKET co_array_spec T_RBRACKET {hasCoArraySpec=true;})?
            {action.dimension_decl($T_IDENT, hasArraySpec, hasCoArraySpec);}
    ;

// R509-F2008
// ERR_CHK 509-F2008 at least one of the array specs must exist
dimension_spec
    :   T_DIMENSION ( T_LPAREN array_spec T_RPAREN )?
            ( T_LBRACKET co_array_spec T_RBRACKET )?
            { action.dimension_spec($T_DIMENSION); }
    ;

// R536
// generic_name_list substituted for dummy_arg_name_list
intent_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_INTENT T_LPAREN intent_spec T_RPAREN
            ( T_COLON_COLON )? generic_name_list end_of_stmt
            {action.intent_stmt(lbl,$T_INTENT,$end_of_stmt.tk);}
    ;

// R537
// generic_name_list substituted for dummy_arg_name_list
optional_stmt
@init{Token lbl = null;}
    :   (label {lbl=$label.tk;})? T_OPTIONAL ( T_COLON_COLON )?
            generic_name_list end_of_stmt
            { action.optional_stmt(lbl, $T_OPTIONAL, $end_of_stmt.tk); }

    ;

// R538
parameter_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_PARAMETER T_LPAREN
            named_constant_def_list T_RPAREN end_of_stmt
            {action.parameter_stmt(lbl,$T_PARAMETER,$end_of_stmt.tk);}
    ;

named_constant_def_list
@init{ int count=0;}
    :          {action.named_constant_def_list__begin();}
        named_constant_def {count++;}
            ( T_COMMA named_constant_def {count++;} )*
              {action.named_constant_def_list(count);}
    ;

// R539
// ERR_CHK 539 initialization_expr replaced by expr
// ERR_CHK 539 named_constant replaced by T_IDENT
named_constant_def
    :    T_IDENT T_EQUALS expr
            {action.named_constant_def($T_IDENT);}
    ;

// R540
pointer_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_POINTER ( T_COLON_COLON )?
            pointer_decl_list end_of_stmt
            {action.pointer_stmt(lbl,$T_POINTER,$end_of_stmt.tk);}
    ;

pointer_decl_list
@init{ int count=0;}
    :          {action.pointer_decl_list__begin();}
        pointer_decl {count++;} ( T_COMMA pointer_decl {count++;} )*
              {action.pointer_decl_list(count);}
    ;

// R541
// T_IDENT inlined as object_name and proc_entity_name (removing second alt)
pointer_decl
@init{boolean hasSpecList=false;}
    :    T_IDENT ( T_LPAREN deferred_shape_spec_list T_RPAREN
            {hasSpecList=true;})?
            {action.pointer_decl($T_IDENT,hasSpecList);}
    ;

// R542
// generic_name_list substituted for entity_name_list
protected_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_PROTECTED ( T_COLON_COLON )?
            generic_name_list end_of_stmt
            {action.protected_stmt(lbl,$T_PROTECTED,$end_of_stmt.tk);}
    ;

// R543
save_stmt
@init{Token lbl = null;boolean hasSavedEntityList=false;}
    : (label {lbl=$label.tk;})? T_SAVE ( ( T_COLON_COLON )?
            saved_entity_list {hasSavedEntityList=true;})? end_of_stmt
            {action.save_stmt(lbl,$T_SAVE,$end_of_stmt.tk,hasSavedEntityList);}
    ;

// R544
// T_IDENT inlined for object_name, proc_pointer_name (removing second alt),
// and common_block_name
saved_entity
    :    id=T_IDENT
            {action.saved_entity(id, false);}
    |    T_SLASH id=T_IDENT T_SLASH
            {action.saved_entity(id, true);}    // is common block name
    ;

saved_entity_list
@init{ int count=0;}
    :          {action.saved_entity_list__begin();}
        saved_entity {count++;} ( T_COMMA saved_entity {count++;} )*
              {action.saved_entity_list(count);}
    ;


// R545 proc_pointer_name was name inlined as T_IDENT

// R546, R555-F2008
// T_IDENT inlined for object_name
target_stmt
@init{Token lbl = null;int count=1;}
    :    (label {lbl=$label.tk;})? T_TARGET ( T_COLON_COLON )? target_decl
            ( T_COMMA target_decl {count++;} )* end_of_stmt
            {action.target_stmt(lbl,$T_TARGET,$end_of_stmt.tk,count);}
    ;

// R556-F2008
target_decl
@init{boolean hasArraySpec=false; boolean hasCoArraySpec=false;}
    : T_IDENT ( T_LPAREN array_spec T_RPAREN {hasArraySpec=true;} )?
              ( T_LBRACKET co_array_spec T_RBRACKET {hasCoArraySpec=true;} )?
            {action.target_decl($T_IDENT,hasArraySpec,hasCoArraySpec);}
    ;



