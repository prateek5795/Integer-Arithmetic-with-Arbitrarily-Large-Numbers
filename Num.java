package sxv176330;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static sxv176330.UtilClass.*;


/**
 * Num class provides Immutable precision Integers that stores and
 * performs the arithmetic operations on arbitrarily large integers.
 * Num provides analogues to all of Java's primitive integer operators.
 * Additionally it provides support for evaluating Infix and Postfix
 * Expression.
 * <p>
 * <p>Semantics of arithmetic operations exactly mimic those of Java's integer
 * arithmetic operators, as defined in <i>The Java Language Specification</i>.
 * For example, division by zero throws an {@code ArithmeticException}, and
 * division of a negative by a positive yields a negative (or zero) remainder.
 * <p>
 * <p>Comparison operations perform between two Num are analogous to those Java's
 * comparison
 * <p>
 * <p>All methods and constructors in this class throw
 * {@code NullPointerException} when passed
 * a null object reference for any input parameter.
 *
 * @author Sivagurunathan Velayutham
 * @author Sai Spandan
 * @author Prateek
 * @implNote Num constructors and operations throws {@code ArithmeticException} and
 * {@code NumberFormatException} when the input is not valid or the result is
 * out of range.
 * -2<sup>{@code Integer.MAX_VALUE}</sup> (exclusive) to
 * +2<sup>{@code Integer.MAX_VALUE}</sup> (exclusive).
 * @jls 4.2.2 Integer Operations
 * @see Num
 * @since 1.9
 */

public class Num implements Comparable<Num> {

    /**
     * The defaultBase of Num: 10, Assuming that all the operations done in the Num class
     * get input as Base 10 digit.
     */
    static long defaultBase = 10;  // Change as needed


    private long base = defaultBase;  // Change as needed

    /**
     * arr used to store arbitrarily large numbers in the <i>little endian</i> order.
     * zeroth element of Num will store the most significant bit i.e in reversed order
     * arr stores the elements in 0 index format len = n, then it will be stored as
     * {0...n-1} format
     */

    private long[] arr;  // array to store arbitrarily large integers

    /**
     * isNegative keep tracks of the negative numbers in the Num. This ensure that Num class
     * can distinguishes the positive and negative number using this field.
     */
    private boolean isNegative;  // boolean flag to represent negative numbers


    /**
     * Actual number of elements stored in the array that used. Ignoring all the trailing zero's
     * which allocated extra during the initialization.
     */
    private int len;  // actual number of elements of array that are used;  number is stored in arr[0..len-1]

    /**
     * Zero Num represents the "0" in integer
     */
    public static final Num ZERO = new Num("0");

    /**
     * One Num represents the "1" in integer
     */

    public static final Num ONE = new Num("1");


    public Num() {
    }

    private Num(long[] arr, long base, boolean sign) {
        this.base = base;
        this.arr = arr;
        this.len = removeTrailingZeros(arr);
        this.isNegative = sign;
    }

    /**
     * Construct Num Object from String, assume string in base 10
     *
     * @param s - input string to construct Num object
     * @return Num object
     */

    public Num(String s) {
        s = s.trim();

        if (s.length() == 0)
            throw new IllegalArgumentException("Given String is Empty");
        Num num = copyStringToNumInReverse(s);
        this.arr = num.arr;
        this.isNegative = num.isNegative;
        this.len = num.len;
    }

    private Num copyStringToNumInReverse(String s) {
        boolean sign = false;
        if (s.charAt(0) == '-')
            sign = true;
        long arr[] = new long[s.length() + 2];
        int len = -1;
        for (int i = s.length() - 1; i >= ((sign) ? 1 : 0); i--) {
            long num = Long.parseLong(String.valueOf(s.charAt(i)));
            arr[++len] = num;
        }
        return new Num(arr, defaultBase, sign);
    }

    /**
     * Construct Num Object from Long number, assume long number in base 10
     *
     * @param x - input long number to construct the Num object
     * @return Num
     */

    public Num(long x) {
        this(Long.toString(x));
        this.len = removeTrailingZeros(this.arr);
        this.convertBase(1_000_000_000);
    }

    /**
     * Add two Num object and return the result Num
     *
     * @param a - operand a to add, in base 10
     * @param b - operand b to add, in base 10
     * @return addition {@code a + b}
     */

    public static Num add(Num a, Num b) {
        if (a.checkNumbers(b) < 0) {
            Num temp = a;
            a = b;
            b = temp;
        }
        if (a.isNegative != b.isNegative) {
            return calcDiff(a, b).convertBase10();
        } else {
            return calcSum(a, b).convertBase10();
        }
    }

    public static Num calcSum(Num a, Num b) {
        int len = Math.max(a.len, b.len) + 1;
        long[] res = new long[len * 2];
        long carry = 0;
        int i = 0, j = 0, l = 0;
        while (i <= a.len || j <= b.len) {
            long sum = (i <= a.len ? a.arr[i] : 0) + (j <= b.len ? b.arr[j] : 0) + carry;
            res[l] = sum % a.base;
            carry = sum / a.base;
            l++;
            j++;
            i++;
        }

        while (carry != 0) {
            res[l] += carry;
            carry = res[l] / a.base;
            res[l] %= a.base;
            l++;
        }

        Num ans = new Num(res, a.base, a.isNegative);
        return ans.isZero() ? ZERO : ans;
    }

    /**
     * Subtract a and b
     *
     * @param a operand a
     * @param b operand b
     * @return {@code a-b}
     */

    public static Num subtract(Num a, Num b) {
        if (a.checkNumbers(b) < 0) {
            Num temp = a;
            a = b;
            b = temp;
        }
        if (a.isNegative != b.isNegative) {
            return calcSum(a, b).convertBase10();
        } else {
            return calcDiff(a, b).convertBase10();
        }
    }

