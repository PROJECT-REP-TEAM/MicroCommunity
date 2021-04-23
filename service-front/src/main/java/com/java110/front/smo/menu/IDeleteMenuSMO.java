package com.java110.front.smo.menu;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 添加菜单接口
 *
 * add by wuxw 2019-06-30
 */
public interface IDeleteMenuSMO {

    /**
     * 添加菜单
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> deleteMenu(IPageData pd);
}
