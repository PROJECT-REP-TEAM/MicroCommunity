package com.java110.front.smo.advert;

import com.java110.core.context.IPageData;
import com.java110.utils.exception.SMOException;
import org.springframework.http.ResponseEntity;

/**
 * 发布广告管理服务接口类
 * <p>
 * add by wuxw 2019-06-29
 */
public interface IListAdvertPhotoAndVediosSMO {

    /**
     * 查询发布广告信息
     *
     * @param pd 页面数据封装
     * @return ResponseEntity 对象数据
     * @throws SMOException 业务代码层
     */
    ResponseEntity<String> listAdvertPhotoAndVideos(IPageData pd) throws SMOException;
}
