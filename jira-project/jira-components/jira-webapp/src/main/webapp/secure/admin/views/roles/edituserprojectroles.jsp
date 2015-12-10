<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.edit.user.projectroles.title'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>

<body>

<script type="text/javascript" language="JavaScript">

var projectSelectShown = false;
var projectRoleIds = new Array(<ww:iterator value="/allProjectRoles" status="'status'">'<ww:property value="./id"/>' <ww:if test="@status/last == false">, </ww:if></ww:iterator>);

function selectProject(projectIds)
{
    document.forms['edituserprojectroles'].action = 'EditUserProjectRoles!refresh.jspa';
    document.getElementById("projects_to_add").value = projectIds;
    document.forms['edituserprojectroles'].submit();
    return false;
}
function unselectRoleForCategory(categoryId, roleId)
{
    var categoryTDs = getElementsByName("td", "category_" + categoryId);

    var controlCheckbox = document.getElementById(roleId + "_check_" + categoryId);
    var check = controlCheckbox.checked;

    // run through all the checkboxes in the row and uncheck
    for (var i = 0; i < categoryTDs.length; i++)
    {
        var categoryTD = categoryTDs[i];
        for (var j = 0; j < categoryTD.childNodes.length; j++)
        {
            var child = categoryTD.childNodes[j];

            if (child.type && child.type.indexOf("checkbox") != -1)
            {
                if (child.id.indexOf("_" + roleId) != -1)
                {
                    child.checked = check;
                }
            }
        }
    }

    return false;
}

function getElementsByName(tag, name)
{
    var elem = document.getElementsByTagName(tag);
    var arr = new Array();
    for (i = 0,iarr = 0; i < elem.length; i++)
    {
        att = elem[i].getAttribute("name");
        if (att == name)
        {
            arr[iarr] = elem[i];
            iarr++;
        }
    }
    return arr;
}

function setProjectSelectTop()
{
    var projectSelect = document.getElementById("projectselect");
    projectSelect.style.top = findPos(document.getElementById("add_project_link"))[1];
}

function findPos(obj)
{
    var curleft = curtop = 0;
    if (obj.offsetParent)
    {
        curleft = obj.offsetLeft;
        curtop = obj.offsetTop;
        while (obj = obj.offsetParent)
        {
            curleft += obj.offsetLeft;
            curtop += obj.offsetTop;
        }
    }
    return [curleft,curtop];
}

</script>

<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.edit.user.projectroles.title'"/>: <ww:property value="/projectRoleEditUser/displayName"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">users</page:param>
    <page:param name="helpURLFragment">#Assigning+a+User+to+a+Project+Role</page:param>
    <page:param name="description">
        <p><ww:text name="'admin.edit.user.projectroles.description.1'"/></p>
        <p><ww:text name="'admin.edit.user.projectroles.description.2'"/></p>
    </page:param>
