document.addEventListener("DOMContentLoaded", function () {
    // Configuração do Flatpickr para todos os campos de data
    const dateInputs = document.querySelectorAll(".date-picker input");

    dateInputs.forEach(input => {
        const flatpickrInstance = flatpickr(input, {
            dateFormat: "d/m/Y", // Formato da data
            locale: "pt", // Usa o locale "pt"
            disableMobile: true, // Desativa o seletor nativo em dispositivos móveis
            allowInput: true, // Permite digitação manual
            onChange: function (selectedDates, dateStr) {
                input.value = dateStr; // Atualiza o valor do campo com a data selecionada
            },
        });

        // Configuração do Cleave.js para máscara de data
        const cleave = new Cleave(input, {
            date: true,
            datePattern: ["d", "m", "Y"],
            delimiter: "/",
        });

        // Adiciona um evento de clique ao ícone do calendário
        const calendarIcon = input.parentElement.querySelector(".calendar-icon");
        if (calendarIcon) {
            calendarIcon.addEventListener("click", function () {
                flatpickrInstance.open(); // Abre o calendário ao clicar no ícone
            });
        }
    });
});