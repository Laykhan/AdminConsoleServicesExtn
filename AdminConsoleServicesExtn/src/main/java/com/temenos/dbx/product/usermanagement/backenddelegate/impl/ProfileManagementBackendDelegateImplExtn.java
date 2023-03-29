package com.temenos.dbx.product.usermanagement.backenddelegate.impl;

import com.dbp.core.api.factory.impl.DBPAPIAbstractFactoryImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kony.dbp.exception.ApplicationException;
import com.kony.dbputilities.kms.KMSUtil;
import com.kony.dbputilities.util.EnvironmentConfigurationsHandler;
import com.kony.dbputilities.util.ErrorCodeEnum;
import com.kony.dbputilities.util.HelperMethods;
import com.kony.dbputilities.util.IntegrationTemplateURLFinder;
import com.kony.dbputilities.util.JSONUtil;
import com.kony.dbputilities.util.OperationName;
import com.kony.dbputilities.util.ServiceCallHelper;
import com.kony.dbputilities.util.ServiceId;
import com.kony.dbputilities.util.StatusEnum;
import com.kony.dbputilities.util.URLFinder;
import com.kony.dbputilities.util.logger.LoggerUtil;
import com.konylabs.middleware.dataobject.Param;
import com.temenos.dbx.product.api.DBPDTOEXT;
import com.temenos.dbx.product.businessdelegate.api.SystemConfigurationBusinessDelegate;
import com.temenos.dbx.product.contract.businessdelegate.api.CoreCustomerBusinessDelegate;
import com.temenos.dbx.product.dto.BackendIdentifierDTO;
import com.temenos.dbx.product.dto.CredentialCheckerDTO;
import com.temenos.dbx.product.dto.CustomerAddressDTO;
import com.temenos.dbx.product.dto.CustomerCommunicationDTO;
import com.temenos.dbx.product.dto.CustomerDTO;
import com.temenos.dbx.product.dto.CustomerGroupDTO;
import com.temenos.dbx.product.dto.DBXResult;
import com.temenos.dbx.product.dto.MemberSearchBean;
import com.temenos.dbx.product.dto.MembershipDTO;
import com.temenos.dbx.product.dto.PasswordLockoutSettingsDTO;
import com.temenos.dbx.product.usermanagement.backenddelegate.api.BackendIdentifiersBackendDelegate;
import com.temenos.dbx.product.usermanagement.backenddelegate.api.CommunicationBackendDelegate;
import com.temenos.dbx.product.usermanagement.businessdelegate.api.CustomerGroupBusinessDelegate;
import com.temenos.dbx.product.utils.DTOUtils;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileManagementBackendDelegateImplExtn extends ProfileManagementBackendDelegateImpl {
    LoggerUtil logger = new LoggerUtil(ProfileManagementBackendDelegateImplExtn.class);




    private JsonObject orchestrationResultProcess(JsonObject result) {
        JsonObject finalResult = new JsonObject();
        JsonArray customerDataset = new JsonArray();
        JsonObject user = new JsonObject();
        if (result.has("customer") && !result.get("customer").isJsonNull() && result
                .get("customer").isJsonArray()) {
            customerDataset = result.get("customer").getAsJsonArray();
            user = customerDataset.get(0).getAsJsonObject();
        }
        if (user.has("LastName") && !user.get("LastName").isJsonNull())
            user.addProperty("userlastname", user.get("LastName").getAsString());
        if (user.has("Ssn") && !user.get("Ssn").isJsonNull())
            user.addProperty("Ssn", user.get("Ssn").getAsString());
        if (user.has("DateOfBirth") && !user.get("DateOfBirth").isJsonNull())
            user.addProperty("dateOfBirth", user.get("DateOfBirth").getAsString());
        if (user.has("DateOfBirth") && !user.get("DateOfBirth").isJsonNull())
            user.addProperty("dateOfBirth", user.get("DateOfBirth").getAsString());
        JsonObject jsonObject = new JsonObject();
        if (user.has("PreferredContactTime") && !user.get("PreferredContactTime").isJsonNull())
            jsonObject.add("PreferredContactTime", user.get("PreferredContactTime"));
        if (user.has("PreferredContactMethod") && !user.get("PreferredContactMethod").isJsonNull())
            jsonObject.add("PreferredContactMethod", user.get("PreferredContactMethod"));
        user.add("PreferredTimeAndMethod", (JsonElement)jsonObject);
        if (result.has("CustomerPreferences") && !result.get("CustomerPreferences").isJsonNull())
            addParamsFromRecord(result.get("CustomerPreferences").getAsJsonObject(), user);
        if (result.has("bankName") && !result.get("bankName").isJsonNull())
            user.addProperty("bankName", result.get("bankName").getAsString());
        if (result.has("feedbackUserId") && !result.get("feedbackUserId").isJsonNull())
            user.addProperty("feedbackUserId", result.get("feedbackUserId").getAsString());
        if (result.has("isSecurityQuestionConfigured") && !result.get("isSecurityQuestionConfigured").isJsonNull())
            user.addProperty("isSecurityQuestionConfigured", result.get("isSecurityQuestionConfigured").getAsString());
        addCurrencyCode(user);
        if (result.has("ContactNumbers") && !result.get("ContactNumbers").isJsonNull())
            user.add("ContactNumbers", (JsonElement)result.get("ContactNumbers").getAsJsonArray());
        if (result.has("EmailIds") && !result.get("EmailIds").isJsonNull())
            user.add("EmailIds", (JsonElement)result.get("EmailIds").getAsJsonArray());
        if (result.has("Addresses") && !result.get("Addresses").isJsonNull())
            user.add("Addresses", (JsonElement)result.get("Addresses").getAsJsonArray());
        if (result.has("customers") && !result.get("customers").isJsonNull())
            user.add("customers", (JsonElement)result.get("customers").getAsJsonArray());
        JsonArray jsonArray = new JsonArray();
        jsonArray.add((JsonElement)user);
        finalResult.add("customer", (JsonElement)jsonArray);
        return finalResult;
    }

    private void addParamsFromRecord(JsonObject jsonObject, JsonObject user) {
        for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)jsonObject.entrySet())
            user.add(entry.getKey(), entry.getValue());
    }

    private String getDeviceId(Map<String, Object> headers) {
        String deviceId = null;
        if (headers != null && headers.containsKey("X-Kony-ReportingParams")) {
            String reportingParams = (String)headers.get("X-Kony-ReportingParams");
            if (StringUtils.isNotBlank(reportingParams)) {
                JSONObject reportingParamsJson = null;
                try {
                    reportingParamsJson = new JSONObject(URLDecoder.decode(reportingParams, StandardCharsets.UTF_8.name()));
                } catch (JSONException|java.io.UnsupportedEncodingException e) {
                    this.logger.error(e.getMessage());
                }
                if (null != reportingParamsJson)
                    deviceId = reportingParamsJson.optString("did");
            }
        }
        return deviceId;
    }

    private void addCurrencyCode(JsonObject user) {
        String country = "";
        if (user.has("CountryCode") && !user.get("CountryCode").isJsonNull())
            user.addProperty("currencyCode", HelperMethods.getCurrencyCode(country));
        try {
            user.addProperty("lastlogintime",
                    HelperMethods.convertDateFormat(user.get("lastlogintime").getAsString(), "yyyy-MM-dd'T'HH:mm:ss"));
        } catch (Exception e) {
            this.logger.error(e.getMessage());
        }
    }

    public DBXResult createCustomer(CustomerDTO customerDTO, Map<String, Object> headerMap) {
        DBXResult dbxResult = new DBXResult();
        if (DTOUtils.persistObject((DBPDTOEXT)customerDTO, headerMap)) {
            dbxResult.setResponse(customerDTO.getId());
        } else {
            dbxResult.setDbpErrMsg("Propect Creation Failed");
        }
        return dbxResult;
    }

    public DBXResult updateCustomer(CustomerDTO customerDTO, Map<String, Object> headerMap) {
        DBXResult dbxResult = new DBXResult();
        JsonObject jsonObject = new JsonObject();
        JsonObject responseJsonObject = new JsonObject();
        customerDTO.setUserName(null);
        limitPhoneEmailAndAddressestoThree(customerDTO);
        if (DTOUtils.persistObject((DBPDTOEXT)customerDTO, headerMap)) {
            responseJsonObject.addProperty("success", "success");
            responseJsonObject.addProperty("Status", "Operation successful");
            responseJsonObject.addProperty("status", "Operation successful");
            dbxResult.setResponse(responseJsonObject);
            return dbxResult;
        }
        ErrorCodeEnum.ERR_10218.setErrorCode(responseJsonObject);
        responseJsonObject.addProperty("errcode", "10218");
        responseJsonObject.addProperty("errmsg", "dbxCustomer update failed");
        dbxResult.setResponse(responseJsonObject);
        return dbxResult;
    }

    private void limitPhoneEmailAndAddressestoThree(CustomerDTO customerDTO) {
        int size = 0;
        if (IntegrationTemplateURLFinder.isIntegrated) {
            size = 2;
        } else {
            size = 3;
        }
        int phoneSize = 0;
        int emailSize = 0;
        int addressSize = 0;
        int i;
        for (i = 0; i < customerDTO.getCustomerCommuncation().size(); i++) {
            CustomerCommunicationDTO communicationDTO = customerDTO.getCustomerCommuncation().get(i);
            if (communicationDTO.getType_id().equals(HelperMethods.getCommunicationTypes().get("Phone"))) {
                if (phoneSize < size && !communicationDTO.isIsdeleted()) {
                    phoneSize++;
                } else if (!communicationDTO.isIsdeleted()) {
                    customerDTO.getCustomerCommuncation().remove(i);
                    i--;
                }
            } else if (communicationDTO.getType_id().equals(HelperMethods.getCommunicationTypes().get("Email"))) {
                if (emailSize < size && !communicationDTO.isIsdeleted()) {
                    emailSize++;
                } else if (!communicationDTO.isIsdeleted()) {
                    customerDTO.getCustomerCommuncation().remove(i);
                    i--;
                }
            }
        }
        for (i = 0; i < customerDTO.getCustomerAddress().size(); i++) {
            CustomerAddressDTO addressDTO = customerDTO.getCustomerAddress().get(i);
            if (addressSize < size && !addressDTO.isIsdeleted()) {
                addressSize++;
            } else if (!addressDTO.isIsdeleted()) {
                customerDTO.getCustomerAddress().remove(i);
                i--;
            }
        }
    }

    public DBXResult verifyCustomer(CustomerDTO customerDTO, Map<String, Object> headerMap) throws ApplicationException {
        DBXResult dbxResult = new DBXResult();
        JsonObject response = new JsonObject();
        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("_dateOfBirth", customerDTO.getDateOfBirth());
        String dateofbirth = customerDTO.getDateOfBirth();
        String phone = "";
        String email = "";
        List<CustomerCommunicationDTO> commDTOS = new ArrayList<>();
        commDTOS = customerDTO.getCustomerCommuncation();
        for (CustomerCommunicationDTO dto : commDTOS) {
            if ("COMM_TYPE_EMAIL".equalsIgnoreCase(dto.getType_id())) {
                inputParams.put("_email", dto.getValue());
                email = dto.getValue();
            }
            if ("COMM_TYPE_PHONE".equalsIgnoreCase(dto.getType_id())) {
                inputParams.put("_phone", dto.getValue());
                phone = dto.getValue();
            }
        }
        String IS_Integrated = Boolean.toString(IntegrationTemplateURLFinder.isIntegrated);
        if (StringUtils.isNotBlank(IS_Integrated) && IS_Integrated.equalsIgnoreCase("true")) {
            CoreCustomerBusinessDelegate corecustomerBD = (CoreCustomerBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(CoreCustomerBusinessDelegate.class);
            List<MembershipDTO> membershipList = new ArrayList<>();
            try {
                membershipList = corecustomerBD.getMembershipDetails(dateofbirth, email, phone, headerMap);
            } catch (Exception exception) {}
            StringBuilder backendIdentifiers = new StringBuilder();
            for (MembershipDTO dto : membershipList) {
                if (StringUtils.isBlank(backendIdentifiers)) {
                    backendIdentifiers.append(dto.getId());
                    continue;
                }
                backendIdentifiers.append(",").append(dto.getId());
            }
            String backendType = ServiceId.BACKEND_TYPE;
            inputParams.put("_backendIdentifiers", backendIdentifiers.toString());
            inputParams.put("_backendType", backendType);
        }
        JsonObject jsonObject = ServiceCallHelper.invokeServiceAndGetJson(inputParams, headerMap, "verify_user_proc");
        if (!jsonObject.has("records")) {
            dbxResult.setError(ErrorCodeEnum.ERR_10024);
            return dbxResult;
        }
        if (jsonObject.get("records").isJsonNull() || jsonObject.get("records").getAsJsonArray().size() <= 0) {
            response.addProperty("isUserExists", "false");
            dbxResult.setResponse(response);
            return dbxResult;
        }
        JsonArray customers = jsonObject.get("records").getAsJsonArray();
        for (int i = 0; i < customers.size(); i++) {
            JsonObject record = customers.get(i).getAsJsonObject();
            String activationToken = UUID.randomUUID().toString();
            Map<String, Object> map = new HashMap<>();
            map.put("id", activationToken);
            map.put("UserName", record.get("UserName").getAsString());
            map.put("linktype", HelperMethods.CREDENTIAL_TYPE.RESETPASSWORD.toString());
            map.put("createdts", HelperMethods.getCurrentTimeStamp());
            ServiceCallHelper.invokeServiceAndGetJson(map, headerMap, "credentialChecker.createRecord");
            record.addProperty("securityKey", activationToken);
        }
        response.addProperty("isUserExists", "true");
        response.add("user_attributes", (JsonElement)customers);
        dbxResult.setResponse(response);
        return dbxResult;
    }

    public DBXResult searchCustomer(Map<String, String> configurations, MemberSearchBean memberSearchBean, Map<String, Object> headerMap) {
        DBXResult dbxResult = new DBXResult();
        JsonObject processedResult = new JsonObject();
        JsonObject searchResults = new JsonObject();
        String IS_Integrated = Boolean.toString(IntegrationTemplateURLFinder.isIntegrated);
        boolean isCustomerSearch = false;
        if (StringUtils.isNotBlank(memberSearchBean.getCustomerId())) {
            memberSearchBean.setMemberId(memberSearchBean.getCustomerId());
            isCustomerSearch = true;
            memberSearchBean.setCustomerId(null);
        }
        if (isCustomerSearch && StringUtils.isNotBlank(memberSearchBean.getMemberId()) && !IntegrationTemplateURLFinder.isIntegrated &&

                !memberPresent(memberSearchBean.getMemberId(), headerMap)) {
            dbxResult.setError(ErrorCodeEnum.ERR_10335);
            return dbxResult;
        }
        if (!isCustomerSearch && StringUtils.isNotBlank(memberSearchBean.getMemberId()) && !IntegrationTemplateURLFinder.isIntegrated &&

                memberPresent(memberSearchBean.getMemberId(), headerMap)) {
            dbxResult.setError(ErrorCodeEnum.ERR_10335);
            return dbxResult;
        }
        if (memberSearchBean.getSearchType().equalsIgnoreCase("APPLICANT_SEARCH")) {
            processedResult.addProperty("TotalResultsFound", Integer.valueOf(0));
            searchResults = searchCustomers(headerMap, memberSearchBean.getSearchType(), memberSearchBean);

            this.logger.error("20230328 APPLICANT_SEARCH searchResults: " + searchResults.getAsString());
        }
        if (memberSearchBean.getSearchType().equalsIgnoreCase("GROUP_SEARCH")) {
            searchResults = searchCustomers(headerMap, memberSearchBean.getSearchType(), memberSearchBean);

            this.logger.error("20230328 GROUP_SEARCH searchResults: " + searchResults.getAsString());

            if (searchResults.has("records1") && searchResults.get("records1").getAsJsonArray().size() > 0)
                if (searchResults.get("records1").getAsJsonArray().get(0).getAsJsonObject().has("SearchMatchs")) {
                    processedResult.addProperty("TotalResultsFound", Integer.valueOf(searchResults.get("records1").getAsJsonArray()
                            .get(0).getAsJsonObject().get("SearchMatchs").getAsInt()));
                } else {
                    processedResult.addProperty("TotalResultsFound", Integer.valueOf(0));
                }
        }
        if (memberSearchBean.getSearchType().equalsIgnoreCase("CUSTOMER_SEARCH")) {
            searchResults = searchCustomers(headerMap, memberSearchBean.getSearchType(), memberSearchBean);

            this.logger.error("20230328 CUSTOMER_SEARCH searchResults: " + searchResults.getAsString());

            if (searchResults.has("records1") && searchResults.get("records1").getAsJsonArray().size() > 0)
                if (searchResults.get("records1").getAsJsonArray().get(0).getAsJsonObject().has("SearchMatchs")) {
                    processedResult.addProperty("TotalResultsFound", Integer.valueOf(searchResults.get("records1").getAsJsonArray()
                            .get(0).getAsJsonObject().get("SearchMatchs").getAsInt()));
                } else {
                    processedResult.addProperty("TotalResultsFound", "0");
                }
        }
        JsonArray recordsArray = new JsonArray();
        if (searchResults.has("records")) {
            recordsArray = searchResults.get("records").getAsJsonArray();
            processedResult.add("records", (JsonElement)recordsArray);
        }
        if (StringUtils.isNotBlank(IS_Integrated) && IS_Integrated.equalsIgnoreCase("true") && (
                StringUtils.isBlank(memberSearchBean.getMemberId()) || isCustomerSearch))
            processedResult = searchCustomerinT24(recordsArray, configurations, memberSearchBean, headerMap, isCustomerSearch);
        mergeResults(recordsArray, headerMap);
        processedResult.addProperty("Status", "Records returned: " + recordsArray.size());
        processedResult.addProperty("TotalResultsFound", Integer.valueOf(recordsArray.size()));
        processedResult.add("records", (JsonElement)recordsArray);
        if (recordsArray.size() == 1 && memberSearchBean
                .getSearchType().equalsIgnoreCase("CUSTOMER_SEARCH")) {
            String customerId = recordsArray.get(0).getAsJsonObject().has("id") ? recordsArray.get(0).getAsJsonObject().get("id").getAsString() : recordsArray.get(0).getAsJsonObject().get("primaryCustomerId").getAsString();
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setId(customerId);
            dbxResult = getBasicInformation(configurations, customerDTO, headerMap, isCustomerSearch);
            if (dbxResult.getResponse() != null) {
                JsonObject jsonObject = (JsonObject)dbxResult.getResponse();
                for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)jsonObject.entrySet())
                    processedResult.add(entry.getKey(), entry.getValue());
            }
        }
        for (JsonElement recordElement : recordsArray) {
            isAssociated(recordElement, headerMap);
            if (!Boolean.parseBoolean(recordElement
                    .getAsJsonObject().get("isProfileExist").getAsString()))
                recordElement.getAsJsonObject().addProperty("isEnrolled", "false");
        }
        dbxResult.setResponse(processedResult);
        if (recordsArray.size() == 0)
            dbxResult.setError(ErrorCodeEnum.ERR_10335);
        return dbxResult;
    }

    private void mergeResults(JsonArray recordsArray, Map<String, Object> headerMap) {
        for (int i = 0; i < recordsArray.size(); i++) {
            JsonObject jsonObject = recordsArray.get(i).getAsJsonObject();
            String id = (jsonObject.has("id") && !jsonObject.get("id").isJsonNull()) ? jsonObject.get("id").getAsString() : null;
            for (int j = i + 1; j < recordsArray.size(); j++) {
                JsonObject t24Object = recordsArray.get(j).getAsJsonObject();
                String primaryCustomerId = (t24Object.has("primaryCustomerId") && !t24Object.get("primaryCustomerId").isJsonNull()) ? t24Object.get("primaryCustomerId").getAsString() : null;
                if (StringUtils.isNotBlank(primaryCustomerId)) {
                    BackendIdentifierDTO backendIdentifierDTO = new BackendIdentifierDTO();
                    backendIdentifierDTO.setCustomer_id(id);
                    backendIdentifierDTO.setBackendId(primaryCustomerId);
                    backendIdentifierDTO
                            .setBackendType(IntegrationTemplateURLFinder.getBackendURL("BackendType"));
                    DBXResult dbxResult = new DBXResult();
                    try {
                        dbxResult = ((BackendIdentifiersBackendDelegate)DBPAPIAbstractFactoryImpl.getBackendDelegate(BackendIdentifiersBackendDelegate.class)).get(backendIdentifierDTO, headerMap);
                    } catch (ApplicationException e) {
                        e.printStackTrace();
                    }
                    if (dbxResult.getResponse() != null) {
                        for (Map.Entry<String, JsonElement> featureJsonEntry : (Iterable<Map.Entry<String, JsonElement>>)t24Object.entrySet())
                            jsonObject.add(featureJsonEntry.getKey(), featureJsonEntry.getValue());
                        jsonObject.addProperty("id", id);
                        recordsArray.remove(j);
                        break;
                    }
                }
            }
        }
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

    private void isAssociated(JsonElement recordElement, Map<String, Object> headerMap) {
        if (!recordElement.getAsJsonObject().has("isProfileExist"))
            if (recordElement.getAsJsonObject().has("id") &&
                    !recordElement.getAsJsonObject().get("id").isJsonNull() && recordElement
                    .getAsJsonObject().has("Username") &&
                    !recordElement.getAsJsonObject().get("Username").isJsonNull()) {
                if (recordElement.getAsJsonObject().get("id").getAsString()
                        .equals(recordElement.getAsJsonObject().get("Username").getAsString())) {
                    recordElement.getAsJsonObject().add("primaryCustomerId", recordElement
                            .getAsJsonObject().get("id"));
                    recordElement.getAsJsonObject().addProperty("isProfileExist", "false");
                    recordElement.getAsJsonObject().remove("id");
                } else {
                    recordElement.getAsJsonObject().addProperty("isProfileExist", "true");
                }
            } else {
                recordElement.getAsJsonObject().addProperty("isProfileExist", "false");
            }
        String CustomerType_id = "";
        if (!recordElement.getAsJsonObject().has("id") || recordElement
                .getAsJsonObject().get("id").isJsonNull()) {
            recordElement.getAsJsonObject().addProperty("isAssociated", "false");
            return;
        }
        BackendIdentifierDTO backendIdentifierDTO = new BackendIdentifierDTO();
        backendIdentifierDTO.setCustomer_id(recordElement.getAsJsonObject().get("id").getAsString());
        if (IntegrationTemplateURLFinder.isIntegrated) {
            backendIdentifierDTO.setIdentifier_name(
                    IntegrationTemplateURLFinder.getBackendURL("BackendCustomerIdentifierName"));
            backendIdentifierDTO
                    .setBackendType(IntegrationTemplateURLFinder.getBackendURL("BackendType"));
        } else {
            backendIdentifierDTO.setBackendType("CORE");
        }
        try {
            DBXResult dbxResult = ((BackendIdentifiersBackendDelegate)DBPAPIAbstractFactoryImpl.getBackendDelegate(BackendIdentifiersBackendDelegate.class)).get(backendIdentifierDTO, headerMap);
            if (dbxResult.getResponse() != null) {
                BackendIdentifierDTO identifierDTO = (BackendIdentifierDTO)dbxResult.getResponse();
                recordElement.getAsJsonObject().addProperty("primaryCustomerId", identifierDTO
                        .getBackendId());
            } else {
                recordElement.getAsJsonObject().addProperty("primaryCustomerId", backendIdentifierDTO
                        .getCustomer_id());
            }
        } catch (ApplicationException e1) {
            this.logger.error("Error while fetching backend identifier for backend ID " + recordElement
                    .getAsJsonObject().get("id").getAsString());
        }
        String customerId = recordElement.getAsJsonObject().get("id").getAsString();
        backendIdentifierDTO = new BackendIdentifierDTO();
        backendIdentifierDTO.setBackendId(customerId);
        if (IntegrationTemplateURLFinder.isIntegrated) {
            backendIdentifierDTO.setIdentifier_name(
                    IntegrationTemplateURLFinder.getBackendURL("BackendCustomerIdentifierName"));
            backendIdentifierDTO
                    .setBackendType(IntegrationTemplateURLFinder.getBackendURL("BackendType"));
        } else {
            backendIdentifierDTO.setBackendType("CORE");
        }
        try {
            DBXResult dbxResult = ((BackendIdentifiersBackendDelegate)DBPAPIAbstractFactoryImpl.getBackendDelegate(BackendIdentifiersBackendDelegate.class)).get(backendIdentifierDTO, headerMap);
            if (dbxResult.getResponse() != null) {
                backendIdentifierDTO = (BackendIdentifierDTO)dbxResult.getResponse();
                recordElement.getAsJsonObject().addProperty("id", backendIdentifierDTO
                        .getCustomer_id());
                recordElement.getAsJsonObject().addProperty("Customer_id", backendIdentifierDTO
                        .getCustomer_id());
                CustomerDTO userDTO = (CustomerDTO)(new CustomerDTO()).loadDTO(backendIdentifierDTO.getCustomer_id());
                recordElement.getAsJsonObject().addProperty("isEnrolled", "" + userDTO.getIsEnrolled());
                recordElement.getAsJsonObject().addProperty("CustomerType_id", userDTO
                        .getCustomerType_id());
                customerId = backendIdentifierDTO.getCustomer_id();
                if (!IntegrationTemplateURLFinder.isIntegrated) {
                    CustomerCommunicationDTO customerCommunicationDTO = new CustomerCommunicationDTO();
                    customerCommunicationDTO.setCustomer_id(customerId);
                    CommunicationBackendDelegate backendDelegate = (CommunicationBackendDelegate)DBPAPIAbstractFactoryImpl.getBackendDelegate(CommunicationBackendDelegate.class);
                    dbxResult = backendDelegate.getPrimaryMFACommunicationDetails(customerCommunicationDTO, headerMap);
                    if (dbxResult.getResponse() != null) {
                        JsonObject jsonObject = (JsonObject)dbxResult.getResponse();
                        if (jsonObject.has("customercommunication")) {
                            JsonElement jsonElement = jsonObject.get("customercommunication");
                            if (jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size() > 0) {
                                JsonArray jsonArray = jsonElement.getAsJsonArray();
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    jsonObject = jsonArray.get(i).getAsJsonObject();
                                    if (jsonObject.get("Type_id").getAsString()
                                            .equals(HelperMethods.getCommunicationTypes().get("Phone"))) {
                                        recordElement.getAsJsonObject().addProperty("PrimaryPhoneNumber", (jsonObject

                                                .has("Value") && !jsonObject.get("Value").isJsonNull()) ? jsonObject
                                                .get("Value").getAsString() : null);
                                    } else if (jsonObject.get("Type_id").getAsString()
                                            .equals(HelperMethods.getCommunicationTypes().get("Email"))) {
                                        recordElement.getAsJsonObject().addProperty("PrimaryEmailAddress", (jsonObject

                                                .has("Value") && !jsonObject.get("Value").isJsonNull()) ? jsonObject
                                                .get("Value").getAsString() : null);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ApplicationException e1) {
            this.logger.error("Error while fetching backend identifier for backend ID " + recordElement
                    .getAsJsonObject().get("id").getAsString());
        }
        String filter = "customerId eq " + customerId;
        Map<String, Object> input = new HashMap<>();
        input.put("$filter", filter);
        JsonObject response = ServiceCallHelper.invokeServiceAndGetJson(input, headerMap, "contractcustomers.getRecord");
        if (response.has("contractcustomers")) {
            JsonElement jsonElement = response.get("contractcustomers");
            if (jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size() > 0) {
                for (JsonElement arrayElement : jsonElement.getAsJsonArray()) {
                    filter = "id eq " + arrayElement.getAsJsonObject().get("contractId").getAsString();
                    input = new HashMap<>();
                    input.put("$filter", filter);
                    JsonObject jsonObject = ServiceCallHelper.invokeServiceAndGetJson(input, headerMap, "contract.readRecord");
                    input.clear();
                    if (jsonObject.has("contract")) {
                        jsonElement = jsonObject.get("contract");
                        if (jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size() > 0) {
                            JsonArray jsonArray = jsonElement.getAsJsonArray();
                            jsonObject = jsonArray.get(0).getAsJsonObject();
                            if (StringUtils.isBlank(CustomerType_id) ||
                                    !CustomerType_id.contains(jsonObject.get("serviceType").getAsString())) {
                                if (StringUtils.isNotBlank(CustomerType_id))
                                    CustomerType_id = CustomerType_id + ",";
                                CustomerType_id = CustomerType_id + jsonObject.get("serviceType").getAsString();
                            }
                        }
                    }
                }
                recordElement.getAsJsonObject().addProperty("CustomerType_id", CustomerType_id);
                recordElement.getAsJsonObject().addProperty("CustomerTypeId", CustomerType_id);
                recordElement.getAsJsonObject().addProperty("isAssociated", "true");
            } else {
                recordElement.getAsJsonObject().addProperty("isAssociated", "false");
                getCustomerType(recordElement.getAsJsonObject(), recordElement
                        .getAsJsonObject().get("id").getAsString(), headerMap);
                recordElement.getAsJsonObject().add("CustomerTypeId", recordElement
                        .getAsJsonObject().get("CustomerType_id"));
            }
        }
    }

    private JsonObject searchCustomers(Map<String, Object> headerMap, String searchType, MemberSearchBean memberSearchBean) {
        Map<String, Object> searchPostParameters = new HashMap<>();
        searchPostParameters.put("_id", memberSearchBean.getMemberId());
        searchPostParameters.put("_name", memberSearchBean.getCustomerName());
        searchPostParameters.put("_username", memberSearchBean.getCustomerUsername());
        searchPostParameters.put("_SSN", memberSearchBean.getSsn());
        searchPostParameters.put("_phone", memberSearchBean.getCustomerPhone());
        searchPostParameters.put("_email", memberSearchBean.getCustomerEmail());
        searchPostParameters.put("_IsStaffMember", memberSearchBean.getIsStaffMember());
        searchPostParameters.put("_cardorAccountnumber", memberSearchBean.getCardorAccountnumber());
        searchPostParameters.put("_TIN", memberSearchBean.getTin());
        searchPostParameters.put("_group", memberSearchBean.getCustomerGroup());
        searchPostParameters.put("_IDType", memberSearchBean.getCustomerIDType());
        searchPostParameters.put("_IDValue", memberSearchBean.getCustomerIDValue());
        searchPostParameters.put("_companyId", memberSearchBean.getCustomerCompanyId());
        searchPostParameters.put("_requestID", memberSearchBean.getCustomerRequest());
        searchPostParameters.put("_branchIDS", memberSearchBean.getBranchIDS());
        searchPostParameters.put("_productIDS", memberSearchBean.getProductIDS());
        searchPostParameters.put("_cityIDS", memberSearchBean.getCityIDS());
        searchPostParameters.put("_entitlementIDS", memberSearchBean.getEntitlementIDS());
        searchPostParameters.put("_groupIDS", memberSearchBean.getGroupIDS());
        searchPostParameters.put("_customerStatus", memberSearchBean.getCustomerStatus());
        searchPostParameters.put("_before", memberSearchBean.getBeforeDate());
        searchPostParameters.put("_after", memberSearchBean.getAfterDate());
        searchPostParameters.put("_dateOfBirth", memberSearchBean.getDateOfBirth());
        if (HelperMethods.allEmpty(searchPostParameters))
            return new JsonObject();
        searchPostParameters.put("_sortVariable", memberSearchBean.getSortVariable());
        searchPostParameters.put("_sortDirection", memberSearchBean.getSortDirection());
        searchPostParameters.put("_searchType", searchType);
        searchPostParameters.put("_pageOffset", String.valueOf(memberSearchBean.getPageOffset()));
        searchPostParameters.put("_pageSize", String.valueOf(memberSearchBean.getPageSize()));

        this.logger.error("20230328 to stored procedure postparameter" + searchPostParameters.toString());

        return ServiceCallHelper.invokeServiceAndGetJson(searchPostParameters, headerMap, "Customer_Search_Proc");
    }

    public DBXResult getBasicInformation(Map<String, String> configurations, CustomerDTO customerDTO, Map<String, Object> headerMap, boolean isCustomerSearch) {
        String str1;
        DBXResult dbxResult = new DBXResult();
        String customerId = customerDTO.getId();
        String username = customerDTO.getUserName();
        JsonObject basicResult = new JsonObject();
        if (StringUtils.isBlank(customerId) &&
                StringUtils.isBlank(username)) {
            ErrorCodeEnum.ERR_20612.setErrorCode(basicResult);
            basicResult.addProperty("Status", "Failure");
            dbxResult.setResponse(basicResult);
            return dbxResult;
        }
        if (StringUtils.isBlank(customerId)) {
            customerDTO = (CustomerDTO)customerDTO.loadDTO();
            if (customerDTO != null) {
                customerId = customerDTO.getId();
            } else {
                ErrorCodeEnum.ERR_20688.setErrorCode(basicResult);
                basicResult.addProperty("Status", "Failure");
                dbxResult.setResponse(basicResult);
                return dbxResult;
            }
        }
        JsonObject customerViewJson = new JsonObject();
        Map<String, Object> postParametersMap = new HashMap<>();
        postParametersMap.put("_customerId", customerId);
        JsonObject jsonobject = ServiceCallHelper.invokeServiceAndGetJson(postParametersMap, headerMap, "Customer_Basic_Info_Proc");
        String primaryCustomerId = "";
        String IS_Integrated = Boolean.toString(IntegrationTemplateURLFinder.isIntegrated);
        if (jsonobject != null && jsonobject.has("opstatus") && jsonobject.get("opstatus").getAsInt() == 0 && jsonobject
                .has("records")) {
            if (jsonobject.get("records").getAsJsonArray().size() == 0 && (
                    StringUtils.isBlank(IS_Integrated) || !IS_Integrated.equalsIgnoreCase("true"))) {
                ErrorCodeEnum.ERR_20539.setErrorCode(basicResult);
                basicResult.addProperty("Status", "Failure");
                dbxResult.setResponse(basicResult);
                return dbxResult;
            }
            if (jsonobject.get("records").getAsJsonArray().size() != 0)
                customerViewJson = jsonobject.get("records").getAsJsonArray().get(0).getAsJsonObject();
            if (StringUtils.isNotBlank(IS_Integrated) && IS_Integrated.equalsIgnoreCase("true")) {
                dbxResult = getBasicInfo(customerViewJson, configurations, customerDTO, headerMap, isCustomerSearch);
                basicResult = (JsonObject)dbxResult.getResponse();
                if (basicResult.has("customerbasicinfo_view")) {
                    basicResult = (basicResult.has("customerbasicinfo_view") && basicResult.get("customerbasicinfo_view").isJsonObject()) ? basicResult.get("customerbasicinfo_view").getAsJsonObject() : new JsonObject();
                    for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)basicResult.entrySet())
                        customerViewJson.add(entry.getKey(), entry.getValue());
                    basicResult = new JsonObject();
                    basicResult.add("customerbasicinfo_view", (JsonElement)customerViewJson);
                    dbxResult.setResponse(basicResult);
                    return dbxResult;
                }
            }
        } else {
            ErrorCodeEnum.ERR_20689.setErrorCode(basicResult);
            basicResult.addProperty("Status", "Failure");
            dbxResult.setResponse(basicResult);
            return dbxResult;
        }
        if (customerViewJson.has("Customer_id"))
            customerViewJson.add("id", customerViewJson.get("Customer_id"));
        boolean isAssociated = false;
        if (StringUtils.isBlank(IS_Integrated) || !IS_Integrated.equalsIgnoreCase("true")) {
            BackendIdentifierDTO backendIdentifierDTO = new BackendIdentifierDTO();
            backendIdentifierDTO.setBackendType("CORE");
            if (isCustomerSearch) {
                backendIdentifierDTO.setBackendId(customerDTO.getId());
            } else {
                backendIdentifierDTO.setCustomer_id(customerDTO.getId());
            }
            try {
                dbxResult = ((BackendIdentifiersBackendDelegate)DBPAPIAbstractFactoryImpl.getBackendDelegate(BackendIdentifiersBackendDelegate.class)).get(backendIdentifierDTO, headerMap);
                if (dbxResult.getResponse() != null) {
                    BackendIdentifierDTO identifierDTO = (BackendIdentifierDTO)dbxResult.getResponse();
                    primaryCustomerId = identifierDTO.getBackendId();
                    customerId = identifierDTO.getCustomer_id();
                } else {
                    backendIdentifierDTO = new BackendIdentifierDTO();
                    backendIdentifierDTO.setBackendType("CORE");
                    backendIdentifierDTO.setBackendId(customerDTO.getId());
                    dbxResult = ((BackendIdentifiersBackendDelegate)DBPAPIAbstractFactoryImpl.getBackendDelegate(BackendIdentifiersBackendDelegate.class)).get(backendIdentifierDTO, headerMap);
                    if (dbxResult.getResponse() != null) {
                        BackendIdentifierDTO identifierDTO = (BackendIdentifierDTO)dbxResult.getResponse();
                        primaryCustomerId = identifierDTO.getBackendId();
                        customerId = identifierDTO.getCustomer_id();
                    }
                }
            } catch (ApplicationException e1) {
                this.logger.error("Error while fetching backend identifier for backend ID " + customerDTO.getId());
            }
            if (StringUtils.isBlank(primaryCustomerId))
                primaryCustomerId = customerViewJson.get("id").getAsString();
        }
        if (customerViewJson.has("Username") &&
                !customerViewJson.get("Username").isJsonNull() && customerViewJson
                .has("id") &&
                !customerViewJson.get("id").isJsonNull() &&

                !customerViewJson.get("Username").getAsString().equals(customerViewJson.get("id").getAsString()))
            isAssociated = true;
        customerViewJson.addProperty("primaryCustomerId", primaryCustomerId);
        if (StringUtils.isNotBlank(customerId) && (
                !customerId.equals(primaryCustomerId) || (customerId.equals(primaryCustomerId) && customerViewJson
                        .has("Username") && !customerViewJson.get("Username").isJsonNull() &&
                        !customerId.equals(customerViewJson.get("Username").getAsString())))) {
            customerViewJson.addProperty("isProfileExist", "true");
        } else if (customerId.equals(primaryCustomerId)) {
            customerViewJson.addProperty("isProfileExist", "false");
        }
        if (!isAssociated) {
            getCustomerType(customerViewJson, customerViewJson.get("id").getAsString(), headerMap);
            customerViewJson.remove("id");
            customerViewJson.remove("Customer_id");
        } else {
            isAssociated((JsonElement)customerViewJson, headerMap);
            customerViewJson.getAsJsonObject().addProperty("id", customerId);
            customerViewJson.addProperty("Customer_id", customerId);
        }
        String statusId = customerViewJson.has("CustomerStatus_id") ? customerViewJson.get("CustomerStatus_id").getAsString() : "";
        String isEnrolled = customerViewJson.has("isEnrolled") ? customerViewJson.get("isEnrolled").getAsString() : "";
        CredentialCheckerDTO credentialCheckerDTO = new CredentialCheckerDTO();
        credentialCheckerDTO.setUserName(customerDTO.getUserName());
        credentialCheckerDTO.setLinktype(HelperMethods.CREDENTIAL_TYPE.ACTIVATION.toString());
        credentialCheckerDTO = (CredentialCheckerDTO)credentialCheckerDTO.loadDTO();
        customerViewJson.addProperty("isCustomerEnrolled", isEnrolled);
        customerViewJson.addProperty("customerStatus", statusId);
        if (credentialCheckerDTO == null) {
            customerViewJson.addProperty("isActivationLinkSent", "false");
        } else if (credentialCheckerDTO != null) {
            customerViewJson.addProperty("isActivationLinkSent", "true");
        }
        customerViewJson.addProperty("isCustomerAccessiable", Boolean.valueOf(true));
        basicResult.add("customerbasicinfo_view", (JsonElement)customerViewJson);
        JsonObject configuration = new JsonObject();
        configuration.addProperty("value", customerViewJson.get("accountLockoutTime").getAsString());
        basicResult.add("Configuration", (JsonElement)configuration);
        String lockedOnTS = "";
        if (customerViewJson.has("lockedOn"))
            lockedOnTS = customerViewJson.get("lockedOn").getAsString();
        if (customerViewJson.get("CustomerStatus_id").getAsString()
                .equalsIgnoreCase(StatusEnum.SID_CUS_LOCKED.name())) {
            str1 = "LOCKED";
        } else if (customerViewJson.get("CustomerStatus_id").getAsString()
                .equalsIgnoreCase(StatusEnum.SID_CUS_SUSPENDED.name())) {
            str1 = "SUSPENDED";
        } else if (customerViewJson.get("CustomerStatus_id").getAsString()
                .equalsIgnoreCase(StatusEnum.SID_CUS_ACTIVE.name())) {
            str1 = "ACTIVE";
        } else {
            str1 = "NEW";
        }
        if (customerViewJson.get("CustomerStatus_id").getAsString()
                .equalsIgnoreCase(StatusEnum.SID_CUS_LOCKED.name()))
            if (StringUtils.isNotBlank(lockedOnTS)) {
                String lockDuration = "0";
                if (customerViewJson.has("accountLockoutTime"))
                    lockDuration = customerViewJson.get("accountLockoutTime").getAsString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Calendar elapsedLockedOnDate = Calendar.getInstance();
                try {
                    elapsedLockedOnDate.setTime(dateFormat.parse(lockedOnTS));
                } catch (ParseException parseException) {}
                elapsedLockedOnDate.add(12, Integer.parseInt(lockDuration));
                Calendar currentDate = Calendar.getInstance();
                if (elapsedLockedOnDate.before(currentDate)) {
                    postParametersMap = new HashMap<>();
                    postParametersMap.put("id", customerId);
                    postParametersMap.put("lockCount", "0");
                    postParametersMap.put("lockedOn", "");
                    JsonObject unlockResponse = ServiceCallHelper.invokeServiceAndGetJson(postParametersMap, headerMap, "Customer.updateRecord");
                    basicResult.addProperty("unlockStatus", unlockResponse.toString());
                    if (unlockResponse == null || !unlockResponse.has("opstatus") || unlockResponse
                            .get("opstatus").getAsInt() != 0) {
                        ErrorCodeEnum.ERR_20538.setErrorCode(basicResult);
                        dbxResult.setResponse(basicResult);
                        return dbxResult;
                    }
                    str1 = "ACTIVE";
                }
            }
        JsonObject statusResponse = new JsonObject();
        statusResponse.addProperty("LockedOn", lockedOnTS);
        statusResponse.addProperty("Status", str1);
        customerViewJson.add("OLBCustomerFlags", (JsonElement)statusResponse);
        if (customerViewJson.get("CustomerType_id").getAsString()
                .equalsIgnoreCase((String)HelperMethods.getCustomerTypes().get("Prospect"))) {
            postParametersMap = new HashMap<>();
            postParametersMap.put("$filter", "CustomerId eq '" + customerId + "'");
            postParametersMap.put("$select", "Address_id,AddressType,AddressLine1,AddressLine2,ZipCode,CityName,City_id,RegionName,Region_id,RegionCode,CountryName,Country_id,CountryCode,isPrimary");
            JsonObject readCustomerAddr = ServiceCallHelper.invokeServiceAndGetJson(postParametersMap, headerMap, "customerAddressView.readRecord");
            if (readCustomerAddr == null || !readCustomerAddr.has("opstatus") || readCustomerAddr
                    .get("opstatus").getAsInt() != 0 ||
                    !readCustomerAddr.has("customeraddress_view")) {
                ErrorCodeEnum.ERR_20881.setErrorCode(basicResult);
                dbxResult.setResponse(basicResult);
                return dbxResult;
            }
            JsonArray addressDataset = readCustomerAddr.get("customeraddress_view").getAsJsonArray();
            customerViewJson.add("Addresses", (JsonElement)addressDataset);
        }
        dbxResult = getCustomerRequestNotificationCount(customerDTO, headerMap);
        if (dbxResult.getResponse() != null) {
            JsonObject jsonObject = (JsonObject)dbxResult.getResponse();
            for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)jsonObject.entrySet())
                customerViewJson.add(entry.getKey(), entry.getValue());
        }
        basicResult.add("customerbasicinfo_view", (JsonElement)customerViewJson);
        dbxResult.setResponse(basicResult);
        return dbxResult;
    }

    private void getCustomerType(JsonObject json, String id, Map<String, Object> headerMap) {
        String CustomerType_id = "";
        if (IntegrationTemplateURLFinder.isIntegrated) {
            String filter = "customerId eq " + id;
            Map<String, Object> input = new HashMap<>();
            input.put("$filter", filter);
            JsonObject response = ServiceCallHelper.invokeServiceAndGetJson(input, headerMap, "contractcustomers.getRecord");
            if (response.has("contractcustomers")) {
                JsonElement jsonElement = response.get("contractcustomers");
                if (jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size() > 0)
                    for (JsonElement arrayElement : jsonElement.getAsJsonArray()) {
                        filter = "id eq " + arrayElement.getAsJsonObject().get("contractId").getAsString();
                        input = new HashMap<>();
                        input.put("$filter", filter);
                        JsonObject jsonObject = ServiceCallHelper.invokeServiceAndGetJson(input, headerMap, "contract.readRecord");
                        input.clear();
                        if (jsonObject.has("contract")) {
                            jsonElement = jsonObject.get("contract");
                            if (jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size() > 0) {
                                JsonArray jsonArray = jsonElement.getAsJsonArray();
                                jsonObject = jsonArray.get(0).getAsJsonObject();
                                if (StringUtils.isBlank(CustomerType_id) ||
                                        !CustomerType_id.contains(jsonObject.get("serviceType").getAsString())) {
                                    if (StringUtils.isNotBlank(CustomerType_id))
                                        CustomerType_id = CustomerType_id + ",";
                                    CustomerType_id = CustomerType_id + jsonObject.get("serviceType").getAsString();
                                }
                            }
                        }
                    }
            }
        } else {
            String filter = "id eq " + id;
            Map<String, Object> input = new HashMap<>();
            input.put("$filter", filter);
            JsonObject result = ServiceCallHelper.invokeServiceAndGetJson(input, headerMap, "Membership.readRecord");
            if (result.has("membership")) {
                JsonArray jsonArray = result.get("membership").isJsonArray() ? result.get("membership").getAsJsonArray() : new JsonArray();
                if (jsonArray.size() > 0) {
                    result = jsonArray.get(0).getAsJsonObject();
                    if (result.has("isBusinessType") &&
                            !result.get("isBusinessType").isJsonNull())
                        if (Boolean.parseBoolean(result.get("isBusinessType").getAsString()) || "1"
                                .equals(result.get("isBusinessType").getAsString())) {
                            CustomerType_id = (String)HelperMethods.getCustomerTypes().get("Business");
                        } else {
                            CustomerType_id = (String)HelperMethods.getCustomerTypes().get("Retail");
                        }
                }
            }
        }
        if (StringUtils.isBlank(CustomerType_id) && json.has("CustomerType_id") &&
                !json.get("CustomerType_id").isJsonNull()) {
            CustomerType_id = json.get("CustomerType_id").getAsString();
        } else if (StringUtils.isBlank(CustomerType_id) && json.has("CustomerType_Id") &&
                !json.get("CustomerType_Id").isJsonNull()) {
            CustomerType_id = json.get("CustomerType_Id").getAsString();
        } else if (StringUtils.isBlank(CustomerType_id) && json.has("CustomerTypeId") &&
                !json.get("CustomerTypeId").isJsonNull()) {
            CustomerType_id = json.get("CustomerTypeId").getAsString();
        }
        if (CustomerType_id.contains((CharSequence)HelperMethods.getCustomerTypes().get("Business"))) {
            json.addProperty("isBusiness", "true");
        } else {
            json.addProperty("isBusiness", "false");
        }
        json.addProperty("CustomerType_id", CustomerType_id);
    }

    public DBXResult getCustomerRequestNotificationCount(CustomerDTO customerDTO, Map<String, Object> headerMap) {
        DBXResult dbxResult = new DBXResult();
        JsonObject jsonObject = new JsonObject();
        if (customerDTO == null || StringUtils.isBlank(customerDTO.getId())) {
            dbxResult.setResponse(jsonObject);
            return dbxResult;
        }
        Map<String, Object> postParametersMap = new HashMap<>();
        postParametersMap.put("$filter", "customerId eq '" + customerDTO.getId() + "'");
        JsonObject result = ServiceCallHelper.invokeServiceAndGetJson(postParametersMap, headerMap, "Card_Request_Notification_Count_View");
        if (result == null || !result.has("card_request_notification_count_view")) {
            dbxResult.setResponse(jsonObject);
            return dbxResult;
        }
        int totalRequestCount = 0, totalNotificationCount = 0;
        JsonArray countArray = result.get("card_request_notification_count_view").getAsJsonArray();
        JsonObject currRecordJSONObject = null;
        for (int indexVar = 0; indexVar < countArray.size(); indexVar++) {
            if (countArray.get(indexVar).isJsonObject()) {
                currRecordJSONObject = countArray.get(indexVar).getAsJsonObject();
                if (currRecordJSONObject.has("reqType")) {
                    int currRequestCount = 0;
                    if (currRecordJSONObject.has("requestcount"))
                        currRequestCount = currRecordJSONObject.get("requestcount").getAsInt();
                    if (currRecordJSONObject.has("reqType"))
                        if (currRecordJSONObject.get("reqType").getAsString()
                                .equalsIgnoreCase("REQUEST")) {
                            totalRequestCount += currRequestCount;
                        } else if (currRecordJSONObject.get("reqType").getAsString()
                                .equalsIgnoreCase("NOTIFICATION")) {
                            totalNotificationCount += currRequestCount;
                        }
                }
            }
            jsonObject.addProperty("requestCount", String.valueOf(totalRequestCount));
            jsonObject.addProperty("notificationCount", String.valueOf(totalNotificationCount));
            dbxResult.setResponse(jsonObject);
        }
        return dbxResult;
    }

    public DBXResult getCustomerDetailsToAdmin(CustomerDTO customerDTO, Map<String, Object> headerMap) {
        UserManagementBackendDelegateImpl backendDelegateImpl = (UserManagementBackendDelegateImpl)DBPAPIAbstractFactoryImpl.getBackendDelegate(UserManagementBackendDelegateImpl.class);
        DBXResult dbxResult = backendDelegateImpl.get(customerDTO, headerMap);
        if (dbxResult.getResponse() != null)
            customerDTO = (CustomerDTO)dbxResult.getResponse();
        String currencyCode = HelperMethods.getCurrencyCode(customerDTO.getCountryCode());
        try {
            customerDTO.setDateOfBirth(
                    HelperMethods.convertDateFormat(customerDTO.getDateOfBirth(), "yyyy-MM-dd'T'hh:mm:ss'Z'"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JsonObject record = DTOUtils.getJsonObjectFromObject(customerDTO, true);
        record.addProperty("currencyCode", currencyCode);
        record.addProperty("dateOfBirth", record.get("DateOfBirth").getAsString());
        CommunicationBackendDelegateImpl backendDelegate = new CommunicationBackendDelegateImpl();
        CustomerCommunicationDTO communicationDTO = new CustomerCommunicationDTO();
        communicationDTO.setCustomer_id(customerDTO.getId());
        dbxResult = backendDelegate.get(communicationDTO, headerMap);
        if (dbxResult.getResponse() != null) {
            List<CustomerCommunicationDTO> dtoList = (List<CustomerCommunicationDTO>)dbxResult.getResponse();
            if (dtoList != null && dtoList.size() > 0)
                for (CustomerCommunicationDTO rec : dtoList) {
                    if (rec.getIsPrimary().booleanValue()) {
                        String type = rec.getType_id();
                        String key = "";
                        if ("COMM_TYPE_EMAIL".equalsIgnoreCase(type)) {
                            key = "Email";
                        } else {
                            key = "Phone";
                        }
                        record.addProperty(key, rec.getValue());
                    }
                }
        }
        dbxResult.setResponse(record);
        return dbxResult;
    }

    public DBXResult checkifUserEnrolled(CustomerDTO customerDTO, Map<String, Object> headerMap) {
        DBXResult dbxResult = new DBXResult();
        JsonObject jsonObject = new JsonObject();
        dbxResult.setResponse(jsonObject);
        String filter = "";
        if (StringUtils.isNotBlank(customerDTO.getSsn()))
            filter = filter + "Ssn eq " + customerDTO.getSsn();
        if (StringUtils.isNotBlank(customerDTO.getLastName())) {
            if (StringUtils.isNotBlank(filter))
                filter = filter + " and ";
            filter = filter + "LastName eq " + customerDTO.getLastName();
        }
        if (StringUtils.isNotBlank(customerDTO.getDateOfBirth())) {
            if (StringUtils.isNotBlank(filter))
                filter = filter + " and ";
            filter = filter + "DateOfBirth eq " + customerDTO.getDateOfBirth();
        }
        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("$filter", filter);
        JsonObject customerObject = ServiceCallHelper.invokeServiceAndGetJson(inputParams, headerMap, "Customer.readRecord");
        if (!customerObject.has("customer")) {
            dbxResult.setError(ErrorCodeEnum.ERR_10024);
            return dbxResult;
        }
        JsonArray customers = customerObject.get("customer").getAsJsonArray();
        JsonObject customer = null;
        if (customers.size() > 0)
            for (int i = 0; i < customers.size(); i++) {
                if (!HelperMethods.getBusinessUserTypes().contains(customers.get(i).getAsJsonObject().get("CustomerType_id").getAsString())) {
                    customer = customers.get(i).getAsJsonObject();
                    break;
                }
            }
        if (customer != null) {
            String isUserEnrolled = "true";
            Param p = null;
            boolean isEnrolled = Boolean.parseBoolean(customer.get("isEnrolled").getAsString());
            boolean isEnrolledFromSpotlight = (Boolean.parseBoolean(customer.get("isEnrolledFromSpotlight").getAsString()) || "1".equals(customer.get("isEnrolledFromSpotlight").getAsString()));
            String password = new String();
            if (customer.getAsJsonObject().has("Password") && !customer.get("Password").isJsonNull() &&
                    StringUtils.isNotBlank(customer.get("Password").getAsString()))
                password = customer.get("Password").getAsString();
            if (isEnrolled) {
                isUserEnrolled = "true";
                jsonObject.addProperty("result", "User Already Enrolled");
            } else if (StringUtils.isBlank(password) || (
                    StringUtils.isNotBlank(password) && !isEnrolledFromSpotlight)) {
                p = new Param("result", "User Not Enrolled", "string");
                isUserEnrolled = "false";
                jsonObject.addProperty("result", "User Not Enrolled");
                String userId = customer.get("id").getAsString();
                JsonObject communication = getCommunicationData(userId, headerMap);
                JsonObject requestPayload = getRequestPayload(customerDTO);
                String communicationString = communication.toString();
                String requestPayloadString = requestPayload.toString();
                jsonObject.addProperty("communication", communicationString);
                jsonObject.addProperty("requestPayload", requestPayloadString);
            }
            jsonObject.addProperty("isUserEnrolled", isUserEnrolled);
        } else {
            JsonObject requestPayload = getRequestPayload(customerDTO);
            String requestPayloadString = requestPayload.toString();
            jsonObject.addProperty("requestPayload", requestPayloadString);
            jsonObject.addProperty("errmsg", "No Record Found");
        }
        dbxResult.setResponse(jsonObject);
        return dbxResult;
    }

    private JsonObject getRequestPayload(CustomerDTO customerDTO) {
        JsonObject payload = new JsonObject();
        payload.addProperty("Ssn", customerDTO.getSsn());
        payload.addProperty("LastName", customerDTO.getLastName());
        payload.addProperty("DateOfBirth", customerDTO.getDateOfBirth());
        return payload;
    }

    public JsonObject getCommunicationData(String user_id, Map<String, Object> headerMap) {
        JsonObject communication = new JsonObject();
        CommunicationBackendDelegateImpl backendDelegateImpl = new CommunicationBackendDelegateImpl();
        CustomerCommunicationDTO dto = new CustomerCommunicationDTO();
        dto.setCustomer_id(user_id);
        DBXResult dbxResult = backendDelegateImpl.get(dto, headerMap);
        List<CustomerCommunicationDTO> dtoList = (List<CustomerCommunicationDTO>)dbxResult.getResponse();
        JsonArray phone = new JsonArray();
        JsonArray email = new JsonArray();
        JsonObject contact = new JsonObject();
        if (dtoList != null && dtoList.size() > 0) {
            for (CustomerCommunicationDTO record : dtoList) {
                contact = new JsonObject();
                if (record.getType_id().equals("COMM_TYPE_EMAIL")) {
                    contact.addProperty("unmasked", record.getValue());
                    if (record.getIsPrimary().booleanValue())
                        contact.addProperty("isPrimary", "true");
                    email.add((JsonElement)contact);
                    continue;
                }
                String mobile = record.getValue();
                contact.addProperty("unmasked", mobile);
                if (record.getIsPrimary().booleanValue())
                    contact.addProperty("isPrimary", "true");
                phone.add((JsonElement)contact);
            }
            communication.add("phone", (JsonElement)phone);
            communication.add("email", (JsonElement)email);
        }
        return communication;
    }

    public DBXResult sendCustomerUnlockEmail(CustomerDTO customerDTO, Map<String, Object> headerMap) {
        DBXResult dbxResult = new DBXResult();
        JsonObject jsonObject = new JsonObject();
        customerDTO = (CustomerDTO)customerDTO.loadDTO();
        if (customerDTO == null) {
            jsonObject.addProperty("mailRequestSent", "false");
            ErrorCodeEnum.ERR_10192.setErrorCode(jsonObject);
            dbxResult.setResponse(jsonObject);
            return dbxResult;
        }
        CommunicationBackendDelegate backendDelegate = (CommunicationBackendDelegate)DBPAPIAbstractFactoryImpl.getBackendDelegate(CommunicationBackendDelegateImpl.class);
        CustomerCommunicationDTO communicationDTO = new CustomerCommunicationDTO();
        communicationDTO.setCustomer_id(customerDTO.getId());
        dbxResult = backendDelegate.getPrimaryCommunicationForLogin(communicationDTO, headerMap);
        String email = "";
        if (dbxResult.getResponse() != null) {
            JsonObject communicaiton = (JsonObject)dbxResult.getResponse();
            communicaiton = communicaiton.has("customercommunication") ? communicaiton.get("customercommunication").getAsJsonObject() : new JsonObject();
            email = communicaiton.has("Email") ? communicaiton.get("Email").getAsString() : "";
        }
        if (StringUtils.isBlank(email)) {
            jsonObject.addProperty("mailRequestSent", "false");
            ErrorCodeEnum.ERR_10193.setErrorCode(jsonObject);
            dbxResult.setResponse(jsonObject);
            return dbxResult;
        }
        String filter = "UserName eq " + customerDTO.getUserName();
        Map<String, Object> inputParam = new HashMap<>();
        inputParam.put("$filter", filter);
        JsonObject checkerResult = ServiceCallHelper.invokeServiceAndGetJson(inputParam, headerMap, "credentialChecker.readRecord");
        if (checkerResult.has("credentialchecker")) {
            JsonArray existingRecords = checkerResult.get("credentialchecker").getAsJsonArray();
            for (int i = 0; i < existingRecords.size(); i++) {
                if (existingRecords.get(i).getAsJsonObject().get("linktype").getAsString()
                        .equals(HelperMethods.CREDENTIAL_TYPE.UNLOCK.toString())) {
                    String existingToken = existingRecords.get(i).getAsJsonObject().get("id").getAsString();
                    inputParam = new HashMap<>();
                    inputParam.put("id", existingToken);
                    ServiceCallHelper.invokeServiceAndGetJson(inputParam, headerMap, "credentialChecker.deleteRecord");
                }
            }
        }
        String activationToken = UUID.randomUUID().toString();
        Map<String, Object> map = new HashMap<>();
        map.put("id", activationToken);
        map.put("UserName", customerDTO.getUserName());
        map.put("linktype", HelperMethods.CREDENTIAL_TYPE.UNLOCK.toString());
        map.put("createdts", HelperMethods.getCurrentTimeStamp());
        JsonObject createCredential = ServiceCallHelper.invokeServiceAndGetJson(map, headerMap, "credentialChecker.createRecord");
        if (!HelperMethods.hasError(createCredential)) {
            String link = URLFinder.getServerRuntimeProperty("DBX_CUSTOMER_UNLOCK_LINK") + "?qp=" + new String(Base64.getEncoder().encode(activationToken.getBytes()));
            PasswordLockoutSettingsDTO settingsDTO = (PasswordLockoutSettingsDTO)(new PasswordLockoutSettingsDTO()).loadDTO();
            Map<String, Object> input = new HashMap<>();
            input.put("Subscribe", "true");
            input.put("FirstName", customerDTO.getFirstName());
            input.put("EmailType", "UNLOCK_CUSTOMER");
            input.put("LastName", customerDTO.getLastName());
            JSONObject addContext = new JSONObject();
            addContext.put("unlockAccountLink", link);
            addContext.put("userName", customerDTO.getUserName());
            addContext.put("linkExpiry", String.valueOf(Math.floorDiv(settingsDTO.getRecoveryEmailLinkValidity(), 60)));
            input.put("AdditionalContext", KMSUtil.getOTPContent(null, null, addContext));
            input.put("Email", email);
            headerMap.put("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            HelperMethods.callApiAsync(input, headerMap, "KMS.sendEmailOrch");
            jsonObject.addProperty("mailRequestSent", "true");
            dbxResult.setResponse(jsonObject);
            return dbxResult;
        }
        jsonObject.addProperty("mailRequestSent", "false");
        dbxResult.setResponse(jsonObject);
        return dbxResult;
    }

    public DBXResult fetchCustomerIdForEnrollment(CustomerDTO customerDTO, Map<String, Object> headerMap) throws ApplicationException {
        String IS_Integrated = Boolean.toString(IntegrationTemplateURLFinder.isIntegrated);
        try {
            if (StringUtils.isNotBlank(IS_Integrated) && IS_Integrated.equalsIgnoreCase("true"))
                return fetchCustomerIdForEnrollmentForT24(customerDTO, headerMap);
        } catch (ApplicationException e) {
            throw new ApplicationException(e.getErrorCodeEnum());
        } catch (Exception e) {
            this.logger.error("Exception occured while enrolling customer in Infinity Digital banking through T24" + e
                    .getMessage());
            throw new ApplicationException(ErrorCodeEnum.ERR_10738);
        }
        DBXResult result = new DBXResult();
        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("$filter", "id eq " + customerDTO.getId());
        try {
            boolean isPresentInDBXDB = false;
            String customerIdinDB = customerDTO.getId();
            String coreId = customerDTO.getId();
            inputParams.put("$filter", "id eq " + customerIdinDB);
            JsonObject customerJson = ServiceCallHelper.invokeServiceAndGetJson(inputParams, headerMap, "Customer.readRecord");
            isPresentInDBXDB = (JSONUtil.hasKey(customerJson, "opstatus") && JSONUtil.getString(customerJson, "opstatus").equalsIgnoreCase("0") && JSONUtil.hasKey(customerJson, "customer") && customerJson.get("customer").getAsJsonArray().size() > 0);
            BackendIdentifierDTO backendIdentifierDTO = new BackendIdentifierDTO();
            if (isPresentInDBXDB) {
                backendIdentifierDTO.setCustomer_id(customerIdinDB);
            } else {
                backendIdentifierDTO.setBackendId(coreId);
            }
            backendIdentifierDTO.setBackendType("CORE");
            BackendIdentifierDTO identifierDTO = new BackendIdentifierDTO();
            try {
                DBXResult dbxResult = ((BackendIdentifiersBackendDelegate)DBPAPIAbstractFactoryImpl.getBackendDelegate(BackendIdentifiersBackendDelegate.class)).get(backendIdentifierDTO, headerMap);
                identifierDTO = (BackendIdentifierDTO)dbxResult.getResponse();
            } catch (ApplicationException e1) {
                this.logger.error("Exception occured while fetching party backendidentifier ID" + customerDTO.getId());
                return result;
            }
            if (identifierDTO != null) {
                customerIdinDB = identifierDTO.getCustomer_id();
                coreId = identifierDTO.getBackendId();
            }
            inputParams.clear();
            inputParams.put("$filter", "id eq " + customerIdinDB);
            JsonObject response = ServiceCallHelper.invokeServiceAndGetJson(inputParams, headerMap, "Customer.readRecord");
            if (JSONUtil.hasKey(response, "opstatus") &&
                    JSONUtil.getString(response, "opstatus").equalsIgnoreCase("0") &&
                    JSONUtil.hasKey(response, "customer") && response
                    .get("customer").getAsJsonArray().size() > 0) {
                result.setResponse(DTOUtils.loadJsonObjectIntoObject(response
                        .get("customer").getAsJsonArray().get(0).getAsJsonObject(), CustomerDTO.class, true));
                CustomerDTO dto = (CustomerDTO)result.getResponse();
                if (dto.getIsEnrolled().booleanValue())
                    throw new ApplicationException(ErrorCodeEnum.ERR_10748);
                if (!"1".equals(dto.getIsEnrolledFromSpotlight())) {
                    inputParams.clear();
                    inputParams.put("id", dto.getId());
                    inputParams.put("isEnrolledFromSpotlight", "1");
                    ServiceCallHelper.invokeServiceAndGetJson(inputParams, headerMap, "Customer.updateRecord");
                }
                CustomerGroupDTO customerGroupDTO = new CustomerGroupDTO();
                CustomerGroupBusinessDelegate customerGroup = (CustomerGroupBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(CustomerGroupBusinessDelegate.class);
                customerGroupDTO.setCustomerId(dto.getId());
                customerGroupDTO.setGroupId("DEFAULT_GROUP");
                List<CustomerGroupDTO> list = customerGroup.getCustomerGroup(customerGroupDTO, headerMap);
                if (list != null && list.isEmpty())
                    customerGroup.createCustomerGroup(customerGroupDTO, headerMap);
            }
        } catch (Exception e) {
            this.logger.error("Exception occured while enrolling customer in Infinity Digital banking" + e.getMessage());
            throw new ApplicationException(ErrorCodeEnum.ERR_10738);
        }
        return result;
    }

    private DBXResult fetchCustomerIdForEnrollmentForT24(CustomerDTO customerDTO, Map<String, Object> headersMap) throws ApplicationException {
        DBXResult result = new DBXResult();
        CustomerDTO responseDTO = new CustomerDTO();
        Map<String, Object> inputParams = new HashMap<>();
        boolean isPresentInDBXDB = false;
        String customerIdinDB = customerDTO.getId();
        String t24Id = customerDTO.getId();
        if (StringUtils.isBlank(customerDTO.getId()))
            return result;
        inputParams.put("$filter", "id eq " + customerIdinDB);
        JsonObject customerJson = ServiceCallHelper.invokeServiceAndGetJson(inputParams, headersMap, "Customer.readRecord");
        isPresentInDBXDB = (JSONUtil.hasKey(customerJson, "opstatus") && JSONUtil.getString(customerJson, "opstatus").equalsIgnoreCase("0") && JSONUtil.hasKey(customerJson, "customer") && customerJson.get("customer").getAsJsonArray().size() > 0);
        BackendIdentifierDTO backendIdentifierDTO = new BackendIdentifierDTO();
        if (isPresentInDBXDB) {
            backendIdentifierDTO.setCustomer_id(customerIdinDB);
        } else {
            backendIdentifierDTO.setBackendId(t24Id);
        }
        backendIdentifierDTO.setBackendType(IntegrationTemplateURLFinder.getBackendURL("BackendType"));
        BackendIdentifierDTO identifierDTO = new BackendIdentifierDTO();
        try {
            DBXResult dbxResult = ((BackendIdentifiersBackendDelegate)DBPAPIAbstractFactoryImpl.getBackendDelegate(BackendIdentifiersBackendDelegate.class)).get(backendIdentifierDTO, headersMap);
            identifierDTO = (BackendIdentifierDTO)dbxResult.getResponse();
        } catch (ApplicationException e1) {
            this.logger.error("Exception occured while fetching party backendidentifier ID" + customerDTO.getId());
            return result;
        }
        if (identifierDTO != null) {
            customerIdinDB = identifierDTO.getCustomer_id();
            t24Id = identifierDTO.getBackendId();
        }
        try {
            if (!isPresentInDBXDB && identifierDTO == null) {
                HelperMethods.addJWTAuthHeader(headersMap, "PreLogin");
                inputParams.put("customerId", t24Id);
                headersMap.put("companyId",
                        EnvironmentConfigurationsHandler.getValue("BRANCH_ID_REFERENCE"));
                JsonObject t24Response = ServiceCallHelper.invokeServiceAndGetJson(ServiceId.T24ISUSER_INTEGRATION_SERVICE, null, OperationName.CORE_CUSTOMER_SEARCH, inputParams, headersMap);
                JsonObject customerResponse = new JsonObject();
                if (!JSONUtil.hasKey(t24Response, "customers") || t24Response
                        .get("customers").getAsJsonArray().size() < 0)
                    throw new ApplicationException(ErrorCodeEnum.ERR_10741);
                customerResponse = t24Response.get("customers").getAsJsonArray().get(0).getAsJsonObject();
                SystemConfigurationBusinessDelegate systemConfigBD = (SystemConfigurationBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(SystemConfigurationBusinessDelegate.class);
                Integer userNameLength = Integer.valueOf(
                        Integer.parseInt(systemConfigBD.getSystemConfigurationValue("USERNAME_LENGTH", headersMap)));
                String customerId = String.valueOf(HelperMethods.getNumericId(userNameLength));
                inputParams.put("id", customerId);
                inputParams.put("UserName", inputParams.get("id"));
                inputParams.put("Status_id", "SID_CUS_NEW");
                inputParams.put("isEnrolledFromSpotlight", "1");
                inputParams.put("DateOfBirth", JSONUtil.getString(customerResponse, "dateOfBirth"));
                ServiceCallHelper.invokeServiceAndGetJson(inputParams, headersMap, "Customer.createRecord");
                SimpleDateFormat idformatter = new SimpleDateFormat("yyMMddHHmmssSSS");
                CustomerCommunicationDTO commDTO = new CustomerCommunicationDTO();
                commDTO.setIsNew(true);
                inputParams.clear();
                inputParams.put("Customer_id", customerId);
                inputParams.put("id", "CUS_" + idformatter.format(new Date()));
                inputParams.put("Type_id", "COMM_TYPE_EMAIL");
                inputParams.put("Value", JSONUtil.getString(customerResponse, "email"));
                commDTO.persist(inputParams, headersMap);
                inputParams.clear();
                inputParams.put("Customer_id", customerId);
                inputParams.put("id", "CUS_" + idformatter.format(new Date()));
                inputParams.put("Type_id", "COMM_TYPE_PHONE");
                inputParams.put("Value", JSONUtil.getString(customerResponse, "phone"));
                commDTO.persist(inputParams, headersMap);
                inputParams.clear();
                inputParams.put("id", UUID.randomUUID().toString());
                inputParams.put("Customer_id", customerId);
                inputParams.put("customerId", "1");
                inputParams.put("BackendId", customerDTO.getId());
                inputParams.put("BackendType",
                        IntegrationTemplateURLFinder.getBackendURL("BackendType"));
                inputParams.put("identifier_name",
                        IntegrationTemplateURLFinder.getBackendURL("BackendCustomerIdentifierName"));
                inputParams.put("sequenceNumber", "9");
                ServiceCallHelper.invokeServiceAndGetJson(inputParams, headersMap, "backendidentifier.createRecord");
                CustomerGroupDTO customerGroupDTO = new CustomerGroupDTO();
                CustomerGroupBusinessDelegate customerGroup = (CustomerGroupBusinessDelegate)DBPAPIAbstractFactoryImpl.getBusinessDelegate(CustomerGroupBusinessDelegate.class);
                customerGroupDTO.setCustomerId(customerId);
                customerGroupDTO.setGroupId("DEFAULT_GROUP");
                customerGroup.createCustomerGroup(customerGroupDTO, headersMap);
                responseDTO.setId(customerId);
                responseDTO.setFirstName(JSONUtil.getString(customerResponse, "firstName"));
                responseDTO.setLastName(JSONUtil.getString(customerResponse, "lastName"));
                responseDTO.setUserName(customerId);
            } else {
                inputParams.put("$filter", "id eq " + customerIdinDB);
                if (t24Id != customerIdinDB)
                    customerJson = ServiceCallHelper.invokeServiceAndGetJson(inputParams, headersMap, "Customer.readRecord");
                CustomerDTO infintyCustomerDTO = (CustomerDTO)DTOUtils.loadJsonObjectIntoObject(customerJson
                        .get("customer").getAsJsonArray().get(0).getAsJsonObject(), CustomerDTO.class, true);
                String scaenabled = EnvironmentConfigurationsHandler.getValue("IS_SCA_ENABLED");
                if (StringUtils.isNotBlank(scaenabled) && !Boolean.valueOf(scaenabled).booleanValue() &&
                        infintyCustomerDTO.getIsEnrolled().booleanValue())
                    throw new ApplicationException(ErrorCodeEnum.ERR_10748);
                responseDTO.setId(infintyCustomerDTO.getId());
                responseDTO.setUserName(infintyCustomerDTO.getUserName());
                responseDTO.setFirstName(infintyCustomerDTO.getFirstName());
                responseDTO.setLastName(infintyCustomerDTO.getLastName());
                responseDTO.setIsEnrolled(infintyCustomerDTO.getIsEnrolled());
            }
            result.setResponse(responseDTO);
        } catch (ApplicationException e) {
            throw new ApplicationException(e.getErrorCodeEnum());
        } catch (Exception e) {
            this.logger.error("Exception occured while creating Infinity Digital profile" + e.getMessage());
            throw new ApplicationException(ErrorCodeEnum.ERR_10741);
        }
        return result;
    }

    public DBXResult getAddressTypes(Map<String, Object> map) {
        JsonObject jsonObject = ServiceCallHelper.invokeServiceAndGetJson(new HashMap<>(), map, "addressTypes.readRecord");
        DBXResult dbxResult = new DBXResult();
        JsonObject result = new JsonObject();
        if (jsonObject.has("addresstype") &&
                !jsonObject.get("addresstype").isJsonNull()) {
            dbxResult.setResponse(jsonObject);
        } else {
            ErrorCodeEnum.ERR_10751.setErrorCode(result);
        }
        return dbxResult;
    }


  }
