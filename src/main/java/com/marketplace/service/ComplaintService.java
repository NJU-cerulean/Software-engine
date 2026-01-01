package com.marketplace.service;

import com.marketplace.dao.ComplaintDAO;
import com.marketplace.models.Complaint;
import com.marketplace.models.Enums;

import java.sql.SQLException;
import java.util.UUID;

/**
 * 投诉服务：提供更高层的提交投诉接口（供UI/控制台调用）
 */
public class ComplaintService {
    private final ComplaintDAO dao = new ComplaintDAO();

   
    public boolean submitComplaint(String userId, String targetId, Enums.ComplaintType type) {
        try {
            dao.save(new Complaint(UUID.randomUUID().toString(), userId, targetId, type));
            return true;
        } catch (SQLException e) {
            // 简化：不在此打印堆栈，调用者可决定如何处理或记录
            return false;
        }
    }

    public java.util.List<String> listOpenComplaints() throws SQLException {
        return dao.listOpenComplaints();
    }
}
