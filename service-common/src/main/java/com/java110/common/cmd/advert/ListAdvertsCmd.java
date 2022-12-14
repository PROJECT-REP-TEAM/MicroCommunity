package com.java110.common.cmd.advert;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.FloorDto;
import com.java110.dto.RoomDto;
import com.java110.dto.advert.AdvertDto;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.unit.FloorAndUnitDto;
import com.java110.intf.common.IAdvertInnerServiceSMO;
import com.java110.intf.community.ICommunityInnerServiceSMO;
import com.java110.intf.community.IFloorInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.community.IUnitInnerServiceSMO;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.vo.api.advert.ApiAdvertDataVo;
import com.java110.vo.api.advert.ApiAdvertVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@Java110Cmd(serviceCode = "advert.listAdverts")
public class ListAdvertsCmd extends Cmd {


    @Autowired
    private IAdvertInnerServiceSMO advertInnerServiceSMOImpl;

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMOImpl;

    @Autowired
    private IFloorInnerServiceSMO floorInnerServiceSMOImpl;

    @Autowired
    private IUnitInnerServiceSMO unitInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException {
        super.validatePageInfo(reqJson);
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException {
        AdvertDto advertDto = BeanConvertUtil.covertBean(reqJson, AdvertDto.class);
        int count = advertInnerServiceSMOImpl.queryAdvertsCount(advertDto);
        List<ApiAdvertDataVo> adverts = null;
        if (count > 0) {
            List<AdvertDto> advertDtos = advertInnerServiceSMOImpl.queryAdverts(advertDto);
            adverts = BeanConvertUtil.covertBeanList(advertDtos, ApiAdvertDataVo.class);
        } else {
            adverts = new ArrayList<>();
        }
        ApiAdvertVo apiAdvertVo = new ApiAdvertVo();
        apiAdvertVo.setTotal(count);
        apiAdvertVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiAdvertVo.setAdverts(adverts);
        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiAdvertVo), HttpStatus.OK);
        context.setResponseEntity(responseEntity);
    }

    private void refreshAdvert(List<AdvertDto> advertDtos) {
        //???????????? ??????
        refreshCommunitys(advertDtos);
        //????????????????????????
        refreshFloors(advertDtos);
        //????????????????????????
        refreshUnits(advertDtos);
        //???????????? ????????????
        refreshRooms(advertDtos);
    }

    /**
     * ??????????????????
     *
     * @param advertDtos ????????????
     * @return ??????userIds ??????
     */
    private void refreshCommunitys(List<AdvertDto> advertDtos) {
        List<String> communityIds = new ArrayList<String>();
        List<AdvertDto> tmpAdvertDtos = new ArrayList<>();
        for (AdvertDto advertDto : advertDtos) {
            if ("1000".equals(advertDto.getLocationTypeCd())) {
                communityIds.add(advertDto.getLocationObjId());
                tmpAdvertDtos.add(advertDto);
            }
        }
        if (communityIds.size() < 1) {
            return;
        }
        String[] tmpCommunityIds = communityIds.toArray(new String[communityIds.size()]);
        CommunityDto communityDto = new CommunityDto();
        communityDto.setCommunityIds(tmpCommunityIds);
        //?????? userId ??????????????????
        List<CommunityDto> communityDtos = communityInnerServiceSMOImpl.queryCommunitys(communityDto);
        for (AdvertDto advertDto : tmpAdvertDtos) {
            for (CommunityDto tmpCommunityDto : communityDtos) {
                if (advertDto.getLocationObjId().equals(tmpCommunityDto.getCommunityId())) {
                    advertDto.setLocationObjName(tmpCommunityDto.getName());
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param adverts ????????????
     * @return ??????userIds ??????
     */
    private void refreshFloors(List<AdvertDto> adverts) {
        List<String> floorIds = new ArrayList<String>();
        List<AdvertDto> tmpAdvertDtos = new ArrayList<>();
        for (AdvertDto advertDto : adverts) {
            if ("4000".equals(advertDto.getLocationTypeCd())) {
                floorIds.add(advertDto.getLocationObjId());
                tmpAdvertDtos.add(advertDto);
            }
        }
        if (floorIds.size() < 1) {
            return;
        }
        for (AdvertDto advertDto : tmpAdvertDtos) {
            FloorDto floorDto = new FloorDto();
            floorDto.setFloorId(advertDto.getLocationObjId());
            floorDto.setCommunityId(advertDto.getCommunityId());
            List<FloorDto> floorDtos = floorInnerServiceSMOImpl.queryFloors(floorDto);
            if (floorDtos == null || floorDtos.size() < 1) {
                return;
            }
            if (advertDto.getLocationObjId().equals(floorDtos.get(0).getFloorId())) {
                advertDto.setLocationObjName(floorDtos.get(0).getFloorNum() + "???");
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param adverts ????????????
     * @return ??????userIds ??????
     */
    private void refreshUnits(List<AdvertDto> adverts) {
        List<String> unitIds = new ArrayList<String>();
        List<AdvertDto> tmpAdvertDtos = new ArrayList<>();
        for (AdvertDto advertDto : adverts) {
            if ("2000".equals(advertDto.getLocationTypeCd())) {
                unitIds.add(advertDto.getLocationObjId());
                tmpAdvertDtos.add(advertDto);
            }
        }
        if (unitIds.size() < 1) {
            return;
        }
        String[] tmpUnitIds = unitIds.toArray(new String[unitIds.size()]);
        FloorAndUnitDto floorAndUnitDto = new FloorAndUnitDto();
        floorAndUnitDto.setUnitIds(tmpUnitIds);
        //?????? userId ??????????????????
        List<FloorAndUnitDto> unitDtos = unitInnerServiceSMOImpl.getFloorAndUnitInfo(floorAndUnitDto);
        for (AdvertDto advertDto : tmpAdvertDtos) {
            for (FloorAndUnitDto tmpUnitDto : unitDtos) {
                if (advertDto.getLocationObjId().equals(tmpUnitDto.getUnitId())) {
                    advertDto.setLocationObjName(tmpUnitDto.getFloorNum() + "???" + tmpUnitDto.getUnitNum() + "??????");
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param adverts ????????????
     * @return ??????userIds ??????
     */
    private void refreshRooms(List<AdvertDto> adverts) {
        List<String> roomIds = new ArrayList<String>();
        List<AdvertDto> tmpAdvertDtos = new ArrayList<>();
        for (AdvertDto advertDto : adverts) {
            if ("3000".equals(advertDto.getLocationTypeCd())) {
                roomIds.add(advertDto.getLocationObjId());
                tmpAdvertDtos.add(advertDto);
            }
        }
        if (roomIds.size() < 1) {
            return;
        }
        String[] tmpRoomIds = roomIds.toArray(new String[roomIds.size()]);
        RoomDto roomDto = new RoomDto();
        roomDto.setRoomIds(tmpRoomIds);
        roomDto.setCommunityId(adverts.get(0).getCommunityId());
        //?????? userId ??????????????????
        List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);
        for (AdvertDto advertDto : tmpAdvertDtos) {
            for (RoomDto tmpRoomDto : roomDtos) {
                if (advertDto.getLocationObjId().equals(tmpRoomDto.getRoomId())) {
                    advertDto.setLocationObjName(tmpRoomDto.getFloorNum() + "???" + tmpRoomDto.getUnitNum() + "??????" + tmpRoomDto.getRoomNum() + "???");
                }
            }
        }
    }

}
