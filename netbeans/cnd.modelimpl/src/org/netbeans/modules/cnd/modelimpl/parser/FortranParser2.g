parser grammar FortranParser2;

@members {

    public void reportError(RecognitionException re) {
        gParent.reportError(re);
        gParent.hasErrorOccurred = true;
    }


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
        if(this.gParent.inputStreams.empty() == false) {
            if(input.LA(1) != APTTokenTypes.EOF) {
                FortranToken next = (FortranToken)(input.LT(1));
                String tosName = this.gParent.inputStreams.peek();
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
                        this.gParent.inputStreams.push(nextName);
                        return nextName;
                    }
                }
            }
        }

        return null;
    }

}// end members









// R547
// generic_name_list substituted for dummy_arg_name_list
value_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_VALUE ( T_COLON_COLON )?
            generic_name_list end_of_stmt
            {gParent.action.value_stmt(lbl,$T_VALUE,$end_of_stmt.tk);}
    ;

// R548
// generic_name_list substituted for object_name_list
volatile_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_VOLATILE ( T_COLON_COLON )?
            generic_name_list end_of_stmt
            {gParent.action.volatile_stmt(lbl,$T_VOLATILE,$end_of_stmt.tk);}
    ;

// R549
implicit_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_IMPLICIT implicit_spec_list end_of_stmt
            {gParent.action.implicit_stmt(lbl, $T_IMPLICIT, null, $end_of_stmt.tk,
                true);} // hasImplicitSpecList=true
    |    (label {lbl=$label.tk;})? T_IMPLICIT T_NONE end_of_stmt
            {gParent.action.implicit_stmt(lbl, $T_IMPLICIT, $T_NONE, $end_of_stmt.tk,
                false);} // hasImplicitSpecList=false
    ;

// R550
implicit_spec
    :    declaration_type_spec T_LPAREN letter_spec_list T_RPAREN
        { gParent.action.implicit_spec(); }
    ;

implicit_spec_list
@init{ int count=0;}
    :          {gParent.action.implicit_spec_list__begin();}
        implicit_spec {count++;} ( T_COMMA implicit_spec {count++;} )*
              {gParent.action.implicit_spec_list(count);}
    ;


// R551
// TODO: here, we'll accept a T_IDENT, and then we'll have to do error
// checking on it.
letter_spec
    : id1=T_IDENT ( T_MINUS id2=T_IDENT )?
        { gParent.action.letter_spec(id1, id2); }
    ;

letter_spec_list
@init{ int count=0;}
    :          {gParent.action.letter_spec_list__begin();}
        letter_spec {count++;} ( T_COMMA letter_spec {count++;} )*
              {gParent.action.letter_spec_list(count);}
    ;

// R552
// T_IDENT inlined for namelist_group_name
namelist_stmt
@init{Token lbl = null;int count =1;}
    :    (label {lbl=$label.tk;})? T_NAMELIST T_SLASH nlName=T_IDENT T_SLASH
            {gParent.action.namelist_group_name(nlName);}
        namelist_group_object_list
        ( ( T_COMMA )?  T_SLASH nlName=T_IDENT T_SLASH
            {gParent.action.namelist_group_name(nlName);}
        namelist_group_object_list {count++;})* end_of_stmt
            {gParent.action.namelist_stmt(lbl,$T_NAMELIST,$end_of_stmt.tk,count);}
    ;

// R553 namelist_group_object was variable_name inlined as T_IDENT

// T_IDENT inlined for namelist_group_object
namelist_group_object_list
@init{ int count=0;}
    :          {gParent.action.namelist_group_object_list__begin();}
        goName=T_IDENT {gParent.action.namelist_group_object(goName); count++;}
            ( T_COMMA goName=T_IDENT
            {gParent.action.namelist_group_object(goName); count++;} )*
              {gParent.action.namelist_group_object_list(count);}
    ;

// R554
equivalence_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_EQUIVALENCE equivalence_set_list
            end_of_stmt
            {gParent.action.equivalence_stmt(lbl, $T_EQUIVALENCE, $end_of_stmt.tk);}
    ;

// R555
equivalence_set
    :    T_LPAREN equivalence_object T_COMMA equivalence_object_list T_RPAREN
        { gParent.action.equivalence_set(); }
    ;


equivalence_set_list
@init{ int count=0;}
    :          {gParent.action.equivalence_set_list__begin();}
        equivalence_set {count++;} ( T_COMMA equivalence_set {count++;} )*
              {gParent.action.equivalence_set_list(count);}
    ;

// R556
// T_IDENT inlined for variable_name
// data_ref inlined for array_element
// data_ref isa T_IDENT so T_IDENT deleted (removing first alt)
// substring isa data_ref so data_ref deleted (removing second alt)
equivalence_object
    :    substring { gParent.action.equivalence_object(); }
    ;

equivalence_object_list
@init{ int count=0;}
    :          {gParent.action.equivalence_object_list__begin();}
        equivalence_object {count++;}
            ( T_COMMA equivalence_object {count++;} )*
              {gParent.action.equivalence_object_list(count);}
    ;

// R557
// gParent.action.common_block_name must be called here because it needs
//     to be called even if optional '/common_block_name/' is not present
common_stmt
@init{Token lbl=null; int numBlocks=1;}
    : (label {lbl=$label.tk;})?
        T_COMMON ( cb_name=common_block_name )?
            { gParent.action.common_block_name(cb_name.id); }
        common_block_object_list
        ( ( T_COMMA )? cb_name=common_block_name
            { gParent.action.common_block_name(cb_name.id); }
        common_block_object_list {numBlocks++;} )* end_of_stmt
            {gParent.action.common_stmt(lbl, $T_COMMON, $end_of_stmt.tk, numBlocks);}
    ;

// T_SLASH_SLASH must be a option in case there are no spaces slashes, '//'
common_block_name returns [Token id]
    : T_SLASH_SLASH {retval.id=null;}
    | T_SLASH (T_IDENT)? T_SLASH {retval.id=$T_IDENT;}
    ;

// R558
// T_IDENT inlined for variable_name and proc_pointer_name
// T_IDENT covered by first alt so second deleted
common_block_object
@init{boolean hasShapeSpecList=false;}
    : T_IDENT ( T_LPAREN explicit_shape_spec_list T_RPAREN
            {hasShapeSpecList=true;})?
            {gParent.action.common_block_object($T_IDENT,hasShapeSpecList);}
    ;

common_block_object_list
@init{ int count=0;}
    :          {gParent.action.common_block_object_list__begin();}
        common_block_object {count++;}
            ( T_COMMA common_block_object {count++;} )*
              {gParent.action.common_block_object_list(count);}
    ;

/*
Section 6:
 */

// R601
variable
    :    designator {gParent.action.variable();}
    ;

// R602 variable_name was name inlined as T_IDENT

// R603
//  :   object-name             // T_IDENT (data-ref isa T_IDENT)
//    |    array-element           // R616 is data-ref
//    |    array-section           // R617 is data-ref [ (substring-range) ]
//    |    structure-component     // R614 is data-ref
//    |    substring
// (substring-range) may be matched in data-ref
// this rule is now identical to substring
designator
@init{boolean hasSubstringRange = false;}
    :    data_ref (T_LPAREN substring_range {hasSubstringRange=true;} T_RPAREN)?
            { gParent.action.designator(hasSubstringRange); }
    |    char_literal_constant T_LPAREN substring_range T_RPAREN
            { hasSubstringRange=true; gParent.action.substring(hasSubstringRange); }
    ;

//
// a function_reference is ambiguous with designator, ie, foo(b) could be an
// array element
//    function_reference : procedure_designator T_LPAREN
// ( actual_arg_spec_list )? T_RPAREN
//                       procedure_designator isa data_ref
// C1220 (R1217) The procedure-designator shall designate a function.
// data_ref may (or not) match T_LPAREN ( actual_arg_spec_list )? T_RPAREN,
// so is optional
designator_or_func_ref
@init {
    boolean hasSubstringRangeOrArgList = false;
    boolean hasSubstringRange = false;
}
@after {
    gParent.action.designator_or_func_ref();
}
    :    data_ref (T_LPAREN substring_range_or_arg_list
                    {
                        hasSubstringRangeOrArgList = true;
                        hasSubstringRange=
                            $substring_range_or_arg_list.isSubstringRange;
                    }
                  T_RPAREN)?
            {
                if (hasSubstringRangeOrArgList) {
                    if (hasSubstringRange) {
                        gParent.action.designator(hasSubstringRange);
                    } else {
                        // hasActualArgSpecList=true
                        gParent.action.function_reference(true);
                    }
                }
            }
    |    char_literal_constant T_LPAREN substring_range T_RPAREN
            { hasSubstringRange=true; gParent.action.substring(hasSubstringRange); }
    ;

substring_range_or_arg_list returns [boolean isSubstringRange]
@init {
    boolean hasUpperBound = false;
    Token keyword = null;
    int count = 0;
}
@after {
    gParent.action.substring_range_or_arg_list();
}
    :    T_COLON (expr {hasUpperBound = true;})? // substring_range
            {
                // hasLowerBound=false
                gParent.action.substring_range(false, hasUpperBound);
                retval.isSubstringRange=true;
            }
    |        {
                /* mimic actual-arg-spec-list */
                gParent.action.actual_arg_spec_list__begin();
            }
        expr substr_range_or_arg_list_suffix
            {
                retval.isSubstringRange =
                    $substr_range_or_arg_list_suffix.isSubstringRange;
            }
    |        {
                /* mimic actual-arg-spec-list */
                gParent.action.actual_arg_spec_list__begin();
            }
        T_IDENT T_EQUALS expr
            {
                count++;
                gParent.action.actual_arg(true, null);
                gParent.action.actual_arg_spec($T_IDENT);
            }
        ( T_COMMA actual_arg_spec {count++;} )*
            {
                gParent.action.actual_arg_spec_list(count);
                retval.isSubstringRange = false;
            }
    |        {
                /* mimic actual-arg-spec-list */
                gParent.action.actual_arg_spec_list__begin();
            }
        ( T_IDENT T_EQUALS {keyword=$T_IDENT;} )? T_ASTERISK label
            {
                count++;
                gParent.action.actual_arg(false, $label.tk);
                gParent.action.actual_arg_spec(keyword);
            }
        ( T_COMMA actual_arg_spec {count++;} )*
            {
                gParent.action.actual_arg_spec_list(count);
                retval.isSubstringRange = false;
            }
    ;

substr_range_or_arg_list_suffix returns [boolean isSubstringRange]
@init{boolean hasUpperBound = false; int count = 0;}
@after {
    gParent.action.substr_range_or_arg_list_suffix();
}
    :        {
                // guessed wrong on list creation, inform of error
                gParent.action.actual_arg_spec_list(-1);
            }
        T_COLON (expr {hasUpperBound=true;})? // substring_range
            {
                // hasLowerBound=true
                gParent.action.substring_range(true, hasUpperBound);
                retval.isSubstringRange = true;
            }
    |
            {
                count++;
                gParent.action.actual_arg(true, null);    // hasExpr=true, label=null
                gParent.action.actual_arg_spec(null);        // keywork=null
            }
        ( T_COMMA actual_arg_spec {count++;} )*
            {
                gParent.action.actual_arg_spec_list(count);
                retval.isSubstringRange=false;
            }    // actual_arg_spec_list
    ;

// R604
logical_variable
    :    variable
            { gParent.action.logical_variable(); }
    ;

// R605
default_logical_variable
    :    variable
            { gParent.action.default_logical_variable(); }
    ;

scalar_default_logical_variable
    :    variable
            { gParent.action.scalar_default_logical_variable(); }
    ;

// R606
char_variable
    :    variable
            { gParent.action.char_variable(); }
    ;

// R607
default_char_variable
    :    variable
            { gParent.action.default_char_variable(); }
    ;

scalar_default_char_variable
    :    variable
            { gParent.action.scalar_default_char_variable(); }
    ;

// R608
int_variable
    :    variable
            { gParent.action.int_variable(); }
    ;

// R609
// C608 (R610) parent_string shall be of type character
// fix for ambiguity in data_ref allows it to match T_LPAREN substring_range
// T_RPAREN, so required T_LPAREN substring_range T_RPAREN made optional
// ERR_CHK 609 ensure final () is (substring-range)
substring
@init{boolean hasSubstringRange = false;}
    :    data_ref (T_LPAREN substring_range {hasSubstringRange=true;} T_RPAREN)?
            { gParent.action.substring(hasSubstringRange); }
    |    char_literal_constant T_LPAREN substring_range T_RPAREN
            { gParent.action.substring(true); }
    ;

// R610 parent_string inlined in R609 as (data_ref | char_literal_constant)
// T_IDENT inlined for scalar_variable_name
// data_ref inlined for scalar_structure_component and array_element
// data_ref isa T_IDENT so T_IDENT deleted
// scalar_constant replaced by char_literal_constant as data_ref isa T_IDENT
// and must be character

// R611
// ERR_CHK 611 scalar_int_expr replaced by expr
substring_range
@init{
    boolean hasLowerBound = false;
    boolean hasUpperBound = false;
}
    :    (expr {hasLowerBound = true;})? T_COLON    (expr {hasUpperBound = true;})?
            { gParent.action.substring_range(hasLowerBound, hasUpperBound); }
    ;

// R612
data_ref
@init{int numPartRefs = 0;}
    :    part_ref {numPartRefs += 1;} ( T_PERCENT part_ref {numPartRefs += 1;})*
            {gParent.action.data_ref(numPartRefs);}
    ;

// R613, R613-F2008
// T_IDENT inlined for part_name
// with k=2, this path is chosen over T_LPAREN substring_range T_RPAREN
// TODO error: if a function call, should match id rather than
// (section_subscript_list)
// a = foo(b) is ambiguous YUK...
// TODO putback F2008
part_ref
options {k=2;}
@init{boolean hasSSL = false;
      boolean hasImageSelector = false;
     }
    :    ( T_IDENT T_LPAREN) => T_IDENT T_LPAREN section_subscript_list T_RPAREN
        ( image_selector {hasImageSelector=true;})?
            {hasSSL=true; gParent.action.part_ref($T_IDENT, hasSSL, hasImageSelector);}
//    |     T_IDENT image_selector
//            {hasImageSelector=true; gParent.action.part_ref($T_IDENT, hasSSL,
//                hasImageSelector);}
    |    T_IDENT
            {gParent.action.part_ref($T_IDENT, hasSSL, hasImageSelector);}
    ;

// R614 structure_component inlined as data_ref

// R615 type_param_inquiry inlined in R701 then deleted as can be designator
// T_IDENT inlined for type_param_name

// R616 array_element inlined as data_ref

