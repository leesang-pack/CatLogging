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

package com.catlogging.config.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import javax.servlet.http.HttpSessionListener;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;

//    @Autowired
//    private CustomAuthenticationProvider customAuthenticationProvider;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 보안
                .csrf().disable()
                .headers()
                    .frameOptions().sameOrigin()
                    .xssProtection().disable()
                    .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy", "default-src 'self'; connect-src 'self'; script-src 'self' 'unsafe-inline'; img-src 'self'; style-src 'self' 'unsafe-inline'; object-src 'none'"))
                    .and()
                .authorizeRequests()
                // "/member/new" 요청은 권한 없이 접근이 가능합니다.
                    .antMatchers("/member/new").permitAll()

                // "/admin" 요청은 "ADMIN" 권한을 가진 사용자만 접근할 수 있습니다.
                    .antMatchers("/admin").hasRole("ADMIN")

                // 그 외의 모든 요청은 인증된 사용자만 접근할 수 있습니다.
                    .anyRequest().authenticated()
                    .and()

                // FormLogin과 logout 기능을 사용할 수 있으며 login 페이지는 권한 없이 접근이 가능하고 login이 성공하면 "/" Url을 요청합니다.
                .formLogin()
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .loginPage("/login")                                        // login page url
                    .loginProcessingUrl("/loginauth")                           // login process url
                    .defaultSuccessUrl("/")
                    .failureUrl("/login?error=true")
//                    .failureHandler(authenticationFailureHandler)
                    .permitAll()
                    .and()
                // logout 할 시
                .logout()
                    .logoutUrl("/logout")
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                    .and()
                // 에러 핸들링
                .exceptionHandling()
                    // authenticationEntryPoint 일반 요청인지 ajax나 http 에 따라 추가적으로 확인 후에 보낸다.
                    .authenticationEntryPoint(new AjaxAndHttpAuthenticationEntryPoint("/login"));
    }

    // 세션 관리
    @Bean
    public HttpSessionListener httpSessionListener(){
        return new CustomHttpSessionEventPublisher();
    }

    // 비밀번호를 암호화해서 저장하기 위해 PasswordEncoder Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }



//    ////////////////////////////
//    // LoginSuccesshandler로 로그인 성공후 AuthenticationSuccessHandler에서 redirect하는 부분.
//    // getPassword하는 부분에서 null이 발생합니다.
//    @Override
//    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
//        // ProviderManager 접근
//        authenticationManagerBuilder.eraseCredentials(false);
//        authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider);
//    }

    // 추가적으로 static page를 권한없이 접근 가능하도록 ignoring() 설정을 작성.
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/static/**"
                ,"/ng/**"
        );
    }
}