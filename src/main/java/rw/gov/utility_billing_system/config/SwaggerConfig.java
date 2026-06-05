package rw.gov.utility_billing_system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Utility Billing System API")
                        .version("1.0"))
                .tags(orderedTags())
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    private List<Tag> orderedTags() {
        return List.of(
                tag(SwaggerTags.AUTH, "Public login, register, OTP. Start here."),
                tag(SwaggerTags.USERS, "Admin manages staff and users. ROLE_ADMIN"),
                tag(SwaggerTags.CUSTOMERS, "Self-register (public) or admin/operator creates customer at office"),
                tag(SwaggerTags.METERS, "Assign meters to customers. ADMIN / OPERATOR"),
                tag(SwaggerTags.READINGS, "Operator captures readings. ROLE_OPERATOR"),
                tag(SwaggerTags.TARIFFS, "Admin configures pricing. ROLE_ADMIN"),
                tag(SwaggerTags.BILLS, "Generate and approve bills. ADMIN / FINANCE"),
                tag(SwaggerTags.PAYMENTS, "Record payments. ROLE_FINANCE"),
                tag(SwaggerTags.NOTIFICATIONS, "View bill/payment notifications"),
                tag(SwaggerTags.FILES, "Upload profile pictures and documents"),
                tag(SwaggerTags.AUDIT, "View system audit trail. ROLE_ADMIN")
        );
    }

    private Tag tag(String name, String description) {
        return new Tag().name(name).description(description);
    }
}
