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

package com.catlogging.web.controller.system.auth;

import com.catlogging.event.h2.jpa.MemberRepository;
import com.catlogging.model.auth.Member;
import com.catlogging.model.auth.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@AllArgsConstructor
@RestController
@Slf4j
public class MemberController {

    @Autowired
    private final MemberRepository memberRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    MessageSource messageSource;

//    @GetMapping("/member/new")
//    public String memberJoinForm(Member memberForm) {
//
//        // view에서 post 형태는 키:벨류 형태로 데이터 리턴
//        // Get처럼 url리턴하면 안됨.
//        // return "redirect:/login";
//        return "th/login/memberJoinForm";
//    }

    @RequestMapping(value ="/member/new", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Result memberJoin(@RequestBody Member member){

        Result.ResultBuilder result = Result.builder()
                .type("success")
                .message(messageSource.getMessage("catlogging.member.add.success", new String[]{member.getMemberId()}, LocaleContextHolder.getLocale()));

        boolean isPasswordReset = true;

        // check Already User
        if(member.getId() == null && member.getMemberId() != null){
            Optional<Member> prevMember = memberRepository.findByMemberId(member.getMemberId());
            if (prevMember.isPresent()) {
                return result
                        .message(messageSource.getMessage("catlogging.member.add.already", new String[]{member.getMemberId()}, LocaleContextHolder.getLocale()))
                        .type("warring")
                        .build();
            }
        }

        // Update user
        if (member.getId() != null) {
            Optional<Member> prevMember = memberRepository.findById(member.getId());

            if (prevMember.isPresent() && StringUtils.equals(prevMember.get().getPassword(), member.getPassword())) {
                isPasswordReset = false;
            }
        }

        // new User OR User Password reset
        if(isPasswordReset) {
            member.setPassword(passwordEncoder.encode(member.getPassword()));
        }
        memberRepository.save(member);

        return result.build();
    }

    @RequestMapping(value ="/member/del", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Result memberRemote(@RequestBody Member member) {

        Result.ResultBuilder result = Result.builder()
                .type("success")
                .message(messageSource.getMessage("catlogging.member.del.success", new String[]{member.getMemberId()}, LocaleContextHolder.getLocale()));

        if(member.getId() == null)
            return result.build();

        memberRepository.delete(member);

        return result.build();
    }

    @RequestMapping(value = "/member", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Member> getMember(@RequestParam(value = "memberId", required = false) String member) {

        if(StringUtils.isEmpty(member))
            return memberRepository.findAll();

        Optional<Member> members = memberRepository.findByMemberId(member);
        return members.isPresent() ? Collections.singletonList(members.get()) : Collections.emptyList();
    }
}