package com.vmacgar473.aad.service;

import com.vmacgar473.aad.Model.Students;
import com.vmacgar473.aad.respository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class StudentService implements CustomService<Students> {

    private final StudentRepository studentRepository;

//    public StudentService(StudentRepository studentRepository) {
//        this.studentRepository = studentRepository;
//    }

    /**
     * @param entity
     * @return
     */
    @Override
    public boolean validate(Students entity) {
        return entity.getDni().isBlank() && entity.getName().isBlank();
    }

    public Students createStudents(Students students) {

        if (validate(students)) {
            return studentRepository.create(students);
        }
        return null;

    }
}
