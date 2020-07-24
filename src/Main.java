import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static boolean isCorrectBrackets(String s) throws Exception {
        if (s.matches(".*\\([- +*/]*\\).*")) {
            throw new Exception("Invalid expression");
        }
        ArrayDeque<Character> stack = new ArrayDeque<>();
        String e = "()";
        for (int i = 0; i < s.length(); i++) {
            char cur = s.charAt(i);
            if (e.indexOf(cur) == -1) {
                continue;
            }
            if (stack.isEmpty() || e.indexOf(stack.peekLast()) + 1 != e.indexOf(cur)) {
                stack.addLast(cur);
            } else {
                stack.removeLast();
            }
        }
        return stack.isEmpty();
    }

    public static boolean isVariableAssignment(String s) {
        Pattern pattern = Pattern.compile(".*=.*");
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    public static boolean isCorrectVariableName(String variableName) {
        return variableName.matches("[a-zA-Z]+");
    }

    public static boolean isNumber(String s) {
        return s.matches("-?\\d+");
    }

    public static int getValue(String s, Map variables) throws Exception {
        s = s.replaceAll(" ", "");
        if (isNumber(s)) {
            return Integer.parseInt(s);
        } else {
            if (!isCorrectVariableName(s)) {
                throw new Exception("Invalid identifier");
            }
            if (variables.containsKey(s)) {
                return (int)variables.get(s);
            } else {
                throw new Exception("Unknown variable");
            }
        }
    }

    public static boolean isOperator (Character c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    public static boolean isOperator(String s) {
        char c = s.charAt(0);
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    public static boolean isMathSymbol(Character c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')';
    }

    public static boolean isMathSymbol(String s) {
        char c = s.charAt(0);
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')';
    }

    public static String[] separateExpression(String expression) {
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == ' ') {
                if (cur.length() > 0) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append("|");
                    }
                    stringBuilder.append(cur);
                    cur = new StringBuilder();
                }
            } else {
                if (isMathSymbol(c)) {
                    if (cur.length() > 0) {
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append("|");
                        }
                        stringBuilder.append(cur);
                    }
                    cur = new StringBuilder();
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append("|");
                    }
                    stringBuilder.append(c);
                } else {
                    cur.append(c);
                }
            }
        }
        if (cur.length() > 0) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("|");
            }
            stringBuilder.append(cur);
        }
        return stringBuilder.toString().split("\\|");
    }

    public static int getPiority(String operation) throws Exception {
        if (operation.equals("-") || operation.equals("+")) {
            return 1;
        }
        if (operation.equals("*") || operation.equals("/")) {
            return 2;
        }
        throw new Exception("Unexpected Operator");
    }

    public static void checkSeparatedExpression(String[] separatedExpression, Map variables) throws Exception {
        for (int i = 0; i < separatedExpression.length; i++) {
            String cur = separatedExpression[i];
            if ((!isMathSymbol(cur) && !isNumber(cur) && !isCorrectVariableName(cur))) {
                throw new Exception("Invalid identifier");
            }
            if (isCorrectVariableName(cur) && !variables.containsKey(cur)) {
                throw new Exception("Unknown variable");
            }
            if (i + 1 < separatedExpression.length) {
                String next = separatedExpression[i + 1];
                if (cur.equals("(") || cur.equals(")")) {
                    continue;
                }
                boolean isOpCur = isOperator(cur);
                boolean isOpNext = isOperator(next);
                if ((isOperator(cur) && isOperator(next)) || (!isMathSymbol(cur) && !isMathSymbol(next))) {
                    throw new Exception("Invalid expression");
                }
                if ((!isMathSymbol(cur) && !isNumber(cur) && !isCorrectVariableName(cur)) || (!isMathSymbol(next) && !isNumber(next) && !isCorrectVariableName(next))) {
                    throw new Exception("Invalid identifier");
                }
            }
        }
    }

    public static List<String> getPostfixExpression(String[] separatedExpression) throws Exception {
        List<String> list = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        for (String cur : separatedExpression) {
            if (cur.equals("(")) {
                stack.push(cur);
            } else if (cur.equals(")")) {
                while (!stack.peek().equals("(")) {
                    list.add(stack.peek());
                    stack.pop();
                }
                stack.pop();
            } else if (isOperator(cur)) {
                while (!stack.isEmpty() && !stack.peek().equals("(") && getPiority(cur) <= getPiority(stack.peek())) {
                    list.add(stack.peek());
                    stack.pop();
                }
                stack.add(cur);
            } else {
                list.add(cur);
            }
        }
        while (!stack.isEmpty()) {
            list.add(stack.peek());
            stack.pop();
        }
        return list;
    }

    public static BigInteger getOperationResult(BigInteger first, BigInteger second, String operation) {
        if (operation.equals("+")) {
            return first.add(second);
        }
        if (operation.equals("-")) {
            return first.subtract(second);
        }
        if (operation.equals("*")) {
            return first.multiply(second);
        }
        if (operation.equals("/")) {
            return first.divide(second);
        }
        return null;
    }

    public static BigInteger getResultPostfixExpression(List<String> list, Map variables) {
        Stack<BigInteger> stack = new Stack<>();
        for (String cur : list) {
            if (isOperator(cur)) {
                BigInteger second = stack.peek();
                stack.pop();
                BigInteger first = BigInteger.ZERO;
                if (!stack.isEmpty()) {
                    first = stack.peek();
                    stack.pop();
                }
                stack.push(getOperationResult(first, second, cur));
            } else {
                BigInteger val;
                if (isNumber(cur)) {
                    val = new BigInteger(cur);
                } else {
                    val = new BigInteger(String.valueOf((BigInteger)variables.get(cur)));
                }
                stack.push(val);
            }
        }
        return stack.peek();
    }

    public static String processExpressionRepitivePlusAndMinus(String s) {
        Pattern minusPattern = Pattern.compile("-(--)*");
        Pattern plusPattren = Pattern.compile("(\\++|(--)+)");
        Matcher minusMatcher = minusPattern.matcher(s);
        Matcher plusMatcher = plusPattren.matcher(minusMatcher.replaceAll("-"));
        return plusMatcher.replaceAll("+");
    }

    public static BigInteger getResultOfExpression(String expression, Map variables) throws Exception {
        if (!isCorrectBrackets(expression)) {
            throw new Exception("Invalid expression");
        }
        expression = processExpressionRepitivePlusAndMinus(expression);;
        String[] separatedExpression = separateExpression(expression);
        checkSeparatedExpression(separatedExpression, variables);
        List<String> postfixExpression = getPostfixExpression(separatedExpression);
        return getResultPostfixExpression(postfixExpression, variables);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String s;
        Map<String, BigInteger> variables = new HashMap<>();
        while (true) {
            s = scanner.nextLine();
            if (s.isEmpty()) {
                continue;
            }
            if (s.equals("/exit")) {
                System.out.println("Bye!");
                break;
            }
            if (s.equals("/help")) {
                System.out.println("The program calculates the sum of numbers");
                continue;
            }
            if (s.charAt(0) == '/') {
                System.out.println("Unknown command");
                continue;
            }
            if (isVariableAssignment(s)) {
                try {
                    String[] values = s.split("=");
                    if (values.length != 2 || !values[0].matches("\\s*\\w+\\s*")) {
                        throw new Exception("Invalid assignment");
                    }
                    String variableName = values[0].replaceAll(" ", "");
                    if (!isCorrectVariableName(variableName)) {
                        throw new Exception("Invalid identifier");
                    }
                    String expression = values[1];
                    variables.put(variableName, getResultOfExpression(expression, variables));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                continue;
            }
            try {
                System.out.println(getResultOfExpression(s, variables));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}