-- Active: 1751467122814@@localhost@3306@DecliTech_Db
-- Database Configuration
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+01:00";

-- Clean up existing data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE app_user;
TRUNCATE TABLE child;
TRUNCATE TABLE co_build_space;
TRUNCATE TABLE module;
TRUNCATE TABLE module_session;
TRUNCATE TABLE inscription;
TRUNCATE TABLE reservation;
TRUNCATE TABLE presence;
TRUNCATE TABLE notification;
TRUNCATE TABLE reclamation;
TRUNCATE TABLE penalty;
TRUNCATE TABLE token;
TRUNCATE TABLE tracking_sheet;
TRUNCATE TABLE news;
SET FOREIGN_KEY_CHECKS = 1;

-- Reset auto-increment counters
ALTER TABLE app_user AUTO_INCREMENT = 1;
ALTER TABLE child AUTO_INCREMENT = 1;
ALTER TABLE co_build_space AUTO_INCREMENT = 1;
ALTER TABLE module AUTO_INCREMENT = 1;
ALTER TABLE module_session AUTO_INCREMENT = 1;
ALTER TABLE inscription AUTO_INCREMENT = 1;
ALTER TABLE reservation AUTO_INCREMENT = 1;
ALTER TABLE presence AUTO_INCREMENT = 1;
ALTER TABLE notification AUTO_INCREMENT = 1;
ALTER TABLE reclamation AUTO_INCREMENT = 1;
ALTER TABLE penalty AUTO_INCREMENT = 1;
ALTER TABLE token AUTO_INCREMENT = 1;
ALTER TABLE tracking_sheet AUTO_INCREMENT = 1;
ALTER TABLE news AUTO_INCREMENT = 1;

-- Insert CoBuildSpaces
INSERT INTO co_build_space (space_id, name, address, actif) VALUES
(1, 'Creative Hub Tunis', 'Rue de l''Innovation, Tunis', true),
(2, 'Innovation Center Sfax', 'Tech Park, Sfax', true),
(3, 'Kids Lab Sousse', 'Avenue de la Plage, Sousse', true);

-- Insert Users (using bcrypt for passwords)
INSERT INTO app_user (id, email, password, phone, role, active, first_name, last_name, username, is_verified, registration_date, co_build_space_space_id) VALUES
(1, 'admin1@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698123456', 'ADMIN', true, 'Admin', 'One', 'admin1', true, '2025-07-08 10:00:00', 1),
(2, 'admin2@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698123457', 'ADMIN', true, 'Admin', 'Two', 'admin2', true, '2025-07-08 10:00:00', 2),
(3, 'admin3@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698123458', 'ADMIN', true, 'Admin', 'Three', 'admin3', true, '2025-07-08 10:00:00', 3),
(4, 'parent1@email.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698234567', 'PARENT', true, 'Ahmed', 'Ben Ali', 'ahmed.benali', true, '2025-07-08 10:00:00', null),
(5, 'parent2@email.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698234568', 'PARENT', true, 'Fatma', 'Ben Salem', 'fatma.bensalem', true, '2025-07-08 10:00:00', null),
(6, 'parent3@email.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698234569', 'PARENT', true, 'Mohamed', 'Karim', 'mohamed.karim', true, '2025-07-08 10:00:00', null),
(7, 'parent4@email.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698234570', 'PARENT', true, 'Sarra', 'Mansour', 'sarra.mansour', true, '2025-07-08 10:00:00', null),
(8, 'parent5@email.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698234571', 'PARENT', true, 'Yassine', 'Ben Amor', 'yassine.benamor', true, '2025-07-08 10:00:00', null),
(9, 'instructor1@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698345678', 'INSTRUCTOR', true, 'Ines', 'Instructor', 'ines.instructor', true, '2025-07-08 10:00:00', 1),
(10, 'instructor2@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698345679', 'INSTRUCTOR', true, 'Kamel', 'Instructor', 'kamel.instructor', true, '2025-07-08 10:00:00', 2),
(11, 'instructor3@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698345680', 'INSTRUCTOR', true, 'Leila', 'Instructor', 'leila.instructor', true, '2025-07-08 10:00:00', 3),
(12, 'instructor4@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698345681', 'INSTRUCTOR', true, 'Nabil', 'Instructor', 'nabil.instructor', true, '2025-07-08 10:00:00', 1),
(13, 'orgteam1@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698456789', 'ORGANIZINGTEAM', true, 'Organizer', 'One', 'organizer1', true, '2025-07-08 10:00:00', 1),
(14, 'orgteam2@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698456790', 'ORGANIZINGTEAM', true, 'Organizer', 'Two', 'organizer2', true, '2025-07-08 10:00:00', 2),
(15, 'orgteam3@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698456791', 'ORGANIZINGTEAM', true, 'Organizer', 'Three', 'organizer3', true, '2025-07-08 10:00:00', 3),
(16, 'manager1@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698567890', 'MANAGER', true, 'Manager', 'One', 'manager1', true, '2025-07-08 10:00:00', 1),
(17, 'manager2@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698567891', 'MANAGER', true, 'Manager', 'Two', 'manager2', true, '2025-07-08 10:00:00', 2),
(18, 'manager3@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698567892', 'MANAGER', true, 'Manager', 'Three', 'manager3', true, '2025-07-08 10:00:00', 3),
(19, 'regteam1@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698678901', 'REGULATIONTEAM', true, 'Regulation', 'One', 'regteam1', true, '2025-07-08 10:00:00', 1),
(20, 'regteam2@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698678902', 'REGULATIONTEAM', true, 'Regulation', 'Two', 'regteam2', true, '2025-07-08 10:00:00', 2),
(21, 'regteam3@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698678903', 'REGULATIONTEAM', true, 'Regulation', 'Three', 'regteam3', true, '2025-07-08 10:00:00', 3),
(22, 'superadmin1@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698789012', 'SUPERADMIN', true, 'Super', 'Admin1', 'superadmin1', true, '2025-07-08 10:00:00', null),
(23, 'superadmin2@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698789013', 'SUPERADMIN', true, 'Super', 'Admin2', 'superadmin2', true, '2025-07-08 10:00:00', null),
(24, 'superadmin3@declitech.com', '$2a$10$xPJ5y1J3S5B1xpx6Lx3zWuqYWS.H3QhC1B1xJ1xX1xX1xX1xX1xX', '+21698789014', 'SUPERADMIN', true, 'Super', 'Admin3', 'superadmin3', true, '2025-07-08 10:00:00', null);

