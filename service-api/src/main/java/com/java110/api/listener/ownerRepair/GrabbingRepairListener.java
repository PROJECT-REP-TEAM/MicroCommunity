package com.java110.api.listener.ownerRepair;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.ownerRepair.IOwnerRepairBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.dto.repair.RepairDto;
import com.java110.dto.repair.RepairTypeUserDto;
import com.java110.dto.repair.RepairUserDto;
import com.java110.intf.community.IRepairInnerServiceSMO;
import com.java110.intf.community.IRepairTypeUserInnerServiceSMO;
import com.java110.intf.community.IRepairUserInnerServiceSMO;
import com.java110.po.owner.RepairUserPo;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ServiceCodeRepairDispatchStepConstant;
import com.java110.utils.lock.DistributedLock;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 完成报修
 * add by wuxw 2019-06-30
 */
@Java110Listener("grabbingRepairListener")
public class GrabbingRepairListener extends AbstractServiceApiPlusListener {

    private static Logger logger = LoggerFactory.getLogger(GrabbingRepairListener.class);

    @Autowired
    private IOwnerRepairBMO ownerRepairBMOImpl;

    @Autowired
    private IRepairUserInnerServiceSMO repairUserInnerServiceSMOImpl;

    @Autowired
    private IRepairInnerServiceSMO repairInnerServiceSMOImpl;

    @Autowired
    private IRepairTypeUserInnerServiceSMO repairTypeUserInnerServiceSMOImpl;

    @Autowired
    private IRepairTypeUserInnerServiceSMO repairTypeUserInnerServiceSMO;

    //域
    public static final String DOMAIN_COMMON = "DOMAIN.COMMON";

