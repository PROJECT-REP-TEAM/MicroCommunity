package com.java110.job.adapt.payment.notice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.java110.core.factory.WechatFactory;
import com.java110.core.smo.IComputeFeeSMO;
import com.java110.dto.basePrivilege.BasePrivilegeDto;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.fee.FeeDetailDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.owner.OwnerAppUserDto;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.dto.owner.OwnerRoomRelDto;
import com.java110.dto.smallWeChat.SmallWeChatDto;
import com.java110.dto.smallWechatAttr.SmallWechatAttrDto;
import com.java110.dto.staffAppAuth.StaffAppAuthDto;
import com.java110.dto.user.UserDto;
import com.java110.entity.order.Business;
import com.java110.entity.wechat.Content;
import com.java110.entity.wechat.Data;
import com.java110.entity.wechat.PropertyFeeTemplateMessage;
import com.java110.intf.community.ICommunityInnerServiceSMO;
import com.java110.intf.fee.IFeeDetailInnerServiceSMO;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.order.IPrivilegeInnerServiceSMO;
import com.java110.intf.store.ISmallWeChatInnerServiceSMO;
import com.java110.intf.store.ISmallWechatAttrInnerServiceSMO;
import com.java110.intf.user.IOwnerAppUserInnerServiceSMO;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.intf.user.IOwnerInnerServiceSMO;
import com.java110.intf.user.IOwnerRoomRelInnerServiceSMO;
import com.java110.intf.user.IStaffAppAuthInnerServiceSMO;
import com.java110.job.adapt.DatabusAdaptImpl;
import com.java110.po.fee.PayFeeDetailPo;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.constant.WechatConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ?????????????????????
 *
 * @author fqz
 * @date 2020-12-11  18:54
 */
@Component(value = "machinePaymentNoticeAdapt")
public class MachinePaymentNoticeAdapt extends DatabusAdaptImpl {

    private static Logger logger = LoggerFactory.getLogger(MachinePaymentNoticeAdapt.class);

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMO;

    @Autowired
    private ISmallWeChatInnerServiceSMO smallWeChatInnerServiceSMOImpl;

    @Autowired
    private ISmallWechatAttrInnerServiceSMO smallWechatAttrInnerServiceSMOImpl;

    @Autowired
    private IPrivilegeInnerServiceSMO privilegeInnerServiceSMO;

    @Autowired
    private RestTemplate outRestTemplate;

    @Autowired
    private IStaffAppAuthInnerServiceSMO staffAppAuthInnerServiceSMO;

    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IFeeDetailInnerServiceSMO feeDetailInnerServiceSMOImpl;

    @Autowired
    private IComputeFeeSMO computeFeeSMOImpl;

    @Autowired
    private IOwnerRoomRelInnerServiceSMO ownerRoomRelInnerServiceSMO;

    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMO;

    @Autowired
    private IOwnerInnerServiceSMO ownerInnerServiceSMO;

    @Autowired
    private IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMO;

    //????????????????????????
    private static String sendMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";

    public final static String ALI_SMS_DOMAIN = "ALI_SMS";

    @Override
    public void execute(Business business, List<Business> businesses) {
        JSONObject data = business.getData();
        JSONArray businessPayFeeDetails = null;
        if (data == null) {
            FeeDetailDto feeDetailDto = new FeeDetailDto();
            feeDetailDto.setbId(business.getbId());
            List<FeeDetailDto> feeDetailDtos = feeDetailInnerServiceSMOImpl.queryFeeDetails(feeDetailDto);
            Assert.listOnlyOne(feeDetailDtos, "????????????????????????");
            businessPayFeeDetails = JSONArray.parseArray(JSONArray.toJSONString(feeDetailDtos, SerializerFeature.WriteDateUseDateFormat));
        } else if (data.containsKey(PayFeeDetailPo.class.getSimpleName())) {
            Object bObj = data.get(PayFeeDetailPo.class.getSimpleName());
            if (bObj instanceof JSONObject) {
                businessPayFeeDetails = new JSONArray();
                businessPayFeeDetails.add(bObj);
            } else if (bObj instanceof Map) {
                businessPayFeeDetails = new JSONArray();
                businessPayFeeDetails.add(JSONObject.parseObject(JSONObject.toJSONString(bObj)));
            } else if (bObj instanceof List) {
                businessPayFeeDetails = JSONArray.parseArray(JSONObject.toJSONString(bObj));
            } else {
                businessPayFeeDetails = (JSONArray) bObj;
            }
        } else {
            if (data instanceof JSONObject) {
                businessPayFeeDetails = new JSONArray();
                businessPayFeeDetails.add(data);
            }
        }
        for (int bPayFeeDetailIndex = 0; bPayFeeDetailIndex < businessPayFeeDetails.size(); bPayFeeDetailIndex++) {
            JSONObject businessPayFeeDetail = businessPayFeeDetails.getJSONObject(bPayFeeDetailIndex);
            doSendPayFeeDetail(business, businessPayFeeDetail);
        }
    }

