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
      <h1>Login</h1>

      {error && <div className={styles['error-message']}>{error}</div>}

      <div className={styles['input-box']}>
        <input
          type="text"
          placeholder="Username"
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
          placeholder="Password"
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
          Remember Me
        </label>
        <button
          type="button"
          className={styles['text-link']}
          onClick={onForgotPassword}
          disabled={loading}
        >
          Forgot Password?
        </button>
      </div>

      <button type="submit" className={styles.btn} disabled={loading}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
};

export default LoginForm;