// R617 array_section inlined in R603

// R618 subscript inlined as expr
// ERR_CHK 618 scalar_int_expr replaced by expr

// R619
// expr inlined for subscript, vector_subscript, and stride (thus deleted
// option 3)
// refactored first optional expr from subscript_triplet
// modified to also match actual_arg_spec_list to reduce ambiguities and
// need for backtracking
section_subscript returns [boolean isEmpty]
@init {
    boolean hasLowerBounds = false;
    boolean hasUpperBounds = false;
    boolean hasStride = false;
    boolean hasExpr = false;
}
    :    expr section_subscript_ambiguous
    |    T_COLON (expr {hasUpperBounds=true;})?
            (T_COLON expr {hasStride=true;})?
            { gParent.action.section_subscript(hasLowerBounds, hasUpperBounds,
                hasStride, false); }
    |   T_COLON_COLON expr
            {hasStride=true; gParent.action.section_subscript(hasLowerBounds,
                hasUpperBounds, hasStride, false);}
    |    T_IDENT T_EQUALS expr    // could be an actual-arg, see R1220
            { hasExpr=true; gParent.action.actual_arg(hasExpr, null);
                gParent.action.actual_arg_spec($T_IDENT); }
    |    T_IDENT T_EQUALS T_ASTERISK label // could be an actual-arg, see R1220
            { gParent.action.actual_arg(hasExpr, $label.tk);
                gParent.action.actual_arg_spec($T_IDENT); }
    |    T_ASTERISK label /* could be an actual-arg, see R1220 */
            { gParent.action.actual_arg(hasExpr, $label.tk);
                gParent.action.actual_arg_spec(null); }
    |        { retval.isEmpty = true; /* empty could be an actual-arg, see R1220 */ }
    ;

section_subscript_ambiguous
@init {
    boolean hasLowerBound = true;
    boolean hasUpperBound = false;
    boolean hasStride = false;
    boolean isAmbiguous = false;
}
    :    T_COLON (expr {hasUpperBound=true;})? (T_COLON expr {hasStride=true;})?
            { gParent.action.section_subscript(hasLowerBound, hasUpperBound,
                hasStride, isAmbiguous);}
        // this alternative is necessary because if alt1 above has no expr
        // following the first : and there is the optional second : with no
        // WS between the two, the lexer will make a T_COLON_COLON token
        // instead of two T_COLON tokens.  in this case, the second expr is
        // required.  for an example, see J3/04-007, Note 7.44.
    |   T_COLON_COLON expr
            { hasStride=true;
              gParent.action.section_subscript(hasLowerBound, hasUpperBound,
                                       hasStride, isAmbiguous);}
    |        { /* empty, could be an actual-arg, see R1220 */
                isAmbiguous=true;
                gParent.action.section_subscript(hasLowerBound, hasUpperBound,
                    hasStride, isAmbiguous);
            }
    ;

section_subscript_list
@init{int count = 0;}
    :        { gParent.action.section_subscript_list__begin(); }
        isEmpty=section_subscript
            {
                if (isEmpty.isEmpty == false) count += 1;
            }
        (T_COMMA section_subscript {count += 1;})*
            { gParent.action.section_subscript_list(count); }
    ;

// R620 subscript_triplet inlined in R619
// inlined expr as subscript and stride in subscript_triplet

// R621 stride inlined as expr
// ERR_CHK 621 scalar_int_expr replaced by expr

// R622
// ERR_CHK 622 int_expr replaced by expr
vector_subscript
    :    expr
            { gParent.action.vector_subscript(); }
    ;

// R622 inlined vector_subscript as expr in R619
// ERR_CHK 622 int_expr replaced by expr

// R624-F2008
image_selector
@init {
    int exprCount = 0;
}
    :    T_LBRACKET expr ( T_COMMA expr { exprCount++; })* T_RBRACKET
            { gParent.action.image_selector(exprCount); }
    ;

// R625-F2008 co_subscript was scalar_int_expr inlined as expr in R624-F2004

// R623
// modified to remove backtracking by looking for the token inserted during
// the lexical prepass if a :: was found (which required alt1 below).
allocate_stmt
@init{Token lbl = null;
      boolean hasTypeSpec = false;
      boolean hasAllocOptList = false;
}
    :    (label {lbl=$label.tk;})? T_ALLOCATE_STMT_1 T_ALLOCATE T_LPAREN
        type_spec T_COLON_COLON
        allocation_list
        ( T_COMMA alloc_opt_list {hasAllocOptList=true;} )? T_RPAREN
            end_of_stmt
            {
                hasTypeSpec = true;
                gParent.action.allocate_stmt(lbl, $T_ALLOCATE, $end_of_stmt.tk,
                                     hasTypeSpec, hasAllocOptList);
            }
    |    (label {lbl=$label.tk;})? T_ALLOCATE T_LPAREN
        allocation_list
        ( T_COMMA alloc_opt_list {hasAllocOptList=true;} )? T_RPAREN
            end_of_stmt
            {
                gParent.action.allocate_stmt(lbl, $T_ALLOCATE, $end_of_stmt.tk,
                                     hasTypeSpec, hasAllocOptList);
            }
    ;

// R624
// ERR_CHK 624 source_expr replaced by expr
// stat_variable and errmsg_variable replaced by designator
alloc_opt
    :    T_IDENT T_EQUALS expr
            /* {'STAT','ERRMSG'} are variables {SOURCE'} is expr */
            { gParent.action.alloc_opt($T_IDENT); }
    ;

alloc_opt_list
@init{ int count=0;}
    :          {gParent.action.alloc_opt_list__begin();}
        alloc_opt {count++;} ( T_COMMA alloc_opt {count++;} )*
              {gParent.action.alloc_opt_list(count);}
    ;

// R625 stat_variable was scalar_int_variable inlined in R624 and R636
// R626 errmsg_variable was scalar_default_char_variable inlined in R624
// and R636
// R627 inlined source_expr was expr

// R628, R631-F2008
allocation
@init {
    boolean hasAllocateShapeSpecList = false;
    boolean hasAllocateCoArraySpec = false;
}
    : allocate_object
        ( T_LPAREN allocate_shape_spec_list {hasAllocateShapeSpecList=true;}
            T_RPAREN )?
        ( T_LBRACKET allocate_co_array_spec {hasAllocateCoArraySpec=true;}
            T_RBRACKET )?
            { gParent.action.allocation(hasAllocateShapeSpecList,
                hasAllocateCoArraySpec); }
    ;


allocation_list
@init{ int count=0;}
    :          {gParent.action.allocation_list__begin();}
        allocation {count++;} ( T_COMMA allocation {count++;} )*
              {gParent.action.allocation_list(count);}
    ;

// R629
// T_IDENT inlined for variable_name
// data_ref inlined for structure_component
// data_ref isa T_IDENT so T_IDENT deleted
allocate_object
    :    data_ref
            { gParent.action.allocate_object(); }
    ;

allocate_object_list
@init{ int count=0;}
    :          {gParent.action.allocate_object_list__begin();}
        allocate_object {count++;} ( T_COMMA allocate_object {count++;} )*
              {gParent.action.allocate_object_list(count);}
    ;

// R630
// ERR_CHK 630a lower_bound_expr replaced by expr
// ERR_CHK 630b upper_bound_expr replaced by expr
allocate_shape_spec
@init{boolean hasLowerBound = false; boolean hasUpperBound = true;}
    :    expr (T_COLON expr)?
            {    // note, allocate-shape-spec always has upper bound
                // grammar was refactored to remove left recursion,
                // looks deceptive
                gParent.action.allocate_shape_spec(hasLowerBound, hasUpperBound);
            }
    ;

allocate_shape_spec_list
@init{ int count=0;}
    :          {gParent.action.allocate_shape_spec_list__begin();}
        allocate_shape_spec {count++;}
            ( T_COMMA allocate_shape_spec {count++;} )*
              {gParent.action.allocate_shape_spec_list(count);}
    ;

// R631 inlined lower_bound_expr was scalar_int_expr

// R632 inlined upper_bound_expr was scalar_int_expr

// R633
nullify_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})?
        T_NULLIFY T_LPAREN pointer_object_list T_RPAREN end_of_stmt
            { gParent.action.nullify_stmt(lbl, $T_NULLIFY, $end_of_stmt.tk); }
    ;

// R634
// T_IDENT inlined for variable_name and proc_pointer_name
// data_ref inlined for structure_component
// data_ref can be a T_IDENT so T_IDENT deleted
pointer_object
    :    data_ref
            { gParent.action.pointer_object(); }
    ;

pointer_object_list
@init{ int count=0;}
    :          {gParent.action.pointer_object_list__begin();}
        pointer_object {count++;} ( T_COMMA pointer_object {count++;} )*
              {gParent.action.pointer_object_list(count);}
    ;

// R635
deallocate_stmt
@init{Token lbl = null; boolean hasDeallocOptList=false;}
    :    (label {lbl=$label.tk;})? T_DEALLOCATE T_LPAREN allocate_object_list
            ( T_COMMA dealloc_opt_list {hasDeallocOptList=true;})?
            T_RPAREN end_of_stmt
            {gParent.action.deallocate_stmt(lbl, $T_DEALLOCATE, $end_of_stmt.tk,
                hasDeallocOptList);}
    ;

// R636
// stat_variable and errmsg_variable replaced by designator
dealloc_opt
    :    T_IDENT /* {'STAT','ERRMSG'} */ T_EQUALS designator
            { gParent.action.dealloc_opt($T_IDENT); }
    ;

dealloc_opt_list
@init{ int count=0;}
    :          {gParent.action.dealloc_opt_list__begin();}
        dealloc_opt {count++;} ( T_COMMA dealloc_opt {count++;} )*
              {gParent.action.dealloc_opt_list(count);}
    ;

// R636-F2008
// TODO putback F2008
allocate_co_array_spec
    :   /* ( allocate_co_shape_spec_list T_COMMA )? ( expr T_COLON )? */
            T_ASTERISK
            { gParent.action.allocate_co_array_spec(); }
    ;

// R637-F2008
allocate_co_shape_spec
@init { boolean hasExpr = false; }
    :    expr ( T_COLON expr { hasExpr = true; })?
            { gParent.action.allocate_co_shape_spec(hasExpr); }
    ;

allocate_co_shape_spec_list
@init{ int count=0;}
    :          {gParent.action.allocate_co_shape_spec_list__begin();}
        allocate_co_shape_spec {count++;}
            ( T_COMMA allocate_co_shape_spec {count++;} )*
              {gParent.action.allocate_co_shape_spec_list(count);}
    ;

/*
 * Section 7:
 */

// R701
// constant replaced by literal_constant as T_IDENT can be designator
// T_IDENT inlined for type_param_name
// data_ref in designator can be a T_IDENT so T_IDENT deleted
// type_param_inquiry is designator T_PERCENT T_IDENT can be designator so
// deleted
// function_reference integrated with designator (was ambiguous) and
// deleted (to reduce backtracking)
primary
options {backtrack=true;}       // alt 1,4 ambiguous
@after {
    gParent.action.primary();
}
    :    designator_or_func_ref
    |    literal_constant
    |    array_constructor
    |    structure_constructor
    |    T_LPAREN expr T_RPAREN
    ;

// R702
level_1_expr
@init{Token tk = null;} //@init{INIT_TOKEN_NULL(tk);}
    : (defined_unary_op {tk = $defined_unary_op.tk;})? primary
            {gParent.action.level_1_expr(tk);}
    ;

// R703
defined_unary_op returns [Token tk]
    :    T_DEFINED_OP {retval.tk = $T_DEFINED_OP;}
            { gParent.action.defined_unary_op($T_DEFINED_OP); }
    ;

// inserted as R704 functionality
power_operand
@init{boolean hasPowerOperand = false;}
    : level_1_expr (power_op power_operand {hasPowerOperand = true;})?
            {gParent.action.power_operand(hasPowerOperand);}
    ;

// R704
// see power_operand
mult_operand
@init{int numMultOps = 0;}
//    : level_1_expr ( power_op mult_operand )?
//    : power_operand
    : power_operand (mult_op power_operand
            { gParent.action.mult_operand__mult_op($mult_op.tk); numMultOps += 1; })*
            { gParent.action.mult_operand(numMultOps); }
    ;

// R705
// moved leading optionals to mult_operand
add_operand
@init{int numAddOps = 0;}
//    : ( add_operand mult_op )? mult_operand
//    : ( mult_operand mult_op )* mult_operand
    : (tk=add_op)? mult_operand
        ( tk1=add_op mult_operand
            {gParent.action.add_operand__add_op(tk1.tk); numAddOps += 1;}
        )*
            {gParent.action.add_operand(tk!=null?tk.tk:null, numAddOps);}
    ;

// R706
// moved leading optionals to add_operand
level_2_expr
@init{int numConcatOps = 0;}
//    : ( ( level_2_expr )? add_op )? add_operand
// check notes on how to remove this left recursion
// (WARNING something like the following)
//    : (add_op)? ( add_operand add_op )* add_operand
    : add_operand ( concat_op add_operand {numConcatOps += 1;})*
            {gParent.action.level_2_expr(numConcatOps);}
    ;

// R707
power_op returns [Token tk]
    :    T_POWER    {retval.tk = $T_POWER;}
            { gParent.action.power_op($T_POWER); }
    ;

// R708
mult_op returns [Token tk]
    :    T_ASTERISK    { retval.tk = $T_ASTERISK; gParent.action.mult_op(retval.tk); }
    |    T_SLASH        { retval.tk = $T_SLASH; gParent.action.mult_op(retval.tk); }
    ;

// R709
add_op returns [Token tk]
    :    T_PLUS  { retval.tk = $T_PLUS; gParent.action.add_op(retval.tk); }
    |    T_MINUS { retval.tk = $T_MINUS; gParent.action.add_op(retval.tk); }
    ;

// R710
// moved leading optional to level_2_expr
level_3_expr
@init{Token relOp = null;} //@init{INIT_TOKEN_NULL(relOp);}
//    : ( level_3_expr concat_op )? level_2_expr
//    : ( level_2_expr concat_op )* level_2_expr
    : level_2_expr (rel_op level_2_expr {relOp = $rel_op.tk;})?
            {gParent.action.level_3_expr(relOp);}
    ;

// R711
concat_op returns [Token tk]
    :    T_SLASH_SLASH    { retval.tk = $T_SLASH_SLASH; gParent.action.concat_op(retval.tk); }
    ;

// R712
// moved leading optional to level_3_expr
// inlined level_3_expr for level_4_expr in R714
//level_4_expr
//    : ( level_3_expr rel_op )? level_3_expr
//    : level_3_expr
//    ;

