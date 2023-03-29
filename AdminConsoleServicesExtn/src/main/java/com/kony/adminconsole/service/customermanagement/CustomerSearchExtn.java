package com.kony.adminconsole.service.customermanagement;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.dto.MemberSearchBean;
import com.kony.adminconsole.dto.MemberSearchBeanExtn;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.*;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class CustomerSearchExtn implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerSearch.class);

    static final String APPLICANT_SEARCH = "APPLICANT_SEARCH";

    static final String APPLICANT_SEARCH_TOTAL_COUNT = "APPLICANT_SEARCH_TOTAL_COUNT";

    static final String GROUP_SEARCH = "GROUP_SEARCH";

    static final String GROUP_SEARCH_TOTAL_COUNT = "GROUP_SEARCH_TOTAL_COUNT";

    static final String CUSTOMER_SEARCH = "CUSTOMER_SEARCH";

    static final String CUSTOMER_SEARCH_TOTAL_COUNT = "CUSTOMER_SEARCH_TOTAL_COUNT";

    public static final String STATUS_SUCCESS = "Success";

    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        try {
            if (StringUtils.isNotBlank(requestInstance.getParameter("_sortVariable")) && requestInstance
                    .getParameter("_sortVariable").contains(" ")) {
                ErrorCodeEnum.ERR_20541.setErrorCode(processedResult);
                return processedResult;
            }
            if (StringUtils.isNotBlank(requestInstance.getParameter("_sortDirection")) && (requestInstance
                    .getParameter("_sortDirection").contains(" ") || (
                    !requestInstance.getParameter("_sortDirection").equalsIgnoreCase("ASC") &&
                            !requestInstance.getParameter("_sortDirection").equalsIgnoreCase("DESC")))) {
                ErrorCodeEnum.ERR_20541.setErrorCode(processedResult);
                return processedResult;
            }
            String id = requestInstance.getParameter("_id");
            if (StringUtils.isBlank(id) || id.equalsIgnoreCase("null"))
                id = "";
            String name = requestInstance.getParameter("_name");
            if (StringUtils.isBlank(name) || name.equalsIgnoreCase("null"))
                name = "";
            String SSN = requestInstance.getParameter("_SSN");
            if (StringUtils.isBlank(SSN) || SSN.equalsIgnoreCase("null"))
                SSN = "";
            String username = requestInstance.getParameter("_username");
            if (StringUtils.isBlank(username) || username.equalsIgnoreCase("null"))
                username = "";
            String phone = requestInstance.getParameter("_phone");
            if (StringUtils.isBlank(phone) || phone.equalsIgnoreCase("null"))
                phone = "";
            String email = requestInstance.getParameter("_email");
            if (StringUtils.isBlank(email) || email.equalsIgnoreCase("null"))
                email = "";
            String accNo = requestInstance.getParameter("_cardorAccountnumber");
            if (StringUtils.isBlank(accNo) || accNo.equalsIgnoreCase("null"))
                accNo = "";
            String Tin = requestInstance.getParameter("_TIN");
            if (StringUtils.isBlank(Tin) || Tin.equalsIgnoreCase("null"))
                Tin = "";
            String IDType = requestInstance.getParameter("_IDType");
            if (StringUtils.isBlank(IDType) || IDType.equalsIgnoreCase("null"))
                IDType = "";
            String IDValue = requestInstance.getParameter("_IDValue");
            if (StringUtils.isBlank(IDValue) || IDValue.equalsIgnoreCase("null"))
                IDValue = "";
            String CompanyId = requestInstance.getParameter("_companyId");
            if (StringUtils.isBlank(CompanyId) || CompanyId.equalsIgnoreCase("null"))
                CompanyId = "";
            String searchType = requestInstance.getParameter("_searchType");
            if (StringUtils.isBlank(searchType) || searchType.equalsIgnoreCase("null"))
                searchType = "";
            String dateOfBirth = requestInstance.getParameter("_dateOfBirth");
            if (StringUtils.isBlank(dateOfBirth) || dateOfBirth.equalsIgnoreCase("null"))
                dateOfBirth = "";
            String customerId = requestInstance.getParameter("_customerId");
            if (StringUtils.isBlank(customerId) || customerId.equalsIgnoreCase("null"))
                customerId = "";
            String isStaffMember = requestInstance.getParameter("_IsStaffMember");
            if (StringUtils.isBlank(isStaffMember) || isStaffMember.equalsIgnoreCase("null"))
                isStaffMember = "";
            String group = requestInstance.getParameter("_group");
            if (StringUtils.isBlank(group) || group.equalsIgnoreCase("null"))
                group = "";
            String requestID = requestInstance.getParameter("_requestID");
            if (StringUtils.isBlank(requestID) || requestID.equalsIgnoreCase("null"))
                requestID = "";
            String branchIDS = requestInstance.getParameter("_branchIDS");
            if (StringUtils.isBlank(branchIDS) || branchIDS.equalsIgnoreCase("null"))
                branchIDS = "";
            String productIDS = requestInstance.getParameter("_productIDS");
            if (StringUtils.isBlank(productIDS) || productIDS.equalsIgnoreCase("null"))
                productIDS = "";
            String cityIDS = requestInstance.getParameter("_cityIDS");
            if (StringUtils.isBlank(cityIDS) || cityIDS.equalsIgnoreCase("null"))
                cityIDS = "";
            String entitlementIDS = requestInstance.getParameter("_entitlementIDS");
            if (StringUtils.isBlank(entitlementIDS) || entitlementIDS.equalsIgnoreCase("null"))
                entitlementIDS = "";
            String groupIDS = requestInstance.getParameter("_groupIDS");
            if (StringUtils.isBlank(groupIDS) || groupIDS.equalsIgnoreCase("null"))
                groupIDS = "";
            String custStatus = requestInstance.getParameter("_customerStatus");
            if (StringUtils.isBlank(custStatus) || custStatus.equalsIgnoreCase("null"))
                custStatus = "";
            String before = requestInstance.getParameter("_before");
            if (StringUtils.isBlank(before) || before.equalsIgnoreCase("null"))
                before = "";
            String after = requestInstance.getParameter("_after");
            if (StringUtils.isBlank(after) || after.equalsIgnoreCase("null"))
                after = "";
            MemberSearchBeanExtn memberSearchBean = new MemberSearchBeanExtn();
            String authToken = requestInstance.getHeader("X-Kony-Authorization");
            memberSearchBean.setSearchType(searchType);
            memberSearchBean.setMemberId(id);
            memberSearchBean.setCustomerName(name);
            memberSearchBean.setSsn(SSN);
            memberSearchBean.setCustomerUsername(username);
            memberSearchBean.setCustomerPhone(phone);
            memberSearchBean.setCustomerEmail(email);
            memberSearchBean.setIsStaffMember(isStaffMember);
            memberSearchBean.setCardorAccountnumber(accNo);
            memberSearchBean.setTin(Tin);
            memberSearchBean.setCustomerGroup(group);
            memberSearchBean.setCustomerIDType(IDType);
            memberSearchBean.setCustomerIDValue(IDValue);
            memberSearchBean.setCustomerCompanyId(CompanyId);
            memberSearchBean.setCustomerRequest(requestID);
            memberSearchBean.setBranchIDS(branchIDS);
            memberSearchBean.setProductIDS(productIDS);
            memberSearchBean.setCityIDS(cityIDS);
            memberSearchBean.setEntitlementIDS(entitlementIDS);
            memberSearchBean.setGroupIDS(groupIDS);
            memberSearchBean.setCustomerStatus(custStatus);
            memberSearchBean.setBeforeDate(before);
            memberSearchBean.setAfterDate(after);
            memberSearchBean.setDateOfBirth(dateOfBirth);
            memberSearchBean.setCustomerId(customerId);
            memberSearchBean.setSortVariable(requestInstance.getParameter("_sortVariable"));
            memberSearchBean.setSortDirection(requestInstance.getParameter("_sortDirection"));
            memberSearchBean.setPageOffset(requestInstance.getParameter("_pageOffset"));
            memberSearchBean.setPageSize(requestInstance.getParameter("_pageSize"));
            if (StringUtils.isBlank(memberSearchBean.getSearchType())) {
                ErrorCodeEnum.ERR_20690.setErrorCode(processedResult);
                return processedResult;
            }
            processedResult
                    .addParam(new Param("SortVariable", memberSearchBean.getSortVariable(), "string"));
            processedResult
                    .addParam(new Param("SortDirection", memberSearchBean.getSortDirection(), "string"));
            processedResult.addParam(new Param("PageOffset",
                    String.valueOf(memberSearchBean.getPageOffset()), "int"));
            processedResult.addParam(new Param("PageSize",
                    String.valueOf(memberSearchBean.getPageSize()), "int"));
            if (StringUtils.isBlank(id) &&
                    StringUtils.isBlank(customerId) &&
                    StringUtils.isBlank(dateOfBirth) &&
                    StringUtils.isBlank(name) &&
                    StringUtils.isBlank(SSN) &&
                    StringUtils.isBlank(username) &&
                    StringUtils.isBlank(phone) &&
                    StringUtils.isBlank(email) &&
                    StringUtils.isBlank(accNo) &&
                    StringUtils.isBlank(Tin) &&
                    StringUtils.isBlank(IDType) &&
                    StringUtils.isBlank(IDValue) &&
                    StringUtils.isBlank(CompanyId) && searchType
                    .equalsIgnoreCase("CUSTOMER_SEARCH")) {
                processedResult.addParam(new Param("TotalResultsFound", "0", "int"));
                Dataset recordsDS = new Dataset();
                recordsDS.setId("records");
                processedResult.addDataset(recordsDS);
                return processedResult;
            }
            if (memberSearchBean.getSearchType().equalsIgnoreCase("APPLICANT_SEARCH"))
                processedResult.addParam(new Param("TotalResultsFound", "0", "int"));
            JSONObject customers = null;
            if (StringUtils.isNotBlank(accNo)) {
                if (StringUtils.isNotBlank(id) ||
                        StringUtils.isNotBlank(customerId) ||
                        StringUtils.isNotBlank(name) ||
                        StringUtils.isNotBlank(username) ||
                        StringUtils.isNotBlank(phone) ||
                        StringUtils.isNotBlank(email) ||
                        StringUtils.isNotBlank(Tin) ||
                        StringUtils.isNotBlank(IDType) ||
                        StringUtils.isNotBlank(IDValue) ||
                        StringUtils.isNotBlank(CompanyId)) {
                    processedResult.addParam(new Param("TotalResultsFound", "0", "int"));
                    Dataset recordsDS = new Dataset();
                    recordsDS.setId("records");
                    processedResult.addDataset(recordsDS);
                    return processedResult;
                }
                customers = DBPServices.getCustomerWithAccountNumber(authToken, accNo, requestInstance);
            } else {
                LOG.error("20230327");
                LOG.error(memberSearchBean.getSearchType());
                LOG.error(memberSearchBean.toString());
                customers = DBPServicesExtn.searchCustomers(authToken, memberSearchBean.getSearchType(), memberSearchBean, requestInstance);
                LOG.error(customers.toString());
            }
            LOG.error("20230327");
            LOG.error(customers.toString());
            if (customers.has("TotalResultsFound"))
                processedResult.addParam(new Param("TotalResultsFound", customers.get("TotalResultsFound").toString(), "string"));
            if (customers.has("customerbasicinfo_view")) {
                JSONObject customerViewJson = customers.getJSONObject("customerbasicinfo_view");
                SSN = customerViewJson.optString("SSN");
                String isEnrolledFromSpotlight = customerViewJson.optString("isEnrolledFromSpotlight");
                String isProfileExist = customerViewJson.optString("isProfileExist");
                String isCustomerEnrolled = customerViewJson.optString("isCustomerEnrolled");
                String userId = customerViewJson.optString("id");
                if (StringUtils.isNotBlank(userId) && (null == isEnrolledFromSpotlight ||
                        !isEnrolledFromSpotlight.equalsIgnoreCase("1") || null == isProfileExist ||
                        !isProfileExist.equalsIgnoreCase("false") || null == isCustomerEnrolled ||
                        !isCustomerEnrolled.equalsIgnoreCase("false"))) {
                    boolean hasAccess = CustomerHandler.doesCurrentLoggedinUserHasAccessToGivenCustomer("", userId, requestInstance, new Result()).booleanValue();
                    if (hasAccess) {
                        customerViewJson.put("isCustomerAccessiable", true);
                    } else {
                        customerViewJson.put("isCustomerAccessiable", false);
                    }
                }
//                customerViewJson.put("SSN", CustomerHandler.maskSSN(SSN));
                Record customerbasicinfo_view = CommonUtilities.constructRecordFromJSONObject(customerViewJson);
                customerbasicinfo_view.setId("customerbasicinfo_view");
                processedResult.addRecord(customerbasicinfo_view);
                Record configuration = new Record();
                configuration.setId("Configuration");
                if (customerViewJson.has("accountLockoutTime"))
                    configuration.addParam(new Param("value", customerViewJson.getString("accountLockoutTime")));
                processedResult.addRecord(configuration);
            }
            if (customers.has("records")) {
                JSONArray recordsArray = customers.getJSONArray("records");
                Dataset recordsDataset = CommonUtilities.constructDatasetFromJSONArray(recordsArray);
                recordsDataset.setId("records");
                Param recordsStatus = new Param("Status", "Records returned: " + recordsArray.length(), "string");
                processedResult.addDataset(recordsDataset);
                processedResult.addParam(recordsStatus);
            } else {
                Dataset recordsDataset = new Dataset();
                recordsDataset.setId("records");
                processedResult.addDataset(recordsDataset);
                if (!processedResult.hasParamByName("TotalResultsFound"))
                    processedResult.addParam(new Param("TotalResultsFound", "0", "int"));
                if (!processedResult.hasParamByName("Status"))
                    processedResult.addParam(new Param("Status", "Records returned: 0", "string"));
                return processedResult;
            }
            return processedResult;
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            processedResult.addParam(new Param("FailureReason", e.getMessage()));
            ErrorCodeEnum.ERR_20716.setErrorCode(processedResult);
            return processedResult;
        }
    }

    public static JSONObject searchCustomers(String authToken, String searchType, MemberSearchBean memberSearchBean, DataControllerRequest requestInstance) {
        Map<String, String> searchPostParameters = new HashMap<>();
        searchPostParameters.put("_searchType", searchType);
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
        searchPostParameters.put("_sortVariable", memberSearchBean.getSortVariable());
        searchPostParameters.put("_sortDirection", memberSearchBean.getSortDirection());
        searchPostParameters.put("_pageOffset", String.valueOf(memberSearchBean.getPageOffset()));
        searchPostParameters.put("_pageSize", String.valueOf(memberSearchBean.getPageSize()));
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_SEARCH_PROC_SERVICE, searchPostParameters, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

    public static Result CustomerSearchByUserName(DataControllerRequest requestInstance) {
        Result processedResult = new Result();
        try {
            if (StringUtils.isNotBlank(requestInstance.getParameter("_sortVariable")) && requestInstance
                    .getParameter("_sortVariable").contains(" ")) {
                ErrorCodeEnum.ERR_20541.setErrorCode(processedResult);
                return processedResult;
            }
            if (StringUtils.isNotBlank(requestInstance.getParameter("_sortDirection")) && (requestInstance
                    .getParameter("_sortDirection").contains(" ") || (
                    !requestInstance.getParameter("_sortDirection").equalsIgnoreCase("ASC") &&
                            !requestInstance.getParameter("_sortDirection").equalsIgnoreCase("DESC")))) {
                ErrorCodeEnum.ERR_20541.setErrorCode(processedResult);
                return processedResult;
            }
            MemberSearchBean memberSearchBean = new MemberSearchBean();
            String authToken = requestInstance.getHeader("X-Kony-Authorization");
            memberSearchBean.setSearchType(requestInstance.getParameter("_searchType"));
            memberSearchBean.setMemberId(requestInstance.getParameter("_id"));
            memberSearchBean.setCustomerName(requestInstance.getParameter("_name"));
            memberSearchBean.setSsn(requestInstance.getParameter("_SSN"));
            memberSearchBean.setCustomerUsername(requestInstance.getParameter("_username"));
            memberSearchBean.setCustomerPhone(requestInstance.getParameter("_phone"));
            memberSearchBean.setCustomerEmail(requestInstance.getParameter("_email"));
            memberSearchBean.setIsStaffMember(requestInstance.getParameter("_IsStaffMember"));
            memberSearchBean.setCardorAccountnumber(requestInstance.getParameter("_cardorAccountnumber"));
            memberSearchBean.setTin(requestInstance.getParameter("_TIN"));
            memberSearchBean.setCustomerGroup(requestInstance.getParameter("_group"));
            memberSearchBean.setCustomerIDType(requestInstance.getParameter("_IDType"));
            memberSearchBean.setCustomerIDValue(requestInstance.getParameter("_IDValue"));
            memberSearchBean.setCustomerCompanyId(requestInstance.getParameter("_companyId"));
            memberSearchBean.setCustomerRequest(requestInstance.getParameter("_requestID"));
            memberSearchBean.setBranchIDS(requestInstance.getParameter("_branchIDS"));
            memberSearchBean.setProductIDS(requestInstance.getParameter("_productIDS"));
            memberSearchBean.setCityIDS(requestInstance.getParameter("_cityIDS"));
            memberSearchBean.setEntitlementIDS(requestInstance.getParameter("_entitlementIDS"));
            memberSearchBean.setGroupIDS(requestInstance.getParameter("_groupIDS"));
            memberSearchBean.setCustomerStatus(requestInstance.getParameter("_customerStatus"));
            memberSearchBean.setBeforeDate(requestInstance.getParameter("_before"));
            memberSearchBean.setAfterDate(requestInstance.getParameter("_after"));
            memberSearchBean.setSortVariable(requestInstance.getParameter("_sortVariable"));
            memberSearchBean.setSortDirection(requestInstance.getParameter("_sortDirection"));
            memberSearchBean.setPageOffset(requestInstance.getParameter("_pageOffset"));
            memberSearchBean.setPageSize(requestInstance.getParameter("_pageSize"));
            if (StringUtils.isBlank(memberSearchBean.getSearchType())) {
                ErrorCodeEnum.ERR_20690.setErrorCode(processedResult);
                return processedResult;
            }
            processedResult
                    .addParam(new Param("SortVariable", memberSearchBean.getSortVariable(), "string"));
            processedResult
                    .addParam(new Param("SortDirection", memberSearchBean.getSortDirection(), "string"));
            processedResult.addParam(new Param("PageOffset",
                    String.valueOf(memberSearchBean.getPageOffset()), "int"));
            processedResult.addParam(new Param("PageSize",
                    String.valueOf(memberSearchBean.getPageSize()), "int"));
            if (memberSearchBean.getSearchType().equalsIgnoreCase("APPLICANT_SEARCH"))
                processedResult.addParam(new Param("TotalResultsFound", "0", "int"));
            if (memberSearchBean.getSearchType().equalsIgnoreCase("GROUP_SEARCH")) {
                JSONObject searchResults = searchCustomers(authToken, "GROUP_SEARCH_TOTAL_COUNT", memberSearchBean, requestInstance);
                if (searchResults.has("records") && ((JSONArray)searchResults.get("records")).length() > 0)
                    if (searchResults.getJSONArray("records").getJSONObject(0).has("SearchMatchs")) {
                        processedResult.addParam(new Param("TotalResultsFound", searchResults
                                .getJSONArray("records").getJSONObject(0).getString("SearchMatchs"), "int"));
                    } else {
                        processedResult.addParam(new Param("TotalResultsFound", "0", "int"));
                    }
            }
            if (memberSearchBean.getSearchType().equalsIgnoreCase("CUSTOMER_SEARCH")) {
                JSONObject searchResults = searchCustomers(authToken, "CUSTOMER_SEARCH_TOTAL_COUNT", memberSearchBean, requestInstance);
                if (searchResults.has("records") && ((JSONArray)searchResults.get("records")).length() > 0)
                    if (searchResults.getJSONArray("records").getJSONObject(0).has("SearchMatchs")) {
                        processedResult.addParam(new Param("TotalResultsFound", searchResults
                                .getJSONArray("records").getJSONObject(0).getString("SearchMatchs"), "int"));
                    } else {
                        processedResult.addParam(new Param("TotalResultsFound", "0", "int"));
                    }
            }
            JSONObject customers = searchCustomers(authToken, memberSearchBean.getSearchType(), memberSearchBean, requestInstance);
            if (customers.has("customerbasicinfo_view")) {
                JSONObject customerViewJson = customers.getJSONObject("customerbasicinfo_view");
                Record customerbasicinfo_view = CommonUtilities.constructRecordFromJSONObject(customerViewJson);
                customerbasicinfo_view.setId("customerbasicinfo_view");
                processedResult.addRecord(customerbasicinfo_view);
                Record configuration = new Record();
                configuration.setId("Configuration");
                configuration.addParam(new Param("value", customerViewJson.getString("accountLockoutTime")));
                processedResult.addRecord(configuration);
            }
            if (customers.has("records")) {
                JSONArray recordsArray = customers.getJSONArray("records");
                Dataset recordsDataset = CommonUtilities.constructDatasetFromJSONArray(recordsArray);
                recordsDataset.setId("records");
                Param recordsStatus = new Param("Status", "Records returned: " + recordsArray.length(), "string");
                processedResult.addDataset(recordsDataset);
                processedResult.addParam(recordsStatus);
            } else {
                ErrorCodeEnum.ERR_20716.setErrorCode(processedResult);
                return processedResult;
            }
            return processedResult;
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            processedResult.addParam(new Param("FailureReason", e.getMessage()));
            ErrorCodeEnum.ERR_20716.setErrorCode(processedResult);
            return processedResult;
        }
    }
}
