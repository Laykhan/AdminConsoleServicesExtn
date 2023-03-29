package com.temenos.dbx.product.javaservice;

import com.dbp.core.api.factory.impl.DBPAPIAbstractFactoryImpl;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.temenos.dbx.product.usermanagement.resource.api.ProfileManagementResource;
import org.apache.log4j.Logger;

public class CustomerSearchOperationExtn implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerSearchOperation.class);

    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
        Result result = new Result();
        try {
            ProfileManagementResource resource = (ProfileManagementResource)DBPAPIAbstractFactoryImpl.getResource(ProfileManagementResource.class);
            result = resource.searchCustomer(methodID, inputArray, dcRequest, dcResponse);
        } catch (Exception e) {
            LOG.error("Caught exception while searching Customer: " + e);
        }
        return result;
    }
}
