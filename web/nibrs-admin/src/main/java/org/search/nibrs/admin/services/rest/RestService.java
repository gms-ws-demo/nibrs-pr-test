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
package org.search.nibrs.admin.services.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.search.nibrs.admin.AppProperties;
import org.search.nibrs.admin.security.AuthUser;
import org.search.nibrs.admin.uploadfile.PersistReportTask;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.model.AbstractReport;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.stagingdata.model.Owner;
import org.search.nibrs.stagingdata.model.PreCertificationError;
import org.search.nibrs.stagingdata.model.SubmissionTrigger;
import org.search.nibrs.stagingdata.model.search.IncidentDeleteRequest;
import org.search.nibrs.stagingdata.model.search.IncidentSearchRequest;
import org.search.nibrs.stagingdata.model.search.IncidentSearchResult;
import org.search.nibrs.stagingdata.model.search.PrecertErrorSearchRequest;
import org.search.nibrs.stagingdata.model.search.SearchResult;
import org.search.nibrs.stagingdata.model.segment.AdministrativeSegment;
import org.search.nibrs.stagingdata.model.segment.ArrestReportSegment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Profile({"incident-search"})
public class RestService{
	private final Log log = LogFactory.getLog(this.getClass());

	private final WebClient webClient;

	@Autowired
	public RestService(WebClient.Builder webClientBuilder, AppProperties appProperties) {
		this.webClient = webClientBuilder.baseUrl(appProperties.getRestServiceBaseUrl()).build();
	}
	
