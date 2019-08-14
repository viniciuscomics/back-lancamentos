package br.com.example.sistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import br.com.example.sistema.config.property.ApiProperty;

@SpringBootApplication
@EnableConfigurationProperties(ApiProperty.class)
public class ViniciusApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ViniciusApiApplication.class, args);
	}
}
