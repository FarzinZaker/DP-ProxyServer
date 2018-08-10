package opera.Core.dom;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class OperaErrorHandler implements ErrorHandler
{
    /** Warning. */
    public void warning(SAXParseException ex)
    {
        System.err.println("[Warning] " + getLocationString(ex) + ": " + ex.getMessage());
    }

    /** Error. */
    public void error(SAXParseException ex)
    {
        System.err.println("[Error] " + getLocationString(ex) + ": " + ex.getMessage());
    }

    /** Fatal error. */
    public void fatalError(SAXParseException ex) throws SAXException
    {
    	String sMsg = "[Fatal Error] " + getLocationString(ex) + ": " + ex.getMessage();
        System.err.println(sMsg);
        throw new SAXException(sMsg);
    }

    //
    // Private methods
    //

    /** Returns a string of the location. */
    private String getLocationString(SAXParseException ex)
    {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null)
        {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
            {
                systemId = systemId.substring(index + 1);
            }
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();
    } // getLocationString(SAXParseException):String
}

