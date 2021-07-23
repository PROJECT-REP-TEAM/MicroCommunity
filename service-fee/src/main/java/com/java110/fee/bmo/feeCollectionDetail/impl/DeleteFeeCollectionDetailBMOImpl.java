package com.java110.fee.bmo.feeCollectionDetail.impl;

import com.java110.core.annotation.Java110Transactional;
import com.java110.fee.bmo.feeCollectionDetail.IDeleteFeeCollectionDetailBMO;
import com.java110.intf.IFeeCollectionDetailInnerServiceSMO;
import com.java110.po.feeCollectionDetail.FeeCollectionDetailPo;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("deleteFeeCollectionDetailBMOImpl")
public class DeleteFeeCollectionDetailBMOImpl implements IDeleteFeeCollectionDetailBMO {

    @Autowired
    private IFeeCollectionDetailInnerServiceSMO feeCollectionDetailInnerServiceSMOImpl;

    /**
     * @param feeCollectionDetailPo 数据
     * @return 订单服务能够接受的报文
     */
    @Java110Transactional
    public ResponseEntity<String> delete(FeeCollectionDetailPo feeCollectionDetailPo) {

        int flag = feeCollectionDetailInnerServiceSMOImpl.deleteFeeCollectionDetail(feeCollectionDetailPo);

        if (flag > 0) {
            return ResultVo.createResponseEntity(ResultVo.CODE_OK, "保存成功");
        }

        return ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "保存失败");
    }

}
