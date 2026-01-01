package com.marketplace.service;

import com.marketplace.db.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

/**
 * 消息服务：将通知写入 messages 表，模拟推送功能。
 */
public class MessageService {

    /**
     * 发送通知给商家（将消息写入 messages 表）
     */
    public void sendToMerchant(String merchantId, String content) throws SQLException {
        sendMessage("system", merchantId, content);
    }

    /**
     * 发送通知给用户（将消息写入 messages 表）
     */
    public void notifyUser(String userId, String content) throws SQLException {
        sendMessage("system", userId, content);
    }

    /**
     * 发送任意双方消息（用户 <-> 商家）并持久化
     */
    public void sendMessagePublic(String senderId, String receiverId, String content) throws SQLException {
        sendMessage(senderId, receiverId, content);
    }

    /**
     * 查询接收者的消息列表（按时间）
     */
    public java.util.List<String> getMessagesFor(String receiverId) throws SQLException {
        return getMessagesForInternal(receiverId, true);
    }

    /**
     * 查询发送者发出的消息列表（按时间倒序）
     */
    public java.util.List<String> getSentMessages(String senderId) throws SQLException {
        java.util.List<String> res = new java.util.ArrayList<>();
        String sql = "SELECT id, receiver_id, content, timestamp FROM messages WHERE sender_id = ? ORDER BY timestamp DESC";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, senderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String receiver = rs.getString("receiver_id");
                    String content = maskSensitiveNumbers(rs.getString("content"));
                    res.add(String.format("%s | to:%s - %s", id, receiver, content));
                }
            }
            return res;
        }
    }

    // 内部实现：允许在初次失败时自动尝试补充 is_read 列（兼容老数据库）
    private java.util.List<String> getMessagesForInternal(String receiverId, boolean allowRepair) throws SQLException {
        java.util.List<String> res = new java.util.ArrayList<>();
        String sql = "SELECT id, sender_id, content, timestamp, is_read FROM messages WHERE receiver_id = ? ORDER BY timestamp DESC";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, receiverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String sender = rs.getString("sender_id");
                    String content = maskSensitiveNumbers(rs.getString("content"));
                    String prefix = rs.getInt("is_read") == 0 ? "[未读] " : "";
                    res.add(String.format("%s | %sfrom:%s - %s", id, prefix, sender, content));
                }
            }
            return res;
        } catch (SQLException e) {
            // 如果是旧数据库缺少 is_read 列，则尝试修复并重试一次
            if (allowRepair && e.getMessage() != null && e.getMessage().contains("no such column") && e.getMessage().contains("is_read")) {
                try (Connection c2 = DBUtil.getConnection(); java.sql.Statement st = c2.createStatement()) {
                    st.executeUpdate("ALTER TABLE messages ADD COLUMN is_read INTEGER DEFAULT 0");
                } catch (SQLException ignore) { /* 若无法补救，则继续抛原异常 */ }
                return getMessagesForInternal(receiverId, false);
            }
            throw e;
        }
    }

    /**
     * 获取某用户的未读消息数量
     */
    public int getUnreadCount(String receiverId) throws SQLException {
        String sql = "SELECT COUNT(1) FROM messages WHERE receiver_id = ? AND is_read = 0";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, receiverId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
            return 0;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("no such column") && e.getMessage().contains("is_read")) {
                // 尝试修复表结构然后重试一次
                try (Connection c2 = DBUtil.getConnection(); java.sql.Statement st = c2.createStatement()) {
                    st.executeUpdate("ALTER TABLE messages ADD COLUMN is_read INTEGER DEFAULT 0");
                } catch (SQLException ignore) { }
                try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, receiverId);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
                    return 0;
                }
            }
            throw e;
        }
    }

    /**
     * 获取两者之间的完整会话（按时间升序），并将当前接收者的未读消息标为已读。
     */
    public java.util.List<String> getConversation(String me, String other) throws SQLException {
        java.util.List<String> res = new java.util.ArrayList<>();
        String sql = "SELECT id, sender_id, receiver_id, content, timestamp FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY timestamp ASC";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, me);
            ps.setString(2, other);
            ps.setString(3, other);
            ps.setString(4, me);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String sender = rs.getString("sender_id");
                    String receiver = rs.getString("receiver_id");
                    String content = maskSensitiveNumbers(rs.getString("content"));
                    res.add(sender + " -> " + receiver + ": " + content);
                    // 若当前用户为接收者且消息为未读（若列不存在则无法判断），则尝试标记为已读
                    if (receiver.equals(me)) {
                        try (PreparedStatement up = c.prepareStatement("UPDATE messages SET is_read = 1 WHERE id = ?")) {
                            up.setString(1, id);
                            up.executeUpdate();
                        } catch (SQLException ignore) { /* 若没有 is_read 列，则忽略 */ }
                    }
                }
            }
            return res;
        } catch (SQLException e) {
            // 若由于缺失 is_read 导致错误，则尝试补救并重试一次（兼容老数据库）
            if (e.getMessage() != null && e.getMessage().contains("no such column") && e.getMessage().contains("is_read")) {
                try (Connection c2 = DBUtil.getConnection(); java.sql.Statement st = c2.createStatement()) {
                    st.executeUpdate("ALTER TABLE messages ADD COLUMN is_read INTEGER DEFAULT 0");
                } catch (SQLException ignore) { }
                // 重试一次
                return getConversation(me, other);
            }
            throw e;
        }
    }

    /**
     * 以受控方式发送购买后联系方式交换（只发送掩码形式）
     */
    public void sendContactExchange(String userPhone, String merchantPhone) throws SQLException {
        String userMasked = maskPhone(userPhone);
        String merchantMasked = maskPhone(merchantPhone);

        // 故意引入一个潜在的空指针解引用缺陷，方便 JBMC 检测
        // if (userMasked.isEmpty()) {
        //     userMasked = null;
        // }
        // int debugLen = userMasked.length();
        // sendMessage("system", merchantPhone, "有新订单，买家联系方式(掩码): " + userMasked);
        // sendMessage("system", userPhone, "订单已创建，商家联系方式(掩码): " + merchantMasked);
    }

    // 简单掩码：保留前三位与末尾两位，其余用星号替代；若长度太短则整体用星号
    private String maskPhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() <= 4) return "****";
        if (digits.length() <= 6) return digits.substring(0, 1) + "****" + digits.substring(digits.length()-1);
        String start = digits.substring(0, Math.min(3, digits.length()));
        String end = digits.substring(Math.max(digits.length()-2, 0));
        return start + "****" + end;
    }

    // 掩码任意较长数字串（用于消息内容的安全展示）
    private String maskSensitiveNumbers(String text) {
        if (text == null) return null;
        // 只掩码纯数字序列（长度至少 5）的中间部分，避免误掩码包含字母或短序列（如 productId）
        return text.replaceAll("\\b(\\d{3})\\d+(\\d{2})\\b", "$1****$2");
    }

    private void sendMessage(String senderId, String receiverId, String content) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO messages (id, sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, ?, ?)") ) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, senderId);
            ps.setString(3, receiverId);
            ps.setString(4, content);
            ps.setString(5, String.valueOf(Instant.now().toEpochMilli()));
            ps.executeUpdate();
        }
    }
}
