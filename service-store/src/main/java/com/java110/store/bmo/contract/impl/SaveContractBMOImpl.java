package com.java110.store.bmo.contract.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Transactional;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.RoomDto;
import com.java110.dto.contract.ContractDto;
import com.java110.dto.contractType.ContractTypeDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.owner.OwnerRoomRelDto;
import com.java110.dto.rentingPool.RentingPoolDto;
import com.java110.dto.store.StoreDto;
import com.java110.intf.common.IContractApplyUserInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.store.*;
import com.java110.intf.user.IOwnerRoomRelInnerServiceSMO;
import com.java110.intf.user.IRentingPoolInnerServiceSMO;
import com.java110.po.contract.ContractPo;
import com.java110.po.contractAttr.ContractAttrPo;
import com.java110.po.contractFile.ContractFilePo;
import com.java110.po.contractRoom.ContractRoomPo;
import com.java110.po.owner.OwnerRoomRelPo;
import com.java110.po.rentingPool.RentingPoolPo;
import com.java110.po.room.RoomPo;
import com.java110.store.bmo.contract.ISaveContractBMO;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("saveContractBMOImpl")
public class SaveContractBMOImpl implements ISaveContractBMO {

    @Autowired
    private IContractInnerServiceSMO contractInnerServiceSMOImpl;

    @Autowired
    private IContractAttrInnerServiceSMO contractAttrInnerServiceSMOImpl;

    @Autowired
    private IContractTypeInnerServiceSMO contractTypeInnerServiceSMOImpl;

    @Autowired
    private IRentingPoolInnerServiceSMO rentingPoolInnerServiceSMOImpl; // ????????????

    @Autowired
    private IContractApplyUserInnerServiceSMO contractApplyUserInnerServiceSMOImpl;

    @Autowired
    private IContractFileInnerServiceSMO contractFileInnerServiceSMOImpl;

    @Autowired
    private IContractRoomInnerServiceSMO contractRoomInnerServiceSMOImpl;

