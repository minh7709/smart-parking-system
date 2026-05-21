import { NotificationContainer } from "./components/Notification/NotificationContainer";
import AppRoutes from "./routes/AppRoutes";
import { AuthProvider } from "./context/AuthContext";
function App() {
  return (
    <>
      <NotificationContainer />
      <AuthProvider>
        <AppRoutes />
    </AuthProvider>
    </>
  );
}

export default App;
