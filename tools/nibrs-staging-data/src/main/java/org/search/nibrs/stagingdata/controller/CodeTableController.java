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
package org.search.nibrs.stagingdata.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.search.nibrs.stagingdata.AppProperties;
import org.search.nibrs.stagingdata.model.Agency;
import org.search.nibrs.stagingdata.model.NibrsErrorCodeType;
import org.search.nibrs.stagingdata.model.Owner;
import org.search.nibrs.stagingdata.model.UcrOffenseCodeType;
import org.search.nibrs.stagingdata.repository.AgencyRepository;
import org.search.nibrs.stagingdata.repository.AgencyRepositoryCustom;
import org.search.nibrs.stagingdata.repository.NibrsErrorCodeTypeRepository;
import org.search.nibrs.stagingdata.repository.OwnerRepository;
import org.search.nibrs.stagingdata.repository.UcrOffenseCodeTypeRepository;
import org.search.nibrs.stagingdata.repository.segment.AdministrativeSegmentRepository;
import org.search.nibrs.stagingdata.repository.segment.ArrestReportSegmentRepository;
import org.search.nibrs.stagingdata.util.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/codeTables")
public class CodeTableController {
	private static final Log log = LogFactory.getLog(CodeTableController.class);
	@Autowired
	private AgencyRepository agencyRepository;
	@Autowired
	private AgencyRepositoryCustom agencyRepositoryCustom;
	@Autowired
	private OwnerRepository ownerRepository;
	@Autowired
	private AdministrativeSegmentRepository administrativeSegmentRepository;
	@Autowired
	private ArrestReportSegmentRepository arrestReportSegmentRepository;
	@Autowired
	private UcrOffenseCodeTypeRepository ucrOffenseCodeTypeRepository;
	@Autowired
	private NibrsErrorCodeTypeRepository nibrsErrorCodeTypeRepository;
	@Autowired
	public AppProperties appProperties;
	
	private final List<String> unknownOrBlank = Arrays.asList("UNKOWN", "BLANK");

	@PostMapping("/user")
	public @ResponseBody Owner search(@RequestBody Owner owner){
		log.debug("Owner:" + owner);
		Owner savedOwner = ownerRepository.findFirstByFederationId(owner.getFederationId());
		
		if (savedOwner == null) {
			savedOwner=ownerRepository.save(owner);
		}
		
		return savedOwner;
	}

	@GetMapping("/agencies")
	public Map<Integer, String> agencies(){
		Map<Integer, String> agencyMap = 
			StreamSupport.stream(agencyRepository.findAll(new Sort(Sort.Direction.ASC, "agencyName")).spliterator(), false)
				.filter(agency-> !"UNKNOWN".equalsIgnoreCase(agency.getAgencyName()) && !"BLANK".equalsIgnoreCase(agency.getAgencyName()))
				.collect(Collectors.toMap(Agency::getAgencyId, Agency::getAgencyName, (u, v) -> u,
					      LinkedHashMap::new));
		return agencyMap;
	}
	
	@GetMapping("/agencies/{ownerId}")
	public Map<Integer, String> agenciesByOwnerId(@PathVariable Integer ownerId){
		
		Set<Integer> agencyIds = administrativeSegmentRepository.findAgencyIdsByOwnerId(ownerId); 
		Set<Integer> agencyIdsFromGroupB = arrestReportSegmentRepository.findAgencyIdsByOwnerId(ownerId); 
		agencyIds.addAll(agencyIdsFromGroupB); 
		
		Comparator<Agency> compareByAgencyName = (Agency a1, Agency a2) -> a1.getAgencyName().compareTo( a2.getAgencyName() );
		List<Agency> agencies = agencyRepository.findAllById(agencyIds); 
		Collections.sort(agencies, compareByAgencyName); 
		
		Map<Integer, String> agencyMap = 
			StreamSupport.stream(agencies.spliterator(), false)
				.filter(agency-> !unknownOrBlank.contains(agency.getAgencyName().toUpperCase()))
					.collect(Collectors.toMap(Agency::getAgencyId, Agency::getAgencyName, (u, v) -> u,
						      LinkedHashMap::new));
		return agencyMap;
	}
	
