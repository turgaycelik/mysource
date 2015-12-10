package com.atlassian.jira.config.util;

/**
 * Very simple mock license check. Instantiate with the level you want to be.
 */
public class MockEditionCheck
{
    public static EditionCheck getMockEnterprise()
    {
        return new EditionCheck()
        {
            public boolean isEnterprise()
            {
                return true;
            }

            public boolean isProfessional()
            {
                return false;
            }
        };
    }

    public static EditionCheck getMockProfessional()
    {
        return new EditionCheck()
        {
            public boolean isEnterprise()
            {
                return false;
            }

            public boolean isProfessional()
            {
                return true;
            }
        };
    }

    public static EditionCheck getMockStandard()
    {
        return new EditionCheck()
        {
            public boolean isEnterprise()
            {
                return false;
            }

            public boolean isProfessional()
            {
                return false;
            }
        };
    }


    // we only have public static factory methods.
    private MockEditionCheck() {};
}