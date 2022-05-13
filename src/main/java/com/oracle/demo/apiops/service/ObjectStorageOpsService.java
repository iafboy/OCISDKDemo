package com.oracle.demo.apiops.service;


import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObjectStorageOpsService {
    @Autowired
    ObjectStorageClient objectStorageClient;

    public ListObjectsRequest getObjectsList() {
        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .namespaceName("ocichina001")
                .bucketName("bucket1").build();
        return listObjectsRequest;
    }


}
