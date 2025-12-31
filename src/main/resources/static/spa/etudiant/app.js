const API_BASE_URL = 'http://localhost:8080/api';
let token = localStorage.getItem('jwt_token');
let currentUser = null;
let currentStudentId = null;

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    if (token) {
        verifyTokenAndLoadApp();
    } else {
        showLoginPage();
    }

    document.getElementById('login-form').addEventListener('submit', handleLogin);
    
    // Set today's date as default for schedule
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('schedule-date').value = today;
});

async function verifyTokenAndLoadApp() {
    try {
        // Verify token by getting current user
        const response = await fetch(`${API_BASE_URL}/user/current`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            currentUser = await response.json();
            if (currentUser.type === 'STUDENT' && currentUser.studentId) {
                currentStudentId = currentUser.studentId;
            }
            loadApp();
        } else {
            localStorage.removeItem('jwt_token');
            showLoginPage();
        }
    } catch (error) {
        console.error('Token verification failed:', error);
        localStorage.removeItem('jwt_token');
        showLoginPage();
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('login-error');

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok && data.token) {
            token = data.token;
            currentUser = data;
            localStorage.setItem('jwt_token', token);
            
            // Get full user info to get student ID
            const userResponse = await fetch(`${API_BASE_URL}/user/current`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (userResponse.ok) {
                const userInfo = await userResponse.json();
                currentUser = userInfo;
                if (userInfo.type === 'STUDENT' && userInfo.studentId) {
                    currentStudentId = userInfo.studentId;
                }
            }
            
            loadApp();
        } else {
            errorDiv.textContent = data.error || 'Erreur de connexion';
            errorDiv.classList.remove('d-none');
        }
    } catch (error) {
        errorDiv.textContent = 'Erreur de connexion au serveur';
        errorDiv.classList.remove('d-none');
    }
}

function showLoginPage() {
    document.getElementById('login-page').classList.remove('d-none');
    document.getElementById('app-content').classList.add('d-none');
}

function loadApp() {
    document.getElementById('login-page').classList.add('d-none');
    document.getElementById('app-content').classList.remove('d-none');
    
    if (currentUser) {
        document.getElementById('username-display').textContent = 
            `Connecté en tant que: ${currentUser.username}`;
    }
    
    loadMyCourses();
}

function showTab(tabName) {
    // Update nav tabs
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
        if (link.dataset.tab === tabName) {
            link.classList.add('active');
        }
    });

    // Show/hide tab content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.add('d-none');
    });
    
    document.getElementById(`${tabName}-tab`).classList.remove('d-none');

    // Load data if needed
    if (tabName === 'available') {
        loadAvailableCourses();
    } else if (tabName === 'grades') {
        loadGrades();
    } else if (tabName === 'schedule') {
        loadSchedule();
    }
}

async function loadMyCourses() {
    if (!currentStudentId) return;
    
    const loading = document.getElementById('courses-loading');
    const list = document.getElementById('courses-list');
    const empty = document.getElementById('courses-empty');

    try {
        loading.classList.remove('d-none');
        list.innerHTML = '';
        empty.classList.add('d-none');

        const response = await fetch(`${API_BASE_URL}/inscriptions/student/${currentStudentId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const enrollments = await response.json();
            
            if (enrollments.length === 0) {
                empty.classList.remove('d-none');
            } else {
                enrollments.forEach(enrollment => {
                    if (enrollment.cours) {
                        const card = createCourseCard(enrollment.cours, enrollment.id);
                        list.appendChild(card);
                    }
                });
            }
        }
    } catch (error) {
        console.error('Error loading courses:', error);
    } finally {
        loading.classList.add('d-none');
    }
}

async function loadAvailableCourses() {
    if (!currentStudentId) return;
    
    const loading = document.getElementById('available-loading');
    const list = document.getElementById('available-list');
    const empty = document.getElementById('available-empty');

    try {
        loading.classList.remove('d-none');
        list.innerHTML = '';
        empty.classList.add('d-none');

        // Get all courses
        const allCoursesResponse = await fetch(`${API_BASE_URL}/cours`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        // Get enrolled courses
        const enrolledResponse = await fetch(`${API_BASE_URL}/inscriptions/student/${currentStudentId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (allCoursesResponse.ok && enrolledResponse.ok) {
            const allCourses = await allCoursesResponse.json();
            const enrollments = await enrolledResponse.json();
            const enrolledCourseIds = enrollments.map(e => e.cours?.id).filter(id => id);
            
            const availableCourses = allCourses.filter(course => !enrolledCourseIds.includes(course.id));
            
            if (availableCourses.length === 0) {
                empty.classList.remove('d-none');
            } else {
                availableCourses.forEach(course => {
                    const card = createAvailableCourseCard(course);
                    list.appendChild(card);
                });
            }
        }
    } catch (error) {
        console.error('Error loading available courses:', error);
    } finally {
        loading.classList.add('d-none');
    }
}

function createCourseCard(course, enrollmentId) {
    const col = document.createElement('div');
    col.className = 'col-md-4 mb-3';
    
    col.innerHTML = `
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">${course.titre}</h5>
                <p class="text-muted"><small>Code: ${course.code}</small></p>
                <p class="card-text">${course.description || 'Pas de description'}</p>
                <div class="d-flex justify-content-between align-items-center">
                    <small class="text-muted">
                        <i class="bi bi-person me-1"></i>
                        ${course.formateur ? course.formateur.nom + ' ' + course.formateur.prenom : 'N/A'}
                    </small>
                    <button class="btn btn-sm btn-danger" onclick="unenrollFromCourse(${course.id})">
                        <i class="bi bi-x-circle me-1"></i>Se désinscrire
                    </button>
                </div>
            </div>
        </div>
    `;
    
    return col;
}

function createAvailableCourseCard(course) {
    const col = document.createElement('div');
    col.className = 'col-md-4 mb-3';
    
    col.innerHTML = `
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">${course.titre}</h5>
                <p class="text-muted"><small>Code: ${course.code}</small></p>
                <p class="card-text">${course.description || 'Pas de description'}</p>
                <div class="d-flex justify-content-between align-items-center">
                    <small class="text-muted">
                        <i class="bi bi-person me-1"></i>
                        ${course.formateur ? course.formateur.nom + ' ' + course.formateur.prenom : 'N/A'}
                    </small>
                    <button class="btn btn-sm btn-success" onclick="enrollInCourse(${course.id})">
                        <i class="bi bi-plus-circle me-1"></i>S'inscrire
                    </button>
                </div>
            </div>
        </div>
    `;
    
    return col;
}

async function enrollInCourse(courseId) {
    if (!currentStudentId) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/inscriptions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                studentId: currentStudentId,
                coursId: courseId
            })
        });

        if (response.ok) {
            alert('Inscription réussie !');
            loadMyCourses();
            loadAvailableCourses();
        } else {
            const error = await response.json();
            alert('Erreur: ' + (error.error || 'Impossible de s\'inscrire au cours'));
        }
    } catch (error) {
        console.error('Error enrolling:', error);
        alert('Erreur lors de l\'inscription');
    }
}

