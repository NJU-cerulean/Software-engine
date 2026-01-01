package com.marketplace.models;

import java.util.HashSet;
import java.util.Set;

public class BanClient {
    private final Set<String> banPhoneList = new HashSet<>();

    public void banPhone(String phone) { banPhoneList.add(phone); }
    public void unbanPhone(String phone) { banPhoneList.remove(phone); }
    public boolean isBanned(String phone) { return banPhoneList.contains(phone); }
}
