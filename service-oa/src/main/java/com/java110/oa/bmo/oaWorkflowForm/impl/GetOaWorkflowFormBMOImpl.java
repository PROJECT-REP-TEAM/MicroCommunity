package com.java110.oa.bmo.oaWorkflowForm.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Transactional;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.oaWorkflow.OaWorkflowDto;
import com.java110.dto.oaWorkflowData.OaWorkflowDataDto;
import com.java110.dto.oaWorkflowForm.OaWorkflowFormDto;
import com.java110.dto.user.UserDto;
import com.java110.dto.workflowDataFile.WorkflowDataFileDto;
import com.java110.entity.audit.AuditUser;
import com.java110.intf.common.IOaWorkflowUserInnerServiceSMO;
import com.java110.intf.oa.IOaWorkflowDataInnerServiceSMO;
import com.java110.intf.oa.IOaWorkflowFormInnerServiceSMO;
import com.java110.intf.oa.IOaWorkflowInnerServiceSMO;
import com.java110.intf.oa.IWorkflowDataFileV1InnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.oa.bmo.oaWorkflowForm.IGetOaWorkflowFormBMO;
import com.java110.po.workflowDataFile.WorkflowDataFilePo;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("getOaWorkflowFormBMOImpl")
public class GetOaWorkflowFormBMOImpl implements IGetOaWorkflowFormBMO {

    @Autowired
    private IOaWorkflowFormInnerServiceSMO oaWorkflowFormInnerServiceSMOImpl;

    @Autowired
    private IOaWorkflowInnerServiceSMO oaWorkflowInnerServiceSMOImpl;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Autowired
    private IOaWorkflowUserInnerServiceSMO oaWorkflowUserInnerServiceSMOImpl;

    @Autowired
    private IOaWorkflowDataInnerServiceSMO oaWorkflowDataInnerServiceSMOImpl;

    @Autowired
    private IWorkflowDataFileV1InnerServiceSMO workflowDataFileV1InnerServiceSMOImpl;


