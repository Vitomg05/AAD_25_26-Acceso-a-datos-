package com.vmacgar473.aad.respository;

import com.vmacgar473.aad.Model.Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j

public class ModuleRepository implements crudRepository<Module> {
    /**
     * @param entity
     * @return
     */
    @Override
    public Module create(Module entity) {
        log.info("Insert: {}", entity.toString());

        return entity;
    }

    /**
     * @param entity
     * @return
     */
    @Override
    public Module read(Module entity) {
        log.info("Insert: {}", entity.toString());

        return entity;
    }

    /**
     * @param entity
     * @return
     */
    @Override
    public Module update(Module entity) {
        log.info("Insert: {}", entity.toString());

        return entity;
    }

    /**
     * @param entity
     * @return
     */
    @Override
    public boolean delete(Module entity) {
        log.info("Insert: {}", entity.toString());

        return true;
    }
}
