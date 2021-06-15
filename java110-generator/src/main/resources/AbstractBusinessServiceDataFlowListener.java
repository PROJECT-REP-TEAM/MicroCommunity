package com.java110.store.listener;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.entity.center.Business;
import com.java110.core.event.service.AbstractBusinessServiceDataFlowListener;
import com.java110.store.dao.IStoreServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 商户 服务侦听 父类
 * Created by wuxw on 2018/7/4.
 */
public abstract class AbstractStoreBusinessServiceDataFlowListener extends AbstractBusinessServiceDataFlowListener{
    private final static Logger logger = LoggerFactory.getLogger(AbstractStoreBusinessServiceDataFlowListener.class);


    /**
     * 获取 DAO工具类
     * @return
     */
    public abstract IStoreServiceDao getStoreServiceDaoImpl();

    /**
     * 刷新 businessStoreInfo 数据
     * 主要将 数据库 中字段和 接口传递字段建立关系
     * @param businessStoreInfo
     */
    protected void flushBusinessStoreInfo(Map businessStoreInfo,String statusCd){
        businessStoreInfo.put("newBId",businessStoreInfo.get("b_id"));
        businessStoreInfo.put("storeId",businessStoreInfo.get("store_id"));
        businessStoreInfo.put("userId",businessStoreInfo.get("user_id"));
        businessStoreInfo.put("storeTypeCd",businessStoreInfo.get("store_type_cd"));
        businessStoreInfo.put("nearbyLandmarks",businessStoreInfo.get("nearby_landmarks"));
        businessStoreInfo.put("mapX",businessStoreInfo.get("map_x"));
        businessStoreInfo.put("mapY",businessStoreInfo.get("map_y"));
        businessStoreInfo.put("statusCd", statusCd);
    }


    /**
     * 当修改数据时，查询instance表中的数据 自动保存删除数据到business中
     * @param businessStore 商户信息
     */
    protected void autoSaveDelBusinessStore(Business business, JSONObject businessStore){
//自动插入DEL
        Map info = new HashMap();
        info.put("storeId",businessStore.getString("storeId"));
        info.put("statusCd",StatusConstant.STATUS_CD_VALID);
        Map currentStoreInfo = getStoreServiceDaoImpl().getStoreInfo(info);
        if(currentStoreInfo == null || currentStoreInfo.isEmpty()){
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"未找到需要修改数据信息，入参错误或数据有问题，请检查"+info);
        }
        currentStoreInfo.put("bId",business.getbId());
        currentStoreInfo.put("storeId",currentStoreInfo.get("store_id"));
        currentStoreInfo.put("userId",currentStoreInfo.get("user_id"));
        currentStoreInfo.put("storeTypeCd",currentStoreInfo.get("store_type_cd"));
        currentStoreInfo.put("nearbyLandmarks",currentStoreInfo.get("nearby_landmarks"));
        currentStoreInfo.put("mapX",currentStoreInfo.get("map_x"));
        currentStoreInfo.put("mapY",currentStoreInfo.get("map_y"));
        currentStoreInfo.put("operate",StatusConstant.OPERATE_DEL);
        getStoreServiceDaoImpl().saveBusinessStoreInfo(currentStoreInfo);
    }





}
