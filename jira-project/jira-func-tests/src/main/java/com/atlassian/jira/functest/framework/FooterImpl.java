package com.atlassian.jira.functest.framework;

import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import org.xml.sax.SAXException;

class FooterImpl implements Footer
{
    private WebResponse webResponse;
    private WebLink reportProblemLink;

    FooterImpl(WebResponse webResponse)
    {
        this.webResponse = webResponse;
    }

    @Override
    public WebLink getReportProblemLink()
    {
        if (reportProblemLink == null)
        {
            try
            {
                reportProblemLink = webResponse.getLinkWithID("footer-report-problem-link");
            }
            catch (SAXException e)
            {
                throw new RuntimeException(e);
            }
        }
        return reportProblemLink;
    }
} 
