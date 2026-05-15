const request = require('../../utils/request');
const subscribe = require('../../utils/subscribe');
const { fmtDateTime, nextDays } = require('../../utils/datetime');

const DEFAULT_STORE_ID = 1; // 一期单门店；多店时从客户主门店 / 选择器拿

Page({
  data: {
    step: 1,
    projects: [],
    dates: nextDays(14),
    selectedProject: null,
    selectedDate: '',
    selectedTime: '',
    availability: { slots: [] },
    customerNote: '',
    submitting: false,
    canNext: false,
  },

  onLoad(query) {
    // 支持从规划详情 / 首页提醒直跳，预选项目
    const preProjectId = query.projectId ? Number(query.projectId) : null;
    this.loadProjects(preProjectId);
  },

  loadProjects(preProjectId) {
    request.get('/api/projects')
      .then(list => {
        const projects = list || [];
        this.setData({ projects });
        if (preProjectId) {
          const p = projects.find(x => x.id === preProjectId);
          if (p) this.applyProject(p);
        }
      });
  },

  // ─────────────── 步骤 1：项目 ───────────────
  onSelectProject(e) {
    const id = Number(e.currentTarget.dataset.id);
    const p = this.data.projects.find(x => x.id === id);
    if (p) this.applyProject(p);
  },
  applyProject(p) {
    this.setData({ selectedProject: p, canNext: true });
  },

  // ─────────────── 步骤 2：日期 ───────────────
  onSelectDate(e) {
    const date = e.currentTarget.dataset.date;
    this.setData({ selectedDate: date, canNext: true, selectedTime: '' });
    this.loadAvailability(date);
  },

  loadAvailability(date) {
    if (!this.data.selectedProject || !date) return;
    const url = `/api/appointments/availability?storeId=${DEFAULT_STORE_ID}`
              + `&projectId=${this.data.selectedProject.id}&date=${date}`;
    request.get(url)
      .then(data => this.setData({ availability: data || { slots: [] } }))
      .catch(() => this.setData({ availability: { slots: [] } }));
  },

  // ─────────────── 步骤 3：时段 ───────────────
  onSelectTime(e) {
    const { time, available } = e.currentTarget.dataset;
    if (!available) {
      wx.showToast({ title: '该时段已约满', icon: 'none' });
      return;
    }
    this.setData({ selectedTime: time, canNext: true });
  },

  // ─────────────── 步骤 4：备注 ───────────────
  onNoteInput(e) {
    this.setData({ customerNote: e.detail.value });
  },

  // ─────────────── 步骤切换 ───────────────
  onNext() {
    const { step, selectedProject, selectedDate, selectedTime } = this.data;
    if (step === 1 && !selectedProject) return;
    if (step === 2) {
      if (!selectedDate) { wx.showToast({ title: '请选日期', icon: 'none' }); return; }
      this.loadAvailability(selectedDate);
    }
    if (step === 3 && !selectedTime) { wx.showToast({ title: '请选时段', icon: 'none' }); return; }

    const next = step + 1;
    this.setData({
      step: next,
      // 进入下一步时按当前状态预判 canNext
      canNext:
        (next === 2 && !!selectedDate) ||
        (next === 3 && !!selectedTime) ||
        (next === 4),
    });
  },

  onBack() {
    if (this.data.step === 1) {
      wx.navigateBack();
      return;
    }
    this.setData({ step: this.data.step - 1, canNext: true });
  },

  // ─────────────── 提交 ───────────────
  onSubmit() {
    const { selectedProject, selectedDate, selectedTime, customerNote } = this.data;
    if (!selectedProject || !selectedDate || !selectedTime) return;

    this.setData({ submitting: true });
    // 提交前先引导订阅消息授权（微信要求在 tap 同一帧内调用）
    subscribe.requestForAppointment().finally(() => {
      request.post('/api/appointments', {
        projectId: selectedProject.id,
        storeId: DEFAULT_STORE_ID,
        startAt: fmtDateTime(selectedDate, selectedTime),
        customerNote,
      })
        .then(appt => {
          wx.showToast({ title: '已提交，等待门店确认', icon: 'success' });
          setTimeout(() => {
            wx.redirectTo({ url: '/pages/appointment/detail?id=' + appt.id });
          }, 800);
        })
        .catch(() => this.setData({ submitting: false }));
    });
  },
});
