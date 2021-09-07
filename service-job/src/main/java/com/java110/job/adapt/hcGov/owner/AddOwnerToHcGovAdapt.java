/*
 * Copyright 2017-2020 吴学文 and java110 team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.java110.job.adapt.hcGov.owner;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.dto.FloorDto;
import com.java110.dto.RoomAttrDto;
import com.java110.dto.RoomDto;
import com.java110.dto.UnitDto;
import com.java110.dto.community.CommunityAttrDto;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.floorAttr.FloorAttrDto;
import com.java110.dto.owner.OwnerAttrDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.dto.owner.OwnerRoomRelDto;
import com.java110.entity.order.Business;
import com.java110.intf.community.*;
import com.java110.intf.user.IOwnerAttrInnerServiceSMO;
import com.java110.intf.user.IOwnerInnerServiceSMO;
import com.java110.intf.user.IOwnerRoomRelInnerServiceSMO;
import com.java110.job.adapt.DatabusAdaptImpl;
import com.java110.job.adapt.hcGov.HcGovConstant;
import com.java110.job.adapt.hcGov.asyn.BaseHcGovSendAsyn;
import com.java110.po.owner.OwnerPo;
import com.java110.po.room.RoomPo;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 新增业主信息同步HC政务接口
 * <p>
 * 接口协议地址： https://gitee.com/java110/microCommunityInformation/tree/master/info-doc#1%E6%A5%BC%E6%A0%8B%E4%B8%8A%E4%BC%A0
 *
 * @desc add by 吴学文 16:20
 */
@Component(value = "addOwnerToHcGovAdapt")
public class AddOwnerToHcGovAdapt extends DatabusAdaptImpl {

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMOImpl;
    @Autowired
    private IRoomAttrInnerServiceSMO roomAttrInnerServiceSMOImpl;
    @Autowired
    private IOwnerRoomRelInnerServiceSMO ownerRoomRelInnerServiceSMOImpl;
    @Autowired
    private BaseHcGovSendAsyn baseHcGovSendAsynImpl;
    @Autowired
    private IOwnerAttrInnerServiceSMO ownerAttrInnerServiceSMOImpl;
    @Autowired
    private IOwnerInnerServiceSMO ownerInnerServiceSMOImpl;


    /**
     * @param business   当前处理业务
     * @param businesses 所有业务信息
     */
    @Override
    public void execute(Business business, List<Business> businesses) {
        JSONObject data = business.getData();
        if (data.containsKey(OwnerPo.class.getSimpleName())) {
            Object bObj = data.get(OwnerPo.class.getSimpleName());
            JSONArray businessOwner = null;
            if (bObj instanceof JSONObject) {
                businessOwner = new JSONArray();
                businessOwner.add(bObj);
            } else if (bObj instanceof List) {
                businessOwner = JSONArray.parseArray(JSONObject.toJSONString(bObj));
            } else {
                businessOwner = (JSONArray) bObj;
            }
            //JSONObject businessOwnerCar = data.getJSONObject("businessOwnerCar");
            for (int bOwnerIndex = 0; bOwnerIndex < businessOwner.size(); bOwnerIndex++) {
                JSONObject businessOwnerCar = businessOwner.getJSONObject(bOwnerIndex);
                doAddOwner(business, businessOwnerCar);

            }
        }
    }

