package com.java110.core.language;

import com.java110.dto.menuCatalog.MenuCatalogDto;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 英文
 */
@Component
public class En extends DefaultLanguage{
    private static Map<String, String> menuCatalogs = new HashMap<>();
    private static Map<String, String> menus = new HashMap<>();
    private static Map<String, String> msgs = new HashMap<>();



    public List<MenuCatalogDto> getMenuCatalog(List<MenuCatalogDto> menuCatalogDtos) {
        String menuCatalogsName = "";
        for (MenuCatalogDto menuCatalogDto : menuCatalogDtos) {
            menuCatalogsName = menuCatalogs.get(menuCatalogDto.getName());
            if (!StringUtil.isEmpty(menuCatalogsName)) {
                menuCatalogDto.setName(menuCatalogsName);
            }
        }
        return menuCatalogDtos;
    }


    @Override
    public List<Map> getMenuDto(List<Map> menuDtos) {
        String menuName = "";
        for (Map menuDto : menuDtos) {
            menuName = menus.get(menuDto.get("menuGroupName"));
            if (!StringUtil.isEmpty(menuName)) {
                menuDto.put("menuGroupName", menuName);
            }

            menuName = menus.get(menuDto.get("menuName"));
            if (!StringUtil.isEmpty(menuName)) {
                menuDto.put("menuName", menuName);
            }

        }
        return menuDtos;
    }

    @Override
    public ResultVo getResultVo(ResultVo resultVo) {
        String msg = msgs.get(resultVo.getMsg());
        if (!StringUtil.isEmpty(msg)) {
            resultVo.setMsg(msg);
        }

        return resultVo;
    }

