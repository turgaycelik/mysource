package com.atlassian.jira.rest.v2.entity.property;

import com.atlassian.fugue.Function2;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.entity.WithKey;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.EntityWithKeyPropertyService;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.2
 */
public class BasePropertyWithKeyResourceTest
{
    @Mock public EntityWithKeyPropertyService<ArtificialEntity> entityPropertyService;
    @Mock public JiraAuthenticationContext authenticationContext;
    @Mock public ApplicationUser user;
    @Mock public JiraBaseUrls jiraBaseUrls;
    @Mock public I18nHelper i18n;
    @Mock public Function2<Long, String, String> entityIdAndPropertyKeyToSelfFunction;
    @Mock public Predicate<String> keyPredicate;
    @Mock @AvailableInContainer public ApplicationProperties applicationProperties;

    @Rule public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    @Rule public final ExpectedException exception = ExpectedException.none();

    private BasePropertyWithKeyResource<ArtificialEntity> basePropertyResource;

    @Before
    public void setup()
    {
        when(applicationProperties.getEncoding()).thenReturn(Charsets.UTF_8.toString());
        when(authenticationContext.getUser()).thenReturn(user);
        this.basePropertyResource = new BasePropertyWithKeyResource<ArtificialEntity>(entityPropertyService, authenticationContext, jiraBaseUrls, i18n, keyPredicate, entityIdAndPropertyKeyToSelfFunction, EntityPropertyType.ISSUE_PROPERTY);
    }

    @Test
    public void badRequestWhenInvalidKeyOrId()
    {
        when(keyPredicate.apply(eq("xyz"))).thenReturn(false);
        exception.expect(BadRequestWebException.class);
        basePropertyResource.getPropertiesKeys(user, "xyz").getKeys();
    }

    private static interface ArtificialEntity extends WithId, WithKey { }
}
