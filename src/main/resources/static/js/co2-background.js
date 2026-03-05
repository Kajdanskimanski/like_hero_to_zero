(function () {
    const canvas = document.getElementById('co2-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    let particles = [];

    function randomBetween(a, b) { return a + Math.random() * (b - a); }

    function initParticles() {
        particles = Array.from({ length: 80 }, () => ({
            x: randomBetween(0, canvas.width),
            y: randomBetween(0, canvas.height),
            r: randomBetween(4, 18),
            speed: randomBetween(0.2, 0.7),
            opacity: randomBetween(0.05, 0.25),
            wobble: randomBetween(0, Math.PI * 2),
            wobbleSpeed: randomBetween(0.005, 0.02),
            wobbleRange: randomBetween(0.3, 1.5),
        }));
    }

    function resize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
        initParticles();
    }

    function isDark() {
        return document.documentElement.getAttribute('data-theme') === 'dark';
    }

    function drawCO2(x, y, r, opacity) {
        const dark = isDark();
        const color = dark ? `rgba(100,160,255,${opacity})` : `rgba(255,255,255,${opacity})`;
        const bond = r * 2.2;
        ctx.save();

        ctx.beginPath(); ctx.arc(x - bond, y, r * 0.75, 0, Math.PI * 2);
        ctx.fillStyle = color; ctx.fill();

        ctx.beginPath(); ctx.arc(x, y, r, 0, Math.PI * 2);
        ctx.fillStyle = color; ctx.fill();

        ctx.beginPath(); ctx.arc(x + bond, y, r * 0.75, 0, Math.PI * 2);
        ctx.fillStyle = color; ctx.fill();

        const lineColor = dark ? `rgba(100,160,255,${opacity * 0.8})` : `rgba(255,255,255,${opacity * 0.8})`;
        ctx.strokeStyle = lineColor;
        ctx.lineWidth = r * 0.22;

        [-r * 0.25, r * 0.25].forEach(offset => {
            ctx.beginPath(); ctx.moveTo(x - bond + r * 0.75, y + offset);
            ctx.lineTo(x - r, y + offset); ctx.stroke();
            ctx.beginPath(); ctx.moveTo(x + r, y + offset);
            ctx.lineTo(x + bond - r * 0.75, y + offset); ctx.stroke();
        });

        ctx.fillStyle = dark ? `rgba(180,210,255,${opacity * 1.5})` : `rgba(255,255,255,${opacity * 1.8})`;
        ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
        ctx.font = `bold ${r * 0.9}px sans-serif`; ctx.fillText('C', x, y);
        ctx.font = `bold ${r * 0.7}px sans-serif`;
        ctx.fillText('O', x - bond, y); ctx.fillText('O', x + bond, y);
        ctx.restore();
    }

    function draw() {
        const { width, height } = canvas;
        const dark = isDark();
        const grad = ctx.createLinearGradient(0, 0, width, height);
        if (dark) {
            grad.addColorStop(0, '#0f1623');
            grad.addColorStop(1, '#1a2035');
        } else {
            grad.addColorStop(0, '#667eea');
            grad.addColorStop(1, '#764ba2');
        }
        ctx.fillStyle = grad;
        ctx.fillRect(0, 0, width, height);

        particles.forEach(p => {
            p.wobble += p.wobbleSpeed;
            p.x += Math.sin(p.wobble) * p.wobbleRange;
            p.y -= p.speed;
            if (p.y + p.r < 0) { p.y = height + p.r; p.x = randomBetween(0, width); }
            drawCO2(p.x, p.y, p.r, p.opacity);
        });

        requestAnimationFrame(draw);
    }

    window.addEventListener('resize', resize);
    resize();
    draw();
})();