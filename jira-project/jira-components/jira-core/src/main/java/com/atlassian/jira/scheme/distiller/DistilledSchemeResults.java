package com.atlassian.jira.scheme.distiller;

import com.atlassian.jira.scheme.Scheme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class will contain two collections. One containing a Collection of un-matched schemes (those that could not be
 * 'distilled') And a Collection of DistilledSchemeResult objects, those schemes that can be distilled.
 */
public class DistilledSchemeResults
{
    private final List distilledSchemeResults;
    private final Collection unDistilledSchemes;
    private final String schemeType;

    public DistilledSchemeResults(String schemeType)
    {
        this.schemeType = schemeType;
        this.distilledSchemeResults = new ArrayList();
        this.unDistilledSchemes = new ArrayList();
    }

    public DistilledSchemeResults(Collection distilledSchemeResults, Collection unDistilledSchemes, String schemeType)
    {
        this.schemeType = schemeType;
        if (distilledSchemeResults == null)
        {
            this.distilledSchemeResults = new ArrayList();
        }
        else
        {
            this.distilledSchemeResults = new ArrayList(distilledSchemeResults);
        }

        if (unDistilledSchemes == null)
        {
            this.unDistilledSchemes = new ArrayList();
        }
        else
        {
            this.unDistilledSchemes = unDistilledSchemes;
        }
    }

    public Collection getDistilledSchemes()
    {
        Collection distilledSchemes = new ArrayList();
        for (final Object distilledSchemeResult1 : distilledSchemeResults)
        {
            DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) distilledSchemeResult1;
            distilledSchemes.add(distilledSchemeResult.getResultingScheme());
        }
        return distilledSchemes;
    }

    public void addDistilledSchemeResult(DistilledSchemeResult distilledSchemeResult)
    {
        this.distilledSchemeResults.add(distilledSchemeResult);
    }

    public Collection getDistilledSchemeResults()
    {
        Collections.sort(distilledSchemeResults, new DistilledSchemeResultComparator());

        return distilledSchemeResults;
    }

    public void addUndistilledScheme(Scheme scheme)
    {
        this.unDistilledSchemes.add(scheme);
    }

    public Collection getUnDistilledSchemes()
    {
        return Collections.unmodifiableCollection(unDistilledSchemes);
    }

    public String getSchemeType()
    {
        return schemeType;
    }

    private static class DistilledSchemeResultComparator implements Comparator, Serializable
    {
        static final long serialVersionUID = -7050803095518619538L;

        public int compare(Object o1, Object o2)
        {
            if (o1 != null && o2 != null)
            {
                DistilledSchemeResult distilledSchemeResult1 = (DistilledSchemeResult) o1;
                DistilledSchemeResult distilledSchemeResult2 = (DistilledSchemeResult) o2;

                Scheme resultingScheme1 = distilledSchemeResult1.getResultingScheme();
                Scheme resultingScheme2 = distilledSchemeResult2.getResultingScheme();
                if (resultingScheme1 != null && resultingScheme2 != null)
                {
                    return resultingScheme1.getName().compareTo(resultingScheme2.getName());
                }
            }
            return 0;
        }
    }

}
