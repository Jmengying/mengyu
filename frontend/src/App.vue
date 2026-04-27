<template>
  <div class="app-container">
    <div class="loading-bar" ref="barRef" :class="{ show: loading }"></div>
    <Navbar />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <Transition name="route-fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </router-view>
    </main>
    <Live2d />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from './components/Navbar.vue'
import Live2d from './components/Live2d.vue'

const router = useRouter()
const loading = ref(false)

const barRef = ref(null)

router.beforeEach(() => {
  loading.value = true
  setTimeout(() => {
    if (barRef.value) barRef.value.style.width = '85%'
  }, 50)
})
router.afterEach(() => {
  if (barRef.value) {
    barRef.value.style.width = '100%'
    setTimeout(() => {
      loading.value = false
      if (barRef.value) barRef.value.style.width = '0'
    }, 400)
  } else {
    loading.value = false
  }
})
router.onError(() => { loading.value = false })
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

/* Top loading bar */
.loading-bar {
  position: fixed;
  top: 0;
  left: 0;
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, #ff5c72, #D87CFF, #f093fb, #ff5c72);
  background-size: 200% 100%;
  z-index: 10001;
  transition: width 0.3s ease, opacity 0.3s;
  opacity: 0;
}

.loading-bar.show {
  width: 80%;
  opacity: 1;
  animation: shimmer 1s linear infinite;
}

@keyframes shimmer {
  0% { background-position: 0% 0; }
  100% { background-position: 200% 0; }
}

/* Cute bounce route transition */
.route-fade-enter-active {
  transition: all 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.route-fade-leave-active {
  transition: all 0.25s ease-in;
}

.route-fade-enter-from {
  opacity: 0;
  transform: scale(0.92) translateY(20px);
}

.route-fade-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-10px);
}
</style>
