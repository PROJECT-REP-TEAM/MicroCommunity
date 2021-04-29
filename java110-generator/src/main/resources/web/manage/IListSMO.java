package com.java110.front.smo.@@templateCode@@;

import com.java110.core.context.IPageData;
import com.java110.utils.exception.SMOException;
import org.springframework.http.ResponseEntity;
/**
 * @@templateName@@管理服务接口类
 *
 * add by wuxw 2019-06-29
 */
public interface IList@@TemplateCode@@sSMO {

    /**
     * 查询@@templateName@@信息
     * @param pd 页面数据封装
     * @return ResponseEntity 对象数据
     * @throws SMOException 业务代码层
     */
    ResponseEntity<String> list@@TemplateCode@@s(IPageData pd) throws SMOException;
}
