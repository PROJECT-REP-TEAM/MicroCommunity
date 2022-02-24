package com.java110.job.task.wechat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Synchronized;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.core.factory.Java110ThreadPoolFactory;
import com.java110.core.factory.WechatFactory;
import com.java110.core.log.LoggerFactory;
import com.java110.core.smo.ISaveTransactionLogSMO;
import com.java110.dto.RoomDto;
import com.java110.dto.app.AppDto;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.logSystemError.LogSystemErrorDto;
import com.java110.dto.notice.NoticeDto;
import com.java110.dto.owner.OwnerAppUserDto;
import com.java110.dto.owner.OwnerRoomRelDto;
import com.java110.dto.smallWeChat.SmallWeChatDto;
import com.java110.dto.smallWechatAttr.SmallWechatAttrDto;
import com.java110.dto.task.TaskDto;
import com.java110.dto.wechatSubscribe.WechatSubscribeDto;
import com.java110.entity.wechat.Content;
import com.java110.entity.wechat.Data;
import com.java110.entity.wechat.Miniprogram;
import com.java110.entity.wechat.PropertyFeeTemplateMessage;
import com.java110.intf.community.INoticeInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.store.ISmallWeChatInnerServiceSMO;
import com.java110.intf.store.ISmallWechatAttrInnerServiceSMO;
import com.java110.intf.user.IOwnerAppUserInnerServiceSMO;
import com.java110.intf.user.IOwnerRoomRelInnerServiceSMO;
import com.java110.intf.user.IWechatSubscribeV1InnerServiceSMO;
import com.java110.job.quartz.TaskSystemQuartz;
import com.java110.po.logSystemError.LogSystemErrorPo;
import com.java110.po.transactionLog.TransactionLogPo;
import com.java110.po.wechatSubscribe.WechatSubscribePo;
import com.java110.service.smo.ISaveSystemErrorSMO;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.constant.WechatConstant;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.ExceptionUtil;
import com.java110.utils.util.StringUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @program: MicroCommunity
 * @description: 微信公众号主动推送信息
 * @author: wuxw
 * @create: 2020-06-15 13:35
 **/
@Component
public class WeChatPushMessageTemplate extends TaskSystemQuartz {

    private static Logger logger = LoggerFactory.getLogger(WeChatPushMessageTemplate.class);

    public static final int DEFAULT_THREAD_NUM = 20;
    public static final int DEFAULT_SUBSCRIBE_PERSON = 100;

    @Autowired
    private INoticeInnerServiceSMO noticeInnerServiceSMOImpl;

    @Autowired
    private ISmallWeChatInnerServiceSMO smallWeChatInnerServiceSMOImpl;

    @Autowired
    private ISmallWechatAttrInnerServiceSMO smallWechatAttrInnerServiceSMOImpl;

    @Autowired
    private IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IOwnerRoomRelInnerServiceSMO ownerRoomRelInnerServiceSMOImpl;
    @Autowired
    private ISaveTransactionLogSMO saveTransactionLogSMOImpl;

    @Autowired
    private RestTemplate outRestTemplate;

    @Autowired
    private ISaveSystemErrorSMO saveSystemErrorSMOImpl;

    @Autowired
    private IWechatSubscribeV1InnerServiceSMO wechatSubscribeV1InnerServiceSMOImpl;

    //模板信息推送地址
    private static String sendMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";
    private static String getUser = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=ACCESS_TOKEN";

    private Java110ThreadPoolFactory<PublicWeChatPushMessageTemplate> publicWeChatPushMessageTemplateJava110ThreadPoolFactory = null;


