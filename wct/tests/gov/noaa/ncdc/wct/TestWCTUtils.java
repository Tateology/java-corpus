package gov.noaa.ncdc.wct;

import org.junit.Test;

public class TestWCTUtils {

    @Test
    public void testProgressCalculator1() {
        int[] processProgress   = new int[] { 1 };
        int[] processCompletion = new int[] { 2 };
        
        double progress = WCTUtils.progressCalculator(processProgress, processCompletion);
        System.out.println(progress);
    }
    
    @Test
    public void testProgressCalculator2() {
        int[] processProgress   = new int[] { 1, 1 };
        int[] processCompletion = new int[] { 2, 2 };
        
        double progress = WCTUtils.progressCalculator(processProgress, processCompletion);
        System.out.println(progress);
    }
    
    @Test
    public void testProgressCalculator3() {
        int[] processProgress   = new int[] { 1, 1, 1 };
        int[] processCompletion = new int[] { 2, 2, 2 };
        
        double progress = WCTUtils.progressCalculator(processProgress, processCompletion);
        System.out.println(progress);
    }
    
    @Test
    public void testProgressCalculator4() {
        int[] processProgress   = new int[] {  7 };
        int[] processCompletion = new int[] { 14 };
        
        double progress = WCTUtils.progressCalculator(processProgress, processCompletion);
        System.out.println(progress);
    }
    
    @Test
    public void testProgressCalculator5() {
        int[] processProgress   = new int[] {  7, 3 };
        int[] processCompletion = new int[] { 14, 6 };
        
        double progress = WCTUtils.progressCalculator(processProgress, processCompletion);
        System.out.println(progress);
    }
    
    @Test
    public void testProgressCalculator6() {
        int[] processProgress   = new int[] {  7, 3, 2 };
        int[] processCompletion = new int[] { 14, 6, 4 };
        
        double progress = WCTUtils.progressCalculator(processProgress, processCompletion);
        System.out.println(progress);
    }

}
