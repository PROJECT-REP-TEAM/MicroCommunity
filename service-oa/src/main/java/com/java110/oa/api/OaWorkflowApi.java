package com.java110.oa.api;

import com.alibaba.fastjson.JSONObject;
import com.java110.dto.oaWorkflow.OaWorkflowDto;
import com.java110.dto.oaWorkflowData.OaWorkflowDataDto;
import com.java110.dto.oaWorkflowForm.OaWorkflowFormDto;
import com.java110.dto.oaWorkflowXml.OaWorkflowXmlDto;
import com.java110.oa.bmo.oaWorkflow.IDeleteOaWorkflowBMO;
import com.java110.oa.bmo.oaWorkflow.IGetOaWorkflowBMO;
import com.java110.oa.bmo.oaWorkflow.ISaveOaWorkflowBMO;
import com.java110.oa.bmo.oaWorkflow.IUpdateOaWorkflowBMO;
import com.java110.oa.bmo.oaWorkflowData.IDeleteOaWorkflowDataBMO;
import com.java110.oa.bmo.oaWorkflowData.IGetOaWorkflowDataBMO;
import com.java110.oa.bmo.oaWorkflowData.ISaveOaWorkflowDataBMO;
import com.java110.oa.bmo.oaWorkflowData.IUpdateOaWorkflowDataBMO;
import com.java110.oa.bmo.oaWorkflowForm.IDeleteOaWorkflowFormBMO;
import com.java110.oa.bmo.oaWorkflowForm.IGetOaWorkflowFormBMO;
import com.java110.oa.bmo.oaWorkflowForm.ISaveOaWorkflowFormBMO;
import com.java110.oa.bmo.oaWorkflowForm.IUpdateOaWorkflowFormBMO;
import com.java110.oa.bmo.oaWorkflowXml.IDeleteOaWorkflowXmlBMO;
import com.java110.oa.bmo.oaWorkflowXml.IGetOaWorkflowXmlBMO;
import com.java110.oa.bmo.oaWorkflowXml.ISaveOaWorkflowXmlBMO;
import com.java110.oa.bmo.oaWorkflowXml.IUpdateOaWorkflowXmlBMO;
import com.java110.po.oaWorkflow.OaWorkflowPo;
import com.java110.po.oaWorkflowData.OaWorkflowDataPo;
import com.java110.po.oaWorkflowForm.OaWorkflowFormPo;
import com.java110.po.oaWorkflowXml.OaWorkflowXmlPo;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/oaWorkflow")
public class OaWorkflowApi {

    @Autowired
    private ISaveOaWorkflowBMO saveOaWorkflowBMOImpl;
    @Autowired
    private IUpdateOaWorkflowBMO updateOaWorkflowBMOImpl;
    @Autowired
    private IDeleteOaWorkflowBMO deleteOaWorkflowBMOImpl;

    @Autowired
    private IGetOaWorkflowBMO getOaWorkflowBMOImpl;

    @Autowired
    private ISaveOaWorkflowXmlBMO saveOaWorkflowXmlBMOImpl;
    @Autowired
    private IUpdateOaWorkflowXmlBMO updateOaWorkflowXmlBMOImpl;
    @Autowired
    private IDeleteOaWorkflowXmlBMO deleteOaWorkflowXmlBMOImpl;

    @Autowired
    private IGetOaWorkflowXmlBMO getOaWorkflowXmlBMOImpl;

    @Autowired
    private ISaveOaWorkflowFormBMO saveOaWorkflowFormBMOImpl;
    @Autowired
    private IUpdateOaWorkflowFormBMO updateOaWorkflowFormBMOImpl;
    @Autowired
    private IDeleteOaWorkflowFormBMO deleteOaWorkflowFormBMOImpl;

    @Autowired
    private IGetOaWorkflowFormBMO getOaWorkflowFormBMOImpl;


    @Autowired
    private ISaveOaWorkflowDataBMO saveOaWorkflowDataBMOImpl;
    @Autowired
    private IUpdateOaWorkflowDataBMO updateOaWorkflowDataBMOImpl;
    @Autowired
    private IDeleteOaWorkflowDataBMO deleteOaWorkflowDataBMOImpl;

