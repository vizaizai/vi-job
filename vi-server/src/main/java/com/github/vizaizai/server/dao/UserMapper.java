package com.github.vizaizai.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.vizaizai.server.dao.dataobject.UserDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author liaochongwei
 * @date 2023/5/6 11:29
 */
public interface UserMapper extends BaseMapper<UserDO> {

    @Select("select * from sys_user where user_name = #{userName}")
    UserDO findByUserName(@Param("userName") String userName);

}
