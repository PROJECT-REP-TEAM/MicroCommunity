package com.java110.community.cmd.community;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.org.OrgCommunityDto;
import com.java110.dto.org.OrgDto;
import com.java110.dto.org.OrgStaffRelDto;
import com.java110.dto.store.StoreDto;
import com.java110.intf.community.ICommunityInnerServiceSMO;
import com.java110.intf.store.IStoreV1InnerServiceSMO;
import com.java110.intf.user.IOrgCommunityInnerServiceSMO;
import com.java110.intf.user.IOrgInnerServiceSMO;
import com.java110.intf.user.IOrgStaffRelInnerServiceSMO;
import com.java110.utils.constant.StateConstant;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.vo.api.community.ApiCommunityDataVo;
import com.java110.vo.api.community.ApiCommunityVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
@Java110Cmd(serviceCode = "community.listMyEnteredCommunitys")
public class ListMyEnteredCommunitysCmd extends Cmd {

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMOImpl;

    @Autowired
    private IOrgStaffRelInnerServiceSMO orgStaffRelInnerServiceSMOImpl;

    @Autowired
    private IOrgCommunityInnerServiceSMO orgCommunityInnerServiceSMOImpl;

    @Autowired
    private IOrgInnerServiceSMO orgInnerServiceSMOImpl;

    @Autowired
    private IStoreV1InnerServiceSMO storeV1InnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "storeId", "请求报文中未包含商户信息");
        Assert.hasKeyAndValue(reqJson, "userId", "请求报文中未包含用户信息");
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException {

        //校验商户是否存在;
        StoreDto storeDto = new StoreDto();
        storeDto.setStoreId(reqJson.getString("storeId"));
        List<StoreDto> storeDtos = storeV1InnerServiceSMOImpl.queryStores(storeDto);

        Assert.listOnlyOne(storeDtos, "商户不存在");

        //1.0 先查询 员工对应的部门
        OrgStaffRelDto orgStaffRelDto = new OrgStaffRelDto();
        orgStaffRelDto.setStoreId(reqJson.getString("storeId"));
        orgStaffRelDto.setStaffId(reqJson.getString("userId"));
        List<OrgStaffRelDto> orgStaffRelDtos = orgStaffRelInnerServiceSMOImpl.queryOrgStaffRels(orgStaffRelDto);
        Assert.listOnlyOne(orgStaffRelDtos, "未查询到相应员工对应的部门信息或查询到多条");
        //2.0 再根据 部门对应的 小区ID查询小区信息
        OrgDto orgDto = new OrgDto();
        orgDto.setOrgId(orgStaffRelDtos.get(0).getParentOrgId());
        orgDto.setStoreId(reqJson.getString("storeId"));
        orgDto.setOrgLevel("2");
        List<OrgDto> orgDtos = orgInnerServiceSMOImpl.queryOrgs(orgDto);
        Assert.listOnlyOne(orgDtos, "根据组织ID未查询到员工对应部门信息或查询到多条数据");
        int count = 0;
        List<ApiCommunityDataVo> communitys = null;
        if ("9999".equals(orgDtos.get(0).getBelongCommunityId())) {
            CommunityDto communityDto = BeanConvertUtil.covertBean(reqJson, CommunityDto.class);
            communityDto.setMemberId(reqJson.getString("storeId"));
            communityDto.setAuditStatusCd(StateConstant.AGREE_AUDIT);
            if (reqJson.containsKey("communityName")) {
                communityDto.setName(reqJson.getString("communityName"));
            }
            count = communityInnerServiceSMOImpl.queryCommunitysCount(communityDto);
            if (count > 0) {
                communitys = BeanConvertUtil.covertBeanList(communityInnerServiceSMOImpl.queryCommunitys(communityDto), ApiCommunityDataVo.class);
            } else {
                communitys = new ArrayList<>();
            }
        } else {
            String companyOrgId = orgDtos.get(0).getOrgId();
            OrgCommunityDto orgCommunityDto = BeanConvertUtil.covertBean(reqJson, OrgCommunityDto.class);
            orgCommunityDto.setOrgId(companyOrgId);
            count = orgCommunityInnerServiceSMOImpl.queryOrgCommunitysCount(orgCommunityDto);
            if (count > 0) {
                List<OrgCommunityDto> orgCommunityDtos = orgCommunityInnerServiceSMOImpl.queryOrgCommunitys(orgCommunityDto);
                communitys = BeanConvertUtil.covertBeanList(orgCommunityDtos, ApiCommunityDataVo.class);
                for (OrgCommunityDto tmpOrgCommunityDto : orgCommunityDtos) {
                    for (ApiCommunityDataVo tmpApiCommunityDataVo : communitys) {
                        if (tmpOrgCommunityDto.getCommunityId().equals(tmpApiCommunityDataVo.getCommunityId())) {
                            tmpApiCommunityDataVo.setName(tmpOrgCommunityDto.getCommunityName());
                        }
                    }
                }
            } else {
                communitys = new ArrayList<>();
            }
        }

        //兼容 系统刚开始没有小区时
        if (communitys.size() < 1 && (StoreDto.STORE_TYPE_ADMIN.equals(storeDtos.get(0).getStoreTypeCd()) || StoreDto.STORE_TYPE_DEV.equals(storeDtos.get(0).getStoreTypeCd()))) {
            ApiCommunityDataVo apiCommunityDataVo = new ApiCommunityDataVo();
            apiCommunityDataVo.setCommunityId("-1");
            apiCommunityDataVo.setName("默认小区");
            apiCommunityDataVo.setTel("18909711234");
            apiCommunityDataVo.setState("1100");
            communitys.add(apiCommunityDataVo);
        }

        //if(communitys.size() < 1 && )
        ApiCommunityVo apiCommunityVo = new ApiCommunityVo();
        apiCommunityVo.setTotal(count);
        apiCommunityVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiCommunityVo.setCommunitys(communitys);
        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiCommunityVo), HttpStatus.OK);
        context.setResponseEntity(responseEntity);
    }
}
