package com.craftinginterpreters.lox;

/**
 * 抽象语法树打印类
 */
class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) { // 调用这个方法时传入expr就可以下面对应的vistxxx方法
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) { // 对于二元的，给操作符词素，左表达式，右表达式加括号
        return parenthesize(expr.operator.lexeme,
                expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) { // 对于group，加括号和group并处理里面的表达式
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) { // 对于字面量，若为null返回nil否则直接返回它的词素
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) { // 对于一元的，给操作符和expr加括号
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    /**
     * 给expr加括号，会递归地处理expr
     * @param name 词素
     * @param exprs 上面词素相关联的expr
     * @return 拼接后的字符串
     */
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
    public static void main(String[] args) {
        Expr expression = new Expr.Binary( // -123*45.67 实际输出(* (- 123) (group 45.67))
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));

        System.out.println(new AstPrinter().print(expression));
    }
}