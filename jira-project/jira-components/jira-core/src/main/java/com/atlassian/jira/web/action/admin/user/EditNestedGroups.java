/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.GroupRemoveChildMapper;
import com.atlassian.jira.bc.group.GroupService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.customfields.converters.MultiUserConverter;
import com.atlassian.jira.issue.fields.option.ChildGroupOption;
import com.atlassian.jira.issue.fields.option.GroupOption;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@WebSudoRequired
public class EditNestedGroups extends JiraWebActionSupport
{
    private String[] selectedGroupsStr;
    private List selectedGroups;
    private Set selectedGroupsChildnames;

    private String[] childrenToUnassign;

    private String[] childrenToAssignStr;
    private ArrayList childrenToAssign;
    private String prunedChildrenToAssign;

    private ArrayList membersList;
    private ArrayList overloadedGroups;

    //form buttons
    private String assign;
    private String unassign;

    private Integer maxMembers;

    private final MultiUserConverter multiUserConverter;
    private final ApplicationProperties applicationProperties;
    private final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil;
    private final GroupService groupService;
    private final CrowdService crowdService;
    private final GroupManager groupManager;

    private static final int MAX_LIST_SIZE = 20;
    private static final String OPTION_VALUE_SEPERATOR = "______";

    public EditNestedGroups(MultiUserConverter multiUserConverter, ApplicationProperties applicationProperties, GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil, GroupService groupService, CrowdService crowdService, GroupManager groupManager)
    {
        this.globalPermissionGroupAssociationUtil = globalPermissionGroupAssociationUtil;
        this.multiUserConverter = multiUserConverter;
        this.applicationProperties = applicationProperties;
        this.groupService = groupService;
        this.crowdService = crowdService;
        this.groupManager = groupManager;
    }

