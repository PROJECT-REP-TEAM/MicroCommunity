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
package com.java110.common.bmo.machine.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.common.bmo.machine.IMachineOpenDoorBMO;
import com.java110.intf.job.IDataBusInnerServiceSMO;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * 设备开门功能
 *
 * @desc add by 吴学文 14:15
 */
@Service
public class MachineOpenDoorBMOImpl implements IMachineOpenDoorBMO {


    @Autowired
    IDataBusInnerServiceSMO dataBusInnerServiceSMOImpl;

    /**
     * 开门功能
     *
     * @param reqJson 请求报文信息
     * @return
     */
    @Override
    public ResponseEntity<String> openDoor(JSONObject reqJson) {
        ResultVo resultVo = dataBusInnerServiceSMOImpl.openDoor(reqJson);
        return ResultVo.createResponseEntity(resultVo);
    }

    /**
     * 重启设备
     *
     * @param reqJson 请求报文信息
     * @return
     */
    @Override
    public ResponseEntity<String> restartMachine(JSONObject reqJson) {
        ResultVo resultVo = dataBusInnerServiceSMOImpl.restartMachine(reqJson);
        return ResultVo.createResponseEntity(resultVo);
    }

    @Override
    public ResponseEntity<String> resendIot(JSONObject reqJson) {
        ResultVo resultVo = dataBusInnerServiceSMOImpl.resendIot(reqJson);
        return ResultVo.createResponseEntity(resultVo);
    }
}
