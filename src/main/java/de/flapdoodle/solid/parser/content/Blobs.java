package de.flapdoodle.solid.parser.content;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import de.flapdoodle.solid.generator.PropertyResolver;
import de.flapdoodle.solid.parser.path.Path;
import de.flapdoodle.solid.types.Maybe;
import de.flapdoodle.solid.types.Pair;

public abstract class Blobs {
	
	private Blobs() {
		// no instance
	}

	public static Maybe<?> propertyOf(Blob blob, Function<String, Collection<String>> pathPropertyMapping, PropertyResolver propertyResolver,
			String propertyName) {
		Collection<String> aliasList = pathPropertyMapping.apply(propertyName);
		for (String alias : aliasList) {
			Maybe<?> resolved = propertyResolver.resolve(blob.meta(), Splitter.on('.').split(alias));
			if (resolved.isPresent()) {
				return resolved;
			}
		}
		return Maybe.empty();
	}

	public static ImmutableMap<String, Object> pathPropertiesOf(Blob blob, Function<String, Collection<String>> pathPropertyMapping, Path path, PropertyResolver propertyResolver) {
		ImmutableList<String> pathProperties = path.propertyNames().stream()
			.filter(p -> !Path.PAGE.equals(p))
			.collect(ImmutableList.toImmutableList());
		
		ImmutableMap<String, Object> blopPathPropertyMap = pathProperties.stream()
			.map(p -> Pair.<String, Maybe<?>>of(p, propertyOf(blob, pathPropertyMapping, propertyResolver, p)))
			.filter(pair -> pair.b().isPresent())
			.map(pair -> Pair.<String, Object>of(pair.a(), pair.b().get()))
			.collect(ImmutableMap.toImmutableMap(Pair::a, Pair::b));
		
		return blopPathPropertyMap;
	}

	public static ImmutableMultimap<ImmutableMap<String, Object>, Blob> groupByPage(ImmutableMultimap<ImmutableMap<String, Object>, Blob> src, String pageKey, int itemsPerPage) {
		ImmutableMultimap.Builder<ImmutableMap<String, Object>, Blob> builder = ImmutableMultimap.<ImmutableMap<String, Object>, Blob>builder();
		
		src.asMap().forEach((key, blobs) -> {
			Iterable<List<Blob>> partitions = Iterables.partition(blobs, itemsPerPage);
			AtomicInteger currentPage=new AtomicInteger(0);
			partitions.forEach((List<Blob> partition) -> {
				ImmutableMap<String, Object> newKey = ImmutableMap.<String, Object>builder().putAll(key)
					.put(pageKey, currentPage.incrementAndGet())
					.build();
				builder.putAll(newKey, partition);
			});
		});
		
		return builder.build();
	}

	public static Comparable<?> propertyOf(Blob blob, String property) {
		Maybe<Object> result = blob.meta().find(Object.class, Splitter.on('.').split(property));
		if (result.isPresent() && result.get() instanceof Comparable) {
			return (Comparable<?>) result.get();
		}
		return null;
	}

	public static Ordering<Blob> orderingFor(String property) {
		boolean reverse;
		String cleanedProperty;
		if (property.startsWith("!")) {
			cleanedProperty=property.substring(1);
			reverse=true;
		} else {
			cleanedProperty=property;
			reverse=false;
		}
		
		Ordering<Blob> ret = Ordering.natural().nullsLast()
				.onResultOf(blob -> propertyOf(blob, cleanedProperty));
		return reverse ? ret.reverse() : ret;
	}

	public static ImmutableList<Blob> filter(ImmutableList<Blob> src, Predicate<Blob> filter) {
		return src.stream().filter(filter).collect(ImmutableList.toImmutableList());
	}

	public static Ordering<Blob> comparatorOf(ImmutableList<String> currentOrdering) {
		Preconditions.checkArgument(!currentOrdering.isEmpty(),"invalid ordering: %s",currentOrdering);
		
		ImmutableList<Ordering<Blob>> all = currentOrdering.stream()
			.map(p -> orderingFor(p))
			.collect(ImmutableList.toImmutableList());
		
		return Ordering.compound(all);
	}

	public static ImmutableList<Blob> sort(ImmutableList<Blob> blobs, ImmutableList<String> currentOrdering) {
		Ordering<Blob> comparator=comparatorOf(currentOrdering);
		return comparator.immutableSortedCopy(blobs);
	}
}