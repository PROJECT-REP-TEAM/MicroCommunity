package com.java110.fee.cmd.fee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.RoomDto;
import com.java110.dto.contract.ContractDto;
import com.java110.dto.fee.FeeAttrDto;
import com.java110.dto.fee.FeeConfigDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.dto.payFeeBatch.PayFeeBatchDto;
import com.java110.dto.user.UserDto;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.fee.IFeeAttrInnerServiceSMO;
import com.java110.intf.fee.IFeeConfigInnerServiceSMO;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.fee.IPayFeeBatchV1InnerServiceSMO;
import com.java110.intf.store.IContractInnerServiceSMO;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.intf.user.IOwnerInnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.po.fee.FeeAttrPo;
import com.java110.po.fee.PayFeePo;
import com.java110.po.payFeeBatch.PayFeeBatchPo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Java110Cmd(serviceCode = "fee.createFeeByCombo")
public class CreateFeeByComboCmd extends Cmd {

    @Autowired
    private IPayFeeBatchV1InnerServiceSMO payFeeBatchV1InnerServiceSMOImpl;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Autowired
    private IFeeConfigInnerServiceSMO feeConfigInnerServiceSMOImpl;

    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IFeeAttrInnerServiceSMO feeAttrInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IOwnerInnerServiceSMO ownerInnerServiceSMOImpl;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;

    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMOImpl;


    @Autowired
    private IContractInnerServiceSMO contractInnerServiceSMOImpl;

