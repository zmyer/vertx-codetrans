package variable;

import io.vertx.codetrans.annotations.CodeTranslate;
import io.vertx.codetrans.VariableTest;
import io.vertx.core.AbstractVerticle;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Variable extends AbstractVerticle {

  @CodeTranslate
  public void declare() throws Exception {
    String o = VariableTest.constant;
    String b;
    b = o;
    VariableTest.o = b;
  }

  @CodeTranslate
  public void declareAndAssignNull() throws Exception {
    String o = null;
    if (VariableTest.cond) {
      o = "lazy";
    }
    VariableTest.o = o;
  }

  @CodeTranslate
  public void globalExpression() throws Exception {
    VariableTest.o = vertx;
  }

  private String member = "member_value";

  @CodeTranslate
  public void memberExpression() throws Exception {
    VariableTest.o = member;
  }

  private String uninitializedMember;

  @CodeTranslate
  public void uninitializedMemberExpression() throws Exception {
    VariableTest.o = uninitializedMember;
  }

  @CodeTranslate
  public void memberExpressionAccessedByMethod() throws Exception {
    accessMember();
  }

  private void accessMember() {
    VariableTest.o = member;
  }
}
