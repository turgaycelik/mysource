<%@ taglib uri="webwork" prefix="ww" %>
<%@ include file="/template/standard/controlheader.jsp" %>
<%@ page import="com.atlassian.jira.web.util.HelpUtil" %>

<% HelpUtil helpUtil = new HelpUtil();
   HelpUtil.HelpPath internalHelpPath = helpUtil.getHelpPath("hsqldb");
   HelpUtil.HelpPath externalHelpPath = helpUtil.getHelpPath("dbconfig.index");
%>

<div class="formOne">
    <div>
        <input class="radio" type="radio" id="databaseOption_INTERNAL" name="databaseOption" value="INTERNAL" <ww:if test="databaseOption == 'INTERNAL'">CHECKED</ww:if> />
        <label for="databaseOption_INTERNAL"><ww:text name="'setupdb.internal.label'"/></label>
        <div class="field-description">
            <p>
                <ww:text name="'setupdb.internal.desc'">
                    <ww:param name="'value0'"><a href="<%=internalHelpPath.getUrl()%>" target="_blank"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </p>
        </div>
    </div>
    <br />
    <div>
        <input class="radio" type="radio" id="databaseOption_EXTERNAL" name="databaseOption" value="EXTERNAL" <ww:if test="databaseOption == 'EXTERNAL'">CHECKED</ww:if> />
        <label for="databaseOption_EXTERNAL"><ww:text name="'setupdb.external.label'"/></label>
        <div class="field-description">
            <p>
                <ww:text name="'setupdb.external.desc'">
                    <ww:param name="'value0'"><a href="<%=externalHelpPath.getUrl()%>" target="_blank"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </p>
        </div>
    </div>
</div>

<%@ include file="/template/standard/controlfooter.jsp" %>
