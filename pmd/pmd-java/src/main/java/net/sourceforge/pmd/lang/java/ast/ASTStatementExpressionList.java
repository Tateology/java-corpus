/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTStatementExpressionList.java */

package net.sourceforge.pmd.lang.java.ast;

public class ASTStatementExpressionList extends AbstractJavaNode {
    public ASTStatementExpressionList(int id) {
        super(id);
    }

    public ASTStatementExpressionList(JavaParser p, int id) {
        super(p, id);
    }


    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}