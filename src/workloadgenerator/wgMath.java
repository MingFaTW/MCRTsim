/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workloadgenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
/**
 * Small numeric utility helpers used by the workload generator.
 * <p>
 * This class provides fixed-precision arithmetic wrappers (add, sub, mul,
 * div) using BigDecimal to avoid floating point accumulation issues, and a
 * simple integer-range random helper used to perturb times when generating
 * workload elements.
 * </p>
 *
 * @author YC
 */
public class wgMath 
{
    /**
     * Construct a helper utility. The class is stateless; all arithmetic
     * helpers are provided as static methods. The public constructor exists
     * only for completeness and potential future extension.
     */
    public wgMath()
    {
        
    }
    
    /**
     * Return a random integer in the inclusive range [min, max]. When
     * {@code min > max} the method returns 0.
     *
     * @param min the minimum integer value (inclusive)
     * @param max the maximum integer value (inclusive)
     * @return a random long value between min and max, or 0 if min>max
     */
    public static long rangeRandom(long min, long max)
    {
        
        if(min<=max)
            return ((int)(Math.random()*(max-min+1)+min));
        else
            return 0;
        
    }
    
    /**
     * Precise addition using BigDecimal to avoid double rounding errors.
     *
     * @param min left operand
     * @param max right operand
     * @return precise sum as double
     */
    public static double add(double min,double max){
        BigDecimal m = new BigDecimal(Double.toString(min));
        BigDecimal M = new BigDecimal(Double.toString(max));
        return m.add(M).doubleValue();
    }
    /**
     * Precise subtraction using BigDecimal to avoid double rounding errors.
     *
     * @param min left operand
     * @param max right operand
     * @return precise difference as double
     */
    public static double sub(double min,double max){
        BigDecimal m = new BigDecimal(Double.toString(min));
        BigDecimal M = new BigDecimal(Double.toString(max));
        return m.subtract(M).doubleValue();
    }
    /**
     * Precise multiplication using BigDecimal to avoid double rounding errors.
     *
     * @param min left operand
     * @param max right operand
     * @return precise product as double
     */
    public static double mul(double min,double max){
        BigDecimal m = new BigDecimal(Double.toString(min));
        BigDecimal M = new BigDecimal(Double.toString(max));
        return m.multiply(M).doubleValue();
    }
    /**
     * Precise division using BigDecimal to avoid double rounding errors.
     * <p>
     * The result is rounded to 10 decimal places using HALF_UP rounding mode.
     * </p>
     *
     * @param min dividend
     * @param max divisor
     * @return precise quotient as double
     */
    public static double div(double min,double max){

        BigDecimal m = new BigDecimal(Double.toString(min));
        BigDecimal M = new BigDecimal(Double.toString(max));
        
        BigDecimal result = m.divide(M, 10, RoundingMode.HALF_UP);
        return result.doubleValue();        
    }
}