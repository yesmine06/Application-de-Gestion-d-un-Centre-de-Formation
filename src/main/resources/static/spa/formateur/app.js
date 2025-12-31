const API_BASE_URL = 'http://localhost:8080/api';
let token = localStorage.getItem('jwt_token');
let currentUser = null;

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    if (token) {
        verifyTokenAndLoadApp();
    } else {
        showLoginPage();
    }

    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('grade-form').addEventListener('submit', handleGradeSubmit);
    document.getElementById('grade-course').addEventListener('change', loadStudentsForCourse);
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

async function loadApp() {
    document.getElementById('login-page').classList.add('d-none');
    document.getElementById('app-content').classList.remove('d-none');
    
    // Get full user info if not already loaded
    if (!currentUser || !currentUser.id) {
        try {
            const userResponse = await fetch(`${API_BASE_URL}/user/current`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (userResponse.ok) {
                currentUser = await userResponse.json();
            }
        } catch (error) {
            console.error('Error loading user info:', error);
        }
    }
    
    if (currentUser) {
        document.getElementById('username-display').textContent = 
            `Connecté en tant que: ${currentUser.username}`;
    }
    
    await loadCourses();
    await loadCoursesForGrades();
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
    if (tabName === 'grades') {
        loadGrades();
    }
}

async function loadCourses() {
    const loading = document.getElementById('courses-loading');
    const list = document.getElementById('courses-list');
    const empty = document.getElementById('courses-empty');

    try {
        loading.classList.remove('d-none');
        list.innerHTML = '';
        empty.classList.add('d-none');

        // Get current user to find trainer ID
        let trainerId = null;
        if (currentUser && currentUser.trainerId) {
            trainerId = currentUser.trainerId;
        } else {
            // Fallback: get current user info
            const userResponse = await fetch(`${API_BASE_URL}/user/current`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (userResponse.ok) {
                currentUser = await userResponse.json();
                trainerId = currentUser.trainerId;
            }
        }

        let courses = [];
        if (trainerId) {
            // Get courses for specific trainer
            const response = await fetch(`${API_BASE_URL}/cours/trainer/${trainerId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (response.ok) {
                courses = await response.json();
            }
        } else {
            // Fallback: get all courses
            const response = await fetch(`${API_BASE_URL}/cours`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (response.ok) {
                courses = await response.json();
            }
        }

        if (courses.length === 0) {
            empty.classList.remove('d-none');
        } else {
            courses.forEach(course => {
                const card = createCourseCard(course);
                list.appendChild(card);
            });
        }
    } catch (error) {
        console.error('Error loading courses:', error);
    } finally {
        loading.classList.add('d-none');
    }
}

function createCourseCard(course) {
    const col = document.createElement('div');
    col.className = 'col-md-4 mb-3';
    
    col.innerHTML = `
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">${course.titre}</h5>
                <p class="text-muted"><small>Code: ${course.code}</small></p>
                <p class="card-text">${course.description || 'Pas de description'}</p>
                <div class="d-flex justify-content-between">
                    <small class="text-muted">
                        <i class="bi bi-people me-1"></i>
                        ${course.enrollments ? course.enrollments.length : 0} étudiants
                    </small>
                    <button class="btn btn-sm btn-primary" onclick="loadStudentsForCourse(${course.id})">
                        Voir étudiants
                    </button>
                </div>
            </div>
        </div>
    `;
    
    return col;
}

async function loadCoursesForGrades() {
    const select = document.getElementById('grade-course');
    
    try {
        // Get current user to find trainer ID
        let trainerId = null;
        if (currentUser && currentUser.trainerId) {
            trainerId = currentUser.trainerId;
        } else {
            const userResponse = await fetch(`${API_BASE_URL}/user/current`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (userResponse.ok) {
                currentUser = await userResponse.json();
                trainerId = currentUser.trainerId;
            }
        }

        let courses = [];
        if (trainerId) {
            const response = await fetch(`${API_BASE_URL}/cours/trainer/${trainerId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (response.ok) {
                courses = await response.json();
            }
        } else {
            const response = await fetch(`${API_BASE_URL}/cours`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (response.ok) {
                courses = await response.json();
            }
        }

        select.innerHTML = '<option value="">Sélectionner un cours</option>';
        courses.forEach(course => {
            const option = document.createElement('option');
            option.value = course.id;
            option.textContent = `${course.code} - ${course.titre}`;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading courses:', error);
    }
}

async function loadStudentsForCourse(courseId) {
    const select = document.getElementById('grade-student');
    
    if (!courseId) {
        select.innerHTML = '<option value="">Sélectionner d\'abord un cours</option>';
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/inscriptions/course/${courseId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const enrollments = await response.json();
            select.innerHTML = '<option value="">Sélectionner un étudiant</option>';
            enrollments.forEach(enrollment => {
                if (enrollment.student) {
                    const option = document.createElement('option');
                    option.value = enrollment.student.id;
                    option.textContent = `${enrollment.student.nom} ${enrollment.student.prenom}`;
                    select.appendChild(option);
                }
            });
        }
    } catch (error) {
        console.error('Error loading students:', error);
    }
}

async function handleGradeSubmit(e) {
    e.preventDefault();
    const courseId = document.getElementById('grade-course').value;
    const studentId = document.getElementById('grade-student').value;
    const valeur = parseFloat(document.getElementById('grade-value').value);
    const commentaire = document.getElementById('grade-comment').value;

    try {
        const response = await fetch(`${API_BASE_URL}/grades`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                studentId: parseInt(studentId),
                courseId: parseInt(courseId),
                valeur: valeur,
                commentaire: commentaire
            })
        });

        if (response.ok) {
            alert('Note enregistrée avec succès !');
            document.getElementById('grade-form').reset();
            loadGrades();
        } else {
            const error = await response.json();
            alert('Erreur: ' + (error.error || 'Impossible d\'enregistrer la note'));
        }
    } catch (error) {
        console.error('Error saving grade:', error);
        alert('Erreur lors de l\'enregistrement de la note');
    }
}

async function loadGrades() {
    const loading = document.getElementById('grades-loading');
    const list = document.getElementById('grades-list');

    try {
        loading.classList.remove('d-none');
        
        const response = await fetch(`${API_BASE_URL}/grades`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const grades = await response.json();
            displayGrades(grades);
        }
    } catch (error) {
        console.error('Error loading grades:', error);
    } finally {
        loading.classList.add('d-none');
    }
}

function displayGrades(grades) {
    const list = document.getElementById('grades-list');
    
    if (grades.length === 0) {
        list.innerHTML = '<div class="alert alert-info">Aucune note enregistrée</div>';
        return;
    }

    const table = document.createElement('table');
    table.className = 'table table-striped';
    table.innerHTML = `
        <thead>
            <tr>
                <th>Étudiant</th>
                <th>Cours</th>
                <th>Note</th>
                <th>Commentaire</th>
                <th>Date</th>
            </tr>
        </thead>
        <tbody>
            ${grades.map(grade => `
                <tr>
                    <td>${grade.student ? grade.student.nom + ' ' + grade.student.prenom : 'N/A'}</td>
                    <td>${grade.cours ? grade.cours.titre : 'N/A'}</td>
                    <td><strong>${grade.valeur}/20</strong></td>
                    <td>${grade.commentaire || '-'}</td>
                    <td>${new Date(grade.dateAttribution).toLocaleDateString('fr-FR')}</td>
                </tr>
            `).join('')}
        </tbody>
    `;
    
    list.innerHTML = '';
    list.appendChild(table);
}

function logout() {
    localStorage.removeItem('jwt_token');
    token = null;
    currentUser = null;
    showLoginPage();
}

