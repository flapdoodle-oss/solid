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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;

import de.flapdoodle.types.Either;

//wie json

//key = value -> [value]
//key = [ ... ]
//key = { ... } -> [{}]
//value -> String | Number | Date
//key = [{}]|[value]

//map = key -> [ value | map ]
public interface PropertyTree {
	Set<String> properties();
	List<Either<Object, ? extends PropertyTree>> get(String key);
	
	default String prettyPrinted() {
		return PropertyTreePrinter.prettyPrinted(this);
	}
	
	default Either<Object,? extends PropertyTree> single(String key) {
		List<Either<Object, ? extends PropertyTree>> result = get(key);
		Preconditions.checkArgument(result.size()==1,"more or less than one element: %s", result);
		return result.get(0);
	}
	
	default <T> Optional<T> find(Class<T> type, String ... path) {
		return find(type, FluentIterable.from(path));
	}
	
	default <T> Optional<T> find(Class<T> type, Iterable<String> path) {
		Iterator<String> iterator = path.iterator();
		Preconditions.checkArgument(iterator.hasNext(),"empty path: %s",path);
		Optional<PropertyTree> current = Optional.of(this);
		
		while (current.isPresent() && iterator.hasNext()) {
			String key=iterator.next();
			List<Either<Object, ? extends PropertyTree>> list = current.get().get(key);
			if (list.size()==1) {
				Either<Object, ? extends PropertyTree> value = list.get(0);
				if (value.isLeft()) {
					if (!iterator.hasNext()) {
						Object leftValue = value.left();
						return type.isInstance(leftValue) ? Optional.of((T) leftValue) : Optional.empty();
					}
				} else {
					if (iterator.hasNext()) {
						current=Optional.of(value.right());
					}
				}
			} else {
				break;
			}
		}
		
		return Optional.empty();
	}

}
