package com.java110.api.listener.returnPayFee;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.intf.fee.IReturnPayFeeInnerServiceSMO;
import com.java110.dto.returnPayFee.ReturnPayFeeDto;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.ServiceCodeReturnPayFeeConstant;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.api.returnPayFee.ApiReturnPayFeeDataVo;
import com.java110.vo.api.returnPayFee.ApiReturnPayFeeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;


/**
 * 查询小区侦听类
 */
@Java110Listener("listReturnPayFeesListener")
public class ListReturnPayFeesListener extends AbstractServiceApiListener {

    @Autowired
    private IReturnPayFeeInnerServiceSMO returnPayFeeInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeReturnPayFeeConstant.LIST_RETURNPAYFEES;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }


    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    public IReturnPayFeeInnerServiceSMO getReturnPayFeeInnerServiceSMOImpl() {
        return returnPayFeeInnerServiceSMOImpl;
    }

    public void setReturnPayFeeInnerServiceSMOImpl(IReturnPayFeeInnerServiceSMO returnPayFeeInnerServiceSMOImpl) {
        this.returnPayFeeInnerServiceSMOImpl = returnPayFeeInnerServiceSMOImpl;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        ReturnPayFeeDto returnPayFeeDto = BeanConvertUtil.covertBean(reqJson, ReturnPayFeeDto.class);

        int count = returnPayFeeInnerServiceSMOImpl.queryReturnPayFeesCount(returnPayFeeDto);

        List<ApiReturnPayFeeDataVo> returnPayFees = new ArrayList<>();

        if (count > 0) {
            List<ApiReturnPayFeeDataVo> apiReturnPayFeeDataVos = BeanConvertUtil.covertBeanList(returnPayFeeInnerServiceSMOImpl.queryReturnPayFees(returnPayFeeDto), ApiReturnPayFeeDataVo.class);
            for (ApiReturnPayFeeDataVo apiReturnPayFeeDataVo : apiReturnPayFeeDataVos) {
                //获取周期
                String cycles = apiReturnPayFeeDataVo.getCycles();
                if (!StringUtil.isEmpty(cycles) && cycles.contains("-")) {
                    String substring = cycles.substring(1);
                    apiReturnPayFeeDataVo.setCycles(substring);
                }
                //获取应收金额
                String receivableAmount = apiReturnPayFeeDataVo.getReceivableAmount();
                if (!StringUtil.isEmpty(receivableAmount) && receivableAmount.contains("-")) {
                    String substring = receivableAmount.substring(1);
                    apiReturnPayFeeDataVo.setReceivableAmount(substring);
                }
                //获取实收金额
                String receivedAmount = apiReturnPayFeeDataVo.getReceivedAmount();
                if (!StringUtil.isEmpty(receivedAmount) && receivedAmount.contains("-")) {
                    String substring = receivedAmount.substring(1);
                    apiReturnPayFeeDataVo.setReceivedAmount(substring);
                }
                returnPayFees.add(apiReturnPayFeeDataVo);
            }
        } else {
            returnPayFees = new ArrayList<>();
        }

        ApiReturnPayFeeVo apiReturnPayFeeVo = new ApiReturnPayFeeVo();

        apiReturnPayFeeVo.setTotal(count);
        apiReturnPayFeeVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiReturnPayFeeVo.setReturnPayFees(returnPayFees);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiReturnPayFeeVo), HttpStatus.OK);

        context.setResponseEntity(responseEntity);

    }
}