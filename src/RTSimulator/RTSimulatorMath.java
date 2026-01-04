/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RTSimulator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import static RTSimulator.Definition.magnificationFormat;

/**
 * Utility class for performing mathematical operations used in the RTSimulator.
 * @author ShiuJia
 */
public class RTSimulatorMath
{
    /**
     * Calculates the Least Common Multiple (LCM) of two integers.
     * 
     * @param m the first number
     * @param n the second number
     * @return the least common multiple of {@code m} and {@code n}
     */
    public long Math_lcm(long m, long n)
    {
        return m * n / Math_gcd(m, n);
    }
    
    /**
     * Calculates the Greatest Common Divisor (GCD) of two long integers.
     * 
     * @param m the first number
     * @param n the second number
     * @return the greatest common divisor of {@code m} and {@code n}
     */
    public long Math_gcd(long m, long n)
    {
        if(n != 0)
        {
            return Math_gcd(n, m % n); 
        }
        else
        {
            return m;
        }
    }
    
    /**
     * Formats a double value to a specific decimal format.
     * 
     * @param d the double value to format
     * @return the formatted double value
     */
    public double changeDecimalFormat(double d)
    {
        DecimalFormat df = new DecimalFormat(magnificationFormat);
        return Double.parseDouble(df.format(d));
    }
    
    /**
     * Formats a double value to 5 decimal places.
     * 
     * @param d the double value to format
     * @return the formatted double value with 5 decimal places
     */
    public double changeDecimalFormatFor5(double d)
    {
        DecimalFormat df = new DecimalFormat("##.00000");
        return Double.parseDouble(df.format(d));
    }
    
    /**
     * Adds two double values with high precision.
     * 
     * @param value1 the first value
     * @param value2 the second value
     * @return the sum of {@code value1} and {@code value2}
     */
    public static double add(double value1,double value2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        return b1.add(b2).doubleValue();
    }
    
    /**
     * Subtracts one double value from another with high precision.
     * 
     * @param value1 the value to be subtracted from
     * @param value2 the value to subtract
     * @return the result of {@code value1} - {@code value2}
     */
    public static double sub(double value1,double value2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        return b1.subtract(b2).doubleValue();
    }
    
    /**
     * Multiplies two double values with high precision.
     * 
     * @param value1 the first value
     * @param value2 the second value
     * @return the product of {@code value1} and {@code value2}
     */
    public static double mul(double value1,double value2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        return b1.multiply(b2).doubleValue();
    }
    
    /**
     * Divides one double value by another with high precision. The result is rounded
     * to 10 decimal places.
     * 
     * @param value1 the dividend
     * @param value2 the divisor
     * @return the result of {@code value1} / {@code value2}
     */
    public static double div(double value1,double value2) 
    {
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        return b1.divide(b2, 10, RoundingMode.HALF_UP).doubleValue();
    }
}
