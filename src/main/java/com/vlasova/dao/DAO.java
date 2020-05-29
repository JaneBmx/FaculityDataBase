package com.vlasova.dao;

import com.vlasova.exception.dao.DAOException;

public interface DAO<T> {
    void add(T t) throws DAOException;

    void remove(T t) throws DAOException;

    void remove(int id) throws DAOException;

    void update(T t) throws DAOException;
}
