package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntegrityChecker
{
    private final IntegrityCheckManager integrityCheckManager;

    public IntegrityChecker(IntegrityCheckManager integrityCheckManager)
    {
        this.integrityCheckManager = integrityCheckManager;
    }

    public Map preview(List<Check> checks) throws IntegrityException
    {
        Map results = new HashMap();
        for (Check check : checks)
        {
            results.put(check, preview(check));
        }
        return results;
    }

    public Map previewWithIds(List<Long> checkIds) throws IntegrityException
    {
        Map results = new HashMap();
        for (Long checkId : checkIds)
        {
            Check check = integrityCheckManager.getCheck(checkId);
            results.put(check, preview(check));
        }
        return results;
    }

    public Map correct(List<Check> checks) throws IntegrityException
    {
        Map results = new HashMap();
        for (Check check : checks)
        {
            results.put(check, correct(check));
        }
        return results;
    }

    public Map correctWithIds(List<Long> checkIds) throws IntegrityException
    {
        Map results = new HashMap();
        for (Long checkId : checkIds)
        {
            Check check = integrityCheckManager.getCheck(checkId);
            results.put(check, correct(check));
        }
        return results;
    }

    public List preview(Check check) throws IntegrityException
    {
        return check.preview();
    }

    public List correct(Check check) throws IntegrityException
    {
        return check.correct();
    }
}
