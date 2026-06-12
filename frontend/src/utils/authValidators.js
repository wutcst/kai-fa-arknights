export function validateLoginForm({ username, password }) {
  if (!username.trim()) return '用户名不能为空';
  if (!password.trim()) return '密码不能为空';
  return '';
}

export function validateRegisterForm({ username, password, confirmPassword }) {
  if (!username.trim()) return '用户名不能为空';
  if (!password.trim()) return '密码不能为空';
  if (password !== confirmPassword) return '两次密码输入不一致';
  return '';
}

export function validateChangePasswordForm({
  username,
  oldPassword,
  newPassword,
  confirmNewPassword
}) {
  if (!username.trim()) return '用户名不能为空';
  if (!oldPassword.trim()) return '旧密码不能为空';
  if (!newPassword.trim()) return '新密码不能为空';
  if (newPassword !== confirmNewPassword) return '两次新密码输入不一致';
  if (newPassword === oldPassword) return '新密码不能与旧密码相同';
  return '';
}
