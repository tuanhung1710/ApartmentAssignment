<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">${fee.title}</h2>
        <p class="text-muted small mb-0">
            ${fee.categoryName}
            ·
            <c:choose>
                <c:when test="${fee.feeType == 'ONE_TIME'}"><span class="badge text-bg-warning">Phát sinh</span></c:when>
                <c:otherwise><span class="badge text-bg-info">Hàng tháng</span></c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${not empty fee.feeMonth && not empty fee.feeYear}"> · ${fee.feeMonth}/${fee.feeYear}</c:when>
                <c:when test="${not empty fee.feeYear}"> · ${fee.feeYear}</c:when>
            </c:choose>
            ·
            <c:choose>
                <c:when test="${fee.status == 'DRAFT'}"><span class="badge text-bg-secondary">DRAFT</span></c:when>
                <c:when test="${fee.status == 'ASSIGNED'}"><span class="badge text-bg-success">ASSIGNED</span></c:when>
                <c:when test="${fee.status == 'PUBLISHED'}"><span class="badge text-bg-warning">PUBLISHED</span></c:when>
                <c:otherwise><span class="badge text-bg-light text-dark">${fee.status}</span></c:otherwise>
            </c:choose>
        </p>
    </div>
    <div class="d-flex flex-wrap gap-2 justify-content-end">
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/fee?action=list">Quay lại list</a>

        <c:if test="${fee.status == 'DRAFT'}">
            <form method="post" action="${pageContext.request.contextPath}/fee" class="d-inline">
                <input type="hidden" name="action" value="assign"/>
                <input type="hidden" name="id" value="${fee.feeId}"/>
                <button type="submit" class="btn btn-primary"
                        onclick="return confirm('Gán phí cho các căn trong phạm vi (ACTIVE + INACTIVE)? DRAFT → ASSIGNED');">
                    <i class="bi bi-people me-1"></i> Gán căn
                </button>
            </form>
        </c:if>

        <c:if test="${fee.status == 'ASSIGNED'}">
            <form method="post" action="${pageContext.request.contextPath}/fee" class="d-inline">
                <input type="hidden" name="action" value="publish"/>
                <input type="hidden" name="id" value="${fee.feeId}"/>
                <button type="submit" class="btn btn-warning"
                        onclick="return confirm('Công bố đợt phí? Cư dân sẽ thấy trên Phí của tôi. ASSIGNED → PUBLISHED');">
                    <i class="bi bi-megaphone me-1"></i> Công bố
                </button>
            </form>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/fee" class="d-inline">
            <input type="hidden" name="action" value="delete"/>
            <input type="hidden" name="id" value="${fee.feeId}"/>
            <button type="submit" class="btn btn-outline-danger"
                    onclick="return confirm('Xóa đợt phí này? Chỉ xóa được khi chưa có căn thanh toán.');">
                Xóa đợt phí
            </button>
        </form>
    </div>
</div>

<c:if test="${fee.status == 'DRAFT'}">
    <div class="alert alert-secondary py-2 small">
        <strong>DRAFT</strong> — đợt phí nháp, chưa gán căn. Bấm <em>Gán căn</em> để tạo assignment theo phạm vi.
    </div>
</c:if>
<c:if test="${fee.status == 'ASSIGNED'}">
    <div class="alert alert-success py-2 small">
        <strong>ASSIGNED</strong> — đã gán căn (nội bộ). Cư dân <em>chưa</em> thấy. Bấm <em>Công bố</em> để publish.
    </div>
</c:if>
<c:if test="${fee.status == 'PUBLISHED'}">
    <div class="alert alert-warning py-2 small">
        <strong>PUBLISHED</strong> — cư dân đã thấy phí trên màn <em>Phí của tôi</em>.
    </div>
</c:if>

