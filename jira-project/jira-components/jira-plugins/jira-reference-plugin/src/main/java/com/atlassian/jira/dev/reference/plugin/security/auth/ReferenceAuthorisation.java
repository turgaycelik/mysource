package com.atlassian.jira.dev.reference.plugin.security.auth;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.auth.Authorisation;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;

import static com.atlassian.jira.security.auth.Authorisation.Decision.ABSTAIN;
import static com.atlassian.jira.security.auth.Authorisation.Decision.DENIED;
import static com.atlassian.jira.security.auth.Authorisation.Decision.GRANTED;
import static java.lang.Boolean.parseBoolean;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 */
public class ReferenceAuthorisation implements Authorisation
{
    @Override
    public Decision authoriseForLogin(@Nonnull User user, HttpServletRequest httpServletRequest)
    {
        String webParam = httpServletRequest.getParameter("referenceAuthLogin");
        if (isBlank(webParam))
        {
            return ABSTAIN;
        }
        if ("exception".equalsIgnoreCase(webParam))
        {
            throw new RuntimeException("You asked for it and here is is!  Pow! Alice. Right in the kisser");
        }
        return parseBoolean(webParam) ? GRANTED : DENIED;
    }


    @Override
    public Set<String> getRequiredRoles(HttpServletRequest httpServletRequest)
    {
        Set<String> requiredRoles = Sets.newHashSet();
        String webParam = httpServletRequest.getParameter("referenceAuthRole");
        if (! isBlank(webParam)) {
            requiredRoles.add(webParam);
        }
        return requiredRoles;
    }

    @Override
    public Decision authoriseForRole(@Nullable User user, HttpServletRequest httpServletRequest, String role)
    {
        String answerParam = httpServletRequest.getParameter("referenceAuthAnswer");
        if (isBlank(answerParam))
        {
            return ABSTAIN;
        }
        if ("abstain".equalsIgnoreCase(answerParam))
        {
            return ABSTAIN;
        }
        if ("exception".equalsIgnoreCase(answerParam))
        {
            throw new RuntimeException("You asked for it and here is is!  Pow! Alice. Right in the kisser");
        }
        return parseBoolean(answerParam) ? GRANTED : DENIED;
    }
}
