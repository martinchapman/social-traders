package edu.cuny.util;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * An interface for object registry, where the references are stored to all
 * sorts of objects so as to avoid direct references between objects.
 * </p>
 * 
 * <p>
 * Multiple set of objects can be stored in the registry, each set for a system,
 * which is identified by a string. This, for example, enables to support
 * multiple random number engines, each for one of interacting systems.
 * </p>
 * 
 * <p>
 * In some cases, classes in a utility package need access to a common
 * supporting object and the classes are used across different systems. To avoid
 * the conflict, an additional system, i.e., a separate set of objects, is used
 * for the package and is identified by the full name of the package. It is the
 * responsibility of other systems to update the objects for this package system
 * to make sure a correct collection of objects is used by classes in the
 * package at different moments.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public interface ObjectRegistry {

	/**
	 * 
	 * @param system
	 * @param type
	 * @param value
	 */
	public void put(String system, Object type, Object value);

	/**
	 * 
	 * @param system
	 * @param pairs
	 */
	public void put(String system, Map<Object, Object> pairs);

	/**
	 * 
	 * @param system
	 * @return a map of all objects registered for the given system and
	 *         successfully removed from the registry.
	 */
	public Map<Object, Object> remove(String system);

	/**
	 * 
	 * @param system
	 * @param key
	 * @return the object registered for the given system, mapped from the given
	 *         key, and successfully removed from the registry.
	 */
	public Object remove(String system, Object key);

	/**
	 * 
	 * @param system
	 * @return a map of objects registered for the given system.
	 */
	public Map<Object, Object> get(String system);

	/**
	 * 
	 * @param system
	 * @param key
	 * @return the object registered for the given system and mapped from the
	 *         given key.
	 */
	public Object get(String system, Object key);

	/**
	 * 
	 * @param <V>
	 * @param system
	 * @param type
	 * @return the single object of type V registered for the given system and
	 *         mapped from Class<V>.
	 */
	public <V> V getTyped(String system, Class<V> type);

	/**
	 * 
	 * @return a set of strings, each identifing an object system.
	 */
	public Set<String> systems();

	/**
	 * 
	 * @return a string that represents the currently active system.
	 */
	public String getDefaultSystem();
}
