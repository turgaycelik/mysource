package com.atlassian.jira.security.auth.rememberme;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.Clock;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.user.MockUserKeyService;
import com.atlassian.jira.user.util.MockUserKeyStore;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.seraph.service.rememberme.DefaultRememberMeToken;
import com.atlassian.seraph.service.rememberme.RememberMeToken;
import com.atlassian.seraph.spi.rememberme.RememberMeConfiguration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import junit.framework.Assert;

/**
 */
public class TestJiraRememberMeTokenDao
{
    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);


    @AvailableInContainer (instantiateMe = true)
    private MockOfBizDelegator delegator;

    @Mock
    @AvailableInContainer
    private RememberMeConfiguration rememberMeConfiguration;

    private JiraRememberMeTokenDao dao;
    private Date now;
    private static final String FRED_FLINSTONE = "Fred Flinstone";
    private static final String TABLE_NAME = JiraRememberMeTokenDao.TABLE;
    private static final Long ID_123 = 123L;
    private static final String RANDOM_STRING = "token";
    private static final long ID_456 = 456L;
    private static final long ID_789 = 789L;
    private static final String BARNEY_RUBBLE = "Barney Rubble";

    @Before
    public void setUp() throws Exception
    {
        now = new Date(1275617972945L);
        Clock clock = new ConstantClock(now);

        MockUserKeyService userKeyService = mockitoContainer.getMockWorker().getMockUserKeyService();
        userKeyService.setMapping("brubble", BARNEY_RUBBLE);
        userKeyService.setMapping("fflinstone", FRED_FLINSTONE);

        dao = new JiraRememberMeTokenDao(delegator, rememberMeConfiguration, clock, userKeyService);
    }

    @Test
    public void testFindById_NotFound()
    {
        final RememberMeToken token = dao.findById(ID_123);
        Assert.assertNull(token);
    }

    @Test
    public void testFindById_Expired()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(10), FRED_FLINSTONE);

        final RememberMeToken token = dao.findById(ID_123);
        Assert.assertNull(token);

        // and it got deleted because it was expired
        Assert.assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
    }

    @Test
    public void testFindById()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);

        final RememberMeToken token = dao.findById(ID_123);
        Assert.assertNotNull(token);
        Assert.assertEquals(ID_123, token.getId());
        Assert.assertEquals(RANDOM_STRING, token.getRandomString());
        Assert.assertEquals(FRED_FLINSTONE, token.getUserName());
        Assert.assertEquals(now.getTime(), token.getCreatedTime());

        // and it did not get deleted because it was WANT expired
        Assert.assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
    }

    @Test
    public void testCountAll()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_789, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);

        final long count = dao.countAll();
        Assert.assertEquals(3, count);
    }

    @Test
    public void testFindByName()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        final List<RememberMeToken> tokens = dao.findForUserName(FRED_FLINSTONE);
        Assert.assertEquals(1, tokens.size());
        RememberMeToken token = tokens.get(0);

        Assert.assertEquals(ID_123, token.getId());
        Assert.assertEquals(RANDOM_STRING, token.getRandomString());
        Assert.assertEquals(FRED_FLINSTONE, token.getUserName());
        Assert.assertEquals(now.getTime(), token.getCreatedTime());

        // and it did not get deleted because it was WANT expired
        Assert.assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
    }

    @Test
    public void testFindByName_NonFound()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        final List<RememberMeToken> tokens = dao.findForUserName("NotNone");
        Assert.assertEquals(0, tokens.size());
    }

    @Test
    public void testFindByName_ButExpired()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(10), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        final List<RememberMeToken> tokens = dao.findForUserName(FRED_FLINSTONE);
        Assert.assertEquals(0, tokens.size());

        // and it got deleted because it was expired
        Assert.assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
    }

    @Test
    public void testSave()
    {
        Assert.assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));

        final RememberMeToken inputToken = DefaultRememberMeToken.builder(RANDOM_STRING).setUserName(FRED_FLINSTONE).build();
        Assert.assertNull(inputToken.getId());

        final RememberMeToken persistedToken = dao.save(inputToken);

        Assert.assertNotNull(persistedToken);
        Assert.assertNotNull(persistedToken.getId());
        Assert.assertEquals(RANDOM_STRING, persistedToken.getRandomString());
        Assert.assertEquals(FRED_FLINSTONE, persistedToken.getUserName());
        Assert.assertEquals(now.getTime(), persistedToken.getCreatedTime());

        Assert.assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, persistedToken.getId()));

    }

    private GenericValue addRow(Long id, String token, Timestamp timestamp, final String userName)
    {
        String userKey = mockitoContainer.getMockWorker().getMockUserKeyService().getKeyForUsername(userName);
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(JiraRememberMeTokenDao.Columns.ID, id);
        map.put(JiraRememberMeTokenDao.Columns.TOKEN, token);
        map.put(JiraRememberMeTokenDao.Columns.CREATED, timestamp);
        map.put(JiraRememberMeTokenDao.Columns.USERKEY, userKey);
        final GenericValue gv = UtilsForTests.getTestEntity(TABLE_NAME, map);
        Assert.assertNotNull(gv);
        return gv;
    }

    @Test
    public void testRemoveByUserName()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_789, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        dao.removeAllForUser(FRED_FLINSTONE);

        Assert.assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
        Assert.assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_456));
        Assert.assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, ID_789));
    }

    @Test
    public void testRemoveById()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_789, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        dao.remove(ID_456);

        Assert.assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
        Assert.assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_456));
        Assert.assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, ID_789));
    }

    @Test
    public void testRemoveAll()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_789, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        dao.removeAll();

        Assert.assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
        Assert.assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_456));
        Assert.assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_789));
    }

}
