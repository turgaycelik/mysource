<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<ui:component label="text('admin.issuefields.customfields.choose.applicable.issue.types')" template="sectionbreak.jsp">
   <ui:param name="'instructions'">
    <ww:text name="'admin.schemecontext.customfield.instruction'"/>
   </ui:param>
</ui:component>

<ui:select label="text('admin.menu.issuesettings.issue.types')" name="'issuetypes'" template="selectmultiple.jsp"
           list="/allIssueTypes" listKey="'id'" listValue="'nameTranslation'" >
    <ui:param name="'headerrow'"><ww:text name="'admin.schemecontext.anyissuetype'"/></ui:param>
    <ui:param name="'headervalue'">-1</ui:param>
    <ui:param name="'optionIcon'">iconUrl</ui:param>
    <ui:param name="'optionTitle'">descTranslation</ui:param>
    <ui:param name="'description'"><ww:text name="'admin.schemecontext.applyforall'"/></ui:param>
    <ui:param name="'size'">5</ui:param>
</ui:select>

    <ui:component label="text('admin.schemecontext.chooseapplicable')" template="sectionbreak.jsp">
       <ui:param name="'instructions'">
       <ww:text name="'admin.schemecontext.instruction'"/>
       </ui:param>
    </ui:component>


    <ww:if test="/globalAvailable != false">
        <ui:radio label="''"
                  name="'global'"
                  list="/globalContextOption">
        <%--        <ui:param name="'onclick'" value="'return handleProjectDisplay();'" />--%>
        </ui:radio>
    </ww:if>
    <ww:else>
        <ui:component name="'global'" template="hidden.jsp" theme="'single'" value="'false'" />
    </ww:else>

<%--<ui:select label="'Project Categories'" name="'projectCategories'" template="selectmultiple.jsp"--%>
<%--           list="/allProjectCategories" listKey="'long('id')'" listValue="'string('name')'" >--%>
<%--   <ui:param name="'onclick'">document.getElementById('global_false').click();</ui:param>--%>
<%--   <ui:param name="'optionTitle'">string('description')</ui:param>--%>
<%--   <ui:param name="'description'">Apply for all issues in any selected project categories</ui:param>--%>
<%--</ui:select>--%>

<ui:select label="text('common.concepts.projects')" name="'projects'" template="selectmultiple.jsp"
           list="/allProjects" listKey="'long('id')'" listValue="'string('name')'" >
   <ww:if test="/globalAvailable != false">
        <ui:param name="'onclick'">document.getElementById('global_false').click();</ui:param>
   </ww:if>
   <ui:param name="'optionTitle'">string('description')</ui:param>
   <ui:param name="'description'"><ww:text name="'admin.schemecontext.applyforall2'"/></ui:param>
   <ui:param name="'size'">5</ui:param>
</ui:select>

