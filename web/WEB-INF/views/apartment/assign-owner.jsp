<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">Gán chủ sở hữu</h2>
        <p class="text-muted small mb-0">
            Căn <strong>${apartment.apartmentCode}</strong>
            · ${apartment.building} · Tầng ${apartment.floorNumber}
        </p>
    </div>
    <a class="btn btn-outline-secondary btn-sm"
       href="${pageContext.request.contextPath}/apartment?action=detail&amp;id=${apartment.apartmentId}">
        <i class="bi bi-arrow-left me-1"></i> Về chi tiết
    </a>
</div>

<c:if test="${not empty errors}">
    <div class="alert alert-danger">
        <ul class="mb-0 ps-3">
            <c:forEach var="err" items="${errors}">
                <li>${err}</li>
            </c:forEach>
        </ul>
    </div>
</c:if>

<div class="row g-3">
    <div class="col-md-5">
        <div class="card shadow-sm h-100">
            <div class="card-header bg-white fw-semibold">Chủ sở hữu hiện tại</div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty currentOwner}">
                        <div class="text-muted small text-center py-3">
                            <i class="bi bi-person-x fs-4 d-block mb-1"></i>
                            Chưa có owner · gán lần đầu
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="fw-semibold"><c:out value="${currentOwner.fullName}"/></div>
                        <div class="small text-muted">@<c:out value="${currentOwner.username}"/></div>
                        <div class="small mt-2">
                            Từ:
                            <c:choose>
                                <c:when test="${empty currentOwner.startDate}">—</c:when>
                                <c:otherwise>${currentOwner.startDate}</c:otherwise>
                            </c:choose>
                        </div>
                        <div class="alert alert-warning small mt-3 mb-0">
                            Khi chọn owner mới, owner hiện tại sẽ được <strong>đóng</strong>
                            (giữ lịch sử, <code>is_current = 0</code>).
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <div class="col-md-7">
        <div class="card shadow-sm">
            <div class="card-header bg-white fw-semibold">
                <c:choose>
                    <c:when test="${empty currentOwner}">Chọn chủ sở hữu mới</c:when>
                    <c:otherwise>Đổi chủ sở hữu</c:otherwise>
                </c:choose>
            </div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/apartment">
                    <input type="hidden" name="action" value="assign-owner">
                    <input type="hidden" name="apartmentId" value="${apartment.apartmentId}">

                    <div class="mb-3">
                        <label class="form-label" for="userId">
                            Chủ sở hữu <span class="text-danger">*</span>
                        </label>
                        <select class="form-select" id="userId" name="userId" required>
                            <option value="">-- Chọn user --</option>
                            <c:forEach var="u" items="${candidateUsers}">
                                <option value="${u.userId}"
                                    ${selectedUserId != null && selectedUserId == u.userId ? 'selected' : ''}>
                                    <c:out value="${u.fullName}"/> (@<c:out value="${u.username}"/>)
                                    · ${u.role}
                                </option>
                            </c:forEach>
                        </select>
                        <div class="form-text">Ưu tiên RESIDENT active · 1 căn tối đa 1 owner hiện tại</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="startDate">Ngày bắt đầu</label>
                        <input type="date" class="form-control" id="startDate" name="startDate"
                               value="${startDate}">
                        <div class="form-text">Để trống = hôm nay</div>
                    </div>

                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-check-lg me-1"></i>
                            <c:choose>
                                <c:when test="${empty currentOwner}">Gán chủ sở hữu</c:when>
                                <c:otherwise>Đổi chủ sở hữu</c:otherwise>
                            </c:choose>
                        </button>
                        <a class="btn btn-outline-secondary"
                           href="${pageContext.request.contextPath}/apartment?action=detail&amp;id=${apartment.apartmentId}">
                            Hủy
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
