/**
 * Copyright 2002-2007 Atlassian.
 */
package com.atlassian.jira.security.auth.trustedapps;

import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.security.auth.trustedapps.Application;
import com.atlassian.security.auth.trustedapps.RequestConditions;

import com.google.common.base.CharMatcher;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestTrustedApplicationBuilder
{
    @Test
    public void testBuildByFields()
    {
        KeyPair keyPair = KeyUtil.generateNewKeyPair("RSA");

        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        TrustedApplicationInfo info = builder.setId(10).setApplicationId("appId").setTimeout(1000).setPublicKey(keyPair.getPublic()).setName("appName").toInfo();
        Assert.assertEquals(10, info.getNumericId());
        Assert.assertEquals("appId", info.getID());
        Assert.assertEquals(1000, info.getTimeout());
        Assert.assertNotNull(info.getPublicKey());
        Assert.assertEquals("RSA", info.getPublicKey().getAlgorithm());
        Assert.assertEquals("appName", info.getName());
        Assert.assertNull(info.getIpMatch());
        Assert.assertNull(info.getUrlMatch());
    }

    @Test
    public void testBuildQueryString() throws Exception
    {
        KeyPair keyPair = KeyUtil.generateNewKeyPair("RSA");

        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setId(10).setApplicationId("appId").setTimeout(1321).setPublicKey(keyPair.getPublic()).setName("appName").setIpMatch("192.168.0.*").setUrlMatch("portlet.jspa");
        assertQueryStringContains("applicationId=appId", builder);
        assertQueryStringContains("timeout=1321", builder);
        assertQueryStringContains("publicKey=" + URLEncoder.encode(KeyFactory.encode(keyPair.getPublic()), "UTF8"), builder);
        assertQueryStringContains("name=appName", builder);
        assertQueryStringContains("ipMatch=192.168.0.*", builder);
        assertQueryStringContains("urlMatch=portlet.jspa", builder);
    }

    @Test
    public void testBuildEncodedQueryString()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setId(10).setApplicationId("appId").setTimeout(1321).setName("JIRA & Confluence Hosted");
        assertQueryStringContains("name=JIRA+%26+Confluence+Hosted", builder);
    }

    private void assertQueryStringContains(String contains, TrustedApplicationBuilder info)
    {
        final String str = info.toQueryString();
        Assert.assertNotNull(str);
        Assert.assertTrue("Could not find '" + contains + "' in '" + str + "'", str.indexOf(contains) > -1);
    }

    @Test
    public void testBuildByData()
    {
        MockTrustedApplicationData data = new MockTrustedApplicationData(10, "appId", "someName", 1000);
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        TrustedApplicationInfo info = builder.set(data).toInfo();

        Assert.assertEquals(10, info.getNumericId());
        Assert.assertEquals("appId", info.getID());
        Assert.assertEquals(1000, info.getTimeout());
        Assert.assertNotNull(info.getPublicKey());
        Assert.assertEquals("RSA", info.getPublicKey().getAlgorithm());
        Assert.assertEquals("someName", info.getName());
        Assert.assertNull(info.getIpMatch());
        Assert.assertNull(info.getUrlMatch());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testNullDataSetter()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set((TrustedApplicationData) null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testNullInfoSetter()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set((TrustedApplicationInfo) null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testNullGVSetter()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set((GenericValue) null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testNullIdSetter()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setApplicationId(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testNullPublicKeySetter()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setPublicKey((PublicKey) null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testNullPublicKeyStringSetter()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setPublicKey((String) null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testNullNameSetter()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setName(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testNullCreatedSetter()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setCreated((AuditLog) null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testNullUpdatedSetter()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setUpdated((AuditLog) null);
    }

    @Test (expected = IllegalStateException.class)
    public void testIllegalStateFromToDataIfNotConstructed()
    {
        new TrustedApplicationBuilder().toData();
    }

    @Test (expected = IllegalStateException.class)
    public void testIllegalStateFromToInfoIfNotConstructed()
    {
        new TrustedApplicationBuilder().toInfo();
        Assert.fail("IllegalStateEx expected");
    }

    @Test
    public void testBuildsCorrectlyWithZeroId()
    {
        Map fields = new HashMap();
        fields.put("applicationId", "myAppId");
        fields.put("name", "myName");
        fields.put("publicKey", "1234567890");
        fields.put("algorithm", "RSA");
        fields.put("timeout", new Long(1234));
        fields.put("created", new Timestamp(1000000));
        fields.put("createdBy", "creator");
        fields.put("updated", new Timestamp(2000000));
        fields.put("updatedBy", "updator");
        fields.put("ipMatch", "192.168.0.10");
        fields.put("urlMatch", "/some/url");
        GenericValue gv = new MockGenericValue("TrustedApplication", fields);

        TrustedApplicationData data = new TrustedApplicationBuilder().set(gv).toData();
        Assert.assertNotNull(data);
        Assert.assertEquals(0, data.getId());
        Assert.assertEquals("myAppId", data.getApplicationId());
        Assert.assertEquals("myName", data.getName());
        Assert.assertEquals("1234567890", data.getPublicKey());
        Assert.assertEquals(1234, data.getTimeout());
        Assert.assertEquals("creator", data.getCreated().getWho());
        Assert.assertEquals(1000000, data.getCreated().getWhen().getTime());
        Assert.assertEquals("updator", data.getUpdated().getWho());
        Assert.assertEquals(2000000, data.getUpdated().getWhen().getTime());
        Assert.assertEquals("192.168.0.10", data.getIpMatch());
        Assert.assertEquals("/some/url", data.getUrlMatch());
    }

    @Test
    public void testBuildsCorrectlyWithoutUrlOrIp()
    {
        Map fields = new HashMap();
        fields.put("id", new Long(2));
        fields.put("applicationId", "myAppIds");
        fields.put("name", "myNameWhat");
        fields.put("publicKey", "12345678901234567890");
        fields.put("algorithm", "RSA");
        fields.put("timeout", new Long(123456));
        fields.put("created", new Timestamp(100000));
        fields.put("createdBy", "creator");
        fields.put("updated", new Timestamp(200000));
        fields.put("updatedBy", "updator");
        GenericValue gv = new MockGenericValue("TrustedApplication", fields);

        TrustedApplicationData data = new TrustedApplicationBuilder().set(gv).toData();
        Assert.assertNotNull(data);
        Assert.assertEquals(2, data.getId());
        Assert.assertEquals("myAppIds", data.getApplicationId());
        Assert.assertEquals("myNameWhat", data.getName());
        Assert.assertEquals("12345678901234567890", data.getPublicKey());
        Assert.assertEquals(123456, data.getTimeout());
        Assert.assertEquals("creator", data.getCreated().getWho());
        Assert.assertEquals(100000, data.getCreated().getWhen().getTime());
        Assert.assertEquals("updator", data.getUpdated().getWho());
        Assert.assertEquals(200000, data.getUpdated().getWhen().getTime());
        Assert.assertNull(data.getIpMatch());
        Assert.assertNull(data.getUrlMatch());
    }

    @Test
    public void testWrongEntityName() throws Exception
    {
        GenericValue gv = new MockGenericValue("TrustingApplication");

        try
        {
            new TrustedApplicationBuilder().set(gv);
            Assert.fail("Should have thrown IllegalArg");
        }
        catch (IllegalArgumentException yay)
        {
            // expected
        }
    }

    @Test
    public void testBuildsGenericValue()
    {
        TrustedApplicationData input = new TrustedApplicationData(3, "someAppId", "someName", "somePublicKey", 54321, new AuditLog("me", new Date(10)), new AuditLog("you", new Date(20)), "192.*", "/some/other/url");
        Map map = new TrustedApplicationBuilder().set(input).toMap();
        Assert.assertNotNull(map);
        Assert.assertEquals(new Long(3), map.get("id"));
        Assert.assertEquals("someAppId", map.get("applicationId"));
        Assert.assertEquals("someName", map.get("name"));
        Assert.assertEquals("somePublicKey", map.get("publicKey"));
        Assert.assertEquals(new Long(54321), map.get("timeout"));
        Assert.assertEquals("me", map.get("createdBy"));
        Assert.assertEquals(new Timestamp(10), map.get("created"));
        Assert.assertEquals(Timestamp.class, map.get("created").getClass());
        Assert.assertEquals("you", map.get("updatedBy"));
        Assert.assertEquals(new Timestamp(20), map.get("updated"));
        Assert.assertEquals(Timestamp.class, map.get("updated").getClass());
        Assert.assertEquals("192.*", map.get("ipMatch"));
        Assert.assertEquals("/some/other/url", map.get("urlMatch"));
    }

    @Test
    public void testBuildsGenericValueWithoutZeroId()
    {
        TrustedApplicationData input = new TrustedApplicationData("someAppId", "someName", "somePublicKey", 54321, new AuditLog("me", new Date(10)), new AuditLog("you", new Date(20)), "192.*", "/some/other/url");
        Map map = new TrustedApplicationBuilder().set(input).toMap();
        Assert.assertNotNull(map);
        Assert.assertNull(map.get("id"));
        Assert.assertEquals("someAppId", map.get("applicationId"));
        Assert.assertEquals("someName", map.get("name"));
        Assert.assertEquals("somePublicKey", map.get("publicKey"));
        Assert.assertEquals(new Long(54321), map.get("timeout"));
        Assert.assertEquals("me", map.get("createdBy"));
        Assert.assertEquals(new Timestamp(10), map.get("created"));
        Assert.assertEquals(Timestamp.class, map.get("created").getClass());
        Assert.assertEquals("you", map.get("updatedBy"));
        Assert.assertEquals(new Timestamp(20), map.get("updated"));
        Assert.assertEquals(Timestamp.class, map.get("updated").getClass());
        Assert.assertEquals("192.*", map.get("ipMatch"));
        Assert.assertEquals("/some/other/url", map.get("urlMatch"));
    }

    @Test
    public void testBuildsCorrectlyFromMap()
    {
        Map fields = new HashMap();
        fields.put("id", new Long(1));
        fields.put("applicationId", "myAppId");
        fields.put("name", "myName");
        fields.put("publicKey", "1234567890");
        fields.put("algorithm", "RSA");
        fields.put("timeout", new Long(1234));
        fields.put("created", new Timestamp(1000000));
        fields.put("createdBy", "creator");
        fields.put("updated", new Timestamp(2000000));
        fields.put("updatedBy", "updator");
        fields.put("ipMatch", "192.168.0.10");
        fields.put("urlMatch", "/some/url");
        GenericValue gv = new MockGenericValue("TrustedApplication", fields);

        TrustedApplicationData data = new TrustedApplicationBuilder().set(gv).toData();
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.getId());
        Assert.assertEquals("myAppId", data.getApplicationId());
        Assert.assertEquals("myName", data.getName());
        Assert.assertEquals("1234567890", data.getPublicKey());
        Assert.assertEquals(1234, data.getTimeout());
        Assert.assertEquals("creator", data.getCreated().getWho());
        Assert.assertEquals(1000000, data.getCreated().getWhen().getTime());
        Assert.assertEquals("updator", data.getUpdated().getWho());
        Assert.assertEquals(2000000, data.getUpdated().getWhen().getTime());
        Assert.assertEquals("192.168.0.10", data.getIpMatch());
        Assert.assertEquals("/some/url", data.getUrlMatch());
    }

    @Test
    public void testIllegalState()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setId(1).setApplicationId("appId").setName("name");
        try
        {
            builder.toData();
            Assert.fail("IllegalStateEx expected");
        }
        catch (IllegalStateException yay2)
        {
            // expected
        }
        try
        {
            builder.toInfo();
            Assert.fail("IllegalStateEx expected");
        }
        catch (IllegalStateException yay2)
        {
            // expected
        }

        builder.setCreated(new AuditLog("me", new Date()));
        try
        {
            builder.toData();
            Assert.fail("IllegalStateEx expected");
        }
        catch (IllegalStateException yay1)
        {
            // expected
        }
        try
        {
            builder.toInfo();
            Assert.fail("IllegalStateEx expected");
        }
        catch (IllegalStateException yay1)
        {
            // expected
        }

        builder.setUpdated(new AuditLog("you", new Date()));
        try
        {
            builder.toData();
            Assert.fail("IllegalStateEx expected");
        }
        catch (IllegalStateException yay)
        {
            // expected
        }
        try
        {
            builder.toInfo();
            Assert.fail("IllegalStateEx expected");
        }
        catch (IllegalStateException yay)
        {
            // expected
        }

        builder.setPublicKey("garbledKey");
        Assert.assertNotNull(builder.toData());
        final TrustedApplicationInfo applicationInfo = builder.toInfo();
        Assert.assertNotNull(applicationInfo);
        Assert.assertFalse(applicationInfo.getPublicKey().toString(), applicationInfo.isValidKey());
    }

    @Test
    public void testInvalidKey()
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setId(1).setApplicationId("appId").setName("name").setCreated(new AuditLog("me", new Date())).setUpdated(new AuditLog("you", new Date()));
        builder.setPublicKey(KeyFactory.getPublicKey(""));

        Assert.assertNotNull(builder.toData());
        Assert.assertNotNull(builder.toInfo());
        Assert.assertFalse(builder.toInfo().isValidKey());
    }

    @Test
    public void testCanonisesIpMatch() throws Exception
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setIpMatch("two\rthree\rone");
        Assert.assertEquals("two\nthree\none", builder.getIpMatch());
        builder.setIpMatch("two\r\nthree\r\none");
        Assert.assertEquals("two\nthree\none", builder.getIpMatch());
        builder.setIpMatch("two\nthree\none");
        Assert.assertEquals("two\nthree\none", builder.getIpMatch());
    }

    @Test
    public void testCanonisesUrlMatch() throws Exception
    {
        TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setUrlMatch("two\rthree\rone");
        Assert.assertEquals("two\nthree\none", builder.getUrlMatch());
        builder.setUrlMatch("two\r\nthree\r\none");
        Assert.assertEquals("two\nthree\none", builder.getUrlMatch());
        builder.setUrlMatch("two\nthree\none");
        Assert.assertEquals("two\nthree\none", builder.getUrlMatch());
    }

    @Test
    public void testSettingRequestConditionsSets() throws Exception
    {
        byte[] oneByte = new byte[] { 1 };
        PublicKey pk = createMock(PublicKey.class);
        expect(pk.getEncoded()).andReturn(oneByte).anyTimes();
        replay(pk);

        Application app = createMock(Application.class);
        expect(app.getID()).andReturn("myApp").anyTimes();
        expect(app.getPublicKey()).andReturn(pk).anyTimes();
        replay(app);

        RequestConditions rc = RequestConditions.builder()
                .addIPPattern("1.1.1.1")
                .addIPPattern("2.2.2.2")
                .addURLPattern("/one/one")
                .addURLPattern("/two/two")
                .setCertificateTimeout(50)
                .build();

        TrustedApplicationInfo info = new TrustedApplicationBuilder().set(app).set(rc).toInfo();
        assertThat(info.getID(), equalTo("myApp"));
        assertThat(info.getIpMatch(), equalTo("1.1.1.1\n2.2.2.2"));
        // Unpredictable Set ordering ... different in Java 7 vs Java 8
        assertThat(info.getUrlMatch(), CoreMatchers.anyOf(equalTo("/one/one\n/two/two"), equalTo("/two/two\n/one/one")));
        assertThat(info.getTimeout(), equalTo(50L));
    }
}
