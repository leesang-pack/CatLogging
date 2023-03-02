/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
 *
 * catlogging is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * catlogging is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.catlogging.web.controller;

import com.catlogging.h2.jpa.MemberRepository;
import com.catlogging.model.auth.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
