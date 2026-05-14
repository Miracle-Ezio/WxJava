const request = require('../../utils/request');
const app = getApp();

const LEVEL_LABELS = {
  STARDUST: '星尘',
  SILVER:   '银河',
  GOLD:     '星辰',
  NIGHT:    '夜空',
  NOVA:     '新星',
};

Page({
  data: {
    nickname: '',
    levelCode: 'STARDUST',
    levelLabel: '星尘',
    plans: [],
    loading: true,
    nextReminder: null,
  },

  onShow() {
    if (!app.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/index' });
      return;
    }
    this.setData({
      nickname: app.globalData.nickname,
      levelCode: app.globalData.levelCode || 'STARDUST',
      levelLabel: LEVEL_LABELS[app.globalData.levelCode] || '星尘',
    });
    this.loadPlans();
  },

  onPullDownRefresh() {
    this.loadPlans().finally(() => wx.stopPullDownRefresh());
  },

  loadPlans() {
    this.setData({ loading: true });
    return request.get('/api/plans/mine')
      .then(plans => {
        const list = (plans || []).map(p => ({
          ...p,
          doneItems: p.doneItems || 0,
          totalItems: p.totalItems || 0,
        }));
        // 演示：取首个执行中项作为"下一项提醒"
        let nextReminder = null;
        const inFlight = list.find(p => p.status === 2 || p.status === 4);
        if (inFlight) {
          nextReminder = {
            title: inFlight.title,
            desc: '请按顾问建议的周期完成下一次治疗',
          };
        }
        this.setData({ plans: list, nextReminder, loading: false });
      })
      .catch(() => this.setData({ loading: false }));
  },

  goPlanDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/plan/detail?id=' + id });
  },

  goPlanList() {
    wx.navigateTo({ url: '/pages/plan/list' });
  },

  onBook() {
    wx.switchTab({ url: '/pages/appointment/index' });
  },
});
