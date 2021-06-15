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
package com.java110.job.adapt.hcIot.attendance;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.dto.attendanceClasses.AttendanceClassesDto;
import com.java110.dto.org.OrgStaffRelDto;
import com.java110.entity.order.Business;
import com.java110.intf.common.IAttendanceClassesInnerServiceSMO;
import com.java110.intf.user.IOrgStaffRelInnerServiceSMO;
import com.java110.job.adapt.DatabusAdaptImpl;
import com.java110.job.adapt.hcIot.asyn.IIotSendAsyn;
import com.java110.po.attendanceClasses.AttendanceClassesPo;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * HC iot 车辆同步适配器
 * <p>
 * 接口协议地址： https://gitee.com/java110/MicroCommunityThings/blob/master/back/docs/api.md
 *
 * @desc add by 吴学文 18:58
 */
@Component(value = "addAttendanceToIotAdapt")
public class AddAttendanceToIotAdapt extends DatabusAdaptImpl {

    @Autowired
    private IIotSendAsyn hcOwnerAttendanceAsynImpl;


    @Autowired
    private IAttendanceClassesInnerServiceSMO attendanceClassesInnerServiceSMOImpl;

    @Autowired
    private IOrgStaffRelInnerServiceSMO orgStaffRelInnerServiceSMOImpl;


    /**
     * accessToken={access_token}
     * &extCommunityUuid=01000
     * &extCommunityId=1
     * &devSn=111111111
     * &name=设备名称
     * &positionType=0
     * &positionUuid=1
     *
     * @param business   当前处理业务
     * @param businesses 所有业务信息
     */
    @Override
    public void execute(Business business, List<Business> businesses) {
        JSONObject data = business.getData();
        if (data.containsKey(AttendanceClassesPo.class.getSimpleName())) {
            Object bObj = data.get(AttendanceClassesPo.class.getSimpleName());
            JSONArray businessOwnerAttendances = null;
            if (bObj instanceof JSONObject) {
                businessOwnerAttendances = new JSONArray();
                businessOwnerAttendances.add(bObj);
            } else if (bObj instanceof List) {
                businessOwnerAttendances = JSONArray.parseArray(JSONObject.toJSONString(bObj));
            } else {
                businessOwnerAttendances = (JSONArray) bObj;
            }
            //JSONObject businessOwnerAttendance = data.getJSONObject("businessOwnerAttendance");
            for (int bOwnerAttendanceIndex = 0; bOwnerAttendanceIndex < businessOwnerAttendances.size(); bOwnerAttendanceIndex++) {
                JSONObject businessOwnerAttendance = businessOwnerAttendances.getJSONObject(bOwnerAttendanceIndex);
                doSendOwnerAttendance(business, businessOwnerAttendance);
            }
        }
    }

    private void doSendOwnerAttendance(Business business, JSONObject businessOwnerAttendance) {

        AttendanceClassesPo ownerAttendancePo = BeanConvertUtil.covertBean(businessOwnerAttendance, AttendanceClassesPo.class);

        AttendanceClassesDto ownerAttendanceDto = new AttendanceClassesDto();
        ownerAttendanceDto.setClassesId(ownerAttendancePo.getClassesId());
        ownerAttendanceDto.setStoreId(ownerAttendancePo.getStoreId());
        List<AttendanceClassesDto> attendanceDtos = attendanceClassesInnerServiceSMOImpl.queryAttendanceClassess(ownerAttendanceDto);

        Assert.listOnlyOne(attendanceDtos, "未找到考勤班组");

        //查询考勤组下的员工
        String classObjType = attendanceDtos.get(0).getClassesObjType();
        List<JSONObject> staffs = null;
        switch (classObjType) {
            case AttendanceClassesDto.CLASSES_OBJ_TYPE_PARTMENT:
                staffs = getPartmentStaff(attendanceDtos.get(0));
                break;
            default:
                staffs = new ArrayList<>();
        }


        JSONObject postParameters = new JSONObject();
        postParameters.put("classesName", attendanceDtos.get(0).getClassesName());
        postParameters.put("timeOffset", attendanceDtos.get(0).getTimeOffset());
        postParameters.put("clockCount", attendanceDtos.get(0).getClockCount());
        postParameters.put("clockType", attendanceDtos.get(0).getClockType());
        postParameters.put("clockTypeValue", attendanceDtos.get(0).getClockTypeValue());
        postParameters.put("lateOffset", attendanceDtos.get(0).getLateOffset());
        postParameters.put("leaveOffset", attendanceDtos.get(0).getLeaveOffset());
        postParameters.put("extClassesId", attendanceDtos.get(0).getClassesId());
        postParameters.put("extCommunityId", "-1");
        postParameters.put("attrs", JSONArray.parseArray(JSONArray.toJSONString(attendanceDtos.get(0).getAttrs())));
        hcOwnerAttendanceAsynImpl.addAttendance(postParameters, staffs);
    }

    private List<JSONObject> getPartmentStaff(AttendanceClassesDto attendanceClassesDto) {

        OrgStaffRelDto orgStaffRelDto = new OrgStaffRelDto();
        orgStaffRelDto.setOrgId(attendanceClassesDto.getClassesObjId());
        orgStaffRelDto.setStoreId(attendanceClassesDto.getStoreId());
        List<OrgStaffRelDto> staffRelDtos = orgStaffRelInnerServiceSMOImpl.queryOrgStaffRels(orgStaffRelDto);
        List<JSONObject> staffObjs = new ArrayList<>();
        JSONObject staffObj = null;
        for (OrgStaffRelDto orgStaffRelDto1 : staffRelDtos) {
            staffObj = new JSONObject();
            staffObj.put("extClassesId", attendanceClassesDto.getClassesId());
            staffObj.put("extStaffId", orgStaffRelDto1.getStaffId());
            staffObj.put("staffName", orgStaffRelDto1.getStaffName());
            staffObj.put("departmentId", attendanceClassesDto.getClassesObjId());
            staffObj.put("departmentName", attendanceClassesDto.getClassesObjName());
            staffObjs.add(staffObj);
        }
        return staffObjs;
    }
}
