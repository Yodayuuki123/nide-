/* Module Slideshow - auto-init all .module-slideshow elements */
(function() {
    'use strict';

    /* Inject CSS directly so it always works regardless of stylesheet loading */
    var css = '.module-slideshow{position:relative;width:100%;height:100%;overflow:hidden}' +
        '.module-slideshow img{position:absolute;top:0;left:0;width:100%;height:100%;object-fit:cover;opacity:0;transition:opacity 1.0s ease}' +
        '.module-slideshow img.ss-active{opacity:1;position:relative}' +
        '.module-slideshow .ss-arrows{position:absolute;top:0;left:0;right:0;bottom:0;display:flex;align-items:center;justify-content:space-between;padding:0 8px;pointer-events:none;opacity:0;transition:opacity 0.3s ease;z-index:2}' +
        '.module-slideshow:hover .ss-arrows{opacity:1}' +
        '.module-slideshow .ss-arrow{width:36px;height:36px;border-radius:50%;background:rgba(255,255,255,0.85);border:none;cursor:pointer;display:flex;align-items:center;justify-content:center;pointer-events:auto;font-size:14px;color:#333;transition:all 0.3s ease;box-shadow:0 2px 8px rgba(0,0,0,0.15)}' +
        '.module-slideshow .ss-arrow:hover{background:#fff;transform:scale(1.1)}' +
        '.module-slideshow .ss-dots{position:absolute;bottom:10px;left:50%;transform:translateX(-50%);display:flex;gap:6px;z-index:2}' +
        '.module-slideshow .ss-dot{width:6px;height:6px;border-radius:50%;background:rgba(255,255,255,0.5);border:none;padding:0;cursor:pointer;transition:all 0.3s ease}' +
        '.module-slideshow .ss-dot.active{background:#fff;transform:scale(1.3)}';
    var style = document.createElement('style');
    style.textContent = css;
    document.head.appendChild(style);

    function initSlideshows() {
        var slideshows = document.querySelectorAll('.module-slideshow');
        if (!slideshows.length) return;

        slideshows.forEach(function(ss) {
            var imgs = ss.querySelectorAll('img');
            if (imgs.length <= 1) { if (imgs[0]) imgs[0].classList.add('ss-active'); return; }
            if (ss.querySelector('.ss-active')) return;

            imgs[0].classList.add('ss-active');

            var arrowsDiv = document.createElement('div');
            arrowsDiv.className = 'ss-arrows';
            arrowsDiv.innerHTML = '<button class="ss-arrow ss-prev" aria-label="Previous"><i class="bi bi-chevron-left"></i></button><button class="ss-arrow ss-next" aria-label="Next"><i class="bi bi-chevron-right"></i></button>';
            ss.appendChild(arrowsDiv);

            var dotsDiv = document.createElement('div');
            dotsDiv.className = 'ss-dots';
            for (var i = 0; i < imgs.length; i++) {
                var dot = document.createElement('button');
                dot.className = 'ss-dot' + (i === 0 ? ' active' : '');
                dot.setAttribute('data-idx', i);
                dotsDiv.appendChild(dot);
            }
            ss.appendChild(dotsDiv);

            var current = 0;
            var total = imgs.length;
            var autoTimer = null;

            function goTo(idx) {
                if (idx === current) return;
                imgs[current].classList.remove('ss-active');
                dotsDiv.children[current].classList.remove('active');
                current = ((idx % total) + total) % total;
                imgs[current].classList.add('ss-active');
                dotsDiv.children[current].classList.add('active');
            }

            arrowsDiv.querySelector('.ss-prev').addEventListener('click', function(e) {
                e.stopPropagation();
                goTo(current - 1);
                resetAuto();
            });
            arrowsDiv.querySelector('.ss-next').addEventListener('click', function(e) {
                e.stopPropagation();
                goTo(current + 1);
                resetAuto();
            });
            dotsDiv.addEventListener('click', function(e) {
                var dot = e.target.closest('.ss-dot');
                if (dot) {
                    e.stopPropagation();
                    goTo(parseInt(dot.getAttribute('data-idx'), 10));
                    resetAuto();
                }
            });

            var touchX = 0;
            ss.addEventListener('touchstart', function(e) {
                touchX = e.changedTouches[0].clientX;
            }, { passive: true });
            ss.addEventListener('touchend', function(e) {
                var dx = e.changedTouches[0].clientX - touchX;
                if (Math.abs(dx) > 40) {
                    goTo(dx < 0 ? current + 1 : current - 1);
                    resetAuto();
                }
            }, { passive: true });

            function startAuto() {
                autoTimer = setInterval(function() { goTo(current + 1); }, 7000);
            }
            function resetAuto() {
                clearInterval(autoTimer);
                startAuto();
            }
            startAuto();

            ss.addEventListener('mouseenter', function() { clearInterval(autoTimer); });
            ss.addEventListener('mouseleave', function() { startAuto(); });
        });
    }

    // Run when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initSlideshows);
    } else {
        initSlideshows();
    }
})();
