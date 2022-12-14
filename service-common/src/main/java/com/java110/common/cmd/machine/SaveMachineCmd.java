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
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.annotation.Java110Transactional;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.intf.common.IMachineV1InnerServiceSMO;
import com.java110.po.machine.MachinePo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;

/**
 * 类表述：保存
 * 服务编码：machine.saveMachine
 * 请求路劲：/app/machine.SaveMachine
 * add by 吴学文 at 2021-11-09 15:42:41 mail: 928255095@qq.com
 * open source address: https://gitee.com/wuxw7/MicroCommunity
 * 官网：http://www.homecommunity.cn
 * 温馨提示：如果您对此文件进行修改 请不要删除原有作者及注释信息，请补充您的 修改的原因以及联系邮箱如下
 * // modify by 张三 at 2021-09-12 第10行在某种场景下存在某种bug 需要修复，注释10至20行 加入 20行至30行
 */
@Java110Cmd(serviceCode = "machine.saveMachine")
public class SaveMachineCmd extends Cmd {

    private static Logger logger = LoggerFactory.getLogger( SaveMachineCmd.class );

    public static final String CODE_PREFIX_ID = "10";

    @Autowired
    private IMachineV1InnerServiceSMO machineV1InnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        Assert.hasKeyAndValue( reqJson, "machineCode", "请求报文中未包含machineCode" );
        Assert.hasKeyAndValue( reqJson, "machineVersion", "请求报文中未包含machineVersion" );
        Assert.hasKeyAndValue( reqJson, "machineTypeCd", "请求报文中未包含machineTypeCd" );
        Assert.hasKeyAndValue( reqJson, "communityId", "请求报文中未包含communityId" );
        Assert.hasKeyAndValue( reqJson, "machineName", "请求报文中未包含machineName" );
        Assert.hasKeyAndValue( reqJson, "authCode", "请求报文中未包含authCode" );
        Assert.hasKeyAndValue( reqJson, "direction", "请求报文中未包含direction" );
        Assert.hasKeyAndValue( reqJson, "typeId", "请求报文中未包含typeId" );

    }

    @Override
    @Java110Transactional
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {

        MachinePo machinePo = BeanConvertUtil.covertBean( reqJson, MachinePo.class );
        machinePo.setMachineId( GenerateCodeFactory.getGeneratorId( CODE_PREFIX_ID ) );
        int flag = machineV1InnerServiceSMOImpl.saveMachine( machinePo );

        if (flag < 1) {
            throw new CmdException( "保存数据失败" );
        }

        cmdDataFlowContext.setResponseEntity( ResultVo.success() );
    }
}
