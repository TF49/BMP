package com.badminton.bmp.modules.course;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class CoachStudentsMigrationSqlTest {

    private static final Path PATCH = Path.of("src/main/resources/sql/patch-coach-students.sql");

    @Test
    void migrationIsIdempotentAndBackfillsOnlyCompletedBookingsInRecoverableBatches() throws IOException {
        assertTrue(Files.exists(PATCH), "attendance migration SQL must exist");
        String sql = Files.readString(PATCH, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);

        assertTrue(sql.contains("information_schema.columns"));
        assertTrue(sql.contains("information_schema.statistics"));
        assertTrue(sql.contains("attendance_status"));
        assertTrue(sql.contains("actual_checkin_time"));
        assertTrue(sql.contains("actual_finish_time"));
        assertTrue(sql.contains("prepare"));
        assertTrue(sql.contains("execute"));

        assertTrue(sql.contains("idx_cb_course_member_status"));
        assertTrue(sql.contains("idx_cb_member_attendance"));
        assertTrue(sql.contains("idx_c_coach_date_range"));
        assertTrue(sql.contains("idx_cb_course_attendance"));
        assertFalse(sql.contains("idx_sm_name"), "wildcard member-name search must not get a normal btree index");
        assertFalse(sql.contains("idx_sm_phone"), "wildcard phone search must not get a normal btree index");

        assertTrue(sql.matches("(?s).*set\\s+@backfill_batch_size\\s*=\\s*(1000|[1-4][0-9]{3}|5000).*"));
        assertTrue(sql.contains("@backfill_last_id"));
        assertTrue(sql.contains("status = 4"));
        assertTrue(sql.contains("attendance_status = 2"));
        assertFalse(sql.matches("(?s).*status\\s*=\\s*0.*attendance_status\\s*=\\s*3.*"));
        assertTrue(sql.contains("backfill_checkpoint"));
        assertTrue(sql.contains("group by status, attendance_status"));
    }
}
