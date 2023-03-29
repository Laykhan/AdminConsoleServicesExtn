package com.kony.adminconsole.service.permissions;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.dto.Action;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.ActionHandlerExtn;
import com.kony.adminconsole.handler.ApplicationParametersHandler;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.service.permissions.ManagePermissionsService;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class ManagePermissionsServiceExtn extends ManagePermissionsService {
    private static final int PERMISSION_NAME_MAX_CHARS = 25;

    private static final int PERMISSION_DESCRIPTION_MAX_CHARS = 300;

    private static final Logger LOG = Logger.getLogger(ManagePermissionsService.class);

    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance, DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        Map<String, String> postParametersMap = new HashMap<>();
        UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
        String userID = userDetailsBeanInstance.getUserId();
        String permissionDetailsJSONString = requestInstance.getParameter("Permission_Details");
        String roleAssignedToJSONString = requestInstance.getParameter("AssignedTo");
        String roleRemovedFromJSONString = requestInstance.getParameter("RemovedFrom");
        String permissionName = null;
        try {
            boolean isValidPermissionData = true;
            JSONObject permissionDetailsJSONObject = CommonUtilities.getStringAsJSONObject(permissionDetailsJSONString);
            JSONObject permissionAssignedToJSONObject = CommonUtilities.getStringAsJSONObject(roleAssignedToJSONString);
            JSONObject permissionRemovedFromJSONObject = CommonUtilities.getStringAsJSONObject(roleRemovedFromJSONString);
            String permissionId = permissionDetailsJSONObject.getString("id");
            StringBuffer errorMessageBuffer = new StringBuffer();
            String permissionDescription = null, updatePermissionResponse = null;
            postParametersMap.clear();
            postParametersMap.put("id", permissionId);
            if (permissionDetailsJSONObject.has("Name")) {
                permissionName = permissionDetailsJSONObject.getString("Name");
                if (StringUtils.isBlank(permissionName)) {
                    errorMessageBuffer.append("Permission Name cannot be an empty string\n");
                    isValidPermissionData = false;
                } else if (CommonUtilities.containSpecialChars(permissionName)) {
                    errorMessageBuffer.append("Permission Name cannot contain special characters");
                    isValidPermissionData = false;
                } else if (permissionName.length() > 25) {
                    errorMessageBuffer.append("Permission Name cannot have more than 25 characters\n");
                    isValidPermissionData = false;
                } else {
                    postParametersMap.put("Name", permissionName);
                }
            }
            if (permissionDetailsJSONObject.has("Description")) {
                permissionDescription = permissionDetailsJSONObject.getString("Description");
                if (StringUtils.isBlank(permissionDescription)) {
                    errorMessageBuffer.append("Permission Description cannot be an empty string\n");
                    isValidPermissionData = false;
                } else if (CommonUtilities.containSpecialChars(permissionDescription)) {
                    errorMessageBuffer.append("Permission Description cannot contain special characters");
                    isValidPermissionData = false;
                } else if (permissionDescription.length() > 300) {
                    errorMessageBuffer.append("Permission Description cannot have more than 300 characters\n");
                    isValidPermissionData = false;
                } else {
                    postParametersMap.put("Description", permissionDescription);
                }
            }
            if (permissionDetailsJSONObject.has("Status_id"))
                postParametersMap.put("Status_id", permissionDetailsJSONObject.getString("Status_id"));
            if (isValidPermissionData) {
                if (StringUtils.isBlank(permissionName))
                    try {
                        Map<String, String> readPostParametersMap = new HashMap<>();
                        readPostParametersMap.put("$filter", "id eq " + permissionId);
                        JSONObject readPermissionResponseJSON = CommonUtilities.getStringAsJSONObject(Executor.invokeService(ServiceURLEnum.PERMISSION_READ, readPostParametersMap, null, requestInstance));
                        permissionName = readPermissionResponseJSON.getJSONArray("permission").getJSONObject(0).getString("Name");
                    } catch (Exception exception) {}
                updatePermissionResponse = Executor.invokeService(ServiceURLEnum.PERMISSION_UPDATE, postParametersMap, null, requestInstance);
                Param updatePermissionRecord = new Param("updatePermissionResponse", updatePermissionResponse, "string");
                processedResult.addParam(updatePermissionRecord);
                JSONObject updatePermissionResponseJSON = CommonUtilities.getStringAsJSONObject(updatePermissionResponse);
                if (updatePermissionResponseJSON != null && updatePermissionResponseJSON.has("opstatus") && updatePermissionResponseJSON
                        .getInt("opstatus") == 0) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERMISSIONS, EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL, "Update Permission Successful. Permission name: " + permissionName);
                } else {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERMISSIONS, EventEnum.UPDATE, ActivityStatusEnum.FAILED, "Update Permission Failed. Permission name: " + permissionName);
                }
            } else {
                ErrorCodeEnum.ERR_20523.setErrorCode(processedResult);
                Param errorDescriptionParam = new Param("errorDescription", errorMessageBuffer.toString(), "string");
                processedResult.addParam(errorDescriptionParam);
                return processedResult;
            }
            Set<String> listOfPermissions = new HashSet<>();
            listOfPermissions.add(permissionId);
            JSONArray permissionRemovedFromRolesArray = null, permissionAssignedToRolesArray = null;
            JSONArray permissionRemovedFromUsersArray = null, permissionAssignedToUsersArray = null;
            if (permissionRemovedFromJSONObject != null) {
                if (permissionRemovedFromJSONObject.has("rolesList"))
                    permissionRemovedFromRolesArray = permissionRemovedFromJSONObject.getJSONArray("rolesList");
                if (permissionRemovedFromJSONObject.has("usersList"))
                    permissionRemovedFromUsersArray = permissionRemovedFromJSONObject.getJSONArray("usersList");
            }
            if (permissionAssignedToJSONObject != null) {
                if (permissionAssignedToJSONObject.has("rolesList"))
                    permissionAssignedToRolesArray = permissionAssignedToJSONObject.getJSONArray("rolesList");
                if (permissionAssignedToJSONObject.has("usersList"))
                    permissionAssignedToUsersArray = permissionAssignedToJSONObject.getJSONArray("usersList");
            }
            HashMap<String, ArrayList<Action>> compositeActionMapping = ActionHandlerExtn.getChildActions(listOfPermissions, requestInstance);
            ActionHandlerExtn.removeActionFromRoles(requestInstance, permissionId,
                    CommonUtilities.getJSONArrayAsList(permissionRemovedFromRolesArray), compositeActionMapping);
            ActionHandlerExtn.assignActionToRoles(requestInstance, userID, permissionId,
                    CommonUtilities.getJSONArrayAsList(permissionAssignedToRolesArray), compositeActionMapping);
            String attributeValue = ApplicationParametersHandler.fetchIsKeyCloakEnabled(requestInstance);
            if (attributeValue.equals("false")) {
                ActionHandlerExtn.removeActionFromUsers(requestInstance, permissionId,
                        CommonUtilities.getJSONArrayAsList(permissionRemovedFromUsersArray));
                ActionHandlerExtn.assignActionToUsers(requestInstance, userID, permissionId,
                        CommonUtilities.getJSONArrayAsList(permissionAssignedToUsersArray), compositeActionMapping);
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERMISSIONS, EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL, "Permission name: " + permissionName);
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", (Throwable)e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            Param javaExceptionParam = new Param("JavaError", e.getMessage(), "string");
            errorResult.addParam(javaExceptionParam);
            LOG.error("Exception in Managing Permission Configuration. Exception:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
        return processedResult;
    }
}
