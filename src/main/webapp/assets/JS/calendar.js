document.addEventListener("DOMContentLoaded", function () {
    const dateInputs = document.querySelectorAll(".date-picker input");
    dateInputs.forEach(input => {
        const flatpickrInstance = flatpickr(input, {
            dateFormat: "d/m/Y",
            locale: "pt",
            disableMobile: true, 
            allowInput: true,
            onChange: function (selectedDates, dateStr) {
                input.value = dateStr; 
            },
        });

        const cleave = new Cleave(input, {
            date: true,
            datePattern: ["d", "m", "Y"],
            delimiter: "/",
        });

        const calendarIcon = input.parentElement.querySelector(".calendar-icon");
        if (calendarIcon) {
            calendarIcon.addEventListener("click", function () {
                flatpickrInstance.open(); 
            });
        }
    });
});