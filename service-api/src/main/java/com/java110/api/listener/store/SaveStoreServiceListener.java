package com.java110.api.listener.store;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.account.IAccountBMO;
import com.java110.api.bmo.store.IStoreBMO;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.core.factory.DataFlowFactory;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.account.AccountDto;
import com.java110.entity.center.AppService;
import com.java110.po.account.AccountPo;
import com.java110.po.store.StorePo;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.constant.StoreTypeConstant;
import com.java110.utils.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

/**
 * 保存商户信息
 * Created by Administrator on 2019/3/29.
 */
@Java110Listener("saveStoreServiceListener")
public class SaveStoreServiceListener extends AbstractServiceApiListener {

    @Autowired
    private IStoreBMO storeBMOImpl;

    @Autowired
    private IAccountBMO accountBMOImpl;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_SAVE_STORE_INFO;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }


    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {

    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        //获取数据上下文对象
        DataFlowContext dataFlowContext = event.getDataFlowContext();
        AppService service = event.getAppService();
        String paramIn = dataFlowContext.getReqData();
        Assert.isJsonObject(paramIn, "添加员工时请求参数有误，不是有效的json格式 " + paramIn);

        //校验json 格式中是否包含 name,email,levelCd,tel
        Assert.jsonObjectHaveKey(paramIn, StorePo.class.getSimpleName(), "请求参数中未包含businessStore 节点，请确认");
        JSONObject paramObj = JSONObject.parseObject(paramIn);
        HttpHeaders header = new HttpHeaders();
        dataFlowContext.getRequestCurrentHeaders().put(CommonConstant.HTTP_USER_ID, "-1");
        dataFlowContext.getRequestCurrentHeaders().put(CommonConstant.HTTP_ORDER_TYPE_CD, "D");
        JSONArray businesses = new JSONArray();
        //添加商户
        businesses.add(storeBMOImpl.addStore(paramObj));
        //添加员工
        businesses.add(storeBMOImpl.addStaff(paramObj));
        //添加公司级组织
        businesses.add(storeBMOImpl.addOrg(paramObj));
        //公司总部
        businesses.add(storeBMOImpl.addOrgHeadCompany(paramObj));
        //总部办公室
        businesses.add(storeBMOImpl.addOrgHeadPart(paramObj));
        businesses.add(storeBMOImpl.addStaffOrg(paramObj));
        businesses.add(storeBMOImpl.addPurchase(paramObj));
        //businesses.add(storeBMOImpl.addCollection(paramObj)); //物品领用 各个小区设置各自的流程  废弃次操作
        businesses.add(storeBMOImpl.contractApply(paramObj));
        businesses.add(storeBMOImpl.contractChange(paramObj));
        //物品调拨流程
        businesses.add(storeBMOImpl.allocationStorehouse(paramObj));

        //新建账户 目前只有商家创建账户
        JSONObject businessStoreObj = paramObj.getJSONObject(StorePo.class.getSimpleName());
        if (StoreTypeConstant.STORE_TYPE_MALL.equals(businessStoreObj.getString("storeTypeCd"))) {
            //物品调拨流程
            businesses.add(storeBMOImpl.addAccount(paramObj,AccountDto.ACCT_TYPE_CASH));
            businesses.add(storeBMOImpl.addAccount(paramObj,AccountDto.ACCT_TYPE_INTEGRAL));
//            businesses.add(storeBMOImpl.addAccount(paramObj,AccountDto.ACCT_TYPE_GOLD));
        }

        //super.doResponse(dataFlowContext);
        ResponseEntity<String> responseEntity = storeBMOImpl.callService(dataFlowContext, service.getServiceCode(), businesses);
        dataFlowContext.setResponseEntity(responseEntity);
        //如果不成功直接返回
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return;
        }


        //赋权
        privilegeUserDefault(dataFlowContext, paramObj);
    }


    /**
     * 用户赋权
     *
     * @return
     */
    private void privilegeUserDefault(DataFlowContext dataFlowContext, JSONObject paramObj) {
        ResponseEntity responseEntity = null;
        AppService appService = DataFlowFactory.getService(dataFlowContext.getAppId(), ServiceCodeConstant.SERVICE_CODE_SAVE_USER_DEFAULT_PRIVILEGE);
        if (appService == null) {
            responseEntity = new ResponseEntity<String>("当前没有权限访问" + ServiceCodeConstant.SERVICE_CODE_SAVE_USER_DEFAULT_PRIVILEGE, HttpStatus.UNAUTHORIZED);
            dataFlowContext.setResponseEntity(responseEntity);
            return;
        }
        String requestUrl = appService.getUrl();
        HttpHeaders header = new HttpHeaders();
        header.add(CommonConstant.HTTP_SERVICE.toLowerCase(), ServiceCodeConstant.SERVICE_CODE_SAVE_USER_DEFAULT_PRIVILEGE);
        storeBMOImpl.freshHttpHeader(header, dataFlowContext.getRequestCurrentHeaders());
        JSONObject paramInObj = new JSONObject();
        paramInObj.put("userId", paramObj.getJSONObject(StorePo.class.getSimpleName()).getString("userId"));
        paramInObj.put("storeTypeCd", paramObj.getJSONObject(StorePo.class.getSimpleName()).getString("storeTypeCd"));
        paramInObj.put("storeId", paramObj.getString("storeId"));
        paramInObj.put("userFlag", "admin");
        HttpEntity<String> httpEntity = new HttpEntity<String>(paramInObj.toJSONString(), header);
        doRequest(dataFlowContext, appService, httpEntity);
        responseEntity = dataFlowContext.getResponseEntity();

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            dataFlowContext.setResponseEntity(responseEntity);
        }
    }
}
