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
      <h1>Forgot Password</h1>

      {error && <div className="error-message">{error}</div>}
      {phoneError && <div className="error-message">{phoneError}</div>}

      <div className="input-box">
        <input
          type="text"
          placeholder="Nhap so dien thoai"
          value={phone}
          onChange={(e) => onPhoneChange(e.target.value)}
          required
          disabled={loading}
        />
        <i className="bx bx-phone"></i>
      </div>

      <button type="submit" className="btn" disabled={loading}>
        {loading ? 'Sending...' : 'Send OTP'}
      </button>
      <button type="button" className="btn btn-secondary" onClick={onBack} disabled={loading}>
        Back to Login
      </button>
    </form>
  );
};

export default ForgotPasswordForm;
