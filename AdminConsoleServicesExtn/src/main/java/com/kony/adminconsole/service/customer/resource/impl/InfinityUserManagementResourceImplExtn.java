package com.kony.adminconsole.service.customer.resource.impl;

import com.dbp.core.api.factory.impl.DBPAPIAbstractFactoryImpl;
import com.dbp.core.error.DBPApplicationException;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.exception.DBPAuthenticationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.service.customer.businessdelegate.api.InfinityUserManagementBusinessDelegate;
import com.kony.adminconsole.service.customer.resource.api.InfinityUserManagementResource;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class InfinityUserManagementResourceImplExtn implements InfinityUserManagementResource {
    private static final Logger LOG = Logger.getLogger(InfinityUserManagementResourceImpl.class);

    private static final String INPUT_ID = "id";

    private static final String INPUT_USER_DETAILS = "userDetails";

    private static final String INPUT_COMPANY_LIST = "companyList";

    private static final String INPUT_ACCOUNT_LEVEL_PERMISSIONS = "accountLevelPermissions";

    private static final String INPUT_GLOBAL_LEVEL_PERMISSIONS = "globalLevelPermissions";

    private static final String INPUT_TRANSACTION_LIMITS = "transactionLimits";

    private static final String INPUT_CORE_CUSTOMER_ID = "coreCustomerId";

    private static final String INPUT_CORE_CUSTOMER_ROLE_LIST = "coreCustomerRoleIdList";

    private static final String INPUT_CONTRACT_DETAILS = "contractDetails";

    private static final String INPUT_USERID = "userId";

    private static final String INPUT_REMOVED_COMPANIES = "removedCompanies";

    private static final String INPUT_SIGNATORY_GROUP = "signatoryGroups";

    InfinityUserManagementBusinessDelegate infinityUserManagementBusinessDelegate = (InfinityUserManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(InfinityUserManagementBusinessDelegate.class);

    public Result getInfinityUser(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) {
        Result result = new Result();
        try {
            if (request.getParameter("id") == null) {
                ErrorCodeEnum.ERR_21991.setErrorCode(result);
                return result;
            }
            String userId = request.getParameter("id");
            Map<String, Object> postParametersMap = new HashMap<>();
            postParametersMap.put("id", userId);
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(request);
            JSONObject getCustomerresponse = this.infinityUserManagementBusinessDelegate.getInfinityUser(postParametersMap, dbpServicesClaimsToken);
            if (getCustomerresponse == null || !getCustomerresponse.has("opstatus") || getCustomerresponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_21992.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(request, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Get infinity user failed");
                return result;
            }
            if (getCustomerresponse.has("dbpErrMsg")) {
                result = CommonUtilities.constructResultFromJSONObject(getCustomerresponse);
                result.addParam(new Param("status", "Failure", "string"));
                return result;
            }
            result = CommonUtilities.constructResultFromJSONObject(getCustomerresponse);
            AuditHandler.auditAdminActivity(request, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.SUCCESSFUL, "Successfully fetched infinity user details. id= " + userId);
        } catch (Exception e) {
            LOG.error("Unexepected Error in get infinity user details", e);
            result.addParam(new Param("FailureReason", e.getMessage(), "string"));
            ErrorCodeEnum.ERR_21993.setErrorCode(result);
            AuditHandler.auditAdminActivity(request, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Infinity user details parse Failed");
        }
        return result;
    }

    public Result createInfinityUser(String methodId, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse response) {
        Result result = new Result();
        try {
            if (requestInstance.getParameter("userDetails") == null) {
                ErrorCodeEnum.ERR_21994.setErrorCode(result);
                return result;
            }
            if (requestInstance.getParameter("companyList") == null) {
                ErrorCodeEnum.ERR_21995.setErrorCode(result);
                return result;
            }
            String userDetails = requestInstance.getParameter("userDetails");
            String companyList = requestInstance.getParameter("companyList");
            String accountLevelPermissions = requestInstance.getParameter("accountLevelPermissions");
            String globalLevelPermissions = requestInstance.getParameter("globalLevelPermissions");
            String transactionLimits = requestInstance.getParameter("transactionLimits");
            String contractDetails = requestInstance.getParameter("contractDetails");
            String signatoryGroups = requestInstance.getParameter("signatoryGroups");
            Map<String, Object> postParametersMap = new HashMap<>();
            postParametersMap.put("userDetails", stringifyForVelocityTemplate(userDetails));
            postParametersMap.put("companyList", stringifyForVelocityTemplate(companyList));
            postParametersMap.put("accountLevelPermissions", stringifyForVelocityTemplate(accountLevelPermissions));
            postParametersMap.put("globalLevelPermissions", stringifyForVelocityTemplate(globalLevelPermissions));
            postParametersMap.put("transactionLimits", stringifyForVelocityTemplate(transactionLimits));
            postParametersMap.put("contractDetails", stringifyForVelocityTemplate(contractDetails));
            postParametersMap.put("signatoryGroups", stringifyForVelocityTemplate(signatoryGroups));
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(requestInstance);
            JSONObject createInfinityUserResponse = this.infinityUserManagementBusinessDelegate.createInfinityUser(postParametersMap, dbpServicesClaimsToken);
            if (createInfinityUserResponse == null || !createInfinityUserResponse.has("opstatus") || createInfinityUserResponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_21996.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE, ActivityStatusEnum.FAILED, "Infinity user creation failed");
                return result;
            }
            if (createInfinityUserResponse.has("dbpErrMsg")) {
                result = CommonUtilities.constructResultFromJSONObject(createInfinityUserResponse);
                result.addParam(new Param("status", "Failure", "string"));
                return result;
            }
            result.addParam(new Param("status", "Success", "string"));
            result.addParam(new Param("opstatus", createInfinityUserResponse.get("opstatus").toString(), "string"));
            result.addParam(new Param("id", createInfinityUserResponse
                    .getString("id"), "string"));
        } catch (Exception e) {
            LOG.error("Unexpected Error in create infinity user: ", e);
            result.addParam(new Param("FailureReason", e.getMessage(), "string"));
            ErrorCodeEnum.ERR_21996.setErrorCode(result);
        }
        return result;
    }

    public Result getAssociatedCustomers(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse dcResponse) throws ApplicationException {
        Result result = new Result();
        Map<String, Object> postParametersMap = new HashMap<>();
        try {
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(request);
            JSONObject jSONObject = this.infinityUserManagementBusinessDelegate.getAssociatedCustomers(postParametersMap, dbpServicesClaimsToken);
        } catch (DBPAuthenticationException e1) {
            e1.printStackTrace();
        } catch (DBPApplicationException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Result editInfinityUser(String methodId, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse response) {
        Result result = new Result();
        try {
            if (requestInstance.getParameter("userDetails") == null) {
                ErrorCodeEnum.ERR_21994.setErrorCode(result);
                return result;
            }
            if (requestInstance.getParameter("companyList") == null) {
                ErrorCodeEnum.ERR_21995.setErrorCode(result);
                return result;
            }
            String userDetails = requestInstance.getParameter("userDetails");
            String companyList = requestInstance.getParameter("companyList");
            String accountLevelPermissions = requestInstance.getParameter("accountLevelPermissions");
            String globalLevelPermissions = requestInstance.getParameter("globalLevelPermissions");
            String transactionLimits = requestInstance.getParameter("transactionLimits");
            String removedCompanies = requestInstance.getParameter("removedCompanies");
            String signatoryGroups = requestInstance.getParameter("signatoryGroups");
            Map<String, Object> postParametersMap = new HashMap<>();
            postParametersMap.put("userDetails", stringifyForVelocityTemplate(userDetails));
            postParametersMap.put("companyList", stringifyForVelocityTemplate(companyList));
            postParametersMap.put("accountLevelPermissions", stringifyForVelocityTemplate(accountLevelPermissions));
            postParametersMap.put("globalLevelPermissions", stringifyForVelocityTemplate(globalLevelPermissions));
            postParametersMap.put("transactionLimits", stringifyForVelocityTemplate(transactionLimits));
            postParametersMap.put("removedCompanies", stringifyForVelocityTemplate(removedCompanies));
            postParametersMap.put("signatoryGroups", stringifyForVelocityTemplate(signatoryGroups));
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(requestInstance);
            LOG.error(  "20230327 + dbpServicesClaimsToken: "  + dbpServicesClaimsToken);
            LOG.error(  "20230327 + postParametersMap: "  + postParametersMap.toString());
            JSONObject editInfinityUserResponse = this.infinityUserManagementBusinessDelegate.editInfinityUser(postParametersMap, dbpServicesClaimsToken);
            if (editInfinityUserResponse == null || !editInfinityUserResponse.has("opstatus") || editInfinityUserResponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_21997.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE, ActivityStatusEnum.FAILED, "Infinity user edit failed");
                return result;
            }
            if (editInfinityUserResponse.has("dbpErrMsg")) {
                result = CommonUtilities.constructResultFromJSONObject(editInfinityUserResponse);
                result.addParam(new Param("status", "Failure", "string"));
                return result;
            }
            result = CommonUtilities.constructResultFromJSONObject(editInfinityUserResponse);
        } catch (Exception e) {
            LOG.error("Unexpected Error in edit infinity user: ", e);
            result.addParam(new Param("FailureReason", e.getMessage(), "string"));
            ErrorCodeEnum.ERR_21997.setErrorCode(result);
        }
        return result;
    }

    public Result getAllEligibleRelationalCustomers(String methodId, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse response) {
        Result result = new Result();
        String coreCustomerId = "";
        try {
            if (StringUtils.isBlank(requestInstance.getParameter("coreCustomerId"))) {
                ErrorCodeEnum.ERR_22026.setErrorCode(result);
                return result;
            }
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(requestInstance);
            coreCustomerId = requestInstance.getParameter("coreCustomerId");
            Map<String, Object> postParametersMap = new HashMap<>();
            postParametersMap.put("coreCustomerId", coreCustomerId);
            JSONObject serviceResponse = this.infinityUserManagementBusinessDelegate.getAllEligibleRelationalCustomers(postParametersMap, dbpServicesClaimsToken);
            if (serviceResponse == null || !serviceResponse.has("opstatus") || serviceResponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_22025.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONTRACTS, EventEnum.UPDATE, ActivityStatusEnum.FAILED, "Failed to fetch AllEligibleRelationalCustomers for coreCustomerId : " + coreCustomerId);
                return result;
            }
            if (serviceResponse.has("dbpErrMsg")) {
                result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
                result.addParam(new Param("status", "Failure", "string"));
                return result;
            }
            result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
        } catch (Exception e) {
            LOG.error("Unexpected Error in getAllEligibleRelationalCustomers", e);
            result.addParam(new Param("FailureReason", e.getMessage(), "string"));
            ErrorCodeEnum.ERR_22025.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONTRACTS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch AllEligibleRelationalCustomers for coreCustomerId : " + coreCustomerId);
        }
        return result;
    }

    public Result getCoreCustomerRoleFeatureActionLimits(String methodId, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse response) {
        Result result = new Result();
        String coreCustomerRoleIdList = "";
        try {
            if (StringUtils.isBlank(requestInstance.getParameter("coreCustomerRoleIdList"))) {
                ErrorCodeEnum.ERR_22028.setErrorCode(result);
                return result;
            }
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(requestInstance);
            Map<String, Object> postParametersMap = new HashMap<>();
            coreCustomerRoleIdList = requestInstance.getParameter("coreCustomerRoleIdList").toString();
            coreCustomerRoleIdList = "\"" + coreCustomerRoleIdList.replace("\"", "\\\"") + "\"";
            postParametersMap.put("coreCustomerRoleIdList", coreCustomerRoleIdList);
            JSONObject serviceResponse = this.infinityUserManagementBusinessDelegate.getCoreCustomerRoleFeatureActionLimits(postParametersMap, dbpServicesClaimsToken);
            if (serviceResponse == null || !serviceResponse.has("opstatus") || serviceResponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_22027.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONTRACTS, EventEnum.UPDATE, ActivityStatusEnum.FAILED, "Failed to fetch CoreCustomerRoleFeatureActionLimits for coreCustomerRoleIdList : " + coreCustomerRoleIdList);
                return result;
            }
            if (serviceResponse.has("dbpErrMsg")) {
                result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
                result.addParam(new Param("status", "Failure", "string"));
                return result;
            }
            result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
        } catch (Exception e) {
            LOG.error("Unexpected Error in getCoreCustomerRoleFeatureActionLimits", e);
            result.addParam(new Param("FailureReason", e.getMessage(), "string"));
            ErrorCodeEnum.ERR_22027.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONTRACTS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch CoreCustomerRoleFeatureActionLimits for coreCustomerRoleIdList : " + coreCustomerRoleIdList);
        }
        return result;
    }

    public Result getRelativeCoreCustomerContractDetails(String methodId, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse response) {
        Result result = new Result();
        String coreCustomerId = "";
        try {
            if (StringUtils.isBlank(requestInstance.getParameter("coreCustomerId"))) {
                ErrorCodeEnum.ERR_22029.setErrorCode(result);
                return result;
            }
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(requestInstance);
            coreCustomerId = requestInstance.getParameter("coreCustomerId");
            Map<String, Object> postParametersMap = new HashMap<>();
            postParametersMap.put("coreCustomerId", coreCustomerId);
            JSONObject serviceResponse = this.infinityUserManagementBusinessDelegate.getRelativeCoreCustomerContractDetails(postParametersMap, dbpServicesClaimsToken);
            if (serviceResponse == null || !serviceResponse.has("opstatus") || serviceResponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_22029.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONTRACTS, EventEnum.UPDATE, ActivityStatusEnum.FAILED, "Failed to fetch RelativeCoreCustomerContractDetails for coreCustomerId : " + coreCustomerId);
                return result;
            }
            if (serviceResponse.has("dbpErrMsg")) {
                result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
                result.addParam(new Param("status", "Failure", "string"));
                return result;
            }
            result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
        } catch (Exception e) {
            LOG.error("Unexpected Error in getRelativeCoreCustomerContractDetails", e);
            result.addParam(new Param("FailureReason", e.getMessage(), "string"));
            ErrorCodeEnum.ERR_22029.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONTRACTS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch RelativeCoreCustomerContractDetails for coreCustomerId : " + coreCustomerId);
        }
        return result;
    }

    public Result getCoreCustomerContractDetails(String methodId, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse response) {
        Result result = new Result();
        String coreCustomerId = "";
        try {
            if (StringUtils.isBlank(requestInstance.getParameter("coreCustomerId"))) {
                ErrorCodeEnum.ERR_22026.setErrorCode(result);
                return result;
            }
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(requestInstance);
            coreCustomerId = requestInstance.getParameter("coreCustomerId");
            Map<String, Object> postParametersMap = new HashMap<>();
            postParametersMap.put("coreCustomerId", coreCustomerId);
            JSONObject serviceResponse = this.infinityUserManagementBusinessDelegate.getCoreCustomerContractDetails(postParametersMap, dbpServicesClaimsToken);
            if (serviceResponse == null || !serviceResponse.has("opstatus") || serviceResponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_22030.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONTRACTS, EventEnum.UPDATE, ActivityStatusEnum.FAILED, "Failed to fetch CoreCustomerContractDetails for coreCustomerId : " + coreCustomerId);
                return result;
            }
            if (serviceResponse.has("dbpErrMsg")) {
                Dataset recordsDS = new Dataset();
                recordsDS.setId("contracts");
                result.addDataset(recordsDS);
                return result;
            }
            result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
        } catch (Exception e) {
            LOG.error("Unexpected Error in getCoreCustomerContractDetails", e);
            result.addParam(new Param("FailureReason", e.getMessage(), "string"));
            ErrorCodeEnum.ERR_22030.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONTRACTS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch CoreCustomerContractDetails for coreCustomerId : " + coreCustomerId);
        }
        return result;
    }

    public Result getInfinityUserContractDetails(String methodId, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse response) {
        Result result = new Result();
        String userId = "";
        try {
            if (StringUtils.isBlank(requestInstance.getParameter("userId"))) {
                ErrorCodeEnum.ERR_22081.setErrorCode(result);
                return result;
            }
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(requestInstance);
            userId = requestInstance.getParameter("userId");
            Map<String, Object> postParametersMap = new HashMap<>();
            postParametersMap.put("userId", userId);
            JSONObject serviceResponse = this.infinityUserManagementBusinessDelegate.getInfinityUserContractDetails(postParametersMap, dbpServicesClaimsToken);
            if (serviceResponse == null || !serviceResponse.has("opstatus") || serviceResponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_22077.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONTRACTS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch infinity user contract details for userId : " + userId);
                return result;
            }
            if (serviceResponse.has("dbpErrMsg")) {
                result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
                result.addParam(new Param("status", "Failure", "string"));
                return result;
            }
            result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONTRACTS, EventEnum.SEARCH, ActivityStatusEnum.SUCCESSFUL, "Succefully fetched Infinity User Contract Details for userId :" + userId);
        } catch (Exception e) {
            LOG.error("Unexpected Error in getInfinityUserContractDetails", e);
            result.addParam(new Param("FailureReason", e.getMessage(), "string"));
            ErrorCodeEnum.ERR_22077.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONTRACTS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch infinity user contract details for userId : " + userId);
        }
        return result;
    }

    public Result getInfinityUserAccounts(String methodId, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse response) {
        Result result = new Result();
        String userId = "";
        try {
            if (StringUtils.isBlank(requestInstance.getParameter("userId"))) {
                ErrorCodeEnum.ERR_22081.setErrorCode(result);
                return result;
            }
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(requestInstance);
            userId = requestInstance.getParameter("userId");
            Map<String, Object> postParametersMap = new HashMap<>();
            postParametersMap.put("userId", userId);
            JSONObject serviceResponse = this.infinityUserManagementBusinessDelegate.getInfinityUserAccounts(postParametersMap, dbpServicesClaimsToken);
            if (serviceResponse == null || !serviceResponse.has("opstatus") || serviceResponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_22078.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch infinity user Accounts for userId : " + userId);
                return result;
            }
            if (serviceResponse.has("dbpErrMsg")) {
                result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
                result.addParam(new Param("status", "Failure", "string"));
                return result;
            }
            result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.SUCCESSFUL, "Succefully fetched Infinity User Accounts for userId :" + userId);
        } catch (Exception e) {
            LOG.error("Unexpected Error in getInfinityUserAccounts", e);
            result.addParam(new Param("FailureReason", e.getMessage(), "string"));
            ErrorCodeEnum.ERR_22078.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch infinity user Accounts for userId : " + userId);
        }
        return result;
    }

    public Result getInfinityUserFeatureActions(String methodId, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse response) {
        Result result = new Result();
        String userId = "";
        try {
            if (StringUtils.isBlank(requestInstance.getParameter("userId"))) {
                ErrorCodeEnum.ERR_22081.setErrorCode(result);
                return result;
            }
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(requestInstance);
            userId = requestInstance.getParameter("userId");
            Map<String, Object> postParametersMap = new HashMap<>();
            postParametersMap.put("userId", userId);
            JSONObject serviceResponse = this.infinityUserManagementBusinessDelegate.getInfinityUserFeatureActions(postParametersMap, dbpServicesClaimsToken);
            if (serviceResponse == null || !serviceResponse.has("opstatus") || serviceResponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_22079.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch infinity user features and actions for userId : " + userId);
                return result;
            }
            if (serviceResponse.has("dbpErrMsg")) {
                result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
                result.addParam(new Param("status", "Failure", "string"));
                return result;
            }
            result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.SUCCESSFUL, "Succefully fetched Infinity User features and actions for userId :" + userId);
        } catch (Exception e) {
            LOG.error("Unexpected Error in getInfinityUserFeatureActions", e);
            result.addParam(new Param("FailureReason", e.getMessage(), "string"));
            ErrorCodeEnum.ERR_22079.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch infinity user features and actions for userId : " + userId);
        }
        return result;
    }

    public Result getInfinityUserLimits(String methodId, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse response) {
        Result result = new Result();
        String userId = "";
        try {
            if (StringUtils.isBlank(requestInstance.getParameter("userId"))) {
                ErrorCodeEnum.ERR_22081.setErrorCode(result);
                return result;
            }
            String dbpServicesClaimsToken = DBPServices.getDBPServicesClaimsToken(requestInstance);
            userId = requestInstance.getParameter("userId");
            Map<String, Object> postParametersMap = new HashMap<>();
            postParametersMap.put("userId", userId);
            JSONObject serviceResponse = this.infinityUserManagementBusinessDelegate.getInfinityUserLimits(postParametersMap, dbpServicesClaimsToken);
            if (serviceResponse == null || !serviceResponse.has("opstatus") || serviceResponse
                    .getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_22080.setErrorCode(result);
                result.addParam(new Param("status", "Failure", "string"));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch infinity user limits for userId : " + userId);
                return result;
            }
            if (serviceResponse.has("dbpErrMsg")) {
                result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
                result.addParam(new Param("status", "Failure", "string"));
                return result;
            }
            result = CommonUtilities.constructResultFromJSONObject(serviceResponse);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.SUCCESSFUL, "Succefully fetched Infinity User limits for userId :" + userId);
        } catch (Exception e) {
            LOG.error("Unexpected Error in getInfinityUserLimits", e);
            result.addParam(new Param("FailureReason", e.getMessage(), "string"));
            ErrorCodeEnum.ERR_22080.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.SEARCH, ActivityStatusEnum.FAILED, "Failed to fetch infinity user limits for userId : " + userId);
        }
        return result;
    }

    private String stringifyForVelocityTemplate(String str) {
        if (StringUtils.isBlank(str))
            return "\"\"";
        if (str.contains("\\"))
            str = str.replace("\\", "\\\\");
        return "\"" + str.replace("\"", "\\\"") + "\"";
    }
}
