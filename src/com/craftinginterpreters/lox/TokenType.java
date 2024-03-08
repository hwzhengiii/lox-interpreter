package com.craftinginterpreters.lox;

/**
 * token类型枚举类
 */
enum TokenType {
  // 单字符token
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
  COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

  /*
LEFT_PAREN：左括号
RIGHT_PAREN：右括号
LEFT_BRACE：左花括号
RIGHT_BRACE：右花括号
COMMA：逗号
DOT：句点
MINUS：减号
PLUS：加号
SEMICOLON：分号
SLASH：斜杠
STAR：星号
   */

  // 一个或两个符号的token
  BANG, BANG_EQUAL,
  EQUAL, EQUAL_EQUAL,
  GREATER, GREATER_EQUAL,
  LESS, LESS_EQUAL,
/*
BANG：感叹号
BANG_EQUAL：不等号
EQUAL：等号
EQUAL_EQUAL：相等号
GREATER：大于号
GREATER_EQUAL：大于等于号
LESS：小于号
LESS_EQUAL：小于等于号
 */

  // 字面量（字面量是指表示固定值的符号或文本）
  IDENTIFIER, STRING, NUMBER,
  /*
  IDENTIFIER 表示标识符字面量，即变量名或函数名。
  STRING 表示字符串字面量，包含在双引号中的一串字符。
  NUMBER 表示数字字面量，包括整数和浮点数。
   */

  // 关键字
  AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
  PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

  EOF
}