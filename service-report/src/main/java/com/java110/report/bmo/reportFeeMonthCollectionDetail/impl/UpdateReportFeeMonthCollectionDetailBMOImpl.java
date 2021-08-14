package com.java110.report.bmo.reportFeeMonthCollectionDetail.impl;

import com.java110.core.annotation.Java110Transactional;
import com.java110.intf.report.IReportFeeMonthCollectionDetailInnerServiceSMO;
import com.java110.po.reportFeeMonthCollectionDetail.ReportFeeMonthCollectionDetailPo;
import com.java110.report.bmo.reportFeeMonthCollectionDetail.IUpdateReportFeeMonthCollectionDetailBMO;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("updateReportFeeMonthCollectionDetailBMOImpl")
public class UpdateReportFeeMonthCollectionDetailBMOImpl implements IUpdateReportFeeMonthCollectionDetailBMO {

    @Autowired
    private IReportFeeMonthCollectionDetailInnerServiceSMO reportFeeMonthCollectionDetailInnerServiceSMOImpl;

    /**
     * @param reportFeeMonthCollectionDetailPo
     * @return 订单服务能够接受的报文
     */
    @Java110Transactional
    public ResponseEntity<String> update(ReportFeeMonthCollectionDetailPo reportFeeMonthCollectionDetailPo) {

        int flag = reportFeeMonthCollectionDetailInnerServiceSMOImpl.updateReportFeeMonthCollectionDetail(reportFeeMonthCollectionDetailPo);

        if (flag > 0) {
            return ResultVo.createResponseEntity(ResultVo.CODE_OK, "保存成功");
        }

        return ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "保存失败");
    }

}
