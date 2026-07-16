<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Base URL giữ filter khi sort / pagination --%>
<c:url value="/apartment" var="listBaseUrl">
    <c:param name="action" value="list"/>
    <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}"/></c:if>
    <c:if test="${not empty buildingFilter}"><c:param name="building" value="${buildingFilter}"/></c:if>
    <c:if test="${not empty statusFilter}"><c:param name="status" value="${statusFilter}"/></c:if>
    <c:if test="${not empty occupancyFilter}"><c:param name="occupancyType" value="${occupancyFilter}"/></c:if>
</c:url>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">Danh sách căn hộ</h2>
        <p class="text-muted small mb-0">Tìm kiếm · lọc · sắp xếp · phân trang</p>
    </div>
    <c:if test="${canManage}">
        <div class="d-flex gap-2">
            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/apartment?action=init-floor"
               onclick="showListLoading()">
                <i class="bi bi-building-add me-1"></i> Khởi tạo tầng
            </a>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/apartment?action=create"
               onclick="showListLoading()">
                <i class="bi bi-plus-lg me-1"></i> Thêm lẻ
            </a>
        </div>
    </c:if>
</div>

<%-- ========== FILTER + SEARCH ========== --%>
<div class="card shadow-sm mb-3">
    <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/apartment" class="row g-2 align-items-end"
              onsubmit="showListLoading()">
            <input type="hidden" name="action" value="list">
            <input type="hidden" name="sort" value="${sort}">
            <input type="hidden" name="dir" value="${dir}">

            <div class="col-md-3">
                <label class="form-label small mb-1" for="keyword">Tìm kiếm</label>
                <input type="text" class="form-control form-control-sm" id="keyword" name="keyword"
                       value="${keyword}" placeholder="Mã căn, tòa nhà, ghi chú...">
            </div>
            <div class="col-md-2">
                <label class="form-label small mb-1" for="building">Tòa nhà</label>
                <input type="text" class="form-control form-control-sm" id="building" name="building"
                       value="${buildingFilter}" placeholder="VD: Tòa A">
            </div>
            <div class="col-md-2">
                <label class="form-label small mb-1" for="status">Trạng thái</label>
                <select class="form-select form-select-sm" id="status" name="status">
                    <option value="" ${empty statusFilter ? 'selected' : ''}>Tất cả</option>
                    <option value="ACTIVE" ${statusFilter == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                    <option value="INACTIVE" ${statusFilter == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                </select>
            </div>
            <div class="col-md-2">
                <label class="form-label small mb-1" for="occupancyType">Loại hình</label>
                <select class="form-select form-select-sm" id="occupancyType" name="occupancyType">
                    <option value="" ${empty occupancyFilter ? 'selected' : ''}>Tất cả</option>
                    <option value="OWNED" ${occupancyFilter == 'OWNED' ? 'selected' : ''}>OWNED</option>
                    <option value="RENTED" ${occupancyFilter == 'RENTED' ? 'selected' : ''}>RENTED</option>
                    <option value="VACANT" ${occupancyFilter == 'VACANT' ? 'selected' : ''}>VACANT</option>
                    <option value="N/A" ${occupancyFilter == 'N/A' ? 'selected' : ''}>N/A</option>
                </select>
            </div>
            <div class="col-md-3 d-flex gap-2">
                <button type="submit" class="btn btn-sm btn-primary">
                    <i class="bi bi-search me-1"></i> Lọc
                </button>
                <a class="btn btn-sm btn-outline-secondary"
                   href="${pageContext.request.contextPath}/apartment?action=list"
                   onclick="showListLoading()">Xóa lọc</a>
            </div>
        </form>
    </div>
</div>

<%-- ========== TABLE ========== --%>
<div class="card shadow-sm position-relative" id="apartmentListCard">
    <%-- Loading overlay (MVP) --%>
    <div id="listLoading" class="list-loading-overlay d-none">
        <div class="text-center text-primary">
            <div class="spinner-border mb-2" role="status"></div>
            <div class="small">Đang tải…</div>
        </div>
    </div>

    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th style="width: 50px;">#</th>
                    <th>
                        <a class="text-decoration-none text-dark" onclick="showListLoading()"
                           href="${listBaseUrl}&amp;sort=code&amp;dir=${sort == 'code' && dir == 'asc' ? 'desc' : 'asc'}&amp;page=1">
                            Mã căn
                            <c:if test="${sort == 'code'}"><i class="bi bi-caret-${dir == 'asc' ? 'up' : 'down'}-fill"></i></c:if>
                        </a>
                    </th>
                    <th>
                        <a class="text-decoration-none text-dark" onclick="showListLoading()"
                           href="${listBaseUrl}&amp;sort=building&amp;dir=${sort == 'building' && dir == 'asc' ? 'desc' : 'asc'}&amp;page=1">
                            Tòa
                            <c:if test="${sort == 'building'}"><i class="bi bi-caret-${dir == 'asc' ? 'up' : 'down'}-fill"></i></c:if>
                        </a>
                    </th>
                    <th>
                        <a class="text-decoration-none text-dark" onclick="showListLoading()"
                           href="${listBaseUrl}&amp;sort=floor&amp;dir=${sort == 'floor' && dir == 'asc' ? 'desc' : 'asc'}&amp;page=1">
                            Tầng
                            <c:if test="${sort == 'floor'}"><i class="bi bi-caret-${dir == 'asc' ? 'up' : 'down'}-fill"></i></c:if>
                        </a>
                    </th>
                    <th>
                        <a class="text-decoration-none text-dark" onclick="showListLoading()"
                           href="${listBaseUrl}&amp;sort=area&amp;dir=${sort == 'area' && dir == 'asc' ? 'desc' : 'asc'}&amp;page=1">
                            Diện tích
                            <c:if test="${sort == 'area'}"><i class="bi bi-caret-${dir == 'asc' ? 'up' : 'down'}-fill"></i></c:if>
                        </a>
                    </th>
                    <th>
                        <a class="text-decoration-none text-dark" onclick="showListLoading()"
                           href="${listBaseUrl}&amp;sort=occupancy&amp;dir=${sort == 'occupancy' && dir == 'asc' ? 'desc' : 'asc'}&amp;page=1">
                            Loại hình
                            <c:if test="${sort == 'occupancy'}"><i class="bi bi-caret-${dir == 'asc' ? 'up' : 'down'}-fill"></i></c:if>
                        </a>
                    </th>
                    <th>
                        <a class="text-decoration-none text-dark" onclick="showListLoading()"
                           href="${listBaseUrl}&amp;sort=status&amp;dir=${sort == 'status' && dir == 'asc' ? 'desc' : 'asc'}&amp;page=1">
                            Trạng thái
                            <c:if test="${sort == 'status'}"><i class="bi bi-caret-${dir == 'asc' ? 'up' : 'down'}-fill"></i></c:if>
                        </a>
                    </th>
                    <th>
                        <a class="text-decoration-none text-dark" onclick="showListLoading()"
                           href="${listBaseUrl}&amp;sort=members&amp;dir=${sort == 'members' && dir == 'asc' ? 'desc' : 'asc'}&amp;page=1">
                            TV
                            <c:if test="${sort == 'members'}"><i class="bi bi-caret-${dir == 'asc' ? 'up' : 'down'}-fill"></i></c:if>
                        </a>
                        <div class="small fw-normal text-muted">thành viên</div>
                    </th>
                    <th>Ghi chú</th>
                    <c:if test="${canManage}">
                        <th style="min-width: 220px;">Thao tác</th>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <%-- Empty state --%>
                    <c:when test="${empty apartments}">
                        <tr>
                            <td colspan="${canManage ? 10 : 9}" class="text-center py-5">
                                <div class="text-muted">
                                    <i class="bi bi-inbox display-6 d-block mb-2"></i>
                                    <c:choose>
                                        <c:when test="${hasFilter}">
                                            <div class="fw-semibold mb-1">Không tìm thấy căn hộ phù hợp</div>
                                            <div class="small mb-3">Thử đổi từ khóa hoặc bộ lọc.</div>
                                            <a class="btn btn-sm btn-outline-secondary"
                                               href="${pageContext.request.contextPath}/apartment?action=list">
                                                Xóa bộ lọc
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="fw-semibold mb-1">Chưa có căn hộ nào</div>
                                            <div class="small mb-3">Bắt đầu bằng cách thêm căn hộ đầu tiên.</div>
                                            <c:if test="${canManage}">
                                                <a class="btn btn-sm btn-primary"
                                                   href="${pageContext.request.contextPath}/apartment?action=create">
                                                    <i class="bi bi-plus-lg me-1"></i> Thêm căn hộ
                                                </a>
                                            </c:if>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="apt" items="${apartments}" varStatus="st">
                            <tr class="${apt.status == 'INACTIVE' ? 'table-secondary' : ''}">
                                <td class="text-muted">${fromIndex + st.index}</td>
                                <td class="fw-semibold">
                                    <a class="text-decoration-none"
                                       href="${pageContext.request.contextPath}/apartment?action=detail&amp;id=${apt.apartmentId}">
                                        <c:out value="${apt.apartmentCode}"/>
                                    </a>
                                </td>
                                <td><c:out value="${apt.building}"/></td>
                                <td>${apt.floorNumber}</td>
                                <td>
                                    <fmt:formatNumber value="${apt.areaM2}" minFractionDigits="0" maxFractionDigits="2"/> m²
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${apt.occupancyType == 'OWNED'}">
                                            <span class="badge text-bg-info">OWNED</span>
                                        </c:when>
                                        <c:when test="${apt.occupancyType == 'RENTED'}">
                                            <span class="badge text-bg-primary">RENTED</span>
                                        </c:when>
                                        <c:when test="${apt.occupancyType == 'VACANT'}">
                                            <span class="badge text-bg-light border">VACANT</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-secondary">N/A</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${apt.status == 'ACTIVE'}">
                                            <span class="badge text-bg-success">ACTIVE</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-warning">INACTIVE</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${empty apt.memberCount}">0</c:when>
                                        <c:otherwise>${apt.memberCount}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="small text-muted text-truncate" style="max-width: 160px;">
                                    <c:choose>
                                        <c:when test="${empty apt.notes}">—</c:when>
                                        <c:otherwise><c:out value="${apt.notes}"/></c:otherwise>
                                    </c:choose>
                                </td>
                                <c:if test="${canManage}">
                                    <td>
                                        <div class="d-flex flex-wrap gap-1">
                                            <a class="btn btn-sm btn-outline-secondary"
                                               href="${pageContext.request.contextPath}/apartment?action=detail&amp;id=${apt.apartmentId}">
                                                <i class="bi bi-eye"></i> Chi tiết
                                            </a>
                                            <a class="btn btn-sm btn-outline-primary"
                                               href="${pageContext.request.contextPath}/apartment?action=edit&amp;id=${apt.apartmentId}">
                                                <i class="bi bi-pencil"></i> Sửa
                                            </a>
                                            <c:choose>
                                                <c:when test="${apt.status == 'ACTIVE'}">
                                                    <form method="post" action="${pageContext.request.contextPath}/apartment" class="d-inline"
                                                          onsubmit="return confirm('Vô hiệu hóa căn ${apt.apartmentCode}?');">
                                                        <input type="hidden" name="action" value="deactivate">
                                                        <input type="hidden" name="id" value="${apt.apartmentId}">
                                                        <button type="submit" class="btn btn-sm btn-outline-warning">
                                                            <i class="bi bi-pause-circle"></i> Vô hiệu
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <a class="btn btn-sm btn-outline-success"
                                                       href="${pageContext.request.contextPath}/apartment?action=activate&amp;id=${apt.apartmentId}">
                                                        <i class="bi bi-play-circle"></i> Kích hoạt
                                                    </a>
                                                    <form method="post" action="${pageContext.request.contextPath}/apartment" class="d-inline"
                                                          onsubmit="return confirm('XÓA VĨNH VIỄN căn ${apt.apartmentCode}? Không hoàn tác!');">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="id" value="${apt.apartmentId}">
                                                        <button type="submit" class="btn btn-sm btn-outline-danger">
                                                            <i class="bi bi-trash"></i> Xóa
                                                        </button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <%-- ========== PAGINATION ========== --%>
    <c:if test="${totalItems > 0}">
        <div class="card-footer bg-white d-flex flex-wrap justify-content-between align-items-center gap-2">
            <div class="small text-muted">
                Hiển thị <strong>${fromIndex}</strong>–<strong>${toIndex}</strong>
                / <strong>${totalItems}</strong> căn
                · Trang ${currentPage}/${totalPages}
            </div>
            <nav>
                <ul class="pagination pagination-sm mb-0">
                    <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                        <a class="page-link" onclick="showListLoading()"
                           href="${listBaseUrl}&amp;sort=${sort}&amp;dir=${dir}&amp;page=${currentPage - 1}">Trước</a>
                    </li>
                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <c:if test="${i <= 3 || i > totalPages - 2 || (i >= currentPage - 1 && i <= currentPage + 1)}">
                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                <a class="page-link" onclick="showListLoading()"
                                   href="${listBaseUrl}&amp;sort=${sort}&amp;dir=${dir}&amp;page=${i}">${i}</a>
                            </li>
                        </c:if>
                    </c:forEach>
                    <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                        <a class="page-link" onclick="showListLoading()"
                           href="${listBaseUrl}&amp;sort=${sort}&amp;dir=${dir}&amp;page=${currentPage + 1}">Sau</a>
                    </li>
                </ul>
            </nav>
        </div>
    </c:if>
</div>

<style>
    .list-loading-overlay {
        position: absolute;
        inset: 0;
        background: rgba(255, 255, 255, 0.72);
        z-index: 5;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 0.375rem;
    }
    .list-loading-overlay.d-none { display: none !important; }
</style>
<script>
    function showListLoading() {
        var el = document.getElementById('listLoading');
        if (el) {
            el.classList.remove('d-none');
        }
    }
</script>
