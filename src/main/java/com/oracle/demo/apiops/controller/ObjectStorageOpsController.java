package com.oracle.demo.apiops.controller;

import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import com.oracle.demo.apiops.service.ObjectStorageOpsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ObjectStorageOpsController {
    @Autowired
    ObjectStorageOpsService objectOpsService;
    @RequestMapping(value = "/ObjectList")
    public ListObjectsRequest getObjectsList(){
        return objectOpsService.getObjectsList();
    }
}
