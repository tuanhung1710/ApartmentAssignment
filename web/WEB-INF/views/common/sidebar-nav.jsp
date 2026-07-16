<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<a class="brand" href="${pageContext.request.contextPath}/dashboard">
    <i class="bi bi-buildings"></i> Skyland
</a>

<nav class="nav flex-column py-2">
    <a class="nav-link" href="${pageContext.request.contextPath}/dashboard">
        <i class="bi bi-speedometer2 me-2"></i> Dashboard
    </a>

    <%-- ADMIN: quản trị hệ thống --%>
    <c:if test="${sessionScope.currentUser.role == 'ADMIN'}">
        <div class="nav-section">Quản trị</div>
        <a class="nav-link" href="${pageContext.request.contextPath}/admin?action=users">
            <i class="bi bi-people me-2"></i> Người dùng
        </a>
        <a class="nav-link" href="${pageContext.request.contextPath}/admin?action=announcements">
            <i class="bi bi-megaphone me-2"></i> Thông báo
        </a>
    </c:if>

    <%-- ADMIN / MANAGER / STAFF: tòa nhà (master) + căn hộ & phí --%>
    <c:if test="${sessionScope.currentUser.role == 'MANAGER'
               || sessionScope.currentUser.role == 'ADMIN'
               || sessionScope.currentUser.role == 'STAFF'}">
        <div class="nav-section">Cơ sở &amp; phí</div>
        <a class="nav-link" href="${pageContext.request.contextPath}/building?action=list">
            <i class="bi bi-building me-2"></i> Tòa nhà
        </a>
        <a class="nav-link" href="${pageContext.request.contextPath}/apartment?action=list">
            <i class="bi bi-door-open me-2"></i> Căn hộ
        </a>
        <a class="nav-link" href="${pageContext.request.contextPath}/apartment?action=members">
            <i class="bi bi-people me-2"></i> Thành viên
        </a>
        <a class="nav-link" href="${pageContext.request.contextPath}/fee?action=list">
            <i class="bi bi-cash-coin me-2"></i> Phí tháng
        </a>
    </c:if>

    <%-- ADMIN / MANAGER / STAFF: xử lý yêu cầu --%>
    <c:if test="${sessionScope.currentUser.role == 'MANAGER'
               || sessionScope.currentUser.role == 'STAFF'
               || sessionScope.currentUser.role == 'ADMIN'}">
        <div class="nav-section">Yêu cầu</div>
        <a class="nav-link" href="${pageContext.request.contextPath}/request?action=manage">
            <i class="bi bi-inboxes me-2"></i> Xử lý yêu cầu
        </a>
    </c:if>

    <%-- RESIDENT --%>
    <c:if test="${sessionScope.currentUser.role == 'RESIDENT'}">
        <div class="nav-section">Cư dân</div>
        <a class="nav-link" href="${pageContext.request.contextPath}/apartment?action=my">
            <i class="bi bi-house-heart me-2"></i> Căn hộ của tôi
        </a>
        <a class="nav-link" href="${pageContext.request.contextPath}/fee?action=my">
            <i class="bi bi-receipt me-2"></i> Phí của tôi
        </a>
        <a class="nav-link" href="${pageContext.request.contextPath}/request?action=my">
            <i class="bi bi-send me-2"></i> Yêu cầu của tôi
        </a>
        <a class="nav-link" href="${pageContext.request.contextPath}/request?action=create">
            <i class="bi bi-plus-circle me-2"></i> Gửi yêu cầu
        </a>
    </c:if>

    <%-- Cá nhân – mọi role --%>
    <div class="nav-section">Cá nhân</div>
    <a class="nav-link" href="${pageContext.request.contextPath}/profile">
        <i class="bi bi-person me-2"></i> Hồ sơ
    </a>
    <a class="nav-link" href="${pageContext.request.contextPath}/profile?action=change-password">
        <i class="bi bi-key me-2"></i> Đổi mật khẩu
    </a>
    <a class="nav-link text-danger" href="${pageContext.request.contextPath}/auth?action=logout">
        <i class="bi bi-box-arrow-right me-2"></i> Đăng xuất
    </a>
</nav>
