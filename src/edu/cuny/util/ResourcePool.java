/*
 * JCAT - TAC Market Design Competition Platform
 * Copyright (C) 2006-2010 Jinzhong Niu, Kai Cai
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package edu.cuny.util;

import org.apache.commons.collections15.Buffer;
import org.apache.commons.collections15.BufferUtils;
import org.apache.commons.collections15.buffer.UnboundedFifoBuffer;
import org.apache.log4j.Logger;

/**
 * A class for maintaining resources in a pool.
 * 
 * @param <R>
 *          the type of resources that this pool maintains.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class ResourcePool<R extends Object> {

	static Logger logger = Logger.getLogger(ResourcePool.class);

	public static final int DEFAULT_INITIAL_CAPACITY = 100;

	protected ResourceFactory<R> factory;

	protected Buffer<R> buffer;

	public ResourcePool(final ResourceFactory<R> factory,
			final int initial_capacity) {
		this.factory = factory;
		this.buffer = BufferUtils.synchronizedBuffer(new UnboundedFifoBuffer<R>());

		for (int i = 0; i < initial_capacity; i++) {
			addResource();
		}
	}

	public ResourcePool(final ResourceFactory<R> factory) {
		this(factory, ResourcePool.DEFAULT_INITIAL_CAPACITY);
	}

	protected synchronized void addResource() {
		buffer.add(factory.create());
	}

	/**
	 * gets the factory class for the resources under control.
	 * 
	 * @return the factory class to create the resource
	 */
	public ResourceFactory<R> getFactory() {
		return factory;
	}

	/**
	 * gets a resource that is available in the pool.
	 * 
	 * @return the available resource
	 */
	public synchronized R get() {
		if (buffer.isEmpty()) {
			addResource();
		}

		return buffer.remove();
	}

	/**
	 * puts the resource back to the pool.
	 * 
	 * @param resource
	 *          the resource to be returned
	 */
	public void put(final R resource) {
		buffer.add(resource);
	}
}