package com.darcklh.louise.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darcklh.louise.Model.Louise.User;
import com.darcklh.louise.Model.VO.UserRole;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/8/7 19:12
 * @Description
 */
@Mapper
@Repository
public interface UserDao extends BaseMapper<User> {

    //重写一下这个 涉及到获取创建时间丢到SQL里简单一些
    @Override
    @Insert("INSERT INTO t_user (user_id, group_id, role_id, avatar, nickname, create_time, count_setu, count_upload, isenabled, credit, credit_buff) VALUES" +
            "(#{user_id}, #{group_id}, #{role_id}, #{avatar}, #{nickname}, NOW(), #{count_setu}, #{count_upload}, #{isEnabled}, #{credit}, #{credit_buff})")
    public int insert(User user);

    @Select("SELECT *, role_name, info FROM t_user LEFT JOIN t_role ON t_role.role_id = t_user.role_id")
    public List<UserRole> findBy();

    @Select("SELECT user_id FROM t_user")
    public long[] findAllUserID();

    @Select("SELECT COUNT(user_id) FROM t_user WHERE user_id = #{user_id}")
    public Integer isUserExist(long user_id);

    @Select("SELECT isenabled FROM t_user WHERE user_id = #{user_id}")
    public Integer isUserEnabled(long user_id);

    @Update("UPDATE t_user SET isenabled = -isenabled WHERE user_id = #{user_id}")
    public Integer banUser(long user_id);

    @Update("UPDATE t_user SET count_setu = count_setu + 1 WHERE user_id = #{user_id}")
    public Integer updateCountSetu(long user_id);

    @Update("UPDATE t_user SET count_upload = count_upload + 1 WHERE user_id = #{user_id}")
    public Integer updateCountUpload(long user_id);

    @Update("UPDATE t_user SET credit = credit - #{credit_cost} WHERE user_id = #{user_id}")
    public Integer minusCredit(Integer credit_cost, long user_id);
}
