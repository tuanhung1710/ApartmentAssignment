<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
    <div>
        <h2 class="h4 mb-1">Chi tiết người dùng</h2>
        <p class="text-muted small mb-0">
            User ID #<c:out value="${detailUser.userId}"/>
            · <c:out value="${detailUser.username}"/>
        </p>
    </div>
    <div class="d-flex gap-2 flex-wrap">
        <a class="btn btn-sm btn-primary"
           href="${pageContext.request.contextPath}/admin?action=edit&amp;id=${detailUser.userId}">
            <i class="bi bi-pencil me-1"></i> Edit
        </a>
        <a class="btn btn-sm btn-outline-secondary"
           href="${pageContext.request.contextPath}/admin?action=users">
            <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
        </a>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-8">
        <div class="card border-0 shadow-sm">
            <div class="card-header bg-white">
                <span class="fw-semibold">Thông tin tài khoản</span>
            </div>
            <div class="card-body">
                <div class="row g-3">
                    <div class="col-md-6">
                        <div class="text-muted small">User ID</div>
                        <div class="fw-semibold">#${detailUser.userId}</div>
                    </div>
                    <div class="col-md-6">
                        <div class="text-muted small">Username</div>
                        <div class="fw-semibold"><c:out value="${detailUser.username}"/></div>
                    </div>
                    <div class="col-md-6">
                        <div class="text-muted small">Full Name</div>
                        <div class="fw-semibold"><c:out value="${detailUser.fullName}"/></div>
                    </div>
                    <div class="col-md-6">
                        <div class="text-muted small">Email</div>
                        <div>
                            <c:choose>
                                <c:when test="${not empty detailUser.email}">
                                    <c:out value="${detailUser.email}"/>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted">—</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="text-muted small">Phone</div>
                        <div>
                            <c:choose>
                                <c:when test="${not empty detailUser.phone}">
                                    <c:out value="${detailUser.phone}"/>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted">—</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="text-muted small">Department</div>
                        <div>
                            <c:choose>
                                <c:when test="${not empty detailUser.department}">
                                    <c:out value="${detailUser.department}"/>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted">—</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="text-muted small">Role</div>
                        <div>
                            <c:choose>
                                <c:when test="${detailUser.role == 'ADMIN'}">
                                    <span class="badge text-bg-dark">ADMIN</span>
                                </c:when>
                                <c:when test="${detailUser.role == 'MANAGER'}">
                                    <span class="badge text-bg-primary">MANAGER</span>
                                </c:when>
                                <c:when test="${detailUser.role == 'STAFF'}">
                                    <span class="badge text-bg-info">STAFF</span>
                                </c:when>
                                <c:when test="${detailUser.role == 'RESIDENT'}">
                                    <span class="badge text-bg-secondary">RESIDENT</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge text-bg-light text-dark">
                                        <c:out value="${detailUser.role}"/>
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="text-muted small">Status</div>
                        <div>
                            <c:choose>
                                <c:when test="${detailUser.isActive}">
                                    <span class="badge text-bg-success">Active</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge text-bg-danger">Locked</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <div class="card border-0 shadow-sm h-100">
            <div class="card-header bg-white">
                <span class="fw-semibold">Thời gian</span>
            </div>
            <div class="card-body">
                <dl class="row mb-0 small">
                    <dt class="col-5 text-muted">Created</dt>
                    <dd class="col-7">
                        <c:choose>
                            <c:when test="${not empty detailUser.createdAt}">
                                <t:rt value="${detailUser.createdAt}" mode="full"/>
                            </c:when>
                            <c:otherwise>—</c:otherwise>
                        </c:choose>
                    </dd>
                    <dt class="col-5 text-muted">Updated</dt>
                    <dd class="col-7">
                        <c:choose>
                            <c:when test="${not empty detailUser.updatedAt}">
                                <t:rt value="${detailUser.updatedAt}" mode="full"/>
                            </c:when>
                            <c:when test="${not empty detailUser.createdAt}">
                                <t:rt value="${detailUser.createdAt}" mode="full"/>
                            </c:when>
                            <c:otherwise>—</c:otherwise>
                        </c:choose>
                    </dd>
                </dl>
            </div>
            <div class="card-footer bg-white d-flex flex-wrap gap-2">
                <c:choose>
                    <c:when test="${detailUser.isActive}">
                        <c:choose>
                            <c:when test="${detailUser.role == 'ADMIN' || detailUser.userId == sessionScope.currentUser.userId}">
                                <button type="button" class="btn btn-sm btn-outline-warning" disabled
                                        title="Không thể khóa ADMIN / tài khoản đang đăng nhập">
                                    <i class="bi bi-unlock me-1"></i> Lock
                                </button>
                            </c:when>
                            <c:otherwise>
                                <a class="btn btn-sm btn-outline-warning"
                                   href="${pageContext.request.contextPath}/admin?action=lock&amp;id=${detailUser.userId}"
                                   onclick="return confirm('Khóa user này?');">
                                    <i class="bi bi-unlock me-1"></i> Lock
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <a class="btn btn-sm btn-outline-success"
                           href="${pageContext.request.contextPath}/admin?action=unlock&amp;id=${detailUser.userId}"
                           onclick="return confirm('Mở khóa user này?');">
                            <i class="bi bi-lock me-1"></i> Unlock
                        </a>
                    </c:otherwise>
                </c:choose>
                <a class="btn btn-sm btn-outline-danger"
                   href="${pageContext.request.contextPath}/admin?action=reset-password&amp;id=${detailUser.userId}"
                   onclick="return confirm('Reset password về 123456?');">
                    <i class="bi bi-key me-1"></i> Reset Password
                </a>
            </div>
        </div>
    </div>
</div>
