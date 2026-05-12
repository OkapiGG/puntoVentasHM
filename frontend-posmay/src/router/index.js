import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import { ROLES, tieneAlgunRol } from '../utils/permisos'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView,
      meta: { public: true }
    },
    {
      path: '/dashboard',
      component: DashboardView,
      children: [
        {
          path: '', // Al entrar a /dashboard (Pantalla de bienvenida)
          name: 'principal',
          component: () => import('../components/PrincipalView.vue'),
          meta: { roles: [ROLES.ADMIN, ROLES.GERENTE, ROLES.CAJERO, ROLES.REPARTIDOR] }
        },
        {
          path: 'menu', // Al entrar a /dashboard/menu (Vista de las 8 categorías)
          name: 'menu-categorias',
          component: () => import('../components/CategoriasMenu.vue'),
          meta: { roles: [ROLES.ADMIN, ROLES.GERENTE, ROLES.CAJERO, ROLES.REPARTIDOR] }
        },
        {
          /* RUTA DINÁMICA: 
             El :categoria permite que la URL sea /dashboard/menu/hamburguesas 
             o /dashboard/menu/bebidas usando el mismo componente.
          */
          path: 'menu/:categoria', 
          name: 'productos-categoria',
          component: () => import('../components/ProductosGrid.vue'),
          props: true,
          meta: { roles: [ROLES.ADMIN, ROLES.GERENTE, ROLES.CAJERO, ROLES.REPARTIDOR] }
        },
        {
          path: 'ventas',
          name: 'ventas',
          component: () => import('../components/VentasView.vue'),
          meta: { roles: [ROLES.ADMIN, ROLES.GERENTE, ROLES.CAJERO, ROLES.REPARTIDOR] }
        },
        {
          path: 'caja',
          name: 'caja',
          component: () => import('../components/CajaView.vue'),
          meta: { roles: [ROLES.ADMIN, ROLES.GERENTE, ROLES.CAJERO] }
        },
        {
          path: 'estadisticas',
          name: 'estadisticas',
          component: () => import('../components/EstadisticasView.vue'),
          meta: { roles: [ROLES.ADMIN, ROLES.GERENTE] }
        },
        {
          path: 'inventario',
          name: 'inventario',
          component: () => import('../components/InventarioView.vue'),
          meta: { roles: [ROLES.ADMIN, ROLES.GERENTE] }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  if (to.meta.public) {
    next()
    return
  }

  const raw = sessionStorage.getItem('usuarioActual')
  if (!raw) {
    next('/')
    return
  }

  const requiredRoles = to.matched.flatMap(record => record.meta?.roles || [])
  if (requiredRoles.length && !tieneAlgunRol(requiredRoles)) {
    next('/dashboard')
    return
  }

  next()
})

export default router
