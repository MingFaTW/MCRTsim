/*
 * Comprehensive test for all example directories
 */
package test;

import SystemEnvironment.DataReader;
import SystemEnvironment.JSONDataReader;
import WorkLoadSet.DataSetting;
import WorkLoad.Task;

/**
 * Test all three example directories with both XML and JSON formats.
 * 
 * @author mingfa
 */
public class AllExamplesTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  All Examples XML vs JSON Test");
        System.out.println("========================================\n");
        
        String[] examples = {
            "Single_Core Scheduling Example",
            "Multi_Core Scheduling Example",
            "OPA's Priority Example"
        };
        
        for (String example : examples) {
            System.out.println("┌─────────────────────────────────────────");
            System.out.println("│ Testing: " + example);
            System.out.println("└─────────────────────────────────────────\n");
            
            testExample(example);
            System.out.println();
        }
        
        System.out.println("========================================");
        System.out.println("  All Tests Complete!");
        System.out.println("========================================");
    }
    
    private static void testExample(String exampleName) {
        String basePath = "RTSimulator_examples/" + exampleName + "/";
        String xmlWorkload = basePath + "workload.xml";
        String jsonWorkload = basePath + "workload.json";
        String xmlProcessor = basePath + "processor.xml";
        String jsonProcessor = basePath + "processor.json";
        
        // Test Workload
        System.out.println("📄 WORKLOAD Comparison:");
        System.out.println("─────────────────────────────────────────");
        
        DataSetting xmlData = null;
        DataSetting jsonData = null;
        long xmlTime = 0, jsonTime = 0;
        
        // Load XML Workload
        try {
            long start = System.nanoTime();
            DataReader xmlReader = new DataReader();
            xmlReader.loadSource(xmlWorkload);
            xmlData = xmlReader.getDataSetting();
            xmlTime = System.nanoTime() - start;
            
            System.out.println("✓ XML  loaded: " + String.format("%.3f ms", xmlTime / 1000000.0));
            System.out.println("  - Tasks: " + xmlData.getTaskSet().size());
            System.out.println("  - Resources: " + xmlData.getSharedResourceSet().size());
            System.out.println("  - Base Speed: " + xmlData.getTaskSet().getProcessingSpeed());
        } catch (Exception e) {
            System.out.println("✗ XML  failed: " + e.getMessage());
        }
        
        // Load JSON Workload
        try {
            long start = System.nanoTime();
            JSONDataReader jsonReader = new JSONDataReader();
            jsonReader.loadSource(jsonWorkload);
            jsonData = jsonReader.getDataSetting();
            jsonTime = System.nanoTime() - start;
            
            System.out.println("✓ JSON loaded: " + String.format("%.3f ms", jsonTime / 1000000.0));
            System.out.println("  - Tasks: " + jsonData.getTaskSet().size());
            System.out.println("  - Resources: " + jsonData.getSharedResourceSet().size());
            System.out.println("  - Base Speed: " + jsonData.getTaskSet().getProcessingSpeed());
        } catch (Exception e) {
            System.out.println("✗ JSON failed: " + e.getMessage());
        }
        
        // Compare performance
        if (xmlTime > 0 && jsonTime > 0) {
            double speedup = (double) xmlTime / jsonTime;
            System.out.println("\n⚡ Performance: JSON is " + String.format("%.2fx", speedup) + " faster");
        }
        
        // Verify data consistency
        if (xmlData != null && jsonData != null) {
            boolean match = compareWorkloadData(xmlData, jsonData);
            if (match) {
                System.out.println("✓ Data verification: PASSED (identical results)");
            } else {
                System.out.println("✗ Data verification: FAILED (differences found)");
            }
        }
        
        // Test Processor
        System.out.println("\n🖥️  PROCESSOR Comparison:");
        System.out.println("─────────────────────────────────────────");
        
        DataSetting xmlProc = null;
        DataSetting jsonProc = null;
        long xmlProcTime = 0, jsonProcTime = 0;
        
        // Load XML Processor
        try {
            long start = System.nanoTime();
            DataReader xmlReader = new DataReader();
            xmlReader.loadSource(xmlProcessor);
            xmlProc = xmlReader.getDataSetting();
            xmlProcTime = System.nanoTime() - start;
            
            System.out.println("✓ XML  loaded: " + String.format("%.3f ms", xmlProcTime / 1000000.0));
            System.out.println("  - Model: " + xmlProc.getProcessor().getModelName());
            System.out.println("  - Cores: " + xmlProc.getProcessor().getAllCore().size());
            System.out.println("  - DVFS: " + xmlProc.getProcessor().getDynamicVoltageRegulator().getDVFSType());
        } catch (Exception e) {
            System.out.println("✗ XML  failed: " + e.getMessage());
        }
        
        // Load JSON Processor
        try {
            long start = System.nanoTime();
            JSONDataReader jsonReader = new JSONDataReader();
            jsonReader.loadSource(jsonProcessor);
            jsonProc = jsonReader.getDataSetting();
            jsonProcTime = System.nanoTime() - start;
            
            System.out.println("✓ JSON loaded: " + String.format("%.3f ms", jsonProcTime / 1000000.0));
            System.out.println("  - Model: " + jsonProc.getProcessor().getModelName());
            System.out.println("  - Cores: " + jsonProc.getProcessor().getAllCore().size());
            System.out.println("  - DVFS: " + jsonProc.getProcessor().getDynamicVoltageRegulator().getDVFSType());
        } catch (Exception e) {
            System.out.println("✗ JSON failed: " + e.getMessage());
        }
        
        // Compare performance
        if (xmlProcTime > 0 && jsonProcTime > 0) {
            double speedup = (double) xmlProcTime / jsonProcTime;
            System.out.println("\n⚡ Performance: JSON is " + String.format("%.2fx", speedup) + " faster");
        }
        
        // Verify processor data
        if (xmlProc != null && jsonProc != null) {
            boolean match = compareProcessorData(xmlProc, jsonProc);
            if (match) {
                System.out.println("✓ Data verification: PASSED (identical results)");
            } else {
                System.out.println("✗ Data verification: FAILED (differences found)");
            }
        }
    }
    
    private static boolean compareWorkloadData(DataSetting xml, DataSetting json) {
        if (xml.getTaskSet().size() != json.getTaskSet().size()) return false;
        if (xml.getSharedResourceSet().size() != json.getSharedResourceSet().size()) return false;
        if (xml.getTaskSet().getProcessingSpeed() != json.getTaskSet().getProcessingSpeed()) return false;
        
        // Compare each task
        for (int i = 0; i < xml.getTaskSet().size(); i++) {
            Task xmlTask = xml.getTaskSet().get(i);
            Task jsonTask = json.getTaskSet().get(i);
            
            if (xmlTask.getID() != jsonTask.getID()) return false;
            if (xmlTask.getPeriod() != jsonTask.getPeriod()) return false;
            if (xmlTask.getRelativeDeadline() != jsonTask.getRelativeDeadline()) return false;
            if (xmlTask.getComputationAmount() != jsonTask.getComputationAmount()) return false;
        }
        
        return true;
    }
    
    private static boolean compareProcessorData(DataSetting xml, DataSetting json) {
        if (!xml.getProcessor().getModelName().equals(json.getProcessor().getModelName())) return false;
        if (xml.getProcessor().getAllCore().size() != json.getProcessor().getAllCore().size()) return false;
        if (!xml.getProcessor().getDynamicVoltageRegulator().getDVFSType()
                .equals(json.getProcessor().getDynamicVoltageRegulator().getDVFSType())) return false;
        
        return true;
    }
}
