package com.java110.job.adapt.fee.asyn.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.core.smo.IComputeFeeSMO;
import com.java110.dto.fee.FeeDto;
import com.java110.entity.order.Business;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.fee.IPayFeeDetailMonthInnerServiceSMO;
import com.java110.job.adapt.fee.asyn.IPayFeeDetailToMonth;
import com.java110.po.fee.PayFeeDetailPo;
import com.java110.po.payFeeDetailMonth.PayFeeDetailMonthPo;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Service
public class PayFeeDetailToMonthIImpl implements IPayFeeDetailToMonth {


    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IComputeFeeSMO computeFeeSMOImpl;


    @Autowired
    private IPayFeeDetailMonthInnerServiceSMO payFeeDetailMonthInnerServiceSMOImpl;

    @Override
    @Async
    public void doPayFeeDetail(Business business, JSONObject businessPayFeeDetail) {
        //查询缴费明细
        PayFeeDetailPo payFeeDetailPo = BeanConvertUtil.covertBean(businessPayFeeDetail, PayFeeDetailPo.class);
        FeeDto feeDto = new FeeDto();
        feeDto.setFeeId(payFeeDetailPo.getFeeId());
        feeDto.setCommunityId(payFeeDetailPo.getCommunityId());
        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);
        Assert.listOnlyOne(feeDtos, "未查询到费用信息");
        feeDto = feeDtos.get(0);

        Date startTime = null;
        Date endTime = null;
        try {
            startTime = DateUtil.getDateFromString(businessPayFeeDetail.getString("startTime"), DateUtil.DATE_FORMATE_STRING_A);
            endTime = DateUtil.getDateFromString(businessPayFeeDetail.getString("endTime"), DateUtil.DATE_FORMATE_STRING_A);
        } catch (ParseException e) {
            throw new IllegalArgumentException("时间格式错误");
        }

        double maxMonth = Math.ceil(computeFeeSMOImpl.dayCompare(startTime, endTime));

        if (maxMonth < 1) {
            return;
        }

        Map feePriceAll = computeFeeSMOImpl.getFeePrice(feeDto);

        Double feePrice = Double.parseDouble(feePriceAll.get("feePrice").toString());

        BigDecimal totalRecDec = new BigDecimal(businessPayFeeDetail.getDouble("receivedAmount"));
        //每月平均值
        BigDecimal priRecDec = totalRecDec.divide(new BigDecimal(maxMonth), 2, BigDecimal.ROUND_HALF_EVEN);

        List<PayFeeDetailMonthPo> payFeeDetailMonthPos = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        PayFeeDetailMonthPo tmpPayFeeDetailMonthPo = null;
        BigDecimal discountAmount = new BigDecimal(0.0);
        for (int month = 0; month < maxMonth; month++) {
            calendar.setTime(startTime);
            calendar.add(Calendar.MONTH, month);
            discountAmount = new BigDecimal(feePrice).subtract(priRecDec).setScale(2, BigDecimal.ROUND_HALF_EVEN);

            tmpPayFeeDetailMonthPo = new PayFeeDetailMonthPo();
            tmpPayFeeDetailMonthPo.setFeeId(feeDto.getFeeId());
            tmpPayFeeDetailMonthPo.setCommunityId(feeDto.getCommunityId());
            tmpPayFeeDetailMonthPo.setDetailId(businessPayFeeDetail.getString("detailId"));
            tmpPayFeeDetailMonthPo.setDetailMonth((calendar.get(Calendar.MONTH) + 1) + "");
            tmpPayFeeDetailMonthPo.setDetailYear(calendar.get(Calendar.YEAR) + "");
            tmpPayFeeDetailMonthPo.setDiscountAmount(discountAmount.doubleValue() + "");
            tmpPayFeeDetailMonthPo.setMonthId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_monthId));
            tmpPayFeeDetailMonthPo.setReceivableAmount(feePrice + "");
            tmpPayFeeDetailMonthPo.setReceivedAmount(priRecDec.doubleValue() + "");
            tmpPayFeeDetailMonthPo.setRemark("程序计算生成");
            payFeeDetailMonthPos.add(tmpPayFeeDetailMonthPo);
        }

        payFeeDetailMonthInnerServiceSMOImpl.savePayFeeDetailMonths(payFeeDetailMonthPos);

    }

}
