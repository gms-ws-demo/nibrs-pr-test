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
package org.search.nibrs.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.search.nibrs.model.codes.PropertyDescriptionCode;

public class PropertySegmentTests {

	@Test
	public void testContainsProperty() {
		PropertySegment ps = new PropertySegment();
		assertFalse(ps.containsPropertyDescription(PropertyDescriptionCode._01.code));
		ps.setPropertyDescription(0, PropertyDescriptionCode._01.code);
		ps.setPropertyDescription(1, PropertyDescriptionCode._02.code);
		assertTrue(ps.containsPropertyDescription(PropertyDescriptionCode._01.code));
		assertFalse(ps.containsPropertyDescription(PropertyDescriptionCode._03.code));
	}
	
}
