package com.web.assigment.web;

import com.ejb.assigment.model.Store;
import com.ejb.assigment.services.StoreEJB;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Named
@ViewScoped
public class StoreBean implements Serializable {
    private transient Context context;

    private Store store = new Store();
    private List<Store> stores = new ArrayList<>();


    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public List<Store> getStores() throws NamingException {

        stores = getEJBServices().getAll();

        return stores;
    }

    @PostConstruct
    private void createContext() throws NamingException {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.wildfly.naming.client.WildFlyInitialContextFactory");

        context = new InitialContext(properties);
    }

    private Object lookupEJB(String ejbName) throws NamingException {
        return context.lookup(ejbName);
    }

    //Fetch InventoryEJB Service
    private StoreEJB getEJBServices() throws NamingException {
        return ((StoreEJB) context.lookup("ejb:/web-app/StoreService!com.ejb.assigment.services.StoreEJB"));
    }


    public void processForm(ActionEvent event) throws NamingException {
        getEJBServices().addStore(store);
        store = new Store();
    }
}
