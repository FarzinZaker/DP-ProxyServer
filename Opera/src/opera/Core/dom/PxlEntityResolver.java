package opera.Core.dom;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PxlEntityResolver implements EntityResolver
{

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException
	{
        if (systemId.contains("Opera.dtd"))
        {
        	URL dtdPath = null;
        	try
        	{
            	// check if  the dtd file exists
        		if (new File(new URI(systemId)).exists())
        		{
        			dtdPath = new URL(systemId);
        		}
        	}
        	catch (Exception ex) { }

        	// path not valid. use the default one
        	if (dtdPath == null)
        	{
        		dtdPath = this.getClass().getResource("/resources/Opera.dtd");
        	}

        	InputSource is = new InputSource(dtdPath.openStream());

            return is;
        }
        return null;
	}
}
