/*
 * JSON Data Reader for RTSimulator
 * Reads simulator configuration from JSON files
 */
package SystemEnvironment;

import WorkLoad.CoreSpeed;
import WorkLoad.CriticalSection;
import WorkLoad.SharedResource;
import WorkLoad.Task;
import WorkLoadSet.CoreSet;
import WorkLoadSet.DataSetting;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import RTSimulator.Definition.DVFSType;
import static RTSimulator.Definition.magnificationFactor;
import static RTSimulator.RTSimulator.println;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Reads simulator configuration from JSON and builds an in-memory {@link DataSetting}.
 *
 * <p>This reader supports two root document types:
 * <ul>
 *   <li><b>workload</b> – parses tasks, critical sections and shared resources</li>
 *   <li><b>processor</b> – parses the processor model, DVFS type, available core
 *       speeds and the power-consumption function</li>
 * </ul>
 *
 * @author mingfa
 */
public class JSONDataReader {
    private DataSetting dataSetting;
    private JsonObject root;
    
    /**
     * Creates a new JSON reader with an empty {@link DataSetting} container.
     */
    public JSONDataReader() {
        this.dataSetting = new DataSetting();
    }
    
    /**
     * Loads and parses a JSON source file, then builds the corresponding data
     * structures into this reader's {@link DataSetting}.
     *
     * @param sourcePath path to the JSON file
     * @throws Exception if the file cannot be read or the JSON cannot be parsed
     */
    public void loadSource(String sourcePath) throws Exception {
        Gson gson = new Gson();
        FileReader reader = new FileReader(sourcePath);
        this.root = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();
        
        if (this.root.has("workload") || this.root.has("maximumUtilization")) {
            this.createWorkload();
        } else if (this.root.has("processor") || this.root.has("model")) {
            this.createProcessor();
        } else {
            println("You loaded source is not supported (It's not Workload or Processor)");
        }
    }
    
    /*Workload*/
    private void createWorkload() {
        if (root.has("baseSpeed")) {
            this.dataSetting.getTaskSet().setProcessingSpeed(root.get("baseSpeed").getAsDouble());
        }
        
        try {
            if (root.has("maximumCriticalSectionRatio")) {
                this.dataSetting.getTaskSet().setMaximumCriticalSectionRatio(
                    root.get("maximumCriticalSectionRatio").getAsDouble());
            }
            if (root.has("actualCriticalSectionRatio")) {
                this.dataSetting.getTaskSet().setActualCriticalSectionRatio(
                    root.get("actualCriticalSectionRatio").getAsDouble());
            }
            if (root.has("maximumUtilization")) {
                this.dataSetting.getTaskSet().setMaximumUtilization(
                    root.get("maximumUtilization").getAsDouble());
            }
            if (root.has("actualUtilization")) {
                this.dataSetting.getTaskSet().setActualUtilization(
                    root.get("actualUtilization").getAsDouble());
            }
        } catch (Exception e) {
            println("Warning: Some workload attributes are missing");
        }
        
        // Process resources
        if (root.has("resources")) {
            JsonArray resourcesArray = root.getAsJsonArray("resources");
            for (JsonElement resElement : resourcesArray) {
                this.createSharedResource(resElement.getAsJsonObject());
            }
        }
        
        // Process tasks
        if (root.has("tasks")) {
            JsonArray tasksArray = root.getAsJsonArray("tasks");
            for (JsonElement taskElement : tasksArray) {
                this.createTask(taskElement.getAsJsonObject());
            }
        }
        
        this.dataSetting.getTaskSet().setNestSetForTask();
    }
    
    private void createSharedResource(JsonObject resObj) {
        SharedResource sharedResource = new SharedResource();
        sharedResource.setID(resObj.get("ID").getAsInt());
        sharedResource.createResources(resObj.get("quantity").getAsInt());
        this.dataSetting.addSharedResource(sharedResource);
    }
    
