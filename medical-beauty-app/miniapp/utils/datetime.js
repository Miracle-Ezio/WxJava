function pad2(n) { return n < 10 ? '0' + n : '' + n; }

/** "yyyy-MM-dd" */
function fmtDate(d) {
  if (!d) return '';
  if (typeof d === 'string') return d.slice(0, 10);
  return d.getFullYear() + '-' + pad2(d.getMonth() + 1) + '-' + pad2(d.getDate());
}

/** "yyyy-MM-ddTHH:mm:ss" — 兼容后端 LocalDateTime ISO 解析 */
function fmtDateTime(date, hhmm) {
  return fmtDate(date) + 'T' + hhmm + ':00';
}

const ZH_WEEK = ['日', '一', '二', '三', '四', '五', '六'];

/** 返回从今天起 days 天的日期数组（含今天） */
function nextDays(days) {
  const result = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  for (let i = 0; i < days; i++) {
    const d = new Date(today.getTime() + i * 86400000);
    result.push({
      date: fmtDate(d),
      day: d.getDate(),
      month: d.getMonth() + 1,
      weekLabel: i === 0 ? '今天' : (i === 1 ? '明天' : '周' + ZH_WEEK[d.getDay()]),
      isToday: i === 0,
    });
  }
  return result;
}

function relativeFromNow(isoStr) {
  if (!isoStr) return '';
  const t = new Date(isoStr.replace(' ', 'T')).getTime();
  const diff = t - Date.now();
  const min = Math.round(diff / 60000);
  if (min < -60) return '已过期';
  if (min < 0) return '刚刚';
  if (min < 60) return min + ' 分钟后';
  const hr = Math.round(min / 60);
  if (hr < 24) return hr + ' 小时后';
  const d = Math.round(hr / 24);
  return d + ' 天后';
}

module.exports = { fmtDate, fmtDateTime, nextDays, relativeFromNow };
