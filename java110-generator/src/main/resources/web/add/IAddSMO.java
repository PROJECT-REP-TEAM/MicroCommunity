package com.java110.front.smo.@@templateCode@@;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 添加@@templateName@@接口
 *
 * add by wuxw 2019-06-30
 */
public interface IAdd@@TemplateCode@@SMO {

    /**
     * 添加@@templateName@@
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> save@@TemplateCode@@(IPageData pd);
}
