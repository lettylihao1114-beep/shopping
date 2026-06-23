package com.gec.shop.product.controller;

import com.gec.shop.common.ResultData;
import com.gec.shop.product.api.entity.Product;
import com.gec.shop.product.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private IProductService productService;

    @Value("${server.port}")
    private String port;

    /** Day02 关键：直接返回 Product，方便 RestTemplate / Feign 反序列化 */
    @GetMapping("/{pid}")
    public Product findById(@PathVariable Long pid) {
        Product product = productService.getById(pid);
        // 追加端口号，直观看到负载均衡效果
        product.setName(product.getName() + "—" + port);
        return product;
    }

    @GetMapping
    public ResultData<List<Product>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        List<Product> list = productService.lambdaQuery()
                .eq(category != null && !category.isEmpty() && !"全部".equals(category),
                        Product::getCategory, category)
                .like(keyword != null && !keyword.isEmpty(),
                        Product::getName, keyword)
                .list();
        return ResultData.success(list);
    }

    @PostMapping
    public ResultData<String> add(@RequestBody Product product) {
        productService.save(product);
        return ResultData.success("新增成功");
    }

    @PutMapping("/{pid}")
    public ResultData<String> update(@PathVariable Long pid, @RequestBody Product product) {
        product.setPid(pid);
        productService.updateById(product);
        return ResultData.success("更新成功");
    }

    @DeleteMapping("/{pid}")
    public ResultData<String> delete(@PathVariable Long pid) {
        productService.removeById(pid);
        return ResultData.success("删除成功");
    }

    /** Day08 库存扣减/恢复 — 借鉴 mall4cloud SkuStockLockFeignClient.lock() */
    @PutMapping("/{pid}/stock")
    public ResultData<String> updateStock(@PathVariable Long pid, @RequestParam Integer delta) {
        Product product = productService.getById(pid);
        if (product == null) return ResultData.fail(404, "商品不存在");
        int newStock = product.getStock() + delta;
        if (newStock < 0) return ResultData.fail(400, "库存不足");
        product.setStock(newStock);
        productService.updateById(product);
        return ResultData.success("ok");
    }
}
