package com.marketplace;

import com.marketplace.models.Merchant;
import com.marketplace.models.User;
import com.marketplace.models.Product;
import com.marketplace.service.AdminService;
import com.marketplace.service.AuthService;
import com.marketplace.service.OrderService;
import com.marketplace.service.ProductService;
import com.marketplace.service.MessageService;
import com.marketplace.service.ComplaintService;
import com.marketplace.models.Enums;
import com.marketplace.dao.ProductDAO;
import com.marketplace.dao.MerchantDAO;
import com.marketplace.dao.UserDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * 主程序入口：提供控制台交互界面以演示各项功能。
 * 
 */
public class Main {
    private static final AuthService auth = new AuthService();
    private static final ProductService productService = new ProductService();
    private static final OrderService orderService = new OrderService();
    private static final AdminService adminService = new AdminService();
    private static final MessageService messageService = new MessageService();
    private static final ComplaintService complaintService = new ComplaintService();
    private static final com.marketplace.service.CartService cartService = new com.marketplace.service.CartService();
    private static final ProductDAO productDAO = new ProductDAO();
    private static final MerchantDAO merchantDAO = new MerchantDAO();
    private static final UserDAO userDAO = new UserDAO();

    private static String currentUserPhone = null; 
    private static Merchant currentMerchant = null; 
    private static String adminUser = null;