    /**
     * communityId: vc.getCurrentCommunity().communityId,
     * configs: _fees,
     * payerObjId: $that.createFeeByComboInfo.payerObjId,
     * payerObjName: $that.createFeeByComboInfo.payerObjName,
     * payerObjType: $that.createFeeByComboInfo.payerObjType,
     * comboId: $that.createFeeByComboInfo.comboId
     *
     * @param event              ????????????
     * @param cmdDataFlowContext ??????????????????
     * @param reqJson
     * @throws CmdException
     */
    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {
        Assert.jsonObjectHaveKey(reqJson, "communityId", "????????????????????????communityId??????");
        Assert.jsonObjectHaveKey(reqJson, "payerObjId", "????????????????????????communityId??????");
        Assert.jsonObjectHaveKey(reqJson, "payerObjName", "????????????????????????communityId??????");
        Assert.jsonObjectHaveKey(reqJson, "payerObjType", "????????????????????????communityId??????");
        Assert.jsonObjectHaveKey(reqJson, "comboId", "????????????????????????communityId??????");

        if (!reqJson.containsKey("configs")) {
            throw new IllegalArgumentException("?????????????????????");
        }

        JSONArray configs = reqJson.getJSONArray("configs");

        if (configs == null || configs.size() < 1) {
            throw new IllegalArgumentException("?????????????????????");
        }

    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {

        //???????????????
        generatorBatch(reqJson);

        JSONArray configs = reqJson.getJSONArray("configs");

        for (int configIndex = 0; configIndex < configs.size(); configIndex++) {
            doCreateConfigFee(reqJson, configs.getJSONObject(configIndex));
        }

    }

    private void doCreateConfigFee(JSONObject reqJson, JSONObject config) {

        FeeConfigDto feeConfigDto = new FeeConfigDto();
        feeConfigDto.setCommunityId(reqJson.getString("communityId"));
        feeConfigDto.setConfigId(config.getString("configId"));
        List<FeeConfigDto> feeConfigDtos = feeConfigInnerServiceSMOImpl.queryFeeConfigs(feeConfigDto);
        Assert.listOnlyOne(feeConfigDtos, "???????????????ID????????????????????????" + reqJson.getString("configId"));

        if (!FeeDto.FEE_FLAG_CYCLE.equals(feeConfigDtos.get(0).getFeeFlag()) && config.containsKey("endTime")) {
            Date endTime = null;
            Date configEndTime = null;
            try {
                endTime = DateUtil.getDateFromString(config.getString("endTime"), DateUtil.DATE_FORMATE_STRING_B);
                configEndTime = DateUtil.getDateFromString(feeConfigDtos.get(0).getEndTime(), DateUtil.DATE_FORMATE_STRING_A);
                if (endTime.getTime() > configEndTime.getTime()) {
                    throw new IllegalArgumentException("???????????????????????????????????????");
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("??????????????????" + reqJson.getString("endTime"));
            }
        }

        List<PayFeePo> feePos = new ArrayList<>();
        List<FeeAttrPo> feeAttrsPos = new ArrayList<>();

        feePos.add(BeanConvertUtil.covertBean(addRoomFee(config, reqJson,feeConfigDtos), PayFeePo.class));

        if (!FeeDto.FEE_FLAG_CYCLE.equals(feeConfigDtos.get(0).getFeeFlag())) {
            feeAttrsPos.add(addFeeAttr(reqJson, FeeAttrDto.SPEC_CD_ONCE_FEE_DEADLINE_TIME,
                    config.containsKey("endTime") ? config.getString("endTime") : reqJson.getString("configEndTime")));
        }


        saveOwnerFeeAttr(reqJson, feeAttrsPos);


        feeAttrsPos.add(addFeeAttr(reqJson, FeeAttrDto.SPEC_CD_COMBO_ID,
                reqJson.getString("comboId")));


        int saveFlag = saveFeeAndAttrs(feePos, feeAttrsPos);

        if (saveFlag < 1) {
            throw new CmdException("??????????????????");
        }

    }

    private void saveOwnerFeeAttr(JSONObject reqJson, List<FeeAttrPo> feeAttrsPos) {

        String objName = "";
        String ownerId = "";
        String tel = "";
        String name = "";

        if (FeeDto.PAYER_OBJ_TYPE_ROOM.equals(reqJson.getString("payerObjType"))) {
            RoomDto roomDto = new RoomDto();
            roomDto.setRoomId(reqJson.getString("payerObjId"));
            roomDto.setCommunityId(reqJson.getString("communityId"));
            List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);
            if (roomDtos == null || roomDtos.size() != 1) {
                return;
            }
            roomDto = roomDtos.get(0);
            objName = roomDto.getFloorNum() + "-" + roomDto.getUnitNum() + "-" + roomDto.getRoomNum();

            OwnerDto ownerDto = new OwnerDto();
            ownerDto.setRoomId(roomDto.getRoomId());
            List<OwnerDto> ownerDtos = ownerInnerServiceSMOImpl.queryOwners(ownerDto);

            if (ownerDtos != null && ownerDtos.size() > 0) {
                ownerId = ownerDtos.get(0).getOwnerId();
                tel = ownerDtos.get(0).getLink();
                name = ownerDtos.get(0).getName();
            }
        } else if (FeeDto.PAYER_OBJ_TYPE_CAR.equals(reqJson.getString("payerObjType"))) {
            OwnerCarDto ownerCarDto = new OwnerCarDto();
            ownerCarDto.setCarId(reqJson.getString("payerObjId"));
            ownerCarDto.setCommunityId(reqJson.getString("communityId"));
            List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);

            if (ownerCarDtos == null || ownerCarDtos.size() < 1) {
                return;
            }
            objName = ownerCarDtos.get(0).getCarNum();
            ownerId = ownerCarDtos.get(0).getOwnerId();
            tel = ownerCarDtos.get(0).getLink();
            name = ownerCarDtos.get(0).getOwnerName();
        } else if (FeeDto.PAYER_OBJ_TYPE_CONTRACT.equals(reqJson.getString("payerObjType"))) {


            ContractDto contractDto = new ContractDto();
            contractDto.setContractId(reqJson.getString("payerObjId"));
            List<ContractDto> contractDtos = contractInnerServiceSMOImpl.queryContracts(contractDto);

            if (contractDtos == null || contractDtos.size() < 1) {
                return;
            }

            objName = contractDtos.get(0).getContractCode();
            tel = contractDtos.get(0).getbLink();
            name = contractDtos.get(0).getPartyB();
        }
        if (!StringUtil.isEmpty(ownerId)) {
            feeAttrsPos.add(addFeeAttr(reqJson, FeeAttrDto.SPEC_CD_OWNER_ID, ownerId));
        }
        if (!StringUtil.isEmpty(tel)) {
            feeAttrsPos.add(addFeeAttr(reqJson, FeeAttrDto.SPEC_CD_OWNER_LINK, tel));
        }
        if (!StringUtil.isEmpty(name)) {
            feeAttrsPos.add(addFeeAttr(reqJson, FeeAttrDto.SPEC_CD_OWNER_NAME, name));
        }
        //??????????????????
        if (!StringUtil.isEmpty(objName)) {
            feeAttrsPos.add(addFeeAttr(reqJson, FeeAttrDto.SPEC_CD_PAY_OBJECT_NAME,
                    objName));
        }
    }


