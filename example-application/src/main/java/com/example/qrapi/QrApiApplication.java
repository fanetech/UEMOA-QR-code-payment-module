package com.example.qrapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.aveplus.uemoa.qr.config.UemoaQrProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;

/**
 * ⭐ VOICI LE MAIN QUI UTILISE VOTRE MODULE !
 * 
 * Cette APPLICATION utilise votre MODULE comme dépendance
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.qrapi", "com.aveplus.uemoa.qr"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {UemoaQrProperties.class}
    )
)
@EnableConfigurationProperties(UemoaQrProperties.class)
@OpenAPIDefinition(
    info = @Info(
        title = "UEMOA QR Code API",
        version = "1.0.0",
        description = "API REST pour générer et parser des QR codes de paiement UEMOA",
        contact = @Contact(
            name = "AvePlus Team",
            email = "support@aveplus.com",
            url = "https://www.aveplus.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "http://www.apache.org/licenses/LICENSE-2.0"
        )
    )
)
public class QrApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(QrApiApplication.class, args);
        
        System.out.println("\n" +
            "╔══════════════════════════════════════════════════════╗\n" +
            "║                                                      ║\n" +
            "║     🚀 UEMOA QR Code API Started Successfully!      ║\n" +
            "║                                                      ║\n" +
            "║     📍 API: http://localhost:8080                   ║\n" +
            "║     📚 Swagger: http://localhost:8080/swagger-ui    ║\n" +
            "║     💚 Health: http://localhost:8080/actuator/health║\n" +
            "║                                                      ║\n" +
            "╚══════════════════════════════════════════════════════╝\n"
        );
    }
}
