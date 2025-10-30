package com.vmacgar473.aad.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Person {

    private String dni;
    private String name;
    private String prename;


//    public Person(String dni, String name, String prename) {
//        this.prename = prename;
//    }
//
//    public String getPrename() {
//        return prename;
//    }
//
//    public void setPrename(String prename) {
//        this.prename = prename;
//    }
}
