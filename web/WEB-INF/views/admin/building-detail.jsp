<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-start gap-2 mb-3">
    <div>
        <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
            <h2 class="h4 mb-0">
                <i class="bi bi-building me-1 text-primary"></i>
                ${building.buildingName}
            </h2>
            <span class="badge rounded-pill text-bg-primary">${building.buildingCode}</span>
            <c:choose>
                <c:when test="${building.status == 'ACTIVE'}">
                    <span class="badge text-bg-success">ACTIVE</span>
                </c:when>
                <c:otherwise>
                    <span class="badge text-bg-dark">INACTIVE</span>
                </c:otherwise>
            </c:choose>
        </div>
        <p class="text-muted small mb-0">ID #${building.buildingId}</p>
    </div>
    <div class="d-flex flex-wrap gap-2">
        <a class="btn btn-outline-secondary"
           href="${pageContext.request.contextPath}/building?action=list">
            <i class="bi bi-arrow-left me-1"></i> Danh sách
        </a>
        <c:if test="${sessionScope.currentUser.role == 'ADMIN' || sessionScope.currentUser.role == 'MANAGER'}">
            <a class="btn btn-primary"
               href="${pageContext.request.contextPath}/building?action=edit&id=${building.buildingId}">
                <i class="bi bi-pencil me-1"></i> Sửa
            </a>
            <c:choose>
                <c:when test="${building.status == 'ACTIVE'}">
                    <button type="button" class="btn btn-outline-warning"
                            onclick="confirmDeactivate(${building.buildingId}, '${building.buildingCode}')">
                        <i class="bi bi-pause-fill me-1"></i> Ngưng
                    </button>
                </c:when>
                <c:otherwise>
                    <a class="btn btn-outline-success"
                       href="${pageContext.request.contextPath}/building?action=activate&id=${building.buildingId}">
                        <i class="bi bi-play-fill me-1"></i> Kích hoạt
                    </a>
                </c:otherwise>
            </c:choose>
            <button type="button" class="btn btn-outline-danger"
                    onclick="confirmDelete(${building.buildingId}, '${building.buildingCode}', ${empty building.apartmentCount ? 0 : building.apartmentCount})">
                <i class="bi bi-trash me-1"></i> Xóa
            </button>
        </c:if>
    </div>
</div>

<div class="row g-3 mb-3">
    <div class="col-12 col-lg-8">
        <div class="card shadow-sm h-100">
            <div class="card-header bg-white fw-semibold">Thông tin chi tiết</div>
            <div class="card-body">
                <div class="row g-3">
                    <div class="col-12 col-sm-3 text-muted">Mã tòa</div>
                    <div class="col-12 col-sm-9 fw-semibold">${building.buildingCode}</div>

                    <div class="col-12 col-sm-3 text-muted">Tên tòa</div>
                    <div class="col-12 col-sm-9">${building.buildingName}</div>

                    <div class="col-12 col-sm-3 text-muted">Địa chỉ</div>
                    <div class="col-12 col-sm-9">
                        <c:choose>
                            <c:when test="${empty building.address}"><span class="text-muted">—</span></c:when>
                            <c:otherwise>${building.address}</c:otherwise>
                        </c:choose>
                    </div>

                    <div class="col-12 col-sm-3 text-muted">Số tầng</div>
                    <div class="col-12 col-sm-9">
                        <c:choose>
                            <c:when test="${empty building.totalFloors}"><span class="text-muted">—</span></c:when>
                            <c:otherwise>${building.totalFloors} tầng</c:otherwise>
                        </c:choose>
                    </div>

                    <div class="col-12 col-sm-3 text-muted">Mô tả</div>
                    <div class="col-12 col-sm-9 text-break">
                        <c:choose>
                            <c:when test="${empty building.description}"><span class="text-muted">—</span></c:when>
                            <c:otherwise>${building.description}</c:otherwise>
                        </c:choose>
                    </div>

                    <div class="col-12 col-sm-3 text-muted">Tạo lúc</div>
                    <div class="col-12 col-sm-9 small">
                        <c:choose>
                            <c:when test="${empty building.createdAt}">—</c:when>
                            <c:otherwise>
                                <fmt:formatDate value="${building.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="col-12 col-sm-3 text-muted">Cập nhật</div>
                    <div class="col-12 col-sm-9 small">
                        <c:choose>
                            <c:when test="${empty building.updatedAt}"><span class="text-muted">Chưa cập nhật</span></c:when>
                            <c:otherwise>
                                <fmt:formatDate value="${building.updatedAt}" pattern="dd/MM/yyyy HH:mm"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-12 col-lg-4">
        <div class="row g-3">
            <div class="col-12 col-sm-4 col-lg-12">
                <div class="card shadow-sm border-0 h-100">
                    <div class="card-body py-3">
                        <div class="text-muted small"><i class="bi bi-door-open me-1"></i> Tổng căn</div>
                        <div class="fs-3 fw-semibold text-primary">${empty aptAllCount ? 0 : aptAllCount}</div>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-4 col-lg-12">
                <div class="card shadow-sm border-0 h-100">
                    <div class="card-body py-3">
                        <div class="text-muted small"><i class="bi bi-check-circle me-1"></i> ACTIVE</div>
                        <div class="fs-3 fw-semibold text-success">${empty aptActiveCount ? 0 : aptActiveCount}</div>
                    </div>
                </div>
            </div>
            <div class="col-12 col-sm-4 col-lg-12">
                <div class="card shadow-sm border-0 h-100">
                    <div class="card-body py-3">
                        <div class="text-muted small"><i class="bi bi-pause-circle me-1"></i> INACTIVE</div>
                        <div class="fs-3 fw-semibold text-secondary">${empty aptInactiveCount ? 0 : aptInactiveCount}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%-- ========== Căn hộ thuộc tòa + filter ACTIVE / INACTIVE ========== --%>
