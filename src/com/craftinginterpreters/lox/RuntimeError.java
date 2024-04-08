package com.craftinginterpreters.lox;

/**
 * 运行时异常处理类
 */
class RuntimeError extends RuntimeException {
  final Token token;

  RuntimeError(Token token, String message) {
    super(message);
    this.token = token;
  }
}