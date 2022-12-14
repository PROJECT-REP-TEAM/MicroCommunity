package com.java110.common.smo.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java110.common.dao.IWorkflowServiceDao;
import com.java110.core.base.smo.BaseServiceSMO;
import com.java110.dto.PageDto;
import com.java110.dto.user.UserDto;
import com.java110.dto.workflow.WorkflowAuditInfoDto;
import com.java110.dto.workflow.WorkflowDto;
import com.java110.dto.workflow.WorkflowModelDto;
import com.java110.dto.workflow.WorkflowStepDto;
import com.java110.dto.workflow.WorkflowStepStaffDto;
import com.java110.intf.common.IWorkflowInnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.utils.util.Base64Convert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.task.Comment;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName FloorInnerServiceSMOImpl
 * @Description ??????????????????????????????
 * @Author wuxw
 * @Date 2019/4/24 9:20
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
@RestController
public class WorkflowInnerServiceSMOImpl extends BaseServiceSMO implements IWorkflowInnerServiceSMO {
    private static final Logger logger = LoggerFactory.getLogger(BaseServiceSMO.class);

    @Autowired
    private IWorkflowServiceDao workflowServiceDaoImpl;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<WorkflowDto> queryWorkflows(@RequestBody WorkflowDto workflowDto) {

        //?????????????????? ????????????

        int page = workflowDto.getPage();

        if (page != PageDto.DEFAULT_PAGE) {
            workflowDto.setPage((page - 1) * workflowDto.getRow());
        }

        List<WorkflowDto> workflows = BeanConvertUtil.covertBeanList(workflowServiceDaoImpl.getWorkflowInfo(BeanConvertUtil.beanCovertMap(workflowDto)), WorkflowDto.class);

        if (workflows == null || workflows.size() == 0) {
            return workflows;
        }

        String[] userIds = getUserIds(workflows);
        //?????? userId ??????????????????
        List<UserDto> users = userInnerServiceSMOImpl.getUserInfo(userIds);

        for (WorkflowDto workflow : workflows) {
            refreshWorkflow(workflow, users);
        }
        return workflows;
    }

    /**
     * ?????????????????????????????????????????????????????? ????????? floor?????????
     *
     * @param workflow ?????????????????????
     * @param users    ????????????
     */
    private void refreshWorkflow(WorkflowDto workflow, List<UserDto> users) {
        for (UserDto user : users) {
            if (workflow.getFlowId().equals(user.getUserId())) {
                BeanConvertUtil.covertBean(user, workflow);
            }
        }
    }

    /**
     * ????????????userId
     *
     * @param workflows ???????????????
     * @return ??????userIds ??????
     */
    private String[] getUserIds(List<WorkflowDto> workflows) {
        List<String> userIds = new ArrayList<String>();
        for (WorkflowDto workflow : workflows) {
            userIds.add(workflow.getFlowId());
        }

        return userIds.toArray(new String[userIds.size()]);
    }

