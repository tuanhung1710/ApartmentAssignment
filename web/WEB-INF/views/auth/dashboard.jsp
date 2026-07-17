<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="mb-3">
    <h2 class="h4 mb-1">Xin chào, ${sessionScope.currentUser.fullName}!</h2>
    <p class="text-muted mb-0">
        Vai trò: <span class="badge text-bg-primary">${sessionScope.currentUser.role}</span>
        <c:if test="${not empty sessionScope.currentUser.department}">
            · Bộ phận: ${sessionScope.currentUser.department}
        </c:if>
    </p>
</div>

<div class="row g-3">
    <c:choose>
        <%-- ========== ADMIN ========== --%>
        <c:when test="${sessionScope.currentUser.role == 'ADMIN'}">
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-people me-1"></i> Tổng người dùng</div>
                        <div class="stat-value text-primary">${empty totalUsers ? 0 : totalUsers}</div>
                        <c:if test="${empty totalUsers || totalUsers == 0}">
                            <p class="small text-muted mb-0 mt-1">Chưa có dữ liệu / chưa seed</p>
                        </c:if>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-lock me-1"></i> Tài khoản bị khóa</div>
                        <div class="stat-value text-danger">${empty lockedUsers ? 0 : lockedUsers}</div>
                        <c:if test="${empty lockedUsers || lockedUsers == 0}">
                            <p class="small text-muted mb-0 mt-1">Không có tài khoản bị khóa</p>
                        </c:if>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-building me-1"></i> Số tòa nhà</div>
                        <div class="stat-value text-info">${empty totalBuildings ? 0 : totalBuildings}</div>
                        <c:if test="${empty totalBuildings || totalBuildings == 0}">
                            <p class="small text-muted mb-0 mt-1">Chưa có tòa / chưa seed</p>
                        </c:if>
                        <a class="btn btn-sm btn-outline-primary mt-2"
                           href="${pageContext.request.contextPath}/building?action=list">
                            Quản lý tòa
                        </a>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-door-open me-1"></i> Số căn hộ</div>
                        <div class="stat-value text-success">${empty totalApartments ? 0 : totalApartments}</div>
                        <c:if test="${empty totalApartments || totalApartments == 0}">
                            <p class="small text-muted mb-0 mt-1">Chưa có căn hộ / chưa seed</p>
                        </c:if>
                        <a class="btn btn-sm btn-outline-primary mt-2"
                           href="${pageContext.request.contextPath}/admin?action=users">
                            Quản lý user
                        </a>
                    </div>
                </div>
            </div>
        </c:when>

        <%-- ========== MANAGER ========== --%>
        <c:when test="${sessionScope.currentUser.role == 'MANAGER'}">
            <div class="col-12 col-sm-6 col-lg-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-hourglass-split me-1"></i> Request chờ duyệt</div>
                        <div class="stat-value text-warning">${empty pendingRequests ? 0 : pendingRequests}</div>
                        <c:if test="${empty pendingRequests || pendingRequests == 0}">
                            <p class="small text-muted mb-0 mt-1">Không có request PENDING</p>
                        </c:if>
                        <a class="btn btn-sm btn-primary mt-2"
                           href="${pageContext.request.contextPath}/request?action=manage">
                            Duyệt yêu cầu
                        </a>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-6 col-lg-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-gear me-1"></i> Request đang xử lý</div>
                        <div class="stat-value text-primary">${empty processingRequests ? 0 : processingRequests}</div>
                        <p class="small text-muted mb-0 mt-1">ASSIGNED + IN_PROGRESS</p>
                        <c:if test="${empty processingRequests || processingRequests == 0}">
                            <p class="small text-muted mb-0">Không có request đang xử lý</p>
                        </c:if>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-6 col-lg-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-file-earmark me-1"></i> Phí chưa công bố</div>
                        <div class="stat-value text-secondary">${empty draftFees ? 0 : draftFees}</div>
                        <c:if test="${empty draftFees || draftFees == 0}">
                            <p class="small text-muted mb-0 mt-1">Không có phí DRAFT</p>
                        </c:if>
                        <a class="btn btn-sm btn-outline-primary mt-2"
                           href="${pageContext.request.contextPath}/fee?action=list">
                            Quản lý phí
                        </a>
                    </div>
                </div>
            </div>
        </c:when>

        <%-- ========== STAFF ========== --%>
        <c:when test="${sessionScope.currentUser.role == 'STAFF'}">
            <div class="col-12 col-sm-6 col-lg-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-clipboard-check me-1"></i> Việc được gán</div>
                        <div class="stat-value text-primary">${empty assignedJobs ? 0 : assignedJobs}</div>
                        <c:if test="${empty assignedJobs || assignedJobs == 0}">
                            <p class="small text-muted mb-0 mt-1">Chưa có việc ASSIGNED</p>
                        </c:if>
                        <a class="btn btn-sm btn-primary mt-2"
                           href="${pageContext.request.contextPath}/request?action=manage">
                            Mở
                        </a>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-6 col-lg-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-arrow-repeat me-1"></i> Đang xử lý</div>
                        <div class="stat-value text-warning">${empty inProgressJobs ? 0 : inProgressJobs}</div>
                        <c:if test="${empty inProgressJobs || inProgressJobs == 0}">
                            <p class="small text-muted mb-0 mt-1">Không có việc IN_PROGRESS</p>
                        </c:if>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-6 col-lg-4">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-check2-circle me-1"></i> Hoàn thành tuần này</div>
                        <div class="stat-value text-success">${empty completedWeek ? 0 : completedWeek}</div>
                        <p class="small text-muted mb-0 mt-1">7 ngày gần nhất</p>
                        <c:if test="${empty completedWeek || completedWeek == 0}">
                            <p class="small text-muted mb-0">Chưa hoàn thành việc nào tuần này</p>
                        </c:if>
                    </div>
                </div>
            </div>
        </c:when>

        <%-- ========== RESIDENT (default) ========== --%>
        <c:otherwise>
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-house-heart me-1"></i> Căn hộ của tôi</div>
                        <div class="h5 mt-2 mb-0 text-primary text-break">
                            <c:choose>
                                <c:when test="${empty myApartment or myApartment == 'Chưa gán căn hộ'}">
                                    <span class="text-muted">Chưa gán căn hộ</span>
                                </c:when>
                                <c:otherwise>${myApartment}</c:otherwise>
                            </c:choose>
                        </div>
                        <a class="btn btn-sm btn-primary mt-2"
                           href="${pageContext.request.contextPath}/apartment?action=my">
                            Chi tiết
                        </a>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-receipt me-1"></i> Phí tháng gần nhất</div>
                        <div class="small mt-2 mb-0 fw-semibold text-break">
                            <c:choose>
                                <c:when test="${empty latestFee or latestFee == 'Chưa có phí'}">
                                    <span class="text-muted">Chưa có phí</span>
                                </c:when>
                                <c:otherwise>${latestFee}</c:otherwise>
                            </c:choose>
                        </div>
                        <a class="btn btn-sm btn-outline-primary mt-2"
                           href="${pageContext.request.contextPath}/fee?action=my">
                            Xem phí
                        </a>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-send me-1"></i> Request đang mở</div>
                        <div class="stat-value text-warning">${empty openRequests ? 0 : openRequests}</div>
                        <c:if test="${empty openRequests || openRequests == 0}">
                            <p class="small text-muted mb-0 mt-1">Không có request đang mở</p>
                        </c:if>
                        <a class="btn btn-sm btn-outline-primary mt-2"
                           href="${pageContext.request.contextPath}/request?action=my">
                            Yêu cầu của tôi
                        </a>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-6 col-xl-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="text-muted small"><i class="bi bi-megaphone me-1"></i> Thông báo mới</div>
                        <div class="stat-value text-success">${empty newAnnouncements ? 0 : newAnnouncements}</div>
                        <p class="small text-muted mb-0 mt-1">30 ngày gần đây</p>
                        <c:if test="${empty newAnnouncements || newAnnouncements == 0}">
                            <p class="small text-muted mb-0">Chưa có thông báo mới</p>
                        </c:if>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>
