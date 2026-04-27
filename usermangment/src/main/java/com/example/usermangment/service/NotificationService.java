package com.example.usermangment.service;

import com.example.usermangment.model.Notifications;
import com.example.usermangment.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final JdbcTemplate jdbcTemplate;

    public NotificationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Notifications> rowMapper = (rs, rowNum) ->
            new Notifications(
                    rs.getLong("id"),
                    rs.getString("type"),
                    rs.getString("level"),
                    rs.getString("title"),
                    rs.getString("message"),
                    rs.getString("source"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at"),
                    rs.getObject("company_id", Long.class)
            );

    public List<Notifications> findAll() {
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Notifications> findAllForUser(User currentUser) {
        boolean isSuperAdmin = currentUser.getRole().name().equals("SUPER_ADMIN");

        if (isSuperAdmin) {
            String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
            return jdbcTemplate.query(sql, rowMapper);
        }

        Long companyId = currentUser.getCompany().getId();

        String sql = "SELECT * FROM notifications WHERE company_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, companyId);
    }

    public int create(Notifications item) {
        String sql = """
            INSERT INTO notifications (type, level, title, message, source, status, company_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        return jdbcTemplate.update(
                sql,
                item.getType(),
                item.getLevel(),
                item.getTitle(),
                item.getMessage(),
                item.getSource(),
                item.getStatus(),
                item.getCompanyId()
        );
    }

    public void createSuccess(String title, String message, String source, Long companyId) {
        Notifications item = new Notifications();
        item.setType("notification");
        item.setLevel("success");
        item.setTitle(title);
        item.setMessage(message);
        item.setSource(source);
        item.setStatus("unread");
        item.setCompanyId(companyId);

        create(item);
    }

    public void createError(String title, String message, String source, Long companyId) {
        Notifications item = new Notifications();
        item.setType("notification");
        item.setLevel("critical");
        item.setTitle(title);
        item.setMessage(message);
        item.setSource(source);
        item.setStatus("unread");
        item.setCompanyId(companyId);

        create(item);
    }

    public int markAsRead(Long id) {
        String sql = "UPDATE notifications SET status = 'read' WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int markAllAsRead() {
        String sql = "UPDATE notifications SET status = 'read' WHERE status = 'unread'";
        return jdbcTemplate.update(sql);
    }

    public int delete(Long id) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
    public int deleteByCompanyId(Long companyId) {
        String sql = "DELETE FROM notifications WHERE company_id = ?";
        return jdbcTemplate.update(sql, companyId);
    }
}