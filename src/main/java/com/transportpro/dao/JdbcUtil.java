package com.transportpro.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Conversions JDBC ↔ java.time / enums communes aux DAO. */
final class JdbcUtil {
    private JdbcUtil() {}

    static LocalDate date(ResultSet rs, String col) throws SQLException {
        Date d = rs.getDate(col);
        return d == null ? null : d.toLocalDate();
    }

    static LocalTime time(ResultSet rs, String col) throws SQLException {
        Time t = rs.getTime(col);
        return t == null ? null : t.toLocalTime();
    }

    static LocalDateTime dateTime(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }

    static Integer intOrNull(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    static <E extends Enum<E>> E enumOf(Class<E> type, String value, E defaut) {
        if (value == null) return defaut;
        try {
            return Enum.valueOf(type, value.toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return defaut;
        }
    }

    static void setDate(PreparedStatement ps, int i, LocalDate d) throws SQLException {
        if (d == null) ps.setNull(i, Types.DATE); else ps.setDate(i, Date.valueOf(d));
    }

    static void setTime(PreparedStatement ps, int i, LocalTime t) throws SQLException {
        if (t == null) ps.setNull(i, Types.TIME); else ps.setTime(i, Time.valueOf(t));
    }

    static void setDateTime(PreparedStatement ps, int i, LocalDateTime t) throws SQLException {
        if (t == null) ps.setNull(i, Types.TIMESTAMP); else ps.setTimestamp(i, Timestamp.valueOf(t));
    }

    static void setInt(PreparedStatement ps, int i, Integer v) throws SQLException {
        if (v == null) ps.setNull(i, Types.INTEGER); else ps.setInt(i, v);
    }
}
