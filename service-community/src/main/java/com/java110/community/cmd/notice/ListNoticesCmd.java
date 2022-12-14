package com.java110.community.cmd.notice;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.FloorDto;
import com.java110.dto.RoomDto;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.notice.NoticeDto;
import com.java110.dto.unit.FloorAndUnitDto;
import com.java110.intf.community.*;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.api.notice.ApiNoticeDataVo;
import com.java110.vo.api.notice.ApiNoticeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Java110Cmd(serviceCode = "notice.listNotices")
public class ListNoticesCmd extends Cmd {

    @Autowired
    private INoticeInnerServiceSMO noticeInnerServiceSMOImpl;

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMOImpl;

    @Autowired
    private IFloorInnerServiceSMO floorInnerServiceSMOImpl;

    @Autowired
    private IUnitInnerServiceSMO unitInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {
        super.validatePageInfo(reqJson);
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {
        reqJson.remove("userId");
        NoticeDto noticeDto = BeanConvertUtil.covertBean(reqJson, NoticeDto.class);
        if (!StringUtil.isEmpty("clientType") && "H5".equals(reqJson.get("clientType"))) {
            Date day = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            noticeDto.setStartTime(df.format(day));
            noticeDto.setEndTime(df.format(day));
        }

        // ?????????????????????
        if (!StringUtil.isEmpty(noticeDto.getNoticeTypeCd()) && noticeDto.getNoticeTypeCd().contains(",")) {
            noticeDto.setNoticeTypeCds(noticeDto.getNoticeTypeCd().split(","));
            noticeDto.setNoticeTypeCd("");
        }

        int count = noticeInnerServiceSMOImpl.queryNoticesCount(noticeDto);

        List<ApiNoticeDataVo> notices = null;

        if (count > 0) {
            notices = BeanConvertUtil.covertBeanList(noticeInnerServiceSMOImpl.queryNotices(noticeDto), ApiNoticeDataVo.class);
            refreshNotice(notices);
        } else {
            notices = new ArrayList<>();
        }

        ApiNoticeVo apiNoticeVo = new ApiNoticeVo();

        apiNoticeVo.setTotal(count);
        apiNoticeVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiNoticeVo.setNotices(notices);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiNoticeVo), HttpStatus.OK);

        cmdDataFlowContext.setResponseEntity(responseEntity);
    }

    private void refreshNotice(List<ApiNoticeDataVo> notices) {

        refreshWechats(notices);
        //???????????? ??????
        refreshCommunitys(notices);

        //????????????
        refreshFloors(notices);

        //????????????????????????
        refreshUnits(notices);

        //???????????? ????????????
        refreshRooms(notices);


    }

    /**
     * ??????????????????
     *
     * @param notices ????????????
     * @return ??????userIds ??????
     */
    private void refreshWechats(List<ApiNoticeDataVo> notices) {

        for (ApiNoticeDataVo noticeDto : notices) {
            if (NoticeDto.OBJ_TYPE_ALL.equals(noticeDto.getObjType())) {
                noticeDto.setObjName("????????????");
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param notices ????????????
     * @return ??????userIds ??????
     */
    private void refreshCommunitys(List<ApiNoticeDataVo> notices) {
        List<String> communityIds = new ArrayList<String>();
        List<ApiNoticeDataVo> tmpNoticeDtos = new ArrayList<>();
        for (ApiNoticeDataVo noticeDto : notices) {

            if (NoticeDto.OBJ_TYPE_COMMUNITY.equals(noticeDto.getObjType())
            ) {
                communityIds.add(noticeDto.getObjId());
                tmpNoticeDtos.add(noticeDto);
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

        for (ApiNoticeDataVo noticeDto : tmpNoticeDtos) {
            for (CommunityDto tmpCommunityDto : communityDtos) {
                if (noticeDto.getObjId().equals(tmpCommunityDto.getCommunityId())) {
                    noticeDto.setObjName(tmpCommunityDto.getName());
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param notices ????????????
     * @return ??????userIds ??????
     */
    private void refreshFloors(List<ApiNoticeDataVo> notices) {
        List<String> floorIds = new ArrayList<String>();
        List<ApiNoticeDataVo> tmpNoticeDtos = new ArrayList<>();
        for (ApiNoticeDataVo noticeDto : notices) {
            if (NoticeDto.OBJ_TYPE_UNIT.equals(noticeDto.getObjType())) {
                floorIds.add(noticeDto.getObjId());
                tmpNoticeDtos.add(noticeDto);
            }
        }

        if (floorIds.size() < 1) {
            return;
        }
        String[] tmpFloorIds = floorIds.toArray(new String[floorIds.size()]);

        FloorDto floorDto = new FloorDto();
        floorDto.setFloorIds(tmpFloorIds);
        //?????? userId ??????????????????
        List<FloorDto> floorDtos = floorInnerServiceSMOImpl.queryFloors(floorDto);

        for (ApiNoticeDataVo noticeDto : tmpNoticeDtos) {
            for (FloorDto tmpFloorDto : floorDtos) {
                if (noticeDto.getObjId().equals(tmpFloorDto.getFloorId())) {
                    noticeDto.setObjName(tmpFloorDto.getFloorNum() + "???");
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param notices ????????????
     * @return ??????userIds ??????
     */
    private void refreshUnits(List<ApiNoticeDataVo> notices) {
        List<String> unitIds = new ArrayList<String>();
        List<ApiNoticeDataVo> tmpNoticeDtos = new ArrayList<>();
        for (ApiNoticeDataVo noticeDto : notices) {
            if (NoticeDto.OBJ_TYPE_UNIT.equals(noticeDto.getObjType())) {
                unitIds.add(noticeDto.getObjId());
                tmpNoticeDtos.add(noticeDto);
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

        for (ApiNoticeDataVo noticeDto : tmpNoticeDtos) {
            for (FloorAndUnitDto tmpUnitDto : unitDtos) {
                if (noticeDto.getObjId().equals(tmpUnitDto.getUnitId())) {
                    noticeDto.setObjName(tmpUnitDto.getFloorNum() + "???" + tmpUnitDto.getUnitNum() + "??????");
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param notices ????????????
     * @return ??????userIds ??????
     */
    private void refreshRooms(List<ApiNoticeDataVo> notices) {
        List<String> roomIds = new ArrayList<String>();
        List<ApiNoticeDataVo> tmpNoticeDtos = new ArrayList<>();
        for (ApiNoticeDataVo noticeDto : notices) {
            if (NoticeDto.OBJ_TYPE_ROOM.equals(noticeDto.getObjType())) {
                if ("3000".equals(noticeDto.getObjType())) {
                    roomIds.add(noticeDto.getObjId());
                    tmpNoticeDtos.add(noticeDto);
                }
            }
            if (roomIds.size() < 1) {
                return;
            }
            String[] tmpRoomIds = roomIds.toArray(new String[roomIds.size()]);

            RoomDto roomDto = new RoomDto();
            roomDto.setRoomIds(tmpRoomIds);
            roomDto.setCommunityId(notices.get(0).getCommunityId());
            //?????? userId ??????????????????
            List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

            for (ApiNoticeDataVo tmpNoticeDto : tmpNoticeDtos) {
                for (RoomDto tmpRoomDto : roomDtos) {
                    if (tmpNoticeDto.getObjId().equals(tmpRoomDto.getRoomId())) {
                        tmpNoticeDto.setObjName(tmpRoomDto.getFloorNum() + "???" + tmpRoomDto.getUnitNum() + "??????" + tmpRoomDto.getRoomNum() + "???");
                    }
                }
            }
        }
    }
}
