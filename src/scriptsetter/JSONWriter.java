/*
 * JSON Writer for script results
 */
package scriptsetter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Utility class for writing JSON documents with statistical results.
 * 
 * @author mingfa
 */
public class JSONWriter {
    
    public JSONWriter() {
    }

    /**
     * Create a JSON string containing statistical results from ScriptSetter.
     *
     * @param ss the ScriptSetter containing the results to export
     * @return a JSON string with all statistical results
     */
    public static String createJSON(ScriptSetter ss) {
        JsonObject statisticalResult = new JsonObject();
        statisticalResult.addProperty("numGroup", ss.getTableTabbedPane().getComponentCount());
        
        JsonArray groupsArray = new JsonArray();
        
        for (int i = 0; i < ss.getTableTabbedPane().getComponentCount(); i++) {
            ScriptTable st = (ScriptTable)ss.getTableTabbedPane().getComponent(i);
            JsonObject groupObj = new JsonObject();
            groupObj.addProperty("groupID", st.getGroupID());
            groupObj.addProperty("numResult", st.getScriptCount());
            
            JsonArray resultsArray = new JsonArray();
            
            for (int j = 0; j < st.getScriptCount(); j++) {
                Script s = st.getScriptSet().get(j);
                JsonObject resultObj = new JsonObject();
                
                resultObj.addProperty("resultID", s.getID());
                resultObj.addProperty("maximumUtilization", s.getAverageMaximumUtilization());
                resultObj.addProperty("actualUtilization", s.getAverageActualUtilization());
                resultObj.addProperty("maximumCriticalSectionRatio", s.getAverageMaximumCriticalSectionRatio());
                resultObj.addProperty("actualCriticalSectionRatio", s.getAverageActualCriticalSectionRatio());
                resultObj.addProperty("partitionAlgorithm", s.getPartitionAlgorithm());
                resultObj.addProperty("DVFSMethod", s.getDVFSMethod());
                resultObj.addProperty("schedulingAlgorithm", s.getSchedulingAlgorithm());
                resultObj.addProperty("CCProtocol", s.getCCProtocol());
                resultObj.addProperty("simulationTime", s.getSimulationTime());
                resultObj.addProperty("workloadCount", s.getWorkloadCount());
                resultObj.addProperty("taskCount", s.getAverageTaskCount());
                resultObj.addProperty("schedulableCount", s.getSchedulableCount());
                resultObj.addProperty("nonSchedulableCount", s.getNonSchedulableCount());
                resultObj.addProperty("powerConsumption", s.getAveragePowerConsumption());
                resultObj.addProperty("completedRatio", s.getAverageCompletedRatio());
                resultObj.addProperty("deadlineMissRatio", s.getAverageDeadlineMissRatio());
                resultObj.addProperty("beBlockedTimeRatio", s.getAverageActualBeBlockedTimeRatio());
                resultObj.addProperty("averagePendingTime", s.getAveragePendingTime());
                resultObj.addProperty("averageResponseTime", s.getAverageResponseTime());
                
                resultsArray.add(resultObj);
            }
            
            groupObj.add("results", resultsArray);
            groupsArray.add(groupObj);
        }
        
        statisticalResult.add("groups", groupsArray);
        
        // Pretty print JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(statisticalResult);
    }
}
