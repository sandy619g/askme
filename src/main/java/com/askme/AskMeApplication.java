
package main.java.com.askme;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


@SpringBootApplication
public class AskMeApplication
{

	public static void main(String[] args)
	{
		new SpringApplicationBuilder(AskMeApplication.class).properties("spring.config.name:application").build().run(args);
	}

	protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
	{
		return application.sources(AskMeApplication.class).properties("spring.config.name:application");
	}

}
