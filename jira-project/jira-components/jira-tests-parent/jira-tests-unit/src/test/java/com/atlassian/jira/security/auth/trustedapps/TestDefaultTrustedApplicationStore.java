package com.atlassian.jira.security.auth.trustedapps;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableSortedMap;

public class TestDefaultTrustedApplicationStore
{

    private final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    private final TrustedApplicationStore testedObject = new DefaultTrustedApplicationStore(ofBizDelegator);

    @Test
    public void testCreateAndLoadById()
    {
        final TrustedApplicationData toStore = new TrustedApplicationData("someAppId", "someName", "somePublicKey", 54321, new AuditLog(
                "me", new Date(10)), new AuditLog("you", new Date(20)), "192.*", "/some/other/url");

        final TrustedApplicationData stored = testedObject.store(toStore);
        assertEquals(toMapWithoutId(toStore), toMapWithoutId(stored));

        final TrustedApplicationData storedByApplicationId = testedObject.getByApplicationId(stored.getApplicationId());
        assertEquals(toMap(stored), toMap(storedByApplicationId));
    }

    @Test
    public void testCreateAndLoadByApplicationId()
    {
        final TrustedApplicationData toStore = new TrustedApplicationData("someAppId", "someName", "somePublicKey", 54321, new AuditLog(
                "me", new Date(10)), new AuditLog("you", new Date(20)), "192.*", "/some/other/url");

        final TrustedApplicationData stored = testedObject.store(toStore);
        assertEquals(toMapWithoutId(toStore), toMapWithoutId(stored));

        final TrustedApplicationData storedByApplicationId = testedObject.getByApplicationId(stored.getApplicationId());
        assertEquals(toMap(stored), toMap(storedByApplicationId));
    }

    @Test
    public void testCreateAndLoadAll()
    {
        TrustedApplicationData first = new TrustedApplicationData("firstAppId", "firstName", "firstPublicKey", 2, new AuditLog("firstMe",
                new Date(1)), new AuditLog("firstYou", new Date(2)), "255.*", "/first/url");
        first = testedObject.store(first);

        TrustedApplicationData second = new TrustedApplicationData("secondAppId", "secondName", "secondPublicKey", 2, new AuditLog(
                "secondMe", new Date(2)), new AuditLog("secondYou", new Date(3)), "255.*", "/second/url");
        second = testedObject.store(second);

        assertEquals(toMaps(Arrays.asList(first, second)), toMaps(testedObject.getAll()));
    }

    @Test
    public void testUpdate()
    {
        TrustedApplicationData initial = new TrustedApplicationData("someAppId", "someName", "somePublicKey", 54321, new AuditLog("me",
                new Date(10)), new AuditLog("you", new Date(20)), "192.*", "/some/other/url");
        initial = testedObject.store(initial);

        final TrustedApplicationData updated = new TrustedApplicationData(initial.getId(), "someAppId", "newName", "newPublicKey", 98765,
                new AuditLog("newMe", new Date(30)), new AuditLog("newYou", new Date(40)), "255.*", "/new/url");
        testedObject.store(updated);

        final TrustedApplicationData updatedLoaded = testedObject.getByApplicationId(initial.getApplicationId());
        assertEquals(toMap(updated), toMap(updatedLoaded));
    }

    @Test
    public void testDeleteById()
    {
        TrustedApplicationData data = new TrustedApplicationData("someAppId", "someName", "somePublicKey", 54321, new AuditLog("me",
                new Date(10)), new AuditLog("you", new Date(20)), "192.*", "/some/other/url");
        data = testedObject.store(data);
        testedObject.delete(data.getId());
        assertEquals(Collections.emptyList(), toMaps(testedObject.getAll()));
    }

    @Test
    public void testDeleteByApplicationId()
    {
        TrustedApplicationData data = new TrustedApplicationData("someAppId", "someName", "somePublicKey", 54321, new AuditLog("me",
                new Date(10)), new AuditLog("you", new Date(20)), "192.*", "/some/other/url");
        data = testedObject.store(data);
        testedObject.delete(data.getApplicationId());
        assertEquals(Collections.emptyList(), toMaps(testedObject.getAll()));
    }

    /**
     * @see #toMap(TrustedApplicationData)
     * @param trustedApplicationsData
     *            collection of {@link TrustedApplicationData} for mapping
     * @return re-maps collection via {@link #toMap(TrustedApplicationData)}
     */
    private List<Map<String, Object>> toMaps(final Collection<TrustedApplicationData> trustedApplicationsData)
    {
        final List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();
        for (final TrustedApplicationData trustedApplicationData : trustedApplicationsData)
        {
            result.add(toMap(trustedApplicationData));
        }
        return result;
    }

    private Map<String, Object> toMap(final TrustedApplicationData trustedApplicationData)
    {
        if (trustedApplicationData != null)
        {
            return ImmutableSortedMap.<String, Object> naturalOrder()
                    .put("id", trustedApplicationData.getId())
                    .putAll(toMapWithoutId(trustedApplicationData))
                    .build();
        } else
        {
            return null;
        }
    }

    private Map<String, Object> toMapWithoutId(final TrustedApplicationData trustedApplicationData)
    {
        if (trustedApplicationData != null)
        {
            return ImmutableSortedMap.<String, Object> naturalOrder()
                    .put("applicationId", trustedApplicationData.getApplicationId())
                    .put("name", trustedApplicationData.getName())
                    .put("publicKey", trustedApplicationData.getPublicKey())
                    .put("timeout", trustedApplicationData.getTimeout())
                    .put("ipMatch", trustedApplicationData.getIpMatch())
                    .put("urlMatch", trustedApplicationData.getUrlMatch())
                    .put("created", toMap(trustedApplicationData.getCreated()))
                    .put("updated", toMap(trustedApplicationData.getUpdated()))
                    .build();
        } else
        {
            return null;
        }
    }

    private Map<String, Object> toMap(final AuditLog auditLog)
    {
        if (auditLog != null)
        {
            return ImmutableSortedMap.<String, Object> of("who", auditLog.getWho(), "when", auditLog.getWhen());
        } else
        {
            return null;
        }
    }

}
