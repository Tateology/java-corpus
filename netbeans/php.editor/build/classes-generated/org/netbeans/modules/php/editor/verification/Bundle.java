package org.netbeans.modules.php.editor.verification;
/** Localizable strings for {@link org.netbeans.modules.php.editor.verification}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @return <i>Declare Abstract Class</i>
     * @see ImplementAbstractMethodsHintError
     */
    static String AbstractClassFixDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AbstractClassFixDesc");
    }
    /**
     * @param Class_name Class name
     * @return <i>Abstract class </i>{@code Class_name}<i> can not be instantiated</i>
     * @see AbstractClassInstantiationHintError
     */
    static String AbstractClassInstantiationDesc(Object Class_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AbstractClassInstantiationDesc", Class_name);
    }
    /**
     * @return <i>Abstract Class Instantiation</i>
     * @see AbstractClassInstantiationHintError
     */
    static String AbstractClassInstantiationHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AbstractClassInstantiationHintDispName");
    }
    /**
     * @param Method_name Method name
     * @return <i>Method "</i>{@code Method_name}<i>" can not be declared abstract and final</i>
     * @see ModifiersCheckHintError
     */
    static String AbstractFinalMethod(Object Method_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AbstractFinalMethod", Method_name);
    }
    /**
     * @param Method_name Method name
     * @return <i>Abstract method "</i>{@code Method_name}<i>" can not be declared private</i>
     * @see ModifiersCheckHintError
     */
    static String AbstractPrivateMethod(Object Method_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AbstractPrivateMethod", Method_name);
    }
    /**
     * @param Method_name Method name
     * @return <i>Abstract method "</i>{@code Method_name}<i>" can not contain body</i>
     * @see ModifiersCheckHintError
     */
    static String AbstractWithBlockMethod(Object Method_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AbstractWithBlockMethod", Method_name);
    }
    /**
     * @param Condition_text Condition text
     * @return <i>Accidental assignment in a condition </i>{@code Condition_text}
     * @see AccidentalAssignmentHint
     */
    static String AccidentalAssignmentHintCustom(Object Condition_text) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AccidentalAssignmentHintCustom", Condition_text);
    }
    /**
     * @return <i>Using an assignment operator (=) instead of comparison operator (===) is a frequent cause of bugs. Therefore assignments in conditional clauses should be avoided.</i>
     * @see AccidentalAssignmentHint
     */
    static String AccidentalAssignmentHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AccidentalAssignmentHintDesc");
    }
    /**
     * @return <i>Accidental Assignments</i>
     * @see AccidentalAssignmentHint
     */
    static String AccidentalAssignmentHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AccidentalAssignmentHintDispName");
    }
    /**
     * @return <i>Add Braces</i>
     * @see BracesHint
     */
    static String AddBraces() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AddBraces");
    }
    /**
     * @param Modifier_name Modifier name
     * @return <i>Add modifier: </i>{@code Modifier_name}
     * @see ModifiersCheckHintError
     */
    static String AddModifierFixDesc(Object Modifier_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AddModifierFixDesc", Modifier_name);
    }
    /**
     * @param Use_statement Use statement
     * @return <i>Generate "</i>{@code Use_statement}<i>"</i>
     * @see AddUseImportSuggestion
     */
    static String AddUseImportFix_Description(Object Use_statement) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AddUseImportFix_Description", Use_statement);
    }
    /**
     * @return <i>Add Use Import</i>
     * @see AddUseImportSuggestion
     */
    static String AddUseImportRuleDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AddUseImportRuleDesc");
    }
    /**
     * @return <i>Add Use Import</i>
     * @see AddUseImportSuggestion
     */
    static String AddUseImportRuleDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AddUseImportRuleDispName");
    }
    /**
     * @return <i>Possible accidental comparison found. Check if you wanted to use '=' instead.</i>
     * @see AmbiguousComparisonHint
     */
    static String AmbiguousComparisonHintCustom() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AmbiguousComparisonHintCustom");
    }
    /**
     * @return <i>Tries to reveal typos in assignments (assignments with more than one assignment operator).</i>
     * @see AmbiguousComparisonHint
     */
    static String AmbiguousComparisonHintDescName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AmbiguousComparisonHintDescName");
    }
    /**
     * @return <i>Ambiguous Comparison</i>
     * @see AmbiguousComparisonHint
     */
    static String AmbiguousComparisonHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AmbiguousComparisonHintDispName");
    }
    /**
     * @return <i>You can use new shorter array creation syntax</i>
     * @see ArraySyntaxSuggestion
     */
    static String ArraySyntaxDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ArraySyntaxDesc");
    }
    /**
     * @return <i>Allows you to change old array syntax to new shorter one.</i>
     * @see ArraySyntaxSuggestion
     */
    static String ArraySyntaxDescName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ArraySyntaxDescName");
    }
    /**
     * @return <i>Array Syntax</i>
     * @see ArraySyntaxSuggestion
     */
    static String ArraySyntaxDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ArraySyntaxDispName");
    }
    /**
     * @return <i>Assign Return Value To New Variable</i>
     * @see AssignVariableSuggestion
     */
    static String AssignVariableHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AssignVariableHintDesc");
    }
    /**
     * @return <i>Introduce Variable</i>
     * @see AssignVariableSuggestion
     */
    static String AssignVariableHintDisplayName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AssignVariableHintDisplayName");
    }
    /**
     * @return <i>Change Comparison to Assignment</i>
     * @see AmbiguousComparisonHint
     */
    static String AssignmentHintFixDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "AssignmentHintFixDisp");
    }
    /**
     * @return <i>Hints</i>
     * @see HintsAdvancedOption
     */
    static String CTL_Hints_DisplayName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CTL_Hints_DisplayName");
    }
    /**
     * @return <i>Static code verification for PHP</i>
     * @see HintsAdvancedOption
     */
    static String CTL_Hints_ToolTip() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CTL_Hints_ToolTip");
    }
    /**
     * @param Comparison_text Comparison text
     * @return <i>Change assignment to comparison: </i>{@code Comparison_text}
     * @see AccidentalAssignmentHint
     */
    static String ChangeAssignmentDisp(Object Comparison_text) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ChangeAssignmentDisp", Comparison_text);
    }
    /**
     * @param Fixed_name Fixed name
     * @return <i>Fix Name To "</i>{@code Fixed_name}<i>"</i>
     * @see AddUseImportSuggestion
     */
    static String ChangeNameFix_Description(Object Fixed_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ChangeNameFix_Description", Fixed_name);
    }
    /**
     * @return <i>Detect language features not compatible with PHP version indicated in project settings</i>
     * @see PHP54UnhandledError
     */
    static String CheckPHP54VerDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CheckPHP54VerDesc");
    }
    /**
     * @return <i>Language feature not compatible with PHP version indicated in project settings</i>
     * @see PHP54UnhandledError
     */
    static String CheckPHP54VerDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CheckPHP54VerDisp");
    }
    /**
     * @return <i>Detect language features not compatible with PHP version indicated in project settings</i>
     * @see PHP55UnhandledError
     */
    static String CheckPHP55VerDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CheckPHP55VerDesc");
    }
    /**
     * @return <i>Language feature not compatible with PHP version indicated in project settings</i>
     * @see PHP55UnhandledError
     */
    static String CheckPHP55VerDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CheckPHP55VerDisp");
    }
    /**
     * @return <i>Detect language features not compatible with PHP version indicated in project settings</i>
     * @see PHP56UnhandledError
     */
    static String CheckPHP56VerDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CheckPHP56VerDesc");
    }
    /**
     * @return <i>Language feature not compatible with PHP version indicated in project settings</i>
     * @see PHP56UnhandledError
     */
    static String CheckPHP56VerDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CheckPHP56VerDisp");
    }
    /**
     * @return <i>Maximum allowed lines per class declaration.</i>
     * @see TooManyLinesHint
     */
    static String ClassLinesHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ClassLinesHintDesc");
    }
    /**
     * @return <i>Class Declaration</i>
     * @see TooManyLinesHint
     */
    static String ClassLinesHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ClassLinesHintDisp");
    }
    /**
     * @param class_length_in_lines class length in lines
     * @param allowed_lines_per_class_declaration allowed lines per class declaration
     * @return <i>Class Length is </i>{@code class_length_in_lines}<i> Lines (</i>{@code allowed_lines_per_class_declaration}<i> allowed)</i>
     * @see TooManyLinesHint
     */
    static String ClassLinesHintText(Object class_length_in_lines, Object allowed_lines_per_class_declaration) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ClassLinesHintText", class_length_in_lines, allowed_lines_per_class_declaration);
    }
    /**
     * @return <i>Do-While Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String DoWhileBracesHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "DoWhileBracesHintDesc");
    }
    /**
     * @return <i>Do-While Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String DoWhileBracesHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "DoWhileBracesHintDisp");
    }
    /**
     * @return <i>Do-While Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String DoWhileBracesHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "DoWhileBracesHintText");
    }
    /**
     * @return <i>Empty statements should be removed.</i>
     * @see EmptyStatementHint
     */
    static String EmptyStatementHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "EmptyStatementHintDesc");
    }
    /**
     * @return <i>Empty Statement</i>
     * @see EmptyStatementHint
     */
    static String EmptyStatementHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "EmptyStatementHintDisp");
    }
    /**
     * @return <i>Remove Empty Statement</i>
     * @see EmptyStatementHint
     */
    static String EmptyStatementHintFix() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "EmptyStatementHintFix");
    }
    /**
     * @return <i>Empty Statement</i>
     * @see EmptyStatementHint
     */
    static String EmptyStatementHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "EmptyStatementHintText");
    }
    /**
     * @return <i>Error control operator disables all error reporting for an affected expression. It should be used only for some special cases (like fopen(), unlink(), etc.). Otherwise it's a cause of an unexpected behavior of the application. Handle your errors in a common way.</i>
     * @see ErrorControlOperatorHint
     */
    static String ErrorControlOperatorHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ErrorControlOperatorHintDesc");
    }
    /**
     * @return <i>Error Control Operator Misused</i>
     * @see ErrorControlOperatorHint
     */
    static String ErrorControlOperatorHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ErrorControlOperatorHintDisp");
    }
    /**
     * @return <i>Remove Error Control Operator</i>
     * @see ErrorControlOperatorHint
     */
    static String ErrorControlOperatorHintFix() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ErrorControlOperatorHintFix");
    }
    /**
     * @return <i>Error Control Operator Misused</i>
     * @see ErrorControlOperatorHint
     */
    static String ErrorControlOperatorHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ErrorControlOperatorHintText");
    }
    /**
     * @param Class_name Class name
     * @return <i>Class "</i>{@code Class_name}<i>" contains abstract methods and can not be declared final</i>
     * @see ModifiersCheckHintError
     */
    static String FinalPossibleAbstractClass(Object Class_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "FinalPossibleAbstractClass", Class_name);
    }
    /**
     * @return <i>Use New Array Creation Syntax</i>
     * @see ArraySyntaxSuggestion
     */
    static String FixDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "FixDesc");
    }
    /**
     * @return <i>For Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String ForBracesHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ForBracesHintDesc");
    }
    /**
     * @return <i>For Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String ForBracesHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ForBracesHintDisp");
    }
    /**
     * @return <i>For Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String ForBracesHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ForBracesHintText");
    }
    /**
     * @return <i>ForEach Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String ForEachBracesHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ForEachBracesHintDesc");
    }
    /**
     * @return <i>ForEach Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String ForEachBracesHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ForEachBracesHintDisp");
    }
    /**
     * @return <i>ForEach Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String ForEachBracesHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ForEachBracesHintText");
    }
    /**
     * @return <i>Maximum allowed lines per function/method declaration.</i>
     * @see TooManyLinesHint
     */
    static String FunctionLinesHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "FunctionLinesHintDesc");
    }
    /**
     * @return <i>Function (Method) Declaration</i>
     * @see TooManyLinesHint
     */
    static String FunctionLinesHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "FunctionLinesHintDisp");
    }
    /**
     * @param function_length_in_lines function length in lines
     * @param allowed_lines_per_function_declaration allowed lines per function declaration
     * @return <i>Method Length is </i>{@code function_length_in_lines}<i> Lines (</i>{@code allowed_lines_per_function_declaration}<i> allowed)</i>
     * @see TooManyLinesHint
     */
    static String FunctionLinesHintText(Object function_length_in_lines, Object allowed_lines_per_function_declaration) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "FunctionLinesHintText", function_length_in_lines, allowed_lines_per_function_declaration);
    }
    /**
     * @return <i>Comparison with "equal (==)" operator should be avoided, use "identical (===)" operator instead</i>
     * @see IdenticalComparisonSuggestion
     */
    static String IdenticalComparisonDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IdenticalComparisonDesc");
    }
    /**
     * @return <i>You should use "identical" instead of "equal" comparison to have better control over your code.</i>
     * @see IdenticalComparisonSuggestion
     */
    static String IdenticalComparisonHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IdenticalComparisonHintDesc");
    }
    /**
     * @return <i>Identical Comparisons</i>
     * @see IdenticalComparisonSuggestion
     */
    static String IdenticalComparisonHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IdenticalComparisonHintDispName");
    }
    /**
     * @return <i>If-Else Statements Must Use Braces</i>
     * @see BracesHint
     */
    static String IfBracesHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IfBracesHintDesc");
    }
    /**
     * @return <i>If-Else Statements Must Use Braces</i>
     * @see BracesHint
     */
    static String IfBracesHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IfBracesHintDisp");
    }
    /**
     * @return <i>If-Else Statements Must Use Braces</i>
     * @see BracesHint
     */
    static String IfBracesHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IfBracesHintText");
    }
    /**
     * @param Method_name Method name
     * @return <i>Interface method "</i>{@code Method_name}<i>" can not contain body</i>
     * @see ModifiersCheckHintError
     */
    static String IfaceMethodWithBlock(Object Method_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IfaceMethodWithBlock", Method_name);
    }
    /**
     * @return <i>Checks a number of assignments into a variable in a block.</i>
     * @see ImmutableVariablesHint
     */
    static String ImmutableVariableHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ImmutableVariableHintDesc");
    }
    /**
     * @return <i>Immutable Variables</i>
     * @see ImmutableVariablesHint
     */
    static String ImmutableVariableHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ImmutableVariableHintDispName");
    }
    /**
     * @param Number_of_allowed_assignments Number of allowed assignments
     * @param Number_of_assignments Number of assignments
     * @param Variable_name Variable name
     * @return <i>You should use only:<br></i>{@code Number_of_allowed_assignments}<i> assignment(s) (</i>{@code Number_of_assignments}<i> used)<br>to a variable:<br>$</i>{@code Variable_name}<i><br>to avoid accidentally overwriting it and make your code easier to read.</i>
     * @see ImmutableVariablesHint
     */
    static String ImmutableVariablesHintCustom(Object Number_of_allowed_assignments, Object Number_of_assignments, Object Variable_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ImmutableVariablesHintCustom", Number_of_allowed_assignments, Number_of_assignments, Variable_name);
    }
    /**
     * @return <i>Implement All Abstract Methods</i>
     * @see ImplementAbstractMethodsHintError
     */
    static String ImplementAbstractMethodsDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ImplementAbstractMethodsDesc");
    }
    /**
     * @return <i>Implement All Abstract Methods</i>
     * @see ImplementAbstractMethodsHintError
     */
    static String ImplementAbstractMethodsDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ImplementAbstractMethodsDispName");
    }
    /**
     * @param Class_name Class name
     * @param Abstract_method_name Abstract method name
     * @param Owner_class_of_abstract_method Owner (class) of abstract method
     * @return {@code Class_name}<i> is not abstract and does not override abstract method </i>{@code Abstract_method_name}<i> in </i>{@code Owner_class_of_abstract_method}
     * @see ImplementAbstractMethodsHintError
     */
    static String ImplementAbstractMethodsHintDesc(Object Class_name, Object Abstract_method_name, Object Owner_class_of_abstract_method) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ImplementAbstractMethodsHintDesc", Class_name, Abstract_method_name, Owner_class_of_abstract_method);
    }
    /**
     * @return <i>Initializes field with a parameter passed to constructor.</i>
     * @see InitializeFieldSuggestion
     */
    static String InitializeFieldSuggestionDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InitializeFieldSuggestionDesc");
    }
    /**
     * @return <i>Initialize Field in Constructor</i>
     * @see InitializeFieldSuggestion
     */
    static String InitializeFieldSuggestionDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InitializeFieldSuggestionDisp");
    }
    /**
     * @param Field_name Field name
     * @return <i>Initialize Field: </i>{@code Field_name}
     * @see InitializeFieldSuggestion
     */
    static String InitializeFieldSuggestionFix(Object Field_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InitializeFieldSuggestionFix", Field_name);
    }
    /**
     * @param Field_name Field name
     * @return <i>Initialize Field: </i>{@code Field_name}
     * @see InitializeFieldSuggestion
     */
    static String InitializeFieldSuggestionText(Object Field_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InitializeFieldSuggestionText", Field_name);
    }
    /**
     * @return <i>Maximum allowed lines per interface declaration.</i>
     * @see TooManyLinesHint
     */
    static String InterfaceLinesHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InterfaceLinesHintDesc");
    }
    /**
     * @return <i>Interface Declaration</i>
     * @see TooManyLinesHint
     */
    static String InterfaceLinesHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InterfaceLinesHintDisp");
    }
    /**
     * @param interface_length_in_lines interface length in lines
     * @param allowed_lines_per_interface_declaration allowed lines per interface declaration
     * @return <i>Interface Length is </i>{@code interface_length_in_lines}<i> Lines (</i>{@code allowed_lines_per_interface_declaration}<i> allowed)</i>
     * @see TooManyLinesHint
     */
    static String InterfaceLinesHintText(Object interface_length_in_lines, Object allowed_lines_per_interface_declaration) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InterfaceLinesHintText", interface_length_in_lines, allowed_lines_per_interface_declaration);
    }
    /**
     * @param Constant_name Constant name
     * @param Class_name Class name
     * @param File_name File name
     * @return <i>Create Constant "</i>{@code Constant_name}<i>" in Class "</i>{@code Class_name}<i>" (</i>{@code File_name}<i>)</i>
     * @see IntroduceSuggestion
     */
    static String IntroduceHintClassConstDesc(Object Constant_name, Object Class_name, Object File_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IntroduceHintClassConstDesc", Constant_name, Class_name, File_name);
    }
    /**
     * @param Class_name Class name
     * @param File_name File name
     * @return <i>Create Class "</i>{@code Class_name}<i>" in </i>{@code File_name}
     * @see IntroduceSuggestion
     */
    static String IntroduceHintClassDesc(Object Class_name, Object File_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IntroduceHintClassDesc", Class_name, File_name);
    }
    /**
     * @return <i>Introduce Hint</i>
     * @see IntroduceSuggestion
     */
    static String IntroduceHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IntroduceHintDesc");
    }
    /**
     * @return <i>Introduce Hint</i>
     * @see IntroduceSuggestion
     */
    static String IntroduceHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IntroduceHintDispName");
    }
    /**
     * @param Field_name Field name
     * @param Class_name Class name
     * @param File_name File name
     * @return <i>Create Field "</i>{@code Field_name}<i>" in Class "</i>{@code Class_name}<i>" (</i>{@code File_name}<i>)</i>
     * @see IntroduceSuggestion
     */
    static String IntroduceHintFieldDesc(Object Field_name, Object Class_name, Object File_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IntroduceHintFieldDesc", Field_name, Class_name, File_name);
    }
    /**
     * @param Method_name Method name
     * @param Class_name Class name
     * @param File_name File name
     * @return <i>Create Method "</i>{@code Method_name}<i>" in Class "</i>{@code Class_name}<i>" (</i>{@code File_name}<i>)</i>
     * @see IntroduceSuggestion
     */
    static String IntroduceHintMethodDesc(Object Method_name, Object Class_name, Object File_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IntroduceHintMethodDesc", Method_name, Class_name, File_name);
    }
    /**
     * @param Field_name Field name
     * @param Class_name Class name
     * @param File_name File name
     * @return <i>Create Field "</i>{@code Field_name}<i>" in Class "</i>{@code Class_name}<i>" (</i>{@code File_name}<i>)</i>
     * @see IntroduceSuggestion
     */
    static String IntroduceHintStaticFieldDesc(Object Field_name, Object Class_name, Object File_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IntroduceHintStaticFieldDesc", Field_name, Class_name, File_name);
    }
    /**
     * @param Method_name Method name
     * @param Class_name Class name
     * @param File_name File name
     * @return <i>Create Method "</i>{@code Method_name}<i>" in Class "</i>{@code Class_name}<i>" (</i>{@code File_name}<i>)</i>
     * @see IntroduceSuggestion
     */
    static String IntroduceHintStaticMethodDesc(Object Method_name, Object Class_name, Object File_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IntroduceHintStaticMethodDesc", Method_name, Class_name, File_name);
    }
    /**
     * @param Field_name Field name
     * @param Modifier_name Modifier name
     * @return <i>Field "</i>{@code Field_name}<i>" can not be declared </i>{@code Modifier_name}
     * @see ModifiersCheckHintError
     */
    static String InvalidField(Object Field_name, Object Modifier_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InvalidField", Field_name, Modifier_name);
    }
    /**
     * @param Method_name Method name
     * @param Modifier_name Modifier name
     * @return <i>Interface method "</i>{@code Method_name}<i>" can not be declared </i>{@code Modifier_name}
     * @see ModifiersCheckHintError
     */
    static String InvalidIfaceMethod(Object Method_name, Object Modifier_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InvalidIfaceMethod", Method_name, Modifier_name);
    }
    /**
     * @return <i>Checks whether the keyword is used in a proper control structure.</i>
     * @see LoopOnlyKeywordsUnhandledError
     */
    static String LoopOnlyKeywordsDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "LoopOnlyKeywordsDesc");
    }
    /**
     * @param name_of_keyword name of keyword
     * @return {@code name_of_keyword}<i> outside of for, foreach, while, do-while or switch statement.</i>
     * @see LoopOnlyKeywordsUnhandledError
     */
    static String LoopOnlyKeywordsDisp(Object name_of_keyword) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "LoopOnlyKeywordsDisp", name_of_keyword);
    }
    /**
     * @return <i>Keyword outside of for, foreach, while, do-while or switch statement.</i>
     * @see LoopOnlyKeywordsUnhandledError
     */
    static String LoopOnlyKeywordsDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "LoopOnlyKeywordsDispName");
    }
    /**
     * @param Method_name Method name
     * @return <i>Method or function "</i>{@code Method_name}<i>" has already been declared</i>
     * @see MethodRedeclarationHintError
     */
    static String MethodRedeclarationCustom(Object Method_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "MethodRedeclarationCustom", Method_name);
    }
    /**
     * @return <i>Method Redeclaration</i>
     * @see MethodRedeclarationHintError
     */
    static String MethodRedeclarationHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "MethodRedeclarationHintDispName");
    }
    /**
     * @return <i>Modifiers Checker</i>
     * @see ModifiersCheckHintError
     */
    static String ModifiersCheckHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ModifiersCheckHintDispName");
    }
    /**
     * @return <i>It is a good practice to introduce a new function (method) rather than to use more nested blocks.</i>
     * @see NestedBlocksHint
     */
    static String NestedBlocksHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "NestedBlocksHintDesc");
    }
    /**
     * @return <i>Nested Blocks in Functions</i>
     * @see NestedBlocksHint
     */
    static String NestedBlocksHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "NestedBlocksHintDisp");
    }
    /**
     * @return <i>Too Many Nested Blocks in Function Declaration<br>- It is a good practice to introduce a new function rather than to use more nested blocks.</i>
     * @see NestedBlocksHint
     */
    static String NestedBlocksHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "NestedBlocksHintText");
    }
    /**
     * @return <i>Language feature not compatible with PHP version indicated in project settings</i>
     * @see PHP53UnhandledError
     */
    static String PHP53VersionErrorHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PHP53VersionErrorHintDispName");
    }
    /**
     * @return <i>Language feature not compatible with PHP version indicated in project settings</i>
     * @see PHP54UnhandledError
     */
    static String PHP54VersionErrorHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PHP54VersionErrorHintDispName");
    }
    /**
     * @return <i>Language feature not compatible with PHP version indicated in project settings</i>
     * @see PHP55UnhandledError
     */
    static String PHP55VersionErrorHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PHP55VersionErrorHintDispName");
    }
    /**
     * @return <i>Language feature not compatible with PHP version indicated in project settings</i>
     * @see PHP56UnhandledError
     */
    static String PHP56VersionErrorHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PHP56VersionErrorHintDispName");
    }
    /**
     * @return 
     * @see PSR0Hint
     */
    static String PSR0NamespaceHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR0NamespaceHintDesc");
    }
    /**
     * @return <i>Namespace Declaration</i>
     * @see PSR0Hint
     */
    static String PSR0NamespaceHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR0NamespaceHintDisp");
    }
    /**
     * @return 
     * @see PSR0Hint
     */
    static String PSR0TypeHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR0TypeHintDesc");
    }
    /**
     * @return <i>Type Declaration</i>
     * @see PSR0Hint
     */
    static String PSR0TypeHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR0TypeHintDisp");
    }
    /**
     * @param Text_which_describes_the_violation Text which describes the violation
     * @return <i>PSR-0 Violation:<br></i>{@code Text_which_describes_the_violation}
     * @see PSR0Hint
     */
    static String PSR0ViolationHintText(Object Text_which_describes_the_violation) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR0ViolationHintText", Text_which_describes_the_violation);
    }
    /**
     * @return <i>Namespace declaration name doesn't correspond to current directory structure.</i>
     * @see PSR0Hint
     */
    static String PSR0WrongNamespaceNameHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR0WrongNamespaceNameHintText");
    }
    /**
     * @return <i>Type declaration name doesn't correspond to current file path.</i>
     * @see PSR0Hint
     */
    static String PSR0WrongTypeNameHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR0WrongTypeNameHintText");
    }
    /**
     * @return <i>Class constants MUST be declared in all upper case with underscore separators.</i>
     * @see PSR1Hint
     */
    static String PSR1ConstantDeclarationHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1ConstantDeclarationHintText");
    }
    /**
     * @return <i>Class constants MUST be declared in all upper case with underscore separators.</i>
     * @see PSR1Hint
     */
    static String PSR1ConstantHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1ConstantHintDesc");
    }
    /**
     * @return <i>Class Constant Declaration</i>
     * @see PSR1Hint
     */
    static String PSR1ConstantHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1ConstantHintDisp");
    }
    /**
     * @return <i>Method names MUST be declared in camelCase().</i>
     * @see PSR1Hint
     */
    static String PSR1MethodDeclarationHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1MethodDeclarationHintDesc");
    }
    /**
     * @return <i>Method Declaration</i>
     * @see PSR1Hint
     */
    static String PSR1MethodDeclarationHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1MethodDeclarationHintDisp");
    }
    /**
     * @return <i>Method names MUST be declared in camelCase().</i>
     * @see PSR1Hint
     */
    static String PSR1MethodDeclarationHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1MethodDeclarationHintText");
    }
    /**
     * @return <i>Property names SHOULD be declared in $StudlyCaps, $camelCase, or $under_score format (consistently in a scope).</i>
     * @see PSR1Hint
     */
    static String PSR1PropertyNameHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1PropertyNameHintDesc");
    }
    /**
     * @return <i>Property Name</i>
     * @see PSR1Hint
     */
    static String PSR1PropertyNameHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1PropertyNameHintDisp");
    }
    /**
     * @return <i>Property names SHOULD be declared in $StudlyCaps, $camelCase, or $under_score format (consistently in a scope).<br>Previous property usage was in a different format, or this property name is absolutely wrong.</i>
     * @see PSR1Hint
     */
    static String PSR1PropertyNameHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1PropertyNameHintText");
    }
    /**
     * @return <i>A file SHOULD declare new symbols and cause no other side effects, or it SHOULD execute logic with side effects, but SHOULD NOT do both.</i>
     * @see PSR1Hint
     */
    static String PSR1SideEffectHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1SideEffectHintDesc");
    }
    /**
     * @return <i>Side Effects</i>
     * @see PSR1Hint
     */
    static String PSR1SideEffectHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1SideEffectHintDisp");
    }
    /**
     * @return <i>A file SHOULD declare new symbols and cause no other side effects, or it SHOULD execute logic with side effects, but SHOULD NOT do both.</i>
     * @see PSR1Hint
     */
    static String PSR1SideEffectHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1SideEffectHintText");
    }
    /**
     * @return <i>Type names SHOULD use the pseudo-namespacing convention of Vendor_ prefixes on type names.</i>
     * @see PSR1Hint
     */
    static String PSR1TypeDeclaration52HintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1TypeDeclaration52HintText");
    }
    /**
     * @return <i>Type names MUST be declared in StudlyCaps.</i>
     * @see PSR1Hint
     */
    static String PSR1TypeDeclaration53HintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1TypeDeclaration53HintText");
    }
    /**
     * @return <i>Each type MUST be in a namespace of at least one level: a top-level vendor name.</i>
     * @see PSR1Hint
     */
    static String PSR1TypeDeclaration53NoNsHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1TypeDeclaration53NoNsHintText");
    }
    /**
     * @return <i>Type names MUST be declared in StudlyCaps (Code written for 5.2.x and before SHOULD use the pseudo-namespacing convention of Vendor_ prefixes on type names). Each type is in a file by itself, and is in a namespace of at least one level: a top-level vendor name.</i>
     * @see PSR1Hint
     */
    static String PSR1TypeDeclarationHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1TypeDeclarationHintDesc");
    }
    /**
     * @return <i>Type Declaration</i>
     * @see PSR1Hint
     */
    static String PSR1TypeDeclarationHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1TypeDeclarationHintDisp");
    }
    /**
     * @return <i>Each type MUST be in a file by itself.</i>
     * @see PSR1Hint
     */
    static String PSR1TypeDeclarationMoreTypesHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1TypeDeclarationMoreTypesHintText");
    }
    /**
     * @param Text_which_describes_the_violation Text which describes the violation
     * @return <i>PSR-1 Violation:<br></i>{@code Text_which_describes_the_violation}
     * @see PSR1Hint
     */
    static String PSR1ViolationHintText(Object Text_which_describes_the_violation) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PSR1ViolationHintText", Text_which_describes_the_violation);
    }
    /**
     * @return <i>Constructor of parent class should be called if exists (it ensures the right initialization of instantiated object).</i>
     * @see ParentConstructorCallHint
     */
    static String ParentConstructorCallHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ParentConstructorCallHintDesc");
    }
    /**
     * @return <i>Parent Constructor Call</i>
     * @see ParentConstructorCallHint
     */
    static String ParentConstructorCallHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ParentConstructorCallHintDisp");
    }
    /**
     * @param Number_of_used_parameters Number of used parameters
     * @param Number_of_mandatory_parameters Number of mandatory parameters
     * @param Number_of_optional_parameters Number of optional parameters
     * @return <i>Parent Constructor is Called<br>- with wrong number of parameters: </i>{@code Number_of_used_parameters}<i>.<br>- </i>{@code Number_of_mandatory_parameters}<i> mandatory and </i>{@code Number_of_optional_parameters}<i> optional parameters needed.</i>
     * @see ParentConstructorCallHint
     */
    static String ParentConstructorCallHintIsCalledText(Object Number_of_used_parameters, Object Number_of_mandatory_parameters, Object Number_of_optional_parameters) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ParentConstructorCallHintIsCalledText", Number_of_used_parameters, Number_of_mandatory_parameters, Number_of_optional_parameters);
    }
    /**
     * @param Number_of_mandatory_parameters Number of mandatory parameters
     * @param Number_of_optional_parameters Number of optional parameters
     * @return <i>Parent Constructor is Not Called<br>- </i>{@code Number_of_mandatory_parameters}<i> mandatory and </i>{@code Number_of_optional_parameters}<i> optional parameters needed.<br>- Your objects can be wrongly initialized.</i>
     * @see ParentConstructorCallHint
     */
    static String ParentConstructorCallHintNotCalledText(Object Number_of_mandatory_parameters, Object Number_of_optional_parameters) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ParentConstructorCallHintNotCalledText", Number_of_mandatory_parameters, Number_of_optional_parameters);
    }
    /**
     * @return <i>Detect language features not compatible with PHP version indicated in project settings</i>
     * @see PHP53UnhandledError
     */
    static String PhpVersionErrorDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PhpVersionErrorDesc");
    }
    /**
     * @return <i>Language feature not compatible with PHP version indicated in project settings</i>
     * @see PHP53UnhandledError
     */
    static String PhpVersionErrorDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PhpVersionErrorDisp");
    }
    /**
     * @param Class_name Class name
     * @return <i>Class "</i>{@code Class_name}<i>" contains abstract methods and must be declared abstract</i>
     * @see ModifiersCheckHintError
     */
    static String PossibleAbstractClass(Object Class_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PossibleAbstractClass", Class_name);
    }
    /**
     * @param Method_or_function_name Method or function name
     * @return <i>Rearrange arguments of the method or function: </i>{@code Method_or_function_name}
     * @see WrongOrderOfArgsHint
     */
    static String RearrangeParamsDisp(Object Method_or_function_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RearrangeParamsDisp", Method_or_function_name);
    }
    /**
     * @param Method_name Method name
     * @return <i>Remove body of the method: </i>{@code Method_name}
     * @see ModifiersCheckHintError
     */
    static String RemoveBodyFixDesc(Object Method_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoveBodyFixDesc", Method_name);
    }
    /**
     * @param Modifier_name Modifier name
     * @return <i>Remove modifier: </i>{@code Modifier_name}
     * @see ModifiersCheckHintError
     */
    static String RemoveModifierFixDesc(Object Modifier_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoveModifierFixDesc", Modifier_name);
    }
    /**
     * @return <i>Remove Unused Use Statement</i>
     * @see UnusedUsesHint
     */
    static String RemoveUnusedUseFixDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoveUnusedUseFixDesc");
    }
    /**
     * @param Superglobal_Array_Name Superglobal Array Name
     * @return <i>Do not Access Superglobal </i>{@code Superglobal_Array_Name}<i> Array Directly.<br><br>Use some filtering functions instead (e.g. filter_input(), conditions with is_*() functions, etc.).</i>
     * @see SuperglobalsHint
     */
    static String SuperglobalHintText(Object Superglobal_Array_Name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "SuperglobalHintText", Superglobal_Array_Name);
    }
    /**
     * @return <i>Use some filtering functions instead (e.g. filter_input(), conditions with is_*() functions, etc.).</i>
     * @see SuperglobalsHint
     */
    static String SuperglobalsHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "SuperglobalsHintDesc");
    }
    /**
     * @param Superglobal_Array_Name Superglobal Array Name
     * @return <i>Do not Access </i>{@code Superglobal_Array_Name}<i> Array Directly</i>
     * @see SuperglobalsHint
     */
    static String SuperglobalsHintDisp(Object Superglobal_Array_Name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "SuperglobalsHintDisp", Superglobal_Array_Name);
    }
    /**
     * @return <i>It is a good practice to have just one return point from functions and methods. It makes it more difficult to read such a function where more return statements are used.</i>
     * @see TooManyReturnStatementsHint
     */
    static String TooManyReturnStatementsHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "TooManyReturnStatementsHintDesc");
    }
    /**
     * @return <i>Too Many Return Statements</i>
     * @see TooManyReturnStatementsHint
     */
    static String TooManyReturnStatementsHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "TooManyReturnStatementsHintDisp");
    }
    /**
     * @return <i>Too Many Return Statements</i>
     * @see TooManyReturnStatementsHint
     */
    static String TooManyReturnStatementsHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "TooManyReturnStatementsHintText");
    }
    /**
     * @return <i>Maximum allowed lines per trait declaration.</i>
     * @see TooManyLinesHint
     */
    static String TraitLinesHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "TraitLinesHintDesc");
    }
    /**
     * @return <i>Trait Declaration</i>
     * @see TooManyLinesHint
     */
    static String TraitLinesHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "TraitLinesHintDisp");
    }
    /**
     * @param trait_length_in_lines trait length in lines
     * @param allowed_lines_per_trait_declaration allowed lines per trait declaration
     * @return <i>Trait Length is </i>{@code trait_length_in_lines}<i> Lines (</i>{@code allowed_lines_per_trait_declaration}<i> allowed)</i>
     * @see TooManyLinesHint
     */
    static String TraitLinesHintText(Object trait_length_in_lines, Object allowed_lines_per_trait_declaration) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "TraitLinesHintText", trait_length_in_lines, allowed_lines_per_trait_declaration);
    }
    /**
     * @param Type_name Type name
     * @return <i>Type "</i>{@code Type_name}<i>" has been already declared</i>
     * @see TypeRedeclarationHintError
     */
    static String TypeRedeclarationDesc(Object Type_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "TypeRedeclarationDesc", Type_name);
    }
    /**
     * @return <i>Type Redeclaration</i>
     * @see TypeRedeclarationHintError
     */
    static String TypeRedeclarationRuleDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "TypeRedeclarationRuleDispName");
    }
    /**
     * @return <i>Detects variables which are used, but not initialized.&lt;br>&lt;br>Every variable should be initialized before its first use.</i>
     * @see UninitializedVariableHint
     */
    static String UninitializedVariableHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UninitializedVariableHintDesc");
    }
    /**
     * @return <i>Uninitialized Variables</i>
     * @see UninitializedVariableHint
     */
    static String UninitializedVariableHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UninitializedVariableHintDispName");
    }
    /**
     * @param Name_of_the_variable Name of the variable
     * @return <i>Variable $</i>{@code Name_of_the_variable}<i> seems to be uninitialized</i>
     * @see UninitializedVariableHint
     */
    static String UninitializedVariableVariableHintCustom(Object Name_of_the_variable) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UninitializedVariableVariableHintCustom", Name_of_the_variable);
    }
    /**
     * @return <i>It is a good practise to omit closing PHP delimiter at the end of file. It's just a source of "Headers already sent" errors.</i>
     * @see UnnecessaryClosingDelimiterHint
     */
    static String UnnecessaryClosingDelimiterHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnnecessaryClosingDelimiterHintDesc");
    }
    /**
     * @return <i>Unnecessary Closing Delimiter</i>
     * @see UnnecessaryClosingDelimiterHint
     */
    static String UnnecessaryClosingDelimiterHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnnecessaryClosingDelimiterHintDisp");
    }
    /**
     * @return <i>Remove Closing Delimiter</i>
     * @see UnnecessaryClosingDelimiterHint
     */
    static String UnnecessaryClosingDelimiterHintFix() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnnecessaryClosingDelimiterHintFix");
    }
    /**
     * @return <i>Unnecessary Closing Delimiter</i>
     * @see UnnecessaryClosingDelimiterHint
     */
    static String UnnecessaryClosingDelimiterHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnnecessaryClosingDelimiterHintText");
    }
    /**
     * @return <i>Detects unreachable statements after return, throw, break and continue statements.</i>
     * @see UnreachableStatementHint
     */
    static String UnreachableStatementHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnreachableStatementHintDesc");
    }
    /**
     * @return <i>Unreachable Statement</i>
     * @see UnreachableStatementHint
     */
    static String UnreachableStatementHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnreachableStatementHintDisp");
    }
    /**
     * @return <i>Unreachable Statement</i>
     * @see UnreachableStatementHint
     */
    static String UnreachableStatementHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnreachableStatementHintText");
    }
    /**
     * @return <i>Unused Use Statement</i>
     * @see UnusedUsesHint
     */
    static String UnsedUsesHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnsedUsesHintDisp");
    }
    /**
     * @return <i>Checks unused use statements.</i>
     * @see UnusedUsesHint
     */
    static String UnusedUsesHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnusedUsesHintDesc");
    }
    /**
     * @param Name_of_the_variable Name of the variable
     * @return <i>Variable $</i>{@code Name_of_the_variable}<i> seems to be unused in its scope</i>
     * @see UnusedVariableHint
     */
    static String UnusedVariableHintCustom(Object Name_of_the_variable) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnusedVariableHintCustom", Name_of_the_variable);
    }
    /**
     * @return <i>Detects variables which are declared, but not used in their scope.</i>
     * @see UnusedVariableHint
     */
    static String UnusedVariableHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnusedVariableHintDesc");
    }
    /**
     * @return <i>Unused Variables</i>
     * @see UnusedVariableHint
     */
    static String UnusedVariableHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnusedVariableHintDispName");
    }
    /**
     * @return <i>Generate Type Comment For Variable</i>
     * @see VarDocSuggestion
     */
    static String VarDocHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "VarDocHintDesc");
    }
    /**
     * @return <i>Generate Type Comment For Variable /* &#64;var $myvariable MyClass &#x2A;/</i>
     * @see VarDocSuggestion
     */
    static String VarDocHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "VarDocHintDispName");
    }
    /**
     * @return <i>While Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String WhileBracesHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WhileBracesHintDesc");
    }
    /**
     * @return <i>While Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String WhileBracesHintDisp() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WhileBracesHintDisp");
    }
    /**
     * @return <i>While Loops Must Use Braces</i>
     * @see BracesHint
     */
    static String WhileBracesHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WhileBracesHintText");
    }
    /**
     * @param Type_name Type name
     * @return <i>Fix comparison: === (</i>{@code Type_name}<i>) </i>
     * @see IdenticalComparisonSuggestion
     */
    static String WithRightTypeFixDesc(Object Type_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WithRightTypeFixDesc", Type_name);
    }
    /**
     * @return <i>Fix comparison: ===</i>
     * @see IdenticalComparisonSuggestion
     */
    static String WithoutTypeFixDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WithoutTypeFixDesc");
    }
    /**
     * @return <i>Wrong order of arguments</i>
     * @see WrongOrderOfArgsHint
     */
    static String WrongOrderOfArgsDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WrongOrderOfArgsDesc");
    }
    /**
     * @return <i>Optional arguments should be grouped on the right side for better readability.&lt;br>&lt;br>Example offending code:&lt;br>&lt;code>function foo($optional=NULL, $required){}&lt;/code>&lt;br>&lt;br>Recommended code:&lt;br>&lt;code>function foo($required, $optional=NULL){}&lt;/code></i>
     * @see WrongOrderOfArgsHint
     */
    static String WrongOrderOfArgsHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WrongOrderOfArgsHintDesc");
    }
    /**
     * @return <i>Order of Arguments</i>
     * @see WrongOrderOfArgsHint
     */
    static String WrongOrderOfArgsHintDispName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WrongOrderOfArgsHintDispName");
    }
    /**
     * @return <i>Parameter names in &#64;param annotations should correspond with parameter names in commented functions.</i>
     * @see WrongParamNameHint
     */
    static String WrongParamNameHintDesc() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WrongParamNameHintDesc");
    }
    /**
     * @return <i>Rename Param</i>
     * @see WrongParamNameHint
     */
    static String WrongParamNameHintFix() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WrongParamNameHintFix");
    }
    /**
     * @return <i>Wrong Param Name</i>
     * @see WrongParamNameHint
     */
    static String WrongParamNameHintName() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WrongParamNameHintName");
    }
    /**
     * @return <i>Wrong Param Name</i>
     * @see WrongParamNameHint
     */
    static String WrongParamNameHintText() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "WrongParamNameHintText");
    }
    private void Bundle() {}
}
