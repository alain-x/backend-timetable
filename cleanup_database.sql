-- Cleanup script to fix foreign key constraint issues
-- Run this script in your MySQL database

USE digital_timetable-db;

-- Drop the problematic foreign key constraint if it exists
SET FOREIGN_KEY_CHECKS = 0;

-- Clean up any orphaned timetable records that reference non-existent users
DELETE FROM timetables WHERE class_rep_id IS NOT NULL AND class_rep_id NOT IN (SELECT id FROM users);

-- Set class_rep_id to NULL for any remaining problematic records
UPDATE timetables SET class_rep_id = NULL WHERE class_rep_id IS NOT NULL AND class_rep_id NOT IN (SELECT id FROM users);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Verify the cleanup
SELECT COUNT(*) as orphaned_timetables FROM timetables WHERE class_rep_id IS NOT NULL AND class_rep_id NOT IN (SELECT id FROM users); 