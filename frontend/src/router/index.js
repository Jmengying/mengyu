import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/resources', name: 'Resources', component: () => import('../views/Resources.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/articles/create', name: 'CreateArticle', component: () => import('../views/CreateArticle.vue') },
  { path: '/articles/:id', name: 'ArticleDetail', component: () => import('../views/ArticleDetail.vue') },
  { path: '/articles/:id/edit', name: 'EditArticle', component: () => import('../views/CreateArticle.vue') },
  { path: '/profile', name: 'Profile', component: () => import('../views/UserProfile.vue') },
  { path: '/user/:id', name: 'UserHome', component: () => import('../views/UserHome.vue') },
  { path: '/admin', name: 'Admin', component: () => import('../views/Admin.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  const needAuth = ['CreateArticle', 'EditArticle', 'Profile'].includes(to.name)
  if (needAuth && !token) {
    next('/login')
  } else if (to.name === 'Admin') {
    if (!token) {
      next('/login')
    } else if (user.role !== 'admin') {
      next('/')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
