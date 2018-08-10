package ceraslabs.hogna.configuration;

import org.w3c.dom.Element;

public abstract class ConfigurationSection
{
	protected abstract void ParseSection(Element elemSection);
}
