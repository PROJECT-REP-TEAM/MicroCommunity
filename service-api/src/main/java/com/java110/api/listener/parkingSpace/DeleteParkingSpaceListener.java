package com.java110.api.listener.parkingSpace;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.parkingSpace.IParkingSpaceBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.intf.community.ICommunityInnerServiceSMO;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.StringUtil;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

/**
 * 删除小区楼信息
 */
@Java110Listener("deleteParkingSpaceListener")
public class DeleteParkingSpaceListener extends AbstractServiceApiPlusListener {

    private static Logger logger = LoggerFactory.getLogger(DeleteParkingSpaceListener.class);

    @Autowired
    private IParkingSpaceBMO parkingSpaceBMOImpl;

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_DELETE_PARKING_SPACE;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }


    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        Assert.jsonObjectHaveKey(reqJson, "psId", "请求报文中未包含psId");
        Assert.jsonObjectHaveKey(reqJson, "communityId", "请求报文中未包含communityId");
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        if (reqJson.containsKey("state") && !StringUtil.isEmpty("state") && reqJson.getString("state").equals("F")) {
            parkingSpaceBMOImpl.deleteParkingSpace(reqJson, context);
        } else if (reqJson.containsKey("state") && !StringUtil.isEmpty("state") && reqJson.getString("state").equals("S")) {
            throw new IllegalArgumentException("车位已出售，不能删除！");
        } else if (reqJson.containsKey("state") && !StringUtil.isEmpty("state") && reqJson.getString("state").equals("H")) {
            throw new IllegalArgumentException("车位已出租，不能删除！");
        } else {
            throw new IllegalArgumentException("未知车位！");
        }
    }


    public ICommunityInnerServiceSMO getCommunityInnerServiceSMOImpl() {
        return communityInnerServiceSMOImpl;
    }

    public void setCommunityInnerServiceSMOImpl(ICommunityInnerServiceSMO communityInnerServiceSMOImpl) {
        this.communityInnerServiceSMOImpl = communityInnerServiceSMOImpl;
    }
}
