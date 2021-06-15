package com.java110.api.listener.@@templateCode@@;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.util.StringUtil;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.utils.util.Assert;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.AppService;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.utils.constant.ServiceCode@@TemplateCode@@Constant;




import com.java110.core.annotation.Java110Listener;
/**
 * 保存小区侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("binding@@TemplateCode@@Listener")
public class Binding@@TemplateCode@@Listener extends AbstractServiceApiListener {
    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        //Assert.hasKeyAndValue(reqJson, "xxx", "xxx");
        JSONArray infos = reqJson.getJSONArray("data");

        @@validateTemplateColumns@@
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        HttpHeaders header = new HttpHeaders();
        context.getRequestCurrentHeaders().put(CommonConstant.HTTP_ORDER_TYPE_CD, "D");
        JSONArray businesses = new JSONArray();

        AppService service = event.getAppService();


        JSONArray infos = reqJson.getJSONArray("data");


        @@doSoService@@


        JSONObject paramInObj = super.restToCenterProtocol(businesses, context.getRequestCurrentHeaders());

        //将 rest header 信息传递到下层服务中去
        super.freshHttpHeader(header, context.getRequestCurrentHeaders());

        ResponseEntity<String> responseEntity = this.callService(context, service.getServiceCode(), paramInObj);

        context.setResponseEntity(responseEntity);
    }

    @Override
    public String getServiceCode() {
        return ServiceCode@@TemplateCode@@Constant.BINDING_@@TEMPLATECODE@@;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    @@bindingMethod@@




    private boolean hasKey(JSONObject info, String key){
        if(!info.containsKey(key)
            || StringUtil.isEmpty(info.getString(key))
            || info.getString(key).startsWith("-")){
            return false;
        }
        return true;

    }

    private JSONObject getObj(JSONArray infos , String flowComponent){

        JSONObject serviceInfo = null;

        for(int infoIndex = 0 ; infoIndex < infos.size(); infoIndex ++){

            Assert.hasKeyAndValue(infos.getJSONObject(infoIndex), "flowComponent", "未包含服务流程组件名称");

            if(flowComponent.equals(infos.getJSONObject(infoIndex).getString("flowComponent"))){
                serviceInfo = infos.getJSONObject(infoIndex);
                Assert.notNull(serviceInfo, "未包含服务信息");
                return serviceInfo;
            }
         }

        throw new IllegalArgumentException("未找到组件编码为【" + flowComponent + "】数据");
     }


}
