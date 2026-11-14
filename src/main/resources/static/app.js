// AuraRoutine Frontend Application
document.addEventListener('DOMContentLoaded', () => {
  const state = {
    selectedDate: new Date().toISOString().split('T')[0],
    tasks: [],
    anchors: [],
    profile: null,
    habitDrift: null,
    aiStatus: null,
    activeFeedbackTaskId: null,
    selectedFeedbackRating: 'JUST_RIGHT'
  };

  // DOM Elements
  const dateInput = document.getElementById('current-date-input');
  const prevDayBtn = document.getElementById('prev-day-btn');
  const nextDayBtn = document.getElementById('next-day-btn');
  const smartScheduleBtn = document.getElementById('smart-schedule-btn');
  const exportIcsBtn = document.getElementById('export-ics-btn');
  const aiStatusBadge = document.getElementById('ai-status-badge');
  const aiModelLabel = document.getElementById('ai-model-label');
  const newTaskForm = document.getElementById('new-task-form');
  const timelineScroll = document.getElementById('timeline-scroll-container');
  const timelineDateTitle = document.getElementById('timeline-date-title');
  const timelineSubtitle = document.getElementById('timeline-subtitle');
  const overallAlignmentBadge = document.getElementById('overall-alignment-badge');
  const anchorsList = document.getElementById('anchors-list');
  const circadianChart = document.getElementById('circadian-chart');
  const chronotypeBadge = document.getElementById('chronotype-badge');
  const multipliersList = document.getElementById('multipliers-list');
  const metricDriftPct = document.getElementById('metric-drift-pct');
  const metricAccuracyPct = document.getElementById('metric-accuracy-pct');
  const habitInsightsContainer = document.getElementById('habit-insights-container');
  const routineUploadText = document.getElementById('routine-upload-text');
  const submitRoutineUploadBtn = document.getElementById('submit-routine-upload-btn');
  const loadSampleRoutineBtn = document.getElementById('load-sample-routine-btn');
  const loadSampleTextBtn = document.getElementById('load-sample-text-btn');

  // Modal elements
  const feedbackModal = document.getElementById('feedback-modal');
  const modalTaskTitle = document.getElementById('modal-task-title');
  const modalActualMinutes = document.getElementById('modal-actual-minutes');
  const modalNotes = document.getElementById('modal-notes');
  const closeFeedbackModalBtn = document.getElementById('close-feedback-modal-btn');
  const saveFeedbackBtn = document.getElementById('save-feedback-btn');
  const feedbackOptionBtns = document.querySelectorAll('.feedback-btn');

  // Initialize
  dateInput.value = state.selectedDate;
  updateExportLink();
  initEventListeners();
  loadAll();

  function initEventListeners() {
    dateInput.addEventListener('change', (e) => {
      state.selectedDate = e.target.value;
      updateExportLink();
      loadTasks();
    });

    prevDayBtn.addEventListener('click', () => shiftDate(-1));
    nextDayBtn.addEventListener('click', () => shiftDate(1));

    smartScheduleBtn.addEventListener('click', handleSmartSchedule);
    newTaskForm.addEventListener('submit', handleCreateTask);

    loadSampleRoutineBtn.addEventListener('click', () => {
      routineUploadText.value =
`Name,StartTime,EndTime,Category,Flexible
Morning Walk & Hydration,06:30,07:15,HEALTH_FITNESS,false
Breakfast & News,07:15,08:00,REST_LEISURE,false
Daily Engineering Standup,09:30,10:00,ADMINISTRATIVE,false
Lunch & Power Walk,12:30,13:30,REST_LEISURE,false
Gym Strength Session,17:30,18:45,HEALTH_FITNESS,true
Dinner & Family Time,19:30,20:30,REST_LEISURE,false
Night Reading & Sleep,22:45,23:30,REST_LEISURE,false`;
    });

    loadSampleTextBtn.addEventListener('click', () => {
      routineUploadText.value =
`Here is my typical weekday routine:
I usually wake up around 6:45 AM and prepare coffee.
Gym or morning run is from 7:15 AM to 8:15 AM.
My team standup is fixed at 10:00 AM every day.
I like eating lunch around 12:45 PM to 1:45 PM.
My afternoon energy dips between 2pm and 3:30pm, so I prefer light tasks or meetings then.
Evening workout or family walk around 6:00 PM.
Dinner around 8:00 PM and bedtime by 11:15 PM.`;
    });

    submitRoutineUploadBtn.addEventListener('click', handleRoutineUpload);

    // Modal events
    closeFeedbackModalBtn.addEventListener('click', () => {
      feedbackModal.classList.remove('active');
    });

    feedbackOptionBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        feedbackOptionBtns.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        state.selectedFeedbackRating = btn.getAttribute('data-rating');
      });
    });

    saveFeedbackBtn.addEventListener('click', handleSaveFeedback);
  }

  function shiftDate(days) {
    const d = new Date(state.selectedDate);
    d.setDate(d.getDate() + days);
    state.selectedDate = d.toISOString().split('T')[0];
    dateInput.value = state.selectedDate;
    updateExportLink();
    loadTasks();
  }

  function updateExportLink() {
    exportIcsBtn.href = `/api/calendar/export.ics?date=${state.selectedDate}`;
  }

  async function loadAll() {
    await Promise.all([
      checkAiStatus(),
      loadProfile(),
      loadAnchors(),
      loadTasks(),
      loadHabitDrift()
    ]);
  }

  async function checkAiStatus() {
    try {
      const res = await fetch('/api/ai/status');
      const data = await res.json();
      state.aiStatus = data;

      const dot = aiStatusBadge.querySelector('.status-dot');
      if (data.ollamaAvailable) {
        dot.className = 'status-dot';
        aiModelLabel.textContent = `Ollama: ${data.activeModel}`;
        aiStatusBadge.title = `Connected to local open-source LLM (${data.activeModel})`;
      } else {
        dot.className = 'status-dot offline';
        aiModelLabel.textContent = 'Java Heuristics Engine';
        aiStatusBadge.title = 'Pure Java Circadian Engine active. (Start Ollama for neural mode)';
      }
    } catch (err) {
      console.warn('AI Status check error', err);
    }
  }

  async function loadProfile() {
    try {
      const res = await fetch('/api/routine/profile');
      state.profile = await res.json();
      renderCircadianChart();
      renderMultipliers();
      if (state.profile.chronotype) {
        chronotypeBadge.textContent = state.profile.chronotype;
      }
    } catch (err) {
      console.error('Failed to load profile', err);
    }
  }

  async function loadAnchors() {
    try {
      const res = await fetch('/api/routine/anchors');
      state.anchors = await res.json();
      renderAnchors();
    } catch (err) {
      console.error('Failed to load anchors', err);
    }
  }

  async function loadTasks() {
    try {
      timelineDateTitle.innerHTML = `<span>🗓️</span> Daily Schedule (${formatDisplayDate(state.selectedDate)})`;
      const res = await fetch(`/api/tasks/day?date=${state.selectedDate}`);
      state.tasks = await res.json();
      renderTimeline();
    } catch (err) {
      console.error('Failed to load tasks', err);
    }
  }

  async function loadHabitDrift() {
    try {
      const res = await fetch('/api/routine/drift');
      state.habitDrift = await res.json();
      renderHabitDrift();
    } catch (err) {
      console.error('Failed to load drift report', err);
    }
  }

  function renderAnchors() {
    anchorsList.innerHTML = '';
    if (!state.anchors || state.anchors.length === 0) {
      anchorsList.innerHTML = '<p style="font-size: 0.8rem; color: var(--text-dim);">No routine anchors defined.</p>';
      return;
    }

    state.anchors.forEach(a => {
      const item = document.createElement('div');
      item.className = 'anchor-item';
      item.innerHTML = `
        <div>
          <strong style="color: white;">${escapeHtml(a.name)}</strong>
          <div style="font-size: 0.72rem; color: var(--text-dim);">${a.category || 'ROUTINE'} ${a.flexible ? '(Flexible)' : '(Fixed)'}</div>
        </div>
        <span class="anchor-time">${a.startTime} - ${a.endTime}</span>
      `;
      anchorsList.appendChild(item);
    });
  }

  function renderTimeline() {
    timelineScroll.innerHTML = '';

    // Calculate wake & sleep range, default 07:00 to 23:00
    let startHour = 7;
    let endHour = 23;
    if (state.profile && state.profile.wakeTime) {
      startHour = Math.max(0, parseInt(state.profile.wakeTime.split(':')[0]));
    }
    if (state.profile && state.profile.sleepTime) {
      endHour = Math.min(24, parseInt(state.profile.sleepTime.split(':')[0]) + 1);
    }

    // Check overall scheduled status
    let scheduledCount = 0;
    state.tasks.forEach(t => {
      if (t.scheduledStartTime) scheduledCount++;
    });

    if (scheduledCount === 0 && state.tasks.length > 0) {
      timelineSubtitle.textContent = `${state.tasks.length} pending tasks need AI scheduling. Click "Smart Schedule Day".`;
    } else {
      timelineSubtitle.textContent = `${scheduledCount} tasks time-blocked around routine anchors.`;
    }

    for (let h = startHour; h <= endHour; h++) {
      const hourStr = h < 10 ? `0${h}:00` : `${h}:00`;
      const nextHourStr = (h + 1) < 10 ? `0${h + 1}:00` : `${h + 1}:00`;

      const slotRow = document.createElement('div');
      slotRow.className = 'timeline-hour-slot';

      const label = document.createElement('div');
      label.className = 'hour-label';
      label.textContent = hourStr;

      const content = document.createElement('div');
      content.className = 'hour-content';

      // 1. Routine anchors falling in this hour
      state.anchors.forEach(a => {
        if (a.startTime && isTimeInHour(a.startTime, h)) {
          const aBlock = document.createElement('div');
          aBlock.className = 'task-block anchor';
          aBlock.innerHTML = `
            <div class="task-top">
              <span class="task-title" style="color: #38bdf8;">⚓ ${escapeHtml(a.name)}</span>
              <span class="badge" style="background: rgba(56, 189, 248, 0.15); color: #38bdf8;">Fixed Routine</span>
            </div>
            <div class="task-meta">
              <span class="task-time-range">${a.startTime} - ${a.endTime}</span>
              <span>Non-negotiable</span>
            </div>
          `;
          content.appendChild(aBlock);
        }
      });

      // 2. Scheduled Tasks in this hour
      state.tasks.forEach(t => {
        if (t.scheduledStartTime && isTimeInHour(t.scheduledStartTime, h)) {
          const tBlock = document.createElement('div');
          const isCompleted = t.status === 'COMPLETED';
          tBlock.className = `task-block ${isCompleted ? 'completed' : ''}`;

          const catClass = getCategoryBadgeClass(t.category);
          const energyBadge = t.energyDemand === 'HIGH'
            ? '<span class="badge" style="background: rgba(244, 63, 94, 0.2); color: #fb7185;">🔥 High Energy</span>'
            : (t.energyDemand === 'LOW'
                ? '<span class="badge" style="background: rgba(16, 185, 129, 0.2); color: #34d399;">🌱 Low Energy</span>'
                : '<span class="badge" style="background: rgba(99, 102, 241, 0.2); color: #818cf8;">⚡ Balanced</span>');

          tBlock.innerHTML = `
            <div class="task-top">
              <div style="display: flex; align-items: center; gap: 0.5rem;">
                <input type="checkbox" class="task-complete-check" data-id="${t.id}" ${isCompleted ? 'checked disabled' : ''}>
                <span class="task-title">${escapeHtml(t.title)}</span>
              </div>
              <div class="task-badges">
                <span class="badge ${catClass}">${t.category || 'DEEP_WORK'}</span>
                ${energyBadge}
              </div>
            </div>
            <div class="task-meta">
              <span class="task-time-range">${t.scheduledStartTime} - ${t.scheduledEndTime || '--'} (${t.estimatedMinutes}m)</span>
              <span style="font-weight: 500; color: ${t.priority === 'HIGH' ? '#f43f5e' : '#94a3b8'};">Priority: ${t.priority}</span>
            </div>
            ${t.reasoning ? `<div class="ai-reasoning-tag"><span>🤖</span> ${escapeHtml(t.reasoning)}</div>` : ''}
          `;

          // Completion click handler
          const check = tBlock.querySelector('.task-complete-check');
          if (check && !isCompleted) {
            check.addEventListener('change', () => openFeedbackModal(t));
          }

          content.appendChild(tBlock);
        }
      });

      slotRow.appendChild(label);
      slotRow.appendChild(content);
      timelineScroll.appendChild(slotRow);
    }
  }

  function isTimeInHour(timeStr, hour) {
    if (!timeStr) return false;
    const h = parseInt(timeStr.split(':')[0], 10);
    return h === hour;
  }

  function getCategoryBadgeClass(cat) {
    switch (cat) {
      case 'DEEP_WORK': return 'badge-deep';
      case 'CREATIVE': return 'badge-creative';
      case 'ADMINISTRATIVE': return 'badge-admin';
      case 'HEALTH_FITNESS': return 'badge-health';
      case 'LEARNING': return 'badge-learning';
      case 'ERRAND': return 'badge-errand';
      default: return 'badge-deep';
    }
  }

  function renderCircadianChart() {
    circadianChart.innerHTML = '';
    if (!state.profile || !state.profile.hourlyProductivityScores) return;

    const scores = state.profile.hourlyProductivityScores;
    const peakHours = state.profile.peakCognitiveHours || [];
    const dipHours = state.profile.postLunchDipHours || [];

    for (let h = 0; h < 24; h++) {
      const score = scores[h] !== undefined ? scores[h] : 0.2;
      const pct = Math.max(10, Math.round(score * 100));

      const barWrap = document.createElement('div');
      barWrap.className = 'chart-bar-wrap';
      barWrap.title = `${h}:00 - Cognitive Energy: ${pct}%`;

      const bar = document.createElement('div');
      bar.className = 'chart-bar';
      if (peakHours.includes(h)) bar.classList.add('peak');
      if (dipHours.includes(h)) bar.classList.add('dip');
      bar.style.height = `${pct}%`;

      barWrap.appendChild(bar);

      if (h % 3 === 0) {
        const tick = document.createElement('div');
        tick.className = 'chart-hour-tick';
        tick.textContent = `${h}h`;
        barWrap.appendChild(tick);
      }

      circadianChart.appendChild(barWrap);
    }
  }

  function renderMultipliers() {
    multipliersList.innerHTML = '';
    if (!state.profile || !state.profile.categoryDurationMultipliers) return;

    const multipliers = state.profile.categoryDurationMultipliers;
    Object.keys(multipliers).forEach(cat => {
      const val = multipliers[cat];
      const diff = Math.round((val - 1.0) * 100);
      let diffLabel = 'On Track (1.0x)';
      let diffColor = 'var(--text-muted)';
      if (diff > 5) {
        diffLabel = `+${diff}% buffer added (${val}x)`;
        diffColor = '#f59e0b';
      } else if (diff < -5) {
        diffLabel = `${diff}% quick pace (${val}x)`;
        diffColor = '#10b981';
      }

      const item = document.createElement('div');
      item.style.display = 'flex';
      item.style.justifyContent = 'space-between';
      item.style.fontSize = '0.78rem';
      item.style.marginBottom = '0.4rem';
      item.style.padding = '0.3rem 0.5rem';
      item.style.background = 'rgba(15, 23, 42, 0.4)';
      item.style.borderRadius = '6px';
      item.innerHTML = `
        <span style="color: white; font-weight: 500;">${cat}</span>
        <span style="color: ${diffColor}; font-weight: 600;">${diffLabel}</span>
      `;
      multipliersList.appendChild(item);
    });
  }

  function renderHabitDrift() {
    if (!state.habitDrift) return;

    metricDriftPct.textContent = `${state.habitDrift.overallDriftPercentage || 0}%`;
    if (state.habitDrift.overallDriftPercentage > 25) {
      metricDriftPct.className = 'metric-val warn';
    } else {
      metricDriftPct.className = 'metric-val good';
    }

    metricAccuracyPct.textContent = `${state.habitDrift.durationAccuracyPercentage || 100}%`;

    habitInsightsContainer.innerHTML = '';

    // Detected drifts
    if (state.habitDrift.detectedDrifts) {
      state.habitDrift.detectedDrifts.forEach(d => {
        const item = document.createElement('div');
        item.className = 'insight-item warn';
        item.innerHTML = `<span>⚠️</span> <span>${escapeHtml(d)}</span>`;
        habitInsightsContainer.appendChild(item);
      });
    }

    // Actionable recommendations
    if (state.habitDrift.actionableRecommendations) {
      state.habitDrift.actionableRecommendations.forEach(r => {
        const item = document.createElement('div');
        item.className = 'insight-item';
        item.innerHTML = `<span>💡</span> <span>${escapeHtml(r)}</span>`;
        habitInsightsContainer.appendChild(item);
      });
    }

    // Strength habits
    if (state.habitDrift.strengthHabits) {
      state.habitDrift.strengthHabits.forEach(s => {
        const item = document.createElement('div');
        item.className = 'insight-item success';
        item.innerHTML = `<span>🌟</span> <span>${escapeHtml(s)}</span>`;
        habitInsightsContainer.appendChild(item);
      });
    }
  }

  async function handleSmartSchedule() {
    smartScheduleBtn.disabled = true;
    smartScheduleBtn.innerHTML = '<span>⏳</span> Optimizing...';

    try {
      const res = await fetch('/api/tasks/schedule', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          date: state.selectedDate,
          bufferMinutes: 15,
          energyAware: true,
          autoExtendDuration: true
        })
      });

      const plan = await res.json();
      state.tasks = plan.scheduledTasks || [];
      renderTimeline();

      if (plan.overallEnergyAlignmentScore !== undefined) {
        overallAlignmentBadge.textContent = `Circadian Alignment: ${plan.overallEnergyAlignmentScore}%`;
      }

      const engineName = plan.engineUsed || 'AI Engine';
      showToast(`Schedule generated by ${engineName}!`);
    } catch (err) {
      console.error('Schedule error', err);
      alert('Failed to schedule tasks.');
    } finally {
      smartScheduleBtn.disabled = false;
      smartScheduleBtn.innerHTML = '<span>✨</span> Smart Schedule Day';
    }
  }

  async function handleCreateTask(e) {
    e.preventDefault();
    const title = document.getElementById('task-title').value.trim();
    const category = document.getElementById('task-category').value;
    const estimatedMinutes = parseInt(document.getElementById('task-est-mins').value, 10);
    const priority = document.getElementById('task-priority').value;
    const energyDemand = document.getElementById('task-energy').value;

    if (!title) return;

    try {
      const payload = {
        title,
        category,
        estimatedMinutes,
        priority,
        energyDemand,
        scheduledDate: state.selectedDate
      };

      const res = await fetch('/api/tasks', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const savedTask = await res.json();
      state.tasks.push(savedTask);
      renderTimeline();
      document.getElementById('task-title').value = '';
      showToast('Task added! Click "Smart Schedule Day" to position it.');
    } catch (err) {
      console.error('Failed to create task', err);
    }
  }

  function openFeedbackModal(task) {
    state.activeFeedbackTaskId = task.id;
    modalTaskTitle.textContent = `Task: ${task.title}`;
    modalActualMinutes.value = task.estimatedMinutes || 45;
    feedbackModal.classList.add('active');
  }

  async function handleSaveFeedback() {
    if (!state.activeFeedbackTaskId) return;

    const actualMinutes = parseInt(modalActualMinutes.value, 10);
    const notes = modalNotes.value.trim();

    try {
      const res = await fetch('/api/tasks/feedback', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          taskId: state.activeFeedbackTaskId,
          actualMinutes,
          feedbackRating: state.selectedFeedbackRating,
          notes
        })
      });

      const updatedTask = await res.json();
      feedbackModal.classList.remove('active');

      // Update task in state
      const idx = state.tasks.findIndex(t => t.id === updatedTask.id);
      if (idx !== -1) state.tasks[idx] = updatedTask;

      renderTimeline();

      // Refresh learned profile & analytics
      await Promise.all([loadProfile(), loadHabitDrift()]);
      showToast('AI profile updated with your completion feedback!');
    } catch (err) {
      console.error('Failed to save feedback', err);
    }
  }

  async function handleRoutineUpload() {
    const content = routineUploadText.value.trim();
    if (!content) {
      alert('Please paste routine text or upload a CSV/JSON/ICS file.');
      return;
    }

    submitRoutineUploadBtn.disabled = true;
    submitRoutineUploadBtn.innerHTML = '<span>🧠</span> Analyzing routine...';

    try {
      const res = await fetch('/api/routine/upload', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          format: 'AUTO',
          content,
          autoApplyToProfile: true
        })
      });

      const result = await res.json();
      showToast(`Routine parsed by ${result.engineUsed}! Extracted ${result.extractedAnchors.length} anchors.`);

      await Promise.all([loadAnchors(), loadProfile(), loadHabitDrift()]);
      renderTimeline();
    } catch (err) {
      console.error('Upload routine error', err);
      alert('Failed to parse routine.');
    } finally {
      submitRoutineUploadBtn.disabled = false;
      submitRoutineUploadBtn.innerHTML = '🧠 Analyze & Learn Routine';
    }
  }

  function formatDisplayDate(dateStr) {
    const today = new Date().toISOString().split('T')[0];
    if (dateStr === today) return 'Today';
    return dateStr;
  }

  function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, m => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[m]);
  }

  function showToast(msg) {
    const toast = document.createElement('div');
    toast.style.position = 'fixed';
    toast.style.bottom = '2rem';
    toast.style.right = '2rem';
    toast.style.background = 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)';
    toast.style.color = 'white';
    toast.style.padding = '0.75rem 1.25rem';
    toast.style.borderRadius = '10px';
    toast.style.boxShadow = '0 10px 25px rgba(0,0,0,0.5)';
    toast.style.fontSize = '0.85rem';
    toast.style.fontWeight = '500';
    toast.style.zIndex = '9999';
    toast.style.animation = 'fadeIn 0.2s ease-out';
    toast.textContent = msg;
    document.body.appendChild(toast);
    setTimeout(() => {
      toast.remove();
    }, 3500);
  }
});
