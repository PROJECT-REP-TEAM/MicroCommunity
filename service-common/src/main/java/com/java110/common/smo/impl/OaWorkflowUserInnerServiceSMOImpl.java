package com.java110.common.smo.impl;


import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Transactional;
import com.java110.core.base.smo.BaseServiceSMO;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.PageDto;
import com.java110.dto.auditMessage.AuditMessageDto;
import com.java110.dto.oaWorkflowData.OaWorkflowDataDto;
import com.java110.dto.user.UserDto;
import com.java110.dto.workflow.WorkflowDto;
import com.java110.entity.audit.AuditUser;
import com.java110.intf.common.IOaWorkflowUserInnerServiceSMO;
import com.java110.intf.common.IWorkflowInnerServiceSMO;
import com.java110.intf.oa.IOaWorkflowDataInnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.po.oaWorkflowData.OaWorkflowDataPo;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.query.Query;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Service("resourceEntryStoreSMOImpl")
@RestController
public class OaWorkflowUserInnerServiceSMOImpl extends BaseServiceSMO implements IOaWorkflowUserInnerServiceSMO {

    private static Logger logger = LoggerFactory.getLogger(OaWorkflowUserInnerServiceSMOImpl.class);
    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Autowired
    private IWorkflowInnerServiceSMO workflowInnerServiceSMOImpl;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private IOaWorkflowDataInnerServiceSMO oaWorkflowDataInnerServiceSMOImpl;


