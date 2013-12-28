package org.theospi.portfolio.presentation.support;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.DerivedCache;
import org.sakaiproject.memory.api.MemoryService;

public class ShareUserCacheImpl implements DerivedCache, CacheEventListener {
	   
	protected final Log logger = LogFactory.getLog(getClass());
	protected Cache cache = null;
	private ServerConfigurationService serverConfigurationService;
	private int cacheEventReportInterval = 0;

	
	// Modify constructor to allow injecting the server configuration service.
	public ShareUserCacheImpl(MemoryService memoryService, long sleep, String pattern, ServerConfigurationService serverConfigurationService)
	{
		cache = memoryService.newCache(
				"org.theospi.portfolio.presentation.support.ShareUserCacheImpl.cache", pattern);

		// setup as the derived cache
		cache.attachDerivedCache(this);

		// Provide an instance of the server configuration service.
		this.serverConfigurationService = serverConfigurationService;

		cacheEventReportInterval = serverConfigurationService.getInt("org.theospi.portfolio.presentation.support.ShareUserCacheImpl.cache.cacheEventReportInterval",
				cacheEventReportInterval);
	}

	// Supply a default server configuration service if it is not supplied.
	public ShareUserCacheImpl(MemoryService memoryService, long sleep, String pattern) {
		this(memoryService,sleep,pattern,
				(ServerConfigurationService)org.sakaiproject.component.cover.ServerConfigurationService.getInstance());
	}
	
	
	/**
	 * Cache an object
	 * 
	 * @param key
	 *        The key with which to find the object.
	 * @param payload
	 *        The object to cache.
	 * @param duration
	 *        The time to cache the object (seconds).
	 */
	public void put(String key, Object payload)
	{
		cache.put(key, payload);
	}

	/**
	 * Test for a non expired entry in the cache.
	 * 
	 * @param key
	 *        The cache key.
	 * @return true if the key maps to a non-expired cache entry, false if not.
	 */
	public boolean containsKey(String key)
	{
		return cache.containsKey(key);
	}

	/**
	 * Get the non expired entry, or null if not there (or expired)
	 * 
	 * @param key
	 *        The cache key.
	 * @return The payload, or null if the payload is null, the key is not found, or the entry has expired (Note: use containsKey() to remove this ambiguity).
	 */
	public Object get(String key)
	{
		return cache.get(key);
	}

	/**
	 * Clear all entries.
	 */
	public void clear()
	{
		cache.clear();
	}

	/**
	 * Remove this entry from the cache.
	 * 
	 * @param key
	 *        The cache key.
	 */
	public void remove(String key)
	{
		cache.remove(key);
	}


	public void dispose() {
		logger.debug("ehcache event: dispose");	
	}

	public void notifyElementEvicted(Ehcache arg0, Element arg1) {
	    logger.debug("**** ElementEvicted: " + arg0 + ", " + arg1);
	}

	public void notifyElementExpired(Ehcache arg0, Element arg1) {
        logger.debug("**** ElementExpired: " + arg0 + ", " + arg1);
	}

	public void notifyElementPut(Ehcache arg0, Element arg1)
			throws CacheException {
        logger.debug("**** ElementPut: " + arg0 + ", " + arg1);
	}

	public void notifyElementRemoved(Ehcache arg0, Element arg1)
			throws CacheException {
        logger.debug("**** ElementRemoved: " + arg0 + ", " + arg1);
	}

	public void notifyElementUpdated(Ehcache arg0, Element arg1)
			throws CacheException {
        logger.debug("**** ElementUpdated: " + arg0 + ", " + arg1);
	}

	public void notifyRemoveAll(Ehcache arg0) {
        logger.debug("**** Removeall: " + arg0);
	}

	public void notifyCacheClear() {
        logger.debug("**** CacheClear");
	}

	public void notifyCachePut(Object arg0, Object arg1) {
        logger.debug("**** CachePut: " + arg0 + ", " + arg1);
	}

	public void notifyCacheRemove(Object arg0, Object arg1) {
        logger.debug("**** CacheRemove: " + arg0 + ", " + arg1);
	}

	@Override
	public Object clone() throws CloneNotSupportedException 
	{
		logger.debug("event: clone()");
		
		// Creates a clone of this listener. This method will only be called by ehcache before a cache is initialized.
		// This may not be possible for listeners after they have been initialized. Implementations should throw CloneNotSupportedException if they do not support clone.
		throw new CloneNotSupportedException(
				"CacheEventListener implementations should throw CloneNotSupportedException if they do not support clone");
	}
}
