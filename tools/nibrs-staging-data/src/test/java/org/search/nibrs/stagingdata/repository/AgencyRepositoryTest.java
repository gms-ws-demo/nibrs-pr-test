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
package org.search.nibrs.stagingdata.repository;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.search.nibrs.stagingdata.model.Agency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AgencyRepositoryTest {
	
	@Autowired
	public AgencyRepository agencyRepository; 

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		Map<Integer, String> agencyPairs = StreamSupport.stream(agencyRepository.findAll().spliterator(), false)
				.collect(Collectors.toMap(Agency::getAgencyId, Agency::getAgencyName));
		System.out.println(agencyPairs);
	}
				
				
}