async function unenrollFromCourse(courseId) {
    if (!currentStudentId) return;
    
    if (!confirm('Êtes-vous sûr de vouloir vous désinscrire de ce cours ?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/inscriptions/student/${currentStudentId}/course/${courseId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok || response.status === 204) {
            alert('Désinscription réussie !');
            loadMyCourses();
            loadAvailableCourses();
        } else {
            alert('Erreur lors de la désinscription');
        }
    } catch (error) {
        console.error('Error unenrolling:', error);
        alert('Erreur lors de la désinscription');
    }
}

async function loadGrades() {
    if (!currentStudentId) return;
    
    const loading = document.getElementById('grades-loading');
    const content = document.getElementById('grades-content');

    try {
        loading.classList.remove('d-none');
        
        const response = await fetch(`${API_BASE_URL}/grades`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const allGrades = await response.json();
            // Filter grades for current student
            const studentGrades = allGrades.filter(grade => 
                grade.student && grade.student.id === currentStudentId
            );
            
            displayGrades(studentGrades);
        }
    } catch (error) {
        console.error('Error loading grades:', error);
    } finally {
        loading.classList.add('d-none');
    }
}

function displayGrades(grades) {
    const content = document.getElementById('grades-content');
    
    if (grades.length === 0) {
        content.innerHTML = '<div class="alert alert-info">Aucune note disponible</div>';
        return;
    }

    // Calculate average
    const average = grades.reduce((sum, grade) => sum + grade.valeur, 0) / grades.length;
    
    const html = `
        <div class="card mb-4">
            <div class="card-body">
                <h5>Moyenne générale</h5>
                <h2 class="text-primary">${average.toFixed(2)}/20</h2>
            </div>
        </div>
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Cours</th>
                    <th>Note</th>
                    <th>Commentaire</th>
                    <th>Date</th>
                </tr>
            </thead>
            <tbody>
                ${grades.map(grade => `
                    <tr>
                        <td>${grade.cours ? grade.cours.titre : 'N/A'}</td>
                        <td><strong class="${grade.valeur >= 10 ? 'text-success' : 'text-danger'}">${grade.valeur}/20</strong></td>
                        <td>${grade.commentaire || '-'}</td>
                        <td>${new Date(grade.dateAttribution).toLocaleDateString('fr-FR')}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    
    content.innerHTML = html;
}

async function loadSchedule() {
    if (!currentStudentId) return;
    
    const date = document.getElementById('schedule-date').value;
    const list = document.getElementById('schedule-list');
    
    if (!date) {
        list.innerHTML = '<div class="alert alert-info">Veuillez sélectionner une date</div>';
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/schedules/student/${currentStudentId}?date=${date}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const schedules = await response.json();
            displaySchedule(schedules);
        } else {
            list.innerHTML = '<div class="alert alert-info">Aucune séance prévue pour cette date</div>';
        }
    } catch (error) {
        console.error('Error loading schedule:', error);
        list.innerHTML = '<div class="alert alert-warning">Erreur lors du chargement de l\'emploi du temps</div>';
    }
}

function displaySchedule(schedules) {
    const list = document.getElementById('schedule-list');
    
    if (!schedules || schedules.length === 0) {
        list.innerHTML = '<div class="alert alert-info">Aucune séance prévue pour cette date</div>';
        return;
    }
    
    const html = `
        <div class="list-group">
            ${schedules.map(schedule => `
                <div class="list-group-item">
                    <div class="d-flex w-100 justify-content-between">
                        <h5 class="mb-1">${schedule.cours ? schedule.cours.titre : 'N/A'}</h5>
                        <small>${schedule.heureDebut} - ${schedule.heureFin}</small>
                    </div>
                    <p class="mb-1">
                        <i class="bi bi-geo-alt me-2"></i>${schedule.salle || 'Salle non spécifiée'}
                    </p>
                </div>
            `).join('')}
        </div>
    `;
    
    list.innerHTML = html;
}

function logout() {
    localStorage.removeItem('jwt_token');
    token = null;
    currentUser = null;
    currentStudentId = null;
    showLoginPage();
}

