package com.java110.store.listener;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.entity.center.Business;
import com.java110.store.dao.IStoreServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 保存 用户信息 侦听
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("saveStoreInfoListener")
@Transactional
public class SaveStoreInfoListener extends AbstractStoreBusinessServiceDataFlowListener{

    private final static Logger logger = LoggerFactory.getLogger(SaveStoreInfoListener.class);

    @Autowired
    IStoreServiceDao storeServiceDaoImpl;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_SAVE_STORE_INFO;
    }

    /**
     * 保存商户信息 business 表中
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {
        JSONObject data = business.getDatas();
        Assert.notEmpty(data,"没有datas 节点，或没有子节点需要处理");

        //处理 businessStore 节点
        if(data.containsKey("businessStore")){
            JSONObject businessStore = data.getJSONObject("businessStore");
            doBusinessStore(business,businessStore);
            dataFlowContext.addParamOut("storeId",businessStore.getString("storeId"));
        }
    }

    /**
     * business 数据转移到 instance
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {
        JSONObject data = business.getDatas();

        Map info = new HashMap();
        info.put("bId",business.getbId());
        info.put("operate",StatusConstant.OPERATE_ADD);

        //商户信息
        Map businessStoreInfo = storeServiceDaoImpl.getBusinessStoreInfo(info);
        if( businessStoreInfo != null && !businessStoreInfo.isEmpty()) {
            storeServiceDaoImpl.saveStoreInfoInstance(info);
            dataFlowContext.addParamOut("storeId",businessStoreInfo.get("store_id"));
        }
    }

    /**
     * 撤单
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doRecover(DataFlowContext dataFlowContext, Business business) {
        String bId = business.getbId();
        //Assert.hasLength(bId,"请求报文中没有包含 bId");
        Map info = new HashMap();
        info.put("bId",bId);
        info.put("statusCd",StatusConstant.STATUS_CD_VALID);
        Map paramIn = new HashMap();
        paramIn.put("bId",bId);
        paramIn.put("statusCd",StatusConstant.STATUS_CD_INVALID);
        //商户信息
        Map storeInfo = storeServiceDaoImpl.getStoreInfo(info);
        if(storeInfo != null && !storeInfo.isEmpty()){
            paramIn.put("storeId",storeInfo.get("store_id").toString());
            storeServiceDaoImpl.updateStoreInfoInstance(paramIn);
            dataFlowContext.addParamOut("storeId",storeInfo.get("store_id"));
        }
    }



    /**
     * 处理 businessStore 节点
     * @param business 总的数据节点
     * @param businessStore 商户节点
     */
    private void doBusinessStore(Business business,JSONObject businessStore){

        Assert.jsonObjectHaveKey(businessStore,"storeId","businessStore 节点下没有包含 storeId 节点");

        if(businessStore.getString("storeId").startsWith("-")){
            //刷新缓存
            flushStoreId(business.getDatas());
        }

        businessStore.put("bId",business.getbId());
        businessStore.put("operate", StatusConstant.OPERATE_ADD);
        //保存商户信息
        storeServiceDaoImpl.saveBusinessStoreInfo(businessStore);

    }

    public IStoreServiceDao getStoreServiceDaoImpl() {
        return storeServiceDaoImpl;
    }

    public void setStoreServiceDaoImpl(IStoreServiceDao storeServiceDaoImpl) {
        this.storeServiceDaoImpl = storeServiceDaoImpl;
    }
}
