package com.catlogging.web.controller;

import antlr.ASTNULLType;
import com.catlogging.event.h2.jpa.MemberRepository;
import com.catlogging.model.auth.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
public class MainController {

	@Autowired
	private MemberRepository memberRepository;

	// root로 왔을 시 login page
	@RequestMapping("/")
	public String mainIndex() {


        List<Member> members = memberRepository.findAll();

        for( Member m : members) {
			log.debug("===========> member?? {}", m);

		}
        return "index";
	}
}
