package com.java110.front.components.fee;

import com.java110.core.context.IPageData;
import com.java110.front.smo.IFeeServiceSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @ClassName ViewPropertyFeeConfigComponent
 * @Description 展示物业费信息
 * @Author wuxw
 * @Date 2019/6/1 14:33
 * @Version 1.0
 * add by wuxw 2019/6/1
 **/
@Component("propertyFee")
public class PropertyFeeComponent {

    @Autowired
    private IFeeServiceSMO feeServiceSMOImpl;

    public ResponseEntity<String> listFeeDetail(IPageData pd) {
        return feeServiceSMOImpl.loadFeeDetail(pd);
    }


    public IFeeServiceSMO getFeeServiceSMOImpl() {
        return feeServiceSMOImpl;
    }

    public void setFeeServiceSMOImpl(IFeeServiceSMO feeServiceSMOImpl) {
        this.feeServiceSMOImpl = feeServiceSMOImpl;
    }
}
