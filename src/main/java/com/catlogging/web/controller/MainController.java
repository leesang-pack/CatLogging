package com.catlogging.web.controller;

import com.catlogging.event.h2.jpa.MemberRepository;
import com.catlogging.model.auth.Member;
import com.catlogging.util.WelcomeNotificationProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class MainController {

	@Autowired
	private MemberRepository memberRepository;

	// root로 왔을 시 login page
	@RequestMapping("/")
	public String mainIndex() {

//        List<Member> members = memberRepository.findAll();
//
//        for( Member m : members) {
//			log.debug("===========> member?? {}", m);
//
//		}
        return "forward:/sources";
	}

	@GetMapping("/login")
	public String loginPage(Map<String, Object> model) {
		List<Member> members = memberRepository.findAll();
		model.put("members", members);

		return "templates/login/loginForm5";
	}
}
