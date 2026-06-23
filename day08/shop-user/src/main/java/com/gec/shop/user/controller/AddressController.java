package com.gec.shop.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gec.shop.common.ResultData;
import com.gec.shop.user.entity.UserAddress;
import com.gec.shop.user.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/a/address")
public class AddressController {
    @Autowired private UserAddressMapper mapper;

    @GetMapping
    public ResultData<List<UserAddress>> list(@RequestHeader("X-User-Id") Long userId) {
        return ResultData.success(mapper.selectList(
                new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId)));
    }

    @PostMapping
    public ResultData<String> add(@RequestBody UserAddress addr, @RequestHeader("X-User-Id") Long userId) {
        addr.setUserId(userId);
        mapper.insert(addr);
        return ResultData.success("新增成功");
    }

    @PutMapping("/{id}")
    public ResultData<String> update(@PathVariable Long id, @RequestBody UserAddress addr) {
        addr.setId(id);
        mapper.updateById(addr);
        return ResultData.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public ResultData<String> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return ResultData.success("删除成功");
    }
}
