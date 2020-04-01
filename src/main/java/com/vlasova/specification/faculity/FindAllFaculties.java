package com.vlasova.specification.faculity;

import com.vlasova.entity.faculity.Faculty;
import com.vlasova.exception.specification.QueryException;
import com.vlasova.pool.ConnectionPool;
import com.vlasova.pool.ProxyConnection;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class FindAllFaculties extends AbstractFacultySpecification implements FacultySpecification {
    private static final String FIND_ALL_FACULTIES = "SELECT * FROM faculties";

    @Override
    public Set<Faculty> query() throws QueryException {
        faculties = new HashSet<>();
        try (ProxyConnection connection = ConnectionPool.INSTANCE.getConnection();
             Statement statement = connection.createStatement()) {
            if (statement != null) {
                resultSet = statement.executeQuery(FIND_ALL_FACULTIES);
                while (resultSet.next()) {
                    faculties.add(createFaculty());
                }
            }
        } catch (SQLException e) {
            throw new QueryException(e);
        } finally {
            closeResultSet();
        }
        return faculties;
    }
}