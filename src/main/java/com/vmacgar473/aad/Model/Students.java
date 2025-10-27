package com.vmacgar473.aad.Model;

import lombok.ToString;

import java.util.List;

@ToString

public class Students extends Person {


    private List<Module> modules;

    private String course;

    public Students(String course) {
        this.course = course;
    }

    public Students(String dni, String name, String prename) {
        super(dni, name, prename);
    }
}
