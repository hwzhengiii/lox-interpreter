package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lox {
  private static final Interpreter interpreter = new Interpreter();
  static boolean hadError = false; // 编译时判断是否出现了错误
  static boolean hadRuntimeError = false; // 检测运行时异常
  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64); 
    } else if (args.length == 1) {
      runFile(args[0]); // 从文件读取，参数为文件路径
    } else {
      runPrompt();
    }
  }

  /**
   * 从文件中读取代码
   * @param path 文件路径
   * @throws IOException
   */
  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    // Indicate an error in the exit code.
    if (hadError) System.exit(65); // 若出现错误则退出程序
    if (hadRuntimeError) System.exit(70);
  }

  /**
   * 从命令行读取代码
   * @throws IOException
   */
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
      System.out.print("> ");
      String line = reader.readLine(); // control+D读取为null，退出
      if (line == null) break;
      run(line);
      hadError = false; // 命令行的方式出现编译错误不用退出循环，所以重置hadError字段
    }
  }

  /**
   * 执行源代码
   * @param source 程序源代码
   */
  private static void run(String source) {
    Scanner scanner=new Scanner(source);
    List<Token> tokens = scanner.scanTokens(); // 从源代码中解析出所有token

//    // For now, just print the tokens.
//    for (Token token : tokens) {
//      System.out.println(token);
//    }
    Parser parser = new Parser(tokens);
    Expr expression = parser.parse();

    // Stop if there was a syntax error.
    if (hadError) return;

    interpreter.interpret(expression);
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println(
            "[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }
  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() +
            "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }
}