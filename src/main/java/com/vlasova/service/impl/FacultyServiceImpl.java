package com.vlasova.service.impl;

import com.vlasova.entity.faculity.Faculty;
import com.vlasova.entity.faculity.Subject;
import com.vlasova.exception.repository.RepositoryException;
import com.vlasova.exception.service.ServiceException;
import com.vlasova.repository.faculity.FacultyRepository;
import com.vlasova.repository.faculity.FacultyRepositoryImpl;
import com.vlasova.service.FacultyService;
import com.vlasova.specification.faculity.FindAllFaculties;
import com.vlasova.specification.faculity.FindFacultyByFreePaid;
import com.vlasova.specification.faculity.FindFacultyById;
import com.vlasova.specification.faculity.FindFacultyBySubject;
import com.vlasova.validation.FacultyValidator;
import static com.vlasova.validation.FacultyValidator.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FacultyServiceImpl implements FacultyService {
    private final FacultyRepository facultyRepository;

    private static class Holder {
        private static final FacultyServiceImpl INSTANCE = new FacultyServiceImpl();
    }

    public static FacultyServiceImpl getInstance() {
        return FacultyServiceImpl.Holder.INSTANCE;
    }

    private FacultyServiceImpl() {
        facultyRepository = FacultyRepositoryImpl.getInstance();
    }

    public Set<Faculty> getAllFaculties() throws ServiceException {
        try {
            return facultyRepository.query(new FindAllFaculties());
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }
    }

    public Set<Faculty> getAllFreePaidFaculties() throws ServiceException {
        try {
            return facultyRepository.query(new FindFacultyByFreePaid());
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }
    }

    public Set<Faculty> getFacultiesBySubjects(Subject subject) throws ServiceException {
        if (subject != null) {
            try {
                return facultyRepository.query(new FindFacultyBySubject(subject));
            } catch (RepositoryException e) {
                throw new ServiceException(e);
            }
        }
        return new HashSet<>();
    }

    public Faculty getFacultyById(int id) throws ServiceException {
        try {
            return facultyRepository.query(new FindFacultyById(id)).iterator().next();
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }
    }

    public void addFaculty(String name, int free, int paid, Subject... subjects) throws ServiceException {
        if (FacultyValidator.isValidFaculty(name, free, paid, subjects)) {
            Faculty faculty = new Faculty();
            faculty.setName(name);
            faculty.setFreeAcceptPlan(free);
            faculty.setPaidAcceptPlan(paid);
            faculty.setSubjects(new HashSet<>(Arrays.asList(subjects)));
            try {
                facultyRepository.add(faculty);
            } catch (RepositoryException e) {
                throw new ServiceException(e);
            }
        }
    }

    public void deleteFaculty(Faculty faculty) throws ServiceException {
        if (isValidFaculty(faculty)) {
            try {
                facultyRepository.remove(faculty);
            } catch (RepositoryException e) {
                throw new ServiceException(e);
            }
        }
    }

    public void updateFaculty(Faculty faculty) throws ServiceException {
        if (isValidFaculty(faculty)) {
            try {
                facultyRepository.update(faculty);
            } catch (RepositoryException e) {
                throw new ServiceException(e);
            }
        }
    }
}