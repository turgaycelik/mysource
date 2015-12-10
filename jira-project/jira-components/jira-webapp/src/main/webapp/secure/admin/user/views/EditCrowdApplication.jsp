<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
    <title>
        <ww:text name="titleTextKey"/>
    </title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_configuration"/>
    <meta name="admin.active.tab" content="crowd_application_list"/>
</head>
<body>

    <page:applyDecorator id="edit-crowd-application" name="auiform">
        <page:param name="action"><ww:property value="submitAction"/></page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.save'"/></page:param>
        <page:param name="submitButtonHideAccessKey">true</page:param>
        <page:param name="cancelLinkURI"><ww:property value="cancelAction"/></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="titleTextKey"/></aui:param>
            <aui:param name="'helpURL'" value="'jira_as_a_crowd-server'"/>
        </aui:component>

        <%-- application name --%>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.jaacs.application.remote.application.description'"/></page:param>
            <aui:textfield id="'name'" label="text('admin.jaacs.application.remote.application')" mandatory="true" name="'name'" theme="'aui'" value="name"/>
        </page:applyDecorator>

        <%-- application password--%>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="passwordDescriptionKey"/></page:param>
            <%-- Password is only mandatory when adding not editing. --%>
            <ww:if test="id">
                <aui:password id="'credential'" label="text('admin.jaacs.application.password')" name="'credential'" theme="'aui'">
                    <aui:param name="'autocomplete'" value="'off'"/>
                </aui:password>
            </ww:if>
            <ww:else>
                <aui:password id="'credential'" label="text('admin.jaacs.application.password')" mandatory="true" name="'credential'" theme="'aui'">
                    <aui:param name="'autocomplete'" value="'off'"/>
                </aui:password>
            </ww:else>
        </page:applyDecorator>

        <%-- application remote addresses --%>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.jaacs.application.edit.address.description'"/></page:param>
            <aui:textarea id="'remoteAddresses'" label="text('admin.jaacs.application.remote.address')" name="'remoteAddresses'" theme="'aui'" value="remoteAddresses" rows="5" size="'large'"/>
        </page:applyDecorator>

        <%-- application id --%>
        <ww:if test="id">
            <aui:component id="applicationId" name="'id'" template="hidden.jsp" theme="'aui'" value="id"/>
        </ww:if>

    </page:applyDecorator>
</body>

</html>