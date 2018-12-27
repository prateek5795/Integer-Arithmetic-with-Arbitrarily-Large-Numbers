package sxv176330;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static sxv176330.UtilClass.Operator.LEFT_PARENTHESIS;
import static sxv176330.UtilClass.Operator.RIGHT_PARENTHESIS;
import static java.util.Map.entry;

/**
 * UtilClass to provide Helper functions like Add, Subtract, Square etc. which can be in
 * @see Num
 * @author Sivagurunathan Velayutham
 * @since 1.9
 */
public class UtilClass {


    /**
     * Zero Num represents the "0" in integer
     */
    public static final Num ZERO = new Num(0);

    /**
     * One Num represents the "1" in integer
     */

    public static final Num ONE = new Num(1);

    /**
     * List of functions which can be used in {@see Num} class.
     */

    static Supplier<Num> numFactory = Num::new;
    static Function<String, Num> builder = Num::new;
    static BiFunction<Num, Num, Num> add = sxv176330.Num::calcSum;
    static BiFunction<Num, Num, Num> subtract = sxv176330.Num::calcDiff;
    static Function<Num, Num> identity = num -> num;
    static BiFunction<Num, Num, Num> multiply = sxv176330.Num::prod;
    static BiFunction<Num, Num, Num> divide = Num::calcDivide;
    static BiFunction<Num, Num, Num> mod = Num::mod;
    static BiFunction<Num,Num, Num> power = Num::power;
    static Function<Num,Num> square = num1 -> Num.power(num1, 2);
    static Function<String[], String[]> applyShuntingYard = UtilClass::rpnUsingShuntingYard;
    static Function<String[], Num> evaluateExpression = UtilClass::evaluateExpression;

    /**
     * Storing the list of operators which can be used in evaluating the expression
     * Operators contains operator and the precedence stored.
     */

    public enum Operator {
        ADD("+",1),
        SUBTRACT("-", 2),
        MULTIPLY("*", 3),
        DIVIDE("/", 4),
        MOD("%", 5),
        POWER("^", 6),
        LEFT_PARENTHESIS("(", 7),
        RIGHT_PARENTHESIS(")", 8);
        String sign;
        int order;

        Operator(String sign,int order) {
            this.sign = sign;
            this.order = order;
        }

        public String getSign() {
            return sign;
        }
    }

    /**
     * Creating an immutable map for storing the operator and precedence
     * @use java 1.9
     */

    private static Map<String, Operator> opsPrecedenceMap = Map.ofEntries(
            entry("+", Operator.ADD),
            entry("-", Operator.SUBTRACT),
            entry("*", Operator.MULTIPLY),
            entry("/", Operator.DIVIDE),
            entry("%", Operator.MOD),
            entry("^", Operator.POWER));


    /**
     * Check the precedence of the operator
     * @param op1 sign of the operator in string
     * @param op2 sign of the operator in string
     * @return true if the op1 having higher precedence than op2
     */

    private static boolean isOp1HigherPrecOp2(String op1, String op2) {
        return opsPrecedenceMap.containsKey(op1) &&
                opsPrecedenceMap.get(op1).order >= opsPrecedenceMap.get(op2).order;
    }

    /**
     * Convert the given infix expression to Postfix expression using
     * Shunting yard algorithm. See https://en.wikipedia.org/wiki/Shunting-yard_algorithm
     * @param expr representing the infix expression
     * @return string[] representing postfix expression
     */

    public static String[] rpnUsingShuntingYard(String[] expr) {
        ArrayDeque<String> stack = new ArrayDeque<>();
        List<String> outputQueue = new LinkedList<>();
        for(String token : expr) {
            // check token
            if(opsPrecedenceMap.containsKey(token)) {
                while (!stack.isEmpty() && isOp1HigherPrecOp2(stack.peek(), token)) {
                    outputQueue.add(stack.pop());
                }
                stack.push(token);
            }
            else if(token.equals(LEFT_PARENTHESIS.getSign())) {
                stack.push(token);
            }
            else if(token.equals(RIGHT_PARENTHESIS.getSign())) {
                while (!stack.isEmpty() && !stack.peek().equals(LEFT_PARENTHESIS.getSign())) {
                    outputQueue.add(stack.pop());
                }
                // pop left parenthesis
                stack.pop();
            }
            else {
                outputQueue.add(token);
            }
        }

        while (!stack.isEmpty()) {
            outputQueue.add(stack.pop());
        }
        System.out.println(outputQueue);
        return outputQueue.stream().toArray(String[]::new);
    }


    /**
     * @param string input to check
     * @return true if the string is Number else return false
     */

    private static boolean isNumber(String string) {
        try {
            Long.parseLong(string);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * evaluate the given postfix expression and return the result as Num
     * @param expr postfix expression
     * @return result of the expression as Num
     * @throws IllegalFormatException if the input have extra operands
     * @throws NumberFormatException if the input is not valid
     */

    private static Num evaluateExpression(String[] expr) {
        ArrayDeque<Num> stack = new ArrayDeque<>();
        for (String token : expr) {
            if(opsPrecedenceMap.containsKey(token)){
                Num b = stack.pop();
                Num a = stack.pop();
                Operator operator = opsPrecedenceMap.get(token);
                switch (operator) {
                    case ADD:
                        stack.push(add.apply(a,b));
                        break;
                    case SUBTRACT:
                        stack.push(subtract.apply(a,b));
                        break;
                    case MULTIPLY:
                        stack.push(multiply.apply(a,b));
                        break;
                    case DIVIDE:
                        stack.push(divide.apply(a,b));
                        break;
                    case MOD:
                        stack.push(mod.apply(a,b));
                        break;
                    case POWER:
                        stack.push(power.apply(a,b));
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            }
            else {
                stack.push(builder.apply(token));
            }
        }

        if(!stack.isEmpty() && stack.size() == 1)
            return stack.pop();
        else
            throw new IllegalArgumentException();
    }
}