    private void doAddOwner(Business business, JSONObject businessOwner) {

        OwnerPo ownerPo = BeanConvertUtil.covertBean(businessOwner, OwnerPo.class);

        CommunityDto communityDto = new CommunityDto();
        communityDto.setCommunityId(ownerPo.getCommunityId());
        List<CommunityDto> communityDtos = communityInnerServiceSMOImpl.queryCommunitys(communityDto);
        Assert.listNotNull(communityDtos, "未包含小区信息");

        CommunityDto tmpCommunityDto = communityDtos.get(0);
        String extCommunityId = "";
        JSONArray extRoomId = null;
        String communityId = tmpCommunityDto.getCommunityId();
        String memberId = ownerPo.getMemberId();

        for (CommunityAttrDto communityAttrDto : tmpCommunityDto.getCommunityAttrDtos()) {
            if (HcGovConstant.EXT_COMMUNITY_ID.equals(communityAttrDto.getSpecCd())) {
                extCommunityId = communityAttrDto.getValue();
            }
        }
        OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
        ownerRoomRelDto.setOwnerId(memberId);
        List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);
        if (ownerRoomRelDtos != null && ownerRoomRelDtos.size() > 0) {

            RoomAttrDto roomAttrDto = new RoomAttrDto();
            roomAttrDto.setRoomId(ownerRoomRelDtos.get(0).getRoomId());
            roomAttrDto.setSpecCd(HcGovConstant.EXT_COMMUNITY_ID);
            List<RoomAttrDto> roomAttrDtos = roomAttrInnerServiceSMOImpl.queryRoomAttrs(roomAttrDto);
            if (roomAttrDtos != null && roomAttrDtos.size() > 0) {
                extRoomId = new JSONArray();
                for (RoomAttrDto roomAttr : roomAttrDtos) {
                    if (HcGovConstant.EXT_COMMUNITY_ID.equals(roomAttr.getSpecCd())) {
                        extRoomId.add(roomAttr.getValue());
                    }
                }
            }
        }
        JSONObject body = new JSONObject();
        //1001 业主本人 1002 家庭成员
        if ("1001".equals(ownerPo.getOwnerTypeCd())) {
            body.put("idType", "1");
            body.put("idCard", ownerPo.getIdCard());
            body.put("personName", ownerPo.getName());
            body.put("personTel", ownerPo.getLink());
            body.put("personSex", ownerPo.getSex());
            body.put("prePersonName", ownerPo.getName());
            body.put("birthday", DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_B));
            body.put("nation", "未知");
            body.put("nativePlace", "中国");
            body.put("politicalOutlook", "无");
            body.put("maritalStatus", "N");
            body.put("religiousBelief", "无");
            body.put("ramark", ownerPo.getRemark());
            body.put("extRoomId", extRoomId);
            body.put("ownerType", "2002");
            body.put("ownerAddress", "无");
            body.put("ownerTypeCd", ownerPo.getOwnerTypeCd());
        }
        if ("1002".equals(ownerPo.getOwnerTypeCd())) {
            String extOwnerId = "";
            OwnerAttrDto ownerAttrDto = new OwnerAttrDto();
            ownerAttrDto.setMemberId(ownerPo.getOwnerId());
            ownerAttrDto.setSpecCd(HcGovConstant.EXT_COMMUNITY_ID);
            List<OwnerAttrDto> ownerAttr = ownerAttrInnerServiceSMOImpl.queryOwnerAttrs(ownerAttrDto);
            if (ownerAttr != null && ownerAttr.size() > 0) {
                for (OwnerAttrDto roomAttr : ownerAttr) {
                    if (HcGovConstant.EXT_COMMUNITY_ID.equals(roomAttr.getSpecCd())) {
                        extOwnerId = roomAttr.getValue();
                    }
                }
            }
            if ("".equals(extOwnerId)) {
                throw new IllegalArgumentException("未获得业主外部编码！");
            }

           /* String extMemberId = "";
            ownerAttrDto = new OwnerAttrDto();
            ownerAttrDto.setMemberId(ownerPo.getMemberId());
            ownerAttrDto.setSpecCd(HcGovConstant.EXT_COMMUNITY_ID);
            ownerAttr = ownerAttrInnerServiceSMOImpl.queryOwnerAttrs(ownerAttrDto);
            if (ownerAttr != null && ownerAttr.size() > 0) {
                for (OwnerAttrDto roomAttr : ownerAttr) {
                    if (HcGovConstant.EXT_COMMUNITY_ID.equals(roomAttr.getSpecCd())) {
                        extMemberId = roomAttr.getValue();
                    }
                }
            }
            if ("".equals(extOwnerId)) {
                throw new IllegalArgumentException("未获得业主外部编码！");
            }*/

            body.put("idType", "1");
            body.put("idCard", ownerPo.getIdCard());
            body.put("personName", ownerPo.getName());
            body.put("personTel", ownerPo.getLink());
            body.put("personSex", ownerPo.getSex());
            body.put("prePersonName", ownerPo.getName());
            body.put("birthday", DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_B));
            body.put("nation", "未知");
            body.put("nativePlace", "中国");
            body.put("politicalOutlook", "无");
            body.put("maritalStatus", "N");
            body.put("religiousBelief", "无");
            body.put("ramark", ownerPo.getRemark());
            body.put("ownerType", "2002");
            body.put("ownerAddress", "无");
            body.put("extOwnerId", extOwnerId);
        }

        JSONObject kafkaData = baseHcGovSendAsynImpl.createHeadersOrBody(body, extCommunityId, HcGovConstant.ADD_OWNER_ACTION, HcGovConstant.COMMUNITY_SECURE);
        baseHcGovSendAsynImpl.sendKafka(HcGovConstant.GOV_TOPIC, kafkaData, communityId, memberId, HcGovConstant.COMMUNITY_SECURE);
    }

}
