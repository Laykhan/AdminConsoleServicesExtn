package com.temenos.dbx.product.usermanagement.servlet;

import com.dbp.core.api.APIImplementationTypes;
import com.dbp.core.api.DBPAPIMapper;
import com.dbp.core.api.factory.BackendDelegateFactory;
import com.dbp.core.api.factory.BusinessDelegateFactory;
import com.dbp.core.api.factory.ResourceFactory;
import com.dbp.core.api.factory.impl.DBPAPIAbstractFactoryImpl;
import com.konylabs.middleware.servlet.IntegrationCustomServlet;
import com.temenos.dbx.product.usermanagement.mapper.*;
import com.temenos.dbx.product.utils.ThreadExecutor;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

@IntegrationCustomServlet(servletName = "DBXUserCustomerResourcesServlet", urlPatterns = {"DBXUserCustomerResourcesServlet"})
public class ProductUserManagementServletExtn extends HttpServlet {
    private static final long serialVersionUID = -7996896027215639726L;

    public void init() throws ServletException {
        ((ResourceFactory)DBPAPIAbstractFactoryImpl.getInstance().getFactoryInstance(ResourceFactory.class))
                .registerResourceMappings((DBPAPIMapper)new UserManagementResourceMapperExtn(), APIImplementationTypes.EXTENSION);
        ((BusinessDelegateFactory)DBPAPIAbstractFactoryImpl.getInstance().getFactoryInstance(BusinessDelegateFactory.class))
                .registerBusinessDelegateMappings((DBPAPIMapper)new UserManagementBusinessDelegateMapper(), APIImplementationTypes.BASE);
        ((BackendDelegateFactory)DBPAPIAbstractFactoryImpl.getInstance().getFactoryInstance(BackendDelegateFactory.class))
                .registerBackendDelegateMappings((DBPAPIMapper)new UserManagementBackendDelegateMapperExtn(), APIImplementationTypes.EXTENSION);
    }

    public void destroy() {
        try {
            ThreadExecutor.getExecutor().shutdownExecutor();
        } catch (Exception exception) {}
    }
}
