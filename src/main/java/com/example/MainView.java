package com.example;

import com.example.db.ContactRepositoryJdbc;
import com.example.model.Contact;
import com.example.util.PhoneUtil;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

    private final ContactRepositoryJdbc repo = new ContactRepositoryJdbc();

    private final TextField search = new TextField("Search");
    private final Crud<Contact> crud;

    private final CallbackDataProvider<Contact, Void> dataProvider;

    public MainView() {
        setSizeFull();
        add(new H1("Phone Book CRUD App"));

        // Search
        search.setPlaceholder("Search by name, phone, email, city, country...");
        search.setClearButtonVisible(true);
        search.setWidthFull();
        search.setValueChangeMode(ValueChangeMode.LAZY);

        // CRUD
        crud = new Crud<>(Contact.class, createEditor());
        crud.setSizeFull();

        // Grid summary columns
        crud.getGrid().removeAllColumns();
        crud.getGrid().addColumn(Contact::getName).setHeader("Name").setAutoWidth(true).setFlexGrow(1);
        crud.getGrid().addColumn(Contact::getEmail).setHeader("Email").setAutoWidth(true).setFlexGrow(1);
        crud.getGrid().addColumn(Contact::getPhone).setHeader("Phone").setAutoWidth(true);

        // Make editing obvious by opening editor when a row is clicked
        crud.setEditOnClick(false);
        crud.getGrid().addItemClickListener(event ->
                crud.edit(event.getItem(), Crud.EditMode.EXISTING_ITEM)
        );

        // Optional: reduce "empty" look when few rows
        crud.getGrid().setHeight("350px");

        // Data provider (DB-backed)
        dataProvider = new CallbackDataProvider<>(
                query -> repo.search(search.getValue()).stream(),
                query -> repo.search(search.getValue()).size()
        );
        crud.setDataProvider(dataProvider);

        search.addValueChangeListener(e -> dataProvider.refreshAll());

        // Save = create or update
        crud.addSaveListener(e -> {
            Contact item = e.getItem();
            try {
                if (item.getId() == null) {
                    repo.insert(item);
                    Notification.show("Added");
                } else {
                    int expectedVersion = item.getVersion();
                    repo.update(item, expectedVersion);
                    Notification.show("Updated");
                }
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 4500, Notification.Position.MIDDLE);
            } finally {
                dataProvider.refreshAll();
            }
        });

        // Delete
        crud.addDeleteListener(e -> {
            Contact item = e.getItem();
            try {
                if (item.getId() != null) {
                    repo.deleteById(item.getId());
                    Notification.show("Deleted");
                }
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 4500, Notification.Position.MIDDLE);
            } finally {
                dataProvider.refreshAll();
            }
        });

        add(search, crud);
        expand(crud);
    }

    private CrudEditor<Contact> createEditor() {
        TextField name = new TextField("Name");
        TextField street = new TextField("Street");
        TextField city = new TextField("City");
        TextField country = new TextField("Country");
        TextField phone = new TextField("Phone");
        EmailField email = new EmailField("Email");

        name.setWidthFull();
        street.setWidthFull();
        city.setWidthFull();
        country.setWidthFull();
        phone.setWidthFull();
        email.setWidthFull();

        phone.setHelperText("Allowed: digits, optional leading '+', or start with 00 (will be converted to +).");
        phone.setPlaceholder("+12025550198  or  00442079460958");

        FormLayout form = new FormLayout(name, phone, email, street, city, country);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("700px", 2)
        );

        Binder<Contact> binder = new Binder<>(Contact.class);

        // Name: trim + required
        binder.forField(name)
                .withConverter(v -> v == null ? "" : v.trim(), v -> v)
                .asRequired("Name is required")
                .bind(Contact::getName, Contact::setName);

        // Phone: normalize + validate + store normalized
        binder.forField(phone)
                .asRequired("Phone is required")
                .withValidator(v -> PhoneUtil.isValidNormalized(PhoneUtil.normalize(v)),
                        "Phone must be 7–16 digits (optionally starting with '+'). You may also start with 00.")
                .withConverter(
                        v -> PhoneUtil.normalize(v),
                        v -> v
                )
                .bind(Contact::getPhone, Contact::setPhone);

        // Optional fields: trim -> null
        binder.forField(street)
                .withConverter(PhoneUtil::trimToNull, v -> v)
                .bind(Contact::getStreet, Contact::setStreet);

        binder.forField(city)
                .withConverter(PhoneUtil::trimToNull, v -> v)
                .bind(Contact::getCity, Contact::setCity);

        binder.forField(country)
                .withConverter(PhoneUtil::trimToNull, v -> v)
                .bind(Contact::getCountry, Contact::setCountry);

        binder.forField(email)
                .withConverter(PhoneUtil::trimToNull, v -> v)
                .bind(Contact::getEmail, Contact::setEmail);

        return new BinderCrudEditor<>(binder, form);
    }
}
