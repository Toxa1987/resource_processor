package com.epam.esm.model;

public enum StorageType {
    STAGING ("STAGING"),
    PERMANENT("PERMANENT");

    private String typeValue;
    StorageType(String typeValue) {
        this.typeValue = typeValue;
    }

    public String getTypeValue(){
        return typeValue;
    }
}
