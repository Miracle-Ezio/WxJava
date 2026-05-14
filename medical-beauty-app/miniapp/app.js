// app.js
App({
  globalData: {
    /** 后端 base URL，发布时改为生产域名 */
    apiBase: 'https://api.starry-mb.example.com',
    /** 登录态 */
    token: '',
    customerId: null,
    nickname: '',
    avatarUrl: '',
    levelCode: '',
  },

  onLaunch() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      this.globalData.customerId = wx.getStorageSync('customerId');
      this.globalData.nickname   = wx.getStorageSync('nickname');
      this.globalData.avatarUrl  = wx.getStorageSync('avatarUrl');
      this.globalData.levelCode  = wx.getStorageSync('levelCode');
    }
  },

  /** 写入登录态 */
  setSession(loginResp) {
    const { token, customerId, nickname, avatarUrl, levelCode } = loginResp;
    this.globalData.token      = token;
    this.globalData.customerId = customerId;
    this.globalData.nickname   = nickname || '';
    this.globalData.avatarUrl  = avatarUrl || '';
    this.globalData.levelCode  = levelCode || 'STARDUST';

    wx.setStorageSync('token', token);
    wx.setStorageSync('customerId', customerId);
    wx.setStorageSync('nickname', nickname || '');
    wx.setStorageSync('avatarUrl', avatarUrl || '');
    wx.setStorageSync('levelCode', levelCode || 'STARDUST');
  },

  /** 清登录态 */
  clearSession() {
    this.globalData.token = '';
    this.globalData.customerId = null;
    ['token', 'customerId', 'nickname', 'avatarUrl', 'levelCode'].forEach(k => wx.removeStorageSync(k));
  },

  isLoggedIn() {
    return !!this.globalData.token;
  },
});
