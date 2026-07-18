<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-start gap-2 mb-3">
    <div>
        <h2 class="h4 mb-1"><i class="bi bi-building me-1 text-primary"></i> Quản lý tòa nhà</h2>
        <p class="text-muted small mb-0">
            Master data cho căn hộ, cư dân và phí theo tòa · TV2–TV5 dùng <code>building_id</code>
        </p>
    </div>
    <c:if test="${sessionScope.currentUser.role == 'ADMIN' || sessionScope.currentUser.role == 'MANAGER'}">
        <a class="btn btn-primary w-100 w-sm-auto"
           href="${pageContext.request.contextPath}/building?action=create">
            <i class="bi bi-plus-lg me-1"></i> Thêm tòa
        </a>
    </c:if>
</div>

<%-- Stat cards --%>
<div class="row g-3 mb-3">
    <div class="col-12 col-sm-4">
        <div class="card border-0 shadow-sm h-100">
            <div class="card-body py-3">
                <div class="text-muted small"><i class="bi bi-buildings me-1"></i> Tổng tòa</div>
                <div class="fs-4 fw-semibold text-primary">${empty totalBuildings ? 0 : totalBuildings}</div>
            </div>
        </div>
    </div>
    <div class="col-12 col-sm-4">
        <div class="card border-0 shadow-sm h-100">
            <div class="card-body py-3">
                <div class="text-muted small"><i class="bi bi-check-circle me-1"></i> Đang hoạt động</div>
                <div class="fs-4 fw-semibold text-success">${empty activeCount ? 0 : activeCount}</div>
            </div>
        </div>
    </div>
    <div class="col-12 col-sm-4">
        <div class="card border-0 shadow-sm h-100">
            <div class="card-body py-3">
                <div class="text-muted small"><i class="bi bi-pause-circle me-1"></i> Ngưng</div>
                <div class="fs-4 fw-semibold text-secondary">${empty inactiveCount ? 0 : inactiveCount}</div>
            </div>
        </div>
    </div>
</div>

<%-- Filters --%>
<div class="card shadow-sm mb-3">
    <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/building" class="row g-2 align-items-end">
            <input type="hidden" name="action" value="list"/>
            <div class="col-12 col-md-5">
                <label class="form-label small mb-1" for="q">Tìm kiếm</label>
                <input type="text" class="form-control" id="q" name="q"
                       value="${filterQ}" placeholder="Mã, tên, địa chỉ…">
            </div>
            <div class="col-12 col-md-3">
                <label class="form-label small mb-1" for="status">Trạng thái</label>
                <select class="form-select" id="status" name="status">
                    <option value="" ${empty filterStatus ? 'selected' : ''}>Tất cả</option>
                    <option value="ACTIVE" ${filterStatus == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                    <option value="INACTIVE" ${filterStatus == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                </select>
            </div>
            <div class="col-12 col-md-4 d-flex flex-column flex-sm-row gap-2">
                <button type="submit" class="btn btn-outline-primary flex-grow-1">
                    <i class="bi bi-search me-1"></i> Lọc
                </button>
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/building?action=list">
                    Xóa lọc
                </a>
            </div>
        </form>
    </div>
</div>

<%-- Table --%>
<div class="card shadow-sm">
    <div class="card-header bg-white d-flex justify-content-between align-items-center flex-wrap gap-2">
        <span class="fw-semibold">Danh sách tòa</span>
        <span class="badge text-bg-light text-dark">${totalItems} kết quả</span>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th>Mã</th>
                    <th>Tên tòa</th>
                    <th class="d-none d-md-table-cell">Địa chỉ</th>
                    <th class="text-center">Tầng</th>
                    <th class="text-center">Căn hộ</th>
                    <th>Trạng thái</th>
                    <th class="text-end">Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty buildings}">
                        <tr>
                            <td colspan="7" class="text-center text-muted py-5">
                                <i class="bi bi-inbox fs-3 d-block mb-2"></i>
                                Chưa có tòa nhà / không khớp bộ lọc
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="b" items="${buildings}">
                            <tr>
                                <td>
                                    <span class="badge rounded-pill text-bg-primary">${b.buildingCode}</span>
                                </td>
                                <td class="fw-semibold">${b.buildingName}</td>
                                <td class="d-none d-md-table-cell text-muted small">
                                    <c:choose>
                                        <c:when test="${empty b.address}">—</c:when>
                                        <c:otherwise>${b.address}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${empty b.totalFloors}">—</c:when>
                                        <c:otherwise>${b.totalFloors}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <span class="badge text-bg-secondary">${empty b.apartmentCount ? 0 : b.apartmentCount}</span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${b.status == 'ACTIVE'}">
                                            <span class="badge text-bg-success">ACTIVE</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-dark">INACTIVE</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end text-nowrap">
                                    <a class="btn btn-sm btn-outline-primary"
                                       href="${pageContext.request.contextPath}/building?action=detail&id=${b.buildingId}"
                                       title="Chi tiết">
                                        <i class="bi bi-eye"></i>
                                        <span class="d-none d-xl-inline"> Chi tiết</span>
                                    </a>
                                    <c:if test="${sessionScope.currentUser.role == 'ADMIN' || sessionScope.currentUser.role == 'MANAGER'}">
                                        <a class="btn btn-sm btn-outline-secondary"
                                           href="${pageContext.request.contextPath}/building?action=edit&id=${b.buildingId}"
                                           title="Sửa">
                                            <i class="bi bi-pencil"></i>
                                        </a>
                                        <c:choose>
                                            <c:when test="${b.status == 'ACTIVE'}">
                                                <button type="button" class="btn btn-sm btn-outline-warning"
                                                        title="Ngưng"
                                                        onclick="confirmDeactivate(${b.buildingId}, '${b.buildingCode}')">
                                                    <i class="bi bi-pause-fill"></i>
                                                </button>
                                            </c:when>
                                            <c:otherwise>
                                                <a class="btn btn-sm btn-outline-success"
                                                   href="${pageContext.request.contextPath}/building?action=activate&id=${b.buildingId}"
                                                   title="Kích hoạt">
                                                    <i class="bi bi-play-fill"></i>
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>
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
        <div class="card-footer bg-white">
            <c:url value="/building" var="paginationUrl">
                <c:param name="action" value="list"/>
                <c:if test="${not empty filterQ}">
                    <c:param name="q" value="${filterQ}"/>
                </c:if>
                <c:if test="${not empty filterStatus}">
                    <c:param name="status" value="${filterStatus}"/>
                </c:if>
            </c:url>
            <c:set var="pageParam" value="page"/>
            <c:set var="paginationLabel" value="Phân trang tòa nhà"/>
            <c:set var="paginationAlign" value="justify-content-center"/>
            <%@ include file="/WEB-INF/views/common/pagination.jsp" %>
        </div>
    </c:if>
</div>

<script>
    function confirmDeactivate(id, code) {
        if (confirm('Ngưng hoạt động tòa "' + code + '"?\nCăn hộ thuộc tòa vẫn giữ nguyên — chỉ soft-delete tòa.')) {
            window.location.href = '<%= request.getContextPath() %>/building?action=deactivate&id=' + id;
        }
    }
</script>
