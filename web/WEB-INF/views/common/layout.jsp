<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">
    <title>${empty pageTitle ? 'Apartment Management' : pageTitle} | Skyland Apartment</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/app.css" rel="stylesheet">
</head>
<body>
<div class="app-wrapper">
    <%-- Desktop sidebar (≥ lg) --%>
    <%@ include file="/WEB-INF/views/common/sidebar.jsp" %>

    <%-- Mobile / tablet menu: Bootstrap offcanvas --%>
    <div class="offcanvas offcanvas-start sidebar-offcanvas d-lg-none"
         tabindex="-1"
         id="sidebarOffcanvas"
         aria-labelledby="sidebarOffcanvasLabel">
        <div class="offcanvas-header border-bottom border-secondary border-opacity-25">
            <h2 class="offcanvas-title h6 text-white mb-0" id="sidebarOffcanvasLabel">Menu</h2>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="offcanvas"
                    aria-label="Đóng"></button>
        </div>
        <div class="offcanvas-body p-0">
            <%@ include file="/WEB-INF/views/common/sidebar-nav.jsp" %>
        </div>
    </div>

    <div class="main-content">
        <%-- Top bar + dropdown user --%>
        <%@ include file="/WEB-INF/views/common/header.jsp" %>

        <div class="content-body">
            <%-- Flash success/error (đã move từ session sang request ở servlet) --%>
            <%@ include file="/WEB-INF/views/common/flash.jsp" %>

            <c:if test="${not empty contentPage}">
                <jsp:include page="${contentPage}" />
            </c:if>
        </div>

        <%@ include file="/WEB-INF/views/common/footer.jsp" %>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    
    window.addEventListener('pageshow', function (event) {
        var nav = window.performance && window.performance.getEntriesByType
            ? window.performance.getEntriesByType('navigation')[0]
            : null;
        var fromBfCache = event.persisted
            || (nav && nav.type === 'back_forward');
        if (fromBfCache) {
            window.location.reload();
        }
    });
</script>
</body>
</html>
