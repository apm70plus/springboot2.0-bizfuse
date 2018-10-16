package com.apm70.bizfuse.web.support;

/**
 * 构建登录用户信息的接口
 * <p>
 * 业务扩展接口。根据具体业务，提供对该接口的实现，用来构建自定义的登录用户信息。
 */
public interface LoginUserBeanGenerator {

    Object getLoginUser();
}
