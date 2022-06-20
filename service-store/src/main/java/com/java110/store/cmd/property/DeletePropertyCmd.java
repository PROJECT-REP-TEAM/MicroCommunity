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
package com.java110.store.cmd.property;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.annotation.Java110Transactional;
import com.java110.core.context.Environment;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.CommunityMemberDto;
import com.java110.intf.community.ICommunityMemberV1InnerServiceSMO;
import com.java110.intf.store.IStoreV1InnerServiceSMO;
import com.java110.intf.user.IMenuGroupCommunityV1InnerServiceSMO;
import com.java110.po.community.CommunityMemberPo;
import com.java110.po.menuGroupCommunity.MenuGroupCommunityPo;
import com.java110.po.store.StorePo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 类表述：删除
 * 服务编码：store.deleteStore
 * 请求路劲：/app/store.DeleteStore
 * add by 吴学文 at 2022-02-28 10:46:30 mail: 928255095@qq.com
 * open source address: https://gitee.com/wuxw7/MicroCommunity
 * 官网：http://www.homecommunity.cn
 * 温馨提示：如果您对此文件进行修改 请不要删除原有作者及注释信息，请补充您的 修改的原因以及联系邮箱如下
 * // modify by 张三 at 2021-09-12 第10行在某种场景下存在某种bug 需要修复，注释10至20行 加入 20行至30行
 */
@Java110Cmd(serviceCode = "property.deleteProperty")
public class DeletePropertyCmd extends Cmd {
    private static Logger logger = LoggerFactory.getLogger(DeletePropertyCmd.class);

    @Autowired
    private IStoreV1InnerServiceSMO storeV1InnerServiceSMOImpl;

    @Autowired
    private ICommunityMemberV1InnerServiceSMO communityMemberV1InnerServiceSMOImpl;

    @Autowired
    private IMenuGroupCommunityV1InnerServiceSMO menuGroupCommunityV1InnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        Environment.isDevEnv();
        Assert.hasKeyAndValue(reqJson, "storeId", "storeId不能为空");
        Assert.hasKeyAndValue(reqJson, "storeTypeCd", "storeTypeCd不能为空");

    }

    @Override
    @Java110Transactional
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {

        StorePo storePo = BeanConvertUtil.covertBean(reqJson, StorePo.class);
        int flag = storeV1InnerServiceSMOImpl.deleteStore(storePo);

        if (flag < 1) {
            throw new CmdException("删除数据失败");
        }

        CommunityMemberDto communityMemberDto = new CommunityMemberDto();
        communityMemberDto.setMemberId(storePo.getStoreId());
        List<CommunityMemberDto> communityMemberDtos = communityMemberV1InnerServiceSMOImpl.queryCommunityMembers(communityMemberDto);

        if (communityMemberDtos == null || communityMemberDtos.size() < 1) {
            cmdDataFlowContext.setResponseEntity(ResultVo.success());
            return;
        }

        //释放小区
        CommunityMemberPo communityMemberPo = new CommunityMemberPo();
        communityMemberPo.setMemberId(storePo.getStoreId());
        flag = communityMemberV1InnerServiceSMOImpl.deleteCommunityMember(communityMemberPo);
        if (flag < 1) {
            throw new CmdException("释放小区失败");
        }

        //小区权限也踢掉
        for (CommunityMemberDto tmpCommunityMemberDto : communityMemberDtos) {
            MenuGroupCommunityPo menuGroupCommunityPo = new MenuGroupCommunityPo();
            menuGroupCommunityPo.setCommunityId(tmpCommunityMemberDto.getCommunityId());
            flag = menuGroupCommunityV1InnerServiceSMOImpl.deleteMenuGroupCommunity(menuGroupCommunityPo);
            if (flag < 1) {
                throw new CmdException("小区权限踢掉失败");
            }
        }

        cmdDataFlowContext.setResponseEntity(ResultVo.success());
    }
}
