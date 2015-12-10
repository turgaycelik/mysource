package com.atlassian.jira.security.roles;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TestRoleActorComparator
{
    private static final String DESCRIPTOR1 = "descriptor1";
    private static final String DESCRIPTOR2 = "descriptor2";
    private static final String PARAM1 = "param1";
    private static final String PARAM2 = "param2";

    private RoleActorComparator roleActorComparator = RoleActorComparator.COMPARATOR;
    private RoleActor roleActorD1P1;
    private RoleActor roleActorD1P2;
    private RoleActor roleActorD2P1;
    private RoleActor roleActorD2P2;
    private RoleActor roleActorDnullP1;
    private RoleActor roleActorDnullP2;
    private RoleActor roleActorD1Pnull;
    private RoleActor roleActorD2Pnull;
    private RoleActor roleActorDnullPnull;

    @Before
    public void setUp() throws Exception
    {
        roleActorD1P1 = new MockRoleActor(DESCRIPTOR1, PARAM1);
        roleActorD1P2 = new MockRoleActor(DESCRIPTOR1, PARAM2);
        roleActorD2P1 = new MockRoleActor(DESCRIPTOR2, PARAM1);
        roleActorD2P2 = new MockRoleActor(DESCRIPTOR2, PARAM2);
        roleActorDnullP1 = new MockRoleActor(null, PARAM1);
        roleActorDnullP2 = new MockRoleActor(null, PARAM2);
        roleActorD1Pnull = new MockRoleActor(DESCRIPTOR1, null);
        roleActorD2Pnull = new MockRoleActor(DESCRIPTOR2, null);
        roleActorDnullPnull = new MockRoleActor(null, null);
    }

    /**
     * Tests all combinations of descriptors and parameter comaprisons.
     */
    @Test
    public void testRoleActorComparatorBasic()
    {
        //check object null cases
        int cmp = roleActorComparator.compare(null, null);
        assertEquals(0, cmp);
        cmp = roleActorComparator.compare(null, roleActorD1P1);
        assertEquals(1, cmp);
        cmp = roleActorComparator.compare(roleActorD1P1, null);
        assertEquals(-1, cmp);

        //check same descriptor and same paramter == 0
        cmp = roleActorComparator.compare(roleActorD1P1, roleActorD1P1);
        assertEquals(0, cmp);

        //check same descriptor and different paramter
        cmp = roleActorComparator.compare(roleActorD1P1, roleActorD1P2);
        assertEquals(-1, cmp);
        cmp = roleActorComparator.compare(roleActorD1P2, roleActorD1P1);
        assertEquals(-1, cmp);
        cmp = roleActorComparator.compare(roleActorD1P1, roleActorD1Pnull);
        assertEquals(-1, cmp);
        cmp = roleActorComparator.compare(roleActorD1Pnull, roleActorD1P1);
        assertEquals(-1, cmp);

        //check same descriptor and null parameter
        cmp = roleActorComparator.compare(roleActorD1Pnull, roleActorD1Pnull);
        assertEquals(0, cmp);

        //check different descriptor and same parameter
        cmp = roleActorComparator.compare(roleActorD1P1, roleActorD2P1);
        assertEquals(-1, cmp);
        cmp = roleActorComparator.compare(roleActorD2P1, roleActorD1P1);
        assertEquals(1, cmp);
        cmp = roleActorComparator.compare(roleActorD1P1, roleActorDnullP1);
        assertEquals(-1, cmp);
        cmp = roleActorComparator.compare(roleActorDnullP1, roleActorD1P1);
        assertEquals(1, cmp);

        //check different descriptor and different paramter
        cmp = roleActorComparator.compare(roleActorD1P1, roleActorD2P2);
        assertEquals(-1, cmp);
        cmp = roleActorComparator.compare(roleActorD2P2, roleActorD1P1);
        assertEquals(1, cmp);
        cmp = roleActorComparator.compare(roleActorDnullP2, roleActorD1P1);
        assertEquals(1, cmp);
        cmp = roleActorComparator.compare(roleActorD1P2, roleActorDnullP1);
        assertEquals(-1, cmp);

        //check `different descriptor and null parameter
        cmp = roleActorComparator.compare(roleActorD1Pnull, roleActorD2Pnull);
        assertEquals(-1, cmp);
        cmp = roleActorComparator.compare(roleActorD2Pnull, roleActorD1Pnull);
        assertEquals(1, cmp);
        cmp = roleActorComparator.compare(roleActorD1Pnull, roleActorDnullPnull);
        assertEquals(-1, cmp);
        cmp = roleActorComparator.compare(roleActorDnullPnull, roleActorD1Pnull);
        assertEquals(1, cmp);

        //check null descriptor and same parameter
        cmp = roleActorComparator.compare(roleActorDnullP1, roleActorDnullP1);
        assertEquals(0, cmp);

        //check null descriptor and different parameter
        cmp = roleActorComparator.compare(roleActorDnullP1, roleActorDnullP2);
        assertEquals(-1, cmp);
        cmp = roleActorComparator.compare(roleActorDnullP2, roleActorDnullP1);
        assertEquals(-1, cmp);
        cmp = roleActorComparator.compare(roleActorDnullP1, roleActorDnullPnull);
        assertEquals(-1, cmp);
        cmp = roleActorComparator.compare(roleActorDnullPnull, roleActorDnullP1);
        assertEquals(-1, cmp);

        //check null descriptor and null parameter
        cmp = roleActorComparator.compare(roleActorDnullPnull, roleActorDnullPnull);
        assertEquals(0, cmp);
    }
}
