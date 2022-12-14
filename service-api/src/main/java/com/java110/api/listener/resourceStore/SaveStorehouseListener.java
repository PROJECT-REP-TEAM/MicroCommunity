package com.java110.api.listener.resourceStore;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.storehouse.IStorehouseBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.dto.storehouse.StorehouseDto;
import com.java110.intf.store.IStorehouseInnerServiceSMO;
import com.java110.utils.constant.ServiceCodeStorehouseConstant;
import com.java110.utils.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

/**
 * 保存商户侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("saveStorehouseListener")
public class SaveStorehouseListener extends AbstractServiceApiPlusListener {

    @Autowired
    private IStorehouseBMO storehouseBMOImpl;

    @Autowired
    private IStorehouseInnerServiceSMO storehouseInnerServiceSMOImpl;

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        //Assert.hasKeyAndValue(reqJson, "xxx", "xxx");

        Assert.hasKeyAndValue(reqJson, "shName", "请求报文中未包含shName");
        Assert.hasKeyAndValue(reqJson, "shType", "请求报文中未包含shType");
        Assert.hasKeyAndValue(reqJson, "storeId", "请求报文中未包含storeId");

        StorehouseDto storehouseDto = new StorehouseDto();
        storehouseDto.setShName(reqJson.getString("shName"));
        storehouseDto.setStoreId(reqJson.getString("storeId"));
        int flag  =  storehouseInnerServiceSMOImpl.queryStorehousesCount(storehouseDto);

        if(flag > 0){
            throw new IllegalArgumentException("已存在仓库");
        }
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        storehouseBMOImpl.addStorehouse(reqJson, context);
    }

    @Override
    public String getServiceCode() {
        return ServiceCodeStorehouseConstant.ADD_STOREHOUSE;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

}
