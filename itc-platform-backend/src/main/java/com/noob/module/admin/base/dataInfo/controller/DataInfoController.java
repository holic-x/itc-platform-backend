package com.noob.module.admin.base.dataInfo.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.noob.framework.annotation.AuthCheck;
import com.noob.framework.common.*;
import com.noob.framework.realm.ShiroUtil;
import com.noob.module.admin.base.dataInfo.model.dto.DataInfoAddRequest;
import com.noob.module.admin.base.dataInfo.model.dto.DataInfoQueryRequest;
import com.noob.module.admin.base.dataInfo.model.dto.DataInfoStatusUpdateRequest;
import com.noob.module.admin.base.dataInfo.model.dto.DataInfoUpdateRequest;
import com.noob.module.admin.base.dataInfo.model.entity.DataInfo;
import com.noob.module.admin.base.dataInfo.model.vo.DataInfoVO;
import com.noob.module.admin.base.dataInfo.service.DataInfoService;
import com.noob.module.admin.base.user.constant.UserConstant;
import com.noob.framework.exception.BusinessException;
import com.noob.framework.exception.ThrowUtils;
import com.noob.module.admin.base.user.model.entity.User;
import com.noob.module.admin.base.user.model.vo.LoginUserVO;
import com.noob.module.admin.base.user.service.UserService;
import com.noob.module.admin.base.dataInfo.constant.DataInfoConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 数据接口
 */
//@Api(tags = {"admin","DataInfoController"})
@RestController
@RequestMapping("/admin/dataInfo")
@Slf4j
public class DataInfoController {

    @Resource
    private DataInfoService dataInfoService;

    @Resource
    private UserService userService;

    // region 增删改查
    /**
     * 创建
     * @param dataInfoAddRequest
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addDataInfo(@RequestBody DataInfoAddRequest dataInfoAddRequest) {
        if (dataInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DataInfo dataInfo = new DataInfo();
        BeanUtils.copyProperties(dataInfoAddRequest, dataInfo);

        // 设置状态
        dataInfo.setStatus(DataInfoConstant.TEMPLATE_STATUS_DRAFT);
        dataInfoService.validDataInfo(dataInfo, true);

        // 获取当前登陆用户
        Date currentTime = new Date();
        dataInfo.setCreater(ShiroUtil.getCurrentUserId());
        dataInfo.setUpdater(ShiroUtil.getCurrentUserId());
        dataInfo.setCreateTime(currentTime);
        dataInfo.setUpdateTime(currentTime);

        // 新增
        boolean result = dataInfoService.save(dataInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newDataInfoId = dataInfo.getId();
        return ResultUtils.success(newDataInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteDataInfo(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        LoginUserVO currentUser = ShiroUtil.getCurrentUser();
        long id = deleteRequest.getId();
        // 判断是否存在
        DataInfo oldDataInfo = dataInfoService.getById(id);
        ThrowUtils.throwIf(oldDataInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldDataInfo.getCreater().equals(currentUser.getId()) && !ShiroUtil.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = dataInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param dataInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateDataInfo(@RequestBody DataInfoUpdateRequest dataInfoUpdateRequest) {
        if (dataInfoUpdateRequest == null || dataInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DataInfo dataInfo = new DataInfo();
        dataInfo.setUpdateTime(new Date());
        BeanUtils.copyProperties(dataInfoUpdateRequest, dataInfo);

        // 参数校验
        dataInfoService.validDataInfo(dataInfo, false);
        long id = dataInfoUpdateRequest.getId();
        // 判断是否存在
        DataInfo oldDataInfo = dataInfoService.getById(id);
        ThrowUtils.throwIf(oldDataInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = dataInfoService.updateById(dataInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<DataInfoVO> getDataInfoVOById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DataInfo dataInfo = dataInfoService.getById(id);
        if (dataInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(dataInfoService.getDataInfoVO(dataInfo));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param dataInfoQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<DataInfo>> listDataInfoByPage(@RequestBody DataInfoQueryRequest dataInfoQueryRequest) {
        long current = dataInfoQueryRequest.getCurrent();
        long size = dataInfoQueryRequest.getPageSize();
        Page<DataInfo> dataInfoPage = dataInfoService.page(new Page<>(current, size),
                dataInfoService.getQueryWrapper(dataInfoQueryRequest));
        return ResultUtils.success(dataInfoPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param dataInfoQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<DataInfoVO>> listDataInfoVOByPage(@RequestBody DataInfoQueryRequest dataInfoQueryRequest) {
        long current = dataInfoQueryRequest.getCurrent();
        long size = dataInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<DataInfo> dataInfoPage = dataInfoService.page(new Page<>(current, size),
                dataInfoService.getQueryWrapper(dataInfoQueryRequest));
        return ResultUtils.success(dataInfoService.getDataInfoVOPage(dataInfoPage));
    }



    /**
     * 分页获取列表（自定义SQL处理）
     *
     * @param dataInfoQueryRequest
     * @return
     */
    @PostMapping("/listByPage")
    public BaseResponse<Page<DataInfoVO>> listByPage(@RequestBody DataInfoQueryRequest dataInfoQueryRequest) {
        // 获取分页信息
        return ResultUtils.success(dataInfoService.getVOByPage(dataInfoQueryRequest));
    }


    // endregion

    /**
     * 更新数据状态
     *
     * @param dataInfoStatusUpdateRequest
     * @return
     */
    @PostMapping("/handleDataInfoStatus")
    public BaseResponse<Boolean> handleDataInfoStatus(@RequestBody DataInfoStatusUpdateRequest dataInfoStatusUpdateRequest) {
        if (dataInfoStatusUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long dataInfoId = dataInfoStatusUpdateRequest.getId();
        DataInfo findDataInfo = dataInfoService.getById(dataInfoId);
        if (findDataInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"数据信息不存在");
        }

        // 根据指定操作更新数据信息（此处根据操作类型更新数据状态信息）
        DataInfo dataInfo = new DataInfo();
        dataInfo.setId(dataInfoId);
        dataInfo.setUpdateTime(new Date());
        String operType = dataInfoStatusUpdateRequest.getOperType();
        Integer currentUserStatus = dataInfo.getStatus();
        if("publish".equals(operType)){
            // 校验当前状态，避免重复发布
            if(currentUserStatus== DataInfoConstant.TEMPLATE_STATUS_PUBLISH){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"当前数据已发布，请勿重复操作");
            }
            dataInfo.setStatus(DataInfoConstant.TEMPLATE_STATUS_PUBLISH);
            dataInfoService.updateById(dataInfo);
        }else if("draft".equals(operType)){
            dataInfo.setStatus(DataInfoConstant.TEMPLATE_STATUS_DRAFT);
            dataInfoService.updateById(dataInfo);
        }else {
            // 其余操作类型则不允许操作
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"指定操作类型错误，请联系管理员处理");
        }
        return ResultUtils.success(true);
    }

    // --------------- 批量操作定义 ---------------
    /**
     * 批量删除数据
     *
     * @param batchDeleteRequest
     * @return
     */
    @PostMapping("/batchDeleteDataInfo")
    public BaseResponse<Boolean> batchDeleteDataInfo(@RequestBody BatchDeleteRequest batchDeleteRequest) {
        if (batchDeleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 执行批量操作
        List<Long> idList = batchDeleteRequest.getIdList();
        if(idList == null || idList.isEmpty()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"指定操作列表不能为空");
        }
        // 批量删除
        boolean b = dataInfoService.removeBatchByIds(idList);
        return ResultUtils.success(b);
    }

}
