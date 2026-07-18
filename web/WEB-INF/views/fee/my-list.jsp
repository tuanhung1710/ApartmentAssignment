<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="mb-3">
    <h2 class="h4 mb-1">Phí của tôi</h2>
    <p class="text-muted small mb-0">Các khoản phí được gán cho căn hộ bạn đang ở.</p>
</div>

<div class="row g-3 mb-3">
    <div class="col-6 col-md">
        <div class="card shadow-sm h-100 border-0 border-start border-4 border-primary">
            <div class="card-body py-3">
                <div class="text-muted small">Khoản phí</div>
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
                <div class="text-muted small">Tổng phải đóng</div>
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
                <div class="small text-muted">${summary.paidCount} khoản</div>
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
                <div class="small text-muted">${summary.unpaidCount} khoản</div>
            </div>
        </div>
    </div>
</div>

<div class="card shadow-sm mb-3">
    <div class="card-body">
        <form class="row g-2 align-items-end" method="get" action="${pageContext.request.contextPath}/fee">
            <input type="hidden" name="action" value="my"/>
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
                    <option value="UNPAID" ${filterStatus == 'UNPAID' ? 'selected' : ''}>UNPAID</option>
                    <option value="PAID" ${filterStatus == 'PAID' ? 'selected' : ''}>PAID</option>
                </select>
            </div>
            <div class="col-md-4 d-flex gap-2">
                <button type="submit" class="btn btn-outline-primary">Lọc</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/fee?action=my">Xóa lọc</a>
            </div>
        </form>
    </div>
</div>

<div class="card shadow-sm">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <th>Khoản phí</th>
                <th>Danh mục</th>
                <th>Căn hộ</th>
                <th class="text-end">Số tiền</th>
                <th>Trạng thái</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty assignments}">
                    <tr>
                        <td colspan="6" class="text-center text-muted py-4">Chưa có phí được gán.</td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="a" items="${assignments}">
                        <tr>
                            <td>
                                <strong>${a.feeTitle}</strong>
                                <div class="small text-muted">
                                    <c:choose>
                                        <c:when test="${a.feeType == 'ONE_TIME'}">
                                            <span class="badge text-bg-warning">Phát sinh</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-info">Hàng tháng</span>
                                        </c:otherwise>
                                    </c:choose>
                                    <c:choose>
                                        <c:when test="${not empty a.feeMonth && not empty a.feeYear}">
                                            · ${a.feeMonth}/${a.feeYear}
                                        </c:when>
                                        <c:when test="${not empty a.feeYear}">
                                            · ${a.feeYear}
                                        </c:when>
                                    </c:choose>
                                </div>
                            </td>
                            <td>${a.categoryName}</td>
                            <td>${a.apartmentCode}</td>
                            <td class="text-end fw-semibold">
                                <fmt:formatNumber value="${a.amount}" type="number" maxFractionDigits="0"/> đ
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${a.status == 'PAID'}"><span class="badge text-bg-success">PAID</span></c:when>
                                    <c:otherwise><span class="badge text-bg-warning">UNPAID</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-outline-primary"
                                   href="${pageContext.request.contextPath}/fee?action=assignment&amp;id=${a.assignmentId}">Chi tiết</a>
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
                <c:param name="action" value="my"/>
                <c:if test="${not empty filterCategoryId}">
                    <c:param name="categoryId" value="${filterCategoryId}"/>
                </c:if>
                <c:if test="${not empty filterFeeType}">
                    <c:param name="feeType" value="${filterFeeType}"/>
                </c:if>
                <c:if test="${not empty filterMonth}">
                    <c:param name="month" value="${filterMonth}"/>
                </c:if>
                <c:if test="${not empty filterYear}">
                    <c:param name="year" value="${filterYear}"/>
                </c:if>
                <c:if test="${not empty filterStatus}">
                    <c:param name="status" value="${filterStatus}"/>
                </c:if>
            </c:url>
            <c:set var="pageParam" value="page"/>
            <c:set var="paginationLabel" value="Phân trang phí của tôi"/>
            <c:set var="paginationAlign" value=""/>
            <%@ include file="/WEB-INF/views/common/pagination.jsp" %>
        </div>
    </c:if>
</div>
