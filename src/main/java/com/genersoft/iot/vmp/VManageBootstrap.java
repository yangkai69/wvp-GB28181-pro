package com.genersoft.iot.vmp;

import java.util.Timer;
import java.util.logging.LogManager;

import com.genersoft.iot.vmp.filter.MyFilter;
import com.genersoft.iot.vmp.utils.HttpUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableOpenApi
public class VManageBootstrap extends LogManager {
	private static String[] args;
	private static ConfigurableApplicationContext context;
	public static void main(String[] args) {
		VManageBootstrap.args = args;
		VManageBootstrap.context = SpringApplication.run(VManageBootstrap.class, args);

		HttpUtil httpUtil = new HttpUtil();
		httpUtil.sendRestart();

	}
	// 项目重启
	public static void restart() {
		context.close();
		VManageBootstrap.context = SpringApplication.run(VManageBootstrap.class, args);
 
	}

	@Bean
	public FilterRegistrationBean filterRegiste() {
		FilterRegistrationBean regFilter = new FilterRegistrationBean();
		regFilter.setFilter(new MyFilter());
		regFilter.addUrlPatterns("/*");
		regFilter.setName("MyFilter");
		regFilter.setOrder(6);
		return regFilter;
	}

}
