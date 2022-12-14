package com.java110.api.listener.resourceStore;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.dto.PageDto;
import com.java110.dto.basePrivilege.BasePrivilegeDto;
import com.java110.dto.resourceStore.ResourceStoreDto;
import com.java110.dto.storehouse.StorehouseDto;
import com.java110.intf.community.IMenuInnerServiceSMO;
import com.java110.intf.store.IResourceStoreInnerServiceSMO;
import com.java110.utils.constant.ServiceCodeResourceStoreConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.api.resourceStore.ApiResourceStoreDataVo;
import com.java110.vo.api.resourceStore.ApiResourceStoreVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查询小区侦听类
 */
@Java110Listener("listResourceStoresListener")
public class ListResourceStoresListener extends AbstractServiceApiListener {

    @Autowired
    private IResourceStoreInnerServiceSMO resourceStoreInnerServiceSMOImpl;

    @Autowired
    private IMenuInnerServiceSMO menuInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeResourceStoreConstant.LIST_RESOURCESTORES;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    public IResourceStoreInnerServiceSMO getResourceStoreInnerServiceSMOImpl() {
        return resourceStoreInnerServiceSMOImpl;
    }

    public void setResourceStoreInnerServiceSMOImpl(IResourceStoreInnerServiceSMO resourceStoreInnerServiceSMOImpl) {
        this.resourceStoreInnerServiceSMOImpl = resourceStoreInnerServiceSMOImpl;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
        Assert.hasKeyAndValue(reqJson, "communityId", "请求报文中未包含小区ID");
        Assert.hasKeyAndValue(reqJson, "storeId", "请求报文中未包含商户ID");
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        ResourceStoreDto resourceStoreDto = BeanConvertUtil.covertBean(reqJson, ResourceStoreDto.class);
        //采购2806集团仓库 物品领用2807小区仓库  默认查询当前小区所有商品
        //是否具有查看集团仓库物品权限
        String userId = reqJson.getString("userId");
        BasePrivilegeDto basePrivilegeDto = new BasePrivilegeDto();
        basePrivilegeDto.setResource("/viewGroupResource");
        basePrivilegeDto.setUserId(userId);
        List<Map> privileges = menuInnerServiceSMOImpl.checkUserHasResource(basePrivilegeDto);
        if (reqJson.containsKey("operationType") && reqJson.getString("operationType").equals("1000") && privileges.size() > 0) {
            resourceStoreDto.setShType("");
            resourceStoreDto.setShObjIds(new String[]{reqJson.getString("communityId"), reqJson.getString("storeId")});
        } else if (StorehouseDto.SH_TYPE_COMMUNITY.equals(resourceStoreDto.getShType()) || privileges.size() == 0) {
            resourceStoreDto.setShType(StorehouseDto.SH_TYPE_COMMUNITY);
            resourceStoreDto.setShObjId(reqJson.getString("communityId"));
        }
        int count = resourceStoreInnerServiceSMOImpl.queryResourceStoresCount(resourceStoreDto);
        List<ApiResourceStoreDataVo> resourceStores = new ArrayList<>();
        //计算总价(小计)
        BigDecimal subTotalPrice = BigDecimal.ZERO;
        //计算总价(大计)
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (count > 0) {
            resourceStores = BeanConvertUtil.covertBeanList(resourceStoreInnerServiceSMOImpl.queryResourceStores(resourceStoreDto), ApiResourceStoreDataVo.class);
            //查询总价
            subTotalPrice = new BigDecimal(resourceStoreInnerServiceSMOImpl.queryResourceStoresTotalPrice(resourceStoreDto));
            resourceStoreDto.setPage(PageDto.DEFAULT_PAGE);
            totalPrice = new BigDecimal(resourceStoreInnerServiceSMOImpl.queryResourceStoresTotalPrice(resourceStoreDto));
        } else {
            resourceStores = new ArrayList<>();
        }
        ApiResourceStoreVo apiResourceStoreVo = new ApiResourceStoreVo();
        apiResourceStoreVo.setTotal(count);
        apiResourceStoreVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiResourceStoreVo.setResourceStores(resourceStores);
        apiResourceStoreVo.setTotalPrice(String.format("%.2f", totalPrice));
        apiResourceStoreVo.setSubTotal(String.format("%.2f", subTotalPrice));
        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiResourceStoreVo), HttpStatus.OK);
        context.setResponseEntity(responseEntity);
    }
}
