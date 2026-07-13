<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="mb-3">
    <h2 class="h4">Xin chào, ${sessionScope.currentUser.fullName}!</h2>
    <p class="text-muted mb-0">
        Vai trò: <span class="badge text-bg-primary">MANAGER</span>
        · Quản lý căn hộ, phí &amp; duyệt yêu cầu
    </p>
</div>

<div class="row g-3">
    <div class="col-md-4">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-hourglass-split me-1"></i> Request chờ duyệt</div>
                <div class="stat-value text-warning">${empty pendingRequests ? 0 : pendingRequests}</div>
                <a class="btn btn-sm btn-outline-primary mt-2"
                   href="${pageContext.request.contextPath}/request?action=manage">Duyệt yêu cầu</a>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-gear me-1"></i> Request đang xử lý</div>
                <div class="stat-value text-primary">${empty processingRequests ? 0 : processingRequests}</div>
                <p class="small text-muted mb-0 mt-1">ASSIGNED + IN_PROGRESS</p>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-file-earmark me-1"></i> Phí chưa công bố</div>
                <div class="stat-value text-secondary">${empty draftFees ? 0 : draftFees}</div>
                <a class="btn btn-sm btn-outline-primary mt-2"
                   href="${pageContext.request.contextPath}/fee?action=list">Quản lý phí</a>
            </div>
        </div>
    </div>
</div>
