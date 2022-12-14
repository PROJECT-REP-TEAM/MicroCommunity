package com.java110.community.listener.inspectionTaskDetail;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.community.dao.IInspectionTaskDetailServiceDao;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.Business;
import com.java110.po.inspection.InspectionTaskDetailPo;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 删除巡检任务明细信息 侦听
 * <p>
 * 处理节点
 * 1、businessInspectionTaskDetail:{} 巡检任务明细基本信息节点
 * 2、businessInspectionTaskDetailAttr:[{}] 巡检任务明细属性信息节点
 * 3、businessInspectionTaskDetailPhoto:[{}] 巡检任务明细照片信息节点
 * 4、businessInspectionTaskDetailCerdentials:[{}] 巡检任务明细证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E5%88%A0%E9%99%A4%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("deleteInspectionTaskDetailInfoListener")
@Transactional
public class DeleteInspectionTaskDetailInfoListener extends AbstractInspectionTaskDetailBusinessServiceDataFlowListener {

    private final static Logger logger = LoggerFactory.getLogger(DeleteInspectionTaskDetailInfoListener.class);
    @Autowired
    IInspectionTaskDetailServiceDao inspectionTaskDetailServiceDaoImpl;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_DELETE_INSPECTION_TASK_DETAIL;
    }

    /**
     * 根据删除信息 查出Instance表中数据 保存至business表 （状态写DEL） 方便撤单时直接更新回去
     *
     * @param dataFlowContext 数据对象
     * @param business        当前业务对象
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {
        JSONObject data = business.getDatas();

        Assert.notEmpty(data, "没有datas 节点，或没有子节点需要处理");

        //处理 businessInspectionTaskDetail 节点
        if (data.containsKey(InspectionTaskDetailPo.class.getSimpleName())) {
            Object _obj = data.get(InspectionTaskDetailPo.class.getSimpleName());
            JSONArray businessInspectionTaskDetails = null;
            if (_obj instanceof JSONObject) {
                businessInspectionTaskDetails = new JSONArray();
                businessInspectionTaskDetails.add(_obj);
            } else {
                businessInspectionTaskDetails = (JSONArray) _obj;
            }
            //JSONObject businessInspectionTaskDetail = data.getJSONObject("businessInspectionTaskDetail");
            for (int _inspectionTaskDetailIndex = 0; _inspectionTaskDetailIndex < businessInspectionTaskDetails.size(); _inspectionTaskDetailIndex++) {
                JSONObject businessInspectionTaskDetail = businessInspectionTaskDetails.getJSONObject(_inspectionTaskDetailIndex);
                doBusinessInspectionTaskDetail(business, businessInspectionTaskDetail);
                if (_obj instanceof JSONObject) {
                    dataFlowContext.addParamOut("taskDetailId", businessInspectionTaskDetail.getString("taskDetailId"));
                }
            }

        }


    }

    /**
     * 删除 instance数据
     *
     * @param dataFlowContext 数据对象
     * @param business        当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {
        String bId = business.getbId();
        //Assert.hasLength(bId,"请求报文中没有包含 bId");

        //巡检任务明细信息
        Map info = new HashMap();
        info.put("bId", business.getbId());
        info.put("operate", StatusConstant.OPERATE_DEL);

        //巡检任务明细信息
        List<Map> businessInspectionTaskDetailInfos = inspectionTaskDetailServiceDaoImpl.getBusinessInspectionTaskDetailInfo(info);
        if (businessInspectionTaskDetailInfos != null && businessInspectionTaskDetailInfos.size() > 0) {
            for (int _inspectionTaskDetailIndex = 0; _inspectionTaskDetailIndex < businessInspectionTaskDetailInfos.size(); _inspectionTaskDetailIndex++) {
                Map businessInspectionTaskDetailInfo = businessInspectionTaskDetailInfos.get(_inspectionTaskDetailIndex);
                flushBusinessInspectionTaskDetailInfo(businessInspectionTaskDetailInfo, StatusConstant.STATUS_CD_INVALID);
                inspectionTaskDetailServiceDaoImpl.updateInspectionTaskDetailInfoInstance(businessInspectionTaskDetailInfo);
                dataFlowContext.addParamOut("taskDetailId", businessInspectionTaskDetailInfo.get("task_detail_id"));
            }
        }

    }

    /**
     * 撤单
     * 从business表中查询到DEL的数据 将instance中的数据更新回来
     *
     * @param dataFlowContext 数据对象
     * @param business        当前业务对象
     */
    @Override
    protected void doRecover(DataFlowContext dataFlowContext, Business business) {
        String bId = business.getbId();
        //Assert.hasLength(bId,"请求报文中没有包含 bId");
        Map info = new HashMap();
        info.put("bId", bId);
        info.put("statusCd", StatusConstant.STATUS_CD_INVALID);

        Map delInfo = new HashMap();
        delInfo.put("bId", business.getbId());
        delInfo.put("operate", StatusConstant.OPERATE_DEL);
        //巡检任务明细信息
        List<Map> inspectionTaskDetailInfo = inspectionTaskDetailServiceDaoImpl.getInspectionTaskDetailInfo(info);
        if (inspectionTaskDetailInfo != null && inspectionTaskDetailInfo.size() > 0) {

            //巡检任务明细信息
            List<Map> businessInspectionTaskDetailInfos = inspectionTaskDetailServiceDaoImpl.getBusinessInspectionTaskDetailInfo(delInfo);
            //除非程序出错了，这里不会为空
            if (businessInspectionTaskDetailInfos == null || businessInspectionTaskDetailInfos.size() == 0) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR, "撤单失败（inspectionTaskDetail），程序内部异常,请检查！ " + delInfo);
            }
            for (int _inspectionTaskDetailIndex = 0; _inspectionTaskDetailIndex < businessInspectionTaskDetailInfos.size(); _inspectionTaskDetailIndex++) {
                Map businessInspectionTaskDetailInfo = businessInspectionTaskDetailInfos.get(_inspectionTaskDetailIndex);
                flushBusinessInspectionTaskDetailInfo(businessInspectionTaskDetailInfo, StatusConstant.STATUS_CD_VALID);
                inspectionTaskDetailServiceDaoImpl.updateInspectionTaskDetailInfoInstance(businessInspectionTaskDetailInfo);
            }
        }
    }


    /**
     * 处理 businessInspectionTaskDetail 节点
     *
     * @param business                     总的数据节点
     * @param businessInspectionTaskDetail 巡检任务明细节点
     */
    private void doBusinessInspectionTaskDetail(Business business, JSONObject businessInspectionTaskDetail) {

        Assert.jsonObjectHaveKey(businessInspectionTaskDetail, "taskDetailId", "businessInspectionTaskDetail 节点下没有包含 taskDetailId 节点");

        if (businessInspectionTaskDetail.getString("taskDetailId").startsWith("-")) {
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR, "taskDetailId 错误，不能自动生成（必须已经存在的taskDetailId）" + businessInspectionTaskDetail);
        }
        //自动插入DEL
        autoSaveDelBusinessInspectionTaskDetail(business, businessInspectionTaskDetail);
    }

    public IInspectionTaskDetailServiceDao getInspectionTaskDetailServiceDaoImpl() {
        return inspectionTaskDetailServiceDaoImpl;
    }

    public void setInspectionTaskDetailServiceDaoImpl(IInspectionTaskDetailServiceDao inspectionTaskDetailServiceDaoImpl) {
        this.inspectionTaskDetailServiceDaoImpl = inspectionTaskDetailServiceDaoImpl;
    }
}
