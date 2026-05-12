<template>
  <div class="welcome-container">
    <div class="welcome-card">
      <h1>¡BIENVENIDO, GUILLERMO!</h1>
      <p>Selecciona una opción en el menú lateral para comenzar.</p>
      
      <div class="stats-overview">
        <div class="stat-box">
          <span>VENTAS DEL DÍA</span>
          <strong>$ 0.00</strong>
        </div>
        <div class="stat-box">
          <span>PEDIDOS ACTIVOS</span>
          <strong>0</strong>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.welcome-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
}
.welcome-card {
  background: white;
  border: 4px solid black;
  border-radius: 25px;
  padding: 40px;
  text-align: center;
  box-shadow: 10px 10px 0px rgba(0,0,0,0.1);
}
.stats-overview {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}
.stat-box {
  background: var(--amarillo-may);
  padding: 20px;
  border: 3px solid black;
  border-radius: 15px;
  flex: 1;
}
.stat-box strong { font-size: 1.5rem; display: block; }
</style>