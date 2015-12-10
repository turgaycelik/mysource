<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<ww:bean id="math" name="'com.atlassian.core.bean.MathBean'"/>
<html>
<head>
	<title><ww:text name="'admin.systeminfo.memory.info'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/troubleshooting_and_support"/>
    <meta name="admin.active.tab" content="system_info"/>
</head>
<body>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">jvm-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.java.vm.memory.statistics'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover">
                <tbody>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.memory.graph'"/></strong></td>
                        <td class="cell-type-value">
                            <table border="0" cellpadding="0" cellspacing="0" style="float:left;margin-right:8px;" width="40%">
                                <td bgcolor="#CC3333" width="<ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)"/>%">
                                    <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                                        <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                             alt=""
                                             height="15"
                                             width="100%"
                                             border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                                         <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)"/></ww:param>
                                                     </ww:text>">
                                    </a>
                                </td>
                                <td bgcolor="#00CC00" width="<ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeAllocatedMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)"/>%">
                                    <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                                        <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                             alt=""
                                             height="15"
                                             width="100%"
                                             border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                                             <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeAllocatedMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)"/></ww:param>
                                                         </ww:text>">
                                    </a>
                                </td>
                                <td bgcolor="#55FF55" width="<ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/unAllocatedMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)"/>%">
                                    <a title="<ww:text name="'admin.systeminfo.uncommitted.memory'"/>" >
                                        <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                             alt=""
                                             height="15"
                                             width="100%"
                                             border="0" title="<ww:text name="'admin.systeminfo.uncommitted.memory.percent'">
                                                             <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/unAllocatedMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)"/></ww:param>
                                                         </ww:text>">
                                    </a>
                                </td>
                            </table>
                            <strong>
                                <ww:text name="'admin.systeminfo.memory.percent.free'">
                                    <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)"/></ww:param>
                                </ww:text>
                            </strong>
                            (<ww:text name="'common.words.used'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/usedMemory"/> MB
                            <ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalMemory"/> MB)
                            <span>(<a href="<ww:url value="'ViewMemoryInfo!garbageCollection.jspa'" atltoken="false" />"><ww:text name="'admin.systeminfo.force.garbage.collection'"><ww:param name="value0">garbage collection</ww:param></ww:text></a>)</span>
                        </td>
                    </tr>
                    <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/usedPermGenMemory != 0">
                        <tr>
                            <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.perm.gen.memory.graph'"/></strong></td>
                            <td class="cell-type-value">
                                <table border="0" cellpadding="0" cellspacing="0" style="float:left;margin-right:8px;" width="40%">
                                    <td bgcolor="#CC3333" width="<ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedPermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)"/>%">
                                        <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                                            <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                 alt=""
                                                 height="15"
                                                 width="100%"
                                                 border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                                             <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedPermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)"/></ww:param>
                                                         </ww:text>">
                                        </a>
                                    </td>
                                    <td bgcolor="#00CC00" width="<ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freePermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)"/>%">
                                        <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                                            <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                 alt=""
                                                 height="15"
                                                 width="100%"
                                                 border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                                                 <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freePermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)"/></ww:param>
                                                             </ww:text>">
                                        </a>
                                    </td>
                                </table>
                                <strong>
                                    <ww:text name="'admin.systeminfo.memory.percent.free'">
                                        <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freePermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)"/></ww:param>
                                    </ww:text>
                                </strong>
                                (<ww:text name="'common.words.used'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/usedPermGenMemory"/> MB
                                <ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory"/> MB)
                            </td>
                        </tr>
                    </ww:if>
                    <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory != 0">
                        <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory != 0">
                            <tr>
                                <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.nonheap.memory.graph'"/></strong></td>
                                <td class="cell-type-value">
                                    <table border="0" cellpadding="0" cellspacing="0" style="float:left;margin-right:8px;" width="40%">
                                        <td bgcolor="#CC3333" width="<ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)"/>%">
                                            <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                                                <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                     alt=""
                                                     height="15"
                                                     width="100%"
                                                     border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                                                 <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)"/></ww:param>
                                                             </ww:text>">
                                            </a>
                                        </td>
                                        <td bgcolor="#00CC00" width="<ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)"/>%">
                                            <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                                                <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                     alt=""
                                                     height="15"
                                                     width="100%"
                                                     border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                                                     <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)"/></ww:param>
                                                                 </ww:text>">
                                            </a>
                                        </td>
                                    </table>
                                    <strong>
                                        <ww:text name="'admin.systeminfo.memory.percent.free'">
                                            <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)"/></ww:param>
                                        </ww:text>
                                    </strong>
                                    (<ww:text name="'common.words.used'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory"/> MB
                                    <ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory"/> MB)
                                </td>
                            </tr>
                        </ww:if>

                        <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory == 0">
                            <tr>
                                <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.nonheap.memory.value'"/></strong></td>
                                <td class="cell-type-value"><strong><ww:text name="'common.words.used'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory"/> MB</strong></td>
                            </tr>
                        </ww:if>
                    </ww:if>
                </tbody>
            </table>
        </aui:param>
    </aui:component>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">memory-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.memory.pool.list'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.systeminfo.memory.pool.description'"/></p>
                </aui:param>
            </aui:component>
            <table class="aui aui-table-rowhover">
                <tbody>
                    <ww:iterator value="/runtimeInformation/memoryPoolInformation" status="'status'">
                        <tr>
                            <td class="cell-type-key"><strong><ww:property value="name"/></strong></td>
                            <td class="cell-type-value">
                                <table border="0" cellpadding="0" cellspacing="0" style="float:left;margin-right:8px;" width="40%">
                                    <td bgcolor="#CC3333" width="<ww:property value="@math/percentage(used, total)"/>%">
                                        <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                                            <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                 alt=""
                                                 height="15"
                                                 width="100%"
                                                 border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                                             <ww:param name="'value0'"><ww:property value="@math/percentage(used, total)"/></ww:param>
                                                         </ww:text>">
                                        </a>
                                    </td>
                                    <td bgcolor="#00CC00" width="<ww:property value="@math/percentage(free, total)"/>%">
                                        <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                                            <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                 alt=""
                                                 height="15"
                                                 width="100%"
                                                 border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                                                 <ww:param name="'value0'"><ww:property value="@math/percentage(free, total)"/></ww:param>
                                                             </ww:text>">
                                        </a>
                                    </td>
                                </table>
                                <strong>
                                    <ww:text name="'admin.systeminfo.memory.percent.free'">
                                        <ww:param name="'value0'"><ww:property value="@math/percentage(free, total)"/></ww:param>
                                    </ww:text>
                                </strong>
                                (<ww:text name="'common.words.used'"/>: <ww:property value="used"/> MB
                                <ww:text name="'common.words.total'"/>: <ww:property value="total"/> MB)
                            </td>
                        </tr>
                    </ww:iterator>
                </tbody>
            </table>
        </aui:param>
    </aui:component>
</body>
</html>
