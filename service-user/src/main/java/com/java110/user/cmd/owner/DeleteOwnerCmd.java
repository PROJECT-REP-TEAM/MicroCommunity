package com.java110.user.cmd.owner;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.AbstractServiceCmdListener;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.RoomDto;
import com.java110.dto.owner.OwnerAppUserDto;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.intf.community.IOwnerV1InnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.user.IOwnerAppUserInnerServiceSMO;
import com.java110.intf.user.IOwnerAppUserV1InnerServiceSMO;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.intf.user.IOwnerInnerServiceSMO;
import com.java110.po.owner.OwnerAppUserPo;
import com.java110.po.owner.OwnerPo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Java110Cmd(serviceCode = "owner.deleteOwner")
public class DeleteOwnerCmd extends AbstractServiceCmdListener {

    @Autowired
    private IOwnerInnerServiceSMO ownerInnerServiceSMOImpl;

    @Autowired
    private IOwnerV1InnerServiceSMO ownerV1InnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMOImpl;

    @Autowired
    private IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMOImpl;

    @Autowired
    private IOwnerAppUserV1InnerServiceSMO ownerAppUserV1InnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {
        Assert.jsonObjectHaveKey(reqJson, "memberId", "请求报文中未包含memberId");
        Assert.jsonObjectHaveKey(reqJson, "communityId", "请求报文中未包含communityId");
        if (!"1001".equals(reqJson.getString("ownerTypeCd"))) { //不是业主成员不管
            return;
        }

        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setOwnerId(reqJson.getString("memberId"));
        ownerDto.setCommunityId(reqJson.getString("communityId"));
        ownerDto.setOwnerTypeCds(new String[]{OwnerDto.OWNER_TYPE_CD_MEMBER, OwnerDto.OWNER_TYPE_CD_RENTING});

        List<OwnerDto> ownerDtos = ownerInnerServiceSMOImpl.queryOwnerMembers(ownerDto);

        if (ownerDtos != null && ownerDtos.size() > 0) {
            throw new IllegalArgumentException("请先删除业主下的成员");
        }

        if (OwnerDto.OWNER_TYPE_CD_OWNER.equals(reqJson.getString("ownerTypeCd"))) {
            //ownerId 写为 memberId
            reqJson.put("ownerId", reqJson.getString("memberId"));
            RoomDto roomDto = new RoomDto();
            roomDto.setOwnerId(reqJson.getString("ownerId"));
            List<RoomDto> roomDtoList = roomInnerServiceSMOImpl.queryRoomsByOwner(roomDto);
            if (roomDtoList.size() > 0) {
                throw new IllegalArgumentException("删除失败,删除前请先解绑房屋信息");
            }
            //查询车位信息
            OwnerCarDto ownerCarDto = new OwnerCarDto();
            ownerCarDto.setOwnerId(reqJson.getString("ownerId"));
            List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);
            if (ownerCarDtos.size() > 0) {
                throw new IllegalArgumentException("删除失败,删除前请先解绑车位信息");
            }
            //小区楼添加到小区中
            //ownerBMOImpl.exitCommunityMember(reqJson, context);
        }
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {
        JSONArray businesses = new JSONArray();


        JSONObject businessOwner = new JSONObject();
        businessOwner.put("memberId", reqJson.getString("memberId"));
        businessOwner.put("communityId", reqJson.getString("communityId"));

        OwnerPo ownerPo = BeanConvertUtil.covertBean(businessOwner, OwnerPo.class);
        int flag = ownerV1InnerServiceSMOImpl.deleteOwner(ownerPo);

        if (flag < 1) {
            throw new CmdException("删除失败");
        }
        OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
        ownerAppUserDto.setMemberId(reqJson.getString("ownerId"));
        //查询app用户表
        List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsers(ownerAppUserDto);
        if (ownerAppUserDtos != null && ownerAppUserDtos.size() > 0) {
            for (OwnerAppUserDto ownerAppUser : ownerAppUserDtos) {
                OwnerAppUserPo ownerAppUserPo = BeanConvertUtil.covertBean(ownerAppUser, OwnerAppUserPo.class);
                flag = ownerAppUserV1InnerServiceSMOImpl.deleteOwnerAppUser(ownerAppUserPo);
                if (flag < 1) {
                    throw new CmdException("删除失败");
                }
            }
        }
    }
}
