package com.atlassian.jira.local;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentAccessorWorker;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.event.MockEventPublisher;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.security.DefaultGlobalPermissionManager;
import com.atlassian.jira.security.DefaultPermissionManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.MockGlobalPermissionTypeManager;
import com.atlassian.jira.security.MockProjectPermissionTypesManager;
import com.atlassian.jira.security.plugin.ProjectPermissionTypesManager;
import com.atlassian.jira.sharing.index.MockSharedEntityIndexer;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserKeyStore;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.RedirectSanitiser;

import java.util.Collection;

import static org.easymock.classextension.EasyMock.createNiceMock;

/**
 * <p/>
 * Anything would be better than this base class.  Its SLOW, SLOW, SLOW.
 *
 * <p/>
 * The only use for this test class is when you are testing say OfBiz Stores.
 *
 * @deprecated since v4.0. Use standard JUnit 4 patterns
 */
public abstract class LegacyJiraMockTestCase extends Junit3ListeningTestCase
{
    static
    {
        UtilsForTestSetup.loadDatabaseDriver();
    }

    /**
     * This will be set to true if one or more JiraMockTestCase have actually been run
     */
    static boolean jiraMockTestCaseHasBeenRun = false;
    protected FileFactory fileFactory;

    protected LegacyJiraMockTestCase()
    {
    }

    public LegacyJiraMockTestCase(String s)
    {
        super(s);
    }

    @Override
    public void runBare() throws Throwable
    {
        //
        // In fast mode we can do all tests in 43 seconds otherwise its 300 seconds
        //
        // LegacyJiraMockTestCase is the culprit, especially its setUp() method
        //
        if (!Boolean.getBoolean("unit.test.fastmode"))
        {
            super.runBare();
        }
        else
        {
            System.err.printf("*** Not running slow test case %s because unit.test.fastmode=true\n", getClass().getSimpleName() + "." + getName());

        }
    }

    protected void setUp() throws Exception
    {
        jiraMockTestCaseHasBeenRun = true;
        fileFactory = createNiceMock(FileFactory.class);

        // Ensure the UserManager and static crowd service factory are initialised with the Mock service
        new StaticCrowdServiceFactory(new MockCrowdService()
        {
            @Override
            protected UserKeyStore getUserKeyStore()
            {
                return ComponentAccessor.getComponent(UserKeyStore.class);
            }
        });

        UtilsForTestSetup.mockTestCaseSetup(getServiceOverrider());
        UtilsForTestSetup.configureOsWorkflow();

        ComponentAccessor.initialiseWorker(new ComponentAccessorWorker());

        JiraSystemProperties.getInstance().refresh();

        // Pretend the static permissions are still there, even though they are now in module descriptors.
        // We do this by making the GlobalPermissionManager hold our MockGlobalPermissionTypeManager.
        ManagerFactory.removeService(GlobalPermissionManager.class);
        ManagerFactory.addService(GlobalPermissionManager.class, new DefaultGlobalPermissionManager(
            ComponentAccessor.getCrowdService(),
            ComponentAccessor.getOfBizDelegator(),
            new MockEventPublisher(),
            new MockGlobalPermissionTypeManager(),
            new MemoryCacheManager())
        );

        DefaultPermissionManager defaultPermissionManager = (DefaultPermissionManager) ManagerFactory.getPermissionManager();
        defaultPermissionManager.setProjectPermissionTypesManager(new MockProjectPermissionTypesManager());
    }

    private UtilsForTestSetup.ServiceOverrider getServiceOverrider()
    {
        return new UtilsForTestSetup.ServiceOverrider()
        {
            public void override()
            {
                overrideServices();
            }
        };
    }

    /**
     * Override services by setting {@link ManagerFactory#addService(Class, Object)} for any objects you want to
     * override
     */
    protected void overrideServices()
    {
        ManagerFactory.addService(SharedEntityIndexer.class, new MockSharedEntityIndexer());
        ManagerFactory.addService(FileFactory.class, fileFactory); // don't let tests get at the real file system
        ManagerFactory.addService(RedirectSanitiser.class, new MockRedirectSanitiser());
        ManagerFactory.addService(ProjectPermissionTypesManager.class, new MockProjectPermissionTypesManager());
    }


    protected void tearDown() throws Exception
    {
        UtilsForTestSetup.mockTestCaseTearDown();

        // Clean up any users that might be hanging about in the MockCrowdService

        super.tearDown();
    }

    /**
     * Check that a collection has only one element, and that is the object provided
     */
    protected void checkSingleElementCollection(Collection collection, Object expected)
    {
        assertEquals(1, collection.size());
        assertEquals(expected, collection.iterator().next());
    }

    /**
     * Checks that the given collection contains only the objects in the other collection.
     */
    public void assertContainsOnly(Collection collection, Collection other)
    {
        assertEquals(collection.size(), other.size());
        assertTrue(collection.containsAll(other));
    }

    /**
     * Asserts that the given object is the only thing in the given collection.
     */
    public void assertContainsOnly(Object expected, Collection other)
    {
        assertContainsOnly(EasyList.build(expected), other);
    }

    protected ApplicationUser createMockApplicationUser(String userName, String name, String email)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        createMockUser(userName, name, email);
        ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(userName);
        if (user == null)
        {
            throw new RuntimeException("You just created a user and it doesn't exist? Check if you haven't introduced or "
                    + "changed any implementations of InitializingComponent. Legacy tests are broken and if you introduce InitializingComponent "
                    + "that uses anything that uses CrowdService before CrowdService is mocked by the test you will create a user above but getUserByName will "
                    + "return null because it's using the real CrowdService, not the one mocked by the test.");
        }
        return user;
    }

    protected ApplicationUser createMockApplicationUser(String userName)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        return createMockApplicationUser(userName, userName, userName + "@example.com");
    }

    protected User createMockUser(String userName, String name, String email)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        User user = new MockUser(userName, name, email);
        CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addUser(user, "password");
        ComponentAccessor.getComponent(UserKeyStore.class).ensureUniqueKeyForNewUser(user.getName());
        return user;
    }

    protected User createMockUser(String userName)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        return createMockUser(userName, userName, "");
    }

    protected Group createMockGroup(String groupName)
            throws OperationNotPermittedException, InvalidGroupException
    {
        Group group = new MockGroup(groupName);
        CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addGroup(group);
        return group;
    }

    protected void addUserToGroup(User user, Group group)
            throws OperationNotPermittedException, InvalidGroupException
    {
        CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addUserToGroup(user, group);
    }

    protected void addUserToGroup(ApplicationUser user, Group group)
            throws OperationNotPermittedException, InvalidGroupException
    {
        CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addUserToGroup(user != null ? user.getDirectoryUser() : null, group);
    }

    protected NodeAssociationStore getNodeAssociationStore()
    {
        return ComponentAccessor.getComponent(NodeAssociationStore.class);
    }

}
