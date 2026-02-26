// Theme Toggle Funktionalität
(function() {
    // Theme aus localStorage laden oder System-Präferenz verwenden
    const initTheme = () => {
        const savedTheme = localStorage.getItem('theme');
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

        if (savedTheme) {
            document.documentElement.setAttribute('data-theme', savedTheme);
        } else if (prefersDark) {
            document.documentElement.setAttribute('data-theme', 'dark');
        }

        updateToggleIcon();
    };

    // Theme wechseln
    const toggleTheme = () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);

        updateToggleIcon();
    };

    // Icon aktualisieren
    const updateToggleIcon = () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const toggleBtn = document.getElementById('theme-toggle');

        if (toggleBtn) {
            if (currentTheme === 'dark') {
                toggleBtn.innerHTML = '<span class="theme-toggle-icon">☀️</span>';
                toggleBtn.title = 'Zu hellem Modus wechseln';
            } else {
                toggleBtn.innerHTML = '<span class="theme-toggle-icon">🌙</span>';
                toggleBtn.title = 'Zu dunklem Modus wechseln';
            }
        }
    };

    // Event Listener für Toggle Button
    const setupToggleButton = () => {
        const toggleBtn = document.getElementById('theme-toggle');
        if (toggleBtn) {
            toggleBtn.addEventListener('click', toggleTheme);
        }
    };

    // Bei Seitenladung initialisieren
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            initTheme();
            setupToggleButton();
        });
    } else {
        initTheme();
        setupToggleButton();
    }
})();