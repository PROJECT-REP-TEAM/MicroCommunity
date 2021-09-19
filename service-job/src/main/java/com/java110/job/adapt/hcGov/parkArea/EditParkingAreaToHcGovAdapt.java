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
package com.java110.job.adapt.hcGov.parkArea;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.dto.community.CommunityAttrDto;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.parking.ParkingAreaDto;
import com.java110.dto.parking.ParkingSpaceDto;
import com.java110.dto.parkingAreaAttr.ParkingAreaAttrDto;
import com.java110.entity.order.Business;
import com.java110.intf.community.ICommunityInnerServiceSMO;
import com.java110.intf.community.IParkingAreaInnerServiceSMO;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.job.adapt.DatabusAdaptImpl;
import com.java110.job.adapt.hcGov.HcGovConstant;
import com.java110.job.adapt.hcGov.asyn.BaseHcGovSendAsyn;
import com.java110.po.parking.ParkingAreaPo;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 修改楼栋同步HC政务接口
 * <p>
 * 接口协议地址： https://gitee.com/java110/microCommunityInformation/tree/master/info-doc#1%E6%A5%BC%E6%A0%8B%E4%B8%8A%E4%BC%A0
 *
 * @desc add by 吴学文 16:20
 */
@Component(value = "editParkingAreaToHcGovAdapt")
public class EditParkingAreaToHcGovAdapt extends DatabusAdaptImpl {

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMOImpl;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;
    @Autowired
    private BaseHcGovSendAsyn baseHcGovSendAsynImpl;
    @Autowired
    private IParkingAreaInnerServiceSMO parkingAreaInnerServiceSMOImpl;

    /**
     * @param business   当前处理业务
     * @param businesses 所有业务信息
     */
    @Override
    public void execute(Business business, List<Business> businesses) {
        JSONObject data = business.getData();
        if (data.containsKey(ParkingAreaPo.class.getSimpleName())) {
            Object bObj = data.get(ParkingAreaPo.class.getSimpleName());
            JSONArray businessOwnerCars = null;
            if (bObj instanceof JSONObject) {
                businessOwnerCars = new JSONArray();
                businessOwnerCars.add(bObj);
            } else if (bObj instanceof List) {
                businessOwnerCars = JSONArray.parseArray(JSONObject.toJSONString(bObj));
            } else {
                businessOwnerCars = (JSONArray) bObj;
            }
            //JSONObject businessOwnerCar = data.getJSONObject("businessOwnerCar");
            for (int bOwnerCarIndex = 0; bOwnerCarIndex < businessOwnerCars.size(); bOwnerCarIndex++) {
                JSONObject businessOwnerCar = businessOwnerCars.getJSONObject(bOwnerCarIndex);
                doAddParkingArea(business, businessOwnerCar);
            }
        }
    }


    private void doAddParkingArea(Business business, JSONObject businessFloor) {

        ParkingAreaPo parkingAreaPo = BeanConvertUtil.covertBean(businessFloor, ParkingAreaPo.class);

        CommunityDto communityDto = new CommunityDto();
        communityDto.setCommunityId(parkingAreaPo.getCommunityId());
        List<CommunityDto> communityDtos = communityInnerServiceSMOImpl.queryCommunitys(communityDto);
        Assert.listNotNull(communityDtos, "未包含小区信息");
        CommunityDto tmpCommunityDto = communityDtos.get(0);
        String extCommunityId = "";
        String communityId = tmpCommunityDto.getCommunityId();
        for (CommunityAttrDto communityAttrDto : tmpCommunityDto.getCommunityAttrDtos()) {
            if (HcGovConstant.EXT_COMMUNITY_ID.equals(communityAttrDto.getSpecCd())) {
                extCommunityId = communityAttrDto.getValue();
            }
        }

        //查询车位数量
        ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
        parkingSpaceDto.setCommunityId(communityId);
        parkingSpaceDto.setPaId(parkingAreaPo.getPaId());
        int count = parkingSpaceInnerServiceSMOImpl.queryParkingSpacesCount(parkingSpaceDto);

        //查询 外部停车
        ParkingAreaDto parkingAreaDto = new ParkingAreaDto();
        parkingAreaDto.setPaId(parkingAreaPo.getPaId());
        parkingAreaDto.setCommunityId(parkingAreaPo.getCommunityId());
        List<ParkingAreaDto> parkingAreaDtos = parkingAreaInnerServiceSMOImpl.queryParkingAreas(parkingAreaDto);
        Assert.listOnlyOne(parkingAreaDtos, "停车场不存在");

        String extPaId = getExtPaId(parkingAreaDtos.get(0).getAttrs());

        JSONObject body = new JSONObject();
        body.put("num", parkingAreaPo.getNum());
        body.put("parkingCount", count);
        body.put("typeCd", parkingAreaPo.getTypeCd());
        //如果为空 走添加接口
        if (StringUtil.isEmpty(extPaId)) {
            JSONObject kafkaData = baseHcGovSendAsynImpl.createHeadersOrBody(body, extCommunityId, HcGovConstant.ADD_PARKING_AREA_ACTION, HcGovConstant.COMMUNITY_SECURE);
            baseHcGovSendAsynImpl.sendKafka(HcGovConstant.GOV_TOPIC, kafkaData, communityId, parkingAreaPo.getPaId(), HcGovConstant.COMMUNITY_SECURE);
            return;
        }
        body.put("extPaId", extPaId);

        JSONObject kafkaData = baseHcGovSendAsynImpl.createHeadersOrBody(body, extCommunityId, HcGovConstant.EDIT_PARKING_AREA_ACTION, HcGovConstant.COMMUNITY_SECURE);
        baseHcGovSendAsynImpl.sendKafka(HcGovConstant.GOV_TOPIC, kafkaData, communityId, parkingAreaPo.getPaId(), HcGovConstant.COMMUNITY_SECURE);
    }


    private String getExtPaId(List<ParkingAreaAttrDto> attrs) {

        if (attrs == null || attrs.size() < 1) {
            return "";
        }

        for (ParkingAreaAttrDto parkingAreaAttrDto : attrs) {
            if (parkingAreaAttrDto.getSpecCd().equals(HcGovConstant.EXT_COMMUNITY_ID)) {
                return parkingAreaAttrDto.getValue();
            }
        }
        return "";
    }

}
