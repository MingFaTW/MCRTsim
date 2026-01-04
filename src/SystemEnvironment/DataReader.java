/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SystemEnvironment;

import WorkLoad.CoreSpeed;
import WorkLoad.CriticalSection;
import WorkLoad.PriorityLevel;
import WorkLoad.SharedResource;
import WorkLoad.Task;
import WorkLoadSet.CoreSet;
import WorkLoadSet.DataSetting;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import RTSimulator.Definition.DVFSType;
import static RTSimulator.Definition.magnificationFactor;
import static RTSimulator.RTSimulator.println;
import RTSimulator.RTSimulatorMath;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Reads simulator configuration from XML and builds an in-memory {@link DataSetting}.
 *
 * <p>This reader supports two root document types:
 * <ul>
 *   <li><b>workload</b> – parses tasks, critical sections and shared resources
 *       from a workload.xml and populates the TaskSet (integrating attributes
 *       such as baseSpeed, utilization ratios and critical-section ratios);</li>
 *   <li><b>processor</b> – parses the processor model, DVFS type, available core
 *       speeds and the power-consumption function and creates {@link CoreSet}s
 *       and cores under the Processor.</li>
 * </ul>
 *
 * <p>The original inline comments have been integrated: you can either edit the
 * XML to change task properties or read them to instantiate tasks; critical
 * sections and shared resources are wired accordingly; for a processor file the
 * DVFS type controls whether a per-core CoreSet is created.</p>
 *
 * @author ShiuJia, mingfa
 */
public class DataReader
{
    private DataSetting dataSetting;
    private Element root;
    
    /**
     * Creates a new reader with an empty {@link DataSetting} container that will
     * be populated after calling {@link #loadSource(String)}.
     */
    public DataReader()
    {
        this.dataSetting = new DataSetting();
    }
    
    /**
     * Loads and parses an XML source file, then builds the corresponding data
     * structures into this reader's {@link DataSetting}.
     *
     * <p>Behavior depends on the XML root element name:</p>
     * <ul>
     *   <li><b>workload</b>: creates tasks (including optional priority/criticality
     *       variants), critical sections and shared resources;</li>
     *   <li><b>processor</b>: creates the processor model, core sets, available
     *       speeds and power-consumption function, honoring the configured DVFS type;</li>
     *   <li>otherwise: prints a warning that the source is not supported.</li>
     * </ul>
     *
     * @param sourcePath path to the XML file (absolute or resolvable by dom4j)
     * @throws DocumentException if the file cannot be read or the XML cannot be parsed
     */
    public void loadSource(String sourcePath) throws DocumentException
    {
        SAXReader reader = new SAXReader();
        Document document = reader.read(sourcePath);
        
        this.root = document.getRootElement();
        switch(this.root.getName())
        {
            case "workload":
            {
                this.createWorkload();
                break;
            }
            case "processor":
            {
                this.createProcessor();
                break;
            }
            default:
            {
                println("You loaded source is not supported(It's not Workload or Processor):" + this.root.getName().toString());
            }
        }
    }
    
    /*Workload*/
    private void createWorkload()
    {
        Iterator it = root.elementIterator();
        this.dataSetting.getTaskSet().setProcessingSpeed(Double.valueOf(this.root.attribute("baseSpeed").getText()));
        try
        {
            this.dataSetting.getTaskSet().setMaximumCriticalSectionRatio(Double.valueOf(this.root.attribute("maximumCriticalSectionRatio").getText()));
            this.dataSetting.getTaskSet().setActualCriticalSectionRatio(Double.valueOf(this.root.attribute("actualCriticalSectionRatio").getText()));
            this.dataSetting.getTaskSet().setMaximumUtilization(Double.valueOf(this.root.attribute("maximumUtilization").getText()));
            this.dataSetting.getTaskSet().setActualUtilization(Double.valueOf(this.root.attribute("actualUtilization").getText()));
        }
        catch(Exception e)
        {
            
        }
        
        
        while(it.hasNext())
        {
            Element typeElement = (Element)it.next();
            
            switch(typeElement.getQualifiedName())
            {
                case "task":
                {
                    this.createTask(typeElement);
                    break;
                }
                
                case "resources":
                {
                    this.createSharedResource(typeElement);
                    break;
                }
            }
        }
        
        this.dataSetting.getTaskSet().setNestSetForTask();
    }
    
    private void createSharedResource(Element re)
    {
        SharedResource sharedResource = new SharedResource();
        sharedResource.setID(Integer.valueOf(re.attribute("ID").getText()));
        sharedResource.createResources(Integer.valueOf(re.attribute("quantity").getText()));
        this.dataSetting.addSharedResource(sharedResource);
    }
    
    
    