    /**
     * ????????????
     *
     * @return
     */
    public JSONObject startProcess(@RequestBody JSONObject reqJson) {
        //???????????????map,?????????????????????
        Map<String, Object> variables = new HashMap<String, Object>();
        //variables.put("reqJson", reqJson);
        variables.put("startUserId", reqJson.getString("createUserId"));
        variables.put("nextUserId", reqJson.getString("createUserId"));
        UserDto userDto = new UserDto();
        userDto.setUserId(reqJson.getString("createUserId"));
        List<UserDto> users = userInnerServiceSMOImpl.getUsers(userDto);

        Assert.listOnlyOne(users, "???????????????");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(getWorkflowDto(reqJson.getString("flowId")), reqJson.getString("id"), variables);
        //????????????????????????id??????????????????????????????
        String processInstanceId = processInstance.getId();
        // System.out.println("??????????????????.......????????????id:" + processInstanceId);
        reqJson.put("processInstanceId", processInstanceId);
        //???????????????????????????
        //autoFinishFirstTask(reqJson);
        //???????????????
        OaWorkflowDataPo oaWorkflowDataPo = null;
        oaWorkflowDataPo = new OaWorkflowDataPo();
        oaWorkflowDataPo.setBusinessKey(reqJson.getString("id"));
        oaWorkflowDataPo.setFlowId(reqJson.getString("flowId"));
        oaWorkflowDataPo.setContext(reqJson.getString("auditMessage"));
        oaWorkflowDataPo.setDataId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_dataId));
        oaWorkflowDataPo.setEvent(OaWorkflowDataDto.EVENT_COMMIT);
        oaWorkflowDataPo.setPreDataId("-1");
        oaWorkflowDataPo.setStaffId(reqJson.getString("createUserId"));
        oaWorkflowDataPo.setStaffName(users.get(0).getName());
        oaWorkflowDataPo.setStartTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        oaWorkflowDataPo.setStoreId(reqJson.getString("storeId"));
        oaWorkflowDataInnerServiceSMOImpl.saveOaWorkflowData(oaWorkflowDataPo);
        return reqJson;
    }

    private String getWorkflowDto(String flowId) {
        return WorkflowDto.DEFAULT_PROCESS + flowId;
    }

    /**
     * ?????????????????????
     */
    private void autoFinishFirstTask(JSONObject reqJson) {
        Task task = null;
        TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(reqJson.getString("createUserId")).active();
        List<Task> todoList = query.list();//????????????????????????????????????
        for (Task tmp : todoList) {
            if (tmp.getProcessInstanceId().equals(reqJson.getString("processInstanceId"))) {
                task = tmp;//?????????????????????????????????????????????????????????
                break;
            }
        }
        Assert.notNull(task, "???????????????????????????userId = " + reqJson.getString("createUserId"));
        reqJson.put("taskId", task.getId());
        reqJson.put("auditCode", "10000");
        reqJson.put("auditMessage", "??????");
        completeTask(reqJson);
    }

    /**
     * ?????????????????????
     *
     * @param user
     * @return
     */
    public long getUserTaskCount(@RequestBody AuditUser user) {
        TaskService taskService = processEngine.getTaskService();
        TaskQuery query = taskService.createTaskQuery().processDefinitionKey(getWorkflowDto(user.getFlowId()));
        query.taskAssignee(user.getUserId());
        return query.count();
    }

    /**
     * ??????????????????
     *
     * @param user ????????????
     */
    public List<JSONObject> getUserTasks(@RequestBody AuditUser user) {
        TaskService taskService = processEngine.getTaskService();
        TaskQuery query = taskService.createTaskQuery().processDefinitionKey(getWorkflowDto(user.getFlowId()));

        query.taskAssignee(user.getUserId());
        query.orderByTaskCreateTime().desc();
        List<Task> list = null;
        if (user.getPage() != PageDto.DEFAULT_PAGE) {
            list = query.listPage((user.getPage() - 1) * user.getRow(), user.getRow());
        } else {
            list = query.list();
        }
        JSONObject taskBusinessKeyMap = null;
        List<JSONObject> tasks = new ArrayList<>();
        for (Task task : list) {
            taskBusinessKeyMap = new JSONObject();
            String processInstanceId = task.getProcessInstanceId();
            //3.???????????????????????????
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            //4.??????????????????????????????BusinessKey
            String business_key = pi.getBusinessKey();
            taskBusinessKeyMap.put(business_key, task.getId());
            taskBusinessKeyMap.put("taskId", task.getId());
            taskBusinessKeyMap.put("id", business_key);
            tasks.add(taskBusinessKeyMap);
        }
        return tasks;
    }


    /**
     * ?????????????????????
     *
     * @param user
     * @return
     */
    public long getUserHistoryTaskCount(@RequestBody AuditUser user) {
        HistoryService historyService = processEngine.getHistoryService();
//        Query query = historyService.createHistoricTaskInstanceQuery()
//                .processDefinitionKey("complaint")
//                .taskAssignee(user.getUserId());

        HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(getWorkflowDto(user.getFlowId()))
                .taskAssignee(user.getUserId())
                .finished();
        if (!StringUtil.isEmpty(user.getAuditLink()) && "START".equals(user.getAuditLink())) {
            historicTaskInstanceQuery.taskName("complaint");
        } else if (!StringUtil.isEmpty(user.getAuditLink()) && "AUDIT".equals(user.getAuditLink())) {
            historicTaskInstanceQuery.taskName("complaitDealUser");
        }

        Query query = historicTaskInstanceQuery;

        return query.count();
    }

    /**
     * ???????????????????????????
     *
     * @param user ????????????
     */
    public List<JSONObject> getUserHistoryTasks(@RequestBody AuditUser user) {
        HistoryService historyService = processEngine.getHistoryService();

        HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(getWorkflowDto(user.getFlowId()))
                .taskAssignee(user.getUserId())
                .finished();
        if (!StringUtil.isEmpty(user.getAuditLink()) && "START".equals(user.getAuditLink())) {
            historicTaskInstanceQuery.taskName("complaint");
        } else if (!StringUtil.isEmpty(user.getAuditLink()) && "AUDIT".equals(user.getAuditLink())) {
            historicTaskInstanceQuery.taskName("complaitDealUser");
        }

        Query query = historicTaskInstanceQuery.orderByHistoricTaskInstanceStartTime().desc();

        List<HistoricTaskInstance> list = null;
        if (user.getPage() != PageDto.DEFAULT_PAGE) {
            list = query.listPage((user.getPage() - 1) * user.getRow(), user.getRow());
        } else {
            list = query.list();
        }
        JSONObject taskBusinessKeyMap = null;
        List<JSONObject> tasks = new ArrayList<>();
        List<String> complaintIds = new ArrayList<>();
        for (HistoricTaskInstance task : list) {
            taskBusinessKeyMap = new JSONObject();
            String processInstanceId = task.getProcessInstanceId();
            //3.???????????????????????????
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            //4.??????????????????????????????BusinessKey
            String business_key = pi.getBusinessKey();
            taskBusinessKeyMap.put(business_key, task.getId());
            taskBusinessKeyMap.put("taskId", task.getId());
            taskBusinessKeyMap.put("id", business_key);
            tasks.add(taskBusinessKeyMap);
        }

        return tasks;
    }


    @Java110Transactional
    public boolean completeTask(@RequestBody JSONObject reqJson) {
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().taskId(reqJson.getString("taskId")).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("???????????????");
        }

        //???????????????????????????
        if ("1500".equals(reqJson.getString("auditCode"))) {
            doTaskFinish(reqJson);
        } else {
            //?????? ???????????????
            doTaskAuditAgree(reqJson);
        }

        String processInstanceId = task.getProcessInstanceId();
        Authentication.setAuthenticatedUserId(reqJson.getString("nextUserId"));
        taskService.addComment(reqJson.getString("taskId"), processInstanceId, reqJson.getString("auditMessage"));
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("nextUserId", reqJson.getString("nextUserId"));
        variables.put("auditCode", reqJson.getString("auditCode"));
        taskService.complete(reqJson.getString("taskId"), variables);
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (pi == null) {
            return true;
        }
        return false;
    }

    private void doTaskFinish(JSONObject reqJson) {
        OaWorkflowDataDto oaWorkflowDataDto = new OaWorkflowDataDto();
        oaWorkflowDataDto.setStoreId(reqJson.getString("storeId"));
        oaWorkflowDataDto.setBusinessKey(reqJson.getString("id"));
        oaWorkflowDataDto.setPage(1);
        oaWorkflowDataDto.setRow(1);
        List<OaWorkflowDataDto> oaWorkflowDataDtos = oaWorkflowDataInnerServiceSMOImpl.queryOaWorkflowDatas(oaWorkflowDataDto);

        if (oaWorkflowDataDtos == null || oaWorkflowDataDtos.size() < 1) {
            return;
        }
        //?????? ?????? ?????????
        OaWorkflowDataPo oaWorkflowDataPo = new OaWorkflowDataPo();
        oaWorkflowDataPo.setDataId(oaWorkflowDataDtos.get(0).getDataId());
        oaWorkflowDataPo.setEndTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        oaWorkflowDataPo.setContext(reqJson.getString("auditMessage"));
        oaWorkflowDataInnerServiceSMOImpl.updateOaWorkflowData(oaWorkflowDataPo);
    }

    /**
     * ?????? ????????????
     *
     * @param reqJson
     */
    private void doTaskAuditAgree(JSONObject reqJson) {
        UserDto userDto = new UserDto();
        userDto.setUserId(reqJson.getString("nextUserId"));
        List<UserDto> users = userInnerServiceSMOImpl.getUsers(userDto);

        Assert.listOnlyOne(users, "???????????????");

        String preDataId = "-1";
        //??????????????????
        OaWorkflowDataDto oaWorkflowDataDto = new OaWorkflowDataDto();
        oaWorkflowDataDto.setStoreId(reqJson.getString("storeId"));
        oaWorkflowDataDto.setBusinessKey(reqJson.getString("id"));
        oaWorkflowDataDto.setPage(1);
        oaWorkflowDataDto.setRow(1);
        List<OaWorkflowDataDto> oaWorkflowDataDtos = oaWorkflowDataInnerServiceSMOImpl.queryOaWorkflowDatas(oaWorkflowDataDto);

        OaWorkflowDataPo oaWorkflowDataPo = null;
        if (oaWorkflowDataDtos == null || oaWorkflowDataDtos.size() < 1) {
            oaWorkflowDataPo = new OaWorkflowDataPo();
            oaWorkflowDataPo.setBusinessKey(reqJson.getString("id"));
            oaWorkflowDataPo.setFlowId(reqJson.getString("flowId"));
            oaWorkflowDataPo.setContext("");
            oaWorkflowDataPo.setDataId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_dataId));
            oaWorkflowDataPo.setEvent(OaWorkflowDataDto.EVENT_COMMIT);
            oaWorkflowDataPo.setPreDataId(preDataId);
            oaWorkflowDataPo.setStaffId(reqJson.getString("nextUserId"));
            oaWorkflowDataPo.setStaffName(users.get(0).getName());
            oaWorkflowDataPo.setStartTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
            oaWorkflowDataPo.setStoreId(reqJson.getString("storeId"));
            oaWorkflowDataInnerServiceSMOImpl.saveOaWorkflowData(oaWorkflowDataPo);
            return;
        }
        //?????? ?????? ?????????
        oaWorkflowDataPo = new OaWorkflowDataPo();
        oaWorkflowDataPo.setDataId(oaWorkflowDataDtos.get(0).getDataId());
        oaWorkflowDataPo.setEndTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        oaWorkflowDataPo.setContext(reqJson.getString("auditMessage"));
        oaWorkflowDataInnerServiceSMOImpl.updateOaWorkflowData(oaWorkflowDataPo);

        //?????????-1 ???????????????
        if ("-1".equals(reqJson.getString("nextUserId"))) {
            return;
        }

        oaWorkflowDataPo = new OaWorkflowDataPo();
        oaWorkflowDataPo.setBusinessKey(reqJson.getString("id"));
        oaWorkflowDataPo.setFlowId(reqJson.getString("flowId"));
        oaWorkflowDataPo.setContext("");
        oaWorkflowDataPo.setDataId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_dataId));
        oaWorkflowDataPo.setEvent(OaWorkflowDataDto.EVENT_COMMIT);
        oaWorkflowDataPo.setPreDataId(oaWorkflowDataDtos.get(0).getDataId());
        oaWorkflowDataPo.setStaffId(reqJson.getString("nextUserId"));
        oaWorkflowDataPo.setStaffName(users.get(0).getName());
        oaWorkflowDataPo.setStartTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        oaWorkflowDataPo.setStoreId(reqJson.getString("storeId"));
        oaWorkflowDataInnerServiceSMOImpl.saveOaWorkflowData(oaWorkflowDataPo);
    }

    /**
     * ??????
     *
     * @param reqJson
     * @return
     */
    @Java110Transactional
    public boolean changeTaskToOtherUser(@RequestBody JSONObject reqJson) {

        //??????????????????
        OaWorkflowDataDto oaWorkflowDataDto = new OaWorkflowDataDto();
        oaWorkflowDataDto.setStoreId(reqJson.getString("storeId"));
        oaWorkflowDataDto.setBusinessKey(reqJson.getString("id"));
        oaWorkflowDataDto.setPage(1);
        oaWorkflowDataDto.setRow(1);
        List<OaWorkflowDataDto> oaWorkflowDataDtos = oaWorkflowDataInnerServiceSMOImpl.queryOaWorkflowDatas(oaWorkflowDataDto);

        Assert.listOnlyOne(oaWorkflowDataDtos, "?????????????????????????????????");

        UserDto userDto = new UserDto();
        userDto.setUserId(reqJson.getString("nextUserId"));
        List<UserDto> users = userInnerServiceSMOImpl.getUsers(userDto);

        Assert.listOnlyOne(users, "???????????????");

        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().taskId(reqJson.getString("taskId")).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        taskService.addComment(reqJson.getString("taskId"), processInstanceId, reqJson.getString("auditMessage"));
        taskService.setAssignee(reqJson.getString("taskId"), reqJson.getString("nextUserId"));
        //taskService.setOwner(reqJson.getString("taskId"), reqJson.getString("nextUserId"));

        OaWorkflowDataPo oaWorkflowDataPo = null;
        //?????? ?????? ?????????
        oaWorkflowDataPo = new OaWorkflowDataPo();
        oaWorkflowDataPo.setDataId(oaWorkflowDataDtos.get(0).getDataId());
        oaWorkflowDataPo.setEndTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        oaWorkflowDataPo.setContext(reqJson.getString("auditMessage"));
        oaWorkflowDataInnerServiceSMOImpl.updateOaWorkflowData(oaWorkflowDataPo);

        oaWorkflowDataPo = new OaWorkflowDataPo();
        oaWorkflowDataPo.setBusinessKey(reqJson.getString("id"));
        oaWorkflowDataPo.setFlowId(reqJson.getString("flowId"));
        oaWorkflowDataPo.setContext("");
        oaWorkflowDataPo.setDataId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_dataId));
        oaWorkflowDataPo.setEvent(OaWorkflowDataDto.EVENT_TRANSFER);
        oaWorkflowDataPo.setPreDataId(oaWorkflowDataDtos.get(0).getDataId());
        oaWorkflowDataPo.setStaffId(reqJson.getString("nextUserId"));
        oaWorkflowDataPo.setStaffName(users.get(0).getName());
        oaWorkflowDataPo.setStartTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        oaWorkflowDataPo.setStoreId(reqJson.getString("storeId"));
        oaWorkflowDataInnerServiceSMOImpl.saveOaWorkflowData(oaWorkflowDataPo);
        return true;
    }
    @Java110Transactional
    public boolean goBackTask(@RequestBody JSONObject reqJson) {
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().taskId(reqJson.getString("taskId")).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("???????????????");
        }