    public static void main(String[] args) {
        // ====== SpotBugs 实验用 BUG 1: 忽略异常 (DE_MIGHT_IGNORE) ======
        // try {
        //     throw new RuntimeException("SpotBugs test exception");
        // } catch (Exception e) {
        //     // 故意什么也不做
        // }

        // // ====== SpotBugs 实验用 BUG 2: 潜在空指针解引用 (NP 系列) ======
        // String spotbugsNpeTest = null;
        // if (args != null && args.length >= 0) { // 这个判断总是 true
        //     // 这里会在某条路径上对 null 调用方法
        //     if (System.nanoTime() % 2 == 0) {
        //         spotbugsNpeTest.toString();
        //     }
        // }

        // ====== SpotBugs 实验用 BUG 3: 资源未关闭，模拟 Memory Leak 风格问题 ======
        // java.io.InputStream leakStream = null;
        // try {
        //     // 每次启动程序都打开一个输入流，但从不关闭
        //     leakStream = new java.io.FileInputStream("non_existent_file_for_spotbugs_test.dat");
        // } catch (java.io.IOException ex) {
        //     // 故意只打印一行而不关闭资源
        //     System.out.println("忽略的IO异常: " + ex.getMessage());
        // }
        System.out.println("欢迎使用简易 Marketplace 系统-demo");
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("主菜单：1 注册用户 2 用户登录 3 注册商家 4 商家登录 5 管理员登录 0 退出");
                String cmd = sc.nextLine().trim();
                try {
                    switch (cmd) {
                        case "1":
                            registerUser(sc);
                            break;
                        case "2":
                            userLogin(sc);
                            break;
                        case "3":
                            registerMerchant(sc);
                            break;
                        case "4":
                            merchantLogin(sc);
                            break;
                        case "5":
                            adminLogin(sc);
                            break;
                        
                        case "0":
                            System.out.println("退出");
                            return;
                        default:
                            System.out.println("未知命令");
                    }
                } catch (SQLException e) {
                    System.out.println("操作失败：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    // ---------- 注册/登录/流程实现 ----------
    private static void registerUser(Scanner sc) throws SQLException {
        System.out.print("用户名: ");
        String name = sc.nextLine();
        System.out.print("手机号: ");
        String phone = sc.nextLine();
        System.out.print("密码: ");
        String pwd = sc.nextLine();
        User u = new User(name, phone, pwd);
        boolean ok = auth.registerUser(u, pwd);
        if (ok) {
            System.out.println("注册成功，已自动登录");
            currentUserPhone = phone; // 注册后默认登录
            // 登录后显示用户信息与VIP等级
            try {
                java.util.Map<String, Object> info = userDAO.getVipAndTotalByPhone(currentUserPhone);
                if (info != null) System.out.println("当前VIP: " + info.get("vip") + "，总消费: " + info.get("total_spent"));
            } catch (Exception ignored) {}
            userMenu(sc);
        } else {
            System.out.println("注册失败（可能被封禁或手机号已存在）");
        }
    }

    private static void userLogin(Scanner sc) throws SQLException {
        System.out.print("手机号: ");
        String phone = sc.nextLine();
        System.out.print("密码: ");
        String pwd = sc.nextLine();
        boolean ok = auth.loginUser(phone, pwd);
        if (ok) {
            currentUserPhone = phone;
            System.out.println("用户登录成功，进入用户菜单");
            userMenu(sc);
        } else {
            System.out.println("登录失败（检查手机号/密码或是否被封禁）");
        }
    }

    private static void registerMerchant(Scanner sc) throws SQLException {
        System.out.print("店铺名: ");
        String shop = sc.nextLine();
        System.out.print("手机号: ");
        String phone = sc.nextLine();
        System.out.print("联系方式: ");
        String contact = sc.nextLine();
        System.out.print("密码: ");
        String pwd = sc.nextLine();
        Merchant m = new Merchant(shop, phone, contact, com.marketplace.models.Enums.IDENTITY.BOSS);
        boolean ok = auth.registerMerchant(m, pwd);
        System.out.println(ok ? "商家注册成功" : "商家注册失败（可能被封禁）");
    }

    private static void merchantLogin(Scanner sc) throws SQLException {
        System.out.print("商家手机号: ");
        String phone = sc.nextLine();
        System.out.print("密码: ");
        String pwd = sc.nextLine();
        boolean ok = auth.loginMerchant(phone, pwd);
        if (ok) {
            currentMerchant = merchantDAO.findByPhone(phone);
            System.out.println("商家登录成功，进入商家菜单");
            merchantMenu(sc);
        } else {
            System.out.println("商家登录失败（检查手机号/密码或是否被封禁）");
        }
    }

    private static void adminLogin(Scanner sc) throws SQLException {
        System.out.print("管理员用户名: ");
        String name = sc.nextLine();
        System.out.print("密码: ");
        String pwd = sc.nextLine();
        boolean ok = auth.loginAdmin(name, pwd);
        if (ok) {
            adminUser = name;
            System.out.println("管理员登录成功，进入管理员菜单");
            adminMenu(sc);
        } else {
            System.out.println("管理员登录失败");
        }
    }

    // ---------- 用户菜单 (登录后) ----------
    private static void userMenu(Scanner sc) throws SQLException {
        while (true) {
            System.out.println("用户菜单：1 列出商品(可购买/私信/加入购物车) 2 搜索商品 3 查看消息 4 切换到商家 5 领取优惠券 6 加入购物车 0 注销");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    List<Product> list = productService.listPublished();
                    for (Product p : list)
                        System.out.println(p.getProductId() + " | " + p.getTitle() + " | " + p.getPrice() + " | 库存:" + p.getStock() + " | 联系方式: 使用站内私信或购买后交换");
                    // 浏览时提供购买或私信操作（购买只能在浏览界面触发）
                    System.out.print("输入要操作的商品ID（或回车返回）: ");
                    String sel = sc.nextLine().trim();
                    if (!sel.isEmpty()) {
                        Product prod = productDAO.findById(sel);
                        if (prod == null) { System.out.println("商品不存在"); break; }
                        System.out.println("1 购买 2 私信卖家 3 举报 4 加入购物车 其它 返回");
                        String act = sc.nextLine().trim();
                        if (act.equals("1")) {
                            System.out.print("数量: ");
                            int qty = Integer.parseInt(sc.nextLine());
                            double total = prod.getPrice() * qty;
                            // 提示用户可使用优惠券
                            java.util.List<String> coupons = productService.listUserCoupons(currentUserPhone);
                            String useCouponId = null;
                            if (coupons != null && !coupons.isEmpty()) {
                                System.out.println("您已领取的优惠券:");
                                for (String uc : coupons) System.out.println(uc);
                                System.out.print("输入要使用的 user_coupon_id (或回车跳过): ");
                                String chosen = sc.nextLine().trim();
                                if (!chosen.isEmpty()) useCouponId = chosen;
                            }
                            // 减少库存并创建订单（若使用优惠券则调用带券的下单）
                            productDAO.reduceStock(sel, qty);
                            if (useCouponId == null) {
                                orderService.createOrder(currentUserPhone, prod.getMerchantId(), total, 0.0, 0.0);
                            } else {
                                orderService.createOrderWithCoupon(currentUserPhone, prod.getMerchantId(), total, useCouponId);
                            }
                            System.out.println("购买成功，已创建订单");
                            messageService.sendContactExchange(currentUserPhone, prod.getMerchantPhone());
                        } else if (act.equals("2")) {
                            System.out.print("输入给卖家的消息（勿发明文联系方式）: ");
                            String content = sc.nextLine();
                            messageService.sendMessagePublic(currentUserPhone, prod.getMerchantPhone(), content);
                            System.out.println("消息已发送");
                        } else if (act.equals("3")) {
                            // 举报流程：可举报商家或商品
                            System.out.println("举报选项：1 举报商家 2 举报商品 其它 取消");
                            String which = sc.nextLine().trim();
                            if (which.equals("1") || which.equals("2")) {
                                System.out.println("请选择举报类型：1 服务 2 质量 3 欺诈");
                                String t = sc.nextLine().trim();
                                Enums.ComplaintType type = Enums.ComplaintType.SERVICE;
                                if ("2".equals(t)) type = Enums.ComplaintType.QUALITY;
                                else if ("3".equals(t)) type = Enums.ComplaintType.FRAUD;
                                String target = which.equals("1") ? prod.getMerchantId() : prod.getProductId();
                                boolean ok = complaintService.submitComplaint(currentUserPhone, target, type);
                                System.out.println(ok ? "举报提交成功，管理员将会处理" : "举报提交失败，请稍后重试");
                            } else {
                                System.out.println("已取消举报");
                            }
                        } else if (act.equals("4")) {
                            System.out.print("数量: ");
                            int qty = Integer.parseInt(sc.nextLine());
                            try {
                                // TODO: addToCart 实现仍为占位，后续需实现持久化或内存原型
                                cartService.addToCart(currentUserPhone, sel, qty);
                                System.out.println("已添加至购物车（若该接口被实现）。");
                            } catch (UnsupportedOperationException e) {
                                System.out.println("该功能尚未实现。接口已存在。");
                            }
                        }
                    }
                    break;
                case "6":
                    // 简单入口：让用户通过商品 ID 与数量将商品加入购物车（调用接口）
                    System.out.print("输入要加入购物车的商品ID: ");
                    String pid = sc.nextLine().trim();
                    if (pid.isEmpty()) { System.out.println("已取消"); break; }
                    System.out.print("数量: ");
                    int q = Integer.parseInt(sc.nextLine());
                    try {
                        // TODO: addToCart 为占位实现，应在后续迭代中实现购物车持久化和 checkout
                        cartService.addToCart(currentUserPhone, pid, q);
                        System.out.println("已添加至购物车（若该接口被实现）。");
                    } catch (UnsupportedOperationException e) {
                        System.out.println("该功能尚未实现。接口已存在。");
                    }
                    break;
                case "2":
                    System.out.print("关键词: ");
                    String kw = sc.nextLine();
                    List<Product> sres = productService.searchProducts(kw);
                    for (Product p : sres)
                        System.out.println(p.getProductId() + " | " + p.getTitle() + " | " + p.getPrice() + " | 库存:" + p.getStock() + " | 联系方式: 使用站内私信或购买后交换");
                    break;
                case "3":
                    // 列出与当前用户相关的所有消息（包括收到和已发送），选择后可回复或继续发送给对方
                    java.util.List<String> inboxList = messageService.getMessagesFor(currentUserPhone);
                    java.util.List<String> sentList = messageService.getSentMessages(currentUserPhone);
                    java.util.List<String> combined = new java.util.ArrayList<>();
                    if (inboxList != null) {
                        for (String s : inboxList) combined.add("[收] " + s);
                    }
                    if (sentList != null) {
                        for (String s : sentList) combined.add("[发] " + s);
                    }
                    if (combined.isEmpty()) {
                        System.out.println("暂无相关消息");
                        break;
                    }
                    System.out.println("相关消息列表：");
                    for (int i = 0; i < combined.size(); i++) System.out.println((i+1) + ". " + combined.get(i));
                    System.out.print("输入消息序号以回复/继续发送，输入 n 发送新消息，或回车返回: ");
                    String choice2 = sc.nextLine().trim();
                    if (choice2.isEmpty()) break;
                    if (choice2.equalsIgnoreCase("n")) {
                        System.out.print("目标ID(手机号/商家ID): ");
                        String to = sc.nextLine().trim();
                        if (to.isEmpty()) { System.out.println("已取消"); break; }
                        System.out.print("内容: ");
                        String cmsg = sc.nextLine();
                        messageService.sendMessagePublic(currentUserPhone, to, cmsg);
                        System.out.println("消息已发送");
                        break;
                    }
                    try {
                        int selectedIndex = Integer.parseInt(choice2) - 1;
                        if (selectedIndex < 0 || selectedIndex >= combined.size()) { System.out.println("序号无效"); break; }
                        String entry = combined.get(selectedIndex);
                        // 解析对方 ID（支持 from: 和 to: 两种格式）
                        String other = null;
                        if (entry.contains("from:")) {
                            String[] p = entry.split("from:");
                            if (p.length > 1) other = p[1].split(" - ")[0].trim();
                        } else if (entry.contains("to:")) {
                            String[] p = entry.split("to:");
                            if (p.length > 1) other = p[1].split(" - ")[0].trim();
                        }
                        if (other == null) { System.out.println("无法解析对方 ID，操作取消"); break; }
                        System.out.println("已选对象: " + other);
                        System.out.println("操作：1 回复/继续发送 其它 返回");
                        String op2 = sc.nextLine().trim();
                        if (op2.equals("1")) {
                            System.out.print("输入要发送的内容: ");
                            String body = sc.nextLine();
                            if (!body.isEmpty()) {
                                messageService.sendMessagePublic(currentUserPhone, other, body);
                                System.out.println("消息已发送");
                            } else System.out.println("已取消发送（内容为空）");
                        }
                    } catch (NumberFormatException nfe) { System.out.println("无效输入"); }
                    break;
                case "4":
                    // 切换为商家：若当前手机号已有商家账户则直接登录，否则创建一个简单商家并登录
                    com.marketplace.models.Merchant m = merchantDAO.findByPhone(currentUserPhone);
                    if (m == null) {
                        System.out.print("输入店铺名以创建商家账户: ");
                        String shop = sc.nextLine();
                        m = new com.marketplace.models.Merchant(shop, currentUserPhone, "", com.marketplace.models.Enums.IDENTITY.BOSS);
                        merchantDAO.save(m, "");
                        System.out.println("已为当前用户创建商家账户并切换到商家模式");
                    } else {
                        System.out.println("检测到已有商家账户，已切换到商家模式");
                    }
                    currentMerchant = merchantDAO.findByPhone(currentUserPhone);
                    merchantMenu(sc);
                    break;
                case "5":
                    System.out.print("输入商家 ID 以查看其优惠券: ");
                    String mid = sc.nextLine();
                    java.util.List<String> coupons = productService.listCouponsForMerchant(mid);
                    if (coupons == null || coupons.isEmpty()) System.out.println("无可领取的优惠券或商家不存在");
                    else {
                        for (String cc : coupons) System.out.println(cc);
                        System.out.print("输入要领取的 coupon_id (或回车取消): ");
                        String chosen = sc.nextLine().trim();
                        if (!chosen.isEmpty()) {
                            boolean ok = productService.claimCoupon(chosen, currentUserPhone);
                            System.out.println(ok ? "领取成功" : "领取失败（可能已发完）");
                        }
                    }
                    break;
                case "0":
                    currentUserPhone = null;
                    System.out.println("已退出用户菜单");
                    return;
                default:
                    System.out.println("未知命令");
            }
        }
    }

    // ---------- 商家菜单 (登录后) ----------
    private static void merchantMenu(Scanner sc) throws SQLException {
        while (true) {
            System.out.println("商家菜单：1 发布商品 2 列出我的商品 3 查看消息 4 私信用户 0 注销");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    System.out.print("标题: ");
                    String title = sc.nextLine();
                    System.out.print("描述: ");
                    String desc = sc.nextLine();
                    System.out.print("价格: ");
                    double price = Double.parseDouble(sc.nextLine());
                    System.out.print("库存: ");
                    int stock = Integer.parseInt(sc.nextLine());
                    productService.publishProduct(title, desc, price, stock, currentMerchant.getMerchantId(), currentMerchant.getPhone());
                    System.out.println("发布成功");
                    break;
                case "2":
                    List<Product> my = productDAO.listByMerchant(currentMerchant.getMerchantId());
                    for (Product p : my)
                        System.out.println(p.getProductId() + " | " + p.getTitle() + " | " + p.getPrice() + " | 库存:" + p.getStock());
                    break;
                case "5":
                    // 创建优惠券
                    System.out.print("优惠码: ");
                    String code = sc.nextLine();
                    System.out.print("折扣金额(直接减免): ");
                    double d = Double.parseDouble(sc.nextLine());
                    System.out.print("有效期(可留空): ");
                    String until = sc.nextLine();
                    System.out.print("总发行数量: ");
                    int qty = Integer.parseInt(sc.nextLine());
                    productService.createCoupon(currentMerchant.getMerchantId(), code, d, until, qty);
                    System.out.println("已创建优惠券: " + code);
                    break;
                case "3":
                    // 商家：列出与商家相关的所有消息（收到与已发），选择后可回复或继续发送给对方
                    java.util.List<String> inboxListM = messageService.getMessagesFor(currentMerchant.getPhone());
                    java.util.List<String> sentListM = messageService.getSentMessages(currentMerchant.getPhone());
                    java.util.List<String> combinedM = new java.util.ArrayList<>();
                    if (inboxListM != null) for (String s : inboxListM) combinedM.add("[收] " + s);
                    if (sentListM != null) for (String s : sentListM) combinedM.add("[发] " + s);
                    if (combinedM.isEmpty()) { System.out.println("暂无相关消息"); break; }
                    System.out.println("相关消息列表：");
                    for (int i = 0; i < combinedM.size(); i++) System.out.println((i+1) + ". " + combinedM.get(i));
                    System.out.print("输入消息序号以回复/继续发送，输入 n 发送新消息，或回车返回: ");
                    String mchChoice2 = sc.nextLine().trim();
                    if (mchChoice2.isEmpty()) break;
                    if (mchChoice2.equalsIgnoreCase("n")) {
                        System.out.print("目标ID(手机号/商家ID): ");
                        String to = sc.nextLine().trim();
                        if (to.isEmpty()) { System.out.println("已取消"); break; }
                        System.out.print("内容: ");
                        String content = sc.nextLine();
                        messageService.sendMessagePublic(currentMerchant.getPhone(), to, content);
                        System.out.println("消息已发送");
                        break;
                    }
                    try {
                        int selectedIndexM = Integer.parseInt(mchChoice2) - 1;
                        if (selectedIndexM < 0 || selectedIndexM >= combinedM.size()) { System.out.println("序号无效"); break; }
                        String entry = combinedM.get(selectedIndexM);
                        String other = null;
                        if (entry.contains("from:")) {
                            String[] p = entry.split("from:");
                            if (p.length > 1) other = p[1].split(" - ")[0].trim();
                        } else if (entry.contains("to:")) {
                            String[] p = entry.split("to:");
                            if (p.length > 1) other = p[1].split(" - ")[0].trim();
                        }
                        if (other == null) { System.out.println("无法解析对方 ID，操作取消"); break; }
                        System.out.println("已选对象: " + other);
                        System.out.println("操作：1 回复/继续发送 其它 返回");
                        String op = sc.nextLine().trim();
                        if (op.equals("1")) {
                            System.out.print("输入要发送的内容: ");
                            String body = sc.nextLine();
                            if (!body.isEmpty()) {
                                messageService.sendMessagePublic(currentMerchant.getPhone(), other, body);
                                System.out.println("消息已发送");
                            } else System.out.println("已取消发送（内容为空）");
                        }
                    } catch (NumberFormatException nfe) { System.out.println("无效输入"); }
                    break;
                case "4":
                    System.out.print("目标用户手机号: ");
                    String to = sc.nextLine();
                    System.out.print("内容: ");
                    String content = sc.nextLine();
                    messageService.sendMessagePublic(currentMerchant.getPhone(), to, content);
                    System.out.println("已发送");
                    break;
                case "0":
                    currentMerchant = null;
                    System.out.println("商家已注销");
                    return;
                default:
                    System.out.println("未知命令");
            }
        }
    }

    

    // ---------- 管理员菜单 (登录后) ----------
    private static void adminMenu(Scanner sc) throws SQLException {
        while (true) {
            System.out.println("管理员菜单：1 搜索商品 2 查看所有用户手机号 3 查看被封商品 4 查看被封手机号 5 封禁商品 6 封禁手机号 7 取消封禁 8 强制删除商品 9 一键清空数据 10 加载样例数据 0 注销");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    System.out.print("关键词: ");
                    String kw = sc.nextLine();
                    List<Product> sres = productService.searchProducts(kw);
                    for (Product p : sres)
                        System.out.println(p.getProductId() + " | " + p.getTitle() + " | " + p.getPrice() + " | 库存:" + p.getStock());
                    break;
                case "2":
                    java.util.List<String> phones = adminService.listAllUserPhonesSorted();
                    if (phones.isEmpty()) System.out.println("无用户"); else phones.forEach(System.out::println);
                    break;
                case "3":
                    java.util.List<String> bp = adminService.listBannedProductsSorted();
                    if (bp.isEmpty()) System.out.println("无被封商品"); else bp.forEach(System.out::println);
                    break;
                case "4":
                    java.util.List<String> bphones = adminService.listBannedPhonesSorted();
                    if (bphones.isEmpty()) System.out.println("无被封手机号"); else bphones.forEach(System.out::println);
                    break;
                case "5":
                    System.out.print("商品ID: ");
                    String pidBan = sc.nextLine();
                    adminService.banProduct(pidBan, "管理员封禁 by " + adminUser);
                    System.out.println("商品已封禁 " + pidBan);
                    break;
                case "6":
                    System.out.print("手机号: ");
                    String bp2 = sc.nextLine();
                    adminService.banPhone(bp2, "管理员操作 by " + adminUser);
                    System.out.println("已封禁 " + bp2);
                    break;
                case "7":
                    System.out.print("手机号: ");
                    String ub = sc.nextLine();
                    adminService.unbanPhone(ub);
                    System.out.println("已解禁 " + ub);
                    break;
                case "8":
                    System.out.print("商品ID: ");
                    String pid = sc.nextLine();
                    adminService.forceDeleteProduct(pid);
                    System.out.println("商品已删除 " + pid);
                    break;
                case "9":
                    System.out.print("确认清空所有主要数据？这将删除用户/商品/订单等数据，仍保留管理员账号。输入 YES 确认: ");
                    String conf = sc.nextLine();
                    if ("YES".equals(conf)) {
                        adminService.clearAllData();
                        System.out.println("已清空主要数据");
                    } else System.out.println("已取消");
                    break;
                case "10":
                    adminService.seedSampleData();
                    System.out.println("已加载样例商家与商品（若不存在）");
                    break;
                case "0":
                    adminUser = null;
                    System.out.println("管理员已退出");
                    return;
                default:
                    System.out.println("未知命令");
            }
        }
    }
}
