package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.util.SearchRequestConverter;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.ofbiz.core.entity.GenericValue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public abstract class AbstractSearchRequestUpgradeTask extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(AbstractSearchRequestUpgradeTask.class);

    private final OfBizDelegator delegator;

    protected AbstractSearchRequestUpgradeTask(OfBizDelegator delegator)
    {
        super(false);
        this.delegator = delegator;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        List<GenericValue> requests = getSearchRequestGVs();
        SearchRequestConverter converter = getConverter();
        for (final GenericValue searchRequestGv : requests)
        {
            String xml = searchRequestGv.getString("request");

            Document original = readDocument(xml);
            Document result = converter.process(original);

            searchRequestGv.setString("request", getDocumentAsString(result));
            searchRequestGv.store();
            log.info("Upgraded search request " + searchRequestGv.getString("name"));
        }
    }

    private List<GenericValue> getSearchRequestGVs()
    {
        return delegator.findAll("SearchRequest");
    }

    private Document readDocument(String xml) throws DocumentException
    {
        SAXReader xmlReader = new SAXReader();

        Reader stringReader = new StringReader(xml);
        Document original = xmlReader.read(stringReader);
        return original;
    }

    private String getDocumentAsString(Document result) throws IOException
    {
        return result.asXML();
    }

    protected abstract SearchRequestConverter getConverter();
}
