package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * 生成抽象语法树工具类
 */
public class GenerateAst {
  public static void main(String[] args) throws IOException {
//    if (args.length != 1) {
//      System.err.println("Usage: generate_ast <output directory>");
//      System.exit(64);
//    }
//    String outputDir = args[0];
    String outputDir="src/com/craftinginterpreters/lox"; // 目标目录
    defineAst(outputDir, "Expr", Arrays.asList( // 调用生成抽象语法树的方法，生成expr类
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right"
    ));
  }

  /**
   * 生成抽象语法树
   * @param outputDir 输出目录
   * @param baseName 基类名称
   * @param types 每个子类名称及其参数列表
   * @throws IOException io异常
   */
  private static void defineAst(
          String outputDir, String baseName, List<String> types)
          throws IOException {
    String path = outputDir + "/" + baseName + ".java";
    PrintWriter writer = new PrintWriter(path, "UTF-8");

    writer.println("package com.craftinginterpreters.lox;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println();
    writer.println("abstract class " + baseName + " {");
    defineVisitor(writer, baseName, types);
    for (String type : types) { // 调用方法生成其所有子类
      String className = type.split(":")[0].trim(); // 子类名称
      String fields = type.split(":")[1].trim(); // 参数列表
      defineType(writer, baseName, className, fields);
    }
    writer.println();
    writer.println("  abstract <R> R accept(Visitor<R> visitor);"); // 在基类中声明接受访问者的抽象方法，子类会实现这个方法
    writer.println("}");
    writer.close();
  }

  /**
   * 在基类中生成访问者接口的方法
   * @param writer 输出类
   * @param baseName 基类名称
   * @param types 子类名称：参数列表
   */
  private static void defineVisitor(
          PrintWriter writer, String baseName, List<String> types) {
    writer.println("  interface Visitor<R> {"); // 声明接口

    for (String type : types) { // 为基类的每个子类声明各自的访问方法
      String typeName = type.split(":")[0].trim();
      writer.println("    R visit" + typeName + baseName + "(" +
              typeName + " " + baseName.toLowerCase() + ");"); // 比如R visitBinaryExpr(Binary expr);调用时接受的参数是this
    }

    writer.println("  }");
  }

  /**
   * 生成抽象树中的每个子类
   * @param writer 输出类
   * @param baseName 基类名称
   * @param className 子类名称
   * @param fieldList 子类参数里列表
   */
  private static void defineType(
          PrintWriter writer, String baseName,
          String className, String fieldList) {
    writer.println("  static class " + className + " extends " +
            baseName + " {");

    // 构造方法
    writer.println("    " + className + "(" + fieldList + ") {");

    // 在构造方法中为每个成员变量赋值
    String[] fields = fieldList.split(", ");
    for (String field : fields) {
      String name = field.split(" ")[1];
      writer.println("      this." + name + " = " + name + ";");
    }

    writer.println("    }");

    // 实现父类中抽象的接受访问者的方法
    writer.println();
    writer.println("    @Override");
    writer.println("    <R> R accept(Visitor<R> visitor) {");
    writer.println("      return visitor.visit" +
            className + baseName + "(this);");
    writer.println("    }");

    // 成员变量
    writer.println();
    for (String field : fields) {
      writer.println("    final " + field + ";");
    }

    writer.println("  }");
  }
}