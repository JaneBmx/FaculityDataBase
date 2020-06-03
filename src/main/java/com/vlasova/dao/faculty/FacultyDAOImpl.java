package com.vlasova.dao.faculty;

import com.vlasova.dao.AbstractDAO;
import com.vlasova.dao.mapper.FacultyResultSetMapper;
import com.vlasova.entity.faculity.Faculty;
import com.vlasova.entity.faculity.Subject;
import com.vlasova.exception.dao.CreateObjectException;
import com.vlasova.exception.dao.DAOException;
import com.vlasova.pool.ConnectionPool;
import com.vlasova.pool.ProxyConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class FacultyDAOImpl extends AbstractDAO implements FacultyDAO {
    private static final Logger LOGGER = LogManager.getLogger(FacultyDAOImpl.class);
    private static final String INSERT_FACULTY = "INSERT INTO faculties(faculty_name, free_accept_plan, paid_accept_plan) " +
            "VALUES(?,?,?)";
    private static final String INSERT_SUBJECTS = "INSERT INTO faculty2subject (faculty_id, subject_id) VALUES (?,?)";
    private static final String DELETE = "DELETE FROM faculties WHERE faculty_id = ?";
    private static final String DELETE_MARKS = "DELETE FROM faculty2subject WHERE faculty_id = ?";
    private static final String UPDATE = "UPDATE faculty SET faculty_name = ?, free_accept_plan = ?, paid_accept_plan = ? " +
            "WHERE faculty_id = ?";
    private static final String FIND_ALL_FACULTIES =
            "SELECT f.faculty_id, f.faculty_name, f.free_accept_plan, f.paid_accept_plan, sf.subject_id " +
                    "FROM faculties f LEFT JOIN  faculty2subject sf ON f.faculty_id = sf.faculty_id " +
                    "UNION SELECT f.faculty_id, f.faculty_name, f.free_accept_plan, f.paid_accept_plan, sf.subject_id " +
                    "FROM faculties f RIGHT JOIN faculty2subject sf ON f.faculty_id = sf.faculty_id;";
    private static final String FIND_BY_PAID =
            "SELECT f.faculty_id, f.faculty_name, f.free_accept_plan, f.paid_accept_plan, sf.subject_id " +
                    "FROM faculties f LEFT JOIN  faculty2subject sf ON f.faculty_id = sf.faculty_id " +
                    "WHERE f.free_accept_plan ? " +
                    "UNION SELECT f.faculty_id, f.faculty_name, f.free_accept_plan, f.paid_accept_plan, sf.subject_id " +
                    "FROM faculties f RIGHT JOIN faculty2subject sf ON f.faculty_id = sf.faculty_id WHERE f.free_accept_plan ?;";
    private static final String FIND_BY_ID =
            "SELECT faculty_id, faculty_name, free_accept_plan, paid_accept_plan FROM faculties WHERE faculty_id = ?";
    private static final String FIND_BY_SUBJECT =
            "SELECT f.faculty_id, f.faculty_name, f.free_accept_plan, f.paid_accept_plan, sf.subject_id " +
                    "FROM faculties f LEFT JOIN  faculty2subject sf ON f.faculty_id = sf.faculty_id WHERE sf.subject_id " +
                    "UNION SELECT f.faculty_id, f.faculty_name, f.free_accept_plan, f.paid_accept_plan, sf.subject_id " +
                    "FROM faculties f RIGHT JOIN faculty2subject sf ON f.faculty_id = sf.faculty_id WHERE sf.subject_id = ?;";
    private static final String FIND_BY_NAME = "SELECT * FROM faculties WHERE faculty_name = ?";
    private final FacultyResultSetMapper mapper = new FacultyResultSetMapper();

    @Override
    public void add(Faculty faculty) throws DAOException {
        try (ProxyConnection connection = ConnectionPool.INSTANCE.getConnection()) {
            addFaculty(connection, faculty);
            int facultyID = findByName(connection, faculty.getName()).getId();
            addSubjects(connection, facultyID, faculty.getSubjects());
        } catch (CreateObjectException | SQLException e) {
            LOGGER.warn(e);
            throw new DAOException(e);
        }
    }

    /**
     * Add faculty into database only with name, free accept plan, paid accept plan
     *
     * @param connection
     * @param faculty
     * @throws SQLException
     */
    public void addFaculty(ProxyConnection connection, Faculty faculty) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(INSERT_FACULTY);
        statement.setString(1, faculty.getName());
        statement.setInt(2, faculty.getFreeAcceptPlan());
        statement.setInt(3, faculty.getPaidAcceptPlan());
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Get faculty from db with id (given by db)
     *
     * @param connection
     * @param name
     * @return incomplete Faculty
     * @throws SQLException
     * @throws CreateObjectException
     * @see Faculty
     */
    private Faculty findByName(ProxyConnection connection, String name) throws SQLException, CreateObjectException {
        PreparedStatement statement = connection.prepareStatement(FIND_BY_NAME);
        statement.setString(1, name);
        resultSet = statement.executeQuery();
        Faculty faculty = mapper.mapOne(resultSet);
        statement.close();
        return faculty;
    }

    /**
     * Add subjects to adjacent table
     *
     * @param connection
     * @param facultyId  faculty id
     * @param subjects   Set with subjects
     * @throws SQLException
     */
    private void addSubjects(ProxyConnection connection, int facultyId, Set<Subject> subjects) throws SQLException {
        connection.setAutoCommit(false);
        PreparedStatement statement = connection.prepareStatement(INSERT_SUBJECTS);
        for (Subject s : subjects) {
            statement.setInt(1, facultyId);
            statement.setInt(2, s.getId());
            statement.addBatch();
        }
        statement.executeBatch();
    }

    @Deprecated
    @Override
    public void remove(Faculty faculty) throws DAOException {
        try (ProxyConnection connection = ConnectionPool.INSTANCE.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE);
             PreparedStatement preparedStatement1 = connection.prepareStatement(DELETE_MARKS)) {
            preparedStatement.setInt(1, faculty.getId());
            preparedStatement.executeUpdate();
            preparedStatement1.setInt(1, faculty.getId());
        } catch (SQLException e) {
            LOGGER.warn(e);
            throw new DAOException(e);
        }
    }

    @Override
    public void remove(int facultyId) throws DAOException {
        try (ProxyConnection connection = ConnectionPool.INSTANCE.getConnection();
             PreparedStatement preparedStatement1 = connection.prepareStatement(DELETE_MARKS);
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE);) {
            preparedStatement1.setInt(1, facultyId);
            preparedStatement1.executeUpdate();
            preparedStatement.setInt(1, facultyId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warn(e);
            throw new DAOException(e);
        }
    }

    @Override
    public void update(Faculty faculty) throws DAOException {
        try (ProxyConnection connection = ConnectionPool.INSTANCE.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE);) {
            preparedStatement.setString(1, faculty.getName());
            preparedStatement.setInt(2, faculty.getFreeAcceptPlan());
            preparedStatement.setInt(3, faculty.getPaidAcceptPlan());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warn(e);
            throw new DAOException(e);
        }
    }

    @Override
    public Set<Faculty> findAllFaculties() throws DAOException {
        try (ProxyConnection connection = ConnectionPool.INSTANCE.getConnection();
             Statement statement = connection.createStatement()) {
            resultSet = statement.executeQuery(FIND_ALL_FACULTIES);
            return mapper.map(resultSet);
        } catch (SQLException | CreateObjectException e) {
            LOGGER.warn(e);
            throw new DAOException(e);
        } finally {
            closeResultSet();
        }
    }

    @Override
    public Set<Faculty> findFacultyByPaid(boolean isPaid) throws DAOException {
        String paid = isPaid ? "> 0" : " = 0";
        try (ProxyConnection connection = ConnectionPool.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_PAID)) {
            statement.setString(1, paid);
            statement.setString(2, paid);
            resultSet = statement.executeQuery(FIND_BY_PAID);
            return mapper.map(resultSet);
        } catch (SQLException | CreateObjectException e) {
            LOGGER.warn(e);
            throw new DAOException(e);
        } finally {
            closeResultSet();
        }
    }

    @Override
    public Faculty findFacultyById(int id) throws DAOException {
        try (ProxyConnection connection = ConnectionPool.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapper.mapOne(resultSet);
            }
            return null;
        } catch (SQLException | CreateObjectException e) {
            LOGGER.warn(e);
            throw new DAOException(e);
        } finally {
            closeResultSet();
        }
    }

    @Override
    public Set<Faculty> findFacultyBySubject(Subject subject) throws DAOException {
        try (ProxyConnection connection = ConnectionPool.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_SUBJECT);) {
            statement.setInt(1, subject.getId());
            statement.setInt(2, subject.getId());
            resultSet = statement.executeQuery();
            return mapper.map(resultSet);
        } catch (SQLException | CreateObjectException e) {
            LOGGER.warn(e);
            throw new DAOException(e);
        } finally {
            closeResultSet();
        }
    }
}
