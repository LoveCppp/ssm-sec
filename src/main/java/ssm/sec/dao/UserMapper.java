package ssm.sec.dao;

import org.apache.ibatis.annotations.Param;

import ssm.sec.pojo.User;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
    
    User selectByUsername1(String name);
    
    User selectByUsername2(@Param("name") String name);
}