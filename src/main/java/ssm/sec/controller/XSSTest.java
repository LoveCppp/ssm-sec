package ssm.sec.controller;

import java.io.ObjectInputStream.GetField;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import ssm.sec.pojo.User;
import ssm.sec.service.IUserService;

@Controller
@RequestMapping("/xsstest")
public class XSSTest {
	
	@Autowired
	private IUserService userserivce;
	
	@RequestMapping("/show")
	public String show(HttpServletRequest request){
		return "xssshow";
	}
	
	@RequestMapping("get")
	public String get(HttpServletRequest request, Model model){
		Integer id = Integer.parseInt(request.getParameter("id"));

		User user = this.userserivce.getUserById(id);
		model.addAttribute("user", user);
		return "getuser";
	}
	
	@RequestMapping("/add")
	public String add(User user){
		int i = this.userserivce.insert(user);
		return "redirect:/xsstest/show";
	}
	


}
