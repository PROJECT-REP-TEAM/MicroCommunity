package com.java110.job.task.fee;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.java110.core.factory.WechatFactory;
import com.java110.dto.RoomDto;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.owner.OwnerAppUserDto;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.dto.owner.OwnerRoomRelDto;
import com.java110.dto.parking.ParkingSpaceDto;
import com.java110.dto.smallWeChat.SmallWeChatDto;
import com.java110.dto.smallWechatAttr.SmallWechatAttrDto;
import com.java110.dto.task.TaskDto;
import com.java110.entity.wechat.Content;
import com.java110.entity.wechat.Data;
import com.java110.entity.wechat.PropertyFeeTemplateMessage;
import com.java110.intf.community.ICommunityInnerServiceSMO;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.store.ISmallWeChatInnerServiceSMO;
import com.java110.intf.store.ISmallWechatAttrInnerServiceSMO;
import com.java110.intf.user.IOwnerAppUserInnerServiceSMO;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.intf.user.IOwnerInnerServiceSMO;
import com.java110.intf.user.IOwnerRoomRelInnerServiceSMO;
import com.java110.job.quartz.TaskSystemQuartz;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.util.StringUtil;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author fqz
 * @Description ??????????????????
 * @date 2021-01-22 08:40
 */
@Component
public class FeeDueReminderTemplate extends TaskSystemQuartz {

    private static Logger logger = LoggerFactory.getLogger(FeeDueReminderTemplate.class);

    @Autowired
    private ISmallWeChatInnerServiceSMO smallWeChatInnerServiceSMOImpl;

    @Autowired
    private ISmallWechatAttrInnerServiceSMO smallWechatAttrInnerServiceSMOImpl;

    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMO;

    @Autowired
    private IOwnerRoomRelInnerServiceSMO ownerRoomRelInnerServiceSMO;

    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMO;

    @Autowired
    private IOwnerInnerServiceSMO ownerInnerServiceSMO;

    @Autowired
    private IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMO;

    @Autowired
    private RestTemplate outRestTemplate;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMO;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMO;

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMO;

    //???
    public static final String DOMAIN_COMMON = "DOMAIN.COMMON";

    //???(?????????)
    public static final String BLACKLIST = "blacklist";

    //???(??????????????????????????????)
    public static final String FEE_REMINDER_DAYS = "feeDueReminderDays";

    //????????????????????????
    private static String sendMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";

    //???????????????
    public final static String ALI_SMS_DOMAIN = "ALI_SMS";

    @Override
    protected void process(TaskDto taskDto) {
        logger.debug("????????????????????????????????????" + taskDto.toString());
        // ????????????
        List<CommunityDto> communityDtos = getAllCommunity();
        //??????????????????????????????
        String remark = MappingCache.getRemark(DOMAIN_COMMON, BLACKLIST);
        String[] split = remark.split(",");
        //???????????????list??????
        List<String> remarkList = Arrays.asList(split);
        for (CommunityDto communityDto : communityDtos) {
            if (remarkList.contains(communityDto.getCommunityId())) {
                continue;
            } else {
                try {
                    publishMsg(taskDto, communityDto);
                } catch (Exception e) {
                    logger.error("??????????????????", e);
                }
            }
        }
    }

