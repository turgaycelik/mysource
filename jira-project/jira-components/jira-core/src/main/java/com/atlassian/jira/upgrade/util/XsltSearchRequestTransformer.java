package com.atlassian.jira.upgrade.util;

import com.atlassian.core.util.ClassLoaderUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class XsltSearchRequestTransformer implements SearchRequestConverter
{
    private static final Logger log = Logger.getLogger(UsersGroupParamConverter.class);
    protected final Transformer transformer;

    public XsltSearchRequestTransformer(String upgradeXslFilename)
    {
        InputStream xslStream = ClassLoaderUtils.getResourceAsStream(upgradeXslFilename, this.getClass());
        // load the transformer
        TransformerFactory factory = TransformerFactory.newInstance();
        try
        {
            transformer = factory.newTransformer(new StreamSource(xslStream));
        }
        catch (TransformerConfigurationException e)
        {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Could not parse XSLT " + upgradeXslFilename + " used for upgrading search requests");
        }
    }


    public Document process(Document document)
    {
        // now lets create the TrAX source and result
        // objects and do the transformation
        Source source = new DocumentSource(document);
        DocumentResult result = new DocumentResult();
        try
        {
            transformer.transform(source, result);
        }
        catch (TransformerException e)
        {
            log.error("Error converting searchrequest document " + document + " returning original search request");
            return document;
        }

        // output the transformed document
        return result.getDocument();
    }
}
