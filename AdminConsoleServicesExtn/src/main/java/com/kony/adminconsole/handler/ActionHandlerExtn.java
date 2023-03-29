package com.kony.adminconsole.handler;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.dto.Action;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.service.permissions.ManagePermissionsService;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class ActionHandlerExtn{

    private static final Logger LOG = Logger.getLogger(ActionHandler.class);
    public static HashMap<String, ArrayList<Action>> getChildActions(Set<String> permissionsList, DataControllerRequest requestInstance) throws ApplicationException {
        Map<String, String> inputMap = new HashMap<>();
        StringBuilder filterQueryBuffer = new StringBuilder();
        HashMap<String, ArrayList<Action>> actionsInfo = new HashMap<>();
        inputMap.put("$select", "id,Name,Permission_id,isEnabled");
        for (String currPermission : permissionsList)
            filterQueryBuffer.append("Permission_id eq '" + currPermission + "' or ");
        if (filterQueryBuffer.toString().trim().endsWith("or")) {
            filterQueryBuffer.delete(filterQueryBuffer.lastIndexOf("or"), filterQueryBuffer.length());
            filterQueryBuffer.trimToSize();
        }
        inputMap.put("$filter", filterQueryBuffer.toString().trim());
        String operationResponse = Executor.invokeService(ServiceURLEnum.COMPOSITEACTION_READ, inputMap, null, requestInstance);
        JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
        if (operationResponseJSON != null && operationResponseJSON.has("opstatus") && operationResponseJSON
                .getInt("opstatus") == 0 && operationResponseJSON
                .has("compositeaction")) {
            JSONArray actionsRecords = operationResponseJSON.getJSONArray("compositeaction");
            for (int indexVar = 0; indexVar < actionsRecords.length(); indexVar++) {
                if (actionsRecords.get(indexVar) instanceof JSONObject) {
                    JSONObject currActionJSONObject = actionsRecords.getJSONObject(indexVar);
                    String parentPermissionId = currActionJSONObject.optString("Permission_id");
                    if (!actionsInfo.containsKey(parentPermissionId)) {
                        ArrayList<Action> arrayList = new ArrayList<>();
                        actionsInfo.put(parentPermissionId, arrayList);
                    }
                    ArrayList<Action> childActionList = actionsInfo.get(parentPermissionId);
                    Action currChildActionObject = new Action();
                    String childActionName = currActionJSONObject.optString("Name");
                    String childActionId = currActionJSONObject.optString("id");
                    if (StringUtils.equalsIgnoreCase(currActionJSONObject.optString("isEnabled"), "1") ||
                            StringUtils.equalsIgnoreCase(currActionJSONObject.optString("isEnabled"),
                                    String.valueOf(true))) {
                        currChildActionObject.setIsEnabled(true);
                    } else {
                        currChildActionObject.setIsEnabled(false);
                    }
                    currChildActionObject.setParentPermissionId(parentPermissionId);
                    currChildActionObject.setId(childActionId);
                    currChildActionObject.setName(childActionName);
                    childActionList.add(currChildActionObject);
                }
            }
            return actionsInfo;
        }
        throw new ApplicationException(ErrorCodeEnum.ERR_20744);
    }

    public static void removeActionFromRoles(DataControllerRequest requestInstance, String permissionId, List<String> rolesList, Map<String, ArrayList<Action>> compositeActionMapping) throws ApplicationException {
        Map<String, String> inputMap = new HashMap<>();
        if (rolesList != null && !rolesList.isEmpty()) {
            ArrayList<Action> currentChildActions = new ArrayList<>();
            if (compositeActionMapping.containsKey(permissionId))
                currentChildActions = compositeActionMapping.get(permissionId);
            for (String currRoleId : rolesList) {
                if (StringUtils.isBlank(currRoleId))
                    continue;
                inputMap.put("Role_id", currRoleId);
                inputMap.remove("Permission_id");
                for (Action currAction : currentChildActions) {
                    inputMap.put("CompositeAction_id", currAction.getId());
                    Executor.invokeService(ServiceURLEnum.ROLECOMPOSITEPERMISSION_DELETE, inputMap, null, requestInstance);
                }
                Map<String, String> postParametersMap = new HashMap<>();
                postParametersMap.put("_roleId", currRoleId);
                postParametersMap.put("_PermissionIds", permissionId);
                String getDeleteResponse = Executor.invokeService(ServiceURLEnum.ROLEPERMISSIONDELETE_PROC, postParametersMap, null, requestInstance);
                JSONObject getDeleteResponseJson = CommonUtilities.getStringAsJSONObject(getDeleteResponse);

                LOG.error("20527 v1 currRoleId " + currRoleId);
                LOG.error("20527 v1 permissionId " + permissionId);
                LOG.error("20527 v1 getDeleteResponse " + getDeleteResponse);
                LOG.error("20527 v1 postParametersMap " + postParametersMap.toString());


                if (getDeleteResponseJson == null || !getDeleteResponseJson.has("opstatus") || getDeleteResponseJson
                        .getInt("opstatus") != 0)
                    throw new ApplicationException(ErrorCodeEnum.ERR_20527);
            }
        }
    }

    public static void assignActionToRoles(DataControllerRequest requestInstance, String loggedInUserId, String permissionId, List<String> rolesList, HashMap<String, ArrayList<Action>> compositeActionMapping) throws ApplicationException {
        Map<String, String> inputMap = new HashMap<>();
        Map<String, String> roleActionMap = new HashMap<>();
        if (rolesList != null && !rolesList.isEmpty()) {
            ArrayList<Action> currentChildActions = new ArrayList<>();
            inputMap.put("Permission_id", permissionId);
            inputMap.put("createdby", loggedInUserId);
            inputMap.put("modifiedby", loggedInUserId);
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
            if (compositeActionMapping.containsKey(permissionId))
                currentChildActions = compositeActionMapping.get(permissionId);
            for (String currRoleId : rolesList) {
                if (StringUtils.isBlank(currRoleId))
                    continue;
                inputMap.put("Role_id", currRoleId);
                if (!currentChildActions.isEmpty()) {
                    inputMap.remove("Permission_id");
                    for (Action currAction : currentChildActions) {
                        String filter = "CompositeAction_id eq '" + currAction.getId() + "' and Role_id eq '" + currRoleId + "'";
                        roleActionMap.put("$filter", filter);
                        String readEndpoint = Executor.invokeService(ServiceURLEnum.ROLECOMPOSITEACTION_READ, roleActionMap, null, requestInstance);
                        JSONObject readEndpointResponse = CommonUtilities.getStringAsJSONObject(readEndpoint);
                        JSONObject currJson = null;
                        if (readEndpointResponse != null && readEndpointResponse.has("opstatus") && readEndpointResponse
                                .getInt("opstatus") == 0) {
                            JSONArray roleActionJSONArray = readEndpointResponse.getJSONArray("rolecompositeaction");
                            if (roleActionJSONArray.length() > 0)
                                currJson = roleActionJSONArray.optJSONObject(0);
                        }
                        inputMap.put("CompositeAction_id", currAction.getId());
                        if (currAction.isEnabled()) {
                            inputMap.put("isEnabled", "1");
                        } else {
                            inputMap.put("isEnabled", "0");
                        }
                        if (currJson == null) {
                            String str = Executor.invokeService(ServiceURLEnum.ROLECOMPOSITEACTION_CREATE, inputMap, null, requestInstance);
                            JSONObject jSONObject = CommonUtilities.getStringAsJSONObject(str);
                            if (jSONObject == null || !jSONObject.has("opstatus") || jSONObject
                                    .getInt("opstatus") != 0)
                                throw new ApplicationException(ErrorCodeEnum.ERR_20525);
                        }
                    }
                    inputMap.remove("isEnabled");
                    inputMap.remove("CompositeAction_id");
                    inputMap.put("Permission_id", permissionId);
                }
                String operationResponse = Executor.invokeService(ServiceURLEnum.ROLEPERMISSION_CREATE, inputMap, null, requestInstance);
                JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

                if (operationResponseJSON == null || !operationResponseJSON.has("opstatus") || operationResponseJSON
                        .getInt("opstatus") != 0)
                    throw new ApplicationException(ErrorCodeEnum.ERR_20528);
            }
        }
    }

    public static void removeActionFromUsers(DataControllerRequest requestInstance, String permissionId, List<String> usersList) throws ApplicationException {
        if (usersList != null && !usersList.isEmpty())
            for (String currUserID : usersList) {
                if (StringUtils.isBlank(currUserID))
                    continue;
                Map<String, String> postParametersMap = new HashMap<>();
                postParametersMap.put("_userId", currUserID);
                postParametersMap.put("_PermissionIds", permissionId);
                String getDeleteResponse = Executor.invokeService(ServiceURLEnum.USERPERMISSIONDELETE_PROC, postParametersMap, null, requestInstance);
                JSONObject getDeleteResponseJson = CommonUtilities.getStringAsJSONObject(getDeleteResponse);
                if (getDeleteResponseJson == null || !getDeleteResponseJson.has("opstatus") || getDeleteResponseJson
                        .getInt("opstatus") != 0)
                    throw new ApplicationException(ErrorCodeEnum.ERR_20528);
            }
    }

    public static void assignActionToUsers(DataControllerRequest requestInstance, String loggedInUserId, String permissionID, List<String> usersList, HashMap<String, ArrayList<Action>> compositeActionMapping) throws ApplicationException {
        if (usersList != null && !usersList.isEmpty()) {
            Map<String, String> inputMap = new HashMap<>();
            Map<String, String> userActionMap = new HashMap<>();
            ArrayList<Action> currentChildActions = new ArrayList<>();
            inputMap.put("createdby", loggedInUserId);
            inputMap.put("modifiedby", loggedInUserId);
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
            if (compositeActionMapping.containsKey(permissionID))
                currentChildActions = compositeActionMapping.get(permissionID);
            for (String currUserId : usersList) {
                if (StringUtils.isBlank(currUserId))
                    continue;
                inputMap.put("User_id", currUserId);
                inputMap.remove("Permission_id");
                for (Action currAction : currentChildActions) {
                    String filter = "CompositeAction_id eq '" + currAction.getId() + "' and User_id eq '" + currUserId + "'";
                    userActionMap.put("$filter", filter);
                    String readEndpoint = Executor.invokeService(ServiceURLEnum.USERCOMPOSITEACTION_READ, userActionMap, null, requestInstance);
                    JSONObject readEndpointResponse = CommonUtilities.getStringAsJSONObject(readEndpoint);
                    JSONObject currJson = null;
                    if (readEndpointResponse != null && readEndpointResponse.has("opstatus") && readEndpointResponse
                            .getInt("opstatus") == 0) {
                        JSONArray userActionJSONArray = readEndpointResponse.getJSONArray("usercompositeaction");
                        if (userActionJSONArray.length() > 0)
                            currJson = userActionJSONArray.optJSONObject(0);
                    }
                    inputMap.put("CompositeAction_id", currAction.getId());
                    if (currAction.isEnabled()) {
                        inputMap.put("isEnabled", "1");
                    } else {
                        inputMap.put("isEnabled", "0");
                    }
                    if (currJson == null) {
                        String str = Executor.invokeService(ServiceURLEnum.USERCOMPOSITEACTION_CREATE, inputMap, null, requestInstance);
                        JSONObject jSONObject = CommonUtilities.getStringAsJSONObject(str);
                        LOG.error("20527 v2 str " + str);
                        LOG.error("20527 v2 inputMap " + inputMap.toString());
                        if (jSONObject == null || !jSONObject.has("opstatus") || jSONObject
                                .getInt("opstatus") != 0)
                            throw new ApplicationException(ErrorCodeEnum.ERR_20527);
                    }
                }
                inputMap.remove("isEnabled");
                inputMap.remove("CompositeAction_id");
                inputMap.put("Permission_id", permissionID);
                String operationResponse = Executor.invokeService(ServiceURLEnum.USERPERMISSION_CREATE, inputMap, null, requestInstance);
                JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

                LOG.error("20527 v3 operationResponse " + operationResponse);
                LOG.error("20527 v3 permissionID " + permissionID);
                LOG.error("20527 v3 inputMap " + inputMap.toString());

                if (operationResponseJSON == null || !operationResponseJSON.has("opstatus") || operationResponseJSON
                        .getInt("opstatus") != 0)
                    throw new ApplicationException(ErrorCodeEnum.ERR_20527);
            }
        }
    }

    public static JSONObject getAggregateCompositeActions(String userID, String roleID, String permissionID, DataControllerRequest requestInstance, String selectQuery, String isEnabled) {
        JSONObject readCompositePermission = getCompositeActions(selectQuery, permissionID, isEnabled, requestInstance);
        if (!readCompositePermission.has("opstatus") || readCompositePermission
                .getInt("opstatus") != 0 ||
                !readCompositePermission.has("composite_actions_view")) {
            JSONObject res = new JSONObject();
            res.put("ErrorEnum", ErrorCodeEnum.ERR_20744);
            res.put("response", String.valueOf(readCompositePermission));
            return res;
        }
        JSONArray finalCompositeActions = readCompositePermission.getJSONArray("composite_actions_view");
        if (StringUtils.isNotBlank(roleID)) {
            JSONObject readRoleCompositeAction = getRoleCompositeActions(roleID, isEnabled, requestInstance);
            if (readRoleCompositeAction == null || !readRoleCompositeAction.has("opstatus") || readRoleCompositeAction
                    .getInt("opstatus") != 0) {
                JSONObject res = new JSONObject();
                res.put("ErrorEnum", ErrorCodeEnum.ERR_20745);
                res.put("response", String.valueOf(readRoleCompositeAction));
                return res;
            }
            JSONArray roleCompositeActions = readRoleCompositeAction.getJSONArray("records");
            Set<String> roleCompActionSet = new HashSet<>();
            for (Object actionObject : roleCompositeActions) {
                JSONObject action = (JSONObject)actionObject;
                for (Object finalActionObject : finalCompositeActions) {
                    JSONObject finalAction = (JSONObject)finalActionObject;
                    if (finalAction.getString("id").equals(action.getString("CompositeAction_id"))) {
                        finalAction.put("isEnabled", action.getString("isEnabled"));
                        roleCompActionSet.add(finalAction.getString("id"));
                    }
                }
            }
            for (Object finalActionObject : finalCompositeActions) {
                JSONObject finalAction = (JSONObject)finalActionObject;
                if (!roleCompActionSet.contains(finalAction.getString("id")))
                    finalAction.put("isEnabled", "false");
            }
        }
        String attributeValue = ApplicationParametersHandler.fetchIsKeyCloakEnabled(requestInstance);
        if (attributeValue.equals("false") &&
                StringUtils.isNotBlank(userID)) {
            JSONObject readUserDisabledCompositeAction = getUserCompositeActions(userID, "0", requestInstance);
            if (!readUserDisabledCompositeAction.has("opstatus") || readUserDisabledCompositeAction
                    .getInt("opstatus") != 0 ||
                    !readUserDisabledCompositeAction.has("usercompositeaction")) {
                JSONObject res = new JSONObject();
                res.put("ErrorEnum", ErrorCodeEnum.ERR_20746);
                res.put("response", String.valueOf(readUserDisabledCompositeAction));
                return res;
            }
            JSONArray userDisabledCompositeActions = readUserDisabledCompositeAction.getJSONArray("usercompositeaction");
            for (Object actionObject : userDisabledCompositeActions) {
                JSONObject disabledAction = (JSONObject)actionObject;
                for (Object finalActionObject : finalCompositeActions) {
                    JSONObject finalAction = (JSONObject)finalActionObject;
                    if (finalAction.getString("id").equals(disabledAction.getString("CompositeAction_id")))
                        finalAction.put("isEnabled", disabledAction.getString("isEnabled"));
                }
            }
        }
        JSONObject aggregateCompositeActions = new JSONObject();
        aggregateCompositeActions.put("CompositeActions", finalCompositeActions);
        return aggregateCompositeActions;
    }

    public static JSONObject getCompositeActions(String selectQuery, String permissionID, String isEnabled, DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("$select", selectQuery);
        String filter = "Permission_id eq '" + permissionID + "'";
        if (isEnabled != null)
            filter = filter + " and isEnabled eq '" + isEnabled + "'";
        postParametersMap.put("$filter", filter);
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.COMPOSITEACTION_VIEW_READ, postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

    public static JSONObject getRoleCompositeActions(String roleID, String isEnabled, DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("_roleIds", roleID);
        if (isEnabled == null)
            isEnabled = "NULL";
        postParametersMap.put("_isEnabled", isEnabled);
        String getRolesResponse = Executor.invokeService(ServiceURLEnum.ROLESCOMPOSITEACTIONS_PROC, postParametersMap, null, requestInstance);
        JSONObject getRolesResponseJson = CommonUtilities.getStringAsJSONObject(getRolesResponse);
        return getRolesResponseJson;
    }

    public static JSONObject getUserCompositeActions(String userID, String isEnabled, DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<>();
        String filter = "User_id eq '" + userID + "'";
        if (isEnabled != null)
            filter = filter + " and isEnabled eq '" + isEnabled + "'";
        postParametersMap.put("$filter", filter);
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.USERCOMPOSITEACTION_READ, postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

    public static String getAdminRoleId(String role, DataControllerRequest requestInstance) {
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("$filter", "Name eq '" + role + "'");
        inputMap.put("$select", "id");
        String readRoleResponse = Executor.invokeService(ServiceURLEnum.ROLE_READ, inputMap, null, requestInstance);
        String roleId = null;
        JSONObject readRoleResponseJSON = CommonUtilities.getStringAsJSONObject(readRoleResponse);
        if (readRoleResponseJSON != null && readRoleResponseJSON.has("opstatus") && readRoleResponseJSON
                .getInt("opstatus") == 0 && readRoleResponseJSON
                .has("role")) {
            JSONArray readRoleJSONArray = readRoleResponseJSON.optJSONArray("role");
            if (readRoleJSONArray != null && readRoleJSONArray.length() >= 1) {
                JSONObject roleObj = readRoleJSONArray.getJSONObject(0);
                roleId = roleObj.getString("id");
            }
        }
        return roleId;
    }
}
