package com.atlassian.jira.scheme;

import java.util.Collections;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.security.type.GroupDropdown;

import org.junit.Before;

/**
 * Created by IntelliJ IDEA. User: detkin Date: Jun 22, 2006 Time: 11:43:21 AM To change this template use File |
 * Settings | File Templates.
 */
public abstract class AbstractSchemeTest
{
    protected SchemeEntity entity1;
    protected SchemeEntity entity2;
    protected SchemeEntity entity3;
    protected SchemeEntity entity4;
    protected Scheme testScheme1;
    protected Scheme testScheme2;
    protected Scheme testScheme3;
    protected Scheme testScheme4;

    protected static final String TEST_PARAM_1 = "test param 1";
    protected static final String TEST_PARAM_2 = "test param 2";
    protected static final String TEST_PARAM_3 = "test param 3";
    protected static final String TEST_PARAM_4 = "test param 4";
    protected static final String TEST_SCHEME_4 = "test scheme 4";
    protected static final String TEST_SCHEME_3 = "test scheme 3";
    protected static final String TEST_SCHEME_2 = "test scheme 2";
    protected static final String TEST_SCHEME_1 = "test scheme 1";
    protected static final Long ENTITY_TYPE = new Long(1);

    @Before
    public void setUp() throws Exception
    {
        entity1 = new SchemeEntity(new Long(1), GroupDropdown.DESC, TEST_PARAM_1, ENTITY_TYPE, null, null);
        entity2 = new SchemeEntity(new Long(2), GroupDropdown.DESC, TEST_PARAM_2, ENTITY_TYPE, null, null);
        entity3 = new SchemeEntity(new Long(3), GroupDropdown.DESC, TEST_PARAM_3, ENTITY_TYPE, null, null);
        entity4 = new SchemeEntity(new Long(4), GroupDropdown.DESC, TEST_PARAM_4, ENTITY_TYPE, null, null);
    }

    protected List getSchemesForType(String schemeType)
    {
        // Create a scheme
        List entities = EasyList.build(entity1, entity2, entity3);
        testScheme1 = new Scheme(new Long(1), schemeType, TEST_SCHEME_1, entities);

        // Create a second scheme that will be the same
        testScheme2 = new Scheme(new Long(2), schemeType, TEST_SCHEME_2, entities);


        List otherEntities = EasyList.build(entity1, entity2, entity3, entity4);
        // Create a third scheme that will be different
        testScheme3 = new Scheme(new Long(3), schemeType, TEST_SCHEME_3, otherEntities);

        testScheme4 = new Scheme(new Long(4), schemeType, TEST_SCHEME_4, Collections.EMPTY_LIST);

        return EasyList.build(testScheme1, testScheme2, testScheme3);
    }

    protected Scheme getSchemeForType(String schemeType)
    {
        List entities = EasyList.build(entity1, entity2, entity3);
        return testScheme1 = new Scheme(null, schemeType, TEST_SCHEME_1, entities);
    }
}