// R713
rel_op returns [Token tk]
@after {
    gParent.action.rel_op(retval.tk);
}
    :    T_EQ                {retval.tk=$T_EQ;}
    |    T_NE                {retval.tk=$T_NE;}
    |    T_LT                {retval.tk=$T_LT;}
    |    T_LE                {retval.tk=$T_LE;}
    |    T_GT                {retval.tk=$T_GT;}
    |    T_GE                {retval.tk=$T_GE;}
    |    T_EQ_EQ                {retval.tk=$T_EQ_EQ;}
    |    T_SLASH_EQ            {retval.tk=$T_SLASH_EQ;}
    |    T_LESSTHAN            {retval.tk=$T_LESSTHAN;}
    |    T_LESSTHAN_EQ        {retval.tk=$T_LESSTHAN_EQ;}
    |    T_GREATERTHAN        {retval.tk=$T_GREATERTHAN;}
    |    T_GREATERTHAN_EQ    {retval.tk=$T_GREATERTHAN_EQ;}
    ;

// R714
// level_4_expr inlined as level_3_expr
and_operand
@init {
    boolean hasNotOp0 = false; // @init{INIT_BOOL_FALSE(hasNotOp0);
    boolean hasNotOp1 = false; // @init{INIT_BOOL_FALSE(hasNotOp1);
    int numAndOps = 0;
}
//    :    ( not_op )? level_3_expr
    :    (not_op {hasNotOp0=true;})?
        level_3_expr
        (and_op {hasNotOp1=false;} (not_op {hasNotOp1=true;})? level_3_expr
                {gParent.action.and_operand__not_op(hasNotOp1); numAndOps += 1;}
        )*
                {gParent.action.and_operand(hasNotOp0, numAndOps);}
    ;

// R715
// moved leading optional to or_operand
or_operand
@init{int numOrOps = 0;}
//    : ( or_operand and_op )? and_operand
//    : ( and_operand and_op )* and_operand
    : and_operand (or_op and_operand {numOrOps += 1;})*
            { gParent.action.or_operand(numOrOps); }
    ;

// R716
// moved leading optional to or_operand
// TODO - action for equiv_op token
equiv_operand
@init{int numEquivOps = 0;}
//    : ( equiv_operand or_op )? or_operand
//    : ( or_operand or_op )* or_operand
    : or_operand
        (equiv_op or_operand
            {gParent.action.equiv_operand__equiv_op($equiv_op.tk); numEquivOps += 1;}
        )*
            {gParent.action.equiv_operand(numEquivOps);}
    ;

// R717
// moved leading optional to equiv_operand
level_5_expr
@init{int numDefinedBinaryOps = 0;}
//    : ( level_5_expr equiv_op )? equiv_operand
//    : ( equiv_operand equiv_op )* equiv_operand
    : equiv_operand (defined_binary_op equiv_operand
            {gParent.action.level_5_expr__defined_binary_op($defined_binary_op.tk);
                numDefinedBinaryOps += 1;} )*
            {gParent.action.level_5_expr(numDefinedBinaryOps);}
    ;

// R718
not_op returns [Token tk]
    :    T_NOT { retval.tk = $T_NOT; gParent.action.not_op(retval.tk); }
    ;

// R719
and_op returns [Token tk]
    :    T_AND { retval.tk = $T_AND; gParent.action.and_op(retval.tk); }
    ;

// R720
or_op returns [Token tk]
    :    T_OR { retval.tk = $T_OR; gParent.action.or_op(retval.tk); }
    ;

// R721
equiv_op returns [Token tk]
    :    T_EQV { retval.tk = $T_EQV; gParent.action.equiv_op(retval.tk); }
    |    T_NEQV { retval.tk = $T_NEQV; gParent.action.equiv_op(retval.tk); }
    ;

// R722
// moved leading optional to level_5_expr
expr
//    : ( expr defined_binary_op )? level_5_expr
//    : ( level_5_expr defined_binary_op )* level_5_expr
    : level_5_expr
        {gParent.action.expr();}
    ;

// R723
defined_binary_op returns [Token tk]
    :    T_DEFINED_OP { retval.tk = $T_DEFINED_OP; gParent.action.defined_binary_op(retval.tk); }
    ;

// R724 inlined logical_expr was expr

// R725 inlined char_expr was expr

// R726 inlined default_char_expr

// R727 inlined int_expr

// R728 inlined numeric_expr was expr

// inlined scalar_numeric_expr was expr

// R729 inlined specification_expr was scalar_int_expr

// R730 inlined initialization_expr

// R731 inlined char_initialization_expr was char_expr

// inlined scalar_char_initialization_expr was char_expr

// R732 inlined int_initialization_expr was int_expr

// inlined scalar_int_initialization_expr was int_initialization_expr

// R733 inlined logical_initialization_expr was logical_expr

// inlined scalar_logical_initialization_expr was logical_expr

// R734
assignment_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_ASSIGNMENT_STMT variable
        T_EQUALS expr end_of_stmt
            {gParent.action.assignment_stmt(lbl, $end_of_stmt.tk);}
    ;

// R735
// ERR_TEST 735 ensure that part_ref in data_ref doesn't capture the T_LPAREN
// data_pointer_object and proc_pointer_object replaced by designator
// data_target and proc_target replaced by expr
// third alt covered by first alt so proc_pointer_object assignment deleted
// designator (R603), minus the substring part is data_ref, so designator
// replaced by data_ref,
// see NOTE 6.10 for why array-section does not have pointer attribute
// TODO: alt1 and alt3 require the backtracking.  if find a way to disambiguate
// them, should be able to remove backtracking.
pointer_assignment_stmt
options {backtrack=true;}
@init{Token lbl = null;}
    : (label {lbl=$label.tk;})? T_PTR_ASSIGNMENT_STMT data_ref T_EQ_GT
            expr end_of_stmt
            {gParent.action.pointer_assignment_stmt(lbl, $end_of_stmt.tk,false,false);}
    | (label {lbl=$label.tk;})? T_PTR_ASSIGNMENT_STMT data_ref T_LPAREN
            bounds_spec_list T_RPAREN T_EQ_GT expr end_of_stmt
            {gParent.action.pointer_assignment_stmt(lbl, $end_of_stmt.tk, true,false);}
    | (label {lbl=$label.tk;})? T_PTR_ASSIGNMENT_STMT data_ref T_LPAREN
            bounds_remapping_list T_RPAREN T_EQ_GT expr end_of_stmt
            {gParent.action.pointer_assignment_stmt(lbl, $end_of_stmt.tk, false,true);}
    ;

// R736
// ERR_CHK 736 ensure ( T_IDENT | designator ending in T_PERCENT T_IDENT)
// T_IDENT inlined for variable_name and data_pointer_component_name
// variable replaced by designator
data_pointer_object
    :    designator
            { gParent.action.data_pointer_object(); }
    ;

// R737
// ERR_CHK 737 lower_bound_expr replaced by expr
bounds_spec
    :    expr T_COLON
            { gParent.action.bounds_spec(); }
    ;

bounds_spec_list
@init{ int count=0;}
    :          {gParent.action.bounds_spec_list__begin();}
        bounds_spec {count++;} ( T_COMMA bounds_spec {count++;} )*
              {gParent.action.bounds_spec_list(count);}
    ;

// R738
// ERR_CHK 738a lower_bound_expr replaced by expr
// ERR_CHK 738b upper_bound_expr replaced by expr
bounds_remapping
    :    expr T_COLON expr
            { gParent.action.bounds_remapping(); }
    ;

bounds_remapping_list
@init{ int count=0;}
    :          {gParent.action.bounds_remapping_list__begin();}
        bounds_remapping {count++;} ( T_COMMA bounds_remapping {count++;} )*
              {gParent.action.bounds_remapping_list(count);}
    ;

// R739 data_target inlined as expr in R459 and R735
// expr can be designator (via primary) so variable deleted

// R740
// ERR_CHK 740 ensure ( T_IDENT | ends in T_PERCENT T_IDENT )
// T_IDENT inlined for proc_pointer_name
// proc_component_ref replaced by designator T_PERCENT T_IDENT replaced
// by designator
proc_pointer_object
    :    designator
            { gParent.action.proc_pointer_object(); }
    ;

// R741 proc_component_ref inlined as designator T_PERCENT T_IDENT in R740,
// R742, R1219, and R1221
// T_IDENT inlined for procedure_component_name
// designator inlined for variable

// R742 proc_target inlined as expr in R459 and R735
// ERR_CHK 736 ensure ( expr | designator ending in T_PERCENT T_IDENT)
// T_IDENT inlined for procedure_name
// T_IDENT isa expr so T_IDENT deleted
// proc_component_ref is variable T_PERCENT T_IDENT can be designator
// so deleted

// R743
// ERR_CHK 743 mask_expr replaced by expr
// assignment_stmt inlined for where_assignment_stmt
where_stmt
@init {
    Token lbl = null;
    gParent.action.where_stmt__begin();
}
    :
        (label {lbl=$label.tk;})? T_WHERE_STMT T_WHERE
        T_LPAREN expr T_RPAREN assignment_stmt
            {gParent.action.where_stmt(lbl, $T_WHERE);}
    ;

// R744
where_construct
@init {
    int numConstructs = 0;
    int numMaskedConstructs = 0;
    boolean hasMaskedElsewhere = false;
    int numElsewhereConstructs = 0;
    boolean hasElsewhere = false;
}
    :    where_construct_stmt ( where_body_construct {numConstructs += 1;} )*
          ( masked_elsewhere_stmt ( where_body_construct
                {numMaskedConstructs += 1;} )*
                {hasMaskedElsewhere = true;
                gParent.action.masked_elsewhere_stmt__end(numMaskedConstructs);}
          )*
          ( elsewhere_stmt ( where_body_construct
                {numElsewhereConstructs += 1;} )*
                {hasElsewhere = true;
                gParent.action.elsewhere_stmt__end(numElsewhereConstructs);}
          )?
         end_where_stmt
                {gParent.action.where_construct(numConstructs, hasMaskedElsewhere,
                    hasElsewhere);}
    ;

// R745
// ERR_CHK 745 mask_expr replaced by expr
where_construct_stmt
@init{Token id=null;}
    :    ( T_IDENT T_COLON {id=$T_IDENT;})? T_WHERE_CONSTRUCT_STMT T_WHERE
            T_LPAREN expr T_RPAREN end_of_stmt
                {gParent.action.where_construct_stmt(id, $T_WHERE, $end_of_stmt.tk);}
    ;

// R746
// assignment_stmt inlined for where_assignment_stmt
where_body_construct
@after {
    gParent.action.where_body_construct();
}
    :    assignment_stmt
    |    where_stmt
    |    where_construct
    ;

// R747 where_assignment_stmt inlined as assignment_stmt in R743 and R746

// R748 inlined mask_expr was logical_expr

// inlined scalar_mask_expr was scalar_logical_expr

// inlined scalar_logical_expr was logical_expr

// R749
// ERR_CHK 749 mask_expr replaced by expr
masked_elsewhere_stmt
@init{Token lbl = null;Token id=null;}
    :    (label {lbl=$label.tk;})? T_ELSE T_WHERE T_LPAREN expr T_RPAREN
            ( T_IDENT {id=$T_IDENT;})? end_of_stmt
            {gParent.action.masked_elsewhere_stmt(lbl, $T_ELSE, $T_WHERE, id,
                $end_of_stmt.tk);}
    |    (label {lbl=$label.tk;})? T_ELSEWHERE T_LPAREN expr T_RPAREN
            ( T_IDENT {id=$T_IDENT;})? end_of_stmt
            {gParent.action.masked_elsewhere_stmt(lbl, $T_ELSEWHERE, null,id,
                $end_of_stmt.tk);}
    ;

// R750
elsewhere_stmt
@init{ Token lbl = null; Token id=null;}
    :    (label {lbl=$label.tk;})? T_ELSE T_WHERE
            (T_IDENT {id=$T_IDENT;})? end_of_stmt
            {gParent.action.elsewhere_stmt(lbl, $T_ELSE, $T_WHERE, id,
                $end_of_stmt.tk);}
    |    (label {lbl=$label.tk;})? T_ELSEWHERE (T_IDENT {id=$T_IDENT;})?
            end_of_stmt
            {gParent.action.elsewhere_stmt(lbl, $T_ELSEWHERE, null, id,
                $end_of_stmt.tk);}
    ;

// R751
end_where_stmt
@init{Token lbl = null; Token id=null;} // @init{INIT_TOKEN_NULL(lbl);}
    : (label {lbl=$label.tk;})? T_END T_WHERE ( T_IDENT {id=$T_IDENT;} )?
        end_of_stmt
        {gParent.action.end_where_stmt(lbl, $T_END, $T_WHERE, id, $end_of_stmt.tk);}
    | (label {lbl=$label.tk;})? T_ENDWHERE ( T_IDENT {id=$T_IDENT;} )?
        end_of_stmt
        {gParent.action.end_where_stmt(lbl, $T_ENDWHERE, null, id, $end_of_stmt.tk);}
    ;

// R752
forall_construct
@after {
    gParent.action.forall_construct();
}
    :    forall_construct_stmt
        ( forall_body_construct )*
        end_forall_stmt
    ;

// R753
forall_construct_stmt
@init{Token lbl = null; Token id = null;}
    :    (label {lbl=$label.tk;})? ( T_IDENT T_COLON {id=$T_IDENT;})?
            T_FORALL_CONSTRUCT_STMT T_FORALL
            forall_header end_of_stmt
                {gParent.action.forall_construct_stmt(lbl, id, $T_FORALL,
                    $end_of_stmt.tk);}
    ;

// R754
// ERR_CHK 754 scalar_mask_expr replaced by expr
forall_header
@after {
    gParent.action.forall_header();
}
    : T_LPAREN forall_triplet_spec_list ( T_COMMA expr )? T_RPAREN
    ;

// R755
// T_IDENT inlined for index_name
// expr inlined for subscript and stride
forall_triplet_spec
@init{boolean hasStride=false;}
    : T_IDENT T_EQUALS expr T_COLON expr ( T_COLON expr {hasStride=true;})?
            {gParent.action.forall_triplet_spec($T_IDENT,hasStride);}
    ;


forall_triplet_spec_list
@init{ int count=0;}
    :          {gParent.action.forall_triplet_spec_list__begin();}
        forall_triplet_spec {count++;}
            ( T_COMMA forall_triplet_spec {count++;} )*
              {gParent.action.forall_triplet_spec_list(count);}
    ;

// R756
forall_body_construct
@after {
    gParent.action.forall_body_construct();
}
    :    forall_assignment_stmt
    |    where_stmt
    |    where_construct
    |    forall_construct
    |    forall_stmt
    ;

