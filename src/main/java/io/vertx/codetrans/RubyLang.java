package io.vertx.codetrans;

import com.sun.source.tree.LambdaExpressionTree;
import io.vertx.codegen.Case;
import io.vertx.codegen.TypeInfo;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class RubyLang implements Lang {

  static class RubyRenderer extends CodeWriter {
    LinkedHashSet<TypeInfo.Class> imports = new LinkedHashSet<>();
    RubyRenderer(Lang lang) {
      super(lang);
    }
  }

  @Override
  public Script loadScript(ClassLoader loader, String path) throws Exception {
    String filename = path + ".rb";
    InputStream in = loader.getResourceAsStream(filename);
    if (in == null) {
      throw new Exception("Could not find " + filename);
    }
    String source = new Scanner(in,"UTF-8").useDelimiter("\\A").next();
    ScriptingContainer container = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
    return new Script() {
      @Override
      public String getSource() {
        return source;
      }
      @Override
      public Void call() throws Exception {
        container.runScriptlet(source);
        return null;
      }
    };
  }

  @Override
  public void renderBlock(BlockModel block, CodeWriter writer) {
    if (writer instanceof RubyRenderer) {
      Lang.super.renderBlock(block, writer);
    } else {
      RubyRenderer langRenderer = new RubyRenderer(this);
      Lang.super.renderBlock(block, langRenderer);
      for (TypeInfo.Class type : langRenderer.imports) {
        writer.append("require '").
            append(type.getModuleName()).
            append('/').
            append(Case.SNAKE.format(Case.CAMEL.parse(type.getSimpleName()))).
            append("'\n");
      }
      writer.append(langRenderer.getBuffer());
    }
  }

  @Override
  public void renderIfThenElse(ExpressionModel condition, StatementModel thenBody, StatementModel elseBody, CodeWriter writer) {
    writer.append("if ");
    condition.render(writer);
    writer.append("\n");
    writer.indent();
    thenBody.render(writer);
    writer.unindent();
    if (elseBody != null) {
      writer.append("else\n");
      writer.indent();
      elseBody.render(writer);
      writer.unindent();
      writer.append("end");
    } else {
      writer.append("end");
    }
  }

  @Override
  public void renderPrefixIncrement(ExpressionModel expression, CodeWriter writer) {
    expression.render(writer);
    writer.append("+=1");
  }

  @Override
  public void renderPostfixIncrement(ExpressionModel expression, CodeWriter writer) {
    expression.render(writer);
    writer.append("+=1");
  }

  @Override
  public void renderPostfixDecrement(ExpressionModel expression, CodeWriter writer) {
    expression.render(writer);
    writer.append("-=1");
  }

  @Override
  public void renderPrefixDecrement(ExpressionModel expression, CodeWriter writer) {
    expression.render(writer);
    writer.append("-=1");
  }

  @Override
  public void renderNullLiteral(CodeWriter writer) {
    writer.append("nil");
  }

  @Override
  public void renderBinary(BinaryExpressionModel expression, CodeWriter writer) {
    if (Helper.isString(expression)) {
      Helper.renderInterpolatedString(expression, writer, "#{", "}");
    } else {
      Lang.super.renderBinary(expression, writer);
    }
  }

  @Override
  public void renderStatement(StatementModel statement, CodeWriter writer) {
    statement.render(writer);
    writer.append("\n");
  }

  @Override
  public void renderMethodReference(ExpressionModel expression, String methodName, CodeWriter writer) {
    // That works only for &block, not PROC!!!!
    writer.append("&");
    expression.render(writer);
    writer.append(".method(:").append(Case.SNAKE.format(Case.CAMEL.parse(methodName))).append(")");
  }

  @Override
  public void renderMethodInvocation(ExpressionModel expression, String methodName, List<TypeInfo> parameterTypes, List<ExpressionModel> argumentModels, List<TypeInfo> argumentTypes, CodeWriter writer) {
    Lang.super.renderMethodInvocation(expression, Case.SNAKE.format(Case.CAMEL.parse(methodName)), parameterTypes, argumentModels, argumentTypes, writer);
  }

  @Override
  public void renderFloatLiteral(String value, CodeWriter writer) {
    renderCharacters(value, writer);
  }

  @Override
  public void renderDoubleLiteral(String value, CodeWriter writer) {
    renderCharacters(value, writer);
  }

  @Override
  public void renderLongLiteral(String value, CodeWriter writer) {
    renderCharacters(value, writer);
  }

  @Override
  public void renderMapGet(ExpressionModel map, ExpressionModel arg, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public void renderMapForEach(ExpressionModel map, String keyName, TypeInfo keyType, String valueName, TypeInfo valueType, LambdaExpressionTree.BodyKind bodyKind, CodeModel block, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public String getExtension() {
    return "rb";
  }

  @Override
  public void renderJsonObject(JsonObjectLiteralModel jsonObject, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public void renderJsonArray(JsonArrayLiteralModel jsonArray, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public void renderDataObject(DataObjectLiteralModel model, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public void renderJsonObjectAssign(ExpressionModel expression, ExpressionModel name, ExpressionModel value, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public void renderDataObjectAssign(ExpressionModel expression, ExpressionModel name, ExpressionModel value, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public void renderJsonObjectToString(ExpressionModel expression, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public void renderJsonObjectMemberSelect(ExpressionModel expression, ExpressionModel name, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public void renderDataObjectMemberSelect(ExpressionModel expression, ExpressionModel name, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public void renderLambda(LambdaExpressionTree.BodyKind bodyKind, List<TypeInfo> parameterTypes, List<String> parameterNames, CodeModel body, CodeWriter writer) {
    writer.append("lambda {");
    if (parameterNames.size() > 0) {
      writer.append(" |");
      for (int i = 0; i < parameterNames.size(); i++) {
        if (i == 0) {
          writer.append(" ");
        } else {
          writer.append(", ");
        }
        writer.append(parameterNames.get(i));
      }
      writer.append("|");
    }
    writer.append("\n");
    writer.indent();
    body.render(writer);
    writer.unindent();
    writer.append("}");
  }

  @Override
  public void renderEnumConstant(TypeInfo.Class.Enum type, String constant, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public void renderThrow(String throwableType, ExpressionModel reason, CodeWriter writer) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public ExpressionModel classExpression(TypeInfo.Class type) {
    return ExpressionModel.render(
        "Java::" + Case.CAMEL.format(Case.QUALIFIED.parse(type.getPackageName())) + "::" + type.getSimpleName()
    );
  }

  @Override
  public ExpressionModel asyncResult(String identifier) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public ExpressionModel asyncResultHandler(LambdaExpressionTree.BodyKind bodyKind, String resultName, CodeModel body) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public ExpressionModel staticFactory(TypeInfo.Class type, String methodName, List<TypeInfo> parameterTypes, List<ExpressionModel> arguments, List<TypeInfo> argumentTypes) {
    return ExpressionModel.render(writer -> {
      RubyRenderer jsRenderer = (RubyRenderer) writer;
      jsRenderer.imports.add(type);
      String expr = Case.CAMEL.format(Case.KEBAB.parse(type.getModule().getName())) + "::" + type.getSimpleName();
      renderMethodInvocation(ExpressionModel.render(expr), methodName, parameterTypes, arguments, argumentTypes, writer);
    });
  }

  @Override
  public StatementModel variable(TypeInfo type, String name, ExpressionModel initializer) {
    return StatementModel.render(renderer -> {
      if (initializer != null) {
        renderer.append(name);
        renderer.append(" = ");
        initializer.render(renderer);
      }
    });
  }

  @Override
  public StatementModel enhancedForLoop(String variableName, ExpressionModel expression, StatementModel body) {
    return StatementModel.render(renderer -> {
      expression.render(renderer);
      renderer.append(".each do |").append(variableName).append("|\n");
      renderer.indent();
      body.render(renderer);
      renderer.unindent();
      renderer.append("end");
    });
  }

  @Override
  public StatementModel forLoop(StatementModel initializer, ExpressionModel condition, ExpressionModel update, StatementModel body) {
    return StatementModel.render(writer -> {
      initializer.render(writer);
      writer.append('\n');
      writer.append("while (");
      condition.render(writer);
      writer.append(")\n");
      writer.indent();
      body.render(writer);
      update.render(writer);
      writer.append('\n');
      writer.unindent();
      writer.append("end");
    });
  }

  @Override
  public ExpressionModel console(ExpressionModel expression) {
    throw new UnsupportedOperationException("todo");
  }
}
