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
package com.java110.community.cmd.inspectionItemTitle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.annotation.Java110Transactional;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.AbstractServiceCmdListener;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.inspectionItemTitle.InspectionItemTitleDto;
import com.java110.intf.community.IInspectionItemTitleV1InnerServiceSMO;
import com.java110.intf.community.IInspectionItemTitleValueV1InnerServiceSMO;
import com.java110.po.inspectionItemTitle.InspectionItemTitlePo;
import com.java110.po.inspectionItemTitleValue.InspectionItemTitleValuePo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 类表述：更新
 * 服务编码：inspectionItemTitle.updateInspectionItemTitle
 * 请求路劲：/app/inspectionItemTitle.UpdateInspectionItemTitle
 * add by 吴学文 at 2022-03-15 11:07:38 mail: 928255095@qq.com
 * open source address: https://gitee.com/wuxw7/MicroCommunity
 * 官网：http://www.homecommunity.cn
 * 温馨提示：如果您对此文件进行修改 请不要删除原有作者及注释信息，请补充您的 修改的原因以及联系邮箱如下
 * // modify by 张三 at 2021-09-12 第10行在某种场景下存在某种bug 需要修复，注释10至20行 加入 20行至30行
 */
@Java110Cmd(serviceCode = "inspectionItemTitle.updateInspectionItemTitle")
public class UpdateInspectionItemTitleCmd extends AbstractServiceCmdListener {

    private static Logger logger = LoggerFactory.getLogger(UpdateInspectionItemTitleCmd.class);


    @Autowired
    private IInspectionItemTitleV1InnerServiceSMO inspectionItemTitleV1InnerServiceSMOImpl;

    @Autowired
    private IInspectionItemTitleValueV1InnerServiceSMO inspectionItemTitleValueV1InnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "titleId", "titleId不能为空");
        Assert.hasKeyAndValue(reqJson, "communityId", "communityId不能为空");
        JSONArray titleValues = null;
        if (!InspectionItemTitleDto.TITLE_TYPE_QUESTIONS.equals(reqJson.getString("titleType"))) {
            titleValues = reqJson.getJSONArray("titleValues");
            if (titleValues.size() < 1) {
                throw new IllegalArgumentException("未包含选项");
            }
        }
    }

    @Override
    @Java110Transactional
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {

        InspectionItemTitlePo inspectionItemTitlePo = BeanConvertUtil.covertBean(reqJson, InspectionItemTitlePo.class);
        int flag = inspectionItemTitleV1InnerServiceSMOImpl.updateInspectionItemTitle(inspectionItemTitlePo);

        if (flag < 1) {
            throw new CmdException("更新数据失败");
        }

        if (InspectionItemTitleDto.TITLE_TYPE_QUESTIONS.equals(inspectionItemTitlePo.getTitleType())) {
            cmdDataFlowContext.setResponseEntity(ResultVo.success());
            return;
        }
        InspectionItemTitleValuePo deleteInspectionItemTitleValuePo = new InspectionItemTitleValuePo();
        deleteInspectionItemTitleValuePo.setTitleId(inspectionItemTitlePo.getTitleId());
        flag = inspectionItemTitleValueV1InnerServiceSMOImpl.deleteInspectionItemTitleValue(deleteInspectionItemTitleValuePo);
        if (flag < 1) {
            throw new CmdException("更新数据失败");
        }

        JSONArray titleValues = reqJson.getJSONArray("titleValues");
        InspectionItemTitleValuePo reportInfoSettingTitleValuePo = null;
        for (int titleValueIndex = 0; titleValueIndex < titleValues.size(); titleValueIndex++) {
            reportInfoSettingTitleValuePo = new InspectionItemTitleValuePo();
            reportInfoSettingTitleValuePo.setItemValue(titleValues.getJSONObject(titleValueIndex).getString("itemValue"));
            reportInfoSettingTitleValuePo.setSeq(titleValues.getJSONObject(titleValueIndex).getString("seq"));
            reportInfoSettingTitleValuePo.setTitleId(inspectionItemTitlePo.getTitleId());
            reportInfoSettingTitleValuePo.setValueId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_valueId));
            reportInfoSettingTitleValuePo.setCommunityId(inspectionItemTitlePo.getCommunityId());
            inspectionItemTitleValueV1InnerServiceSMOImpl.saveInspectionItemTitleValue(reportInfoSettingTitleValuePo);
        }

        cmdDataFlowContext.setResponseEntity(ResultVo.success());
    }
}
