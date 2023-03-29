package com.kony.adminconsole.service.customer.servlet;

import com.dbp.core.api.APIImplementationTypes;
import com.dbp.core.api.DBPAPIMapper;
import com.dbp.core.api.factory.BackendDelegateFactory;
import com.dbp.core.api.factory.BusinessDelegateFactory;
import com.dbp.core.api.factory.ResourceFactory;
import com.dbp.core.api.factory.impl.DBPAPIAbstractFactoryImpl;
import com.kony.adminconsole.service.customer.mapper.CustomerBackendDelegateMapper;
import com.kony.adminconsole.service.customer.mapper.CustomerBusinessDelegateMapper;
import com.kony.adminconsole.service.customer.mapper.CustomerResourceMapper;
import com.kony.adminconsole.service.customer.mapper.CustomerResourceMapperExtn;
import com.konylabs.middleware.servlet.IntegrationCustomServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

@IntegrationCustomServlet(servletName = "CustomerServletCustomResourceServlet", urlPatterns = {"CustomerServletCustomResourceServlet"})
public class CustomerServletExtn extends HttpServlet {
    public static final long serialVersionUID = 1L;

    public void init() throws ServletException {
        ((ResourceFactory)DBPAPIAbstractFactoryImpl.getInstance().getFactoryInstance(ResourceFactory.class))
                .registerResourceMappings((DBPAPIMapper)new CustomerResourceMapperExtn(), APIImplementationTypes.EXTENSION);
        ((BusinessDelegateFactory)DBPAPIAbstractFactoryImpl.getInstance().getFactoryInstance(BusinessDelegateFactory.class))
                .registerBusinessDelegateMappings((DBPAPIMapper)new CustomerBusinessDelegateMapper(), APIImplementationTypes.BASE);
        ((BackendDelegateFactory)DBPAPIAbstractFactoryImpl.getInstance().getFactoryInstance(BackendDelegateFactory.class))
                .registerBackendDelegateMappings((DBPAPIMapper)new CustomerBackendDelegateMapper(), APIImplementationTypes.BASE);
    }

    public void destroy() {}
}