/**
 * 全局轻量交互：表单提交态、可访问性增强
 */
(function () {
  'use strict';

  function ready(fn) {
    if (document.readyState !== 'loading') fn();
    else document.addEventListener('DOMContentLoaded', fn);
  }

  ready(function () {
    document.documentElement.classList.add('js');

    document.querySelectorAll('form').forEach(function (form) {
      form.addEventListener('submit', function () {
        var btn = form.querySelector('button[type="submit"], input[type="submit"]');
        if (!btn || btn.disabled) return;
        btn.classList.add('btn--loading');
        btn.setAttribute('aria-busy', 'true');
        btn.setAttribute('disabled', '');
        form.setAttribute('data-submitting', '1');
      });
    });
  });
})();
