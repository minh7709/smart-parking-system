import styles from '../pages/LoginPage.module.css';

const ResetPasswordForm = ({
  newPassword,
  loading,
  error,
  passwordError,
  onNewPasswordChange,
  onSubmit,
  onBack,
}) => {
  return (
    <form onSubmit={onSubmit}>
      <h1>Đặt lại mật khẩu</h1>

      {error && <div className={styles['error-message']}>{error}</div>}
      {passwordError && <div className={styles['error-message']}>{passwordError}</div>}

      <div className={styles['input-box']}>
        <input
          type="password"
          placeholder="Mật khẩu mới (tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số)"
          value={newPassword}
          onChange={(e) => onNewPasswordChange(e.target.value)}
          required
          disabled={loading}
        />
        <i className="bx bx-lock-alt"></i>
      </div>

      <button type="submit" className={styles.btn} disabled={loading}>
        {loading ? 'Đang đặt lại...' : 'Xác nhận'}
      </button>
      <button type="button" className={`${styles.btn} ${styles['btn-secondary']}`} onClick={onBack} disabled={loading}>
        Quay lại
      </button>
    </form>
  );
};

export default ResetPasswordForm;