    private void createTask(JsonObject taskObj) {
        Task task = new Task();
        task.setID(taskObj.get("ID").getAsInt());
        
        // Handle priority level
        String taskPriority = taskObj.has("priorityLevel") ? 
            taskObj.get("priorityLevel").getAsString() : "1";
        
        JsonObject selectedPriorityObj = null;
        if (taskObj.has("priorityLevels")) {
            JsonArray priorityLevels = taskObj.getAsJsonArray("priorityLevels");
            for (JsonElement levelElement : priorityLevels) {
                JsonObject levelObj = levelElement.getAsJsonObject();
                if (levelObj.get("value").getAsString().equals(taskPriority)) {
                    selectedPriorityObj = levelObj;
                    break;
                }
            }
        }
        
        // Handle criticality level
        String criticalityLevelValue = taskObj.has("criticalityLevel") ? 
            taskObj.get("criticalityLevel").getAsString() : null;
        JsonObject selectedCriticalityObj = null;
        
        if (taskObj.has("criticalityLevels")) {
            JsonArray criticalityLevels = taskObj.getAsJsonArray("criticalityLevels");
            
            List<Long> periodList = new ArrayList<>();
            List<Long> relativeDeadLineList = new ArrayList<>();
            List<Long> computationAmountList = new ArrayList<>();
            List<Long> criticalityLevelList = new ArrayList<>();
            
            for (JsonElement levelElement : criticalityLevels) {
                JsonObject levelObj = levelElement.getAsJsonObject();
                
                if (levelObj.has("period")) {
                    periodList.add((long)(levelObj.get("period").getAsDouble() * magnificationFactor));
                }
                if (levelObj.has("relativeDeadline")) {
                    relativeDeadLineList.add((long)(levelObj.get("relativeDeadline").getAsDouble() * magnificationFactor));
                }
                if (levelObj.has("computationAmount")) {
                    computationAmountList.add((long)(levelObj.get("computationAmount").getAsDouble() * magnificationFactor));
                }
                if (levelObj.has("value")) {
                    criticalityLevelList.add((long)(levelObj.get("value").getAsDouble() * magnificationFactor));
                }
                
                if (criticalityLevelValue != null && 
                    levelObj.get("value").getAsString().equals(criticalityLevelValue)) {
                    selectedCriticalityObj = levelObj;
                }
            }
            
            task.setPeriodList(periodList);
            task.setComputationAmountList(computationAmountList);
            task.setRelativeDeadlineList(relativeDeadLineList);
            task.setCriticalityLevelList(criticalityLevelList);
        }
        
        // Set task parameters based on selected priority/criticality level
        if (selectedPriorityObj != null) {
            task.setPriorityLevel(Integer.parseInt(taskPriority));
            setTaskParameters(task, selectedPriorityObj);
        } else if (selectedCriticalityObj != null) {
            setTaskParameters(task, selectedCriticalityObj);
            if (selectedCriticalityObj.has("value")) {
                task.setCriticalityLevel((long)(selectedCriticalityObj.get("value").getAsDouble() * magnificationFactor));
            }
        } else {
            // Default parameters
            if (taskObj.has("arrivalTime")) {
                task.setEnterTime((long)(taskObj.get("arrivalTime").getAsDouble() * magnificationFactor));
            }
            if (taskObj.has("period")) {
                task.setPeriod((long)(taskObj.get("period").getAsDouble() * magnificationFactor));
            }
            if (taskObj.has("relativeDeadline")) {
                task.setRelativeDeadline((long)(taskObj.get("relativeDeadline").getAsDouble() * magnificationFactor));
            }
            if (taskObj.has("computationAmount")) {
                task.setComputationAmount((long)(taskObj.get("computationAmount").getAsDouble() * magnificationFactor));
            }
        }
        
        // Handle critical sections
        if (taskObj.has("criticalSections")) {
            JsonArray criticalSections = taskObj.getAsJsonArray("criticalSections");
            for (JsonElement csElement : criticalSections) {
                JsonObject csObj = csElement.getAsJsonObject();
                CriticalSection cs = new CriticalSection();
                
                int resourceID = csObj.get("resourceID").getAsInt();
                cs.setUseSharedResource(this.dataSetting.getSharedResource(resourceID - 1));
                this.dataSetting.getSharedResource(resourceID - 1).addAccessTask(task);
                cs.setRelativeStartTime((long)(csObj.get("startTime").getAsDouble() * magnificationFactor));
                cs.setRelativeEndTime((long)(csObj.get("endTime").getAsDouble() * magnificationFactor));
                task.addCriticalSection(cs);
            }
        }
        
        task.setTotalCriticalSectionTime();
        task.setParentTaskSet(this.dataSetting.getTaskSet());
        this.dataSetting.addTask(task);
    }
    
