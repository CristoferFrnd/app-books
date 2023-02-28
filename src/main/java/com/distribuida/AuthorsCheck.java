package com.distribuida;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import java.time.LocalTime;

@ApplicationScoped
@Liveness
public class AuthorsCheck implements HealthCheck {


    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("app-authors")
                .withData("time", LocalTime.now().toString())
                .down()
                .build();
    }
}
