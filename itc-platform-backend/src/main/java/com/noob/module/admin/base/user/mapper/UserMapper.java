package com.noob.module.admin.base.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.noob.module.admin.base.user.model.entity.User;
import com.noob.module.admin.base.user.model.vo.UserVO;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据库操作
 *
 */
public interface UserMapper extends BaseMapper<User> {


    public UserVO getUserVOMore(@Param(value="userId") long userId);

}




