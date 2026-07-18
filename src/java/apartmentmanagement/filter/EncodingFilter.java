package apartmentmanagement.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Bộ lọc đặt charset UTF-8 cho request/response trên mọi URL.
 * <p>
 * Đảm bảo form tiếng Việt và nội dung Unicode được đọc/ghi đúng
 * trước khi controller hoặc JSP xử lý.
 * </p>
 */
@WebFilter("/*")
public class EncodingFilter implements Filter {

    /**
     * Gán encoding UTF-8 rồi chuyển request xuống chuỗi filter tiếp theo.
     *
     * @param request  servlet request
     * @param response servlet response
     * @param chain    chuỗi filter tiếp theo
     * @throws IOException      lỗi I/O khi chuyển tiếp
     * @throws ServletException lỗi servlet khi chuyển tiếp
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // setCharacterEncoding phải chạy trước khi đọc parameter hoặc ghi body
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
