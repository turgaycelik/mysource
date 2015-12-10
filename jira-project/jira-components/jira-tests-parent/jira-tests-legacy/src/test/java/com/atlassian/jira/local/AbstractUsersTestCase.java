package com.atlassian.jira.local;

import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.directory.InternalDirectory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.jira.component.ComponentAccessor;

/**
 * This test is to be extended by any test case which creates
 * mock users. It will handle all removals after each test.
 *
 * @deprecated v4.3 - Please stop using these TestCases
 */
@Deprecated
public abstract class AbstractUsersTestCase extends AbstractWebworkTestCase
{
    public AbstractUsersTestCase(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        // Add in a Crowd Embedded Application for user-based Jelly Tests
        Application application = ApplicationImpl.newInstance("crowd-embedded", ApplicationType.CROWD);
        ComponentAccessor.getComponentOfType(ApplicationDAO.class).add(application, new PasswordCredential("foo", true));
        // Add in a Crowd Embedded Directory for user-based Jelly Tests
        DirectoryImpl directory = new DirectoryImpl("Mock Internal", DirectoryType.INTERNAL, InternalDirectory.class.getCanonicalName());
        directory.addAllowedOperation(OperationType.CREATE_USER);
        directory.addAllowedOperation(OperationType.UPDATE_USER);
        directory.addAllowedOperation(OperationType.DELETE_USER);
        directory.addAllowedOperation(OperationType.UPDATE_USER_ATTRIBUTE);
        directory.addAllowedOperation(OperationType.CREATE_GROUP);
        directory.addAllowedOperation(OperationType.UPDATE_GROUP);
        directory.addAllowedOperation(OperationType.DELETE_GROUP);
        ComponentAccessor.getComponentOfType(DirectoryDao.class).add(directory);
    }

    /**
     * We need to remove any users we have created between each test. Should be fairly fast.
     */
    protected void tearDown() throws Exception
    {
        CoreTransactionUtil.setUseTransactions(false);
        super.tearDown();
    }
}
