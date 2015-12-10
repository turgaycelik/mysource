package com.atlassian.jira.web.bean;

import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperationImpl;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


public class ConvertIssueBean implements OperationContext, Serializable
{
    private static final long serialVersionUID = 2944128397663718521L;
    private static final String SESSION_KEY = "com.atlassian.jira.web.bean.ConvertIssueBean";

    private static final AtomicLong versionCounter = new AtomicLong(0);

    protected String issueId;
    protected Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();

    protected String issueType;
    protected String targetStatusId;
    protected String version;
    protected String sessionKey;
    protected int currentStep = 0;


    public ConvertIssueBean()
    {
        fieldValuesHolder = new HashMap<String, Object>();
    }

    /**
     * Retrieves itself from the session. If not found in the session, a new
     * instance is created and stored in the session.
     *
     * @param session session
     * @param issueId issue id
     * @return bean
     */
    public static ConvertIssueBean getBean(HttpSession session, String issueId)
    {
        return getBeanFromSession(ConvertIssueBean.class, SESSION_KEY, session, issueId);
    }

    public static ConvertIssueBean getBeanFromSession(Class beanClass, String sessionKey, HttpSession session, String issueId)
    {
        final Object foo = session.getAttribute(sessionKey);
        final Map beanMap;
        if (foo == null)
        {
            beanMap = new HashMap(1);
            session.setAttribute(sessionKey, beanMap);
        }
        else
        {
            beanMap = (Map) foo;
        }

        ConvertIssueBean bean = (ConvertIssueBean) beanMap.get(issueId);
        if (bean == null)
        {
            try
            {
                bean = (ConvertIssueBean) beanClass.newInstance();
            }
            catch (InstantiationException e)
            {
                throw new RuntimeException(e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }

            bean.setIssueId(issueId);
            bean.setSessionKey(sessionKey);
            beanMap.put(issueId, bean);
        }

        return bean;
    }


    /**
     * Clears everything in the bean except id.
     */
    public void clearBean()
    {
        fieldValuesHolder.clear();
        issueType = null;
        targetStatusId = null;
        version = null;
    }

    /**
     * Removes itself from the session
     *
     * @param session session
     */
    public void clearSession(HttpSession session)
    {
        final Object foo = session.getAttribute(sessionKey);
        if (foo != null)
        {
            Map beanMap = (Map) foo;
            beanMap.remove(issueId);

            if (beanMap.isEmpty())
            {
                session.removeAttribute(sessionKey);
            }
        }
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName());
        sb.append(" [");
        if (issueId != null)
        {
            sb.append(" issueId:");
            sb.append(issueId);
        }
        if (issueType != null)
        {
            sb.append(" issueType:");
            sb.append(issueType);
        }
        if (targetStatusId != null)
        {
            sb.append(" targetStatusId:");
            sb.append(targetStatusId);
        }
        if (version != null)
        {
            sb.append(" GUID:");
            sb.append(version);
        }
        String extraFields = extraFieldsToString();
        if (extraFields != null)
        {
            sb.append(extraFields);
        }
        sb.append(" ]");
        return sb.toString();
    }

    public String extraFieldsToString()
    {
        return null;
    }

    /**
     * Returns a custom values holder
     *
     * @return a custom values holder
     */
    public Map<String, Object> getFieldValuesHolder()
    {
        return fieldValuesHolder;
    }

    /**
     * Returns the issue operation of this bean
     *
     * @return the issue operation of this bean
     */
    public IssueOperation getIssueOperation()
    {
        return new IssueOperationImpl("Convert Issue Operation", "Convert issue Operation");
    }

    /**
     * Returns issue ID
     *
     * @return issue ID
     */
    public String getIssueId()
    {
        return issueId;
    }

    /**
     * Sets the issue ID
     *
     * @param issueId issue ID
     */
    public void setIssueId(String issueId)
    {
        this.issueId = issueId;
    }

    /**
     * Returns issue type
     *
     * @return issue type
     */
    public String getIssueType()
    {
        return issueType;
    }

    /**
     * Sets issue type
     *
     * @param issueType issue type
     */
    public void setIssueType(String issueType)
    {
        this.issueType = issueType;
    }

    /**
     * Returns target status id
     *
     * @return target status id
     */
    public String getTargetStatusId()
    {
        return targetStatusId;
    }

    /**
     * Sets target status id
     *
     * @param targetStatusId target status id
     */
    public void setTargetStatusId(String targetStatusId)
    {
        this.targetStatusId = targetStatusId;
    }

    public String getVersion()
    {
        return version;
    }

    public void generateNextVersion()
    {
        version = String.valueOf(versionCounter.getAndIncrement());
    }

    public void setCurrentStep(int step)
    {
        this.currentStep = step;
    }

    public int getCurrentStep()
    {
        return currentStep;
    }


    public String getSessionKey()
    {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey)
    {
        this.sessionKey = sessionKey;
    }
}
