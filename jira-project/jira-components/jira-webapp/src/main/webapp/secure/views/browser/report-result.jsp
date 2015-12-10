<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'common.concepts.report'"/> - <ww:property value="report/name" /></title>
    <style>
        .excel {
            float:right;
            text-align: right;
            padding-top: .5em;
            padding-right: .5em;
            padding-bottom: .5em;
        }
    </style>
    <style media="print">
        .excel {
            display: none;
        }
    </style>
</head>
<body class="page-type-report">
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h1><ww:property value="report/label" /></h1>
        </ui:param>
        <ui:param name="'actionsContent'">
            <div class="aui-buttons">
                <a class="aui-button" href="<ww:url page="ConfigureReport!default.jspa"/>&<ww:property value="queryString"/>">
                    <span class="aui-icon aui-icon-small aui-iconfont-configure"></span>
                    <ww:text name="'report.result.back.to.configuration.link.text'"/>
                </a>
            </div>

        </ui:param>
    </ui:soy>

    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">

            <ww:if test="report/module/excelViewSupported == true">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <p class="excel">
                            <a href="<%= request.getContextPath() %>/secure/ConfigureReport!excelView.jspa?<ww:property value="queryString"/>"><ww:text name="'excel.view'"/><img src="<%= request.getContextPath() %>/images/icons/attach/excel.gif" height="16" width="16" border="0" align="absmiddle" alt="<ww:text name="'excel.view'"/>"/></a>
                        </p>
                        <ww:if test="report/description">
                            <p>
                                <b><ww:text name="'common.concepts.description'"/>:</b><br/>
                                <ww:property value="report/description" escape="false" />
                            </p>
                        </ww:if>
                    </aui:param>
                </aui:component>
            </ww:if>
            <ww:property value="generatedReport" escape="false" />

                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
