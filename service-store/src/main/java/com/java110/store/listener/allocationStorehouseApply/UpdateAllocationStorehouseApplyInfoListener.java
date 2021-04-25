package com.java110.store.listener.allocationStorehouseApply;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.po.allocationStorehouseApply.AllocationStorehouseApplyPo;
import com.java110.store.listener.allocationStorehouseApply.AbstractAllocationStorehouseApplyBusinessServiceDataFlowListener;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.Business;
import com.java110.store.dao.IAllocationStorehouseApplyServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 修改调拨申请信息 侦听
 *
 * 处理节点
 * 1、businessAllocationStorehouseApply:{} 调拨申请基本信息节点
 * 2、businessAllocationStorehouseApplyAttr:[{}] 调拨申请属性信息节点
 * 3、businessAllocationStorehouseApplyPhoto:[{}] 调拨申请照片信息节点
 * 4、businessAllocationStorehouseApplyCerdentials:[{}] 调拨申请证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E4%BF%AE%E6%94%B9%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("updateAllocationStorehouseApplyInfoListener")
@Transactional
public class UpdateAllocationStorehouseApplyInfoListener extends AbstractAllocationStorehouseApplyBusinessServiceDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(UpdateAllocationStorehouseApplyInfoListener.class);
    @Autowired
    private IAllocationStorehouseApplyServiceDao allocationAllocationStorehouseApplyhouseApplyServiceDaoImpl;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_UPDATE_ALLOCATION_STOREHOUSE_APPLY;
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


            //处理 businessAllocationStorehouseApply 节点
            if(data.containsKey(AllocationStorehouseApplyPo.class.getSimpleName())){
                Object _obj = data.get(AllocationStorehouseApplyPo.class.getSimpleName());
                JSONArray businessAllocationStorehouseApplys = null;
                if(_obj instanceof JSONObject){
                    businessAllocationStorehouseApplys = new JSONArray();
                    businessAllocationStorehouseApplys.add(_obj);
                }else {
                    businessAllocationStorehouseApplys = (JSONArray)_obj;
                }
                //JSONObject businessAllocationStorehouseApply = data.getJSONObject(AllocationStorehouseApplyPo.class.getSimpleName());
                for (int _allocationAllocationStorehouseApplyhouseApplyIndex = 0; _allocationAllocationStorehouseApplyhouseApplyIndex < businessAllocationStorehouseApplys.size();_allocationAllocationStorehouseApplyhouseApplyIndex++) {
                    JSONObject businessAllocationStorehouseApply = businessAllocationStorehouseApplys.getJSONObject(_allocationAllocationStorehouseApplyhouseApplyIndex);
                    doBusinessAllocationStorehouseApply(business, businessAllocationStorehouseApply);
                    if(_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("applyId", businessAllocationStorehouseApply.getString("applyId"));
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

        //调拨申请信息
        List<Map> businessAllocationStorehouseApplyInfos = allocationAllocationStorehouseApplyhouseApplyServiceDaoImpl.getBusinessAllocationStorehouseApplyInfo(info);
        if( businessAllocationStorehouseApplyInfos != null && businessAllocationStorehouseApplyInfos.size() >0) {
            for (int _allocationAllocationStorehouseApplyhouseApplyIndex = 0; _allocationAllocationStorehouseApplyhouseApplyIndex < businessAllocationStorehouseApplyInfos.size();_allocationAllocationStorehouseApplyhouseApplyIndex++) {
                Map businessAllocationStorehouseApplyInfo = businessAllocationStorehouseApplyInfos.get(_allocationAllocationStorehouseApplyhouseApplyIndex);
                flushBusinessAllocationStorehouseApplyInfo(businessAllocationStorehouseApplyInfo,StatusConstant.STATUS_CD_VALID);
                allocationAllocationStorehouseApplyhouseApplyServiceDaoImpl.updateAllocationStorehouseApplyInfoInstance(businessAllocationStorehouseApplyInfo);
                if(businessAllocationStorehouseApplyInfo.size() == 1) {
                    dataFlowContext.addParamOut("applyId", businessAllocationStorehouseApplyInfo.get("allocationAllocationStorehouseApplyhouseApply_id"));
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
        //调拨申请信息
        List<Map> allocationAllocationStorehouseApplyhouseApplyInfo = allocationAllocationStorehouseApplyhouseApplyServiceDaoImpl.getAllocationStorehouseApplyInfo(info);
        if(allocationAllocationStorehouseApplyhouseApplyInfo != null && allocationAllocationStorehouseApplyhouseApplyInfo.size() > 0){

            //调拨申请信息
            List<Map> businessAllocationStorehouseApplyInfos = allocationAllocationStorehouseApplyhouseApplyServiceDaoImpl.getBusinessAllocationStorehouseApplyInfo(delInfo);
            //除非程序出错了，这里不会为空
            if(businessAllocationStorehouseApplyInfos == null || businessAllocationStorehouseApplyInfos.size() == 0){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败（allocationAllocationStorehouseApplyhouseApply），程序内部异常,请检查！ "+delInfo);
            }
            for (int _allocationAllocationStorehouseApplyhouseApplyIndex = 0; _allocationAllocationStorehouseApplyhouseApplyIndex < businessAllocationStorehouseApplyInfos.size();_allocationAllocationStorehouseApplyhouseApplyIndex++) {
                Map businessAllocationStorehouseApplyInfo = businessAllocationStorehouseApplyInfos.get(_allocationAllocationStorehouseApplyhouseApplyIndex);
                flushBusinessAllocationStorehouseApplyInfo(businessAllocationStorehouseApplyInfo,StatusConstant.STATUS_CD_VALID);
                allocationAllocationStorehouseApplyhouseApplyServiceDaoImpl.updateAllocationStorehouseApplyInfoInstance(businessAllocationStorehouseApplyInfo);
            }
        }

    }



    /**
     * 处理 businessAllocationStorehouseApply 节点
     * @param business 总的数据节点
     * @param businessAllocationStorehouseApply 调拨申请节点
     */
    private void doBusinessAllocationStorehouseApply(Business business,JSONObject businessAllocationStorehouseApply){

        Assert.jsonObjectHaveKey(businessAllocationStorehouseApply,"applyId","businessAllocationStorehouseApply 节点下没有包含 applyId 节点");

        if(businessAllocationStorehouseApply.getString("applyId").startsWith("-")){
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"applyId 错误，不能自动生成（必须已经存在的applyId）"+businessAllocationStorehouseApply);
        }
        //自动保存DEL
        autoSaveDelBusinessAllocationStorehouseApply(business,businessAllocationStorehouseApply);

        businessAllocationStorehouseApply.put("bId",business.getbId());
        businessAllocationStorehouseApply.put("operate", StatusConstant.OPERATE_ADD);
        //保存调拨申请信息
        allocationAllocationStorehouseApplyhouseApplyServiceDaoImpl.saveBusinessAllocationStorehouseApplyInfo(businessAllocationStorehouseApply);

    }



    @Override
    public IAllocationStorehouseApplyServiceDao getAllocationStorehouseApplyServiceDaoImpl() {
        return allocationAllocationStorehouseApplyhouseApplyServiceDaoImpl;
    }

    public void setAllocationStorehouseApplyServiceDaoImpl(IAllocationStorehouseApplyServiceDao allocationAllocationStorehouseApplyhouseApplyServiceDaoImpl) {
        this.allocationAllocationStorehouseApplyhouseApplyServiceDaoImpl = allocationAllocationStorehouseApplyhouseApplyServiceDaoImpl;
    }



}
