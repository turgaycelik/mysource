<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="/issueValid == true">
        <title><ww:text name="'viewissue.remove.attachment.title'"/>: <ww:property value="/attachment/filename" /></title>
        <meta name="decorator" content="issueaction" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/issueValid == true">
    <page:applyDecorator id="delete-attachment" name="auiform">
        <page:param name="action">DeleteAttachment.jspa</page:param>
        <page:param name="submitButtonName">Delete</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url atltoken="false" value="/nextUrl"/></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'viewissue.remove.attachment.title'"/>: <ww:property value="/attachment/filename" /></aui:param>
            <aui:param name="'escape'" value="false"/>
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'"><p><ww:text name="'viewissue.remove.attachment.msg'"/></p></aui:param>
        </aui:component>

        <aui:component template="hidden.jsp" theme="'aui'" name="'id'" value="/issueId" />
        <aui:component template="hidden.jsp" theme="'aui'" name="'deleteAttachmentId'" value="/deleteAttachmentId" />
        <aui:component template="hidden.jsp" theme="'aui'" name="'from'" />
    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <%@ include file="/includes/issue/generic-errors.jsp" %>
    </div>
</ww:else>
</body>
</html>
