package com.marketplace.ui;

import com.marketplace.dao.ProductDAO;
import com.marketplace.dao.UserDAO;
import com.marketplace.dao.MerchantDAO;
import com.marketplace.models.Product;
import com.marketplace.service.AdminService;
import com.marketplace.service.AuthService;
import com.marketplace.service.MessageService;
import com.marketplace.service.OrderService;
import com.marketplace.service.ProductService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
// action event/listener imports removed where unused
import java.sql.SQLException;
import java.util.List;

/**
 * 改进版 Swing UI：更现代的卡片式商品展示、顶部工具栏（搜索/登录/主题切换）、管理员面板入口。
 * - 使用 NJU 紫色风格，支持浅色/深色主题切换（手动切换）。
 * - 商品卡片显示缩略、标题、价格、库存与“联系卖家”按钮（联系方式通过站内消息，不直接显示）。
 */
public class AppGUI {
    private final AuthService auth = new AuthService();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final MessageService messageService = new MessageService();
    private final AdminService adminService = new AdminService();
    private final ProductDAO productDAO = new ProductDAO();
    private final UserDAO userDAO = new UserDAO();
    private final MerchantDAO merchantDAO = new MerchantDAO();
    private JFrame frame;
    private JPanel productGrid;
    private boolean darkMode = false;
    private String currentUserPhone = null;
    private String currentMerchantPhone = null;
    private JPanel recommendPanel;
    private String currentMerchantId = null;
    private JLabel userInfoLabel;
    private JTextField searchField;
    private JButton btnSearch;
    // 主题色：RGB(85,0,75)
    private final Color THEME = new Color(85, 0, 75);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppGUI().createAndShow());
    }

    private void createAndShow() {
        frame = new JFrame("NJU Marketplace");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

    JPanel topBar = buildTopBar();
    JScrollPane centerScroll = buildCenterPanel();
    // 推荐栏（登录后显示）
    recommendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    recommendPanel.setBorder(new EmptyBorder(8,12,8,12));

    // 将搜索框移动到推荐区域的右侧，便于在浏览示例时直接搜索
    JPanel recommendWithSearch = new JPanel(new BorderLayout());
    recommendWithSearch.add(recommendPanel, BorderLayout.WEST);
    // create search controls (fields are class members so we can wire actions elsewhere)
    searchField = new JTextField(24);
    btnSearch = new JButton("搜索");
    styleButton(btnSearch);
    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    searchPanel.add(searchField);
    searchPanel.add(btnSearch);
    recommendWithSearch.add(searchPanel, BorderLayout.EAST);

    JPanel centerContainer = new JPanel(new BorderLayout());
    centerContainer.add(recommendWithSearch, BorderLayout.NORTH);
    centerContainer.add(centerScroll, BorderLayout.CENTER);

    frame.add(topBar, BorderLayout.NORTH);
    frame.add(centerContainer, BorderLayout.CENTER);

        applyTheme();
        refreshProducts();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(THEME);
        top.setBorder(new EmptyBorder(8, 12, 8, 12));

        // left: brand
    JLabel brand = new JLabel("小蓝鲸市场");
        brand.setFont(new Font("SansSerif", Font.BOLD, 18));
    brand.setForeground(Color.WHITE);

    // search moved to recommend area; top bar keeps brand + controls on right

    // right: user buttons and theme toggle
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnUser = new JButton("用户 登录/注册");
        JButton btnMerchant = new JButton("商家 登录/注册");
    JButton btnMessages = new JButton("消息");
    JButton btnOrders = new JButton("我的订单");
        JToggleButton themeToggle = new JToggleButton("深色模式");

    userInfoLabel = new JLabel("");
    userInfoLabel.setForeground(Color.WHITE);

    right.add(btnUser);
    right.add(btnMerchant);
    right.add(userInfoLabel);
    right.add(btnMessages);
        right.add(btnOrders);
        right.add(themeToggle);

        // style buttons with theme
    styleButton(btnUser);
    styleButton(btnMerchant);
        styleButton(btnMessages);
    styleButton(btnOrders);
        styleButton(themeToggle);

    top.add(brand, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        // actions
    btnUser.addActionListener(e -> showUserDialog());
    btnMerchant.addActionListener(e -> showMerchantDialog());
    btnMessages.addActionListener(e -> showMessagesDialog());
    btnOrders.addActionListener(e -> showOrdersDialog());
        // 我的优惠券入口
        JButton btnMyCoupons = new JButton("我的优惠券");
        right.add(btnMyCoupons);
        btnMyCoupons.addActionListener(e -> showMyCouponsDialog());
    // 每次展现顶部条刷新未读计数
    btnMessages.addHierarchyListener(e -> updateUnreadBadge(btnMessages));
    btnOrders.addHierarchyListener(e -> { /* placeholder if future badge needed */ });

        // wire search action (if search controls created in createAndShow)
        if (btnSearch != null) {
            btnSearch.addActionListener(e -> {
                String kw = searchField.getText();
                refreshProducts(kw);
            });
        }

        themeToggle.addActionListener(e -> {
            darkMode = themeToggle.isSelected();
            applyTheme();
        });

        return top;
    }

    private JScrollPane buildCenterPanel() {
        productGrid = new JPanel();
        productGrid.setLayout(new GridLayout(0, 3, 12, 12));
        productGrid.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane sp = new JScrollPane(productGrid);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private void refreshProducts() {
        refreshProducts(null);
    }

    private void refreshProducts(String keyword) {
        SwingUtilities.invokeLater(() -> {
            productGrid.removeAll();
            try {
                List<Product> list;
                if (keyword == null || keyword.trim().isEmpty()) list = productService.listPublished();
                else list = productService.searchProducts(keyword);
                if (list == null || list.isEmpty()) {
                    JLabel empty = new JLabel("暂无商品");
                    productGrid.add(empty);
                } else {
                    for (Product p : list) {
                        productGrid.add(buildProductCard(p));
                    }
                }
            } catch (SQLException ex) {
                showError(ex);
            }
            productGrid.revalidate();
            productGrid.repaint();
            refreshRecommendations();
            // 更新未读消息数
            updateUnreadBadgeForCurrentUser();
        });
    }

    private void updateUnreadBadgeForCurrentUser() {
        // Show unread badge for the active actor (user or merchant)
        String phone = currentUserPhone != null ? currentUserPhone : currentMerchantPhone;
        if (phone == null) return;
        try {
            int c = messageService.getUnreadCount(phone);
            // find btnMessages by walking components (simple approach)
            for (Component comp : ((Container)frame.getContentPane().getComponent(0)).getComponents()) {
                if (comp instanceof JButton) {
                    JButton b = (JButton) comp;
                    if (b.getText().startsWith("消息")) { b.setText("消息 (" + c + ")"); }
                }
            }
        } catch (SQLException ignored) { }
    }

    private void updateUnreadBadge(JButton btn) {
        String phone = currentUserPhone != null ? currentUserPhone : currentMerchantPhone;
        if (phone == null) { btn.setText("消息"); return; }
        try {
            int c = messageService.getUnreadCount(phone);
            btn.setText("消息 (" + c + ")");
        } catch (SQLException ignored) { }
    }

    // 登录后显示推荐（示例：随机或最新的前3个商品）
    private void refreshRecommendations() {
        if (recommendPanel == null) return;
        recommendPanel.removeAll();
        try {
            List<Product> all = productService.listPublished();
            int count = Math.min(3, all.size());
            for (int i = 0; i < count; i++) {
                Product p = all.get(i);
                JLabel l = new JLabel(p.getTitle() + " - ¥" + p.getPrice());
                recommendPanel.add(l);
            }
        } catch (SQLException e) {
            // ignore recommend failure
        }
        recommendPanel.revalidate();
        recommendPanel.repaint();
    }

    /**
     * 为按钮统一应用主题样式
     */
    private void styleButton(AbstractButton b) {
        if (b == null) return;
        b.setBackground(THEME);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
    }

    private JPanel buildProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        card.setBackground(darkMode ? new Color(45, 45, 48) : Color.WHITE);

        // image placeholder (Product model currently has no imageUrl field)
        JLabel img = new JLabel("[图片]");
        img.setHorizontalAlignment(SwingConstants.CENTER);
        img.setPreferredSize(new Dimension(200, 120));

        JPanel info = new JPanel(new GridLayout(0, 1));
        info.setBorder(new EmptyBorder(8, 8, 8, 8));
        info.setBackground(card.getBackground());
        JLabel title = new JLabel(p.getTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel price = new JLabel(String.format("¥ %.2f", p.getPrice()));
        JLabel stock = new JLabel("库存: " + p.getStock());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(card.getBackground());
        JButton btnContact = new JButton("联系卖家");
        JButton btnBuy = new JButton("购买");
    JButton btnClaim = new JButton("领取优惠券");
        bottom.add(btnContact);
    bottom.add(btnClaim);
        bottom.add(btnBuy);

        // apply theme styling to action buttons
        styleButton(btnContact);
        styleButton(btnClaim);
        styleButton(btnBuy);

        // Enforce: only logged-in users can perform purchase-related actions
        boolean loggedIn = currentUserPhone != null;
        btnBuy.setEnabled(loggedIn);
        btnClaim.setEnabled(loggedIn);
        if (!loggedIn) {
            btnBuy.setToolTipText("请先登录后购买");
            btnClaim.setToolTipText("请先登录以领取优惠券");
        } else {
            btnBuy.setToolTipText(null);
            btnClaim.setToolTipText(null);
        }

        info.add(title);
        info.add(price);
        info.add(stock);
        info.add(bottom);

        card.add(img, BorderLayout.NORTH);
        card.add(info, BorderLayout.CENTER);

        // actions
        btnContact.addActionListener(e -> {
            // 不直接显示联系方式：打开站内消息对话 (简化实现：弹出发送消息窗口)
            String content = JOptionPane.showInputDialog(frame, "输入要发送给卖家的信息（联系方式请勿直接输入）：");
            if (content != null && !content.isBlank()) {
                try {
                    // 发送站内消息给商家 phone (实际消息中不可包含明文联系方式)
                    // 若用户已登录则使用用户手机号作为 senderId，让商家能看到是哪位顾客发来的消息并可回复；
                    // 若未登录则提示输入手机号作为发送者标识（或取消）
                    String senderId = currentUserPhone;
                    if (senderId == null) {
                        senderId = JOptionPane.showInputDialog(frame, "您未登录，请输入用于发送的手机号（将作为发送者标识）：");
                        if (senderId == null || senderId.isBlank()) {
                            // 用户取消或未输入，不发送
                            return;
                        }
                    }
                    messageService.sendMessagePublic(senderId, p.getMerchantPhone(), content);
                    JOptionPane.showMessageDialog(frame, "消息已发送，卖家可在站内回复。请勿在消息中直接填写敏感联系方式。");
                } catch (SQLException ex) {
                    showError(ex);
                }
            }
        });

        btnClaim.addActionListener(e -> {
            if (currentUserPhone == null) { JOptionPane.showMessageDialog(frame, "请先登录以领取优惠券"); return; }
            try {
                java.util.List<String> coupons = productService.listCouponsForMerchant(p.getMerchantId());
                if (coupons == null || coupons.isEmpty()) { JOptionPane.showMessageDialog(frame, "该商家暂无可领取的优惠券"); return; }
                String sel = (String) JOptionPane.showInputDialog(frame, "选择要领取的优惠券:", "领取优惠券", JOptionPane.PLAIN_MESSAGE, null, coupons.toArray(), coupons.get(0));
                if (sel == null) return;
                // 选中项格式 coupon_id | code | discount | remain
                String couponId = sel.split("\\|",2)[0].trim();
                boolean ok = productService.claimCoupon(couponId, currentUserPhone);
                JOptionPane.showMessageDialog(frame, ok?"领取成功":"领取失败（可能已被领取完）");
            } catch (SQLException ex) { showError(ex); }
        });

        btnBuy.addActionListener(e -> {
            if (currentUserPhone == null) { JOptionPane.showMessageDialog(frame, "请先登录后购买"); return; }
            String qtys = JOptionPane.showInputDialog(frame, "购买数量（整数）：", "1");
            if (qtys == null) return;
            try {
                int qty = Integer.parseInt(qtys);
                double total = p.getPrice() * qty;
                // 允许用户使用优惠券
                java.util.List<String> coupons = productService.listUserCoupons(currentUserPhone);
                String useCouponId = null;
                if (coupons != null && !coupons.isEmpty()) {
                    String message = "您已领取的优惠券:\n" + String.join("\n", coupons) + "\n输入要使用的 user_coupon_id (或留空):";
                    String chosen = JOptionPane.showInputDialog(frame, message);
                    if (chosen != null && !chosen.isBlank()) useCouponId = chosen.trim();
                }
                // 扣库存并下单（带/不带优惠券）
                productDAO.reduceStock(p.getProductId(), qty);
                if (useCouponId == null) {
                    orderService.createOrder(currentUserPhone, p.getMerchantId(), total, 0, 0);
                } else {
                    orderService.createOrderWithCoupon(currentUserPhone, p.getMerchantId(), total, useCouponId);
                }
                messageService.sendMessagePublic(currentUserPhone, p.getMerchantPhone(), "有买家已下单，请在站内沟通发货事宜（勿公开联系方式）。");
                JOptionPane.showMessageDialog(frame, "购买成功（演示）。请在站内继续沟通。联系方式不会直接展示。" );
                refreshProducts();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "请输入有效整数数量");
            } catch (SQLException ex) {
                showError(ex);
            }
        });

        return card;
    }

    private void showUserDialog() {
        String[] opts = {"注册", "登录", "取消"};
        int sel = JOptionPane.showOptionDialog(frame, "用户操作", "用户", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        try {
            if (sel == 0) {
                String name = JOptionPane.showInputDialog(frame, "用户名:");
                String phone = JOptionPane.showInputDialog(frame, "手机号:");
                String pwd = JOptionPane.showInputDialog(frame, "密码:");
                if (name == null || phone == null || pwd == null) return;
                boolean ok = auth.registerUser(new com.marketplace.models.User(name, phone, pwd), pwd);
                if (ok) {
                    currentUserPhone = phone; // 注册后自动登录
                    JOptionPane.showMessageDialog(frame, "注册并已登录: " + phone);
                    // 显示 VIP 等级
                    try {
                        java.util.Map<String, Object> info = userDAO.getVipAndTotalByPhone(currentUserPhone);
                        if (info != null) {
                            JOptionPane.showMessageDialog(frame, "当前VIP: " + info.get("vip") + "，总消费: " + info.get("total_spent"));
                            userInfoLabel.setText("用户: " + currentUserPhone + " | VIP: " + info.get("vip"));
                        }
                    } catch (Exception ignored) {}
                    refreshProducts();
                } else {
                    JOptionPane.showMessageDialog(frame, "注册失败（手机号可能已存在或被封禁）");
                }
            } else if (sel == 1) {
                String phone = JOptionPane.showInputDialog(frame, "手机号:");
                String pwd = JOptionPane.showInputDialog(frame, "密码:");
                if (phone == null || pwd == null) return;
                boolean ok = auth.loginUser(phone, pwd);
                if (ok) {
                    currentUserPhone = phone;
                    JOptionPane.showMessageDialog(frame, "登录成功: " + phone);
                    try {
                        java.util.Map<String, Object> info = userDAO.getVipAndTotalByPhone(currentUserPhone);
                        if (info != null) {
                            JOptionPane.showMessageDialog(frame, "当前VIP: " + info.get("vip") + "，总消费: " + info.get("total_spent"));
                            userInfoLabel.setText("用户: " + currentUserPhone + " | VIP: " + info.get("vip"));
                        }
                    } catch (Exception ignored) {}
                    refreshProducts();
                } else JOptionPane.showMessageDialog(frame, "登录失败");
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void showMessagesDialog() {
        // Allow either logged-in user or logged-in merchant to view messages
        String activePhone = currentUserPhone != null ? currentUserPhone : currentMerchantPhone;
        if (activePhone == null) { JOptionPane.showMessageDialog(frame, "请先登录以查看消息"); return; }
        try {
            JDialog d = new JDialog(frame, "消息中心", true);
            d.setSize(700, 500);
            JTabbedPane tabs = new JTabbedPane();

            // 收件箱（按时间倒序）
            DefaultListModel<String> inboxModel = new DefaultListModel<>();
            java.util.List<String> inbox = messageService.getMessagesFor(activePhone);
            if (inbox != null) inbox.forEach(inboxModel::addElement);
            JList<String> inboxList = new JList<>(inboxModel);
            JScrollPane inboxSp = new JScrollPane(inboxList);
            JPanel inboxPanel = new JPanel(new BorderLayout());
            JButton openInbox = new JButton("打开会话");
            openInbox.addActionListener(e -> {
                String sel = inboxList.getSelectedValue();
                if (sel == null) return;
                try {
                    String[] parts = sel.split("from:");
                    if (parts.length < 2) return;
                    String sender = parts[1].split(" - ")[0].trim();
                    showConversationDialog(activePhone, sender);
                    java.util.List<String> refreshed = messageService.getMessagesFor(activePhone);
                    inboxModel.clear(); if (refreshed!=null) refreshed.forEach(inboxModel::addElement);
                    updateUnreadBadgeForCurrentUser();
                } catch (SQLException ex) { showError(ex); }
            });
            JPanel inBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); inBottom.add(openInbox);
            inboxPanel.add(inboxSp, BorderLayout.CENTER); inboxPanel.add(inBottom, BorderLayout.SOUTH);

            // 已发送
            DefaultListModel<String> sentModel = new DefaultListModel<>();
            java.util.List<String> sent = messageService.getSentMessages(activePhone);
            if (sent != null) sent.forEach(sentModel::addElement);
            JList<String> sentList = new JList<>(sentModel);
            JScrollPane sentSp = new JScrollPane(sentList);
            JPanel sentPanel = new JPanel(new BorderLayout());
            JButton openSent = new JButton("查看接收方会话");
            openSent.addActionListener(e -> {
                String sel = sentList.getSelectedValue();
                if (sel == null) return;
                try {
                    // 格式: id | to:receiver - content
                    String[] parts = sel.split("to:");
                    if (parts.length < 2) return;
                    String receiver = parts[1].split(" - ")[0].trim();
                    showConversationDialog(activePhone, receiver);
                    // 刷新 sent list
                    java.util.List<String> refreshed = messageService.getSentMessages(activePhone);
                    sentModel.clear(); if (refreshed!=null) refreshed.forEach(sentModel::addElement);
                } catch (SQLException ex) { showError(ex); }
            });
            JPanel sentBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); sentBottom.add(openSent);
            sentPanel.add(sentSp, BorderLayout.CENTER); sentPanel.add(sentBottom, BorderLayout.SOUTH);

            tabs.addTab("收件箱", inboxPanel);
            tabs.addTab("已发送", sentPanel);

            d.setLayout(new BorderLayout());
            d.add(tabs, BorderLayout.CENTER);
            d.setLocationRelativeTo(frame);
            d.setVisible(true);
        } catch (SQLException ex) { showError(ex); }
    }

    private void showOrdersDialog() {
        if (currentUserPhone == null) { JOptionPane.showMessageDialog(frame, "请先登录以查看订单"); return; }
        try {
            java.util.List<com.marketplace.models.Order> orders = orderService.listByUser(currentUserPhone);
            JDialog d = new JDialog(frame, "我的订单", true);
            d.setSize(700, 500);
            DefaultListModel<String> lm = new DefaultListModel<>();
            if (orders != null && !orders.isEmpty()) {
                for (com.marketplace.models.Order o : orders) {
                    String line = String.format("%s | ¥%.2f | 折扣:¥%.2f | 状态:%s | %s",
                            o.getOrderId(), o.getTotalAmount(), o.getDiscount(), o.getStatus(), o.getCreateTime());
                    lm.addElement(line);
                }
            } else lm.addElement("无订单");
            JList<String> list = new JList<>(lm);
            JScrollPane sp = new JScrollPane(list);
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton view = new JButton("查看详情");
            view.addActionListener(e -> {
                String sel = list.getSelectedValue(); if (sel==null) return;
                JOptionPane.showMessageDialog(d, "订单详情:\n" + sel);
            });
            bottom.add(view);
            d.setLayout(new BorderLayout());
            d.add(sp, BorderLayout.CENTER);
            d.add(bottom, BorderLayout.SOUTH);
            d.setLocationRelativeTo(frame);
            d.setVisible(true);
        } catch (SQLException ex) { showError(ex); }
    }

    /**
     * 商家面板：商家可以在此创建优惠券并查看自己已发布的优惠券
     */
    private void showMerchantPanel() {
        if (currentMerchantId == null) { JOptionPane.showMessageDialog(frame, "无法打开商家面板：商家未登录或未找到商家信息"); return; }
        JDialog d = new JDialog(frame, "商家面板", true);
        d.setSize(700, 500);
        d.setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField codeField = new JTextField(10);
        JTextField discountField = new JTextField(6);
        JTextField untilField = new JTextField(10);
        JTextField qtyField = new JTextField(6);
        JButton btnCreate = new JButton("创建优惠券");
    JButton btnAddProduct = new JButton("添加商品");
        top.add(new JLabel("优惠码:")); top.add(codeField);
        top.add(new JLabel("折扣:")); top.add(discountField);
        top.add(new JLabel("有效期:")); top.add(untilField);
        top.add(new JLabel("总量:")); top.add(qtyField);
        top.add(btnCreate);
    top.add(btnAddProduct);

        DefaultListModel<String> lm = new DefaultListModel<>();
        JList<String> list = new JList<>(lm);
        JScrollPane sp = new JScrollPane(list);

        // load coupons
        try {
            java.util.List<String> coupons = productService.listCouponsForMerchant(currentMerchantId);
            if (coupons != null) coupons.forEach(lm::addElement);
        } catch (SQLException ex) { showError(ex); }

        btnCreate.addActionListener(e -> {
            String code = codeField.getText();
            String disc = discountField.getText();
            String until = untilField.getText();
            String qtys = qtyField.getText();
            if (code == null || code.isBlank() || disc==null || disc.isBlank()) { JOptionPane.showMessageDialog(d, "请输入优惠码与折扣"); return; }
            try {
                double dsc = Double.parseDouble(disc);
                int qty = Integer.parseInt(qtys);
                productService.createCoupon(currentMerchantId, code, dsc, until, qty);
                JOptionPane.showMessageDialog(d, "已创建优惠券: " + code);
                lm.clear();
                java.util.List<String> coupons = productService.listCouponsForMerchant(currentMerchantId);
                if (coupons != null) coupons.forEach(lm::addElement);
            } catch (NumberFormatException nfe) { JOptionPane.showMessageDialog(d, "折扣或数量格式错误"); }
            catch (SQLException ex) { showError(ex); }
        });

        btnAddProduct.addActionListener(e -> {
            // only allow when merchant context exists
            if (currentMerchantId == null || currentMerchantPhone == null) { JOptionPane.showMessageDialog(frame, "商家未登录或未找到商家信息，无法添加商品"); return; }
            JPanel inputs = new JPanel(new GridLayout(0,2));
            JTextField titleF = new JTextField(20);
            JTextField descF = new JTextField(20);
            JTextField priceF = new JTextField(8);
            JTextField stockF = new JTextField(6);
            inputs.add(new JLabel("商品标题:")); inputs.add(titleF);
            inputs.add(new JLabel("描述:")); inputs.add(descF);
            inputs.add(new JLabel("价格:")); inputs.add(priceF);
            inputs.add(new JLabel("库存:")); inputs.add(stockF);
            int sel = JOptionPane.showConfirmDialog(d, inputs, "添加商品", JOptionPane.OK_CANCEL_OPTION);
            if (sel != JOptionPane.OK_OPTION) return;
            try {
                String title = titleF.getText();
                String desc = descF.getText();
                double price = Double.parseDouble(priceF.getText());
                int stock = Integer.parseInt(stockF.getText());
                productService.publishProduct(title, desc, price, stock, currentMerchantId, currentMerchantPhone);
                JOptionPane.showMessageDialog(d, "商品已发布");
                refreshProducts();
            } catch (NumberFormatException nfe) { JOptionPane.showMessageDialog(d, "价格或库存格式错误"); }
            catch (SQLException ex) { showError(ex); }
        });

        d.add(top, BorderLayout.NORTH);
        d.add(sp, BorderLayout.CENTER);
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    /**
     * 用户端：显示当前用户已领取的优惠券
     */
    private void showMyCouponsDialog() {
        if (currentUserPhone == null) { JOptionPane.showMessageDialog(frame, "请先登录以查看我的优惠券"); return; }
        try {
            java.util.List<String> coupons = productService.listUserCoupons(currentUserPhone);
            JDialog d = new JDialog(frame, "我的优惠券", true);
            d.setSize(600, 400);
            DefaultListModel<String> lm = new DefaultListModel<>();
            if (coupons != null) coupons.forEach(lm::addElement);
            JList<String> list = new JList<>(lm);
            d.setLayout(new BorderLayout());
            d.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton close = new JButton("关闭");
            JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT)); b.add(close);
            close.addActionListener(e -> d.dispose());
            d.add(b, BorderLayout.SOUTH);
            d.setLocationRelativeTo(frame);
            d.setVisible(true);
        } catch (SQLException ex) { showError(ex); }
    }

    private void showConversationDialog(String me, String other) {
        try {
            java.util.List<String> conv = messageService.getConversation(me, other);
            JDialog d = new JDialog(frame, "与 " + other + " 的会话", true);
            d.setSize(600, 500);
            JTextArea area = new JTextArea(); area.setEditable(false);
            conv.forEach(s -> area.append(s + "\n"));
            JTextField input = new JTextField(); input.setColumns(40);
            JButton send = new JButton("发送");
            send.addActionListener(e -> {
                String txt = input.getText();
                if (txt == null || txt.isBlank()) return;
                try {
                    messageService.sendMessagePublic(me, other, txt);
                    area.append(me + " -> " + other + ": " + txt + "\n");
                    input.setText("");
                } catch (SQLException ex) { showError(ex); }
            });
            JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT));
            b.add(input); b.add(send);
            d.setLayout(new BorderLayout());
            d.add(new JScrollPane(area), BorderLayout.CENTER);
            d.add(b, BorderLayout.SOUTH);
            d.setLocationRelativeTo(frame);
            d.setVisible(true);
        } catch (SQLException ex) { showError(ex); }
    }

    private void showMerchantDialog() {
        String[] opts = {"注册", "登录", "取消"};
        int sel = JOptionPane.showOptionDialog(frame, "商家操作", "商家", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        try {
            if (sel == 0) {
                String shop = JOptionPane.showInputDialog(frame, "店铺名:");
                String phone = JOptionPane.showInputDialog(frame, "手机号:");
                String contact = JOptionPane.showInputDialog(frame, "联系方式(仅用于后台存储):");
                String pwd = JOptionPane.showInputDialog(frame, "密码:");
                if (shop==null||phone==null||pwd==null) return;
                com.marketplace.models.Merchant m = new com.marketplace.models.Merchant(shop, phone, contact, com.marketplace.models.Enums.IDENTITY.BOSS);
                boolean ok = auth.registerMerchant(m, pwd);
                JOptionPane.showMessageDialog(frame, ok?"商家注册成功":"商家注册失败");
            } else if (sel == 1) {
                String phone = JOptionPane.showInputDialog(frame, "手机号:");
                String pwd = JOptionPane.showInputDialog(frame, "密码:");
                if (phone==null||pwd==null) return;
                boolean ok = auth.loginMerchant(phone, pwd);
                if (ok) {
                    // 设置当前商家上下文并打开商家面板
                    try {
                        com.marketplace.models.Merchant m = merchantDAO.findByPhone(phone);
                        if (m != null) {
                            currentMerchantId = m.getMerchantId();
                            currentMerchantPhone = phone;
                            userInfoLabel.setText("商家: " + m.getShopName() + " | " + currentMerchantPhone);
                        } else {
                            currentMerchantPhone = phone;
                        }
                    } catch (SQLException ignored) {}
                    JOptionPane.showMessageDialog(frame, "登录成功，打开商家面板");
                    refreshProducts();
                    showMerchantPanel();
                } else {
                    JOptionPane.showMessageDialog(frame, "登录失败");
                }
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void showAdminDialog() {
        String name = JOptionPane.showInputDialog(frame, "管理员用户名:");
        String pwd = JOptionPane.showInputDialog(frame, "密码:");
        if (name==null || pwd==null) return;
        boolean ok = auth.loginAdmin(name, pwd);
        if (!ok) { JOptionPane.showMessageDialog(frame, "管理员登录失败"); return; }
        // Admin panel: search users & view banned products/phones
        JDialog panel = new JDialog(frame, "管理员面板", true);
        panel.setSize(700, 500);
        panel.setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField search = new JTextField(30);
        JButton btnSearch = new JButton("搜索用户");
        JButton btnViewUsers = new JButton("查看所有用户手机号(按数字)");
        JButton btnViewBanned = new JButton("查看被封商品/手机号");
    JButton btnLoadSamples = new JButton("加载示例数据");
        top.add(search);
        top.add(btnSearch);
        top.add(btnViewUsers);
        top.add(btnViewBanned);
    top.add(btnLoadSamples);

        JTextArea area = new JTextArea(); area.setEditable(false);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);

        btnSearch.addActionListener(e -> {
            String kw = search.getText();
            try { area.setText(String.join("\n", adminService.searchUsersByKeyword(kw))); }
            catch (SQLException ex) { showError(ex); }
        });

        btnViewUsers.addActionListener(e -> {
            try { area.setText(String.join("\n", adminService.listAllUserPhonesSorted())); }
            catch (SQLException ex) { showError(ex); }
        });

        btnViewBanned.addActionListener(e -> {
            try {
                List<String> bannedProducts = adminService.listBannedProductsSorted();
                List<String> bannedPhones = adminService.listBannedPhonesSorted();
                StringBuilder sb = new StringBuilder("被封商品:\n");
                bannedProducts.forEach(s -> sb.append(s).append("\n"));
                sb.append("\n被封手机号:\n");
                bannedPhones.forEach(s -> sb.append(s).append("\n"));
                area.setText(sb.toString());
            } catch (SQLException ex) { showError(ex); }
        });

        btnLoadSamples.addActionListener(e -> {
            // 使用 Admin 对象的方法触发样例数据加载
            try {
                com.marketplace.models.Admin admin = new com.marketplace.models.Admin("admin", name);
                boolean loaded = admin.loadSampleData();
                if (loaded) {
                    area.setText("已加载示例商家与商品到数据库（如已存在则忽略重复项）。\n点击“刷新”或关闭面板以查看效果。");
                    // 刷新商品展示
                    refreshProducts();
                } else {
                    area.setText("示例数据加载请求已发出，但未确认成功。");
                }
            } catch (SQLException ex) { showError(ex); }
        });

        panel.setLocationRelativeTo(frame);
        panel.setVisible(true);
    }

    private void applyTheme() {
        if (darkMode) {
            frame.getContentPane().setBackground(new Color(30, 30, 34));
            productGrid.setBackground(new Color(30, 30, 34));
        } else {
            frame.getContentPane().setBackground(Color.WHITE);
            productGrid.setBackground(Color.WHITE);
        }
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private void showError(Exception e) { JOptionPane.showMessageDialog(frame, "错误: "+e.getMessage()); e.printStackTrace(); }
}
