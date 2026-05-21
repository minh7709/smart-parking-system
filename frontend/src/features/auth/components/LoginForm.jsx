import styles from '../pages/LoginPage.module.css';

const LoginForm = ({
  username,
  password,
  rememberMe,
  loading,
  error,
  onUsernameChange,
  onPasswordChange,
  onRememberMeChange,
  onSubmit,
  onForgotPassword,
}) => {
  return (
    <form onSubmit={onSubmit}>
      <h1>Đăng nhập</h1>

      {error && <div className={styles['error-message']}>{error}</div>}

      <div className={styles['input-box']}>
        <input
          type="text"
          placeholder="Tên đăng nhập"
          required
          value={username}
          onChange={(e) => onUsernameChange(e.target.value)}
          disabled={loading}
        />
        <i className="bx bx-user"></i>
      </div>

      <div className={styles['input-box']}>
        <input
          type="password"
          placeholder="Mật khẩu"
          required
          value={password}
          onChange={(e) => onPasswordChange(e.target.value)}
          disabled={loading}
        />
        <i className="bx bx-lock"></i>
      </div>

      <div className={styles['remember-forgot']}>
        <label>
          <input
            type="checkbox"
            checked={rememberMe}
            onChange={(e) => onRememberMeChange(e.target.checked)}
            disabled={loading}
          />{' '}
          Ghi nhớ đăng nhập
        </label>
        <button
          type="button"
          className={styles['text-link']}
          onClick={onForgotPassword}
          disabled={loading}
        >
          Quên mật khẩu?
        </button>
      </div>

      <button type="submit" className={styles.btn} disabled={loading}>
        {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
      </button>
    </form>
  );
};

export default LoginForm;