//        //??????????????????
//        if ("1400".equals(reqJson.getString("auditCode"))) {
//            String processInstanceId = task.getProcessInstanceId();
//            Authentication.setAuthenticatedUserId(reqJson.getString("startUserId"));
//            taskService.addComment(reqJson.getString("taskId"), processInstanceId, reqJson.getString("auditMessage"));
//            Map<String, Object> variables = new HashMap<String, Object>();
//            variables.put("nextUserId", reqJson.getString("startUserId"));
//            variables.put("auditCode", reqJson.getString("auditCode"));
//            taskService.complete(reqJson.getString("taskId"), variables);
//
//            return true;
//        }

        String event = doTaskAuditUnAgree(reqJson);
        if (OaWorkflowDataDto.EVENT_COMMIT.equals(event)) { //????????????
            String processInstanceId = task.getProcessInstanceId();
            Authentication.setAuthenticatedUserId(reqJson.getString("nextUserId"));
            taskService.addComment(reqJson.getString("taskId"), processInstanceId, reqJson.getString("auditMessage"));
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("nextUserId", reqJson.getString("nextUserId"));
            variables.put("auditCode", reqJson.getString("auditCode"));
            taskService.complete(reqJson.getString("taskId"), variables);
        } else { //??????
            taskService.setAssignee(reqJson.getString("taskId"), reqJson.getString("nextUserId"));
        }
        return true;
    }

    /**
     * ??????????????? ??????
     *
     * @param reqJson
     */
    private String doTaskAuditUnAgree(JSONObject reqJson) {
        //??????????????????
        OaWorkflowDataDto oaWorkflowDataDto = new OaWorkflowDataDto();
        oaWorkflowDataDto.setStoreId(reqJson.getString("storeId"));
        oaWorkflowDataDto.setBusinessKey(reqJson.getString("id"));
        oaWorkflowDataDto.setPage(1);
        oaWorkflowDataDto.setRow(1);
        List<OaWorkflowDataDto> oaWorkflowDataDtos = oaWorkflowDataInnerServiceSMOImpl.queryOaWorkflowDatas(oaWorkflowDataDto);

        Assert.listOnlyOne(oaWorkflowDataDtos, "?????????????????????");

        if ("-1".equals(oaWorkflowDataDtos.get(0).getPreDataId())) {
            throw new IllegalArgumentException("?????????????????????");
        }

        oaWorkflowDataDto = new OaWorkflowDataDto();
        oaWorkflowDataDto.setStoreId(reqJson.getString("storeId"));
        oaWorkflowDataDto.setDataId(oaWorkflowDataDtos.get(0).getPreDataId());
        oaWorkflowDataDto.setPage(1);
        oaWorkflowDataDto.setRow(1);
        List<OaWorkflowDataDto> preOaWorkflowDataDtos = oaWorkflowDataInnerServiceSMOImpl.queryOaWorkflowDatas(oaWorkflowDataDto);

        //??????????????????????????????
        OaWorkflowDataPo oaWorkflowDataPo = new OaWorkflowDataPo();
        oaWorkflowDataPo.setDataId(oaWorkflowDataDtos.get(0).getDataId());
        oaWorkflowDataPo.setEndTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        oaWorkflowDataPo.setContext(reqJson.getString("auditMessage"));
        oaWorkflowDataInnerServiceSMOImpl.updateOaWorkflowData(oaWorkflowDataPo);

        reqJson.put("nextUserId", preOaWorkflowDataDtos.get(0).getStaffId());
        if ("1400".equals(reqJson.getString("auditCode"))) {
            reqJson.put("nextUserId", reqJson.getString("startUserId"));
        }
        oaWorkflowDataPo = new OaWorkflowDataPo();
        oaWorkflowDataPo.setBusinessKey(preOaWorkflowDataDtos.get(0).getBusinessKey());
        oaWorkflowDataPo.setFlowId(preOaWorkflowDataDtos.get(0).getFlowId());
        oaWorkflowDataPo.setContext("");
        oaWorkflowDataPo.setDataId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_dataId));
        oaWorkflowDataPo.setEvent(preOaWorkflowDataDtos.get(0).getEvent());
        oaWorkflowDataPo.setPreDataId(oaWorkflowDataDtos.get(0).getPreDataId());
        oaWorkflowDataPo.setStaffId(reqJson.getString("nextUserId"));
        oaWorkflowDataPo.setStaffName(preOaWorkflowDataDtos.get(0).getStaffName());
        oaWorkflowDataPo.setStartTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        oaWorkflowDataPo.setStoreId(preOaWorkflowDataDtos.get(0).getStoreId());
        oaWorkflowDataInnerServiceSMOImpl.saveOaWorkflowData(oaWorkflowDataPo);

        return oaWorkflowDataDtos.get(0).getEvent();
    }

    public List<AuditMessageDto> getAuditMessage(@RequestBody JSONObject reqJson) {

        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().taskId(reqJson.getString("taskId")).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        List<Comment> comments = taskService.getProcessInstanceComments(processInstanceId);
        List<AuditMessageDto> auditMessageDtos = new ArrayList<>();
        if (comments == null || comments.size() < 1) {
            return auditMessageDtos;
        }
        AuditMessageDto messageDto = null;
        for (Comment comment : comments) {
            messageDto = new AuditMessageDto();
            messageDto.setCreateTime(comment.getTime());
            messageDto.setUserId(comment.getUserId());
            messageDto.setMessage(comment.getFullMessage());
        }

        return auditMessageDtos;
    }

    /**
     * ???????????????????????????
     *
     * @param reqJson
     * @return
     */
    public JSONObject getTaskCurrentUser(@RequestBody JSONObject reqJson) {

        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(reqJson.getString("id")).list();

        if (tasks == null || tasks.size() == 0) {
            reqJson.put("currentUserId", "");
            reqJson.put("currentUserName", "");
            reqJson.put("currentUserTel", "");
            return reqJson;
        }
        String userIds = "";
        String userNames = "";
        String userTels = "";
        String taskIds = "";
        for (Task task : tasks) {
            String userId = task.getAssignee();
            taskIds += (task.getId() + "/");
            List<UserDto> users = userInnerServiceSMOImpl.getUserInfo(new String[]{userId});
            if (users == null || users.size() == 0) {
                continue;
            }
            userIds += (userId + "/");
            userNames += (users.get(0).getName() + "/");
            userTels += (users.get(0).getTel() + "/");
        }
        userIds = userIds.endsWith("/") ? userIds.substring(0, userIds.length() - 1) : userIds;
        userNames = userNames.endsWith("/") ? userNames.substring(0, userNames.length() - 1) : userNames;
        userTels = userTels.endsWith("/") ? userTels.substring(0, userTels.length() - 1) : userTels;
        taskIds = taskIds.endsWith("/") ? taskIds.substring(0, taskIds.length() - 1) : taskIds;

        reqJson.put("currentUserId", userIds);
        reqJson.put("currentUserName", userNames);
        reqJson.put("currentUserTel", userTels);
        reqJson.put("taskId", taskIds);
        return reqJson;

    }

    @Override
    public List<JSONObject> nextAllNodeTaskList(@RequestBody JSONObject reqJson) {
        List<JSONObject> tasks = new ArrayList<>();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().taskId(reqJson.getString("taskId")).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("???????????????");
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        //??????????????????????????????
        List<SequenceFlow> outgoingFlows = flowNode.getOutgoingFlows();
        JSONObject taskObj = null;
        taskObj = new JSONObject();
        taskObj.put("assignee", "-1"); //  ?????? ????????????????????????????????? ????????????
        boolean isReturn = false;
        //??????????????????
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            //????????????????????????
            FlowElement targetFlowElement = outgoingFlow.getTargetFlowElement();
            isReturn = false;
            //???????????????????????????
            if (targetFlowElement instanceof UserTask) {
                //?????????????????????el?????????
                Map vars = new HashMap();
                vars.put("auditCode", "1200");
                if (isCondition(outgoingFlow.getConditionExpression(), vars)) {
                    //true ????????????????????????
                    taskObj.put("back", outgoingFlow.getTargetFlowElement().getName());
                    isReturn = true;
                }
                vars.put("auditCode", "1400");
                if (isCondition(outgoingFlow.getConditionExpression(), vars)) {
                    //true ????????????????????????
                    taskObj.put("backIndex", outgoingFlow.getTargetFlowElement().getName());
                    isReturn = true;
                }
                //??????
                vars.put("auditCode", "1500");
                if (isCondition(outgoingFlow.getConditionExpression(), vars)) {
                    //true ????????????????????????
                    taskObj.put("exit", outgoingFlow.getTargetFlowElement().getName());
                    isReturn = true;
                }
                if (!isReturn) {
                    String assignee = ((UserTask) targetFlowElement).getAssignee();
                    if (!StringUtil.isEmpty(assignee) && assignee.indexOf("${") < 0) {
                        taskObj.put("assignee", assignee); // ?????????????????????
                    }
                    if ("${startUserId}".equals(assignee)) {
                        taskObj.put("assignee", reqJson.getString("startUserId")); // ?????????
                    }
                    if ("${nextUserId}".equals(assignee)) {
                        taskObj.put("assignee", "-2"); // ??????????????????
                    }
                    taskObj.put("next", outgoingFlow.getTargetFlowElement().getName());
                }
            }
            //?????????????????? ????????????
            if (targetFlowElement instanceof EndEvent) {
                //true ????????????????????????
                taskObj.put("exit", "1");
            }
        }
        tasks.add(taskObj);
        return tasks;
    }

    /**
     * el???????????????
     *
     * @param expression
     * @param vars
     * @return
     */
    private static boolean isCondition(String expression, Map<String, Object> vars) {
        if (expression == null || expression == "") {
            return false;
        }

        //???????????????
        String[] exprArr = expression.split("[{}$&]");
        for (String expr : exprArr) {
            //???????????????message
            if (expr.contains("auditCode")) {
                if (!vars.containsKey("auditCode")) {
                    continue;
                }
                if (expr.contains("==")) {
                    String[] primes = expr.split("==");
                    String valExpr = primes[1].trim();
                    if (valExpr.startsWith("'")) {
                        valExpr = valExpr.substring(1);
                    }
                    if (valExpr.endsWith("'")) {
                        valExpr = valExpr.substring(0, valExpr.length() - 1);
                    }
                    if (primes.length == 2 && valExpr.equals(vars.get("auditCode"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public RuntimeService getRuntimeService() {
        return runtimeService;
    }

    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

}
