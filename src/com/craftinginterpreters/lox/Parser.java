package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

/**
 * 解析器：用于将tokens根据优先级转变成一棵语法树
 */
class Parser {
  private static class ParseError extends RuntimeException {} // 异常处理类
  private final List<Token> tokens; // scan扫描到的token
  private int current = 0; // tokens索引

  Parser(List<Token> tokens) { // 构造函数
    this.tokens = tokens;
  }
  Expr parse() { // 调用这个方法进行解析
    try {
      return expression();
    } catch (ParseError error) {
      return null;
    }
  }
  // expression     → equality
  private Expr expression() {
    return equality();
  }
  // equality       → comparison ( ( "!=" | "==" ) comparison )* ;
  private Expr equality() {
    Expr expr = comparison();

    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
  // comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
  private Expr comparison() {
    Expr expr = term();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
  // term           → factor ( ( "-" | "+" ) factor )* ;
  private Expr term() {
    Expr expr = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
  // factor         → unary ( ( "/" | "*" ) unary )* ;
  private Expr factor() {
    Expr expr = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
  // unary          → ( "!" | "-" ) unary
  //               | primary ;
  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return primary();
  }
  // primary        → NUMBER | STRING | "true" | "false" | "nil"
  //               | "(" expression ")" ;
  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }
    throw error(peek(), "Expect expression.");
  }
  private boolean match(TokenType... types) { // 检查current的类型是否匹配并移动指针
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }
  private Token consume(TokenType type, String message) { // 检查类型是否匹配并消耗掉一个，若不匹配抛出异常
    if (check(type)) return advance();

    throw error(peek(), message);
  }
  private boolean check(TokenType type) { // 检查current类型是否匹配
    if (isAtEnd()) return false;
    return peek().type == type;
  }
  private Token advance() { // 移动指针并返回前一个token
    if (!isAtEnd()) current++;
    return previous();
  }
  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }
  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }
  private void synchronize() { // 实现同步功能，发现错误后丢弃一部分token重新回到正轨
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }
}