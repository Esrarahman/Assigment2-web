package com.web.assigment.web;


import com.ejb.assigment.model.Inventory;
import com.ejb.assigment.model.Store;
import com.ejb.assigment.services.InventoryEJB;
import com.ejb.assigment.services.StoreEJB;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class StoreDetailsBean implements Serializable {

    private int storeId;
    private Store store = new Store();
    private Inventory inventory = new Inventory();
    private transient Context context;
    private boolean isAscSort = true;

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) throws NamingException, IOException {
        if (storeId == 0) {
            FacesContext.getCurrentInstance().getExternalContext().redirect("stores.xhtml");
        }
        this.storeId = storeId;
        store = getStoreEJBServices().findStoreById(storeId);
    }

    public Store getStore() throws NamingException {
        if(storeId > 0) // Validate that exit param in the url called store_id to get data
            fetchDataAndSorted();

        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
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

    //Fetch getStoreEJBServices Service
    private StoreEJB getStoreEJBServices() throws NamingException {
        return ((StoreEJB) context.lookup("ejb:/web-app/StoreService!com.ejb.assigment.services.StoreEJB"));
    }

    //Fetch getInventoryEJBServices Service
    private InventoryEJB getInventoryEJBServices() throws NamingException {
        return ((InventoryEJB) context.lookup("ejb:/web-app/InventoryService!com.ejb.assigment.services.InventoryEJB"));
    }

    //Method will be trigger to create a inventory from page
    //Once user press save button
    public void processForm(AjaxBehaviorEvent event) throws NamingException {
        inventory.setCreated(LocalDateTime.now());
        inventory.setUpdated(LocalDateTime.now());
        inventory.setStore(store);
        getInventoryEJBServices().addInventory(inventory);
        fetchDataAndSorted();
        inventory = new Inventory();
    }

    //Method to execute sort data
    public void sort(AjaxBehaviorEvent event) {
        isAscSort = !isAscSort;
    }

    //Method will be trigger to delete item click from page
    public void deleteItem(AjaxBehaviorEvent event) throws NamingException {
        Long itemToDelete = (Long) event.getComponent().getAttributes().get("itemToDelete");
        boolean isDeleted = getInventoryEJBServices().deleteInventory(itemToDelete);
        if (isDeleted) {
            fetchDataAndSorted();
        } else {
            System.out.println("Something was wrong!!!");
        }
    }

    //Fetch data from EJB Service and Sort data
    private void fetchDataAndSorted() throws NamingException {
        //Fetch Data From Service
        store = getStoreEJBServices().findStoreById(storeId);

        //Sort data depend on ascending or descending
        if (isAscSort) {
            store.setInventories(store.getInventories().stream().sorted(Comparator.comparing(Inventory::getUpdated)).collect(Collectors.toList()));;
        } else {
            store.setInventories(store.getInventories().stream().sorted(Comparator.comparing(Inventory::getUpdated).reversed()).collect(Collectors.toList()));
            ;
        }
    }

}
