/**
 * Copyright (C) 2017
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.solid.types.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class PropertyTreeTest {

	@Test
	public void simpleProperty() {
		PropertyTree tree = FixedPropertyTree.builder()
				.put("simple", 17)
				.build();
		
		assertEquals(Integer.valueOf(17),tree.find(Integer.class, "simple").get());
	}

	@Test
	public void propertyInSub() {
		PropertyTree tree = FixedPropertyTree.builder()
				.put("sub", FixedPropertyTree.builder()
						.put("simple", 17)
						.build())
				.build();
		
		assertEquals(Integer.valueOf(17),tree.find(Integer.class, "sub", "simple").get());
	}
	
	@Test
	public void wontFindAnythingIfMoreThanOneInList() {
		PropertyTree tree = FixedPropertyTree.builder()
				.put("sub", FixedPropertyTree.builder()
						.put("simple", 17)
						.build())
				.put("sub", FixedPropertyTree.builder()
						.put("simple", 18)
						.build())
				.build();
		
		assertFalse(tree.find(Integer.class, "sub", "simple").isPresent());
	}
	
	@Test
	public void wontFindAnythingBecausePathEndsOnPropertyTree() {
		PropertyTree tree = FixedPropertyTree.builder()
				.put("sub", FixedPropertyTree.builder()
						.put("simple", 17)
						.build())
				.build();
		
		assertFalse(tree.find(Integer.class, "sub").isPresent());
	}
	
	@Test
	public void wontFindAnythingBecauseOfNoMatchingPropertyTree() {
		PropertyTree tree = FixedPropertyTree.builder()
				.put("sub", 19)
				.build();
		
		assertFalse(tree.find(Integer.class, "sub", "simple").isPresent());
	}
	
	@Test
	public void wontFindAnythingBecauseOfTypeMissmatch() {
		PropertyTree tree = FixedPropertyTree.builder()
				.put("simple", 19)
				.build();
		
		assertFalse(tree.find(String.class, "simple").isPresent());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void emptyPathIsInvalid() {
		PropertyTree tree = FixedPropertyTree.builder()
				.put("simple", 19)
				.build();
		
		tree.find(String.class);
	}
}
