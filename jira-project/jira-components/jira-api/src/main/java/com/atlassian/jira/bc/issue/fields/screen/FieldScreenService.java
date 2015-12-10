package com.atlassian.jira.bc.issue.fields.screen;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.user.ApplicationUser;

/**
 *
 * @since v5.2
 */
@PublicApi
public interface FieldScreenService
{
    /**
     * Creates a copy of the passed field screen assigning the passed name and description.
     *
     * @param screen screen to copy
     * @param copyName name for the copied screen
     * @param copyDescription description for the copied screen
     * @param loggedInUser user performing the copy
     * @return copy result
     */
    ServiceOutcome<FieldScreen> copy(FieldScreen screen, String copyName, String copyDescription, ApplicationUser loggedInUser);
}
