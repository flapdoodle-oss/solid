package de.flapdoodle.solid.types;

import java.util.Date;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public final class ImmutablePropertyTreeMap implements PropertyTreeMap {
	
	private static final ImmutableSet<Class<?>> VALID_VALUE_TYPES=ImmutableSet.<Class<?>>builder()
			.add(Double.class, Float.class, String.class, Character.class, Boolean.class)
			.add(Integer.class, Date.class)
			.build();
	private final ImmutableMap<String, Object> map;
	
	private ImmutablePropertyTreeMap(ImmutableMap<String, Object> map) {
		this.map = map;
	}
	
	public ImmutableMap<String, Object> asMap() {
		return map;
	}
	
	@Override
	public Object get(String key, String... keys) {
		Object ret = map.get(key);
		for (String k : keys) {
			if (ret instanceof Map) {
				ret=((Map) ret).get(k);
			}
		}
		return ret;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(getClass())
				.addValue(map)
				.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImmutablePropertyTreeMap other = (ImmutablePropertyTreeMap) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}

	public static MapBuilder builder() {
		return new MapBuilder(null);
	}
	
	public static class MapBuilder {
		
		private final Map<String, Object> map=Maps.newLinkedHashMap();
		private final MapBuilder parent;
		
		private MapBuilder(MapBuilder parent) {
			this.parent = parent;
		}
		
		public MapBuilder put(String key, Object value) {
			Object old = map.put(key, asImmutable(value));
			Preconditions.checkArgument(old==null,"value for %s was already set to %s",key,old);
			return this;
		}
		
		public MapBuilder start(String key) {
			Object value = map.get(key);
			Preconditions.checkArgument(value==null,"property %s already set to %s",key,value);
			
			MapBuilder sub = new MapBuilder(this);
			map.put(key, sub);
			return sub;
		}
		
		public MapBuilder end() {
			return parent;
		}
		
		private Object asImmutable(Object value) {
			Preconditions.checkArgument(VALID_VALUE_TYPES.contains(value.getClass()) || value instanceof Iterable,"not a valid type: %s (not %s,%s)",value.getClass(), VALID_VALUE_TYPES, Iterable.class);
			if (value instanceof Iterable) {
				return FluentIterable.from((Iterable) value).transform(v -> asImmutable(v)).toList();
			}
			return value;
		}

		public ImmutablePropertyTreeMap build() {
			return new ImmutablePropertyTreeMap(buildMap());
		}

		private ImmutableMap<String, Object> buildMap() {
			ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
			map.forEach((key,value) -> {
				if (value instanceof MapBuilder) {
					builder.put(key,((MapBuilder) value).buildMap());
				} else {
					builder.put(key,value);
				}
			});
			return builder.build();
		}
	}
	
}
