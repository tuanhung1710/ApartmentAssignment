<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
    <div>
        <h2 class="h4 mb-1">Sửa người dùng</h2>
        <p class="text-muted small mb-0">
            UC-ADM-02 · ID #<c:out value="${editUser.userId}"/>
            · Username: <strong><c:out value="${editUser.username}"/></strong>
        </p>
    </div>
    <a class="btn btn-sm btn-outline-secondary"
       href="${pageContext.request.contextPath}/admin?action=users">
        <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
    </a>
</div>

<c:if test="${not empty error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle me-1"></i> <c:out value="${error}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
</c:if>

<div class="card border-0 shadow-sm">
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/admin" class="row g-3">
            <input type="hidden" name="action" value="edit"/>
            <input type="hidden" name="id" value="${editUser.userId}"/>

            <div class="col-md-6">
                <label class="form-label">User ID</label>
                <input type="text" class="form-control" value="#${editUser.userId}" disabled readonly/>
            </div>

            <div class="col-md-6">
                <label class="form-label">Username</label>
                <input type="text"
                       class="form-control"
                       value="<c:out value='${editUser.username}'/>"
                       disabled
                       readonly/>
                <div class="form-text">Không được sửa Username</div>
            </div>

            <div class="col-md-6">
                <label for="fullName" class="form-label">
                    Full Name <span class="text-danger">*</span>
                </label>
                <input type="text"
                       class="form-control"
                       id="fullName"
                       name="fullName"
                       required
                       maxlength="100"
                       value="<c:out value='${editUser.fullName}'/>"/>
            </div>

            <div class="col-md-6">
                <label class="form-label">Email</label>
                <input type="email"
                       class="form-control"
                       value="<c:out value='${editUser.email}'/>"
                       disabled
                       readonly/>
                <div class="form-text">Chỉ xem · user tự cập nhật qua Hồ sơ cá nhân</div>
            </div>

            <div class="col-md-6">
                <label class="form-label">Phone Number</label>
                <input type="text"
                       class="form-control"
                       value="<c:out value='${editUser.phone}'/>"
                       disabled
                       readonly/>
                <div class="form-text">Chỉ xem · user tự cập nhật qua Hồ sơ cá nhân</div>
            </div>

            <div class="col-md-6">
                <label for="department" class="form-label">Department (tuỳ chọn)</label>
                <input type="text"
                       class="form-control"
                       id="department"
                       name="department"
                       maxlength="50"
                       value="<c:out value='${editUser.department}'/>"/>
            </div>

            <div class="col-md-6">
                <label for="role" class="form-label">
                    Role <span class="text-danger">*</span>
                </label>
                <select class="form-select" id="role" name="role" required>
                    <option value="ADMIN" ${editUser.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                    <option value="MANAGER" ${editUser.role == 'MANAGER' ? 'selected' : ''}>MANAGER</option>
                    <option value="STAFF" ${editUser.role == 'STAFF' ? 'selected' : ''}>STAFF</option>
                    <option value="RESIDENT" ${editUser.role == 'RESIDENT' ? 'selected' : ''}>RESIDENT</option>
                </select>
            </div>

            <div class="col-md-6">
                <label for="status" class="form-label">
                    Status <span class="text-danger">*</span>
                </label>
                <c:choose>
                    <c:when test="${editUser.role == 'ADMIN' || editUser.userId == sessionScope.currentUser.userId}">
                        <input type="hidden" name="status" value="active"/>
                        <select class="form-select" id="status" disabled>
                            <option value="active" selected>Active</option>
                        </select>
                        <div class="form-text text-warning">
                            Không thể khóa tài khoản ADMIN / tài khoản đang đăng nhập.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <select class="form-select" id="status" name="status" required>
                            <option value="active" ${editUser.isActive ? 'selected' : ''}>Active</option>
                            <option value="locked" ${!editUser.isActive ? 'selected' : ''}>Locked</option>
                        </select>
                        <div class="form-text">Map cột is_active</div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="col-12 d-flex gap-2">
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-save me-1"></i> Lưu thay đổi
                </button>
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/admin?action=users">
                    Hủy
                </a>
            </div>
        </form>
    </div>
</div>
