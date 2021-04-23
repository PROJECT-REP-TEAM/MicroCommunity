package com.java110.common.listener.attendanceClassesAttr;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.common.dao.IAttendanceClassesAttrServiceDao;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.Business;
import com.java110.po.attendanceClassesAttr.AttendanceClassesAttrPo;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 修改考勤班组属性信息 侦听
 * <p>
 * 处理节点
 * 1、businessAttendanceClassesAttr:{} 考勤班组属性基本信息节点
 * 2、businessAttendanceClassesAttrAttr:[{}] 考勤班组属性属性信息节点
 * 3、businessAttendanceClassesAttrPhoto:[{}] 考勤班组属性照片信息节点
 * 4、businessAttendanceClassesAttrCerdentials:[{}] 考勤班组属性证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E4%BF%AE%E6%94%B9%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("updateAttendanceClassesAttrInfoListener")
@Transactional
public class UpdateAttendanceClassesAttrInfoListener extends AbstractAttendanceClassesAttrBusinessServiceDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(UpdateAttendanceClassesAttrInfoListener.class);
    @Autowired
    private IAttendanceClassesAttrServiceDao attendanceClassesAttrServiceDaoImpl;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_UPDATE_ATTENDANCE_CLASSES_ATTR;
    }

    /**
     * business过程
     *
     * @param dataFlowContext 上下文对象
     * @param business        业务对象
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {

        JSONObject data = business.getDatas();

        Assert.notEmpty(data, "没有datas 节点，或没有子节点需要处理");


        //处理 businessAttendanceClassesAttr 节点
        if (data.containsKey(AttendanceClassesAttrPo.class.getSimpleName())) {
            Object _obj = data.get(AttendanceClassesAttrPo.class.getSimpleName());
            JSONArray businessAttendanceClassesAttrs = null;
            if (_obj instanceof JSONObject) {
                businessAttendanceClassesAttrs = new JSONArray();
                businessAttendanceClassesAttrs.add(_obj);
            } else {
                businessAttendanceClassesAttrs = (JSONArray) _obj;
            }
            //JSONObject businessAttendanceClassesAttr = data.getJSONObject(AttendanceClassesAttrPo.class.getSimpleName());
            for (int _attendanceClassesAttrIndex = 0; _attendanceClassesAttrIndex < businessAttendanceClassesAttrs.size(); _attendanceClassesAttrIndex++) {
                JSONObject businessAttendanceClassesAttr = businessAttendanceClassesAttrs.getJSONObject(_attendanceClassesAttrIndex);
                doBusinessAttendanceClassesAttr(business, businessAttendanceClassesAttr);
                if (_obj instanceof JSONObject) {
                    dataFlowContext.addParamOut("attrId", businessAttendanceClassesAttr.getString("attrId"));
                }
            }
        }
    }


    /**
     * business to instance 过程
     *
     * @param dataFlowContext 数据对象
     * @param business        当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {

        JSONObject data = business.getDatas();

        Map info = new HashMap();
        info.put("bId", business.getbId());
        info.put("operate", StatusConstant.OPERATE_ADD);

        //考勤班组属性信息
        List<Map> businessAttendanceClassesAttrInfos = attendanceClassesAttrServiceDaoImpl.getBusinessAttendanceClassesAttrInfo(info);
        if (businessAttendanceClassesAttrInfos != null && businessAttendanceClassesAttrInfos.size() > 0) {
            for (int _attendanceClassesAttrIndex = 0; _attendanceClassesAttrIndex < businessAttendanceClassesAttrInfos.size(); _attendanceClassesAttrIndex++) {
                Map businessAttendanceClassesAttrInfo = businessAttendanceClassesAttrInfos.get(_attendanceClassesAttrIndex);
                flushBusinessAttendanceClassesAttrInfo(businessAttendanceClassesAttrInfo, StatusConstant.STATUS_CD_VALID);
                attendanceClassesAttrServiceDaoImpl.updateAttendanceClassesAttrInfoInstance(businessAttendanceClassesAttrInfo);
                if (businessAttendanceClassesAttrInfo.size() == 1) {
                    dataFlowContext.addParamOut("attrId", businessAttendanceClassesAttrInfo.get("attr_id"));
                }
            }
        }

    }

    /**
     * 撤单
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
        info.put("statusCd", StatusConstant.STATUS_CD_VALID);
        Map delInfo = new HashMap();
        delInfo.put("bId", business.getbId());
        delInfo.put("operate", StatusConstant.OPERATE_DEL);
        //考勤班组属性信息
        List<Map> attendanceClassesAttrInfo = attendanceClassesAttrServiceDaoImpl.getAttendanceClassesAttrInfo(info);
        if (attendanceClassesAttrInfo != null && attendanceClassesAttrInfo.size() > 0) {

            //考勤班组属性信息
            List<Map> businessAttendanceClassesAttrInfos = attendanceClassesAttrServiceDaoImpl.getBusinessAttendanceClassesAttrInfo(delInfo);
            //除非程序出错了，这里不会为空
            if (businessAttendanceClassesAttrInfos == null || businessAttendanceClassesAttrInfos.size() == 0) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR, "撤单失败（attendanceClassesAttr），程序内部异常,请检查！ " + delInfo);
            }
            for (int _attendanceClassesAttrIndex = 0; _attendanceClassesAttrIndex < businessAttendanceClassesAttrInfos.size(); _attendanceClassesAttrIndex++) {
                Map businessAttendanceClassesAttrInfo = businessAttendanceClassesAttrInfos.get(_attendanceClassesAttrIndex);
                flushBusinessAttendanceClassesAttrInfo(businessAttendanceClassesAttrInfo, StatusConstant.STATUS_CD_VALID);
                attendanceClassesAttrServiceDaoImpl.updateAttendanceClassesAttrInfoInstance(businessAttendanceClassesAttrInfo);
            }
        }

    }


    /**
     * 处理 businessAttendanceClassesAttr 节点
     *
     * @param business                      总的数据节点
     * @param businessAttendanceClassesAttr 考勤班组属性节点
     */
    private void doBusinessAttendanceClassesAttr(Business business, JSONObject businessAttendanceClassesAttr) {

        Assert.jsonObjectHaveKey(businessAttendanceClassesAttr, "attrId", "businessAttendanceClassesAttr 节点下没有包含 attrId 节点");

        if (businessAttendanceClassesAttr.getString("attrId").startsWith("-")) {
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR, "attrId 错误，不能自动生成（必须已经存在的attrId）" + businessAttendanceClassesAttr);
        }
        //自动保存DEL
        autoSaveDelBusinessAttendanceClassesAttr(business, businessAttendanceClassesAttr);

        businessAttendanceClassesAttr.put("bId", business.getbId());
        businessAttendanceClassesAttr.put("operate", StatusConstant.OPERATE_ADD);
        //保存考勤班组属性信息
        attendanceClassesAttrServiceDaoImpl.saveBusinessAttendanceClassesAttrInfo(businessAttendanceClassesAttr);

    }


    @Override
    public IAttendanceClassesAttrServiceDao getAttendanceClassesAttrServiceDaoImpl() {
        return attendanceClassesAttrServiceDaoImpl;
    }

    public void setAttendanceClassesAttrServiceDaoImpl(IAttendanceClassesAttrServiceDao attendanceClassesAttrServiceDaoImpl) {
        this.attendanceClassesAttrServiceDaoImpl = attendanceClassesAttrServiceDaoImpl;
    }


}