-- Insert Children
INSERT INTO child (id, first_name, last_name, sexe, age, parent_id) VALUES
(1, 'Youssef', 'Ben Ali', 'M', 8, 4),
(2, 'Yasmine', 'Ben Ali', 'F', 10, 4),
(3, 'Adam', 'Ben Salem', 'M', 7, 5),
(4, 'Rania', 'Ben Salem', 'F', 12, 5),
(5, 'Omar', 'Karim', 'M', 9, 6),
(6, 'Nour', 'Mansour', 'F', 11, 7),
(7, 'Amir', 'Ben Amor', 'M', 6, 8);

-- Insert Modules
INSERT INTO module (id, title, description, jour, actif, enrolled_count, annulation, space_id) VALUES
(1, 'Creative Coding for Kids', 'Introduction to programming concepts through fun activities', 'Monday', true, 0, false, 1),
(2, 'Science Discovery Workshop', 'Hands-on science experiments and learning', 'Tuesday', true, 0, false, 2),
(3, 'Art & Design Studio', 'Creative expression through various art forms', 'Wednesday', true, 0, false, 3),
(4, 'Robotics Basics', 'Introduction to robotics and mechanical concepts', 'Thursday', true, 0, false, 1);

-- Link Instructors to Modules
INSERT INTO module_instructors (module_id, user_id) VALUES
(1, 9), -- Creative Coding - Ines
(1, 12), -- Creative Coding - Nabil
(2, 10), -- Science Discovery - Kamel
(3, 11), -- Art & Design - Leila
(4, 9), -- Robotics - Ines
(4, 12); -- Robotics - Nabil

-- Insert Module Sessions (for the last 3 days)
INSERT INTO module_session (id, date, start_time, end_time, capcity, enrolled_count, is_active, tranche_age_min, tranche_age_max, module_id, co_build_space_space_id) VALUES
-- July 8
(1, '2025-07-08', '09:00:00', '11:00:00', 15, 0, true, 6, 10, 1, 1),
(2, '2025-07-08', '14:00:00', '16:00:00', 15, 0, true, 11, 15, 2, 2),
(3, '2025-07-08', '10:00:00', '12:00:00', 12, 0, true, 8, 12, 3, 3),
-- July 9
(4, '2025-07-09', '09:00:00', '11:00:00', 15, 0, true, 6, 10, 1, 1),
(5, '2025-07-09', '14:00:00', '16:00:00', 15, 0, true, 11, 15, 2, 2),
(6, '2025-07-09', '10:00:00', '12:00:00', 12, 0, true, 8, 12, 4, 1),
-- July 10
(7, '2025-07-10', '09:00:00', '11:00:00', 15, 0, true, 6, 10, 1, 1),
(8, '2025-07-10', '14:00:00', '16:00:00', 15, 0, true, 11, 15, 3, 3),
(9, '2025-07-10', '10:00:00', '12:00:00', 12, 0, true, 8, 12, 4, 1);

