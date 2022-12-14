package com.java110.api.listener.purchaseApply;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.dto.basePrivilege.BasePrivilegeDto;
import com.java110.dto.resourceStore.ResourceStoreDto;
import com.java110.intf.community.IMenuInnerServiceSMO;
import com.java110.intf.store.IPurchaseApplyInnerServiceSMO;
import com.java110.intf.common.IPurchaseApplyUserInnerServiceSMO;
import com.java110.dto.purchaseApply.PurchaseApplyDto;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.intf.store.IResourceStoreInnerServiceSMO;
import com.java110.utils.constant.ServiceCodePurchaseApplyConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.api.purchaseApply.ApiPurchaseApplyDataVo;
import com.java110.vo.api.purchaseApply.ApiPurchaseApplyVo;
import com.java110.vo.api.purchaseApply.PurchaseApplyDetailVo;
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
@Java110Listener("listPurchaseApplysListener")
public class ListPurchaseApplysListener extends AbstractServiceApiListener {

    @Autowired
    private IPurchaseApplyInnerServiceSMO purchaseApplyInnerServiceSMOImpl;

    @Autowired
    private IPurchaseApplyUserInnerServiceSMO purchaseApplyUserInnerServiceSMOImpl;

    @Autowired
    private IMenuInnerServiceSMO menuInnerServiceSMOImpl;

    @Autowired
    private IResourceStoreInnerServiceSMO resourceStoreInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodePurchaseApplyConstant.LIST_PURCHASE_APPLY;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    public IPurchaseApplyInnerServiceSMO getPurchaseApplyInnerServiceSMOImpl() {
        return purchaseApplyInnerServiceSMOImpl;
    }

