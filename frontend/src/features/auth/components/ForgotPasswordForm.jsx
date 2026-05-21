import styles from '../pages/LoginPage.module.css';

const ForgotPasswordForm = ({
  phone,
  loading,
  error,
  phoneError,
  onPhoneChange,
  onSubmit,
  onBack,
}) => {
  return (
    <form onSubmit={onSubmit}>
      <h1>Quên mật khẩu</h1>

      {error && <div className={styles['error-message']}>{error}</div>}
      {phoneError && <div className={styles['error-message']}>{phoneError}</div>}

      <div className={styles['input-box']}>
        <input
          type="text"
          placeholder="Nhập số điện thoại"
          value={phone}
          onChange={(e) => onPhoneChange(e.target.value)}
          required
          disabled={loading}
        />
        <i className="bx bx-phone"></i>
      </div>

      <button type="submit" className={styles.btn} disabled={loading}>
        {loading ? 'Đang gửi...' : 'Gửi OTP'}
      </button>
      <button type="button" className={`${styles.btn} ${styles['btn-secondary']}`} onClick={onBack} disabled={loading}>
        Quay lại đăng nhập
      </button>
    </form>
  );
};

export default ForgotPasswordForm;
