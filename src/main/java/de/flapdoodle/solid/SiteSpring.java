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
package de.flapdoodle.solid;

import java.nio.file.Path;

import com.google.common.base.Supplier;

import de.flapdoodle.solid.parser.DefaultSiteFactory;
import de.flapdoodle.solid.parser.content.BlobParser;
import de.flapdoodle.solid.parser.content.DefaultBlobParser;
import de.flapdoodle.solid.parser.content.Site;
import de.flapdoodle.solid.parser.types.PropertyTreeParserFactory;

public interface SiteSpring extends Supplier<Site> {

	public static SiteSpring withPath(Path siteRoot) {
		return () -> {
			PropertyTreeParserFactory parserFactory = PropertyTreeParserFactory.defaultFactory();
			BlobParser blobParser = new DefaultBlobParser(parserFactory);
			DefaultSiteFactory siteFactory = new DefaultSiteFactory(parserFactory, blobParser);
			return siteFactory.siteOf(siteRoot);
		};
	}
}
