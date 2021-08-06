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
package com.java110.community.bmo.initializeBuildingUnit.impl;

import com.java110.community.bmo.initializeBuildingUnit.IinitializeBuildingUnitBmo;
import com.java110.core.factory.AuthenticationFactory;
import com.java110.dto.FloorDto;
import com.java110.dto.user.UserDto;
import com.java110.intf.community.IFloorInnerServiceSMO;
import com.java110.intf.community.IinitializeBuildingUnitSMO;
import com.java110.intf.fee.IInitializePayFeeInnerServiceSMO;
import com.java110.intf.user.IInitializeOwnerInnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户小区 查询实现类
 * <p>
 * add by 吴学文 2020-12-23
 * <p>
 * 文档参考：http://www.homecommunity.cn
 */
@Service
public class IinitializeBuildingUnitBmoImpl implements IinitializeBuildingUnitBmo {

    @Autowired
    private IinitializeBuildingUnitSMO initializeBuildingUnitSMOImpl;
    @Autowired
    private IFloorInnerServiceSMO floorInnerServiceSMOImpl;
    @Autowired
    private IInitializeOwnerInnerServiceSMO initializeOwnerInnerServiceSMOImpl;
    @Autowired
    private IInitializePayFeeInnerServiceSMO initializePayFeeInnerServiceSMOImpl;
    @Autowired
    private IUserInnerServiceSMO UserInnerServiceSMOImpl;
    @Override
    public ResponseEntity<String> deleteBuildingUnit(String communityId,String userId,String userPassword) {
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        List<UserDto> userDtos = UserInnerServiceSMOImpl.getUserHasPwd(userDto);
        if(null == userDtos || userDtos.size() < 1){
            return ResultVo.createResponseEntity("没有查到用户信息，初始化失败！");
        }
        if(!AuthenticationFactory.passwdMd5(userPassword).equals(userDtos.get(0).getPassword())){
            return ResultVo.createResponseEntity("初始化时输入的密码不正确，初始化失败！");
        }
        StringBuffer massage = new StringBuffer();
        FloorDto floorDto = new FloorDto();
        floorDto.setCommunityId(communityId);
        List<FloorDto> floorDtos =  floorInnerServiceSMOImpl.queryFloors(floorDto);
        List floors = new ArrayList();
        int communitys = 0;
        if(null != floorDtos && floorDtos.size() > 0){
            for (FloorDto floorDtotmp: floorDtos){
                floors.add(floorDtotmp.getFloorId());
            }
            Map floorIds = new HashMap<String,String []>();
            floorIds.put("floorIds",floors.toArray(new String[floors.size()]));
            massage.append( "开始格式化小区数据：" );
            //单元
            communitys = initializeBuildingUnitSMOImpl.deleteBuildingUnit(floorIds);
        }

        if(communitys > 0){
            massage.append("单元初始化成功【"+communitys+"】");
        }else {
            massage.append("单元初始化失败");
        }

        Map communityIds = new HashMap<String,String>();
        communityIds.put("communityId",communityId);
        //楼栋
        int deleteFlag = initializeBuildingUnitSMOImpl.deletefFloor(communityIds);
        if(deleteFlag > 0){
            massage.append("楼栋初始化成功【"+deleteFlag+"】");
        }else {
            massage.append("楼栋初始化失败");
        }
        //房屋
        int deleteFlagRoot = initializeBuildingUnitSMOImpl.deleteBuildingRoom(communityIds);
        if(deleteFlagRoot > 0){
            massage.append("房屋初始化成功【"+deleteFlagRoot+"】");
        }else {
            massage.append("房屋初始化失败");
        }
        //业主
        int deleteFlagOwner = initializeOwnerInnerServiceSMOImpl.deleteBuildingOwner(communityIds);
        if(deleteFlagOwner > 0){
            massage.append("业主初始化成功【"+deleteFlagOwner+"】");
        }else {
            massage.append("业主初始化失败");
        }
        //费用
        int deleteFlagFee = initializePayFeeInnerServiceSMOImpl.deletePayFee(communityIds);
        if(deleteFlagFee > 0){
            massage.append("费用初始化成功【"+deleteFlagFee+"】");
        }else {
            massage.append("费用初始化失败");
        }

        //停车场
        int deleteFlagArea = initializeBuildingUnitSMOImpl.deleteParkingArea(communityIds);
        if(deleteFlagArea > 0){
            massage.append("停车场初始化成功【"+deleteFlagArea+"】");
        }else {
            massage.append("停车场初始化失败");
        }
        //停车位
        int deleteFlagSpace = initializeBuildingUnitSMOImpl.deleteParkingSpace(communityIds);
        if(deleteFlagSpace > 0){
            massage.append("停车位初始化成功【"+deleteFlagSpace+"】");
        }else {
            massage.append("停车位初始化失败");
        }

        return ResultVo.createResponseEntity(massage);
    }

    public IinitializeBuildingUnitSMO getInitializeBuildingUnitSMOImpl() {
        return initializeBuildingUnitSMOImpl;
    }

    public void setInitializeBuildingUnitSMOImpl(IinitializeBuildingUnitSMO initializeBuildingUnitSMOImpl) {
        this.initializeBuildingUnitSMOImpl = initializeBuildingUnitSMOImpl;
    }

    public IFloorInnerServiceSMO getFloorInnerServiceSMOImpl() {
        return floorInnerServiceSMOImpl;
    }

    public void setFloorInnerServiceSMOImpl(IFloorInnerServiceSMO floorInnerServiceSMOImpl) {
        this.floorInnerServiceSMOImpl = floorInnerServiceSMOImpl;
    }

    public IInitializeOwnerInnerServiceSMO getInitializeOwnerInnerServiceSMOImpl() {
        return initializeOwnerInnerServiceSMOImpl;
    }

    public void setInitializeOwnerInnerServiceSMOImpl(IInitializeOwnerInnerServiceSMO initializeOwnerInnerServiceSMOImpl) {
        this.initializeOwnerInnerServiceSMOImpl = initializeOwnerInnerServiceSMOImpl;
    }

    public IInitializePayFeeInnerServiceSMO getInitializePayFeeInnerServiceSMOImpl() {
        return initializePayFeeInnerServiceSMOImpl;
    }

    public void setInitializePayFeeInnerServiceSMOImpl(IInitializePayFeeInnerServiceSMO initializePayFeeInnerServiceSMOImpl) {
        this.initializePayFeeInnerServiceSMOImpl = initializePayFeeInnerServiceSMOImpl;
    }

    public IUserInnerServiceSMO getUserInnerServiceSMOImpl() {
        return UserInnerServiceSMOImpl;
    }

    public void setUserInnerServiceSMOImpl(IUserInnerServiceSMO userInnerServiceSMOImpl) {
        UserInnerServiceSMOImpl = userInnerServiceSMOImpl;
    }
}
