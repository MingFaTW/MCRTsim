package schedulingAlgorithm.implementation;

import WorkLoadSet.JobQueue;
import WorkLoadSet.TaskSet;
import RTSimulator.Definition;
import schedulingAlgorithm.SingleCoreSchedulingAlgorithm;
import java.util.ArrayList;
import java.util.List;
import WorkLoad.Priority;
import WorkLoad.Task;
/*
 * Similiar to OPA algorithm, adding criticality level after RTA calculation.
 * In this version, i only used 1 and 2 to represent Low and High Criticality (Limitation).
 * author mingfa
 */
/**
 * Static Mixed Criticality (SMC) scheduling algorithm.
 * 
 * @author mingfa
 */
public class SMC extends SingleCoreSchedulingAlgorithm{
		
	/**
	 * Constructs a new SMC instance.
	 */
	public SMC() {
        this.setName("Static Mixed Criticality (SMC)");
        this.setPriorityType(Definition.PriorityType.Fixed);
	}
	
	@Override
	public void calculatePriority(TaskSet ts) {
	    int numTasks = ts.size();
	    boolean[] assigned = new boolean[numTasks]; 
	    Priority[] priorities = new Priority[numTasks];
	    int priorityRiseTaskIndex[] = new int[numTasks];
	    ArrayList<Double> responseTimeList = new ArrayList<Double>();
	    for (int priorityIndex = 1; priorityIndex <= numTasks; priorityIndex++) { 
	        Task bestTask = null;
	        int bestTaskIndex = -1;
	        
	        Boolean isAssignBoolean = false;
	        for (int i = 0; i < numTasks; i++) {
	            Task t = ts.get(i);
	            if (!assigned[i] && isSchedulableWithPriority(ts, t, priorityIndex, assigned, responseTimeList)) {          
                    bestTask = t;
                    bestTaskIndex = i;
                    isAssignBoolean = true;
	            }
	            if(isAssignBoolean)
	            	break;
	        }
	
	        // 檢查是否找到合適的任務
	        if (bestTask == null) {
	            System.out.println("No schedulable task found for priority level " + priorityIndex);
	            continue;
	        }
	
	        assigned[bestTaskIndex] = true;
	        priorities[bestTaskIndex] = new Priority(priorityIndex);
	        
	        System.out.println("Task " + bestTask.getID() + " assigned priority: " + priorityIndex);
	    }   
	
	    // 設定優先權
	    for (int i = 0; i < numTasks; i++) {
	        ts.get(i).setPriority(priorities[i]);
	    }
	
	    // 設定優先權
	    for (int i = 0; i < numTasks; i++) {
	        ts.get(i).setPriority(priorities[i]);
	    }

	    for (Task t : ts) {
	        if (isAssignedWithCriticality(ts, t, responseTimeList)) {
	            t.setCriticalityLevel(1L);  // 設定為 High Criticality
	        } else {
	            t.setCriticalityLevel(0L);  // 設定為 Low Criticality
	        }
	        System.out.println("Task " + t.getID() + " assigned Criticality Level: " + t.getCriticalityLevel());
	    }
	    
	    // 依據priority的大小排序工作，由大到小儲存在priorityRiseTaskIndex裡面
	    for (int i = 0; i < numTasks; i++) {
	        priorityRiseTaskIndex[i] = i;
	    }

	    for (int i = 0; i < numTasks - 1; i++) {
	        for (int j = 0; j < numTasks - 1 - i; j++) {
	            if (priorities[j].getValue() < priorities[j + 1].getValue()) {
	                Priority tempPriority = priorities[j];
	                priorities[j] = priorities[j + 1];
	                priorities[j + 1] = tempPriority;

	                int tempIndex = priorityRiseTaskIndex[j];
	                priorityRiseTaskIndex[j] = priorityRiseTaskIndex[j + 1];
	                priorityRiseTaskIndex[j + 1] = tempIndex;
	            }
	        }
	    }
	    
	    for (int i = 0; i < numTasks; i++) {  
	    	if (i==numTasks-1)
	    		break;
	    	if (ts.get(priorityRiseTaskIndex[i]).getCriticalityLevel()>ts.get(priorityRiseTaskIndex[i+1]).getCriticalityLevel()) {
	    		ts.get(priorityRiseTaskIndex[i]).setComputationAmount(ts.get(priorityRiseTaskIndex[i+1]).getComputationAmount());
	    	} else if (ts.get(priorityRiseTaskIndex[i]).getCriticalityLevel()<ts.get(priorityRiseTaskIndex[i+1]).getCriticalityLevel()) {
	    		ts.get(priorityRiseTaskIndex[i]).setComputationAmount(ts.get(priorityRiseTaskIndex[i+1]).getComputationAmount());
	    	}
	    }

//	    for (int index = 0; index < numTasks; index++) {
//	        System.out.println("Task " + priorityRiseTaskIndex[index] + " has priority: " + priorities[index].getValue());
//	    }
	    
	    
	    for (Task task : ts) {
	        System.out.println("Task " + task.getID() + " has final priority: " + task.getPriority().getValue());
	    }
	}

	
	private boolean isSchedulableWithPriority(TaskSet ts, Task t, int priorityIndex, boolean[] assigned, List<Double> responseTimeList) {
		
		System.out.println("現在Task("+t.getID()+")，確認是否可排程中");
	    double responseTime = 0;
	    double previousResponseTime = 0;
	    double totalComputationTime = 0;
	    // 只考慮尚未分配優先權的任務
	    for (int i = 0; i < ts.size(); i++) {
	        if (!assigned[i]) { // 只考慮尚未指派優先權的任務
	        	totalComputationTime += ts.get(i).getComputationAmount();
	        }
	    }
	
	    // 如果所有其他尚未指派優先權的任務總計計算時間超過當前任務的週期，則無法排程
	    if (totalComputationTime > t.getRelativeDeadline()) {
	    	System.out.println("不可排程");
	        return false;
	    }
	
	    previousResponseTime = totalComputationTime;
	    boolean isNotConvergence = true;
	    int calculationTimes = 0;
	    // 計算回應時間，確認回應時間是否有收斂的情形
	    while(isNotConvergence) {
	    	++calculationTimes;
	    	System.out.println("第"+calculationTimes+"次計算");
		    for (int i = 0; i < ts.size(); i++) {
		        if (!assigned[i]) { // 只考慮尚未指派優先權的任務
		            responseTime += Math.ceil(previousResponseTime / ts.get(i).getPeriod()) * ts.get(i).getComputationAmount();
		        }
		    }
		    System.out.println("回應時間："+responseTime+",上次回應時間："+previousResponseTime+",截止時間："+t.getRelativeDeadline());
		    
		    if(responseTime == previousResponseTime) {
	    		System.out.println("RTA可排程");
	    		responseTimeList.add(responseTime);
	    		return true;
		    } else if(responseTime > t.getRelativeDeadline()) {
	    		System.out.println("RTA不可排程！");
	    		return false;
		    }
		    else {
		    	previousResponseTime = responseTime;
		    	responseTime = 0;
		    }
	    }
	    return false;
	}
	
	/*
	 * After RTA finished, starting to assigned the criticality Level for each task.
	 * */
	private boolean isAssignedWithCriticality(TaskSet ts, Task t, List<Double> responseTimeList) {
	    return responseTimeList.get(ts.indexOf(t)) <= t.getRelativeDeadline();
	}

//	private boolean isScheduledWithCriticality(TaskSet ts, Task t) {
//	    int numTasks = ts.size();
//
//	    for (int i = 0; i < numTasks; i++) {
//	        Task Ti = ts.get(i);
//
//	        for (int j = 0; j < i; j++) { 
//	            Task Tj = ts.get(j);
//
//	            if (Tj.getPriority().getValue() > Ti.getPriority().getValue()) {
//	                if (Tj.getCriticalityLevel() > Ti.getCriticalityLevel()) {
//
//	                } else if (Tj.getCriticalityLevel() < Ti.getCriticalityLevel()) {
//	       
//	                } else {
//	          
//	                }
//	            }
//	        }
//	    }
//
//	    return false; 
//	}
//	
    @Override
    public JobQueue calculatePriority(JobQueue jq) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
