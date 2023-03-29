package com.temenos.dbx.product.usermanagement.mapper;

import com.dbp.core.api.DBPAPIMapper;
import com.dbp.core.api.Resource;
import com.temenos.dbx.product.resource.api.BankResource;
import com.temenos.dbx.product.resource.impl.BankResourceImpl;
import com.temenos.dbx.product.usermanagement.resource.api.CustomRoleResource;
import com.temenos.dbx.product.usermanagement.resource.api.CustomerAddressResource;
import com.temenos.dbx.product.usermanagement.resource.api.CustomerCommunicationResource;
import com.temenos.dbx.product.usermanagement.resource.api.CustomerIdentityAttributesResource;
import com.temenos.dbx.product.usermanagement.resource.api.CustomerImageResource;
import com.temenos.dbx.product.usermanagement.resource.api.CustomerPreferenceResource;
import com.temenos.dbx.product.usermanagement.resource.api.CustomerSecurityQuestionsResource;
import com.temenos.dbx.product.usermanagement.resource.api.FeedBackStatusResource;
import com.temenos.dbx.product.usermanagement.resource.api.InfinityUserManagementResource;
import com.temenos.dbx.product.usermanagement.resource.api.ProfileManagementResource;
import com.temenos.dbx.product.usermanagement.resource.api.PushExternalEventResource;
import com.temenos.dbx.product.usermanagement.resource.api.UserManagementResource;
import com.temenos.dbx.product.usermanagement.resource.impl.*;
import com.temenos.dbx.product.usermanagement.resource.impl.ProfileManagementResourceImplExtn;

import java.util.HashMap;
import java.util.Map;

public class UserManagementResourceMapperExtn implements DBPAPIMapper<Resource> {
    public Map<Class<? extends Resource>, Class<? extends Resource>> getAPIMappings() {
        Map<Class<? extends Resource>, Class<? extends Resource>> map = new HashMap<>();
        map.put(UserManagementResource.class, UserManagementResourceImpl.class);
        map.put(CustomerImageResource.class, CustomerImageResourceImpl.class);
        map.put(CustomerCommunicationResource.class, CustomerCommunicationResourceImpl.class);
        map.put(CustomerAddressResource.class, CustomerAddressResourceImpl.class);
        map.put(CustomRoleResource.class, CustomRoleResourceImpl.class);
        map.put(CustomerPreferenceResource.class, CustomerPreferenceResourceImpl.class);
        map.put(CustomerSecurityQuestionsResource.class, CustomerSecurityQuestionsResourceImpl.class);
        map.put(FeedBackStatusResource.class, FeedbackStatusResourceImpl.class);
        map.put(ProfileManagementResource.class, ProfileManagementResourceImplExtn.class);
        map.put(UserManagementResource.class, UserManagementResourceImpl.class);
        map.put(BankResource.class, BankResourceImpl.class);
        map.put(CustomerIdentityAttributesResource.class, CustomerIdentityAttributesResourceImpl.class);
        map.put(PushExternalEventResource.class, PushExternalEventResourceImpl.class);
        map.put(InfinityUserManagementResource.class, InfinityUserManagementResourceImpl.class);
        return map;
    }
}
