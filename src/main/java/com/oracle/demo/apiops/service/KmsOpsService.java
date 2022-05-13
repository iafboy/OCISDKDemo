package com.oracle.demo.apiops.service;

import com.oracle.bmc.encryption.*;
import com.oracle.bmc.io.internal.WrappedFileInputStream;
import com.oracle.bmc.keymanagement.KmsCryptoClient;
import com.oracle.bmc.keymanagement.KmsManagementClient;
import com.oracle.bmc.keymanagement.KmsVaultClient;
import com.oracle.bmc.keymanagement.model.*;
import com.oracle.bmc.keymanagement.requests.*;
import com.oracle.bmc.keymanagement.responses.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import shaded.com.oracle.oci.javasdk.org.apache.commons.codec.binary.Base64;
import shaded.com.oracle.oci.javasdk.org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

@Service
public class KmsOpsService {
    @Autowired
    KmsMasterKeyProvider kmsMasterKeyProvider;
    @Autowired
    KmsVaultClient kmsVaultClient;
    @Autowired
    KmsManagementClient kmsManagementClient;
    @Autowired
    KmsCryptoClient kmsCryptoClient;

    @Value("${demo.vaultId}")
    private String vaultId;

    public byte[] encryptionTextByMK(byte[] plainText){
        OciCrypto ociCrypto = new OciCrypto();
        OciCryptoResult result = null;
        try {
            result = ociCrypto.encryptData(kmsMasterKeyProvider, plainText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.getResult();
    }

    public byte[] dencryptionTextByMK(byte[] cipherText){
        OciCrypto ociCrypto = new OciCrypto();
        OciCryptoResult decryptResult = null;
        try {
            decryptResult = ociCrypto.decryptData(kmsMasterKeyProvider, cipherText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptResult.getResult();
    }

    public boolean encryptionStreamByMK(String filePath, Map<String,String> keysContent){
        OciCrypto ociCrypto = new OciCrypto();
        FileInputStream fileInputStreamText = null;
        FileOutputStream fileOutputStreamEnc = null;
        try {
            fileInputStreamText = new FileInputStream(filePath);
            fileOutputStreamEnc = new FileOutputStream(filePath + ".encrypted");
            // Create encryption stream
            OciCryptoInputStream inputCipherStreamEncrypt =
                    ociCrypto.createEncryptingStream(
                            kmsMasterKeyProvider, fileInputStreamText, keysContent);
            // Encrypt data
            IOUtils.copy(inputCipherStreamEncrypt, fileOutputStreamEnc);
            inputCipherStreamEncrypt.close();
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally{
            try {
                fileOutputStreamEnc.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean decryptingStreamByMK(String filePath){
        OciCrypto ociCrypto = new OciCrypto();
        FileInputStream fileInputStreamEnc = null;
        FileOutputStream fileOutputStreamText = null;
        try {
            fileInputStreamEnc = new FileInputStream(filePath);
            fileOutputStreamText = new FileOutputStream(filePath + ".decrypted");
            // Create decryption stream
           //kmsMasterKeyProvider = new KmsMasterKeyProvider(provider);
            OciCryptoInputStream inputCipherStreamDecrypt =
                    ociCrypto.createDecryptingStream(kmsMasterKeyProvider, fileInputStreamEnc);
            // Decrypt data
            IOUtils.copy(inputCipherStreamDecrypt, fileOutputStreamText);
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }finally{
            try {
                fileOutputStreamText.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public String createKey(KeyShape keyShape,String compartmentId, String displayName,Map<String,String> tag) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        CreateKeyDetails createKeyDetails =
                CreateKeyDetails.builder()
                        .keyShape(keyShape)
                        .compartmentId(compartmentId)
                        .displayName(displayName)
                        .freeformTags(tag)
                        .build();
        CreateKeyRequest createKeyRequest =
                CreateKeyRequest.builder().createKeyDetails(createKeyDetails).build();
        CreateKeyResponse response = kmsManagementClient.createKey(createKeyRequest);
        return response.getKey().getId();
    }

    public Key getKey(String keyId) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        GetKeyRequest getKeyRequest = GetKeyRequest.builder().keyId(keyId).build();
        GetKeyResponse response = kmsManagementClient.getKey(getKeyRequest);
        return response.getKey();
    }

    public ListKeysResponse listKeys(String compartmentId) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        ListKeysRequest listKeysRequest =
                ListKeysRequest.builder().compartmentId(compartmentId).build();
        return kmsManagementClient.listKeys(listKeysRequest);

    }

    public Key updateKeyResetTags(String displayName,String keyId) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        Map<String, String> newEmptyFreeformTag = Collections.emptyMap();
        UpdateKeyDetails updateKeyDetails =
                UpdateKeyDetails.builder()
                        .displayName(displayName)
                        .freeformTags(newEmptyFreeformTag)
                        .build();
        UpdateKeyRequest updateKeyRequest =
                UpdateKeyRequest.builder().updateKeyDetails(updateKeyDetails).keyId(keyId).build();
        UpdateKeyResponse response = kmsManagementClient.updateKey(updateKeyRequest);
        return response.getKey();
    }

    public Key updateKey(String keyId,Map<String,String> newFreeformTag,String displayName) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        UpdateKeyDetails updateKeyDetails =
                UpdateKeyDetails.builder()
                        .displayName(displayName)
                        .freeformTags(newFreeformTag)
                        .build();
        UpdateKeyRequest updateKeyRequest =
                UpdateKeyRequest.builder().updateKeyDetails(updateKeyDetails).keyId(keyId).build();
        UpdateKeyResponse response = kmsManagementClient.updateKey(updateKeyRequest);
        return response.getKey();
    }

    public Key disableKey(String keyId) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        DisableKeyRequest disableKeyRequest = DisableKeyRequest.builder().keyId(keyId).build();
        DisableKeyResponse response = kmsManagementClient.disableKey(disableKeyRequest);
        return response.getKey();
    }

    public Key enableKeyTest(String keyId) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        EnableKeyRequest enableKeyRequest = EnableKeyRequest.builder().keyId(keyId).build();
        EnableKeyResponse response = kmsManagementClient.enableKey(enableKeyRequest);
        return response.getKey();
    }

    public Key cancelKeyDeletion(String keyId) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        CancelKeyDeletionRequest cancelKeyDeletionRequest =
                CancelKeyDeletionRequest.builder().keyId(keyId).build();
        CancelKeyDeletionResponse response =
                kmsManagementClient.cancelKeyDeletion(cancelKeyDeletionRequest);
        return response.getKey();
    }

    public Key scheduleKeyDeletion(
            KmsManagementClient kmsManagementClient, String keyId, Date timeOfDeletion) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        ScheduleKeyDeletionDetails scheduleKeyDeletionDetails =
                ScheduleKeyDeletionDetails.builder().timeOfDeletion(timeOfDeletion).build();
        ScheduleKeyDeletionRequest scheduleKeyDeletionRequest =
                ScheduleKeyDeletionRequest.builder()
                        .keyId(keyId)
                        .scheduleKeyDeletionDetails(scheduleKeyDeletionDetails)
                        .build();
        ScheduleKeyDeletionResponse response =
                kmsManagementClient.scheduleKeyDeletion(scheduleKeyDeletionRequest);
        return response.getKey();
    }

    public KeyVersion createKeyVersion(String keyId) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        CreateKeyVersionRequest createKeyVersionRequest =
                CreateKeyVersionRequest.builder().keyId(keyId).build();
        CreateKeyVersionResponse response =
                kmsManagementClient.createKeyVersion(createKeyVersionRequest);
        return response.getKeyVersion();
    }

    public ListKeyVersionsResponse listKeyVersions(String keyId) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        ListKeyVersionsRequest listKeyVersionsRequest =
                ListKeyVersionsRequest.builder().keyId(keyId).build();
        return kmsManagementClient.listKeyVersions(listKeyVersionsRequest);

    }

