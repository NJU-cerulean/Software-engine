package com.marketplace.models;

import java.util.List;

public interface Client {
    int getWeChatAssociation();
    int getQQAssociation();
    int getAppleAssociation();
    int getGoogleAssociation();
    String getPhone();
    List<Message> getMymessage();

    void register();
    void reportToAdmin(String content);
    boolean login(String password);
    void logout();
}
