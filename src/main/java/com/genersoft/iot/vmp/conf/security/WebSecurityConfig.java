package com.genersoft.iot.vmp.conf.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 配置Spring Security
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${userSettings.interfaceAuthentication}")
    private boolean interfaceAuthentication;

    @Autowired
    private DefaultUserDetailsServiceImpl userDetailsService;
    /**
     * 登出成功的处理
     */
    @Autowired
    private LoginFailureHandler loginFailureHandler;
    /**
     * 登录成功的处理
     */
    @Autowired
    private LoginSuccessHandler loginSuccessHandler;
    /**
     * 登出成功的处理
     */
    @Autowired
    private LogoutHandler logoutHandler;
    /**
     * 未登录的处理
     */
    @Autowired
    private AnonymousAuthenticationEntryPoint anonymousAuthenticationEntryPoint;
//    /**
//     * 超时处理
//     */
//    @Autowired
//    private InvalidSessionHandler invalidSessionHandler;

//    /**
//     * 顶号处理
//     */
//    @Autowired
//    private SessionInformationExpiredHandler sessionInformationExpiredHandler;
//    /**
//     * 登录用户没有权限访问资源
//     */
//    @Autowired
//    private LoginUserAccessDeniedHandler accessDeniedHandler;


    /**
     * 描述: 静态资源放行，这里的放行，是不走 Spring Security 过滤器链
     **/
    @Override
    public void configure(WebSecurity web) {

        if (!interfaceAuthentication) {
            web.ignoring().antMatchers("**");
        }else {
            // 可以直接访问的静态数据
            web.ignoring()
                    .antMatchers("/")
                    .antMatchers("/#/**")
                    .antMatchers("/static/**")
                    .antMatchers("/index.html")
                    .antMatchers("/doc.html") // "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**"
                    .antMatchers("/webjars/**")
                    .antMatchers("/swagger-resources/**")
                    .antMatchers("/v3/api-docs/**")
                    .antMatchers("/js/**");
        }
    }

    /**
     * 配置认证方式
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // 设置不隐藏 未找到用户异常
        provider.setHideUserNotFoundExceptions(true);
        // 用户认证service - 查询数据库的逻辑
        provider.setUserDetailsService(userDetailsService);
        // 设置密码加密算法
        provider.setPasswordEncoder(passwordEncoder());
        auth.authenticationProvider(provider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable();
        // 设置允许添加静态文件
        http.headers().contentTypeOptions().disable();
        http.authorizeRequests()
                // 放行接口
                .antMatchers("/api/user/login","/index/hook/**").permitAll()
                // 除上面外的所有请求全部需要鉴权认证
//                .anyRequest().authenticated()//去掉cookie验证
                // 异常处理(权限拒绝、登录失效等)
                .and().exceptionHandling()
                .authenticationEntryPoint(anonymousAuthenticationEntryPoint)//匿名用户访问无权限资源时的异常处理
//                .accessDeniedHandler(accessDeniedHandler)//登录用户没有权限访问资源
                // 登入
                .and().formLogin().permitAll()//允许所有用户
                .successHandler(loginSuccessHandler)//登录成功处理逻辑
                .failureHandler(loginFailureHandler)//登录失败处理逻辑
                // 登出
                .and().logout().logoutUrl("/api/user/logout").permitAll()//允许所有用户
                .logoutSuccessHandler(logoutHandler)//登出成功处理逻辑
                .deleteCookies("JSESSIONID")
                // 会话管理
//                .and().sessionManagement().invalidSessionStrategy(invalidSessionHandler) // 超时处理
//                .maximumSessions(1)//同一账号同时登录最大用户数
//                .expiredSessionStrategy(sessionInformationExpiredHandler) // 顶号处理
        ;

    }

    /**
     * 描述: 密码加密算法 BCrypt 推荐使用
     **/
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 描述: 注入AuthenticationManager管理器
     **/
    @Override
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
}
