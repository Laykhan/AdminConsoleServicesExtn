//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.kony.adminconsole.dto;

import org.apache.commons.lang3.StringUtils;

public class MemberSearchBeanExtn extends MemberSearchBean {
    private String searchType;
    private String memberId = "";
    private String customerName = "";
    private String ssn = "";
    private String customerUsername = "";
    private String customerPhone = "";
    private String customerEmail = "";
    private String isStaffMember = "";
    private String cardorAccountnumber = "";
    private String tin = "";
    private String customerGroup = "";
    private String customerIDType = "";
    private String customerIDValue = "";
    private String customerCompanyId = "";
    private String customerRequest = "";
    private String branchIDS = "";
    private String productIDS = "";
    private String cityIDS = "";
    private String entitlementIDS = "";
    private String groupIDS = "";
    private String customerStatus = "";
    private String beforeDate = "";
    private String afterDate = "";
    private String sortVariable;
    private String sortDirection;
    private long pageOffset;
    private long pageSize;
    private String dateOfBirth = "";
    private String searchStatus = "Success";
    private String customerId = "";

    public MemberSearchBeanExtn() {
    }

    public String getCardorAccountnumber() {
        return this.cardorAccountnumber;
    }

    public void setCardorAccountnumber(String cardorAccountnumber) {
        if (StringUtils.isBlank(cardorAccountnumber)) {
            cardorAccountnumber = "";
        }

        this.cardorAccountnumber = cardorAccountnumber;
    }

    public String getTin() {
        return this.tin;
    }

    public void setTin(String tin) {
        if (StringUtils.isBlank(tin)) {
            tin = "";
        }

        this.tin = tin;
    }

    public String getIsStaffMember() {
        return this.isStaffMember;
    }

    public void setIsStaffMember(String isStaffMember) {
        if (StringUtils.isBlank(isStaffMember)) {
            isStaffMember = "";
        }

        this.isStaffMember = isStaffMember;
    }

    public String getSearchStatus() {
        return this.searchStatus;
    }

    public void setSearchStatus(String searchStatus) {
        if (StringUtils.isBlank(searchStatus)) {
            searchStatus = "";
        }

        this.searchStatus = searchStatus;
    }

    public long getPageOffset() {
        return this.pageOffset;
    }

    public long getPageSize() {
        return this.pageSize;
    }

    public String getSearchType() {
        return this.searchType;
    }

