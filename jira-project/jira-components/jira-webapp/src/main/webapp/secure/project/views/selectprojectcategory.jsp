
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="aui"  %>
<%@ taglib prefix="jira" uri="jiratags" %>

<html>
<head>
	<title><ww:text name="'admin.projects.select.project.category'"/></title>
    <meta name="admin.active.section" content="atl.jira.proj.config"/>
    <jira:web-resource-require modules="jira.webresources:global-static"/>
</head>

<body>

<ww:if test="projectCategories/size == 1">
    <h2><ww:text name="'admin.projects.no.project.category'"/></h2>
    <div class="form-body">
        <p>
            <ww:text name="'admin.projects.no.categories.created'"/>
        </p>
        <p>
            <ww:text name="'admin.projects.add.new.project.category'">
                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/projectcategories/ViewProjectCategories!default.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </p>
    </div>
</ww:if>
<ww:else>
	<page:applyDecorator id="select-project-category" name="auiform">
        <page:param name="action">SelectProjectCategory.jspa</page:param>
        <page:param name="method">post</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.select'"/></page:param>
        <page:param name="submitButtonName">Select</page:param>
        <page:param name="cancelLinkURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/key"/>/summary</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.projects.select.project.category'"/></aui:param>
        </aui:component>

        <aui:component name="'pid'" template="hidden.jsp" theme="'aui'"/>

        <page:applyDecorator name="auifieldgroup">
            <aui:select label="text('portlet.projects.field.project.category.name')" id="'id'" name="'pcid'"
                        list="/projectCategories" listKey="'./id'" listValue="'./name'"
                        theme="'aui'">
            </aui:select>
        </page:applyDecorator>
	</page:applyDecorator>
</ww:else>

</body>
</html>