    /**
     * @param oaWorkflowFormDto
     * @return ?????????????????????????????????
     */
    public ResponseEntity<String> get(OaWorkflowFormDto oaWorkflowFormDto) {


        int count = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowFormsCount(oaWorkflowFormDto);

        List<OaWorkflowFormDto> oaWorkflowFormDtos = null;
        if (count > 0) {
            oaWorkflowFormDtos = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowForms(oaWorkflowFormDto);
        } else {
            oaWorkflowFormDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) oaWorkflowFormDto.getRow()), count, oaWorkflowFormDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    /**
     * {"schemaVersion":1,"exporter":{"name":"form-js","version":"0.1.0"},
     * "components":[{"text":"# Text","type":"text"},{"key":"textfield2","label":"?????????","type":"textfield"},{"key":"number2","label":"Number","type":"number"},
     * {"key":"checkbox2","label":"Checkbox","type":"checkbox"},
     * {"key":"radio2","label":"Radio","type":"radio","values":[{"label":"Value","value":"value"}]},
     * {"key":"select2","label":"Select","type":"select","values":[{"label":"Value","value":"value"}]},
     * {"text":"# Text","type":"text"},{"key":"textarea1","label":"???????????????","type":"textarea"},
     * {"key":"textdate1","label":"??????","type":"textdate"},
     * {"key":"textdatetime1","label":"??????","type":"textdatetime"},
     * {"action":"submit","key":"button1","label":"Button","type":"button"}],"type":"default"}
     *
     * @param paramIn
     * @return
     */
    @Override
    public ResponseEntity<String> queryOaWorkflowFormData(Map paramIn) {

        OaWorkflowFormDto oaWorkflowFormDto = new OaWorkflowFormDto();
        oaWorkflowFormDto.setFlowId(paramIn.get("flowId").toString());
        oaWorkflowFormDto.setStoreId(paramIn.get("storeId").toString());
        oaWorkflowFormDto.setRow(1);
        oaWorkflowFormDto.setPage(1);
        List<OaWorkflowFormDto> oaWorkflowFormDtos = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowForms(oaWorkflowFormDto);

        Assert.listOnlyOne(oaWorkflowFormDtos, "??????????????????????????????????????????");

        paramIn.put("tableName", oaWorkflowFormDtos.get(0).getTableName());

        int count = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowFormDataCount(paramIn);

        List<Map> datas = null;
        if (count > 0) {
            datas = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowFormDatas(paramIn);

        } else {
            datas = new ArrayList<>();
        }

        //??????file
        queryFilesFromData(datas);

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (int) paramIn.get("row")), count, datas);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    private void queryFilesFromData(List<Map> datas) {
        if (datas.size() != 1) {
            return;
        }

        WorkflowDataFileDto workflowDataFileDto = new WorkflowDataFileDto();
        workflowDataFileDto.setId(datas.get(0).get("id").toString());
        workflowDataFileDto.setStoreId(datas.get(0).get("store_id").toString());
        List<WorkflowDataFileDto> workflowDataFileDtos = workflowDataFileV1InnerServiceSMOImpl.queryWorkflowDataFiles(workflowDataFileDto);

        if (workflowDataFileDtos == null || workflowDataFileDtos.size() < 1) {
            return;
        }
        String imgUrl = MappingCache.getValue("IMG_PATH");
        for(WorkflowDataFileDto tmpWorkflowDataFileDto: workflowDataFileDtos){
            tmpWorkflowDataFileDto.setRealFileName(imgUrl + tmpWorkflowDataFileDto.getRealFileName());
        }

        datas.get(0).put("files",workflowDataFileDtos);
    }

    /**
     * ??????????????????
     *
     * @param reqJson
     * @return
     */
    @Override
    public ResponseEntity<String> saveOaWorkflowFormData(JSONObject reqJson) {
        OaWorkflowFormDto oaWorkflowFormDto = new OaWorkflowFormDto();
        oaWorkflowFormDto.setFlowId(reqJson.get("flowId").toString());
        oaWorkflowFormDto.setStoreId(reqJson.get("storeId").toString());
        oaWorkflowFormDto.setRow(1);
        oaWorkflowFormDto.setPage(1);
        List<OaWorkflowFormDto> oaWorkflowFormDtos = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowForms(oaWorkflowFormDto);
        Assert.listOnlyOne(oaWorkflowFormDtos, "??????????????????????????????????????????");

        //
        OaWorkflowDto oaWorkflowDto = new OaWorkflowDto();
        oaWorkflowDto.setStoreId(reqJson.getString("storeId"));
        oaWorkflowDto.setFlowId(reqJson.getString("flowId"));
        List<OaWorkflowDto> oaWorkflowDtos = oaWorkflowInnerServiceSMOImpl.queryOaWorkflows(oaWorkflowDto);
        Assert.listOnlyOne(oaWorkflowDtos, "???????????????");

        if (!OaWorkflowDto.STATE_COMPLAINT.equals(oaWorkflowDtos.get(0).getState())) {
            throw new IllegalArgumentException(oaWorkflowDtos.get(0).getFlowName() + "???????????????");
        }

        if (StringUtil.isEmpty(oaWorkflowDtos.get(0).getProcessDefinitionKey())) {
            throw new IllegalArgumentException(oaWorkflowDtos.get(0).getFlowName() + "???????????????");
        }

        //??????????????????
        UserDto userDto = new UserDto();
        userDto.setUserId(reqJson.getString("userId"));
        List<UserDto> userDtos = userInnerServiceSMOImpl.getUsers(userDto);

        Assert.listOnlyOne(userDtos, "???????????????");

        //??????????????????
        reqJson.put("id", GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_flowId));
        reqJson.put("state", "1001");
        reqJson.put("createUserId", reqJson.getString("userId"));
        reqJson.put("createUserName", userDtos.get(0).getUserName());
        reqJson.put("tableName", oaWorkflowFormDtos.get(0).getTableName());

        int flag = oaWorkflowFormInnerServiceSMOImpl.saveOaWorkflowFormData(reqJson);
        if (flag < 1) {
            throw new IllegalArgumentException("????????????");
        }

        //?????????????????????
        saveOaWorkflowFile(reqJson);

        reqJson.put("processDefinitionKey", oaWorkflowDtos.get(0).getProcessDefinitionKey());
        oaWorkflowUserInnerServiceSMOImpl.startProcess(reqJson);

        return ResultVo.success();
    }

    private void saveOaWorkflowFile(JSONObject reqJson) {
        if (!reqJson.containsKey("fileName")) {
            return;
        }

        String fileName = reqJson.getString("fileName");
        if (StringUtil.isEmpty(fileName)) {
            return;
        }

        WorkflowDataFilePo workflowDataFilePo = new WorkflowDataFilePo();
        workflowDataFilePo.setCreateUserId(reqJson.getString("userId"));
        workflowDataFilePo.setCreateUserName(reqJson.getString("createUserName"));
        workflowDataFilePo.setFileId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_file_id));
        workflowDataFilePo.setFileName(reqJson.getString("fileName"));
        workflowDataFilePo.setId(reqJson.getString("id"));
        workflowDataFilePo.setRealFileName(reqJson.getString("realFileName"));
        workflowDataFilePo.setStoreId(reqJson.getString("storeId"));
        int flag = workflowDataFileV1InnerServiceSMOImpl.saveWorkflowDataFile(workflowDataFilePo);
        if (flag < 1) {
            throw new CmdException("??????????????????");
        }
    }

    /**
     * ?????????????????????
     *
     * @param paramIn
     * @return
     */
    @Override
    public ResponseEntity<String> queryOaWorkflowUserTaskFormData(JSONObject paramIn) {

        OaWorkflowDto oaWorkflowDto = new OaWorkflowDto();
        oaWorkflowDto.setStoreId(paramIn.getString("storeId"));
        oaWorkflowDto.setFlowId(paramIn.getString("flowId"));
        List<OaWorkflowDto> oaWorkflowDtos = oaWorkflowInnerServiceSMOImpl.queryOaWorkflows(oaWorkflowDto);
        Assert.listOnlyOne(oaWorkflowDtos, "???????????????");

        OaWorkflowFormDto oaWorkflowFormDto = new OaWorkflowFormDto();
        oaWorkflowFormDto.setFlowId(paramIn.get("flowId").toString());
        oaWorkflowFormDto.setStoreId(paramIn.get("storeId").toString());
        oaWorkflowFormDto.setRow(1);
        oaWorkflowFormDto.setPage(1);
        List<OaWorkflowFormDto> oaWorkflowFormDtos = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowForms(oaWorkflowFormDto);
        Assert.listOnlyOne(oaWorkflowFormDtos, "??????????????????????????????????????????");

        AuditUser auditUser = new AuditUser();
        auditUser.setProcessDefinitionKey(oaWorkflowDtos.get(0).getProcessDefinitionKey());
        auditUser.setFlowId(paramIn.getString("flowId"));
        auditUser.setUserId(paramIn.getString("userId"));
        auditUser.setStoreId(paramIn.getString("storeId"));
        auditUser.setPage(paramIn.getInteger("page"));
        auditUser.setRow(paramIn.getInteger("row"));

        long count = oaWorkflowUserInnerServiceSMOImpl.getUserTaskCount(auditUser);

        List<JSONObject> datas = null;

        if (count > 0) {
            datas = oaWorkflowUserInnerServiceSMOImpl.getUserTasks(auditUser);
            //?????? ????????????
            freshFormData(datas, paramIn, oaWorkflowFormDtos.get(0));
        } else {
            datas = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) paramIn.getInteger("row")), count, datas);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);
        return responseEntity;
    }

    /**
     * ?????????????????????
     *
     * @param paramIn
     * @return
     */
    @Override
    public ResponseEntity<String> queryOaWorkflowUserHisTaskFormData(JSONObject paramIn) {

        OaWorkflowDto oaWorkflowDto = new OaWorkflowDto();
        oaWorkflowDto.setStoreId(paramIn.getString("storeId"));
        oaWorkflowDto.setFlowId(paramIn.getString("flowId"));
        List<OaWorkflowDto> oaWorkflowDtos = oaWorkflowInnerServiceSMOImpl.queryOaWorkflows(oaWorkflowDto);
        Assert.listOnlyOne(oaWorkflowDtos, "???????????????");

        OaWorkflowFormDto oaWorkflowFormDto = new OaWorkflowFormDto();
        oaWorkflowFormDto.setFlowId(paramIn.get("flowId").toString());
        oaWorkflowFormDto.setStoreId(paramIn.get("storeId").toString());
        oaWorkflowFormDto.setRow(1);
        oaWorkflowFormDto.setPage(1);
        List<OaWorkflowFormDto> oaWorkflowFormDtos = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowForms(oaWorkflowFormDto);
        Assert.listOnlyOne(oaWorkflowFormDtos, "??????????????????????????????????????????");

        OaWorkflowDataDto oaWorkflowDataDto = new OaWorkflowDataDto();
        oaWorkflowDataDto.setFlowId(paramIn.getString("flowId"));
        oaWorkflowDataDto.setStaffId(paramIn.getString("userId"));
        oaWorkflowDataDto.setStoreId(paramIn.getString("storeId"));
        oaWorkflowDataDto.setPage(paramIn.getInteger("page"));
        oaWorkflowDataDto.setRow(paramIn.getInteger("row"));
        oaWorkflowDataDto.setHis("Y");

        long count = oaWorkflowDataInnerServiceSMOImpl.queryOaWorkflowDatasCount(oaWorkflowDataDto);

        List<JSONObject> datas = new ArrayList<>();
        if (count > 0) {
            List<OaWorkflowDataDto> oaWorkflowDataDtos = oaWorkflowDataInnerServiceSMOImpl.queryOaWorkflowDatas(oaWorkflowDataDto);
            for (OaWorkflowDataDto oaWorkflowDataDto1 : oaWorkflowDataDtos) {
                datas.add(BeanConvertUtil.beanCovertJson(oaWorkflowDataDto1));
            }
            //?????? ????????????
            freshFormData(datas, paramIn, oaWorkflowFormDtos.get(0));
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) paramIn.getInteger("row")), count, datas);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);
        return responseEntity;
    }

    @Override
    @Java110Transactional
    public ResponseEntity<String> auditOaWorkflow(JSONObject reqJson) {
        //????????????????????????
        OaWorkflowDto oaWorkflowDto = new OaWorkflowDto();
        oaWorkflowDto.setStoreId(reqJson.getString("storeId"));
        oaWorkflowDto.setFlowId(reqJson.getString("flowId"));
        List<OaWorkflowDto> oaWorkflowDtos = oaWorkflowInnerServiceSMOImpl.queryOaWorkflows(oaWorkflowDto);
        Assert.listOnlyOne(oaWorkflowDtos, "???????????????");

        //????????????????????????
        OaWorkflowFormDto oaWorkflowFormDto = new OaWorkflowFormDto();
        oaWorkflowFormDto.setFlowId(reqJson.get("flowId").toString());
        oaWorkflowFormDto.setStoreId(reqJson.get("storeId").toString());
        oaWorkflowFormDto.setRow(1);
        oaWorkflowFormDto.setPage(1);
        List<OaWorkflowFormDto> oaWorkflowFormDtos = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowForms(oaWorkflowFormDto);
        Assert.listOnlyOne(oaWorkflowFormDtos, "??????????????????????????????????????????");

        //????????????????????????
        Map paramMap = new HashMap();
        paramMap.put("storeId", reqJson.getString("storeId"));
        paramMap.put("id", reqJson.getString("id"));
        paramMap.put("tableName", oaWorkflowFormDtos.get(0).getTableName());
        paramMap.put("page", 1);
        paramMap.put("row", 1);
        List<Map> formDatas = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowFormDatas(paramMap);

        Assert.listOnlyOne(formDatas, "?????????????????????");

        reqJson.put("startUserId", formDatas.get(0).get("create_user_id"));

        //????????????
        if ("1100".equals(reqJson.getString("auditCode"))
                || "1500".equals(reqJson.getString("auditCode"))) { //????????????
            reqJson.put("nextUserId", reqJson.getString("staffId"));
            boolean isLastTask = oaWorkflowUserInnerServiceSMOImpl.completeTask(reqJson);
            if (isLastTask) {
                reqJson.put("state", "1005"); //????????????
            } else {
                reqJson.put("state", "1002"); //????????????
            }
            reqJson.put("tableName", oaWorkflowFormDtos.get(0).getTableName());
            oaWorkflowFormInnerServiceSMOImpl.updateOaWorkflowFormData(reqJson);
            //?????????????????? ?????????????????????
        } else if ("1300".equals(reqJson.getString("auditCode"))) { //????????????
            reqJson.put("nextUserId", reqJson.getString("staffId"));
            oaWorkflowUserInnerServiceSMOImpl.changeTaskToOtherUser(reqJson);
            reqJson.put("state", "1004"); //????????????
            reqJson.put("tableName", oaWorkflowFormDtos.get(0).getTableName());
            oaWorkflowFormInnerServiceSMOImpl.updateOaWorkflowFormData(reqJson);
        } else if ("1200".equals(reqJson.getString("auditCode"))
                || "1400".equals(reqJson.getString("auditCode"))
        ) { //????????????
            oaWorkflowUserInnerServiceSMOImpl.goBackTask(reqJson);
            reqJson.put("state", "1003"); //????????????
            reqJson.put("tableName", oaWorkflowFormDtos.get(0).getTableName());
            oaWorkflowFormInnerServiceSMOImpl.updateOaWorkflowFormData(reqJson);
        } else {
            throw new IllegalArgumentException("??????????????????");
        }





        return ResultVo.success();
    }

    @Override
    public ResponseEntity<String> getNextTask(JSONObject reqJson) {
//????????????????????????
        OaWorkflowFormDto oaWorkflowFormDto = new OaWorkflowFormDto();
        oaWorkflowFormDto.setFlowId(reqJson.get("flowId").toString());
        oaWorkflowFormDto.setStoreId(reqJson.get("storeId").toString());
        oaWorkflowFormDto.setRow(1);
        oaWorkflowFormDto.setPage(1);
        List<OaWorkflowFormDto> oaWorkflowFormDtos = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowForms(oaWorkflowFormDto);
        Assert.listOnlyOne(oaWorkflowFormDtos, "??????????????????????????????????????????");

        Map paramMap = new HashMap();
        paramMap.put("storeId", reqJson.getString("storeId"));
        paramMap.put("id", reqJson.getString("id"));
        paramMap.put("tableName", oaWorkflowFormDtos.get(0).getTableName());
        paramMap.put("page", 1);
        paramMap.put("row", 1);
        List<Map> formDatas = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowFormDatas(paramMap);
        Assert.listOnlyOne(formDatas, "?????????????????????");
        reqJson.put("startUserId", formDatas.get(0).get("create_user_id"));
        List<JSONObject> tasks = oaWorkflowUserInnerServiceSMOImpl.nextAllNodeTaskList(reqJson);
        return ResultVo.createResponseEntity(tasks);
    }

    @Override
    public ResponseEntity<String> queryOaWorkflowUser(JSONObject paramIn) {
        OaWorkflowDto oaWorkflowDto = new OaWorkflowDto();
        oaWorkflowDto.setStoreId(paramIn.getString("storeId"));
        oaWorkflowDto.setFlowId(paramIn.getString("flowId"));
        List<OaWorkflowDto> oaWorkflowDtos = oaWorkflowInnerServiceSMOImpl.queryOaWorkflows(oaWorkflowDto);
        Assert.listOnlyOne(oaWorkflowDtos, "???????????????");

        OaWorkflowFormDto oaWorkflowFormDto = new OaWorkflowFormDto();
        oaWorkflowFormDto.setFlowId(paramIn.get("flowId").toString());
        oaWorkflowFormDto.setStoreId(paramIn.get("storeId").toString());
        oaWorkflowFormDto.setRow(1);
        oaWorkflowFormDto.setPage(1);
        List<OaWorkflowFormDto> oaWorkflowFormDtos = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowForms(oaWorkflowFormDto);
        Assert.listOnlyOne(oaWorkflowFormDtos, "??????????????????????????????????????????");

        OaWorkflowDataDto oaWorkflowDataDto = new OaWorkflowDataDto();
        oaWorkflowDataDto.setFlowId(paramIn.getString("flowId"));
        oaWorkflowDataDto.setBusinessKey(paramIn.getString("id"));
        oaWorkflowDataDto.setStoreId(paramIn.getString("storeId"));
        oaWorkflowDataDto.setPage(paramIn.getInteger("page"));
        oaWorkflowDataDto.setRow(paramIn.getInteger("row"));

        long count = oaWorkflowDataInnerServiceSMOImpl.queryOaWorkflowDatasCount(oaWorkflowDataDto);

        List<JSONObject> datas = new ArrayList<>();

        if (count > 0) {
            List<OaWorkflowDataDto> oaWorkflowDataDtos = oaWorkflowDataInnerServiceSMOImpl.queryOaWorkflowDatas(oaWorkflowDataDto);
            for (OaWorkflowDataDto oaWorkflowDataDto1 : oaWorkflowDataDtos) {
                datas.add(BeanConvertUtil.beanCovertJson(oaWorkflowDataDto1));
            }
            //?????? ????????????
            freshFormData(datas, paramIn, oaWorkflowFormDtos.get(0));
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) paramIn.getInteger("row")), count, datas);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);
        return responseEntity;
    }

    /**
     * ??????????????????
     *
     * @param datas
     */
    private void freshFormData(List<JSONObject> datas, JSONObject paramIn, OaWorkflowFormDto oaWorkflowFormDto) {
        List<String> ids = new ArrayList<>();
        List<String> userIds = new ArrayList<>();
        for (JSONObject data : datas) {
            ids.add(data.getString("id"));
            if (!StringUtil.isEmpty(data.getString("staffId"))) {
                userIds.add(data.getString("staffId"));
            }
        }
        if (ids.size() < 1) {
            return;
        }

        Map paramMap = new HashMap();
        paramMap.put("storeId", paramIn.getString("storeId"));
        paramMap.put("ids", ids.toArray(new String[ids.size()]));
        paramMap.put("tableName", oaWorkflowFormDto.getTableName());
        paramMap.put("page", 1);
        paramMap.put("row", ids.size());
        List<Map> formDatas = oaWorkflowFormInnerServiceSMOImpl.queryOaWorkflowFormDatas(paramMap);
        long duration = 0L;
        for (JSONObject data : datas) {
            for (Map form : formDatas) {
                if (data.getString("id").equals(form.get("id"))) {
                    data.putAll(form);
                }
            }

            if (data.containsKey("startTime") && data.containsKey("endTime")) {
                try {
                    if (data.getString("endTime") == null) {
                        duration = DateUtil.getCurrentDate().getTime() - DateUtil.getDateFromString(data.getString("startTime"), DateUtil.DATE_FORMATE_STRING_A).getTime();
                    } else {
                        duration = DateUtil.getDateFromString(data.getString("endTime"), DateUtil.DATE_FORMATE_STRING_A).getTime()
                                - DateUtil.getDateFromString(data.getString("startTime"), DateUtil.DATE_FORMATE_STRING_A).getTime();
                    }
                } catch (Exception e) {
                    duration = 0;
                }
                data.put("duration", getCostTime(duration));
            }
        }

        if (userIds.size() < 1) {
            return;
        }

        //??????????????????
        UserDto userDto = new UserDto();
        userDto.setUserIds(userIds.toArray(new String[userIds.size()]));
        List<UserDto> userDtos = userInnerServiceSMOImpl.getStaffs(userDto);

        for (JSONObject data : datas) {
            for (UserDto userDto1 : userDtos) {
                if (data.getString("staffId").equals(userDto1.getUserId())) {
                    data.put("orgName", userDto1.getOrgName());
                    data.put("staffName", userDto1.getUserName());
                }
            }
        }
    }

    public String getCostTime(Long time) {
        if (time == null) {
            return "00:00";
        }
        long hours = time / (1000 * 60 * 60);
        long minutes = (time - hours * (1000 * 60 * 60)) / (1000 * 60);
        String diffTime = "";
        if (minutes < 10) {
            diffTime = hours + ":0" + minutes;
        } else {
            diffTime = hours + ":" + minutes;
        }
        return diffTime;
    }

}
