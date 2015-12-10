<%--

Required Parameters:
    * title                      - The text to be used in the heading
    * issueKey                   - The issue key
    * issueSummary               - The summary of the issue
    * cameFromParent             - Whether this dialog was triggered from teh parent issue
    * cameFromSelf               - Whether this dialog was triggered from viewing itself

Optional Params:
    * subtaskTitle               - The title of the dialog for whne it was opened as subtask

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<aui:component template="formHeading.jsp" theme="'aui'">
    <aui:param name="'escape'" value="false"/>
    <aui:param name="'cssClass'">dialog-title</aui:param>
    <aui:param name="'text'">
    <ww:property value="parameters['cameFromSelf']">
        <ww:property value="parameters['cameFromParent']">
            <ww:if test=". && . == true">
                <ww:property value="parameters['subtaskTitle']"/><span <ww:if test=".. & .. == true">style="display:none"</ww:if> class="header-separator">:&nbsp;</span>
            </ww:if>
            <ww:else>
                <ww:property value="parameters['title']"/><span <ww:if test=".. & .. == true">style="display:none"</ww:if> class="header-separator">:&nbsp;</span>
            </ww:else>
        </ww:property>
        <span class="header-issue-key" <ww:if test=". && . == true">style="display:none"</ww:if>><ww:property value="parameters['issueKey']"/></span>
    </ww:property>
    </aui:param>
</aui:component>
