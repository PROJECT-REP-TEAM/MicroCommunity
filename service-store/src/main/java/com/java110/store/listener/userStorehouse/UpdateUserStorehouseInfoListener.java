package com.java110.store.listener.userUserStorehousehouse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.po.userStorehouse.UserStorehousePo;
import com.java110.store.listener.userStorehouse.AbstractUserStorehouseBusinessServiceDataFlowListener;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.Business;
import com.java110.store.dao.IUserStorehouseServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 修改个人物品信息 侦听
 *
 * 处理节点
 * 1、businessUserStorehouse:{} 个人物品基本信息节点
 * 2、businessUserStorehouseAttr:[{}] 个人物品属性信息节点
 * 3、businessUserStorehousePhoto:[{}] 个人物品照片信息节点
 * 4、businessUserStorehouseCerdentials:[{}] 个人物品证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E4%BF%AE%E6%94%B9%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("updateUserStorehouseInfoListener")
@Transactional
public class UpdateUserStorehouseInfoListener extends AbstractUserStorehouseBusinessServiceDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(UpdateUserStorehouseInfoListener.class);
    @Autowired
    private IUserStorehouseServiceDao userUserStorehousehouseServiceDaoImpl;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_UPDATE_USER_STOREHOUSE;
    }

    /**
     * business过程
     * @param dataFlowContext 上下文对象
     * @param business 业务对象
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {

        JSONObject data = business.getDatas();

        Assert.notEmpty(data,"没有datas 节点，或没有子节点需要处理");


            //处理 businessUserStorehouse 节点
            if(data.containsKey(UserStorehousePo.class.getSimpleName())){
                Object _obj = data.get(UserStorehousePo.class.getSimpleName());
                JSONArray businessUserStorehouses = null;
                if(_obj instanceof JSONObject){
                    businessUserStorehouses = new JSONArray();
                    businessUserStorehouses.add(_obj);
                }else {
                    businessUserStorehouses = (JSONArray)_obj;
                }
                //JSONObject businessUserStorehouse = data.getJSONObject(UserStorehousePo.class.getSimpleName());
                for (int _userUserStorehousehouseIndex = 0; _userUserStorehousehouseIndex < businessUserStorehouses.size();_userUserStorehousehouseIndex++) {
                    JSONObject businessUserStorehouse = businessUserStorehouses.getJSONObject(_userUserStorehousehouseIndex);
                    doBusinessUserStorehouse(business, businessUserStorehouse);
                    if(_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("usId", businessUserStorehouse.getString("usId"));
                    }
                }
            }
    }


    /**
     * business to instance 过程
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {

        JSONObject data = business.getDatas();

        Map info = new HashMap();
        info.put("bId",business.getbId());
        info.put("operate",StatusConstant.OPERATE_ADD);

        //个人物品信息
        List<Map> businessUserStorehouseInfos = userUserStorehousehouseServiceDaoImpl.getBusinessUserStorehouseInfo(info);
        if( businessUserStorehouseInfos != null && businessUserStorehouseInfos.size() >0) {
            for (int _userUserStorehousehouseIndex = 0; _userUserStorehousehouseIndex < businessUserStorehouseInfos.size();_userUserStorehousehouseIndex++) {
                Map businessUserStorehouseInfo = businessUserStorehouseInfos.get(_userUserStorehousehouseIndex);
                flushBusinessUserStorehouseInfo(businessUserStorehouseInfo,StatusConstant.STATUS_CD_VALID);
                userUserStorehousehouseServiceDaoImpl.updateUserStorehouseInfoInstance(businessUserStorehouseInfo);
                if(businessUserStorehouseInfo.size() == 1) {
                    dataFlowContext.addParamOut("usId", businessUserStorehouseInfo.get("us_id"));
                }
            }
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
        Map delInfo = new HashMap();
        delInfo.put("bId",business.getbId());
        delInfo.put("operate",StatusConstant.OPERATE_DEL);
        //个人物品信息
        List<Map> userUserStorehousehouseInfo = userUserStorehousehouseServiceDaoImpl.getUserStorehouseInfo(info);
        if(userUserStorehousehouseInfo != null && userUserStorehousehouseInfo.size() > 0){

            //个人物品信息
            List<Map> businessUserStorehouseInfos = userUserStorehousehouseServiceDaoImpl.getBusinessUserStorehouseInfo(delInfo);
            //除非程序出错了，这里不会为空
            if(businessUserStorehouseInfos == null || businessUserStorehouseInfos.size() == 0){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败（userUserStorehousehouse），程序内部异常,请检查！ "+delInfo);
            }
            for (int _userUserStorehousehouseIndex = 0; _userUserStorehousehouseIndex < businessUserStorehouseInfos.size();_userUserStorehousehouseIndex++) {
                Map businessUserStorehouseInfo = businessUserStorehouseInfos.get(_userUserStorehousehouseIndex);
                flushBusinessUserStorehouseInfo(businessUserStorehouseInfo,StatusConstant.STATUS_CD_VALID);
                userUserStorehousehouseServiceDaoImpl.updateUserStorehouseInfoInstance(businessUserStorehouseInfo);
            }
        }

    }



    /**
     * 处理 businessUserStorehouse 节点
     * @param business 总的数据节点
     * @param businessUserStorehouse 个人物品节点
     */
    private void doBusinessUserStorehouse(Business business,JSONObject businessUserStorehouse){

        Assert.jsonObjectHaveKey(businessUserStorehouse,"usId","businessUserStorehouse 节点下没有包含 usId 节点");

        if(businessUserStorehouse.getString("usId").startsWith("-")){
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"usId 错误，不能自动生成（必须已经存在的usId）"+businessUserStorehouse);
        }
        //自动保存DEL
        autoSaveDelBusinessUserStorehouse(business,businessUserStorehouse);

        businessUserStorehouse.put("bId",business.getbId());
        businessUserStorehouse.put("operate", StatusConstant.OPERATE_ADD);
        //保存个人物品信息
        userUserStorehousehouseServiceDaoImpl.saveBusinessUserStorehouseInfo(businessUserStorehouse);

    }



    @Override
    public IUserStorehouseServiceDao getUserStorehouseServiceDaoImpl() {
        return userUserStorehousehouseServiceDaoImpl;
    }

    public void setUserStorehouseServiceDaoImpl(IUserStorehouseServiceDao userUserStorehousehouseServiceDaoImpl) {
        this.userUserStorehousehouseServiceDaoImpl = userUserStorehousehouseServiceDaoImpl;
    }



}
