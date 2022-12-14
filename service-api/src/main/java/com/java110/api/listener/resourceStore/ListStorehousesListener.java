package com.java110.api.listener.resourceStore;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.dto.basePrivilege.BasePrivilegeDto;
import com.java110.dto.storehouse.StorehouseDto;
import com.java110.intf.community.IMenuInnerServiceSMO;
import com.java110.intf.store.IStorehouseInnerServiceSMO;
import com.java110.utils.constant.ServiceCodeStorehouseConstant;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查询小区侦听类
 */
@Java110Listener("listStorehousesListener")
public class ListStorehousesListener extends AbstractServiceApiListener {

    @Autowired
    private IStorehouseInnerServiceSMO storehouseInnerServiceSMOImpl;

    @Autowired
    private IMenuInnerServiceSMO menuInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeStorehouseConstant.LIST_STOREHOUSES;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }


    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    public IStorehouseInnerServiceSMO getStorehouseInnerServiceSMOImpl() {
        return storehouseInnerServiceSMOImpl;
    }

    public void setStorehouseInnerServiceSMOImpl(IStorehouseInnerServiceSMO storehouseInnerServiceSMOImpl) {
        this.storehouseInnerServiceSMOImpl = storehouseInnerServiceSMOImpl;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        StorehouseDto storehouseDto = BeanConvertUtil.covertBean(reqJson, StorehouseDto.class);

        //是否具有查看集团仓库权限
        String userId = reqJson.getString("userId");
        BasePrivilegeDto basePrivilegeDto = new BasePrivilegeDto();
        basePrivilegeDto.setResource("/viewGroupWarehouse");
        basePrivilegeDto.setUserId(userId);
        List<Map> privileges = menuInnerServiceSMOImpl.checkUserHasResource(basePrivilegeDto);
        if (privileges.size() == 0) {
            storehouseDto.setShObjIds(new String[]{reqJson.getString("communityId")});
        } else {
            if (reqJson.containsKey("operationType") && reqJson.getString("operationType").equals("1000")) {
                storehouseDto.setShType("");
            }
            storehouseDto.setShObjIds(new String[]{reqJson.getString("communityId"), reqJson.getString("storeId")});
        }
        if (reqJson.containsKey("flag") && reqJson.getString("flag").equals("0")) {//调拨申请查看所有仓库
            storehouseDto.setShObjIds(null);
        }
        int count = storehouseInnerServiceSMOImpl.queryStorehousesCount(storehouseDto);

        List<StorehouseDto> storehouseDtos = null;

        if (count > 0) {
            storehouseDtos = storehouseInnerServiceSMOImpl.queryStorehouses(storehouseDto);
        } else {
            storehouseDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reqJson.getInteger("row")), count, storehouseDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        context.setResponseEntity(responseEntity);

    }
}