	public Map<Integer, String> getAgencies(Integer ownerId) {
		String ownerIdString = Objects.toString(ownerId, "");
		return this.webClient.get().uri("/codeTables/agencies/"+ownerIdString)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<Integer, String>>() {})
				.block();
	}
	
	public Map<Integer, String> getAgenciesNoChache(String ownerId) {
		return this.webClient.get().uri("/codeTables/agencies/"+ownerId)
				.header("Expires", "0")
				.header("Pragma", "no-cache")
				.header("Cache-Control", "private",  "no-store", "max-age=0")
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<Integer, String>>() {})
				.block();
	}
	
	public Map<Integer, String> getAgenciesByOwnerAndState(Integer ownerId, String stateCode) {
		return this.webClient.get().uri("/codeTables/states/" + stateCode + "/agencies/"+ Objects.toString(ownerId, ""))
				.header("Expires", "0")
				.header("Pragma", "no-cache")
				.header("Cache-Control", "private",  "no-store", "max-age=0")
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<Integer, String>>() {})
				.block();
	}
	
	public Map<String, String> getOris(String ownerId) {
		return this.webClient.get().uri("/codeTables/agenciesHavingData/" + ownerId)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<String, String>>() {})
				.block();
	}
	
	public Map<Integer, String> getOffenseCodes() {
		return this.webClient.get().uri("/codeTables/offenseCodes")
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<Integer, String>>() {})
				.block();
	}
	
	public Map<Integer, String> getNibrsErrorCodes() {
		return this.webClient.get().uri("/codeTables/nibrsErrorCodes")
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<Integer, String>>() {})
				.block();
	}
	
	public IncidentSearchResult getIncidents(IncidentSearchRequest incidentSearchRequest){
		return this.webClient.post().uri("/reports/search")
				.body(BodyInserters.fromObject(incidentSearchRequest))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<IncidentSearchResult>() {})
				.block();
	}
	
	public Owner getSavedUser(Owner webUser){
		return this.webClient.post().uri("/codeTables/user")
				.body(BodyInserters.fromObject(webUser))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Owner>() {})
				.block();
	}
	
	public AdministrativeSegment getAdministrativeSegment(String id){
		return this.webClient.get().uri("/reports/A/" + id)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<AdministrativeSegment>() {})
				.block();
	}
	
	public ArrestReportSegment getArrestReportSegment(String id){
		return this.webClient.get().uri("/reports/B/" + id)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ArrestReportSegment>() {})
				.block();
	}
	
	public void persistGroupBReport(List<GroupBArrestReport> groupBArrestReports, PersistReportTask persistReportTask) {
		try{
			webClient.post().uri("/arrestReports")
			.body(BodyInserters.fromObject(groupBArrestReports))
			.retrieve()
			.bodyToMono(String.class)
			.block();
			
			persistReportTask.increaseProcessedCount(groupBArrestReports.size());
			log.info("Progress: " + persistReportTask.getProcessedCount() + "/" + persistReportTask.getTotalCount());
			groupBArrestReports.clear();
		}
		catch(ResourceAccessException rae){
			List<String> identifiers = groupBArrestReports.stream()
					.map(GroupBArrestReport::getIdentifier)
					.collect(Collectors.toList());
			log.error("Failed to connect to the rest service to process the Group B Arrest Reports " + 
					" with Identifiers " + identifiers);
			persistReportTask.setAborted(true);
			throw rae;
		}
		catch(Exception e){
			persistReportTask.increaseProcessedCount(groupBArrestReports.size());
			groupBArrestReports.stream()
				.map(GroupBArrestReport::getUniqueReportDescription)
				.forEach(item->persistReportTask.addFailedToProcess(item));
			List<String> identifiers = groupBArrestReports.stream()
					.map(GroupBArrestReport::getIdentifier)
					.collect(Collectors.toList());
			log.warn("Failed to persist incident " + identifiers);
			log.error(e);
			log.info("Progress: " + persistReportTask.getProcessedCount() + "/" + persistReportTask.getTotalCount());
			groupBArrestReports.clear();
		}
	}
	
	public void persistGroupAReport(List<GroupAIncidentReport> groupAIncidentReports, PersistReportTask persistReportTask) {
		
		try {
			log.info("About to post for group A incident report " + groupAIncidentReports.size());
			webClient.post().uri("/groupAIncidentReports")
				.body(BodyInserters.fromObject(groupAIncidentReports))
				.retrieve()
				.bodyToMono(String.class)
				.block();
			persistReportTask.increaseProcessedCount(groupAIncidentReports.size());
			log.info("Progress: " + persistReportTask.getProcessedCount() + "/" + persistReportTask.getTotalCount());
			groupAIncidentReports.clear();
		}
		catch(ResourceAccessException rae){
			List<String> identifiers = groupAIncidentReports.stream()
					.map(GroupAIncidentReport::getIncidentNumber)
					.collect(Collectors.toList());
			log.error("Failed to connect to the rest service to process the group A reports " + 
					"  Identifiers " + identifiers);
			persistReportTask.setAborted(true);
			throw rae;
		}
		catch(Exception e){
			persistReportTask.increaseProcessedCount(groupAIncidentReports.size());
			
			groupAIncidentReports.stream()
				.map(GroupAIncidentReport::getUniqueReportDescription)
				.forEach(item->persistReportTask.addFailedToProcess(item));
			List<String> identifiers = groupAIncidentReports.stream()
					.map(GroupAIncidentReport::getIncidentNumber)
					.collect(Collectors.toList());
			log.warn("Failed to persist incident " + identifiers);
			log.error(e);
			log.info("Progress: " + persistReportTask.getProcessedCount() + "/" + persistReportTask.getTotalCount());
			groupAIncidentReports.clear();
		}
	}
	
	public String generateSubmissionFiles(IncidentSearchRequest incidentSearchRequest) {
		SubmissionTrigger submissionTrigger = new SubmissionTrigger(incidentSearchRequest);
		log.info("submissionTrigger: " + submissionTrigger);
		log.info("submissionIncidentSearchRequest: " + incidentSearchRequest);
		
		String response = ""; 
		try { 
			response = webClient.post().uri("/submissions/trigger")
				.body(BodyInserters.fromObject(submissionTrigger))
				.retrieve()
				.bodyToMono(String.class)
				.block();
		}
		catch(Throwable e) {
			log.error("Got error when calling the service /submissions/trigger", e);
			response = "Failed to process the request, please report the error or check back later."; 
		}
		
		return response; 
	}
	
	public String deleteByIncidentDeleteRequest(IncidentDeleteRequest incidentDeleteRequest) {
		
		String response = ""; 
		String deleteGroupAIncidentsResponse = "";
		String deleteGroupBArrestsResponse = "";
		try { 
			deleteGroupAIncidentsResponse = webClient.method(HttpMethod.DELETE)
					.uri("/groupAIncidentReports")
					.body(BodyInserters.fromObject(incidentDeleteRequest))
					.retrieve()
					.bodyToMono(String.class)
					.block();
			deleteGroupBArrestsResponse = webClient.method(HttpMethod.DELETE)
					.uri("/arrestReports")
					.body(BodyInserters.fromObject(incidentDeleteRequest))
					.retrieve()
					.bodyToMono(String.class)
					.block();
			
			response = deleteGroupAIncidentsResponse + "\n" + deleteGroupBArrestsResponse;
		}
		catch(Throwable e) {
			log.error("Got error when calling the delete services of /groupAIncidentReports or /arrestReports ", e);
			response = "Failed to process the request, please report the error or check back later."; 
		}
		
		return response; 
	}
	
	@Async
	public void persistValidReportsAsync(PersistReportTask persistReportTask, List<AbstractReport> validReports, AuthUser authUser) {
		log.info("Execute method asynchronously. "
			      + Thread.currentThread().getName());
		persistReportTask.setStarted(true);
		int groupAReportCount = 0;
		List<GroupAIncidentReport> groupAIncidentReports = new ArrayList<>();
		int groupBReportCount = 0; 
		List<GroupBArrestReport> groupBArrestReports = new ArrayList<>();
		for(AbstractReport abstractReport: validReports){
			if (authUser != null) {
				abstractReport.setOwnerId(authUser.getUserId());
			}
			
			if (abstractReport instanceof GroupAIncidentReport) {
				groupAIncidentReports.add((GroupAIncidentReport) abstractReport);
				groupAReportCount ++;
			}
			else if(abstractReport instanceof GroupBArrestReport) {
				groupBArrestReports.add((GroupBArrestReport) abstractReport);
				groupBReportCount ++;
			}
			
			if (groupAReportCount == 30) {
				groupAReportCount = 0;
				this.persistGroupAReport(groupAIncidentReports, persistReportTask);
			}
			if (groupBReportCount == 30) {
				groupBReportCount = 0;
				this.persistGroupBReport(groupBArrestReports, persistReportTask);
			}
		}
		
		if (groupAReportCount > 0) {
			groupAReportCount = 0;
			this.persistGroupAReport(groupAIncidentReports, persistReportTask);
		}
		if (groupBReportCount > 0) {
			groupBReportCount = 0;
			this.persistGroupBReport(groupBArrestReports, persistReportTask);
		}
	}
	
	public String persistPreCertificationErrors(List<NIBRSError> nibrsErrors, AuthUser authUser) {
		log.info("Execute method asynchronously. "
				+ Thread.currentThread().getName());
		
		List<PreCertificationError> preCertificationErrors = nibrsErrors.stream()
				.map(nibrsError -> new PreCertificationError(nibrsError))
				.collect(Collectors.toList());
		
		if (authUser != null) {
			preCertificationErrors.forEach(error -> error.setOwnerId(authUser.getUserId()));
		}
		
		Integer savedCount = webClient.post().uri("/preCertificationErrors")
			.body(BodyInserters.fromObject(preCertificationErrors))
			.retrieve()
			.bodyToMono(Integer.class)
			.block();
		
		return savedCount + " errors are persisted."; 
		
	}

	public SearchResult<PreCertificationError> getPrecertErrors(PrecertErrorSearchRequest precertErrorSearchRequest) {
		return this.webClient.post().uri("/preCertificationErrors/search")
				.body(BodyInserters.fromObject(precertErrorSearchRequest))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<SearchResult<PreCertificationError>>() {})
				.block();
	}

	public Map<String, String> getStatesNoChache(String ownerId) {
		
		String ownerIdString = Objects.toString(ownerId, "");
		return this.webClient.get().uri("/codeTables/states/"+ownerIdString)
				.header("Expires", "0")
				.header("Pragma", "no-cache")
				.header("Cache-Control", "private",  "no-store", "max-age=0")
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<String, String>>() {})
				.block();
	}

	public List<Integer> getYearsByStateCode(String stateCode, String ownerId) {
		return this.webClient.get().uri("/codeTables/state/years/"+ ownerId + "/" + stateCode)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<List<Integer>>() {})
				.block();
	}
	
	
	public List<Integer> getYears(Integer agencyId, String ownerId) {
		return this.webClient.get().uri("/codeTables/years/"+ ownerId + "/" + agencyId)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<List<Integer>>() {})
				.block();
	}

	public List<Integer> getMonths(Integer agencyId, Integer year, String ownerId) {
		return this.webClient.get().uri("/codeTables/months/" + year + "/" + ownerId + "/"+ agencyId)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<List<Integer>>() {})
				.block();
	}
	
	public List<Integer> getMonthsByStateCode(String stateCode, Integer year, String ownerId) {
		return this.webClient.get().uri("/codeTables/state/months/" + year + "/" + ownerId + "/"+ stateCode)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<List<Integer>>() {})
				.block();
	}
	
}