    @Autowired
    private IGetOaWorkflowDataBMO getOaWorkflowDataBMOImpl;

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/saveOaWorkflow
     * @path /app/oaWorkflow/saveOaWorkflow
     */
    @RequestMapping(value = "/saveOaWorkflow", method = RequestMethod.POST)
    public ResponseEntity<String> saveOaWorkflow(@RequestHeader(value = "store-id") String storeId,
                                                 @RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "flowName", "????????????????????????flowName");
        Assert.hasKeyAndValue(reqJson, "flowType", "????????????????????????flowType");


        OaWorkflowPo oaWorkflowPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowPo.class);
        oaWorkflowPo.setStoreId(storeId);
        return saveOaWorkflowBMOImpl.save(oaWorkflowPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/updateOaWorkflow
     * @path /app/oaWorkflow/updateOaWorkflow
     */
    @RequestMapping(value = "/updateOaWorkflow", method = RequestMethod.POST)
    public ResponseEntity<String> updateOaWorkflow(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "flowName", "????????????????????????flowName");
        Assert.hasKeyAndValue(reqJson, "flowType", "????????????????????????flowType");
        Assert.hasKeyAndValue(reqJson, "flowId", "flowId????????????");


        OaWorkflowPo oaWorkflowPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowPo.class);
        return updateOaWorkflowBMOImpl.update(oaWorkflowPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/deleteOaWorkflow
     * @path /app/oaWorkflow/deleteOaWorkflow
     */
    @RequestMapping(value = "/deleteOaWorkflow", method = RequestMethod.POST)
    public ResponseEntity<String> deleteOaWorkflow(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "flowId", "flowId????????????");


        OaWorkflowPo oaWorkflowPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowPo.class);
        return deleteOaWorkflowBMOImpl.delete(oaWorkflowPo);
    }

    /**
     * ????????????????????????
     *
     * @param storeId ??????ID
     * @return
     * @serviceCode /oaWorkflow/queryOaWorkflow
     * @path /app/oaWorkflow/queryOaWorkflow
     */
    @RequestMapping(value = "/queryOaWorkflow", method = RequestMethod.GET)
    public ResponseEntity<String> queryOaWorkflow(@RequestHeader(value = "store-id") String storeId,
                                                  @RequestHeader(value = "user-id") String userId,
                                                  @RequestParam(value = "flowId", required = false) String flowId,
                                                  @RequestParam(value = "flowName", required = false) String flowName,
                                                  @RequestParam(value = "flowType", required = false) String flowType,
                                                  @RequestParam(value = "state", required = false) String state,
                                                  @RequestParam(value = "page") int page,
                                                  @RequestParam(value = "row") int row) {
        OaWorkflowDto oaWorkflowDto = new OaWorkflowDto();
        oaWorkflowDto.setPage(page);
        oaWorkflowDto.setRow(row);
        oaWorkflowDto.setStoreId(storeId);
        oaWorkflowDto.setFlowId(flowId);
        oaWorkflowDto.setFlowName(flowName);
        oaWorkflowDto.setFlowType(flowType);
        oaWorkflowDto.setState(state);
        oaWorkflowDto.setUserId(userId);
        return getOaWorkflowBMOImpl.get(oaWorkflowDto);
    }


    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/saveOaWorkflowXml
     * @path /app/oaWorkflow/saveOaWorkflowXml
     */
    @RequestMapping(value = "/saveOaWorkflowXml", method = RequestMethod.POST)
    public ResponseEntity<String> saveOaWorkflowXml(@RequestHeader(value = "store-id") String storeId,
                                                    @RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "flowId", "????????????????????????flowId");
        Assert.hasKeyAndValue(reqJson, "bpmnXml", "????????????????????????bpmnXml");


        OaWorkflowXmlPo oaWorkflowXmlPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowXmlPo.class);
        oaWorkflowXmlPo.setStoreId(storeId);
        return saveOaWorkflowXmlBMOImpl.save(oaWorkflowXmlPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/updateOaWorkflowXml
     * @path /app/oaWorkflow/updateOaWorkflowXml
     */
    @RequestMapping(value = "/updateOaWorkflowXml", method = RequestMethod.POST)
    public ResponseEntity<String> updateOaWorkflowXml(@RequestHeader(value = "store-id") String storeId,
                                                      @RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "flowId", "????????????????????????flowId");
        Assert.hasKeyAndValue(reqJson, "bpmnXml", "????????????????????????bpmnXml");
        Assert.hasKeyAndValue(reqJson, "xmlId", "xmlId????????????");


        OaWorkflowXmlPo oaWorkflowXmlPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowXmlPo.class);
        oaWorkflowXmlPo.setStoreId(storeId);
        return updateOaWorkflowXmlBMOImpl.update(oaWorkflowXmlPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/deleteOaWorkflowXml
     * @path /app/oaWorkflow/deleteOaWorkflowXml
     */
    @RequestMapping(value = "/deleteOaWorkflowXml", method = RequestMethod.POST)
    public ResponseEntity<String> deleteOaWorkflowXml(@RequestHeader(value = "store-id") String storeId,
                                                      @RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "xmlId", "xmlId????????????");


        OaWorkflowXmlPo oaWorkflowXmlPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowXmlPo.class);
        oaWorkflowXmlPo.setStoreId(storeId);
        return deleteOaWorkflowXmlBMOImpl.delete(oaWorkflowXmlPo);
    }

    /**
     * ????????????????????????
     *
     * @param storeId ??????ID
     * @return
     * @serviceCode /oaWorkflow/queryOaWorkflowXml
     * @path /app/oaWorkflow/queryOaWorkflowXml
     */
    @RequestMapping(value = "/queryOaWorkflowXml", method = RequestMethod.GET)
    public ResponseEntity<String> queryOaWorkflowXml(@RequestHeader(value = "store-id") String storeId,
                                                     @RequestParam(value = "flowId") String flowId,
                                                     @RequestParam(value = "page") int page,
                                                     @RequestParam(value = "row") int row) {
        OaWorkflowXmlDto oaWorkflowXmlDto = new OaWorkflowXmlDto();
        oaWorkflowXmlDto.setPage(page);
        oaWorkflowXmlDto.setRow(row);
        oaWorkflowXmlDto.setStoreId(storeId);
        oaWorkflowXmlDto.setFlowId(flowId);
        return getOaWorkflowXmlBMOImpl.get(oaWorkflowXmlDto);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/saveOaWorkflowForm
     * @path /app/oaWorkflow/saveOaWorkflowForm
     */
    @RequestMapping(value = "/saveOaWorkflowForm", method = RequestMethod.POST)
    public ResponseEntity<String> saveOaWorkflowForm(@RequestHeader(value = "store-id") String storeId,
                                                     @RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "flowId", "????????????????????????flowId");
        Assert.hasKeyAndValue(reqJson, "formJson", "????????????????????????formJson");


        OaWorkflowFormPo oaWorkflowFormPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowFormPo.class);
        oaWorkflowFormPo.setStoreId(storeId);
        return saveOaWorkflowFormBMOImpl.save(oaWorkflowFormPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/updateOaWorkflowForm
     * @path /app/oaWorkflow/updateOaWorkflowForm
     */
    @RequestMapping(value = "/updateOaWorkflowForm", method = RequestMethod.POST)
    public ResponseEntity<String> updateOaWorkflowForm(@RequestHeader(value = "store-id") String storeId,
                                                       @RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "flowId", "????????????????????????flowId");
        Assert.hasKeyAndValue(reqJson, "formJson", "????????????????????????formJson");
        Assert.hasKeyAndValue(reqJson, "formId", "formId????????????");

        OaWorkflowFormPo oaWorkflowFormPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowFormPo.class);
        oaWorkflowFormPo.setStoreId(storeId);
        return updateOaWorkflowFormBMOImpl.update(oaWorkflowFormPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/deleteOaWorkflowForm
     * @path /app/oaWorkflow/deleteOaWorkflowForm
     */
    @RequestMapping(value = "/deleteOaWorkflowForm", method = RequestMethod.POST)
    public ResponseEntity<String> deleteOaWorkflowForm(@RequestHeader(value = "store-id") String storeId,
                                                       @RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "xmlId", "xmlId????????????");


        OaWorkflowFormPo oaWorkflowFormPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowFormPo.class);
        oaWorkflowFormPo.setStoreId(storeId);
        return deleteOaWorkflowFormBMOImpl.delete(oaWorkflowFormPo);
    }

    /**
     * ????????????????????????
     *
     * @param storeId ??????ID
     * @return
     * @serviceCode /oaWorkflow/queryOaWorkflowForm
     * @path /app/oaWorkflow/queryOaWorkflowForm
     */
    @RequestMapping(value = "/queryOaWorkflowForm", method = RequestMethod.GET)
    public ResponseEntity<String> queryOaWorkflowForm(@RequestHeader(value = "store-id") String storeId,
                                                      @RequestParam(value = "flowId", required = false) String flowId,
                                                      @RequestParam(value = "page") int page,
                                                      @RequestParam(value = "row") int row) {
        OaWorkflowFormDto oaWorkflowFormDto = new OaWorkflowFormDto();
        oaWorkflowFormDto.setPage(page);
        oaWorkflowFormDto.setRow(row);
        oaWorkflowFormDto.setStoreId(storeId);
        oaWorkflowFormDto.setFlowId(flowId);
        return getOaWorkflowFormBMOImpl.get(oaWorkflowFormDto);
    }

    /**
     * ????????????????????????
     *
     * @param storeId ??????ID
     * @return
     * @serviceCode /oaWorkflow/queryOaWorkflowFormData
     * @path /app/oaWorkflow/queryOaWorkflowFormData
     */
    @RequestMapping(value = "/queryOaWorkflowFormData", method = RequestMethod.GET)
    public ResponseEntity<String> queryOaWorkflowFormData(@RequestHeader(value = "store-id") String storeId,
                                                          @RequestParam(value = "flowId", required = false) String flowId,
                                                          @RequestParam(value = "id", required = false) String id,
                                                          @RequestParam(value = "startTime", required = false) String startTime,
                                                          @RequestParam(value = "endTime", required = false) String endTime,
                                                          @RequestParam(value = "createUserName", required = false) String createUserName,
                                                          @RequestParam(value = "page") int page,
                                                          @RequestParam(value = "row") int row) {
        Map paramIn = new HashMap();
        paramIn.put("page", page);
        paramIn.put("row", row);
        paramIn.put("createUserName", createUserName);
        paramIn.put("endTime", endTime);
        paramIn.put("startTime", startTime);
        paramIn.put("flowId", flowId);
        paramIn.put("storeId", storeId);
        paramIn.put("id", id);
        return getOaWorkflowFormBMOImpl.queryOaWorkflowFormData(paramIn);
    }

    /**
     * ??????????????????
     *
     * @param storeId ??????ID
     * @return
     * @serviceCode /oaWorkflow/saveOaWorkflowFormData
     * @path /app/oaWorkflow/saveOaWorkflowFormData
     */
    @RequestMapping(value = "/saveOaWorkflowFormData", method = RequestMethod.POST)
    public ResponseEntity<String> saveOaWorkflowFormData(@RequestHeader(value = "store-id") String storeId,
                                                         @RequestHeader(value = "user-id") String userId,
                                                         @RequestBody JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "flowId", "flowId????????????");
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (String key : reqJson.keySet()) {
            if ("flowId".equals(key)) {
                continue;
            }
            if ("fileName".equals(key)) {
                continue;
            }

            if ("realFileName".equals(key)) {
                continue;
            }

            columns.add(key);
            values.add(reqJson.getString(key));
        }
        reqJson.put("columns", columns.toArray(new String[columns.size()]));
        reqJson.put("values", values.toArray(new String[values.size()]));
        reqJson.put("storeId", storeId);
        reqJson.put("userId", userId);
        return getOaWorkflowFormBMOImpl.saveOaWorkflowFormData(reqJson);
    }

    /**
     * ????????????
     *
     * @param storeId ??????ID
     * @return
     * @serviceCode /oaWorkflow/queryOaWorkflowUserTaskFormData
     * @path /app/oaWorkflow/queryOaWorkflowUserTaskFormData
     */
    @RequestMapping(value = "/queryOaWorkflowUserTaskFormData", method = RequestMethod.GET)
    public ResponseEntity<String> queryOaWorkflowUserTaskFormData(@RequestHeader(value = "store-id") String storeId,
                                                                  @RequestHeader(value = "user-id") String userId,
                                                                  @RequestParam(value = "flowId", required = false) String flowId,
                                                                  @RequestParam(value = "startTime", required = false) String startTime,
                                                                  @RequestParam(value = "endTime", required = false) String endTime,
                                                                  @RequestParam(value = "createUserName", required = false) String createUserName,
                                                                  @RequestParam(value = "page") int page,
                                                                  @RequestParam(value = "row") int row) {
        JSONObject paramIn = new JSONObject();
        paramIn.put("page", page);
        paramIn.put("row", row);
        paramIn.put("createUserName", createUserName);
        paramIn.put("endTime", endTime);
        paramIn.put("startTime", startTime);
        paramIn.put("flowId", flowId);
        paramIn.put("storeId", storeId);
        paramIn.put("userId", userId);
        return getOaWorkflowFormBMOImpl.queryOaWorkflowUserTaskFormData(paramIn);
    }

    /**
     * ????????????
     *
     * @param storeId ??????ID
     * @return
     * @serviceCode /oaWorkflow/queryOaWorkflowUserHisTaskFormData
     * @path /app/oaWorkflow/queryOaWorkflowUserHisTaskFormData
     */
    @RequestMapping(value = "/queryOaWorkflowUserHisTaskFormData", method = RequestMethod.GET)
    public ResponseEntity<String> queryOaWorkflowUserHisTaskFormData(@RequestHeader(value = "store-id") String storeId,
                                                                     @RequestHeader(value = "user-id") String userId,
                                                                     @RequestParam(value = "flowId", required = false) String flowId,
                                                                     @RequestParam(value = "startTime", required = false) String startTime,
                                                                     @RequestParam(value = "endTime", required = false) String endTime,
                                                                     @RequestParam(value = "createUserName", required = false) String createUserName,
                                                                     @RequestParam(value = "page") int page,
                                                                     @RequestParam(value = "row") int row) {
        JSONObject paramIn = new JSONObject();
        paramIn.put("page", page);
        paramIn.put("row", row);
        paramIn.put("createUserName", createUserName);
        paramIn.put("endTime", endTime);
        paramIn.put("startTime", startTime);
        paramIn.put("flowId", flowId);
        paramIn.put("storeId", storeId);
        paramIn.put("userId", userId);
        return getOaWorkflowFormBMOImpl.queryOaWorkflowUserHisTaskFormData(paramIn);
    }

    /**
     * ????????????
     *
     * @param storeId ??????ID
     * @return
     * @serviceCode /oaWorkflow/queryOaWorkflowUser
     * @path /app/oaWorkflow/queryOaWorkflowUser
     */
    @RequestMapping(value = "/queryOaWorkflowUser", method = RequestMethod.GET)
    public ResponseEntity<String> queryOaWorkflowUser(@RequestHeader(value = "store-id") String storeId,
                                                      @RequestHeader(value = "user-id") String userId,
                                                      @RequestParam(value = "flowId", required = false) String flowId,
                                                      @RequestParam(value = "id", required = false) String id,
                                                      @RequestParam(value = "page") int page,
                                                      @RequestParam(value = "row") int row) {
        JSONObject paramIn = new JSONObject();
        paramIn.put("page", page);
        paramIn.put("row", row);
        paramIn.put("flowId", flowId);
        paramIn.put("id", id);
        paramIn.put("storeId", storeId);
        paramIn.put("userId", userId);
        return getOaWorkflowFormBMOImpl.queryOaWorkflowUser(paramIn);
    }

    /**
     * ????????????
     *
     * @param storeId ??????ID
     * @return
     * @serviceCode /oaWorkflow/auditOaWorkflow
     * @path /app/oaWorkflow/auditOaWorkflow
     */
    @RequestMapping(value = "/auditOaWorkflow", method = RequestMethod.POST)
    public ResponseEntity<String> auditOaWorkflow(@RequestHeader(value = "store-id") String storeId,
                                                  @RequestHeader(value = "user-id") String userId,
                                                  @RequestBody JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "flowId", "flowId????????????");
        Assert.hasKeyAndValue(reqJson, "id", "id????????????");
        Assert.hasKeyAndValue(reqJson, "taskId", "??????????????????");
        Assert.hasKeyAndValue(reqJson, "auditMessage", "????????????????????????");
        Assert.hasKeyAndValue(reqJson, "auditCode", "????????????????????????");
        reqJson.put("storeId", storeId);
        reqJson.put("curUserId", userId);
        return getOaWorkflowFormBMOImpl.auditOaWorkflow(reqJson);
    }

    /**
     * ????????????
     *
     * @param storeId ??????ID
     * @return
     * @serviceCode /oaWorkflow/getNextTask
     * @path /app/oaWorkflow/getNextTask
     */
    @RequestMapping(value = "/getNextTask", method = RequestMethod.GET)
    public ResponseEntity<String> getNextTask(@RequestHeader(value = "store-id") String storeId,
                                              @RequestHeader(value = "user-id") String userId,
                                              @RequestParam(value = "flowId") String flowId,
                                              @RequestParam(value = "id") String id,
                                              @RequestParam(value = "taskId") String taskId) {
        JSONObject reqJson = new JSONObject();
        reqJson.put("storeId", storeId);
        reqJson.put("taskId", taskId);
        reqJson.put("userId", userId);
        reqJson.put("flowId", flowId);
        reqJson.put("id", id);
        return getOaWorkflowFormBMOImpl.getNextTask(reqJson);
    }


    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/saveOaWorkflowData
     * @path /app/oaWorkflow/saveOaWorkflowData
     */
    @RequestMapping(value = "/saveOaWorkflowData", method = RequestMethod.POST)
    public ResponseEntity<String> saveOaWorkflowData(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "businessKey", "????????????????????????businessKey");
        Assert.hasKeyAndValue(reqJson, "context", "????????????????????????context");


        OaWorkflowDataPo oaWorkflowDataPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowDataPo.class);
        return saveOaWorkflowDataBMOImpl.save(oaWorkflowDataPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/updateOaWorkflowData
     * @path /app/oaWorkflow/updateOaWorkflowData
     */
    @RequestMapping(value = "/updateOaWorkflowData", method = RequestMethod.POST)
    public ResponseEntity<String> updateOaWorkflowData(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "businessKey", "????????????????????????businessKey");
        Assert.hasKeyAndValue(reqJson, "context", "????????????????????????context");
        Assert.hasKeyAndValue(reqJson, "dataId", "dataId????????????");


        OaWorkflowDataPo oaWorkflowDataPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowDataPo.class);
        return updateOaWorkflowDataBMOImpl.update(oaWorkflowDataPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /oaWorkflow/deleteOaWorkflowData
     * @path /app/oaWorkflow/deleteOaWorkflowData
     */
    @RequestMapping(value = "/deleteOaWorkflowData", method = RequestMethod.POST)
    public ResponseEntity<String> deleteOaWorkflowData(@RequestBody JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "communityId", "??????ID????????????");

        Assert.hasKeyAndValue(reqJson, "dataId", "dataId????????????");


        OaWorkflowDataPo oaWorkflowDataPo = BeanConvertUtil.covertBean(reqJson, OaWorkflowDataPo.class);
        return deleteOaWorkflowDataBMOImpl.delete(oaWorkflowDataPo);
    }

    /**
     * ????????????????????????
     *
     * @param storeId ??????ID
     * @return
     * @serviceCode /oaWorkflow/queryOaWorkflowData
     * @path /app/oaWorkflow/queryOaWorkflowData
     */
    @RequestMapping(value = "/queryOaWorkflowData", method = RequestMethod.GET)
    public ResponseEntity<String> queryOaWorkflowData(@RequestHeader(value = "store-id") String storeId,
                                                      @RequestParam(value = "page") int page,
                                                      @RequestParam(value = "businessKey") String businessKey,
                                                      @RequestParam(value = "row") int row) {
        OaWorkflowDataDto oaWorkflowDataDto = new OaWorkflowDataDto();
        oaWorkflowDataDto.setPage(page);
        oaWorkflowDataDto.setRow(row);
        oaWorkflowDataDto.setStoreId(storeId);
        oaWorkflowDataDto.setBusinessKey(businessKey);
        return getOaWorkflowDataBMOImpl.get(oaWorkflowDataDto);
    }
}