    public String getRunWorkflowImage(@RequestBody String businessKey) {

        String image = "";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        try {
            //  ????????????????????????
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceBusinessKey(businessKey).singleResult();

            if (historicProcessInstance == null) {
                //throw new BusinessException("??????????????????ID[" + pProcessInstanceId + "]????????????????????????????????????");
            } else {
                // ??????????????????
                ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(historicProcessInstance.getProcessDefinitionId());

                // ??????????????????????????????????????????????????????????????????????????????????????????
                List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(historicProcessInstance.getId()).orderByHistoricActivityInstanceId().asc().list();

                // ??????????????????ID??????
                List<String> executedActivityIdList = new ArrayList<String>();
                int index = 1;
                //logger.info("???????????????????????????ID");
                for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
                    executedActivityIdList.add(activityInstance.getActivityId());

                    //logger.info("???[" + index + "]??????????????????=" + activityInstance.getActivityId() + " : " +activityInstance.getActivityName());
                    index++;
                }

                BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());

                // ?????????????????????
                List<String> flowIds = new ArrayList<String>();
                // ???????????????????????? (getHighLightedFlows??????????????????)
                flowIds = getHighLightedFlows(bpmnModel, processDefinition, historicActivityInstanceList);


                // ??????????????????????????????
                ProcessDiagramGenerator pec = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();
                //????????????
                InputStream imageStream = pec.generateDiagram(bpmnModel, "png", executedActivityIdList, flowIds, "??????", "??????", "??????", null, 2.0);


                image = Base64Convert.ioToBase64(imageStream);
            }
        } catch (Exception e) {
            logger.error("??????????????????", e);

        }
        return image;
    }

    public List<String> getHighLightedFlows(BpmnModel bpmnModel, ProcessDefinitionEntity processDefinitionEntity, List<HistoricActivityInstance> historicActivityInstances) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //24?????????
        List<String> highFlows = new ArrayList<String>();// ????????????????????????flowId

        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
            // ?????????????????????????????????
            // ?????????????????????????????????
            FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i).getActivityId());


            List<FlowNode> sameStartTimeNodes = new ArrayList<FlowNode>();// ?????????????????????????????????????????????
            FlowNode sameActivityImpl1 = null;

            HistoricActivityInstance activityImpl_ = historicActivityInstances.get(i);// ???????????????
            HistoricActivityInstance activityImp2_;

            for (int k = i + 1; k <= historicActivityInstances.size() - 1; k++) {
                activityImp2_ = historicActivityInstances.get(k);// ?????????1?????????

                if (activityImpl_.getActivityType().equals("userTask") && activityImp2_.getActivityType().equals("userTask") &&
                        df.format(activityImpl_.getStartTime()).equals(df.format(activityImp2_.getStartTime()))) //??????usertask???????????????????????????????????????????????????????????????????????????????????????
                {

                } else {
                    sameActivityImpl1 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(k).getActivityId());//????????????????????????????????????
                    break;
                }

            }
            sameStartTimeNodes.add(sameActivityImpl1); // ????????????????????????????????????????????????????????????
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// ?????????????????????
                HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// ?????????????????????

                if (df.format(activityImpl1.getStartTime()).equals(df.format(activityImpl2.getStartTime()))) {// ???????????????????????????????????????????????????????????????
                    FlowNode sameActivityImpl2 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {// ????????????????????????
                    break;
                }
            }
            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows(); // ?????????????????????????????????

            for (SequenceFlow pvmTransition : pvmTransitions) {// ???????????????????????????
                FlowNode pvmActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(pvmTransition.getTargetRef());// ?????????????????????????????????????????????????????????????????????????????????id?????????????????????
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }

        }
        return highFlows;

    }


    public String getWorkflowImage(@RequestBody WorkflowDto workflowDto) {

        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        String image = "";
        List<String> list = processEngine.getRepositoryService()//
                .getDeploymentResourceNames(workflowDto.getProcessDefinitionKey());

        String resourceName = "";
        if (list != null && list.size() > 0) {
            for (String name : list) {
                if (name.indexOf(".png") >= 0) {
                    resourceName = name;
                }
            }
        }

        InputStream in = processEngine.getRepositoryService()
                .getResourceAsStream(workflowDto.getProcessDefinitionKey(), resourceName);


        try {
            image = Base64Convert.ioToBase64(in);
        } catch (IOException e) {
            logger.error("??????????????????", e);
        }
        return image;
    }


    /**
     * @Date???2017/11/24
     * @Description????????????????????????
     */
    public WorkflowDto addFlowDeployment(@RequestBody WorkflowDto workflowDto) {

        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//
//        RepositoryService repositoryService  = processEngine.getRepositoryService();
//        repositoryService.deleteDeployment("1");
        try {
            // 1. ????????????
            BpmnModel model = new BpmnModel();
            Process process = new Process();
            model.addProcess(process);
            process.setId(WorkflowDto.DEFAULT_PROCESS + workflowDto.getFlowId());
            process.setName(workflowDto.getFlowName());
            process.setDocumentation(workflowDto.getDescrible());
            //????????????
            //????????????
            process.addFlowElement(createStartEvent());
            List<WorkflowStepDto> workflowStepDtos = workflowDto.getWorkflowSteps();
            for (int i = 0; i < workflowStepDtos.size(); i++) {
                WorkflowStepDto step = workflowStepDtos.get(i);
                //??????????????????
                if (WorkflowStepDto.TYPE_COUNTERSIGN.equals(step.getType())) {
                    //??????
                    //??????????????????-??????
                    process.addFlowElement(createParallelGateway("parallelGateway-fork" + i, "parallelGateway-fork" + i));
                    //???????????????????????????
                    List<WorkflowStepStaffDto> userList = step.getWorkflowStepStaffs();
                    for (int u = 0; u < userList.size(); u++) {
                        //?????????????????????????????????
                        process.addFlowElement(createUserTask("userTask" + i + u, userList.get(u).getStaffName(), userList.get(u).getStaffId()));
                    }
                    //????????????-??????
                    process.addFlowElement(createParallelGateway("parallelGateway-join" + i, "parallelGateway-join" + i));

                    process.addFlowElement(createUserTask("repulse" + i, "?????????", "${startUserId}"));

                } else {
                    //????????????
                    //????????????
                    process.addFlowElement(createUserTask("task" + i, step.getWorkflowStepStaffs().get(0).getStaffName(), step.getWorkflowStepStaffs().get(0).getStaffId()));
                    //????????????
                    process.addFlowElement(createUserTask("repulse" + i, "?????????", "${startUserId}"));
                }
            }
            //????????????
            process.addFlowElement(createEndEvent());


            //??????
            for (int y = 0; y < workflowStepDtos.size(); y++) {
                WorkflowStepDto step = workflowStepDtos.get(y);
                //????????????
                if (WorkflowStepDto.TYPE_COUNTERSIGN.equals(step.getType())) {
                    //??????
                    //???????????????????????????
                    if (y == 0) {
                        //???????????????????????????-????????????
                        process.addFlowElement(createSequenceFlow("startEvent", "parallelGateway-fork" + y, "startEvent-parallelGateway-fork" + y, ""));
                    } else {
                        //??????????????????????????????-?????????????????????-??????
                        //????????????????????????????????????
                        if (WorkflowStepDto.TYPE_COUNTERSIGN.equals(workflowStepDtos.get(y - 1).getType())) {
                            process.addFlowElement(createSequenceFlow("parallelGateway-join" + (y - 1), "parallelGateway-fork" + y, "parallelGateway-join-parallelGateway-fork-??????" + y, "${flag=='true'}"));
                        } else {
                            process.addFlowElement(createSequenceFlow("task" + (y - 1), "parallelGateway-fork" + y, "task-parallelGateway-fork" + y, "${flag=='true'}"));
                        }
                    }
                    //????????????-?????????????????????????????????????????????????????????-????????????
                    List<WorkflowStepStaffDto> userList = step.getWorkflowStepStaffs();
                    for (int u = 0; u < userList.size(); u++) {
                        process.addFlowElement(createSequenceFlow("parallelGateway-fork" + y, "userTask" + y + u, "parallelGateway-fork-userTask" + y + u, ""));
                        process.addFlowElement(createSequenceFlow("userTask" + y + u, "parallelGateway-join" + y, "userTask-parallelGateway-join", ""));
                        if (u == (userList.size() - 1)) {
                            if (y == (workflowStepDtos.size() - 1)) {
                                if("Y".equals(workflowDto.getStartNodeFinish())){ //?????????????????????
                                    process.addFlowElement(createSequenceFlow("parallelGateway-join" + y, "repulse" + y, "parallelGateway-join-repulse", ""));
                                }else {
                                    process.addFlowElement(createSequenceFlow("parallelGateway-join" + y, "endEvent", "parallelGateway-join-endEvent" + y, "${flag=='true'}"));
                                    process.addFlowElement(createSequenceFlow("parallelGateway-join" + y, "repulse" + y, "tparallelGateway-join-repulse" + y, "${flag=='false'}"));
                                }
                            } else {
                                process.addFlowElement(createSequenceFlow("parallelGateway-join" + y, "repulse" + y, "parallelGateway-join-repulse", "${flag=='false'}"));
                            }
                            process.addFlowElement(createSequenceFlow("repulse" + y, "task" + getNormal(workflowStepDtos, y), "repulse-task" + y, "${flag=='true'}"));
                        }
                    }


                    process.addFlowElement(createSequenceFlow("repulse" + y, "endEvent", "parallelGateway-join-endEvent", "${flag=='false'}"));

                } else {
                    //????????????
                    //???????????????
                    if (y == 0) {
                        //???????????????????????????1
                        process.addFlowElement(createSequenceFlow("startEvent", "task" + y, "startEvent-task" + y, ""));
                    } else {
                        //?????????????????????????????????
                        if (WorkflowStepDto.TYPE_COUNTERSIGN.equals(workflowStepDtos.get(y - 1).getType())) {
                            //??????
                            //????????????-?????????????????????
                            process.addFlowElement(createSequenceFlow("parallelGateway-join" + (y - 1), "task" + y, "parallelGateway-join-task" + y, "${flag=='true'}"));
                        } else {
                            //??????
                            process.addFlowElement(createSequenceFlow("task" + (y - 1), "task" + y, "task" + (y - 1) + "task" + y, "${flag=='true'}"));
                        }
                    }
                    //????????????????????????
                  /*  if (y == (workflowStepDtos.size() - 1)) {
                        //???????????????????????????
                        process.addFlowElement(createSequenceFlow("repulse" + y, "endEvent", "task" + y + "endEvent", "${flag=='false'}"));
                        process.addFlowElement(createSequenceFlow("task" + y, "repulse" + y, "task-repulse" + y, "${flag=='false'}"));
                    } else {
                        //???????????????????????????
                        process.addFlowElement(createSequenceFlow("task" + y, "repulse" + y, "task-repulse" + y, "${flag=='false'}"));
                    }*/
                    process.addFlowElement(createSequenceFlow("repulse" + y, "endEvent", "repulse" + y + "endEvent", "${flag=='false'}"));
                    if (y == (workflowStepDtos.size() - 1)) {
                        if("Y".equals(workflowDto.getStartNodeFinish())){ //?????????????????????
                            process.addFlowElement(createSequenceFlow("task" + y, "repulse" + y, "task-repulse" + y, ""));
                        }else {
                            process.addFlowElement(createSequenceFlow("task" + y, "endEvent", "task-endEvent" + y, "${flag=='true'}"));
                            process.addFlowElement(createSequenceFlow("task" + y, "repulse" + y, "task-repulse" + y, "${flag=='false'}"));
                        }
                    } else {
                        process.addFlowElement(createSequenceFlow("task" + y, "repulse" + y, "task-repulse" + y, "${flag=='false'}"));
                    }

                    process.addFlowElement(createSequenceFlow("repulse" + y, "task" + y, "repulse-task" + y, "${flag=='true'}"));
                }
            }

            // 2. ?????????????????????
            new BpmnAutoLayout(model).execute();

            // 3. ????????????
            Deployment deployment = processEngine.getRepositoryService().createDeployment()
                    .addBpmnModel(process.getId() + ".bpmn", model).name(process.getId() + "_deployment").deploy();
            workflowDto.setProcessDefinitionKey(deployment.getId());

        } catch (Exception e) {
            logger.error("?????????????????????", e);
        }

        logger.debug("?????????????????????");
        return workflowDto;
    }

    private int getNormal(List<WorkflowStepDto> workflowStepDtos, int y) {
        for (int stepIndex = y; stepIndex > 0; stepIndex--) {
            if (WorkflowStepDto.TYPE_NORMAL.equals(workflowStepDtos.get(stepIndex).getType())) {
                return stepIndex;
            }
        }

        return 0;
    }


    //????????????-???
    protected UserTask createGroupTask(String id, String name, String candidateGroup) {
        List<String> candidateGroups = new ArrayList<String>();
        candidateGroups.add(candidateGroup);
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setCandidateGroups(candidateGroups);
        return userTask;
    }

    //????????????-??????
    protected UserTask createUserTask(String id, String name, String userPkno) {
        List<String> candidateUsers = new ArrayList<String>();
        candidateUsers.add(userPkno);
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee(userPkno);
        //userTask.setCandidateUsers(candidateUsers);
        return userTask;
    }

    //????????????-?????????
    protected UserTask createAssigneeTask(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee(assignee);
        return userTask;
    }

    /*??????*/
    protected SequenceFlow createSequenceFlow(String from, String to, String name, String conditionExpression) {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        flow.setName(name);
        if (!StringUtil.isEmpty(conditionExpression)) {
            flow.setConditionExpression(conditionExpression);
        }
        return flow;
    }

    //????????????
    protected ExclusiveGateway createExclusiveGateway(String id, String name) {
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(id);
        exclusiveGateway.setName(name);
        return exclusiveGateway;
    }

    //????????????
    protected ParallelGateway createParallelGateway(String id, String name) {
        ParallelGateway gateway = new ParallelGateway();
        gateway.setId(id);
        gateway.setName(name);
        return gateway;
    }

    //????????????
    protected StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");
        return startEvent;
    }

    /*????????????*/
    protected EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        return endEvent;
    }

    @Override
    public int queryWorkflowsCount(@RequestBody WorkflowDto workflowDto) {
        return workflowServiceDaoImpl.queryWorkflowsCount(BeanConvertUtil.beanCovertMap(workflowDto));
    }

    /**
     * ??????????????????
     *
     * @param workflowAuditInfoDto
     * @return
     */
    public List<WorkflowAuditInfoDto> queryWorkflowAuditHistory(@RequestBody WorkflowAuditInfoDto workflowAuditInfoDto) {
        //List<TaskBo> taskBoList = new ArrayList<TaskBo>();
        HistoricProcessInstance hisProcessInstance = (HistoricProcessInstance) historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(workflowAuditInfoDto.getBusinessKey()).singleResult();
        List<HistoricActivityInstance> hisActInstList = new ArrayList<>();
        if (hisProcessInstance != null) {
            // ??????????????????????????????????????????
            hisActInstList = getHisUserTaskActivityInstanceList(hisProcessInstance
                    .getId());
        }
        List<WorkflowAuditInfoDto> workflowAuditInfoDtos = new ArrayList<>();
        WorkflowAuditInfoDto tmpWorkflowAuditInfoDto = null;
        for (Iterator iterator = hisActInstList.iterator(); iterator.hasNext(); ) {
            // ???????????????HistoricActivityInstance
            HistoricActivityInstance activityInstance = (HistoricActivityInstance) iterator
                    .next();

            tmpWorkflowAuditInfoDto = new WorkflowAuditInfoDto();
            tmpWorkflowAuditInfoDto.setAuditName(activityInstance.getActivityName());
            if (activityInstance.getEndTime() != null) {
                tmpWorkflowAuditInfoDto.setAuditTime(DateUtil.getFormatTimeString(activityInstance.getEndTime(), DateUtil.DATE_FORMATE_STRING_A));
                tmpWorkflowAuditInfoDto.setStateName("??????");
                tmpWorkflowAuditInfoDto.setState(WorkflowAuditInfoDto.STATE_FINISH);
            } else {
                tmpWorkflowAuditInfoDto.setStateName("????????????");
                tmpWorkflowAuditInfoDto.setState(WorkflowAuditInfoDto.STATE_NO);
            }
            Long time = activityInstance.getDurationInMillis() == null ? new Date().getTime() - activityInstance.getStartTime().getTime()
                    : activityInstance.getDurationInMillis();
            tmpWorkflowAuditInfoDto.setDuration(getCostTime(time));

            //?????????????????????????????????
            List<Comment> comments = taskService.getTaskComments(activityInstance.getTaskId());
            String msg = "";
            if (comments != null && comments.size() > 0) {
                for (Comment comment : comments) {
                    msg += (comment.getFullMessage() + "/");
                }
            }
            msg = msg.endsWith("/") ? msg.substring(0, msg.length() - 1) : msg;
            tmpWorkflowAuditInfoDto.setUserId(activityInstance.getAssignee());
            tmpWorkflowAuditInfoDto.setMessage(msg);
            workflowAuditInfoDtos.add(tmpWorkflowAuditInfoDto);
        }
        return workflowAuditInfoDtos;
    }

    @Override
    public WorkflowModelDto createModel(@RequestBody  WorkflowModelDto workflowModelDto) {
        logger.info("??????????????????name???{},key:{}", workflowModelDto.getName(), workflowModelDto.getKey());
        Model model = repositoryService.newModel();
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, workflowModelDto.getName());
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, "");
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        model.setName(workflowModelDto.getName());
        model.setKey(workflowModelDto.getKey());
        model.setMetaInfo(modelNode.toString());
        repositoryService.saveModel(model);
        createObjectNode(model.getId());
        logger.info("?????????????????????????????????ID???{}", model.getId());
        workflowModelDto.setModelId(model.getId());
        return workflowModelDto;
    }


    /**
     * ?????????????????????ModelEditorSource
     *
     * @param modelId
     */
    @SuppressWarnings("deprecation")
    private void createObjectNode(String modelId) {
        logger.info("??????????????????ModelEditorSource????????????ID???{}", modelId);
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        try {
            repositoryService.addModelEditorSource(modelId, editorNode.toString().getBytes("utf-8"));
        } catch (Exception e) {
            logger.info("?????????????????????ModelEditorSource???????????????{}", e);
        }
        logger.info("??????????????????ModelEditorSource??????");
    }



    /**
     * @param processInstanceId
     * @return
     * @CreateUser:xxxx
     * @ReturnType:List
     * @CreateDate:2014-6-25??????5:03:13
     * @UseFor :??? ACT_HI_ACTINST ?????????????????????????????????userTask?????? ?????????startEvent
     * <p>
     * .finished()
     */
    private List<HistoricActivityInstance> getHisUserTaskActivityInstanceList(
            String processInstanceId) {
        List<HistoricActivityInstance> hisActivityInstanceList = ((HistoricActivityInstanceQuery) historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).activityType("userTask")
                .orderByHistoricActivityInstanceStartTime().asc())
                .list();
        return hisActivityInstanceList;
    }

    public IWorkflowServiceDao getWorkflowServiceDaoImpl() {
        return workflowServiceDaoImpl;
    }

    public void setWorkflowServiceDaoImpl(IWorkflowServiceDao workflowServiceDaoImpl) {
        this.workflowServiceDaoImpl = workflowServiceDaoImpl;
    }

    public IUserInnerServiceSMO getUserInnerServiceSMOImpl() {
        return userInnerServiceSMOImpl;
    }

    public void setUserInnerServiceSMOImpl(IUserInnerServiceSMO userInnerServiceSMOImpl) {
        this.userInnerServiceSMOImpl = userInnerServiceSMOImpl;
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
