package com.java110.api.listener.app;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.utils.constant.ServiceCodeAppConstant;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.intf.community.IAppInnerServiceSMO;
import com.java110.dto.app.AppDto;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.vo.api.app.ApiAppDataVo;
import com.java110.vo.api.app.ApiAppVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;


/**
 * 查询小区侦听类
 */
@Java110Listener("listAppsListener")
@Api(value = "查询应用服务")
public class ListAppsListener extends AbstractServiceApiListener {

    @Autowired
    private IAppInnerServiceSMO appInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeAppConstant.LIST_APPS;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }


    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    public IAppInnerServiceSMO getAppInnerServiceSMOImpl() {
        return appInnerServiceSMOImpl;
    }

    public void setAppInnerServiceSMOImpl(IAppInnerServiceSMO appInnerServiceSMOImpl) {
        this.appInnerServiceSMOImpl = appInnerServiceSMOImpl;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
    }

    @ApiOperation(value = "查询应用信息", notes = "test: 返回 2XX 表示服务正常", httpMethod = "GET", response = ApiAppVo.class)
    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        AppDto appDto = BeanConvertUtil.covertBean(reqJson, AppDto.class);

        int count = appInnerServiceSMOImpl.queryAppsCount(appDto);

        List<ApiAppDataVo> apps = null;

        if (count > 0) {
            apps = BeanConvertUtil.covertBeanList(appInnerServiceSMOImpl.queryApps(appDto), ApiAppDataVo.class);
        } else {
            apps = new ArrayList<>();
        }

        ApiAppVo apiAppVo = new ApiAppVo();

        apiAppVo.setTotal(count);
        apiAppVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiAppVo.setApps(apps);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiAppVo), HttpStatus.OK);

        context.setResponseEntity(responseEntity);

    }
}
