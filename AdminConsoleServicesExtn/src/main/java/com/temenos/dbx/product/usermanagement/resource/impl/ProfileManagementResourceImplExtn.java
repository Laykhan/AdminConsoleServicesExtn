package com.temenos.dbx.product.usermanagement.resource.impl;

import com.dbp.core.api.factory.impl.DBPAPIAbstractFactoryImpl;
import com.dbp.core.fabric.extn.DBPServiceInvocationWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kony.dbp.exception.ApplicationException;
import com.kony.dbputilities.exceptions.HttpCallException;
import com.kony.dbputilities.sessionmanager.SessionScope;
import com.kony.dbputilities.util.BundleConfigurationHandler;
import com.kony.dbputilities.util.EnvironmentConfigurationsHandler;
import com.kony.dbputilities.util.ErrorCodeEnum;
import com.kony.dbputilities.util.HelperMethods;
import com.kony.dbputilities.util.IntegrationTemplateURLFinder;
import com.kony.dbputilities.util.JSONUtil;
import com.kony.dbputilities.util.ServiceCallHelper;
import com.kony.dbputilities.util.logger.LoggerUtil;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.JSONToResult;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;
import com.temenos.dbx.product.dto.BackendIdentifierDTO;
import com.temenos.dbx.product.dto.CustomerDTO;
import com.temenos.dbx.product.dto.DBXResult;
import com.temenos.dbx.product.dto.MemberSearchBean;
import com.temenos.dbx.product.usermanagement.backenddelegate.api.BackendIdentifiersBackendDelegate;
import com.temenos.dbx.product.usermanagement.backenddelegate.impl.BackendIdentifiersBackendDelegateimpl;
import com.temenos.dbx.product.usermanagement.backenddelegate.impl.ProfileManagementBackendDelegateImpl;
import com.temenos.dbx.product.usermanagement.businessdelegate.api.ProfileManagementBusinessDelegate;
import com.temenos.dbx.product.usermanagement.resource.api.InfinityUserManagementResource;
import com.temenos.dbx.product.usermanagement.resource.api.ProfileManagementResource;
import com.temenos.dbx.product.utils.CustomerUtils;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class ProfileManagementResourceImplExtn implements ProfileManagementResource {
    private static LoggerUtil logger = new LoggerUtil(ProfileManagementResourceImpl.class);
    private static final Logger LOG = Logger.getLogger(ProfileManagementResourceImplExtn.class);

    public Result getCustomerForUserResponse(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) {
        Result result = new Result();
        Map<String, String> inputParams = HelperMethods.getInputParamMap(inputArray);
        String id = inputParams.get("Customer_id");
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(id);
        ProfileManagementBusinessDelegate businessDelegate = (ProfileManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(ProfileManagementBusinessDelegate.class);
        DBXResult dbxResult = businessDelegate.getCustomerForUserResponse(customerDTO, dcRequest.getHeaderMap());
        if (dbxResult.getResponse() != null) {
            Record record = new Record();
            Map<String, String> map = (Map<String, String>)dbxResult.getResponse();
            for (Map.Entry<String, String> entry : map.entrySet())
                record.addParam(entry.getKey(), entry.getValue());
            Dataset dataset = new Dataset();
            dataset.setId("customer");
            dataset.addRecord(record);
            result.addDataset(dataset);
            return result;
        }
        ErrorCodeEnum.ERR_10003.setErrorCode(result);
        return result;
    }

    public Result getUserDetailsConcurrent(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) {
        Result result = new Result();
        SessionScope.clear(dcRequest);
        Map<String, String> inputmap = HelperMethods.getInputParamMap(inputArray);
        String Username = inputmap.get("Username");
        String Customer_id = "";
        String coreCustomerID = "";
        String Bank_id = inputmap.get("Bank_id");
        Map<String, String> loggedInUserInfo = HelperMethods.getCustomerFromAPIDBPIdentityService(dcRequest);
        boolean IS_Integrated = IntegrationTemplateURLFinder.isIntegrated;
        if (!HelperMethods.isAuthenticationCheckRequiredForService(loggedInUserInfo)) {
            Customer_id = inputmap.get("Customer_id");
            if (StringUtils.isNotBlank(Customer_id)) {
                BackendIdentifiersBackendDelegateimpl backendDelegateimpl = new BackendIdentifiersBackendDelegateimpl();
                BackendIdentifierDTO backendIdentifierDTO = new BackendIdentifierDTO();
                backendIdentifierDTO.setBackendId(Customer_id);
                if (IS_Integrated) {
                    backendIdentifierDTO
                            .setBackendType(IntegrationTemplateURLFinder.getBackendURL("BackendType"));
                } else {
                    backendIdentifierDTO.setBackendType("CORE");
                }
                DBXResult dBXResult = backendDelegateimpl.get(backendIdentifierDTO, dcRequest.getHeaderMap());
                if (dBXResult.getResponse() != null) {
                    backendIdentifierDTO = (BackendIdentifierDTO)dBXResult.getResponse();
                    Customer_id = backendIdentifierDTO.getCustomer_id();
                    coreCustomerID = backendIdentifierDTO.getBackendId();
                }
            }
        }
        DBXResult dbxResult = new DBXResult();
        if (StringUtils.isBlank(Username) && StringUtils.isBlank(Customer_id)) {
            loggedInUserInfo = HelperMethods.getUserFromIdentityService(dcRequest);
            if (HelperMethods.isAuthenticationCheckRequiredForService(loggedInUserInfo)) {
                Username = loggedInUserInfo.get("UserName");
                Customer_id = loggedInUserInfo.get("Customer_id");
                Bank_id = loggedInUserInfo.get("Bank_id");
            }
        }
        if (StringUtils.isBlank(Customer_id)) {
            result.addParam("errmsg", "Unable to find userId");
            return result;
        }
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(Customer_id);
        customerDTO = (CustomerDTO)customerDTO.loadDTO();
        if (customerDTO == null) {
            result.addParam("errmsg", "Unable to find userId");
            return result;
        }
        if (StringUtils.isBlank(coreCustomerID)) {
            BackendIdentifiersBackendDelegateimpl backendDelegateimpl = new BackendIdentifiersBackendDelegateimpl();
            BackendIdentifierDTO backendIdentifierDTO = new BackendIdentifierDTO();
            backendIdentifierDTO.setCustomer_id(Customer_id);
            if (IS_Integrated) {
                backendIdentifierDTO
                        .setBackendType(IntegrationTemplateURLFinder.getBackendURL("BackendType"));
            } else {
                backendIdentifierDTO.setBackendType("CORE");
            }
            dbxResult = backendDelegateimpl.get(backendIdentifierDTO, dcRequest.getHeaderMap());
            if (dbxResult.getResponse() != null) {
                backendIdentifierDTO = (BackendIdentifierDTO)dbxResult.getResponse();
                coreCustomerID = backendIdentifierDTO.getBackendId();
            }
        }
        ProfileManagementBusinessDelegate businessDelegate = (ProfileManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(ProfileManagementBusinessDelegate.class);
        dbxResult = businessDelegate.getUserResponse(customerDTO, dcRequest.getHeaderMap());
        if (dbxResult.getResponse() != null) {
            JsonObject resultObject = (JsonObject)dbxResult.getResponse();
            JsonObject jsonObject = (resultObject.has("customer") && resultObject.get("customer").isJsonArray() && resultObject.get("customer").getAsJsonArray().size() > 0 && resultObject.get("customer").getAsJsonArray().get(0).isJsonObject()) ? resultObject.get("customer").getAsJsonArray().get(0).getAsJsonObject() : new JsonObject();
            if (JSONUtil.isJsonNotNull((JsonElement)jsonObject) && JSONUtil.hasKey(jsonObject, "customers") && jsonObject
                    .get("customers").isJsonArray() &&
                    JSONUtil.getJsonArrary(jsonObject, "customers").size() > 0) {
                JsonArray array = JSONUtil.getJsonArrary(jsonObject, "customers");
                for (JsonElement element : array) {
                    JsonObject coreCustomerObject = element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
                    String coreCustomerId = coreCustomerObject.has("coreCustomerId") ? coreCustomerObject.get("coreCustomerId").getAsString() : "";
                    if (StringUtils.isNotBlank(coreCustomerId) && coreCustomerId.equalsIgnoreCase(coreCustomerID)) {
                        coreCustomerObject.addProperty("isPrimary", "true");
                        continue;
                    }
                    coreCustomerObject.addProperty("isPrimary", "false");
                }
            }
            result = JSONToResult.convert(resultObject.toString());
        }
        return result;
    }

    public Result getUserResponseForAlerts(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) {
        Result result = new Result();
        HashMap<String, Object> identityParam = new HashMap<>();
        Map<String, Object> identitytHeaders = new HashMap<>();
        String dbpAppKey = EnvironmentConfigurationsHandler.getValue("AC_DBP_APP_KEY");
        String dbpAppSecret = EnvironmentConfigurationsHandler.getValue("AC_DBP_APP_SECRET");
        String sharedSecret = EnvironmentConfigurationsHandler.getValue("AC_DBP_SHARED_SECRET");
        identitytHeaders.put("X-Kony-App-Key", dbpAppKey);
        identitytHeaders.put("X-Kony-App-Secret", dbpAppSecret);
        identitytHeaders.put("x-kony-dbp-api-access-token", sharedSecret);
        String authToken = "";
        if (StringUtils.isBlank(dbpAppKey) || StringUtils.isBlank(dbpAppSecret) ||
                StringUtils.isBlank(sharedSecret))
            logger.error("Error while file fetching DBP_CORE_APPKEY or DBP_CORE_SECRET or DBP_CORE_SHARED_SECRET");
        JsonObject identityResult = ServiceCallHelper.invokeServiceAndGetJson(identityParam, identitytHeaders, "api.login");
        if (identityResult.has("claimsToken")) {
            authToken = identityResult.get("claimsToken").getAsString();
        } else {
            logger.error("Error while file fetching auth Token in combined statements");
            return result;
        }
        if (StringUtils.isNotBlank(authToken)) {
            Map<String, Object> headers = dcRequest.getHeaderMap();
            headers.put("X-Kony-Authorization", authToken);
            headers.put("backendToken", authToken);
            Map<String, Object> inputParams = new HashMap<>();
            inputParams.put("backendToken", authToken);
            inputParams.put("Customer_id", dcRequest
                    .getParameter("customerId".toString()));
            dcRequest.addRequestParam_("Customer_id", dcRequest
                    .getParameter("customerId".toString()));
            try {
                result = JSONToResult.convert(DBPServiceInvocationWrapper.invokePassThroughServiceAndGetString("DBPServices", null, "getCustomerCommunication", inputParams, headers, dcRequest));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result.getDatasetById("records") != null) {
            result.getDatasetById("records").setId("customer");
            if (result.getDatasetById("customer").getAllRecords().size() > 0) {
                Record record = result.getDatasetById("customer").getRecord(0);
                record.addParam("id", dcRequest.getParameter("customerId"));
                record.addParam("FirstName", record.getParamValueByName("userfirstname"));
            }
        }
        return result;
    }

    public Result createCustomer(String methodID, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) {
        Result result = new Result();
        Map<String, String> map = HelperMethods.getInputParamMap(inputArray);
        CustomerDTO customerDTO = CustomerUtils.buildCustomerDTO(null, map, request);
        ProfileManagementBusinessDelegate businessDelegate = (ProfileManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(ProfileManagementBusinessDelegate.class);
        DBXResult dbxResult = businessDelegate.createCustomer(customerDTO, request.getHeaderMap());
        if (dbxResult.getResponse() != null) {
            result = JSONToResult.convert(((JsonObject)dbxResult.getResponse()).toString());
        } else {
            HelperMethods.addError(result, dbxResult);
        }
        return result;
    }

    public Result getUserDetailsToAdmin(String methodID, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) {
        Result result = new Result();
        String adminId = HelperMethods.getAPIUserIdFromSession(request);
        Map<String, String> inputParams = HelperMethods.getInputParamMap(inputArray);
        try {
            if (!HelperMethods.isAdmin(request, adminId))
                HelperMethods.setValidationMsg("logged in user is not admin", request, result);
        } catch (HttpCallException e) {
            HelperMethods.setValidationMsg("logged in user is not admin", request, result);
            return result;
        }
        CustomerDTO customerDTO = new CustomerDTO();
        String customerID = getUserId(request, inputParams.get("accountNumber"));
        if (StringUtils.isBlank(customerID)) {
            HelperMethods.setValidationMsg("Invalid Account number", request, result);
            return result;
        }
        customerDTO.setId(customerID);
        ProfileManagementBusinessDelegate businessDelegate = (ProfileManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(ProfileManagementBusinessDelegate.class);
        DBXResult dbxResult = businessDelegate.getCustomerDetailsToAdmin(customerDTO, request.getHeaderMap());
        if (dbxResult.getResponse() == null) {
            HelperMethods.setValidationMsg("Unable to fetch user details", request, result);
            return result;
        }
        result = JSONToResult.convert(((JsonObject)dbxResult.getResponse()).toString());
        return result;
    }

    private String getUserId(DataControllerRequest dcRequest, String acctNum) {
        String filter = "Account_id eq " + acctNum;
        try {
            Result account = HelperMethods.callGetApi(dcRequest, filter, HelperMethods.getHeaders(dcRequest), "customeraccountsview.readRecord");
            return HelperMethods.getFieldValue(account, "Customer_id");
        } catch (HttpCallException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Result updateProfile(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) {
        Result result = new Result();
        logger = new LoggerUtil(ProfileManagementResourceImpl.class);
        Map<String, String> inputParams = HelperMethods.getInputParamMap(inputArray);
        String customerID = "";
        Map<String, String> loggedInUserInfo = HelperMethods.getCustomerFromAPIDBPIdentityService(dcRequest);
        if (!HelperMethods.isAuthenticationCheckRequiredForService(loggedInUserInfo)) {
            customerID = inputParams.get("Customer_id");
            if (StringUtils.isEmpty(customerID))
                customerID = dcRequest.getParameter("Customer_id");
            if (StringUtils.isBlank(customerID))
                customerID = inputParams.get("coreCustomerID");
        } else {
            customerID = HelperMethods.getCustomerIdFromSession(dcRequest);
        }
        boolean isCoreCustomerIdPresent = false;
        DBXResult dbxResult = new DBXResult();
        boolean IS_Integrated = IntegrationTemplateURLFinder.isIntegrated;
        if (StringUtils.isNotBlank(customerID)) {
            BackendIdentifierDTO backendIdentifierDTO = new BackendIdentifierDTO();
            backendIdentifierDTO.setBackendId(customerID);
            if (IS_Integrated) {
                backendIdentifierDTO
                        .setBackendType(IntegrationTemplateURLFinder.getBackendURL("BackendType"));
            } else {
                backendIdentifierDTO.setBackendType("CORE");
            }
            try {
                dbxResult = ((BackendIdentifiersBackendDelegate)DBPAPIAbstractFactoryImpl.getBackendDelegate(BackendIdentifiersBackendDelegate.class)).get(backendIdentifierDTO, dcRequest.getHeaderMap());
            } catch (ApplicationException e) {
                logger.error("exception while fetching Backend Identifier", (Exception)e);
            }
            if (dbxResult.getResponse() != null) {
                BackendIdentifierDTO identifierDTO = (BackendIdentifierDTO)dbxResult.getResponse();
                customerID = identifierDTO.getCustomer_id();
                isCoreCustomerIdPresent = true;
            } else {
                backendIdentifierDTO = new BackendIdentifierDTO();
                backendIdentifierDTO.setCustomer_id(customerID);
                if (IS_Integrated) {
                    backendIdentifierDTO
                            .setBackendType(IntegrationTemplateURLFinder.getBackendURL("BackendType"));
                } else {
                    backendIdentifierDTO.setBackendType("CORE");
                }
                try {
                    dbxResult = ((BackendIdentifiersBackendDelegate)DBPAPIAbstractFactoryImpl.getBackendDelegate(BackendIdentifiersBackendDelegate.class)).get(backendIdentifierDTO, dcRequest.getHeaderMap());
                } catch (ApplicationException e) {
                    logger.error("exception while fetching Backend Identifier", (Exception)e);
                }
                if (dbxResult.getResponse() != null) {
                    BackendIdentifierDTO identifierDTO = (BackendIdentifierDTO)dbxResult.getResponse();
                    customerID = identifierDTO.getCustomer_id();
                    isCoreCustomerIdPresent = true;
                }
            }
        }
        CustomerDTO customerDto = new CustomerDTO();
        if (StringUtils.isBlank(customerID) || customerDto.loadDTO(customerID) == null) {
            logger.debug("customer table entry is not found for given customerID " + customerID);
            return result;
        }
        if (isCoreCustomerIdPresent) {
            inputParams.put("FirstName", null);
            inputParams.put("LastName", null);
            inputParams.put("dateOfBirth", null);
            inputParams.put("TaxId", null);
        }
        CustomerDTO customerDTO = CustomerUtils.buildCustomerDTOforUpdate(customerID, inputParams, dcRequest, isCoreCustomerIdPresent, IS_Integrated);
        result = new Result();
        ProfileManagementBusinessDelegate businessDelegate = (ProfileManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(ProfileManagementBusinessDelegate.class);
        dbxResult = businessDelegate.updateCustomer(customerDTO, dcRequest.getHeaderMap());
        if (dbxResult.getResponse() != null) {
            JsonObject jsonObject = (JsonObject)dbxResult.getResponse();
            result = JSONToResult.convert(jsonObject.toString());
        }
        return result;
    }

    public Result searchCustomer(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) {
        Result processedResult = new Result();
        if (StringUtils.isNotBlank(dcRequest.getParameter("_sortVariable")) && dcRequest
                .getParameter("_sortVariable").contains(" ")) {
            ErrorCodeEnum.ERR_10333.setErrorCode(processedResult);
            return processedResult;
        }
        if (StringUtils.isNotBlank(dcRequest.getParameter("_sortDirection")) && (dcRequest
                .getParameter("_sortDirection").contains(" ") || (
                !dcRequest.getParameter("_sortDirection").equalsIgnoreCase("ASC") &&
                        !dcRequest.getParameter("_sortDirection").equalsIgnoreCase("DESC")))) {
            ErrorCodeEnum.ERR_10334.setErrorCode(processedResult);
            return processedResult;
        }
        MemberSearchBean memberSearchBean = new MemberSearchBean();
        memberSearchBean.setSearchType(dcRequest.getParameter("_searchType"));
        memberSearchBean.setMemberId(dcRequest.getParameter("_id"));
        memberSearchBean.setCustomerId(dcRequest.getParameter("_customerId"));
        memberSearchBean.setCustomerName(dcRequest.getParameter("_name"));
        memberSearchBean.setSsn(dcRequest.getParameter("_SSN"));
        memberSearchBean.setCustomerUsername(dcRequest.getParameter("_username"));
        memberSearchBean.setCustomerPhone(dcRequest.getParameter("_phone"));
        memberSearchBean.setCustomerEmail(dcRequest.getParameter("_email"));
        memberSearchBean.setIsStaffMember(dcRequest.getParameter("_IsStaffMember"));
        memberSearchBean.setCardorAccountnumber(dcRequest.getParameter("_cardorAccountnumber"));
        memberSearchBean.setTin(dcRequest.getParameter("_TIN"));
        memberSearchBean.setCustomerGroup(dcRequest.getParameter("_group"));
        memberSearchBean.setCustomerIDType(dcRequest.getParameter("_IDType"));
        memberSearchBean.setCustomerIDValue(dcRequest.getParameter("_IDValue"));
        memberSearchBean.setCustomerCompanyId(dcRequest.getParameter("_companyId"));
        memberSearchBean.setCustomerRequest(dcRequest.getParameter("_requestID"));
        memberSearchBean.setBranchIDS(dcRequest.getParameter("_branchIDS"));
        memberSearchBean.setProductIDS(dcRequest.getParameter("_productIDS"));
        memberSearchBean.setCityIDS(dcRequest.getParameter("_cityIDS"));
        memberSearchBean.setEntitlementIDS(dcRequest.getParameter("_entitlementIDS"));
        memberSearchBean.setGroupIDS(dcRequest.getParameter("_groupIDS"));
        memberSearchBean.setCustomerStatus(dcRequest.getParameter("_customerStatus"));
        memberSearchBean.setBeforeDate(dcRequest.getParameter("_before"));
        memberSearchBean.setAfterDate(dcRequest.getParameter("_after"));
        memberSearchBean.setSortVariable(dcRequest.getParameter("_sortVariable"));
        memberSearchBean.setSortDirection(dcRequest.getParameter("_sortDirection"));
        memberSearchBean.setPageOffset(dcRequest.getParameter("_pageOffset"));
        memberSearchBean.setPageSize(dcRequest.getParameter("_pageSize"));
        memberSearchBean.setDateOfBirth(dcRequest.getParameter("_dateOfBirth"));
        if (StringUtils.isBlank(memberSearchBean.getSearchType())) {
            ErrorCodeEnum.ERR_10334.setErrorCode(processedResult);
            return processedResult;
        }
        processedResult
                .addParam(new Param("SortVariable", memberSearchBean.getSortVariable(), "string"));
        processedResult
                .addParam(new Param("SortDirection", memberSearchBean.getSortDirection(), "string"));
        processedResult.addParam(new Param("PageOffset",
                String.valueOf(memberSearchBean.getPageOffset()), "string"));
        processedResult.addParam(new Param("PageSize",
                String.valueOf(memberSearchBean.getPageSize()), "string"));
        String id = dcRequest.getParameter("_id");
        String name = dcRequest.getParameter("_name");
        String SSN = dcRequest.getParameter("_SSN");
        String username = dcRequest.getParameter("_username");
        String phone = dcRequest.getParameter("_phone");
        String email = dcRequest.getParameter("_email");
        String accNo = dcRequest.getParameter("_cardorAccountnumber");
        String Tin = dcRequest.getParameter("_TIN");
        String IDType = dcRequest.getParameter("_IDType");
        String IDValue = dcRequest.getParameter("_IDValue");
        String CompanyId = dcRequest.getParameter("_companyId");
        String searchType = dcRequest.getParameter("_searchType");
        String dateOfBirth = dcRequest.getParameter("_dateOfBirth");
        String customerId = dcRequest.getParameter("_customerId");
        if ((StringUtils.isBlank(id) || id.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(name) || name.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(dateOfBirth) || dateOfBirth.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(SSN) || SSN.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(username) || username.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(phone) || phone.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(email) || email.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(accNo) || accNo.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(Tin) || Tin.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(IDType) || IDType.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(IDValue) || IDValue.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(CompanyId) || CompanyId.equalsIgnoreCase("null")) && (
                StringUtils.isBlank(customerId) || customerId.equalsIgnoreCase("null")) && searchType
                .equalsIgnoreCase("CUSTOMER_SEARCH")) {
            processedResult.addParam(new Param("TotalResultsFound", "0", "int"));
            Dataset recordsDS = new Dataset();
            recordsDS.setId("records");
            processedResult.addDataset(recordsDS);
            return processedResult;
        }
        ProfileManagementBusinessDelegate businessDelegate = (ProfileManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(ProfileManagementBusinessDelegate.class);
        Map<String, String> configurations = BundleConfigurationHandler.fetchBundleConfigurations("C360", dcRequest);
        
        LOG.error("20230328 memberSearchBeanCustomerEmail: " + memberSearchBean.getCustomerEmail());
        LOG.error("20230328 memberSearchBeanSSN: " + memberSearchBean.getSsn());
        
        DBXResult dbxResult = businessDelegate.searchCustomer(configurations, memberSearchBean, dcRequest.getHeaderMap());
        if (dbxResult.getResponse() != null) {
            JsonObject jsonObject = (JsonObject)dbxResult.getResponse();
            Result result = JSONToResult.convert(jsonObject.toString());
            for (Param param : processedResult.getAllParams())
                result.addParam(param);
            return result;
        }
        HelperMethods.addError(processedResult, dbxResult);
        return processedResult;
    }

    private boolean memberPresent(String memberId, Map<String, Object> headerMap) {
        String filter = "id eq " + memberId;
        Map<String, Object> input = new HashMap<>();
        input.put("$filter", filter);
        JsonObject result = ServiceCallHelper.invokeServiceAndGetJson(input, headerMap, "Membership.readRecord");
        if (result.has("membership")) {
            JsonArray jsonArray = result.get("membership").isJsonArray() ? result.get("membership").getAsJsonArray() : new JsonArray();
            return (jsonArray.size() > 0);
        }
        return false;
    }

    public Result getBasicInformation(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) {
        Result result = new Result();
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(dcRequest.getParameter("Customer_id"));
        customerDTO = (CustomerDTO)customerDTO.loadDTO();
        boolean isCustomerSearch = false;
        if (customerDTO == null) {
            isCustomerSearch = true;
            customerDTO = new CustomerDTO();
            customerDTO.setId(dcRequest.getParameter("Customer_id"));
            customerDTO.setUserName(dcRequest.getParameter("userName"));
        } else if (customerDTO.getId().equals(customerDTO.getUserName()) ||
                memberPresent(dcRequest.getParameter("Customer_id"), dcRequest.getHeaderMap())) {
            isCustomerSearch = true;
        }
        ProfileManagementBusinessDelegate businessDelegate = (ProfileManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(ProfileManagementBusinessDelegate.class);
        Map<String, String> configurations = BundleConfigurationHandler.fetchBundleConfigurations("C360", dcRequest);
        DBXResult dbxResult = businessDelegate.getBasicInformation(configurations, customerDTO, dcRequest
                .getHeaderMap(), isCustomerSearch);
        if (dbxResult.getResponse() != null) {
            JsonObject jsonObject = (JsonObject)dbxResult.getResponse();
            result = JSONToResult.convert(jsonObject.toString());
        }
        return result;
    }

    public Result getByUserName(String methodID, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) {
        Map<String, String> map = HelperMethods.getInputParamMap(inputArray);
        String userName = map.get("UserName");
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setUserName(userName);
        ProfileManagementBackendDelegateImpl backendDelegateImpl = new ProfileManagementBackendDelegateImpl();
        DBXResult dbxResult = backendDelegateImpl.get(customerDTO, request.getHeaderMap());
        JsonObject jsonObject = (JsonObject)dbxResult.getResponse();
        return JSONToResult.convert(jsonObject.toString());
    }

    public Result get(String methodID, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) {
        Map<String, String> map = HelperMethods.getInputParamMap(inputArray);
        String userName = map.get("UserName");
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setUserName(userName);
        ProfileManagementBackendDelegateImpl backendDelegateImpl = new ProfileManagementBackendDelegateImpl();
        DBXResult dbxResult = backendDelegateImpl.get(customerDTO, request.getHeaderMap());
        JsonObject jsonObject = (JsonObject)dbxResult.getResponse();
        JsonObject customer = new JsonObject();
        if (jsonObject.has("customer")) {
            JsonArray customers = !jsonObject.get("customer").isJsonNull() ? jsonObject.get("customers").getAsJsonArray() : new JsonArray();
            customer = (customers.size() > 0) ? customers.get(0).getAsJsonObject() : new JsonObject();
            String customerTypeID = customer.has("CustomerType_id") ? customer.get("CustomerType_id").getAsString() : "";
            boolean isCustomerPresent_in_dbxDB = false;
            String isCombinedUser = (customer.has("isCombinedUser") && !customer.get("isCombinedUser").isJsonNull()) ? customer.get("isCombinedUser").getAsString() : "false";
            if (HelperMethods.getBusinessUserTypes().contains(customerTypeID) || Boolean.parseBoolean(isCombinedUser))
                isCustomerPresent_in_dbxDB = true;
            if (!isCustomerPresent_in_dbxDB) {
                ProfileManagementBusinessDelegate businessDelegate = (ProfileManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(ProfileManagementBusinessDelegate.class);
                dbxResult = businessDelegate.get(customerDTO, request.getHeaderMap());
                jsonObject = (JsonObject)dbxResult.getResponse();
            }
        }
        return JSONToResult.convert(jsonObject.toString());
    }

    public Result checkifUserEnrolled(String methodID, Object[] inputArray, DataControllerRequest request, DataControllerResponse dcResponse) {
        Result result = new Result();
        Map<String, String> inputParams = HelperMethods.getInputParamMap(inputArray);
        String customerlastname = inputParams.get("LastName");
        String ssn = inputParams.get("Ssn");
        String dob = inputParams.get("DateOfBirth");
        if (StringUtils.isBlank(customerlastname) || StringUtils.isBlank(ssn) || StringUtils.isBlank(dob)) {
            ErrorCodeEnum.ERR_10183.setErrorCode(result);
            Param p = new Param("errmsg", "Please provide valid Details.", "String");
            result.addParam(p);
            return result;
        }
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setDateOfBirth(dob);
        customerDTO.setSsn(ssn);
        customerDTO.setLastName(customerlastname);
        ProfileManagementBusinessDelegate managementBusinessDelegate = (ProfileManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(ProfileManagementBusinessDelegate.class);
        DBXResult response = managementBusinessDelegate.checkifUserEnrolled(customerDTO, request.getHeaderMap());
        if (response.getResponse() != null) {
            JsonObject jsonObject = (JsonObject)response.getResponse();
            result = JSONToResult.convert(jsonObject.toString());
        }
        return result;
    }

    public Object sendCustomerUnlockEmail(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse response) {
        Result result = new Result();
        String isSuperAdmin = dcRequest.getParameter("isSuperAdmin");
        Map<String, String> inputParams = HelperMethods.getInputParamMap(inputArray);
        if (StringUtils.isBlank(isSuperAdmin))
            isSuperAdmin = inputParams.get("isSuperAdmin");
        if (!"true".equals(isSuperAdmin)) {
            result.addParam(new Param("mailRequestSent", "false"));
            ErrorCodeEnum.ERR_10191.setErrorCode(result);
            return result;
        }
        String userName = dcRequest.getParameter("userName");
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setUserName(userName);
        ProfileManagementBusinessDelegate businessDelegate = (ProfileManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(ProfileManagementBusinessDelegate.class);
        DBXResult dbxResult = businessDelegate.sendCustomerUnlockEmail(customerDTO, dcRequest.getHeaderMap());
        if (dbxResult.getResponse() != null) {
            JsonObject jsonObject = (JsonObject)dbxResult.getResponse();
            result = JSONToResult.convert(jsonObject.toString());
        }
        return result;
    }

    public Result getAddressTypes(String methodID, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) {
        Result result = new Result();
        ProfileManagementBusinessDelegate businessDelegate = (ProfileManagementBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(ProfileManagementBusinessDelegate.class);
        DBXResult dbxResult = businessDelegate.getAddressTypes(request.getHeaderMap());
        if (dbxResult.getResponse() != null) {
            JsonObject jsonObject = (JsonObject)dbxResult.getResponse();
            result = JSONToResult.convert(jsonObject.toString());
        }
        return result;
    }

    public Object createRetailContract(String methodID, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws ApplicationException {
        InfinityUserManagementResource infinityUserManagementResource = (InfinityUserManagementResource)DBPAPIAbstractFactoryImpl.getResource(InfinityUserManagementResource.class);
        return infinityUserManagementResource.createRetailContract(methodID, inputArray, request, response);
    }
}
