<template>
  <section class="menu-container">
    <div class="categorias-header">
      <div class="title-section">
        <h1>CATEGORIAS</h1>
        <div class="underline"></div>
      </div>
      
      <div class="nav-buttons">
        <button class="nav-btn">ANT</button>
        <button class="nav-btn shadow-orange">
          <img src="@/assets/icons/cart.png" alt="carrito" />
          <span class="badge">5</span>
        </button>
        <button class="nav-btn">SIG</button>
      </div>
    </div>

    <div class="grid-categorias">
      <button class="btn-add">
        <span>+</span>
      </button>

      <button 
        v-for="cat in listaCategorias" 
        :key="cat.id" 
        class="categoria-card"
      >
        {{ cat.nombre }}
      </button>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

const listaCategorias = ref([
  { id: 1, nombre: 'HAMBURGUESAS CLASICAS' },
  { id: 2, nombre: 'HAMBURGUESAS ESPECIALES' },
  { id: 3, nombre: 'COMPLEMENTOS' },
  { id: 4, nombre: 'PROMOS' },
  { id: 5, nombre: 'BEBIDAS' },
  { id: 6, nombre: 'SERVICIO' },
  { id: 7, nombre: 'POSTRES' },
])
</script>