    public String getLangMsg(String msg){
        String msgStr = msgs.get(msg);
        if (!StringUtil.isEmpty(msg)) {
            return msgStr;
        }

        return msg;
    }
    static {
        menuCatalogs.put("设备","Machine");
        menuCatalogs.put("首页","Index");
        menuCatalogs.put("业务受理","Business");
        menuCatalogs.put("费用报表","Report");
        menuCatalogs.put("物业服务","Service");
        menuCatalogs.put("设备停车","Parking");
        menuCatalogs.put("常用菜单","Tools");
        menuCatalogs.put("设置","Set up");

        menus.put("小区管理","Community");
        menus.put("我的小区","My community");
        menus.put("小区大屏","Community statistics");
        menus.put("业务受理","Business acceptance");
        menus.put("房屋装修","House renovation");
        menus.put("结构图","Room structure");
        menus.put("车位结构图","Parking structure");
        menus.put("产权登记","Property registration");
        menus.put("视频监控","Video");
        menus.put("资产管理","Asset");
        menus.put("楼栋信息","Floor");
        menus.put("单元信息","Unit");
        menus.put("房屋管理","Room");
        menus.put("商铺管理","Shop");
        menus.put("业主管理","Owner");
        menus.put("业主信息","Owner");
        menus.put("业主成员","Owner members");
        menus.put("绑定业主","Bind the owner");
        menus.put("业主账号","Owner account");
        menus.put("费用管理","Fee");
        menus.put("费用项设置","Fee settings");
        menus.put("房屋收费","Room charges");
        menus.put("车辆收费","Car charges");
        menus.put("合同收费","Contract charges");
        menus.put("账单催缴","Bill fee");
        menus.put("费用导入","Fee import");
        menus.put("员工收费","Staff charges");
        menus.put("费用汇总表","Summary of Fees");
        menus.put("退费审核","Refund audit");
        menus.put("欠费信息","Owe fee");
        menus.put("抄表类型","Meter reading type");
        menus.put("水电抄表","Water and electricity meter");
        menus.put("补打收据","Reprint receipt");
        menus.put("公摊公式","Public area formula");
        menus.put("缴费审核","Payment audit");
        menus.put("折扣设置","Discount settings");
        menus.put("优惠申请","Discount apply");
        menus.put("优惠类型","Discount type");
        menus.put("临时车收费","Temporary car charges");
        menus.put("临时车缴费清单","Temporary car fee");
        menus.put("取消费用","Cancel fee");
        menus.put("智慧停车","Parking");
        menus.put("停车场管理","Parking area");
        menus.put("车位信息","Parking space");
        menus.put("岗亭管理","Booth");
        menus.put("业主车辆","Owner car");
        menus.put("进场记录","inout record");
        menus.put("在场车辆","car present");
        menus.put("黑白名单","Black and white list");
        menus.put("剩余车位","Remaining parking spaces");
        menus.put("车位申请","apply parking space");
        menus.put("报修管理","Repair");
        menus.put("报修设置","Repair settings");
        menus.put("电话报修","Telephone repair");
        menus.put("工单池","Repair pool");
        menus.put("报修待办","Repair pending");
        menus.put("报修已办","Repair has been done");
        menus.put("报修回访","Repair visit");
        menus.put("强制回单","Mandatory receipt");
        menus.put("疫情管控","Epidemic control");
        menus.put("疫情设置","Epidemic settings");
        menus.put("返省上报","Report back to the province");
        menus.put("疫情上报","Epidemic report");
        menus.put("合同管理","Contract");
        menus.put("合同类型","Type of contract");
        menus.put("合同甲方","Party A");
        menus.put("起草合同","Create contracts");
        menus.put("合同查询","Query contract");
        menus.put("合同变更","Changes contract ");
        menus.put("到期合同","Contract expired ");
        menus.put("报表管理","Report");
        menus.put("报表专家","Report Specialist");
        menus.put("楼栋费用表","Building fee schedule");
        menus.put("费用分项表","Fee breakdown");
        menus.put("费用明细表","Schedule of Fees");
        menus.put("缴费清单","Payment list");
        menus.put("欠费明细表","Arrears Schedule");
        menus.put("预缴费提醒","Prepayment reminder");
        menus.put("费用到期提醒","Fee due reminder");
        menus.put("缴费明细表","Payment Schedule");
        menus.put("报修汇总表","Repair report summary");
        menus.put("未收费房屋","Unpaid housing");
        menus.put("问卷明细表","Questionnaire Schedule");
        menus.put("业主缴费明细","Owner's payment details");
        menus.put("华宁物业报表","Huaning Property Report");
        menus.put("押金报表","Deposit Statement");
        menus.put("报修报表","Repair report");
        menus.put("营业报表","Business statement");
        menus.put("协同办公","Work together");
        menus.put("流程实例","Process instance");
        menus.put("我的流程","My process");
        menus.put("工作办理","Work");
        menus.put("发布公告","Annouce");
        menus.put("电话投诉","Phone complaint");
        menus.put("访客登记","Visitor Registration");
        menus.put("信息发布","Information Release");
        menus.put("信息大类","Information categories");
        menus.put("智慧考勤","Smart Attendance");
        menus.put("今日考勤","Attendance today");
        menus.put("月考勤","Monthly attendance");
        menus.put("考勤记录","Attendance Record");
        menus.put("问卷投票","Poll");
        menus.put("我的问卷","My questionnaire");
        menus.put("活动规则","Activity Rules");
        menus.put("采购管理","Purchasing");
        menus.put("仓库信息","Warehouse");
        menus.put("物品信息","Item");
        menus.put("物品类型","Item type");
        menus.put("物品供应商","Item supplier");
        menus.put("物品规格","Item Specifications");
        menus.put("采购申请","Purchase Requisition");
        menus.put("调拨记录","Call record");
        menus.put("出入库明细","Inbound and outbound details");
        menus.put("调拨明细","Transfer details");
        menus.put("物品领用","Items received");
        menus.put("我的物品","My items");
        menus.put("转赠记录","Transfer record");
        menus.put("物品使用记录","Item usage record");
        menus.put("巡检管理","Inspection");
        menus.put("巡检项目","Inspection items");
        menus.put("巡检点","Inspection point");
        menus.put("巡检路线","Inspection route");
        menus.put("巡检计划","Inspection plan");
        menus.put("巡检任务","Inspection tasks");
        menus.put("巡检明细","Inspection details");
        menus.put("设备管理","Device");
        menus.put("设备信息","Device");
        menus.put("设备类型","Device type");
        menus.put("设备数据同步","Device data synchronization");
        menus.put("开门记录","Open door record");
        menus.put("访客留影","Visitor's photo");
        menus.put("申请钥匙","Request key");
        menus.put("钥匙审核","Key audit");
        menus.put("员工门禁授权","Employee access control authorization");
        menus.put("组织管理","Organizational");
        menus.put("组织信息","Organization");
        menus.put("员工信息","Employee");
        menus.put("员工认证","Employee Certification");
        menus.put("系统管理","System");
        menus.put("权限组","Rights Groups");
        menus.put("员工权限","Employee permissions");
        menus.put("小区配置","Cell configuration");
        menus.put("流程管理","Process");
        menus.put("修改密码","Change password");
        menus.put("商户信息","Business");
        menus.put("公众号","Wechat");
        menus.put("小程序配置","Wechat mini");
        menus.put("短信配置","Message configure");
        menus.put("位置信息","Location");
        menus.put("资产导入导出","Assets import&export");
        menus.put("历史缴费导入","Import history fee");
        menus.put("打印配置","Print configuration");
        menus.put("收据模板","Receipt template");
    }
}
