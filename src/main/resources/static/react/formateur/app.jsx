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

    async getCoursesByTrainer(trainerId, token) {
        const response = await fetch(`${API_BASE_URL}/cours/trainer/${trainerId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        return await response.json();
    },

    async getEnrollmentsByCourse(courseId, token) {
        const response = await fetch(`${API_BASE_URL}/inscriptions/course/${courseId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        return await response.json();
    },

    async getAllStudents(token) {
        const response = await fetch(`${API_BASE_URL}/etudiants`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        return await response.json();
    },

    async createGrade(studentId, courseId, valeur, commentaire, token) {
        const response = await fetch(`${API_BASE_URL}/grades/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ studentId, coursId: courseId, valeur, commentaire })
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Erreur lors de l\'enregistrement');
        }
        return await response.json();
    },

    async getGradesByCourse(courseId, token) {
        console.log('Appel API getGradesByCourse:', `${API_BASE_URL}/grades/course/${courseId}`);
        const response = await fetch(`${API_BASE_URL}/grades/course/${courseId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        console.log('Réponse API notes:', response.status, response.statusText);
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Erreur API notes:', errorText);
            throw new Error(`Erreur de chargement: ${response.status} ${response.statusText}`);
        }
        const data = await response.json();
        console.log('Données reçues (notes):', data);
        return data;
    },

    // Course Files API
    async getFilesByCourse(courseId, token) {
        const response = await fetch(`${API_BASE_URL}/course-files/course/${courseId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        return await response.json();
    },

    async uploadFile(courseId, file, description, token) {
        const formData = new FormData();
        formData.append('courseId', courseId);
        formData.append('file', file);
        if (description) formData.append('description', description);
        
        const response = await fetch(`${API_BASE_URL}/course-files/upload`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Erreur lors de l\'upload');
        }
        return await response.json();
    },

    async deleteFile(fileId, token) {
        const response = await fetch(`${API_BASE_URL}/course-files/${fileId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur lors de la suppression');
    },

    async downloadFile(fileId, token) {
        const response = await fetch(`${API_BASE_URL}/course-files/${fileId}/download`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur lors du téléchargement');
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        return url;
    },

    // Schedules API
    async getSchedulesByTrainer(trainerId, token) {
        const response = await fetch(`${API_BASE_URL}/schedules/trainer/${trainerId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        return await response.json();
    },

    async getSchedulesByCourse(courseId, token) {
        const response = await fetch(`${API_BASE_URL}/schedules/course/${courseId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur de chargement');
        return await response.json();
    },

    async createSchedule(schedule, token) {
        const response = await fetch(`${API_BASE_URL}/schedules`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(schedule)
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Erreur lors de la création');
        }
        return await response.json();
    },

    async updateSchedule(scheduleId, schedule, token) {
        const response = await fetch(`${API_BASE_URL}/schedules/${scheduleId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(schedule)
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Erreur lors de la modification');
        }
        return await response.json();
    },

    async deleteSchedule(scheduleId, token) {
        const response = await fetch(`${API_BASE_URL}/schedules/${scheduleId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur lors de la suppression');
    },

    async createCourse(course, token) {
        const response = await fetch(`${API_BASE_URL}/cours/trainer/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(course)
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Erreur lors de la création du cours');
        }
        return await response.json();
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
                    <div className="card-header bg-primary text-white text-center">
                        <h4><i className="bi bi-person-badge me-2"></i>Connexion Formateur</h4>
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
                                className="btn btn-primary w-100"
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
function CourseCard({ course, onSelect }) {
    return (
        <div className="col-md-4 mb-3">
            <div className="card">
                <div className="card-body">
                    <h5 className="card-title">{course.titre}</h5>
                    <p className="text-muted"><small>Code: {course.code}</small></p>
                    <p className="card-text">{course.description || 'Pas de description'}</p>
                    <button
                        className="btn btn-sm btn-primary"
                        onClick={() => onSelect(course)}
                    >
                        <i className="bi bi-eye me-1"></i>Voir les détails
                    </button>
                </div>
            </div>
        </div>
    );
}

// Courses Tab
function Courses({ trainerId, token, onCourseCreated, isActive }) {
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedCourse, setSelectedCourse] = useState(null);
    const [enrollments, setEnrollments] = useState([]);
    const [loadingEnrollments, setLoadingEnrollments] = useState(false);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [formData, setFormData] = useState({
        code: '',
        titre: '',
        description: ''
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [creating, setCreating] = useState(false);

    useEffect(() => {
        loadCourses();
    }, [trainerId, token, isActive]); // Recharger si trainerId, token change ou si l'onglet devient actif

    const loadCourses = async () => {
        try {
            const data = await apiService.getCoursesByTrainer(trainerId, token);
            setCourses(data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleSelectCourse = async (course) => {
        setSelectedCourse(course);
        setLoadingEnrollments(true);
        try {
            const data = await apiService.getEnrollmentsByCourse(course.id, token);
            setEnrollments(data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoadingEnrollments(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleCreateCourse = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setCreating(true);

        try {
            const newCourse = {
                code: formData.code,
                titre: formData.titre,
                description: formData.description || null
            };
            
            await apiService.createCourse(newCourse, token);
            setSuccess('Cours créé avec succès !');
            setFormData({ code: '', titre: '', description: '' });
            setShowCreateForm(false);
            await loadCourses();
            if (onCourseCreated) onCourseCreated();
        } catch (err) {
            setError(err.message || 'Erreur lors de la création du cours');
        } finally {
            setCreating(false);
        }
    };

    if (loading) {
        return <div className="text-center"><div className="spinner-border text-primary"></div></div>;
    }

    if (selectedCourse) {
        return (
            <>
                <button className="btn btn-secondary mb-3" onClick={() => setSelectedCourse(null)}>
                    <i className="bi bi-arrow-left me-2"></i>Retour
                </button>
                <h3>{selectedCourse.titre}</h3>
                <p className="text-muted">Code: {selectedCourse.code}</p>
                {selectedCourse.description && (
                    <p className="mb-4">{selectedCourse.description}</p>
                )}
                <h5 className="mb-4">Étudiants inscrits</h5>
                {loadingEnrollments ? (
                    <div className="text-center"><div className="spinner-border text-primary"></div></div>
                ) : enrollments.length === 0 ? (
                    <div className="alert alert-info">Aucun étudiant inscrit à ce cours</div>
                ) : (
                    <table className="table table-striped">
                        <thead>
                            <tr>
                                <th>Nom</th>
                                <th>Prénom</th>
                                <th>Email</th>
                                <th>Matricule</th>
                                <th>Date d'inscription</th>
                            </tr>
                        </thead>
                        <tbody>
                            {enrollments.map(enrollment => (
                                <tr key={enrollment.id}>
                                    <td>{enrollment.student?.nom || 'N/A'}</td>
                                    <td>{enrollment.student?.prenom || 'N/A'}</td>
                                    <td>{enrollment.student?.email || 'N/A'}</td>
                                    <td>{enrollment.student?.matricule || 'N/A'}</td>
                                    <td>{new Date(enrollment.dateInscription).toLocaleDateString('fr-FR')}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </>
        );
    }

    return (
        <>
            {error && <div className="alert alert-danger">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            <div className="mb-3">
                <button
                    className="btn btn-primary"
                    onClick={() => setShowCreateForm(!showCreateForm)}
                >
                    {showCreateForm ? (
                        <>
                            <i className="bi bi-x-circle me-2"></i>Annuler
                        </>
                    ) : (
                        <>
                            <i className="bi bi-plus-circle me-2"></i>Créer un nouveau cours
                        </>
                    )}
                </button>
            </div>

            {showCreateForm && (
                <div className="card mb-4">
                    <div className="card-header">
                        <h5 className="mb-0">Nouveau cours</h5>
                    </div>
                    <div className="card-body">
                        <form onSubmit={handleCreateCourse}>
                            <div className="row">
                                <div className="col-md-6 mb-3">
                                    <label className="form-label">Code du cours *</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        name="code"
                                        value={formData.code}
                                        onChange={handleInputChange}
                                        placeholder="Ex: JAVA-101"
                                        required
                                        disabled={creating}
                                    />
                                    <small className="text-muted">Code unique pour identifier le cours</small>
                                </div>
                                <div className="col-md-6 mb-3">
                                    <label className="form-label">Titre du cours *</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        name="titre"
                                        value={formData.titre}
                                        onChange={handleInputChange}
                                        placeholder="Ex: Programmation Java"
                                        required
                                        disabled={creating}
                                    />
                                </div>
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Description</label>
                                <textarea
                                    className="form-control"
                                    name="description"
                                    value={formData.description}
                                    onChange={handleInputChange}
                                    rows="3"
                                    placeholder="Description du cours (optionnel)"
                                    disabled={creating}
                                />
                            </div>
                            <div className="d-flex gap-2">
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={creating}
                                >
                                    {creating ? (
                                        <>
                                            <span className="spinner-border spinner-border-sm me-2"></span>
                                            Création en cours...
                                        </>
                                    ) : (
                                        <>
                                            <i className="bi bi-save me-2"></i>Créer le cours
                                        </>
                                    )}
                                </button>
                                <button
                                    type="button"
                                    className="btn btn-secondary"
                                    onClick={() => {
                                        setShowCreateForm(false);
                                        setFormData({ code: '', titre: '', description: '' });
                                        setError('');
                                        setSuccess('');
                                    }}
                                    disabled={creating}
                                >
                                    Annuler
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {loading ? (
                <div className="text-center"><div className="spinner-border text-primary"></div></div>
            ) : courses.length === 0 ? (
                <div className="alert alert-info">
                    <i className="bi bi-info-circle me-2"></i>
                    Aucun cours pour le moment. Créez votre premier cours en cliquant sur le bouton ci-dessus.
                </div>
            ) : (
                <div className="row">
                    {courses.map(course => (
                        <CourseCard
                            key={course.id}
                            course={course}
                            onSelect={handleSelectCourse}
                        />
                    ))}
                </div>
            )}
        </>
    );
}

// Grade Form Component
function GradeForm({ courses, token, onGradeSaved }) {
    const [courseId, setCourseId] = useState('');
    const [studentId, setStudentId] = useState('');
    const [valeur, setValeur] = useState('');
    const [commentaire, setCommentaire] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [enrolledStudents, setEnrolledStudents] = useState([]);
    const [loadingStudents, setLoadingStudents] = useState(false);

    // Charger les étudiants inscrits au cours sélectionné
    useEffect(() => {
        if (courseId) {
            loadEnrolledStudents();
        } else {
            setEnrolledStudents([]);
            setStudentId('');
        }
    }, [courseId]);

    const loadEnrolledStudents = async () => {
        setLoadingStudents(true);
        try {
            const enrollments = await apiService.getEnrollmentsByCourse(parseInt(courseId), token);
            // Le DTO peut retourner soit e.student (objet complet) soit studentId/studentName/studentMatricule
            const students = enrollments
                .filter(e => (e.student && e.student.id) || e.studentId) // Filtrer les inscriptions valides
                .map(e => {
                    // Utiliser l'objet student si disponible, sinon créer à partir des propriétés
                    if (e.student && e.student.id) {
                        return e.student;
                    } else {
                        return {
                            id: e.studentId,
                            nom: e.studentName ? e.studentName.split(' ').slice(-1)[0] : '',
                            prenom: e.studentName ? e.studentName.split(' ').slice(0, -1).join(' ') : '',
                            matricule: e.studentMatricule,
                            username: '',
                            email: ''
                        };
                    }
                });
            setEnrolledStudents(students);
            console.log('Étudiants chargés pour le cours:', courseId, students.length, 'étudiants', students);
        } catch (err) {
            console.error('Erreur lors du chargement des étudiants:', err);
            setEnrolledStudents([]);
        } finally {
            setLoadingStudents(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        try {
            await apiService.createGrade(
                parseInt(studentId),
                parseInt(courseId),
                parseFloat(valeur),
                commentaire,
                token
            );
            setSuccess('Note enregistrée avec succès !');
            setCourseId('');
            setStudentId('');
            setValeur('');
            setCommentaire('');
            if (onGradeSaved) onGradeSaved();
        } catch (err) {
            setError(err.message || 'Erreur lors de l\'enregistrement');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="card mb-4">
            <div className="card-body">
                {error && <div className="alert alert-danger">{error}</div>}
                {success && <div className="alert alert-success">{success}</div>}
                <form onSubmit={handleSubmit}>
                    <div className="row">
                        <div className="col-md-4">
                            <label className="form-label">Cours</label>
                            <select
                                className="form-select"
                                value={courseId}
                                onChange={(e) => {
                                    setCourseId(e.target.value);
                                    setStudentId('');
                                }}
                                required
                            >
                                <option value="">Sélectionner un cours</option>
                                {courses.map(course => (
                                    <option key={course.id} value={course.id}>
                                        {course.titre}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="col-md-4">
                            <label className="form-label">Étudiant</label>
                            <select
                                className="form-select"
                                value={studentId}
                                onChange={(e) => setStudentId(e.target.value)}
                                required
                                disabled={!courseId || loadingStudents}
                            >
                                <option value="">
                                    {loadingStudents ? 'Chargement...' : 'Sélectionner un étudiant'}
                                </option>
                                {enrolledStudents.map(student => (
                                    <option key={student.id} value={student.id}>
                                        {student.nom} {student.prenom} ({student.matricule})
                                    </option>
                                ))}
                            </select>
                            {courseId && !loadingStudents && enrolledStudents.length === 0 && (
                                <small className="text-muted">Aucun étudiant inscrit à ce cours</small>
                            )}
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Note (/20)</label>
                            <input
                                type="number"
                                className="form-control"
                                value={valeur}
                                onChange={(e) => setValeur(e.target.value)}
                                min="0"
                                max="20"
                                step="0.5"
                                required
                            />
                        </div>
                        <div className="col-md-2 d-flex align-items-end">
                            <button
                                type="submit"
                                className="btn btn-primary w-100"
                                disabled={loading}
                            >
                                {loading ? '...' : <><i className="bi bi-save me-1"></i>Enregistrer</>}
                            </button>
                        </div>
                    </div>
                    <div className="row mt-2">
                        <div className="col-md-12">
                            <label className="form-label">Commentaire</label>
                            <textarea
                                className="form-control"
                                value={commentaire}
                                onChange={(e) => setCommentaire(e.target.value)}
                                rows="2"
                            />
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}

// Grades Tab
function Grades({ trainerId, courses, token, onGradeSaved }) {
    const [selectedCourse, setSelectedCourse] = useState(null);
    const [grades, setGrades] = useState([]);
    const [loading, setLoading] = useState(false);
    const [loadingGrades, setLoadingGrades] = useState(false);

    useEffect(() => {
        if (selectedCourse) {
            loadGrades();
        }
    }, [selectedCourse]);
    
    // Réinitialiser la sélection si le cours sélectionné n'existe plus dans la liste
    useEffect(() => {
        if (selectedCourse && !courses.find(c => c.id === selectedCourse.id)) {
            setSelectedCourse(null);
        }
    }, [courses]);

    const loadGrades = async () => {
        if (!selectedCourse) return;
        setLoadingGrades(true);
        try {
            const data = await apiService.getGradesByCourse(selectedCourse.id, token);
            console.log('Notes chargées pour le cours:', selectedCourse.id, data);
            console.log('Première note (exemple):', data && data.length > 0 ? data[0] : 'Aucune note');
            setGrades(data || []);
        } catch (err) {
            console.error('Erreur lors du chargement des notes:', err);
            setGrades([]);
        } finally {
            setLoadingGrades(false);
        }
    };

    const handleGradeSaved = () => {
        if (selectedCourse) {
            loadGrades();
        }
        if (onGradeSaved) onGradeSaved();
    };

    return (
        <>
            <GradeForm
                courses={courses}
                token={token}
                onGradeSaved={handleGradeSaved}
            />

            <div className="mb-3">
                <label className="form-label">Voir les notes par cours</label>
                <select
                    className="form-select"
                    value={selectedCourse?.id || ''}
                    onChange={(e) => {
                        const course = courses.find(c => c.id === parseInt(e.target.value));
                        setSelectedCourse(course || null);
                    }}
                >
                    <option value="">Sélectionner un cours</option>
                    {courses.map(course => (
                        <option key={course.id} value={course.id}>
                            {course.titre}
                        </option>
                    ))}
                </select>
            </div>

            {selectedCourse && (
                <>
                    <h5 className="mb-3">Notes pour: {selectedCourse.titre}</h5>
                    {loadingGrades ? (
                        <div className="text-center"><div className="spinner-border text-primary"></div></div>
                    ) : grades.length === 0 ? (
                        <div className="alert alert-info">Aucune note enregistrée pour ce cours</div>
                    ) : (
                        <table className="table table-striped">
                            <thead>
                                <tr>
                                    <th>Étudiant</th>
                                    <th>Note</th>
                                    <th>Commentaire</th>
                                    <th>Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                {grades.map(grade => (
                                    <tr key={grade.id}>
                                        <td>
                                            {grade.studentName || (grade.student ? `${grade.student.prenom || ''} ${grade.student.nom || ''}`.trim() : 'N/A')}
                                            {(grade.studentMatricule || grade.student?.matricule) && (
                                                <>
                                                    <br />
                                                    <small className="text-muted">{grade.studentMatricule || grade.student?.matricule}</small>
                                                </>
                                            )}
                                        </td>
                                        <td>
                                            <strong className={grade.valeur >= 10 ? 'text-success' : 'text-danger'}>
                                                {grade.valeur}/20
                                            </strong>
                                        </td>
                                        <td>{grade.commentaire || '-'}</td>
                                        <td>{grade.dateAttribution ? new Date(grade.dateAttribution).toLocaleDateString('fr-FR') : '-'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </>
            )}
        </>
    );
}

// Course Files Tab
function CourseFiles({ courses, token }) {
    const [selectedCourse, setSelectedCourse] = useState(null);
    const [files, setFiles] = useState([]);
    const [loading, setLoading] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [uploadFile, setUploadFile] = useState(null);
    const [uploadDescription, setUploadDescription] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        if (selectedCourse) {
            loadFiles();
        }
    }, [selectedCourse]);
    
    // Réinitialiser la sélection si le cours sélectionné n'existe plus dans la liste
    useEffect(() => {
        if (selectedCourse && !courses.find(c => c.id === selectedCourse.id)) {
            setSelectedCourse(null);
        }
    }, [courses]);

    const loadFiles = async () => {
        if (!selectedCourse) return;
        setLoading(true);
        try {
            const data = await apiService.getFilesByCourse(selectedCourse.id, token);
            setFiles(data);
        } catch (err) {
            console.error(err);
            setError('Erreur lors du chargement des fichiers');
        } finally {
            setLoading(false);
        }
    };

    const handleFileSelect = (e) => {
        const file = e.target.files[0];
        if (file) {
            setUploadFile(file);
        }
    };

    const handleUpload = async (e) => {
        e.preventDefault();
        if (!selectedCourse || !uploadFile) {
            setError('Veuillez sélectionner un cours et un fichier');
            return;
        }

        setUploading(true);
        setError('');
        setSuccess('');

        try {
            await apiService.uploadFile(selectedCourse.id, uploadFile, uploadDescription, token);
            setSuccess('Fichier uploadé avec succès !');
            setUploadFile(null);
            setUploadDescription('');
            document.getElementById('fileInput').value = '';
            await loadFiles();
        } catch (err) {
            setError(err.message || 'Erreur lors de l\'upload');
        } finally {
            setUploading(false);
        }
    };

    const handleDelete = async (fileId) => {
        if (!window.confirm('Êtes-vous sûr de vouloir supprimer ce fichier ?')) return;

        try {
            await apiService.deleteFile(fileId, token);
            setSuccess('Fichier supprimé avec succès !');
            await loadFiles();
        } catch (err) {
            setError(err.message || 'Erreur lors de la suppression');
        }
    };

    const handleDownload = async (fileId, fileName) => {
        try {
            const url = await apiService.downloadFile(fileId, token);
            const a = document.createElement('a');
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        } catch (err) {
            setError(err.message || 'Erreur lors du téléchargement');
        }
    };

    const formatFileSize = (bytes) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    };

    return (
        <>
            <div className="mb-3">
                <label className="form-label">Sélectionner un cours</label>
                <select
                    className="form-select"
                    value={selectedCourse?.id || ''}
                    onChange={(e) => {
                        const course = courses.find(c => c.id === parseInt(e.target.value));
                        setSelectedCourse(course || null);
                    }}
                >
                    <option value="">Sélectionner un cours</option>
                    {courses.map(course => (
                        <option key={course.id} value={course.id}>
                            {course.titre}
                        </option>
                    ))}
                </select>
            </div>

            {error && <div className="alert alert-danger">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            {selectedCourse && (
                <>
                    <div className="card mb-4">
                        <div className="card-header">
                            <h5 className="mb-0">Uploader un fichier - {selectedCourse.titre}</h5>
                        </div>
                        <div className="card-body">
                            <form onSubmit={handleUpload}>
                                <div className="mb-3">
                                    <label className="form-label">Fichier</label>
                                    <input
                                        id="fileInput"
                                        type="file"
                                        className="form-control"
                                        onChange={handleFileSelect}
                                        required
                                        disabled={uploading}
                                    />
                                    <small className="text-muted">Formats acceptés: PDF, Word, Excel, PowerPoint</small>
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Description (optionnel)</label>
                                    <textarea
                                        className="form-control"
                                        value={uploadDescription}
                                        onChange={(e) => setUploadDescription(e.target.value)}
                                        rows="2"
                                        disabled={uploading}
                                    />
                                </div>
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={uploading || !uploadFile}
                                >
                                    {uploading ? (
                                        <>
                                            <span className="spinner-border spinner-border-sm me-2"></span>
                                            Upload en cours...
                                        </>
                                    ) : (
                                        <>
                                            <i className="bi bi-upload me-2"></i>Uploader
                                        </>
                                    )}
                                </button>
                            </form>
                        </div>
                    </div>

                    <h5 className="mb-3">Fichiers du cours</h5>
                    {loading ? (
                        <div className="text-center"><div className="spinner-border text-primary"></div></div>
                    ) : files.length === 0 ? (
                        <div className="alert alert-info">Aucun fichier pour ce cours</div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table table-striped">
                                <thead>
                                    <tr>
                                        <th>Nom du fichier</th>
                                        <th>Description</th>
                                        <th>Taille</th>
                                        <th>Date d'upload</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {files.map(file => (
                                        <tr key={file.id}>
                                            <td>{file.originalFileName}</td>
                                            <td>{file.description || '-'}</td>
                                            <td>{formatFileSize(file.fileSize)}</td>
                                            <td>{new Date(file.uploadDate).toLocaleDateString('fr-FR')}</td>
                                            <td>
                                                <button
                                                    className="btn btn-sm btn-primary me-2"
                                                    onClick={() => handleDownload(file.id, file.originalFileName)}
                                                >
                                                    <i className="bi bi-download me-1"></i>Télécharger
                                                </button>
                                                <button
                                                    className="btn btn-sm btn-danger"
                                                    onClick={() => handleDelete(file.id)}
                                                >
                                                    <i className="bi bi-trash me-1"></i>Supprimer
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </>
            )}

            {!selectedCourse && (
                <div className="alert alert-info">
                    <i className="bi bi-info-circle me-2"></i>Sélectionnez un cours pour gérer ses fichiers
                </div>
            )}
        </>
    );
}

// Schedules Tab
function Schedules({ trainerId, courses, token }) {
    const [schedules, setSchedules] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editingSchedule, setEditingSchedule] = useState(null);
    const [formData, setFormData] = useState({
        coursId: '',
        date: '',
        heureDebut: '',
        heureFin: '',
        salle: ''
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        loadSchedules();
    }, []);

    const loadSchedules = async () => {
        setLoading(true);
        try {
            const data = await apiService.getSchedulesByTrainer(trainerId, token);
            setSchedules(data);
        } catch (err) {
            console.error(err);
            setError('Erreur lors du chargement des séances');
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        const scheduleData = {
            cours: { id: parseInt(formData.coursId) },
            date: formData.date,
            heureDebut: formData.heureDebut,
            heureFin: formData.heureFin,
            salle: formData.salle || null,
            status: 'PENDING'
        };

        try {
            if (editingSchedule) {
                await apiService.updateSchedule(editingSchedule.id, scheduleData, token);
                setSuccess('Séance modifiée avec succès !');
            } else {
                await apiService.createSchedule(scheduleData, token);
                setSuccess('Séance créée avec succès ! Elle est en attente de validation par l\'administrateur.');
            }
            resetForm();
            await loadSchedules();
        } catch (err) {
            setError(err.message || 'Erreur lors de l\'enregistrement');
        }
    };

    const handleEdit = (schedule) => {
        setEditingSchedule(schedule);
        setFormData({
            coursId: schedule.cours?.id || '',
            date: schedule.date || '',
            heureDebut: schedule.heureDebut || '',
            heureFin: schedule.heureFin || '',
            salle: schedule.salle || ''
        });
        setShowForm(true);
    };

    const handleDelete = async (scheduleId) => {
        if (!window.confirm('Êtes-vous sûr de vouloir supprimer cette séance ?')) return;

        try {
            await apiService.deleteSchedule(scheduleId, token);
            setSuccess('Séance supprimée avec succès !');
            await loadSchedules();
        } catch (err) {
            setError(err.message || 'Erreur lors de la suppression');
        }
    };

    const resetForm = () => {
        setFormData({
            coursId: '',
            date: '',
            heureDebut: '',
            heureFin: '',
            salle: ''
        });
        setEditingSchedule(null);
        setShowForm(false);
    };

    const getStatusBadge = (status) => {
        const badges = {
            'PENDING': 'warning',
            'APPROVED': 'success',
            'REJECTED': 'danger'
        };
        const labels = {
            'PENDING': 'En attente',
            'APPROVED': 'Approuvée',
            'REJECTED': 'Rejetée'
        };
        return (
            <span className={`badge bg-${badges[status] || 'secondary'}`}>
                {labels[status] || status}
            </span>
        );
    };

    return (
        <>
            {error && <div className="alert alert-danger">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            <div className="mb-3">
                <button
                    className="btn btn-primary"
                    onClick={() => setShowForm(!showForm)}
                >
                    {showForm ? (
                        <>
                            <i className="bi bi-x-circle me-2"></i>Annuler
                        </>
                    ) : (
                        <>
                            <i className="bi bi-plus-circle me-2"></i>Nouvelle séance
                        </>
                    )}
                </button>
            </div>

            {showForm && (
                <div className="card mb-4">
                    <div className="card-header">
                        <h5 className="mb-0">{editingSchedule ? 'Modifier la séance' : 'Nouvelle séance'}</h5>
                    </div>
                    <div className="card-body">
                        <form onSubmit={handleSubmit}>
                            <div className="row">
                                <div className="col-md-6 mb-3">
                                    <label className="form-label">Cours *</label>
                                    <select
                                        className="form-select"
                                        name="coursId"
                                        value={formData.coursId}
                                        onChange={handleInputChange}
                                        required
                                    >
                                        <option value="">Sélectionner un cours</option>
                                        {courses.map(course => (
                                            <option key={course.id} value={course.id}>
                                                {course.titre}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                <div className="col-md-6 mb-3">
                                    <label className="form-label">Date *</label>
                                    <input
                                        type="date"
                                        className="form-control"
                                        name="date"
                                        value={formData.date}
                                        onChange={handleInputChange}
                                        required
                                    />
                                </div>
                            </div>
                            <div className="row">
                                <div className="col-md-4 mb-3">
                                    <label className="form-label">Heure de début *</label>
                                    <input
                                        type="time"
                                        className="form-control"
                                        name="heureDebut"
                                        value={formData.heureDebut}
                                        onChange={handleInputChange}
                                        required
                                    />
                                </div>
                                <div className="col-md-4 mb-3">
                                    <label className="form-label">Heure de fin *</label>
                                    <input
                                        type="time"
                                        className="form-control"
                                        name="heureFin"
                                        value={formData.heureFin}
                                        onChange={handleInputChange}
                                        required
                                    />
                                </div>
                                <div className="col-md-4 mb-3">
                                    <label className="form-label">Salle</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        name="salle"
                                        value={formData.salle}
                                        onChange={handleInputChange}
                                        placeholder="Ex: A101"
                                    />
                                </div>
                            </div>
                            <div className="d-flex gap-2">
                                <button type="submit" className="btn btn-primary">
                                    <i className="bi bi-save me-2"></i>
                                    {editingSchedule ? 'Modifier' : 'Créer'}
                                </button>
                                {editingSchedule && (
                                    <button type="button" className="btn btn-secondary" onClick={resetForm}>
                                        Annuler
                                    </button>
                                )}
                            </div>
                        </form>
                    </div>
                </div>
            )}

            <h5 className="mb-3">Mes séances planifiées</h5>
            {loading ? (
                <div className="text-center"><div className="spinner-border text-primary"></div></div>
            ) : schedules.length === 0 ? (
                <div className="alert alert-info">Aucune séance planifiée</div>
            ) : (
                <div className="table-responsive">
                    <table className="table table-striped">
                        <thead>
                            <tr>
                                <th>Cours</th>
                                <th>Date</th>
                                <th>Heure</th>
                                <th>Salle</th>
                                <th>Statut</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {schedules.map(schedule => (
                                <tr key={schedule.id}>
                                    <td>{schedule.cours?.titre || 'N/A'}</td>
                                    <td>{new Date(schedule.date).toLocaleDateString('fr-FR')}</td>
                                    <td>{schedule.heureDebut} - {schedule.heureFin}</td>
                                    <td>{schedule.salle || '-'}</td>
                                    <td>{getStatusBadge(schedule.status)}</td>
                                    <td>
                                        <button
                                            className="btn btn-sm btn-primary me-2"
                                            onClick={() => handleEdit(schedule)}
                                        >
                                            <i className="bi bi-pencil me-1"></i>Modifier
                                        </button>
                                        <button
                                            className="btn btn-sm btn-danger"
                                            onClick={() => handleDelete(schedule.id)}
                                        >
                                            <i className="bi bi-trash me-1"></i>Supprimer
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
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
    const [courses, setCourses] = useState([]);
    const [refreshKey, setRefreshKey] = useState(0); // Clé de rafraîchissement

    useEffect(() => {
        if (token) {
            verifyToken();
        }
    }, []);

    useEffect(() => {
        if (user && user.trainerId) {
            loadCourses();
        }
    }, [user, refreshKey]); // Recharger quand refreshKey change

    const verifyToken = async () => {
        try {
            const userInfo = await apiService.getCurrentUser(token);
            setUser(userInfo);
        } catch (err) {
            localStorage.removeItem('jwt_token');
            setToken(null);
        }
    };

    const loadCourses = async () => {
        try {
            if (user && user.trainerId) {
                const data = await apiService.getCoursesByTrainer(user.trainerId, token);
                setCourses(data);
                console.log('Cours du formateur rechargés:', data.length, 'cours');
            }
        } catch (err) {
            console.error('Erreur lors du chargement des cours:', err);
        }
    };
    
    const handleCourseCreated = () => {
        // Incrémenter refreshKey pour forcer le rechargement
        setRefreshKey(prev => prev + 1);
        // Recharger immédiatement les cours
        loadCourses();
    };

    const handleLogin = (newToken, userInfo) => {
        setToken(newToken);
        setUser(userInfo);
    };

    const handleLogout = () => {
        localStorage.removeItem('jwt_token');
        setToken(null);
        setUser(null);
        setCourses([]);
    };

    if (!token || !user) {
        return (
            <div className="container main-content">
                <Login onLogin={handleLogin} />
            </div>
        );
    }

    const trainerId = user.trainerId;

    return (
        <>
            <nav className="navbar navbar-expand-lg navbar-dark" style={{background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'}}>
                <div className="container-fluid">
                    <a className="navbar-brand" href="#"><i className="bi bi-person-badge me-2"></i>Gestion Formation - Formateur</a>
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
                            className={`nav-link ${activeTab === 'grades' ? 'active' : ''}`}
                            href="#"
                            onClick={(e) => { e.preventDefault(); setActiveTab('grades'); }}
                        >
                            <i className="bi bi-clipboard-check me-2"></i>Gérer les Notes
                        </a>
                    </li>
                    <li className="nav-item">
                        <a
                            className={`nav-link ${activeTab === 'files' ? 'active' : ''}`}
                            href="#"
                            onClick={(e) => { e.preventDefault(); setActiveTab('files'); }}
                        >
                            <i className="bi bi-folder me-2"></i>Fichiers
                        </a>
                    </li>
                    <li className="nav-item">
                        <a
                            className={`nav-link ${activeTab === 'schedules' ? 'active' : ''}`}
                            href="#"
                            onClick={(e) => { e.preventDefault(); setActiveTab('schedules'); }}
                        >
                            <i className="bi bi-calendar me-2"></i>Planification
                        </a>
                    </li>
                </ul>

                {activeTab === 'courses' && <Courses trainerId={trainerId} token={token} onCourseCreated={handleCourseCreated} isActive={activeTab === 'courses'} />}
                {activeTab === 'grades' && <Grades trainerId={trainerId} courses={courses} token={token} />}
                {activeTab === 'files' && <CourseFiles courses={courses} token={token} />}
                {activeTab === 'schedules' && <Schedules trainerId={trainerId} courses={courses} token={token} />}
            </div>
        </>
    );
}

// Render App
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);

