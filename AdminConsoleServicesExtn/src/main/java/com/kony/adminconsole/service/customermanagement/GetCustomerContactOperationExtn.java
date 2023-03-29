package com.kony.adminconsole.service.customermanagement;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class GetCustomerContactOperationExtn implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(GetCustomerContactOperationExtn.class);

    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse response) throws Exception {
        Result result = new Result();
        long startTime = System.currentTimeMillis();
        long svcEndTime = 0L;
        long svcStartTime = 0L;
        String customerId = null;
        String username = null;
        try {
            if (StringUtils.isEmpty(requestInstance.getParameter("Customer_id")) &&
                    StringUtils.isEmpty(requestInstance.getParameter("Customer_username"))) {
                ErrorCodeEnum.ERR_20565.setErrorCode(result);
                return result;
            }
            customerId = requestInstance.getParameter("Customer_id");
            username = requestInstance.getParameter("Customer_username");
            svcStartTime = System.currentTimeMillis();
            JSONObject getCustomerResponse = DBPServices.getCustomerCommunication(customerId, username, requestInstance);
            svcEndTime = System.currentTimeMillis();
            if (getCustomerResponse == null || !getCustomerResponse.has("opstatus") || getCustomerResponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_20882.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch customer communication information: " + customerId);
                return result;
            }
            if (getCustomerResponse.has("dbpErrMsg")) {
                result.addParam(new Param("dbpErrMsg", getCustomerResponse
                        .getString("dbpErrMsg"), "string"));
                return result;
            }
            if (getCustomerResponse.has("records")) {
                JSONArray recordsArray = getCustomerResponse.getJSONArray("records");
                for (int indexVar = 0; indexVar < recordsArray.length(); indexVar++) {
                    JSONObject currRecordJSONObject = recordsArray.getJSONObject(indexVar);
//                    if (currRecordJSONObject.has("ssn"))
//                        currRecordJSONObject.put("ssn",
//                                CustomerHandler.maskSSN(currRecordJSONObject.get("ssn").toString()));

                    if (currRecordJSONObject.has("Ssn"))
                        currRecordJSONObject.put("Ssn",
                                CustomerHandler.maskSSN(currRecordJSONObject.get("Ssn").toString()));
                }
                Dataset recordsDataset = CommonUtilities.constructDatasetFromJSONArray(recordsArray);
                recordsDataset.setId("records");
                result.addDataset(recordsDataset);
            } else {
                ErrorCodeEnum.ERR_20716.setErrorCode(result);
                return result;
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.SUCCESSFUL, "Successfully fetched customer communication detail. customerId: " + customerId);
            return result;
        } catch (Exception e) {
            LOG.error("Unexepected Error in get list of company by status", e);
            result.addParam(new Param("status", "Failure", "string"));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch customer communication detail. customerId: " + customerId);
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            long endTime = System.currentTimeMillis();
            LOG.error("MF Time company details send rsp:" + (endTime - startTime) + "service time" + (svcEndTime - svcStartTime));
            return result;
        }
    }
}

