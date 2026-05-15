const auth = require('../../utils/auth');
const app = getApp();

Page({
  data: {
    loading: false,
    mockMode: false,
  },

  onLoad() {
    this.setData({ mockMode: !!app.globalData.mockMode });
  },

  onGetUserInfo(e) {
    const profile = (e.detail && e.detail.userInfo) || {};
    this.doLogin(profile);
  },

  onMockLogin() {
    this.doLogin({ nickName: '彭蕾', avatarUrl: '' });
  },

  doLogin(profile) {
    this.setData({ loading: true });
    auth.loginByCode(profile)
      .then(data => {
        wx.showToast({
          title: data.newCustomer ? '欢迎加入 STARRY' : '欢迎回来',
          icon: 'none',
        });
        wx.switchTab({ url: '/pages/home/index' });
      })
      .catch(err => { console.error(err); })
      .finally(() => this.setData({ loading: false }));
  },

  onProtocol() {
    wx.showModal({
      title: '协议',
      content: '一期演示版，协议页未实现。',
      showCancel: false,
    });
  },
});
