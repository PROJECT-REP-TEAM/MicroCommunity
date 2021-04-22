package com.java110.store.listener.resourceResourceStoreTypeType;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.Business;
import com.java110.store.dao.IResourceStoreTypeServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 删除物品类型信息 侦听
 *
 * 处理节点
 * 1、businessResourceStoreType:{} 物品类型基本信息节点
 * 2、businessResourceStoreTypeAttr:[{}] 物品类型属性信息节点
 * 3、businessResourceStoreTypePhoto:[{}] 物品类型照片信息节点
 * 4、businessResourceStoreTypeCerdentials:[{}] 物品类型证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E5%88%A0%E9%99%A4%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("deleteResourceStoreTypeInfoListener")
@Transactional
public class DeleteResourceStoreTypeInfoListener extends AbstractResourceStoreTypeBusinessServiceDataFlowListener {

    private final static Logger logger = LoggerFactory.getLogger(DeleteResourceStoreTypeInfoListener.class);
    @Autowired
    IResourceStoreTypeServiceDao resourceResourceStoreTypeTypeServiceDaoImpl;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_DELETE_RESOURCE_STORE_TYPE;
    }

    /**
     * 根据删除信息 查出Instance表中数据 保存至business表 （状态写DEL） 方便撤单时直接更新回去
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {
        JSONObject data = business.getDatas();

        Assert.notEmpty(data,"没有datas 节点，或没有子节点需要处理");

            //处理 businessResourceStoreType 节点
            if(data.containsKey(ResourceStoreTypePo.class.getSimpleName())){
                Object _obj = data.get(ResourceStoreTypePo.class.getSimpleName());
                JSONArray businessResourceStoreTypes = null;
                if(_obj instanceof JSONObject){
                    businessResourceStoreTypes = new JSONArray();
                    businessResourceStoreTypes.add(_obj);
                }else {
                    businessResourceStoreTypes = (JSONArray)_obj;
                }
                //JSONObject businessResourceStoreType = data.getJSONObject(ResourceStoreTypePo.class.getSimpleName());
                for (int _resourceResourceStoreTypeTypeIndex = 0; _resourceResourceStoreTypeTypeIndex < businessResourceStoreTypes.size();_resourceResourceStoreTypeTypeIndex++) {
                    JSONObject businessResourceStoreType = businessResourceStoreTypes.getJSONObject(_resourceResourceStoreTypeTypeIndex);
                    doBusinessResourceStoreType(business, businessResourceStoreType);
                    if(_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("rstId", businessResourceStoreType.getString("rstId"));
                    }
                }

        }


    }

    /**
     * 删除 instance数据
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {
        String bId = business.getbId();
        //Assert.hasLength(bId,"请求报文中没有包含 bId");

        //物品类型信息
        Map info = new HashMap();
        info.put("bId",business.getbId());
        info.put("operate",StatusConstant.OPERATE_DEL);

        //物品类型信息
        List<Map> businessResourceStoreTypeInfos = resourceResourceStoreTypeTypeServiceDaoImpl.getBusinessResourceStoreTypeInfo(info);
        if( businessResourceStoreTypeInfos != null && businessResourceStoreTypeInfos.size() >0) {
            for (int _resourceResourceStoreTypeTypeIndex = 0; _resourceResourceStoreTypeTypeIndex < businessResourceStoreTypeInfos.size();_resourceResourceStoreTypeTypeIndex++) {
                Map businessResourceStoreTypeInfo = businessResourceStoreTypeInfos.get(_resourceResourceStoreTypeTypeIndex);
                flushBusinessResourceStoreTypeInfo(businessResourceStoreTypeInfo,StatusConstant.STATUS_CD_INVALID);
                resourceResourceStoreTypeTypeServiceDaoImpl.updateResourceStoreTypeInfoInstance(businessResourceStoreTypeInfo);
                dataFlowContext.addParamOut("rstId",businessResourceStoreTypeInfo.get("resourceResourceStoreTypeType_id"));
            }
        }

    }

    /**
     * 撤单
     * 从business表中查询到DEL的数据 将instance中的数据更新回来
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doRecover(DataFlowContext dataFlowContext, Business business) {
        String bId = business.getbId();
        //Assert.hasLength(bId,"请求报文中没有包含 bId");
        Map info = new HashMap();
        info.put("bId",bId);
        info.put("statusCd",StatusConstant.STATUS_CD_INVALID);

        Map delInfo = new HashMap();
        delInfo.put("bId",business.getbId());
        delInfo.put("operate",StatusConstant.OPERATE_DEL);
        //物品类型信息
        List<Map> resourceResourceStoreTypeTypeInfo = resourceResourceStoreTypeTypeServiceDaoImpl.getResourceStoreTypeInfo(info);
        if(resourceResourceStoreTypeTypeInfo != null && resourceResourceStoreTypeTypeInfo.size() > 0){

            //物品类型信息
            List<Map> businessResourceStoreTypeInfos = resourceResourceStoreTypeTypeServiceDaoImpl.getBusinessResourceStoreTypeInfo(delInfo);
            //除非程序出错了，这里不会为空
            if(businessResourceStoreTypeInfos == null ||  businessResourceStoreTypeInfos.size() == 0){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败（resourceResourceStoreTypeType），程序内部异常,请检查！ "+delInfo);
            }
            for (int _resourceResourceStoreTypeTypeIndex = 0; _resourceResourceStoreTypeTypeIndex < businessResourceStoreTypeInfos.size();_resourceResourceStoreTypeTypeIndex++) {
                Map businessResourceStoreTypeInfo = businessResourceStoreTypeInfos.get(_resourceResourceStoreTypeTypeIndex);
                flushBusinessResourceStoreTypeInfo(businessResourceStoreTypeInfo,StatusConstant.STATUS_CD_VALID);
                resourceResourceStoreTypeTypeServiceDaoImpl.updateResourceStoreTypeInfoInstance(businessResourceStoreTypeInfo);
            }
        }
    }



    /**
     * 处理 businessResourceStoreType 节点
     * @param business 总的数据节点
     * @param businessResourceStoreType 物品类型节点
     */
    private void doBusinessResourceStoreType(Business business,JSONObject businessResourceStoreType){

        Assert.jsonObjectHaveKey(businessResourceStoreType,"rstId","businessResourceStoreType 节点下没有包含 rstId 节点");

        if(businessResourceStoreType.getString("rstId").startsWith("-")){
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"rstId 错误，不能自动生成（必须已经存在的rstId）"+businessResourceStoreType);
        }
        //自动插入DEL
        autoSaveDelBusinessResourceStoreType(business,businessResourceStoreType);
    }
    @Override
    public IResourceStoreTypeServiceDao getResourceStoreTypeServiceDaoImpl() {
        return resourceResourceStoreTypeTypeServiceDaoImpl;
    }

    public void setResourceStoreTypeServiceDaoImpl(IResourceStoreTypeServiceDao resourceResourceStoreTypeTypeServiceDaoImpl) {
        this.resourceResourceStoreTypeTypeServiceDaoImpl = resourceResourceStoreTypeTypeServiceDaoImpl;
    }
}
