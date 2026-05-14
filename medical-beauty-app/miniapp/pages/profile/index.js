const app = getApp();

const LEVEL_LABELS = {
  STARDUST: '星尘', SILVER: '银河', GOLD: '星辰', NIGHT: '夜空', NOVA: '新星',
};

Page({
  data: {
    nickname: '',
    avatarUrl: '',
    levelCode: 'STARDUST',
    levelLabel: '星尘',
  },

  onShow() {
    if (!app.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/index' });
      return;
    }
    const g = app.globalData;
    this.setData({
      nickname: g.nickname,
      avatarUrl: g.avatarUrl,
      levelCode: g.levelCode || 'STARDUST',
      levelLabel: LEVEL_LABELS[g.levelCode] || '星尘',
    });
  },

  goPlanList()  { wx.navigateTo({ url: '/pages/plan/list' }); },
  comingSoon()  { wx.showToast({ title: '即将上线', icon: 'none' }); },
  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出吗？',
      success: r => {
        if (r.confirm) {
          app.clearSession();
          wx.reLaunch({ url: '/pages/login/index' });
        }
      },
    });
  },
});
