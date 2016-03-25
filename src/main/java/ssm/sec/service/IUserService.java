package ssm.sec.service;

import ssm.sec.pojo.User;

public interface IUserService {
	public User getUserByUsername1(String name);
	
	public User getUserByUsername2(String name);
	
	public User getUserById(int id);
	
	public int insert(User user);
}
