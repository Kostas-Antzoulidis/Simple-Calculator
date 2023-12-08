package com.example.simplecalculatorapp

import net.objecthunter.exp4j.ExpressionBuilder


// Helper class that uses ObjectHunter's Expression Builder to evaluate strings of mathematical expressions
class Calculator {
    fun evaluateExpression(expression: String): Double {
        val sanitizedExpression = expression.replace("x", "*") // Replacing 'x' with '*' for multiplication
        return try {
            ExpressionBuilder(sanitizedExpression).build().evaluate()
        } catch (e: Exception) {
            Double.NaN
        }
    }
}