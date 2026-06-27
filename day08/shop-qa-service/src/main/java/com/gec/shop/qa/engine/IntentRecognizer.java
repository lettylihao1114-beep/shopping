package com.gec.shop.qa.engine;

import java.util.*;
import java.util.regex.*;

/**
 * 意图识别器 — 10 种意图 + 属性/实体提取。
 * 基于正则关键词权重打分，支持中文电商常见问答场景。
 */
public class IntentRecognizer {

    // ────────── 10 种意图 ──────────
    public static final String INTENT_GREETING       = "greeting";
    public static final String INTENT_QUERY_ATTR     = "query_attr";
    public static final String INTENT_QUERY_RELATION = "query_relation";
    public static final String INTENT_RECOMMEND      = "recommend";
    public static final String INTENT_COMPARE         = "compare";
    public static final String INTENT_FILTER          = "filter";
    public static final String INTENT_SHIPPING        = "shipping";
    public static final String INTENT_PAYMENT         = "payment";
    public static final String INTENT_AFTER_SALES     = "after_sales";
    public static final String INTENT_DISCOUNT        = "discount";

    private static final Map<String, List<Pattern>> RULES = new LinkedHashMap<>();
    static {
        RULES.put(INTENT_GREETING, List.of(
            Pattern.compile("你好"), Pattern.compile("您好"), Pattern.compile("嗨"),
            Pattern.compile("在吗"), Pattern.compile("hello", Pattern.CASE_INSENSITIVE),
            Pattern.compile("hi", Pattern.CASE_INSENSITIVE), Pattern.compile("早上好"),
            Pattern.compile("晚上好"), Pattern.compile("下午好")
        ));
        RULES.put(INTENT_QUERY_ATTR, List.of(
            Pattern.compile("多少[钱]?"), Pattern.compile("什么[价价颜色规格尺寸]?"),
            Pattern.compile("哪些"), Pattern.compile("多少钱"), Pattern.compile("价位"),
            Pattern.compile("多[大便高长重贵]"), Pattern.compile("尺寸"), Pattern.compile("颜色"),
            Pattern.compile("规格"), Pattern.compile("库存"), Pattern.compile("有货"),
            Pattern.compile("缺货"), Pattern.compile("有没有货"), Pattern.compile("重量"),
            Pattern.compile("材质"), Pattern.compile("产地"), Pattern.compile("评价"),
            Pattern.compile("怎么样"), Pattern.compile("好用吗"), Pattern.compile("质量"),
            Pattern.compile("多大"), Pattern.compile("多重"),
            Pattern.compile("主营业务"), Pattern.compile("主营"), Pattern.compile("发源地"),
            Pattern.compile("创立"), Pattern.compile("成立"), Pattern.compile("年份")
        ));
        RULES.put(INTENT_QUERY_RELATION, List.of(
            Pattern.compile("属于"), Pattern.compile("是什么品牌"), Pattern.compile("相关"),
            Pattern.compile("同类"), Pattern.compile("类似"), Pattern.compile("有哪些产品"),
            Pattern.compile("产品有哪些"), Pattern.compile("有哪些系列"),
            Pattern.compile("有没有"), Pattern.compile("属于什么"),
            Pattern.compile("业务范围"), Pattern.compile("品牌"), Pattern.compile("哪些品牌"),
            Pattern.compile("什么品类"), Pattern.compile("卖什么"), Pattern.compile("做什么"),
            Pattern.compile("旗下"), Pattern.compile("产品线"), Pattern.compile("系列"),
            Pattern.compile("列出"), Pattern.compile("所有")
        ));
        RULES.put(INTENT_RECOMMEND, List.of(
            Pattern.compile("推荐"), Pattern.compile("有什么好[的]?"), Pattern.compile("买什么"),
            Pattern.compile("热[卖销]"), Pattern.compile("畅销"), Pattern.compile("人气"),
            Pattern.compile("口碑好"), Pattern.compile("值得买"), Pattern.compile("必买"),
            Pattern.compile("新品"), Pattern.compile("新款"), Pattern.compile("最近流行"),
            Pattern.compile("送[礼人给]"), Pattern.compile("适合"), Pattern.compile("帮我选"),
            Pattern.compile("好用的"), Pattern.compile("性价比高"), Pattern.compile("划算的")
        ));
        RULES.put(INTENT_COMPARE, List.of(
            Pattern.compile("和.*哪个"), Pattern.compile("对比"), Pattern.compile("比较"),
            Pattern.compile("区别"), Pattern.compile("哪个好"), Pattern.compile("更好"),
            Pattern.compile("差异"), Pattern.compile("vs", Pattern.CASE_INSENSITIVE),
            Pattern.compile("还是"), Pattern.compile("选哪个"), Pattern.compile("二选一"),
            Pattern.compile("对比一下"), Pattern.compile("有什么不同"), Pattern.compile("优缺点")
        ));
        RULES.put(INTENT_FILTER, List.of(
            Pattern.compile("[大于高超]"), Pattern.compile("[小低少于]"), Pattern.compile("以上"),
            Pattern.compile("以下"), Pattern.compile("超过"), Pattern.compile("低于"),
            Pattern.compile("价格.*\\d"), Pattern.compile("\\d+.*元"), Pattern.compile("以内"),
            Pattern.compile("之间"), Pattern.compile("排序"), Pattern.compile("最[大便高长重贵便宜]"),
            Pattern.compile("不超过"), Pattern.compile("至少"), Pattern.compile("左右"),
            Pattern.compile("预算"), Pattern.compile("价位在"), Pattern.compile("价格范围")
        ));
        RULES.put(INTENT_SHIPPING, List.of(
            Pattern.compile("快递"), Pattern.compile("发货"), Pattern.compile("物流"),
            Pattern.compile("几天到"), Pattern.compile("多久到"), Pattern.compile("包邮"),
            Pattern.compile("运费"), Pattern.compile("邮费"), Pattern.compile("顺丰"),
            Pattern.compile("中通"), Pattern.compile("圆通"), Pattern.compile("配送"),
            Pattern.compile("送达"), Pattern.compile("自提"), Pattern.compile("什么时候到"),
            Pattern.compile("发货时间"), Pattern.compile("到货"), Pattern.compile("能否加急")
        ));
        RULES.put(INTENT_PAYMENT, List.of(
            Pattern.compile("支付"), Pattern.compile("付款"), Pattern.compile("怎么付"),
            Pattern.compile("支持.*付"), Pattern.compile("微信"), Pattern.compile("支付宝"),
            Pattern.compile("银行卡"), Pattern.compile("花呗"), Pattern.compile("白条"),
            Pattern.compile("分期"), Pattern.compile("货到付款"), Pattern.compile("怎么买"),
            Pattern.compile("下单"), Pattern.compile("购买流程"), Pattern.compile("怎么结[账帐]")
        ));
        RULES.put(INTENT_AFTER_SALES, List.of(
            Pattern.compile("退[货换款]"), Pattern.compile("售后"), Pattern.compile("保修"),
            Pattern.compile("维修"), Pattern.compile("换货"), Pattern.compile("退款"),
            Pattern.compile("坏了"), Pattern.compile("质量问题"), Pattern.compile("不满意"),
            Pattern.compile("无理由"), Pattern.compile("七天"), Pattern.compile("投诉"),
            Pattern.compile("客服"), Pattern.compile("人工"), Pattern.compile("联系.*电话"),
            Pattern.compile("怎么退"), Pattern.compile("退换"), Pattern.compile("质保")
        ));
        RULES.put(INTENT_DISCOUNT, List.of(
            Pattern.compile("优惠"), Pattern.compile("打折"), Pattern.compile("促销"),
            Pattern.compile("便宜"), Pattern.compile("活动"), Pattern.compile("满减"),
            Pattern.compile("优惠券"), Pattern.compile("折扣"), Pattern.compile("特价"),
            Pattern.compile("限时"), Pattern.compile("秒杀"), Pattern.compile("抢购"),
            Pattern.compile("双11"), Pattern.compile("618"), Pattern.compile("红包"),
            Pattern.compile("能不能便宜"), Pattern.compile("最低价"), Pattern.compile("有活动吗")
        ));
    }

