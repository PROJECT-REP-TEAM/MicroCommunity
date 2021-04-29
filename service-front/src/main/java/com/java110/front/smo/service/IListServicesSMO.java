package com.java110.front.smo.service;

import com.java110.utils.exception.SMOException;
import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 服务管理服务接口类
 *
 * add by wuxw 2019-06-29
 */
public interface IListServicesSMO {

    /**
     * 查询服务信息
     * @param pd 页面数据封装
     * @return ResponseEntity 对象数据
     * @throws SMOException 业务代码层
     */
    ResponseEntity<String> listServices(IPageData pd) throws SMOException;
}
