const { useState, useEffect } = React;

const API_BASE_URL = 'http://localhost:8080/api';

// API Service
const apiService = {
    async login(username, password) {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        if (!response.ok) throw new Error('Erreur de connexion');
        return await response.json();
    },

    async getCurrentUser(token) {
        const response = await fetch(`${API_BASE_URL}/user/current`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de récupération utilisateur');
        return await response.json();
    },

    async getEnrollments(studentId, token) {
        const response = await fetch(`${API_BASE_URL}/inscriptions/student/${studentId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        return await response.json();
    },

    async getAllCourses(token) {
        const response = await fetch(`${API_BASE_URL}/cours`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        return await response.json();
    },

    async enroll(studentId, courseId, token) {
        // Validation
        if (!studentId || !courseId) {
            throw new Error('StudentId et CourseId sont requis');
        }
        
        const requestBody = { studentId, coursId: courseId };
        console.log('Envoi inscription:', requestBody);
        
        const response = await fetch(`${API_BASE_URL}/inscriptions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(requestBody)
        });
        if (!response.ok) {
            let errorMessage = 'Erreur d\'inscription';
            try {
                const errorData = await response.json();
                // Le message peut être dans différents champs selon le type d'erreur
                errorMessage = errorData.message || errorData.error || errorData.details || 'Erreur d\'inscription';
            } catch (e) {
                // Si la réponse n'est pas du JSON, utiliser le message par défaut
                errorMessage = `Erreur ${response.status}: ${response.statusText}`;
            }
            throw new Error(errorMessage);
        }
        return await response.json();
    },

    async unenroll(studentId, courseId, token) {
        const response = await fetch(`${API_BASE_URL}/inscriptions/student/${studentId}/course/${courseId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de désinscription');
    },

    async getGrades(token) {
        const response = await fetch(`${API_BASE_URL}/grades`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        return await response.json();
    },

    async getGradesByStudent(studentId, token) {
        console.log('Appel API getGradesByStudent:', `${API_BASE_URL}/grades/student/${studentId}`);
        const response = await fetch(`${API_BASE_URL}/grades/student/${studentId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        console.log('Réponse API notes:', response.status, response.statusText);
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Erreur API notes:', errorText);
            throw new Error(`Erreur de chargement: ${response.status} ${response.statusText}`);
        }
        const data = await response.json();
        console.log('Données reçues:', data);
        return data;
    },

    async getSchedule(studentId, date, token) {
        const response = await fetch(`${API_BASE_URL}/schedules/student/${studentId}?date=${date}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        return await response.json();
    },

    async getAllSchedules(studentId, token) {
        const response = await fetch(`${API_BASE_URL}/schedules/student/${studentId}/all`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        return await response.json();
    },

    async getStudentAverage(studentId, token) {
        const response = await fetch(`${API_BASE_URL}/grades/student/${studentId}/average`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        const data = await response.json();
        return data.average || 0;
    },

    async getCourseFiles(courseId, token) {
        const response = await fetch(`${API_BASE_URL}/course-files/course/${courseId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement des fichiers');
        return await response.json();
    },

    async downloadFile(fileId, fileName, token) {
        try {
            const response = await fetch(`${API_BASE_URL}/course-files/${fileId}/download`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) throw new Error('Erreur de téléchargement');
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', fileName);
            link.style.display = 'none';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
        } catch (err) {
            console.error('Erreur lors du téléchargement:', err);
            alert('Erreur lors du téléchargement du fichier');
        }
    }
};

// Login Component
function Login({ onLogin }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const data = await apiService.login(username, password);
            localStorage.setItem('jwt_token', data.token);
            const userInfo = await apiService.getCurrentUser(data.token);
            onLogin(data.token, userInfo);
        } catch (err) {
            setError(err.message || 'Erreur de connexion');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="row justify-content-center">
            <div className="col-md-4">
                <div className="card shadow">
                    <div className="card-header text-white text-center" style={{background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'}}>
                        <h4><i className="bi bi-mortarboard me-2"></i>Connexion Étudiant</h4>
                    </div>
                    <div className="card-body">
                        {error && <div className="alert alert-danger">{error}</div>}
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label className="form-label">Nom d'utilisateur</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Mot de passe</label>
                                <input
                                    type="password"
                                    className="form-control"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                            </div>
                            <button
                                type="submit"
                                className="btn w-100 text-white"
                                style={{background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'}}
                                disabled={loading}
                            >
                                {loading ? 'Connexion...' : <><i className="bi bi-box-arrow-in-right me-2"></i>Se connecter</>}
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
}

// Course Card Component
function CourseCard({ course, enrolled, onEnroll, onUnenroll, enrollmentId, token, isEnrolling = false }) {
    const [files, setFiles] = useState([]);
    const [loadingFiles, setLoadingFiles] = useState(false);
    const [showFiles, setShowFiles] = useState(false);

    useEffect(() => {
        if (enrolled && showFiles) {
            loadFiles();
        }
    }, [enrolled, showFiles, course.id, token]);

    const loadFiles = async () => {
        setLoadingFiles(true);
        try {
            const courseFiles = await apiService.getCourseFiles(course.id, token);
            setFiles(courseFiles);
        } catch (err) {
            console.error('Erreur lors du chargement des fichiers:', err);
            setFiles([]);
        } finally {
            setLoadingFiles(false);
        }
    };

    const handleDownload = async (fileId, fileName) => {
        await apiService.downloadFile(fileId, fileName, token);
    };

    return (
        <div className="col-md-4 mb-3">
            <div className="card">
                <div className="card-body">
                    <h5 className="card-title">{course.titre}</h5>
                    <p className="text-muted"><small>Code: {course.code}</small></p>
                    <p className="card-text">{course.description || 'Pas de description'}</p>
                    <div className="d-flex justify-content-between align-items-center mb-2">
                        <small className="text-muted">
                            <i className="bi bi-person me-1"></i>
                            {course.formateur ? `${course.formateur.nom} ${course.formateur.prenom}` : 'N/A'}
                        </small>
                        {enrolled ? (
                            <button
                                className="btn btn-sm btn-danger"
                                onClick={() => onUnenroll(course.id)}
                            >
                                <i className="bi bi-x-circle me-1"></i>Se désinscrire
                            </button>
                        ) : (
                            <button
                                className="btn btn-sm btn-success"
                                onClick={() => onEnroll(course.id)}
                                disabled={isEnrolling}
                            >
                                {isEnrolling ? (
                                    <>
                                        <span className="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                                        Inscription...
                                    </>
                                ) : (
                                    <>
                                        <i className="bi bi-plus-circle me-1"></i>S'inscrire
                                    </>
                                )}
                            </button>
                        )}
                    </div>
                    {enrolled && (
                        <div className="mt-2">
                            <button
                                className="btn btn-sm btn-outline-primary w-100"
                                onClick={() => setShowFiles(!showFiles)}
                            >
                                <i className={`bi ${showFiles ? 'bi-chevron-up' : 'bi-chevron-down'} me-1`}></i>
                                {showFiles ? 'Masquer' : 'Voir'} les fichiers ({files.length})
                            </button>
                            {showFiles && (
                                <div className="mt-2">
                                    {loadingFiles ? (
                                        <div className="text-center">
                                            <div className="spinner-border spinner-border-sm text-primary"></div>
                                        </div>
                                    ) : files.length === 0 ? (
                                        <div className="alert alert-info mb-0 py-2">
                                            <small><i className="bi bi-info-circle me-1"></i>Aucun fichier disponible</small>
                                        </div>
                                    ) : (
                                        <div className="list-group">
                                            {files.map(file => (
                                                <div key={file.id} className="list-group-item d-flex justify-content-between align-items-center py-2">
                                                    <div className="flex-grow-1">
                                                        <i className="bi bi-file-earmark me-2"></i>
                                                        <small className="fw-bold">{file.originalFileName}</small>
                                                        {file.description && (
                                                            <div className="text-muted">
                                                                <small>{file.description}</small>
                                                            </div>
                                                        )}
                                                    </div>
                                                    <button
                                                        className="btn btn-sm btn-outline-primary"
                                                        onClick={() => handleDownload(file.id, file.originalFileName)}
                                                        title="Télécharger"
                                                    >
                                                        <i className="bi bi-download"></i>
                                                    </button>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

// My Courses Tab
function MyCourses({ studentId, token, refreshKey, onUnenrollSuccess, isActive }) {
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Toujours recharger quand l'onglet devient actif ou quand refreshKey change
        if (isActive !== false) { // isActive peut être undefined au premier rendu
            loadCourses();
        }
    }, [refreshKey, isActive, studentId, token]); // Recharger quand refreshKey change, quand l'onglet devient actif, ou quand les props changent

    const loadCourses = async () => {
        setLoading(true);
        try {
            const enrollments = await apiService.getEnrollments(studentId, token);
            // Le DTO peut retourner soit e.cours (objet complet) soit coursId/coursTitre/coursCode
            const courseList = enrollments
                .filter(e => (e.cours && e.cours.id) || e.coursId) // Filtrer les inscriptions valides
                .map(e => {
                    // Utiliser l'objet cours si disponible, sinon créer à partir des propriétés
                    if (e.cours && e.cours.id) {
                        return e.cours;
                    } else {
                        return {
                            id: e.coursId,
                            titre: e.coursTitre,
                            code: e.coursCode,
                            description: null
                        };
                    }
                });
            setCourses(courseList);
            console.log('Mes Cours rechargés:', courseList.length, 'cours', courseList);
        } catch (err) {
            console.error('Erreur lors du chargement des cours:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleUnenroll = async (courseId) => {
        if (!window.confirm('Êtes-vous sûr de vouloir vous désinscrire ?')) return;
        try {
            await apiService.unenroll(studentId, courseId, token);
            await loadCourses();
            // Notifier le composant parent pour recharger "Cours Disponibles"
            if (onUnenrollSuccess) {
                onUnenrollSuccess();
            }
        } catch (err) {
            alert('Erreur lors de la désinscription: ' + err.message);
        }
    };

    if (loading) {
        return <div className="text-center"><div className="spinner-border text-primary"></div></div>;
    }

    if (courses.length === 0) {
        return <div className="alert alert-info"><i className="bi bi-info-circle me-2"></i>Vous n'êtes inscrit à aucun cours.</div>;
    }

    return (
        <div className="row">
            {courses.map(course => (
                <CourseCard
                    key={course.id}
                    course={course}
                    enrolled={true}
                    onUnenroll={handleUnenroll}
                    token={token}
                />
            ))}
        </div>
    );
}

// Available Courses Tab
function AvailableCourses({ studentId, token, onEnrollSuccess, refreshKey }) {
    const [courses, setCourses] = useState([]);
    const [enrolledIds, setEnrolledIds] = useState([]);
    const [loading, setLoading] = useState(true);
    const [enrollingCourseId, setEnrollingCourseId] = useState(null);

    useEffect(() => {
        loadData();
    }, [refreshKey, studentId, token]); // Recharger quand refreshKey change, ou quand les props changent

    const loadData = async () => {
        try {
            const [allCourses, enrollments] = await Promise.all([
                apiService.getAllCourses(token),
                apiService.getEnrollments(studentId, token)
            ]);
            // Extraire les IDs des cours inscrits (supporte les deux formats)
            const enrolled = enrollments
                .map(e => {
                    // Utiliser l'objet cours si disponible, sinon coursId
                    if (e.cours && e.cours.id) {
                        return e.cours.id;
                    }
                    return e.coursId;
                })
                .filter(id => id != null);
            setEnrolledIds(enrolled);
            const availableCourses = allCourses.filter(c => !enrolled.includes(c.id));
            setCourses(availableCourses);
            console.log('Cours Disponibles rechargés:', availableCourses.length, 'cours disponibles,', enrolled.length, 'inscrits');
            console.log('IDs des cours inscrits:', enrolled);
            console.log('IDs de tous les cours:', allCourses.map(c => c.id));
        } catch (err) {
            console.error('Erreur lors du chargement des cours disponibles:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleEnroll = async (courseId) => {
        // Empêcher les clics multiples
        if (enrollingCourseId === courseId) {
            return;
        }
        
        setEnrollingCourseId(courseId);
        try {
            await apiService.enroll(studentId, courseId, token);
            
            // Notifier le composant parent IMMÉDIATEMENT pour recharger "Mes Cours"
            if (onEnrollSuccess) {
                onEnrollSuccess();
            }
            
            // Recharger les données pour retirer le cours de "Cours Disponibles"
            await loadData();
            
            // Petit délai pour laisser le temps au rechargement de se faire
            setTimeout(() => {
                alert('Inscription réussie ! Le cours a été ajouté à "Mes Cours".');
            }, 100);
        } catch (err) {
            alert('Erreur: ' + err.message);
        } finally {
            setEnrollingCourseId(null);
        }
    };

    if (loading) {
        return <div className="text-center"><div className="spinner-border text-primary"></div></div>;
    }

    if (courses.length === 0) {
        return <div className="alert alert-info"><i className="bi bi-info-circle me-2"></i>Tous les cours disponibles sont dans votre liste.</div>;
    }

    return (
        <div className="row">
            {courses.map(course => (
                <CourseCard
                    key={course.id}
                    course={course}
                    enrolled={false}
                    onEnroll={handleEnroll}
                    isEnrolling={enrollingCourseId === course.id}
                />
            ))}
        </div>
    );
}

// Grades Tab
function Grades({ studentId, token }) {
    const [grades, setGrades] = useState([]);
    const [average, setAverage] = useState(0);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (studentId) {
            loadGrades();
            loadAverage();
        } else {
            console.error('studentId est null ou undefined dans Grades');
            setLoading(false);
        }
    }, [studentId]);

    const loadGrades = async () => {
        setLoading(true);
        try {
            console.log('Chargement des notes pour studentId:', studentId);
            // Utiliser l'endpoint spécifique pour récupérer les notes de l'étudiant
            const studentGrades = await apiService.getGradesByStudent(studentId, token);
            console.log('Notes chargées:', studentGrades.length, studentGrades);
            setGrades(studentGrades || []);
        } catch (err) {
            console.error('Erreur lors du chargement des notes:', err);
            console.error('Détails de l\'erreur:', err.message, err.stack);
            setGrades([]);
        } finally {
            setLoading(false);
        }
    };

    const loadAverage = async () => {
        try {
            const avg = await apiService.getStudentAverage(studentId, token);
            setAverage(avg);
        } catch (err) {
            console.error('Erreur lors du calcul de la moyenne:', err);
            // Fallback: calculer côté client si l'API échoue
            if (grades.length > 0) {
                const calculatedAvg = grades.reduce((sum, g) => sum + g.valeur, 0) / grades.length;
                setAverage(calculatedAvg);
            }
        }
    };

    useEffect(() => {
        if (grades.length > 0 && average === 0) {
            // Recalculer si les notes sont chargées mais pas la moyenne
            loadAverage();
        }
    }, [grades]);

    if (loading) {
        return <div className="text-center"><div className="spinner-border text-primary"></div></div>;
    }

    if (grades.length === 0) {
        return (
            <div className="alert alert-info">
                <i className="bi bi-info-circle me-2"></i>
                Aucune note disponible pour le moment. Les notes seront affichées ici une fois qu'elles auront été attribuées par vos formateurs.
            </div>
        );
    }

    return (
        <>
            <div className="card mb-4">
                <div className="card-body">
                    <h5>Moyenne générale</h5>
                    <h2 className="text-primary">{average.toFixed(2)}/20</h2>
                </div>
            </div>
            <table className="table table-striped">
                <thead>
                    <tr>
                        <th>Cours</th>
                        <th>Note</th>
                        <th>Commentaire</th>
                        <th>Date</th>
                    </tr>
                </thead>
                <tbody>
                    {grades.map(grade => (
                        <tr key={grade.id}>
                            <td>{grade.coursTitre || grade.cours?.titre || 'N/A'}</td>
                            <td><strong className={grade.valeur >= 10 ? 'text-success' : 'text-danger'}>{grade.valeur}/20</strong></td>
                            <td>{grade.commentaire || '-'}</td>
                            <td>{grade.dateAttribution ? new Date(grade.dateAttribution).toLocaleDateString('fr-FR') : '-'}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </>
    );
}

// Calendar Component
function Calendar({ studentId, token }) {
    const [currentDate, setCurrentDate] = useState(new Date());
    const [schedules, setSchedules] = useState([]);
    const [loading, setLoading] = useState(false);
    const [selectedCourse, setSelectedCourse] = useState(null);
    const [courses, setCourses] = useState([]);
    const [selectedDay, setSelectedDay] = useState(null);
    const [daySchedules, setDaySchedules] = useState([]);

    useEffect(() => {
        loadSchedules();
        loadCourses();
    }, [studentId, token]);

    useEffect(() => {
        if (selectedDay) {
            const dayStr = formatDateToString(selectedDay);
            const filtered = getFilteredSchedules();
            const daySchedulesList = filtered.filter(s => {
                if (!s.date) return false;
                const scheduleDateStr = parseScheduleDate(s.date);
                return scheduleDateStr === dayStr;
            });
            setDaySchedules(daySchedulesList);
        }
    }, [selectedDay, schedules, selectedCourse]);

    const loadCourses = async () => {
        try {
            const enrollments = await apiService.getEnrollments(studentId, token);
            const courseList = enrollments
                .filter(e => (e.cours && e.cours.id) || e.coursId)
                .map(e => e.cours && e.cours.id ? e.cours : {
                    id: e.coursId,
                    titre: e.coursTitre,
                    code: e.coursCode
                });
            setCourses(courseList);
        } catch (err) {
            console.error('Erreur lors du chargement des cours:', err);
        }
    };

    const loadSchedules = async () => {
        setLoading(true);
        try {
            const data = await apiService.getAllSchedules(studentId, token);
            setSchedules(data);
        } catch (err) {
            console.error('Erreur lors du chargement des séances:', err);
            setSchedules([]);
        } finally {
            setLoading(false);
        }
    };

    const getDaysInMonth = (date) => {
        const year = date.getFullYear();
        const month = date.getMonth();
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();
        // Ajuster pour que lundi soit le premier jour (getDay() retourne 0 pour dimanche)
        let startingDayOfWeek = firstDay.getDay() - 1;
        if (startingDayOfWeek < 0) startingDayOfWeek = 6; // Dimanche devient 6
        
        const days = [];
        // Ajouter les jours vides du début
        for (let i = 0; i < startingDayOfWeek; i++) {
            days.push(null);
        }
        // Ajouter les jours du mois
        for (let day = 1; day <= daysInMonth; day++) {
            days.push(new Date(year, month, day));
        }
        return days;
    };

    const getFilteredSchedules = () => {
        return selectedCourse 
            ? schedules.filter(s => s.cours && (s.cours.id === selectedCourse || s.cours.id === parseInt(selectedCourse)))
            : schedules;
    };

    const formatDateToString = (date) => {
        // Formater une date en YYYY-MM-DD sans conversion timezone
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    };

    const parseScheduleDate = (dateStr) => {
        // Parser une date string en YYYY-MM-DD sans conversion timezone
        if (!dateStr) return null;
        if (typeof dateStr === 'string') {
            // Si c'est déjà au format YYYY-MM-DD, retourner tel quel
            if (/^\d{4}-\d{2}-\d{2}/.test(dateStr)) {
                return dateStr.split('T')[0];
            }
            // Sinon, parser et formater
            const date = new Date(dateStr);
            return formatDateToString(date);
        }
        // Si c'est un objet Date, formater
        if (dateStr instanceof Date) {
            return formatDateToString(dateStr);
        }
        return null;
    };

    const getSchedulesForDay = (day) => {
        if (!day) return [];
        const dayStr = formatDateToString(day);
        const filtered = getFilteredSchedules();
        return filtered.filter(s => {
            if (!s.date) return false;
            const scheduleDateStr = parseScheduleDate(s.date);
            return scheduleDateStr === dayStr;
        });
    };

    const formatTime = (timeStr) => {
        if (!timeStr) return '';
        const parts = timeStr.split(':');
        return `${parts[0]}:${parts[1]}`;
    };

    const monthNames = ['janvier', 'février', 'mars', 'avril', 'mai', 'juin', 
                       'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre'];
    const dayNames = ['Lu', 'Ma', 'Me', 'Je', 'Ve', 'Sa', 'Di'];

    const prevMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
    };

    const nextMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
    };

    const isToday = (day) => {
        if (!day) return false;
        const today = new Date();
        return day.toDateString() === today.toDateString();
    };

    const isWeekend = (day) => {
        if (!day) return false;
        const dayOfWeek = day.getDay();
        return dayOfWeek === 0 || dayOfWeek === 6;
    };

    const days = getDaysInMonth(currentDate);

    return (
        <div>
            {/* Header avec filtre et navigation */}
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div className="d-flex align-items-center gap-2">
                    <select 
                        className="form-select" 
                        style={{width: '200px', fontSize: '0.9rem'}}
                        value={selectedCourse || ''}
                        onChange={(e) => setSelectedCourse(e.target.value ? parseInt(e.target.value) : null)}
                    >
                        <option value="">Tous les cours</option>
                        {courses.map(course => (
                            <option key={course.id} value={course.id}>
                                {course.titre}
                            </option>
                        ))}
                    </select>
                </div>
                <div className="d-flex align-items-center gap-2">
                    <button 
                        className="btn btn-sm btn-outline-secondary" 
                        onClick={prevMonth}
                        style={{border: 'none', fontSize: '0.9rem'}}
                    >
                        <i className="bi bi-chevron-left"></i> {monthNames[(currentDate.getMonth() - 1 + 12) % 12]}
                    </button>
                    <h4 className="mb-0 mx-3" style={{fontSize: '1.5rem', fontWeight: '500'}}>
                        {monthNames[currentDate.getMonth()]} {currentDate.getFullYear()}
                    </h4>
                    <button 
                        className="btn btn-sm btn-outline-secondary" 
                        onClick={nextMonth}
                        style={{border: 'none', fontSize: '0.9rem'}}
                    >
                        {monthNames[(currentDate.getMonth() + 1) % 12]} <i className="bi bi-chevron-right"></i>
                    </button>
                </div>
            </div>

            {loading ? (
                <div className="text-center"><div className="spinner-border text-primary"></div></div>
            ) : (
                <>
                    {/* Calendrier */}
                    <div className="card shadow-sm">
                        <div className="card-body p-0">
                            <div className="table-responsive">
                                <table className="table table-bordered mb-0" style={{tableLayout: 'fixed', minHeight: '500px'}}>
                                    <thead style={{backgroundColor: '#f8f9fa'}}>
                                        <tr>
                                            {dayNames.map((day, idx) => (
                                                <th 
                                                    key={idx} 
                                                    className="text-center fw-normal" 
                                                    style={{
                                                        width: '14.28%', 
                                                        padding: '12px 8px',
                                                        fontSize: '0.9rem',
                                                        color: '#666',
                                                        borderBottom: '2px solid #dee2e6'
                                                    }}
                                                >
                                                    {day}
                                                </th>
                                            ))}
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {Array.from({length: Math.ceil(days.length / 7)}).map((_, weekIdx) => (
                                            <tr key={weekIdx}>
                                                {Array.from({length: 7}).map((_, dayIdx) => {
                                                    const day = days[weekIdx * 7 + dayIdx];
                                                    const daySchedules = getSchedulesForDay(day);
                                                    const isCurrentDay = isToday(day);
                                                    const isWeekendDay = isWeekend(day);
                                                    
                                                    return (
                                                        <td 
                                                            key={dayIdx}
                                                            className={`text-center align-top ${isWeekendDay ? 'bg-light' : ''}`}
                                                            style={{
                                                                height: '140px',
                                                                padding: '8px 4px',
                                                                cursor: day ? 'pointer' : 'default',
                                                                backgroundColor: isCurrentDay ? '#e3f2fd' : (isWeekendDay ? '#f8f9fa' : 'white'),
                                                                border: isCurrentDay ? '3px solid #2196F3' : '1px solid #dee2e6',
                                                                verticalAlign: 'top',
                                                                position: 'relative'
                                                            }}
                                                            onClick={() => day && setSelectedDay(day)}
                                                            onMouseEnter={(e) => {
                                                                if (day) e.currentTarget.style.backgroundColor = isCurrentDay ? '#bbdefb' : '#f0f0f0';
                                                            }}
                                                            onMouseLeave={(e) => {
                                                                if (day) e.currentTarget.style.backgroundColor = isCurrentDay ? '#e3f2fd' : (isWeekendDay ? '#f8f9fa' : 'white');
                                                            }}
                                                        >
                                                            {day && (
                                                                <>
                                                                    <div 
                                                                        className={`fw-bold mb-1 ${isCurrentDay ? 'text-primary' : ''}`}
                                                                        style={{
                                                                            fontSize: isCurrentDay ? '1.1rem' : '1rem',
                                                                            color: isCurrentDay ? '#1976D2' : (isWeekendDay ? '#999' : '#333')
                                                                        }}
                                                                    >
                                                                        {day.getDate()}
                                                                    </div>
                                                                    <div className="mt-1" style={{fontSize: '0.7rem'}}>
                                                                        {daySchedules.slice(0, 2).map(schedule => (
                                                                            <div 
                                                                                key={schedule.id}
                                                                                className="badge mb-1 w-100 text-start text-truncate"
                                                                                style={{
                                                                                    fontSize: '0.65rem', 
                                                                                    padding: '3px 6px',
                                                                                    backgroundColor: '#2196F3',
                                                                                    color: 'white',
                                                                                    cursor: 'pointer',
                                                                                    display: 'block',
                                                                                    maxWidth: '100%'
                                                                                }}
                                                                                title={`${schedule.cours?.titre || 'N/A'} - ${formatTime(schedule.heureDebut)}-${formatTime(schedule.heureFin)} ${schedule.salle ? '- ' + schedule.salle : ''}`}
                                                                                onClick={(e) => {
                                                                                    e.stopPropagation();
                                                                                    setSelectedDay(day);
                                                                                }}
                                                                            >
                                                                                {formatTime(schedule.heureDebut)} {schedule.cours?.titre?.substring(0, 12) || ''}
                                                                            </div>
                                                                        ))}
                                                                        {daySchedules.length > 2 && (
                                                                            <div 
                                                                                className="text-muted mt-1" 
                                                                                style={{fontSize: '0.65rem', cursor: 'pointer'}}
                                                                                onClick={(e) => {
                                                                                    e.stopPropagation();
                                                                                    setSelectedDay(day);
                                                                                }}
                                                                            >
                                                                                +{daySchedules.length - 2} autre(s)
                                                                            </div>
                                                                        )}
                                                                    </div>
                                                                </>
                                                            )}
                                                        </td>
                                                    );
                                                })}
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    {/* Détails des séances pour le jour sélectionné */}
                    {selectedDay && (
                        <div className="card mt-4">
                            <div className="card-header d-flex justify-content-between align-items-center">
                                <h5 className="mb-0">
                                    Séances du {selectedDay.getDate()} {monthNames[selectedDay.getMonth()]} {selectedDay.getFullYear()}
                                </h5>
                                <button className="btn btn-sm btn-outline-secondary" onClick={() => setSelectedDay(null)}>
                                    <i className="bi bi-x"></i>
                                </button>
                            </div>
                            <div className="card-body">
                                {daySchedules.length === 0 ? (
                                    <div className="alert alert-info mb-0">Aucune séance prévue pour ce jour</div>
                                ) : (
                                    <div className="list-group">
                                        {daySchedules.map(schedule => (
                                            <div key={schedule.id} className="list-group-item">
                                                <div className="d-flex w-100 justify-content-between align-items-start">
                                                    <div className="flex-grow-1">
                                                        <h6 className="mb-1">
                                                            <i className="bi bi-book me-2"></i>
                                                            {schedule.cours?.titre || 'N/A'}
                                                        </h6>
                                                        <p className="mb-1">
                                                            <i className="bi bi-clock me-2"></i>
                                                            {formatTime(schedule.heureDebut)} - {formatTime(schedule.heureFin)}
                                                        </p>
                                                        {schedule.salle && (
                                                            <p className="mb-0 text-muted">
                                                                <i className="bi bi-geo-alt me-2"></i>
                                                                {schedule.salle}
                                                            </p>
                                                        )}
                                                    </div>
                                                    <span className={`badge ${
                                                        schedule.status === 'APPROVED' ? 'bg-success' :
                                                        schedule.status === 'REJECTED' ? 'bg-danger' :
                                                        'bg-warning'
                                                    }`}>
                                                        {schedule.status === 'APPROVED' ? 'Approuvé' :
                                                         schedule.status === 'REJECTED' ? 'Rejeté' :
                                                         'En attente'}
                                                    </span>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}

// Schedule Tab (alias pour compatibilité)
function Schedule({ studentId, token }) {
    return <Calendar studentId={studentId} token={token} />;
}

// Main App Component
function App() {
    const [token, setToken] = useState(localStorage.getItem('jwt_token'));
    const [user, setUser] = useState(null);
    const [activeTab, setActiveTab] = useState('courses');
    const [refreshKey, setRefreshKey] = useState(0);

    useEffect(() => {
        if (token) {
            verifyToken();
        }
    }, []);
    
    // Fonction pour forcer le rechargement de "Mes Cours" après une inscription
    const handleEnrollSuccess = () => {
        setRefreshKey(prev => prev + 1);
    };
    
    // Fonction pour forcer le rechargement de "Cours Disponibles" après une désinscription
    const handleUnenrollSuccess = () => {
        setRefreshKey(prev => prev + 1);
    };

    const verifyToken = async () => {
        try {
            const userInfo = await apiService.getCurrentUser(token);
            setUser(userInfo);
        } catch (err) {
            localStorage.removeItem('jwt_token');
            setToken(null);
        }
    };

    const handleLogin = (newToken, userInfo) => {
        setToken(newToken);
        setUser(userInfo);
    };

    const handleLogout = () => {
        localStorage.removeItem('jwt_token');
        setToken(null);
        setUser(null);
    };

    if (!token || !user) {
        return (
            <div className="container main-content">
                <Login onLogin={handleLogin} />
            </div>
        );
    }

    const studentId = user.studentId;
    
    // Vérifier que studentId est défini
    if (!studentId) {
        console.error('studentId non défini pour l\'utilisateur:', user);
        return (
            <div className="container main-content">
                <div className="alert alert-danger">
                    <h5>Erreur</h5>
                    <p>Impossible de récupérer l'ID de l'étudiant. Veuillez vous reconnecter.</p>
                    <button className="btn btn-primary" onClick={handleLogout}>Se reconnecter</button>
                </div>
            </div>
        );
    }

    return (
        <>
            <nav className="navbar navbar-expand-lg navbar-dark" style={{background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'}}>
                <div className="container-fluid">
                    <a className="navbar-brand" href="#"><i className="bi bi-mortarboard me-2"></i>Gestion Formation - Étudiant</a>
                    <div className="navbar-nav ms-auto">
                        <span className="navbar-text me-3">Connecté en tant que: {user.username}</span>
                        <button className="btn btn-outline-light btn-sm" onClick={handleLogout}>
                            <i className="bi bi-box-arrow-right me-1"></i>Déconnexion
                        </button>
                    </div>
                </div>
            </nav>

            <div className="container main-content">
                <ul className="nav nav-tabs mb-4">
                    <li className="nav-item">
                        <a
                            className={`nav-link ${activeTab === 'courses' ? 'active' : ''}`}
                            href="#"
                            onClick={(e) => { 
                                e.preventDefault(); 
                                setActiveTab('courses');
                                // Forcer le rechargement de "Mes Cours" quand on clique sur l'onglet
                                setRefreshKey(prev => prev + 1);
                            }}
                        >
                            <i className="bi bi-book me-2"></i>Mes Cours
                        </a>
                    </li>
                    <li className="nav-item">
                        <a
                            className={`nav-link ${activeTab === 'available' ? 'active' : ''}`}
                            href="#"
                            onClick={(e) => { e.preventDefault(); setActiveTab('available'); }}
                        >
                            <i className="bi bi-book-plus me-2"></i>Cours Disponibles
                        </a>
                    </li>
                    <li className="nav-item">
                        <a
                            className={`nav-link ${activeTab === 'grades' ? 'active' : ''}`}
                            href="#"
                            onClick={(e) => { e.preventDefault(); setActiveTab('grades'); }}
                        >
                            <i className="bi bi-clipboard-check me-2"></i>Mes Notes
                        </a>
                    </li>
                    <li className="nav-item">
                        <a
                            className={`nav-link ${activeTab === 'schedule' ? 'active' : ''}`}
                            href="#"
                            onClick={(e) => { e.preventDefault(); setActiveTab('schedule'); }}
                        >
                            <i className="bi bi-calendar me-2"></i>Emploi du temps
                        </a>
                    </li>
                </ul>

                {activeTab === 'courses' && <MyCourses studentId={studentId} token={token} refreshKey={refreshKey} onUnenrollSuccess={handleUnenrollSuccess} isActive={activeTab === 'courses'} />}
                {activeTab === 'available' && <AvailableCourses studentId={studentId} token={token} onEnrollSuccess={handleEnrollSuccess} refreshKey={refreshKey} />}
                {activeTab === 'grades' && <Grades studentId={studentId} token={token} />}
                {activeTab === 'schedule' && <Schedule studentId={studentId} token={token} />}
            </div>
        </>
    );
}

// Render App
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);