    public static Num calcDiff(Num a, Num b) {
        int i = 0, j = 0, l = 0;
        int len = Math.max(a.len, b.len) + 1;
        long[] res = new long[len * 2];

        while (i <= a.len || j <= b.len) {
            long a1, b1;
            a1 = (i <= a.len) ? a.arr[i] : 0;
            b1 = (j <= b.len) ? b.arr[j] : 0;
            if (a1 < b1) {
                int next = i + 1;
                while (next < a.arr.length - 1 && a.arr[next] == 0) {
                    a.arr[next++] = 9;
                }
                a.arr[next] -= 1;
                a.arr[i] += a.base;
                a1 = a.arr[i];
            }
            res[l] = a1 - b1;
            l++;
            j++;
            i++;
        }

        Num ans = new Num(res, a.base, a.isNegative);
        return ans.isZero() ? ZERO : ans;
    }

    /**
     * Product of a and b using O(n*2) algorithm
     *
     * @param a Num a, assume it is in Base 10
     * @param b Num b, assume it is in Base 10
     * @return {@code a * b}
     * @throws NullPointerException if a or b is null
     */

    public static Num product(Num a, Num b) {
        if (a.isZero() || b.isZero())
            return ZERO;

        if (a.len < b.len) {
            Num temp = a;
            a = b;
            b = temp;
        }
        return prod(a, b).convertBase10();
    }

    public static Num prod(Num a, Num b) {
        int arrsize = a.len + b.len + 1;
        long[] result = new long[arrsize * 2];
        int start = 0;
        long carry = 0;
        int len;

        for (int i = 0; i <= b.len; i++) {
            len = start;
            for (int j = 0; j <= a.len; j++) {
                long prev = result[len];
                long product = (a.arr[j] * b.arr[i]) + carry + prev;
                carry = product / a.base;
                result[len++] = product % a.base;
            }
            if (carry != 0) {
                result[len++] = carry;
            }
            start++;
            carry = 0;
        }
        Num ans = new Num(result, a.base(), a.isNegative != b.isNegative);
        return ans.isZero() ? ZERO : ans;
    }


    /**
     * power of a^n using divide and conquer
     *
     * @param a      Num a
     * @param n      long n
     * @param cached to cache the intermediate result while computing the power
     * @return {@code a^n}
     */

    private static Num power(Num a, long n, Map<Long, Num> cached) {
        if (cached.getOrDefault(n, null) != null) {
            return cached.get(n);
        }
        Num temp = power(a, n / 2, cached);
        cached.put(n / 2, temp);
        if (n % 2 == 0)
            return multiply.apply(temp, temp);
        else
            return multiply.apply(a, multiply.apply(temp, temp));
    }

    public static Num power(Num a, long n) {
        Map<Long, Num> cache = new HashMap<>();
        if (n < 0)
            return ZERO;
        cache.put(0L, ONE);
        cache.put(1L, a);
        return power(a, n, cache).convertBase10();
    }

    public static Num power(Num a, Num b) {
        return power(a, b.intValue());
    }

    /**
     * Divide the given two numbers a and b using Binary Search
     *
     * @return {@code a/b}
     * @throws IllegalArgumentException if b is given as Zero
     */

    public static Num divide(Num a, Num b) {
        return calcDivide(a, b).convertBase10();
    }

    public static Num calcDivide(Num a, Num b) {
        Num[] qAndR = quotientAndReminder(a, b);
        return qAndR[0] != null ? (qAndR[0].isZero() ? ZERO : qAndR[0]) : null;
    }

    /**
     * Find the quotient and reminder for a and b
     *
     * @return Num[] as array, num[0] will be quotient
     * num[1] will be reminder
     * @throws IllegalArgumentException if b is given as Zero
     */

    private static Num[] quotientAndReminder(Num a, Num b) {
        Num[] nums = new Num[2];

        //base case
        if (b.compareTo(ONE) == 0) {
            nums[0] = a;
            nums[1] = ZERO;
        } else if (b.isZero()) {
            nums[0] = null;
            return nums;
        } else if (a.compareTo(b) < 0 && !a.isNegative) {
            nums[0] = ZERO;
            nums[1] = a;
        } else if (a.compareTo(b) == 0) {
            nums[0] = ONE;
            nums[1] = ZERO;
        } else {
            boolean bNegative = b.isNegative;
            b.isNegative = false;
            Num finalB = b;
            Function<Num, Num> multiply_b_times = num1 -> multiply.apply(num1, identity.apply(finalB));
            Num quotient = binarySearch(a, multiply_b_times);
            if (a.isNegative != bNegative && !quotient.isZero())
                quotient.isNegative = true;
            nums[0] = quotient;
            if (!bNegative)
                nums[1] = subtract.apply(a, multiply.apply(b, quotient));
            else
                nums[1] = ZERO;
            b.isNegative = bNegative;
        }
        return nums;
    }

    /**
     * Find the mod of a and b
     *
     * @return null if b is negative
     **/

    public static Num mod(Num a, Num b) {
        if (b.isNegative || b.isZero())
            return null;
        Num quotient = calcDivide(a, b);
        return subtract.apply(a, multiply.apply(b, quotient));
    }

    /**
     * Find the square root using Binary search algorithm
     *
     * @return square root a, null if a is negative
     * Time Complexity : O(log(length of digit))
     */

    public static Num squareRoot(Num a) {
        if (a.isNegative)
            return null;
        if (a.compareTo(ZERO) == 0 || a.compareTo(ONE) == 0)
            return a;
        return binarySearch(a, square);
    }

    /**
     * Binary search to find the element
     *
     * @param target         target element to find in the Num
     * @param targetFunction function to check for the target element
     */

    private static Num binarySearch(Num target, Function<Num, Num> targetFunction) {
        return binarySearch(ONE, target, target, targetFunction);
    }

    private static Num binarySearch(Num start, Num end, Num target, Function<Num, Num> targetFunction) {
        boolean sign = target.isNegative;
        // set to positive always
        target.isNegative = false;
        Num ans = ZERO;
        Num left = start;
        Num right = end;

        // boundary check
        if (end.checkNumbers(target) < 0)
            return ans;

        while (left.checkNumbers(right) <= 0) {
            Num range = add.apply(right, left);
            Num mid = range.by2();
            Num targetMid = targetFunction.apply(mid);
            if (targetMid.checkNumbers(target) == 0) {
                ans = mid;
                break;
            } else if (targetMid.checkNumbers(target) < 0) {
                ans = mid;
                left = add.apply(mid, ONE);
            } else {
//                ans = left;
                if (ans.checkNumbers(ZERO) == 0)
                    ans = right;
                right = subtract.apply(mid, ONE);
            }
        }

        target.isNegative = sign;
        return ans;
    }