    private void publishMsg(TaskDto taskDto, CommunityDto communityDto) {
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
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_EXPIRE_TEMPLATE);
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
        //??????id
        feeDto.setCommunityId(communityDto.getCommunityId());
        //????????????
        feeDto.setStatusCd("0");
        //???????????????
        feeDto.setState("2008001");
        //??????????????????
        Date date = new Date();
        feeDto.setNowDate(date);
        int page;
        int row = 50;
        int count = feeInnerServiceSMO.queryFeesCount(feeDto);
        if (count < 0) {
            return;
        }
        double num = (double) count / (double) row;
        double ceil = Math.ceil(num);
        int size = (int) ceil;
        for (int i = 0; i < size; i++) {
            page = i + 1;
            feeDto.setPage(page);
            feeDto.setRow(row);
            List<FeeDto> feeDtos = feeInnerServiceSMO.queryFees(feeDto);
            //????????????????????????(??????????????????)
            String day = MappingCache.getValue(DOMAIN_COMMON, FEE_REMINDER_DAYS);
            if (StringUtil.isEmpty(day)) {
                return;
            }
            for (FeeDto tmpFeeDto : feeDtos) {
                //??????????????????
                Date endTime = tmpFeeDto.getEndTime();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                long time1 = cal.getTimeInMillis();
                cal.setTime(endTime);
                long time2 = cal.getTimeInMillis();
                double between_days = (double) (time2 - time1) / (double) (1000 * 3600 * 24);
                double days = Math.ceil(between_days);
                int feeDay = (int) days;
                int msgTime = Integer.parseInt(day);
                int smallTime = 0;
                if (msgTime > 1) {
                    smallTime = msgTime - 1;
                }
                if (feeDay <= msgTime && feeDay > smallTime) {
                    try {
                        doSentWechat(tmpFeeDto, templateId, accessToken);
                    } catch (Exception e) {
                        logger.error("????????????", e);
                    }
                } else {
                    continue;
                }
            }
        }
    }

    private void doSentWechat(FeeDto feeDto, String templateId, String accessToken) {
        //????????????????????????(3333???????????????6666????????????)
        String payerObjType = feeDto.getPayerObjType();
        CommunityDto communityDto = new CommunityDto();
        communityDto.setStatusCd("0");
        communityDto.setCommunityId(feeDto.getCommunityId());
        List<CommunityDto> communityDtos = communityInnerServiceSMO.queryCommunitys(communityDto);
        //???????????????
        String communityName = communityDtos.get(0).getName();
        String ownerId = "";
        String location = "";
        String feeName = "";
        String msgLocation = "";
        if (payerObjType.equals("3333")) {   //??????
            //?????????id(?????????id)
            String payerObjId = feeDto.getPayerObjId();
            RoomDto roomDto = new RoomDto();
            roomDto.setStatusCd("0");
            roomDto.setCommunityId(feeDto.getCommunityId());
            roomDto.setRoomId(payerObjId);
            //??????????????????
            List<RoomDto> roomDtos = roomInnerServiceSMO.queryRooms(roomDto);
            //?????????
            String roomNum = roomDtos.get(0).getRoomNum();
            //??????
            String unitNum = roomDtos.get(0).getUnitNum();
            //??????
            String floorNum = roomDtos.get(0).getFloorNum();
            //??????
            location = floorNum + "???" + unitNum + "??????" + roomNum + "???";
            msgLocation = floorNum + "-" + unitNum + "-" + roomNum;
            feeName = "?????????";
            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            //????????????
            ownerRoomRelDto.setStatusCd("0");
            ownerRoomRelDto.setRoomId(payerObjId);
            //???????????????????????????
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMO.queryOwnerRoomRels(ownerRoomRelDto);
            //????????????id
            ownerId = ownerRoomRelDtos.get(0).getOwnerId();
        } else if (payerObjType.equals("6666")) {   //??????
            //?????????id(?????????id)
            String payerObjId = feeDto.getPayerObjId();
            OwnerCarDto ownerCarDto = new OwnerCarDto();
            //????????????
            ownerCarDto.setStatusCd("0");
            //??????id
            ownerCarDto.setCarId(payerObjId);
            //?????????????????????
            List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMO.queryOwnerCars(ownerCarDto);
            //??????id
            String psId = ownerCarDtos.get(0).getPsId();
            ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
            parkingSpaceDto.setStatusCd("0");
            parkingSpaceDto.setPsId(psId);
            parkingSpaceDto.setCommunityId(feeDto.getCommunityId());
            List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMO.queryParkingSpaces(parkingSpaceDto);
            //??????????????????
            String num = parkingSpaceDtos.get(0).getNum();
            //?????????????????????
            String areaNum = parkingSpaceDtos.get(0).getAreaNum();
            location = areaNum + "????????????" + num + "?????????";
            msgLocation = areaNum + "-" + num;
            feeName = "?????????";
            ownerId = ownerCarDtos.get(0).getOwnerId();
        }
        //??????????????????
        Date endTime = feeDto.getEndTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String finishTime = format.format(endTime);
        OwnerDto ownerDto = new OwnerDto();
        //????????????
        ownerDto.setStatusCd("0");
        //??????id
        ownerDto.setOwnerId(ownerId);
        //1001 ???????????? 1002 ????????????
        ownerDto.setOwnerTypeCd("1001");
        //?????????????????????
        List<OwnerDto> ownerDtos = ownerInnerServiceSMO.queryOwners(ownerDto);
        if (ownerDtos == null || ownerDtos.size() < 1) {
            return;
        }
        //??????????????????
        String name = ownerDtos.get(0).getName();
        //????????????id
        String memberId = ownerDtos.get(0).getMemberId();
        OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
        //????????????
        ownerAppUserDto.setStatusCd("0");
        //??????id
        ownerAppUserDto.setMemberId(memberId);
        //???????????????????????????
        ownerAppUserDto.setAppType("WECHAT");
        //??????????????????
        List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMO.queryOwnerAppUsers(ownerAppUserDto);
        //???????????????????????????????????????
        if (ownerAppUserDtos.size() > 0) {
            //??????openId
            String openId = ownerAppUserDtos.get(0).getOpenId();
            String url = sendMsgUrl + accessToken;
            Data data = new Data();
            PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
            templateMessage.setTemplate_id(templateId);
            templateMessage.setTouser(openId);
            data.setFirst(new Content("???????????????" + name + "?????????" + feeName + "????????????????????????????????????"));
            data.setKeyword1(new Content(name));
            data.setKeyword2(new Content(communityName + location));
            data.setKeyword3(new Content(finishTime));
            data.setRemark(new Content("??????????????????????????????????????????????????????"));
            templateMessage.setData(data);
            //???????????????????????????
            String wechatUrl = MappingCache.getValue("OWNER_WECHAT_URL");
            templateMessage.setUrl(wechatUrl);
            logger.info("????????????????????????:{}", JSON.toJSONString(templateMessage));
            ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, JSON.toJSONString(templateMessage), String.class);
            logger.info("????????????????????????:{}", responseEntity);
        } else {    //??????????????????????????????
            //?????????????????????
            String link = ownerDtos.get(0).getLink();
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
            request.putQueryParameter("PhoneNumbers", link);
            request.putQueryParameter("SignName", MappingCache.getValue(ALI_SMS_DOMAIN, "signName"));
            //??????????????????(SMS_210079733?????????????????????????????????)
            String feeDueReminderCode = MappingCache.getValue(ALI_SMS_DOMAIN, "FEE_DUE_REMINDER_CODE");
            String substring = "";
            if (!StringUtil.isEmpty(feeDueReminderCode)) {
                substring = feeDueReminderCode.substring(0, 4);
            }
            if (substring.equals("SMS_")) {
                request.putQueryParameter("TemplateCode", feeDueReminderCode);
            }
            request.putQueryParameter("TemplateParam", "{\"user\":" + "\"" + name + "\"" + "," + "\"community\":" + "\"" + communityName + "\"" + "," + "\"house\":" + "\"" + msgLocation + "\"" + "," + "\"feeType\":" + "\"" + feeName + "\"" + "," + "\"time\":" + "\"" + finishTime + "\"" + "}");
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
