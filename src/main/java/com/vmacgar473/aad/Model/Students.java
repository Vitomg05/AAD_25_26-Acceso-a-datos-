package com.vmacgar473.aad.Model;

import lombok.ToString;

@ToString

public class Students extends Person {

    public Students(String dni, String name) {
        super(dni, name, prename);
        this.course = course;
    }

    public Students(String course) {
        this.course = course;
    }

    private String course;

    public Students(String dni, String name, String prename) {
        super(dni, name, prename);
    }
}