    private void createTask(Element te) {
        Task task = new Task();
        task.setID(Integer.valueOf(te.attribute("ID").getText()));
        
        // 讀取priorityLevel的區段
        String taskPriority = null;
        String priorityLevelValue = te.attributeValue("priorityLevel");
        if (priorityLevelValue == null) {
            priorityLevelValue = "1"; // initial value
        }

        taskPriority = te.attributeValue("priorityLevel", priorityLevelValue);
        Element selectedPriorityElement = null;  
        // 選擇與 task priorityLevel 相對應的參數來進行設定（可以參考OPA演算法中的程式碼）
        List<Element> priorityLevelElements = null;
        if(te.elements("priorityLevel")!=null) {
        	priorityLevelElements = te.elements("priorityLevel");
        }
        if (priorityLevelElements != null) {
            for (Element levelNum : priorityLevelElements) {
                if(levelNum.attributeValue("value").equals(taskPriority)) {
                    selectedPriorityElement = levelNum;
                    break;
                }
            }
        }
        
        // 讀取criticalityLevel的區段
        String criticalityLevelValue = null;
        if(te.attribute("criticalityLevel")!=null) {
        	criticalityLevelValue = te.attribute("criticalityLevel").getText();
        }
        String taskCriticality = null;
        Element selectedCriticalityLevelElement = null; //針對有在xml有指定的criticalityLevel
        List<Element> criticalityLevelElements = null;
        if (te.elements("criticalityLevel") != null && !te.elements("criticalityLevel").isEmpty()) {
            criticalityLevelElements = te.elements("criticalityLevel");

            // 初始化 List
            List<Long> periodList = new ArrayList<>();
            List<Long> relativeDeadLineList = new ArrayList<>();
            List<Long> computationAmountList = new ArrayList<>();
            List<Long> criticalityLevelList = new ArrayList<>();
            
            for (Element levelNum : criticalityLevelElements) {
                String periodText = levelNum.elementText("period");
                if (periodText != null) {
                    try {
                        periodList.add((long)(Double.parseDouble(periodText) * magnificationFactor));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: Invalid period value in criticalLevel! [" + periodText + "]");
                    }
                } else {
                    System.err.println("Warning: There is no period value in criticalLevel!");
                }

                String deadlineText = levelNum.elementText("relativeDeadline");
                if (deadlineText != null) {
                    try {
                        relativeDeadLineList.add((long)(Double.parseDouble(deadlineText) * magnificationFactor));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: Invalid relativeDeadline value in criticalLevel! [" + deadlineText + "]");
                    }
                } else {
                    System.err.println("Warning: There is no relativeDeadline value in criticalLevel!");
                }

                String computationText = levelNum.elementText("computationAmount");
                if (computationText != null) {
                    try {
                        computationAmountList.add((long)(Double.parseDouble(computationText) * magnificationFactor));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: Invalid computationAmount value in criticalLevel! [" + computationText + "]");
                    }
                } else {
                    System.err.println("Warning: There is no computationAmount value in criticalLevel!");
                }

                String levelValue = levelNum.attributeValue("value");
                if (levelValue != null) {
                    try {
                        criticalityLevelList.add((long)(Double.parseDouble(levelValue) * magnificationFactor));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: Invalid criticalityLevel value in criticalLevel! [" + levelValue + "]");
                    }
                } else {
                    System.err.println("Warning: There is no criticalityLevel value in criticalLevel!");
                }
                
                if (levelValue != null && levelValue.equals(criticalityLevelValue)) {
                    selectedCriticalityLevelElement = levelNum;
                }
            }
            task.setPeriodList(periodList);
            task.setComputationAmountList(computationAmountList);
            task.setRelativeDeadlineList(relativeDeadLineList);
            task.setCriticalityLevelList(criticalityLevelList);
        }

        // 如果找到選中的 priorityLevel或是criticalityLevel，則設置其相關參數
        if (selectedPriorityElement != null) {
        	task.setPriorityLevel(Integer.parseInt(priorityLevelValue));
            
            String arrivalTimeStr = selectedPriorityElement.elementText("arrivalTime");
            if (arrivalTimeStr != null) {
                task.setEnterTime((long)(Double.parseDouble(arrivalTimeStr) * magnificationFactor));
            } else {
                task.setEnterTime(0L); 
            }

            String periodStr = selectedPriorityElement.elementText("period");
            if (periodStr != null) {
                task.setPeriod((long)(Double.parseDouble(periodStr) * magnificationFactor));
            } else {
                task.setPeriod(0L); 
            }

            String deadlineStr = selectedPriorityElement.elementText("relativeDeadline");
            if (deadlineStr != null) {
                task.setRelativeDeadline((long)(Double.parseDouble(deadlineStr) * magnificationFactor));
            } else {
                task.setRelativeDeadline(0L);  
            }

            String computationStr = selectedPriorityElement.elementText("computationAmount");
            if (computationStr != null) {
                task.setComputationAmount((long)(Double.parseDouble(computationStr) * magnificationFactor));
            } else {
                task.setComputationAmount(0L); 
            }
        } else if(selectedCriticalityLevelElement!=null){
            String arrivalTimeStr = selectedCriticalityLevelElement.elementText("arrivalTime");
            if (arrivalTimeStr != null) {
                task.setEnterTime((long)(Double.parseDouble(arrivalTimeStr) * magnificationFactor));
            } else {
                task.setEnterTime(0L); 
            }
            String periodStr = selectedCriticalityLevelElement.elementText("period");
            if (periodStr != null) {
                task.setPeriod((long)(Double.parseDouble(periodStr) * magnificationFactor));
            } else {
                task.setPeriod(0L); 
            }

            String deadlineStr = selectedCriticalityLevelElement.elementText("relativeDeadline");
            if (deadlineStr != null) {
                task.setRelativeDeadline((long)(Double.parseDouble(deadlineStr) * magnificationFactor));
            } else {
                task.setRelativeDeadline(0L);  
            }

            String computationStr = selectedCriticalityLevelElement.elementText("computationAmount");
            if (computationStr != null) {
                task.setComputationAmount((long)(Double.parseDouble(computationStr) * magnificationFactor));
            } else {
                task.setComputationAmount(0L); 
            }
            
            String criticalityLevelStr = selectedCriticalityLevelElement.attributeValue("value");
            System.out.println("Test: "+criticalityLevelStr);
            if (criticalityLevelStr != null) {
            	task.setCriticalityLevel((long)(Double.parseDouble(criticalityLevelStr) * magnificationFactor));
            } else {
                task.setCriticalityLevel(0L); 
            }
            
        } else { //如果沒有就正常賦予數值
            RTSimulatorMath math = new RTSimulatorMath();
            task.setEnterTime((long)(Double.parseDouble(te.elementText("arrivalTime"))*magnificationFactor));
            task.setPeriod((long)(Double.parseDouble(te.elementText("period"))*magnificationFactor));
            task.setRelativeDeadline((long)(Double.parseDouble(te.elementText("relativeDeadline"))*magnificationFactor));
            task.setComputationAmount((long)(Double.parseDouble(te.elementText("computationAmount"))*magnificationFactor));
        }
        
        // 處理 criticalSections
        if(te.element("criticalSections") != null)
        {
            Iterator cssIt = te.element("criticalSections").elementIterator("criticalSection");
            while(cssIt.hasNext())
            {
                Element cse = (Element)cssIt.next();
                CriticalSection cs = new CriticalSection();
                cs.setUseSharedResource(this.dataSetting.getSharedResource(Integer.valueOf(cse.attribute("resourceID").getText()) - 1));
                this.dataSetting.getSharedResource(Integer.valueOf(cse.attribute("resourceID").getText()) - 1).addAccessTask(task);
                cs.setRelativeStartTime((long)(Double.parseDouble(cse.attribute("startTime").getText())*magnificationFactor));
                cs.setRelativeEndTime((long)(Double.parseDouble(cse.attribute("endTime").getText())*magnificationFactor));
                task.addCriticalSection(cs);
            }
        }  
        task.setTotalCriticalSectionTime();
        task.setParentTaskSet(this.dataSetting.getTaskSet());
        this.dataSetting.addTask(task);
    }

