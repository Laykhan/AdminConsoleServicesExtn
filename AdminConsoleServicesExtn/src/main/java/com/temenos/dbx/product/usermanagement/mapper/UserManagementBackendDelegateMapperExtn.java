package com.temenos.dbx.product.usermanagement.mapper;

import com.dbp.core.api.BackendDelegate;
import com.dbp.core.api.DBPAPIMapper;
import com.temenos.dbx.product.usermanagement.backenddelegate.api.AddressBackendDelegate;
import com.temenos.dbx.product.usermanagement.backenddelegate.api.BackendIdentifiersBackendDelegate;
import com.temenos.dbx.product.usermanagement.backenddelegate.api.CommunicationBackendDelegate;
import com.temenos.dbx.product.usermanagement.backenddelegate.api.InfinityUserManagementBackendDelegate;
import com.temenos.dbx.product.usermanagement.backenddelegate.api.ProfileManagementBackendDelegate;
import com.temenos.dbx.product.usermanagement.backenddelegate.api.PushExternalEventBackendDelegate;
import com.temenos.dbx.product.usermanagement.backenddelegate.api.UserManagementBackendDelegate;
import com.temenos.dbx.product.usermanagement.backenddelegate.impl.*;
import com.temenos.dbx.product.usermanagement.backenddelegate.impl.ProfileManagementBackendDelegateImplExtn;

import java.util.HashMap;
import java.util.Map;

public class UserManagementBackendDelegateMapperExtn implements DBPAPIMapper<BackendDelegate> {
    public Map<Class<? extends BackendDelegate>, Class<? extends BackendDelegate>> getAPIMappings() {
        Map<Class<? extends BackendDelegate>, Class<? extends BackendDelegate>> map = new HashMap<>();
        map.put(UserManagementBackendDelegate.class, UserManagementBackendDelegateImpl.class);
        map.put(AddressBackendDelegate.class, AddressBackendDelegateImpl.class);
        map.put(BackendIdentifiersBackendDelegate.class, BackendIdentifiersBackendDelegateimpl.class);
        map.put(CommunicationBackendDelegate.class, CommunicationBackendDelegateImpl.class);
        map.put(ProfileManagementBackendDelegate.class, ProfileManagementBackendDelegateImplExtn.class);
        map.put(PushExternalEventBackendDelegate.class, PushExternalEventBackendDelegateImpl.class);
        map.put(InfinityUserManagementBackendDelegate.class, InfinityUserManagementBackendDelegateImpl.class);
        return map;
    }
}
