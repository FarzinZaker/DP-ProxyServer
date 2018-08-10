package ceraslabs.hogna.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import ceraslabs.hogna.executor.actuators.IActuator;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

/*
 * <executor>
 *   <actuators>
 *     <actuator type="com.ceraslabs.hogna.executor.SshActuator" commands="ssh.set-tomcat6-threads" />
 *     <actuator type="com.ceraslabs.hogna.executor.DummyActuator" commands="ec2.build-topology, ec2.add-instances, ec2.remove-instances" />
 *   </actuators>
 * </executor>
 */

public class ExecutorConfigurationSection extends ConfigurationSection
{
	private static final String TAG_ROOT = "executor";

	private static final String TAG_ACTUATORS = "actuators";
	
	private static final String TAG_ACTUATOR = "actuator";
	private static final String ATTRIBUTE_ACTUATOR_TYPE     = "type";
	private static final String ATTRIBUTE_ACTUATOR_COMMANDS = "commands";

	//private static final String TAG_ACTUATOR_CONFIG = "config";
	
	private List<ActuatorConfig> m_lstActuators = new ArrayList<>();

	@Override
	protected void ParseSection(Element elemSection)
	{
		Trace.Assert (elemSection.getNodeName() == TAG_ROOT,
				"ExecutorConfigurationSection received [%s] section instead of [%s]. Did the name of the section changed?",
				elemSection.getNodeName(), TAG_ROOT);
		Trace.WriteLine (TraceLevel.DEBUG, "Parsing configuration section [%s].", TAG_ROOT);

		NodeList nodes = elemSection.getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i)
		{
			// remove text nodes, that contain only white spaces
			if (nodes.item(i) instanceof Text && ((Text)nodes.item(i)).getData().trim().length() == 0)
			{
				continue;
			}
			Element elemChild = (Element)nodes.item(i);

			if (elemChild.getNodeName().equals(TAG_ACTUATORS))
			{
				this.ParseActuators(elemChild);
			}
		}
	}
	
	private void ParseActuators(Element elem)
	{
		NodeList nodesActuators = elem.getElementsByTagName(TAG_ACTUATOR);
		for (int i = 0; i < nodesActuators.getLength(); ++i)
		{
			try
			{
				Element elemActuator = (Element)nodesActuators.item(i);
				String sActuator    = elemActuator.getAttribute(ATTRIBUTE_ACTUATOR_TYPE);
				String sLstCommands = elemActuator.getAttribute(ATTRIBUTE_ACTUATOR_COMMANDS);
				
				ActuatorConfig actuatorCfg = new ActuatorConfig();
				actuatorCfg.sActuatorCommands = new ArrayList<>();
				for(String sCommand : sLstCommands.split(","))
				{
					actuatorCfg.sActuatorCommands.add(sCommand.trim());
				}
				
				/*
				 *  TODO: configure the actuator
				 *  Allow something like:
				 *  
				 *  <actuator type="com.ceraslabs.hogna.executor.SshActuator" commands="ssh.set-tomcat6-threads">
				 *      ...
				 *  </actuator>
				 *  
				 */
				NodeList nodes = elemActuator.getChildNodes();
				for (int j = 0; j < nodes.getLength(); ++j)
				{
					// remove comments
					if (nodes.item(j) instanceof Comment)
					{
						continue;
					}
					// remove text nodes, that contain only white spaces
					if (nodes.item(j) instanceof Text && ((Text)nodes.item(j)).getData().trim().length() == 0)
					{
						continue;
					}
					Element elemChild = (Element)nodes.item(j);
					try
					{
						JAXBContext context = JAXBContext.newInstance(Class.forName(sActuator));
						Unmarshaller um = context.createUnmarshaller();
						actuatorCfg.theActuator = (IActuator)um.unmarshal(elemChild);
					}
					catch (JAXBException e) { Trace.WriteException(e); }
				}

				// check if there was a configuration for the actuator.
				// if none, then simply instantiate it.
				if (actuatorCfg.theActuator == null)
				{
					actuatorCfg.theActuator = (IActuator) Class.forName(sActuator).newInstance();
				}
				this.m_lstActuators.add(actuatorCfg);
			}
			catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
			{
				Trace.WriteException(e);
			}
		}
	}
	
	public List<ActuatorConfig> GetActuatorsConfig()
	{
		return this.m_lstActuators;
	}
	
	public class ActuatorConfig
	{
		
		public IActuator theActuator;
		public List<String> sActuatorCommands;
	}
}
