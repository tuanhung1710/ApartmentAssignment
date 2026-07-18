<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
    <div>
        <h2 class="h4 mb-1">Thêm người dùng</h2>
        <p class="text-muted small mb-0">UC-ADM-01 · Chỉ Admin được thêm User</p>
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
        <form method="post" action="${pageContext.request.contextPath}/admin" class="row g-3" autocomplete="off">
            <input type="hidden" name="action" value="add"/>

            <div class="col-md-6">
                <label for="username" class="form-label">
                    Username <span class="text-danger">*</span>
                </label>
                <input type="text"
                       class="form-control"
                       id="username"
                       name="username"
                       required
                       minlength="3"
                       maxlength="50"
                       pattern="[A-Za-z0-9._\-]+"
                       value="<c:out value='${formUsername}'/>"
                       placeholder="vd: staff3"/>
                <div class="form-text">3–50 ký tự · chữ, số, . _ -</div>
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
                       value="<c:out value='${formFullName}'/>"
                       placeholder="Họ và tên"/>
            </div>

            <div class="col-md-6">
                <label for="password" class="form-label">
                    Password <span class="text-danger">*</span>
                </label>
                <input type="password"
                       class="form-control"
                       id="password"
                       name="password"
                       required
                       minlength="6"
                       maxlength="255"
                       autocomplete="new-password"/>
                <div class="form-text">Tối thiểu 6 ký tự</div>
            </div>

            <div class="col-md-6">
                <label for="confirmPassword" class="form-label">
                    Confirm Password <span class="text-danger">*</span>
                </label>
                <input type="password"
                       class="form-control"
                       id="confirmPassword"
                       name="confirmPassword"
                       required
                       minlength="6"
                       maxlength="255"
                       autocomplete="new-password"/>
            </div>

            <div class="col-md-6">
                <label for="email" class="form-label">Email</label>
                <input type="email"
                       class="form-control"
                       id="email"
                       name="email"
                       maxlength="100"
                       value="<c:out value='${formEmail}'/>"
                       placeholder="email@example.com"/>
            </div>

            <div class="col-md-6">
                <label for="phone" class="form-label">Phone Number</label>
                <input type="text"
                       class="form-control"
                       id="phone"
                       name="phone"
                       inputmode="numeric"
                       maxlength="10"
                       minlength="10"
                       pattern="0[0-9]{9}"
                       title="Đúng 10 chữ số, bắt đầu bằng 0"
                       value="<c:out value='${formPhone}'/>"
                       placeholder="0912345678"/>
                <div class="form-text">Đúng 10 số VN, bắt đầu bằng 0</div>
            </div>

            <div class="col-md-4">
                <label for="role" class="form-label">
                    Role <span class="text-danger">*</span>
                </label>
                <select class="form-select" id="role" name="role" required>
                    <option value="">-- Chọn role --</option>
                    <option value="ADMIN" ${formRole == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                    <option value="MANAGER" ${formRole == 'MANAGER' ? 'selected' : ''}>MANAGER</option>
                    <option value="STAFF" ${formRole == 'STAFF' ? 'selected' : ''}>STAFF</option>
                    <option value="RESIDENT" ${formRole == 'RESIDENT' ? 'selected' : ''}>RESIDENT</option>
                </select>
            </div>

            <div class="col-md-4">
                <label for="status" class="form-label">
                    Status <span class="text-danger">*</span>
                </label>
                <select class="form-select" id="status" name="status" required>
                    <option value="active" ${empty formStatus || formStatus == 'active' ? 'selected' : ''}>
                        Active
                    </option>
                    <option value="locked" ${formStatus == 'locked' ? 'selected' : ''}>
                        Locked
                    </option>
                </select>
                <div class="form-text">Map cột is_active (1 / 0)</div>
            </div>

            <div class="col-md-4">
                <label for="department" class="form-label">Department (tuỳ chọn)</label>
                <input type="text"
                       class="form-control"
                       id="department"
                       name="department"
                       maxlength="50"
                       value="<c:out value='${formDepartment}'/>"
                       placeholder="vd: Kỹ thuật"/>
            </div>

            <div class="col-12 d-flex gap-2">
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-person-plus me-1"></i> Thêm User
                </button>
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/admin?action=users">
                    Hủy
                </a>
            </div>
        </form>
    </div>
</div>
