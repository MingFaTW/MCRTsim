/*
 * Test program to compare XML and JSON data reading
 */
package test;

import SystemEnvironment.DataReader;
import SystemEnvironment.JSONDataReader;
import WorkLoadSet.DataSetting;
import WorkLoad.Task;

/**
 * Simple test to compare XML vs JSON data loading.
 * 
 * @author mingfa
 */
public class DataReaderComparisonTest {
    
    public static void main(String[] args) {
        System.out.println("=== XML vs JSON Data Reader Comparison Test ===\n");
        
        // Test workload files
        String xmlWorkloadPath = "RTSimulator_examples/Single_Core Scheduling Example/workload.xml";
        String jsonWorkloadPath = "RTSimulator_examples/JSON_Example/workload.json";
        
        // Test processor files
        String xmlProcessorPath = "RTSimulator_examples/Single_Core Scheduling Example/processor.xml";
        String jsonProcessorPath = "RTSimulator_examples/JSON_Example/processor.json";
        
        // Test XML workload loading
        System.out.println("1. Loading XML Workload...");
        long xmlWorkloadStart = System.nanoTime();
        try {
            DataReader xmlReader = new DataReader();
            xmlReader.loadSource(xmlWorkloadPath);
            DataSetting xmlData = xmlReader.getDataSetting();
            long xmlWorkloadEnd = System.nanoTime();
            
            System.out.println("   OK XML Workload loaded successfully");
            System.out.println("   - Task count: " + xmlData.getTaskSet().size());
            System.out.println("   - Resource count: " + xmlData.getSharedResourceSet().size());
            System.out.println("   - Base speed: " + xmlData.getTaskSet().getProcessingSpeed());
            System.out.println("   - Loading time: " + (xmlWorkloadEnd - xmlWorkloadStart) / 1000000.0 + " ms\n");
            
            // Display task details
            for (Task t : xmlData.getTaskSet()) {
                System.out.println("   Task " + t.getID() + ": Period=" + t.getPeriod() + 
                                 ", Deadline=" + t.getRelativeDeadline() + 
                                 ", CompTime=" + t.getComputationAmount());
            }
        } catch (Exception e) {
            System.out.println("   Error loading XML workload: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n2. Loading JSON Workload...");
        long jsonWorkloadStart = System.nanoTime();
        try {
            JSONDataReader jsonReader = new JSONDataReader();
            jsonReader.loadSource(jsonWorkloadPath);
            DataSetting jsonData = jsonReader.getDataSetting();
            long jsonWorkloadEnd = System.nanoTime();
            
            System.out.println("   OK JSON Workload loaded successfully");
            System.out.println("   - Task count: " + jsonData.getTaskSet().size());
            System.out.println("   - Resource count: " + jsonData.getSharedResourceSet().size());
            System.out.println("   - Base speed: " + jsonData.getTaskSet().getProcessingSpeed());
            System.out.println("   - Loading time: " + (jsonWorkloadEnd - jsonWorkloadStart) / 1000000.0 + " ms\n");
            
            // Display task details
            for (Task t : jsonData.getTaskSet()) {
                System.out.println("   Task " + t.getID() + ": Period=" + t.getPeriod() + 
                                 ", Deadline=" + t.getRelativeDeadline() + 
                                 ", CompTime=" + t.getComputationAmount());
            }
        } catch (Exception e) {
            System.out.println("   Error loading JSON workload: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test XML processor loading
        System.out.println("\n3. Loading XML Processor...");
        long xmlProcessorStart = System.nanoTime();
        try {
            DataReader xmlReader = new DataReader();
            xmlReader.loadSource(xmlProcessorPath);
            DataSetting xmlData = xmlReader.getDataSetting();
            long xmlProcessorEnd = System.nanoTime();
            
            System.out.println("   OK XML Processor loaded successfully");
            System.out.println("   - Model: " + xmlData.getProcessor().getModelName());
            System.out.println("   - Core count: " + xmlData.getProcessor().getAllCore().size());
            System.out.println("   - DVFS type: " + xmlData.getProcessor().getDynamicVoltageRegulator().getDVFSType());
            System.out.println("   - Loading time: " + (xmlProcessorEnd - xmlProcessorStart) / 1000000.0 + " ms");
        } catch (Exception e) {
            System.out.println("   Error loading XML processor: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test JSON processor loading
        System.out.println("\n4. Loading JSON Processor...");
        long jsonProcessorStart = System.nanoTime();
        try {
            JSONDataReader jsonReader = new JSONDataReader();
            jsonReader.loadSource(jsonProcessorPath);
            DataSetting jsonData = jsonReader.getDataSetting();
            long jsonProcessorEnd = System.nanoTime();
            
            System.out.println("   OK JSON Processor loaded successfully");
            System.out.println("   - Model: " + jsonData.getProcessor().getModelName());
            System.out.println("   - Core count: " + jsonData.getProcessor().getAllCore().size());
            System.out.println("   - DVFS type: " + jsonData.getProcessor().getDynamicVoltageRegulator().getDVFSType());
            System.out.println("   - Loading time: " + (jsonProcessorEnd - jsonProcessorStart) / 1000000.0 + " ms");
        } catch (Exception e) {
            System.out.println("   Error loading JSON processor: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Test Complete ===");
    }
}
