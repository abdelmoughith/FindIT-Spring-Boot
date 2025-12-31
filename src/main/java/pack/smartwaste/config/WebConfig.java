package pack.smartwaste.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // General uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/var/www/findit/uploads/");

        registry.addResourceHandler("/tmp/**")
                .addResourceLocations("file:/var/www/findit/tmp/");

        // Profile pictures
        registry.addResourceHandler("/profile_pictures/**")
                .addResourceLocations("file:/var/www/findit/profile_pictures/");
    }
}
