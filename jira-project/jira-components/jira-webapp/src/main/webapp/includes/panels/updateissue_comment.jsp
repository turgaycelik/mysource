<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<ww:if test="/issueExists == true && hasIssuePermission('comment', /issue) == true">
    <ww:property value="/fieldScreenRendererLayoutItemForField(/field('comment'))/fieldLayoutItem/orderableField/editHtml(/fieldScreenRendererLayoutItemForField(/field('comment'))/fieldLayoutItem, /, /, /issueObject, /displayParams)" escape="'false'" />
</ww:if>
