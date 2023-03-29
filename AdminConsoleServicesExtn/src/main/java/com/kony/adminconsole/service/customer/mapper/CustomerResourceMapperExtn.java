package com.kony.adminconsole.service.customer.mapper;

import com.dbp.core.api.DBPAPIMapper;
import com.dbp.core.api.Resource;
import com.kony.adminconsole.service.customer.resource.api.CustomerResource;
import com.kony.adminconsole.service.customer.resource.api.InfinityUserManagementResource;
import com.kony.adminconsole.service.customer.resource.api.PartyUserManagementResource;
import com.kony.adminconsole.service.customer.resource.impl.CustomerResourceImpl;
import com.kony.adminconsole.service.customer.resource.impl.InfinityUserManagementResourceImplExtn;
import com.kony.adminconsole.service.customer.resource.impl.PartyUserManagementResourceImpl;
import java.util.HashMap;
import java.util.Map;

public class CustomerResourceMapperExtn implements DBPAPIMapper<Resource> {
    public Map<Class<? extends Resource>, Class<? extends Resource>> getAPIMappings() {
        Map<Class<? extends Resource>, Class<? extends Resource>> map = new HashMap<>();
        map.put(CustomerResource.class, CustomerResourceImpl.class);
        map.put(InfinityUserManagementResource.class, InfinityUserManagementResourceImplExtn.class);
        map.put(PartyUserManagementResource.class, PartyUserManagementResourceImpl.class);
        return map;
    }
}