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
package com.java110.common.cmd.machine;

import com.alibaba.fastjson.JSONObject;
import com.java110.common.bmo.machine.IMachineOpenDoorBMO;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

/**
 * 类表述：保存
 * 服务编码：machine.customCarInOutCmd
 * 请求路劲：/app/machine.customCarInOutCmd
 * add by 吴学文 at 2021-09-18 13:35:13 mail: 928255095@qq.com
 * open source address: https://gitee.com/wuxw7/MicroCommunity
 * 官网：http://www.homecommunity.cn
 * 温馨提示：如果您对此文件进行修改 请不要删除原有作者及注释信息，请补充您的 修改的原因以及联系邮箱如下
 * // modify by 张三 at 2021-09-12 第10行在某种场景下存在某种bug 需要修复，注释10至20行 加入 20行至30行
 */
@Java110Cmd(serviceCode = "machine.customCarInOutCmd")
public class CustomCarInOutCmd extends Cmd {

    private static Logger logger = LoggerFactory.getLogger(CustomCarInOutCmd.class);

    public static final String CODE_PREFIX_ID = "10";

    @Autowired
    private IMachineOpenDoorBMO machineOpenDoorBMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "communityId", "请求报文中未包含小区信息");
        Assert.hasKeyAndValue(reqJson, "machineId", "请求报文中未包含设备信息");
        Assert.hasKeyAndValue(reqJson, "carNum", "请求报文中未包含车牌号");
        Assert.hasKeyAndValue(reqJson, "type", "请求报文中未包含类型");

    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {

        ResponseEntity<String> responseEntity = machineOpenDoorBMOImpl.customCarInOut(reqJson);
        cmdDataFlowContext.setResponseEntity(responseEntity);
    }
}