// R757
forall_assignment_stmt
    :    assignment_stmt
            {gParent.action.forall_assignment_stmt(false);}
    |    pointer_assignment_stmt
            {gParent.action.forall_assignment_stmt(true);}
    ;

// R758
end_forall_stmt
@init{Token lbl = null; Token id=null;}
    : (label {lbl=$label.tk;})? T_END T_FORALL ( T_IDENT {id=$T_IDENT;})?
        end_of_stmt
        {gParent.action.end_forall_stmt(lbl, $T_END, $T_FORALL, id, $end_of_stmt.tk);}
    | (label {lbl=$label.tk;})? T_ENDFORALL ( T_IDENT {id=$T_IDENT;})?
        end_of_stmt
        {gParent.action.end_forall_stmt(lbl, $T_ENDFORALL, null, id, $end_of_stmt.tk);}
    ;

// R759
// T_FORALL_STMT token is inserted by scanner to remove need for backtracking
forall_stmt
@init {
    Token lbl = null;
    gParent.action.forall_stmt__begin();
}
    :    (label {lbl=$label.tk;})? T_FORALL_STMT T_FORALL
        forall_header
        forall_assignment_stmt
            {gParent.action.forall_stmt(lbl, $T_FORALL);}
    ;

/*
 * Section 8:
 */

// R801
block
@after {
    gParent.action.block();
}
    :    ( execution_part_construct )*
    ;

// R802
if_construct
@after {
    gParent.action.if_construct();
}
    :   if_then_stmt block ( else_if_stmt block )* ( else_stmt block )?
            end_if_stmt
    ;

// R803
// ERR_CHK 803 scalar_logical_expr replaced by expr
if_then_stmt
@init{Token lbl = null; Token id=null;}
    : (label {lbl=$label.tk;})? ( T_IDENT T_COLON {id=$T_IDENT;} )? T_IF
            T_LPAREN expr T_RPAREN T_THEN end_of_stmt
            {gParent.action.if_then_stmt(lbl, id, $T_IF, $T_THEN, $end_of_stmt.tk);}
    ;

// R804
// ERR_CHK 804 scalar_logical_expr replaced by expr
else_if_stmt
@init{Token lbl = null; Token id=null;}
    : (label {lbl=$label.tk;})? T_ELSE T_IF
        T_LPAREN expr T_RPAREN T_THEN ( T_IDENT {id=$T_IDENT;} )? end_of_stmt
            {gParent.action.else_if_stmt(lbl, $T_ELSE, $T_IF, $T_THEN, id,
                $end_of_stmt.tk);}
    | (label {lbl=$label.tk;})? T_ELSEIF
        T_LPAREN expr T_RPAREN T_THEN ( T_IDENT {id=$T_IDENT;} )? end_of_stmt
            {gParent.action.else_if_stmt(lbl, $T_ELSEIF, null, $T_THEN, id,
                $end_of_stmt.tk);}
    ;

// R805
else_stmt
@init{Token lbl = null; Token id=null;}
    :    (label {lbl=$label.tk;})? T_ELSE ( T_IDENT {id=$T_IDENT;} )?
            end_of_stmt
            {gParent.action.else_stmt(lbl, $T_ELSE, id, $end_of_stmt.tk);}
    ;

// R806
end_if_stmt
@init{Token lbl = null; Token id=null;}
    : (label {lbl=$label.tk;})? T_END T_IF ( T_IDENT {id=$T_IDENT;} )?
        end_of_stmt
            {gParent.action.end_if_stmt(lbl, $T_END, $T_IF, id, $end_of_stmt.tk);}
    | (label {lbl=$label.tk;})? T_ENDIF    ( T_IDENT {id=$T_IDENT;} )?
            end_of_stmt
            {gParent.action.end_if_stmt(lbl, $T_ENDIF, null, id, $end_of_stmt.tk);}
    ;

// R807
// ERR_CHK 807 scalar_logical_expr replaced by expr
// T_IF_STMT inserted by scanner to remove need for backtracking
if_stmt
@init {
    Token lbl = null;
    gParent.action.if_stmt__begin();
}
    :    (label {lbl=$label.tk;})? T_IF_STMT T_IF T_LPAREN expr T_RPAREN
            action_stmt
                {gParent.action.if_stmt(lbl, $T_IF);}
    ;

// R808
case_construct
@after {
    gParent.action.case_construct();
}
    :    select_case_stmt ( case_stmt block )* end_select_stmt
    ;

// R809
// ERR_CHK 809 case_expr replaced by expr
select_case_stmt
@init{Token lbl = null; Token id=null; Token tk1 = null; Token tk2 = null;}
    :    (label {lbl=$label.tk;})? ( T_IDENT T_COLON {id=$T_IDENT;})?
        (T_SELECT T_CASE {tk1=$T_SELECT; tk2=$T_CASE;}
            | T_SELECTCASE {tk1=$T_SELECTCASE; tk2=null;} )
            T_LPAREN expr T_RPAREN end_of_stmt
            {gParent.action.select_case_stmt(lbl, id, tk1, tk2, $end_of_stmt.tk);}
    ;

// R810
case_stmt
@init{Token lbl = null; Token id=null;}
    :    (label {lbl=$label.tk;})? T_CASE case_selector
            ( T_IDENT {id=$T_IDENT;})? end_of_stmt
            { gParent.action.case_stmt(lbl, $T_CASE, id, $end_of_stmt.tk);}
    ;

// R811
end_select_stmt
@init{Token lbl = null; Token id=null;}
    : (label {lbl=$label.tk;})? T_END T_SELECT (T_IDENT {id=$T_IDENT;})?
        end_of_stmt
            {gParent.action.end_select_stmt(lbl, $T_END, $T_SELECT, id,
                $end_of_stmt.tk);}
    | (label {lbl=$label.tk;})? T_ENDSELECT    (T_IDENT {id=$T_IDENT;})?
        end_of_stmt
            {gParent.action.end_select_stmt(lbl, $T_ENDSELECT, null, id,
                $end_of_stmt.tk);}
    ;

// R812 inlined case_expr with expr was either scalar_int_expr
// scalar_char_expr scalar_logical_expr

// inlined scalar_char_expr with expr was char_expr

// R813
case_selector
    :    T_LPAREN
        case_value_range_list
        T_RPAREN
            { gParent.action.case_selector(null); }
    |    T_DEFAULT
            { gParent.action.case_selector($T_DEFAULT); }
    ;

// R814
case_value_range
@after {
    gParent.action.case_value_range();
}
    :    T_COLON case_value
    |    case_value case_value_range_suffix
    ;

case_value_range_suffix
@after {
    gParent.action.case_value_range_suffix();
}
    :    T_COLON ( case_value )?
    |    { /* empty */ }
    ;

case_value_range_list
@init{ int count=0;}
    :          {gParent.action.case_value_range_list__begin();}
        case_value_range {count++;} ( T_COMMA case_value_range {count++;} )*
              {gParent.action.case_value_range_list(count);}
    ;

// R815
// ERR_CHK 815 expr either scalar_int_initialization_expr
// scalar_char_initialization_expr scalar_logical_initialization_expr
case_value
    :    expr
            { gParent.action.case_value(); }
    ;

// R816
associate_construct
    :    associate_stmt
        block
        end_associate_stmt
            { gParent.action.associate_construct(); }
    ;

// R817
associate_stmt
@init{Token lbl = null; Token id=null;}
    :   (label {lbl=$label.tk;})? ( T_IDENT T_COLON {id=$T_IDENT;})?
            T_ASSOCIATE T_LPAREN association_list T_RPAREN end_of_stmt
            {gParent.action.associate_stmt(lbl, id, $T_ASSOCIATE, $end_of_stmt.tk);}
    ;

association_list
@init{ int count=0;}
    :          {gParent.action.association_list__begin();}
        association {count++;} ( T_COMMA association {count++;} )*
              {gParent.action.association_list(count);}
    ;

// R818
// T_IDENT inlined for associate_name
association
    :    T_IDENT T_EQ_GT selector
            { gParent.action.association($T_IDENT); }
    ;

// R819
// expr can be designator (via primary) so variable deleted
selector
    :    expr
            { gParent.action.selector(); }
    ;

// R820
end_associate_stmt
@init{Token lbl = null; Token id=null;}
    :   (label {lbl=$label.tk;})? T_END T_ASSOCIATE
            (T_IDENT {id=$T_IDENT;})? end_of_stmt
            {gParent.action.end_associate_stmt(lbl, $T_END, $T_ASSOCIATE, id,
                $end_of_stmt.tk);}
    |   (label {lbl=$label.tk;})? T_ENDASSOCIATE
            (T_IDENT {id=$T_IDENT;})? end_of_stmt
            {gParent.action.end_associate_stmt(lbl, $T_ENDASSOCIATE, null, id,
                                       $end_of_stmt.tk);}
    ;

// R821
select_type_construct
    :   select_type_stmt ( type_guard_stmt block )* end_select_type_stmt
            { gParent.action.select_type_construct(); }
    ;

// R822
// T_IDENT inlined for select_construct_name and associate_name
select_type_stmt
@init{Token lbl = null; Token selectConstructName=null;
        Token associateName=null;}
    : (label {lbl=$label.tk;})?
        ( idTmp=T_IDENT T_COLON {selectConstructName=idTmp;})? select_type
        T_LPAREN ( idTmpx=T_IDENT T_EQ_GT {associateName=idTmpx;} )?
        selector T_RPAREN end_of_stmt
            {gParent.action.select_type_stmt(lbl, selectConstructName, associateName,
                                     $end_of_stmt.tk);}
    ;

select_type
    : T_SELECT T_TYPE { gParent.action.select_type($T_SELECT, $T_TYPE); }
    | T_SELECTTYPE { gParent.action.select_type($T_SELECTTYPE, null); }
    ;

// R823
// T_IDENT inlined for select_construct_name
// TODO - FIXME - have to remove T_TYPE_IS and T_CLASS_IS because the
// lexer never matches the sequences.  lexer now matches a T_IDENT for
// the 'IS'.  this rule should be fixed (see test_select_stmts.f03)
// TODO - The temporary token seems convoluted, but I couldn't figure out
// how to prevent ambiguous use of T_IDENT otherwise. -BMR
type_guard_stmt
@init{Token lbl = null; Token selectConstructName=null;}
    :    (label {lbl=$label.tk;})? T_TYPE id1=T_IDENT
            T_LPAREN type_spec T_RPAREN
            ( idTmp=T_IDENT {selectConstructName=idTmp;})? end_of_stmt
            {gParent.action.type_guard_stmt(lbl, $T_TYPE, id1, selectConstructName,
                                    $end_of_stmt.tk);}
    |    (label {lbl=$label.tk;})? T_CLASS id1=T_IDENT
            T_LPAREN type_spec T_RPAREN
            ( idTmp=T_IDENT {selectConstructName=idTmp;})? end_of_stmt
            {gParent.action.type_guard_stmt(lbl, $T_CLASS, id1, selectConstructName,
                                    $end_of_stmt.tk);}
    |    (label {lbl=$label.tk;})? T_CLASS    T_DEFAULT
        ( idTmp=T_IDENT {selectConstructName=idTmp;})? end_of_stmt
            {gParent.action.type_guard_stmt(lbl, $T_CLASS, $T_DEFAULT,
                                    selectConstructName, $end_of_stmt.tk);}
    ;

// R824
// T_IDENT inlined for select_construct_name
end_select_type_stmt
@init{Token lbl = null; Token id = null;}
    :    (label {lbl=$label.tk;})? T_END T_SELECT
            ( T_IDENT {id=$T_IDENT;})? end_of_stmt
            {gParent.action.end_select_type_stmt(lbl, $T_END, $T_SELECT, id,
                $end_of_stmt.tk);}
    |    (label {lbl=$label.tk;})? T_ENDSELECT
            ( T_IDENT {id=$T_IDENT;})? end_of_stmt
            {gParent.action.end_select_type_stmt(lbl, $T_ENDSELECT, null, id,
                $end_of_stmt.tk);}
    ;

// R825
// deleted second alternative, nonblock_do_construct, to reduce backtracking, see comments for R835 on how
// termination of nested loops must be handled.
do_construct
    :    block_do_construct
            { gParent.action.do_construct(); }
    ;

// R826
// do_block replaced by block
block_do_construct
    :    do_stmt
        block
        end_do
            { gParent.action.block_do_construct(); }
    ;

// R827
// label_do_stmt and nonlabel_do_stmt inlined
do_stmt
@init{Token lbl = null;
        Token id=null;
        Token digitString=null;
        boolean hasLoopControl=false;}
    :    (label {lbl=$label.tk;})? ( T_IDENT T_COLON {id=$T_IDENT;})? T_DO
            ( T_DIGIT_STRING {digitString=$T_DIGIT_STRING;})?
            ( loop_control {hasLoopControl=true;})? end_of_stmt
                {gParent.action.do_stmt(lbl, id, $T_DO, digitString, $end_of_stmt.tk,
                                hasLoopControl);}
    ;

// R828
// T_IDENT inlined for do_construct_name
// T_DIGIT_STRING inlined for label
label_do_stmt
@init{Token lbl = null; Token id=null; boolean hasLoopControl=false;}
    :    (label {lbl=$label.tk;})? ( T_IDENT T_COLON {id=$T_IDENT;} )?
            T_DO T_DIGIT_STRING ( loop_control {hasLoopControl=true;})?
            end_of_stmt
            {gParent.action.label_do_stmt(lbl, id, $T_DO, $T_DIGIT_STRING,
                                  $end_of_stmt.tk, hasLoopControl);}
    ;

// R829 inlined in R827
// T_IDENT inlined for do_construct_name

// R830
// ERR_CHK 830a scalar_int_expr replaced by expr
// ERR_CHK 830b scalar_logical_expr replaced by expr
loop_control
@init {
    boolean hasOptExpr = false;
}
    : ( T_COMMA )? T_WHILE T_LPAREN expr T_RPAREN
            { gParent.action.loop_control($T_WHILE, hasOptExpr); }
    | ( T_COMMA )? do_variable T_EQUALS expr T_COMMA expr
        ( T_COMMA expr { hasOptExpr = true; })?
            { gParent.action.loop_control(null, hasOptExpr); }
    ;

// R831
do_variable
    :    scalar_int_variable
            { gParent.action.do_variable(); }
    ;

// R832 do_block was block inlined in R826

// R833
// TODO continue-stmt is ambiguous with same in action statement, check
// there for label and if
// label matches do-stmt label, then match end-do
// do_term_action_stmt added to allow block_do_construct to cover
// nonblock_do_construct as well
end_do
@after {
    gParent.action.end_do();
}
    :    end_do_stmt
    |    do_term_action_stmt
    ;

