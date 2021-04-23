package com.java110.front.smo.visit;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 添加访客登记接口
 *
 * add by wuxw 2019-06-30
 */
public interface IDeleteVisitSMO {

    /**
     * 添加访客登记
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> deleteVisit(IPageData pd);
}
