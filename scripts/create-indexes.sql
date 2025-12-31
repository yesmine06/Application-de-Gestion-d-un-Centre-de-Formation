-- Script SQL pour créer des index sur les colonnes fréquemment recherchées
-- Ces index améliorent significativement les performances des requêtes

USE formation_db;

-- Index sur la table User (recherches fréquentes par username et email)
CREATE INDEX IF NOT EXISTS idx_user_username ON user(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);
CREATE INDEX IF NOT EXISTS idx_user_enabled ON user(enabled);

-- Index sur la table Student (recherches par matricule)
CREATE INDEX IF NOT EXISTS idx_student_matricule ON student(matricule);
CREATE INDEX IF NOT EXISTS idx_student_specialty_id ON student(specialty_id);
CREATE INDEX IF NOT EXISTS idx_student_group_id ON student(group_id);
CREATE INDEX IF NOT EXISTS idx_student_date_inscription ON student(date_inscription);

-- Index sur la table Course (recherches par code et formateur)
CREATE INDEX IF NOT EXISTS idx_course_code ON course(code);
CREATE INDEX IF NOT EXISTS idx_course_formateur_id ON course(formateur_id);
CREATE INDEX IF NOT EXISTS idx_course_session_id ON course(session_id);

-- Index sur la table Enrollment (recherches par étudiant et cours)
CREATE INDEX IF NOT EXISTS idx_enrollment_student_id ON enrollment(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollment_cours_id ON enrollment(cours_id);
-- Index composite pour la recherche par étudiant ET cours
CREATE INDEX IF NOT EXISTS idx_enrollment_student_cours ON enrollment(student_id, cours_id);

-- Index sur la table Grade (recherches par étudiant et cours)
CREATE INDEX IF NOT EXISTS idx_grade_student_id ON grade(student_id);
CREATE INDEX IF NOT EXISTS idx_grade_cours_id ON grade(cours_id);
-- Index composite pour la recherche par étudiant ET cours
CREATE INDEX IF NOT EXISTS idx_grade_student_cours ON grade(student_id, cours_id);
-- Index sur la valeur pour les calculs de moyenne
CREATE INDEX IF NOT EXISTS idx_grade_valeur ON grade(valeur);

-- Index sur la table Schedule (recherches par date, cours et formateur)
CREATE INDEX IF NOT EXISTS idx_schedule_date ON schedule(date);
CREATE INDEX IF NOT EXISTS idx_schedule_cours_id ON schedule(cours_id);
CREATE INDEX IF NOT EXISTS idx_schedule_status ON schedule(status);
-- Index composite pour les recherches de conflits
CREATE INDEX IF NOT EXISTS idx_schedule_date_heure ON schedule(date, heure_debut, heure_fin);

-- Index sur la table CourseFile (recherches par cours)
CREATE INDEX IF NOT EXISTS idx_course_file_course_id ON course_file(course_id);

-- Index sur la table user_roles (recherches par user_id et role_id)
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- Index sur la table course_groups (recherches par course_id et group_id)
CREATE INDEX IF NOT EXISTS idx_course_groups_course_id ON course_groups(course_id);
CREATE INDEX IF NOT EXISTS idx_course_groups_group_id ON course_groups(group_id);

-- Afficher tous les index créés
SHOW INDEX FROM user;
SHOW INDEX FROM student;
SHOW INDEX FROM course;
SHOW INDEX FROM enrollment;
SHOW INDEX FROM grade;
SHOW INDEX FROM schedule;

