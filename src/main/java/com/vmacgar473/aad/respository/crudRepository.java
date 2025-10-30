package com.vmacgar473.aad.respository;



public interface crudRepository<t> {

   t create(t entity);


   t read(t entity);


   t update(t entity);


   boolean delete(t entity);


}