// R834
// T_IDENT inlined for do_construct_name
end_do_stmt
@init{Token lbl = null; Token id=null;}
    : (label {lbl=$label.tk;})? T_END T_DO ( T_IDENT {id=$T_IDENT;})?
            end_of_stmt
            {gParent.action.end_do_stmt(lbl, $T_END, $T_DO, id, $end_of_stmt.tk);}
    | (label {lbl=$label.tk;})? T_ENDDO    ( T_IDENT {id=$T_IDENT;})?
            end_of_stmt
            {gParent.action.end_do_stmt(lbl, $T_ENDDO, null, id, $end_of_stmt.tk);}
    ;

// R835 nonblock_do_construct deleted as it was combined with
// block_do_construct to reduce backtracking
// Second alternative, outer_shared_do_construct (nested loops sharing a
// termination label) is ambiguous
// with do_construct in do_body, so deleted.  Loop termination will have to
// be coordinated with
// the scanner to unwind nested loops sharing a common termination statement
// (see do_term_action_stmt).

// R836 action_term_do_construct deleted because nonblock_do_construct
// combined with block_do_construct to reduce backtracking

// R837 do_body deleted because nonblock_do_construct combined with
// block_do_construct to reduce backtracking

// R838
// C826 (R842) A do-term-shared-stmt shall not be a goto-stmt, a return-stmt,
// a stop-stmt, an exit-stmt, a cyle-stmt, an end-function-stmt, an
// end-subroutine-stmt, an end-program-stmt, or an arithmetic-if-stmt.
// TODO need interaction with scanner to have this extra terminal emitted
// when do label matched
// TODO need interaction with scanner to terminate shared terminal action
// statements (see R835).
do_term_action_stmt
@init{ Token id=null; Token endToken = null; Token doToken = null;}
    // try requiring an action_stmt and then we can simply insert the new
    // T_LABEL_DO_TERMINAL during the Sale's prepass.  T_EOS is in action_stmt.
    // added the T_END T_DO and T_ENDDO options to this rule because of the
    // token T_LABEL_DO_TERMINAL that is inserted if they end a labeled DO.
    :   label T_LABEL_DO_TERMINAL
        (action_stmt | ( (T_END T_DO {endToken=$T_END; doToken=$T_DO;}
                          | T_ENDDO {endToken=$T_ENDDO; doToken=null;})
                (T_IDENT {id=$T_IDENT;})?) end_of_stmt)
    // BMR- Has to massage the rule a little bit to convince Antlr that thre aren't potentially two identifiers here. Original is below.
       // (action_stmt | ( (T_END T_DO (T_IDENT {id=$T_IDENT;})?) | (T_ENDDO) (T_IDENT {id=$T_IDENT;})? ) T_EOS)
            {gParent.action.do_term_action_stmt($label.tk, endToken, doToken, id,
                                        $end_of_stmt.tk);}
//     :    T_LABEL_DO_TERMINAL action_stmt
//     :    T_LABEL_DO_TERMINAL action_or_cont_stmt
    ;

// R839 outer_shared_do_construct removed because it caused ambiguity in
// R835 (see comment in R835)

// R840 shared_term_do_construct deleted (see comments for R839 and R835)

// R841 inner_shared_do_construct deleted (see comments for R839 and R835)

// R842 do_term_shared_stmt deleted (see comments for R839 and R835)

// R843
// T_IDENT inlined for do_construct_name
cycle_stmt
@init{Token lbl = null; Token id = null;}
    :    (label {lbl=$label.tk;})? T_CYCLE (T_IDENT {id=$T_IDENT;})? end_of_stmt
            { gParent.action.cycle_stmt(lbl, $T_CYCLE, id, $end_of_stmt.tk); }
    ;

// R844
// T_IDENT inlined for do_construct_name
exit_stmt
@init{Token lbl = null; Token id = null;}
    :    (label {lbl=$label.tk;})? T_EXIT (T_IDENT {id=$T_IDENT;})? end_of_stmt
            { gParent.action.exit_stmt(lbl, $T_EXIT, id, $end_of_stmt.tk); }
    ;

// R845
goto_stmt
@init {Token goKeyword=null; Token toKeyword=null;}
    :    (T_GO T_TO { goKeyword=$T_GO; toKeyword=$T_TO;}
         | T_GOTO { goKeyword=$T_GOTO; toKeyword=null;})
            label end_of_stmt
            { gParent.action.goto_stmt(goKeyword, toKeyword, $label.tk,
                $end_of_stmt.tk); }
    ;

// R846
// ERR_CHK 846 scalar_int_expr replaced by expr
computed_goto_stmt
@init{Token lbl = null; Token goKeyword=null; Token toKeyword=null;}
    :    (label {lbl=$label.tk;})?
        (T_GO T_TO {goKeyword=$T_GO; toKeyword=$T_TO;}
         | T_GOTO {goKeyword=$T_GOTO; toKeyword=null;})
            T_LPAREN label_list T_RPAREN ( T_COMMA )? expr end_of_stmt
            { gParent.action.computed_goto_stmt(lbl, goKeyword, toKeyword,
                $end_of_stmt.tk); }
    ;

// The ASSIGN statement is a deleted feature.
assign_stmt
    :   (lbl1=label)? T_ASSIGN lbl2=label T_TO name end_of_stmt
            { gParent.action.assign_stmt(lbl1.tk, $T_ASSIGN, lbl2.tk, $T_TO, $name.tk,
                                 $end_of_stmt.tk); }
    ;

// The assigned GOTO statement is a deleted feature.
assigned_goto_stmt
@init{Token goKeyword=null; Token toKeyword=null;}
    :   (label)? ( T_GOTO {goKeyword=$T_GOTO; toKeyword=null;}
                   | T_GO T_TO {goKeyword=$T_GO; toKeyword=$T_TO;} )
            name (T_COMMA stmt_label_list)? end_of_stmt
            { gParent.action.assigned_goto_stmt($label.tk, goKeyword, toKeyword,
                                        $name.tk, $end_of_stmt.tk); }
    ;

// Used with assigned_goto_stmt (deleted feature)
stmt_label_list
    :   T_LPAREN label ( T_COMMA label )* T_RPAREN
            { gParent.action.stmt_label_list(); }
    ;

// The PAUSE statement is a deleted feature.
pause_stmt
@init{Token tmpToken=null;}
    :   (lbl1=label)? T_PAUSE (lbl2=label {tmpToken=lbl2.tk;}
                 | char_literal_constant {tmpToken=null;})? end_of_stmt
            { gParent.action.pause_stmt(lbl1.tk, $T_PAUSE, tmpToken,
                                $end_of_stmt.tk); }
    ;

// R847
// ERR_CHK 847 scalar_numeric_expr replaced by expr
arithmetic_if_stmt
    :    (lbl=label)? T_ARITHMETIC_IF_STMT T_IF
        T_LPAREN expr T_RPAREN label1=label
        T_COMMA label2=label
        T_COMMA label3=label end_of_stmt
            { gParent.action.arithmetic_if_stmt(lbl.tk, $T_IF, label1.tk, label2.tk, label3.tk,
                                        $end_of_stmt.tk); }
    ;

