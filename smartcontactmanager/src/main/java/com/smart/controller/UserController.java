package com.smart.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForBigInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();

		User user = userRepository.getUserByUserName(userName);

		System.out.println(user);
		model.addAttribute("user", user);
		

	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		
		return "normal/user_dashboard";
	}
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	//procesing add contact 
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,Principal principal,HttpSession session) {
		
		
		try {
		String name= principal.getName();
		
		User user =this.userRepository.getUserByUserName(name);
		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		System.out.println(contact);
		
		
		session.setAttribute("message",new Message("Your contact is Added","success"));
		
		}
		catch(Exception e) {
			e.printStackTrace();
			session.setAttribute("message",new Message("Something went wrong","danger"));
		}
		return "normal/add_contact_form";
	}
	
	
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		
		String userName=principal.getName();
		
		User user=this.userRepository.getUserByUserName(userName);
		
		Pageable pageable=PageRequest.of(page, 5);
		
		Page<Contact> contacts=	this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage",page);
		
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
		
		
	}
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model model)
	{
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact =contactOptional.get();
		
		contact.setUser(null);
		
		
		
		this.contactRepository.delete(contact);
		
		
		return "redirect:/user/show_contacts/0";
	}
	
	
	

}
