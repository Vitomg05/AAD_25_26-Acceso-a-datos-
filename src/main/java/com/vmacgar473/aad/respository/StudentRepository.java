package com.vmacgar473.aad.respository;

import com.vmacgar473.aad.Model.Students;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j

public class StudentRepository implements crudRepository<Students> {

    /**
     * @param entity
     * @return
     */
    @Override
    public Students create(Students entity) {

        log.info("Insert: {}", entity.toString());

        return entity;
    }

    /**
     * @param entity
     * @return
     */
    @Override
    public Students read(Students entity) {
        log.info("Insert: {}", entity.toString());
        return entity;
    }

    /**
     * @param entity
     * @return
     */
    @Override
    public Students update(Students entity) {
        log.info("Insert: {}", entity.toString());
        return entity;
    }

    /**
     * @param entity
     * @return
     */
    @Override
    public boolean delete(Students entity) {
        log.info("Insert: {}", entity.toString());
        return true;
    }
}
