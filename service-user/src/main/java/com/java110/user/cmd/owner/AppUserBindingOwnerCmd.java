package com.java110.user.cmd.owner;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.core.factory.SendSmsFactory;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.msg.SmsDto;
import com.java110.dto.owner.OwnerAppUserDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.dto.user.UserDto;
import com.java110.intf.common.IFileInnerServiceSMO;
import com.java110.intf.common.ISmsInnerServiceSMO;
import com.java110.intf.community.ICommunityInnerServiceSMO;
import com.java110.intf.user.IOwnerAppUserInnerServiceSMO;
import com.java110.intf.user.IOwnerAppUserV1InnerServiceSMO;
import com.java110.intf.user.IOwnerInnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.po.owner.OwnerAppUserPo;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Java110Cmd(serviceCode = "owner.appUserBindingOwner")
public class AppUserBindingOwnerCmd extends Cmd {

    @Autowired
    private IFileInnerServiceSMO fileInnerServiceSMOImpl;

    @Autowired
    private ISmsInnerServiceSMO smsInnerServiceSMOImpl;

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMOImpl;

    @Autowired
    private IOwnerInnerServiceSMO ownerInnerServiceSMOImpl;

    @Autowired
    private IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMOImpl;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Autowired
    private IOwnerAppUserV1InnerServiceSMO ownerAppUserV1InnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException {
        Assert.hasKeyAndValue(reqJson, "communityName", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "areaCode", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "appUserName", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "idCard", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "link", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "msgCode", "??????????????????????????????");

        //?????????????????????ID
        Map<String, String> headers = context.getReqHeaders();

        Assert.hasKeyAndValue(headers, "user_id", "?????????????????????????????????");
        SmsDto smsDto = new SmsDto();
        smsDto.setTel(reqJson.getString("link"));
        smsDto.setCode(reqJson.getString("msgCode"));
        smsDto = smsInnerServiceSMOImpl.validateCode(smsDto);

        if (!smsDto.isSuccess() && "ON".equals(MappingCache.getValue(SendSmsFactory.SMS_SEND_SWITCH))) {
            throw new IllegalArgumentException(smsDto.getMsg());
        }
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException {

        Map<String, String> headers = context.getReqHeaders();

        String userId = headers.get("user_id");
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        List<UserDto> userDtos = userInnerServiceSMOImpl.getUsers(userDto);

        Assert.listOnlyOne(userDtos, "????????????????????????????????????????????????");

        String openId = userDtos.get(0).getOpenId();

        Assert.hasLength(openId, "?????????????????????????????????");

        OwnerAppUserDto ownerAppUserDto = BeanConvertUtil.covertBean(reqJson, OwnerAppUserDto.class);
        ownerAppUserDto.setStates(new String[]{"10000", "12000"});

        List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsers(ownerAppUserDto);

        //Assert.listOnlyOne(ownerAppUserDtos, "???????????????????????????");
        if (ownerAppUserDtos != null && ownerAppUserDtos.size() > 0) {
            throw new IllegalArgumentException("???????????????????????????");
        }

        //????????????????????????
        CommunityDto communityDto = new CommunityDto();
        communityDto.setCityCode(reqJson.getString("areaCode"));
        communityDto.setName(reqJson.getString("communityName"));
        communityDto.setState("1100");
        List<CommunityDto> communityDtos = communityInnerServiceSMOImpl.queryCommunitys(communityDto);

        Assert.listOnlyOne(communityDtos, "????????????????????????");

        CommunityDto tmpCommunityDto = communityDtos.get(0);

        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setCommunityId(tmpCommunityDto.getCommunityId());
        ownerDto.setIdCard(reqJson.getString("idCard"));
        ownerDto.setName(reqJson.getString("appUserName"));
        ownerDto.setLink(reqJson.getString("link"));
        List<OwnerDto> ownerDtos = ownerInnerServiceSMOImpl.queryOwnerMembers(ownerDto);

        Assert.listOnlyOne(ownerDtos, "????????????????????????");

        OwnerDto tmpOwnerDto = ownerDtos.get(0);

        reqJson.put("openId", openId);
        reqJson.put("userId", userId);

        //???????????????
        JSONObject businessOwnerAppUser = new JSONObject();
        businessOwnerAppUser.putAll(reqJson);
        //???????????????10000 ????????????12000 ???????????????13000 ????????????
        businessOwnerAppUser.put("state", "12000");
        businessOwnerAppUser.put("appTypeCd", "10010");
        businessOwnerAppUser.put("appUserId", GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_appUserId));
        businessOwnerAppUser.put("memberId", ownerDto.getMemberId());
        businessOwnerAppUser.put("communityName", communityDto.getName());
        businessOwnerAppUser.put("communityId", communityDto.getCommunityId());
        businessOwnerAppUser.put("appUserName", ownerDto.getName());
        businessOwnerAppUser.put("idCard", ownerDto.getIdCard());
        businessOwnerAppUser.put("link", ownerDto.getLink());
        businessOwnerAppUser.put("userId", reqJson.getString("userId"));
        OwnerAppUserPo ownerAppUserPo = BeanConvertUtil.covertBean(businessOwnerAppUser, OwnerAppUserPo.class);
        int flag = ownerAppUserV1InnerServiceSMOImpl.saveOwnerAppUser(ownerAppUserPo);
        if (flag < 1) {
            throw new IllegalArgumentException("??????????????????");
        }
    }
}