    /**
     * compare two Num objects similar to Integer compareTo
     *
     * @return -1, 0 or 1 as this Num is numerically less than, equal
     * to, or greater than {@code other}.
     * @throws NullPointerException if this or other object is null
     */

    public int compareTo(Num other) {
        Num thisNumber = this; // TODO Implement clone operation
        // check for the sign then check magnitude
        if (thisNumber.isNegative == other.isNegative) {
            if (!this.isNegative) {
                return thisNumber.checkNumbers(other);
            } else {
                // do the opposite
                return other.checkNumbers(this);
            }
        }
        return thisNumber.isNegative ? -1 : 1;
    }

    /**
     * Compares the magnitude array of this Num with the specified
     * other Num. This is the version of compareTo ignoring sign.
     *
     * @param other Num whose magnitude array to be compared.
     * @return -1, 0 or 1 as this magnitude array is less than, equal to or
     * greater than the magnitude array for the specified Num.
     */

    public int checkNumbers(Num other) {
        int lenCompare = Integer.compare(this.len, other.len);
        if (lenCompare != 0)
            return lenCompare;

        for (int i = this.len; i >= 0; i--) {
            int compare = Long.compare(this.arr[i], other.arr[i]);
            if (compare != 0)
                return compare;
        }
        return 0;
    }

    /**
     * Remove the trailing zero's in the Num
     *
     * @throws NullPointerException if this object is null
     */

    public int removeTrailingZeros(long[] arr) {
        int new_len = 0;
        for (int i = arr.length - 1; i >= 0; i--) {
            if (arr[i] != 0) {
                new_len = i;
                break;
            }
        }
        return new_len;
    }

