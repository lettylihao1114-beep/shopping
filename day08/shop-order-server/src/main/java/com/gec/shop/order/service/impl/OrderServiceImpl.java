package com.gec.shop.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.shop.order.api.dto.OrderSubmitRequest;
import com.gec.shop.order.api.entity.Order;
import com.gec.shop.order.feign.IProductFeignService;
import com.gec.shop.order.mapper.OrderMapper;
import com.gec.shop.order.service.IOrderService;
import com.gec.shop.product.api.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 订单服务 — 展示 5 种远程调用方案的演进 + Day08 订单提交/支付/取消
 *
 * 方案1：RestTemplate 硬编码 URL → 无法负载均衡，服务挂了就废了
 * 方案2：DiscoveryClient 手动取实例 → 只取 instances.get(0)，无均衡
 * 方案3：DiscoveryClient + Random → 手动负载均衡，代码繁琐
 * 方案4：@LoadBalanced RestTemplate → Ribbon 自动负载均衡
 * 方案5：Feign 声明式接口 → 最优雅，像调本地方法
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private IProductFeignService productFeignService;

    // ==================== Day08 增强：库存 + 关单 + 发货收货 ====================

    @Override
    public List<Order> submitOrder(OrderSubmitRequest req, Long userId, String username) {
        List<Order> orders = new ArrayList<>();
        String orderNo = generateOrderNo();

        for (OrderSubmitRequest.OrderItem item : req.getItems()) {
            Product product = productFeignService.get(item.getPid());

            // Day08 库存扣减 — 借鉴 mall4cloud SkuStockLockFeignClient.lock()
            try {
                productFeignService.updateStock(item.getPid(), -item.getQty());
            } catch (Exception e) {
                log.error("库存扣减失败 pid={} qty={}: {}", item.getPid(), item.getQty(), e.getMessage());
                throw new RuntimeException("商品 #" + item.getPid() + " 库存不足", e);
            }

            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setPid(item.getPid());
            order.setProductName(product.getName());
            order.setProductCategory(product.getCategory());  // Day09: 冗余类目，商家按此过滤
            order.setProductPrice(product.getPrice());
            order.setUid(userId);
            order.setUsername(username);
            order.setNumber(item.getQty());
            order.setTotalAmount(product.getPrice() * item.getQty());
            order.setStatus("PENDING");
            order.setReceiverName(req.getReceiverName());
            order.setReceiverPhone(req.getReceiverPhone());
            order.setAddress(req.getAddress());

            String port = product.getName().replaceAll(".*—(\\d+)$", "$1");
            order.setHandledByPort(port);
            order.setCreateTime(LocalDateTime.now());
            super.save(order);
            orders.add(order);
            log.info("订单项创建: orderNo={} pid={} qty={} amount={} stock-{}", orderNo, item.getPid(), item.getQty(), order.getTotalAmount(), item.getQty());
        }

        log.info("订单提交完成: orderNo={} items={} receiver={}", orderNo, orders.size(), req.getReceiverName());
        return orders;
    }

    @Override
    public Order payOrder(Long id) {
        Order order = super.getById(id);
        if (order == null || !"PENDING".equals(order.getStatus())) return null;
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        super.updateById(order);
        log.info("订单支付成功: id={} orderNo={} amount={}", id, order.getOrderNo(), order.getTotalAmount());
        return order;
    }

    @Override
    public boolean cancelOrder(Long id) {
        Order order = super.getById(id);
        if (order == null || "CANCELLED".equals(order.getStatus())) return false;

        // Day08 恢复库存 — 借鉴 mall4cloud cancelOrderAndGetCancelOrderIds()
        try {
            productFeignService.updateStock(order.getPid(), order.getNumber());
            log.info("库存恢复: pid={} qty={}", order.getPid(), order.getNumber());
        } catch (Exception e) {
            log.error("库存恢复失败: pid={}", order.getPid(), e);
        }

        order.setStatus("CANCELLED");
        super.updateById(order);
        log.info("订单已取消: id={}", id);
        return true;
    }

    /** Day08 发货 — 借鉴 mall4cloud OrderServiceImpl.delivery() */
    @Override
    public Order shipOrder(Long id) {
        Order order = super.getById(id);
        if (order == null || !"PAID".equals(order.getStatus())) return null;
        order.setStatus("SHIPPING");
        super.updateById(order);
        log.info("订单已发货: id={}", id);
        return order;
    }

    /** Day08 确认收货 — 借鉴 mall4cloud OrderServiceImpl.receiptOrder() */
    @Override
    public Order receiveOrder(Long id) {
        Order order = super.getById(id);
        if (order == null || !"SHIPPING".equals(order.getStatus())) return null;
        order.setStatus("COMPLETED");
        super.updateById(order);
        log.info("订单已收货: id={}", id);
        return order;
    }

    /** Day08 自动取消过期订单 — 借鉴 mall4cloud cancelOrderAndGetCancelOrderIds() */
    @Override
    public int autoCancelExpiredOrders() {
        List<Order> expired = lambdaQuery()
                .eq(Order::getStatus, "PENDING")
                .lt(Order::getCreateTime, LocalDateTime.now().minusMinutes(30))
                .list();
        for (Order o : expired) {
            cancelOrder(o.getId());  // 取消 + 恢复库存
        }
        if (!expired.isEmpty()) {
            log.info("自动取消 {} 个过期订单", expired.size());
        }
        return expired.size();
    }

    /** Day08 定时任务：每60秒扫描过期订单 — 替代 RocketMQ 延时消息 */
    @Scheduled(fixedDelay = 60000)
    public void scheduledAutoCancel() {
        autoCancelExpiredOrders();
    }

    /** 生成订单号：yyyyMMddHHmmss + 4位随机数 */
    private static String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = new Random().nextInt(9000) + 1000;
        return time + rand;
    }

    // ==================== Day02 原有 ====================

    @Override
    public Order createOrder(Long pid, Long uid) {
        Order order = new Order();

        // ==================== 方案1：硬编码 URL ====================
        // String url = "http://localhost:8081/products/" + pid;
        // Product product = restTemplate.getForObject(url, Product.class);

        // ==================== 方案2：DiscoveryClient — 只取第一个 ====================
        // List<ServiceInstance> instances = discoveryClient.getInstances("product-service");
        // ServiceInstance instance = instances.get(0);
        // String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/products/" + pid;
        // Product product = restTemplate.getForObject(url, Product.class);

        // ==================== 方案3：DiscoveryClient + Random 手动负载均衡 ====================
        // List<ServiceInstance> instances = discoveryClient.getInstances("product-service");
        // int index = new Random().nextInt(instances.size());
        // ServiceInstance instance = instances.get(index);
        // String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/products/" + pid;
        // System.out.println("从nacos中获取的url地址：" + url);
        // Product product = restTemplate.getForObject(url, Product.class);

        // ==================== 方案4：Ribbon @LoadBalanced ====================
        // String url = "http://product-service/products/" + pid;
        // Product product = restTemplate.getForObject(url, Product.class);

        // ==================== 方案5：Feign 声明式调用（Day02 最终方案）====================
        Product product = productFeignService.get(pid);

        order.setPid(pid);
        order.setProductName(product.getName());
        order.setProductCategory(product.getCategory());
        order.setProductPrice(product.getPrice());

        // 用户
        order.setUid(uid);
        order.setUsername("dafei");
        order.setNumber(1);
        order.setTotalAmount(product.getPrice());

        // 从商品名提取端口，直观验证负载均衡
        String port = product.getName().replaceAll(".*—(\\d+)$", "$1");
        order.setHandledByPort(port);

        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());

        log.info("订单创建成功: {}", order);
        super.save(order);
        return order;
    }
}
