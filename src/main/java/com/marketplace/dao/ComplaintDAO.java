package com.marketplace.dao;

import com.marketplace.db.DBUtil;
import com.marketplace.models.Complaint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 投诉 DAO：持久化 complaints 表，支持提交与列出投诉。
 */
public class ComplaintDAO {

    public void save(Complaint c) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO complaints (id, user_id, target_id, type, status) VALUES (?, ?, ?, ?, ?)") ) {
            ps.setString(1, c.getComplaintId());
            ps.setString(2, c.getUserId());
            ps.setString(3, c.getTargetId());
            ps.setString(4, c.getType().name());
            ps.setString(5, c.getStatus().name());
            ps.executeUpdate();
        }
    }

    public List<String> listOpenComplaints() throws SQLException {
        List<String> res = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, user_id, target_id, type, status FROM complaints WHERE status = 'OPEN' ORDER BY id DESC")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.add(rs.getString("id") + " | user:" + rs.getString("user_id") + " | target:" + rs.getString("target_id") + " | type:" + rs.getString("type") + " | status:" + rs.getString("status"));
                }
            }
        }
        return res;
    }
}