    private void setTaskParameters(Task task, JsonObject paramObj) {
        if (paramObj.has("arrivalTime")) {
            task.setEnterTime((long)(paramObj.get("arrivalTime").getAsDouble() * magnificationFactor));
        } else {
            task.setEnterTime(0L);
        }
        
        if (paramObj.has("period")) {
            task.setPeriod((long)(paramObj.get("period").getAsDouble() * magnificationFactor));
        } else {
            task.setPeriod(0L);
        }
        
        if (paramObj.has("relativeDeadline")) {
            task.setRelativeDeadline((long)(paramObj.get("relativeDeadline").getAsDouble() * magnificationFactor));
        } else {
            task.setRelativeDeadline(0L);
        }
        
        if (paramObj.has("computationAmount")) {
            task.setComputationAmount((long)(paramObj.get("computationAmount").getAsDouble() * magnificationFactor));
        } else {
            task.setComputationAmount(0L);
        }
    }
    
    /*Processor*/
    private void createProcessor() {
        if (root.has("model")) {
            this.dataSetting.getProcessor().setModelName(root.get("model").getAsString());
        }
        if (root.has("DVFStype")) {
            this.dataSetting.getProcessor().getDynamicVoltageRegulator()
                .setDVFSType(root.get("DVFStype").getAsString());
        }
        
        if (root.has("cores")) {
            JsonArray coresArray = root.getAsJsonArray("cores");
            for (JsonElement coreElement : coresArray) {
                JsonObject coreObj = coreElement.getAsJsonObject();
                createCoreSet(coreObj);
            }
        }
    }
    
    private void createCoreSet(JsonObject coreObj) {
        CoreSet coreSet = new CoreSet();
        
        if (coreObj.has("type")) {
            coreSet.setCoreType(coreObj.get("type").getAsString());
        }
        
        if (coreObj.has("quantity")) {
            int quantity = coreObj.get("quantity").getAsInt();
            for (int i = 0; i < quantity; i++) {
                Core core = new Core();
                core.setParentProcessor(this.dataSetting.getProcessor());
                core.setParentCoreSet(coreSet);
                coreSet.addCore(core);
                this.dataSetting.getProcessor().addCore(core);
            }
        }
        
        // Available speeds
        if (coreObj.has("availableSpeeds")) {
            JsonArray speedsArray = coreObj.getAsJsonArray("availableSpeeds");
            for (JsonElement speedElement : speedsArray) {
                JsonObject speedObj = speedElement.getAsJsonObject();
                CoreSpeed s = new CoreSpeed();
                
                if (speedObj.has("speed")) {
                    String speedStr = speedObj.get("speed").getAsString();
                    if (!speedStr.equals("idle")) {
                        s.setSpeed(Double.valueOf(speedStr));
                    } else {
                        s.setSpeed(0);
                    }
                }
                
                if (speedObj.has("powerConsumption")) {
                    s.setPowerConsumption(speedObj.get("powerConsumption").getAsDouble());
                }
                
                coreSet.addCoreSpeed(s);
            }
        }
        
        // Power consumption function
        if (coreObj.has("powerConsumptionFunction")) {
            JsonObject pcfObj = coreObj.getAsJsonObject("powerConsumptionFunction");
            if (pcfObj.has("alpha")) {
                coreSet.setAlphaValue(pcfObj.get("alpha").getAsDouble());
            }
            if (pcfObj.has("beta")) {
                coreSet.setBetaValue(pcfObj.get("beta").getAsDouble());
            }
            if (pcfObj.has("gamma")) {
                coreSet.setGammaValue(pcfObj.get("gamma").getAsDouble());
            }
        }
        
        // Handle DVFS type
        if (this.dataSetting.getProcessor().getDynamicVoltageRegulator()
                .getDVFSType().equals(DVFSType.PerCore)) {
            for (int i = 0; i < coreSet.size(); i++) {
                CoreSet cSet = new CoreSet(coreSet);
                cSet.addCore(coreSet.getCore(i));
                this.dataSetting.getProcessor().addCoreSet(cSet);
            }
        } else {
            this.dataSetting.getProcessor().addCoreSet(coreSet);
        }
    }
    
    /**
     * Returns the fully built {@link DataSetting} after a successful call to
     * {@link #loadSource(String)}.
     *
     * @return the current {@link DataSetting} instance (never {@code null})
     */
    public DataSetting getDataSetting() {
        return this.dataSetting;
    }
}
