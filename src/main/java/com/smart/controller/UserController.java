package com.smart.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.SessionAttributesHandler;
import org.springframework.web.multipart.MultipartFile;

import com.paytm.pg.merchant.PaytmChecksum;
import com.smart.config.AppConfig;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	public UserRepository userRepository;
	
	@Autowired
	public ContactRepository contactRepository;
	
//	public User user;
	
	//for user data for all method
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();

		System.out.println("USERNAME "+userName);
				
		User user = userRepository.getUserByUserName(userName);
		
		
		System.out.println("USER "+user);
		
		model.addAttribute("user", user);
	}
	
	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
//		String userName = principal.getName();
//
//		System.out.println("USERNAME "+userName);
//				
//		User user = userRepository.getUserByUserName(userName);
//		
		//get the user using username(EMAIL)
//		
//		System.out.println("USER "+user);
//		
//		model.addAttribute("user", user);
		
		return "normal/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("tittle", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file ,
			Principal principal,HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file...
			if (file.isEmpty()) {
				//if the file is empty then try our message
				System.out.println("file is empty");
				contact.setcImage("contact.png");
				
			}else {
				//file the file to folder and update the name to contact
				contact.setcImage(file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is uploaded");
				
			}
			
			contact.setUser(user);
			
			user.getContacts().add(contact);
			this.userRepository.save(user);
			
			System.out.println("DATA "+contact);
			
			System.out.println("Added to data base");
			
			//message success.....
		    session.setAttribute("message", new Message("Your contact is added !! Add more..", "success"));
//			
		    
			return "normal/add_contact_form";
		} catch (Exception e) {
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			//message error
			session.setAttribute("message", new Message("Some went wrong !! Try Again..", "danger"));
		}
		
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	//per page contacts = 5[n]
	//current page = 0 [page]
//	@GetMapping("/show-contacts/{page}")
	@GetMapping("/show-contacts")
//	public String showContacts(@PathVariable("page")Integer page, Model m,Principal principal) {
	public String showContacts(Model m,Principal principal) {
		
		m.addAttribute("tittle", "show user contacts");
		//contact ki list ko bhejni hai
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		List<Contact> contacts = this.contactRepository.findContactsByUser(user.getId());
		
		//pageRequest of in multipage
		//currentPage-page
		//Contact Per Page - 5
		//Pageable pageable = PageRequest.of(page, 5);
		//Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts", contacts);	//list of contacts
		//m.addAttribute("currentPage", page);	//current page
		
		
		//m.addAttribute("currentPage", contacts.getTotalPages());	//total number of page
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details.
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		System.out.println("CID"+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		//login user only see his contact and not to see another contact details..
		//check...
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("tittle", contact.getName());
		}
		
		return "normal/contact_detail";
		
	}
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId , HttpSession session,Principal principal) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		
		
		//remove
		//img
		//
		//contact.getImage
		
		//check...
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		//contact and user unlink before deleting the data..
//		contact.setUser(null);
		
		if (user.getId() == contact.getUser().getId()) {
			
			/*
			//only user_id is to be null but data is not delete from database..
			contact.setUser(null);
			this.contactRepository.delete(contact);
			*/
			
			//new delete method..
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			
			System.out.println("DELETED");
			session.setAttribute("message", new Message("Contact deleted successfully", "success"));
			
		}
		
		return "redirect:/user/show-contacts";
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid ,Model model) {
		
		model.addAttribute("tittle", "Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		model.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
			Model m,HttpSession session,Principal principal) {
		
		try {
			
			//old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			
			//Image..
			if (!file.isEmpty()) {
				//file work..
				//rewrite
				
				//delete old photo
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetail.getcImage());
				file1.delete();
				
				//update old photo
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setcImage(file.getOriginalFilename());
				
				
			}else {
				contact.setcImage(oldContactDetail.getcImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		System.out.println("CONTACT NAME "+contact.getName());
		System.out.println("CONTACT ID "+contact.getcId());
		
		session.setAttribute("message", new Message("Your contact is update...", "success"));
		
		
		return "redirect:/user/"+contact.getcId()+"/contact";   //redirect..to..update file
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("tittle", "Profile Page");
		
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
	//change password..handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal,HttpSession session) {
		
		System.out.println("OLD PASSWORD "+oldPassword);
		System.out.println("NEW PASSWORD "+newPassword);
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully changed..", "success"));
		}else {
			//error
			session.setAttribute("message", new Message("Please Enter correct old password ", "danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
	
	/**  ---------------------------------------------------  **/
	
	/**
	Random random = new Random();

	@PostMapping("/create_order")
	@ResponseBody
	public Map<String, Object> startPayment(Map<String,Integer> data){
		
		String orderID = "ORDER"+random.nextInt(1000000);
		
		//payment created
		JSONObject paytmParams = new JSONObject();
		
		//body information
		JSONObject body = new JSONObject();
		body.put("requestType", "Payment");
		body.put("mid", AppConfig.MID);
		body.put("websiteName", AppConfig.WEBSITE);
		body.put("orderId", orderID);
		body.put("callbackUrl", "http://localhost:8080/payment-success");

		JSONObject txnAmount = new JSONObject();
		txnAmount.put("value", data);
		txnAmount.put("currency", "INR");
		
		JSONObject userInfo = new JSONObject();
		userInfo.put("custId", "CUST_001");
		
		body.put("txnAmount", txnAmount);
		body.put("userInfo", userInfo);
		
		String responseData = "";
		ResponseEntity<Map> response = null;
		
		try {
			
			String checksum = PaytmChecksum.generateSignature(body.toString(), AppConfig.MKEY);
			
			JSONObject head = new JSONObject();
			head.put("signature", checksum);

			paytmParams.put("body", body);
			paytmParams.put("head", head);
			
			String post_data = paytmParams.toString();
			
			/* for Staging */
	
	/**
			URL url = new URL("https://securegw-stage.paytm.in/theia/api/v1/initiateTransaction?mid="+AppConfig.MID+"&orderId="+orderID+"");
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String,Object>> httpEntity = new HttpEntity<>(paytmParams.toMap(),headers);
			
			//calling api
			RestTemplate restTemplate = new RestTemplate();
			response = restTemplate.postForEntity(url.toString(), httpEntity, Map.class);
			
			System.out.println(response);
			
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		Map body1 = response.getBody();
		body1.put("orderId", orderID);
		body1.put("amount",txnAmount.get("value"));
		
		return body1;
		
	}
	*/

	
	
	//payment form
	@RequestMapping("/payment_form")
	public String paymentForm() {
		
		//System.out.println("Hey order function ex. ");
		
		
		return "/normal/paymentRequest";
	}

	
	
	
}
