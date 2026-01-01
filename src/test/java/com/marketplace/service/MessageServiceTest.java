package com.marketplace.service;

import com.marketplace.db.DBUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MessageServiceTest {
    private final MessageService svc = new MessageService();

    @BeforeEach
    public void setup() throws SQLException {
        DBUtil.clearAllData();
        DBUtil.seedSampleData();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        DBUtil.clearAllData();
    }

    @Test
    public void testSendToMerchant_and_getMessagesFor() throws SQLException {
        svc.sendToMerchant("m1", "订单已创建: 订单号 12345678");
        List<String> msgs = svc.getMessagesFor("m1");
        assertFalse(msgs.isEmpty());
        assertTrue(msgs.get(0).contains("from:system"));
    }

    @Test
    public void testNotifyUser_and_getMessagesFor() throws SQLException {
        svc.notifyUser("u-100", "欢迎使用，验证码 9999");
        List<String> msgs = svc.getMessagesFor("u-100");
        assertEquals(1, msgs.size());
        assertTrue(msgs.get(0).contains("system"));
    }

    @Test
    public void testSendMessagePublic_and_getConversation() throws SQLException {
        svc.sendMessagePublic("uA", "m1", "hello 10086");
        svc.sendMessagePublic("m1", "uA", "回复 10086 ok");
        List<String> conv = svc.getConversation("uA", "m1");
        assertEquals(2, conv.size());
        assertTrue(conv.get(0).startsWith("uA -> m1"));
        assertTrue(conv.get(1).startsWith("m1 -> uA"));
    }

    @Test
    public void testGetSentMessages_masks_numbers() throws SQLException {
        svc.sendMessagePublic("uX", "mX", "手机号：13800138000，订单：888888");
        List<String> sent = svc.getSentMessages("uX");
        assertEquals(1, sent.size());
        String out = sent.get(0);
        assertFalse(out.contains("13800138000"));
        assertTrue(out.contains("****"));
    }

    @Test
    public void testGetUnreadCount_initial_and_after_read() throws SQLException {
        svc.sendMessagePublic("uR", "mR", "请查看 2000");
        int unread = svc.getUnreadCount("mR");
        assertEquals(1, unread);
        // fetching conversation marks messages as read for receiver
        svc.getConversation("mR", "uR");
        int unread2 = svc.getUnreadCount("mR");
        assertEquals(0, unread2);
    }

    @Test
    public void testMasking_various_number_positions() throws SQLException {
        svc.sendMessagePublic("a", "b", "起始123456789末尾");
        svc.sendMessagePublic("a", "b", "12短不掩码");
        svc.sendMessagePublic("a", "b", "前12中3456789后");
        List<String> msgs = svc.getMessagesFor("b");
        assertEquals(3, msgs.size());
        boolean foundMasked = false;
        for (String s : msgs) {
            if (s.contains("****")) foundMasked = true;
        }
        assertTrue(foundMasked);
    }

    @Test
    public void testSendMultipleMessages_ordering() throws SQLException, InterruptedException {
        svc.sendMessagePublic("s", "r", "first 1000");
        Thread.sleep(5);
        svc.sendMessagePublic("s", "r", "second 2000");
        List<String> msgs = svc.getMessagesFor("r");
        assertEquals(2, msgs.size());
        // getMessagesFor returns ORDER BY timestamp DESC
        assertTrue(msgs.get(0).contains("second"));
    }

    @Test
    public void testGetSentMessages_format() throws SQLException {
        svc.sendMessagePublic("sender1", "recv1", "内容 abc12345xyz");
        List<String> s = svc.getSentMessages("sender1");
        assertEquals(1, s.size());
        assertTrue(s.get(0).contains("to:recv1 -"));
    }

    @Test
    public void testMultipleReceivers_and_unreadCounts() throws SQLException {
        svc.sendMessagePublic("x", "ra", "a 11111");
        svc.sendMessagePublic("x", "rb", "b 22222");
        assertEquals(1, svc.getUnreadCount("ra"));
        assertEquals(1, svc.getUnreadCount("rb"));
    }

    @Test
    public void testGetMessagesFor_prefix_shows_unread_marker() throws SQLException {
        svc.sendMessagePublic("s1", "r1", "test 4444");
        List<String> msgs = svc.getMessagesFor("r1");
        assertTrue(msgs.get(0).contains("[未读]"));
    }
}
