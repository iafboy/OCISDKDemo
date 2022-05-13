package com.oracle.demo.apiops.config;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.encryption.KmsMasterKey;
import com.oracle.bmc.encryption.KmsMasterKeyProvider;
import com.oracle.bmc.keymanagement.KmsCryptoClient;
import com.oracle.bmc.keymanagement.KmsManagementClient;
import com.oracle.bmc.keymanagement.KmsVaultClient;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ClientCreator {
    @Value("${demo.region}")
    private String region;
    @Value("${demo.vaultId}")
    private String vaultId;
    @Value("${demo.masterKeyId}")
    private String masterKeyId;

    @Autowired
    AuthenticationDetailsProvider authenticationDetailsProvider;

    @Bean
    public KmsMasterKeyProvider getKmsMasterKeyProvider() throws Exception{
        KmsMasterKey kmsMasterKey = new KmsMasterKey(authenticationDetailsProvider, region, vaultId, masterKeyId);
        KmsMasterKeyProvider kmsMasterKeyProvider = new KmsMasterKeyProvider(kmsMasterKey);
        return kmsMasterKeyProvider;
    }

    @Bean
    public ObjectStorageClient getObjectStorageClient() throws Exception{
        ObjectStorageClient client=new ObjectStorageClient(authenticationDetailsProvider);
        client.setRegion(region);
        return client;
    }

    @Bean
    public KmsVaultClient getKmsVaultClient() throws Exception{
        KmsVaultClient kmsVaultClient = new KmsVaultClient(authenticationDetailsProvider);
        kmsVaultClient.setRegion(region);
        return kmsVaultClient;
    }

    @Bean
    public KmsManagementClient getKmsManagementClient() throws Exception{
        return new KmsManagementClient(authenticationDetailsProvider);
    }
    @Bean
    public KmsCryptoClient getKmsCryptoClient() throws Exception{
        return  new KmsCryptoClient(authenticationDetailsProvider);
    }

}
