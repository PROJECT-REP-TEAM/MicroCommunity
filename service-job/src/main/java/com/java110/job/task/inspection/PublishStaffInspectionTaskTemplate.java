package com.java110.job.task.inspection;

import com.alibaba.fastjson.JSON;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.core.factory.WechatFactory;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.inspectionPlan.*;
import com.java110.dto.smallWeChat.SmallWeChatDto;
import com.java110.dto.smallWechatAttr.SmallWechatAttrDto;
import com.java110.dto.staffAppAuth.StaffAppAuthDto;
import com.java110.dto.task.TaskDto;
import com.java110.entity.wechat.Content;
import com.java110.entity.wechat.Data;
import com.java110.entity.wechat.PropertyFeeTemplateMessage;
import com.java110.intf.community.*;
import com.java110.intf.store.ISmallWeChatInnerServiceSMO;
import com.java110.intf.store.ISmallWechatAttrInnerServiceSMO;
import com.java110.intf.user.IStaffAppAuthInnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.job.quartz.TaskSystemQuartz;
import com.java110.po.inspection.InspectionTaskDetailPo;
import com.java110.po.inspection.InspectionTaskPo;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.api.inspectionPoint.ApiInspectionPointDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class PublishStaffInspectionTaskTemplate extends TaskSystemQuartz {

    @Autowired
    private IInspectionPlanInnerServiceSMO inspectionPlanInnerServiceSMOImpl;

    @Autowired
    private IInspectionPlanStaffInnerServiceSMO inspectionPlanStaffInnerServiceSMOImpl;

    @Autowired
    private IInspectionRoutePointRelInnerServiceSMO inspectionRoutePointRelInnerServiceSMOImpl;


    @Autowired
    private IInspectionTaskInnerServiceSMO inspectionTaskInnerServiceSMOImpl;

    @Autowired
    private IInspectionTaskDetailInnerServiceSMO inspectionTaskDetailInnerServiceSMOImpl;

    @Autowired
    private ISmallWeChatInnerServiceSMO smallWeChatInnerServiceSMOImpl;

    @Autowired
    private ISmallWechatAttrInnerServiceSMO smallWechatAttrInnerServiceSMOImpl;

    @Autowired
    private IStaffAppAuthInnerServiceSMO staffAppAuthInnerServiceSMO;

    @Autowired
    private RestTemplate outRestTemplate;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMO;


    //????????????????????????
    private static String sendMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";

    @Override
    protected void process(TaskDto taskDto) throws Exception {
        logger.debug("????????????????????????????????????" + taskDto.toString());

        // ????????????
        List<CommunityDto> communityDtos = getAllCommunity();

        for (CommunityDto communityDto : communityDtos) {
            try {
                publishStaffInspectionTask(taskDto, communityDto);
            } catch (Exception e) {
                logger.error("??????????????????", e);
            }
        }
    }

    /**
     * ????????????
     *
     * @param taskDto
     * @param communityDto
     */
    private void publishStaffInspectionTask(TaskDto taskDto, CommunityDto communityDto) {
        InspectionTaskDetailDto inspectionTaskDetailDto = new InspectionTaskDetailDto();
        inspectionTaskDetailDto.setCommunityId(communityDto.getCommunityId());
        inspectionTaskDetailDto.setState(InspectionTaskDto.STATE_NO_START);
        inspectionTaskDetailDto.setQrCodeTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_B));
        inspectionTaskDetailDto.setSendFlag(InspectionTaskDetailDto.SEND_FLAG_N);
        List<InspectionTaskDetailDto> inspectionTaskDetailDtos = inspectionTaskDetailInnerServiceSMOImpl.queryInspectionTaskDetails(inspectionTaskDetailDto);

        for (InspectionTaskDetailDto inspectionTaskDetailDto1 : inspectionTaskDetailDtos) {
            dealInspectionTaskDetail(inspectionTaskDetailDto1, taskDto, communityDto);
        }
    }

    /**
     * ??????????????????
     *
     * @param inspectionTaskDetailDto
     * @param taskDto
     * @param communityDto
     */
    private void dealInspectionTaskDetail(InspectionTaskDetailDto inspectionTaskDetailDto, TaskDto taskDto, CommunityDto communityDto) {

        String startTime = inspectionTaskDetailDto.getPointStartTime();

        startTime = DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_B) + " " + startTime + ":00";
        Date startDate = null;
        try {
            startDate = DateUtil.getDateFromString(startTime, DateUtil.DATE_FORMATE_STRING_A);
        } catch (ParseException e) {
            e.printStackTrace();
            startDate = new Date();
        }

        //??????????????????
        if (startDate.getTime() > DateUtil.getCurrentDate().getTime()) {
            return;
        }
        SmallWeChatDto smallWeChatDto = new SmallWeChatDto();
        smallWeChatDto.setWeChatType("1100");
        smallWeChatDto.setObjType(SmallWeChatDto.OBJ_TYPE_COMMUNITY);
        smallWeChatDto.setObjId(communityDto.getCommunityId());
        List<SmallWeChatDto> smallWeChatDtos = smallWeChatInnerServiceSMOImpl.querySmallWeChats(smallWeChatDto);
        if (smallWeChatDto == null || smallWeChatDtos.size() <= 0) {
            logger.info("??????????????????????????????,????????????????????????");
            return;
        }
        SmallWeChatDto weChatDto = smallWeChatDtos.get(0);
        SmallWechatAttrDto smallWechatAttrDto = new SmallWechatAttrDto();
        smallWechatAttrDto.setCommunityId(communityDto.getCommunityId());
        smallWechatAttrDto.setWechatId(weChatDto.getWeChatId());
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_TEMPLATE);
        List<SmallWechatAttrDto> smallWechatAttrDtos = smallWechatAttrInnerServiceSMOImpl.querySmallWechatAttrs(smallWechatAttrDto);
        if (smallWechatAttrDtos == null || smallWechatAttrDtos.size() <= 0) {
            logger.info("????????????????????????????????????");
            return;
        }
        String templateId = smallWechatAttrDtos.get(0).getValue();
        String accessToken = WechatFactory.getAccessToken(weChatDto.getAppId(), weChatDto.getAppSecret());
        if (StringUtil.isEmpty(accessToken)) {
            logger.info("??????????????????,??????accessToken??????:{}", accessToken);
            return;
        }
        String url = sendMsgUrl + accessToken;
        //?????? userId ?????????openId
        StaffAppAuthDto staffAppAuthDto = new StaffAppAuthDto();
        staffAppAuthDto.setStaffId(inspectionTaskDetailDto.getPlanUserId());
        staffAppAuthDto.setAppType("WECHAT");
        List<StaffAppAuthDto> staffAppAuthDtos = staffAppAuthInnerServiceSMO.queryStaffAppAuths(staffAppAuthDto);
        if (staffAppAuthDtos == null || staffAppAuthDtos.size() < 1) {
            return;
        }
        String openId = staffAppAuthDtos.get(0).getOpenId();
        Data data = new Data();
        PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
        templateMessage.setTemplate_id(templateId);
        templateMessage.setTouser(openId);
        data.setFirst(new Content("????????????????????????????????????????????????"));
        data.setKeyword1(new Content(inspectionTaskDetailDto.getInspectionName()));
        data.setKeyword2(new Content(inspectionTaskDetailDto.getPlanInsTime()));
        data.setKeyword3(new Content(inspectionTaskDetailDto.getInspectionName() + "???????????????????????????"));
        data.setRemark(new Content("??????????????????????????????"));

        templateMessage.setData(data);

        String staffWechatUrl = MappingCache.getValue("STAFF_WECHAT_URL");
        staffWechatUrl =  staffWechatUrl
                    + "pages/excuteOneQrCodeInspection/excuteOneQrCodeInspection?inspectionId="
                    + inspectionTaskDetailDto.getInspectionId()
                    + "&inspectionName=" + inspectionTaskDetailDto.getInspectionName()
                    + "&itemId=" + inspectionTaskDetailDto.getItemId();


        templateMessage.setUrl(staffWechatUrl);
        logger.info("????????????????????????:{}", JSON.toJSONString(templateMessage));
        ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, JSON.toJSONString(templateMessage), String.class);
        logger.info("????????????????????????:{}", responseEntity);


        InspectionTaskDetailDto inspectionTaskDetailPo = new InspectionTaskDetailDto();
        inspectionTaskDetailPo.setTaskDetailId(inspectionTaskDetailDto.getTaskDetailId());
        inspectionTaskDetailPo.setSendFlag(InspectionTaskDetailDto.SEND_FLAG_Y);
        inspectionTaskDetailDto.setStatusCd("0");
        inspectionTaskDetailInnerServiceSMOImpl.updateInspectionTaskDetail(inspectionTaskDetailPo);

    }

}
