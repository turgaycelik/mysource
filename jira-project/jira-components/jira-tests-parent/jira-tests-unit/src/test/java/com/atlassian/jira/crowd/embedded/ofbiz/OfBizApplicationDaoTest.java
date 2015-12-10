package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.HashSet;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.jira.crowd.embedded.TestData;

import com.google.common.collect.Sets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class OfBizApplicationDaoTest extends AbstractTransactionalOfBizTestCase
{
    private OfBizApplicationDao applicationDao;
    @Mock private CacheManager cacheManager;

    @Before
    public void setUp() throws Exception
    {
        cacheManager = new MemoryCacheManager();
        applicationDao = new OfBizApplicationDao(getOfBizDelegator(), null, cacheManager);
    }

    @After
    public void tearDown() throws Exception
    {
        applicationDao = null;
    }

    @Test
    public void testAddApplicationAndFindById() throws Exception
    {
        Application application = applicationDao.add(TestData.Application.getUnmockedTestData(), new PasswordCredential("secret", true));

        TestData.Application.assertEqualsTestApplication(application);

        application = applicationDao.findById(application.getId());

        TestData.Application.assertEqualsTestApplication(application);
    }

    @Test
    public void testAdd() throws Exception
    {
        Application application = applicationDao.add(TestData.Application.getUnmockedTestData());

        TestData.Application.assertEqualsTestApplication(application);

        applicationDao.updateCredential(application, new PasswordCredential("secret", true));

        application = applicationDao.findById(application.getId());

        assertEquals(TestData.Application.NAME, application.getName());
        assertEquals(TestData.Application.DESCRIPTION, application.getDescription());
        assertEquals(TestData.Application.TYPE, application.getType());
        assertEquals(TestData.Application.ACTIVE, application.isActive());
        assertTrue(application.getRemoteAddresses().containsAll(TestData.Application.ADDRESSES));
    }

    @Test
    public void testUpdate() throws Exception
    {
        Application application = applicationDao.add(TestData.Application.getUnmockedTestData(), new PasswordCredential("secret", true));
        Long applicationId = application.getId();

        com.atlassian.crowd.model.application.ApplicationImpl applicationTemplate =
                com.atlassian.crowd.model.application.ApplicationImpl.newInstanceWithIdAndCredential(
                        "Updated name", ApplicationType.CONFLUENCE, new PasswordCredential("magic", true), applicationId);
        applicationTemplate.setActive(false);
        applicationTemplate.setDescription("Updated desc");
        HashSet<RemoteAddress> addresses = Sets.newHashSet(new RemoteAddress("10.10.10.10"), new RemoteAddress("10.10.10.11"), new RemoteAddress("10.10.10.12"));
        applicationTemplate.setRemoteAddresses(addresses);

        applicationDao.update(applicationTemplate);

        application = applicationDao.findById(applicationId);

        assertEquals("Updated name", application.getName());
        assertEquals("Updated desc", application.getDescription());
        assertEquals(ApplicationType.CONFLUENCE, application.getType());
        assertEquals(false, application.isActive());
        assertTrue(application.getRemoteAddresses().containsAll(addresses));
    }

    @Test
    public void testRemove() throws Exception
    {
        Application application = applicationDao.add(TestData.Application.getUnmockedTestData(), new PasswordCredential("secret", true));
        application = applicationDao.findById(application.getId());
        TestData.Application.assertEqualsTestApplication(application);

        applicationDao.remove(application);

        try
        {
            application = applicationDao.findById(application.getId());
            fail("Should throw ApplicationNotFoundException");
        }
        catch (ApplicationNotFoundException e)
        {
            // OK
        }

    }

    @Test
    public void testAddAndUpdateCredential() throws Exception
    {
        Application application = applicationDao.add(TestData.Application.getUnmockedTestData(), new PasswordCredential("secret", true));

        TestData.Application.assertEqualsTestApplication(application);

        applicationDao.updateCredential(application, new PasswordCredential("secret", true));

        application = applicationDao.findById(application.getId());

        assertEquals(new PasswordCredential("secret", true), application.getCredential());
    }

    @Test
    public void testAddRemoteAddress() throws Exception
    {
        Application application = applicationDao.add(TestData.Application.getUnmockedTestData(), new PasswordCredential("secret", true));

        TestData.Application.assertEqualsTestApplication(application);

        applicationDao.addRemoteAddress(application.getId(), new RemoteAddress("10.10.10.12"));

        application = applicationDao.findById(application.getId());

        assertNotNull(application.getRemoteAddresses());
        assertEquals(3, application.getRemoteAddresses().size());
        HashSet<RemoteAddress> addresses = Sets.newHashSet(new RemoteAddress("10.10.10.10"), new RemoteAddress("10.10.10.11"), new RemoteAddress("10.10.10.12"));
        assertTrue(application.getRemoteAddresses().containsAll(addresses ));

        applicationDao.removeRemoteAddress(application.getId(), new RemoteAddress("10.10.10.10"));
        application = applicationDao.findById(application.getId());

        assertNotNull(application.getRemoteAddresses());
        assertEquals(2, application.getRemoteAddresses().size());
        addresses = Sets.newHashSet(new RemoteAddress("10.10.10.11"), new RemoteAddress("10.10.10.12"));
        assertTrue(application.getRemoteAddresses().containsAll(addresses ));
        applicationDao.removeRemoteAddress(application.getId(), new RemoteAddress("10.10.10.11"));
        applicationDao.removeRemoteAddress(application.getId(), new RemoteAddress("10.10.10.12"));
        application = applicationDao.findById(application.getId());

        assertNotNull(application.getRemoteAddresses());
        assertEquals(0, application.getRemoteAddresses().size());
    }

}
