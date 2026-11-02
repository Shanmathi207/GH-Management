/**
 * HospitalOOP70 - Main Frontend Application Controller
 * Handles routing, data fetching, rendering, HTML5 Canvas map, and interactions.
 */

const App = {
    currentRoute: 'dashboard',
    allPatients: [],
    allBeds: [],
    allDoctors: [],
    allHospitals: [],
    admittedPatients: [],
    hospitalGraph: null,

    init() {
        this.setupRouter();
        this.setupGlobalSearch();
        this.setupClock();
        this.checkDbStatus();
        this.handleHashChange();
    },

    setupClock() {
        const clockEl = document.getElementById('header-clock');
        const update = () => {
            if (clockEl) {
                const now = new Date();
                clockEl.textContent = now.toLocaleDateString('en-US', {
                    weekday: 'short', month: 'short', day: 'numeric',
                    hour: '2-digit', minute: '2-digit', second: '2-digit'
                });
            }
        };
        update();
        setInterval(update, 1000);
    },

    async checkDbStatus() {
        const badge = document.getElementById('db-status-badge');
        try {
            const res = await API.get('/settings/db');
            if (badge) {
                if (res.connected) {
                    badge.className = 'badge-pill badge-db-online';
                    badge.innerHTML = '● MySQL: Online';
                } else {
                    badge.className = 'badge-pill badge-db-offline';
                    badge.innerHTML = '○ In-Memory Store Active';
                }
            }
        } catch (e) {
            if (badge) {
                badge.className = 'badge-pill badge-db-offline';
                badge.innerHTML = '○ Demo Store Active';
            }
        }
    },

    setupRouter() {
        window.addEventListener('hashchange', () => this.handleHashChange());
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                const target = link.getAttribute('href');
                if (target && target.startsWith('#')) {
                    e.preventDefault();
                    window.location.hash = target;
                }
            });
        });
    },

    handleHashChange() {
        const rawHash = window.location.hash.slice(1);
        const route = rawHash || 'dashboard';
        this.navigate(route);
    },

    navigate(routeId) {
        this.currentRoute = routeId;

        // Update active sidebar nav link
        document.querySelectorAll('.nav-link').forEach(link => {
            if (link.getAttribute('href') === `#${routeId}`) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });

        // Hide all views, display the selected view
        document.querySelectorAll('.view-section').forEach(sec => {
            sec.style.display = 'none';
        });

        const activeSec = document.getElementById(`view-${routeId}`);
        if (activeSec) {
            activeSec.style.display = 'block';
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } else {
            console.warn(`View not found: view-${routeId}`);
        }

        // Trigger module loader
        switch (routeId) {
            case 'dashboard': this.loadDashboard(); break;
            case 'registration': this.loadRegistration(); break;
            case 'records': this.loadRecords(); break;
            case 'priority-queue': this.loadPriorityQueue(); break;
            case 'opd': this.loadOPD(); break;
            case 'workflow': this.loadWorkflow(); break;
            case 'emergency': this.loadEmergency(); break;
            case 'admissions': this.loadAdmissions(); break;
            case 'beds': this.loadBeds(); break;
            case 'doctors': this.loadDoctors(); break;
            case 'hospitals': this.loadHospitals(); break;
            case 'discharge': this.loadDischarge(); break;
            case 'referrals': this.loadReferrals(); break;
            case 'history': this.loadHistory(); break;
            case 'reports': this.loadReports(); break;
            case 'settings': this.loadSettings(); break;
        }
    },

    setupGlobalSearch() {
        const input = document.getElementById('global-search-input');
        const btn = document.getElementById('global-search-btn');
        const doSearch = () => {
            const query = (input.value || '').trim();
            if (!query) return;
            window.location.hash = '#records';
            setTimeout(() => {
                const recSearch = document.getElementById('records-search-input');
                if (recSearch) {
                    recSearch.value = query;
                    this.filterRecordsTable(query);
                }
            }, 100);
        };
        if (btn) btn.addEventListener('click', doSearch);
        if (input) input.addEventListener('keyup', (e) => { if (e.key === 'Enter') doSearch(); });
    },

    // ==========================================
    // 1. DASHBOARD MODULE
    // ==========================================
    async loadDashboard() {
        try {
            const stats = await API.get('/dashboard/stats');
            
            // Metrics
            this.setElText('dash-total-patients', stats.totalPatients || 0);
            this.setElText('dash-opd-waiting', stats.opdWaiting || 0);
            this.setElText('dash-inpatients', stats.inpatients || 0);
            this.setElText('dash-available-beds', stats.availableBeds || 0);
            this.setElText('dash-critical-triage', stats.criticalEmergency || 0);
            this.setElText('dash-total-doctors', stats.onDutyDoctors || 0);
            this.setElText('dash-occupancy-rate', (stats.occupancyRate || 0) + '%');
            this.setElText('dash-total-referrals', stats.totalReferrals || 0);

            // Bed Occupancy Progress Bar
            const bar = document.getElementById('dash-occupancy-bar');
            if (bar) {
                bar.style.width = `${Math.min(100, stats.occupancyRate || 0)}%`;
                bar.className = 'progress-bar-fill ' + (stats.occupancyRate > 85 ? 'progress-bar-red' : (stats.occupancyRate > 65 ? 'progress-bar-amber' : 'progress-bar-green'));
            }

            // Recent patients
            const ptTbody = document.getElementById('dash-recent-patients-tbody');
            if (ptTbody && stats.recentPatients) {
                if (stats.recentPatients.length === 0) {
                    ptTbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:#64748B;">No recent patients recorded.</td></tr>';
                } else {
                    ptTbody.innerHTML = stats.recentPatients.map(p => `
                        <tr>
                            <td><strong>${p.patientId}</strong></td>
                            <td>${this.escape(p.fullName)}</td>
                            <td><span class="status-pill ${p.visitType === 'EMERGENCY' ? 'status-critical' : 'status-waiting'}">${p.visitType}</span></td>
                            <td>Priority ${p.triagePriority || 3}</td>
                            <td>${p.assignedDoctor || 'Dr. On Duty'}</td>
                        </tr>
                    `).join('');
                }
            }

            // Recent audit logs
            const logTbody = document.getElementById('dash-activity-tbody');
            if (logTbody && stats.recentActivity) {
                logTbody.innerHTML = stats.recentActivity.map(act => `
                    <tr>
                        <td style="color:#64748B;font-size:11px;">${act.timestamp}</td>
                        <td><strong>${this.escape(act.action)}</strong></td>
                        <td>${this.escape(act.patientId || '-')}</td>
                        <td>${this.escape(act.details || '')}</td>
                    </tr>
                `).join('');
            }
        } catch (err) {
            console.error('Error loading dashboard:', err);
        }
    },

    // ==========================================
    // 2. PATIENT REGISTRATION MODULE
    // ==========================================
    loadRegistration() {
        const form = document.getElementById('registration-form');
        if (!form) return;

        // Auto-generate next candidate ID
        API.get('/patients').then(pts => {
            const nextId = 'PAT-' + String((pts ? pts.length : 0) + 101);
            const idInput = document.getElementById('reg-patient-id');
            if (idInput && !idInput.value) idInput.value = nextId;
        }).catch(() => {});

        // Populate doctors dropdown
        this.populateDoctorDropdown('reg-assigned-doctor');

        // Form submit handler
        form.onsubmit = async (e) => {
            e.preventDefault();
            const submitBtn = document.getElementById('reg-submit-btn');

            const patientId = document.getElementById('reg-patient-id').value.trim();
            const fullName = document.getElementById('reg-fullname').value.trim();
            const age = parseInt(document.getElementById('reg-age').value) || 0;
            const gender = document.getElementById('reg-gender').value;
            const contactNumber = document.getElementById('reg-contact').value.trim();
            const visitType = document.getElementById('reg-visit-type').value;
            const triagePriority = parseInt(document.getElementById('reg-triage').value) || 3;
            const symptoms = document.getElementById('reg-symptoms').value.trim();
            const assignedDoctor = document.getElementById('reg-assigned-doctor').value;

            // Strict Validation: Initial patient registration must NEVER be IPD directly!
            if (visitType !== 'OPD' && visitType !== 'EMERGENCY') {
                showToast('Initial patient registration must be OPD or Emergency only! Direct IPD is prohibited.', 'error');
                return;
            }

            if (!fullName || !contactNumber) {
                showToast('Please fill in patient name and contact number.', 'warning');
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = 'Registering...';

            try {
                const res = await API.post('/patients', {
                    patientId, fullName, age, gender, contactNumber,
                    visitType, triagePriority, symptoms, assignedDoctor
                });

                if (res.success) {
                    showToast(`Patient ${res.patientId || patientId} successfully registered (${visitType})!`, 'success');
                    form.reset();
                    // Generate new ID for next entry
                    const idInput = document.getElementById('reg-patient-id');
                    if (idInput) idInput.value = 'PAT-' + (Math.floor(Math.random() * 899) + 100);
                } else {
                    showToast(res.error || 'Registration failed.', 'error');
                }
            } catch (err) {
                showToast(err.message || 'Registration failed.', 'error');
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Register Patient';
            }
        };
    },

    // ==========================================
    // 3. PATIENT RECORDS MODULE
    // ==========================================
    async loadRecords() {
        const tbody = document.getElementById('records-table-tbody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Loading patient directory...</td></tr>';

        try {
            this.allPatients = await API.get('/patients');
            this.renderRecordsTable(this.allPatients);

            // Filter listeners
            const searchInput = document.getElementById('records-search-input');
            const typeFilter = document.getElementById('records-type-filter');

            if (searchInput) {
                searchInput.oninput = () => this.applyRecordsFilter();
            }
            if (typeFilter) {
                typeFilter.onchange = () => this.applyRecordsFilter();
            }
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="8" style="color:red;text-align:center;">Failed to load records: ${err.message}</td></tr>`;
        }
    },

    applyRecordsFilter() {
        const query = (document.getElementById('records-search-input')?.value || '').toLowerCase().trim();
        const type = document.getElementById('records-type-filter')?.value || 'ALL';

        let filtered = this.allPatients || [];
        if (type !== 'ALL') {
            filtered = filtered.filter(p => p.visitType === type || p.status === type);
        }
        if (query) {
            filtered = filtered.filter(p => 
                (p.patientId && p.patientId.toLowerCase().includes(query)) ||
                (p.fullName && p.fullName.toLowerCase().includes(query)) ||
                (p.contactNumber && p.contactNumber.includes(query))
            );
        }
        this.renderRecordsTable(filtered);
    },

    renderRecordsTable(patients) {
        const tbody = document.getElementById('records-table-tbody');
        if (!tbody) return;

        if (!patients || patients.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#64748B;">No matching patient records found.</td></tr>';
            return;
        }

        tbody.innerHTML = patients.map(p => {
            let statusPillClass = 'status-waiting';
            if (p.status === 'ADMITTED') statusPillClass = 'status-admitted';
            else if (p.status === 'DISCHARGED') statusPillClass = 'status-discharged';
            else if (p.status === 'REFERRED') statusPillClass = 'status-referred';

            return `
                <tr>
                    <td><strong>${p.patientId}</strong></td>
                    <td>${this.escape(p.fullName)}</td>
                    <td>${p.age} / ${p.gender}</td>
                    <td>${this.escape(p.contactNumber || '-')}</td>
                    <td><span class="status-pill ${p.visitType === 'EMERGENCY' ? 'status-critical' : 'status-waiting'}">${p.visitType}</span></td>
                    <td><span class="status-pill ${statusPillClass}">${p.status || 'WAITING'}</span></td>
                    <td>${this.escape(p.assignedDoctor || 'Unassigned')}</td>
                    <td>
                        <button class="btn btn-sm btn-secondary" onclick="App.viewPatientDetails('${p.patientId}')">View</button>
                        <button class="btn btn-sm btn-primary" onclick="App.editPatientDemographics('${p.patientId}')">Edit</button>
                    </td>
                </tr>
            `;
        }).join('');
    },

    viewPatientDetails(patientId) {
        const p = (this.allPatients || []).find(x => x.patientId === patientId);
        if (!p) return;

        const body = document.getElementById('modal-details-body');
        if (body) {
            body.innerHTML = `
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:16px;">
                    <div><strong>Patient ID:</strong> ${p.patientId}</div>
                    <div><strong>Full Name:</strong> ${this.escape(p.fullName)}</div>
                    <div><strong>Age / Gender:</strong> ${p.age} / ${p.gender}</div>
                    <div><strong>Contact:</strong> ${this.escape(p.contactNumber)}</div>
                    <div><strong>Visit Type:</strong> <span class="status-pill status-waiting">${p.visitType}</span></div>
                    <div><strong>Clinical Status:</strong> <span class="status-pill status-admitted">${p.status || 'WAITING'}</span></div>
                    <div><strong>Triage Severity:</strong> Level ${p.triagePriority || 3}</div>
                    <div><strong>Assigned Bed:</strong> ${p.assignedBedId || 'None'}</div>
                    <div><strong>Assigned Doctor:</strong> ${this.escape(p.assignedDoctor || 'None')}</div>
                    <div><strong>Registered On:</strong> ${p.registrationTime || 'Today'}</div>
                </div>
                <div style="margin-top:10px;">
                    <strong>Chief Complaint & Symptoms:</strong>
                    <div style="background:#F8FAFC;padding:10px;border-radius:6px;border:1px solid #E2E8F0;margin-top:4px;font-size:12px;">
                        ${this.escape(p.symptoms || 'None recorded')}
                    </div>
                </div>
            `;
        }
        this.openModal('modal-patient-details');
    },

    editPatientDemographics(patientId) {
        const p = (this.allPatients || []).find(x => x.patientId === patientId);
        if (!p) return;

        document.getElementById('edit-patient-id').value = p.patientId;
        document.getElementById('edit-fullname').value = p.fullName || '';
        document.getElementById('edit-age').value = p.age || 0;
        document.getElementById('edit-gender').value = p.gender || 'M';
        document.getElementById('edit-contact').value = p.contactNumber || '';
        document.getElementById('edit-symptoms').value = p.symptoms || '';

        const form = document.getElementById('edit-patient-form');
        form.onsubmit = async (e) => {
            e.preventDefault();
            const fullName = document.getElementById('edit-fullname').value.trim();
            const age = parseInt(document.getElementById('edit-age').value) || 0;
            const gender = document.getElementById('edit-gender').value;
            const contactNumber = document.getElementById('edit-contact').value.trim();
            const symptoms = document.getElementById('edit-symptoms').value.trim();

            try {
                const res = await API.put(`/patients/${p.patientId}`, {
                    fullName, age, gender, contactNumber, symptoms
                });
                if (res.success) {
                    showToast('Demographics updated successfully!', 'success');
                    this.closeModal('modal-patient-edit');
                    this.loadRecords();
                } else {
                    showToast(res.error || 'Update failed', 'error');
                }
            } catch (err) {
                showToast(err.message, 'error');
            }
        };

        this.openModal('modal-patient-edit');
    },

    // ==========================================
    // 4. PRIORITY QUEUE MODULE
    // ==========================================
    async loadPriorityQueue() {
        const tbody = document.getElementById('queue-table-tbody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Calculating triage priority queue...</td></tr>';

        try {
            const queue = await API.get('/priority-queue');
            if (!queue || queue.length === 0) {
                if (tbody) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#64748B;">No patients waiting in queue.</td></tr>';
                return;
            }

            tbody.innerHTML = queue.map((item, idx) => {
                const isUrgent = item.triagePriority <= 2;
                return `
                    <tr style="${isUrgent ? 'background:#FEF2F2;' : ''}">
                        <td><strong>#${idx + 1}</strong></td>
                        <td><strong>${item.patientId}</strong></td>
                        <td>${this.escape(item.fullName)}</td>
                        <td><span class="status-pill ${item.visitType === 'EMERGENCY' ? 'status-critical' : 'status-waiting'}">${item.visitType}</span></td>
                        <td><span class="status-pill ${isUrgent ? 'status-critical' : 'status-medium'}">Level ${item.triagePriority}</span></td>
                        <td><strong>${item.priorityScore}</strong></td>
                        <td>
                            <button class="btn btn-sm ${idx === 0 ? 'btn-danger' : 'btn-primary'}" onclick="App.callPatient('${item.patientId}', '${this.escape(item.fullName)}')">
                                ${idx === 0 ? '🚨 Call NOW' : 'Call'}
                            </button>
                        </td>
                    </tr>
                `;
            }).join('');

            // Call Next button
            const callNextBtn = document.getElementById('queue-call-next-btn');
            if (callNextBtn) {
                callNextBtn.onclick = async () => {
                    try {
                        const res = await API.post('/priority-queue', { action: 'call_next' });
                        if (res.called) {
                            showToast(`Calling Patient: ${res.called.patientId} (${res.called.fullName}) to Consultation Room 1!`, 'warning');
                            this.loadPriorityQueue();
                        } else {
                            showToast('Queue is currently empty.', 'info');
                        }
                    } catch (e) {
                        showToast(e.message, 'error');
                    }
                };
            }
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="7" style="color:red;text-align:center;">${err.message}</td></tr>`;
        }
    },

    callPatient(id, name) {
        showToast(`Calling Patient ${id} (${name}) to Consultation Desk.`, 'warning');
    },

    // ==========================================
    // 5. OPD CONSULTATION MODULE
    // ==========================================
    async loadOPD() {
        const select = document.getElementById('opd-patient-select');
        const form = document.getElementById('opd-consultation-form');
        this.populateDoctorDropdown('opd-doctor-select');

        try {
            const patients = await API.get('/patients');
            const opdWaiting = (patients || []).filter(p => p.visitType === 'OPD' && p.status === 'WAITING');

            if (select) {
                if (opdWaiting.length === 0) {
                    select.innerHTML = '<option value="">-- No OPD patients waiting --</option>';
                } else {
                    select.innerHTML = '<option value="">-- Select Patient from OPD Waiting List --</option>' +
                        opdWaiting.map(p => `<option value="${p.patientId}">${p.patientId} - ${p.fullName} (Age ${p.age})</option>`).join('');
                }

                select.onchange = () => {
                    const chosen = opdWaiting.find(p => p.patientId === select.value);
                    if (chosen) {
                        this.setElText('opd-pt-details', `Selected: ${chosen.fullName} (${chosen.gender}, ${chosen.age}y) | Symptoms: ${chosen.symptoms || 'None'}`);
                    } else {
                        this.setElText('opd-pt-details', '');
                    }
                };
            }

            // Save Consultation Form
            if (form) {
                form.onsubmit = async (e) => {
                    e.preventDefault();
                    const patientId = select ? select.value : '';
                    const doctor = document.getElementById('opd-doctor-select').value;
                    const diagnosis = document.getElementById('opd-diagnosis').value.trim();
                    const notes = document.getElementById('opd-notes').value.trim();
                    const prescription = document.getElementById('opd-prescription').value.trim();

                    if (!patientId) {
                        showToast('Please select a patient.', 'warning');
                        return;
                    }

                    try {
                        const res = await API.post('/opd/consultations', {
                            patientId, doctor, diagnosis, notes, prescription
                        });
                        if (res.success) {
                            showToast(`Consultation saved for patient ${patientId}!`, 'success');
                            form.reset();
                            this.loadOPD();
                        } else {
                            showToast(res.error || 'Failed to save consultation.', 'error');
                        }
                    } catch (err) {
                        showToast(err.message, 'error');
                    }
                };
            }

            // Recommend IPD Admission button
            const recommendBtn = document.getElementById('opd-recommend-ipd-btn');
            if (recommendBtn) {
                recommendBtn.onclick = async () => {
                    const patientId = select ? select.value : '';
                    if (!patientId) {
                        showToast('Please select an OPD patient first.', 'warning');
                        return;
                    }
                    const reason = document.getElementById('opd-diagnosis').value.trim() || 'Clinical deterioration / requires inpatient monitoring';
                    try {
                        const res = await API.post('/opd/recommend-ipd', {
                            patientId,
                            bedType: 'GENERAL',
                            urgency: 'HIGH',
                            reason
                        });
                        if (res.success) {
                            showToast(`IPD Admission Recommended! Request ID: ${res.requestId}. Patient transitioned to Admission Pipeline.`, 'success');
                            window.location.hash = '#workflow';
                        } else {
                            showToast(res.error || 'Failed to recommend IPD', 'error');
                        }
                    } catch (e) {
                        showToast(e.message, 'error');
                    }
                };
            }
        } catch (err) {
            console.error('Error loading OPD:', err);
        }
    },

    // ==========================================
    // 6. OPD TO IPD WORKFLOW MODULE
    // ==========================================
    async loadWorkflow() {
        const tableBody = document.getElementById('workflow-requests-tbody');
        if (tableBody) tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Loading admission pipeline...</td></tr>';

        try {
            const reqs = await API.get('/admissions/requests');
            if (!reqs || reqs.length === 0) {
                if (tableBody) tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#64748B;">No patients currently in the OPD to IPD transition pipeline.</td></tr>';
                return;
            }

            tableBody.innerHTML = reqs.map(r => `
                <tr>
                    <td><strong>${r.requestId}</strong></td>
                    <td><strong>${r.patientId}</strong> - ${this.escape(r.patientName || '')}</td>
                    <td><span class="status-pill ${r.urgency === 'CRITICAL' ? 'status-critical' : 'status-high'}">${r.urgency}</span></td>
                    <td>${r.bedTypeRequired || 'GENERAL'}</td>
                    <td><span class="status-pill ${r.status === 'ALLOCATED' ? 'status-admitted' : 'status-waiting'}">${r.status}</span></td>
                    <td>
                        ${r.status === 'PENDING' ? `
                            <button class="btn btn-sm btn-success" onclick="App.openBedAllocationModal('${r.requestId}', '${r.patientId}', '${r.bedTypeRequired}')">Allocate Bed</button>
                            <button class="btn btn-sm btn-navy" onclick="App.navigateToReferral('${r.patientId}')">Refer External</button>
                        ` : '<span style="color:#16A34A;font-weight:600;font-size:11px;">✓ Allocated</span>'}
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            if (tableBody) tableBody.innerHTML = `<tr><td colspan="6" style="color:red;text-align:center;">${err.message}</td></tr>`;
        }
    },

    navigateToReferral(patientId) {
        window.location.hash = '#referrals';
        setTimeout(() => {
            const ptInput = document.getElementById('ref-patient-id');
            if (ptInput) ptInput.value = patientId;
        }, 150);
    },

    // ==========================================
    // 7. EMERGENCY REGISTRATION MODULE
    // ==========================================
    loadEmergency() {
        const form = document.getElementById('emergency-form');
        if (!form) return;

        // Auto-generate ID
        const idInput = document.getElementById('emg-patient-id');
        if (idInput && !idInput.value) idInput.value = 'EMG-' + (Math.floor(Math.random() * 899) + 100);

        this.populateDoctorDropdown('emg-assigned-doctor');

        form.onsubmit = async (e) => {
            e.preventDefault();
            const patientId = document.getElementById('emg-patient-id').value.trim();
            const fullName = document.getElementById('emg-fullname').value.trim();
            const age = parseInt(document.getElementById('emg-age').value) || 0;
            const gender = document.getElementById('emg-gender').value;
            const triagePriority = parseInt(document.getElementById('emg-triage').value) || 1;
            const symptoms = document.getElementById('emg-symptoms').value.trim();
            const assignedDoctor = document.getElementById('emg-assigned-doctor').value;

            try {
                const res = await API.post('/emergency', {
                    patientId, fullName, age, gender, triagePriority, symptoms, assignedDoctor
                });

                if (res.success) {
                    showToast(`🚨 CRITICAL EMERGENCY REGISTERED: ${res.patientId}! Placed at Top of Priority Queue.`, 'error');
                    form.reset();
                    const newId = document.getElementById('emg-patient-id');
                    if (newId) newId.value = 'EMG-' + (Math.floor(Math.random() * 899) + 100);
                    // Navigate to priority queue to show instant triage
                    setTimeout(() => window.location.hash = '#priority-queue', 800);
                } else {
                    showToast(res.error || 'Emergency registration failed', 'error');
                }
            } catch (err) {
                showToast(err.message, 'error');
            }
        };
    },

    // ==========================================
    // 8. ADMISSION REQUESTS MODULE
    // ==========================================
    async loadAdmissions() {
        const tbody = document.getElementById('admissions-table-tbody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Loading admission registry...</td></tr>';

        try {
            const reqs = await API.get('/admissions/requests');
            if (!reqs || reqs.length === 0) {
                if (tbody) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#64748B;">No admission requests in queue.</td></tr>';
                return;
            }

            tbody.innerHTML = reqs.map(r => `
                <tr>
                    <td><strong>${r.requestId}</strong></td>
                    <td><strong>${r.patientId}</strong></td>
                    <td>${this.escape(r.patientName || '-')}</td>
                    <td><span class="status-pill ${r.urgency === 'CRITICAL' ? 'status-critical' : 'status-high'}">${r.urgency}</span></td>
                    <td>${r.bedTypeRequired || 'GENERAL'}</td>
                    <td><span class="status-pill ${r.status === 'ALLOCATED' ? 'status-admitted' : 'status-waiting'}">${r.status}</span></td>
                    <td>
                        ${r.status === 'PENDING' ? `
                            <button class="btn btn-sm btn-primary" onclick="App.openBedAllocationModal('${r.requestId}', '${r.patientId}', '${r.bedTypeRequired}')">Allocate Bed</button>
                        ` : `<span style="color:#16A34A;font-size:11px;font-weight:600;">Allocated (${r.allocatedBedId || 'Inpatient'})</span>`}
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="7" style="color:red;text-align:center;">${err.message}</td></tr>`;
        }
    },

    async openBedAllocationModal(requestId, patientId, bedType) {
        document.getElementById('alloc-request-id').value = requestId;
        document.getElementById('alloc-patient-id').value = patientId;

        const bedSelect = document.getElementById('alloc-bed-select');
        bedSelect.innerHTML = '<option>Loading available beds...</option>';

        try {
            const beds = await API.get('/beds');
            const avail = (beds || []).filter(b => b.status === 'AVAILABLE');

            if (avail.length === 0) {
                bedSelect.innerHTML = '<option value="">-- No available beds! Please initiate Hospital Referral --</option>';
            } else {
                bedSelect.innerHTML = avail.map(b => `
                    <option value="${b.bedId}">${b.bedId} - ${b.wardName} (${b.bedType})</option>
                `).join('');
            }

            const form = document.getElementById('alloc-bed-form');
            form.onsubmit = async (e) => {
                e.preventDefault();
                const chosenBed = bedSelect.value;
                if (!chosenBed) {
                    showToast('Please select a bed.', 'warning');
                    return;
                }

                try {
                    const res = await API.post('/admissions/allocate', {
                        requestId,
                        patientId,
                        bedId: chosenBed
                    });
                    if (res.success) {
                        showToast(`Bed ${chosenBed} successfully allocated to Patient ${patientId}!`, 'success');
                        this.closeModal('modal-bed-allocation');
                        this.loadAdmissions();
                    } else {
                        showToast(res.error || 'Allocation failed', 'error');
                    }
                } catch (err) {
                    showToast(err.message, 'error');
                }
            };

            this.openModal('modal-bed-allocation');
        } catch (e) {
            showToast(e.message, 'error');
        }
    },

    // ==========================================
    // 9. BED MANAGEMENT MODULE
    // ==========================================
    async loadBeds() {
        const grid = document.getElementById('beds-card-grid');
        if (grid) grid.innerHTML = '<div style="grid-column:1/-1;text-align:center;">Loading hospital ward inventory...</div>';

        try {
            this.allBeds = await API.get('/beds');

            // Update telemetry metrics
            let total = this.allBeds.length;
            let avail = 0, occ = 0, clean = 0, maint = 0, rsv = 0;

            this.allBeds.forEach(b => {
                if (b.status === 'AVAILABLE') avail++;
                else if (b.status === 'OCCUPIED') occ++;
                else if (b.status === 'CLEANING') clean++;
                else if (b.status === 'MAINTENANCE') maint++;
                else if (b.status === 'RESERVED') rsv++;
            });

            this.setElText('bed-stat-total', total);
            this.setElText('bed-stat-available', avail);
            this.setElText('bed-stat-occupied', occ);
            this.setElText('bed-stat-cleaning', clean);
            this.setElText('bed-stat-maint', maint);
            this.setElText('bed-stat-reserved', rsv);

            // Render cards
            if (!this.allBeds || this.allBeds.length === 0) {
                if (grid) grid.innerHTML = '<div style="grid-column:1/-1;text-align:center;">No beds configured in hospital.</div>';
                return;
            }

            grid.innerHTML = this.allBeds.map(b => {
                let pillClass = 'status-available';
                if (b.status === 'OCCUPIED') pillClass = 'status-occupied';
                else if (b.status === 'CLEANING') pillClass = 'status-cleaning';
                else if (b.status === 'MAINTENANCE') pillClass = 'status-critical';

                return `
                    <div class="card" style="margin-bottom:0;border-top:3px solid ${b.status === 'AVAILABLE' ? '#16A34A' : (b.status === 'OCCUPIED' ? '#EA580C' : '#EAB308')};">
                        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
                            <strong style="font-size:15px;color:#1764C0;">${b.bedId}</strong>
                            <span class="status-pill ${pillClass}">${b.status}</span>
                        </div>
                        <div style="font-size:12px;color:#64748B;margin-bottom:4px;">Ward: <strong>${b.wardName}</strong></div>
                        <div style="font-size:12px;color:#64748B;margin-bottom:8px;">Type: ${b.bedType}</div>
                        <div style="font-size:11px;background:#F8FAFC;padding:6px;border-radius:4px;border:1px solid #E2E8F0;min-height:28px;">
                            ${b.patientId ? `Occupied by: <strong>${b.patientId}</strong>` : 'Currently unoccupied'}
                        </div>
                        ${b.status === 'CLEANING' ? `
                            <button class="btn btn-sm btn-success btn-block" style="margin-top:8px;" onclick="App.finishBedCleaning('${b.bedId}')">
                                ✓ Complete Cleaning
                            </button>
                        ` : ''}
                    </div>
                `;
            }).join('');
        } catch (err) {
            if (grid) grid.innerHTML = `<div style="grid-column:1/-1;color:red;text-align:center;">${err.message}</div>`;
        }
    },

    async finishBedCleaning(bedId) {
        try {
            const res = await API.post('/beds/clean', { bedId });
            if (res.success) {
                showToast(`Bed ${bedId} cleaned and returned to Available status!`, 'success');
                this.loadBeds();
            } else {
                showToast(res.error || 'Failed to update bed status', 'error');
            }
        } catch (e) {
            showToast(e.message, 'error');
        }
    },

    // ==========================================
    // 10. DOCTOR MANAGEMENT MODULE
    // ==========================================
    async loadDoctors() {
        const tbody = document.getElementById('doctors-table-tbody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Loading medical staff roster...</td></tr>';

        try {
            this.allDoctors = await API.get('/doctors');
            if (!this.allDoctors || this.allDoctors.length === 0) {
                if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#64748B;">No doctors found.</td></tr>';
                return;
            }

            tbody.innerHTML = this.allDoctors.map(doc => `
                <tr>
                    <td><strong>${doc.doctorId}</strong></td>
                    <td><strong>${this.escape(doc.fullName)}</strong></td>
                    <td>${this.escape(doc.department)}</td>
                    <td>${this.escape(doc.hospitalName || 'District HQ')}</td>
                    <td>
                        <span class="status-pill ${doc.onDuty ? 'status-active' : 'status-waiting'}">
                            ${doc.onDuty ? '● ON DUTY' : '○ OFF DUTY'}
                        </span>
                    </td>
                    <td>
                        <button class="btn btn-sm ${doc.onDuty ? 'btn-secondary' : 'btn-success'}" onclick="App.toggleDoctorDuty('${doc.doctorId}')">
                            ${doc.onDuty ? 'Set Off-Duty' : 'Set On-Duty'}
                        </button>
                    </td>
                </tr>
            `).join('');

            // Add Doctor modal trigger
            const addBtn = document.getElementById('add-doctor-btn');
            if (addBtn) {
                addBtn.onclick = () => {
                    const form = document.getElementById('add-doctor-form');
                    form.reset();
                    form.onsubmit = async (e) => {
                        e.preventDefault();
                        const doctorId = document.getElementById('doc-id').value.trim();
                        const fullName = document.getElementById('doc-name').value.trim();
                        const department = document.getElementById('doc-dept').value;
                        const hospitalName = document.getElementById('doc-hosp').value.trim();

                        try {
                            const res = await API.post('/doctors', { doctorId, fullName, department, hospitalName });
                            if (res.success) {
                                showToast(`Physician ${fullName} added to roster!`, 'success');
                                this.closeModal('modal-add-doctor');
                                this.loadDoctors();
                            } else {
                                showToast(res.error || 'Failed to add doctor', 'error');
                            }
                        } catch (err) {
                            showToast(err.message, 'error');
                        }
                    };
                    this.openModal('modal-add-doctor');
                };
            }
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="6" style="color:red;text-align:center;">${err.message}</td></tr>`;
        }
    },

    async toggleDoctorDuty(doctorId) {
        try {
            const res = await API.post('/doctors', { action: 'toggle_duty', doctorId });
            if (res.success) {
                showToast(`Doctor ${doctorId} status updated to ${res.onDuty ? 'ON DUTY' : 'OFF DUTY'}`, 'info');
                this.loadDoctors();
            } else {
                showToast(res.error || 'Toggle failed', 'error');
            }
        } catch (e) {
            showToast(e.message, 'error');
        }
    },

    // ==========================================
    // 11. HOSPITAL NETWORK MANAGEMENT MODULE
    // ==========================================
    async loadHospitals() {
        const tbody = document.getElementById('hospitals-table-tbody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Loading regional hospital directory...</td></tr>';

        try {
            this.allHospitals = await API.get('/hospitals');
            if (!this.allHospitals || this.allHospitals.length === 0) {
                if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#64748B;">No regional network facilities configured.</td></tr>';
                return;
            }

            tbody.innerHTML = this.allHospitals.map(h => `
                <tr>
                    <td><strong>${h.hospitalId}</strong></td>
                    <td><strong>${this.escape(h.name)}</strong></td>
                    <td>${this.escape(h.location)}</td>
                    <td><strong>${h.totalBeds}</strong> beds (${h.availableBeds} avail)</td>
                    <td>${this.escape(h.contactNumber || '108')}</td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick="App.viewHospitalDetails('${h.hospitalId}')">Details</button>
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="6" style="color:red;text-align:center;">${err.message}</td></tr>`;
        }
    },

    viewHospitalDetails(hospId) {
        const h = (this.allHospitals || []).find(x => x.hospitalId === hospId);
        if (!h) return;
        alert(`Facility: ${h.name}\nLocation: ${h.location}\nCapacity: ${h.totalBeds} Beds\nAvailable: ${h.availableBeds}\nHelpline: ${h.contactNumber || '108'}`);
    },

    // ==========================================
    // 12. PATIENT DISCHARGE MODULE
    // ==========================================
    async loadDischarge() {
        const select = document.getElementById('discharge-patient-select');
        const form = document.getElementById('discharge-form');

        try {
            this.admittedPatients = await API.get('/discharge/admitted');

            if (select) {
                if (!this.admittedPatients || this.admittedPatients.length === 0) {
                    select.innerHTML = '<option value="">-- No admitted inpatients currently in hospital --</option>';
                    this.clearDischargeDetails();
                } else {
                    select.innerHTML = '<option value="">-- Select Inpatient to Process Discharge --</option>' +
                        this.admittedPatients.map(p => `
                            <option value="${p.patientId}">${p.patientId} - ${p.fullName} (Bed: ${p.assignedBedId || 'Ward'})</option>
                        `).join('');
                }

                select.onchange = () => {
                    const chosen = this.admittedPatients.find(p => p.patientId === select.value);
                    if (chosen) {
                        this.populateDischargeDetails(chosen);
                    } else {
                        this.clearDischargeDetails();
                    }
                };
            }

            if (form) {
                form.onsubmit = async (e) => {
                    e.preventDefault();
                    const patientId = select ? select.value : '';
                    if (!patientId) {
                        showToast('Please select an admitted patient.', 'warning');
                        return;
                    }

                    const notes = document.getElementById('discharge-notes').value.trim();
                    const followUp = document.getElementById('discharge-followup').value.trim();
                    const condition = document.getElementById('discharge-condition').value;

                    if (!notes) {
                        showToast('Please write a clinical discharge summary.', 'warning');
                        return;
                    }

                    try {
                        const res = await API.post('/discharge', {
                            patientId, notes, followUp, condition
                        });

                        if (res.success) {
                            showToast(`Patient ${patientId} discharged successfully! Bed marked for sanitization.`, 'success');
                            form.reset();
                            this.clearDischargeDetails();
                            this.loadDischarge();
                        } else {
                            showToast(res.error || 'Discharge failed', 'error');
                        }
                    } catch (err) {
                        showToast(err.message, 'error');
                    }
                };
            }
        } catch (err) {
            console.error('Error loading discharge:', err);
        }
    },

    populateDischargeDetails(p) {
        this.setElText('disc-info-id', p.patientId);
        this.setElText('disc-info-name', p.fullName);
        this.setElText('disc-info-bed', p.assignedBedId || 'General Ward');
        this.setElText('disc-info-doctor', p.assignedDoctor || 'Dr. On Duty');
        this.setElText('disc-info-admit-date', p.registrationTime || 'Recent');
    },

    clearDischargeDetails() {
        this.setElText('disc-info-id', '-');
        this.setElText('disc-info-name', '-');
        this.setElText('disc-info-bed', '-');
        this.setElText('disc-info-doctor', '-');
        this.setElText('disc-info-admit-date', '-');
    },

    // ==========================================
    // 13. HOSPITAL REFERRALS & DIJKSTRA MAP
    // ==========================================
    async loadReferrals() {
        const srcSelect = document.getElementById('ref-src-hospital');
        const dstSelect = document.getElementById('ref-dst-hospital');
        const calcBtn = document.getElementById('ref-calc-route-btn');
        const createRefBtn = document.getElementById('ref-create-btn');

        try {
            const hospitals = await API.get('/hospitals');
            this.allHospitals = hospitals || [];

            const options = this.allHospitals.map(h => `<option value="${h.hospitalId}">${h.name} (${h.location})</option>`).join('');

            if (srcSelect) srcSelect.innerHTML = options;
            if (dstSelect) {
                dstSelect.innerHTML = options;
                if (srcSelect && srcSelect.children.length > 1) {
                    dstSelect.selectedIndex = 1;
                }
            }

            // Draw default graph
            this.drawHospitalCanvasMap([], null);

            // Dijkstra route calculation
            if (calcBtn) {
                calcBtn.onclick = async () => {
                    const src = srcSelect.value;
                    const dst = dstSelect.value;
                    if (src === dst) {
                        showToast('Source and destination hospital must be different!', 'warning');
                        return;
                    }

                    try {
                        const res = await API.post('/referrals', {
                            action: 'calculate_route',
                            sourceId: src,
                            destinationId: dst
                        });

                        if (res.success) {
                            const minutes = res.travelMinutes || res.totalMinutes || 0;
                            const pathStr = res.pathString || `${src} -> ${dst}`;
                            const pathArray = res.path || [src, dst];

                            this.setElText('ref-calc-time', `${minutes} Minutes`);
                            this.setElText('ref-calc-path', pathStr);

                            showToast(`Optimal route found: ${pathStr} (${minutes} mins)`, 'success');
                            this.drawHospitalCanvasMap(pathArray, minutes);
                        } else {
                            showToast(res.error || 'Failed to calculate shortest path', 'error');
                        }
                    } catch (e) {
                        showToast(e.message, 'error');
                    }
                };
            }

            // Create Referral submit
            if (createRefBtn) {
                createRefBtn.onclick = async () => {
                    const patientId = document.getElementById('ref-patient-id').value.trim();
                    const src = srcSelect.value;
                    const dst = dstSelect.value;
                    const bedType = document.getElementById('ref-bed-type').value;

                    if (!patientId) {
                        showToast('Please enter Patient ID to refer.', 'warning');
                        return;
                    }

                    try {
                        const res = await API.post('/referrals', {
                            action: 'create_referral',
                            patientId,
                            sourceId: src,
                            destinationId: dst,
                            bedType
                        });

                        if (res.success) {
                            showToast(`Inter-hospital transfer approved! Referral ID: ${res.referralId}`, 'success');
                            this.loadReferralHistoryTable();
                        } else {
                            showToast(res.error || 'Referral failed', 'error');
                        }
                    } catch (e) {
                        showToast(e.message, 'error');
                    }
                };
            }

            this.loadReferralHistoryTable();
        } catch (err) {
            console.error('Error loading referrals:', err);
        }
    },

    async loadReferralHistoryTable() {
        const tbody = document.getElementById('referrals-table-tbody');
        if (!tbody) return;
        try {
            const history = await API.get('/referrals');
            if (!history || history.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#64748B;">No referrals recorded yet.</td></tr>';
                return;
            }
            tbody.innerHTML = history.map(r => `
                <tr>
                    <td><strong>${r.referralId}</strong></td>
                    <td><strong>${r.patientId}</strong></td>
                    <td>${this.escape(r.sourceHospital || 'Local')}</td>
                    <td>${this.escape(r.destHospital || 'Specialty')}</td>
                    <td><strong>${r.travelMinutes || 35} mins</strong></td>
                    <td><span class="status-pill status-referred">${r.status || 'TRANSFERRED'}</span></td>
                </tr>
            `).join('');
        } catch (ignored) {}
    },

    drawHospitalCanvasMap(highlightPath = [], totalMinutes = null) {
        const canvas = document.getElementById('hospital-map-canvas');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const width = canvas.width = canvas.parentElement.clientWidth || 700;
        const height = canvas.height = 360;

        ctx.clearRect(0, 0, width, height);

        // Defined network coordinates for government hospitals
        const nodes = [
            { id: 'H1', name: 'District HQ Hospital', x: width * 0.18, y: height * 0.3 },
            { id: 'H2', name: 'Apex Medical Center', x: width * 0.5, y: height * 0.18 },
            { id: 'H3', name: 'Metro Trauma Institute', x: width * 0.82, y: height * 0.3 },
            { id: 'H4', name: 'Community Care Unit', x: width * 0.32, y: height * 0.75 },
            { id: 'H5', name: 'Sub-Divisional Hospital', x: width * 0.7, y: height * 0.75 }
        ];

        const edges = [
            { u: 'H1', v: 'H2', w: 25 },
            { u: 'H1', v: 'H4', w: 15 },
            { u: 'H2', v: 'H3', w: 20 },
            { u: 'H2', v: 'H5', w: 30 },
            { u: 'H3', v: 'H5', w: 18 },
            { u: 'H4', v: 'H5', w: 35 }
        ];

        // Draw regular edges
        edges.forEach(e => {
            const n1 = nodes.find(n => n.id === e.u);
            const n2 = nodes.find(n => n.id === e.v);
            if (!n1 || !n2) return;

            const isHighlighted = highlightPath && highlightPath.length > 1 &&
                highlightPath.some((val, i) => (val === e.u && highlightPath[i+1] === e.v) || (val === e.v && highlightPath[i+1] === e.u));

            ctx.beginPath();
            ctx.moveTo(n1.x, n1.y);
            ctx.lineTo(n2.x, n2.y);
            ctx.strokeStyle = isHighlighted ? '#2878DF' : '#CBD5E1';
            ctx.lineWidth = isHighlighted ? 4 : 2;
            if (isHighlighted) {
                ctx.setLineDash([6, 3]);
            } else {
                ctx.setLineDash([]);
            }
            ctx.stroke();
            ctx.setLineDash([]);

            // Edge weight label
            const midX = (n1.x + n2.x) / 2;
            const midY = (n1.y + n2.y) / 2;
            ctx.fillStyle = isHighlighted ? '#1764C0' : '#64748B';
            ctx.font = 'bold 11px Segoe UI, sans-serif';
            ctx.fillText(`${e.w}m`, midX - 10, midY - 6);
        });

        // Draw nodes
        nodes.forEach(n => {
            const isInPath = highlightPath.includes(n.id);

            // Outer ring
            ctx.beginPath();
            ctx.arc(n.x, n.y, isInPath ? 24 : 18, 0, Math.PI * 2);
            ctx.fillStyle = isInPath ? '#2878DF' : '#FFFFFF';
            ctx.fill();
            ctx.lineWidth = isInPath ? 3 : 2;
            ctx.strokeStyle = isInPath ? '#1764C0' : '#94A3B8';
            ctx.stroke();

            // Inner label
            ctx.fillStyle = isInPath ? '#FFFFFF' : '#1764C0';
            ctx.font = 'bold 12px Segoe UI, sans-serif';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText(n.id, n.x, n.y);

            // Name label below
            ctx.fillStyle = isInPath ? '#1764C0' : '#334155';
            ctx.font = isInPath ? 'bold 12px Segoe UI, sans-serif' : '11px Segoe UI, sans-serif';
            ctx.fillText(n.name, n.x, n.y + (isInPath ? 36 : 30));
        });
    },

    // ==========================================
    // 14. PATIENT HISTORY & AUDIT MODULE
    // ==========================================
    async loadHistory() {
        const tbody = document.getElementById('history-table-tbody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">Loading audit trails...</td></tr>';

        try {
            const items = await API.get('/history');
            if (!items || items.length === 0) {
                if (tbody) tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:#64748B;">No clinical history logs found.</td></tr>';
                return;
            }

            tbody.innerHTML = items.map(h => `
                <tr>
                    <td style="color:#64748B;font-size:11px;">${h.timestamp}</td>
                    <td><strong>${this.escape(h.eventType)}</strong></td>
                    <td><strong>${h.patientId || '-'}</strong></td>
                    <td>${this.escape(h.performedBy || 'System')}</td>
                    <td>${this.escape(h.details || '-')}</td>
                </tr>
            `).join('');
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="5" style="color:red;text-align:center;">${err.message}</td></tr>`;
        }
    },

    // ==========================================
    // 15. REPORTS & STATISTICS MODULE
    // ==========================================
    async loadReports() {
        try {
            const data = await API.get('/reports');
            
            this.setElText('rep-total-treated', data.totalTreated || 0);
            this.setElText('rep-avg-los', (data.avgLengthOfStay || 3.2) + ' Days');
            this.setElText('rep-bed-turnover', (data.bedTurnoverRate || 82) + '%');
            this.setElText('rep-emergency-ratio', (data.emergencyRatio || 24) + '%');

            // Visit Type Breakdown Bar
            const opdPct = data.opdPercentage || 76;
            const emgPct = 100 - opdPct;

            const opdBar = document.getElementById('rep-opd-bar');
            const emgBar = document.getElementById('rep-emg-bar');
            if (opdBar) opdBar.style.width = `${opdPct}%`;
            if (emgBar) emgBar.style.width = `${emgPct}%`;

            this.setElText('rep-opd-label', `OPD Visits: ${opdPct}%`);
            this.setElText('rep-emg-label', `Emergency Visits: ${emgPct}%`);
        } catch (err) {
            console.error('Error loading reports:', err);
        }
    },

    // ==========================================
    // 16. SYSTEM SETTINGS MODULE
    // ==========================================
    async loadSettings() {
        try {
            const dbInfo = await API.get('/settings/db');
            this.setElText('set-db-host', dbInfo.host || 'localhost');
            this.setElText('set-db-port', dbInfo.port || '3306');
            this.setElText('set-db-name', dbInfo.database || 'hospital_oop_db');
            this.setElText('set-db-status', dbInfo.connected ? 'ONLINE (MySQL Active)' : 'OFFLINE (Demo Memory Store Active)');

            // Test connection button
            const testBtn = document.getElementById('set-test-db-btn');
            if (testBtn) {
                testBtn.onclick = async () => {
                    testBtn.disabled = true;
                    testBtn.textContent = 'Testing Connection...';
                    try {
                        const res = await API.post('/settings/db', { action: 'test' });
                        if (res.connected) {
                            showToast('MySQL Database Connection Successful!', 'success');
                        } else {
                            showToast('MySQL is unreachable. Active fallback: In-Memory Demo DataStore.', 'warning');
                        }
                        this.loadSettings();
                    } catch (e) {
                        showToast(e.message, 'error');
                    } finally {
                        testBtn.disabled = false;
                        testBtn.textContent = 'Test MySQL Connection';
                    }
                };
            }

            // User accounts
            const users = await API.get('/settings/users');
            const userTbody = document.getElementById('set-users-tbody');
            if (userTbody && users) {
                userTbody.innerHTML = users.map(u => `
                    <tr>
                        <td><strong>${this.escape(u.username)}</strong></td>
                        <td>${this.escape(u.fullName)}</td>
                        <td><span class="status-pill status-admitted">${this.escape(u.role)}</span></td>
                        <td><span class="status-pill status-active">Active</span></td>
                    </tr>
                `).join('');
            }
        } catch (err) {
            console.error('Error loading settings:', err);
        }
    },

    // ==========================================
    // HELPERS & UTILITIES
    // ==========================================
    async populateDoctorDropdown(selectId) {
        const select = document.getElementById(selectId);
        if (!select) return;
        try {
            if (!this.allDoctors || this.allDoctors.length === 0) {
                this.allDoctors = await API.get('/doctors');
            }
            select.innerHTML = '<option value="">-- Select Physician --</option>' +
                this.allDoctors.map(d => `<option value="${d.fullName}">${d.fullName} (${d.department})</option>`).join('');
        } catch (e) {
            select.innerHTML = '<option value="Dr. On Duty">Dr. On Duty (General Medicine)</option>';
        }
    },

    setElText(id, text) {
        const el = document.getElementById(id);
        if (el) el.textContent = text;
    },

    escape(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    },

    openModal(modalId) {
        const m = document.getElementById(modalId);
        if (m) m.classList.add('open');
    },

    closeModal(modalId) {
        const m = document.getElementById(modalId);
        if (m) m.classList.remove('open');
    }
};

// Initialize App when DOM is ready
document.addEventListener('DOMContentLoaded', () => App.init());

