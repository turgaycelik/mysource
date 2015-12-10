<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/issue_features"/>
    <meta name="admin.active.tab" content="linking"/>
	<title><ww:text name="'admin.deletelinktype.title'">
	    <ww:param name="'value0'">[<ww:property value="linkType/string('linkname')" />]</ww:param>
	</ww:text></title>
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.deletelinktype.title'">
        <ww:param name="'value0'"><ww:property value="linkType/string('linkname')" /></ww:param>
    </ww:text></page:param>
	<page:param name="autoSelectFirst">false</page:param>
	<page:param name="description">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.deletelinktype.confirmation'"/></p>
		        <p>
                    <ww:text name="'admin.deletelinktype.matching.links'">
		                <ww:param name="'value0'"><b><ww:property value="links/size" /></b></ww:param>
		            </ww:text>
                </p>
            </aui:param>
        </aui:component>
	</page:param>

    <page:param name="width">100%</page:param>
	<page:param name="action">DeleteLinkType.jspa</page:param>
	<page:param name="submitId">delete_link_type</page:param>
	<page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
	<page:param name="cancelURI">ViewLinkTypes!default.jspa</page:param>

    <ww:if test="links/size > 0">
        <ww:if test="otherLinkTypes/size > 0">
            <tr>
                <td class="fieldLabelArea">&nbsp;</td>
                <td class="fieldValueArea">
                    <input class="radio" type="radio" name="action" value="swap" <ww:if test="action == 'swap'">checked="checked"</ww:if> />
                    <ww:text name="'admin.deletelinktype.swap.current.links.to'"/>:
                    <select name="swapLinkTypeId">
                        <ww:iterator value="otherLinkTypes">
                        <option value="<ww:property value="long('id')" />" <ww:if test="swapLinkTypeId == id">selected="selected"</ww:if> >
                            <ww:property value="string('linkname')" />
                        </option>
                        </ww:iterator>
                    </select>
                </td>
            </tr>
            <tr>
                <td class="fieldLabelArea">&nbsp;</td>
                <td class="fieldValueArea">
                    <input class="radio" id="removeAllLinks" type="radio" name="action" value="remove" <ww:if test="action == 'remove'">checked="checked"</ww:if> />
                    <label for="removeAllLinks"><ww:text name="'admin.deletelinktype.remove.all.links'"/></label>
                </td>
            </tr>
        </ww:if>
        <ww:else>
            <tr>
                <td class="fieldLabelArea">&nbsp;</td>
                <td class="fieldValueArea">
                    <input type="hidden" name="action" value="remove"/>
                    <ww:text name="'admin.deletelinktype.no.other.link.types'"/>
                </td>
            </tr>
        </ww:else>
    </ww:if>
    <ww:else>
        <tr>
            <td class="fieldLabelArea">&nbsp;</td>
            <td class="fieldValueArea">
                <input type="hidden" name="action" value="remove" />
                <ww:text name="'admin.deletelinktype.no.link.types'">
                    <ww:param name="'value0'"><b></ww:param>
                    <ww:param name="'value1'"></b></ww:param>
                </ww:text>
            </td>
        </tr>
    </ww:else>
	<ui:component name="'id'" template="hidden.jsp" />
	<input type="hidden" name="confirm" value="true" />
</page:applyDecorator>

</body>
</html>
