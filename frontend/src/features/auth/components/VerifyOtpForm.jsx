const VerifyOtpForm = ({ otp, loading, error, otpError, onOtpChange, onSubmit, onBack }) => {
  return (
    <form onSubmit={onSubmit}>
      <h1>Verify OTP</h1>

      {error && <div className="error-message">{error}</div>}
      {otpError && <div className="error-message">{otpError}</div>}

      <div className="input-box">
        <input
          type="text"
          placeholder="Enter 6-digit OTP"
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

      <button type="submit" className="btn" disabled={loading}>
        {loading ? 'Verifying...' : 'Verify OTP'}
      </button>
      <button type="button" className="btn btn-secondary" onClick={onBack} disabled={loading}>
        Back
      </button>
    </form>
  );
};

export default VerifyOtpForm;