// R848 continue_stmt
continue_stmt
@init {Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_CONTINUE end_of_stmt
            { gParent.action.continue_stmt(lbl, $T_CONTINUE, $end_of_stmt.tk); }
    ;

// R849
stop_stmt
@init{Token lbl = null; boolean hasStopCode = false;}
    :    (label {lbl=$label.tk;})? T_STOP (stop_code {hasStopCode=true;})?
            end_of_stmt
            { gParent.action.stop_stmt(lbl, $T_STOP, $end_of_stmt.tk, hasStopCode); }
    ;

// R850
// ERR_CHK 850 T_DIGIT_STRING must be 5 digits or less
stop_code
    : scalar_char_constant
        { gParent.action.stop_code(null); }
//     | Digit ( Digit ( Digit ( Digit ( Digit )? )? )? )?
    | T_DIGIT_STRING
        { gParent.action.stop_code($T_DIGIT_STRING); }
    ;

scalar_char_constant
    :    char_constant
                { gParent.action.scalar_char_constant(); }
    ;

/*
Section 9:
 */

// R901
// file_unit_number replaced by expr
// internal_file_variable isa expr so internal_file_variable deleted
io_unit
@after {
    gParent.action.io_unit();
}
    :    expr
    |    T_ASTERISK
    ;

// R902
// ERR_CHK 902 scalar_int_expr replaced by expr
file_unit_number
@after {
    gParent.action.file_unit_number();
}
    :    expr
    ;

// R903 internal_file_variable was char_variable inlined (and then deleted)
// in R901

// R904
open_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_OPEN T_LPAREN connect_spec_list
            T_RPAREN end_of_stmt
            {gParent.action.open_stmt(lbl, $T_OPEN, $end_of_stmt.tk);}
    ;

// R905
// ERR_CHK 905 check expr type with identifier
connect_spec
    : expr
            { gParent.action.connect_spec(null); }
    | T_IDENT
        /* {'UNIT','ACCESS','ACTION','ASYNCHRONOUS','BLANK','DECIMAL', */
        /* 'DELIM','ENCODING'} are expr */
        /* {'ERR'} is T_DIGIT_STRING */
        /* {'FILE','FORM'} are expr */
        /* {'IOMSG','IOSTAT'} are variables */
        /* {'PAD','POSITION','RECL','ROUND','SIGN','STATUS'} are expr */
      T_EQUALS expr
            { gParent.action.connect_spec($T_IDENT); }
    ;

connect_spec_list
@init{ int count=0;}
    :          {gParent.action.connect_spec_list__begin();}
        connect_spec {count++;} ( T_COMMA connect_spec {count++;} )*
              {gParent.action.connect_spec_list(count);}
    ;

// inlined scalar_default_char_expr

// R906 inlined file_name_expr with expr was scalar_default_char_expr

// R907 iomsg_variable inlined as scalar_default_char_variable in
// R905,R909,R913,R922,R926,R928

// R908
close_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_CLOSE T_LPAREN close_spec_list
            T_RPAREN end_of_stmt
            {gParent.action.close_stmt(lbl, $T_CLOSE, $end_of_stmt.tk);}
    ;

// R909
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced
// by expr
close_spec
    :    expr
            { gParent.action.close_spec(null); }
    |    T_IDENT /* {'UNIT','IOSTAT','IOMSG','ERR','STATUS'} */ T_EQUALS expr
            { gParent.action.close_spec($T_IDENT); }
    ;

close_spec_list
@init{ int count=0;}
    :          {gParent.action.close_spec_list__begin();}
        close_spec {count++;} ( T_COMMA close_spec {count++;} )*
              {gParent.action.close_spec_list(count);}
    ;

// R910
read_stmt
options {k=3;}
@init{Token lbl = null; boolean hasInputItemList=false;}
    :    ((label)? T_READ T_LPAREN) =>
            (label {lbl=$label.tk;})? T_READ T_LPAREN io_control_spec_list
            T_RPAREN ( input_item_list {hasInputItemList=true;})? end_of_stmt
            {gParent.action.read_stmt(lbl, $T_READ, $end_of_stmt.tk,
                hasInputItemList);}
    |    ((label)? T_READ) =>
            (label {lbl=$label.tk;})? T_READ format
            ( T_COMMA input_item_list {hasInputItemList=true;})? end_of_stmt
            {gParent.action.read_stmt(lbl, $T_READ, $end_of_stmt.tk,
                hasInputItemList);}
    ;

// R911
write_stmt
@init{Token lbl = null; boolean hasOutputItemList=false;}
    :    (label {lbl=$label.tk;})? T_WRITE T_LPAREN io_control_spec_list
            T_RPAREN ( output_item_list {hasOutputItemList=true;})? end_of_stmt
            { gParent.action.write_stmt(lbl, $T_WRITE, $end_of_stmt.tk,
                hasOutputItemList); }
    ;

// R912
print_stmt
@init{Token lbl = null; boolean hasOutputItemList=false;}
    :    (label {lbl=$label.tk;})? T_PRINT format
            ( T_COMMA output_item_list {hasOutputItemList=true;})? end_of_stmt
            { gParent.action.print_stmt(lbl, $T_PRINT, $end_of_stmt.tk,
                hasOutputItemList); }
    ;

// R913
// ERR_CHK 913 check expr type with identifier
// io_unit and format are both (expr|'*') so combined
io_control_spec
        :    expr
                // hasExpression=true
                { gParent.action.io_control_spec(true, null, false); }
        |    T_ASTERISK
                // hasAsterisk=true
                { gParent.action.io_control_spec(false, null, true); }
        |    T_IDENT /* {'UNIT','FMT'} */ T_EQUALS T_ASTERISK
                // hasAsterisk=true
                { gParent.action.io_control_spec(false, $T_IDENT, true); }
        |    T_IDENT
            /* {'UNIT','FMT'} are expr 'NML' is T_IDENT} */
            /* {'ADVANCE','ASYNCHRONOUS','BLANK','DECIMAL','DELIM'} are expr */
            /* {'END','EOR','ERR'} are labels */
            /* {'ID','IOMSG',IOSTAT','SIZE'} are variables */
            /* {'PAD','POS','REC','ROUND','SIGN'} are expr */
        T_EQUALS expr
                // hasExpression=true
                { gParent.action.io_control_spec(true, $T_IDENT, false); }
    ;


io_control_spec_list
@init{ int count=0;}
    :          {gParent.action.io_control_spec_list__begin();}
        io_control_spec {count++;} ( T_COMMA io_control_spec {count++;} )*
              {gParent.action.io_control_spec_list(count);}
    ;

// R914
// ERR_CHK 914 default_char_expr replaced by expr
// label replaced by T_DIGIT_STRING is expr so deleted
format
@after {
    gParent.action.format();
}
    :    expr
    |    T_ASTERISK
    ;

// R915
input_item
@after {
    gParent.action.input_item();
}
    :    variable
    |    io_implied_do
    ;

input_item_list
@init{ int count=0;}
    :          {gParent.action.input_item_list__begin();}
        input_item {count++;} ( T_COMMA input_item {count++;} )*
              {gParent.action.input_item_list(count);}
    ;

// R916
output_item
options {backtrack=true;}
@after {
    gParent.action.output_item();
}
    :    expr
    |    io_implied_do
    ;


output_item_list
@init{ int count=0;}
    :          {gParent.action.output_item_list__begin();}
        output_item {count++;} ( T_COMMA output_item {count++;} )*
              {gParent.action.output_item_list(count);}
    ;

// R917
io_implied_do
    :    T_LPAREN io_implied_do_object io_implied_do_suffix T_RPAREN
            { gParent.action.io_implied_do(); }
    ;

// R918
// expr in output_item can be variable in input_item so input_item deleted
io_implied_do_object
    :    output_item
            { gParent.action.io_implied_do_object(); }
    ;

io_implied_do_suffix
options {backtrack=true;}
    :    T_COMMA io_implied_do_object io_implied_do_suffix
    |    T_COMMA io_implied_do_control
    ;

// R919
// ERR_CHK 919 scalar_int_expr replaced by expr
io_implied_do_control
    : do_variable T_EQUALS expr T_COMMA expr ( T_COMMA expr )?
            { gParent.action.io_implied_do_control(); }
    ;

// R920
// TODO: remove this?  it is never called.
dtv_type_spec
    :    T_TYPE
        T_LPAREN
        derived_type_spec
        T_RPAREN
            { gParent.action.dtv_type_spec($T_TYPE); }
    |    T_CLASS
        T_LPAREN
        derived_type_spec
        T_RPAREN
            { gParent.action.dtv_type_spec($T_CLASS); }
    ;

// R921
wait_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_WAIT T_LPAREN wait_spec_list T_RPAREN
            end_of_stmt
            {gParent.action.wait_stmt(lbl, $T_WAIT, $end_of_stmt.tk);}
    ;

// R922
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced
// by expr
wait_spec
    :    expr
            { gParent.action.wait_spec(null); }
    |    T_IDENT /* {'UNIT','END','EOR','ERR','ID','IOMSG','IOSTAT'} */
            T_EQUALS expr
            { gParent.action.wait_spec($T_IDENT); }
    ;


wait_spec_list
@init{ int count=0;}
    :          {gParent.action.wait_spec_list__begin();}
        wait_spec {count++;} ( T_COMMA wait_spec {count++;} )*
              {gParent.action.wait_spec_list(count);}
    ;

// R923
backspace_stmt
options {k=3;}
@init{Token lbl = null;}
    :    ((label)? T_BACKSPACE T_LPAREN) =>
            (label {lbl=$label.tk;})? T_BACKSPACE T_LPAREN position_spec_list
            T_RPAREN end_of_stmt
            {gParent.action.backspace_stmt(lbl, $T_BACKSPACE, $end_of_stmt.tk, true);}
    |    ((label)? T_BACKSPACE) =>
            (label {lbl=$label.tk;})? T_BACKSPACE file_unit_number end_of_stmt
            {gParent.action.backspace_stmt(lbl, $T_BACKSPACE, $end_of_stmt.tk, false);}
    ;

// R924
endfile_stmt
options {k=3;}
@init{Token lbl = null;}
    :    ((label)? T_END T_FILE T_LPAREN) =>
            (label {lbl=$label.tk;})? T_END T_FILE T_LPAREN position_spec_list
            T_RPAREN end_of_stmt
            {gParent.action.endfile_stmt(lbl, $T_END, $T_FILE, $end_of_stmt.tk, true);}
    |    ((label)? T_ENDFILE T_LPAREN) =>
            (label {lbl=$label.tk;})? T_ENDFILE T_LPAREN position_spec_list
            T_RPAREN end_of_stmt
            {gParent.action.endfile_stmt(lbl, $T_ENDFILE, null, $end_of_stmt.tk,
                true);}
    |    ((label)? T_END T_FILE) =>
            (label {lbl=$label.tk;})? T_END T_FILE file_unit_number end_of_stmt
            {gParent.action.endfile_stmt(lbl, $T_END, $T_FILE, $end_of_stmt.tk,
                false);}
    |    ((label)? T_ENDFILE) =>
            (label {lbl=$label.tk;})? T_ENDFILE file_unit_number end_of_stmt
            {gParent.action.endfile_stmt(lbl, $T_ENDFILE, null, $end_of_stmt.tk,
                false);}
    ;

// R925
rewind_stmt
options {k=3;}
@init{Token lbl = null;}
    :    ((label)? T_REWIND T_LPAREN) =>
            (label {lbl=$label.tk;})? T_REWIND T_LPAREN position_spec_list
            T_RPAREN end_of_stmt
            {gParent.action.rewind_stmt(lbl, $T_REWIND, $end_of_stmt.tk, true);}
    |    ((label)? T_REWIND) =>
            (label {lbl=$label.tk;})? T_REWIND file_unit_number end_of_stmt
            {gParent.action.rewind_stmt(lbl, $T_REWIND, $end_of_stmt.tk, false);}
    ;

// R926
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced
// by expr
position_spec
    :    expr
            { gParent.action.position_spec(null); }
    |    T_IDENT /* {'UNIT','IOSTAT','IOMSG','ERR'} */ T_EQUALS expr
            { gParent.action.position_spec($T_IDENT); }
    ;

position_spec_list
@init{ int count=0;}
    :          {gParent.action.position_spec_list__begin();}
        position_spec {count++;} ( T_COMMA position_spec {count++;} )*
              {gParent.action.position_spec_list(count);}
    ;

// R927
flush_stmt
options {k=3;}
@init{Token lbl = null;}
    :    ((label)? T_FLUSH T_LPAREN) =>
            (label {lbl=$label.tk;})? T_FLUSH T_LPAREN flush_spec_list
            T_RPAREN end_of_stmt
            {gParent.action.flush_stmt(lbl, $T_FLUSH, $end_of_stmt.tk, true);}
    |    ((label)? T_FLUSH) =>
            (label {lbl=$label.tk;})? T_FLUSH file_unit_number end_of_stmt
            {gParent.action.flush_stmt(lbl, $T_FLUSH, $end_of_stmt.tk, false);}
    ;

// R928
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced
// by expr
flush_spec
    :    expr
            { gParent.action.flush_spec(null); }
    |    T_IDENT /* {'UNIT','IOSTAT','IOMSG','ERR'} */ T_EQUALS expr
            { gParent.action.flush_spec($T_IDENT); }
    ;

flush_spec_list
@init{ int count=0;}
    :          {gParent.action.flush_spec_list__begin();}
        flush_spec {count++;} ( T_COMMA flush_spec {count++;} )*
              {gParent.action.flush_spec_list(count);}
    ;

// R929
inquire_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_INQUIRE T_LPAREN inquire_spec_list
            T_RPAREN end_of_stmt
            {gParent.action.inquire_stmt(lbl, $T_INQUIRE, null, $end_of_stmt.tk,
                false);}
    |    (label {lbl=$label.tk;})? T_INQUIRE_STMT_2
            T_INQUIRE T_LPAREN T_IDENT /* 'IOLENGTH' */ T_EQUALS
            scalar_int_variable T_RPAREN output_item_list end_of_stmt
                {gParent.action.inquire_stmt(lbl, $T_INQUIRE, $T_IDENT,
                    $end_of_stmt.tk, true);}
    ;


// R930
// ERR_CHK 930 file_name_expr replaced by expr
// file_unit_number replaced by expr
// scalar_default_char_variable replaced by designator
inquire_spec
    :    expr
            { gParent.action.inquire_spec(null); }
    |    T_IDENT
        /* {'UNIT','FILE'} '=' expr portion, '=' designator portion below
           {'ACCESS','ACTION','ASYNCHRONOUS','BLANK','DECIMAL',DELIM','DIRECT'}
           {'ENCODING','ERR','EXIST','FORM','FORMATTED','ID','IOMSG','IOSTAT'}
           {'NAME','NAMED','NEXTREC','NUMBER',OPENED','PAD','PENDING','POS'}
           {'POSITION','READ','READWRITE','RECL','ROUND','SEQUENTIAL','SIGN'}
           {'SIZE','STREAM','UNFORMATTED','WRITE'}  */
        T_EQUALS expr
             { gParent.action.inquire_spec($T_IDENT); }
    ;

inquire_spec_list
@init{ int count=0;}
    :          {gParent.action.inquire_spec_list__begin();}
        inquire_spec {count++;} ( T_COMMA inquire_spec {count++;} )*
              {gParent.action.inquire_spec_list(count);}
    ;

/*
Section 10:
 */

// R1001
// TODO: error checking: label is required.  accept as optional so we can
// report the error to the user.
format_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_FORMAT format_specification end_of_stmt
            {gParent.action.format_stmt(lbl, $T_FORMAT, $end_of_stmt.tk);}
    ;

// R1002
format_specification
@init{ boolean hasFormatItemList=false; }
    :    T_LPAREN ( format_item_list {hasFormatItemList=true;})? T_RPAREN
            {gParent.action.format_specification(hasFormatItemList);}
    ;

// R1003
// r replaced by int_literal_constant replaced by char_literal_constant
// replaced by T_CHAR_CONSTANT
// char_string_edit_desc replaced by T_CHAR_CONSTANT
format_item
@init{ Token descOrDigit=null; boolean hasFormatItemList=false; }
    :   T_DATA_EDIT_DESC
            {gParent.action.format_item($T_DATA_EDIT_DESC,hasFormatItemList);}
    |   T_CONTROL_EDIT_DESC
            {gParent.action.format_item($T_CONTROL_EDIT_DESC,hasFormatItemList);}
    |   T_CHAR_STRING_EDIT_DESC
            {gParent.action.format_item($T_CHAR_STRING_EDIT_DESC,hasFormatItemList);}
    |   (T_DIGIT_STRING {descOrDigit=$T_DIGIT_STRING;} )? T_LPAREN
            format_item_list T_RPAREN
            {gParent.action.format_item(descOrDigit,hasFormatItemList);}
    ;

// the comma is not always required.  see J3/04-007, pg. 221, lines
// 17-22
// ERR_CHK
format_item_list
@init{ int count=1;}
    :          {gParent.action.format_item_list__begin();}
        format_item ( (T_COMMA)? format_item {count++;} )*
              {gParent.action.format_item_list(count);}
    ;


// the following rules, from here to the v_list, are the originals.  modifying
// to try and simplify and make match up with the standard.
// original rules. 02.01.07
// // R1003
// // r replaced by int_literal_constant replaced by char_literal_constant replaced by T_CHAR_CONSTANT
// // char_string_edit_desc replaced by T_CHAR_CONSTANT
// format_item
//     :    T_DIGIT_STRING data_edit_desc
//     |    data_plus_control_edit_desc
//     |    T_CHAR_CONSTANT
//     |    (T_DIGIT_STRING)? T_LPAREN format_item_list T_RPAREN
//     ;

// format_item_list
//     :    format_item ( T_COMMA format_item )*
//     ;

// // R1004 r inlined in R1003 and R1011 as int_literal_constant (then as DIGIT_STRING)
// // C1004 (R1004) r shall not have a kind parameter associated with it

// // R1005
// // w,m,d,e replaced by int_literal_constant replaced by T_DIGIT_STRING
// // char_literal_constant replaced by T_CHAR_CONSTANT
// // ERR_CHK 1005 matching T_ID_OR_OTHER with alternatives will have to be done here
// data_edit_desc
//     : T_ID_OR_OTHER /* {'I','B','O','Z','F','E','EN','ES','G','L','A','D'} */
//       T_DIGIT_STRING ( T_PERIOD T_DIGIT_STRING )?
//       ( T_ID_OR_OTHER /* is 'E' */ T_DIGIT_STRING )?
//     | T_ID_OR_OTHER /* is 'DT' */ T_CHAR_CONSTANT ( T_LPAREN v_list T_RPAREN )?
//     | T_ID_OR_OTHER /* {'A','DT'},{'X','P' from control_edit_desc} */
//     ;

// data_plus_control_edit_desc
//     :    T_ID_OR_OTHER /* {'I','B','O','Z','F','E','EN','ES','G','L','A','D'},{T','TL','TR'} */
//             T_DIGIT_STRING ( T_PERIOD T_DIGIT_STRING )?
//             ( T_ID_OR_OTHER /* is 'E' */ T_DIGIT_STRING )?
//     |    T_ID_OR_OTHER /* is 'DT' */ T_CHAR_CONSTANT ( T_LPAREN v_list T_RPAREN )?
//     |    T_ID_OR_OTHER /* {'A','DT'},{'BN','BZ','RU','RD','RZ','RN','RC','RP','DC','DP'} */
// // following only from control_edit_desc
//     |    ( T_DIGIT_STRING )? T_SLASH
//     |    T_COLON
//     |    (T_PLUS|T_MINUS) T_DIGIT_STRING T_ID_OR_OTHER /* is 'P' */
//     ;

// R1006 w inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1007 m inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1008 d inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1009 e inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1010 v inlined as signed_int_literal_constant in v_list replaced by (T_PLUS or T_MINUS) T_DIGIT_STRING

v_list
@init{int count=0;}
    :          {gParent.action.v_list__begin();}
        (pm=T_PLUS|T_MINUS)? ds=T_DIGIT_STRING
            {
                count++;
                gParent.action.v_list_part(pm, ds);
            }
        ( T_COMMA (pm=T_PLUS|T_MINUS)? ds=T_DIGIT_STRING
            {
                count++;
                gParent.action.v_list_part(pm, ds);
            }
        )*
              {gParent.action.v_list(count);}
    ;

// R1011 control_edit_desc inlined/combined in R1005 and data_plus_control_edit_desc
// r replaced by int_literal_constant replaced by T_DIGIT_STRING
// k replaced by signed_int_literal_constant replaced by (T_PLUS|T_MINUS)? T_DIGIT_STRING
// position_edit_desc inlined
// sign_edit_desc replaced by T_ID_OR_OTHER was {'SS','SP','S'}
// blank_interp_edit_desc replaced by T_ID_OR_OTHER was {'BN','BZ'}
// round_edit_desc replaced by T_ID_OR_OTHER was {'RU','RD','RZ','RN','RC','RP'}
// decimal_edit_desc replaced by T_ID_OR_OTHER was {'DC','DP'}
// leading T_ID_OR_OTHER alternates combined with data_edit_desc in data_plus_control_edit_desc

// R1012 k inlined in R1011 as signed_int_literal_constant
// C1009 (R1012) k shall not have a kind parameter specified for it

// R1013 position_edit_desc inlined in R1011
// n in R1013 was replaced by int_literal_constant replaced by T_DIGIT_STRING

// R1014 n inlined in R1013 as int_literal_constant (is T_DIGIT_STRING, see C1010)
// C1010 (R1014) n shall not have a kind parameter specified for it

// R1015 sign_edit_desc inlined in R1011 as T_ID_OR_OTHER was {'SS','SP','S'}

// R1016 blank_interp_edit_desc inlined in R1011 as T_ID_OR_OTHER was {'BN','BZ'}

// R1017 round_edit_desc inlined in R1011 as T_ID_OR_OTHER was {'RU','RD','RZ','RN','RC','RP'}

// R1018 decimal_edit_desc inlined in R1011 as T_ID_OR_OTHER was {'DC','DP'}

// R1019 char_string_edit_desc was char_literal_constant inlined in R1003 as T_CHAR_CONSTANT

/*
 * Section 11:
 */


// R1102
// T_IDENT inlined for program_name
program_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})? T_PROGRAM T_IDENT end_of_stmt
        { gParent.action.program_stmt(lbl, $T_PROGRAM, $T_IDENT, $end_of_stmt.tk); }
    ;

