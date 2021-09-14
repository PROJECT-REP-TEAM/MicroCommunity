package com.java110.oa.cmd.oaWorkflow;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.annotation.Java110Transactional;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.AbstractServiceCmdListener;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.oaWorkflow.OaWorkflowDto;
import com.java110.dto.oaWorkflowForm.OaWorkflowFormDto;
import com.java110.intf.oa.IOaWorkflowFormInnerServiceSMO;
import com.java110.intf.oa.IOaWorkflowInnerServiceSMO;
import com.java110.oa.bmo.oaWorkflowForm.IGetOaWorkflowFormBMO;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 修改表单数据
 */
@Java110Cmd(serviceCode = "oaWorkflow.updateOaWorkflowFormData")
public class UpdateOaWorkflowFormData extends AbstractServiceCmdListener {

    @Autowired
    private IOaWorkflowFormInnerServiceSMO oaWorkflowFormInnerServiceSMOImpl;

    @Autowired
    private IOaWorkflowInnerServiceSMO oaWorkflowInnerServiceSMOImpl;

    @Autowired
    private IGetOaWorkflowFormBMO getOaWorkflowFormBMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "id", "ID不能为空");
        Assert.hasKeyAndValue(reqJson, "flowId", "流程不能为空");
    }

    @Override
    @Java110Transactional
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {
        Map<String,String> headers = cmdDataFlowContext.getReqHeaders();

        reqJson.put("storeId",headers.get("store-id"));

        OaWorkflowFormDto oaWorkflowFormDto = new OaWorkflowFormDto();
        oaWorkflowFormDto.setFlowId(reqJson.get("flowId").toString());
        oaWorkflowFormDto.setStoreId(reqJson.get("storeId").toString());
        oaWorkflowFormDto.setRow(1);
        oaWorkflowFormDto.setPage(1);
        List<OaWorkflowFormDto> oaWorkflowFormDtos = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowForms(oaWorkflowFormDto);
        Assert.listOnlyOne(oaWorkflowFormDtos, "未包含流程表单，请先设置表单");

        //
        OaWorkflowDto oaWorkflowDto = new OaWorkflowDto();
        oaWorkflowDto.setStoreId(reqJson.getString("storeId"));
        oaWorkflowDto.setFlowId(reqJson.getString("flowId"));
        List<OaWorkflowDto> oaWorkflowDtos = oaWorkflowInnerServiceSMOImpl.queryOaWorkflows(oaWorkflowDto);
        Assert.listOnlyOne(oaWorkflowDtos, "流程不存在");

        if (!OaWorkflowDto.STATE_COMPLAINT.equals(oaWorkflowDtos.get(0).getState())) {
            throw new IllegalArgumentException(oaWorkflowDtos.get(0).getFlowName() + "流程未部署");
        }

        if (StringUtil.isEmpty(oaWorkflowDtos.get(0).getProcessDefinitionKey())) {
            throw new IllegalArgumentException(oaWorkflowDtos.get(0).getFlowName() + "流程未部署");
        }

        List<String> columns = new ArrayList<>();
        for (String key : reqJson.keySet()) {
            if ("flowId".equals(key) || "id".equals(key) || "storeId".equals(key)) {
                continue;
            }
            columns.add(key + "='" + reqJson.getString(key)+"'");

            //简单校验
            validateColumns(columns);
        }
        reqJson.put("columns", columns.toArray(new String[columns.size()]));

        //保存表单数据
        reqJson.put("tableName", oaWorkflowFormDtos.get(0).getTableName());

        int flag = oaWorkflowFormInnerServiceSMOImpl.updateOaWorkflowFormDataAll(reqJson);
        if (flag < 1) {
            throw new IllegalArgumentException("保存失败");
        }


        cmdDataFlowContext.setResponseEntity(ResultVo.success());

    }

    private void validateColumns(List<String> columns) {
        String columnBak = "";
        for (String column : columns) {
            columnBak = column.toLowerCase();
            if (containsSqlInjection(columnBak)) {
                throw new IllegalArgumentException("非法操作，可能破坏系统稳定性");
            }
        }
    }

    public static boolean containsSqlInjection(Object obj) {
        Pattern pattern = Pattern.compile("\\b(exec|insert|select|drop|grant|alter|delete|update|count|chr|mid|master|truncate|char|declare)\\b|(\\*|;|\\+|'|%)");
        Matcher matcher = pattern.matcher(obj.toString().toLowerCase());
        return matcher.find();
    }
}
