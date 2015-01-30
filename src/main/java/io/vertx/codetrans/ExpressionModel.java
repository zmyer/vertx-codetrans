package io.vertx.codetrans;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ExpressionModel extends CodeModel {

  public ExpressionModel onMemberSelect(String identifier) {
    if (identifier.equals("equals")) {
      return ExpressionModel.forMethodInvocation(args -> {
        if (args.size() == 1) {
          return ExpressionModel.render(renderer -> {
            renderer.getLang().renderEquals(ExpressionModel.this, args.get(0), renderer);
          });
        } else {
          throw unsupported("equals overloading");
        }
      });
    } else {
      return ExpressionModel.render((renderer) -> {
        renderer.getLang().renderMemberSelect(ExpressionModel.this, identifier, renderer);
      });
    }
  }

  public ExpressionModel onNew(List<ExpressionModel> arguments) {
    throw unsupported(" with arguments " + arguments);
  }

  public ExpressionModel onMethodInvocation(List<ExpressionModel> arguments) {
    return ExpressionModel.render((renderer) -> {
      renderer.getLang().renderMethodInvocation(ExpressionModel.this, arguments, renderer);
    });
  }

  public ExpressionModel onPostFixIncrement() {
    return ExpressionModel.render((renderer) -> {
      renderer.getLang().renderPostfixIncrement(ExpressionModel.this, renderer);
    });
  }

  public ExpressionModel onPrefixIncrement() {
    return ExpressionModel.render((renderer) -> {
      renderer.getLang().renderPrefixIncrement(ExpressionModel.this, renderer);
    });
  }

  public ExpressionModel onPostFixDecrement() {
    return ExpressionModel.render((renderer) -> {
      renderer.getLang().renderPostfixDecrement(ExpressionModel.this, renderer);
    });
  }

  public ExpressionModel onPrefixDecrement() {
    return ExpressionModel.render((renderer) -> {
      renderer.getLang().renderPrefixDecrement(ExpressionModel.this, renderer);
    });
  }

  public ExpressionModel onLogicalComplement() {
    return ExpressionModel.render((renderer) -> {
      renderer.getLang().renderLogicalComplement(ExpressionModel.this, renderer);
    });
  }

  public ExpressionModel unaryMinus() {
    return ExpressionModel.render((renderer) -> {
      renderer.getLang().renderUnaryMinus(ExpressionModel.this, renderer);
    });
  }

  public ExpressionModel unaryPlus() {
    return ExpressionModel.render((renderer) -> {
      renderer.getLang().renderUnaryPlus(ExpressionModel.this, renderer);
    });
  }

  public static ExpressionModel forNew(Function<List<ExpressionModel>, ExpressionModel> f) {
    return new ExpressionModel() {
      @Override
      public ExpressionModel onNew(List<ExpressionModel> arguments) {
        return f.apply(arguments);
      }
    };
  }

  public static ExpressionModel forMemberSelect(String expected, Supplier<ExpressionModel> f) {
    return new ExpressionModel() {
      @Override
      public ExpressionModel onMemberSelect(String identifier) {
        if (expected.equals(identifier)) {
          return f.get();
        } else {
          throw unsupported();
        }
      }
    };
  }

  public static ExpressionModel forMemberSelect(Function<String, ExpressionModel> f) {
    return new ExpressionModel() {
      @Override
      public ExpressionModel onMemberSelect(String identifier) {
        return f.apply(identifier);
      }
    };
  }

  public static ExpressionModel forParenthesized(ExpressionModel expression) {
    return ExpressionModel.render((renderer) -> {
      renderer.getLang().renderParenthesized(expression, renderer);
    });
  }

  public static ExpressionModel forConditionalExpression(ExpressionModel condition, ExpressionModel trueExpression, ExpressionModel falseExpression) {
    return ExpressionModel.render((renderer) -> {
      renderer.getLang().renderConditionalExpression(condition, trueExpression, falseExpression, renderer);
    });
  }

  public static ExpressionModel forAssign(ExpressionModel variable, ExpressionModel expression) {
    return ExpressionModel.render((renderer) -> {
      renderer.getLang().renderAssign(variable, expression, renderer);
    });
  }

  public static ExpressionModel forMethodInvocation(Function<List<ExpressionModel>, ExpressionModel> f) {
    return new ExpressionModel() {
      @Override
      public ExpressionModel onMethodInvocation(List<ExpressionModel> arguments) {
        return f.apply(arguments);
      }
    };
  }

  public static ExpressionModel render(Consumer<CodeWriter> c) {
    return new ExpressionModel() {
      @Override
      public void render(CodeWriter writer) {
        c.accept(writer);
      }
    };
  }

  public static ExpressionModel render(Supplier<String> f) {
    return new ExpressionModel() {
      @Override
      public void render(CodeWriter writer) {
        writer.append(f.get());
      }
    };
  }

  public static ExpressionModel render(String s) {
    return new ExpressionModel() {
      @Override
      public String render(Lang lang) {
        return s;
      }
      @Override
      public void render(CodeWriter writer) {
        writer.append(s);
      }
    };
  }
}