// R1103
// T_IDENT inlined for program_name
end_program_stmt
@init{Token lbl = null; Token id = null;}
    :    (label {lbl=$label.tk;})? T_END T_PROGRAM (T_IDENT {id=$T_IDENT;})?
            end_of_stmt
            { gParent.action.end_program_stmt(lbl, $T_END, $T_PROGRAM, id,
                                      $end_of_stmt.tk); }
    |    (label {lbl=$label.tk;})? T_ENDPROGRAM (T_IDENT {id=$T_IDENT;})?
            end_of_stmt
            { gParent.action.end_program_stmt(lbl, $T_ENDPROGRAM, null, id,
                                      $end_of_stmt.tk); }
    |    (label {lbl=$label.tk;})? T_END end_of_stmt
            { gParent.action.end_program_stmt(lbl, $T_END, null, null,
                                      $end_of_stmt.tk); }
    ;


// R1104
// C1104 (R1104) A module specification-part shall not contain a
// stmt-function-stmt, an entry-stmt or a format-stmt
// specification_part made non-optional to remove END ambiguity (as can
// be empty)
module
@after {
    gParent.action.module();
}
    :    module_stmt
        specification_part
        ( module_subprogram_part )?
        end_module_stmt
    ;

// R1105
module_stmt
@init{Token lbl = null; Token id = null;}
     :        {gParent.action.module_stmt__begin();}
         (label {lbl=$label.tk;})? T_MODULE ( T_IDENT {id=$T_IDENT;} )?
            end_of_stmt
             {gParent.action.module_stmt(lbl, $T_MODULE, id, $end_of_stmt.tk);}
    ;


// R1106
end_module_stmt
@init{Token lbl = null; Token id = null;}
    :  (label {lbl=$label.tk;})? T_END T_MODULE (T_IDENT {id=$T_IDENT;})?
            end_of_stmt
            {gParent.action.end_module_stmt(lbl, $T_END, $T_MODULE, id,
                                    $end_of_stmt.tk);}
    |  (label {lbl=$label.tk;})? T_ENDMODULE (T_IDENT {id=$T_IDENT;})?
            end_of_stmt
        {gParent.action.end_module_stmt(lbl, $T_ENDMODULE, null, id,
                                $end_of_stmt.tk);}
    |  (label {lbl=$label.tk;})? T_END end_of_stmt
            {gParent.action.end_module_stmt(lbl, $T_END, null, id, $end_of_stmt.tk);}
    ;

// R1107
// TODO - add count of module_subprograms
module_subprogram_part
    :    contains_stmt
        module_subprogram
        ( module_subprogram )*
            { gParent.action.module_subprogram_part(); }
    ;

// R1108
// modified to factor optional prefix
module_subprogram
@init{boolean hasPrefix = false;}
    :    (prefix {hasPrefix=true;})? function_subprogram
            {gParent.action.module_subprogram(hasPrefix);}
    |    subroutine_subprogram
            {gParent.action.module_subprogram(hasPrefix);}
    ;

// R1109
use_stmt
@init {
    Token lbl=null;
    boolean hasModuleNature=false;
    boolean hasRenameList=false;
}
    :    (label {lbl=$label.tk;})? T_USE
            ( (T_COMMA module_nature {hasModuleNature=true;})?
            T_COLON_COLON )? T_IDENT ( T_COMMA
            rename_list {hasRenameList=true;})? end_of_stmt
            {gParent.action.use_stmt(lbl, $T_USE, $T_IDENT, null, $end_of_stmt.tk,
                             hasModuleNature, hasRenameList, false);}
    |    (label {lbl=$label.tk;})? T_USE
            ( ( T_COMMA module_nature {hasModuleNature=true;})?
            T_COLON_COLON )? T_IDENT T_COMMA T_ONLY T_COLON ( only_list )?
            end_of_stmt
            {gParent.action.use_stmt(lbl, $T_USE, $T_IDENT, $T_ONLY, $end_of_stmt.tk,
                             hasModuleNature,hasRenameList,true);}
    ;

// R1110
module_nature
    :    T_INTRINSIC
            { gParent.action.module_nature($T_INTRINSIC); }
    |    T_NON_INTRINSIC
            { gParent.action.module_nature($T_NON_INTRINSIC); }
    ;

// R1111
// T_DEFINED_OP inlined for local_defined_operator and use_defined_operator
// T_IDENT inlined for local_name and use_name
rename
    :    id1=T_IDENT T_EQ_GT id2=T_IDENT
            { gParent.action.rename(id1, id2, null, null, null, null); }
    |    op1=T_OPERATOR T_LPAREN defOp1=T_DEFINED_OP T_RPAREN T_EQ_GT
        op2=T_OPERATOR T_LPAREN defOp2=T_DEFINED_OP T_RPAREN
            { gParent.action.rename(null, null, op1, defOp1, op2, defOp2); }
    ;

rename_list
@init{ int count=0;}
    :          {gParent.action.rename_list__begin();}
        rename {count++;} ( T_COMMA rename {count++;} )*
              {gParent.action.rename_list(count);}
    ;

// R1112
// T_IDENT inlined for only_use_name
// generic_spec can be T_IDENT so T_IDENT deleted
only
@after {
    gParent.action.only();
}
    :    generic_spec
    |    rename
    ;

only_list
@init{ int count=0;}
    :          {gParent.action.only_list__begin();}
        only {count++;} ( T_COMMA only {count++;} )*
              {gParent.action.only_list(count);}
    ;

// R1113 only_use_name was use_name inlined as T_IDENT

// R1114 inlined local_defined_operator in R1111 as T_DEFINED_OP

// R1115 inlined use_defined_operator in R1111 as T_DEFINED_OP

// R1116
// specification_part made non-optional to remove END ambiguity (as can
// be empty).
block_data
@after {
    gParent.action.block_data();
}
    :    block_data_stmt
        specification_part
        end_block_data_stmt
    ;

// R1117
block_data_stmt
@init
    {
        Token lbl = null; Token id = null;
        gParent.action.block_data_stmt__begin();
    }
    :    (label {lbl=$label.tk;})? T_BLOCK T_DATA (T_IDENT {id=$T_IDENT;})?
            end_of_stmt
            {gParent.action.block_data_stmt(lbl, $T_BLOCK, $T_DATA, id,
                $end_of_stmt.tk);}
    |   (label {lbl=$label.tk;})? T_BLOCKDATA  (T_IDENT {id=$T_IDENT;})?
            end_of_stmt
            {gParent.action.block_data_stmt(lbl, $T_BLOCKDATA, null, id,
                $end_of_stmt.tk);}
    ;

// R1118
end_block_data_stmt
@init{Token lbl = null; Token id = null;}
    :   (label {lbl=$label.tk;})? T_END T_BLOCK T_DATA
            ( T_IDENT {id=$T_IDENT;})? end_of_stmt
            {gParent.action.end_block_data_stmt(lbl, $T_END, $T_BLOCK, $T_DATA, id,
                                        $end_of_stmt.tk);}
    |   (label {lbl=$label.tk;})? T_ENDBLOCK T_DATA
            ( T_IDENT {id=$T_IDENT;})? end_of_stmt
            {gParent.action.end_block_data_stmt(lbl, $T_ENDBLOCK, null, $T_DATA, id,
                                        $end_of_stmt.tk);}
    |   (label {lbl=$label.tk;})? T_END T_BLOCKDATA
            ( T_IDENT {id=$T_IDENT;})? end_of_stmt
            {gParent.action.end_block_data_stmt(lbl, $T_END, $T_BLOCKDATA, null, id,
                                        $end_of_stmt.tk);}
    |   (label {lbl=$label.tk;})? T_ENDBLOCKDATA
            ( T_IDENT {id=$T_IDENT;})? end_of_stmt
            {gParent.action.end_block_data_stmt(lbl, $T_ENDBLOCKDATA, null, null, id,
                                        $end_of_stmt.tk);}
    |    (label {lbl=$label.tk;})? T_END end_of_stmt
            {gParent.action.end_block_data_stmt(lbl, $T_END, null, null, id,
                                        $end_of_stmt.tk);}
    ;

/*
 * Section 12:
 */

// R1201
interface_block
@after {
    gParent.action.interface_block();
}
    :    interface_stmt
        ( interface_specification )*
        end_interface_stmt
    ;

// R1202
interface_specification
@after {
    gParent.action.interface_specification();
}
    :    interface_body
    |    procedure_stmt
    ;

// R1203 Note that the last argument to the action specifies whether this
// is an abstract interface or not.
interface_stmt
@init{Token lbl = null; boolean hasGenericSpec=false;}
    :        {gParent.action.interface_stmt__begin();}
        (label {lbl=$label.tk;})? T_INTERFACE ( generic_spec
            {hasGenericSpec=true;})? end_of_stmt
            {gParent.action.interface_stmt(lbl, null, $T_INTERFACE, $end_of_stmt.tk,
                                   hasGenericSpec);}
    |    (label {lbl=$label.tk;})? T_ABSTRACT T_INTERFACE end_of_stmt
            {gParent.action.interface_stmt(lbl, $T_ABSTRACT, $T_INTERFACE,
                                   $end_of_stmt.tk, hasGenericSpec);}
    ;

// R1204
end_interface_stmt
@init{Token lbl = null; boolean hasGenericSpec=false;}
    : (label {lbl=$label.tk;})? T_END T_INTERFACE ( generic_spec
            {hasGenericSpec=true;})? end_of_stmt
            {gParent.action.end_interface_stmt(lbl, $T_END, $T_INTERFACE,
                $end_of_stmt.tk, hasGenericSpec);}
    | (label {lbl=$label.tk;})? T_ENDINTERFACE    ( generic_spec
            {hasGenericSpec=true;})? end_of_stmt
            {gParent.action.end_interface_stmt(lbl, $T_ENDINTERFACE, null,
                $end_of_stmt.tk, hasGenericSpec);}
    ;

// R1205
// specification_part made non-optional to remove END ambiguity (as can
// be empty)
interface_body
    :    (prefix)? function_stmt specification_part end_function_stmt
            { gParent.action.interface_body(true); /* true for hasPrefix */ }
    |    subroutine_stmt specification_part end_subroutine_stmt
            { gParent.action.interface_body(false); /* false for hasPrefix */ }
    ;

// R1206
// generic_name_list substituted for procedure_name_list
procedure_stmt
@init{Token lbl = null; Token module=null;}
    :    (label {lbl=$label.tk;})? ( T_MODULE {module=$T_MODULE;})?
            T_PROCEDURE generic_name_list end_of_stmt
            {gParent.action.procedure_stmt(lbl, module, $T_PROCEDURE,
                $end_of_stmt.tk);}
    ;

// R1207
// T_IDENT inlined for generic_name
generic_spec
    :    T_IDENT
            {gParent.action.generic_spec(null, $T_IDENT,
                                 IActionEnums.GenericSpec_generic_name);}
    |    T_OPERATOR T_LPAREN defined_operator T_RPAREN
            {gParent.action.generic_spec($T_OPERATOR, null,
                                 IActionEnums.GenericSpec_OPERATOR);}
    |    T_ASSIGNMENT T_LPAREN T_EQUALS T_RPAREN
            {gParent.action.generic_spec($T_ASSIGNMENT, null,
                                 IActionEnums.GenericSpec_ASSIGNMENT);}
    |    dtio_generic_spec
            { gParent.action.generic_spec(null, null,
                IActionEnums.GenericSpec_dtio_generic_spec); }
    ;

// R1208
dtio_generic_spec
    :    T_READ T_LPAREN T_FORMATTED T_RPAREN
        {gParent.action.dtio_generic_spec($T_READ, $T_FORMATTED,
                                  IActionEnums.
                                  DTIOGenericSpec_READ_FORMATTED);}
    |    T_READ T_LPAREN T_UNFORMATTED T_RPAREN
        {gParent.action.dtio_generic_spec($T_READ, $T_UNFORMATTED,
                                  IActionEnums.
                                  DTIOGenericSpec_READ_UNFORMATTED);}
    |    T_WRITE T_LPAREN T_FORMATTED T_RPAREN
        {gParent.action.dtio_generic_spec($T_WRITE, $T_FORMATTED,
                                  IActionEnums.
                                  DTIOGenericSpec_WRITE_FORMATTED);}
    |    T_WRITE T_LPAREN T_UNFORMATTED T_RPAREN
        {gParent.action.dtio_generic_spec($T_WRITE, $T_UNFORMATTED,
                                  IActionEnums.
                                  DTIOGenericSpec_WRITE_UNFORMATTED);}
    ;

// R1209
// generic_name_list substituted for import_name_list
import_stmt
@init{Token lbl = null; boolean hasGenericNameList=false;}
    :    (label {lbl=$label.tk;})? T_IMPORT ( ( T_COLON_COLON )?
            generic_name_list {hasGenericNameList=true;})? end_of_stmt
            {gParent.action.import_stmt(lbl, $T_IMPORT, $end_of_stmt.tk,
                hasGenericNameList);}
    ;

// R1210
// generic_name_list substituted for external_name_list
external_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})? T_EXTERNAL ( T_COLON_COLON )?
            generic_name_list end_of_stmt
            {gParent.action.external_stmt(lbl, $T_EXTERNAL, $end_of_stmt.tk);}
    ;

// R1211
procedure_declaration_stmt
@init{Token lbl = null; boolean hasProcInterface=false; int count=0;}
    : (label {lbl=$label.tk;})? T_PROCEDURE T_LPAREN
        ( proc_interface {hasProcInterface=true;})? T_RPAREN
           ( ( T_COMMA proc_attr_spec {count++;})* T_COLON_COLON )?
        proc_decl_list end_of_stmt
            {gParent.action.procedure_declaration_stmt(lbl, $T_PROCEDURE,
                $end_of_stmt.tk, hasProcInterface, count);}
    ;

// R1212
// T_IDENT inlined for interface_name
proc_interface
    :    T_IDENT                    { gParent.action.proc_interface($T_IDENT); }
    |    declaration_type_spec    { gParent.action.proc_interface(null); }
    ;

