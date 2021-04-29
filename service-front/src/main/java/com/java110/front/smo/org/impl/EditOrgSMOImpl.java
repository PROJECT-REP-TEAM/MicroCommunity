package com.java110.front.smo.org.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.entity.component.ComponentValidateResult;
import com.java110.utils.constant.PrivilegeCodeConstant;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.util.Assert;
import com.java110.core.context.IPageData;
import com.java110.core.component.AbstractComponentSMO;
import com.java110.front.smo.org.IEditOrgSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 添加组织管理服务实现类
 * add by wuxw 2019-06-30
 */
@Service("eidtOrgSMOImpl")
public class EditOrgSMOImpl extends AbstractComponentSMO implements IEditOrgSMO {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    protected void validate(IPageData pd, JSONObject paramIn) {

        //super.validatePageInfo(pd);

        Assert.hasKeyAndValue(paramIn, "orgId", "组织ID不能为空");
        Assert.hasKeyAndValue(paramIn, "orgName", "必填，请填写组织名称");
        Assert.hasKeyAndValue(paramIn, "orgLevel", "必填，请填写报修人名称");
        Assert.hasKeyAndValue(paramIn, "parentOrgId", "必填，请选择上级ID");
        //Assert.hasKeyAndValue(paramIn, "description", "必填，请填写描述");


        super.checkUserHasPrivilege(pd, restTemplate, PrivilegeCodeConstant.LIST_ORG);

    }

    @Override
    protected ResponseEntity<String> doBusinessProcess(IPageData pd, JSONObject paramIn) {
        ResponseEntity<String> responseEntity = null;
        //super.validateStoreStaffCommunityRelationship(pd, restTemplate);
        ComponentValidateResult result = super.validateStoreStaffCommunityRelationship(pd, restTemplate);

        paramIn.put("storeId", result.getStoreId());
        responseEntity = this.callCenterService(restTemplate, pd, paramIn.toJSONString(),
                ServiceConstant.SERVICE_API_URL + "/api/org.updateOrg",
                HttpMethod.POST);
        return responseEntity;
    }

    @Override
    public ResponseEntity<String> updateOrg(IPageData pd) {
        return super.businessProcess(pd);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
