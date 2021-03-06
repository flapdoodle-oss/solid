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
package de.flapdoodle.solid.theme;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vavr.Tuple;

public class LinksTest {

	@Test
	public void stripDomainPart() {
		assertEquals(Tuple.of("http://foo.bar", "/"), Links.splitDomainPart("http://foo.bar/"));
		assertEquals(Tuple.of("http://foo.bar", "/bar"), Links.splitDomainPart("http://foo.bar/bar"));
		assertEquals(Tuple.of("http://foo.bar", "/baz/nix"), Links.splitDomainPart("http://foo.bar/baz/nix"));
		assertEquals(Tuple.of("http://foo.bar", "/"), Links.splitDomainPart("http://foo.bar"));
		assertEquals(Tuple.of("", "/blub"), Links.splitDomainPart("/blub"));
	}
}
