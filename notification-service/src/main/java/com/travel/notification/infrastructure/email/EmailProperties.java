package com.travel.notification.infrastructure.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "notification.email")
public class EmailProperties {

    private String fromAddress = "noreply@travelplatform.com";
    private String fromName    = "TravelPlatform";
}
