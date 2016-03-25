package ssm.sec.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ssm.sec.dao.UserMapper;
import ssm.sec.pojo.User;
import ssm.sec.service.IUserService;

@Service
public class UserService implements IUserService{

	@Autowired
	private UserMapper userdao;

	public User getUserByUsername1(String name) {
		return this.userdao.selectByUsername1(name);
	}

	public User getUserByUsername2(String name) {
		return this.userdao.selectByUsername2(name);
	}
	

	public User getUserById(int id) {
		// TODO Auto-generated method stub
		return this.userdao.selectByPrimaryKey(id);
	}

	public int insert(User user) {
		// TODO Auto-generated method stub
		return this.userdao.insertSelective(user);
	}
}