    /**
     * 识别用户问题的意图类型
     */
    public String recognize(String query) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (String intent : RULES.keySet()) {
            int score = 0;
            for (Pattern p : RULES.get(intent)) {
                java.util.regex.Matcher m = p.matcher(query);
                if (m.find()) score += m.group().length() * 2;  // 长度加权
            }
            scores.put(intent, score);
        }

        String best = scores.entrySet().stream()
            .max(Map.Entry.comparingByValue()).get().getKey();

        // 零分兜底：检查简单关键词
        if (scores.get(best) == 0) {
            for (String kw : List.of("品牌", "属于", "主营", "业务", "产品", "旗下", "做什么")) {
                if (query.contains(kw)) return INTENT_QUERY_RELATION;
            }
            for (String kw : List.of("推荐", "买", "选", "好", "值得")) {
                if (query.contains(kw)) return INTENT_RECOMMEND;
            }
            // 有问号或"怎么" → 默认属性查询
            if (query.contains("?") || query.contains("？") || query.contains("怎么") || query.contains("什么")) {
                return INTENT_QUERY_ATTR;
            }
            return INTENT_QUERY_ATTR;
        }
        return best;
    }

    /**
     * 从问题中提取属性名（中文 → Cypher属性名）
     */
    public String extractAttrName(String query) {
        // 按优先级排列：具体关键词在前
        Map<String, List<String>> kw = new LinkedHashMap<>();
        kw.put("价格", List.of("价格", "多少钱", "价位", "售价", "单价", "价钱"));
        kw.put("库存", List.of("库存", "有货吗", "缺货", "有没有货", "还有吗"));
        kw.put("主营业务", List.of("主营业务", "主营什么", "主营", "卖什么", "业务"));
        kw.put("品牌发源地", List.of("发源地", "哪个国家", "产地", "哪里产的", "哪国的"));
        kw.put("创立年份", List.of("创立年份", "成立时间", "哪年", "什么时候创立", "几年了"));
        kw.put("产品", List.of("产品", "哪些产品", "商品", "有哪些", "卖哪些"));
        kw.put("类目", List.of("类目", "品类", "分类", "什么类型"));
        kw.put("尺寸", List.of("尺寸", "大小", "规格", "多大", "什么尺寸"));
        kw.put("颜色", List.of("颜色", "什么颜色", "有哪些颜色", "什么色"));
        kw.put("重量", List.of("重量", "多重", "多少克", "多少千克", "kg"));
        kw.put("材质", List.of("材质", "什么材质", "什么材料", "面料"));
        kw.put("评价", List.of("评价", "口碑", "怎么样", "好用吗", "质量"));

        for (Map.Entry<String, List<String>> e : kw.entrySet()) {
            for (String k : e.getValue()) {
                if (query.contains(k)) return e.getKey();
            }
        }
        return null;
    }

    /**
     * 提取用户可能需要的数量
     */
    public int extractQuantity(String query) {
        java.util.regex.Matcher m = Pattern.compile("(\\d+)\\s*[个件台只]").matcher(query);
        if (m.find()) return Integer.parseInt(m.group(1));
        return 1;
    }
}