    public String encrypt(String keyId,String plaintext,Map<String,String> loggingContext) {
        kmsCryptoClient.setEndpoint(getVault(vaultId).getCryptoEndpoint());
        EncryptDataDetails encryptDataDetails =
                EncryptDataDetails.builder()
                        .keyId(keyId)
                        .plaintext(Base64.encodeBase64String(plaintext.getBytes()))
                        .loggingContext(loggingContext)
                        .build();
        EncryptRequest encryptRequest =
                EncryptRequest.builder().encryptDataDetails(encryptDataDetails).build();
        EncryptResponse response = kmsCryptoClient.encrypt(encryptRequest);
        return response.getEncryptedData().getCiphertext();
    }

    public String decrypt(String keyId, String cipherText,Map<String,String> loggingContext) {
        kmsCryptoClient.setEndpoint(getVault(vaultId).getCryptoEndpoint());
        DecryptDataDetails decryptDataDetails =
                DecryptDataDetails.builder()
                        .ciphertext(cipherText)
                        .keyId(keyId)
                        .loggingContext(loggingContext)
                        .build();
        DecryptRequest decryptRequest =
                DecryptRequest.builder().decryptDataDetails(decryptDataDetails).build();
        DecryptResponse response = kmsCryptoClient.decrypt(decryptRequest);
       return response.getDecryptedData().getPlaintext();
    }