</page:applyDecorator>
<form method="post" action="EditUserProjectRoles.jspa" name='edituserprojectroles' class="aui">
    <ww:component name="'atl_token'" value="/xsrfToken" template="hidden.jsp"/>

    <ww:if test="/visibleProjectsByCategory/size != 0">
        <ww:if test="/allProjectsVisible == false">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <div id="add_project_link" class="twixi-block collapsed">
                        <p><ww:text name="'admin.edit.user.projectroles.description.3'"/> <ww:text name="'admin.edit.user.projectroles.description.4.a'"/></p>
                        <p>
                            <a href="#projectselect" class="twixi-trigger" style="display: inline-block;"><span class="icon icon-twixi"></span><ww:text name="'admin.edit.user.projectroles.add.project'"/></a>
                        </p>
                        <div id="projectselect" class="twixi-content">
                                <ww:iterator value="/allProjectCategories">
                                    <ww:if test="/allProjectsInCategoryVisible(.) == false">
                                        <h5><ww:text name="'admin.view.user.projectroles.project.category'"/>:
                                            <a href="#" onclick="return selectProject(new Array( <ww:iterator value="allProjectsForCategory(.)" status="'status'"> '<ww:property value="./id"/>' <ww:if test="@status/last == false">,</ww:if> </ww:iterator> )); return false;">
                                                <ww:property value="./string('name')"/>
                                            </a>
                                        </h5>
                                            <ul>
                                                <ww:iterator value="/allProjectsForCategory(.)">
                                                    <ww:if test="/currentVisibleProjects/contains(.) == false">
                                                        <li class="projectselect" id="project_link_<ww:property value="./id"/>">
                                                            <a href="#" onclick="return selectProject(new Array('<ww:property value="./id"/>')); return false;">
                                                                <ww:property value="./name"/>
                                                            </a>
                                                        </li>
                                                    </ww:if>
                                                </ww:iterator>
                                            </ul>
                                        </ul>
                                    </ww:if>
                                </ww:iterator>

                            <ww:if test="/allProjectsWithoutCategoryVisible == false">
                                <h5><ww:text name="'admin.view.user.projectroles.project.category.uncategorised'"/>:</h5>
                                <ul>
                                    <ww:iterator value="/allProjectsWithoutCategory">
                                        <ww:if test="/currentVisibleProjects/contains(.) == false">
                                            <li class="projectselect" id="project_link_<ww:property value="./id"/>">
                                                <a href="#" onclick="return selectProject(new Array('<ww:property value="./id"/>')); return false;" title="<ww:property value="./description"/>">
                                                    <ww:property value="./name"/>
                                                </a>
                                            </li>
                                        </ww:if>
                                    </ww:iterator>
                                </ul>
                            </ww:if>
                        </div>
                    </div>
                </aui:param>
            </aui:component>
        </ww:if>

        <table id="projecttable" class="aui aui-table-rowhover">
            <thead>
                <tr>
                    <th width="25%">
                        <ww:text name="'common.concepts.projects'"/>
                    </th>
                    <ww:iterator value="allProjectRoles">
                        <th width="<ww:property value="/projectRoleColumnWidth"/>%">
                            <ww:property value="./name"/>
                        </th>
                    </ww:iterator>
                </tr>
            </thead>

            <ww:iterator value="/visibleProjectsByCategory">
                <tbody>
                    <tr class="totals">
                        <td>
                            <ww:if test="key != null">
                                <strong><ww:text name="'admin.edit.user.projectroles.project.categories'"/>:</strong> <ww:property value="key/string('name')"/>
                            </ww:if>
                            <ww:else>
                                <strong><ww:text name="'admin.view.user.projectroles.project.category.uncategorised'"/></strong>
                            </ww:else>
                        </td>
                        <ww:iterator value="allProjectRoles">
                            <td>
                                <input type="checkbox" id="<ww:property value="./id"/>_check_<ww:if test="key != null"><ww:property value="key/string('id')"/></ww:if><ww:else>0</ww:else>"
                                       onclick="unselectRoleForCategory(<ww:if test="key != null"><ww:property value="key/string('id')"/></ww:if><ww:else>0</ww:else>, <ww:property value="./id"/> );"/>
                            </td>
                        </ww:iterator>
                    </tr>
                </tbody>
                <tbody>
                    <ww:iterator value="value">
                        <tr name="project" id="project_<ww:property value="./id" />">
                            <td>
                                <ww:property value="./name"/>
                                <input type="hidden" id="project_shown" name="project_shown" value="<ww:property value='./id'/>"/>
                            </td>
                            <ww:iterator value="allProjectRoles">
                                <td name="category_<ww:if test="../../key != null"><ww:property value="../../key/string('id')"/></ww:if><ww:else>0</ww:else>">
                                    <input type="checkbox"
                                           id="<ww:property value="../id"/>_<ww:property value="./id"/>"
                                           name="<ww:property value="../id"/>_<ww:property value="./id"/>"
                                            <ww:if test="/roleForProjectSelected(., ..) == true">checked="checked"</ww:if> />
                                    <input type="hidden"
                                           id="<ww:property value='../id'/>_<ww:property value='./id'/>_orig"
                                           name="<ww:property value='../id'/>_<ww:property value='./id'/>_orig"
                                           value="<ww:property value='/userInProjectRoleTypeUser(., ..)'/>"/>
                                    <ww:if test="/userInProjectRoleOtherType(., ..) != null">
                                        <span class="secondary-text" title="<ww:text name="'admin.view.user.projectroles.group.association'"/>:<ww:property value="/userInProjectRoleOtherType(., ..)"/>">
                                            (<ww:property value="/userInProjectRoleOtherType(., ..)"/>)
                                        </span>
                                    </ww:if>
                                </td>
                            </ww:iterator>
                        </tr>
                    </ww:iterator>
                </tbody>
            </ww:iterator>
        </table>
    </ww:if>
    <ww:else>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'"><ww:text name="'admin.view.user.projectroles.noprojects.found'"/></aui:param>
        </aui:component>
    </ww:else>



    <div class="buttons-container">
        <ww:if test="/visibleProjectsByCategory/size != 0">
            <input class="aui-button" type="submit" name="<ww:text name="'common.words.save'"/>" value="<ww:text name="'common.words.save'"/>"/>
        </ww:if>
        <a class="aui-button aui-button-link" id="cancelButton" href="<ww:url page="ViewUserProjectRoles!default.jspa"><ww:param name="'name'" value="name" /></ww:url>" title="<ww:property value="text('common.forms.cancel')"/> (<ww:property value="@modifierKey"/> + <ww:text name="'common.forms.cancel.accesskey'" />)">
            <ww:property value="text('common.forms.cancel')"/>
        </a>
    </div>

    <ui:component name="'name'" template="hidden.jsp" theme="'aui'"  />
    <ui:component name="'projects_to_add'" id="'projects_to_add'" template="hidden.jsp" theme="'aui'"  />
</form>

</body>
</html>
