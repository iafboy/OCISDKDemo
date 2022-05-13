package com.oracle.demo.apiops.controller;

import com.oracle.bmc.keymanagement.model.Key;
import com.oracle.bmc.keymanagement.model.KeyShape;
import com.oracle.demo.apiops.service.KmsOpsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
public class KmsOpsController {
    @Autowired
    KmsOpsService kmsOpsService;

    @Value("${demo.compartmentId}")
    String compartmentId;

    @RequestMapping(value = "/EncryptTextByMKTest/{plainText}",method = RequestMethod.GET)
    public String encryptionTextByMK(@PathVariable("plainText")  String plainText){
        final byte[] input = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] cipherText = kmsOpsService.encryptionTextByMK(input);
        return new String(cipherText);
    }
    @RequestMapping(value = "/MKSTest",method = RequestMethod.GET)
    public Map mskTest(){
        Map<String,String> returnValue=new HashMap<>();
        String keyid="ocid1.key.oc1.me-dubai-1.ejrhzhs5aafgg.abshqljreflj2ada4inxwc25mfwhoxhgwao45kpbkznckcb6agu77cthsfcq";
        //-------get key
        Key key=kmsOpsService.getKey(keyid);
        returnValue.put("getKey",key.getKeyShape().getAlgorithm().getValue());
        //-------create key
       KeyShape keyShape =
                KeyShape.builder().algorithm(KeyShape.Algorithm.Aes).length(32).build();
        String displayName="Test_Key_V1";
        String keyid_=kmsOpsService.createKey(keyShape,compartmentId,displayName,getSampleLoggingContext());
        returnValue.put("key",keyid_);
        //--------encrypte test
        String plaintext="Test input";
        String encryptValue=kmsOpsService.encrypt(keyid,plaintext,getSampleLoggingContext());
        returnValue.put("Test input",encryptValue);
        //--------decrypte value
        String cipherText=kmsOpsService.decrypt(keyid,encryptValue,getSampleLoggingContext());
        returnValue.put(cipherText,Boolean.valueOf("Test input".equals(cipherText)).toString());
        //--------disable created key
        Key key_=kmsOpsService.disableKey(keyid_);
        returnValue.put("disableKey",key_.getId());

        return returnValue;

    }
    private static Map<String, String> getSampleLoggingContext() {
        Map<String, String> loggingContext = new HashMap<String, String>();
        loggingContext.put("loggingContextKey1", "loggingContextValue1");
        loggingContext.put("loggingContextKey2", "loggingContextValue2");
        return loggingContext;
    }
}
