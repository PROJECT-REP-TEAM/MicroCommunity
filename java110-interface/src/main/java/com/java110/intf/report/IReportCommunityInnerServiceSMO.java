package com.java110.intf.report;

import com.java110.config.feign.FeignConfiguration;
import com.java110.dto.RoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @ClassName IReportOwnerPayFeeInnerServiceSMO
 * @Description 业主缴费明细接口类
 * @Author wuxw
 * @Date 2019/4/24 9:04
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
@FeignClient(name = "${java110.report-service}", configuration = {FeignConfiguration.class})
@RequestMapping("/reportCommunityApi")
public interface IReportCommunityInnerServiceSMO {


    /**
     * <p>查询结构化房屋信息</p>
     *
     * @param roomDto 数据对象分享
     * @return ReportOwnerPayFeeDto 对象数据
     */
    @RequestMapping(value = "/queryRoomStructures", method = RequestMethod.POST)
    List<RoomDto> queryRoomStructures(@RequestBody RoomDto roomDto);


}
