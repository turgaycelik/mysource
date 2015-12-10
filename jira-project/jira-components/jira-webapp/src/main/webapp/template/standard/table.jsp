<%@ taglib uri="webwork" prefix="ww" %>


<%/*This is just a sample table layout that I sometimes use.
   * it shows fine on Internet Explorer but does not always look
   * so good on Netscape ( a blank cell will show up as balck)
   * you can modify this to shopw the table any way you want
   *
   * NOTE : this should currently be included within the context of the
   * webTable bean.
   *
   */%>

<ww:if test=". != null">
	<ww:if test="model != null">
        <TABLE bgcolor="555555" border="0" cellpadding="0" cellspacing="0" width=95%>
            <TR>
                <TD>
                    <TABLE  border="0" cellpadding="3" cellspacing="1" width=100%>

                    <TR BGColor=bbbbbb>

                    <%/* Show the visible column names.  Use the display name that can
                       * be set in the jsp.
                       */%>
                    <ww:property value="columns">
                        <ww:iterator id="curColumn">
                            <ww:if test="visible == true">
                                <ww:if test="sortable == true">
                                    <td
                                    <ww:if test="sortColumn == offset">
                                        class="colHeaderOver"
                                    </ww:if>
                                    <ww:else>
                                        class="colHeaderLink" onMouseOver="this.className='colHeaderOver'" onMouseOut="this.className='colHeaderLink'"
                                    </ww:else>
                                    >

                                    <ww:if test="(sortColumn == offset) && (sortOrder == 'ASC')">
                                        <a href='<ww:url><ww:param name="sortColumnLinkName" value="offset"/><ww:param name="sortOrderLinkName" value="'DESC'"/></ww:url>'><font color="#ffffff"><ww:property value="displayName"/></font></a>
                                        <a href='<ww:url><ww:param name="sortColumnLinkName" value="offset"/><ww:param name="sortOrderLinkName" value="'DESC'"/></ww:url>'><img src='<%= request.getContextPath() %>/images/sorted_asc.gif' border='0' valign='bottom'/></a>
                                    </ww:if>
                                    <ww:elseIf test="(sortColumn == offset) && (sortOrder == 'DESC')">
                                    <a href='<ww:url><ww:param name="sortColumnLinkName" value="offset"/><ww:param name="sortOrderLinkName" value="'ASC'"/></ww:url>'>	<font color="#ffffff"><ww:property value="displayName"/></font></a>
                                        <a href='<ww:url><ww:param name="sortColumnLinkName" value="offset"/><ww:param name="sortOrderLinkName" value="'ASC'"/></ww:url>'><img src='<%= request.getContextPath() %>/images/sorted_desc.gif' border='0' valign='top'/></a>
                                    </ww:elseIf>
                                    <ww:elseIf test="sortColumn != offset">
                                        <a href='<ww:url><ww:param name="sortColumnLinkName" value="offset"/><ww:param name="sortOrderLinkName" value="'DESC'"/></ww:url>'><font color="#ffffff"><ww:property value="displayName"/></font></a>
                                        <a href='<ww:url><ww:param name="sortColumnLinkName" value="offset"/><ww:param name="sortOrderLinkName" value="'DESC'"/></ww:url>'><img src='<%= request.getContextPath() %>/images/unsorted_desc.gif' border='0' valign='top'/></a>
                                    </ww:elseIf>
                                </ww:if>
                                <ww:else>
                                    <td><ww:property value="displayName"/>
                                </ww:else>
                                </td>
                            </ww:if>
                        </ww:iterator>
                    </ww:property>
                    </TR>

                    <%/*counter used to alternate background row colors*/%>
                    <ww:bean name="'webwork.util.Counter'" id="rowCount">
                        <ww:param name="'wrap'" value="true"/>
                        <ww:param name="'last'" value="2"/>
                    </ww:bean>

                    <%/*Row iterator will iterate through the formated rows*/%>
                    <ww:property value="rowIterator">
                        <ww:iterator>

                            <%/*set the background colors.  Used in 0.95 version
                               * I haven't looked to see if there is a cleaner way
                               * but there must be
                               */%>
                            <TR
                                <ww:if test="@rowCount/next == 1">
                                    BGCOLOR="#fffff0"
                                </ww:if>
                                <ww:if test = "@rowCount/next == 0"/>
                                <ww:if test = "@rowCount/next == 2">
                                    BGCOLOR="#FFFFFF"
                                </ww:if>
                            >
                            <%/*show the actual cell contents.  It is generated by the renderer for the columns*/%>
                            <ww:iterator>
                                <td><ww:property value="."/></td>
                            </ww:iterator>
                            </TR>
                        </ww:iterator>
                    </ww:property>
                    </TABLE>
                </TD>
            </TR>
        </TABLE>
	</ww:if>
	<ww:else>
        <table border=1>
            <tr><th><font color="red">ERROR</font></th></tr>
            <tr><td>no model was available</td></tr>
        </table>
	</ww:else>
</ww:if>
<ww:else>
	this page can not be loaded directly
</ww:else>
