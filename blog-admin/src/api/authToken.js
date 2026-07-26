/**
 * Access Token 只驻留当前标签页内存。刷新页面后由 HttpOnly Refresh Cookie 恢复会话，
 * 不将任何 Token 写入 localStorage 或 sessionStorage。
 */
let accessToken = ''

export const getAccessToken = () => accessToken
export const setAccessToken = (value = '') => { accessToken = value }