    public GeneratedKey generateDataEncryptionKey(KeyShape keyShape,String keyId,Map<String,String> loggingContext) {
        kmsCryptoClient.setEndpoint(getVault(vaultId).getCryptoEndpoint());
        GenerateKeyDetails generateKeyDetails =
                GenerateKeyDetails.builder()
                        .keyId(keyId)
                        .keyShape(keyShape)
                        .includePlaintextKey(true)
                        .loggingContext(loggingContext)
                        .build();
        GenerateDataEncryptionKeyRequest generateDataEncryptionKeyRequest =
                GenerateDataEncryptionKeyRequest.builder()
                        .generateKeyDetails(generateKeyDetails)
                        .build();
        GenerateDataEncryptionKeyResponse response =
                kmsCryptoClient.generateDataEncryptionKey(generateDataEncryptionKeyRequest);
        return response.getGeneratedKey();
    }

    public Key backupKey(String keyId,String bucketName,String objectName,String nameSpace) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        BackupKeyRequest backupKeyRequest =
                BackupKeyRequest.builder()
                        .backupKeyDetails(
                                BackupKeyDetails.builder()
                                        .backupLocation(
                                                BackupLocationBucket.builder()
                                                        .bucketName(bucketName)
                                                        .objectName(objectName)
                                                        .namespace(nameSpace)
                                                        .build())
                                        .build())
                        .keyId(keyId)
                        .build();
        BackupKeyResponse response = kmsManagementClient.backupKey(backupKeyRequest);
        return response.getKey();
    }

    public Key restoreKeyFromObjectStore(String bucketName,String objectName,String nameSpace) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        RestoreKeyFromObjectStoreRequest restoreKeyFromObjectStoreRequest =
                RestoreKeyFromObjectStoreRequest.builder()
                        .restoreKeyFromObjectStoreDetails(
                                RestoreKeyFromObjectStoreDetails.builder()
                                        .backupLocation(
                                                BackupLocationBucket.builder()
                                                        .bucketName(bucketName)
                                                        .objectName(objectName)
                                                        .namespace(nameSpace)
                                                        .build())
                                        .build())
                        .build();
        RestoreKeyFromObjectStoreResponse response =
                kmsManagementClient.restoreKeyFromObjectStore(restoreKeyFromObjectStoreRequest);
        return response.getKey();
    }

    public Key restoreKeyFromFile(String backupFile) {
        kmsManagementClient.setEndpoint(getVault(vaultId).getManagementEndpoint());
        Key restoreKey=null;
        try (WrappedFileInputStream fileInputStream =
                     new WrappedFileInputStream(new File(backupFile))) {
            RestoreKeyFromFileRequest request =
                    RestoreKeyFromFileRequest.builder()
                            .restoreKeyFromFileDetails(fileInputStream)
                            .build();
            RestoreKeyFromFileResponse response = kmsManagementClient.restoreKeyFromFile(request);
            restoreKey=response.getKey();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return restoreKey;
        }
    }
    public Vault getVault(String vaultId) {
        GetVaultRequest getVaultRequest = GetVaultRequest.builder().vaultId(vaultId).build();
        GetVaultResponse response = kmsVaultClient.getVault(getVaultRequest);
        return response.getVault();
    }

}
