<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="mb-3">
    <h2 class="h4">Xin chào, ${sessionScope.currentUser.fullName}!</h2>
    <p class="text-muted mb-0">
        Vai trò: <span class="badge text-bg-primary">${sessionScope.currentUser.role}</span>
        <c:if test="${not empty sessionScope.currentUser.department}">
            · Bộ phận: ${sessionScope.currentUser.department}
        </c:if>
    </p>
</div>

<div class="row g-3">
    <c:choose>
        <c:when test="${sessionScope.currentUser.role == 'ADMIN'}">
            <div class="col-md-4">
                <div class="card stat-card">
                    <div class="card-body">
                        <div class="text-muted small">Tổng người dùng</div>
                        <div class="stat-value text-primary">${totalUsers}</div>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card stat-card">
                    <div class="card-body">
                        <div class="text-muted small">Tài khoản bị khóa</div>
                        <div class="stat-value text-danger">${lockedUsers}</div>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card stat-card">
                    <div class="card-body">
                        <div class="text-muted small">Lối tắt</div>
                        <a class="btn btn-sm btn-outline-primary mt-2"
                           href="${pageContext.request.contextPath}/admin?action=users">Quản lý user</a>
                    </div>
                </div>
            </div>
        </c:when>

        <c:when test="${sessionScope.currentUser.role == 'MANAGER'}">
            <div class="col-md-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <h3 class="h6">Căn hộ</h3>
                        <p class="small text-muted">CRUD căn hộ, gán chủ / người thuê</p>
                        <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/apartment?action=list">Mở</a>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <h3 class="h6">Yêu cầu chờ duyệt</h3>
                        <p class="small text-muted">Module xử lý – TV5</p>
                        <span class="btn btn-sm btn-outline-secondary disabled">TV5</span>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <h3 class="h6">Phí tháng</h3>
                        <p class="small text-muted">Tạo &amp; công bố phí dịch vụ</p>
                        <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/fee?action=list">Mở</a>
                    </div>
                </div>
            </div>
        </c:when>

        <c:when test="${sessionScope.currentUser.role == 'STAFF'}">
            <div class="col-md-6">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <h3 class="h6">Việc được gán</h3>
                        <p class="small text-muted">Cập nhật tiến độ – TV5</p>
                        <span class="btn btn-sm btn-outline-secondary disabled">TV5</span>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <h3 class="h6">Phí tháng</h3>
                        <p class="small text-muted">Hỗ trợ nhập / cập nhật phí</p>
                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/fee?action=list">Mở</a>
                    </div>
                </div>
            </div>
        </c:when>

        <c:otherwise>
            <div class="col-md-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <h3 class="h6">Căn hộ của tôi</h3>
                        <p class="small text-muted">Xem thông tin &amp; thành viên</p>
                        <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/apartment?action=my">Mở</a>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <h3 class="h6">Phí tháng</h3>
                        <p class="small text-muted">Xem phí dịch vụ / nước / xe</p>
                        <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/fee?action=my">Mở</a>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <h3 class="h6">Yêu cầu đang mở</h3>
                        <div class="stat-value text-primary mb-1">${empty openRequests ? 0 : openRequests}</div>
                        <p class="small text-muted mb-2">Gửi &amp; theo dõi tiến độ</p>
                        <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/request?action=my">Mở</a>
                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/request?action=create">Gửi mới</a>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<div class="alert alert-info mt-4 mb-0">
    <i class="bi bi-info-circle me-1"></i>
</div>
