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
package com.java110.fee.cmd.returnPayFee;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.AbstractServiceCmdListener;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.intf.fee.IReturnPayFeeInnerServiceSMO;
import com.java110.intf.fee.IReturnPayFeeV1InnerServiceSMO;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import com.java110.vo.api.returnPayFee.ApiReturnPayFeeDataVo;
import com.java110.vo.api.returnPayFee.ApiReturnPayFeeVo;
import org.springframework.beans.factory.annotation.Autowired;
import com.java110.dto.returnPayFee.ReturnPayFeeDto;
import java.util.List;
import java.util.ArrayList;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 类表述：查询
 * 服务编码：returnPayFee.listReturnPayFee
 * 请求路劲：/app/returnPayFee.ListReturnPayFee
 * add by 吴学文 at 2022-02-21 12:20:03 mail: 928255095@qq.com
 * open source address: https://gitee.com/wuxw7/MicroCommunity
 * 官网：http://www.homecommunity.cn
 * 温馨提示：如果您对此文件进行修改 请不要删除原有作者及注释信息，请补充您的 修改的原因以及联系邮箱如下
 * // modify by 张三 at 2021-09-12 第10行在某种场景下存在某种bug 需要修复，注释10至20行 加入 20行至30行
 */
@Java110Cmd(serviceCode = "returnPayFee.listReturnPayFees")
public class ListReturnPayFeesCmd extends AbstractServiceCmdListener {

  private static Logger logger = LoggerFactory.getLogger(ListReturnPayFeesCmd.class);


    @Autowired
    private IReturnPayFeeInnerServiceSMO returnPayFeeInnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {

        ReturnPayFeeDto returnPayFeeDto = BeanConvertUtil.covertBean(reqJson, ReturnPayFeeDto.class);

        int count = returnPayFeeInnerServiceSMOImpl.queryReturnPayFeesCount(returnPayFeeDto);

        List<ReturnPayFeeDto> returnPayFeeDtos = null;

        List<ApiReturnPayFeeDataVo> returnPayFees;

        if (count > 0) {
            //注意这里处理 记得测试退费逻辑
            returnPayFeeDtos = returnPayFeeInnerServiceSMOImpl.queryReturnPayFees(returnPayFeeDto);
            //List<ReturnPayFeeDto> returnPayFeeDtoList = new ArrayList<>();
//            for (ReturnPayFeeDto returnPayFee : returnPayFeeDtos) {
//                //获取付款方标识
//                String payerObjType = returnPayFee.getPayerObjType();
//                if (!StringUtil.isEmpty(payerObjType) && payerObjType.equals("3333")) { //房屋
//                    returnPayFeeDto.setReturnFeeId(returnPayFee.getReturnFeeId());
//                    List<ReturnPayFeeDto> returnPayFeeList = returnPayFeeInnerServiceSMOImpl.queryRoomReturnPayFees(returnPayFeeDto);
//                    if (returnPayFeeList == null || returnPayFeeList.size() < 1) {
//                        continue;
//                    }
//                    //returnPayFeeDtoList.add(returnPayFeeList.get(0));
//                    BeanConvertUtil.covertBean(returnPayFeeList.get(0), returnPayFee);
//                } else if (!StringUtil.isEmpty(payerObjType) && payerObjType.equals("6666")) { //车辆
//                    returnPayFeeDto.setReturnFeeId(returnPayFee.getReturnFeeId());
//                    List<ReturnPayFeeDto> returnPayFeeList = returnPayFeeInnerServiceSMOImpl.queryCarReturnPayFees(returnPayFeeDto);
//                    if (returnPayFeeList == null || returnPayFeeList.size() < 1) {
//                        continue;
//                    }
//                    BeanConvertUtil.covertBean(returnPayFeeList.get(0), returnPayFee);
//                    //returnPayFeeDtoList.add(returnPayFeeList.get(0));
//                } else {
//                    continue;
//                }
//            }
            returnPayFees = BeanConvertUtil.covertBeanList(returnPayFeeDtos, ApiReturnPayFeeDataVo.class);
        } else {
            returnPayFees = new ArrayList<>();
        }

        ApiReturnPayFeeVo apiReturnPayFeeVo = new ApiReturnPayFeeVo();

        apiReturnPayFeeVo.setTotal(count);
        apiReturnPayFeeVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiReturnPayFeeVo.setReturnPayFees(returnPayFees);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiReturnPayFeeVo), HttpStatus.OK);

        cmdDataFlowContext.setResponseEntity(responseEntity);
    }
}