    /**
     * Converts a list of DOM elements to a list of longs.
     *
     * <p>Each element's trimmed text content is parsed as a {@code long}. If the
     * input list is {@code null} or empty, an empty list is returned. When a value
     * cannot be parsed, an error message is printed to {@code System.err} and a
     * default value {@code 0L} is inserted at that position.</p>
     *
     * @param elements list of {@link Element} whose text values should be converted
     * @return a list of parsed {@link Long} values (possibly empty, never {@code null})
     */
    public List<Long> convertElementsToLongList(List<Element> elements) {
    	List<Long> resultList = new ArrayList<Long>();
    	if((elements == null)||(elements.isEmpty())) {
    		return resultList; // return empty list.
    	}
    	for(Element element : elements) {
    		try {
				resultList.add(Long.parseLong(element.getTextTrim()));
			} catch (NumberFormatException e) {
				System.err.println("Conversion error: " + element.getTextTrim() + " is not a valid long value.");
				resultList.add(0L);
			}
    	}
    	return resultList;
    }
    
    /*Processor*/
    private void createProcessor()
    {
        Iterator it = root.elementIterator();
        this.dataSetting.getProcessor().setModelName(root.attribute("model").getText());
        this.dataSetting.getProcessor().getDynamicVoltageRegulator().setDVFSType(root.attribute("DVFStype").getText());
        
        
        while(it.hasNext())
        {
            /*Core*/
            Element processorElement = (Element)it.next();
            Iterator coreAtb = processorElement.attributeIterator();
            CoreSet coreSet = new CoreSet();
            
            while(coreAtb.hasNext())
            {
                Attribute atb = (Attribute)coreAtb.next();
                switch(atb.getQualifiedName())
                {
                    case "quantity":
                    {
                        for(int i = 0; i < Integer.valueOf(atb.getText()); i++)
                        {
                            Core core = new Core();
                            core.setParentProcessor(this.dataSetting.getProcessor());
                            core.setParentCoreSet(coreSet);
                            coreSet.addCore(core);
                            this.dataSetting.getProcessor().addCore(core);
                        }
                        break;
                    }
                    
                    case "type":
                    {
                        coreSet.setCoreType(atb.getText());
                        break;
                    }
                    default:
                        println("SetCoreAttribute Error!!");
                }
            }
            
            /*CoreSpeed*/
            Iterator coreElement = processorElement.elementIterator();
            while(coreElement.hasNext())
            {
                Element element = (Element)coreElement.next();
                switch(element.getQualifiedName())
                {
                    case "availableSpeeds":
                    {
                        this.setAvailableSpeed(element ,coreSet);
                        break;
                    }
                    case "powerConsumptionFunction":
                    {
                        this.setPowerConsumptionFunction(element ,coreSet);
                        break;
                    }
                    default:
                    {
                        println("SeetCoreAttribute Error!!");
                    }
                }
            }
            /**/
            if(this.dataSetting.getProcessor().getDynamicVoltageRegulator().getDVFSType().equals(DVFSType.PerCore))
            {
                for(int i = 0 ; i<coreSet.size() ; i++)
                {
                    CoreSet cSet = new CoreSet(coreSet);
                    cSet.addCore(coreSet.getCore(i));
                    this.dataSetting.getProcessor().addCoreSet(cSet);
                }
            }
            else
            {
                this.dataSetting.getProcessor().addCoreSet(coreSet);
            }
        }
        println("!!!!!CoreSet::" + this.dataSetting.getProcessor().getCoresSets().size());
    }
    