	@GetMapping("/states/{stateCode}/agencies/{ownerId}") 
	public Map<Integer, String> agenciesByStateAndOwnerId(@PathVariable String stateCode, @PathVariable Integer ownerId){
		
		return agencyRepositoryCustom.findAllAgenciesByStateAndOwnerId(ownerId, stateCode);
	}
	
	@GetMapping("/states/{ownerId}")
	public Map<String, String> statesByOwnerId(@PathVariable Integer ownerId){
		
		return agencyRepositoryCustom.findAllStatesByOwnerId(ownerId);
	}
	
	@GetMapping("/agenciesHavingData")
	public Map<String, String> getAgenciesHavingData(){
		Map<String, String> agencyMap = StreamSupport.stream(agencyRepository.findAllHavingData(null).spliterator(), false)
				.filter(agency-> !unknownOrBlank.contains(agency.getAgencyName().toUpperCase()))
				.collect(Collectors.toMap(Agency::getAgencyOri, Agency::getAgencyName, (u, v) -> u,
						LinkedHashMap::new));
		return agencyMap;
	}
	
	@GetMapping("/agenciesHavingData/{ownerId}")
	public Map<String, String> getAgenciesHavingDataByOwnerId(@PathVariable Integer ownerId){
		Map<String, String> agencyMap = StreamSupport.stream(agencyRepository.findAllHavingData(ownerId).spliterator(), false)
				.filter(agency-> !unknownOrBlank.contains(agency.getAgencyName().toUpperCase()))
				.collect(Collectors.toMap(Agency::getAgencyOri, Agency::getAgencyName, (u, v) -> u,
						LinkedHashMap::new));
		return agencyMap;
	}
	
	@GetMapping("/years/{ownerId}/{ori}") 
	public List<Integer> getYearsByOriAndOwner(@PathVariable String ori, @PathVariable String ownerId){
		List<Integer> years = administrativeSegmentRepository.findDistinctYears(ori, ObjectUtils.getInteger(ownerId));
		log.debug("Years from the database for ORI " + ori + " and ownerId " + ownerId + " : " + years);
		return years;
	}
	
	@GetMapping("/months/{year}/{ownerId}/{ori}") 
	public List<Integer> getMonths(@PathVariable String ori, @PathVariable Integer year, @PathVariable String ownerId){
		return administrativeSegmentRepository.findDistinctMonths(ori, year, ObjectUtils.getInteger(ownerId));
	}
	
	@GetMapping("/offenseCodes")
	public Map<Integer, String> offenseCodes(){
		Map<Integer, String> offenseCodeMap = StreamSupport.stream(ucrOffenseCodeTypeRepository.findAll(new Sort(Sort.Direction.ASC,"stateDescription")).spliterator(), false)
				.filter(offenseCode-> !unknownOrBlank.contains(offenseCode.getNibrsDescription().toUpperCase()))
				.collect(Collectors.toMap(UcrOffenseCodeType::getUcrOffenseCodeTypeId, UcrOffenseCodeType::getStateDescription, (u, v) -> u,
					      LinkedHashMap::new));
		return offenseCodeMap;
	}
	
	@GetMapping("/nibrsErrorCodes")
	public Map<Integer, String> nibrsErrorCodes(){
		Map<Integer, String> nibrsErrorCodeMap = StreamSupport.stream(nibrsErrorCodeTypeRepository.findAll(new Sort(Sort.Direction.ASC,"code")).spliterator(), false)
				.filter(nibrsErrorCode-> !unknownOrBlank.contains(nibrsErrorCode.getMessage().toUpperCase()))
				.collect(Collectors.toMap(NibrsErrorCodeType::getNibrsErrorCodeTypeId, NibrsErrorCodeType::getCode, (u, v) -> u,
						LinkedHashMap::new));
		return nibrsErrorCodeMap;
	}
	
	@GetMapping(value="/localDateTime")
	public String getLocalDateTime(){
		return LocalDateTime.now().toString();
	}
	
}
