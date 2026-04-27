export function initClickEffect() {
  document.addEventListener('click', (e) => {
    const emojis = ['🌸', '✨', '💜', '🩷', '⭐', '🎀']
    spawnParticles(e.clientX, e.clientY, emojis, 6, 800)
  })
}

export function triggerDeleteEffect(el) {
  const rect = el.getBoundingClientRect()
  const x = rect.left + rect.width * (0.2 + Math.random() * 0.6)
  const y = rect.top + rect.height * (0.3 + Math.random() * 0.4)
  const emojis = ['💫', '✨', '🫧', '💨', '·']
  spawnParticles(x, y, emojis, 3, 500)
}

function spawnParticles(cx, cy, emojis, count, duration) {
  for (let i = 0; i < count; i++) {
    const el = document.createElement('span')
    el.textContent = emojis[Math.floor(Math.random() * emojis.length)]
    const size = 12 + Math.random() * 8
    el.style.cssText = `
      position: fixed; left: ${cx}px; top: ${cy}px;
      font-size: ${size}px; pointer-events: none; z-index: 99999;
      transform: translate(0, 0); opacity: 1;
      transition: all ${duration * 0.001}s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    `
    document.body.appendChild(el)
    const angle = (Math.PI * 2 * i) / count + (Math.random() - 0.5) * 0.8
    const dist = 20 + Math.random() * 30
    requestAnimationFrame(() => {
      el.style.transform = `translate(${Math.cos(angle) * dist}px, ${Math.sin(angle) * dist - 15}px)`
      el.style.opacity = '0'
    })
    setTimeout(() => el.remove(), duration)
  }
}
