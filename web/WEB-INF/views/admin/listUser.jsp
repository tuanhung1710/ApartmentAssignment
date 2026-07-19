<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
    <div>
        <h2 class="h4 mb-1">Danh sách người dùng</h2>
        <p class="text-muted small mb-0">
            UC-ADM-04 · Tổng <strong>${totalRecords}</strong> user
        </p>
    </div>
    <%-- UC-ADM-01: Add User --%>
    <a class="btn btn-sm btn-primary"
       href="${pageContext.request.contextPath}/admin?action=add">
        <i class="bi bi-person-plus me-1"></i> Add User
    </a>
</div>

<div class="card border-0 shadow-sm mb-3">
    <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/admin" class="row g-2 align-items-end">
            <input type="hidden" name="action" value="users"/>

            <div class="col-md-4">
                <label for="keyword" class="form-label small mb-1">Tìm kiếm</label>
                <input type="text"
                       class="form-control form-control-sm"
                       id="keyword"
                       name="keyword"
                       value="<c:out value='${keyword}'/>"
                       placeholder="Username hoặc Full Name"/>
            </div>

            <div class="col-md-3">
                <label for="role" class="form-label small mb-1">Role</label>
                <select class="form-select form-select-sm" id="role" name="role">
                    <option value="">-- Tất cả --</option>
                    <option value="ADMIN" ${role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                    <option value="MANAGER" ${role == 'MANAGER' ? 'selected' : ''}>MANAGER</option>
                    <option value="STAFF" ${role == 'STAFF' ? 'selected' : ''}>STAFF</option>
                    <option value="RESIDENT" ${role == 'RESIDENT' ? 'selected' : ''}>RESIDENT</option>
                </select>
            </div>

            <div class="col-md-3">
                <label for="status" class="form-label small mb-1">Status</label>
                <select class="form-select form-select-sm" id="status" name="status">
                    <option value="">-- Tất cả --</option>
                    <option value="active" ${status == 'active' ? 'selected' : ''}>Active</option>
                    <option value="locked" ${status == 'locked' ? 'selected' : ''}>Locked</option>
                </select>
            </div>

            <div class="col-md-2 d-flex gap-2">
                <button type="submit" class="btn btn-sm btn-primary">
                    <i class="bi bi-search me-1"></i> Lọc
                </button>
                <a class="btn btn-sm btn-outline-secondary"
                   href="${pageContext.request.contextPath}/admin?action=users">
                    Reset
                </a>
            </div>
        </form>
    </div>
</div>

<div class="card border-0 shadow-sm">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th scope="col">User ID</th>
                    <th scope="col">Username</th>
                    <th scope="col">Full Name</th>
                    <th scope="col">Email</th>
                    <th scope="col">Phone</th>
                    <th scope="col">Role</th>
                    <th scope="col">Status</th>
                    <th scope="col">Created Date</th>
                    <th scope="col" class="text-center">Action</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty users}">
                        <tr>
                            <td colspan="9" class="text-center text-muted py-4">
                                <i class="bi bi-inbox me-1"></i>
                                Không có user nào phù hợp bộ lọc.
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="u" items="${users}">
                            <tr>
                                <td><span class="fw-semibold">#${u.userId}</span></td>
                                <td><c:out value="${u.username}"/></td>
                                <td><c:out value="${u.fullName}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty u.email}">
                                            <c:out value="${u.email}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty u.phone}">
                                            <c:out value="${u.phone}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${u.role == 'ADMIN'}">
                                            <span class="badge text-bg-dark">ADMIN</span>
                                        </c:when>
                                        <c:when test="${u.role == 'MANAGER'}">
                                            <span class="badge text-bg-primary">MANAGER</span>
                                        </c:when>
                                        <c:when test="${u.role == 'STAFF'}">
                                            <span class="badge text-bg-info">STAFF</span>
                                        </c:when>
                                        <c:when test="${u.role == 'RESIDENT'}">
                                            <span class="badge text-bg-secondary">RESIDENT</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-light text-dark">
                                                <c:out value="${u.role}"/>
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${u.isActive}">
                                            <span class="badge text-bg-success">Active</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-danger">Locked</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${not empty u.createdAt}">
                                        <t:rt value="${u.createdAt}" mode="full"/>
                                    </c:if>
                                </td>
                                <td class="text-center text-nowrap">
                                    <%-- Action: Detail · Edit · Lock/Unlock · Reset Password --%>
                                    <div class="btn-group btn-group-sm" role="group">
                                        <a class="btn btn-outline-secondary"
                                           href="${pageContext.request.contextPath}/admin?action=detail&amp;id=${u.userId}"
                                           title="View Detail">
                                            <i class="bi bi-eye"></i>
                                        </a>
                                        <a class="btn btn-outline-primary"
                                           href="${pageContext.request.contextPath}/admin?action=edit&amp;id=${u.userId}"
                                           title="Edit User">
                                            <i class="bi bi-pencil"></i>
                                        </a>
                                        <c:choose>
                                            <c:when test="${u.isActive}">
                                                <c:choose>
                                                    <c:when test="${u.role == 'ADMIN' || u.userId == sessionScope.currentUser.userId}">
                                                        <button type="button"
                                                                class="btn btn-outline-warning"
                                                                disabled
                                                                title="Không thể khóa tài khoản ADMIN / đang đăng nhập">
                                                            <i class="bi bi-unlock"></i>
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <a class="btn btn-outline-warning"
                                                           href="${pageContext.request.contextPath}/admin?action=lock&amp;id=${u.userId}"
                                                           title="Lock User"
                                                           onclick="return confirm('Khóa user &quot;${u.username}&quot;? User sẽ không đăng nhập được.');">
                                                            <i class="bi bi-unlock"></i>
                                                        </a>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <a class="btn btn-outline-success"
                                                   href="${pageContext.request.contextPath}/admin?action=unlock&amp;id=${u.userId}"
                                                   title="Unlock User"
                                                   onclick="return confirm('Mở khóa user &quot;${u.username}&quot;?');">
                                                    <i class="bi bi-lock"></i>
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
                                        <a class="btn btn-outline-danger"
                                           href="${pageContext.request.contextPath}/admin?action=reset-password&amp;id=${u.userId}"
                                           title="Reset Password"
                                           onclick="return confirm('Reset password user &quot;${u.username}&quot; về mật khẩu mặc định 123456?');">
                                            <i class="bi bi-key"></i>
                                        </a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <c:if test="${totalPages > 1}">
        <div class="card-footer bg-white d-flex justify-content-end">
            <c:url value="/admin" var="paginationUrl">
                <c:param name="action" value="users"/>
                <c:if test="${not empty keyword}">
                    <c:param name="keyword" value="${keyword}"/>
                </c:if>
                <c:if test="${not empty role}">
                    <c:param name="role" value="${role}"/>
                </c:if>
                <c:if test="${not empty status}">
                    <c:param name="status" value="${status}"/>
                </c:if>
            </c:url>
            <c:set var="paginationLabel" value="Phân trang người dùng"/>
            <c:set var="paginationAlign" value="justify-content-end"/>
            <%@ include file="/WEB-INF/views/common/pagination.jsp" %>
        </div>
    </c:if>
</div>