    private void setPowerConsumptionFunction(Element e ,CoreSet coreSet)
    {
        Iterator it = e.attributeIterator();
        while(it.hasNext())
        {
            Attribute PCFatb = (Attribute)it.next();
            switch(PCFatb.getQualifiedName())
            {
                case "alpha":
                    coreSet.setAlphaValue(Double.valueOf(PCFatb.getText()));
                    break;
                case "beta":
                    coreSet.setBetaValue(Double.valueOf(PCFatb.getText()));
                    break;
                case "gamma":
                    coreSet.setGammaValue(Double.valueOf(PCFatb.getText()));
                    break;
                default:
                    println("setPowerConsumptionFunction Error!!!!!");
            }
        }
    }
    
    private void setAvailableSpeed(Element e,CoreSet coreSet)
    {
        Iterator it = e.elementIterator();
        while(it.hasNext())
        {
            CoreSpeed s = new CoreSpeed();
            Element sElement = (Element)it.next();
            
            if(!sElement.getText().equals("idle"))
            {
                s.setSpeed(Double.valueOf(sElement.getText()));
            }
            else
            {
                s.setSpeed(0);
            }
            //需多加入idle判斷
            Iterator atb_it = sElement.attributeIterator();
            while(atb_it.hasNext())
            {
                Attribute atb = (Attribute)atb_it.next();
                switch(atb.getQualifiedName())
                {
                    case "powerConsumption":
                        s.setPowerConsumption(Double.valueOf(atb.getText()));
                    break;
                    default:
                        println("setAvailableSpeed Error!!!");
                }
            }
            
            
            coreSet.addCoreSpeed(s);
        }
    }
    
    /**
     * Returns the fully built {@link DataSetting} after a successful call to
     * {@link #loadSource(String)}. The returned object contains the parsed
     * processor configuration and/or workload definition, depending on the
     * input file.
     *
     * @return the current {@link DataSetting} instance (never {@code null})
     */
    public DataSetting getDataSetting()
    {
        return this.dataSetting;
    }
}
