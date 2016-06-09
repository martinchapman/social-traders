package edu.cuny.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * <p>
 * An implementation of {@link ObjectRegistry} that uses {@link java.util.Map}.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class Galaxy implements ObjectRegistry {

	static Logger logger = Logger.getLogger(Galaxy.class);

	protected static Galaxy instance;

	protected String defaultSystem;

	protected SortedMap<String, Map<Object, Object>> supernova;

	public static Galaxy getInstance() {
		if (Galaxy.instance == null) {
			Galaxy.instance = new Galaxy();
		}

		return Galaxy.instance;
	}

	private Galaxy() {
		supernova = Collections
				.synchronizedSortedMap(new TreeMap<String, Map<Object, Object>>());
	}

	public void put(final String system, final Object key, final Object value) {
		Map<Object, Object> objects = null;

		if (supernova.containsKey(system)) {
			objects = supernova.get(system);
		} else {
			objects = Collections.synchronizedMap(new HashMap<Object, Object>());
			supernova.put(system, objects);
		}

		objects.put(key, value);
	}

	public void put(final String system, final Map<Object, Object> pairs) {
		supernova.put(system, pairs);
	}

	public Map<Object, Object> remove(final String system) {
		return supernova.remove(system);
	}

	public Object remove(final String system, final Object key) {
		if (supernova.containsKey(system)) {
			return supernova.get(system).remove(key);
		} else {
			return null;
		}
	}

	public Map<Object, Object> get(final String system) {
		return supernova.get(system);
	}

	public Object get(final String system, final Object key) {
		if (supernova.containsKey(system)) {
			return supernova.get(system).get(key);
		} else {
			Galaxy.logger.fatal(key.toString() + " unavailable for system " + system
					+ " in " + getClass().getSimpleName() + " !");
			return null;
		}
	}

	public <V> V getTyped(final String system, final Class<V> type) {
		final Object obj = get(system, type);

		if ((obj != null) && type.isInstance(obj)) {
			return type.cast(obj);
		}
		return null;
	}

	public <V> V getDefaultTyped(final Class<V> type) {
		return getTyped(getDefaultSystem(), type);
	}

	public Set<String> systems() {
		return supernova.keySet();
	}

	public void setDefaultSystem(final String defaultSystem) {
		this.defaultSystem = defaultSystem;
	}

	public String getDefaultSystem() {
		return defaultSystem;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
