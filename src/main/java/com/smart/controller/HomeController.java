package com.smart.controller;


import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;


@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	//just for database testing
	/*
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/test")
	@ResponseBody
	public String test() {
		User user = new User();
		user.setName("Avanish Prajapati");
		user.setEmail("ap3387308@gmail.com");
		userRepository.save(user);
		return "working";
	}
	*/
	
	//handler
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("tittle", "Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("tittle", "About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("tittle", "Register - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	//handler for registering user
	@PostMapping("/do_register")
	public String register(@Valid @ModelAttribute("user") User user,BindingResult result1,
			@RequestParam(value = "agreement",defaultValue = "false")boolean agreement,
			Model model1,HttpSession session) {
		try {
			if(!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			
			if(result1.hasErrors()) {
				System.out.println("ERROR"+result1.toString());
				model1.addAttribute("user", user);
				
				return "signup";
			}
			
			user.setRole("Role_User");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement"+agreement);
			System.out.println("USER"+user);
			
			User result = this.userRepository.save(user);
//			model1.addAttribute("user",result);
			model1.addAttribute("user",new User());
			
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			
			
			
			return "signup";
		}
	catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model1.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went wrong !!"+ e.getMessage(), "alert-danger"));
			return "signup";
		}
	}
	
	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("tittle", "Login Page");
		return "login";
	}

}
