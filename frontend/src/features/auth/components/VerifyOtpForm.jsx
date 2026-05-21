import styles from '../pages/LoginPage.module.css';

const VerifyOtpForm = ({ otp, loading, error, otpError, onOtpChange, onSubmit, onBack }) => {
  return (
    <form onSubmit={onSubmit}>
      <h1>Xác minh OTP</h1>

      {error && <div className={styles['error-message']}>{error}</div>}
      {otpError && <div className={styles['error-message']}>{otpError}</div>}

      <div className={styles['input-box']}>
        <input
          type="text"
          placeholder="Nhập OTP 6 chữ số"
          inputMode="numeric"
          maxLength={6}
          value={otp}
          onChange={(e) => {
            const value = e.target.value;
            if (/^\d{0,6}$/.test(value)) {
              onOtpChange(value);
            }
          }}
          required
          disabled={loading}
        />
        <i className="bx bx-shield"></i>
      </div>

      <button type="submit" className={styles.btn} disabled={loading}>
        {loading ? 'Đang xác minh...' : 'Xác minh OTP'}
      </button>
      <button type="button" className={`${styles.btn} ${styles['btn-secondary']}`} onClick={onBack} disabled={loading}>
        Quay lại
      </button>
    </form>
  );
};

export default VerifyOtpForm;
