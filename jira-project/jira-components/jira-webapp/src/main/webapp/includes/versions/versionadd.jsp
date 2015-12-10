<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww" %>
<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.manageversions.add.version'"/></page:param>
    <page:param name="action"><ww:url page="AddVersion.jspa"><ww:param name="'pid'" value="project/long('id')" /></ww:url></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="submitId">add_version_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>

        <ui:textfield label="text('admin.manageversions.version.name')" name="'name'" size="'40'">
            <ui:param name="'mandatory'" value="true" />
        </ui:textfield>

        <ui:textfield label="text('common.words.description')" name="'description'" size="'60'">
        </ui:textfield>

        <ui:component label="text('version.releasedate')" name="'releaseDate'" template="datepicker2.jsp">
            <ui:param name="'formname'" value="'jiraform'" />
            <ui:param name="'style'">width: 20%;</ui:param>
        </ui:component>

        <ww:if test="/versionManager/versions(/project) != null && /versionManager/versions(/project)/empty == false">
            <ui:select name="'scheduleAfterVersion'" list="/versionManager/versions(/project)"
                    label="text('admin.manageversions.schedule.before')" listKey="'id'" listValue="'name'" >
             <%--       label="'Schedule After'" listKey="'id'" listValue="'name'" >    --%>
                <ui:param name="'style'">width: 20%;</ui:param>
                <ui:param name="'headerrow'" value="'-- Before First Version --'" />
                <ui:param name="'headervalue'" value="-1" />
                <ui:param name="'description'"><ww:text name="'admin.manageversions.schedule.before.description'">
                    <ww:param name="'value0'"><strong></ww:param>
                    <ww:param name="'value1'"></strong></ww:param>
                    <ww:param name="'value2'"><br /></ww:param>
                </ww:text></ui:param>
                <ww:if test="scheduleAfterVersion == null">
                    <ui:param name="'selectedValue'"><ww:property value="/lastVersion/id"/></ui:param>
                </ww:if>
            </ui:select>
        </ww:if>

        <ui:component name="'scheduleAfterVersion'" template="hidden.jsp"/>
</page:applyDecorator>

