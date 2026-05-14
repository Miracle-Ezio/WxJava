package com.starry.mb.auth;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starry.mb.auth.dto.LoginResponse;
import com.starry.mb.auth.dto.WxLoginRequest;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.exception.BizException;
import com.starry.mb.common.security.JwtUtil;
import com.starry.mb.customer.domain.Customer;
import com.starry.mb.customer.mapper.CustomerMapper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthService {

    private final WxMaService wxMaService;
    private final CustomerMapper customerMapper;
    private final JwtUtil jwtUtil;
    private final Long defaultTenantId;
    private final Long defaultStoreId;

    public AuthService(WxMaService wxMaService,
                       CustomerMapper customerMapper,
                       JwtUtil jwtUtil,
                       @Value("${starry.demo.default-tenant-id}") Long defaultTenantId,
                       @Value("${starry.demo.default-store-id}") Long defaultStoreId) {
        this.wxMaService = wxMaService;
        this.customerMapper = customerMapper;
        this.jwtUtil = jwtUtil;
        this.defaultTenantId = defaultTenantId;
        this.defaultStoreId = defaultStoreId;
    }

    public LoginResponse loginByCode(WxLoginRequest req) {
        WxMaJscode2SessionResult session;
        try {
            session = wxMaService.getUserService().getSessionInfo(req.getCode());
        } catch (WxErrorException e) {
            log.warn("wx code2session failed: {}", e.getError());
            throw new BizException(40001, "微信登录失败：" + e.getError().getErrorMsg());
        }

        String openid = session.getOpenid();
        String unionid = session.getUnionid();

        Customer existing = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getTenantId, defaultTenantId)
                .eq(Customer::getOpenid, openid)
                .last("LIMIT 1"));

        boolean isNew = false;
        Customer customer;
        if (existing == null) {
            isNew = true;
            customer = new Customer();
            customer.setTenantId(defaultTenantId);
            customer.setStoreId(defaultStoreId);
            customer.setOpenid(openid);
            customer.setUnionid(unionid);
            customer.setNickname(req.getNickname());
            customer.setAvatarUrl(req.getAvatarUrl());
            customer.setGender(req.getGender());
            customer.setLevelCode("STARDUST");
            customer.setTotalRecharge(BigDecimal.ZERO);
            customer.setBalance(BigDecimal.ZERO);
            customer.setStatus(1);
            customer.setFirstVisitAt(LocalDateTime.now());
            customer.setLastVisitAt(LocalDateTime.now());
            customerMapper.insert(customer);
        } else {
            customer = existing;
            customer.setLastVisitAt(LocalDateTime.now());
            if (req.getNickname() != null) customer.setNickname(req.getNickname());
            if (req.getAvatarUrl() != null) customer.setAvatarUrl(req.getAvatarUrl());
            customerMapper.updateById(customer);
        }

        PrincipalContext.Principal principal = new PrincipalContext.Principal(
                customer.getId(),
                PrincipalContext.Type.CUSTOMER,
                customer.getTenantId(),
                customer.getStoreId());

        String token = jwtUtil.issue(principal);

        return new LoginResponse(
                token,
                customer.getId(),
                customer.getNickname(),
                customer.getAvatarUrl(),
                customer.getLevelCode(),
                isNew);
    }
}
