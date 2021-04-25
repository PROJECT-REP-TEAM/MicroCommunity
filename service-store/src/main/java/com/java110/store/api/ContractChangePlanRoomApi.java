package com.java110.store.api;

import com.java110.dto.Dto;.ContractChangePlanRoomDto;
import com.java110.po.contractChangePlanRoom.ContractChangePlanRoomPo;
import com.java110.store.bmo.ContractChangePlanRoom.IDeleteContractChangePlanRoomBMO;
import com.java110.store.bmo.ContractChangePlanRoom.IGetContractChangePlanRoomBMO;
import com.java110.store.bmo.ContractChangePlanRoom.ISaveContractChangePlanRoomBMO;
import com.java110.store.bmo.ContractChangePlanRoom.IUpdateContractChangePlanRoomBMO;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/contractChangePlanRoom")
public class ContractChangePlanRoomApi {

    @Autowired
    private ISaveContractChangePlanRoomBMO saveContractChangePlanRoomBMOImpl;
    @Autowired
    private IUpdateContractChangePlanRoomBMO updateContractChangePlanRoomBMOImpl;
    @Autowired
    private IDeleteContractChangePlanRoomBMO deleteContractChangePlanRoomBMOImpl;

    @Autowired
    private IGetContractChangePlanRoomBMO getContractChangePlanRoomBMOImpl;

    /**
     * 微信保存消息模板
     * @serviceCode /contractChangePlanRoom/saveContractChangePlanRoom
     * @path /app/contractChangePlanRoom/saveContractChangePlanRoom
     * @param reqJson
     * @return
     */
    @RequestMapping(value = "/saveContractChangePlanRoom", method = RequestMethod.POST)
    public ResponseEntity<String> saveContractChangePlanRoom(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "planId", "请求报文中未包含planId");
Assert.hasKeyAndValue(reqJson, "roomId", "请求报文中未包含roomId");


        ContractChangePlanRoomPo contractChangePlanRoomPo = BeanConvertUtil.covertBean(reqJson, ContractChangePlanRoomPo.class);
        return saveContractChangePlanRoomBMOImpl.save(contractChangePlanRoomPo);
    }

    /**
     * 微信修改消息模板
     * @serviceCode /contractChangePlanRoom/updateContractChangePlanRoom
     * @path /app/contractChangePlanRoom/updateContractChangePlanRoom
     * @param reqJson
     * @return
     */
    @RequestMapping(value = "/updateContractChangePlanRoom", method = RequestMethod.POST)
    public ResponseEntity<String> updateContractChangePlanRoom(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "planId", "请求报文中未包含planId");
Assert.hasKeyAndValue(reqJson, "roomId", "请求报文中未包含roomId");
Assert.hasKeyAndValue(reqJson, "prId", "prId不能为空");


        ContractChangePlanRoomPo contractChangePlanRoomPo = BeanConvertUtil.covertBean(reqJson, ContractChangePlanRoomPo.class);
        return updateContractChangePlanRoomBMOImpl.update(contractChangePlanRoomPo);
    }

    /**
     * 微信删除消息模板
     * @serviceCode /contractChangePlanRoom/deleteContractChangePlanRoom
     * @path /app/contractChangePlanRoom/deleteContractChangePlanRoom
     * @param reqJson
     * @return
     */
    @RequestMapping(value = "/deleteContractChangePlanRoom", method = RequestMethod.POST)
    public ResponseEntity<String> deleteContractChangePlanRoom(@RequestBody JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "communityId", "小区ID不能为空");

        Assert.hasKeyAndValue(reqJson, "prId", "prId不能为空");


        ContractChangePlanRoomPo contractChangePlanRoomPo = BeanConvertUtil.covertBean(reqJson, ContractChangePlanRoomPo.class);
        return deleteContractChangePlanRoomBMOImpl.delete(contractChangePlanRoomPo);
    }

    /**
     * 微信删除消息模板
     * @serviceCode /contractChangePlanRoom/queryContractChangePlanRoom
     * @path /app/contractChangePlanRoom/queryContractChangePlanRoom
     * @param communityId 小区ID
     * @return
     */
    @RequestMapping(value = "/queryContractChangePlanRoom", method = RequestMethod.GET)
    public ResponseEntity<String> queryContractChangePlanRoom(@RequestParam(value = "communityId") String communityId,
                                                      @RequestParam(value = "page") int page,
                                                      @RequestParam(value = "row") int row) {
        ContractChangePlanRoomDto contractChangePlanRoomDto = new ContractChangePlanRoomDto();
        contractChangePlanRoomDto.setPage(page);
        contractChangePlanRoomDto.setRow(row);
        contractChangePlanRoomDto.setCommunityId(communityId);
        return getContractChangePlanRoomBMOImpl.get(contractChangePlanRoomDto);
    }
}
