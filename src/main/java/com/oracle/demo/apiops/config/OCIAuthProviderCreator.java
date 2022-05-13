package com.oracle.demo.apiops.config;

import com.oracle.bmc.auth.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shaded.com.oracle.oci.javasdk.com.google.common.base.Supplier;

import java.io.InputStream;

@Configuration
public class OCIAuthProviderCreator {
    @Value("${demo.tenancyId}")
    private String tenancy;
    @Value("${demo.userId}")
    private String user;
    @Value("${demo.fingerprint}")
    private String fingerprint;
    @Value("${demo.keypath}")
    private String keypath;

    @Bean
    public AuthenticationDetailsProvider getAuthDetailsProvider() throws Exception{
        Supplier<InputStream> privateKeySupplier
                = new SimplePrivateKeySupplier(keypath);
        AuthenticationDetailsProvider provider
                = SimpleAuthenticationDetailsProvider.builder()
                .tenantId(tenancy)
                .userId(user)
                .fingerprint(fingerprint)
                .privateKeySupplier(privateKeySupplier)
                .build();
        return provider;
    }
}