<div class="row g-2 mb-3">
    <div class="col-6 col-md">
        <div class="card shadow-sm border-0 border-start border-4 border-info h-100">
            <div class="card-body py-2">
                <div class="text-muted small">Tổng số căn</div>
                <div class="fs-5 fw-semibold">${empty fee.assignmentCount ? 0 : fee.assignmentCount}</div>
            </div>
        </div>
    </div>
    <div class="col-6 col-md">
        <div class="card shadow-sm border-0 border-start border-4 border-secondary h-100">
            <div class="card-body py-2">
                <div class="text-muted small">Tổng phải thu</div>
                <div class="fs-6 fw-semibold">
                    <fmt:formatNumber value="${empty fee.totalReceivable ? 0 : fee.totalReceivable}" type="number" maxFractionDigits="0"/> đ
                </div>
            </div>
        </div>
    </div>
    <div class="col-6 col-md">
        <div class="card shadow-sm border-0 border-start border-4 border-success h-100">
            <div class="card-body py-2">
                <div class="text-muted small">Đã thanh toán</div>
                <div class="fs-6 fw-semibold text-success">
                    <fmt:formatNumber value="${empty fee.totalPaid ? 0 : fee.totalPaid}" type="number" maxFractionDigits="0"/> đ
                </div>
                <div class="small text-muted">${empty fee.paidCount ? 0 : fee.paidCount} căn</div>
            </div>
        </div>
    </div>
    <div class="col-6 col-md">
        <div class="card shadow-sm border-0 border-start border-4 border-warning h-100">
            <div class="card-body py-2">
                <div class="text-muted small">Chưa thanh toán</div>
                <div class="fs-6 fw-semibold text-warning">
                    <fmt:formatNumber value="${empty fee.totalUnpaid ? 0 : fee.totalUnpaid}" type="number" maxFractionDigits="0"/> đ
                </div>
                <div class="small text-muted">${empty fee.unpaidCount ? 0 : fee.unpaidCount} căn</div>
            </div>
        </div>
    </div>
</div>