<div class="card shadow-sm">
    <div class="card-header bg-white">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
            <div>
                <span class="fw-semibold"><i class="bi bi-door-open me-1"></i> Căn hộ thuộc tòa</span>
                <span class="badge text-bg-light text-dark ms-1">${aptTotalItems} kết quả</span>
            </div>
            <%-- Quick filter pills --%>
            <div class="btn-group flex-wrap" role="group" aria-label="Lọc trạng thái căn">
                <c:url value="/building" var="aptFilterAll">
                    <c:param name="action" value="detail"/>
                    <c:param name="id" value="${building.buildingId}"/>
                    <c:if test="${not empty filterAptQ}"><c:param name="aptQ" value="${filterAptQ}"/></c:if>
                </c:url>
                <c:url value="/building" var="aptFilterActive">
                    <c:param name="action" value="detail"/>
                    <c:param name="id" value="${building.buildingId}"/>
                    <c:param name="aptStatus" value="ACTIVE"/>
                    <c:if test="${not empty filterAptQ}"><c:param name="aptQ" value="${filterAptQ}"/></c:if>
                </c:url>
                <c:url value="/building" var="aptFilterInactive">
                    <c:param name="action" value="detail"/>
                    <c:param name="id" value="${building.buildingId}"/>
                    <c:param name="aptStatus" value="INACTIVE"/>
                    <c:if test="${not empty filterAptQ}"><c:param name="aptQ" value="${filterAptQ}"/></c:if>
                </c:url>
                <a href="${aptFilterAll}"
                   class="btn btn-sm ${empty filterAptStatus ? 'btn-primary' : 'btn-outline-primary'}">
                    Tất cả <span class="badge text-bg-light text-dark">${aptAllCount}</span>
                </a>
                <a href="${aptFilterActive}"
                   class="btn btn-sm ${filterAptStatus == 'ACTIVE' ? 'btn-success' : 'btn-outline-success'}">
                    ACTIVE <span class="badge text-bg-light text-dark">${aptActiveCount}</span>
                </a>
                <a href="${aptFilterInactive}"
                   class="btn btn-sm ${filterAptStatus == 'INACTIVE' ? 'btn-dark' : 'btn-outline-dark'}">
                    INACTIVE <span class="badge text-bg-light text-dark">${aptInactiveCount}</span>
                </a>
            </div>
        </div>
    </div>

    <div class="card-body border-bottom">
        <form method="get" action="${pageContext.request.contextPath}/building"
              class="row g-2 align-items-end">
            <input type="hidden" name="action" value="detail"/>
            <input type="hidden" name="id" value="${building.buildingId}"/>
            <div class="col-12 col-md-5">
                <label class="form-label small mb-1" for="aptQ">Tìm mã căn / ghi chú</label>
                <input type="text" class="form-control" id="aptQ" name="aptQ"
                       value="${filterAptQ}" placeholder="VD: A-0801, bảo trì…">
            </div>
            <div class="col-12 col-md-3">
                <label class="form-label small mb-1" for="aptStatus">Trạng thái căn</label>
                <select class="form-select" id="aptStatus" name="aptStatus">
                    <option value="" ${empty filterAptStatus ? 'selected' : ''}>Tất cả</option>
                    <option value="ACTIVE" ${filterAptStatus == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                    <option value="INACTIVE" ${filterAptStatus == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                </select>
            </div>
            <div class="col-12 col-md-4 d-flex flex-column flex-sm-row gap-2">
                <button type="submit" class="btn btn-outline-primary flex-grow-1">
                    <i class="bi bi-search me-1"></i> Lọc
                </button>
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/building?action=detail&id=${building.buildingId}">
                    Xóa lọc
                </a>
            </div>
        </form>
    </div>

    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th>Mã căn</th>
                    <th class="text-center">Tầng</th>
                    <th class="d-none d-sm-table-cell">Diện tích</th>
                    <th>Loại</th>
                    <th>Trạng thái</th>
                    <th class="d-none d-md-table-cell">Ghi chú</th>
                    <th class="text-end"></th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty apartments}">
                        <tr>
                            <td colspan="7" class="text-center text-muted py-5">
                                <i class="bi bi-inbox fs-3 d-block mb-2"></i>
                                <c:choose>
                                    <c:when test="${not empty filterAptStatus || not empty filterAptQ}">
                                        Không có căn khớp bộ lọc
                                    </c:when>
                                    <c:otherwise>
                                        Tòa chưa có căn hộ
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="a" items="${apartments}">
                            <tr class="${a.status == 'INACTIVE' ? 'table-secondary' : ''}">
                                <td class="fw-semibold">${a.apartmentCode}</td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${empty a.floorNumber}">—</c:when>
                                        <c:otherwise>${a.floorNumber}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="d-none d-sm-table-cell">
                                    <c:choose>
                                        <c:when test="${empty a.areaM2}">—</c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${a.areaM2}" maxFractionDigits="2"/> m²
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.occupancyType == 'OWNED'}">
                                            <span class="badge text-bg-info">OWNED</span>
                                        </c:when>
                                        <c:when test="${a.occupancyType == 'RENTED'}">
                                            <span class="badge text-bg-warning">RENTED</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-secondary">${a.occupancyType}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.status == 'ACTIVE'}">
                                            <span class="badge text-bg-success">ACTIVE</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-dark">INACTIVE</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="d-none d-md-table-cell small text-muted text-truncate" style="max-width: 180px;">
                                    <c:choose>
                                        <c:when test="${empty a.notes}">—</c:when>
                                        <c:otherwise>${a.notes}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end">
                                    <a class="btn btn-sm btn-outline-primary"
                                       href="${pageContext.request.contextPath}/apartment?action=detail&id=${a.apartmentId}">
                                        <i class="bi bi-eye"></i>
                                        <span class="d-none d-xl-inline"> Chi tiết</span>
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <c:if test="${aptTotalPages > 1}">
        <div class="card-footer bg-white">
            <c:url value="/building" var="aptPaginationUrl">
                <c:param name="action" value="detail"/>
                <c:param name="id" value="${building.buildingId}"/>
                <c:if test="${not empty filterAptStatus}">
                    <c:param name="aptStatus" value="${filterAptStatus}"/>
                </c:if>
                <c:if test="${not empty filterAptQ}">
                    <c:param name="aptQ" value="${filterAptQ}"/>
                </c:if>
            </c:url>
            <nav aria-label="Phân trang căn hộ tòa">
                <ul class="pagination pagination-sm mb-0 justify-content-center flex-wrap">
                    <li class="page-item ${aptCurrentPage <= 1 ? 'disabled' : ''}">
                        <a class="page-link" href="${aptPaginationUrl}&aptPage=${aptCurrentPage - 1}">Trước</a>
                    </li>
                    <c:forEach begin="1" end="${aptTotalPages}" var="i">
                        <li class="page-item ${i == aptCurrentPage ? 'active' : ''}">
                            <a class="page-link" href="${aptPaginationUrl}&aptPage=${i}">${i}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${aptCurrentPage >= aptTotalPages ? 'disabled' : ''}">
                        <a class="page-link" href="${aptPaginationUrl}&aptPage=${aptCurrentPage + 1}">Sau</a>
                    </li>
                </ul>
            </nav>
        </div>
    </c:if>
</div>

<script>
    function confirmDeactivate(id, code) {
        if (confirm('Ngưng hoạt động tòa "' + code + '"?')) {
            window.location.href = '<%= request.getContextPath() %>/building?action=deactivate&id=' + id;
        }
    }
    function confirmDelete(id, code, aptCount) {
        if (aptCount > 0) {
            alert('Không thể xóa tòa "' + code + '": còn ' + aptCount + ' căn hộ.\nHãy dùng Ngưng hoạt động hoặc chuyển căn trước.');
            return;
        }
        if (confirm('Xóa vĩnh viễn tòa "' + code + '"?\nChỉ được phép khi không còn căn hộ.')) {
            window.location.href = '<%= request.getContextPath() %>/building?action=delete&id=' + id;
        }
    }
</script>