    public String getMemberId() {
        return this.memberId;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public String getSsn() {
        return this.ssn;
    }

    public String getCustomerUsername() {
        return this.customerUsername;
    }

    public String getCustomerPhone() {
        return this.customerPhone;
    }

    public String getCustomerEmail() {
        return this.customerEmail;
    }

    public String getCustomerGroup() {
        return this.customerGroup;
    }

    public String getCustomerRequest() {
        return this.customerRequest;
    }

    public String getBranchIDS() {
        return this.branchIDS;
    }

    public String getProductIDS() {
        return this.productIDS;
    }

    public String getCityIDS() {
        return this.cityIDS;
    }

    public String getEntitlementIDS() {
        return this.entitlementIDS;
    }

    public String getGroupIDS() {
        return this.groupIDS;
    }

    public String getCustomerStatus() {
        return this.customerStatus;
    }

    public String getBeforeDate() {
        return this.beforeDate;
    }

    public String getAfterDate() {
        return this.afterDate;
    }

    public String getSortVariable() {
        return this.sortVariable;
    }

    public String getSortDirection() {
        return this.sortDirection;
    }

    public void setSearchType(String searchType) {
        if (StringUtils.isBlank(searchType)) {
            searchType = "";
        }

        this.searchType = searchType;
    }

    public void setMemberId(String memberId) {
        if (StringUtils.isBlank(memberId)) {
            memberId = "";
        }

        this.memberId = memberId;
    }

    public void setCustomerName(String customerName) {
        if (StringUtils.isBlank(customerName)) {
            customerName = "";
        }

        this.customerName = customerName;
    }

    public void setSsn(String ssn) {
        if (StringUtils.isBlank(ssn)) {
            ssn = "";
        }

        this.ssn = ssn;
    }

    public void setCustomerUsername(String customerUsername) {
        if (StringUtils.isBlank(customerUsername)) {
            customerUsername = "";
        }

        this.customerUsername = customerUsername;
    }

    public void setCustomerPhone(String customerPhone) {
        if (StringUtils.isBlank(customerPhone)) {
            customerPhone = "";
        }

        this.customerPhone = customerPhone;
    }

    public void setCustomerEmail(String customerEmail) {
        if (StringUtils.isBlank(customerEmail)) {
            customerEmail = "";
        }

        this.customerEmail = customerEmail;
    }

    public void setCustomerGroup(String customerGroup) {
        if (StringUtils.isBlank(customerGroup)) {
            customerGroup = "";
        }

        this.customerGroup = customerGroup;
    }

    public void setCustomerRequest(String customerRequest) {
        if (StringUtils.isBlank(customerRequest)) {
            customerRequest = "";
        }

        this.customerRequest = customerRequest;
    }

    public void setBranchIDS(String branchIDS) {
        if (StringUtils.isBlank(branchIDS)) {
            branchIDS = "";
        }

        this.branchIDS = branchIDS;
    }

    public void setProductIDS(String productIDS) {
        if (StringUtils.isBlank(productIDS)) {
            productIDS = "";
        }

        this.productIDS = productIDS;
    }

    public void setCityIDS(String cityIDS) {
        if (StringUtils.isBlank(cityIDS)) {
            cityIDS = "";
        }

        this.cityIDS = cityIDS;
    }

    public void setEntitlementIDS(String entitlementIDS) {
        if (StringUtils.isBlank(entitlementIDS)) {
            entitlementIDS = "";
        }

        this.entitlementIDS = entitlementIDS;
    }

    public void setGroupIDS(String groupIDS) {
        if (StringUtils.isBlank(groupIDS)) {
            groupIDS = "";
        }

        this.groupIDS = groupIDS;
    }

    public void setCustomerStatus(String customerStatus) {
        if (StringUtils.isBlank(customerStatus)) {
            customerStatus = "";
        }

        this.customerStatus = customerStatus;
    }

    public void setBeforeDate(String beforeDate) {
        if (StringUtils.isBlank(beforeDate)) {
            beforeDate = "";
        }

        this.beforeDate = beforeDate;
    }

    public void setAfterDate(String afterDate) {
        if (StringUtils.isBlank(afterDate)) {
            afterDate = "";
        }

        this.afterDate = afterDate;
    }

    public void setSortVariable(String sortVariable) {
        if (StringUtils.isBlank(sortVariable)) {
            sortVariable = "DEFAULT";
        }

        this.sortVariable = sortVariable;
    }

    public void setSortDirection(String sortDirection) {
        if (StringUtils.isBlank(sortDirection)) {
            sortDirection = "ASC";
        }

        this.sortDirection = sortDirection;
    }

    public void setPageOffset(String pageOffset) {
        if (StringUtils.isBlank(pageOffset)) {
            pageOffset = "0";
        }

        this.pageOffset = Long.parseLong(pageOffset);
    }

    public void setPageSize(String pageSize) {
        if (StringUtils.isBlank(pageSize)) {
            pageSize = "20";
        }

        this.pageSize = Long.parseLong(pageSize);
    }

    public String getCustomerIDType() {
        return this.customerIDType;
    }

    public void setCustomerIDType(String customerIDType) {
        if (StringUtils.isBlank(customerIDType)) {
            customerIDType = "";
        }

        this.customerIDType = customerIDType;
    }

    public String getCustomerIDValue() {
        return this.customerIDValue;
    }

    public void setCustomerIDValue(String customerIDValue) {
        if (StringUtils.isBlank(customerIDValue)) {
            customerIDValue = "";
        }

        this.customerIDValue = customerIDValue;
    }

    public String getCustomerCompanyId() {
        return this.customerCompanyId;
    }

    public void setCustomerCompanyId(String customerCompanyId) {
        if (StringUtils.isBlank(customerCompanyId)) {
            customerCompanyId = "";
        }

        this.customerCompanyId = customerCompanyId;
    }

    public String getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        if (StringUtils.isBlank(dateOfBirth)) {
            dateOfBirth = "";
        }

        this.dateOfBirth = dateOfBirth;
    }

    public String getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "MemberSearchBeanExtn{" +
                "searchType='" + searchType + '\'' +
                ", memberId='" + memberId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", ssn='" + ssn + '\'' +
                ", customerUsername='" + customerUsername + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", isStaffMember='" + isStaffMember + '\'' +
                ", cardorAccountnumber='" + cardorAccountnumber + '\'' +
                ", tin='" + tin + '\'' +
                ", customerGroup='" + customerGroup + '\'' +
                ", customerIDType='" + customerIDType + '\'' +
                ", customerIDValue='" + customerIDValue + '\'' +
                ", customerCompanyId='" + customerCompanyId + '\'' +
                ", customerRequest='" + customerRequest + '\'' +
                ", branchIDS='" + branchIDS + '\'' +
                ", productIDS='" + productIDS + '\'' +
                ", cityIDS='" + cityIDS + '\'' +
                ", entitlementIDS='" + entitlementIDS + '\'' +
                ", groupIDS='" + groupIDS + '\'' +
                ", customerStatus='" + customerStatus + '\'' +
                ", beforeDate='" + beforeDate + '\'' +
                ", afterDate='" + afterDate + '\'' +
                ", sortVariable='" + sortVariable + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", pageOffset=" + pageOffset +
                ", pageSize=" + pageSize +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", searchStatus='" + searchStatus + '\'' +
                ", customerId='" + customerId + '\'' +
                '}';
    }
}
