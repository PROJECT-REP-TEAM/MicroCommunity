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
package com.java110.community.cmd.propertyRightRegistration;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.annotation.Java110Transactional;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.AbstractServiceCmdListener;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.propertyRightRegistrationDetail.PropertyRightRegistrationDetailDto;
import com.java110.intf.community.IPropertyRightRegistrationDetailV1InnerServiceSMO;
import com.java110.intf.community.IPropertyRightRegistrationV1InnerServiceSMO;
import com.java110.po.propertyRightRegistration.PropertyRightRegistrationPo;
import com.java110.po.propertyRightRegistrationDetail.PropertyRightRegistrationDetailPo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;

import java.util.List;

/**
 * 类表述：删除
 * 服务编码：propertyRightRegistration.deletePropertyRightRegistration
 * 请求路劲：/app/propertyRightRegistration.DeletePropertyRightRegistration
 * add by 吴学文 at 2021-10-09 10:34:14 mail: 928255095@qq.com
 * open source address: https://gitee.com/wuxw7/MicroCommunity
 * 官网：http://www.homecommunity.cn
 * 温馨提示：如果您对此文件进行修改 请不要删除原有作者及注释信息，请补充您的 修改的原因以及联系邮箱如下
 * // modify by 张三 at 2021-09-12 第10行在某种场景下存在某种bug 需要修复，注释10至20行 加入 20行至30行
 */
@Java110Cmd(serviceCode = "propertyRightRegistration.deletePropertyRightRegistration")
public class DeletePropertyRightRegistrationCmd extends AbstractServiceCmdListener {
    private static Logger logger = LoggerFactory.getLogger(DeletePropertyRightRegistrationCmd.class);

    @Autowired
    private IPropertyRightRegistrationV1InnerServiceSMO propertyRightRegistrationV1InnerServiceSMOImpl;

    @Autowired
    private IPropertyRightRegistrationDetailV1InnerServiceSMO propertyRightRegistrationDetailV1InnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "prrId", "prrId不能为空");
        Assert.hasKeyAndValue(reqJson, "communityId", "communityId不能为空");

    }

    @Override
    @Java110Transactional
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {

        PropertyRightRegistrationPo propertyRightRegistrationPo = BeanConvertUtil.covertBean(reqJson, PropertyRightRegistrationPo.class);
        //删除产权登记表
        int flag = propertyRightRegistrationV1InnerServiceSMOImpl.deletePropertyRightRegistration(propertyRightRegistrationPo);

        if (flag < 1) {
            throw new CmdException("删除数据失败");
        }
        PropertyRightRegistrationDetailDto propertyRightRegistrationDetailDto = new PropertyRightRegistrationDetailDto();
        propertyRightRegistrationDetailDto.setPrrId(propertyRightRegistrationPo.getPrrId());
        //查询产权登记详情
        List<PropertyRightRegistrationDetailDto> propertyRightRegistrationDetailDtos = propertyRightRegistrationDetailV1InnerServiceSMOImpl.queryPropertyRightRegistrationDetails(propertyRightRegistrationDetailDto);
        if (propertyRightRegistrationDetailDtos != null && propertyRightRegistrationDetailDtos.size() > 0) {
            for (PropertyRightRegistrationDetailDto propertyRightRegistrationDetail : propertyRightRegistrationDetailDtos) {
                PropertyRightRegistrationDetailPo propertyRightRegistrationDetailPo = new PropertyRightRegistrationDetailPo();
                propertyRightRegistrationDetailPo.setPrrdId(propertyRightRegistrationDetail.getPrrdId());
                //删除产权登记详情
                propertyRightRegistrationDetailV1InnerServiceSMOImpl.deletePropertyRightRegistrationDetail(propertyRightRegistrationDetailPo);
            }
        }
        cmdDataFlowContext.setResponseEntity(ResultVo.success());
    }
}