    private int saveFeeAndAttrs(List<PayFeePo> feePos, List<FeeAttrPo> feeAttrsPos) {
        int flag = feeInnerServiceSMOImpl.saveFee(feePos);
        if (flag < 1) {
            return flag;
        }

        flag = feeAttrInnerServiceSMOImpl.saveFeeAttrs(feeAttrsPos);

        return flag;
    }

    /**
     * ??????????????????
     *
     * @param paramInJson ???????????????????????????
     * @return ?????????????????????????????????
     */
    public JSONObject addRoomFee(JSONObject config, JSONObject paramInJson,List<FeeConfigDto> feeConfigDtos) {
        //??????????????????
        //???????????????]
        JSONObject businessUnit = new JSONObject();
        businessUnit.put("feeId", GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_feeId));
        businessUnit.put("configId", feeConfigDtos.get(0).getConfigId());
        businessUnit.put("feeTypeCd", feeConfigDtos.get(0).getFeeTypeCd());
        businessUnit.put("incomeObjId", paramInJson.getString("storeId"));
        businessUnit.put("amount", "-1.00");
        if (config.containsKey("amount") && !StringUtil.isEmpty(config.getString("amount"))) {
            businessUnit.put("amount", config.getString("amount"));
        }
        businessUnit.put("startTime", config.getString("startTime"));
        businessUnit.put("endTime", config.getString("startTime"));
        businessUnit.put("communityId", paramInJson.getString("communityId"));
        businessUnit.put("payerObjId", paramInJson.getString("payerObjId"));
        businessUnit.put("payerObjType", paramInJson.getString("payerObjType"));
        businessUnit.put("feeFlag", feeConfigDtos.get(0).getFeeFlag());
        businessUnit.put("state", "2008001");
        businessUnit.put("batchId", paramInJson.getString("batchId"));
        businessUnit.put("userId", paramInJson.getString("userId"));

        paramInJson.put("feeId", businessUnit.getString("feeId"));
        return businessUnit;
    }

    public FeeAttrPo addFeeAttr(JSONObject paramInJson, String specCd, String value) {
        FeeAttrPo feeAttrPo = new FeeAttrPo();
        feeAttrPo.setCommunityId(paramInJson.getString("communityId"));
        feeAttrPo.setSpecCd(specCd);
        feeAttrPo.setValue(value);
        feeAttrPo.setFeeId(paramInJson.getString("feeId"));
        feeAttrPo.setAttrId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_attrId));
        return feeAttrPo;

    }

    /**
     * ???????????????
     *
     * @param reqJson
     */
    private void generatorBatch(JSONObject reqJson) {
        PayFeeBatchPo payFeeBatchPo = new PayFeeBatchPo();
        payFeeBatchPo.setBatchId(GenerateCodeFactory.getGeneratorId("12"));
        payFeeBatchPo.setCommunityId(reqJson.getString("communityId"));
        payFeeBatchPo.setCreateUserId(reqJson.getString("userId"));
        UserDto userDto = new UserDto();
        userDto.setUserId(reqJson.getString("userId"));
        List<UserDto> userDtos = userInnerServiceSMOImpl.getUsers(userDto);

        Assert.listOnlyOne(userDtos, "???????????????");
        payFeeBatchPo.setCreateUserName(userDtos.get(0).getUserName());
        payFeeBatchPo.setState(PayFeeBatchDto.STATE_NORMAL);
        payFeeBatchPo.setMsg("??????");
        int flag = payFeeBatchV1InnerServiceSMOImpl.savePayFeeBatch(payFeeBatchPo);

        if (flag < 1) {
            throw new IllegalArgumentException("??????????????????");
        }

        reqJson.put("batchId", payFeeBatchPo.getBatchId());
    }
}
