/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workloadgenerator;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * Utility to build an XML DOM document that represents a generated workload.
 * <p>
 * The writer constructs the root {@code <workload>} element and attaches attributes
 * such as maximum/actual utilization, CSR bounds, number of tasks/resources,
 * and base speed. It then emits resources and task elements, including
 * critical sections as attributes under a {@code <criticalSections>} block per task.
 * Existing inline comments (in Chinese) about element creation are preserved
 * for context and have been woven into this description.
 * </p>
 *
 * @author YC
 */
public class XMLWriter 
{
    /**
     * Create a new XMLWriter instance.
     */
    public XMLWriter()
    {
        
    }

    /**
     * Build a DOM {@link Document} for the supplied workload.
     * <p>
     * This routine creates the root workload element, sets summary attributes,
     * writes each resources group, and serializes each task with its
     * parameters and generated critical sections. Time values are scaled by
     * the generator's configured accuracy via helper methods on the model.
     * </p>
     *
     * @param w the workload model to export
     * @return a DOM Document containing the workload, or null when the DOM
     *         builder cannot be created
     */
    public static Document creatXML(wgWorkload w)
    {
        try 
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
        /*創建Workload根元素*/
            Element workloadElement = doc.createElement(w.getWorkloadHeader());

            doc.appendChild(workloadElement);
            workloadElement.setAttribute(w.getMaximumUtilizationHeader(), String.valueOf(w.getMaximumUtilization()));
            workloadElement.setAttribute(w.getActualUtilizationHeader(), String.valueOf(w.getActualUtilization()));
            
            workloadElement.setAttribute(w.getMaximumCriticalSectionRatioHeader(), String.valueOf(w.getMaxCriticalSectionRatio()));
            workloadElement.setAttribute(w.getActualCriticalSectionRatioHeader(), String.valueOf(w.getActualCriticalSectionRatio()));
            
            workloadElement.setAttribute(w.getTaskNumberHeader(), String.valueOf(w.getTaskNumber()));
            workloadElement.setAttribute(w.getResourcesNumberHeader(), String.valueOf(w.getResourcesNumber()));
            workloadElement.setAttribute(w.getFrequencyHeader(), String.valueOf(w.getFrequency()));
        /*創建Resources*/


            for(wgResources r : w.getResourcesSet())
            {
                Element resourcesElement = doc.createElement(r.getResourcesHeader());
                resourcesElement.setAttribute(r.getIDHeader(),String.valueOf(r.getID()));
                resourcesElement.setAttribute(r.getResourceAmountHeader(),String.valueOf(r.getResourceAmount()));
               workloadElement.appendChild(resourcesElement);
            }

            for(wgTask t : w.getTaskSet())
            {
                Element taskElement = doc.createElement(t.getTaskHeader());
                taskElement.setAttribute(t.getIDHeader(),String.valueOf(t.getID()));

                Element enterTime = doc.createElement(t.getEnterTimeHeader());
                
                enterTime.appendChild(doc.createTextNode(String.valueOf(t.exporeEnterTime())));
                taskElement.appendChild(enterTime);

                Element period = doc.createElement(t.getPeriodHeader());
                period.appendChild(doc.createTextNode(String.valueOf(t.exporePeriod())));
                taskElement.appendChild(period);

                Element relativeDeadline = doc.createElement(t.getRelativeDeadlineHeader());
                relativeDeadline.appendChild(doc.createTextNode(String.valueOf(t.exporeRelativeDeadline())));
                taskElement.appendChild(relativeDeadline);

                Element computationAmount = doc.createElement(t.getComputationAmountHeader());
                computationAmount.appendChild(doc.createTextNode(String.valueOf(t.exporeComputationAmount())));
                taskElement.appendChild(computationAmount);
                
                if(t.getCriticalSectionSet().size()>0)
                {
                    Element  criticalSections= doc.createElement(t.getCriticalSectionSet().getCriticalSectionSetHeader());
                    taskElement.appendChild(criticalSections);
                    
                    for(wgCriticalSection cs : t.getCriticalSectionSet())
                    {
                        Element  criticalSection= doc.createElement(cs.getCriticalSectionHeader());
//                        taskElement.appendChild(criticalSection);
                        criticalSection.setAttribute(cs.getResourceIDHeader(),String.valueOf(cs.getResources().getID()));
                        criticalSection.setAttribute(cs.getStartTimeHeader(),String.valueOf(cs.exporeStartTime()));
                        criticalSection.setAttribute(cs.getEndTimeHeader(),String.valueOf(cs.exporeEndTime()));
                        
                        criticalSections.appendChild(criticalSection);
                       
//                        Element resources = doc.createElement(cs.getResourceIDHeader());
//                        resources.appendChild(doc.createTextNode(String.valueOf(cs.getResources().getID())));
//                        criticalSection.appendChild(resources);
//
//                        Element startTime = doc.createElement(cs.getStartTimeHeader());
//                        startTime.appendChild(doc.createTextNode(String.valueOf(cs.exporeStartTime())));
//                        criticalSection.appendChild(startTime);
//
//                        Element endTime = doc.createElement(cs.getEndTimeHeader());
//                        endTime.appendChild(doc.createTextNode(String.valueOf(cs.exporeEndTime())));
//                        criticalSection.appendChild(endTime);
                    }
                }
                workloadElement.appendChild(taskElement);
            }

            return doc;
	}
        catch (ParserConfigurationException pce) 
        {
            pce.printStackTrace();
	}
        return null;
    }
}