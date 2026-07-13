<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tra cứu phí dịch vụ | Skyland Apartment</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        html, body { margin: 0; padding: 0; width: 100%; overflow-x: hidden; }
        body {
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
            background-color: #f8f9fa;
            color: #212529;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        .text-primary-custom { color: #1a4388 !important; }
        .bg-primary-custom { background-color: #1a4388 !important; }
        .btn-primary-custom {
            background-color: #1a4388;
            border-color: #1a4388;
            color: #fff;
        }
        .btn-primary-custom:hover,
        .btn-primary-custom:focus {
            background-color: #123063;
            border-color: #123063;
            color: #fff;
        }
        .btn-outline-primary-custom {
            color: #1a4388;
            border-color: #1a4388;
            font-weight: 500;
        }
        .btn-outline-primary-custom:hover {
            background-color: #1a4388;
            color: #fff;
        }
        .navbar {
            width: 100%;
            box-shadow: 0 2px 15px rgba(0,0,0,0.08);
            background: #fff !important;
        }
        .lookup-hero {
            background: linear-gradient(135deg, #1a4388 0%, #2563a8 55%, #3b82c4 100%);
            color: #fff;
            padding: 2.5rem 0 2rem;
        }
        .lookup-hero h1 {
            font-weight: 700;
            font-size: 1.85rem;
            margin-bottom: 0.5rem;
        }
        .lookup-main { flex: 1; padding: 2rem 0 3rem; margin-top: -1.25rem; }
        .lookup-card {
            background: #fff;
            border-radius: 16px;
            box-shadow: 0 12px 32px rgba(0,0,0,0.1);
            padding: 1.75rem;
            height: 100%;
        }
        .lookup-card h2 {
            font-size: 1.15rem;
            font-weight: 700;
            color: #1e293b;
            margin-bottom: 0.35rem;
        }
        .price-item {
            display: flex;
            justify-content: space-between;
            gap: 1rem;
            padding: 0.75rem 0;
            border-bottom: 1px dashed #e5e7eb;
        }
        .price-item:last-child { border-bottom: 0; }
        .price-item strong { color: #1a4388; white-space: nowrap; }
        .note-box {
            background: #f1f5f9;
            border-radius: 12px;
            padding: 0.9rem 1rem;
            font-size: 0.9rem;
            color: #475569;
        }
        .footer {
            width: 100%;
            background-color: #111827;
            color: #9ca3af;
            padding: 40px 0 24px;
            margin-top: auto;
        }
        .footer a { color: #9ca3af; text-decoration: none; }
        .footer a:hover { color: #fff; }
        .badge-status {
            font-weight: 600;
            letter-spacing: 0.02em;
        }
        @media (max-width: 767.98px) {
            .lookup-card { padding: 1.25rem; }
            .lookup-hero h1 { font-size: 1.5rem; }
        }
    </style>
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-light bg-white sticky-top py-2">
    <div class="container">
        <a class="navbar-brand text-primary-custom fw-bold fs-4 d-flex align-items-center"
           href="${pageContext.request.contextPath}/auth?action=login">
            <i class="bi bi-buildings-fill fs-3 me-2"></i> Skyland Apartment
        </a>
        <div class="ms-auto d-flex gap-2">
            <a class="btn btn-outline-primary-custom px-3 py-2 rounded-pill"
               href="${pageContext.request.contextPath}/auth?action=login">
                Đăng nhập
            </a>
        </div>
    </div>
</nav>

<section class="lookup-hero">
    <div class="container">
        <p class="mb-2 opacity-75 small">
            <i class="bi bi-cash-coin me-1"></i> Dịch vụ công khai
        </p>
        <h1>Tra cứu phí dịch vụ</h1>
        <p class="mb-0 opacity-90" style="max-width: 40rem;">
            Xem bảng giá tham khảo và tra cứu hóa đơn phí tháng đã công bố theo mã căn hộ.
            Theo Điều khoản dịch vụ, thông tin phí mang tính quản lý/tham khảo; thanh toán thực tế theo hướng dẫn Ban Quản Lý.
        </p>
    </div>
</section>

<main class="lookup-main">
    <div class="container">
        <div class="row g-4">

            <%-- Bảng giá tham khảo (public, không cần PII) --%>
            <div class="col-lg-5">
                <div class="lookup-card">
                    <div class="d-flex align-items-center gap-2 mb-3">
                        <span class="bg-primary-custom text-white rounded-circle d-inline-flex align-items-center justify-content-center"
                              style="width:42px;height:42px;">
                            <i class="bi bi-tags"></i>
                        </span>
                        <div>
                            <h2 class="mb-0">Bảng giá tham khảo</h2>
                            <small class="text-muted">Áp dụng chung cư Skyland (demo 2026)</small>
                        </div>
                    </div>

                    <div class="price-item">
                        <span><i class="bi bi-building me-1 text-primary-custom"></i> Phí quản lý / dịch vụ</span>
                        <strong>800.000 – 1.000.000đ / tháng</strong>
                    </div>
                    <div class="price-item">
                        <span><i class="bi bi-droplet me-1 text-primary-custom"></i> Phí nước (ước tính)</span>
                        <strong>120.000 – 200.000đ / tháng</strong>
                    </div>
                    <div class="price-item">
                        <span><i class="bi bi-p-circle me-1 text-primary-custom"></i> Phí gửi xe</span>
                        <strong>100.000 – 200.000đ / tháng</strong>
                    </div>
                    <div class="price-item">
                        <span><i class="bi bi-lightning-charge me-1 text-primary-custom"></i> Điện sinh hoạt</span>
                        <strong>Theo hóa đơn EVN</strong>
                    </div>

                    <div class="note-box mt-3">
                        <i class="bi bi-info-circle me-1"></i>
                        Mức phí thực tế theo diện tích, số xe và chỉ số tiêu thụ từng căn.
                        Chỉ hiển thị hóa đơn đã <strong>công bố</strong> (không hiện bản nháp DRAFT).
                    </div>

                    <div class="mt-3 small text-muted">
                        Xem thêm:
                        <a class="text-primary-custom" href="${pageContext.request.contextPath}/auth?action=terms">Điều khoản dịch vụ</a>
                        ·
                        <a class="text-primary-custom" href="${pageContext.request.contextPath}/auth?action=privacy">Chính sách bảo mật</a>
                    </div>
                </div>
            </div>

            <%-- Form tra cứu có xác minh --%>
            <div class="col-lg-7">
                <div class="lookup-card">
                    <div class="d-flex align-items-center gap-2 mb-3">
                        <span class="bg-primary-custom text-white rounded-circle d-inline-flex align-items-center justify-content-center"
                              style="width:42px;height:42px;">
                            <i class="bi bi-search"></i>
                        </span>
                        <div>
                            <h2 class="mb-0">Tra cứu hóa đơn phí tháng</h2>
                            <small class="text-muted">Cần mã căn hộ + SĐT đã đăng ký với BQL</small>
                        </div>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger border-0 rounded-3 d-flex gap-2" role="alert">
                            <i class="bi bi-exclamation-triangle-fill mt-1"></i>
                            <div>${error}</div>
                        </div>
                    </c:if>
                    <c:if test="${not empty info}">
                        <div class="alert alert-info border-0 rounded-3 d-flex gap-2" role="alert">
                            <i class="bi bi-info-circle-fill mt-1"></i>
                            <div>${info}</div>
                        </div>
                    </c:if>

                    <form method="get" action="${pageContext.request.contextPath}/auth" class="row g-3">
                        <input type="hidden" name="action" value="fee-lookup">
                        <div class="col-md-6">
                            <label class="form-label fw-medium" for="apartmentCode">Mã căn hộ</label>
                            <div class="input-group">
                                <span class="input-group-text bg-white"><i class="bi bi-door-open"></i></span>
                                <input type="text" class="form-control" id="apartmentCode" name="apartmentCode"
                                       placeholder="VD: A-0801" required
                                       value="${apartmentCode}" autocomplete="off">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-medium" for="phone">Số điện thoại đăng ký</label>
                            <div class="input-group">
                                <span class="input-group-text bg-white"><i class="bi bi-telephone"></i></span>
                                <input type="text" class="form-control" id="phone" name="phone"
                                       placeholder="VD: 0902000001" required
                                       value="${phone}" autocomplete="tel">
                            </div>
                        </div>
                        <div class="col-12 d-flex flex-wrap gap-2">
                            <button type="submit" class="btn btn-primary-custom px-4 rounded-pill">
                                <i class="bi bi-search me-1"></i> Tra cứu
                            </button>
                            <a class="btn btn-outline-secondary rounded-pill px-3"
                               href="${pageContext.request.contextPath}/auth?action=fee-lookup">
                                Xóa kết quả
                            </a>
                        </div>
                    </form>

                    <div class="note-box mt-3 mb-0">
                        <strong>Bảo mật:</strong> Chỉ tra cứu được khi SĐT trùng cư dân/thành viên hộ của căn.
                        Không hiển thị danh sách toàn bộ căn hộ hay phí nháp. Chi tiết đầy đủ sau khi
                        <a class="text-primary-custom" href="${pageContext.request.contextPath}/auth?action=login">đăng nhập</a>.
                    </div>
                </div>
            </div>
        </div>

        <%-- Kết quả --%>
        <c:if test="${searched}">
            <div class="row mt-4">
                <div class="col-12">
                    <div class="lookup-card">
                        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                            <h2 class="mb-0">
                                Kết quả · căn <span class="text-primary-custom">${apartmentCode}</span>
                            </h2>
                            <span class="small text-muted">Tối đa 12 kỳ gần nhất đã công bố</span>
                        </div>

                        <c:choose>
                            <c:when test="${empty fees}">
                                <div class="text-muted py-3">
                                    <i class="bi bi-inbox me-1"></i>
                                    Không có hóa đơn phí đã công bố cho căn này.
                                    Liên hệ BQL nếu bạn cho rằng đây là thiếu sót.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-hover align-middle mb-0">
                                        <thead class="table-light">
                                        <tr>
                                            <th>Kỳ</th>
                                            <th class="text-end">Phí DV</th>
                                            <th class="text-end">Nước</th>
                                            <th class="text-end">Gửi xe</th>
                                            <th class="text-end">Tổng</th>
                                            <th>Trạng thái</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="f" items="${fees}">
                                            <tr>
                                                <td class="fw-semibold">
                                                    <fmt:formatNumber value="${f.feeMonth}" minIntegerDigits="2"/>/${f.feeYear}
                                                </td>
                                                <td class="text-end">
                                                    <fmt:formatNumber value="${f.serviceFee}" type="number" maxFractionDigits="0"/>đ
                                                </td>
                                                <td class="text-end">
                                                    <fmt:formatNumber value="${f.waterFee}" type="number" maxFractionDigits="0"/>đ
                                                </td>
                                                <td class="text-end">
                                                    <fmt:formatNumber value="${f.parkingFee}" type="number" maxFractionDigits="0"/>đ
                                                </td>
                                                <td class="text-end fw-bold text-primary-custom">
                                                    <fmt:formatNumber value="${f.totalAmount}" type="number" maxFractionDigits="0"/>đ
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${f.status == 'PAID'}">
                                                            <span class="badge text-bg-success badge-status">Đã thanh toán</span>
                                                        </c:when>
                                                        <c:when test="${f.status == 'UNPAID' || f.status == 'PUBLISHED'}">
                                                            <span class="badge text-bg-warning badge-status">Chưa thanh toán</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge text-bg-secondary badge-status">${f.status}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                                <p class="small text-muted mt-3 mb-0">
                                    <i class="bi bi-shield-check me-1"></i>
                                    Dữ liệu chỉ mang tính tham khảo theo Điều khoản dịch vụ.
                                    Thanh toán / biên lai chính thức do Ban Quản Lý xác nhận.
                                </p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </c:if>

        <%-- Gợi ý demo --%>
        <div class="row mt-4">
            <div class="col-12">
                <div class="lookup-card py-3">
                    <div class="small text-muted mb-2">
                        <i class="bi bi-lightbulb me-1"></i>
                        <strong>Gợi ý demo seed</strong>
                    </div>
                    <div class="d-flex flex-wrap gap-2">
                        <span class="badge text-bg-light border">A-0801 + 0902000001</span>
                        <span class="badge text-bg-light border">A-0802 + 0902000003</span>
                        <span class="badge text-bg-light border">A-0901 + 0902000004</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<footer class="footer">
    <div class="container">
        <div class="row text-center text-md-start align-items-center">
            <div class="col-md-6 mb-2 mb-md-0">
                <p class="small mb-0">&copy; 2026 Skyland Apartment · Nhóm 6 SE2036</p>
            </div>
            <div class="col-md-6 text-md-end">
                <a href="${pageContext.request.contextPath}/auth?action=privacy" class="small me-3">Chính sách bảo mật</a>
                <a href="${pageContext.request.contextPath}/auth?action=terms" class="small">Điều khoản dịch vụ</a>
            </div>
        </div>
    </div>
</footer>

</body>
</html>