    public void doValidation()
    {
        super.doValidation();

        // Common validation for both add/remove
        if (selectedGroupsStr == null || selectedGroupsStr.length == 0)
        {
            addErrorMessage(getText("admin.editnestedgroups.error.no.group.selected"));
            return;
        }

        // Perform the add specific validation
        if (TextUtils.stringSet(assign))
        {
            if (childrenToAssignStr == null || childrenToAssignStr.length == 0)
            {
                addErrorMessage(getText("admin.editnestedgroups.error.no.children.to.add"));
                return;
            }

            GroupService.BulkEditGroupValidationResult bulkEditGroupValidationResult =
                    groupService.validateAddGroupsToGroup(getJiraServiceContext(), Arrays.asList(selectedGroupsStr), Arrays.asList(childrenToAssignStr));

            if (!bulkEditGroupValidationResult.isSuccess())
            {
                List validChildren = new ArrayList();
                validChildren.addAll(Arrays.asList(childrenToAssignStr));
                validChildren.removeAll(bulkEditGroupValidationResult.getInvalidChildren());
                prunedChildrenToAssign = StringUtils.join(validChildren.iterator(), ", ");                
            }
        }
        // Perform the remove specific validation
        else if (TextUtils.stringSet(unassign))
        {
            if (childrenToUnassign == null || childrenToUnassign.length <= 0)
            {
                addErrorMessage(getText("admin.editnestedgroups.error.no.children.to.remove"));
                return;
            }

            groupService.validateRemoveGroupsFromGroups(getJiraServiceContext(), getGroupRemoveChildMapper());
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (TextUtils.stringSet(assign))
        {
            groupService.addGroupsToGroups(getJiraServiceContext(), Arrays.asList(selectedGroupsStr), Arrays.asList(childrenToAssignStr));
        }
        else if (TextUtils.stringSet(unassign))
        {
            groupService.removeGroupsFromGroups(getJiraServiceContext(), getGroupRemoveChildMapper());
        }
        return redirectToView();
    }

    /**
     * gets all the groups, used to populate the groups select list
     */
    public Collection getAllVisibleGroups()
    {
        return globalPermissionGroupAssociationUtil.getNonAdminGroups(new ArrayList(groupManager.getAllGroups()));
    }

    /**
     * Checks if the group is selected
     */
    public boolean getIsGroupSelected(Group group)
    {
        return (getSelectedGroups() != null && getSelectedGroups().contains(group));
    }

    /**
     * Used to populate the assigned children of the selected groups.<br>
     * Always has the 'All' group which represents all the members of the selected groups.<br>
     * Rest of the children are added under individual group names.
     */
    public Collection getMembersList()
    {
        if (membersList != null || getSelectedGroups() == null)
            return membersList;

        membersList = new ArrayList();
        overloadedGroups = new ArrayList();
        boolean singleGroupSelected = getSelectedGroups().size() == 1;

        //for each selected group
        GroupOption allGroupOption = new GroupOption(getText("admin.editnestedgroups.all.selected.groups"));
        membersList.add(allGroupOption);
        Iterator groups = getSelectedGroups().iterator();
        while (groups.hasNext())
        {
            Group group = (Group) groups.next();
            //and for each children in that group
            if (group != null)
            {
                int count = 0;
                GroupOption groupOption = new GroupOption(group);
                Collection<String> children = groupService.getChildGroupNames(group);
                for (String childName : children)
                {
                    Group child = crowdService.getGroup(childName);
                    // increment count
                    count++;

                    if (count > getMaxChildrenDisplayedPerGroup())
                    {
                        overloadedGroups.add(group.getName());
                        break;
                    }

                    if (singleGroupSelected || !isChildInAllGroupsSelected(childName))
                    {
                        groupOption.addChildOption(new ChildGroupOption(child));
                    }
                    else //if more than one group is selected and the child is in all groups add to the main list
                    {
                        allGroupOption.addChildOption(new ChildGroupOption(child));
                    }
                }

                if (!groupOption.getChildOptions().isEmpty())
                    membersList.add(groupOption);
            }
        }
        if (allGroupOption.getChildOptions().isEmpty()) //remove only if empty (must be inserted at top to keep order)
            membersList.remove(allGroupOption);
        return membersList;
    }

    /**
     * Counts the total number of child entries from the memberslist.<br>
     * NOTE: This does not count distinct children - so with multiple selected groups, the count may be off
     */
    public int getAssignedChildrenCount()
    {
        int assignedChildrenCount = 0;
        Iterator groupOptions = getMembersList().iterator();
        while (groupOptions.hasNext())
        {
            GroupOption groupOption = (GroupOption) groupOptions.next();
            assignedChildrenCount += groupOption.getChildOptions().size();
        }
        return assignedChildrenCount;
    }

    /**
     * determine what size the assigned children select list should be (capped at MAX_LIST_SIZE)
     */
    public int getAssignedChildrenListSize()
    {
        return getListSize(getAssignedChildrenCount() + getMembersList().size());
    }

    /**
     * use this to limit the listSizes to MAX_LIST_SIZE
     */
    public int getListSize(int size)
    {
        return size < MAX_LIST_SIZE ? size : MAX_LIST_SIZE;
    }

    /**
     * used to determine what the option value (format) for a ChildOption should be
     */
    public String getOptionValue(ChildGroupOption childOption)
    {
        if (childOption != null)
        {
            GroupOption parentOption = childOption.getParentOption();
            if (parentOption != null && parentOption.getGroup() != null)
            {
                return childOption.getName() + OPTION_VALUE_SEPERATOR + parentOption.getRawName();
            }
            else
            {
                return childOption.getName();
            }
        }
        return "";
    }

    public String getUnassign()
    {
        return unassign;
    }

    public void setUnassign(String unassign)
    {
        this.unassign = unassign;
    }

    public String getAssign()
    {
        return assign;
    }

    public void setAssign(String assign)
    {
        this.assign = assign;
    }

    public String[] getSelectedGroupsStr()
    {
        return selectedGroupsStr;
    }

    public void setSelectedGroupsStr(String[] selectedGroupsStr)
    {
        this.selectedGroupsStr = selectedGroupsStr;
    }

    /**
     * Of the groups the user has selected
     * @return List of Groups
     */
    public List getSelectedGroups()
    {
        if (selectedGroupsStr == null)
        {
            return new ArrayList();
        }

        if (selectedGroups == null)
        {
            selectedGroups = new ArrayList();
            for (String groupName : selectedGroupsStr)
            {
                Group group = crowdService.getGroup(groupName);
                if (group != null)
                {
                    selectedGroups.add(group);
                }
            }
        }
        return selectedGroups;
    }

    public void setChildrenToAssignStr(String[] childrenToAssignStr)
    {
        this.childrenToAssignStr = childrenToAssignStr;
    }

    public String[] getChildrenToAssignStr()
    {
        return childrenToAssignStr;
    }

    /**
     * @return collection of valid child names to assign to the currently selected groups
     */
    public String getPrunedChildrenToAssign()
    {
        return prunedChildrenToAssign;
    }

    public void setChildrenToUnassign(String[] childrenToUnassign)
    {
        this.childrenToUnassign = childrenToUnassign;
    }

    public String[] getChildrenToUnassign()
    {
        return childrenToUnassign;
    }

    public boolean isTooManyChildrenListed()
    {
        return overloadedGroups != null && !overloadedGroups.isEmpty();
    }

    public int getMaxChildrenDisplayedPerGroup()
    {
        if (maxMembers == null)
        {
            final int MAX_CHILDREN_DISPLAYED_PER_GROUP = 200;

            String maxMembersStr = applicationProperties.getDefaultBackedString(APKeys.USER_MANAGEMENT_MAX_DISPLAY_MEMBERS);
            if (maxMembersStr != null)
            {
                try
                {
                    maxMembers = Integer.valueOf(maxMembersStr.trim());
                }
                catch (NumberFormatException e)
                {
                    log.warn("Invalid format of '" + APKeys.USER_MANAGEMENT_MAX_DISPLAY_MEMBERS + "' property: '" + maxMembersStr + "'. Value should be an integer. Using " + MAX_CHILDREN_DISPLAYED_PER_GROUP);
                    maxMembers = new Integer(MAX_CHILDREN_DISPLAYED_PER_GROUP);
                }
            }
            else
            {
                log.debug("'" + APKeys.USER_MANAGEMENT_MAX_DISPLAY_MEMBERS + "' is missing. Using " + MAX_CHILDREN_DISPLAYED_PER_GROUP + " instead.");
                maxMembers = new Integer(MAX_CHILDREN_DISPLAYED_PER_GROUP);
            }
        }

        return maxMembers.intValue();
    }

    public String getPrettyPrintOverloadedGroups()
    {
        StringBuilder sb = new StringBuilder();
        int length = overloadedGroups.size();
        for (int i=0; i < length; i++)
        {
            sb.append(overloadedGroups.get(i));
            if (i == length - 2 && length > 1 )
                sb.append(" ").append(getText("common.words.and")).append(" ");
            else if (i < length - 1)
                sb.append(", ");
        }
        return sb.toString();
    }

    private GroupRemoveChildMapper getGroupRemoveChildMapper()
    {
        GroupRemoveChildMapper groupRemoveChildMapper = new GroupRemoveChildMapper(Arrays.asList(selectedGroupsStr));

        for (final String aChildrenToUnassign : childrenToUnassign)
        {
            //extract the childname and the group name
            String childname = extractChildName(aChildrenToUnassign);
            String groupname = extractGroupName(aChildrenToUnassign);
            if (groupname != null)
            {
                groupRemoveChildMapper.register(childname, groupname);
            }
            else
            {
                groupRemoveChildMapper.register(childname);
            }
        }
        return groupRemoveChildMapper;
    }

    /**
     * Returns the childname without without the appended group name
     * @param optionValue
     * @return
     */
    private String extractChildName(String optionValue)
    {
        int splitIndex = optionValue.indexOf(OPTION_VALUE_SEPERATOR);
        //JRA-14495: Need to allow for childnames that are 1 character long too.
        if (splitIndex >= 1)
        {
            return optionValue.substring(0, splitIndex);
        }

        return optionValue;
    }

    /**
     * Returns the group name the child is to be removed from.<br>
     * The group name is null if the child is a member of all the selected groups.
     * @param optionValue
     */
    private String extractGroupName(String optionValue)
    {
        int splitIndex = optionValue.indexOf(OPTION_VALUE_SEPERATOR);
        //JRA-14495: Need to allow for childnames that are 1 character long too.
        if (splitIndex >= 1)
        {
            return optionValue.substring(splitIndex + OPTION_VALUE_SEPERATOR.length());
        }
        return null;
    }

    private String redirectToView()
    {
        StringBuilder redirectUrl = new StringBuilder("EditNestedGroups!default.jspa?");

        Iterator groups = getSelectedGroups().iterator();
        while (groups.hasNext())
        {
            Group group = (Group) groups.next();
            redirectUrl.append("selectedGroupsStr=").append(JiraUrlCodec.encode(group.getName()));
            if (groups.hasNext())
                redirectUrl.append("&");
        }

        return getRedirect(redirectUrl.toString());
    }

    private boolean isChildInAllGroupsSelected(String childname)
    {
        //get all the children of all the selected groups into one hashmap
        if (selectedGroupsChildnames == null)
        {
            selectedGroupsChildnames = new HashSet();
            Iterator selectedGroups = getSelectedGroups().iterator();
            if (selectedGroups.hasNext())
            {
                Group group = (Group) selectedGroups.next();
                selectedGroupsChildnames.addAll(groupService.getChildGroupNames(group));
                while (selectedGroups.hasNext())
                {
                    group = (Group) selectedGroups.next();
                    selectedGroupsChildnames.retainAll(groupService.getChildGroupNames(group));
                }
            }
        }
        return selectedGroupsChildnames.contains(childname);
    }

}
