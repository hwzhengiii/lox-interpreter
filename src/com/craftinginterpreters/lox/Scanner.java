package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

/**
 * 扫描器类
 */
class Scanner {
  private final String source; // 源代码
  private final List<Token> tokens = new ArrayList<>(); // 扫描出的token
  private int start = 0; // 扫描源代码的指针
  private int current = 0;
  private int line = 1;
  private static final Map<String, TokenType> keywords; // 存储关键字的map

  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }

  Scanner(String source) {
    this.source = source;
  }

  /**
   * 从源代码中扫描出所有token
   * @return 含有所有token的列表
   */
  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  /**
   * 根据当前的指针情况获取下一个token
   */
  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break; // 1. 单字符判断
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break;
      case '!': // 2. 有些token可能有两个字符，所以还要判断一下后面的那个字符
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/': // 可能为更长的token，此处可能为注释或除法
        if (match('/')) {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance(); // 使用while循环消耗注释内容
        } else {
          addToken(SLASH);
        }
        break;
      case ' ': // 空格，回车（将当前光标移动到行首），制表符
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;
      case '\n':
        line++;
        break;
      case '"': string(); break; // 表示字符串字面量开始了,调用string方法解析它
      default: // 若没有对应的值则说明用户输入了数字，关键字或非法值
        if (isDigit(c)) {
          number(); // 为数字则调用number方法处理
        } else if (isAlpha(c)) {
          identifier(); // 为标识符则调用identifier方法处理
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }

  /**
   * 处理标识符字面量，如变量名函数名
   */
  private void identifier() {
    while (isAlphaNumeric(peek())) advance(); // 调用这个函数的地方已经确保不会以数字开头

    String text = source.substring(start, current); // 获取到标识符
    TokenType type = keywords.get(text); // 看看是否为关键字
    if (type == null) type = IDENTIFIER; // 若不是关键字则为用户自定义标识符，如变量
    addToken(type);
  }

  /**
   * 处理数字字面量，包括浮点数
   */
  private void number() {
    while (isDigit(peek())) advance(); // 消耗掉之后的数字

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) { // 处理浮点数，（.1和1.都是非法输入，浮点数要是完整的）
      // Consume the "."
      advance();

      while (isDigit(peek())) advance();
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  /**
   * 处理字符串字面量
   */
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++; // 允许跨行字符串
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string."); // 报错，字符串只有开始没有结束的冒号
      return;
    }

    // The closing ".
    advance(); // 指针往后移动一位，指向下引号后面一个字符

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1); // 去掉引号截取出字符串
    addToken(STRING, value); // 添加到token列表中
  }

  /**
   * 判断是否读文原文件
   * @return
   */
  private boolean isAtEnd() {
    return current >= source.length();
  }

  /**
   * 获取源代码当前字符并将指针往后移动1位
   * @return
   */
  private char advance() {
    return source.charAt(current++);
  }

  /**
   * 将token添加到token列表中
   * @param type token类型
   */
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  /**
   * 将token添加到token列表中
   * @param type token类型
   * @param literal 字面量值
   */
  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  /**
   * 判断是否为两字符token
   * @param expected 期望的第二个字符
   * @return
   */
  private boolean match(char expected) {
    if (isAtEnd()) return false; // 若已经到达文件末尾直接返回false
    if (source.charAt(current) != expected) return false; // 若"下一个"字符不是期望的则返回false

    current++; // 是的话则消耗一个字符，将指针往后移动一位
    return true;
  }

  /**
   * 查看"下一个"字符但不移动指针
   * @return
   */
  private char peek() {
    if (isAtEnd()) return '\0'; // 返回空字符，说明到达了字符串的末尾
    return source.charAt(current);
  }

  /**
   * 查看"下下个"字符的值，不移动指针
   * @return
   */
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  /**
   * 判断是否为合法的标识符开头：大小写字母或下划线
   * @param c
   * @return
   */
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
            (c >= 'A' && c <= 'Z') ||
            c == '_';
  }

  /**
   * 判断是否为[a-zA-Z_0-9]中的值
   * @param c
   * @return
   */
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  /**
   * 判断字符是否为0-9的数字
   * @param c
   * @return
   */
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }
}
