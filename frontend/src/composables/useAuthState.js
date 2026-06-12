import { ref } from 'vue';

export function useAuthState(options = {}) {
  const isLoggedIn = ref(false);
  const showGameStart = ref(true);
  const username = ref('');

  const initAuthFromStorage = () => {
    const savedUsername = localStorage.getItem('username');
    if (savedUsername) {
      username.value = savedUsername;
      isLoggedIn.value = true;
      showGameStart.value = true;
    }
  };

  const handleLoginSuccess = (user) => {
    username.value = user;
    isLoggedIn.value = true;
    showGameStart.value = true;
    localStorage.setItem('username', user);
  };

  const handleLogout = () => {
    isLoggedIn.value = false;
    showGameStart.value = true;
    username.value = '';
    localStorage.removeItem('username');
    if (options.onLogout) {
      options.onLogout();
    }
  };

  const handleBackToMenu = () => {
    showGameStart.value = true;
    if (options.onBackToMenu) {
      options.onBackToMenu();
    }
  };

  return {
    isLoggedIn,
    showGameStart,
    username,
    initAuthFromStorage,
    handleLoginSuccess,
    handleLogout,
    handleBackToMenu
  };
}
