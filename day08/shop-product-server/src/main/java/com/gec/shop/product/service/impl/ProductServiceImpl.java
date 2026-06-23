package com.gec.shop.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.shop.product.api.entity.Product;
import com.gec.shop.product.mapper.ProductMapper;
import com.gec.shop.product.service.IProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {
}
