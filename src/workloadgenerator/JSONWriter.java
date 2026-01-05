/*
 * JSON Writer for workload generation
 */
package workloadgenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Utility to build a JSON document that represents a generated workload.
 *
 * @author mingfa
 */
public class JSONWriter {
    
    /**
     * Create a new JSONWriter instance.
     */
    public JSONWriter() {
    }

    /**
     * Build a JSON string for the supplied workload.
     *
     * @param w the workload model to export
     * @return a JSON string containing the workload
     */
    public static String createJSON(wgWorkload w) {
        JsonObject workloadObj = new JsonObject();
        
        // Root workload attributes
        workloadObj.addProperty("workload", w.getWorkloadHeader());
        workloadObj.addProperty("maximumUtilization", w.getMaximumUtilization());
        workloadObj.addProperty("actualUtilization", w.getActualUtilization());
        workloadObj.addProperty("maximumCriticalSectionRatio", w.getMaxCriticalSectionRatio());
        workloadObj.addProperty("actualCriticalSectionRatio", w.getActualCriticalSectionRatio());
        workloadObj.addProperty("taskNumber", w.getTaskNumber());
        workloadObj.addProperty("resourcesNumber", w.getResourcesNumber());
        workloadObj.addProperty("baseSpeed", w.getFrequency());
        
        // Resources array
        JsonArray resourcesArray = new JsonArray();
        for (wgResources r : w.getResourcesSet()) {
            JsonObject resourceObj = new JsonObject();
            resourceObj.addProperty("ID", r.getID());
            resourceObj.addProperty("quantity", r.getResourceAmount());
            resourcesArray.add(resourceObj);
        }
        workloadObj.add("resources", resourcesArray);
        
        // Tasks array
        JsonArray tasksArray = new JsonArray();
        for (wgTask t : w.getTaskSet()) {
            JsonObject taskObj = new JsonObject();
            taskObj.addProperty("ID", t.getID());
            taskObj.addProperty("arrivalTime", t.exporeEnterTime());
            taskObj.addProperty("period", t.exporePeriod());
            taskObj.addProperty("relativeDeadline", t.exporeRelativeDeadline());
            taskObj.addProperty("computationAmount", t.exporeComputationAmount());
            
            // Critical sections
            if (t.getCriticalSectionSet().size() > 0) {
                JsonArray criticalSectionsArray = new JsonArray();
                for (wgCriticalSection cs : t.getCriticalSectionSet()) {
                    JsonObject csObj = new JsonObject();
                    csObj.addProperty("resourceID", cs.getResources().getID());
                    csObj.addProperty("startTime", cs.exporeStartTime());
                    csObj.addProperty("endTime", cs.exporeEndTime());
                    criticalSectionsArray.add(csObj);
                }
                taskObj.add("criticalSections", criticalSectionsArray);
            }
            
            tasksArray.add(taskObj);
        }
        workloadObj.add("tasks", tasksArray);
        
        // Pretty print JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(workloadObj);
    }
}
