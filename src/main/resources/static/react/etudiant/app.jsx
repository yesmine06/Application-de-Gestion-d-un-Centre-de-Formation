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
        const response = await fetch(`${API_BASE_URL}/inscriptions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ studentId, coursId: courseId })
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Erreur d\'inscription');
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

    async getSchedule(studentId, date, token) {
        const response = await fetch(`${API_BASE_URL}/schedules/student/${studentId}?date=${date}`, {
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
function CourseCard({ course, enrolled, onEnroll, onUnenroll, enrollmentId }) {
    return (
        <div className="col-md-4 mb-3">
            <div className="card">
                <div className="card-body">
                    <h5 className="card-title">{course.titre}</h5>
                    <p className="text-muted"><small>Code: {course.code}</small></p>
                    <p className="card-text">{course.description || 'Pas de description'}</p>
                    <div className="d-flex justify-content-between align-items-center">
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
                            >
                                <i className="bi bi-plus-circle me-1"></i>S'inscrire
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

// My Courses Tab
function MyCourses({ studentId, token }) {
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadCourses();
    }, []);

    const loadCourses = async () => {
        try {
            const enrollments = await apiService.getEnrollments(studentId, token);
            setCourses(enrollments.map(e => e.cours).filter(c => c));
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleUnenroll = async (courseId) => {
        if (!window.confirm('Êtes-vous sûr de vouloir vous désinscrire ?')) return;
        try {
            await apiService.unenroll(studentId, courseId, token);
            await loadCourses();
        } catch (err) {
            alert('Erreur lors de la désinscription');
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
                />
            ))}
        </div>
    );
}

// Available Courses Tab
function AvailableCourses({ studentId, token }) {
    const [courses, setCourses] = useState([]);
    const [enrolledIds, setEnrolledIds] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            const [allCourses, enrollments] = await Promise.all([
                apiService.getAllCourses(token),
                apiService.getEnrollments(studentId, token)
            ]);
            const enrolled = enrollments.map(e => e.cours?.id).filter(id => id);
            setEnrolledIds(enrolled);
            setCourses(allCourses.filter(c => !enrolled.includes(c.id)));
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleEnroll = async (courseId) => {
        try {
            await apiService.enroll(studentId, courseId, token);
            alert('Inscription réussie !');
            await loadData();
        } catch (err) {
            alert('Erreur: ' + err.message);
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
        loadGrades();
        loadAverage();
    }, []);

    const loadGrades = async () => {
        try {
            const allGrades = await apiService.getGrades(token);
            const studentGrades = allGrades.filter(g => g.student?.id === studentId);
            setGrades(studentGrades);
        } catch (err) {
            console.error(err);
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
        return <div className="alert alert-info">Aucune note disponible</div>;
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
                            <td>{grade.cours?.titre || 'N/A'}</td>
                            <td><strong className={grade.valeur >= 10 ? 'text-success' : 'text-danger'}>{grade.valeur}/20</strong></td>
                            <td>{grade.commentaire || '-'}</td>
                            <td>{new Date(grade.dateAttribution).toLocaleDateString('fr-FR')}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </>
    );
}

// Schedule Tab
function Schedule({ studentId, token }) {
    const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
    const [schedules, setSchedules] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (date) loadSchedule();
    }, [date]);

    const loadSchedule = async () => {
        setLoading(true);
        try {
            const data = await apiService.getSchedule(studentId, date, token);
            setSchedules(data);
        } catch (err) {
            console.error(err);
            setSchedules([]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <div className="mb-3">
                <label className="form-label">Sélectionner une date</label>
                <input
                    type="date"
                    className="form-control"
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                />
            </div>
            {loading ? (
                <div className="text-center"><div className="spinner-border text-primary"></div></div>
            ) : schedules.length === 0 ? (
                <div className="alert alert-info">Aucune séance prévue pour cette date</div>
            ) : (
                <div className="list-group">
                    {schedules.map(schedule => (
                        <div key={schedule.id} className="list-group-item">
                            <div className="d-flex w-100 justify-content-between">
                                <h5 className="mb-1">{schedule.cours?.titre || 'N/A'}</h5>
                                <small>{schedule.heureDebut} - {schedule.heureFin}</small>
                            </div>
                            <p className="mb-1">
                                <i className="bi bi-geo-alt me-2"></i>{schedule.salle || 'Salle non spécifiée'}
                            </p>
                        </div>
                    ))}
                </div>
            )}
        </>
    );
}

// Main App Component
function App() {
    const [token, setToken] = useState(localStorage.getItem('jwt_token'));
    const [user, setUser] = useState(null);
    const [activeTab, setActiveTab] = useState('courses');

    useEffect(() => {
        if (token) {
            verifyToken();
        }
    }, []);

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
                            onClick={(e) => { e.preventDefault(); setActiveTab('courses'); }}
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

                {activeTab === 'courses' && <MyCourses studentId={studentId} token={token} />}
                {activeTab === 'available' && <AvailableCourses studentId={studentId} token={token} />}
                {activeTab === 'grades' && <Grades studentId={studentId} token={token} />}
                {activeTab === 'schedule' && <Schedule studentId={studentId} token={token} />}
            </div>
        </>
    );
}

// Render App
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);

