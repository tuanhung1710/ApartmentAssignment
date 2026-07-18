<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
    <div>
        <h2 class="h4 mb-1">Chi tiết yêu cầu #${requestDetail.requestId}</h2>
        <p class="text-muted small mb-0">Chi tiết Request · xử lý</p>
    </div>
    <div class="d-flex gap-2 flex-wrap">
        <c:if test="${requestDetail.status == 'PENDING'
                      && (sessionScope.currentUser.role == 'MANAGER'
                          || sessionScope.currentUser.role == 'ADMIN')}">
            <form method="post"
                  action="${pageContext.request.contextPath}/request"
                  class="d-inline"
                  onsubmit="return confirm('Xác nhận phê duyệt yêu cầu này?');">
                <input type="hidden" name="action" value="approve"/>
                <input type="hidden" name="id" value="${requestDetail.requestId}"/>
                <button type="submit" class="btn btn-sm btn-success">
                    <i class="bi bi-check2-circle me-1"></i> Approve
                </button>
            </form>

            <button type="button"
                    class="btn btn-sm btn-danger"
                    data-bs-toggle="collapse"
                    data-bs-target="#rejectFormPanel"
                    aria-expanded="false"
                    aria-controls="rejectFormPanel">
                <i class="bi bi-x-circle me-1"></i> Reject
            </button>
        </c:if>

        <a class="btn btn-sm btn-outline-secondary"
           href="${pageContext.request.contextPath}/request?action=list">
            <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
        </a>
    </div>
</div>

<c:if test="${requestDetail.status == 'PENDING'
              && (sessionScope.currentUser.role == 'MANAGER'
                  || sessionScope.currentUser.role == 'ADMIN')}">
    <div class="collapse mb-3" id="rejectFormPanel">
        <div class="card border-danger border-opacity-25 shadow-sm">
            <div class="card-body">
                <h3 class="h6 text-danger mb-2">
                    <i class="bi bi-exclamation-triangle me-1"></i> Từ chối yêu cầu
                </h3>
                <form method="post" action="${pageContext.request.contextPath}/request">
                    <input type="hidden" name="action" value="reject"/>
                    <input type="hidden" name="id" value="${requestDetail.requestId}"/>
                    <div class="mb-2">
                        <label for="rejectReason" class="form-label small mb-1">
                            Lý do từ chối <span class="text-danger">*</span>
                        </label>
                        <textarea class="form-control form-control-sm"
                                  id="rejectReason"
                                  name="rejectReason"
                                  rows="3"
                                  maxlength="500"
                                  required
                                  placeholder="Nhập lý do từ chối (bắt buộc, tối đa 500 ký tự)"></textarea>
                    </div>
                    <div class="d-flex gap-2">
                        <button type="submit"
                                class="btn btn-sm btn-danger"
                                onclick="return confirm('Xác nhận từ chối yêu cầu này?');">
                            <i class="bi bi-x-circle me-1"></i> Xác nhận từ chối
                        </button>
                        <button type="button"
                                class="btn btn-sm btn-outline-secondary"
                                data-bs-toggle="collapse"
                                data-bs-target="#rejectFormPanel">
                            Hủy
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</c:if>

<%-- UC-PROC-05: Assign Staff – Manager/Admin + status APPROVED --%>
<c:if test="${requestDetail.status == 'APPROVED'
              && (sessionScope.currentUser.role == 'MANAGER'
                  || sessionScope.currentUser.role == 'ADMIN')}">
    <div class="card border-0 shadow-sm mb-3">
        <div class="card-body">
            <h3 class="h6 mb-3">
                <i class="bi bi-person-plus me-1"></i> Gán Staff xử lý
            </h3>
            <form method="post"
                  action="${pageContext.request.contextPath}/request"
                  class="row g-2 align-items-end"
                  onsubmit="return confirm('Xác nhận gán Staff cho yêu cầu này?');">
                <input type="hidden" name="action" value="assign"/>
                <input type="hidden" name="id" value="${requestDetail.requestId}"/>

                <div class="col-md-6">
                    <label for="staffId" class="form-label small mb-1">
                        Staff <span class="text-danger">*</span>
                    </label>
                    <select class="form-select form-select-sm" id="staffId" name="staffId" required>
                        <option value="">-- Chọn Staff --</option>
                        <c:forEach var="s" items="${staffList}">
                            <option value="${s.userId}">
                                <c:out value="${s.fullName}"/>
                                <c:if test="${not empty s.department}">
                                    · <c:out value="${s.department}"/>
                                </c:if>
                                (<c:out value="${s.username}"/>)
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${empty staffList}">
                        <div class="form-text text-danger">Không có Staff active để gán.</div>
                    </c:if>
                </div>

                <div class="col-md-4">
                    <label for="assignNote" class="form-label small mb-1">Ghi chú (tuỳ chọn)</label>
                    <input type="text"
                           class="form-control form-control-sm"
                           id="assignNote"
                           name="note"
                           maxlength="500"
                           placeholder="VD: Ưu tiên xử lý trong ngày"/>
                </div>

                <div class="col-md-2">
                    <button type="submit"
                            class="btn btn-sm btn-primary w-100"
                            ${empty staffList ? 'disabled' : ''}>
                        <i class="bi bi-check2 me-1"></i> Assign
                    </button>
                </div>
            </form>
        </div>
    </div>
