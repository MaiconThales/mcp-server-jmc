package io.github.maiconthales.mcp_server_jmc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Ponto de entrada da aplicação do servidor MCP. */
@SpringBootApplication
public class McpServerJmcApplication {

	/**
	 * Inicializa a aplicação Spring Boot com o banner desativado para preservar
	 * o transporte STDIO do protocolo MCP.
	 *
	 * @param args argumentos recebidos pela linha de comandos
	 */
	public static void main(String[] args) {
		var app = new SpringApplication(McpServerJmcApplication.class);
		app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
		app.run(args);
	}

}
