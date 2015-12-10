<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:task");
%>

<ww:property value="parameters['nameValue']">
    <ww:if test=". != null">
        <div class="pb_border" id="pb_taskid_<ww:property value="taskId"/>">
            <%-- description part --%>
            <div class="pb_section">
                <span class="pb_description"><ww:property value="description"/></span>

                <ww:if test="parameters['acknowledgementURL']  && finished == true && userWhoStartedTask == true">
                    <div class="pb_taskinfo" style="text-align:right;">
                          <span><ww:text name="'common.tasks.info.acknowledge.task'">
                              <ww:param name="'value0'"><a href="<ww:property value="progressURL" escape="false"/>"></ww:param>
                              <ww:param name="'value2'"></a></ww:param>
                          </ww:text></span>
                    </div>
                </ww:if>
            </div>


            <%-- progress bar part --%>
            <div class="pb_barborder pb_section">
                <table class="pb_bartable" border="0" cellpadding="0" cellspacing="0">
                    <tr>
                        <ww:if test="progressNumber > 0">
                            <td class="pb_barlefttd"  style="width : <ww:property value="progressNumber"/>%"><img src="<%= request.getContextPath() %>/images/border/spacer.gif" alt="" class="pb_img" /></td>
                        </ww:if>
                        <ww:if test="progressNumber < 100">
                            <td class="pb_barrighttd" style="width : <ww:property value="inverseProgressNumber"/>%"><img src="<%= request.getContextPath() %>/images/border/spacer.gif" alt="" class="pb_img" /></td>
                        </ww:if>
                    </tr>
                </table>
            </div>

            <%-- progress message  part --%>
            <ww:if test="exceptionCause">
                <div class="pb_section">
                    <span><ww:property value="exceptionCause/message"/></span>
                </div>
            </ww:if>
            <ww:elseIf test="lastProgressEvent">
                <div class="pb_section">
                    <span>
                    <ww:if test="lastProgressEvent/currentSubTask">
                        <ww:property value="lastProgressEvent/currentSubTask"/>:
                    </ww:if>
                    <ww:property value="lastProgressEvent/message"/>
                    </span>
               </div>
            </ww:elseIf>


            <%-- details part --%>
            <div class="pb_section">
                <div class="pb_taskinfo ">
                    <span><ww:property value="formattedProgress"/></span>
                </div>
                <ww:if test="started == true">
                    <div class="pb_taskinfo">
                        <ww:if test="userWhoStartedTask == false">
                            <span><ww:text name="'common.tasks.info.started.by'">
                                <ww:param name="'value0'"><ww:property value="formattedStartedTimestamp"/></ww:param>
                                <ww:param name="'value1'"><a href="<ww:property value="userURL"/>"><ww:property value="userName"/></a></ww:param>
                            </ww:text></span>
                        </ww:if>
                        <ww:else>
                            <span><ww:text name="'common.tasks.info.started'">
                                <ww:param name="'value0'"><ww:property value="formattedStartedTimestamp"/></ww:param>
                            </ww:text></span>
                        </ww:else>
                    </div>
                    <ww:if test="finished == true">
                        <div class="pb_taskinfo">
                            <span><ww:text name="'common.tasks.info.finished'">
                                <ww:param name="'value0'"><ww:property value="formattedFinishedTimestamp"/></ww:param>
                            </ww:text></span>
                        </div>
                    </ww:if>
                    <ww:if test="formattedExceptionCause">
                    <div class="pb_taskexception">
                        <pre><ww:property value="formattedExceptionCause"/></pre>
                    </ww:if>
                    </div>
                </ww:if>
            </div>
        </div>
    </ww:if>
</ww:property>
