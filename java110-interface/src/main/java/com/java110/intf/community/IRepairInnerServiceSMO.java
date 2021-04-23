package com.java110.intf.community;

import com.java110.config.feign.FeignConfiguration;
import com.java110.dto.repair.RepairDto;
import com.java110.po.owner.RepairPoolPo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @ClassName IRepairInnerServiceSMO
 * @Description 报修信息接口类
 * @Author wuxw
 * @Date 2019/4/24 9:04
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
@FeignClient(name = "community-service", configuration = {FeignConfiguration.class})
@RequestMapping("/repairApi")
public interface IRepairInnerServiceSMO {

    /**
     * <p>查询小区楼信息</p>
     *
     *
     * @param repairDto 数据对象分享
     * @return RepairDto 对象数据
     */
    @RequestMapping(value = "/queryRepairs", method = RequestMethod.POST)
    List<RepairDto> queryRepairs(@RequestBody RepairDto repairDto);

    /**
     * 查询<p>小区楼</p>总记录数
     *
     * @param repairDto 数据对象分享
     * @return 小区下的小区楼记录数
     */
    @RequestMapping(value = "/queryRepairsCount", method = RequestMethod.POST)
    int queryRepairsCount(@RequestBody RepairDto repairDto);



    /**
     * <p>查询小区楼信息</p>
     *
     *
     * @param repairDto 数据对象分享
     * @return RepairDto 对象数据
     */
    @RequestMapping(value = "/queryStaffRepairs", method = RequestMethod.POST)
    List<RepairDto> queryStaffRepairs(@RequestBody RepairDto repairDto);

    /**
     * 查询<p>小区楼</p>总记录数
     *
     * @param repairDto 数据对象分享
     * @return 小区下的小区楼记录数
     */
    @RequestMapping(value = "/queryStaffRepairsCount", method = RequestMethod.POST)
    int queryStaffRepairsCount(@RequestBody RepairDto repairDto);



    /**
     * <p>查询小区楼信息</p>
     *
     *
     * @param repairDto 数据对象分享
     * @return RepairDto 对象数据
     */
    @RequestMapping(value = "/queryStaffFinishRepairs", method = RequestMethod.POST)
    List<RepairDto> queryStaffFinishRepairs(@RequestBody RepairDto repairDto);

    /**
     * 查询<p>小区楼</p>总记录数
     *
     * @param repairDto 数据对象分享
     * @return 小区下的小区楼记录数
     */
    @RequestMapping(value = "/queryStaffFinishRepairsCount", method = RequestMethod.POST)
    int queryStaffFinishRepairsCount(@RequestBody RepairDto repairDto);

    @RequestMapping(value = "/updateRepair", method = RequestMethod.POST)
    int updateRepair(@RequestBody RepairPoolPo repairPoolPo);
}
