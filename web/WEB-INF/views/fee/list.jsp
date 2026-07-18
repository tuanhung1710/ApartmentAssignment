<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-2 mb-3">
    <div>
        <h2 class="h4 mb-1">Quản lý phí</h2>
        <p class="text-muted small mb-0">
            Đợt phí theo phạm vi · gán căn · theo dõi thu
            <c:if test="${not empty filterStatus}">
                · Đang lọc:
                <span class="badge text-bg-secondary">${filterStatus}</span>
                <span class="text-body-secondary">(${empty totalItems ? 0 : totalItems} đợt)</span>
            </c:if>
        </p>
    </div>
    <div class="d-flex gap-2">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/fee?action=create">
            <i class="bi bi-plus-lg me-1"></i> Tạo khoản phí
        </a>
    </div>
</div>

<c:if test="${not empty filterStatus}">
    <div class="alert alert-info py-2 small mb-3" role="status">
        Đang xem phí trạng thái <strong>${filterStatus}</strong>
        (<c:choose>
            <c:when test="${filterStatus == 'DRAFT'}"> (bản nháp — chưa gán căn, có thể chưa đủ thông tin).</c:when>
            <c:when test="${filterStatus == 'ASSIGNED'}"> (đã gán căn, chưa công bố cho cư dân).</c:when>
            <c:when test="${filterStatus == 'PUBLISHED'}"> (đã công bố — cư dân thấy được).</c:when>
            <c:otherwise>.</c:otherwise>
        </c:choose>
        Các thẻ thống kê phía trên là <strong>toàn hệ thống</strong>, không đổi theo bộ lọc.
        <a class="alert-link ms-1" href="${pageContext.request.contextPath}/fee?action=list">Xem tất cả</a>
    </div>
</c:if>

<div class="row g-3 mb-3">
    <div class="col-6 col-md">
        <div class="card shadow-sm h-100 border-0 border-start border-4 border-primary">
            <div class="card-body py-3">
                <div class="text-muted small">Đợt phí</div>
                <div class="fs-4 fw-semibold">${summary.feeBatchCount}</div>
            </div>
        </div>
    </div>
    <div class="col-6 col-md">
        <div class="card shadow-sm h-100 border-0 border-start border-4 border-info">
            <div class="card-body py-3">
                <div class="text-muted small">Hàng tháng</div>
                <div class="fs-4 fw-semibold">${summary.monthlyCount}</div>
                <div class="small text-muted">
                    <fmt:formatNumber value="${summary.monthlyReceivable}" type="number" maxFractionDigits="0"/> đ
                </div>
            </div>
        </div>
    </div>
    <div class="col-6 col-md">
        <div class="card shadow-sm h-100 border-0 border-start border-4 border-warning">
            <div class="card-body py-3">
                <div class="text-muted small">Phát sinh</div>
                <div class="fs-4 fw-semibold">${summary.oneTimeCount}</div>
                <div class="small text-muted">
                    <fmt:formatNumber value="${summary.oneTimeReceivable}" type="number" maxFractionDigits="0"/> đ
                </div>
            </div>
        </div>
    </div>
    <div class="col-6 col-md">
        <div class="card shadow-sm h-100 border-0 border-start border-4 border-secondary">
            <div class="card-body py-3">
                <div class="text-muted small">Tổng số căn gán</div>
                <div class="fs-4 fw-semibold">${summary.totalApartments}</div>
            </div>
        </div>
    </div>
    <div class="col-6 col-md">
        <div class="card shadow-sm h-100 border-0 border-start border-4 border-dark">
            <div class="card-body py-3">
                <div class="text-muted small">Tổng phải thu</div>
                <div class="fs-5 fw-semibold">
                    <fmt:formatNumber value="${summary.totalReceivable}" type="number" maxFractionDigits="0"/> đ
                </div>
            </div>
        </div>
    </div>
    <div class="col-6 col-md">
        <div class="card shadow-sm h-100 border-0 border-start border-4 border-success">
            <div class="card-body py-3">
                <div class="text-muted small">Tổng đã thanh toán</div>
                <div class="fs-5 fw-semibold text-success">
                    <fmt:formatNumber value="${summary.totalPaid}" type="number" maxFractionDigits="0"/> đ
                </div>
                <div class="small text-muted">${summary.paidCount} căn</div>
            </div>
        </div>
    </div>
    <div class="col-6 col-md">
        <div class="card shadow-sm h-100 border-0 border-start border-4 border-danger">
            <div class="card-body py-3">
                <div class="text-muted small">Tổng chưa thanh toán</div>
                <div class="fs-5 fw-semibold text-warning">
                    <fmt:formatNumber value="${summary.totalUnpaid}" type="number" maxFractionDigits="0"/> đ
                </div>
                <div class="small text-muted">${summary.unpaidCount} căn</div>
            </div>
        </div>
    </div>
</div>

<div class="card shadow-sm mb-3">
    <div class="card-body">
        <form class="row g-2 align-items-end" method="get" action="${pageContext.request.contextPath}/fee">
            <input type="hidden" name="action" value="list"/>
            <div class="col-md-2">
                <label class="form-label">Danh mục</label>
                <select class="form-select" name="categoryId">
                    <option value="">-- Tất cả --</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.categoryId}" ${filterCategoryId == cat.categoryId ? 'selected' : ''}>${cat.name}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-2">
                <label class="form-label">Loại phí</label>
                <select class="form-select" name="feeType">
                    <option value="">-- Tất cả --</option>
                    <option value="MONTHLY" ${filterFeeType == 'MONTHLY' ? 'selected' : ''}>Hàng tháng</option>
                    <option value="ONE_TIME" ${filterFeeType == 'ONE_TIME' ? 'selected' : ''}>Phát sinh</option>
                </select>
            </div>
            <div class="col-md-1">
                <label class="form-label">Tháng</label>
                <input type="number" class="form-control" name="month" min="1" max="12"
                       value="${filterMonth}" placeholder="1-12"/>
            </div>
            <div class="col-md-1">
                <label class="form-label">Năm</label>
                <input type="number" class="form-control" name="year" min="2000" max="2100"
                       value="${filterYear}" placeholder="YYYY"/>
            </div>
            <div class="col-md-2">
                <label class="form-label">Trạng thái</label>
                <select class="form-select" name="status">
                    <option value="">-- Tất cả --</option>
                    <option value="DRAFT" ${filterStatus == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                    <option value="ASSIGNED" ${filterStatus == 'ASSIGNED' ? 'selected' : ''}>ASSIGNED</option>
                    <option value="PUBLISHED" ${filterStatus == 'PUBLISHED' ? 'selected' : ''}>PUBLISHED</option>
                </select>
            </div>
            <div class="col-md-4 d-flex gap-2">
                <button type="submit" class="btn btn-outline-primary">Lọc</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/fee?action=list">Xóa lọc</a>
            </div>
        </form>
    </div>
</div>

<div class="card shadow-sm">
    <div class="card-header bg-white fw-semibold">Danh sách đợt phí</div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <th>Đợt phí</th>
                <th>Danh mục</th>
                <th>Phạm vi</th>
                <th class="text-end">Đơn giá</th>
                <th class="text-center">Tổng căn</th>
                <th class="text-end">Phải thu</th>
                <th class="text-end">Đã TT</th>
                <th class="text-end">Chưa TT</th>
                <th>Trạng thái</th>
                <th class="text-end">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty fees}">
                    <tr>
                        <td colspan="10" class="text-center text-muted py-4">
                            <c:choose>
                                <c:when test="${not empty filterStatus}">
                                    Không có đợt phí trạng thái <strong>${filterStatus}</strong>.
                                    <a href="${pageContext.request.contextPath}/fee?action=list">Xóa lọc</a>
                                </c:when>
                                <c:otherwise>Chưa có khoản phí nào.</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="f" items="${fees}">
                        <tr>
                            <td>
                                <strong>${f.title}</strong>
                                <div class="small text-muted">
                                    <c:choose>
                                        <c:when test="${f.feeType == 'ONE_TIME'}">
                                            <span class="badge text-bg-warning">Phát sinh</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-info">Hàng tháng</span>
                                        </c:otherwise>
                                    </c:choose>
                                    <c:choose>
                                        <c:when test="${not empty f.feeMonth && not empty f.feeYear}">
                                            · ${f.feeMonth}/${f.feeYear}
                                        </c:when>
                                        <c:when test="${not empty f.feeYear}">
                                            · ${f.feeYear}
                                        </c:when>
                                    </c:choose>
                                </div>
                            </td>
                            <td>${f.categoryName}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${f.scopeType == 'ALL'}"><span class="badge text-bg-info">Toàn khu</span></c:when>
                                    <c:when test="${f.scopeType == 'BUILDING'}"><span class="badge text-bg-primary">Tòa ${f.scopeBuilding}</span></c:when>
                                    <c:when test="${f.scopeType == 'FLOOR'}"><span class="badge text-bg-secondary">Tòa ${f.scopeBuilding} · Tầng ${f.scopeFloor}</span></c:when>
                                    <c:when test="${f.scopeType == 'APARTMENT'}"><span class="badge text-bg-dark">Căn ${f.scopeApartmentCode}</span></c:when>
                                    <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end">
                                <fmt:formatNumber value="${f.amount}" type="number" maxFractionDigits="0"/>
                            </td>
                            <td class="text-center">
                                <span class="badge text-bg-light text-dark border">${empty f.assignmentCount ? 0 : f.assignmentCount}</span>
                            </td>
                            <td class="text-end fw-semibold">
                                <fmt:formatNumber value="${empty f.totalReceivable ? 0 : f.totalReceivable}" type="number" maxFractionDigits="0"/>
                            </td>
                            <td class="text-end text-success">
                                <fmt:formatNumber value="${empty f.totalPaid ? 0 : f.totalPaid}" type="number" maxFractionDigits="0"/>
                                <div class="small text-muted">${empty f.paidCount ? 0 : f.paidCount} căn</div>
                            </td>
                            <td class="text-end text-warning">
                                <fmt:formatNumber value="${empty f.totalUnpaid ? 0 : f.totalUnpaid}" type="number" maxFractionDigits="0"/>
                                <div class="small text-muted">${empty f.unpaidCount ? 0 : f.unpaidCount} căn</div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${f.status == 'DRAFT'}"><span class="badge text-bg-secondary">DRAFT</span></c:when>
                                    <c:when test="${f.status == 'ASSIGNED'}"><span class="badge text-bg-success">ASSIGNED</span></c:when>
                                    <c:when test="${f.status == 'PUBLISHED'}"><span class="badge text-bg-warning">PUBLISHED</span></c:when>
                                    <c:otherwise><span class="badge text-bg-light text-dark">${f.status}</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end text-nowrap">
                                <a class="btn btn-sm btn-outline-primary"
                                   href="${pageContext.request.contextPath}/fee?action=detail&amp;id=${f.feeId}">Chi tiết</a>
                                <c:if test="${f.status == 'DRAFT'}">
                                    <form method="post" action="${pageContext.request.contextPath}/fee" class="d-inline">
                                        <input type="hidden" name="action" value="assign"/>
                                        <input type="hidden" name="id" value="${f.feeId}"/>
                                        <button type="submit" class="btn btn-sm btn-primary"
                                                onclick="return confirm('Gán căn? DRAFT → ASSIGNED');">Gán</button>
                                    </form>
                                </c:if>
                                <c:if test="${f.status == 'ASSIGNED'}">
                                    <form method="post" action="${pageContext.request.contextPath}/fee" class="d-inline">
                                        <input type="hidden" name="action" value="publish"/>
                                        <input type="hidden" name="id" value="${f.feeId}"/>
                                        <button type="submit" class="btn btn-sm btn-warning"
                                                onclick="return confirm('Công bố? ASSIGNED → PUBLISHED');">Công bố</button>
                                    </form>
                                </c:if>
                                <form method="post" action="${pageContext.request.contextPath}/fee" class="d-inline">
                                    <input type="hidden" name="action" value="delete"/>
                                    <input type="hidden" name="id" value="${f.feeId}"/>
                                    <button type="submit" class="btn btn-sm btn-outline-danger"
                                            onclick="return confirm('Xóa đợt phí này? Chỉ xóa được khi chưa có căn thanh toán.');">
                                        Xóa
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>

    <c:if test="${totalPages > 1}">
        <div class="card-footer d-flex justify-content-between align-items-center">
            <span class="small text-muted">Tổng ${totalItems} · trang ${currentPage}/${totalPages}</span>
            <c:url value="/fee" var="paginationUrl">
                <c:param name="action" value="list"/>
                <c:if test="${not empty filterCategoryId}">
                    <c:param name="categoryId" value="${filterCategoryId}"/>
                </c:if>
                <c:if test="${not empty filterFeeType}">
                    <c:param name="feeType" value="${filterFeeType}"/>
                </c:if>
                <c:if test="${not empty filterStatus}">
                    <c:param name="status" value="${filterStatus}"/>
                </c:if>
                <c:if test="${not empty filterMonth}">
                    <c:param name="month" value="${filterMonth}"/>
                </c:if>
                <c:if test="${not empty filterYear}">
                    <c:param name="year" value="${filterYear}"/>
                </c:if>
            </c:url>
            <c:set var="pageParam" value="page"/>
            <c:set var="paginationLabel" value="Phân trang danh sách phí"/>
            <c:set var="paginationAlign" value=""/>
            <%@ include file="/WEB-INF/views/common/pagination.jsp" %>
        </div>
    </c:if>
</div>