// R1213
proc_attr_spec
    :    access_spec
            { gParent.action.proc_attr_spec(null, null, IActionEnums.AttrSpec_none); }
    |    proc_language_binding_spec
            { gParent.action.proc_attr_spec(null, null, IActionEnums.AttrSpec_none); }
    |    T_INTENT T_LPAREN intent_spec T_RPAREN
            { gParent.action.proc_attr_spec($T_INTENT, null,
                IActionEnums.AttrSpec_INTENT); }
    |    T_OPTIONAL
            { gParent.action.proc_attr_spec($T_OPTIONAL, null,
                IActionEnums.AttrSpec_OPTIONAL); }
    |    T_POINTER
            { gParent.action.proc_attr_spec($T_POINTER, null,
                IActionEnums.AttrSpec_POINTER); }
    |    T_SAVE
            { gParent.action.proc_attr_spec($T_SAVE, null,
                IActionEnums.AttrSpec_SAVE); }
// TODO: are T_PASS, T_NOPASS, and T_DEFERRED correct?
// From R453 binding-attr
    |   T_PASS ( T_LPAREN T_IDENT T_RPAREN)?
            { gParent.action.proc_attr_spec($T_PASS, $T_IDENT,
                IActionEnums.AttrSpec_PASS); }
    |   T_NOPASS
            { gParent.action.proc_attr_spec($T_NOPASS, null,
                IActionEnums.AttrSpec_NOPASS); }
    |   T_DEFERRED
            { gParent.action.proc_attr_spec($T_DEFERRED, null,
                IActionEnums.AttrSpec_DEFERRED); }
    ;

// R1214
// T_IDENT inlined for procedure_entity_name
proc_decl
@init{boolean hasNullInit = false;}
    :    T_IDENT ( T_EQ_GT null_init {hasNullInit=true;} )?
            { gParent.action.proc_decl($T_IDENT, hasNullInit); }
    ;

proc_decl_list
@init{ int count=0;}
    :          {gParent.action.proc_decl_list__begin();}
        proc_decl {count++;} ( T_COMMA proc_decl {count++;} )*
              {gParent.action.proc_decl_list(count);}
    ;

// R1215 interface_name was name inlined as T_IDENT

// R1216
// generic_name_list substituted for intrinsic_procedure_name_list
intrinsic_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_INTRINSIC
        ( T_COLON_COLON )?
        generic_name_list end_of_stmt
            {gParent.action.intrinsic_stmt(lbl, $T_INTRINSIC, $end_of_stmt.tk);}
    ;

// R1217 function_reference replaced by designator_or_func_ref to reduce
// backtracking

// R1218
// C1222 (R1218) The procedure-designator shall designate a subroutine.
call_stmt
@init{Token lbl = null; boolean hasActualArgSpecList = false;}
    :    (label {lbl=$label.tk;})? T_CALL procedure_designator
            ( T_LPAREN (actual_arg_spec_list {hasActualArgSpecList=true;})?
            T_RPAREN )? end_of_stmt
             { gParent.action.call_stmt(lbl, $T_CALL, $end_of_stmt.tk,
                hasActualArgSpecList); }
    ;

// R1219
// ERR_CHK 1219 must be (T_IDENT | designator T_PERCENT T_IDENT)
// T_IDENT inlined for procedure_name and binding_name
// proc_component_ref is variable T_PERCENT T_IDENT (variable is designator)
// data_ref subset of designator so data_ref T_PERCENT T_IDENT deleted
// designator (R603), minus the substring part is data_ref, so designator
// replaced by data_ref
//R1219 procedure-designator            is procedure-name
//                                      or proc-component-ref
//                                      or data-ref % binding-name
procedure_designator
    :    data_ref
            { gParent.action.procedure_designator(); }
    ;

// R1220
actual_arg_spec
@init{Token keyword = null;}
    :    (T_IDENT T_EQUALS {keyword=$T_IDENT;})? actual_arg
            { gParent.action.actual_arg_spec(keyword); }
    ;

// TODO - delete greedy?
actual_arg_spec_list
options{greedy=false;}
@init{int count = 0;}
    :        { gParent.action.actual_arg_spec_list__begin(); }
        actual_arg_spec {count++;} ( T_COMMA actual_arg_spec {count++;} )*
            { gParent.action.actual_arg_spec_list(count); }
    ;

// R1221
// ERR_CHK 1221 ensure ( expr | designator ending in T_PERCENT T_IDENT)
// T_IDENT inlined for procedure_name
// expr isa designator (via primary) so variable deleted
// designator isa T_IDENT so T_IDENT deleted
// proc_component_ref is variable T_PERCENT T_IDENT can be designator so
// deleted
actual_arg
@init{boolean hasExpr = false;}
    :    expr
            { hasExpr=true; gParent.action.actual_arg(hasExpr, null); }
    |    T_ASTERISK label
            { gParent.action.actual_arg(hasExpr, $label.tk); }
    ;

// R1222 alt_return_spec inlined as T_ASTERISK label in R1221

// R1223
// 1. left factored optional prefix in function_stmt from function_subprogram
// 2. specification_part made non-optional to remove END ambiguity (as can
// be empty)
function_subprogram
@init {
    boolean hasExePart = false;
    boolean hasIntSubProg = false;
}
    :    function_stmt
        specification_part
        ( execution_part { hasExePart=true; })?
        ( internal_subprogram_part { hasIntSubProg=true; })?
        end_function_stmt
            { gParent.action.function_subprogram(hasExePart, hasIntSubProg); }
    ;

// R1224
// left factored optional prefix from function_stmt
// generic_name_list substituted for dummy_arg_name_list
function_stmt
@init {
    Token lbl = null;
    boolean hasGenericNameList=false;
    boolean hasSuffix=false;
}
    :          {gParent.action.function_stmt__begin();}
        (label {lbl=$label.tk;})? T_FUNCTION T_IDENT
            T_LPAREN ( generic_name_list {hasGenericNameList=true;})? T_RPAREN
            ( suffix {hasSuffix=true;})? end_of_stmt
            {gParent.action.function_stmt(lbl, $T_FUNCTION, $T_IDENT, $end_of_stmt.tk,
                                  hasGenericNameList,hasSuffix);}
    ;

// R1225
proc_language_binding_spec
    :    language_binding_spec
            { gParent.action.proc_language_binding_spec(); }
    ;

// R1226 dummy_arg_name was name inlined as T_IDENT

// R1227
// C1240 (R1227) A prefix shall contain at most one of each prefix-spec
// C1241 (R1227) A prefix shall not specify both ELEMENTAL AND RECURSIVE
prefix
@init{int specCount=1;}
    :    prefix_spec ( prefix_spec{specCount++;}
            (prefix_spec{specCount++;} )? )?
            {gParent.action.prefix(specCount);}
    ;

t_prefix
@init{int specCount=1;}
    :    t_prefix_spec ( t_prefix_spec {specCount++;})?
            {gParent.action.t_prefix(specCount);}
    ;

// R1228
prefix_spec
    :    declaration_type_spec
            {gParent.action.prefix_spec(true);}
    |    t_prefix_spec
            {gParent.action.prefix_spec(false);}
    ;

t_prefix_spec
    :    T_RECURSIVE    {gParent.action.t_prefix_spec($T_RECURSIVE);}
    |    T_PURE        {gParent.action.t_prefix_spec($T_PURE);}
    |    T_ELEMENTAL    {gParent.action.t_prefix_spec($T_ELEMENTAL);}
    ;

// R1229
suffix
@init {
    Token result = null;
    boolean hasProcLangBindSpec = false;
}
    :    proc_language_binding_spec ( T_RESULT T_LPAREN result_name
            T_RPAREN { result=$T_RESULT; })?
            { gParent.action.suffix(result, true); }
    |    T_RESULT T_LPAREN result_name T_RPAREN
            ( proc_language_binding_spec { hasProcLangBindSpec = true; })?
            { gParent.action.suffix($T_RESULT, hasProcLangBindSpec); }
    ;

result_name
    :    name
            { gParent.action.result_name(); }
    ;

// R1230
end_function_stmt
@init{Token lbl = null; Token id = null;}
    : (label {lbl=$label.tk;})? T_END T_FUNCTION ( T_IDENT {id=$T_IDENT;})?
        end_of_stmt
        {gParent.action.end_function_stmt(lbl, $T_END, $T_FUNCTION, id,
                                  $end_of_stmt.tk);}
    | (label {lbl=$label.tk;})? T_ENDFUNCTION    ( T_IDENT {id=$T_IDENT;})?
        end_of_stmt
        {gParent.action.end_function_stmt(lbl, $T_ENDFUNCTION, null, id,
                                  $end_of_stmt.tk);}
    | (label {lbl=$label.tk;})? T_END end_of_stmt
        {gParent.action.end_function_stmt(lbl, $T_END, null, id, $end_of_stmt.tk);}
    ;

// R1231
// specification_part made non-optional to remove END ambiguity (as can
// be empty)
subroutine_subprogram
    :    subroutine_stmt
        specification_part
        ( execution_part )?
        ( internal_subprogram_part )?
        end_subroutine_stmt
    ;

// R1232
subroutine_stmt
@init{Token lbl = null; boolean hasPrefix=false;
        boolean hasDummyArgList=false;
        boolean hasBindingSpec=false;
        boolean hasArgSpecifier=false;}
    :        {gParent.action.subroutine_stmt__begin();}
        (label {lbl=$label.tk;})? (t_prefix {hasPrefix=true;})? T_SUBROUTINE
            T_IDENT ( T_LPAREN ( dummy_arg_list {hasDummyArgList=true;})?
            T_RPAREN ( proc_language_binding_spec {hasBindingSpec=true;})?
            {hasArgSpecifier=true;})? end_of_stmt
              {gParent.action.subroutine_stmt(lbl, $T_SUBROUTINE, $T_IDENT,
                                    $end_of_stmt.tk,
                                    hasPrefix, hasDummyArgList,
                                    hasBindingSpec, hasArgSpecifier);}
    ;

// R1233
// T_IDENT inlined for dummy_arg_name
dummy_arg
options{greedy=false; memoize=false;}
    :    T_IDENT        { gParent.action.dummy_arg($T_IDENT); }
    |    T_ASTERISK    { gParent.action.dummy_arg($T_ASTERISK); }
    ;

dummy_arg_list
@init{ int count=0;}
    :          {gParent.action.dummy_arg_list__begin();}
        dummy_arg {count++;} ( T_COMMA dummy_arg {count++;} )*
              {gParent.action.dummy_arg_list(count);}
    ;

// R1234
end_subroutine_stmt
@init{Token lbl = null; Token id=null;}
    : (label {lbl=$label.tk;})? T_END T_SUBROUTINE ( T_IDENT {id=$T_IDENT;})?
        end_of_stmt
        {gParent.action.end_subroutine_stmt(lbl, $T_END, $T_SUBROUTINE, id,
                                    $end_of_stmt.tk);}
    | (label {lbl=$label.tk;})? T_ENDSUBROUTINE    ( T_IDENT {id=$T_IDENT;})?
        end_of_stmt
        {gParent.action.end_subroutine_stmt(lbl, $T_ENDSUBROUTINE, null, id,
                                    $end_of_stmt.tk);}
    | (label {lbl=$label.tk;})? T_END end_of_stmt
        {gParent.action.end_subroutine_stmt(lbl, $T_END, null, id, $end_of_stmt.tk);}
    ;

// R1235
// T_INDENT inlined for entry_name
entry_stmt
@init {
    Token lbl = null;
    boolean hasDummyArgList=false;
    boolean hasSuffix=false;
}
    :   (label {lbl=$label.tk;})? T_ENTRY T_IDENT
            ( T_LPAREN ( dummy_arg_list {hasDummyArgList=true;} )? T_RPAREN
            ( suffix {hasSuffix=true;})? )? end_of_stmt
            {gParent.action.entry_stmt(lbl, $T_ENTRY, $T_IDENT, $end_of_stmt.tk,
                               hasDummyArgList, hasSuffix);}
    ;

// R1236
// ERR_CHK 1236 scalar_int_expr replaced by expr
return_stmt
@init{Token lbl = null; boolean hasScalarIntExpr=false;}
    :    (label {lbl=$label.tk;})? T_RETURN ( expr {hasScalarIntExpr=true;})?
            end_of_stmt
            {gParent.action.return_stmt(lbl, $T_RETURN, $end_of_stmt.tk,
                hasScalarIntExpr);}
    ;

// R1237
contains_stmt
@init{Token lbl = null;}
    :    (label {lbl=$label.tk;})? T_CONTAINS end_of_stmt
            {gParent.action.contains_stmt(lbl, $T_CONTAINS, $end_of_stmt.tk);}
    ;


// R1238
// ERR_CHK 1239 scalar_expr replaced by expr
// generic_name_list substituted for dummy_arg_name_list
// TODO Hopefully scanner and parser can help work together here to work
// around ambiguity.
// why can't this be accepted as an assignment statement and then the parser
// look up the symbol for the T_IDENT to see if it is a function??
//      Need scanner to send special token if it sees what?
// TODO - won't do a(b==3,c) = 2
stmt_function_stmt
@init{Token lbl = null; boolean hasGenericNameList=false;}
    :    (label {lbl=$label.tk;})? T_STMT_FUNCTION T_IDENT T_LPAREN
            ( generic_name_list {hasGenericNameList=true;})? T_RPAREN
            T_EQUALS expr end_of_stmt
            {gParent.action.stmt_function_stmt(lbl, $T_IDENT, $end_of_stmt.tk,
                                       hasGenericNameList);}
    ;

// added this to have a way to match the T_EOS and EOF combinations
end_of_stmt returns [Token tk]
    :
    T_EOS
        {
            //FortranToken eos = (FortranToken)$T_EOS;
            String nextFileName = null;

            retval.tk = $T_EOS;
            gParent.action.end_of_stmt($T_EOS);

            nextFileName = checkForStartOfFile();
            if(nextFileName != null) {
                gParent.action.start_of_file(nextFileName);
            }

            if(this.gParent.inputStreams.empty() == false
               && (input.LA(1) == APTTokenTypes.T_EOF
                   || input.LA(1) == APTTokenTypes.EOF)) {
                String oldStream;
                if(this.gParent.inputStreams.empty() == false) {
                    oldStream = this.gParent.inputStreams.pop();
                }
                input.consume();

                gParent.action.end_of_file();
            }

        }
        // the (EOF) => EOF is done with lookahead because if it's not there,
        // then antlr will crash with an internal error while trying to
        // generate the java code.  (as of 12.11.06)
    | (EOF) => EOF
        {
            retval.tk = $EOF; gParent.action.end_of_stmt($EOF);
            gParent.action.end_of_file();
        }
    ;