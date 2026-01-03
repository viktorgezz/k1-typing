import { createRouter, createWebHistory } from 'vue-router'
import { hasValidTokens } from '@/services/tokenStorage'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guest: true },
    },
  ],
})

// Navigation guard для проверки аутентификации
router.beforeEach((to, _from, next) => {
  // Проверяем токены через единый сервис
  const isAuthenticated = hasValidTokens()

  // Если маршрут требует аутентификации
  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'login' })
    return
  }

  // Если пользователь аутентифицирован и пытается зайти на страницу для гостей
  if (to.meta.guest && isAuthenticated) {
    next({ name: 'home' })
    return
  }

  next()
})

export default router
