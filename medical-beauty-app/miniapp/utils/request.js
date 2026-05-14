/**
 * 通用请求封装
 *   - 自动带 Authorization: Bearer <token>
 *   - 统一返回 ApiResponse 解包：code === 0 返回 data，否则 reject(err)
 *   - 401 自动跳登录页
 */
const app = getApp();

function request(method, path, data, opts = {}) {
  return new Promise((resolve, reject) => {
    const header = {
      'Content-Type': 'application/json',
    };
    if (app.globalData.token) {
      header.Authorization = 'Bearer ' + app.globalData.token;
    }
    wx.request({
      url: app.globalData.apiBase + path,
      method,
      data,
      header,
      timeout: 15000,
      success: res => {
        const body = res.data || {};
        if (res.statusCode === 200 && body.code === 0) {
          resolve(body.data);
          return;
        }
        if (body.code === 40100 || body.code === 40101) {
          app.clearSession();
          if (!opts.silentAuth) {
            wx.reLaunch({ url: '/pages/login/index' });
          }
          reject(body);
          return;
        }
        wx.showToast({
          title: body.message || `请求失败 (${res.statusCode})`,
          icon: 'none',
          duration: 2000,
        });
        reject(body);
      },
      fail: err => {
        wx.showToast({ title: '网络异常', icon: 'none' });
        reject(err);
      },
    });
  });
}

/**
 * 上传文件（multipart/form-data），用于照片上传。
 * @param path        e.g. '/api/photos'
 * @param filePath    wx 本地临时文件路径
 * @param formData    其它表单字段，会一并 POST
 */
function upload(path, filePath, formData = {}) {
  return new Promise((resolve, reject) => {
    const header = {};
    if (app.globalData.token) {
      header.Authorization = 'Bearer ' + app.globalData.token;
    }
    wx.uploadFile({
      url: app.globalData.apiBase + path,
      filePath,
      name: 'file',
      header,
      formData,
      timeout: 60000,
      success: res => {
        let body = {};
        try { body = JSON.parse(res.data); } catch (e) { body = { code: -1, message: '响应解析失败' }; }
        if (res.statusCode === 200 && body.code === 0) {
          resolve(body.data);
          return;
        }
        if (body.code === 40100 || body.code === 40101) {
          app.clearSession();
          wx.reLaunch({ url: '/pages/login/index' });
        }
        wx.showToast({ title: body.message || '上传失败', icon: 'none' });
        reject(body);
      },
      fail: err => {
        wx.showToast({ title: '上传失败', icon: 'none' });
        reject(err);
      },
    });
  });
}

module.exports = {
  get:    (path, data, opts) => request('GET',    path, data, opts),
  post:   (path, data, opts) => request('POST',   path, data, opts),
  put:    (path, data, opts) => request('PUT',    path, data, opts),
  del:    (path, data, opts) => request('DELETE', path, data, opts),
  upload,
};