    @Override
    protected void process(TaskDto taskDto) {
        logger.debug("开始执行微信模板信息推送" + taskDto.toString());

        //创建连接池
        publicWeChatPushMessageTemplateJava110ThreadPoolFactory = Java110ThreadPoolFactory.getInstance().createThreadPool(DEFAULT_THREAD_NUM);

        // 获取小区
        List<CommunityDto> communityDtos = getAllCommunity();

        for (CommunityDto communityDto : communityDtos) {
            try {
                publishMsg(taskDto, communityDto);
            } catch (Exception e) {
                LogSystemErrorPo logSystemErrorPo = new LogSystemErrorPo();
                logSystemErrorPo.setErrId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_errId));
                logSystemErrorPo.setErrType(LogSystemErrorDto.ERR_TYPE_NOTICE);
                logSystemErrorPo.setMsg(ExceptionUtil.getStackTrace(e));
                saveSystemErrorSMOImpl.saveLog(logSystemErrorPo);
                logger.error("推送消息失败", e);
            }
        }

        publicWeChatPushMessageTemplateJava110ThreadPoolFactory.stop();
    }

    private void publishMsg(TaskDto taskDto, CommunityDto communityDto) throws Exception {

        //查询公众号配置
        SmallWeChatDto smallWeChatDto = new SmallWeChatDto();
        smallWeChatDto.setWeChatType("1100");
        smallWeChatDto.setObjType(SmallWeChatDto.OBJ_TYPE_COMMUNITY);
        smallWeChatDto.setObjId(communityDto.getCommunityId());
        List<SmallWeChatDto> smallWeChatDtos = smallWeChatInnerServiceSMOImpl.querySmallWeChats(smallWeChatDto);

        if (smallWeChatDto == null || smallWeChatDtos.size() <= 0) {
            logger.info("未配置微信公众号信息,定时任务执行结束");
            return;
        }
        SmallWeChatDto weChatDto = smallWeChatDtos.get(0);


        SmallWechatAttrDto smallWechatAttrDto = new SmallWechatAttrDto();
        smallWechatAttrDto.setCommunityId(communityDto.getCommunityId());
        smallWechatAttrDto.setWechatId(weChatDto.getWeChatId());
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_TEMPLATE);
        List<SmallWechatAttrDto> smallWechatAttrDtos = smallWechatAttrInnerServiceSMOImpl.querySmallWechatAttrs(smallWechatAttrDto);

        if (smallWechatAttrDtos == null || smallWechatAttrDtos.size() <= 0) {
            logger.info("未配置微信公众号消息模板");
            return;
        }

        String templateId = smallWechatAttrDtos.get(0).getValue();

        String accessToken = WechatFactory.getAccessToken(weChatDto.getAppId(), weChatDto.getAppSecret());

        if (accessToken == null || accessToken == "") {
            logger.info("推送微信模板,获取accessToken失败:{}", accessToken);
            return;
        }

        //
        List<NoticeDto> noticeDtos = getNotices(communityDto.getCommunityId());

        if (noticeDtos == null || noticeDtos.size() < 1) {
            return;
        }

        for (NoticeDto tmpNotice : noticeDtos) {
            try {
                doSentWechat(tmpNotice, templateId, accessToken, weChatDto);
            } catch (Exception e) {
                LogSystemErrorPo logSystemErrorPo = new LogSystemErrorPo();
                logSystemErrorPo.setErrId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_errId));
                logSystemErrorPo.setErrType(LogSystemErrorDto.ERR_TYPE_NOTICE);
                logSystemErrorPo.setMsg(ExceptionUtil.getStackTrace(e));
                saveSystemErrorSMOImpl.saveLog(logSystemErrorPo);
                logger.error("通知异常", e);
            }
        }

    }

    @Java110Synchronized(value = "communityId")
    private List<NoticeDto> getNotices(String communityId) {
        NoticeDto noticeDto = new NoticeDto();
        noticeDto.setCommunityId(communityId);
        noticeDto.setPage(1);
        noticeDto.setRow(50);
        noticeDto.setState(NoticeDto.STATE_WAIT);
        noticeDto.setNoticeTypeCd(NoticeDto.NOTICE_TYPE_OWNER_WECHAT);
        List<NoticeDto> noticeDtos = noticeInnerServiceSMOImpl.queryNotices(noticeDto);

        //更新为发送中
        for (NoticeDto noticeDto1 : noticeDtos) {
            noticeDto = new NoticeDto();
            noticeDto.setNoticeId(noticeDto1.getNoticeId());
            noticeDto.setState(NoticeDto.STATE_DOING);
            noticeDto.setCommunityId(communityId);
            noticeInnerServiceSMOImpl.updateNotice(noticeDto);
        }

        return noticeDtos;
    }

    private void doSentWechat(NoticeDto noticeDto, String templateId, String accessToken, SmallWeChatDto weChatDto) throws Exception {

//        Date startTime = DateUtil.getDateFromString(noticeDto.getStartTime(), DateUtil.DATE_FORMATE_STRING_A);
//        Date nowTime = DateUtil.getCurrentDate();
//        if (startTime.getTime() > nowTime.getTime()) { //还没有到时间
//            return;
//        }


        String objType = noticeDto.getObjType();

        switch (objType) {
            case NoticeDto.OBJ_TYPE_ALL:
                sendAllOwner(noticeDto, templateId, accessToken, weChatDto);
                break;
            case NoticeDto.OBJ_TYPE_FLOOR:
                sendFloorOwner(noticeDto, templateId, accessToken, weChatDto);
                break;
            case NoticeDto.OBJ_TYPE_UNIT:
                sendUnitOwner(noticeDto, templateId, accessToken, weChatDto);
                break;
            case NoticeDto.OBJ_TYPE_ROOM:
                sendRoomOwner(noticeDto, templateId, accessToken, weChatDto);
                break;
            case NoticeDto.OBJ_TYPE_COMMUNITY:
                sendCommunityOwner(noticeDto, templateId, accessToken, weChatDto);
                break;
        }

        NoticeDto tmpNoticeDto = new NoticeDto();
        tmpNoticeDto.setNoticeId(noticeDto.getNoticeId());
        tmpNoticeDto.setState(NoticeDto.STATE_FINISH);
        noticeInnerServiceSMOImpl.updateNotice(tmpNoticeDto);

    }

    private void sendCommunityOwner(NoticeDto noticeDto, String templateId, String accessToken, SmallWeChatDto weChatDto) {
        OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
        ownerAppUserDto.setAppType(OwnerAppUserDto.APP_TYPE_WECHAT);
        ownerAppUserDto.setCommunityId(noticeDto.getCommunityId());
        List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsers(ownerAppUserDto);
        doSend(ownerAppUserDtos, noticeDto, templateId, accessToken, weChatDto);
    }

    private void sendFloorOwner(NoticeDto noticeDto, String templateId, String accessToken, SmallWeChatDto weChatDto) {

        RoomDto roomDto = new RoomDto();
        roomDto.setCommunityId(noticeDto.getCommunityId());
        roomDto.setFloorId(noticeDto.getObjId());
        List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

        for (RoomDto tmpRoomDto : roomDtos) {
            if (!RoomDto.STATE_SELL.equals(tmpRoomDto.getState())) {
                continue;
            }
            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            ownerRoomRelDto.setRoomId(tmpRoomDto.getRoomId());
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);

            if (ownerRoomRelDtos == null || ownerRoomRelDtos.size() < 1) {
                continue;
            }
            OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
            ownerAppUserDto.setAppType(OwnerAppUserDto.APP_TYPE_WECHAT);
            ownerAppUserDto.setMemberId(ownerRoomRelDtos.get(0).getOwnerId());
            List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsers(ownerAppUserDto);
            doSend(ownerAppUserDtos, noticeDto, templateId, accessToken, weChatDto);
        }
    }

    private void sendUnitOwner(NoticeDto noticeDto, String templateId, String accessToken, SmallWeChatDto weChatDto) {

        RoomDto roomDto = new RoomDto();
        roomDto.setCommunityId(noticeDto.getCommunityId());
        roomDto.setUnitId(noticeDto.getObjId());
        List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

        for (RoomDto tmpRoomDto : roomDtos) {
            if (!RoomDto.STATE_SELL.equals(tmpRoomDto.getState())) {
                continue;
            }
            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            ownerRoomRelDto.setRoomId(tmpRoomDto.getRoomId());
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);

            if (ownerRoomRelDtos == null || ownerRoomRelDtos.size() < 1) {
                continue;
            }
            OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
            ownerAppUserDto.setAppType(OwnerAppUserDto.APP_TYPE_WECHAT);
            ownerAppUserDto.setMemberId(ownerRoomRelDtos.get(0).getOwnerId());
            List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsers(ownerAppUserDto);
            doSend(ownerAppUserDtos, noticeDto, templateId, accessToken, weChatDto);
        }
    }

    private void sendRoomOwner(NoticeDto noticeDto, String templateId, String accessToken, SmallWeChatDto weChatDto) {

        RoomDto roomDto = new RoomDto();
        roomDto.setCommunityId(noticeDto.getCommunityId());
        roomDto.setRoomId(noticeDto.getObjId());
        List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

        for (RoomDto tmpRoomDto : roomDtos) {
            if (!RoomDto.STATE_SELL.equals(tmpRoomDto.getState())) {
                continue;
            }
            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            ownerRoomRelDto.setRoomId(tmpRoomDto.getRoomId());
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);

            if (ownerRoomRelDtos == null || ownerRoomRelDtos.size() < 1) {
                continue;
            }
            OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
            ownerAppUserDto.setAppType(OwnerAppUserDto.APP_TYPE_WECHAT);
            ownerAppUserDto.setMemberId(ownerRoomRelDtos.get(0).getOwnerId());
            List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsers(ownerAppUserDto);
            doSend(ownerAppUserDtos, noticeDto, templateId, accessToken, weChatDto);
        }
    }

    private void sendAllOwner(NoticeDto noticeDto, String templateId, String accessToken, SmallWeChatDto weChatDto) {
        //判断关注表的用户量 是否大于100人
        WechatSubscribeDto wechatSubscribeDto = new WechatSubscribeDto();
        wechatSubscribeDto.setAppId(weChatDto.getAppId());
        wechatSubscribeDto.setOpenType(WechatSubscribeDto.OPEN_TYPE_WECHAT);

        int count = wechatSubscribeV1InnerServiceSMOImpl.queryWechatSubscribesCount(wechatSubscribeDto);

        //可能公众号 已经 使用了好久 但是 程序可能刚开始用
        if (count < DEFAULT_SUBSCRIBE_PERSON) {
            //doSendToOpenId(noticeDto, templateId, accessToken, "", weChatDto);
            WechatSubscribePo tmpWechatSubscribePo = new WechatSubscribePo();
            tmpWechatSubscribePo.setAppId(weChatDto.getAppId());
            wechatSubscribeV1InnerServiceSMOImpl.deleteWechatSubscribe(tmpWechatSubscribePo);
            getAllOpenId(accessToken, "", weChatDto);
        }

        List<WechatSubscribeDto> wechatSubscribeDtos = wechatSubscribeV1InnerServiceSMOImpl.queryWechatSubscribes(wechatSubscribeDto);

        if (wechatSubscribeDtos == null || wechatSubscribeDtos.size() < 1) {
            return;
        }

        String wechatUrl = MappingCache.getValue("OWNER_WECHAT_URL");
        Miniprogram miniprogram = null;
        if (wechatUrl.startsWith("https://") || wechatUrl.startsWith("http://")) {

        } else {
            miniprogram = new Miniprogram();
            miniprogram.setAppid(wechatUrl);
        }

        String sendTemplate = MappingCache.getValue(WechatConstant.WECHAT_DOMAIN, WechatConstant.SEND_TEMPLATE_URL);
        if (StringUtil.isEmpty(sendTemplate)) {
            sendTemplate = sendMsgUrl;
        }
        ResponseEntity<String> responseEntity = null;
        for (WechatSubscribeDto wechatSubscribeDto1 : wechatSubscribeDtos) {
            Date startTime = DateUtil.getCurrentDate();
            Data data = new Data();
            PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
            try {
                templateMessage.setTemplate_id(templateId);
                templateMessage.setTouser(wechatSubscribeDto1.getOpenId());
                data.setFirst(new Content(noticeDto.getTitle()));
                data.setKeyword1(new Content(noticeDto.getTitle()));
                data.setKeyword2(new Content(noticeDto.getStartTime()));
                data.setKeyword3(new Content(StringUtil.delHtmlTag(noticeDto.getContext())));
                data.setRemark(new Content("如有疑问请联系相关物业人员"));
                templateMessage.setData(data);
                if (!StringUtil.isEmpty(wechatUrl)) {
                    if (miniprogram == null) {
                        templateMessage.setUrl(wechatUrl + "/#/pages/notice/detail/detail?noticeId=" + noticeDto.getNoticeId() + "&wAppId=" + weChatDto.getAppId() + "&communityId=" + noticeDto.getCommunityId());
                    } else {
                        miniprogram.setPagepath("/pages/notice/detail/detail?noticeId=" + noticeDto.getNoticeId() + "&wAppId=" + weChatDto.getAppId() + "&communityId=" + noticeDto.getCommunityId());
                        templateMessage.setMiniprogram(miniprogram);
                    }
                }
                //并发处理
                PushWechatTemplateMessageThread pushWechatTemplateMessageThread = new PushWechatTemplateMessageThread(outRestTemplate, sendTemplate + accessToken, JSON.toJSONString(templateMessage));
                publicWeChatPushMessageTemplateJava110ThreadPoolFactory.submit(pushWechatTemplateMessageThread);
                //responseEntity = outRestTemplate.postForEntity(sendTemplate + accessToken, JSON.toJSONString(templateMessage), String.class);
            } catch (Exception e) {
                LogSystemErrorPo logSystemErrorPo = new LogSystemErrorPo();
                logSystemErrorPo.setErrId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_errId));
                logSystemErrorPo.setErrType(LogSystemErrorDto.ERR_TYPE_NOTICE);
                logSystemErrorPo.setMsg(ExceptionUtil.getStackTrace(e));
                saveSystemErrorSMOImpl.saveLog(logSystemErrorPo);
                logger.error("发送失败", e);
            } finally {
                doSaveLog(startTime, DateUtil.getCurrentDate(), "/pages/notice/detail/detail", JSON.toJSONString(templateMessage), responseEntity, wechatSubscribeDto1.getOpenId());
            }
        }
    }

    private void getAllOpenId(String accessToken, String nextOpenid, SmallWeChatDto weChatDto) {
        String url = MappingCache.getValue(WechatConstant.WECHAT_DOMAIN, WechatConstant.GET_USER_URL);
        if (StringUtil.isEmpty(url)) {
            url = getUser;
        }
        url = url.replace("ACCESS_TOKEN", accessToken);
        if (!StringUtil.isEmpty(nextOpenid)) {
            url += ("&next_openid=" + nextOpenid);
        }
        ResponseEntity<String> paramOut = outRestTemplate.getForEntity(url, String.class);

        logger.info("获取用户返回:{}", paramOut);

        if (paramOut.getStatusCode() != HttpStatus.OK) {
            throw new IllegalArgumentException(paramOut.getBody());
        }

        JSONObject paramOutObj = JSONObject.parseObject(paramOut.getBody());
        if (paramOutObj.containsKey("errcode")) {
            throw new IllegalArgumentException(paramOut.getBody());
        }

        if (!paramOutObj.containsKey("data")) {
            return;
        }
        JSONObject dataObj = paramOutObj.getJSONObject("data");
        JSONArray openids = dataObj.getJSONArray("openid");
        nextOpenid = paramOutObj.getString("next_openid");
        List<WechatSubscribePo> wechatSubscribePos = new ArrayList<>();
        for (int openIndex = 0; openIndex < openids.size(); openIndex++) {
            String openId = openids.getString(openIndex);
            WechatSubscribePo wechatSubscribePo = new WechatSubscribePo();
            wechatSubscribePo.setAppId(weChatDto.getAppId());
            wechatSubscribePo.setOpenId(openId);
            wechatSubscribePo.setSubId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_file_id));
            wechatSubscribePo.setOpenType(WechatSubscribeDto.OPEN_TYPE_WECHAT);
            wechatSubscribePos.add(wechatSubscribePo);
        }

        if (wechatSubscribePos.size() > 0) {
            wechatSubscribeV1InnerServiceSMOImpl.saveWechatSubscribes(wechatSubscribePos);
        }

        //（关注者列表已返回完时，返回next_openid为空）
        if (!StringUtil.isEmpty(nextOpenid)) {
            getAllOpenId(accessToken, nextOpenid, weChatDto);
        }
    }

    private void doSend(List<OwnerAppUserDto> ownerAppUserDtos, NoticeDto noticeDto, String templateId, String accessToken, SmallWeChatDto weChatDto) {
        String wechatUrl = MappingCache.getValue("OWNER_WECHAT_URL") + "/#/pages/notice/detail/detail?noticeId=";
        ResponseEntity<String> responseEntity = null;
        String sendTemplate = MappingCache.getValue(WechatConstant.WECHAT_DOMAIN, WechatConstant.SEND_TEMPLATE_URL);
        if (StringUtil.isEmpty(sendTemplate)) {
            sendTemplate = sendMsgUrl;
        }
        for (OwnerAppUserDto appUserDto : ownerAppUserDtos) {
            Date startTime = DateUtil.getCurrentDate();
            PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
            try {
                Data data = new Data();
                templateMessage.setTemplate_id(templateId);
                templateMessage.setTouser(appUserDto.getOpenId());
                data.setFirst(new Content(noticeDto.getTitle()));
                data.setKeyword1(new Content(noticeDto.getTitle()));
                data.setKeyword2(new Content(noticeDto.getStartTime()));
                data.setKeyword3(new Content(StringUtil.delHtmlTag(noticeDto.getContext())));
                data.setRemark(new Content("如有疑问请联系相关物业人员"));
                templateMessage.setData(data);
                templateMessage.setUrl(wechatUrl + noticeDto.getNoticeId() + "&wAppId=" + weChatDto.getAppId() + "&communityId=" + noticeDto.getCommunityId());
                logger.info("发送模板消息内容:{}", JSON.toJSONString(templateMessage));
                //responseEntity = outRestTemplate.postForEntity(sendTemplate + accessToken, JSON.toJSONString(templateMessage), String.class);
                PushWechatTemplateMessageThread pushWechatTemplateMessageThread = new PushWechatTemplateMessageThread(outRestTemplate, sendTemplate + accessToken, JSON.toJSONString(templateMessage));
                publicWeChatPushMessageTemplateJava110ThreadPoolFactory.submit(pushWechatTemplateMessageThread);
                logger.info("微信模板返回内容:{}", responseEntity);
            } catch (Exception e) {
                LogSystemErrorPo logSystemErrorPo = new LogSystemErrorPo();
                logSystemErrorPo.setErrId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_errId));
                logSystemErrorPo.setErrType(LogSystemErrorDto.ERR_TYPE_NOTICE);
                logSystemErrorPo.setMsg(ExceptionUtil.getStackTrace(e));
                saveSystemErrorSMOImpl.saveLog(logSystemErrorPo);
                logger.error("发送失败", e);
            } finally {
                doSaveLog(startTime, DateUtil.getCurrentDate(), "/pages/notice/detail/detail", JSON.toJSONString(templateMessage), responseEntity, appUserDto.getOpenId());
            }
        }
    }

    private void doSaveLog(Date startDate, Date endDate, String serviceCode, String reqJson, ResponseEntity<String> responseEntity, String userId) {
        try {
            TransactionLogPo transactionLogPo = new TransactionLogPo();
            transactionLogPo.setAppId(AppDto.OWNER_WECHAT_PAY);
            transactionLogPo.setCostTime((endDate.getTime() - startDate.getTime()) + "");
            transactionLogPo.setIp("");
            transactionLogPo.setServiceCode(serviceCode);
            transactionLogPo.setSrcIp("127.0.0.1");
            transactionLogPo.setState(responseEntity.getStatusCode() != HttpStatus.OK ? "F" : "S");
            transactionLogPo.setTimestamp(DateUtil.getCurrentDate().getTime() + "");
            transactionLogPo.setUserId(userId);
            transactionLogPo.setTransactionId(userId);
            transactionLogPo.setRequestHeader("");
            transactionLogPo.setResponseHeader(responseEntity.getHeaders().toSingleValueMap().toString());
            transactionLogPo.setRequestMessage(reqJson);
            transactionLogPo.setResponseMessage(responseEntity.getBody());
            saveTransactionLogSMOImpl.saveLog(transactionLogPo);
        } catch (Exception e) {
            logger.error("存日志失败", e);
        }
    }

}
