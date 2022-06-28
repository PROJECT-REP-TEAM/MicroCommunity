package com.java110.fee.cmd.fee;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.fee.FeeAttrDto;
import com.java110.dto.fee.FeeConfigDto;
import com.java110.dto.fee.FeeDto;
import com.java110.fee.smo.impl.FeeAttrInnerServiceSMOImpl;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.fee.IPayFeeV1InnerServiceSMO;
import com.java110.po.fee.FeeAttrPo;
import com.java110.po.fee.PayFeePo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Java110Cmd(serviceCode = "fee.updateFee")
public class UpdateFeeCmd extends Cmd {


    private static final int DEFAULT_ADD_FEE_COUNT = 200;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IPayFeeV1InnerServiceSMO payFeeV1InnerServiceSMOImpl;

    @Autowired
    private FeeAttrInnerServiceSMOImpl feeAttrInnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException {

        // super.validatePageInfo(pd);
        Assert.hasKeyAndValue(reqJson, "communityId", "未包含小区ID");
        Assert.hasKeyAndValue(reqJson, "feeId", "未包含feeId");
        Assert.hasKeyAndValue(reqJson, "startTime", "未包含开始时间");
        Assert.hasKeyAndValue(reqJson, "endTime", "未包含结束时间");

        FeeDto feeDto = new FeeDto();
        feeDto.setCommunityId(reqJson.getString("communityId"));
        feeDto.setFeeId(reqJson.getString("feeId"));

        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);

        Assert.listOnlyOne(feeDtos, "未查询到费用信息 或查询到多条" + reqJson);
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException {

        PayFeePo payFeePo = BeanConvertUtil.covertBean(reqJson, PayFeePo.class);
        int flag = payFeeV1InnerServiceSMOImpl.updatePayFee(payFeePo);

        if(flag < 1){
            throw new CmdException("修改费用");
        }

        if(!reqJson.containsKey("computingFormula")
                || !FeeConfigDto.COMPUTING_FORMULA_RANT_RATE.equals(reqJson.getString("computingFormula"))){
            return ;
        }

        if(reqJson.containsKey("rate")) {
            FeeAttrPo feeAttrPo = new FeeAttrPo();
            feeAttrPo.setFeeId(payFeePo.getFeeId());
            feeAttrPo.setSpecCd(FeeAttrDto.SPEC_CD_RATE);
            feeAttrPo.setValue(reqJson.getString("rate"));
            feeAttrInnerServiceSMOImpl.updateFeeAttr(feeAttrPo);
        }

        if(reqJson.containsKey("rateCycle")) {
            FeeAttrPo feeAttrPo = new FeeAttrPo();
            feeAttrPo.setFeeId(payFeePo.getFeeId());
            feeAttrPo.setSpecCd(FeeAttrDto.SPEC_CD_RATE_CYCLE);
            feeAttrPo.setValue(reqJson.getString("rateCycle"));
            feeAttrInnerServiceSMOImpl.updateFeeAttr(feeAttrPo);
        }

        if(reqJson.containsKey("rateStartTime")) {
            FeeAttrPo feeAttrPo = new FeeAttrPo();
            feeAttrPo.setFeeId(payFeePo.getFeeId());
            feeAttrPo.setSpecCd(FeeAttrDto.SPEC_CD_RATE_START_TIME);
            feeAttrPo.setValue(reqJson.getString("rateStartTime"));
            feeAttrInnerServiceSMOImpl.updateFeeAttr(feeAttrPo);
        }
    }
}
