package com.java110.community.cmd.room;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.RoomDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.dto.owner.OwnerRoomRelDto;
import com.java110.intf.community.ICommunityInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.community.IRoomV1InnerServiceSMO;
import com.java110.intf.community.IUnitInnerServiceSMO;
import com.java110.intf.user.IOwnerRoomRelInnerServiceSMO;
import com.java110.intf.user.IOwnerRoomRelV1InnerServiceSMO;
import com.java110.intf.user.IOwnerV1InnerServiceSMO;
import com.java110.po.owner.OwnerPo;
import com.java110.po.owner.OwnerRoomRelPo;
import com.java110.po.room.RoomPo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Java110Cmd(serviceCode = "room.saveOwnerShops")
public class SaveOwnerShopsCmd extends Cmd {

    @Autowired
    private IUnitInnerServiceSMO unitInnerServiceSMOImpl;

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMOImpl;

    @Autowired
    private IOwnerRoomRelInnerServiceSMO ownerRoomRelInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IOwnerRoomRelV1InnerServiceSMO ownerRoomRelV1InnerServiceSMOImpl;

    @Autowired
    private IRoomV1InnerServiceSMO roomV1InnerServiceSMOImpl;

    @Autowired
    private IOwnerV1InnerServiceSMO ownerV1InnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException {
        Assert.jsonObjectHaveKey(reqJson, "communityId", "????????????????????????communityId??????");
        Assert.jsonObjectHaveKey(reqJson, "roomId", "????????????????????????roomId??????");
        Assert.jsonObjectHaveKey(reqJson, "ownerName", "????????????????????????ownerName??????");
        Assert.jsonObjectHaveKey(reqJson, "tel", "????????????????????????tel??????");
        Assert.jsonObjectHaveKey(reqJson, "startTime", "????????????????????????tel??????");
        Assert.jsonObjectHaveKey(reqJson, "endTime", "????????????????????????tel??????");
        Assert.jsonObjectHaveKey(reqJson, "shopsState", "????????????????????????????????????");
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException {
        int flag = 0;
        if (!reqJson.containsKey("ownerId")
                || reqJson.getString("ownerId").startsWith("-")
                || StringUtil.isEmpty(reqJson.getString("ownerId"))
        ) {
            OwnerPo ownerPo = new OwnerPo();
            ownerPo.setUserId("-1");
            ownerPo.setAge("1");
            ownerPo.setCommunityId(reqJson.getString("communityId"));
            ownerPo.setIdCard("");
            ownerPo.setLink(reqJson.getString("tel"));
            ownerPo.setSex("1");
            ownerPo.setMemberId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_ownerId));
            ownerPo.setName(reqJson.getString("ownerName"));
            ownerPo.setOwnerId(ownerPo.getMemberId()); //?????? ???????????????ID????????????
            ownerPo.setOwnerTypeCd(OwnerDto.OWNER_TYPE_CD_OWNER);
            ownerPo.setRemark(reqJson.getString("remark"));
            ownerPo.setState("2000");
            flag = ownerV1InnerServiceSMOImpl.saveOwner(ownerPo);
            if (flag < 1) {
                throw new IllegalArgumentException("??????????????????");
            }
            reqJson.put("ownerId", ownerPo.getOwnerId());
        }

        //????????????????????? ?????? ????????????
        RoomDto roomDto = new RoomDto();
        roomDto.setRoomId(reqJson.getString("roomId"));
        roomDto.setCommunityId(reqJson.getString("communityId"));
        List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

        Assert.listOnlyOne(roomDtos, "???????????????");

        if (!"2006,2008".contains(roomDtos.get(0).getState())) {
            throw new IllegalArgumentException("?????????????????????????????????");
        }

        //????????????????????????
        OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
        ownerRoomRelDto.setRoomId(reqJson.getString("roomId"));
        List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);

        if (ownerRoomRelDtos != null && ownerRoomRelDtos.size() > 0) {
            JSONObject businessUnit = new JSONObject();
            businessUnit.put("relId", ownerRoomRelDtos.get(0).getRelId());
            OwnerRoomRelPo roomPo = BeanConvertUtil.covertBean(businessUnit, OwnerRoomRelPo.class);
            flag = ownerRoomRelV1InnerServiceSMOImpl.deleteOwnerRoomRel(roomPo);
            if (flag < 1) {
                throw new IllegalArgumentException("??????????????????????????????");
            }
        }

        //??????????????????
        reqJson.put("state", "2001");
        JSONObject businessUnit = new JSONObject();
        businessUnit.putAll(reqJson);
        businessUnit.put("relId", GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_relId));
        businessUnit.put("userId", "-1");
        OwnerRoomRelPo ownerRoomRelPo = BeanConvertUtil.covertBean(businessUnit, OwnerRoomRelPo.class);
        ownerRoomRelV1InnerServiceSMOImpl.saveOwnerRoomRel(ownerRoomRelPo);
        //???????????????????????????
        reqJson.put("state", reqJson.getString("shopsState"));
        businessUnit = new JSONObject();
        businessUnit.putAll(reqJson);
        businessUnit.put("userId", "-1");
        RoomPo roomPo = BeanConvertUtil.covertBean(businessUnit, RoomPo.class);
        flag = roomV1InnerServiceSMOImpl.updateRoom(roomPo);
        if (flag < 1) {
            throw new IllegalArgumentException("??????????????????????????????");
        }
    }


}
