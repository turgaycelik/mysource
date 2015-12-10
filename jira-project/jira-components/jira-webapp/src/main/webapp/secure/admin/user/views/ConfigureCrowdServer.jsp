<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
    <title>
        <ww:text name="'admin.jaacs.application.title'"/>
    </title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_configuration"/>
    <meta name="admin.active.tab" content="crowd_application_list"/>
</head>
<body>
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h1>
                    <ww:text name="'admin.jaacs.application.title'"/>
                    <ww:component name="'jira_as_a_crowd-server'" template="help.jsp"><ww:param name="'noalign'" value="true"/></ww:component>
                </h1>
            </div>
            <div class="aui-page-header-actions">
                <a id="crowd-add-application" class="aui-button" href="<ww:url page="EditCrowdApplication.jspa" atltoken="false"/>">
                    <span class="icon jira-icon-add"></span>
                    <ww:text name="'admin.jaacs.application.add.remote.application'"/>
                </a>
            </div>
        </div>
    </header>

    <%-- info box for giving feedback to user --%>
    <ww:if test="success">
        <div class="aui-message success">
            <span class="aui-icon icon-success"></span>
            <ww:property value="success" escape="true"/>
        </div>
    </ww:if>

    <p><ww:text name="'admin.jaacs.application.description'"/></p>

    <table id="remote-address-list" class="aui">
        <thead>
            <tr>
                <th id="application">
                    <ww:text name="'admin.jaacs.application.remote.application'"/>
                </th>
                <th id="address">
                    <ww:text name="'admin.jaacs.application.remote.address'"/>
                </th>
                <th id="action-header">
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:if test="applications/size == 0">
            <tr>
                <td colspan="3">
                    <ww:text name="'admin.jaacs.application.no.applications.configured'"/>
                    <a id="crowd-add-application-inline" href="<ww:url page="EditCrowdApplication.jspa" atltoken="false"/>"><ww:text name="'admin.jaacs.application.add.first.application'"/></a>
                </td>
            </tr>
        </ww:if>
        <ww:else>
            <ww:iterator value="applications" status="'status'">
                <tr id="crowd-app-<ww:property value="name" escape="true"/>">
                    <td headers="application">
                        <ww:property value="name"/>
                    </td>
                    <td class="action" headers="address">
                        <ww:iterator value="remoteAddresses">
                            <ww:property value="address" escape="true"/><br/>
                        </ww:iterator>
                    </td>
                    <td class="action" headers="action-header">
                        <ul class="menu">
                            <li>
                                <a id="crowd-edit-application-<ww:property value="name" escape="true"/>" class="editApplication" href="<ww:url page="EditCrowdApplication.jspa" atltoken="false">
                                    <ww:param name="'id'" value="id"/>
                                </ww:url>">
                                <ww:text name="'common.words.edit'"/>
                                </a>
                            </li>
                            <li>|</li>
                            <li>
                                <a id="crowd-delete-application-<ww:property value="name" escape="true"/>" class="delete" href="<ww:url page="EditCrowdApplication!delete.jspa">
                                    <ww:param name="'id'" value="id"/>
                                </ww:url>">
                                <ww:text name="'common.words.delete'"/>
                                </a>
                            </li>
                        </ul>
                    </td>
                </tr>
            </ww:iterator>
        </ww:else>
        </tbody>
    </table>
</body>

</html>
