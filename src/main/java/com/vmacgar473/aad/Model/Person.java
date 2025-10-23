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
//        this.dni = dni;
//        this.name = name;
//        this.prename = prename;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getPrename() {
//        return prename;
//    }
//
//    public void setPrename(String prename) {
//        this.prename = prename;
//    }
//
//    public String getDni() {
//        return dni;
//    }
//
//    public void setDni(String dni) {
//        this.dni = dni;
//    }

}