    public void setPurchaseApplyInnerServiceSMOImpl(IPurchaseApplyInnerServiceSMO purchaseApplyInnerServiceSMOImpl) {
        this.purchaseApplyInnerServiceSMOImpl = purchaseApplyInnerServiceSMOImpl;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "resOrderType", "必填，请填写订单类型");
        super.validatePageInfo(reqJson);
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        PurchaseApplyDto purchaseApplyDto = BeanConvertUtil.covertBean(reqJson, PurchaseApplyDto.class);
        //获取用户id
        String userId = reqJson.getString("userId");
        //采购申请、物品领用（管理员查看所有，员工查看当前用户相关）
        //采购申请查看、采购待办查看、采购已办查看、物品领用查看、领用待办查看、领用已办查看（不用限制员工）
        List<Map> privileges = new ArrayList<>();
        if (purchaseApplyDto.getResOrderType().equals(PurchaseApplyDto.RES_ORDER_TYPE_ENTER)) {
            //采购申请所有记录权限
            BasePrivilegeDto basePrivilegeDto = new BasePrivilegeDto();
            basePrivilegeDto.setResource("/viewPurchaseApplyManage");
            basePrivilegeDto.setUserId(userId);
            privileges = menuInnerServiceSMOImpl.checkUserHasResource(basePrivilegeDto);
        }
        if (purchaseApplyDto.getResOrderType().equals(PurchaseApplyDto.RES_ORDER_TYPE_OUT)) {
            //物品领用所有记录权限
            BasePrivilegeDto basePrivilegeDto = new BasePrivilegeDto();
            basePrivilegeDto.setResource("/viewAllItemUse");
            basePrivilegeDto.setUserId(userId);
            privileges = menuInnerServiceSMOImpl.checkUserHasResource(basePrivilegeDto);
        }
        if (privileges.size() != 0 || (!StringUtil.isEmpty(reqJson.getString("applyOrderId")))) {
            purchaseApplyDto.setUserId("");
        }
        int count = purchaseApplyInnerServiceSMOImpl.queryPurchaseApplysCount(purchaseApplyDto);
        List<ApiPurchaseApplyDataVo> purchaseApplys = null;
        if (count > 0) {
            List<PurchaseApplyDto> purchaseApplyDtos = purchaseApplyInnerServiceSMOImpl.queryPurchaseApplyAndDetails(purchaseApplyDto);
            purchaseApplyDtos = freshCurrentUser(purchaseApplyDtos);
            purchaseApplys = BeanConvertUtil.covertBeanList(purchaseApplyDtos, ApiPurchaseApplyDataVo.class);
            for (ApiPurchaseApplyDataVo apiPurchaseApplyDataVo : purchaseApplys) {
                List<PurchaseApplyDetailVo> applyDetailList = apiPurchaseApplyDataVo.getPurchaseApplyDetailVo();
                if (applyDetailList.size() > 0) {
                    StringBuffer resNames = new StringBuffer();
                    BigDecimal totalPrice = new BigDecimal(0);
                    BigDecimal purchaseTotalPrice = new BigDecimal(0);
                    Integer cursor = 0;
                    for (PurchaseApplyDetailVo purchaseApplyDetailVo : applyDetailList) {
                        ResourceStoreDto resourceStoreDto = new ResourceStoreDto();
                        resourceStoreDto.setResId(purchaseApplyDetailVo.getResId());
                        List<ResourceStoreDto> resourceStoreDtos = resourceStoreInnerServiceSMOImpl.queryResourceStores(resourceStoreDto);
                        Assert.listOnlyOne(resourceStoreDtos, "查询物品信息错误！");
                        //是否是固定物品
                        apiPurchaseApplyDataVo.setIsFixed(resourceStoreDtos.get(0).getIsFixed());
                        apiPurchaseApplyDataVo.setIsFixedName(resourceStoreDtos.get(0).getIsFixedName());
                        purchaseApplyDetailVo.setIsFixed(resourceStoreDtos.get(0).getIsFixed());
                        purchaseApplyDetailVo.setIsFixedName(resourceStoreDtos.get(0).getIsFixedName());
                        //获取仓库名称
                        String shName = resourceStoreDtos.get(0).getShName();
                        purchaseApplyDetailVo.setShName(shName);
                        cursor++;
                        if (applyDetailList.size() > 1) {
                            resNames.append(cursor + "：" + purchaseApplyDetailVo.getResName() + "      ");
                        } else {
                            resNames.append(purchaseApplyDetailVo.getResName());
                        }
                        BigDecimal price = new BigDecimal(purchaseApplyDetailVo.getPrice());
                        BigDecimal quantity = new BigDecimal(purchaseApplyDetailVo.getQuantity());
                        totalPrice = totalPrice.add(price.multiply(quantity));
                        if (!StringUtil.isEmpty(purchaseApplyDetailVo.getPurchasePrice()) && !StringUtil.isEmpty(purchaseApplyDetailVo.getPurchaseQuantity())) {
                            BigDecimal purchasePrice = new BigDecimal(purchaseApplyDetailVo.getPurchasePrice());
                            BigDecimal purchaseQuantity = new BigDecimal(purchaseApplyDetailVo.getPurchaseQuantity());
                            purchaseTotalPrice = purchaseTotalPrice.add(purchasePrice.multiply(purchaseQuantity));
                        }
                    }
                    apiPurchaseApplyDataVo.setResourceNames(resNames.toString());
                    apiPurchaseApplyDataVo.setTotalPrice(totalPrice.toString());
                    apiPurchaseApplyDataVo.setPurchaseTotalPrice(purchaseTotalPrice.toString());
                }
            }
        } else {
            purchaseApplys = new ArrayList<>();
        }
        ApiPurchaseApplyVo apiPurchaseApplyVo = new ApiPurchaseApplyVo();
        apiPurchaseApplyVo.setTotal(count);
        apiPurchaseApplyVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiPurchaseApplyVo.setPurchaseApplys(purchaseApplys);
        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiPurchaseApplyVo), HttpStatus.OK);
        context.setResponseEntity(responseEntity);
    }

    private List<PurchaseApplyDto> freshCurrentUser(List<PurchaseApplyDto> purchaseApplyDtos) {
        List<PurchaseApplyDto> tmpPurchaseApplyDtos = new ArrayList<>();
        for (PurchaseApplyDto purchaseApplyDto : purchaseApplyDtos) {
            purchaseApplyDto = purchaseApplyUserInnerServiceSMOImpl.getTaskCurrentUser(purchaseApplyDto);
            tmpPurchaseApplyDtos.add(purchaseApplyDto);
        }
        return tmpPurchaseApplyDtos;
    }
}
