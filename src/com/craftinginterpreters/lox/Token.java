package com.craftinginterpreters.lox;

/**
 * token对象
 */
class Token {
  final TokenType type; // token类型
  final String lexeme; // 词素（具体的token值）
  final Object literal; // 字面量值（比lexeme更小，如lexeme为"123" literal为123）
  final int line;  // 位置（在哪一行）

  Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }

  public String toString() {
    return type + " " + lexeme + " " + literal;
  }
}