    //键(维修师傅未处理最大单数)
    public static final String REPAIR_NUMBER = "REPAIR_NUMBER";

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "repairId", "未包含报修单信息");
        Assert.hasKeyAndValue(reqJson, "communityId", "未包含小区信息");
        Assert.hasKeyAndValue(reqJson, "userId", "未包含用户ID");
        Assert.hasKeyAndValue(reqJson, "userName", "未包含用户名称");
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        String requestId = DistributedLock.getLockUUID();
        String key = this.getClass().getSimpleName() + reqJson.getString("repairId");
        try {
            DistributedLock.waitGetDistributedLock(key, requestId);
            //获取当前处理员工id
            String staffId = reqJson.getString("userId");
            /*RepairUserDto repairUser = new RepairUserDto();
            repairUser.setStaffId(staffId);
            repairUser.setState("10001"); //处理中
            int i = repairUserInnerServiceSMOImpl.queryRepairUsersCount(repairUser);*/
            RepairDto repair = new RepairDto();
            repair.setStaffId(staffId);
            repair.setCommunityId(reqJson.getString("communityId"));
            int i = repairInnerServiceSMOImpl.queryStaffRepairsCount(repair);
            //取出开关映射的值(维修师傅未处理最大单数)
            String repairNumber = MappingCache.getValue(DOMAIN_COMMON, REPAIR_NUMBER);
            if (i >= Integer.parseInt(repairNumber)) {
                ResponseEntity<String> responseEntity = ResultVo.createResponseEntity(ResultVo.CODE_BUSINESS_VERIFICATION, "您有超过" + Integer.parseInt(repairNumber) + "条未处理的订单急需处理，请处理完成后再进行抢单！");
                context.setResponseEntity(responseEntity);
                return;
            }
            RepairDto repairDtoData = new RepairDto();
            repairDtoData.setRepairId(reqJson.getString("repairId"));
            repairDtoData.setCommunityId(reqJson.getString("communityId"));
            List<RepairDto> repairDtoList = repairInnerServiceSMOImpl.queryRepairs(repairDtoData);
            if (repairDtoList != null && repairDtoList.size() != 1) {
                ResponseEntity<String> responseEntity = ResultVo.createResponseEntity(ResultVo.CODE_BUSINESS_VERIFICATION, "未找到工单信息或找到多条！");
                context.setResponseEntity(responseEntity);
                return;
            }
            //获取报修类型
            String repairType = repairDtoList.get(0).getRepairType();
            RepairTypeUserDto repairTypeUser = new RepairTypeUserDto();
            repairTypeUser.setStaffId(staffId);
            repairTypeUser.setRepairType(repairType);
            //查询工单设置表
            List<RepairTypeUserDto> repairTypeUserDtos = repairTypeUserInnerServiceSMO.queryRepairTypeUsers(repairTypeUser);
            if (repairTypeUserDtos != null && repairTypeUserDtos.size() != 1) { //报修类型设置未添加改操作的员工！
                ResponseEntity<String> responseEntity = ResultVo.createResponseEntity(ResultVo.CODE_BUSINESS_VERIFICATION, "对不起，您还没权限进行此操作，请联系管理员处理！");
                context.setResponseEntity(responseEntity);
                return;
            }
            //获取维修师傅状态
            String staffState = repairTypeUserDtos.get(0).getState();
            if (!StringUtil.isEmpty(staffState) && staffState.equals("8888")) { //离线状态
                ResponseEntity<String> responseEntity = ResultVo.createResponseEntity(ResultVo.CODE_BUSINESS_VERIFICATION, "员工处于离线状态，无法进行操作！");
                context.setResponseEntity(responseEntity);
                return;
            }
            RepairTypeUserDto repairTypeUserDto = new RepairTypeUserDto();
            repairTypeUserDto.setCommunityId(reqJson.getString("communityId"));
            repairTypeUserDto.setRepairType(repairType);
            repairTypeUserDto.setStaffId(reqJson.getString("userId"));
            int count = repairTypeUserInnerServiceSMOImpl.queryRepairTypeUsersCount(repairTypeUserDto);
            if (count < 1) {
                ResponseEntity<String> responseEntity = ResultVo.createResponseEntity(ResultVo.CODE_BUSINESS_VERIFICATION, "您没有权限抢该类型报修单！");
                context.setResponseEntity(responseEntity);
                return;
            }
            //获取报修id
            String repairId = reqJson.getString("repairId");
            RepairDto repairDto = new RepairDto();
            repairDto.setRepairId(repairId);
            List<RepairDto> repairDtos = repairInnerServiceSMOImpl.queryRepairs(repairDto);
            if (repairDtos == null || repairDtos.size() < 1) {
                ResponseEntity<String> responseEntity = ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "数据错误！");
                context.setResponseEntity(responseEntity);
            } else {
                //获取状态
                String state = repairDtos.get(0).getState();
                if (state.equals("1000")) {   //1000表示未派单
                    RepairUserDto repairUserDto = new RepairUserDto();
                    repairUserDto.setRepairId(repairId);
                    repairUserDto.setCommunityId(reqJson.getString("communityId"));
                    repairUserDto.setRepairEvent(RepairUserDto.REPAIR_EVENT_START_USER);
                    List<RepairUserDto> repairUserDtos = repairUserInnerServiceSMOImpl.queryRepairUsers(repairUserDto);
                    Assert.listOnlyOne(repairUserDtos, "未找到开始节点或找到多条");
                    String userId = reqJson.getString("userId");
                    String userName = reqJson.getString("userName");
                    RepairUserPo repairUserPo = new RepairUserPo();
                    repairUserPo.setRuId("-1");
                    repairUserPo.setStartTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
                    repairUserPo.setState(RepairUserDto.STATE_DOING);
                    repairUserPo.setRepairId(reqJson.getString("repairId"));
                    repairUserPo.setStaffId(userId);
                    repairUserPo.setStaffName(userName);
                    repairUserPo.setPreStaffId(repairUserDtos.get(0).getStaffId());
                    repairUserPo.setPreStaffName(repairUserDtos.get(0).getStaffName());
                    repairUserPo.setPreRuId(repairUserDtos.get(0).getRuId());
                    repairUserPo.setRepairEvent(RepairUserDto.REPAIR_EVENT_AUDIT_USER);
                    repairUserPo.setContext("");
                    repairUserPo.setCommunityId(reqJson.getString("communityId"));
                    super.insert(context, repairUserPo, BusinessTypeConstant.BUSINESS_TYPE_SAVE_REPAIR_USER);
                    ownerRepairBMOImpl.modifyBusinessRepairDispatch(reqJson, context, RepairDto.STATE_TAKING);
                    ResponseEntity<String> responseEntity = ResultVo.createResponseEntity(ResultVo.CODE_OK, ResultVo.MSG_OK);
                    context.setResponseEntity(responseEntity);
                } else if (state.equals("1100")) {   //1100表示接单
                    ResponseEntity<String> responseEntity = ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "该订单处于接单状态，无法进行抢单！");
                    context.setResponseEntity(responseEntity);
                } else {
                    ResponseEntity<String> responseEntity = ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "状态异常！");
                    context.setResponseEntity(responseEntity);
                }
            }
        } finally {
            DistributedLock.releaseDistributedLock(requestId, key);
        }
    }

    @Override
    public String getServiceCode() {
        return ServiceCodeRepairDispatchStepConstant.BINDING_GRABBING_REPAIR;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    public IRepairInnerServiceSMO getRepairInnerServiceSMOImpl() {
        return repairInnerServiceSMOImpl;
    }

    public void setRepairInnerServiceSMOImpl(IRepairInnerServiceSMO repairInnerServiceSMOImpl) {
        this.repairInnerServiceSMOImpl = repairInnerServiceSMOImpl;
    }
}
