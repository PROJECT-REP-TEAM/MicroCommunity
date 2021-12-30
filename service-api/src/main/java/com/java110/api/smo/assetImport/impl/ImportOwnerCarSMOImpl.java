package com.java110.api.smo.assetImport.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.smo.DefaultAbstractComponentSMO;
import com.java110.api.smo.assetImport.IImportOwnerCarSMO;
import com.java110.core.context.IPageData;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.FloorDto;
import com.java110.dto.RoomDto;
import com.java110.dto.UnitDto;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.dto.owner.OwnerRoomRelDto;
import com.java110.dto.parking.ParkingAreaDto;
import com.java110.dto.parking.ParkingSpaceDto;
import com.java110.entity.component.ComponentValidateResult;
import com.java110.intf.community.*;
import com.java110.intf.user.*;
import com.java110.po.car.OwnerCarPo;
import com.java110.po.parking.ParkingSpacePo;
import com.java110.utils.util.*;
import com.java110.vo.ResultVo;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("importOwnerCarSMOImpl")
public class ImportOwnerCarSMOImpl extends DefaultAbstractComponentSMO implements IImportOwnerCarSMO {

    private final static Logger logger = LoggerFactory.getLogger(ImportOwnerCarSMOImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IOwnerCarV1InnerServiceSMO ownerCarV1InnerServiceSMOImpl;

    @Autowired
    private IFloorInnerServiceSMO floorInnerServiceSMOImpl;

    @Autowired
    private IUnitInnerServiceSMO unitInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IOwnerRoomRelInnerServiceSMO ownerRoomRelInnerServiceSMOImpl;

    @Autowired
    private IOwnerInnerServiceSMO ownerInnerServiceSMOImpl;

    @Autowired
    private IParkingAreaInnerServiceSMO parkingAreaInnerServiceSMOImpl;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;

    @Override
    public ResponseEntity<String> importExcelData(IPageData pd, MultipartFile uploadFile) throws Exception {
        try {
            ComponentValidateResult result = this.validateStoreStaffCommunityRelationship(pd, restTemplate);
            //InputStream is = uploadFile.getInputStream();
            Workbook workbook = ImportExcelUtils.createWorkbook(uploadFile);  //工作簿
            List<OwnerCarDto> ownerCars = new ArrayList<OwnerCarDto>();
            //获取楼信息
            getOwnerCars(workbook, ownerCars);
            // 保存数据
            return dealExcelData(pd, ownerCars, result);
        } catch (Exception e) {
            logger.error("导入失败 ", e);
            return new ResponseEntity<String>("非常抱歉，您填写的模板数据有误：" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 获取业主车辆信息
     *
     * @param workbook
     * @param ownerCarDtos
     */
    private void getOwnerCars(Workbook workbook, List<OwnerCarDto> ownerCarDtos) throws ParseException {
        Sheet sheet = null;
        sheet = ImportExcelUtils.getSheet(workbook, "业主车辆信息");
        List<Object[]> oList = ImportExcelUtils.listFromSheet(sheet);
        OwnerCarDto importOwnerCar = null;
        for (int osIndex = 0; osIndex < oList.size(); osIndex++) {
            Object[] os = oList.get(osIndex);
            if (osIndex == 0 || osIndex == 1) { // 第一行是 头部信息 直接跳过
                continue;
            }
            if (StringUtil.isNullOrNone(os[0])) {
                continue;
            }
            Assert.hasValue(os[0], (osIndex + 1) + "车牌号不能为空");
            Assert.hasValue(os[1], (osIndex + 1) + "房屋号不能为空");
            Assert.hasValue(os[2], (osIndex + 1) + "车辆品牌不能为空");
            Assert.hasValue(os[3], (osIndex + 1) + "车辆类型不能为空");
            Assert.hasValue(os[4], (osIndex + 1) + "颜色不能为空");
            Assert.hasValue(os[5], (osIndex + 1) + "业主不能为空");
            Assert.hasValue(os[6], (osIndex + 1) + "车位不能为空");
            Assert.hasValue(os[7], (osIndex + 1) + "起租时间不能为空");
            Assert.hasValue(os[8], (osIndex + 1) + "截止时间不能为空");
            Assert.hasValue(os[9], (osIndex + 1) + "停车场类型不能为空");
            Assert.hasValue(os[10], (osIndex + 1) + "车位类型不能为空");
            String startTime = excelDoubleToDate(os[7].toString());
            String endTime = excelDoubleToDate(os[8].toString());
            Assert.isDate(startTime, DateUtil.DATE_FORMATE_STRING_A, (osIndex + 1) + "行开始时间格式错误 请填写YYYY-MM-DD HH:mm:ss文本格式");
            Assert.isDate(endTime, DateUtil.DATE_FORMATE_STRING_A, (osIndex + 1) + "行结束时间格式错误 请填写YYYY-MM-DD HH:mm:ss文本格式");
            importOwnerCar = new OwnerCarDto();
            importOwnerCar.setCarNum(os[0].toString());
            importOwnerCar.setRoomName(os[1].toString());
            importOwnerCar.setCarBrand(os[2].toString());
            importOwnerCar.setCarType(os[3].toString());
            importOwnerCar.setCarColor(os[4].toString());
            importOwnerCar.setOwnerName(os[5].toString());
            //获取车位
            String parkingLot = os[6].toString();
            String[] split = parkingLot.split("-");
            importOwnerCar.setAreaNum(split[0]);
            importOwnerCar.setNum(split[1]);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            importOwnerCar.setStartTime(simpleDateFormat.parse(startTime));
            importOwnerCar.setEndTime(simpleDateFormat.parse(endTime));
            importOwnerCar.setTypeCd(os[9].toString());
            importOwnerCar.setSpaceSate(os[10].toString());
            ownerCarDtos.add(importOwnerCar);
        }
    }


    /**
     * 处理ExcelData数据
     */
    private ResponseEntity<String> dealExcelData(IPageData pd, List<OwnerCarDto> ownerCarDtos, ComponentValidateResult result) {
        ResponseEntity<String> responseEntity = null;
        //保存单元信息 和 楼栋信息
        responseEntity = savedOwnerCars(pd, ownerCarDtos, result);
        if (responseEntity == null || responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }
        return responseEntity;
    }

    private ResponseEntity<String> savedOwnerCars(IPageData pd, List<OwnerCarDto> ownerCars, ComponentValidateResult result) {
        JSONObject reqJson = JSONObject.parseObject(pd.getReqData());
        if (ownerCars.size() < 1) {
            throw new IllegalArgumentException("没有数据需要处理");
        }
        for (OwnerCarDto ownerCarDto : ownerCars) {
            OwnerCarPo ownerCarPo = BeanConvertUtil.covertBean(ownerCarDto, OwnerCarPo.class);
            //获取房屋名称
            String roomName = ownerCarDto.getRoomName();
            String[] split = roomName.split("-");
            String floorNum = split[0];
            String unitNum = split[1];
            String roomNum = split[2];
            FloorDto floorDto = new FloorDto();
            floorDto.setCommunityId(reqJson.getString("communityId"));
            floorDto.setFloorNum(floorNum);
            //查询楼栋
            List<FloorDto> floorDtos = floorInnerServiceSMOImpl.queryFloors(floorDto);
            Assert.listOnlyOne(floorDtos, "查询楼栋错误！");
            UnitDto unitDto = new UnitDto();
            unitDto.setUnitNum(unitNum);
            unitDto.setFloorId(floorDtos.get(0).getFloorId());
            //查询单元
            List<UnitDto> unitDtos = unitInnerServiceSMOImpl.queryUnits(unitDto);
            Assert.listOnlyOne(unitDtos, "查询单元错误！");
            RoomDto roomDto = new RoomDto();
            roomDto.setRoomNum(roomNum);
            roomDto.setUnitId(unitDtos.get(0).getUnitId());
            //查询房屋
            List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);
            Assert.listOnlyOne(roomDtos, "查询房屋错误！");
            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            ownerRoomRelDto.setRoomId(roomDtos.get(0).getRoomId());
            //查询业主房屋关系表
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);
            Assert.listOnlyOne(ownerRoomRelDtos, "查询业主房屋信息错误！");
            OwnerDto ownerDto = new OwnerDto();
            ownerDto.setOwnerId(ownerRoomRelDtos.get(0).getOwnerId());
            ownerDto.setName(ownerCarDto.getOwnerName());
            //查询业主
            List<OwnerDto> ownerDtos = ownerInnerServiceSMOImpl.queryOwners(ownerDto);
            Assert.listOnlyOne(ownerDtos, "查询业主信息错误！");
            if (ownerDtos.get(0).getOwnerTypeCd().equals("1001")) { //业主
                ownerCarPo.setCarTypeCd("1001"); //业主车辆
            } else {
                ownerCarPo.setCarTypeCd("1002"); //成员车辆
            }
            ParkingAreaDto parkingAreaDto = new ParkingAreaDto();
            parkingAreaDto.setNum(ownerCarDto.getAreaNum());
            parkingAreaDto.setTypeCd(ownerCarDto.getTypeCd());
            //查询停车场
            List<ParkingAreaDto> parkingAreaDtos = parkingAreaInnerServiceSMOImpl.queryParkingAreas(parkingAreaDto);
            Assert.listOnlyOne(parkingAreaDtos, "查询停车场错误！");
            ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
            parkingSpaceDto.setNum(ownerCarDto.getNum());
            parkingSpaceDto.setPaId(parkingAreaDtos.get(0).getPaId());
            //查询停车位
            List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);
            Assert.listOnlyOne(parkingSpaceDtos, "查询停车位错误！");
            //获取停车位状态(出售 S，出租 H ，空闲 F)
            String state = parkingSpaceDtos.get(0).getState();
            if (!StringUtil.isEmpty(state) && !state.equals("F")) {
                throw new IllegalArgumentException(parkingAreaDtos.get(0).getNum() + "停车场-" + parkingSpaceDtos.get(0).getNum() + "停车位不是空闲状态！");
            }
            ownerCarPo.setPsId(parkingSpaceDtos.get(0).getPsId());
            ownerCarPo.setOwnerId(ownerRoomRelDtos.get(0).getOwnerId());
            ownerCarPo.setUserId("-1");
            ownerCarPo.setCommunityId(reqJson.getString("communityId"));
            ownerCarPo.setCarId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_carId));
            ownerCarPo.setMemberId(ownerCarPo.getCarId());
            ownerCarPo.setState("1001"); //1001 正常状态，2002 车位释放欠费状态，3003 车位释放
            ownerCarV1InnerServiceSMOImpl.saveOwnerCar(ownerCarPo);
            ParkingSpacePo parkingSpacePo = new ParkingSpacePo();
            parkingSpacePo.setPsId(parkingSpaceDtos.get(0).getPsId()); //车位id
            parkingSpacePo.setState(ownerCarDto.getSpaceSate());
            parkingSpaceInnerServiceSMOImpl.updateParkingSpace(parkingSpacePo);
        }
        return ResultVo.success();
    }

    //解析Excel日期格式
    public static String excelDoubleToDate(String strDate) {
        if (strDate.length() == 5) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date tDate = DoubleToDate(Double.parseDouble(strDate));
                return sdf.format(tDate);
            } catch (Exception e) {
                e.printStackTrace();
                return strDate;
            }
        }
        return strDate;
    }

    //解析Excel日期格式
    public static Date DoubleToDate(Double dVal) {
        Date tDate = new Date();
        long localOffset = tDate.getTimezoneOffset() * 60000; //系统时区偏移 1900/1/1 到 1970/1/1 的 25569 天
        tDate.setTime((long) ((dVal - 25569) * 24 * 3600 * 1000 + localOffset));
        return tDate;
    }
}
