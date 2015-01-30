package io.vertx.codetrans;

import io.vertx.codegen.TypeInfo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class OptionsModel extends ExpressionModel {

  public static ExpressionModel classModel(TypeInfo.Class type) {
    return forNew(args -> new OptionsModel(type));
  }

  public static ExpressionModel instanceModel(ExpressionModel expression, TypeInfo.Class type) {
    return new ExpressionModel() {
      @Override
      public ExpressionModel onMemberSelect(String identifier) {
        return ExpressionModel.forMethodInvocation(arguments -> {
          if (isSet(identifier)) {
            return ExpressionModel.render( writer -> {
              writer.getLang().renderOptionsAssign(expression,
                  ExpressionModel.render(unwrapSet(identifier)),
                  arguments.get(0), writer);
            });
          }
          if (isGet(identifier)) {
            return ExpressionModel.render( writer -> {
              writer.getLang().renderOptionsMemberSelect(expression,
                  ExpressionModel.render(unwrapSet(identifier)), writer);
            });
          }
          throw new UnsupportedOperationException("TODO");
        });
      }
      @Override
      public void render(CodeWriter writer) {
        expression.render(writer);
      }
    };
  }

  private final TypeInfo.Class type;
  private final Map<String, Member> members;

  private OptionsModel(TypeInfo.Class type) {
    this(type, Collections.emptyMap());
  }

  private OptionsModel(TypeInfo.Class type, Map<String, Member> members) {
    this.type = type;
    this.members = members;
  }

  public Iterable<Member> getMembers() {
    return members.values();
  }

  @Override
  public ExpressionModel onMemberSelect(String identifier) {
    String name;
    Function<String, Member> memberFactory;
    if (isSet(identifier)) {
      name = unwrapSet(identifier);
      memberFactory = $ -> new Member.Single(render(name));
    } else if (isAdd(identifier)) {
      name = unwrapAdd(identifier);
      memberFactory = $ -> new Member.Array(render(name));
    } else {
      throw unsupported();
    }
    return new ExpressionModel() {
      @Override
      public ExpressionModel onMethodInvocation(List<ExpressionModel> arguments) {
        if (arguments.size() == 1) {
          Map<String, Member> copy = new LinkedHashMap<>(members);
          ExpressionModel value = arguments.get(0);
          Member member = copy.computeIfAbsent(name, memberFactory);
          member.append(value);
          copy.put(name, member);
          return new OptionsModel(type, copy);
        } else {
          throw unsupported();
        }
      }
    };
  }

  public void render(CodeWriter writer) {
    writer.getLang().renderOptions(this, writer);
  }

  private static boolean isGet(String identifier) {
    return (identifier.startsWith("get") && identifier.length() > 3) ||
        (identifier.startsWith("is") && identifier.length() > 2);
  }

  private static String unwrapGet(String identifier) {
    if (identifier.startsWith("get")) {
      return Character.toLowerCase(identifier.charAt(3)) + identifier.substring(4);
    } else {
      return Character.toLowerCase(identifier.charAt(2)) + identifier.substring(3);
    }
  }

  private static boolean isSet(String identifier) {
    return identifier.startsWith("set") && identifier.length() > 3;
  }

  private static String unwrapSet(String identifier) {
    return Character.toLowerCase(identifier.charAt(3)) + identifier.substring(4);
  }

  private static boolean isAdd(String identifier) {
    return identifier.startsWith("add") && identifier.length() > 3;
  }

  private static String unwrapAdd(String identifier) {
    return Character.toLowerCase(identifier.charAt(3)) + identifier.substring(4) + "s"; // 's' for plural
  }
}
