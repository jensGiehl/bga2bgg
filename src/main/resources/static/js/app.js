(function () {
    "use strict";

    const input = document.getElementById("bgaInput");
    const output = document.getElementById("bggOutput");
    const themeSelect = document.getElementById("themeSelect");
    const copyButton = document.getElementById("copyButton");
    const clearButton = document.getElementById("clearButton");
    const errorAlert = document.getElementById("errorAlert");
    const errorMessage = document.getElementById("errorMessage");

    const DEBOUNCE_MS = 150;
    let debounceTimer = null;

    async function convert() {
        try {
            const response = await fetch("/api/convert", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ text: input.value, theme: themeSelect.value })
            });
            if (!response.ok) {
                return;
            }
            const data = await response.json();
            if (data.error) {
                showError(data.error);
            } else {
                clearError();
                output.value = data.output;
            }
        } catch (error) {
            /* Network hiccups simply leave the previous preview untouched. */
        }
    }

    function showError(message) {
        output.value = "";
        errorMessage.textContent = message;
        errorAlert.classList.remove("d-none");
    }

    function clearError() {
        errorMessage.textContent = "";
        errorAlert.classList.add("d-none");
    }

    function scheduleConvert() {
        window.clearTimeout(debounceTimer);
        debounceTimer = window.setTimeout(convert, DEBOUNCE_MS);
    }

    async function copyOutput() {
        if (!output.value) {
            return;
        }
        await navigator.clipboard.writeText(output.value);
        flashCopied();
    }

    function flashCopied() {
        const label = copyButton.querySelector("span");
        const original = copyButton.dataset.copyLabel;
        label.textContent = copyButton.dataset.copiedLabel;
        copyButton.classList.replace("btn-primary", "btn-success");
        window.setTimeout(() => {
            label.textContent = original;
            copyButton.classList.replace("btn-success", "btn-primary");
        }, 1200);
    }

    function clearInput() {
        input.value = "";
        output.value = "";
        clearError();
        input.focus();
    }

    input.addEventListener("input", scheduleConvert);
    themeSelect.addEventListener("change", convert);
    copyButton.addEventListener("click", copyOutput);
    clearButton.addEventListener("click", clearInput);

    // Convert once on load in case the browser restored earlier input.
    if (input.value.trim()) {
        convert();
    }
})();
