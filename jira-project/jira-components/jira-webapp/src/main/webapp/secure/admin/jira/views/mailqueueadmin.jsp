<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.mailqueue.mail.queue.admin'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/mail_section"/>
    <meta name="admin.active.tab" content="mail_queue"/>
</head>

<body>

<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.mailqueue.mail.queue'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
		<ww:if test="/page == 'errorqueue'">
			<ww:text name="'admin.mailqueue.description'"/>
		</ww:if>
		<ww:else>
            <ww:text name="'admin.mailqueue.this.page.shows'"/>
		</ww:else>
        <ww:if test="/mailSendingDisabled == true">
            <p><ww:text name="'admin.mailqueue.sending.mail.is.disabled'">
                    <ww:param name="'value0'"><span class="warning"></ww:param>
                    <ww:param name="'value1'"></span></ww:param>
                </ww:text></p>
        </ww:if>
        <ww:if test="/hasMailServer == false">
            <p><span class="warning"><ww:text name="'admin.common.words.warning'"/></span>:
                <ww:if test="/systemAdministrator == true" >
                    <ww:text name="'admin.mailqueue.no.default.mail.server'">
                        <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/secure/admin/OutgoingMailServers.jspa"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.mailqueue.no.default.mail.server.admin'">
                        <ww:param name="'value0'"> </ww:param>
                        <ww:param name="'value1'"> </ww:param>
                    </ww:text>
                </ww:else>
            </p>
        </ww:if>
        <ww:if test="/enabledNotificationSchemes == false">
            <p><ww:text name="'admin.mailqueue.no.associated.notification.schemes'">
                    <ww:param name="'value0'"><span class="warning"></ww:param>
                	<ww:param name="'value1'"></span></ww:param>
            </ww:text>
            </p>
        </ww:if>
    
    </page:param>
	<ww:if test="/page == 'errorqueue'">
		<p>
			<ww:text name="'admin.mailqueue.number.of.items'">
			    <ww:param name="'value0'"><b><ww:property value="mailQueue/errorSize" /></b></ww:param>
			</ww:text>
		</p>

        <ul class="optionslist">
            <li><ww:text name="'admin.mailqueue.resend.error.queue'">
			    <ww:param name="'value0'"><a href="<ww:url page="MailQueueAdmin.jspa"><ww:param name="'resend'" value="'true'"/><ww:param name="'page'" value="page"/></ww:url>"></ww:param>
			    <ww:param name="'value1'"></a></ww:param>
			</ww:text></li>
            <li><ww:text name="'admin.mailqueue.delete.error.queue'">
			    <ww:param name="'value0'"><a href="<ww:url page="MailQueueAdmin.jspa"><ww:param name="'delete'" value="'true'"/><ww:param name="'page'" value="page"/></ww:url>"></ww:param>
			    <ww:param name="'value1'"></a></ww:param>
			</ww:text></li>
        </ul>

    </ww:if>
    <ww:else>
		<p>
			<ww:text name="'admin.mailqueue.number.of.items'">
			    <ww:param name="'value0'"><b><ww:property value="mailQueue/size" /></b></ww:param>
			</ww:text>
		</p>
        <ww:if test="mailQueue/sending == true">
        <p><ww:text name="'admin.mailqueue.currently.sending'">
            <ww:param name="'value0'"><font color="green"><b></ww:param>
            <ww:param name="'value1'"></b></font></ww:param>
        </ww:text></p>
        <ul class="optionslist">
            <li><ww:text name="'admin.mailqueue.bypass.currently.sending.mail'">
                <ww:param name="'value0'"><a href="<ww:url page="MailQueueAdmin.jspa"><ww:param name="'unstick'" value="'true'"/></ww:url>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text></li>
        </ul>
        </ww:if>
        <div class="buttons-container">
            <ww:text name="'admin.mailqueue.flush.mail.queue'">
                <ww:param name="'value0'"><a class="aui-button" href="<ww:url page="MailQueueAdmin.jspa"><ww:param name="'flush'" value="'true'"/></ww:url>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </div>
        
    </ww:else>
</page:applyDecorator>



<div class="tabwrap tabs2">
    <ul class="tabs horizontal">
	<ww:if test="/page == 'errorqueue'">
		<li>
            <a href="<ww:url page="MailQueueAdmin.jspa"/>">
                <strong><ww:text name="'admin.mailqueue.mail.queue.size'"><ww:param name="'value0'"><ww:property value="mailQueue/size" /></ww:param></ww:text></strong>
            </a>
        </li>
	</ww:if>
	<ww:else>
		<li class="active">
            <strong><ww:text name="'admin.mailqueue.mail.queue.size'"><ww:param name="'value0'"><ww:property value="mailQueue/size" /></ww:param></ww:text></strong>
        </li>
	</ww:else>
	<ww:if test="/page == 'errorqueue'">
		<li class="active">
            <strong><ww:text name="'admin.mailqueue.error.queue.size'"><ww:param name="'value0'"><ww:property value="mailQueue/errorSize" /></ww:param></ww:text></strong>
        </li>
	</ww:if>
	<ww:else>
		<li>
		    <a href="<ww:url page="MailQueueAdmin.jspa"><ww:param name="'page'" value="'errorqueue'"/></ww:url>">
                <strong><ww:text name="'admin.mailqueue.error.queue.size'"><ww:param name="'value0'"><ww:property value="mailQueue/errorSize" /></ww:param></ww:text></strong>
            </a>
		</li>
	</ww:else>
    </ul>
</div>

<table class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="80%">
                <ww:text name="'admin.mailqueue.subject'"/>
            </th>
            <th width="20%">
                <ww:text name="'admin.mailqueue.queued'"/>
            </th>
        </tr>
    </thead>

    <ww:if test="mailQueue/sending == true">
        <tbody>
        <tr style="background-color:#<ww:if test="hasError == true">ffcccc</ww:if><ww:elseIf test="/mailSendingDisabled == true">ffdddd</ww:elseIf>">
            <td>
                <img src="<%= request.getContextPath() %>/images/icons/mail_small.gif" border="0" alt="<ww:text name="'admin.mailqueue.sending'"/>" title="<ww:text name="'admin.mailqueue.sending'"/>">
                <ww:property value="/mailQueue/itemBeingSent/subject"/>
            </td>
            <td>
                <ww:property value="/prettySendingStartTime"/>
            </td>
        </tr>
        </tbody>
    </ww:if>

    <tbody>
        <ww:iterator value="queuedItems" status="'status'">
            <tr style="background-color:#<ww:if test="hasError == true">ffcccc</ww:if><ww:elseIf test="/mailSendingDisabled == true">ffdddd</ww:elseIf>">
                <td>
                    <ww:property value="subject" />
                </td>
                <td>
                    <ww:property value="/outlookDate/formatDMYHMS(dateQueued)" />
                </td>
            </tr>
        </ww:iterator>
    </tbody>

    <ww:if test="queuedItems/size == 0 && mailQueue/sending == false">
        <tbody>
        <tr>
            <td colspan="2">
                <ww:text name="'admin.mailqueue.no.queued.mail.items'"/>
            </td>
        </tr>
        </tbody>
    </ww:if>

</table>

</body>
</html>