</c:if>

<c:if test="${sessionScope.currentUser.role == 'STAFF'
              && requestDetail.assignedTo == sessionScope.currentUser.userId
              && (requestDetail.status == 'ASSIGNED' || requestDetail.status == 'IN_PROGRESS')}">
    <div class="card border-0 shadow-sm mb-3 border-primary border-opacity-25">
        <div class="card-body">
            <h3 class="h6 mb-3">
                <i class="bi bi-arrow-repeat me-1"></i> Cập nhật tiến độ
            </h3>
            <form method="post"
                  action="${pageContext.request.contextPath}/request"
                  class="row g-2"
                  onsubmit="return confirm('Xác nhận cập nhật tiến độ?');">
                <input type="hidden" name="action" value="update-progress"/>
                <input type="hidden" name="id" value="${requestDetail.requestId}"/>

                <div class="col-md-4">
                    <label for="newStatus" class="form-label small mb-1">
                        Trạng thái mới <span class="text-danger">*</span>
                    </label>
                    <select class="form-select form-select-sm" id="newStatus" name="newStatus" required>
                        <option value="">-- Chọn --</option>
                        <c:if test="${requestDetail.status == 'ASSIGNED'}">
                            <option value="IN_PROGRESS">IN_PROGRESS – Đang xử lý</option>
                            <option value="COMPLETED">COMPLETED – Hoàn thành</option>
                        </c:if>
                        <c:if test="${requestDetail.status == 'IN_PROGRESS'}">
                            <option value="IN_PROGRESS">IN_PROGRESS – Cập nhật ghi chú</option>
                            <option value="COMPLETED">COMPLETED – Hoàn thành</option>
                        </c:if>
                    </select>
                    <div class="form-text">
                        Hiện tại: <strong><c:out value="${requestDetail.status}"/></strong>
                    </div>
                </div>

                <div class="col-md-6">
                    <label for="progressNote" class="form-label small mb-1">
                        Ghi chú tiến độ <span class="text-danger">*</span>
                    </label>
                    <textarea class="form-control form-control-sm"
                              id="progressNote"
                              name="note"
                              rows="2"
                              maxlength="500"
                              required
                              placeholder="Mô tả công việc đã làm / tiến độ hiện tại"></textarea>
                </div>

                <div class="col-md-2 d-flex align-items-end">
                    <button type="submit" class="btn btn-sm btn-primary w-100">
                        <i class="bi bi-save me-1"></i> Cập nhật
                    </button>
                </div>
            </form>
        </div>
    </div>

    <div class="card border-0 shadow-sm mb-3 border-success border-opacity-25">
        <div class="card-body">
            <h3 class="h6 mb-3 text-success">
                <i class="bi bi-check2-all me-1"></i> Hoàn thành yêu cầu
            </h3>
            <form method="post"
                  action="${pageContext.request.contextPath}/request"
                  class="row g-2 align-items-end"
                  onsubmit="return confirm('Xác nhận hoàn thành yêu cầu này?');">
                <input type="hidden" name="action" value="complete"/>
                <input type="hidden" name="id" value="${requestDetail.requestId}"/>

                <div class="col-md-9">
                    <label for="completeNote" class="form-label small mb-1">
                        Ghi chú hoàn thành <span class="text-danger">*</span>
                    </label>
                    <textarea class="form-control form-control-sm"
                              id="completeNote"
                              name="note"
                              rows="2"
                              maxlength="500"
                              required
                              placeholder="Tóm tắt kết quả / công việc đã xong"></textarea>
                </div>
                <div class="col-md-3">
                    <button type="submit" class="btn btn-sm btn-success w-100">
                        <i class="bi bi-check2-circle me-1"></i> Complete
                    </button>
                </div>
            </form>
        </div>
    </div>
</c:if>

