<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="mb-3">
    <h2 class="h4">Xin chào, ${sessionScope.currentUser.fullName}!</h2>
    <p class="text-muted mb-0">
        Vai trò: <span class="badge text-bg-primary">STAFF</span>
        · Việc được gán &amp; tiến độ
    </p>
</div>

<div class="row g-3">
    <div class="col-md-4">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-clipboard-check me-1"></i> Việc được gán</div>
                <div class="stat-value text-primary">${empty assignedJobs ? 0 : assignedJobs}</div>
                <p class="small text-muted mb-0 mt-1">Trạng thái ASSIGNED</p>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-arrow-repeat me-1"></i> Đang xử lý</div>
                <div class="stat-value text-warning">${empty inProgressJobs ? 0 : inProgressJobs}</div>
                <p class="small text-muted mb-0 mt-1">Trạng thái IN_PROGRESS</p>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-check2-circle me-1"></i> Hoàn thành tuần này</div>
                <div class="stat-value text-success">${empty completedWeek ? 0 : completedWeek}</div>
                <p class="small text-muted mb-0 mt-1">7 ngày gần nhất</p>
            </div>
        </div>
    </div>
</div>

<div class="mt-3">
    <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/request?action=manage">
        Xem việc được gán
    </a>
</div>
