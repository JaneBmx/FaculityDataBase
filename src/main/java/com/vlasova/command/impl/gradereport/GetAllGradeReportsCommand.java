package com.vlasova.command.impl.gradereport;

import com.vlasova.command.Answer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetAllGradeReportsCommand implements GradeReportCommand {
    @Override
    public Answer execute(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }
}