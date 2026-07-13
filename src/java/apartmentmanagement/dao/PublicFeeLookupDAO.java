package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.MonthlyFee;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tra cứu phí công khai (không đăng nhập).
 * Chỉ trả phí đã công bố / đã TT (không lộ DRAFT).
 * Xác minh bằng mã căn hộ + SĐT gắn với căn (user cư dân hoặc thành viên hộ).
 */
public class PublicFeeLookupDAO extends DBContext {

    public MonthlyFee getFromResultSet(java.sql.ResultSet rs) throws SQLException {
        return MonthlyFee.builder()
                .feeId(rs.getInt("fee_id"))
                .apartmentId(rs.getInt("apartment_id"))
                .feeMonth((int) rs.getByte("fee_month"))
                .feeYear((int) rs.getShort("fee_year"))
                .serviceFee(rs.getBigDecimal("service_fee"))
                .waterFee(rs.getBigDecimal("water_fee"))
                .parkingFee(rs.getBigDecimal("parking_fee"))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .status(rs.getString("status"))
                .note(rs.getString("note"))
                .publishedAt(rs.getTimestamp("published_at"))
                .paidAt(rs.getTimestamp("paid_at"))
                .apartmentCode(rs.getString("apartment_code"))
                .build();
    }

    /**
     * Kiểm tra mã căn ACTIVE + SĐT khớp cư dân hiện tại hoặc thành viên hộ.
     */
    public boolean verifyApartmentContact(String apartmentCode, String phone) {
        String sql = """
                SELECT TOP 1 a.apartment_id
                FROM apartments a
                WHERE a.apartment_code = ?
                  AND a.status = N'ACTIVE'
                  AND (
                    EXISTS (
                        SELECT 1
                        FROM apartment_residents ar
                        INNER JOIN users u ON u.user_id = ar.user_id
                        WHERE ar.apartment_id = a.apartment_id
                          AND ar.is_current = 1
                          AND u.is_active = 1
                          AND u.phone = ?
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM household_members hm
                        WHERE hm.apartment_id = a.apartment_id
                          AND hm.is_active = 1
                          AND hm.phone = ?
                    )
                  )
                """;
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, apartmentCode);
            statement.setString(2, phone);
            statement.setString(3, phone);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("PublicFeeLookupDAO.verifyApartmentContact error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Danh sách phí đã công bố của căn (PUBLISHED / UNPAID / PAID), mới nhất trước.
     * Giới hạn 12 kỳ (TOP cố định — tránh tham số TOP trên JDBC).
     */
    public List<MonthlyFee> findPublishedFees(String apartmentCode, int limit) {
        List<MonthlyFee> list = new ArrayList<>();
        String sql = """
                SELECT TOP 12
                    mf.fee_id, mf.apartment_id, mf.fee_month, mf.fee_year,
                    mf.service_fee, mf.water_fee, mf.parking_fee, mf.total_amount,
                    mf.status, mf.note, mf.published_at, mf.paid_at,
                    a.apartment_code
                FROM monthly_fees mf
                INNER JOIN apartments a ON a.apartment_id = mf.apartment_id
                WHERE a.apartment_code = ?
                  AND a.status = N'ACTIVE'
                  AND mf.status IN (N'PUBLISHED', N'UNPAID', N'PAID')
                ORDER BY mf.fee_year DESC, mf.fee_month DESC
                """;
        try {
            connection = getConnection();
            if (connection == null) {
                return list;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, apartmentCode);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
                if (limit > 0 && list.size() >= limit) {
                    break;
                }
            }
        } catch (SQLException e) {
            System.out.println("PublicFeeLookupDAO.findPublishedFees error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    /** Helper format tiền (VND) cho view nếu cần từ controller. */
    public static BigDecimal safeAmount(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
