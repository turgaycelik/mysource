<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:bean name="'com.atlassian.jira.util.JiraDateUtils'" id="dateUtils" />
<html>
<head>
	<title><ww:text name="'admin.indexing.optimize'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="indexing"/>
</head>
<body>
<ww:if test="indexing == true">
    <%-- Only allow to optimize the index if indexing is enabled --%>
    <page:applyDecorator name="jiraform">
        <page:param name="action">IndexOptimize.jspa</page:param>
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.indexing.optimize'"/></page:param>
        <page:param name="cancelURI">IndexAdmin.jspa</page:param>
        <%-- NOTE: the following MUST be all on one line so we don't get returns inside the submit button
    --%><page:param name="submitName"><ww:if test="optimizeTime > 0"><ww:text name="'admin.indexing.dooptimize.again'"/></ww:if><ww:else><ww:text name="'admin.indexing.dooptimize'"/></ww:else></page:param>
        <page:param name="submitId">optimise_submit</page:param>
        <page:param name="instructions">
            <ww:if test="optimizeTime > 0">
                <p>
                    <ww:text name="'admin.indexing.optimizing.was.successful'">
                        <ww:param name="'value0'"><strong><span class="greenText"></ww:param>
                        <ww:param name="'value1'"></span></strong></ww:param>
                        <ww:param name="'value2'"><strong><ww:property value="@dateUtils/formatTime(optimizeTime)" /></strong></ww:param>
                    </ww:text>
                </p>
                <p><a href="IndexAdmin.jspa"><ww:text name="'admin.indexing.admin.url.title'"/></a></p>
            </ww:if>
            <ww:else>
                <p>
                    <ww:text name="'admin.indexing.to.optimize.click.the.button'"/>
                    <ww:text name="'admin.indexing.this.may.take.a.minute.or.two'"/>
                </p>
                <p>
                    <ww:text name="'admin.indexing.optimize.note'">
                        <ww:param name="'value0'"><span class="note"></ww:param>
                        <ww:param name="'value1'"></span></ww:param>
                    </ww:text>
                </p>
            </ww:else>
        </page:param>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jirapanel">
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.indexing.optimize'"/></page:param>
        <page:param name="description">
            <p>
                <ww:text name="'admin.indexing.is.currently.off'">
                    <ww:param name="'value0'"><span class="warning"></ww:param>
                    <ww:param name="'value1'"></span></ww:param>
                </ww:text>
            </p>
            <p><a href="IndexAdmin.jspa"><ww:text name="'admin.indexing.admin.url.title'"/></a></p>
        </page:param>
    </page:applyDecorator>
</ww:else>
</body>
</html>
