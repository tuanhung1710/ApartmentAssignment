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

<%-- ========== Căn hộ thuộc tòa: status → occupancy (ACTIVE only) ========== --%>
<div class="card shadow-sm">
    <div class="card-header bg-white">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
            <div>
                <span class="fw-semibold"><i class="bi bi-door-open me-1"></i> Căn hộ thuộc tòa</span>
                <span class="badge text-bg-light text-dark ms-1">${aptTotalItems} kết quả</span>
            </div>
            <%-- Quick filter pills theo trạng thái --%>
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
                   class="btn btn-sm ${empty filterAptStatus ? 'btn-primary' : 'btn-outline-primary'}"
                   title="Tất cả căn thuộc tòa">
                    Tất cả <span class="badge text-bg-light text-dark">${aptAllCount}</span>
                </a>
                <a href="${aptFilterActive}"
                   class="btn btn-sm ${filterAptStatus == 'ACTIVE' ? 'btn-success' : 'btn-outline-success'}"
                   title="Căn đang hoạt động — có thể lọc loại hình">
                    ACTIVE <span class="badge text-bg-light text-dark">${aptActiveCount}</span>
                </a>
                <a href="${aptFilterInactive}"
                   class="btn btn-sm ${filterAptStatus == 'INACTIVE' ? 'btn-dark' : 'btn-outline-dark'}"
                   title="Căn tạm ngưng — loại hình = N/A">
                    INACTIVE <span class="badge text-bg-light text-dark">${aptInactiveCount}</span>
                </a>
            </div>
        </div>
    </div>

    <div class="card-body border-bottom">
        <form method="get" action="${pageContext.request.contextPath}/building"
              class="row g-2 align-items-end" id="aptFilterForm">
            <input type="hidden" name="action" value="detail"/>
            <input type="hidden" name="id" value="${building.buildingId}"/>
            <div class="col-12 col-lg-4">
                <label class="form-label small mb-1" for="aptQ">Tìm mã căn / ghi chú</label>
                <input type="text" class="form-control" id="aptQ" name="aptQ"
                       value="${filterAptQ}" placeholder="VD: A-0801, bảo trì…">
            </div>
            <div class="col-12 col-sm-6 col-lg-2">
                <label class="form-label small mb-1" for="aptStatus">
                    Trạng thái
                    <i class="bi bi-info-circle text-muted"
                       title="Chọn trạng thái trước. ACTIVE mới cho phép lọc loại hình."></i>
                </label>
                <select class="form-select" id="aptStatus" name="aptStatus" onchange="onAptStatusChange()">
                    <option value="" ${empty filterAptStatus ? 'selected' : ''}>Tất cả</option>
                    <option value="ACTIVE" ${filterAptStatus == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                    <option value="INACTIVE" ${filterAptStatus == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                </select>
            </div>
            <div class="col-12 col-sm-6 col-lg-3">
                <label class="form-label small mb-1" for="aptType">
                    Loại hình
                    <i class="bi bi-info-circle text-muted"
                       title="Chỉ áp dụng khi trạng thái = ACTIVE. INACTIVE luôn là N/A."></i>
                </label>
                <select class="form-select" id="aptType" name="aptType"
                        ${filterAptStatus == 'ACTIVE' ? '' : 'disabled'}>
                    <option value="" ${empty filterAptType ? 'selected' : ''}>
                        ${filterAptStatus == 'ACTIVE' ? 'Tất cả loại' : '— chọn ACTIVE trước —'}
                    </option>
                    <option value="OWNED" ${filterAptType == 'OWNED' ? 'selected' : ''}>
                        OWNED — có chủ sở hữu
                    </option>
                    <option value="RENTED" ${filterAptType == 'RENTED' ? 'selected' : ''}>
                        RENTED — cho thuê
                    </option>
                    <option value="VACANT" ${filterAptType == 'VACANT' ? 'selected' : ''}>
                        VACANT — sẵn sàng, chưa vào ở
                    </option>
                </select>
            </div>
            <div class="col-12 col-lg-3 d-flex flex-column flex-sm-row gap-2">
                <button type="submit" class="btn btn-outline-primary flex-grow-1">
                    <i class="bi bi-search me-1"></i> Lọc
                </button>
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/building?action=detail&id=${building.buildingId}">
                    Xóa lọc
                </a>
            </div>
        </form>

        <%-- Chú thích trạng thái / loại hình --%>
        <div class="mt-3 small text-muted border-top pt-2">
            <div class="fw-semibold text-body mb-1">
                <i class="bi bi-lightbulb me-1"></i> Chú thích
            </div>
            <div class="d-flex flex-wrap gap-2 mb-2">
                <span class="badge text-bg-success">ACTIVE</span>
                <span class="align-self-center">đang vận hành</span>
                <span class="text-muted">·</span>
                <span class="badge text-bg-dark">INACTIVE</span>
                <span class="align-self-center">tạm ngưng (loại hình = N/A)</span>
            </div>
            <div class="d-flex flex-wrap gap-2 align-items-center">
                <span class="badge text-bg-info">OWNED</span>
                <span>có chủ sở hữu (chủ ở)</span>
                <span class="text-muted">·</span>
                <span class="badge text-bg-warning">RENTED</span>
                <span>cho thuê (chủ hộ + người thuê)</span>
                <span class="text-muted">·</span>
                <span class="badge text-bg-secondary">VACANT</span>
                <span>ACTIVE, sẵn sàng — chưa có ai chuyển vào</span>
                <span class="text-muted">·</span>
                <span class="badge text-bg-light text-dark border">N/A</span>
                <span>chỉ khi INACTIVE</span>
            </div>
            <c:if test="${filterAptStatus == 'ACTIVE'}">
                <div class="mt-2 d-flex flex-wrap gap-2">
                    <span class="text-body-secondary">Trong ACTIVE:</span>
                    <span class="badge rounded-pill text-bg-info">OWNED ${empty aptOwnedCount ? 0 : aptOwnedCount}</span>
                    <span class="badge rounded-pill text-bg-warning">RENTED ${empty aptRentedCount ? 0 : aptRentedCount}</span>
                    <span class="badge rounded-pill text-bg-secondary">VACANT ${empty aptVacantCount ? 0 : aptVacantCount}</span>
                </div>
            </c:if>
        </div>
    </div>

    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th>Mã căn</th>
                    <th class="text-center">Tầng</th>
                    <th class="d-none d-sm-table-cell">Diện tích</th>
                    <th>
                        Loại
                        <i class="bi bi-info-circle text-muted"
                           title="OWNED / RENTED / VACANT khi ACTIVE; N/A khi INACTIVE"></i>
                    </th>
                    <th>
                        Trạng thái
                        <i class="bi bi-info-circle text-muted"
                           title="ACTIVE = đang hoạt động · INACTIVE = tạm ngưng"></i>
                    </th>
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
                                    <c:when test="${not empty filterAptStatus || not empty filterAptType || not empty filterAptQ}">
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
                                    <%-- INACTIVE → N/A; ACTIVE → OWNED / RENTED / VACANT --%>
                                    <c:choose>
                                        <c:when test="${a.status == 'INACTIVE'}">
                                            <span class="badge text-bg-light text-dark border"
                                                  title="Căn tạm ngưng — không áp loại hình">N/A</span>
                                        </c:when>
                                        <c:when test="${a.occupancyType == 'OWNED'}">
                                            <span class="badge text-bg-info"
                                                  title="Có chủ sở hữu (chủ ở)">OWNED</span>
                                        </c:when>
                                        <c:when test="${a.occupancyType == 'RENTED'}">
                                            <span class="badge text-bg-warning"
                                                  title="Cho thuê — chủ hộ + người thuê">RENTED</span>
                                        </c:when>
                                        <c:when test="${a.occupancyType == 'VACANT'}">
                                            <span class="badge text-bg-secondary"
                                                  title="ACTIVE, sẵn sàng — chưa có ai chuyển vào">VACANT</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-light text-dark border">
                                                ${empty a.occupancyType ? 'N/A' : a.occupancyType}
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.status == 'ACTIVE'}">
                                            <span class="badge text-bg-success"
                                                  title="Đang vận hành">ACTIVE</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-dark"
                                                  title="Tạm ngưng hoạt động">INACTIVE</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="d-none d-md-table-cell small text-muted text-truncate" style="max-width: 180px;"
                                    title="${empty a.notes ? '' : a.notes}">
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
                <c:if test="${not empty filterAptType}">
                    <c:param name="aptType" value="${filterAptType}"/>
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
    /** Chỉ bật lọc loại hình khi status = ACTIVE; đổi status → reset type. */
    function onAptStatusChange() {
        var statusEl = document.getElementById('aptStatus');
        var typeEl = document.getElementById('aptType');
        if (!statusEl || !typeEl) return;
        var isActive = statusEl.value === 'ACTIVE';
        typeEl.disabled = !isActive;
        if (!isActive) {
            typeEl.value = '';
            // cập nhật nhãn option đầu
            if (typeEl.options.length > 0) {
                typeEl.options[0].text = '— chọn ACTIVE trước —';
            }
        } else if (typeEl.options.length > 0) {
            typeEl.options[0].text = 'Tất cả loại';
        }
    }
    // sync lúc load (phòng back/forward cache)
    document.addEventListener('DOMContentLoaded', onAptStatusChange);
</script>
