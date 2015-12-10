<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<tr>
    <td align="center" width="40%"><b><ww:text name="'configureportal.leftcolumn'"/></b></td>
    <td width="20%" colspan="3" align="center">&nbsp;
    </td>
    <td align="center" width="40%"><b><ww:text name="'configureportal.rightcolumn'"/></b></td>
</tr>
<tr>
    <td>
        <ui:component label="''" name="'selectedLeftPortlets'" template="../common/selectmultiple.jsp">
            <ww:param name="'style'" value="'width: 100%;'" />
            <ww:param name="'list'" value="leftPortlets" />
            <ww:param name="'listKey'" value="'id'" />
            <ww:param name="'listValue'" value="'/portletName(.)'" />
            <ww:param name="'size'" value="'10'" />
            <ww:param name="'internat'" value="'false'" />
        </ui:component>
    </td>
    <td width="5%" align="left">
        <input type="image" src="<%= request.getContextPath() %>/images/icons/arrow_up_blue.gif" name="moveUpLeftButton" value=" ^ " title="<ww:text name="'configureportal.move.up'"/>"  style="padding-bottom: 10px;"><br />
        <input type="image" src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" name="moveRightButton" value=" > " title="<ww:text name="'configureportal.move.right'"/>" style="padding-bottom: 10px;"><br />
        <input type="image" src="<%= request.getContextPath() %>/images/icons/arrow_down_blue.gif" name="moveDownLeftButton" value=" v " title="<ww:text name="'configureportal.move.down'"/>">
    </td>
    <td width="10%" align="center">
        <div style="padding: 6px; width: 20px;" >
            <input type="image" src="<%= request.getContextPath() %>/images/icons/add_16.gif" name="addButton" value="<ww:text name="'configureportal.add'"/>" title="<ww:text name="'configureportal.tooltip.add'"/>" style="padding-bottom: 10px;"><br />
            <input type="image" src="<%= request.getContextPath() %>/images/icons/confg_16.gif" name="editButton" value="<ww:text name="'configureportal.edit'"/>" title="<ww:text name="'configureportal.tooltip.edit'"/>" style="padding-bottom: 10px;"><br />
            <input type="image" src="<%= request.getContextPath() %>/images/icons/copy_16.gif" name="copyButton" value="<ww:text name="'common.words.copy'"/>" title="<ww:text name="'configureportal.tooltip.copy'"/>" style="padding-bottom: 10px;"><br />
            <input type="image" src="<%= request.getContextPath() %>/images/icons/trash_16.gif" name="deleteButton" value="<ww:text name="'common.words.delete'"/>" title="<ww:text name="'configureportal.tooltip.delete'"/>">
        </div>
    </td>
    <td width="5%" align="right">
        <input type="image" src="<%= request.getContextPath() %>/images/icons/arrow_up_blue.gif" name="moveUpRightButton" value=" ^ " title="<ww:text name="'configureportal.move.up'"/>"style="padding-bottom: 10px;"><br />
        <input type="image" src="<%= request.getContextPath() %>/images/icons/arrow_left_small.gif" name="moveLeftButton" value=" < " title="<ww:text name="'configureportal.move.left'"/>"style="padding-bottom: 10px;"><br/>
        <input type="image" src="<%= request.getContextPath() %>/images/icons/arrow_down_blue.gif" name="moveDownRightButton" value=" v " title="<ww:text name="'configureportal.move.down'"/>">
    </td>
    <td>
        <ui:component label="''" name="'selectedRightPortlets'" template="../common/selectmultiple.jsp" >
            <ww:param name="'style'" value="'width: 100%;'" />
            <ww:param name="'list'" value="rightPortlets" />
            <ww:param name="'listKey'" value="'id'" />
            <ww:param name="'listValue'" value="'/portletName(.)'" />
            <ww:param name="'size'" value="'10'" />
            <ww:param name="'internat'" value="'false'" />
        </ui:component>
    </td>
</tr>
