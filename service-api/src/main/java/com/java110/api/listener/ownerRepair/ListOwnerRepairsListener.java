package com.java110.api.listener.ownerRepair;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.dto.file.FileRelDto;
import com.java110.dto.repair.RepairDto;
import com.java110.dto.repair.RepairUserDto;
import com.java110.intf.common.IFileRelInnerServiceSMO;
import com.java110.intf.community.IRepairInnerServiceSMO;
import com.java110.intf.community.IRepairUserInnerServiceSMO;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.constant.ServiceCodeOwnerRepairConstant;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import com.java110.vo.api.junkRequirement.PhotoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 查询小区侦听类
 */
@Java110Listener("listOwnerRepairsListener")
public class ListOwnerRepairsListener extends AbstractServiceApiListener {

    @Autowired
    private IRepairInnerServiceSMO repairInnerServiceSMOImpl;

    @Autowired
    private IRepairUserInnerServiceSMO repairUserInnerServiceSMOImpl;

    @Autowired
    private IFileRelInnerServiceSMO fileRelInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeOwnerRepairConstant.LIST_OWNERREPAIRS;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    public IRepairInnerServiceSMO getRepairInnerServiceSMOImpl() {
        return repairInnerServiceSMOImpl;
    }

    public void setRepairInnerServiceSMOImpl(IRepairInnerServiceSMO repairInnerServiceSMOImpl) {
        this.repairInnerServiceSMOImpl = repairInnerServiceSMOImpl;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        RepairDto ownerRepairDto = BeanConvertUtil.covertBean(reqJson, RepairDto.class);

        if (!StringUtil.isEmpty(ownerRepairDto.getRoomId()) && ownerRepairDto.getRoomId().contains(",")) {
            String[] roomIds = ownerRepairDto.getRoomId().split(",");
            ownerRepairDto.setRoomIds(roomIds);
            ownerRepairDto.setRoomId("");
        }
        //PC电话报修、PC工单池、H5工单池
        //手机端员工单工单池 只返回未处理状态的数据
        if (!StringUtil.isEmpty(ownerRepairDto.getReqSource()) && ownerRepairDto.getReqSource().equals("mobile")) {
            ownerRepairDto.setState(RepairDto.STATE_WAIT);
        }
        //pc电话报修模块 只返回PC员工登记和手机端员工登记的数据
        if (!StringUtil.isEmpty(ownerRepairDto.getReqSource()) && ownerRepairDto.getReqSource().equals("pc_mobile")) {
            String[] repair_channel = {RepairDto.REPAIR_CHANNEL_STAFF, RepairDto.REPAIR_CHANNEL_TEL};
            ownerRepairDto.setRepairChannels(Arrays.asList(repair_channel));
        }

        int count = repairInnerServiceSMOImpl.queryRepairsCount(ownerRepairDto);

        List<RepairDto> ownerRepairs = new ArrayList<>();
        if (count > 0) {
            List<RepairDto> repairDtos = repairInnerServiceSMOImpl.queryRepairs(ownerRepairDto);
            for (RepairDto repairDto : repairDtos) {
                //获取综合评价得分
                String appraiseScoreNumber = repairDto.getAppraiseScore();
                Double appraiseScoreNum = 0.0;
                if (!StringUtil.isEmpty(appraiseScoreNumber)) {
                    appraiseScoreNum = Double.parseDouble(appraiseScoreNumber);
                }
                int appraiseScore = (int) Math.ceil(appraiseScoreNum);
                //获取上门速度评分
                String doorSpeedScoreNumber = repairDto.getDoorSpeedScore();
                Double doorSpeedScoreNum = 0.0;
                if (!StringUtil.isEmpty(doorSpeedScoreNumber)) {
                    doorSpeedScoreNum = Double.parseDouble(doorSpeedScoreNumber);
                }
                int doorSpeedScore = (int) Math.ceil(doorSpeedScoreNum);
                //获取维修员服务评分
                String repairmanServiceScoreNumber = repairDto.getRepairmanServiceScore();
                Double repairmanServiceScoreNum = 0.0;
                if (!StringUtil.isEmpty(repairmanServiceScoreNumber)) {
                    repairmanServiceScoreNum = Double.parseDouble(repairmanServiceScoreNumber);
                }
                int repairmanServiceScore = (int) Math.ceil(repairmanServiceScoreNum);
                //取得平均分
                double averageNumber = (appraiseScoreNum + doorSpeedScoreNum + repairmanServiceScoreNum) / 3.0;
                BigDecimal averageNum = new BigDecimal(averageNumber);
                Double average = averageNum.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                repairDto.setAppraiseScore(String.valueOf(appraiseScore));
                repairDto.setDoorSpeedScore(String.valueOf(doorSpeedScore));
                repairDto.setRepairmanServiceScore(String.valueOf(repairmanServiceScore));
                repairDto.setAverage(String.valueOf(average));
                ownerRepairs.add(repairDto);
            }
            refreshRepair(ownerRepairs);
        } else {
            ownerRepairs = new ArrayList<>();
        }
        ResponseEntity<String> responseEntity = ResultVo.createResponseEntity((int) Math.ceil((double) count / (double) reqJson.getInteger("row")), count, ownerRepairs);
        context.setResponseEntity(responseEntity);
    }

