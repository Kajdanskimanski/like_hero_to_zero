const sourceSelect = document.getElementById('source');
const countrySelect = document.getElementById('country');
const yearSelect = document.getElementById('year');

function loadCountries(source, selectedCountry = null) {
    countrySelect.innerHTML = '<option value="">-- Land auswählen --</option>';
    yearSelect.innerHTML = '<option value="">-- Jahr auswählen --</option>';

    if (!source) return;

    fetch(`/api/countries?source=${encodeURIComponent(source)}`)
        .then(res => res.json())
        .then(countries => {
            countries.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c;
                opt.textContent = c;
                if (c === selectedCountry) opt.selected = true;
                countrySelect.appendChild(opt);
            });

            // Wenn Land vorausgewählt → Jahre direkt laden
            if (selectedCountry && countries.includes(selectedCountry)) {
                loadYears(source, selectedCountry);
            }
        });
}

function loadYears(source, country, selectedYear = null) {
    yearSelect.innerHTML = '<option value="">-- Jahr auswählen --</option>';

    if (!source || !country) return;

    fetch(`/api/years?source=${encodeURIComponent(source)}&country=${encodeURIComponent(country)}`)
        .then(res => res.json())
        .then(years => {
            years.forEach(y => {
                const opt = document.createElement('option');
                opt.value = y;
                opt.textContent = y;
                if (y == selectedYear) opt.selected = true;
                yearSelect.appendChild(opt);
            });
        });
}

// Beim Laden der Seite
window.addEventListener('DOMContentLoaded', function () {
    const source = sourceSelect.value;
    const country = countrySelect.value || null;
    const year = yearSelect.value || null;

    if (source) {
        loadCountries(source, country);
        if (country) loadYears(source, country, year);
    }
});

// Quelle wechselt → Länder neu laden
sourceSelect.addEventListener('change', function () {
    loadCountries(this.value);
});

// Land wechselt → Jahre neu laden
countrySelect.addEventListener('change', function () {
    loadYears(sourceSelect.value, this.value);
});