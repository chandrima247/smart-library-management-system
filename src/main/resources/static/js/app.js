/* SLMS shared front-end behaviour. Kept intentionally small — the UI is
   server-rendered; JS only adds neobrutalist micro-interactions and a couple of
   progressive enhancements. */
(function () {
    "use strict";

    // Mobile sidebar toggle.
    document.addEventListener("click", function (e) {
        const toggle = e.target.closest("[data-sidebar-toggle]");
        if (toggle) {
            const sidebar = document.getElementById("sidebar");
            if (sidebar) sidebar.classList.toggle("-translate-x-full");
        }
        const dismiss = e.target.closest("[data-dismiss]");
        if (dismiss) {
            const el = dismiss.closest("[data-dismissable]");
            if (el) el.remove();
        }
    });

    // Confirm before destructive form submits (data-confirm="message").
    document.addEventListener("submit", function (e) {
        const form = e.target;
        const message = form.getAttribute("data-confirm");
        if (message && !window.confirm(message)) {
            e.preventDefault();
        }
    });

    // Auto-dismiss flash banners after a few seconds.
    document.querySelectorAll("[data-autohide]").forEach(function (el) {
        setTimeout(function () {
            el.style.transition = "opacity .4s ease";
            el.style.opacity = "0";
            setTimeout(() => el.remove(), 400);
        }, 5000);
    });
})();
