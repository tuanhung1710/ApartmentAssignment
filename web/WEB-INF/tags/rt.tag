<%--
  Hiển thị thời gian realtime (Asia/Ho_Chi_Minh).
  mode=history → HH:mm:ss (hôm nay) / dd/MM HH:mm:ss (khác ngày)
  mode=full    → dd/MM/yyyy HH:mm (luôn đủ ngày tháng)
  mặc định     → HH:mm / dd/MM HH:mm
--%>
<%@ tag description="Realtime datetime display (VN)" pageEncoding="UTF-8" %>
<%@ attribute name="value" required="true" type="java.util.Date" rtexprvalue="true" %>
<%@ attribute name="cssClass" required="false" rtexprvalue="true" %>
<%@ attribute name="mode" required="false" rtexprvalue="true" %>
<%
    java.util.Date v = value;
    String modeVal = (mode == null) ? "" : mode.trim();
    String text;
    String title;
    if ("history".equalsIgnoreCase(modeVal)) {
        text = apartmentmanagement.util.DateTimeUtil.formatHistory(v);
        title = apartmentmanagement.util.DateTimeUtil.formatHistoryTitle(v);
    } else if ("full".equalsIgnoreCase(modeVal)) {
        text = apartmentmanagement.util.DateTimeUtil.formatFull(v);
        title = apartmentmanagement.util.DateTimeUtil.formatTitle(v);
    } else {
        text = apartmentmanagement.util.DateTimeUtil.formatRealtime(v);
        title = apartmentmanagement.util.DateTimeUtil.formatTitle(v);
    }
    String cls = (cssClass == null || cssClass.isEmpty()) ? "text-muted" : cssClass;
%>
<span class="rt-time <%= cls %>" title="<%= title %>"><%= text %></span>
