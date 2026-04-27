<template>
  <div>
    <div class="live2d-box" v-show="show">
      <div class="live2d-tools">
        <span @click="switchModel" title="切换模型">🔄</span>
        <span @click="show = false" title="隐藏">✕</span>
      </div>
    </div>
    <div v-if="!show" class="live2d-fab" @click="restart">
      <i class="fa fa-female"></i>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const show = ref(true)

const models = [
  'https://unpkg.com/live2d-widget-model-shizuku@1.0.5/assets/shizuku.model.json',
  'https://unpkg.com/live2d-widget-model-hijiki@1.0.5/assets/hijiki.model.json',
  'https://unpkg.com/live2d-widget-model-haru@1.0.5/assets/haru.model.json',
  'https://unpkg.com/live2d-widget-model-z16@1.0.5/assets/z16.model.json'
]

let idx = 0

function start() {
  if (typeof window.L2Dwidget === 'undefined') {
    setTimeout(start, 1000)
    return
  }
  window.L2Dwidget.init({
    model: { jsonPath: models[idx] },
    display: { position: 'right', width: 150, height: 300, hOffset: 0, vOffset: -40 },
    mobile: { show: true, scale: 0.5 },
    react: { opacity: 0.85 },
    dialog: { enable: false }
  })
}

function switchModel() {
  idx = (idx + 1) % models.length
  const el = document.getElementById('live2d-widget')
  if (el) el.remove()
  setTimeout(start, 200)
}

function restart() {
  show.value = true
  start()
}

onMounted(() => {
  setTimeout(start, 500)
})

onBeforeUnmount(() => {
  const el = document.getElementById('live2d-widget')
  if (el) el.remove()
})
</script>

<style scoped>
.live2d-box {
  position: fixed;
  right: 0;
  bottom: 0;
  z-index: 999;
  pointer-events: none;
}
.live2d-box :deep(canvas) {
  pointer-events: auto;
}
.live2d-tools {
  position: absolute;
  top: 10px;
  right: 40px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
  z-index: 10;
}
.live2d-box:hover .live2d-tools {
  opacity: 1;
  pointer-events: auto;
}
.live2d-tools span {
  width: 22px; height: 22px; border-radius: 50%;
  background: rgba(0,0,0,0.4); color: #fff;
  font-size: 11px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  line-height: 1; transition: background 0.2s;
}
.live2d-tools span:hover { background: #ff5c72; }

.live2d-fab {
  position: fixed;
  right: 20px; bottom: 20px; z-index: 999;
  width: 44px; height: 44px; border-radius: 50%;
  background: rgba(30,30,40,0.7); color: #fff;
  font-size: 18px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.3s;
}
.live2d-fab:hover { background: #ff5c72; transform: scale(1.1); }
</style>
