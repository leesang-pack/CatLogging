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

package com.catlogging.web.controller.auth;

import com.catlogging.event.h2.jpa.MemberRepository;
import com.catlogging.model.auth.Member;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Controller
public class MemberController {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;


//    @GetMapping("/admin")
//    public String adminPage(Map<String, Object> model) {
//        return "adminpage";
//    }
//

    @GetMapping("/login")
    public String loginPage(Map<String, Object> model) {
        List<Member> members = memberRepository.findAll();
        model.put("members", members);

//        model.

        return "th/login/loginForm5";
    }

    @GetMapping("/member/new")
    public String memberJoinForm(Member memberForm) {
        return "th/login/memberJoinForm";
    }

    @PostMapping("/member/new")
    public String memberJoin(Member memberForm) {

        // PasswordEncoder로 비밀번호를 암호화
        memberForm.setPassword(passwordEncoder.encode(memberForm.getPassword()));

        memberRepository.save(memberForm);

        // post 형태는 키:벨류 형태로 데이터 리턴
        // Get처럼 url리턴하면 안됨.
        return "redirect:/login";

    }


}