<div class="row g-3">
    <div class="col-lg-8">
        <div class="card border-0 shadow-sm">
            <div class="card-header bg-white">
                <span class="fw-semibold">Thông tin yêu cầu</span>
            </div>
            <div class="card-body">
                <div class="row g-3">
                    <div class="col-md-6">
                        <div class="text-muted small">Resident</div>
                        <div class="fw-semibold">
                            <c:out value="${requestDetail.createdByName}"/>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="text-muted small">Apartment</div>
                        <div class="fw-semibold">
                            <c:out value="${requestDetail.apartmentCode}"/>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="text-muted small">Request Type</div>
                        <div>
                            <span class="badge text-bg-secondary">
                                <c:out value="${requestDetail.requestType}"/>
                            </span>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="text-muted small">Created Date</div>
                        <div class="fw-semibold">
                            <c:if test="${not empty requestDetail.createdAt}">
                                <t:rt value="${requestDetail.createdAt}"/>
                            </c:if>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="text-muted small">Status</div>
                        <div>
                            <c:choose>
                                <c:when test="${requestDetail.status == 'PENDING'}">
                                    <span class="badge text-bg-warning">PENDING</span>
                                </c:when>
                                <c:when test="${requestDetail.status == 'APPROVED'}">
                                    <span class="badge text-bg-info">APPROVED</span>
                                </c:when>
                                <c:when test="${requestDetail.status == 'REJECTED'}">
                                    <span class="badge text-bg-danger">REJECTED</span>
                                </c:when>
                                <c:when test="${requestDetail.status == 'ASSIGNED'}">
                                    <span class="badge text-bg-primary">ASSIGNED</span>
                                </c:when>
                                <c:when test="${requestDetail.status == 'IN_PROGRESS'}">
                                    <span class="badge text-bg-primary">IN_PROGRESS</span>
                                </c:when>
                                <c:when test="${requestDetail.status == 'COMPLETED'}">
                                    <span class="badge text-bg-success">COMPLETED</span>
                                </c:when>
                                <c:when test="${requestDetail.status == 'CANCELLED'}">
                                    <span class="badge text-bg-dark">CANCELLED</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge text-bg-light text-dark">
                                        <c:out value="${requestDetail.status}"/>
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="text-muted small">Assigned Staff</div>
                        <div class="fw-semibold">
                            <c:choose>
                                <c:when test="${not empty requestDetail.assignedToName}">
                                    <c:out value="${requestDetail.assignedToName}"/>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted">Chưa gán</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="col-12">
                        <div class="text-muted small">Description</div>
                        <div class="border rounded p-3 bg-light">
                            <c:choose>
                                <c:when test="${not empty requestDetail.description}">
                                    <c:out value="${requestDetail.description}"/>
                                </c:when>
                                <c:when test="${not empty requestDetail.title}">
                                    <c:out value="${requestDetail.title}"/>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted">Không có mô tả.</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="col-12">
                        <div class="text-muted small">Processing Note</div>
                        <div class="border rounded p-3">
                            <c:choose>
                                <c:when test="${not empty processingNote}">
                                    <c:out value="${processingNote}"/>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted">Chưa có ghi chú xử lý.</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <c:if test="${requestDetail.status == 'REJECTED' && not empty requestDetail.rejectReason}">
                        <div class="col-12">
                            <div class="text-muted small">Reject Reason</div>
                            <div class="border border-danger border-opacity-25 rounded p-3 bg-danger bg-opacity-10">
                                <c:out value="${requestDetail.rejectReason}"/>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <div class="card border-0 shadow-sm h-100">
            <div class="card-header bg-white">
                <span class="fw-semibold">Tóm tắt</span>
            </div>
            <div class="card-body">
                <dl class="row mb-0 small">
                    <dt class="col-5 text-muted">Request ID</dt>
                    <dd class="col-7">#${requestDetail.requestId}</dd>

                    <dt class="col-5 text-muted">Title</dt>
                    <dd class="col-7">
                        <c:out value="${empty requestDetail.title ? '—' : requestDetail.title}"/>
                    </dd>

                    <dt class="col-5 text-muted">Urgency</dt>
                    <dd class="col-7">
                        <c:out value="${empty requestDetail.urgency ? '—' : requestDetail.urgency}"/>
                    </dd>

                    <dt class="col-5 text-muted">Updated</dt>
                    <dd class="col-7">
                        <c:if test="${not empty requestDetail.updatedAt}">
                            <t:rt value="${requestDetail.updatedAt}"/>
                        </c:if>
                        <c:if test="${empty requestDetail.updatedAt}">—</c:if>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
</div>

<div class="card border-0 shadow-sm mt-3">
    <div class="card-header bg-white d-flex justify-content-between align-items-center">
        <span class="fw-semibold">History</span>
        <span class="badge text-bg-light text-dark">
            ${fn:length(historyList)} bản ghi
        </span>
    </div>
    <div class="card-body p-0">
        <c:choose>
            <c:when test="${empty historyList}">
                <div class="text-center text-muted py-4">
                    <i class="bi bi-clock-history me-1"></i> Chưa có lịch sử xử lý.
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table table-sm table-hover align-middle mb-0">
                        <thead class="table-light">
                        <tr>
                            <th scope="col">#</th>
                            <th scope="col">Thời gian</th>
                            <th scope="col">Người thực hiện</th>
                            <th scope="col">Trạng thái cũ</th>
                            <th scope="col">Trạng thái mới</th>
                            <th scope="col">Ghi chú</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="h" items="${historyList}" varStatus="st">
                            <tr>
                                <td>${st.count}</td>
                                <td>
                                    <c:if test="${not empty h.createdAt}">
                                        <t:rt value="${h.createdAt}" mode="history"/>
                                    </c:if>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty h.changedByName}">
                                            <c:out value="${h.changedByName}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty h.oldStatus}">
                                            <span class="badge text-bg-light text-dark">
                                                <c:out value="${h.oldStatus}"/>
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty h.newStatus}">
                                            <span class="badge text-bg-secondary">
                                                <c:out value="${h.newStatus}"/>
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty h.note}">
                                            <c:out value="${h.note}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="/WEB-INF/views/request/_comments.jsp" %>
