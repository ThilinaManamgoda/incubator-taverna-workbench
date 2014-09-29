package net.sf.taverna.t2.servicedescriptions;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

public interface ServiceDescriptionRegistry extends
		Observable<ServiceDescriptionRegistryEvent> {
	void addServiceDescriptionProvider(ServiceDescriptionProvider provider);

	Set<ServiceDescriptionProvider> getDefaultServiceDescriptionProviders();

	Set<ServiceDescriptionProvider> getServiceDescriptionProviders();

	Set<ServiceDescriptionProvider> getServiceDescriptionProviders(
			ServiceDescription sd);

	Set<ServiceDescription> getServiceDescriptions();

	ServiceDescription getServiceDescription(URI activityType);

	List<ConfigurableServiceProvider<?>> getUnconfiguredServiceProviders();

	Set<ServiceDescriptionProvider> getUserAddedServiceProviders();

	Set<ServiceDescriptionProvider> getUserRemovedServiceProviders();

	void loadServiceProviders() throws DeserializationException;

	void loadServiceProviders(File serviceProvidersURL)
			throws DeserializationException;

	void loadServiceProviders(URL serviceProvidersURL)
			throws DeserializationException;

	void refresh();

	void removeServiceDescriptionProvider(ServiceDescriptionProvider provider);

	void saveServiceDescriptions();

	void saveServiceDescriptions(File serviceDescriptionsFile);

	void exportCurrentServiceDescriptions(File serviceDescriptionsFile);

	boolean isDefaultSystemConfigurableProvidersLoaded();
}
