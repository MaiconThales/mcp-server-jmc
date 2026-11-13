package io.github.maiconthales.mcp_server_jmc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"io.github.maiconthales.mcp_server_jmc", "com.mcp.jmc"})
public class McpServerJmcApplication {

	public static void main(String[] args) {
		var app = new SpringApplication(McpServerJmcApplication.class);
		app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
		app.run(args);
	}

}
