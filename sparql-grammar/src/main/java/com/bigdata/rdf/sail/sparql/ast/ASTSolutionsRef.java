/* Generated By:JJTree: Do not edit this line. ASTSolutionsRef.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.bigdata.rdf.sail.sparql.ast;

public
class ASTSolutionsRef extends SimpleNode {

  private String name;

  public ASTSolutionsRef(int id) {
    super(id);
  }

  public ASTSolutionsRef(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }

  public String getName() {
      return name;
  }

  public void setName(String name) {
      this.name = name;
  }

}
/* JavaCC - OriginalChecksum=7878191e40fef169b2a851eda042bfb0 (do not edit this line) */
