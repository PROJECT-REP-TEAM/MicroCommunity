package com.java110.front.smo.machine;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 添加设备接口
 *
 * add by wuxw 2019-06-30
 */
public interface IAddMachineSMO {

    /**
     * 添加设备
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> saveMachine(IPageData pd);
}
