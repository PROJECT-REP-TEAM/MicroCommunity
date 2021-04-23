package com.java110.front.components.room;

import com.java110.core.context.IPageData;
import com.java110.front.smo.IRoomServiceSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 售卖房屋
 */
@Component("sellRoom")
public class SellRoomComponent {

    @Autowired
    private IRoomServiceSMO roomServiceSMOImpl;

    /**
     * 售卖房屋
     *
     * @param pd 包含floorId 和小区ID 页面封装对象
     * @return 单元信息
     */
    public ResponseEntity<String> sell(IPageData pd) {
        return roomServiceSMOImpl.sellRoom(pd);
    }


    public IRoomServiceSMO getRoomServiceSMOImpl() {
        return roomServiceSMOImpl;
    }

    public void setRoomServiceSMOImpl(IRoomServiceSMO roomServiceSMOImpl) {
        this.roomServiceSMOImpl = roomServiceSMOImpl;
    }
}
