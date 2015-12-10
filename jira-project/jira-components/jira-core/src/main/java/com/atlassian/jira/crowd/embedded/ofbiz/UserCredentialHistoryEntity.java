package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.transform;

class UserCredentialHistoryEntity
{
    static final String ENTITY = "UserCredentialHistory";
    static final String ID = "id";
    static final String USER_ID = "userId";
    static final String CREDENTIAL = "credential";
    static final String LIST_INDEX = "listIndex";

    static Map<String, Object> getData(final Long userId, final String credential, final Integer order)
    {
        final Map<String, Object> data = Maps.newHashMap();
        data.put(USER_ID, userId);
        data.put(CREDENTIAL, credential);
        data.put(LIST_INDEX, order);

        return data;
    }

    static List<PasswordCredential> toCredentials(final List<GenericValue> credentialGenericValues)
    {
        return transform(credentialGenericValues, TO_CREDENTIAL);
    }

    private static Function<GenericValue, PasswordCredential> TO_CREDENTIAL = new Function<GenericValue, PasswordCredential>()
    {
        public PasswordCredential apply(final GenericValue genericValue)
        {
            return new PasswordCredential(String.valueOf(genericValue.get(CREDENTIAL)), true);
        }
    };

    private UserCredentialHistoryEntity()
    {
    }
}
