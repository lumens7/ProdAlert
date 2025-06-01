document.addEventListener("DOMContentLoaded", function () {
    function typeEffect(element, text, speed = 100) {
        let index = 0;
        element.textContent = ""; 
        function type() {
            if (index < text.length) {
                element.textContent += text[index];
                index++;
                setTimeout(type, speed);
            }
        }
        type();
    }
    document.querySelectorAll(".typing-effect").forEach((element) => {
        const text = element.getAttribute("data-text");
        if (text) {
            typeEffect(element, text);
        }
    });
});
