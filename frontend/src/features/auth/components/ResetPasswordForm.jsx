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
      <h1>Reset Password</h1>

      {error && <div className={styles['error-message']}>{error}</div>}
      {passwordError && <div className={styles['error-message']}>{passwordError}</div>}

      <div className={styles['input-box']}>
        <input
          type="password"
          placeholder="New password (8+ chars, uppercase, lowercase, number)"
          value={newPassword}
          onChange={(e) => onNewPasswordChange(e.target.value)}
          required
          disabled={loading}
        />
        <i className="bx bx-lock-alt"></i>
      </div>

      <button type="submit" className={styles.btn} disabled={loading}>
        {loading ? 'Resetting...' : 'Confirm'}
      </button>
      <button type="button" className={`${styles.btn} ${styles['btn-secondary']}`} onClick={onBack} disabled={loading}>
        Back
      </button>
    </form>
  );
};

export default ResetPasswordForm;
