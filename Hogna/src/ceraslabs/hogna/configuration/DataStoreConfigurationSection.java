package ceraslabs.hogna.configuration;

import ceraslabs.hogna.data.IDataStore;

public abstract class DataStoreConfigurationSection extends ConfigurationSection
{
	public abstract IDataStore GetDataStore();
}
