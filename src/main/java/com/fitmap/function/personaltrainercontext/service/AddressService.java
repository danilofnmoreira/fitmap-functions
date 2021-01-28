package com.fitmap.function.personaltrainercontext.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.fitmap.function.commonfirestore.service.SubCollectionsCrudService;
import com.fitmap.function.personaltrainercontext.domain.Address;
import com.google.cloud.firestore.Firestore;

public class AddressService extends SubCollectionsCrudService<Address> {

    public AddressService(Firestore db) {
        super(db);
    }

    @Override
    protected String getSuperCollection() {
        return "personal-trainers";
    }

    @Override
    protected String getSubCollection() {
        return "addresses";
    }

    @Override
    protected Class<Address> getSubEntityType() {
        return Address.class;
    }

    @Override
    protected BiConsumer<Address, String> updateId() {
        return Address::setId;
    }

    @Override
    protected Function<Address, String> getId() {
        return Address::getId;
    }

    @Override
    protected Function<Address, Map<String, Object>> getFieldsToUpdate() {
        return address -> {

            var props = new HashMap<String, Object>();

            props.put("zipCode", address.getZipCode());
            props.put("publicPlace", address.getPublicPlace());
            props.put("complement", address.getComplement());
            props.put("district", address.getDistrict());
            props.put("city", address.getCity());
            props.put("federalUnit", address.getFederalUnit());

            return props;
        };
    }

}
