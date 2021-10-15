package com.java110.api.listener.fee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.fee.IFeeBMO;
import com.java110.api.bmo.tempCarFeeConfig.ITempCarFeeConfigBMO;
import com.java110.api.bmo.tempCarFeeConfigAttr.ITempCarFeeConfigAttrBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.fee.FeeConfigDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.parking.ParkingAreaDto;
import com.java110.intf.community.IParkingAreaInnerServiceSMO;
import com.java110.po.fee.PayFeeConfigPo;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ServiceCodeTempCarFeeConfigConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * 保存商户侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("saveTempCarFeeConfigListener")
public class SaveTempCarFeeConfigListener extends AbstractServiceApiPlusListener {

    @Autowired
    private ITempCarFeeConfigBMO tempCarFeeConfigBMOImpl;

    @Autowired
    private IFeeBMO feeBMOImpl;

    @Autowired
    private ITempCarFeeConfigAttrBMO tempCarFeeConfigAttrBMOImpl;

    @Autowired
    private IParkingAreaInnerServiceSMO parkingAreaInnerServiceSMOImpl;

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        //Assert.hasKeyAndValue(reqJson, "xxx", "xxx");

        Assert.hasKeyAndValue(reqJson, "feeName", "请求报文中未包含feeName");
        Assert.hasKeyAndValue(reqJson, "paId", "请求报文中未包含paId");
        Assert.hasKeyAndValue(reqJson, "carType", "请求报文中未包含carType");
        Assert.hasKeyAndValue(reqJson, "ruleId", "请求报文中未包含ruleId");
        Assert.hasKeyAndValue(reqJson, "communityId", "请求报文中未包含communityId");
        Assert.hasKeyAndValue(reqJson, "startTime", "请求报文中未包含startTime");
        Assert.hasKeyAndValue(reqJson, "endTime", "请求报文中未包含endTime");

        //查询停车场编号
        ParkingAreaDto parkingAreaDto = new ParkingAreaDto();
        parkingAreaDto.setPaId(reqJson.getString("paId"));
        parkingAreaDto.setCommunityId(reqJson.getString("communityId"));
        List<ParkingAreaDto> parkingAreaDtos = parkingAreaInnerServiceSMOImpl.queryParkingAreas(parkingAreaDto);

        Assert.listOnlyOne(parkingAreaDtos, "停车场不存在");
        reqJson.put("areaNum", parkingAreaDtos.get(0).getNum());
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        reqJson.put("feeConfigId", GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_configId));
        tempCarFeeConfigBMOImpl.addTempCarFeeConfig(reqJson, context);

        //处理房屋属性
        dealAttr(reqJson, context);

        //补费用项数据
        PayFeeConfigPo payFeeConfigPo = new PayFeeConfigPo();
        payFeeConfigPo.setAdditionalAmount("0");
        payFeeConfigPo.setBillType(FeeConfigDto.BILL_TYPE_YEAR);
        payFeeConfigPo.setCommunityId(reqJson.getString("communityId"));
        payFeeConfigPo.setComputingFormula(FeeConfigDto.COMPUTING_FORMULA_TEMP_CAR);
        payFeeConfigPo.setComputingFormulaText("");
        payFeeConfigPo.setConfigId(reqJson.getString("feeConfigId"));
        payFeeConfigPo.setEndTime(reqJson.getString("endTime"));
        payFeeConfigPo.setStartTime(reqJson.getString("startTime"));
        payFeeConfigPo.setFeeFlag(FeeDto.FEE_FLAG_ONCE);
        payFeeConfigPo.setFeeName(reqJson.getString("feeName"));
        payFeeConfigPo.setFeeTypeCd(FeeConfigDto.FEE_TYPE_CD_PARKING);
        payFeeConfigPo.setIsDefault(FeeConfigDto.DEFAULT_FEE_CONFIG);
        payFeeConfigPo.setPaymentCd(FeeConfigDto.PAYMENT_CD_PRE);
        payFeeConfigPo.setPaymentCycle("1");
        payFeeConfigPo.setSquarePrice("0");
        super.insert(context, payFeeConfigPo, BusinessTypeConstant.BUSINESS_TYPE_SAVE_FEE_CONFIG);
        
    }

    private void dealAttr(JSONObject reqJson, DataFlowContext context) {

        if (!reqJson.containsKey("attrs")) {
            return;
        }

        JSONArray attrs = reqJson.getJSONArray("attrs");
        if (attrs == null || attrs.size() < 1) {
            return;
        }


        JSONObject attr = null;
        for (int attrIndex = 0; attrIndex < attrs.size(); attrIndex++) {
            attr = attrs.getJSONObject(attrIndex);
            attr.put("configId", reqJson.getString("configId"));
            attr.put("communityId", reqJson.getString("communityId"));
            tempCarFeeConfigAttrBMOImpl.addTempCarFeeConfigAttr(attr, context);
        }

    }

    @Override
    public String getServiceCode() {
        return ServiceCodeTempCarFeeConfigConstant.ADD_TEMPCARFEECONFIG;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

}
