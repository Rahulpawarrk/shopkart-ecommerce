package com.example.ecommerce.review.dao;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.review.model.RatingSummary;
import com.example.ecommerce.review.model.Review;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Product Reviews and Ratings in Microsoft SQL Server.
 */
public class ReviewDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReviewDAO.class);

    /**
     * Checks if the customer has purchased this product and the order was delivered.
     */
    public boolean isVerifiedPurchase(int userId, int productId) {
        String sql = "SELECT 1 FROM dbo.orders o " +
                     "INNER JOIN dbo.order_items oi ON o.order_id = oi.order_id " +
                     "WHERE o.user_id = ? AND oi.product_id = ? AND o.order_status = 'DELIVERED' LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking verified purchase for userId: {}, productId: {}", userId, productId, e);
        }
        return false;
    }

    public Optional<String> getDeliveredOrderNumber(int userId, int productId) {
        String sql = "SELECT o.order_number FROM dbo.orders o " +
                     "INNER JOIN dbo.order_items oi ON o.order_id = oi.order_id " +
                     "WHERE o.user_id = ? AND oi.product_id = ? AND o.order_status = 'DELIVERED' " +
                     "ORDER BY o.delivered_at DESC, o.created_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("order_number"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting delivered order number for userId: {}, productId: {}", userId, productId, e);
        }
        return Optional.empty();
    }

    public int createReview(Review review) {
        String sql = "INSERT INTO dbo.reviews (product_id, user_id, rating, review_title, review_text, image_url, " +
                     "status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, review.getProductId());
            stmt.setInt(2, review.getUserId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getTitle());
            stmt.setString(5, review.getComment());
            stmt.setString(6, review.getImageUrl());
            stmt.setString(7, review.isApproved() ? "APPROVED" : "PENDING");

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    review.setReviewId(id);

                    // Insert into review_images if multiple images
                    if (review.getImageUrls() != null && !review.getImageUrls().isEmpty()) {
                        saveReviewImages(id, review.getImageUrls(), conn);
                    }
                    return id;
                }
            }
            throw new SQLException("Failed to create review, no generated ID obtained.");
        } catch (SQLException e) {
            logger.error("Error creating review for productId: {}", review.getProductId(), e);
            throw new DatabaseException("Failed to submit review", e);
        }
    }

    public void updateReview(Review review) {
        String sql = "UPDATE dbo.reviews SET rating = ?, review_title = ?, review_text = ?, " +
                     "image_url = COALESCE(?, image_url), updated_at = CURRENT_TIMESTAMP " +
                     "WHERE review_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, review.getRating());
            stmt.setString(2, review.getTitle());
            stmt.setString(3, review.getComment());
            stmt.setString(4, review.getImageUrl());
            stmt.setInt(5, review.getReviewId());
            stmt.setInt(6, review.getUserId());
            stmt.executeUpdate();

            if (review.getImageUrls() != null && !review.getImageUrls().isEmpty()) {
                saveReviewImages(review.getReviewId(), review.getImageUrls(), conn);
            }
        } catch (SQLException e) {
            logger.error("Error updating review ID: {}", review.getReviewId(), e);
            throw new DatabaseException("Failed to update review", e);
        }
    }

    private void saveReviewImages(int reviewId, List<String> imageUrls, Connection conn) {
        try {
            // Delete old images before saving new batch
            String delSql = "DELETE FROM dbo.review_images WHERE review_id = ?";
            try (PreparedStatement delStmt = conn.prepareStatement(delSql)) {
                delStmt.setInt(1, reviewId);
                delStmt.executeUpdate();
            }

            String insSql = "INSERT INTO dbo.review_images (review_id, image_url, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement insStmt = conn.prepareStatement(insSql)) {
                for (String url : imageUrls) {
                    if (url != null && !url.trim().isEmpty()) {
                        insStmt.setInt(1, reviewId);
                        insStmt.setString(2, url.trim());
                        insStmt.addBatch();
                    }
                }
                insStmt.executeBatch();
            }
        } catch (SQLException e) {
            logger.warn("Could not batch insert review images for reviewId: {}", reviewId, e);
        }
    }

    private List<String> loadReviewImages(int reviewId) {
        List<String> images = new ArrayList<>();
        String sql = "SELECT image_url FROM dbo.review_images WHERE review_id = ? ORDER BY review_image_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    images.add(rs.getString("image_url"));
                }
            }
        } catch (SQLException e) {
            logger.warn("Could not load review images for reviewId: {}", reviewId, e);
        }
        return images;
    }

    public Optional<Review> findById(int reviewId) {
        String sql = "SELECT r.review_id, r.product_id, r.user_id, r.rating, r.review_title, r.review_text, r.image_url, " +
                     "r.status, r.created_at, r.updated_at, p.product_name, u.first_name, u.last_name, u.email " +
                     "FROM dbo.reviews r " +
                     "INNER JOIN dbo.products p ON r.product_id = p.product_id " +
                     "INNER JOIN dbo.users u ON r.user_id = u.user_id " +
                     "WHERE r.review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Review r = mapResultSetToReview(rs);
                    List<String> extraImages = loadReviewImages(r.getReviewId());
                    if (!extraImages.isEmpty()) {
                        r.setImageUrls(extraImages);
                    }
                    return Optional.of(r);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding review by ID: {}", reviewId, e);
            throw new DatabaseException("Error retrieving review", e);
        }
        return Optional.empty();
    }

    public Optional<Review> findByUserAndProduct(int userId, int productId) {
        String sql = "SELECT r.review_id, r.product_id, r.user_id, r.rating, r.review_title, r.review_text, r.image_url, " +
                     "r.status, r.created_at, r.updated_at, p.product_name, u.first_name, u.last_name, u.email " +
                     "FROM dbo.reviews r " +
                     "INNER JOIN dbo.products p ON r.product_id = p.product_id " +
                     "INNER JOIN dbo.users u ON r.user_id = u.user_id " +
                     "WHERE r.user_id = ? AND r.product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Review r = mapResultSetToReview(rs);
                    List<String> extraImages = loadReviewImages(r.getReviewId());
                    if (!extraImages.isEmpty()) {
                        r.setImageUrls(extraImages);
                    }
                    return Optional.of(r);
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking existing review for user: {}, product: {}", userId, productId, e);
        }
        return Optional.empty();
    }

    public Pagination<Review> findByProductId(int productId, boolean approvedOnly, int page, int pageSize) {
        String where = approvedOnly ? " WHERE r.product_id = ? AND (r.status = 'APPROVED' OR r.status IS NULL) " 
                                    : " WHERE r.product_id = ? ";

        String countSql = "SELECT COUNT(*) FROM dbo.reviews r " + where;
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            countStmt.setInt(1, productId);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting reviews for product: {}", productId, e);
            throw new DatabaseException("Error counting reviews", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        String dataSql = "SELECT r.review_id, r.product_id, r.user_id, r.rating, r.review_title, r.review_text, r.image_url, " +
                         "r.status, r.created_at, r.updated_at, p.product_name, u.first_name, u.last_name, u.email " +
                         "FROM dbo.reviews r " +
                         "INNER JOIN dbo.products p ON r.product_id = p.product_id " +
                         "INNER JOIN dbo.users u ON r.user_id = u.user_id " +
                         where +
                         " ORDER BY r.created_at DESC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Review> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, (page - 1) * pageSize);
            stmt.setInt(3, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Review r = mapResultSetToReview(rs);
                    List<String> extraImages = loadReviewImages(r.getReviewId());
                    if (!extraImages.isEmpty()) {
                        r.setImageUrls(extraImages);
                    }
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching reviews for productId: {}", productId, e);
            throw new DatabaseException("Error fetching reviews", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }

    public RatingSummary getRatingSummary(int productId) {
        RatingSummary summary = new RatingSummary();
        summary.setProductId(productId);

        String sql = "SELECT " +
                     "COALESCE(AVG(CAST(rating AS FLOAT)), 0.0) AS avg_rating, " +
                     "COUNT(*) AS total_count, " +
                     "SUM(CASE WHEN rating = 5 THEN 1 ELSE 0 END) AS count_5, " +
                     "SUM(CASE WHEN rating = 4 THEN 1 ELSE 0 END) AS count_4, " +
                     "SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END) AS count_3, " +
                     "SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END) AS count_2, " +
                     "SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) AS count_1 " +
                     "FROM dbo.reviews WHERE product_id = ? AND (status = 'APPROVED' OR status IS NULL)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble("avg_rating");
                    summary.setAverageRating(Math.round(avg * 10.0) / 10.0);
                    summary.setTotalReviews(rs.getInt("total_count"));
                    summary.setFiveStarCount(rs.getInt("count_5"));
                    summary.setFourStarCount(rs.getInt("count_4"));
                    summary.setThreeStarCount(rs.getInt("count_3"));
                    summary.setTwoStarCount(rs.getInt("count_2"));
                    summary.setOneStarCount(rs.getInt("count_1"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error generating rating summary for productId: {}", productId, e);
        }
        return summary;
    }

    public void setApprovalStatus(int reviewId, boolean approved) {
        String sql = "UPDATE dbo.reviews SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, approved ? "APPROVED" : "REJECTED");
            stmt.setInt(2, reviewId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error setting approval status for review ID: {}", reviewId, e);
            throw new DatabaseException("Failed to update review status", e);
        }
    }

    public void deleteReview(int reviewId) {
        String sql = "DELETE FROM dbo.reviews WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting review ID: {}", reviewId, e);
            throw new DatabaseException("Failed to delete review", e);
        }
    }

    public Pagination<Review> findAll(String keyword, Boolean isApproved, int page, int pageSize) {
        return findAllForAdmin(keyword, isApproved, page, pageSize);
    }

    public Pagination<Review> findAllForAdmin(String keyword, Boolean isApproved, int page, int pageSize) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append("AND (p.product_name LIKE ? OR r.review_title LIKE ? OR r.review_text LIKE ? OR u.email LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (isApproved != null) {
            where.append("AND r.status = ? ");
            params.add(isApproved ? "APPROVED" : "PENDING");
        }

        String countSql = "SELECT COUNT(*) FROM dbo.reviews r " +
                          "INNER JOIN dbo.products p ON r.product_id = p.product_id " +
                          "INNER JOIN dbo.users u ON r.user_id = u.user_id " + where;
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            int idx = 1;
            for (Object obj : params) countStmt.setObject(idx++, obj);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting admin reviews", e);
            throw new DatabaseException("Error counting reviews", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        String dataSql = "SELECT r.review_id, r.product_id, r.user_id, r.rating, r.review_title, r.review_text, r.image_url, " +
                         "r.status, r.created_at, r.updated_at, p.product_name, u.first_name, u.last_name, u.email " +
                         "FROM dbo.reviews r " +
                         "INNER JOIN dbo.products p ON r.product_id = p.product_id " +
                         "INNER JOIN dbo.users u ON r.user_id = u.user_id " +
                         where +
                         " ORDER BY r.created_at DESC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Review> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            int idx = 1;
            for (Object obj : params) stmt.setObject(idx++, obj);
            stmt.setInt(idx++, (page - 1) * pageSize);
            stmt.setInt(idx, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Review r = mapResultSetToReview(rs);
                    List<String> extraImages = loadReviewImages(r.getReviewId());
                    if (!extraImages.isEmpty()) {
                        r.setImageUrls(extraImages);
                    }
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching admin reviews", e);
            throw new DatabaseException("Error retrieving reviews", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }

    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("review_id"));
        r.setProductId(rs.getInt("product_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setRating(rs.getInt("rating"));
        r.setTitle(rs.getString("review_title"));
        r.setComment(rs.getString("review_text"));

        try {
            String img = rs.getString("image_url");
            r.setImageUrl(img);
        } catch (SQLException ignored) {}

        String status = rs.getString("status");
        r.setApproved("APPROVED".equalsIgnoreCase(status) || status == null);
        r.setVerifiedPurchase(true);
        r.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        r.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        r.setProductName(rs.getString("product_name"));

        String fn = rs.getString("first_name");
        String ln = rs.getString("last_name");
        r.setCustomerName(((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim());
        r.setCustomerEmail(rs.getString("email"));
        return r;
    }
}
