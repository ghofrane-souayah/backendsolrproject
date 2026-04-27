package com.example.usermangment.service;

import com.example.usermangment.model.Report;
import com.example.usermangment.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final JdbcTemplate jdbcTemplate;

    public ReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Report> rowMapper = (rs, rowNum) ->
            new Report(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getLong("company_id"),
                    rs.getObject("solr_instance_id", Long.class),
                    rs.getString("content"),
                    rs.getTimestamp("created_at")
            );

    public List<Report> findAll() {
        String sql = "SELECT * FROM reports ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Report> findAllForUser(User currentUser) {
        boolean isSuperAdmin = currentUser.getRole().name().equals("SUPER_ADMIN");

        if (isSuperAdmin) {
            String sql = "SELECT * FROM reports ORDER BY created_at DESC";
            return jdbcTemplate.query(sql, rowMapper);
        }

        Long companyId = currentUser.getCompany().getId();
        String sql = "SELECT * FROM reports WHERE company_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, companyId);
    }

    public int create(Report item) {
        String sql = """
            INSERT INTO reports (name, type, company_id, solr_instance_id, content)
            VALUES (?, ?, ?, ?, ?)
        """;

        return jdbcTemplate.update(
                sql,
                item.getName(),
                item.getType(),
                item.getCompanyId(),
                item.getSolrInstanceId(),
                item.getContent()
        );
    }

    public int delete(Long id) {
        String sql = "DELETE FROM reports WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}