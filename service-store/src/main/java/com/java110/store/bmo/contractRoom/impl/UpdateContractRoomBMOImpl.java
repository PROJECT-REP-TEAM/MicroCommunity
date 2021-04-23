package com.java110.store.bmo.contractRoom.impl;

import com.java110.core.annotation.Java110Transactional;
import com.java110.intf.IContractRoomInnerServiceSMO;
import com.java110.po.contractRoom.ContractRoomPo;
import com.java110.store.bmo.contractRoom.IUpdateContractRoomBMO;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("updateContractRoomBMOImpl")
public class UpdateContractRoomBMOImpl implements IUpdateContractRoomBMO {

    @Autowired
    private IContractRoomInnerServiceSMO contractRoomInnerServiceSMOImpl;

    /**
     * @param contractRoomPo
     * @return 订单服务能够接受的报文
     */
    @Java110Transactional
    public ResponseEntity<String> update(ContractRoomPo contractRoomPo) {

        int flag = contractRoomInnerServiceSMOImpl.updateContractRoom(contractRoomPo);

        if (flag > 0) {
            return ResultVo.createResponseEntity(ResultVo.CODE_OK, "保存成功");
        }

        return ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "保存失败");
    }

}