    private String subDay(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = sdf.parse(date);
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(dt);
        rightNow.add(Calendar.DAY_OF_MONTH, -1);
        Date dt1 = rightNow.getTime();
        String reStr = sdf.format(dt1);
        return reStr;
    }

    private void doSendPayFeeDetail(Business business, JSONObject businessPayFeeDetail) {
        //??????????????????
        PayFeeDetailPo payFeeDetailPo = BeanConvertUtil.covertBean(businessPayFeeDetail, PayFeeDetailPo.class);
        //??????????????????
        CommunityDto communityDto = new CommunityDto();
        communityDto.setCommunityId(payFeeDetailPo.getCommunityId());
        List<CommunityDto> communityDtos = communityInnerServiceSMO.queryCommunitys(communityDto);
        FeeDto feeDto = new FeeDto();
        feeDto.setFeeId(payFeeDetailPo.getFeeId());
        feeDto.setCommunityId(payFeeDetailPo.getCommunityId());
        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);
        Assert.listOnlyOne(feeDtos, "????????????????????????");
        //??????????????????
        String feeTypeCdName = feeDtos.get(0).getFeeTypeCdName();
        //???????????????????????????????????????
        String payerObjName = computeFeeSMOImpl.getFeeObjName(feeDtos.get(0));
        //??????????????????????????????
        String startTime = DateUtil.dateTimeToDate(payFeeDetailPo.getStartTime());
        //??????????????????????????????
        String endTime = DateUtil.dateTimeToDate(payFeeDetailPo.getEndTime());
        try {
            endTime = subDay(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //????????????????????????
        String receivedAmount = payFeeDetailPo.getReceivedAmount();
        //??????????????????
        String feeTypeCd = feeDtos.get(0).getFeeTypeCd();
        //????????????????????????
        String payerObjType = feeDtos.get(0).getPayerObjType();
        //????????????
        String state = payFeeDetailPo.getState();
        //?????????
        String carNum = "";
        //?????????
        String num = "";
        //??????
        String spaceNum = "";
        if (!StringUtil.isEmpty(payerObjType) && payerObjType.equals("6666")) {
            String[] split = payerObjName.split("-");
            //????????????
            carNum = split[0];
            //???????????????
            num = split[1];
            //????????????
            spaceNum = split[2];
        }
        //??????????????????
        String name = communityDtos.get(0).getName();
        JSONObject paramIn = new JSONObject();
        paramIn.put("payFeeRoom", name + "-" + payerObjName);
        paramIn.put("feeTypeCdName", feeTypeCdName);
        paramIn.put("payFeeTime", startTime + "???" + endTime);
        paramIn.put("receivedAmount", receivedAmount);
        paramIn.put("startTime", startTime);
        paramIn.put("endTime", endTime);
        paramIn.put("payerObjType", payerObjType);
        paramIn.put("carNum", carNum);
        paramIn.put("num", num);
        paramIn.put("spaceNum", spaceNum);
        paramIn.put("state", state);
        //?????????????????????
        sendMessage(paramIn, communityDtos.get(0), payFeeDetailPo);
        if (!StringUtil.isEmpty(state) && !state.equals("1300") && feeTypeCd.equals("888800010012")) {
            //???????????????????????????????????????????????????
            sendMsg(paramIn, communityDtos.get(0), payFeeDetailPo);
        }
        if (!StringUtil.isEmpty(state) && !state.equals("1300")) {
            //?????????????????????
            publishMsg(paramIn, communityDtos.get(0), payFeeDetailPo);
        }
    }

    /**
     * ?????????????????????
     *
     * @param paramIn
     * @param communityDto
     * @param payFeeDetailPo
     */
    private void publishMsg(JSONObject paramIn, CommunityDto communityDto, PayFeeDetailPo payFeeDetailPo) {
        //?????????????????????
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
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_SUCCESS_TEMPLATE);
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
        FeeDto feeDto = new FeeDto();
        feeDto.setFeeId(payFeeDetailPo.getFeeId());
        feeDto.setCommunityId(payFeeDetailPo.getCommunityId());
        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);
        Assert.listOnlyOne(feeDtos, "???????????????");
        // ???????????????????????? ??????????????? ??????
        BasePrivilegeDto basePrivilegeDto = new BasePrivilegeDto();
        //basePrivilegeDto.setPId("502020121454780004");
        basePrivilegeDto.setResource("/wechatNotification");
        basePrivilegeDto.setStoreId(feeDtos.get(0).getIncomeObjId());
        List<UserDto> userDtos = privilegeInnerServiceSMO.queryPrivilegeUsers(basePrivilegeDto);
        String sendTemplate = MappingCache.getValue(WechatConstant.WECHAT_DOMAIN, WechatConstant.SEND_TEMPLATE_URL);
        if (StringUtil.isEmpty(sendTemplate)) {
            sendTemplate = sendMsgUrl;
        }
        String url = sendTemplate + accessToken;
        //????????????????????????
        String payerObjType = paramIn.getString("payerObjType");
        if (userDtos != null && userDtos.size() > 0) {
            for (UserDto userDto : userDtos) {
                //?????? userId ?????????openId
                try {
                    StaffAppAuthDto staffAppAuthDto = new StaffAppAuthDto();
                    staffAppAuthDto.setStaffId(userDto.getUserId());
                    staffAppAuthDto.setAppType("WECHAT");
                    List<StaffAppAuthDto> staffAppAuthDtos = staffAppAuthInnerServiceSMO.queryStaffAppAuths(staffAppAuthDto);
                    if (staffAppAuthDtos == null || staffAppAuthDtos.size() < 1) {
                        continue;
                    }
                    String openId = staffAppAuthDtos.get(0).getOpenId();
                    Data data = new Data();
                    PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
                    templateMessage.setTemplate_id(templateId);
                    templateMessage.setTouser(openId);
                    data.setFirst(new Content("?????????????????????"));
                    if (payerObjType.equals("3333")) {  //??????
                        data.setKeyword1(new Content(paramIn.getString("payFeeRoom")));
                        data.setKeyword2(new Content(paramIn.getString("feeTypeCdName")));
                    } else {  //??????
                        data.setKeyword1(new Content(communityDto.getName() + "-" + paramIn.getString("num") + "-" + paramIn.getString("spaceNum")));
                        data.setKeyword2(new Content(paramIn.getString("feeTypeCdName") + "-" + paramIn.getString("carNum")));
                    }
                    data.setKeyword3(new Content(paramIn.getString("payFeeTime")));
                    data.setKeyword4(new Content(paramIn.getString("receivedAmount") + "???"));
                    data.setRemark(new Content("??????????????????,???????????????????????????????????????"));
                    templateMessage.setData(data);
                    //???????????????????????????
                    String wechatUrl = MappingCache.getValue("STAFF_WECHAT_URL");
                    templateMessage.setUrl(wechatUrl);
                    logger.info("????????????????????????:{}", JSON.toJSONString(templateMessage));
                    ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, JSON.toJSONString(templateMessage), String.class);
                    logger.info("????????????????????????:{}", responseEntity);
                } catch (Exception e) {
                    logger.error("????????????????????????", e);
                }
            }
        }
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param paramIn
     * @param communityDto
     * @param payFeeDetailPo
     */
    private void sendMsg(JSONObject paramIn, CommunityDto communityDto, PayFeeDetailPo payFeeDetailPo) {
        //?????????????????????
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
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_SUCCESS_TEMPLATE);
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
        FeeDto feeDto = new FeeDto();
        feeDto.setFeeId(payFeeDetailPo.getFeeId());
        feeDto.setCommunityId(payFeeDetailPo.getCommunityId());
        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);
        Assert.listOnlyOne(feeDtos, "???????????????");
        //??????????????????,????????????????????????????????????
        String userId = feeDtos.get(0).getUserId();
        String sendTemplate = MappingCache.getValue(WechatConstant.WECHAT_DOMAIN, WechatConstant.SEND_TEMPLATE_URL);
        if (StringUtil.isEmpty(sendTemplate)) {
            sendTemplate = sendMsgUrl;
        }
        String url = sendTemplate + accessToken;
        //?????? userId ?????????openId
        try {
            StaffAppAuthDto staffAppAuthDto = new StaffAppAuthDto();
            staffAppAuthDto.setStaffId(userId);
            staffAppAuthDto.setAppType("WECHAT");
            List<StaffAppAuthDto> staffAppAuthDtos = staffAppAuthInnerServiceSMO.queryStaffAppAuths(staffAppAuthDto);
            Assert.listOnlyOne(staffAppAuthDtos, "???????????????");
            String openId = staffAppAuthDtos.get(0).getOpenId();
            Data data = new Data();
            PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
            templateMessage.setTemplate_id(templateId);
            templateMessage.setTouser(openId);
            data.setFirst(new Content("??????????????????????????????????????????"));
            data.setKeyword1(new Content(paramIn.getString("payFeeRoom")));
            data.setKeyword2(new Content(paramIn.getString("feeTypeCdName")));
            data.setKeyword3(new Content(paramIn.getString("payFeeTime")));
            data.setKeyword4(new Content(paramIn.getString("receivedAmount") + "???"));
            data.setRemark(new Content("??????????????????????????????"));
            templateMessage.setData(data);
            //???????????????????????????
            String wechatUrl = MappingCache.getValue("STAFF_WECHAT_URL");
            templateMessage.setUrl(wechatUrl);
            logger.info("????????????????????????:{}", JSON.toJSONString(templateMessage));
            ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, JSON.toJSONString(templateMessage), String.class);
            logger.info("????????????????????????:{}", responseEntity);
        } catch (Exception e) {
            logger.error("????????????????????????", e);
        }
    }

    /**
     * ?????????????????????
     *
     * @param paramIn
     * @param communityDto
     * @param payFeeDetailPo
     */
    private void sendMessage(JSONObject paramIn, CommunityDto communityDto, PayFeeDetailPo payFeeDetailPo) {
        //?????????????????????
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
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_SUCCESS_TEMPLATE);
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
        FeeDto feeDto = new FeeDto();
        feeDto.setFeeId(payFeeDetailPo.getFeeId());
        feeDto.setCommunityId(payFeeDetailPo.getCommunityId());
        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);
        Assert.listOnlyOne(feeDtos, "???????????????");
        //????????????id(????????????id)
        String payerObjId = feeDtos.get(0).getPayerObjId();
        //????????????(???????????????)
        String payerObjType = feeDtos.get(0).getPayerObjType();
        String ownerId = "";
        //3333 ???????????? 6666 ???????????????
        if (payerObjType.equals("3333")) {
            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            ownerRoomRelDto.setRoomId(payerObjId);
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMO.queryOwnerRoomRels(ownerRoomRelDto);
            //????????????id
            ownerId = ownerRoomRelDtos.get(0).getOwnerId();
        } else if (payerObjType.equals("6666")) {
            OwnerCarDto ownerCarDto = new OwnerCarDto();
            ownerCarDto.setCarId(payerObjId);
            List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMO.queryOwnerCars(ownerCarDto);
            Assert.listOnlyOne(ownerCarDtos, "???????????????????????????????????????");
            //????????????id
            ownerId = ownerCarDtos.get(0).getOwnerId();
        }
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setOwnerId(ownerId);
        //1001 ???????????? 1002 ????????????
        ownerDto.setOwnerTypeCd("1001");
        //????????????
        List<OwnerDto> ownerDtos = ownerInnerServiceSMO.queryOwners(ownerDto);
        Assert.listOnlyOne(ownerDtos, "???????????????????????????");
        //????????????id
        String memberId = ownerDtos.get(0).getMemberId();
        OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
        ownerAppUserDto.setMemberId(memberId);
        ownerAppUserDto.setAppType("WECHAT");
        List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMO.queryOwnerAppUsers(ownerAppUserDto);
        if (ownerAppUserDtos.size() > 0) {
            //??????openId
            String openId = ownerAppUserDtos.get(0).getOpenId();
            String url = sendMsgUrl + accessToken;
            Data data = new Data();
            PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
            templateMessage.setTemplate_id(templateId);
            templateMessage.setTouser(openId);
            if (!StringUtil.isEmpty(paramIn.getString("state")) && paramIn.getString("state").equals("1300")) {
                data.setFirst(new Content("?????????????????????"));
            } else {
                data.setFirst(new Content("?????????????????????"));
            }
            if (payerObjType.equals("3333")) {  //??????
                data.setKeyword1(new Content(paramIn.getString("payFeeRoom")));
                data.setKeyword2(new Content(paramIn.getString("feeTypeCdName")));
            } else {  //??????
                data.setKeyword1(new Content(communityDto.getName() + "-" + paramIn.getString("num") + "-" + paramIn.getString("spaceNum")));
                data.setKeyword2(new Content(paramIn.getString("feeTypeCdName") + "-" + paramIn.getString("carNum")));
            }
            data.setKeyword3(new Content(paramIn.getString("payFeeTime")));
            if (!StringUtil.isEmpty(paramIn.getString("state")) && paramIn.getString("state").equals("1300")) {
                //??????????????????
                double receivedAmount = Double.parseDouble(paramIn.getString("receivedAmount"));
                double money = receivedAmount * (-1.00);
                data.setKeyword4(new Content("??????" + money + "???"));
            } else {
                data.setKeyword4(new Content(paramIn.getString("receivedAmount") + "???"));
            }
            data.setRemark(new Content("??????????????????,???????????????????????????????????????"));
            templateMessage.setData(data);
            //???????????????????????????
            String wechatUrl = MappingCache.getValue("OWNER_WECHAT_URL");
            if(wechatUrl.contains("?")){
                wechatUrl += ( "&wAppId="+smallWeChatDtos.get(0).getAppId());
            }else{
                wechatUrl += ( "?wAppId="+smallWeChatDtos.get(0).getAppId());
            }
            templateMessage.setUrl(wechatUrl);
            logger.info("????????????????????????:{}", JSON.toJSONString(templateMessage));
            ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, JSON.toJSONString(templateMessage), String.class);
            logger.info("????????????????????????:{}", responseEntity);
        } else {
            //?????????????????????
            String tel = ownerDtos.get(0).getLink();
            //??????????????????
            String name = ownerDtos.get(0).getName();
            //??????????????????
            String feeTypeCdName = paramIn.getString("feeTypeCdName");
            //????????????????????????
            String startTime = paramIn.getString("startTime");
            //????????????????????????
            String endTime = paramIn.getString("endTime");
            //??????????????????
            String receivedAmount = paramIn.getString("receivedAmount");
            //???????????????
            String payFeeRoom = paramIn.getString("payFeeRoom");
            DefaultProfile profile = DefaultProfile.getProfile(MappingCache.getValue(ALI_SMS_DOMAIN, "region"),
                    MappingCache.getValue(ALI_SMS_DOMAIN, "accessKeyId"),
                    MappingCache.getValue(ALI_SMS_DOMAIN, "accessSecret"));
            IAcsClient client = new DefaultAcsClient(profile);

            CommonRequest request = new CommonRequest();
            request.setSysMethod(MethodType.POST);
            request.setSysDomain("dysmsapi.aliyuncs.com");
            request.setSysVersion("2017-05-25");
            request.setSysAction("SendSms");
            request.putQueryParameter("RegionId", MappingCache.getValue(ALI_SMS_DOMAIN, "region"));
            request.putQueryParameter("PhoneNumbers", tel);
            request.putQueryParameter("SignName", MappingCache.getValue(ALI_SMS_DOMAIN, "signName"));
            //??????????????????(SMS_207160078?????????????????????????????????)
            String payFeeCode = MappingCache.getValue(ALI_SMS_DOMAIN, "PayFeeCode");
            String substring = "";
            if (!StringUtil.isEmpty(payFeeCode)) {
                substring = payFeeCode.substring(0, 4);
            }
            if (substring.equals("SMS_")) {
                request.putQueryParameter("TemplateCode", payFeeCode);
            }
            request.putQueryParameter("TemplateParam", "{\"user\":" + "\"" + name + "\"" + "," + "\"house\":" + "\"" + payFeeRoom + "\"" + "," + "\"feeType\":" + "\"" + feeTypeCdName + "\"" + "," + "\"startTime\":" + "\"" + startTime + "\"" + "," + "\"endTime\":" + "\"" + endTime + "\"" + "," + "\"mount\":" + "\"" + receivedAmount + "\"" + "}");
            try {
                CommonResponse response = client.getCommonResponse(request);
                logger.debug("???????????????{}", response.getData());
            } catch (ServerException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }
    }
}