    @Autowired
    private IOwnerRoomRelInnerServiceSMO ownerRoomRelInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    /**
     * ??????????????????
     *
     * @param contractPo
     * @return ?????????????????????????????????
     */
    @Java110Transactional
    public ResponseEntity<String> save(ContractPo contractPo, JSONObject reqJson) {

        //?????? ????????????????????????
        ContractTypeDto contractTypeDto = new ContractTypeDto();
        contractTypeDto.setContractTypeId(contractPo.getContractType());
        contractTypeDto.setStoreId(contractPo.getStoreId());
        List<ContractTypeDto> contractTypeDtos = contractTypeInnerServiceSMOImpl.queryContractTypes(contractTypeDto);

        Assert.listOnlyOne(contractTypeDtos, "????????????????????????");

        validateRoom(contractPo, reqJson);

        String audit = contractTypeDtos.get(0).getAudit();

        if (ContractTypeDto.NO_AUDIT.equals(audit)) {
            contractPo.setState("22");
        } else {
            contractPo.setState("11");
        }
        //??????????????????????????????
        ContractDto contractDto = new ContractDto();
        contractDto.setStoreId(contractPo.getStoreId());
        contractDto.setContractCode(contractPo.getContractCode());
        List<ContractDto> contractDtos = contractInnerServiceSMOImpl.queryContracts(contractDto);

        if (contractDtos != null && contractDtos.size() > 0) {
            throw new IllegalArgumentException("??????" + "[" + contractPo.getContractCode() + "]?????????");
        }

        contractPo.setContractId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_contractId));
        //????????????
        List<ContractFilePo> filePos = contractPo.getContractFilePo();
        int flag = contractInnerServiceSMOImpl.saveContract(contractPo);
        for (ContractFilePo file : filePos) {
            if (file.getFileRealName().length() > 0 && file.getFileSaveName().length() > 0) {
                file.setContractId(contractPo.getContractId());
                contractFileInnerServiceSMOImpl.saveContractFile(file);
            }
        }

        if (flag < 0) {
            return ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "????????????");
        }

        saveContractRoomRel(reqJson, contractPo);

        //????????????????????? ?????????
        if (!ContractTypeDto.NO_AUDIT.equals(audit)) {
            //????????????
            ContractDto tmpContractDto = BeanConvertUtil.covertBean(contractPo, ContractDto.class);
            tmpContractDto.setCurrentUserId(reqJson.getString("userId"));
            tmpContractDto.setNextUserId(reqJson.getString("nextUserId"));
            contractApplyUserInnerServiceSMOImpl.startProcess(tmpContractDto);
        }


        if (StoreDto.STORE_ADMIN.equals(contractPo.getStoreId())) {
            noticeRentUpdateState(contractPo, audit);
        }


        if (!reqJson.containsKey("contractTypeSpecs")) {
            return ResultVo.createResponseEntity(ResultVo.CODE_OK, "????????????");
        }

        JSONArray contractTypeSpecs = reqJson.getJSONArray("contractTypeSpecs");

        if (contractTypeSpecs.size() < 1) {
            return ResultVo.createResponseEntity(ResultVo.CODE_OK, "????????????");
        }

        for (int typeSpecIndex = 0; typeSpecIndex < contractTypeSpecs.size(); typeSpecIndex++) {
            saveContractAttr(contractTypeSpecs.getJSONObject(typeSpecIndex), contractPo);
        }


        return ResultVo.createResponseEntity(ResultVo.CODE_OK, "????????????");

    }

    /**
     * ????????????????????????
     *
     * @param contractPo
     * @param reqJson
     */
    private void validateRoom(ContractPo contractPo, JSONObject reqJson) {
        //?????? ??????????????????????????????
        if (!reqJson.containsKey("rooms")) {
            return;
        }
        JSONArray rooms = reqJson.getJSONArray("rooms");
        for (int conFileIndex = 0; conFileIndex < rooms.size(); conFileIndex++) {
            JSONObject roomObj = rooms.getJSONObject(conFileIndex);

            //????????????????????????
            RoomDto roomDto = new RoomDto();
            roomDto.setRoomId(roomObj.getString("roomId"));
            roomDto.setCommunityId(reqJson.getString("communityId"));
            List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

            Assert.listOnlyOne(roomDtos, "???????????????");

            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            ownerRoomRelDto.setRoomId(roomObj.getString("roomId"));
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);
            //???????????????
            if (ownerRoomRelDtos == null || ownerRoomRelDtos.size() == 0) { // ?????????????????????????????????????????????????????????
                continue;
            }
            //???????????? ??????????????????
            if (contractPo.getObjId().equals(ownerRoomRelDtos.get(0).getOwnerId())) {
                continue;
            }

            //???????????????????????????
            FeeDto feeDto = new FeeDto();
            feeDto.setPayerObjType(FeeDto.PAYER_OBJ_TYPE_ROOM);
            feeDto.setPayerObjId(roomObj.getString("roomId"));
            feeDto.setState(FeeDto.STATE_DOING);
            List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);
            if (feeDtos != null && feeDtos.size() > 0) {
                throw new IllegalArgumentException(roomDtos.get(0).getRoomNum() + "?????????????????????????????? ????????????");
            }
        }
    }

    private void saveContractRoomRel(JSONObject reqJson, ContractPo contractPo) {

        //??????????????????
        if (!reqJson.containsKey("rooms")) {
            return;
        }
        JSONArray rooms = reqJson.getJSONArray("rooms");
        for (int conFileIndex = 0; conFileIndex < rooms.size(); conFileIndex++) {
            JSONObject resourceStore = rooms.getJSONObject(conFileIndex);
            ContractRoomPo contractRoomPo = BeanConvertUtil.covertBean(resourceStore, ContractRoomPo.class);
            contractRoomPo.setCrId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_crId));
            contractRoomPo.setContractId(contractPo.getContractId());
            contractRoomPo.setStoreId(contractPo.getStoreId());
            contractRoomPo.setRoomName(
                    resourceStore.getString("floorNum") + "-"
                            + resourceStore.getString("unitNum") + "-" + resourceStore.getString("roomNum"));
            contractRoomInnerServiceSMOImpl.saveContractRoom(contractRoomPo);

            //?????????
            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            ownerRoomRelDto.setRoomId(contractRoomPo.getRoomId());
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);

            if (ownerRoomRelDtos != null && ownerRoomRelDtos.size() > 0) { // ?????????????????????????????????????????????????????????
                if (contractPo.getObjId().equals(ownerRoomRelDtos.get(0).getOwnerId())) {
                    continue;
                }
            }

            //?????? B???????????? ADD
            String relId = GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_relId);
            OwnerRoomRelPo ownerRoomRelPo = new OwnerRoomRelPo();
            ownerRoomRelPo.setEndTime(contractPo.getEndTime());
            ownerRoomRelPo.setStartTime(contractPo.getStartTime());
            ownerRoomRelPo.setOwnerId(contractPo.getObjId());
            ownerRoomRelPo.setRelId(relId);
            ownerRoomRelPo.setRemark("????????????????????????");
            ownerRoomRelPo.setRoomId(contractRoomPo.getRoomId());
            ownerRoomRelPo.setState("2001");
            ownerRoomRelPo.setUserId("-1");
            ownerRoomRelPo.setOperate("ADD");
            ownerRoomRelPo.setbId("-1");
            ownerRoomRelInnerServiceSMOImpl.saveBusinessOwnerRoomRels(ownerRoomRelPo);

            ownerRoomRelPo = new OwnerRoomRelPo();
            ownerRoomRelPo.setEndTime(contractPo.getEndTime());
            ownerRoomRelPo.setStartTime(contractPo.getStartTime());
            ownerRoomRelPo.setOwnerId(contractPo.getObjId());
            ownerRoomRelPo.setRelId(relId);
            ownerRoomRelPo.setRemark("????????????????????????");
            ownerRoomRelPo.setRoomId(contractRoomPo.getRoomId());
            ownerRoomRelPo.setState("2001");
            ownerRoomRelPo.setUserId("-1");
            ownerRoomRelInnerServiceSMOImpl.saveOwnerRoomRels(ownerRoomRelPo);

            //??????????????????
            RoomPo roomPo = new RoomPo();
            roomPo.setRoomId(contractRoomPo.getRoomId());
            roomPo.setState(RoomDto.STATE_SELL);
            roomPo.setStatusCd(StatusConstant.STATUS_CD_VALID);
            roomInnerServiceSMOImpl.updateRooms(roomPo);
            //????????????
            if (ownerRoomRelDtos != null && ownerRoomRelDtos.size() > 0) {
                ownerRoomRelPo = new OwnerRoomRelPo();
                ownerRoomRelPo.setStatusCd(StatusConstant.STATUS_CD_INVALID);
                ownerRoomRelPo.setRelId(ownerRoomRelDtos.get(0).getRelId());
                ownerRoomRelInnerServiceSMOImpl.updateOwnerRoomRels(ownerRoomRelPo);
                ownerRoomRelPo = BeanConvertUtil.covertBean(ownerRoomRelDtos.get(0), OwnerRoomRelPo.class);
                ownerRoomRelPo.setbId("-1");
                ownerRoomRelPo.setOperate("DEL");
                ownerRoomRelInnerServiceSMOImpl.saveBusinessOwnerRoomRels(ownerRoomRelPo);
            }
        }

    }

    /**
     * ?????? ??????????????????
     *
     * @param contractPo
     */
    private void noticeRentUpdateState(ContractPo contractPo, String audit) {

        if (!contractPo.getObjType().equals(FeeDto.PAYER_OBJ_TYPE_ROOM)
                || StringUtil.isEmpty(contractPo.getObjId())
                || contractPo.getObjId().startsWith("-")) {
            return;
        }
        RentingPoolDto rentingPoolDto = new RentingPoolDto();
        rentingPoolDto.setRoomId(contractPo.getObjId());
        rentingPoolDto.setState(RentingPoolDto.STATE_APPLY_AGREE);
        List<RentingPoolDto> rentingPoolDtos = rentingPoolInnerServiceSMOImpl.queryRentingPools(rentingPoolDto);

        if (rentingPoolDtos == null || rentingPoolDtos.size() < 1) {
            return;
        }


        RentingPoolPo rentingPoolPo = new RentingPoolPo();
        rentingPoolPo.setCommunityId(rentingPoolDtos.get(0).getCommunityId());
        rentingPoolPo.setRentingId(rentingPoolDtos.get(0).getRentingId());

        if (ContractTypeDto.NO_AUDIT.equals(audit)) {
            rentingPoolPo.setState(RentingPoolDto.STATE_FINISH);
        } else {
            rentingPoolPo.setState(RentingPoolDto.STATE_ADMIN_AUDIT);
        }
        rentingPoolInnerServiceSMOImpl.updateRentingPool(rentingPoolPo);
    }

    /**
     * ??????????????????
     *
     * @param jsonObject
     */
    private void saveContractAttr(JSONObject jsonObject, ContractPo contractPo) {

        ContractAttrPo contractAttrPo = new ContractAttrPo();
        contractAttrPo.setAttrId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_attrId));
        contractAttrPo.setContractId(contractPo.getContractId());
        contractAttrPo.setSpecCd(jsonObject.getString("specCd"));
        contractAttrPo.setValue(jsonObject.getString("value"));
        contractAttrPo.setStoreId(contractPo.getStoreId());
        int count = contractAttrInnerServiceSMOImpl.saveContractAttr(contractAttrPo);

        if (count < 1) {
            throw new IllegalArgumentException("??????????????????");
        }
    }

}
