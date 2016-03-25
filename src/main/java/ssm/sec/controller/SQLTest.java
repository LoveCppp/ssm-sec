package ssm.sec.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import ssm.sec.pojo.User;
import ssm.sec.service.IUserService;

@Controller
@RequestMapping("/sqltest")
public class SQLTest {

	@Autowired
	private IUserService userserivce;

	@RequestMapping("/get1")
	public String username1(HttpServletRequest request, Model model) {
		String name = request.getParameter("name");

		User user = this.userserivce.getUserByUsername1(name);
		model.addAttribute("user", user);
		return "showuser";
	}
	
	@RequestMapping("/get2")
	public String username2(HttpServletRequest request, Model model) {
		String name = request.getParameter("name");

		User user = this.userserivce.getUserByUsername2(name);
		model.addAttribute("user", user);
		return "showuser";
	}
}
