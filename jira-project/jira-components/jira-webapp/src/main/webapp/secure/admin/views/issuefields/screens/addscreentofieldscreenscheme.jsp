<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screen_scheme"/>
    <title><ww:text name="'admin.issuefields.screenschemes.configure.screen.scheme'"/></title>
</head>
<body>

<ww:if test="/addableIssueOperations/empty == false && /fieldScreens/empty == false">
    <page:applyDecorator name="auiform" id="add-screen-scheme-item-form">
        <page:param name="id">add-screen-scheme-item-form</page:param>
        <page:param name="cancelLinkURI"><ww:url atltoken="false" page="ConfigureFieldScreenScheme.jspa"><ww:param name="'id'" value="/id"/></ww:url></page:param>
        <page:param name="action">AddFieldScreenSchemeItem.jspa</page:param>
        <page:param name="submitButtonName">Add</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.issuefields.screenschemes.add.issue.operation'"/></aui:param>
        </aui:component>

        <aui:component template="hidden.jsp" theme="'aui'" name="'id'" label="'label'" value="/id"/>

        <page:applyDecorator name="auifieldgroup">
            <aui:select theme="'aui'" label="text('admin.issuefields.screenschemes.issue.operation')" name="'issueOperationId'" list="/addableIssueOperations" listKey="'/issueOperaionId(.)'" listValue="'/text(./nameKey)'" id="'operation'"/>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.issuefields.screenschemes.add.description'"/></page:param>
            <aui:select theme="'aui'" label="text('admin.common.words.screen')" name="'fieldScreenId'" list="/fieldScreens" listKey="'./id'" listValue="'./name'" id="'screen'"/>
        </page:applyDecorator>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="auiform">
        <page:param name="cancelLinkURI"><ww:url atltoken="false" page="ConfigureFieldScreenScheme.jspa"><ww:param name="'id'" value="/id"/></ww:url></page:param>
        <page:param name="id">add-screen-scheme-item-form</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.issuefields.screenschemes.add.issue.operation'"/></aui:param>
        </aui:component>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
            <ww:if test="/addableIssueOperations/empty == true">
                <p><ww:text name="'admin.issuefields.screenschemes.all.operations.have.an.association'"/></p>
            </ww:if>
            <ww:elseIf test="/fieldScreens/empty == true">
                <p><ww:text name="'admin.issuefields.screenschemes.no.screens.exist'">
                    <ww:param name="'value0'"><b><a id="create_fieldscreen" href="ViewFieldScreens.jspa"></ww:param>
                    <ww:param name="'value1'"></a></b></ww:param>
                </ww:text></p>
            </ww:elseIf>
            </aui:param>
        </aui:component>
    </page:applyDecorator>
</ww:else>
</body>
</html>
