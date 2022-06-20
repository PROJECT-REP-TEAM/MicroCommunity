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
package com.java110.community.cmd.parkingBox;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.annotation.Java110Transactional;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.parking.ParkingAreaDto;
import com.java110.dto.parkingBoxArea.ParkingBoxAreaDto;
import com.java110.intf.community.IParkingAreaInnerServiceSMO;
import com.java110.intf.community.IParkingBoxAreaV1InnerServiceSMO;
import com.java110.intf.community.IParkingBoxV1InnerServiceSMO;
import com.java110.po.parkingBox.ParkingBoxPo;
import com.java110.po.parkingBoxArea.ParkingBoxAreaPo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


/**
 * 类表述：更新
 * 服务编码：parkingBox.updateParkingBox
 * 请求路劲：/app/parkingBox.UpdateParkingBox
 * add by 吴学文 at 2021-10-18 00:10:40 mail: 928255095@qq.com
 * open source address: https://gitee.com/wuxw7/MicroCommunity
 * 官网：http://www.homecommunity.cn
 * 温馨提示：如果您对此文件进行修改 请不要删除原有作者及注释信息，请补充您的 修改的原因以及联系邮箱如下
 * // modify by 张三 at 2021-09-12 第10行在某种场景下存在某种bug 需要修复，注释10至20行 加入 20行至30行
 */
@Java110Cmd(serviceCode = "parkingBox.updateParkingBox")
public class UpdateParkingBoxCmd extends Cmd {

    private static Logger logger = LoggerFactory.getLogger(UpdateParkingBoxCmd.class);
    public static final String CODE_PREFIX_ID = "10";


    @Autowired
    private IParkingBoxV1InnerServiceSMO parkingBoxV1InnerServiceSMOImpl;

    @Autowired
    private IParkingAreaInnerServiceSMO parkingAreaInnerServiceSMOImpl;

    @Autowired
    private IParkingBoxAreaV1InnerServiceSMO parkingBoxAreaV1InnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "boxId", "boxId不能为空");
        Assert.hasKeyAndValue(reqJson, "communityId", "communityId不能为空");

    }

    @Override
    @Java110Transactional
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {

        ParkingBoxPo parkingBoxPo = BeanConvertUtil.covertBean(reqJson, ParkingBoxPo.class);
        int flag = parkingBoxV1InnerServiceSMOImpl.updateParkingBox(parkingBoxPo);

        if (flag < 1) {
            throw new CmdException("更新数据失败");
        }

        if (!reqJson.containsKey("paId") || StringUtil.isEmpty(reqJson.getString("paId"))) {
            cmdDataFlowContext.setResponseEntity(ResultVo.success());
            return ;
        }

        //判断停车场是否存在
        ParkingAreaDto parkingAreaDto = new ParkingAreaDto();
        parkingAreaDto.setPaId(reqJson.getString("paId"));
        parkingAreaDto.setCommunityId(reqJson.getString("communityId"));
        List<ParkingAreaDto> parkingAreaDtos = parkingAreaInnerServiceSMOImpl.queryParkingAreas(parkingAreaDto);

        Assert.listOnlyOne(parkingAreaDtos, "停车场不存在");

        ParkingBoxAreaDto parkingBoxAreaDto = new ParkingBoxAreaDto();
        parkingBoxAreaDto.setBoxId(reqJson.getString("boxId"));
        parkingBoxAreaDto.setPaId(parkingAreaDtos.get(0).getPaId());
        parkingBoxAreaDto.setCommunityId(parkingAreaDtos.get(0).getCommunityId());
        List<ParkingBoxAreaDto> parkingBoxAreaDtos = parkingBoxAreaV1InnerServiceSMOImpl.queryParkingBoxAreas(parkingBoxAreaDto);
        ParkingBoxAreaPo parkingBoxAreaPo = new ParkingBoxAreaPo();
        parkingBoxAreaPo.setBoxId(parkingBoxPo.getBoxId());
        parkingBoxAreaPo.setPaId(parkingAreaDtos.get(0).getPaId());
        parkingBoxAreaPo.setCommunityId(parkingAreaDtos.get(0).getCommunityId());
        if(parkingBoxAreaDtos == null || parkingBoxAreaDtos.size() < 1) {
            parkingBoxAreaPo.setBaId(GenerateCodeFactory.getGeneratorId(CODE_PREFIX_ID));
            parkingBoxAreaPo.setDefaultArea(ParkingBoxAreaDto.DEFAULT_AREA_TRUE);
            flag = parkingBoxAreaV1InnerServiceSMOImpl.saveParkingBoxArea(parkingBoxAreaPo);
        }else{
            parkingBoxAreaPo.setBaId(parkingBoxAreaDtos.get(0).getBaId());
            flag = parkingBoxAreaV1InnerServiceSMOImpl.updateParkingBoxArea(parkingBoxAreaPo);
        }
        if (flag < 1) {
            throw new CmdException("保存数据失败");
        }

        cmdDataFlowContext.setResponseEntity(ResultVo.success());
    }
}