<div class="row g-3 mb-3">
    <div class="col-lg-4">
        <div class="card shadow-sm h-100">
            <div class="card-body">
                <dl class="row mb-0 small">
                    <dt class="col-5">Số tiền / căn</dt>
                    <dd class="col-7 fw-semibold">
                        <fmt:formatNumber value="${fee.amount}" type="number" maxFractionDigits="0"/> đ
                    </dd>
                    <dt class="col-5">Loại phí</dt>
                    <dd class="col-7">
                        <c:choose>
                            <c:when test="${fee.feeType == 'ONE_TIME'}">Phát sinh trong tháng</c:when>
                            <c:otherwise>Hàng tháng / thường niên</c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${not empty fee.feeMonth && not empty fee.feeYear}">
                                <div class="text-muted">${fee.feeMonth}/${fee.feeYear}</div>
                            </c:when>
                            <c:when test="${not empty fee.feeYear}">
                                <div class="text-muted">${fee.feeYear}</div>
                            </c:when>
                        </c:choose>
                    </dd>
                    <dt class="col-5">Trạng thái</dt>
                    <dd class="col-7">
                        <c:choose>
                            <c:when test="${fee.status == 'DRAFT'}"><span class="badge text-bg-secondary">DRAFT</span></c:when>
                            <c:when test="${fee.status == 'ASSIGNED'}"><span class="badge text-bg-success">ASSIGNED</span></c:when>
                            <c:when test="${fee.status == 'PUBLISHED'}"><span class="badge text-bg-warning">PUBLISHED</span></c:when>
                            <c:otherwise>${fee.status}</c:otherwise>
                        </c:choose>
                    </dd>
                    <dt class="col-5">Phạm vi</dt>
                    <dd class="col-7">
                        <c:choose>
                            <c:when test="${scope.scopeType == 'ALL'}">Toàn khu</c:when>
                            <c:when test="${scope.scopeType == 'BUILDING'}">Tòa ${scope.building}</c:when>
                            <c:when test="${scope.scopeType == 'FLOOR'}">Tòa ${scope.building} · Tầng ${scope.floorNumber}</c:when>
                            <c:when test="${scope.scopeType == 'APARTMENT'}">Căn ${scope.apartmentCode}</c:when>
                            <c:otherwise>—</c:otherwise>
                        </c:choose>
                    </dd>
                    <dt class="col-5">Ghi chú</dt>
                    <dd class="col-7">${empty fee.note ? '—' : fee.note}</dd>
                </dl>
            </div>
        </div>
    </div>
    <div class="col-lg-8">
        <div class="card shadow-sm">
            <div class="card-header d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-2">
                <span>Danh sách căn được gán phí</span>
                <div class="d-flex flex-wrap align-items-center gap-2">
                    <c:if test="${not empty totalItems}">
                        <span class="badge text-bg-light text-dark border">${totalItems} căn</span>
                    </c:if>
                    <c:set var="hasAptFilter" value="${not empty filterQ || not empty filterBuilding || not empty filterFloor}"/>
                    <button class="btn btn-sm btn-outline-primary"
                            type="button"
                            data-bs-toggle="collapse"
                            data-bs-target="#feeApartmentSearch"
                            aria-expanded="${hasAptFilter ? 'true' : 'false'}"
                            aria-controls="feeApartmentSearch">
                        <i class="bi bi-search me-1"></i> Tìm căn hộ
                    </button>
                </div>
            </div>
            <div id="feeApartmentSearch" class="collapse ${hasAptFilter ? 'show' : ''}">
                <div class="card-body border-bottom py-3">
                    <form method="get" action="${pageContext.request.contextPath}/fee"
                          class="row g-2 align-items-end" id="feeDetailFilterForm">
                        <input type="hidden" name="action" value="detail"/>
                        <input type="hidden" name="id" value="${fee.feeId}"/>
                        <div class="col-12 col-md-3">
                            <label class="form-label small mb-1 fw-semibold" for="filterBuilding">
                                Chọn tòa
                            </label>
                            <select class="form-select" id="filterBuilding" name="building"
                                    onchange="var fl=document.getElementById('filterFloor'); if(fl){fl.value=''; fl.disabled=true;} this.form.submit();">
                                <option value="">-- Tất cả tòa --</option>
                                <c:forEach var="b" items="${filterBuildings}">
                                    <option value="${b}" ${filterBuilding == b ? 'selected' : ''}>${b}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-12 col-md-3">
                            <label class="form-label small mb-1 fw-semibold" for="filterFloor">
                                Chọn tầng
                            </label>
                            <select class="form-select" id="filterFloor" name="floor"
                                    ${empty filterBuilding ? 'disabled' : ''}
                                    onchange="this.form.submit()">
                                <option value="">-- Tất cả tầng --</option>
                                <c:forEach var="f" items="${filterFloors}">
                                    <option value="${f}" ${filterFloor == f ? 'selected' : ''}>Tầng ${f}</option>
                                </c:forEach>
                            </select>
                            <c:if test="${empty filterBuilding}">
                                <div class="form-text small">Chọn tòa trước để lọc tầng</div>
                            </c:if>
                        </div>
                        <div class="col-12 col-md">
                            <label class="form-label small mb-1 fw-semibold" for="q">Tìm căn hộ</label>
                            <div class="input-group fee-search-group">
                                <span class="input-group-text"><i class="bi bi-search"></i></span>
                                <input type="text" class="form-control" id="q" name="q"
                                       value="${filterQ}"
                                       placeholder="Mã căn… (vd: A101)"
                                       autocomplete="off"/>
                            </div>
                        </div>
                        <div class="col-12 col-md-auto d-flex flex-column flex-sm-row gap-2">
                            <button type="submit" class="btn btn-outline-primary">
                                <i class="bi bi-search me-1"></i> Tìm
                            </button>
                            <c:if test="${hasAptFilter}">
                                <a class="btn btn-outline-secondary"
                                   href="${pageContext.request.contextPath}/fee?action=detail&amp;id=${fee.feeId}">
                                    Xóa lọc
                                </a>
                            </c:if>
                        </div>
                    </form>
                    <c:if test="${hasAptFilter}">
                        <p class="small text-muted mb-0 mt-2">
                            Đang lọc
                            <c:if test="${not empty filterBuilding}">
                                · Tòa <strong><c:out value="${filterBuilding}"/></strong>
                            </c:if>
                            <c:if test="${not empty filterFloor}">
                                · Tầng <strong>${filterFloor}</strong>
                            </c:if>
                            <c:if test="${not empty filterQ}">
                                · &ldquo;<strong><c:out value="${filterQ}"/></strong>&rdquo;
                            </c:if>
                            · ${empty totalItems ? 0 : totalItems} căn
                        </p>
                    </c:if>
                </div>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th>Căn hộ</th>
                        <th>Tòa / Tầng</th>
                        <th class="text-end">Số tiền</th>
                        <th>Trạng thái</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty assignments}">
                            <tr>
                                <td colspan="5" class="text-center text-muted py-3">
                                    <c:choose>
                                        <c:when test="${hasAptFilter}">
                                            Không tìm thấy căn khớp bộ lọc hiện tại.
                                        </c:when>
                                        <c:otherwise>Chưa gán căn nào.</c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="a" items="${assignments}">
                                <tr>
                                    <td><strong>${a.apartmentCode}</strong></td>
                                    <td>${a.building}<c:if test="${not empty a.floorNumber}"> / ${a.floorNumber}</c:if></td>
                                    <td class="text-end"><fmt:formatNumber value="${a.amount}" type="number" maxFractionDigits="0"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${a.status == 'PAID'}"><span class="badge text-bg-success">PAID</span></c:when>
                                            <c:otherwise><span class="badge text-bg-warning">UNPAID</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end">
                                        <c:if test="${fee.status == 'PUBLISHED' && a.status == 'UNPAID'}">
                                            <form method="post" action="${pageContext.request.contextPath}/fee" class="d-inline">
                                                <input type="hidden" name="action" value="mark-paid"/>
                                                <input type="hidden" name="id" value="${a.assignmentId}"/>
                                                <input type="hidden" name="feeId" value="${fee.feeId}"/>
                                                <button type="submit" class="btn btn-sm btn-success"
                                                        onclick="return confirm('Đánh dấu đã TT?');">Đã TT</button>
                                            </form>
                                        </c:if>
                                        <c:if test="${fee.status == 'PUBLISHED' && a.status == 'PAID'}">
                                            <form method="post" action="${pageContext.request.contextPath}/fee" class="d-inline">
                                                <input type="hidden" name="action" value="mark-unpaid"/>
                                                <input type="hidden" name="id" value="${a.assignmentId}"/>
                                                <input type="hidden" name="feeId" value="${fee.feeId}"/>
                                                <button type="submit" class="btn btn-sm btn-outline-warning"
                                                        onclick="return confirm('Đánh dấu chưa TT?');">Chưa TT</button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
            <c:if test="${totalPages > 1}">
                <div class="card-footer">
                    <c:url value="/fee" var="paginationUrl">
                        <c:param name="action" value="detail"/>
                        <c:param name="id" value="${fee.feeId}"/>
                        <c:if test="${not empty filterBuilding}">
                            <c:param name="building" value="${filterBuilding}"/>
                        </c:if>
                        <c:if test="${not empty filterFloor}">
                            <c:param name="floor" value="${filterFloor}"/>
                        </c:if>
                        <c:if test="${not empty filterQ}">
                            <c:param name="q" value="${filterQ}"/>
                        </c:if>
                    </c:url>
                    <c:set var="pageParam" value="page"/>
                    <c:set var="paginationLabel" value="Phân trang chi tiết phí"/>
                    <c:set var="paginationAlign" value=""/>
                    <%@ include file="/WEB-INF/views/common/pagination.jsp" %>
                </div>
            </c:if>
        </div>
    </div>
</div>