    /**
     * print the number in the order LSB to MSB with base in the front
     * For example, if base=100, and the number stored corresponds to 10965,
     * then the output is "100: 65 9 1"
     */
    public void printList() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.base() + ": ");
        int digit = String.valueOf(this.base()).length();
        StringBuilder builder = new StringBuilder(this.toString());
        String reversed = builder.reverse().toString();
        int i;
        builder.setLength(0);
        for (i = len; i >= 0; i--) {
            sb.append(this.arr[i]);
            sb.append(" ");
        }
        if (this.isNegative)
            sb.append("-");
        System.out.println(sb.toString());
    }


    /**
     * Return the string representation of the number from MSB to LSB
     * For example the number is 31410, it will the Num as "31410" in string form
     *
     * @throws NullPointerException if this object is null
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (this.isNegative)
            sb.append("-");

        for (int i = this.len; i >= 0; i--) {
            sb.append(this.arr[i]);
        }
        return sb.toString();
    }

    /**
     * Return the base of the Num it's stored
     *
     * @return base in long
     */

    public long base() {
        return base;
    }

    /**
     * Convert the given Num to the newBase
     *
     * @param newBase base to convert
     * @return Num with the newBase
     */

    public Num convertBase(int newBase) {
        if (isNegative)
            return ZERO;

        if (this.isZero())
            return ZERO;

        if (this.toString().equals("1"))
            return this;

        Num base = new Num(Integer.toString(newBase));
        return convertBase(base);
    }


    private Num convertBase(Num base) {
        base.convertBase10();
        Num copy = this.convertBase10();
        long[] arr = new long[copy.len * 2 + 1];
        int j = 0;
        while (copy.compareTo(ZERO) > 0) {
            Num[] qAndR = Num.quotientAndReminder(copy, base);
            if (qAndR[0] != null) {
                copy = qAndR[0];
                arr[j++] = Long.parseLong(qAndR[1].toString());
            }
        }
        Num ans = new Num(arr, base.intValue(), false);
        this.arr = ans.arr;
        this.len = this.removeTrailingZeros(this.arr);
        this.base = Long.parseLong(base.toString());
        return this;
    }

    private Num convertBase10() {
        StringBuilder number = new StringBuilder(this.toString());
        return copyStringToNumInReverse(number.toString());
    }

    /**
     * Divide the given Num by 2
     *
     * @return Num by 2
     */

    public Num by2() {

        if (this.checkNumbers(ONE) == 0)
            return ZERO;

        long[] arr = new long[this.len + 1];
        long carry = 0;
        int i;
        for (i = this.len; i >= 0; i--) {
            // handle first number less 2
            if (i == this.len && this.arr[i] < 2) {
                carry = base;
                continue;
            }
            long sum = this.arr[i] + carry;
            arr[i] = sum / 2;
            if (sum % 2 == 0)
                carry = 0;
            else
                carry = base;
        }
        Num ans;
        ans = new Num(arr, this.base, this.isNegative);
        if (carry != 0)
            ans = add(ans, ONE);

        if (this.isNegative)
            ans.isNegative = true;
        return ans;
    }

    /**
     * check the given Num is Zero
     *
     * @return true if the num is Zero else return false
     */

    public boolean isZero() {
        return this.checkNumbers(ZERO) == 0;
    }

    /**
     * @return the integer value of the given Num
     * @throws NullPointerException if this object is null
     */

    public long intValue() {
        if (this.toString().length() > Integer.MAX_VALUE)
            throw new ArithmeticException();
        return Long.parseLong(this.toString());
    }

    /**
     * Evaluate an expression in postfix and return resulting number
     * Each string is one of: "*", "+", "-", "/", "%", "^", "0", or
     * a number: [1-9][0-9]*.  There is no unary minus operator.
     *
     * @return result of the expression in Num
     * @throws NumberFormatException    if the input contains other than the specified operator
     * @throws IllegalArgumentException if the input not valid
     */

    public static Num evaluatePostfix(String[] expr) {
        return evaluateExpression.apply(expr);
    }

    /**
     * Evaluate an expression in infix and return resulting number
     * Each string is one of: "*", "+", "-", "/", "%", "^", "(", ")", "0", or
     * a number: [1-9][0-9]*.  There is no unary minus operator.
     *
     * @return result of the expression in Num
     * @throws NumberFormatException    if the input contains other than the specified operator
     * @throws IllegalArgumentException if the input not valid
     */

    public static Num evaluateInfix(String[] expr) {
        return applyShuntingYard.andThen(evaluateExpression).apply(expr);
    }

    /**
     * Compares two objects
     *
     * @return true if two Num is equal else return false
     */

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Num))
            return false;
        Num that = (Num) obj;
        return this.compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(base, isNegative, len);
        result = 31 * result + Arrays.hashCode(arr);
        return result;
    }

    public static void main(String[] args) {
        Num x = new Num("27");
        Num y = new Num("13");
        Num x1 = new Num("-27");
        Num y1 = new Num("-13");
        Num a = new Num("31568193761112314141411912312234412312312123190");
        Num b = new Num("3659535532566681673026857047264590495633096120170316011130546064934049533282760410899967541");
        System.out.println("3659535532566681673026857047264590495633096120170316011130546064934049533282760410899967541");
        b.convertBase(87654321);
        b.printList();
        b.convertBase(100);
        b.printList();

        System.out.println("add: " + add(x, y));
        System.out.println("add: " + add(x, y1));
        System.out.println("add: " + add(x1, y));
        System.out.println("add: " + add(x1, y1));
        System.out.println("add: " + add(y, x));
        System.out.println("add: " + add(y, x1));
        System.out.println("add: " + add(y1, x));
        System.out.println("add: " + add(y1, x1));

        System.out.println("Subtract: " + subtract(x, y));
        System.out.println("Subtract: " + subtract(x, y1));
        System.out.println("Subtract: " + subtract(x1, y));
        System.out.println("Subtract: " + subtract(x1, y1));
        System.out.println("subtract: " + subtract(y, x));
        System.out.println("subtract: " + subtract(y, x1));
        System.out.println("subtract: " + subtract(y1, x));
        System.out.println("subtract: " + subtract(y1, x1));

        System.out.println("product: " + product(x, y));
        System.out.println("product: " + product(x, y1));
        System.out.println("product: " + product(x1, y));
        System.out.println("product: " + product(x1, y1));
        System.out.println("product: " + product(y, x));
        System.out.println("product: " + product(y, x1));
        System.out.println("product: " + product(y1, x));
        System.out.println("product: " + product(y1, x1));

        System.out.println("divide: " + divide(x, y));
        System.out.println("divide: " + divide(x, y1));
        System.out.println("divide: " + divide(x1, y));
        System.out.println("divide: " + divide(x1, y1));
        System.out.println("divide: " + divide(y, x));
        System.out.println("divide: " + divide(y, x1));
        System.out.println("divide: " + divide(y1, x));
        System.out.println("divide: " + divide(y1, x1));
        System.out.println("divide: " + divide(x1, ZERO));

        System.out.println("remainder: " + mod(x, y));
        System.out.println("remainder: " + mod(y, x));
        System.out.println("remainder: " + mod(x, ZERO));
        System.out.println("remainder: " + mod(ZERO, x));
//
        System.out.println("square root: " + squareRoot(x));
//        System.out.println("power " + a + " ^ " + b + " :  " + power(a,b));
        String[] expr = {"2", "3", "^", "4", "2", "*", "/", "7", "-"};
        System.out.println(evaluateInfix(expr));
        System.out.println(evaluatePostfix(expr));
        x = new Num("3659535532566681673026857047264590495633096120170316011130546064934049533282760410899967541");
        System.out.println(x.convertBase(1000));

        Map<Character, Integer> map = new HashMap<>();
        map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        System.out.println(subtract(a, a));


        Num z = Num.add(x, y);
        System.out.println(z);
        System.out.println(a);
        if(z != null) z.printList();
        Num num = new Num("43633382268981874218423785695394482174141763444003067138865952801521192294019137076869107922375453148249364982439558737844767306676946704228209206040020823113045993252652348468350909509547125052166672666725871419742257019928189551364328216469929034680405242765119367173450463395069050096112794272138197024239083806993995842267767674787197763729649190928669866552781615433880796145596239108504679393471687166647938066733274994992568351194993049924789132914553488392890819749904452553085076255248745035621502511705594688099412155023168158937815288534529722539699764793615841704611986295473722175005112626840014942729199649822592412442235207513290896939548415689598612802729602989337130662773396872920962445217253806393918692167272302008162901263591763653331437794030148350409005579298265581165898870378864397810472755186557104046216387439272057676201947773110449969972358022898863036648862465530164378707908240912293284388872242283751488484848450979660047723020914358490987634117201975664556905550306620974554631513702865986663221274527064693413808416479790786781680145149306929213108460163910082979143641949270872077212058734346515181336461301503310845293025787049077537204658398365930713186650330803601546090681629964075467950648090367550605876419581340822828220357603084351291677883014490035572844747732158385219718456140018769341808069611695829591471006065375257644348587916017598539061127236117788362179939194543764908083240428660227053018251323261606500951781647439162537774595640972844814836985933154011558197530960178849783437384733370557529617267730449972857425872333554501837849877383041950296428412556964754406023378874662012478630570918332247225586552858050534031593902593407159197381928228353393849391614536434461488410397109926995189897109848349296806034010297666592462044564672836877965812685333210416288553036444206372932343254509114205167759283618832488266325108188805888769554938337059600745389614699638690579306570509601050821749380655587468270491464668007486888100413343371314975666326700837700479211826745644821118099687375212715484941490220809588057103824121157864132054659120360095906407075259758670983303932258472226562924527471947437284540713119959765652164505665696814958283445733826588304873363248715310387210865623790587511943808677422324310313029075273518317864611551367423295324255764896095775616462227086910822951093383361115365040016855833720524672800539014007996605301586088089821471847599520525178749075454731186693475629632087004863155395615085796919189640185162426980889619223838841557120669077755825260542542051069432343621758235291377591474774711497351097496427586144102996651503025388308301194169982489955944726061630837112784349721592966578386537960018636046327466157162197148057825184395855533040452340068864320332689152001402197115348290396567657632379232427004690454544733187764059651712623279830409450082777039557362005993075560229329606334702430828219084874326889020308784549196721708465119594583555612261488060147599846023193219480351150466443980554313425747008004066911348177495844317073024925715036592716047079764887284768478471366227601299816868034236912351327189179288319705340157057627188115906030944024420664672107299143210268356868592206244629064046626013728859661457551211318876306311120178874908041807750660799408856736288126196277577896246567263649131036998903777656764325209898257964407839482737051433784807697272326821368027852638407619975662929983495858862792573342996768982379314422739985042986439941276331559381569130405804341296044187520053718214901741748360766218941730636911968665411144072976005767453220789068499794263628804381344442684919305677264459538590445285121019536969791655302947497947989002526937771207519801263374832390138710352446790706552840688359695833027634459898156331547666365748913383730608960723522035940441306788064044586633860628981151862979908338138531015004554846609689245521229333036364448908617115655265423783126243967116841917237012196967723082541553900468166699590157321971779391594852954256063783205505902992235656763134360763835429753757363608531706102874061816854040361929006558842320772909879125554564654281945062069068741727020858701971191311562068240539527153369131431660125897217987827882107590256169956965195996285981348544219742177660411438364040040922623768619664894028824965679544246592425551102907447898441016195221046294901943502643356478781958809556409710340984143203543148893851870121169309471831020357659917471134260676375892418043375082344936150208559262704873739682670178799703689746102930132242597082113928902038506896464933413209750240792701048847036451103215534858801149248417833682568612394687972799833312072571595152086410319465099430504659672283208670515245805161044206331207967437032775057508705534103269249593654862356554567501366726954055988201381808274978549086267386637392919724824306266564166443105363777535313342465540672775659431441665766155735994222054241048995207855068310653749870965513820033213450037328302186800519648696957560428148686932772080701779556187896478146723433750909315738508542693855760539092001203172755434942050749697208016559998984081158101630483397615701611916872690278723411453855679280389524037279985439297041442878617034742801966307337085720482641172355605937600773288427895402153243504316870373472362910381505804881546619098875061641319723032733879884680144397630694832001833100732071820426118388875643930150534762122683176284208290365966129484193710733174019028049992129069583819995938723504777791173537812356372224003657556534119121560808694493190724758807788561105834481603836676170909524765864901571114807452871000240871151949715433991848162431446339231627600132687081057781005101838711183126357208552004765752817116288153655714480987892801497482109521624826629387194144521173718433892284677293311967016130274008382874749037590569885720604143570994764683798255853858186120666919518687527955786804996225359611223155314442617280596217622715737831798004440540219066811658012430506512534046085350578684702719100188030465662769651375323386497887093262475377691615743389217337229515476135721864309570721212998758109779793872248768580280465497087259261241943414159028560863182382648865403556039451336065782687585663568817968734556173593471852554237324473857132140289934280607211503007817759612405559791123446235254773293248324721397722567049863222225750438955682592975998730913013151483787564553086855067574310073176057752197080003977495297683589659477722367377658324252661324411572976427044819209228458394932138404349484398595060397250802199769440185842614498049388310439441683990976027440478104763038520130035322684936154256895615763304294733292337896476907131722476659001547168460423783730460062904800199399395857422110901573477209570956183071475409090341822585620926100412144854252635321156221735813644816345747464051519957505099313595251111659449282687479772915407287536672114101375105669164402892264235035214963779513463226712897574901209497127491169992341323971807277602481485952975665888256164822343799275557856749286420361409076163739617166739231786213147415428115193250733539153793123352555154926196549295880405951083505564741368592652242826934360734755167639629025495036114078263615702386986780229923489796591545944004573395395154990787068444523811947492004874331775142394242802570325368381202375544613677210367342315402394378845252637374201366431582339075758258639612086912329325365338832219000879517139012103891503767365126790695539555715934449850643271054339448380252455807956144848443284986410124308649883268484594698392924526061535778244308622680384351531561278465205948353741325302379290017777714653120324963566441679800073033729228204024369891691336856964049665012643609007755467685176158153853272304586994479653527966763296958401439199550884434680946763414103912332085608982250494298048216796248080790602552717224078074555270996193482140526245449374083497178135320284637894849027297702997941434478691054139587927177584688274101787636864192434931600110461150762350348900334102736689967170319923140143215070948226338407631923653386699209052882672631381879229855057442471936237523131428803709313261742333160507767484236573242708326278793449634671856661218462054104950521413498426715602268653025321133100865750160833484944164818324748474603887561750044867441852290153308242268514328248703331226407306625240864175773059085317896236996277173433940283103929862674762010842062696923415039139004797034308189566324107762371988843002651345845069660990021439023185344059172302264597769422708755512106341511984765246074320575477472210624606391874591783351594316111396849920875874782910456033431051614676925976870166384020719978218644836476596666416971246006007194184247393113538826169440935064490979293731434536350284608831721633953740196809109912175893945494340775320842374565285219806325147148833935678815380461205765281084330088386504098372211006652298622596466756704730415946082862222478307484320437092899401524957657421920329061208757058276383835867749387839712678331121549752473250404516368558452614682000366332997652053592117746388234826235758964913758460974424230171092017547177432684671092726171761585410424233799160984790219878709988766559537302949097366695892358032715328085970990438840288239577226077605688237259172735755874743032260746748270302219841673781636693959879580012792840141542530964101703515252487876057133603973817619459806420585849398631291981342749963037529249439810103504036306383401212275018388597687104332359460350690522613014888602695994775994066466770567728408016101640077027931475110642353205087125741662346175120132093293198439613388496143034897943551473069726859362700712982899509384588194750265918422236592576211087751346056568227851775668197549728315789024332039919694613871358986802090152072015653547525042972794986006507111066054777914301024071455619626134747143327443096302326865441499301474341213089802106718129706993979703218143304611774214362024026448580269343377136644852020259997800969055525086347090728244743805496358907745408451116145358103324365110465723600113083777677838574825695863718825222319523813675335971729516233581844046024383021753012534454612843540909436770906629410291924969846244041498213954617319001665208580199143030798177796085426275726425597380857620201801664378798083990681529416595967951751105528576369455279652005700851695817907134923557290422736117455738512833481335259330764166758061673975379575517704136521056424308133059516458351991266590208181090990877391062452412862457074988757041057660712254187015015065718273432781551291387370782365462927331587645612777484911680353858956711101963701159718014260084994068266955344075557919306818138900102467463985825669241609807078964092217872407623684391520316274278946697784132920038652834586883643413585312548504724348593288648014637917911527887091128631386019512019148624151311317490046170830637132614730338340110259013046474402023284029647040192054586564315550762064900922049491122573233642357930822163653441775984476177263065868208383089186639308227729930961810219386874771908682095473076904112482407138600427508912812646506736631471004814768275159519406291353686024121195351706377580887561278210470016711108020978886424885064269374739628284516843120229302902545493889504379215210331361437981371063122785663571839602002919392806878830975590698801014244029955445678696484695758777194181632444394828192639230586099409388144364465177629825374802938697185340112522818695669065389205343947404981078766048768287314954308237117223767724188124400822639207794997805635082519541404016401085911314124369063757196016002816933712024121190481529178292385398336142336661325062832246852356440277408984625207887837018197577140458928865557741017535812675925537544163280409672131106586898284559848847385619610795124918372961941270934824012283994843818568633820495899057462134055108596298464087182760013644987318131716020528171907567215620504240093089073202548636939735008502953689588085823113148168168063764594878951450540083752397389815982503566904939078580043833423380622041409253845411675486249546261822828201399529702113549830515845293827249521644915444917032872655800723641154151911262633076743194074959616275566012382027757964139565970143446853966269885966490199565071951130575779772442189592876667224044525743413712205923344852847046507359635410659124093020909438520971576306823060084738337004072619202580310219025746103482482145915290498836833105336201919393829625850270163512958251129336518524881395530459952510248351302730159431641349178848968734123970637634338920919634406290837817474455917755519656740521547636886698455109789200415389766180229347183046702671442063422656312766595771628869014426526435682863182095164316692536856806040155623794759734037323017034547508841833582196147945562432008195726307815785764123384330568019396344649545824934973256492081532823735277472708971977212099420917540406148417936823047965036835790294646311858971342753396371655943654378015294309753659840619105654637986784606203255940366765615403572964151583711686315634936166309513384747691324658763432096378662956419900981868663029361609846638999498495793047620212411786271249942824824760625214133681120913263408877017103321502172502576646637300406507290274842444192405064309061928796969374206211578395744668784022509065261177326441256899452691704002677026764580707019079627471686733208503371984347984509293180989064817109662745353325423561430302106667685663280507770352095031933696220077246417649706702476609500891984984708360868225991413878711108964703563652285018571757405291592406840233208890886485300347448142473663026709154054690062576080881455357929058021970045845849806262379880863668089176277704823537935783993682392532686969007801071082188145062942082855193209849768655885553406741973292732751831974869101662326564777704435494549691736764608663138000625122775089408747486797764297275202474751430299286818329570767339779928665247280930719111254307856664747694945999848143947076053198722156030517687504524465281022952449256702685887957201706592621401456978832877482571288397431409009175689873041210564986971807597451030048399280408597698911622020651629612497795258182448555169441188117475355073189120566053917183230285900740204295477673846017433565989997878996174616420349646404937214731031584489186560419073678026305765032522790709054631832146932574308935346019265412632963182081783080211260476322765102897303539374057650645742702306949134733877420526728677357581034482204689577811078478710118935380850331138258982006007726867991249972859729144571170858266298372452197632696293374581689236290127581843605457856624904777510406651618988045874051531399451795102590650327367803032480649968560294879805872139823280048799782102029051482894782300703413373683849656188756445599133740421294386368802252217223342736730706399492348140687148672416043653410669275597410820578548459917859890489653791260620312345783724949563544559465615920590132158867735377472651999440767749789816148269190712483118504814406640844811105164463039703987539942032736535666779525654116670669724092821390359627952081516831521310023191312118018081257392613032219323894303958638444122954373949544556868349470145045522722781549668585456862045567439033700737857254761981687304866432161624170555802074509832068535336088392459564507606556781747397413833366893964527799947733649259911734200114492262158507874688722505881267163322272877408108684303561975417050171095273012091073576879842762866783054659615099112812946677675587551998285728071268820751871133071926872301833845849350523255418181685005289399126077543716935403859399962271722806772807678156390718989595556334030466200318493689096999391700684803664640476686832477971905863002442136244953195831238458578762938609035077095002458215786679941481566973370942207964821637674458285519315444888147015083495090125272364283240");
        System.out.println("43633382268981874218423785695394482174141763444003067138865952801521192294019137076869107922375453148249364982439558737844767306676946704228209206040020823113045993252652348468350909509547125052166672666725871419742257019928189551364328216469929034680405242765119367173450463395069050096112794272138197024239083806993995842267767674787197763729649190928669866552781615433880796145596239108504679393471687166647938066733274994992568351194993049924789132914553488392890819749904452553085076255248745035621502511705594688099412155023168158937815288534529722539699764793615841704611986295473722175005112626840014942729199649822592412442235207513290896939548415689598612802729602989337130662773396872920962445217253806393918692167272302008162901263591763653331437794030148350409005579298265581165898870378864397810472755186557104046216387439272057676201947773110449969972358022898863036648862465530164378707908240912293284388872242283751488484848450979660047723020914358490987634117201975664556905550306620974554631513702865986663221274527064693413808416479790786781680145149306929213108460163910082979143641949270872077212058734346515181336461301503310845293025787049077537204658398365930713186650330803601546090681629964075467950648090367550605876419581340822828220357603084351291677883014490035572844747732158385219718456140018769341808069611695829591471006065375257644348587916017598539061127236117788362179939194543764908083240428660227053018251323261606500951781647439162537774595640972844814836985933154011558197530960178849783437384733370557529617267730449972857425872333554501837849877383041950296428412556964754406023378874662012478630570918332247225586552858050534031593902593407159197381928228353393849391614536434461488410397109926995189897109848349296806034010297666592462044564672836877965812685333210416288553036444206372932343254509114205167759283618832488266325108188805888769554938337059600745389614699638690579306570509601050821749380655587468270491464668007486888100413343371314975666326700837700479211826745644821118099687375212715484941490220809588057103824121157864132054659120360095906407075259758670983303932258472226562924527471947437284540713119959765652164505665696814958283445733826588304873363248715310387210865623790587511943808677422324310313029075273518317864611551367423295324255764896095775616462227086910822951093383361115365040016855833720524672800539014007996605301586088089821471847599520525178749075454731186693475629632087004863155395615085796919189640185162426980889619223838841557120669077755825260542542051069432343621758235291377591474774711497351097496427586144102996651503025388308301194169982489955944726061630837112784349721592966578386537960018636046327466157162197148057825184395855533040452340068864320332689152001402197115348290396567657632379232427004690454544733187764059651712623279830409450082777039557362005993075560229329606334702430828219084874326889020308784549196721708465119594583555612261488060147599846023193219480351150466443980554313425747008004066911348177495844317073024925715036592716047079764887284768478471366227601299816868034236912351327189179288319705340157057627188115906030944024420664672107299143210268356868592206244629064046626013728859661457551211318876306311120178874908041807750660799408856736288126196277577896246567263649131036998903777656764325209898257964407839482737051433784807697272326821368027852638407619975662929983495858862792573342996768982379314422739985042986439941276331559381569130405804341296044187520053718214901741748360766218941730636911968665411144072976005767453220789068499794263628804381344442684919305677264459538590445285121019536969791655302947497947989002526937771207519801263374832390138710352446790706552840688359695833027634459898156331547666365748913383730608960723522035940441306788064044586633860628981151862979908338138531015004554846609689245521229333036364448908617115655265423783126243967116841917237012196967723082541553900468166699590157321971779391594852954256063783205505902992235656763134360763835429753757363608531706102874061816854040361929006558842320772909879125554564654281945062069068741727020858701971191311562068240539527153369131431660125897217987827882107590256169956965195996285981348544219742177660411438364040040922623768619664894028824965679544246592425551102907447898441016195221046294901943502643356478781958809556409710340984143203543148893851870121169309471831020357659917471134260676375892418043375082344936150208559262704873739682670178799703689746102930132242597082113928902038506896464933413209750240792701048847036451103215534858801149248417833682568612394687972799833312072571595152086410319465099430504659672283208670515245805161044206331207967437032775057508705534103269249593654862356554567501366726954055988201381808274978549086267386637392919724824306266564166443105363777535313342465540672775659431441665766155735994222054241048995207855068310653749870965513820033213450037328302186800519648696957560428148686932772080701779556187896478146723433750909315738508542693855760539092001203172755434942050749697208016559998984081158101630483397615701611916872690278723411453855679280389524037279985439297041442878617034742801966307337085720482641172355605937600773288427895402153243504316870373472362910381505804881546619098875061641319723032733879884680144397630694832001833100732071820426118388875643930150534762122683176284208290365966129484193710733174019028049992129069583819995938723504777791173537812356372224003657556534119121560808694493190724758807788561105834481603836676170909524765864901571114807452871000240871151949715433991848162431446339231627600132687081057781005101838711183126357208552004765752817116288153655714480987892801497482109521624826629387194144521173718433892284677293311967016130274008382874749037590569885720604143570994764683798255853858186120666919518687527955786804996225359611223155314442617280596217622715737831798004440540219066811658012430506512534046085350578684702719100188030465662769651375323386497887093262475377691615743389217337229515476135721864309570721212998758109779793872248768580280465497087259261241943414159028560863182382648865403556039451336065782687585663568817968734556173593471852554237324473857132140289934280607211503007817759612405559791123446235254773293248324721397722567049863222225750438955682592975998730913013151483787564553086855067574310073176057752197080003977495297683589659477722367377658324252661324411572976427044819209228458394932138404349484398595060397250802199769440185842614498049388310439441683990976027440478104763038520130035322684936154256895615763304294733292337896476907131722476659001547168460423783730460062904800199399395857422110901573477209570956183071475409090341822585620926100412144854252635321156221735813644816345747464051519957505099313595251111659449282687479772915407287536672114101375105669164402892264235035214963779513463226712897574901209497127491169992341323971807277602481485952975665888256164822343799275557856749286420361409076163739617166739231786213147415428115193250733539153793123352555154926196549295880405951083505564741368592652242826934360734755167639629025495036114078263615702386986780229923489796591545944004573395395154990787068444523811947492004874331775142394242802570325368381202375544613677210367342315402394378845252637374201366431582339075758258639612086912329325365338832219000879517139012103891503767365126790695539555715934449850643271054339448380252455807956144848443284986410124308649883268484594698392924526061535778244308622680384351531561278465205948353741325302379290017777714653120324963566441679800073033729228204024369891691336856964049665012643609007755467685176158153853272304586994479653527966763296958401439199550884434680946763414103912332085608982250494298048216796248080790602552717224078074555270996193482140526245449374083497178135320284637894849027297702997941434478691054139587927177584688274101787636864192434931600110461150762350348900334102736689967170319923140143215070948226338407631923653386699209052882672631381879229855057442471936237523131428803709313261742333160507767484236573242708326278793449634671856661218462054104950521413498426715602268653025321133100865750160833484944164818324748474603887561750044867441852290153308242268514328248703331226407306625240864175773059085317896236996277173433940283103929862674762010842062696923415039139004797034308189566324107762371988843002651345845069660990021439023185344059172302264597769422708755512106341511984765246074320575477472210624606391874591783351594316111396849920875874782910456033431051614676925976870166384020719978218644836476596666416971246006007194184247393113538826169440935064490979293731434536350284608831721633953740196809109912175893945494340775320842374565285219806325147148833935678815380461205765281084330088386504098372211006652298622596466756704730415946082862222478307484320437092899401524957657421920329061208757058276383835867749387839712678331121549752473250404516368558452614682000366332997652053592117746388234826235758964913758460974424230171092017547177432684671092726171761585410424233799160984790219878709988766559537302949097366695892358032715328085970990438840288239577226077605688237259172735755874743032260746748270302219841673781636693959879580012792840141542530964101703515252487876057133603973817619459806420585849398631291981342749963037529249439810103504036306383401212275018388597687104332359460350690522613014888602695994775994066466770567728408016101640077027931475110642353205087125741662346175120132093293198439613388496143034897943551473069726859362700712982899509384588194750265918422236592576211087751346056568227851775668197549728315789024332039919694613871358986802090152072015653547525042972794986006507111066054777914301024071455619626134747143327443096302326865441499301474341213089802106718129706993979703218143304611774214362024026448580269343377136644852020259997800969055525086347090728244743805496358907745408451116145358103324365110465723600113083777677838574825695863718825222319523813675335971729516233581844046024383021753012534454612843540909436770906629410291924969846244041498213954617319001665208580199143030798177796085426275726425597380857620201801664378798083990681529416595967951751105528576369455279652005700851695817907134923557290422736117455738512833481335259330764166758061673975379575517704136521056424308133059516458351991266590208181090990877391062452412862457074988757041057660712254187015015065718273432781551291387370782365462927331587645612777484911680353858956711101963701159718014260084994068266955344075557919306818138900102467463985825669241609807078964092217872407623684391520316274278946697784132920038652834586883643413585312548504724348593288648014637917911527887091128631386019512019148624151311317490046170830637132614730338340110259013046474402023284029647040192054586564315550762064900922049491122573233642357930822163653441775984476177263065868208383089186639308227729930961810219386874771908682095473076904112482407138600427508912812646506736631471004814768275159519406291353686024121195351706377580887561278210470016711108020978886424885064269374739628284516843120229302902545493889504379215210331361437981371063122785663571839602002919392806878830975590698801014244029955445678696484695758777194181632444394828192639230586099409388144364465177629825374802938697185340112522818695669065389205343947404981078766048768287314954308237117223767724188124400822639207794997805635082519541404016401085911314124369063757196016002816933712024121190481529178292385398336142336661325062832246852356440277408984625207887837018197577140458928865557741017535812675925537544163280409672131106586898284559848847385619610795124918372961941270934824012283994843818568633820495899057462134055108596298464087182760013644987318131716020528171907567215620504240093089073202548636939735008502953689588085823113148168168063764594878951450540083752397389815982503566904939078580043833423380622041409253845411675486249546261822828201399529702113549830515845293827249521644915444917032872655800723641154151911262633076743194074959616275566012382027757964139565970143446853966269885966490199565071951130575779772442189592876667224044525743413712205923344852847046507359635410659124093020909438520971576306823060084738337004072619202580310219025746103482482145915290498836833105336201919393829625850270163512958251129336518524881395530459952510248351302730159431641349178848968734123970637634338920919634406290837817474455917755519656740521547636886698455109789200415389766180229347183046702671442063422656312766595771628869014426526435682863182095164316692536856806040155623794759734037323017034547508841833582196147945562432008195726307815785764123384330568019396344649545824934973256492081532823735277472708971977212099420917540406148417936823047965036835790294646311858971342753396371655943654378015294309753659840619105654637986784606203255940366765615403572964151583711686315634936166309513384747691324658763432096378662956419900981868663029361609846638999498495793047620212411786271249942824824760625214133681120913263408877017103321502172502576646637300406507290274842444192405064309061928796969374206211578395744668784022509065261177326441256899452691704002677026764580707019079627471686733208503371984347984509293180989064817109662745353325423561430302106667685663280507770352095031933696220077246417649706702476609500891984984708360868225991413878711108964703563652285018571757405291592406840233208890886485300347448142473663026709154054690062576080881455357929058021970045845849806262379880863668089176277704823537935783993682392532686969007801071082188145062942082855193209849768655885553406741973292732751831974869101662326564777704435494549691736764608663138000625122775089408747486797764297275202474751430299286818329570767339779928665247280930719111254307856664747694945999848143947076053198722156030517687504524465281022952449256702685887957201706592621401456978832877482571288397431409009175689873041210564986971807597451030048399280408597698911622020651629612497795258182448555169441188117475355073189120566053917183230285900740204295477673846017433565989997878996174616420349646404937214731031584489186560419073678026305765032522790709054631832146932574308935346019265412632963182081783080211260476322765102897303539374057650645742702306949134733877420526728677357581034482204689577811078478710118935380850331138258982006007726867991249972859729144571170858266298372452197632696293374581689236290127581843605457856624904777510406651618988045874051531399451795102590650327367803032480649968560294879805872139823280048799782102029051482894782300703413373683849656188756445599133740421294386368802252217223342736730706399492348140687148672416043653410669275597410820578548459917859890489653791260620312345783724949563544559465615920590132158867735377472651999440767749789816148269190712483118504814406640844811105164463039703987539942032736535666779525654116670669724092821390359627952081516831521310023191312118018081257392613032219323894303958638444122954373949544556868349470145045522722781549668585456862045567439033700737857254761981687304866432161624170555802074509832068535336088392459564507606556781747397413833366893964527799947733649259911734200114492262158507874688722505881267163322272877408108684303561975417050171095273012091073576879842762866783054659615099112812946677675587551998285728071268820751871133071926872301833845849350523255418181685005289399126077543716935403859399962271722806772807678156390718989595556334030466200318493689096999391700684803664640476686832477971905863002442136244953195831238458578763295474357833661754983648371638437323821532336707487790604208532303991305064019473896599966087984630129653738675");
        System.out.println(add(num,b));
        Num j = new Num("23456");
        Num k = new Num("34563");
        System.out.println(prod(j,k));
        System.out.println(Num.squareRoot(num));
        Num num1 = new Num(1_000_00);
        System.out.println(num.by2());
        System.out.println(product(a,b));
        System.out.println(multiply.apply(a, b));
        System.out.println(num.convertBase((int) b.intValue()));
    }
}