    private void refreshRepair(List<RepairDto> ownerRepairs) {

        List<String> repairIds = new ArrayList<>();
        for (RepairDto apiOwnerRepairDataVo : ownerRepairs) {
            repairIds.add(apiOwnerRepairDataVo.getRepairId());
        }

        if (repairIds.size() < 1) {
            return;
        }
        RepairUserDto repairUserDto = new RepairUserDto();
        repairUserDto.setRepairIds(repairIds.toArray(new String[repairIds.size()]));
        List<RepairUserDto> repairUserDtos = repairUserInnerServiceSMOImpl.queryRepairUsers(repairUserDto);

        for (RepairUserDto tmpRepairUserDto : repairUserDtos) {
            for (RepairDto apiOwnerRepairDataVo : ownerRepairs) {
                if (tmpRepairUserDto.getRepairId().equals(apiOwnerRepairDataVo.getRepairId())) {
                    apiOwnerRepairDataVo.setStaffId(tmpRepairUserDto.getUserId());
                    //apiOwnerRepairDataVo.setStatmpRepairUserDto.getUserName());
                }
            }
        }

        //刷入图片信息
        List<PhotoVo> photoVos = null;
        List<PhotoVo> repairPhotos = null;  //业主上传维修图片
        List<PhotoVo> beforePhotos = null;  //维修前图片
        List<PhotoVo> afterPhotos = null;  //维修后图片
        PhotoVo photoVo = null;
        String imgUrl = MappingCache.getValue("IMG_PATH");
        imgUrl += (!StringUtil.isEmpty(imgUrl) && imgUrl.endsWith("/") ? "" : "/");
        for (RepairDto repairDto : ownerRepairs) {
            FileRelDto fileRelDto = new FileRelDto();
            fileRelDto.setObjId(repairDto.getRepairId());
            List<FileRelDto> fileRelDtos = fileRelInnerServiceSMOImpl.queryFileRels(fileRelDto);
            photoVos = new ArrayList<>();
            repairPhotos = new ArrayList<>();
            beforePhotos = new ArrayList<>();
            afterPhotos = new ArrayList<>();
            for (FileRelDto tmpFileRelDto : fileRelDtos) {
                photoVo = new PhotoVo();
                //photoVo.setUrl("/callComponent/download/getFile/file?fileId=" + tmpFileRelDto.getFileRealName() + "&communityId=" + repairDto.getCommunityId());
                photoVo.setUrl(imgUrl + tmpFileRelDto.getFileRealName());
                photoVo.setRelTypeCd(tmpFileRelDto.getRelTypeCd());
                photoVos.add(photoVo);
                if (tmpFileRelDto.getRelTypeCd().equals(FileRelDto.REL_TYPE_CD_REPAIR)) {  //维修图片
                    photoVo = new PhotoVo();
                   // photoVo.setUrl("/callComponent/download/getFile/file?fileId=" + tmpFileRelDto.getFileRealName() + "&communityId=" + repairDto.getCommunityId());
                    photoVo.setUrl(imgUrl + tmpFileRelDto.getFileRealName());
                    photoVo.setRelTypeCd(tmpFileRelDto.getRelTypeCd());
                    repairPhotos.add(photoVo);  //维修图片
                } else if (tmpFileRelDto.getRelTypeCd().equals(FileRelDto.BEFORE_REPAIR_PHOTOS)) {  //维修前图片
                    photoVo = new PhotoVo();
                    //photoVo.setUrl("/callComponent/download/getFile/file?fileId=" + tmpFileRelDto.getFileRealName() + "&communityId=" + repairDto.getCommunityId());
                    photoVo.setUrl(imgUrl + tmpFileRelDto.getFileRealName());
                    photoVo.setRelTypeCd(tmpFileRelDto.getRelTypeCd());
                    beforePhotos.add(photoVo);  //维修前图片
                } else if (tmpFileRelDto.getRelTypeCd().equals(FileRelDto.AFTER_REPAIR_PHOTOS)) {  //维修后图片
                    photoVo = new PhotoVo();
                    //photoVo.setUrl("/callComponent/download/getFile/file?fileId=" + tmpFileRelDto.getFileRealName() + "&communityId=" + repairDto.getCommunityId());
                    photoVo.setUrl(imgUrl + tmpFileRelDto.getFileRealName());
                    photoVo.setRelTypeCd(tmpFileRelDto.getRelTypeCd());
                    afterPhotos.add(photoVo);
                }
            }
            repairDto.setPhotos(photoVos);
            repairDto.setRepairPhotos(repairPhotos);
            repairDto.setBeforePhotos(beforePhotos);
            repairDto.setAfterPhotos(afterPhotos);
        }

    }
}
