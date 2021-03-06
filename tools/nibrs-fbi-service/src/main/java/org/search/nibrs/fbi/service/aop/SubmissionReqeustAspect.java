/*
 * Copyright 2016 SEARCH-The National Consortium for Justice Information and Statistics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.search.nibrs.fbi.service.aop;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.search.nibrs.fbi.service.AppProperties;
import org.search.nibrs.fbi.service.service.StagingDataRestClient;
import org.search.nibrs.stagingdata.model.Submission;
import org.search.nibrs.stagingdata.model.Violation;
import org.search.nibrs.xml.XmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Aspect
@Component
public class SubmissionReqeustAspect {
	private final Log log = LogFactory.getLog(this.getClass());
	
	private AppProperties appProperties;
	
	@Autowired
	private StagingDataRestClient stagingDataRestClient;
	
	private String responseFilePath;
	
	@Autowired
	public SubmissionReqeustAspect(AppProperties appProperties) {
		this.appProperties = appProperties; 
		responseFilePath = appProperties.getNibrsNiemDocumentFolder() + "/response/";
		
	    File directorty = new File(responseFilePath); 
	    if (!directorty.exists()){
	    	directorty.mkdirs(); 
	    }

	}

	@Around("execution(* org.search.nibrs.fbi.service.service.SubmissionRequestProcessor.*(..))")
    public void processSubmissionRequest(ProceedingJoinPoint joinPoint) throws Exception {
        //Advice
		
		Document requestDocument = (Document)joinPoint.getArgs()[0];
		log.info("Aspect request: " + XmlUtils.nodeToString(requestDocument)); 
    	log.info(" Check for SubmissionRequestProcessor ");

    	String incidentIdentifier = XmlUtils.xPathStringSearch(requestDocument, "(nibrs:Submission/nibrs:Report/nc:Incident/nc:ActivityIdentification/nc:IdentificationID)"
    			+ "|(nibrs:Submission/nibrs:Report[not(nc:Incident)]/j:Arrest/nc:ActivityIdentification/nc:IdentificationID)");
    	log.info("Incident Identifier: " + incidentIdentifier);
    	
    	Submission submission = new Submission();
    	submission.setIncidentIdentifier(incidentIdentifier);
    	
    	String messageIdentifier = XmlUtils.xPathStringSearch(requestDocument, "nibrs:Submission/cjis:MessageMetadata/cjis:MessageIdentification/nc:IdentificationID");
    	submission.setMessageIdentifier(Integer.valueOf(messageIdentifier));
    	
    	String nibrsReportCategoryCode = XmlUtils.xPathStringSearch(requestDocument, "nibrs:Submission/nibrs:Report/nibrs:ReportHeader/nibrs:NIBRSReportCategoryCode");
    	submission.setNibrsReportCategoryCode(nibrsReportCategoryCode);
    	
    	Exchange exchange = (Exchange)joinPoint.getArgs()[1];
    	log.info("Aspect exchange messageID: " + exchange.getIn().getMessageId());
    	
    	String fileName = (String) exchange.getIn().getHeader("CamelFileName"); 
    	log.info("Aspect exchange Camel File Name: " + fileName); 
    	submission.setRequestFilePath(appProperties.getNibrsNiemDocumentFolder() + "/request/" + fileName);
    	submission.setSubmissionTimestamp(LocalDateTime.now());
    	try {
    		log.info(" Allowed execution for " + joinPoint);
			Document returnedDocument = (Document) joinPoint.proceed();
			log.info("Aspect result: " + XmlUtils.nodeToString(returnedDocument));
			
			submission.setResponseTimestamp(LocalDateTime.now());
			
			String responseFileName = responseFilePath + fileName;
			submission.setResponseFilePath(responseFileName);
			FileUtils.writeStringToFile(new File(responseFileName), XmlUtils.nodeToString(returnedDocument), "UTF-8");
			
			
			String status = XmlUtils.xPathStringSearch(returnedDocument, "//return/ingestResponse/status");
			switch (StringUtils.trimToEmpty(status)) {
			case "ACCEPTED":
				submission.setAcceptedIndicator(true);
				break; 
			case "ERRORS":
				submission.setAcceptedIndicator(false);
				
				processViolations(submission, returnedDocument);
				break;
			case "WARNINGS":
				submission.setAcceptedIndicator(true);
				processViolations(submission, returnedDocument);
				
				break;
			default:  //get no response or a response other than the three types above
				submission.setAcceptedIndicator(false);
				submission.setFaultCode(XmlUtils.xPathStringSearch(returnedDocument, "//faultcode"));
				submission.setFaultDescription(StringUtils.normalizeSpace(XmlUtils.xPathStringSearch(returnedDocument, "//faultstring")));
			}
			
			stagingDataRestClient.persistSubmission(submission);

		} catch (Throwable e) {
			e.printStackTrace();
		}
    	
    }

	private void processViolations(Submission submission, Document returnedDocument) {
		Set<Violation> violationSet = new HashSet<>();
		NodeList violationsNodes = (NodeList) XmlUtils.xPathNodeListSearch(returnedDocument, "//return/ingestResponse/violations");
		
		for (int i=0; i<violationsNodes.getLength(); i++) {
			Node violations = (Node)violationsNodes.item(i);
			Violation violation = new Violation(); 
			violation.setViolationCode(XmlUtils.xPathStringSearch(violations, "violationCode"));
			violation.setViolationLevel(XmlUtils.xPathStringSearch(violations, "violationLevel"));
			violation.setViolationDescription(XmlUtils.xPathStringSearch(violations, "violationDescription"));
			violation.setViolationTimestamp(submission.getResponseTimestamp());
			violationSet.add(violation);
		}
		
		submission.setViolations(violationSet);
	}
}