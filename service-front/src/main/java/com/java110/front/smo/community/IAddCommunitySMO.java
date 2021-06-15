package com.java110.front.smo.community;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 添加小区接口
 *
 * add by wuxw 2019-06-30
 */
public interface IAddCommunitySMO {

    /**
     * 添加小区
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> saveCommunity(IPageData pd);
}
