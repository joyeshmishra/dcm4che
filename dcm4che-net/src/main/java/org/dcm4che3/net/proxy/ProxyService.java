package org.dcm4che3.net.proxy;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

/**
 * Service that retrieves the Service Provider Implementations of Proxy Manager.
 * 
 * @author Amaury Pernette
 *
 */
public class ProxyService {

	// Initialize-on-Demand Holder Class Idiom
	private static class SingletonHolder {
		private static ProxyService service = new ProxyService();
	}
	
	private Map<String, Map<String, ProxyManager>> managersModel;
	
	private ProxyService() {
		this.managersModel = new HashMap<String, Map<String,ProxyManager>>();		
		final ServiceLoader<ProxyManager> loader = ServiceLoader.load(ProxyManager.class);
		// Load managers in internal model
		final Iterator<ProxyManager> managers = loader.iterator();
		while (managers.hasNext()) {
			final ProxyManager proxyManager = managers.next();			
			// Manager provider map
			final String providerName = proxyManager.getProviderName();
			if(!this.managersModel.containsKey(providerName)) {
				this.managersModel.put(providerName, new TreeMap<String, ProxyManager>());
			}
			final Map<String, ProxyManager> providerManagers = this.managersModel.get(providerName);
			// Manager version
			final String version = proxyManager.getVersion();
			providerManagers.put(version, proxyManager);
		}
	}
	
	public static synchronized ProxyService getInstance() {
		return SingletonHolder.service;
//		if(service == null){
//			service = new ProxyService();
//		}
//		return service;
	}
	
	public ProxyManager getProxyManager(final String managerProviderName, final String managerVersion) {
		if(managerProviderName != null && !managerProviderName.isEmpty()){
			// Get managers for this provider
			final Map<String, ProxyManager> providerManagers = this.managersModel.get(managerProviderName);
			if(providerManagers != null && !providerManagers.isEmpty()) {
				ProxyManager manager = null;
				if(managerVersion != null && !managerVersion.isEmpty()) {
					// Get specific version
					manager = providerManagers.get(managerVersion);
				}
				// If no specified version or unavailable version : get the last available
				if(manager == null) {
					final List<String> versions = new ArrayList<String>(providerManagers.keySet());
					Collections.reverse(versions);
					manager = providerManagers.get(versions.get(0));
				}
			}				
		}
		return null;
	}
}
