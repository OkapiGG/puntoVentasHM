<template>
  <div class="dashboard-layout">
    <Navbar @toggle-menu="sidebarOpen = true" />
    <Sidebar :isOpen="sidebarOpen" @close="sidebarOpen = false" />

    <main class="main-content">
      <router-view /> 
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Navbar from '@/components/Navbar.vue'
import Sidebar from '@/components/Sidebar.vue'
const sidebarOpen = ref(false)
</script>

<style scoped>
.dashboard-layout {
  height: 100vh; /* Ocupa el 100% de la ventana, ni un pixel más */
  display: flex;
  flex-direction: column;
  overflow: hidden; /* Refuerzo para que nada salga de aquí */
}

.main-content {
  flex: 1;
  /* Reducimos el padding de 20px a algo más sutil o específico */
  padding: 10px 40px; 
  display: flex;
  justify-content: center;
  /* Cambiamos center por flex-start para que aproveche el espacio superior */
  align-items: flex-start; 
  background-color: #FDFBE2;
  overflow: hidden;
}
</style>