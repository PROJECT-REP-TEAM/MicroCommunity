package com.java110.report.smo.impl;


import com.java110.core.base.smo.BaseServiceSMO;
import com.java110.dto.attendanceClassesTask.AttendanceClassesTaskDto;
import com.java110.dto.fee.FeeConfigDto;
import com.java110.intf.report.IReportAttendanceInnerServiceSMO;
import com.java110.report.dao.IReportAttendanceServiceDao;
import com.java110.report.dao.IReportFeeMonthStatisticsServiceDao;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName FloorInnerServiceSMOImpl
 * @Description 费用月统计内部服务实现类
 * @Author wuxw
 * @Date 2019/4/24 9:20
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
@RestController
public class ReportAttendanceInnerServiceSMOImpl extends BaseServiceSMO implements IReportAttendanceInnerServiceSMO {

    @Autowired
    private IReportAttendanceServiceDao reportAttendanceServiceDaoImpl;


    @Override
    public int getMonthAttendanceCount(@RequestBody  AttendanceClassesTaskDto attendanceClassesTaskDto) {
        return reportAttendanceServiceDaoImpl.getMonthAttendanceCount(BeanConvertUtil.beanCovertMap(attendanceClassesTaskDto));
    }

    @Override
    public List<AttendanceClassesTaskDto> getMonthAttendance(@RequestBody AttendanceClassesTaskDto attendanceClassesTaskDto) {
        return BeanConvertUtil.covertBeanList(reportAttendanceServiceDaoImpl.getMonthAttendance(BeanConvertUtil.beanCovertMap(attendanceClassesTaskDto)),AttendanceClassesTaskDto.class);
    }
}
