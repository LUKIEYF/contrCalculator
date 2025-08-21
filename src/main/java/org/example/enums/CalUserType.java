package org.example.enums;

public enum CalUserType {

    EMPLOYEE("employee"),
    EMPLOYER("employer")
    ;

    private String type;

    CalUserType(String type){
        this.type = type;
    }
}
