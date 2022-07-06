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
package com.java110.community.smo.impl;


import com.java110.community.dao.IInspectionRoutePointRelV1ServiceDao;
import com.java110.dto.inspectionPlan.InspectionRoutePointRelDto;
import com.java110.intf.community.IInspectionRoutePointRelV1InnerServiceSMO;
import com.java110.po.inspection.InspectionRoutePointRelPo;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.core.base.smo.BaseServiceSMO;
import com.java110.dto.user.UserDto;
import com.java110.dto.PageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 类表述： 服务之前调用的接口实现类，不对外提供接口能力 只用于接口建调用
 * add by 吴学文 at 2022-07-04 09:06:07 mail: 928255095@qq.com
 * open source address: https://gitee.com/wuxw7/MicroCommunity
 * 官网：http://www.homecommunity.cn
 * 温馨提示：如果您对此文件进行修改 请不要删除原有作者及注释信息，请补充您的 修改的原因以及联系邮箱如下
 * // modify by 张三 at 2021-09-12 第10行在某种场景下存在某种bug 需要修复，注释10至20行 加入 20行至30行
 */
@RestController
public class InspectionRoutePointRelV1InnerServiceSMOImpl extends BaseServiceSMO implements IInspectionRoutePointRelV1InnerServiceSMO {

    @Autowired
    private IInspectionRoutePointRelV1ServiceDao inspectionRoutePointRelV1ServiceDaoImpl;


    @Override
    public int saveInspectionRoutePointRel(@RequestBody InspectionRoutePointRelPo inspectionRoutePointRelPo) {
        int saveFlag = inspectionRoutePointRelV1ServiceDaoImpl.saveInspectionRoutePointRelInfo(BeanConvertUtil.beanCovertMap(inspectionRoutePointRelPo));
        return saveFlag;
    }

     @Override
    public int updateInspectionRoutePointRel(@RequestBody  InspectionRoutePointRelPo inspectionRoutePointRelPo) {
        int saveFlag = inspectionRoutePointRelV1ServiceDaoImpl.updateInspectionRoutePointRelInfo(BeanConvertUtil.beanCovertMap(inspectionRoutePointRelPo));
        return saveFlag;
    }

     @Override
    public int deleteInspectionRoutePointRel(@RequestBody  InspectionRoutePointRelPo inspectionRoutePointRelPo) {
       inspectionRoutePointRelPo.setStatusCd("1");
       int saveFlag = inspectionRoutePointRelV1ServiceDaoImpl.updateInspectionRoutePointRelInfo(BeanConvertUtil.beanCovertMap(inspectionRoutePointRelPo));
       return saveFlag;
    }

    @Override
    public List<InspectionRoutePointRelDto> queryInspectionRoutePointRels(@RequestBody InspectionRoutePointRelDto inspectionRoutePointRelDto) {

        //校验是否传了 分页信息

        int page = inspectionRoutePointRelDto.getPage();

        if (page != PageDto.DEFAULT_PAGE) {
            inspectionRoutePointRelDto.setPage((page - 1) * inspectionRoutePointRelDto.getRow());
        }

        List<InspectionRoutePointRelDto> inspectionRoutePointRels = BeanConvertUtil.covertBeanList(inspectionRoutePointRelV1ServiceDaoImpl.getInspectionRoutePointRelInfo(BeanConvertUtil.beanCovertMap(inspectionRoutePointRelDto)), InspectionRoutePointRelDto.class);

        return inspectionRoutePointRels;
    }


    @Override
    public int queryInspectionRoutePointRelsCount(@RequestBody InspectionRoutePointRelDto inspectionRoutePointRelDto) {
        return inspectionRoutePointRelV1ServiceDaoImpl.queryInspectionRoutePointRelsCount(BeanConvertUtil.beanCovertMap(inspectionRoutePointRelDto));    }

}
