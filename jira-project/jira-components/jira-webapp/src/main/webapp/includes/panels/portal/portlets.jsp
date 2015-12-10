<%@ taglib uri="webwork" prefix="ww" %>
<style>
.rowSelected  { background-color: #dddddd; }
.rowHover  { background-color: #dddddd; }
</style>
<script language="javascript">

    var selected;

    function cellHover(cell)
    {
        cell.oldClassName = cell.className;
        cell.className = 'rowHover';
        cell.onmouseout = function()
        {
            this.className = this.oldClassName;
        }
    }

    function selectCellRadioBox(cell)
    {
        var id = parseInt(cell.id.substring(4, cell.id.length));
        document.forms['jiraform'].elements['portletId'][id - 1].checked = true;
    }

    function openThumbnail(evt, imageUrl)
    {
        thumbnailWindow = window.open('<%=request.getContextPath() + "/secure/views/user/portletthumbnail.jsp?decorator=none&imagesrc="%>' + escape(imageUrl), 'thumbnail', 'width=475, height=225');
        thumbnailWindow.moveTo((screen.availWidth/2)-237,(screen.availHeight/2)-112);
        thumbnailWindow.focus();
    }
</script>
<ww:iterator value="portlets" status="'status'">
<ww:if test="@status/odd == true">
<tr>
</ww:if>
    <td id="cell<ww:property value="@status/count"/>" valign="top" align="left" width="50%" onmouseover="cellHover(this)"
            onclick="selectCellRadioBox(this)"  style="border-width=0;"
            <ww:if test="@status/modulus(4) == 1 || @status/modulus(4) == 0">class=rowNormal originalClass=rowNormal</ww:if><ww:else>class=rowAlternate originalClass=rowAlternate</ww:else>
            >
        <ww:if test="thumbnailfile != ''">
            <a onClick="javascript:openThumbnail(event, '<ww:property value="thumbnailPath"/>'); return false;" href="#"><img align="right" vspace="5" width="75" height="75" alt="Preview of portlet" title="Click for larger version" border="0" src="<ww:property value="cornerThumbnailPath"/>"/></a>
        </ww:if>
        <label for="portletId_<ww:property value="id" />_id">
            <input type="radio" name="portletId" value="<ww:property value="id" />" id="portletId_<ww:property value="id" />_id"
            <ww:if test="/portletId == id">selected</ww:if>>
            <b><ww:text name="name" /></b> - <ww:text name="description" /><br>
            <font size=1>
            <ww:text name="'addportlet.currentlyconfigured'">
                <ww:param name="'value0'" value="../portletConfigurations(.)/size" />
            </ww:text>
        </label>
    </td>
<ww:if test="@status/last == true && @status/odd == true">
    <td bgcolor="#FFFFFF">&nbsp;</td>
</ww:if>
<ww:if test="@status/even == true">
</tr>
</ww:if>
</ww:iterator>