-- Insert Inscriptions
INSERT INTO inscription (id, date_inscription, date_expiration, active, status_inscription, child_id) VALUES
(1, '2025-07-01', '2026-07-01', true, 'ACTIVE', 1),
(2, '2025-07-01', '2026-07-01', true, 'ACTIVE', 2),
(3, '2025-07-01', '2026-07-01', true, 'ACTIVE', 3),
(4, '2025-07-01', '2026-07-01', true, 'ACTIVE', 4),
(5, '2025-07-01', '2026-07-01', true, 'ACTIVE', 5),
(6, '2025-07-01', '2026-07-01', true, 'ACTIVE', 6),
(7, '2025-07-01', '2026-07-01', true, 'ACTIVE', 7);

-- Insert Reservations
INSERT INTO reservation (id, date_reservation, penalise, created_at, status, session_id, child_id, parent_id, module_id, organizing_team_id) VALUES
(1, '2025-07-08 09:00:00', false, '2025-07-07 10:00:00', 'COMPLETED', 1, 1, 4, 1, 13),
(2, '2025-07-08 14:00:00', false, '2025-07-07 11:00:00', 'COMPLETED', 2, 4, 5, 2, 14),
(3, '2025-07-09 09:00:00', false, '2025-07-08 10:00:00', 'COMPLETED', 4, 2, 4, 1, 13),
(4, '2025-07-09 14:00:00', false, '2025-07-08 11:00:00', 'CANCELED', 5, 5, 6, 2, 14),
(5, '2025-07-10 09:00:00', false, '2025-07-09 10:00:00', 'RESERVED', 7, 3, 5, 1, 13);

-- Insert Module Session Presence Records
INSERT INTO presence (id, date_time, reservation_date, pupil_name, present, parent_name, status, child_id, organizing_team_id, session_id, presence_type) VALUES
(1, '2025-07-08 09:00:00', '2025-07-08', 'Youssef Ben Ali', true, 'Ahmed Ben Ali', 'PRESENT', 1, 13, 1, 'MODULE_SESSION'),
(2, '2025-07-08 14:00:00', '2025-07-08', 'Rania Ben Salem', true, 'Fatma Ben Salem', 'PRESENT', 4, 14, 2, 'MODULE_SESSION'),
(3, '2025-07-09 09:00:00', '2025-07-09', 'Yasmine Ben Ali', true, 'Ahmed Ben Ali', 'PRESENT', 2, 13, 4, 'MODULE_SESSION');

-- Insert Site Entry/Exit Presence Records
INSERT INTO presence (id, date_time, reservation_date, pupil_name, present, parent_name, status, child_id, organizing_team_id, site_id, presence_type) VALUES
(4, '2025-07-08 08:45:00', '2025-07-08', 'Youssef Ben Ali', true, 'Ahmed Ben Ali', 'PRESENT', 1, 13, 1, 'SITE_ENTRY'),
(5, '2025-07-08 11:15:00', '2025-07-08', 'Youssef Ben Ali', true, 'Ahmed Ben Ali', 'PRESENT', 1, 13, 1, 'SITE_EXIT');

-- Insert Tracking Sheets
INSERT INTO tracking_sheet (id, tracking_date, activities, date_dernier_seance, level, child_id, module_id, instructor_id) VALUES
(1, '2025-07-08 11:00:00', 'Completed basic programming exercises', '2025-07-08', 'Beginner', 1, 1, 9),
(2, '2025-07-08 16:00:00', 'Participated in science experiments', '2025-07-08', 'Intermediate', 4, 2, 10);


-- Insert Notifications
INSERT INTO notification (id, message, is_read, created_at, parent_id) VALUES
(1, 'Your child''s session starts in 15 minutes', true, '2025-07-08 08:45:00', 4),
(2, 'New feedback requested for today''s session', false, '2025-07-08 11:15:00', 4);

-- Insert Reclamations
INSERT INTO reclamation (id, subject, description, date_soumission, statut, parent_id, module_id) VALUES
(1, 'Schedule Conflict', 'Need to adjust session timing', '2025-07-08 12:00:00', 'EN_COURS', 4, 1);

-- Insert News
INSERT INTO news (id, title, content, image_url, created_at, type, module_id, created_by_admin_id) VALUES
(1, 'New Coding Workshop', 'Exciting new workshop for beginners', null, '2025-07-08 10:00:00', 'ANNOUNCEMENT', 1, 1),
(2, 'Science Fair Coming Soon', 'Join us for an amazing science fair', null, '2025-07-09 10:00:00', 'EVENT', 2, 2);

COMMIT;
